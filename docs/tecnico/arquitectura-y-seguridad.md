# Arquitectura y seguridad

Cómo está organizado el código, qué guarda la base de datos, quién puede entrar a cada ruta y qué controles de seguridad tiene la aplicación. Refleja `main` al cierre del Sprint 6 (commit `7b213ac`).

## Capas (arquitectura MVC)

Una petición recorre siempre el mismo camino:

```
Navegador → Spring Security (SecurityConfig) → Controlador → Servicio → Repositorio → PostgreSQL
                                                    ↓
                                     Vista Thymeleaf (templates/**/*.html)
```

| Capa | Responsabilidad | Clases |
|---|---|---|
| Seguridad | Decide quién entra a cada ruta, antes de llegar al controlador | `security/SecurityConfig`, `CustomUserDetailsService` |
| Controlador | Recibe la petición, valida el formulario y elige la vista | `TituloController`, `AuditoriaController`, `VerificacionController`, `HomeController` |
| Formulario | Solo los campos que el usuario puede enviar | `TituloForm` |
| Servicio | Reglas de negocio y transacciones | `TituloService`, `AuditoriaService` (registra eventos), `AuditoriaConsultaService` (los consulta), `CredencialPdfService` (PDF y QR), `VerificacionService` (verificación pública y enmascarado) |
| Validación | Reglas de cada dato, reutilizables en las pruebas | `TituloValidator`, `GraduadoValidator`, `TituloValidationException` |
| Repositorio | Acceso a la base de datos (Spring Data JPA) | `TituloRepository`, `repository/*` |
| Entidad | Tablas de la base de datos | `Titulo`, `entity/*` |
| Vista | HTML generado en el servidor | `templates/fragments/encabezado.html`, `templates/titulos/*.html`, `templates/auditoria.html` y las páginas públicas `templates/verificacion/*.html` (con su hoja `static/css/verificacion.css`) |
| Datos iniciales y configuración | Usuarios que se crean al arrancar y el reloj de la aplicación (las pruebas lo reemplazan por uno fijo) | `config/DataInitializer`, `config/UsuarioMonitoreoInitializer`, `config/RelojConfig` |

`security/UsuarioActualAdvice` agrega a todas las pantallas el usuario con sesión y el contador de pendientes del menú, para no repetirlo en cada controlador.

## Modelo de datos

```
graduados 1 ──── N titulo           usuarios           eventos_auditoria
(documento único)   (estado, revisión,  (username, rol)    (quién, qué, cuándo;
                     identificador)                          solo se agregan)
```

| Tabla | Campos principales | Reglas |
|---|---|---|
| `graduados` | `tipo_documento` (CEDULA o PASAPORTE), `numero_documento` (20), `nombre_completo` (150) | Tipo + número únicos (`uk_graduado_documento`): un graduado no se repite |
| `titulo` | `graduado_id`, `nombre_graduado` (120), `nombre_titulo` (150), `fecha_registro`, `estado`, `revisado_por`, `fecha_revision`, `identificador` (100) | Un graduado no puede tener dos veces el mismo título (lo controla `TituloService`, no la base de datos). El `identificador` (UUID) se asigna al aprobar |
| `usuarios` | `username` (único), `password` (BCrypt), `rol` | Roles: REGISTRADOR, APROBADOR, MONITOREO |
| `eventos_auditoria` | `fecha_hora`, `accion` (REGISTRAR, APROBAR o RECHAZAR), `usuario` (50), `titulo_id`, `detalle` (500) | Ninguna columna se puede actualizar. El repositorio hereda de `Repository` y solo tiene `save` y una consulta de lectura con filtros (`@Query`, HU-3.4); no hereda de `JpaRepository` ni de `JpaSpecificationExecutor`, que traen métodos para borrar y editar |

**Estados de un título:** `PENDIENTE` → `APROBADO` o `RECHAZADO`. Solo un título PENDIENTE se puede revisar, y solo uno APROBADO tiene credencial.

**Ojo con `ddl-auto=update`:** Hibernate crea tablas y columnas nuevas, pero no cambia las existentes. Además, cada enum guardado como texto (`@Enumerated(STRING)`) crea una restricción CHECK con sus valores. Si se agrega un valor a un enum (un rol, una acción), las bases locales ya creadas lo rechazan. Por eso todo cambio así necesita su entrada en «Cambios manuales en la base de datos local» del [README](../../README.md).

## Rutas y acceso

| Ruta | Método | REGISTRADOR | APROBADOR | MONITOREO | Sin sesión |
|---|---|---|---|---|---|
| `/` (lleva a `/titulos`) | GET | Sí | Sí | 403 | Inicio de sesión |
| `/titulos` (lista) | GET | Sí | Sí | 403 | Inicio de sesión |
| `/titulos/nuevo` (registrar) | GET y POST | Sí | 403 | 403 | Inicio de sesión |
| `/titulos/pendientes` (bandeja) | GET | 403 | Sí | 403 | Inicio de sesión |
| `/titulos/{id}/aprobar` y `/rechazar` | POST | 403 | Sí | 403 | Inicio de sesión |
| `/titulos/{id}/credencial` (PDF) | GET | Sí | Sí | 403 | Inicio de sesión |
| `/auditoria` (consulta de la auditoría) | GET | 403 | Sí | 403 | Inicio de sesión |
| `/actuator/**` (métricas) | GET | 403 | 403 | Sí (HTTP Basic) | 401 |
| `/verificar` y `/verificar/{código}` (verificación pública) | GET | Público | Público | Público | Público |
| `/login`, `/error`, `/favicon.ico`, `/css/verificacion.css` | — | Público | Público | Público | Público |

- En las pantallas, «403» para MONITOREO es después de iniciar sesión con el formulario. Si alguien envía HTTP Basic a una pantalla, la aplicación lo ignora y lo manda al inicio de sesión.
- La credencial de un título que no existe o no está APROBADO responde 404, para no revelar si el título existe.
- En la verificación pública, un código que no es un UUID, que no existe o de un título que no está APROBADO responde igual: 404 «No encontrado · Not found». El que no es UUID ni siquiera consulta la base de datos (HU-3.1).
- La comprobación con curl y el navegador está en [`docs/evidencias/HU-4.10-matriz-acceso.txt`](../evidencias/HU-4.10-matriz-acceso.txt) y se repitió en el Sprint 6 en [`HU-4.11-matriz-acceso.txt`](../evidencias/HU-4.11-matriz-acceso.txt) (12 de 12).

## Cómo funciona la seguridad

**Dos cadenas de filtros** (`SecurityConfig`):

1. **`/actuator/**`** (`@Order(1)`): solo el rol MONITOREO, con HTTP Basic y sin sesión. Es lo que usa Prometheus. Ningún rol del personal lee métricas (mínimo privilegio).
2. **Todo lo demás** (`@Order(2)`): inicio de sesión con formulario. Esta cadena no acepta HTTP Basic, así que una contraseña filtrada de Prometheus no sirve para entrar a las pantallas.

**Denegar por defecto** (desde HU-4.10): la última regla es `.anyRequest().hasAnyRole("REGISTRADOR", "APROBADOR")`. Una ruta sin regla propia queda solo para el personal.

**Página a la que se vuelve al iniciar sesión** (desde HU-4.11): Spring Security solo recuerda una petición GET que pide una página HTML y que no es de `/.well-known/`. Así, un archivo que el navegador pide por su cuenta (por ejemplo, el de Chrome con la consola abierta) ya no se toma como la página a la que volver, y el usuario llega a una pantalla válida.

> **Regla para rutas nuevas:** una ruta pública (por ejemplo, la verificación de HU-3.1) se declara con `permitAll()`, y una de un solo rol con su `hasRole(...)`, siempre **antes** de `anyRequest()`. El revisor del Pull Request comprueba que esté.

**Otros controles**

| Control | Cómo está hecho | Historia |
|---|---|---|
| Contraseñas | BCrypt (`BCryptPasswordEncoder`) | HU-1.2 |
| Separación de funciones | Quien registra no aprueba: cada ruta exige su rol | HU-1.9 y HU-2.1 |
| CSRF | Activo (lo trae Spring Security). Aprobar, rechazar, registrar y cerrar sesión van por POST, y Thymeleaf agrega el token | HU-1.6 y HU-2.1 |
| Asignación masiva | El formulario se enlaza a `TituloForm`, que solo tiene 4 campos. Estado, fecha, revisor e identificador los pone el servidor | HU-4.7 |
| Validación en el servidor | Anotaciones en `TituloForm` y reglas en `GraduadoValidator`; cada error se muestra en su campo | HU-4.7 |
| Revisor y autor desde la sesión | El usuario sale de `Principal`, nunca del formulario | HU-2.1 y HU-3.3 |
| Consulta de la auditoría | Solo el APROBADOR y solo por GET. Los filtros se validan en el servidor y el error se muestra bajo su campo, sin consultar la base de datos | HU-3.4 |
| Datos públicos mínimos | La verificación muestra solo graduado, tipo de documento con el número enmascarado (`8-123-4567` → `•-•••-4567`), título, institución y fechas; nunca el documento completo, el revisor ni el id interno | HU-3.1 |
| QR sin datos personales | El QR de la credencial solo lleva la dirección de verificación con el identificador | HU-2.5 |
| Auditoría | Registrar, aprobar y rechazar guardan su evento en la misma transacción: si la acción falla, no queda evento; si el evento falla, la acción se deshace | HU-3.3 |
| Cabeceras HTTP | CSP (`default-src 'self'`, `script-src 'self'`, `frame-ancestors 'none'`…), Referrer-Policy, Permissions-Policy y HSTS, en las dos cadenas | HU-4.3 |
| Secretos | Las contraseñas llegan por variables de entorno (`DB_PASSWORD`, `PROMETHEUS_PASSWORD`); nunca se escriben en el repositorio | HU-4.10 |

## Configuración

Todo está en `src/main/resources/application.properties`. Las variables de entorno están descritas en el [README](../../README.md#variables-de-entorno).

| Propiedad | Valor | Para qué |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/titulos_db` | Base de datos local |
| `spring.datasource.username` / `password` | `${DB_USERNAME:postgres}` / `${DB_PASSWORD}` | Credenciales de PostgreSQL |
| `spring.jpa.hibernate.ddl-auto` | `update` | Crea tablas y columnas nuevas (ver «Ojo» arriba) |
| `management.endpoints.web.exposure.include` | `health,metrics,info,prometheus` | Métricas que expone Actuator |
| `titulos.monitoreo.usuario` / `password` | `prometheus` / `${PROMETHEUS_PASSWORD:}` | Usuario técnico (mínimo 12 caracteres; sin la variable, no se crea) |
| `titulos.verificacion.url` | `${TITULOS_VERIFICACION_URL:http://localhost:8080/verificar}` | Dirección que imprime la credencial y que lleva su QR (HU-2.5). Para leer el QR desde un teléfono, se pone la IP de la computadora en esa red |
| `titulos.institucion.nombre` | `Universidad Tecnológica de Panamá` | Nombre que muestra la verificación pública (HU-3.1). Las tildes van escapadas en Unicode, porque Spring Boot lee el archivo como ISO-8859-1 |

Las pruebas usan `src/test/resources/application-test.properties`: H2 en memoria con `create-drop`, sin PostgreSQL ni variables.

**Usuarios de prueba:** con la tabla `usuarios` vacía, `DataInitializer` crea `registrador1` y `aprobador1`, con la contraseña `clave1234`. Son solo para desarrollo; HU-4.6 (modo demostración) los sacará de la versión oficial.

## Limitaciones conocidas

| Tema | Situación | Dónde se resuelve |
|---|---|---|
| HSTS | Nikto lo marca ausente porque la aplicación corre con HTTP; la cabecera solo se envía con HTTPS | Verificar cuando haya HTTPS |
| Páginas de error | Se ve la página genérica de Spring Boot (Whitelabel), sin detalles técnicos | HU-4.5 |
| Estilos en línea | La CSP permite `style-src 'unsafe-inline'` porque la página de inicio de sesión de Spring los usa, y `pendientes.html` tiene 2 | HU-1.5 y HU-4.9 |
| Pruebas de acceso por rol | Hay pruebas con MockMvc para la credencial, la auditoría, la verificación pública y el inicio de sesión; faltan las de todas las rutas y las cabeceras | HU-4.8 |
| Hash de integridad | La credencial y la verificación pública todavía no lo muestran | HU-2.8 |
| Error de los filtros de auditoría | `AuditoriaController` decide bajo qué campo mostrar el error buscando en el texto del mensaje. Conviene que la excepción diga el campo, como `TituloValidationException` en el registro | Sin historia; recomendado |
| Dependencia `spring-boot-h2console` | Está sin `scope` de pruebas. La consola no está activada, así que no expone nada, pero conviene moverla a `test` o quitarla | Sin historia; decidir en el equipo |
