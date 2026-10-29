# Títulos Universitarios Digitales

Aplicación web para que una universidad **registre, apruebe y emita títulos universitarios digitales**, y para que cualquier persona (un empleador u otra institución) pueda **comprobar que un título es auténtico**.

**La necesidad:** un título en papel es difícil de comprobar, sobre todo desde otro país, y un documento alterado puede pasar por bueno. La solución cubre tres requisitos del cliente:

- **Precisión:** datos validados y normalizados en el servidor, sin graduados ni títulos duplicados.
- **Seguridad:** acceso por rol, separación de funciones y un registro de auditoría que no se puede modificar.
- **Verificabilidad internacional:** credencial con identificador único, QR y hash, y una página pública bilingüe para comprobarla.

Más detalle en [visión y MVP](docs/producto/vision-y-mvp.md).

## Estado

**Cierre del Sprint 6 (29/10/2026): el MVP está completo.** 69 de 98 pts del backlog (70 %), 155 pruebas en verde y 2 hallazgos de Nikto, los mismos del sprint anterior.

| Funciona hoy | Lo que sigue (Sprint 7 en adelante) |
|---|---|
| Inicio y cierre de sesión por rol, sin error 404 aunque el navegador pida archivos automáticos | Hash de integridad en la credencial y en la verificación (HU-2.8) |
| Registro del graduado (por cédula o pasaporte) y de su título, validado en el servidor y en la base de datos | Detalle del título (HU-2.6) y búsqueda con paginación (HU-1.8) |
| Bandeja de revisión: aprobar o rechazar, con identificador único al aprobar | Catálogo oficial de programas (HU-1.11) |
| Credencial PDF con un código QR que abre su verificación pública | Revocar o sustituir un título, con confirmación (HU-3.2 y HU-2.7) |
| Verificación pública bilingüe, sin iniciar sesión y con el documento enmascarado | Página de inicio de sesión propia (HU-1.5), páginas de error (HU-4.5) y modo demostración (HU-4.6) |
| Registro de auditoría que no se puede modificar, y su consulta con filtros para el aprobador | Pruebas de acceso de todas las rutas (HU-4.8) y CSP sin estilos en línea (HU-4.9) |
| Métricas para Prometheus con un usuario técnico | |

## Stack

Java 21 · Spring Boot 4.1.1 (MVC, Security, Data JPA, Validation, Actuator) · Thymeleaf · PostgreSQL · Apache PDFBox · ZXing (QR) · JUnit 5 · CircleCI · Prometheus (Docker) · Nikto 2.6.1.

Versiones, qué instalar y cómo comprobarlo: [stack y versiones](docs/tecnico/stack-y-versiones.md).

## Equipo

Francisco Lata (Product Owner) · Henry Birminghan · Anel Gonzalez · Edinson Ramirez.

## Documentación

| Documento | Para qué |
|---|---|
| [Visión y MVP](docs/producto/vision-y-mvp.md) | Qué es el producto, sus usuarios y qué entra en el MVP |
| [Product Backlog](docs/producto/product-backlog.md) | Todas las historias, con pilar, prioridad, puntos y estado |
| [Historias detalladas](docs/producto/historias/README.md) | Criterios de aceptación, reglas y riesgos de cada historia |
| [Guía de pantallas](docs/producto/guia-de-pantallas.md) | Qué debe tener cada pantalla y cómo se valida cada campo |
| [Stack y versiones](docs/tecnico/stack-y-versiones.md) | Herramientas, versiones e instalación |
| [Configuración de PostgreSQL](docs/tecnico/configuracion-postgresql.md) | La base de datos local paso a paso, cómo restablecer la clave de `postgres` y errores frecuentes al arrancar |
| [Arquitectura y seguridad](docs/tecnico/arquitectura-y-seguridad.md) | Capas, tablas, rutas por rol y controles de seguridad |
| [Cómo trabajamos](docs/proceso/como-trabajamos.md) | Planificación, flujo de Git, revisión de PR y cierre del sprint |
| [Historial de sprints](docs/sprints/historial.md) | Resultados, mediciones y lecciones de cada sprint |
| [Prometheus](docs/prometheus.md) | Cómo levantar el monitoreo |
| [Investigación de PDF y QR](docs/investigacion-pdf-qr.md) | Prueba de concepto de PDFBox y ZXing (HU-2.3) |

**Regla del equipo:** si un Pull Request cambia el stack, la configuración, el backlog o una historia, actualiza también el documento que corresponde.

## Si te incorporas al proyecto

1. **Qué se construye y qué falta:** [visión y MVP](docs/producto/vision-y-mvp.md) y el [Product Backlog](docs/producto/product-backlog.md).
2. **Cómo trabajamos:** ramas, Pull Requests, revisión, Definición de Terminado y cierre de cada sprint, en [cómo trabajamos](docs/proceso/como-trabajamos.md).
3. **Tu computadora lista:** instala lo de [stack y versiones](docs/tecnico/stack-y-versiones.md), crea la base de datos (paso a paso en [configuración de PostgreSQL](docs/tecnico/configuracion-postgresql.md)) y las variables de entorno (más abajo) y corre las pruebas con `.\mvnw.cmd clean test`: todas deben quedar en verde (155 al cierre del Sprint 6).
4. **La aplicación por dentro:** capas, tablas y quién entra a cada ruta, en [arquitectura y seguridad](docs/tecnico/arquitectura-y-seguridad.md).
5. **Antes de tomar una historia:** lee su archivo en [historias detalladas](docs/producto/historias/README.md) (si no tiene, se escribe con la plantilla antes de planificar) y su pantalla en la [guía de pantallas](docs/producto/guia-de-pantallas.md).
6. **Cómo se midió cada sprint:** [historial de sprints](docs/sprints/historial.md).

---

## Requisitos

- JDK 21
- Git
- PostgreSQL 17 (referencia del equipo) y pgAdmin 4, para correr la app localmente
- IntelliJ IDEA (recomendado) o VS Code
- Docker Desktop con el motor WSL 2 (solo para el monitoreo con Prometheus)
- Ubuntu en WSL con Nikto 2.6.1 (solo para el escaneo de seguridad del cierre)

Detalle y comandos de comprobación: [stack y versiones](docs/tecnico/stack-y-versiones.md).

## Configuración de base de datos (PostgreSQL)

Cada integrante debe:

1. Tener PostgreSQL instalado localmente, con una base de datos llamada `titulos_db` (paso a paso y solución de problemas: [configuración de PostgreSQL](docs/tecnico/configuracion-postgresql.md)).
2. En IntelliJ: **Edit Configurations** → tu configuración de ejecución → **Environment variables** → agregar las variables, separadas por `;`:
```
   DB_PASSWORD=tu_contraseña_local;PROMETHEUS_PASSWORD=contraseña_de_12_o_mas_caracteres
```
3. El usuario por defecto es `postgres`. Si usas otro usuario, agrega también:
```
   DB_USERNAME=tu_usuario
```

### Variables de entorno

| Variable | Obligatoria | Para qué sirve |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL. |
| `DB_USERNAME` | No (por defecto `postgres`) | Usuario de PostgreSQL. |
| `PROMETHEUS_PASSWORD` | Para el monitoreo | Contraseña del usuario técnico `prometheus` (rol `MONITOREO`), mínimo 12 caracteres. Sin ella la app arranca, pero no crea ese usuario y Prometheus no puede leer las métricas (HU-4.10). |
| `TITULOS_VERIFICACION_URL` | No (por defecto `http://localhost:8080/verificar`) | URL base impresa en la credencial y usada por su QR; puede terminar en `/`. |

Las contraseñas nunca se escriben en el repositorio.

Las pruebas (`mvnw test`) usan H2 en memoria automáticamente y **no** requieren PostgreSQL corriendo ni estas variables.

## Cambios manuales en la base de datos local

La app usa `spring.jpa.hibernate.ddl-auto=update`: Hibernate crea las tablas y columnas nuevas, pero **no modifica las que ya existen** (restricciones, longitudes, columnas obligatorias). Por eso algunos cambios necesitan un paso manual en una base creada antes.

**Después de cada `git pull`, revisa esta lista.** Si tu base de datos es anterior al cambio, aplica el SQL en pgAdmin (Query Tool sobre `titulos_db`). Alternativa: borrar y volver a crear `titulos_db` (se pierden los datos locales).

> **Regla del equipo:** todo PR que necesite un cambio manual en la base de datos agrega aquí una entrada con la fecha, la historia, el PR, el síntoma si no se aplica, cómo comprobarlo y el SQL. Las entradas nuevas van arriba.


### 2026-10-22 · HU-4.7 (PR #23): restricciones en `titulo` y `graduados`

**Síntoma si no se aplica:** ninguno visible. La app funciona, pero la base de datos acepta valores nulos o más largos de lo permitido y CA4 no se cumple en tu equipo.

**Comprobar:**
```sql
   SELECT table_name, column_name, is_nullable, character_maximum_length
   FROM information_schema.columns
   WHERE (table_name = 'titulo'
          AND column_name IN ('nombre_graduado', 'nombre_titulo', 'fecha_registro', 'graduado_id', 'identificador'))
      OR (table_name = 'graduados' AND column_name = 'numero_documento')
   ORDER BY table_name, column_name;
```
Correcto: `is_nullable = NO` en todas menos `identificador`, y longitudes 120 (`nombre_graduado`), 150 (`nombre_titulo`), 100 (`identificador`) y 20 (`numero_documento`).

**Si no coincide, aplica:**
```sql
   ALTER TABLE titulo
       ALTER COLUMN nombre_graduado TYPE varchar(120),
       ALTER COLUMN nombre_graduado SET NOT NULL,
       ALTER COLUMN nombre_titulo TYPE varchar(150),
       ALTER COLUMN nombre_titulo SET NOT NULL,
       ALTER COLUMN fecha_registro SET NOT NULL,
       ALTER COLUMN identificador TYPE varchar(100),
       ALTER COLUMN graduado_id SET NOT NULL;
   ALTER TABLE graduados
       ALTER COLUMN numero_documento TYPE varchar(20);
```
Si falla porque hay filas con valores nulos o más largos (por ejemplo, títulos de antes de HU-1.10 sin graduado), usa la alternativa: recrear `titulos_db`.

### 2026-10-21 · HU-4.10 (PR #24): rol `MONITOREO`

**Síntoma si no se aplica:** con `PROMETHEUS_PASSWORD` definida, la app no arranca y el log muestra un error que menciona `usuarios_rol_check`.

**Comprobar:**
```sql
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'usuarios'::regclass AND contype = 'c';
```

**Si `usuarios_rol_check` no incluye `'MONITOREO'`, reemplázala:**
```sql
ALTER TABLE usuarios DROP CONSTRAINT usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('APROBADOR', 'REGISTRADOR', 'MONITOREO'));
```

## Correr la aplicación

```
.\mvnw.cmd spring-boot:run
```

Abre http://localhost:8080 e inicia sesión con `registrador1` o `aprobador1` (contraseña `clave1234`). Son usuarios de prueba que se crean solo si la tabla `usuarios` está vacía; sirven únicamente para desarrollo.

## Correr las pruebas

```
.\mvnw.cmd clean test
```

## Monitoreo con Prometheus

Requiere Docker Desktop y la variable `PROMETHEUS_PASSWORD`. Pasos, usuarios con acceso a las métricas y verificación: [`docs/prometheus.md`](docs/prometheus.md).

## Generador de identificadores

Para los títulos aprobados se utiliza un generador basado en UUID
(`UUID.randomUUID()`).

Se eligió UUID porque permite generar identificadores no secuenciales,
sin depender de un contador incremental de la base de datos. Cada
identificador generado tiene una probabilidad extremadamente baja de
repetirse, lo que permite identificar los títulos de forma única.

El identificador se genera mediante la clase `IdentificadorGenerator`,
ubicada en:

`src/main/java/pa/edu/utp/titulos_universitarios/IdentificadorGenerator.java`