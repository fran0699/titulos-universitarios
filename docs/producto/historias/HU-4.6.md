# HU-4.6 · Modo demostración y usuarios iniciales

**Dueño: por asignar · Entorno · Seguridad · Prioridad Alta · 3 pts (antes 2) · Sprint por planificar**

## Redacción

> Como equipo, queremos un modo demostración, apagado por defecto, con usuarios de prueba y botones que llenen el acceso de cada rol, y que fuera de ese modo los usuarios iniciales se creen con datos de variables de entorno, para presentar la aplicación con rapidez sin dejar credenciales de prueba en la versión oficial.

**Redacción anterior:** Como equipo, queremos un modo demostración, apagado por defecto, con usuarios de prueba y botones que llenen el acceso por rol, para presentar la aplicación sin dejar credenciales de prueba en la versión oficial.

**Por qué cambió:** Hoy `registrador1` y `aprobador1`, con la contraseña `clave1234`, se crean siempre que la tabla de usuarios está vacía. Si solo existieran en modo demostración, fuera de ese modo nadie podría iniciar sesión, porque la aplicación no tiene otra forma de crear usuarios. Por eso la historia agrega los usuarios iniciales por variables de entorno, igual que el usuario técnico de Prometheus ([HU-4.10](HU-4.10.md)), y sube de 2 a 3 pts.

**Aporte al producto:** En la entrega del producto se presenta con dos clics y sin escribir contraseñas en vivo; la versión oficial no trae ninguna contraseña conocida.

## Criterios de aceptación

1. **CA1.** El modo demostración se activa solo con el perfil `demo` (`SPRING_PROFILES_ACTIVE=demo`). Sin ese perfil está apagado.
2. **CA2.** Con el perfil `demo`, al iniciar la aplicación existen `registrador1` (REGISTRADOR) y `aprobador1` (APROBADOR), con la contraseña `clave1234`; si ya existen, no se duplican.
3. **CA3.** Con el perfil `demo`, la página de inicio de sesión muestra la franja «Modo demostración» y los botones «Registrador» y «Aprobador». Cada botón es un enlace (`/login?demo=registrador` y `/login?demo=aprobador`) que devuelve el formulario con el usuario y la contraseña ya escritos; la persona presiona «Ingresar».
4. **CA4.** Sin el perfil `demo`: no aparecen la franja ni los botones, el parámetro `?demo=` se ignora y, si `registrador1` o `aprobador1` existen (por ejemplo, en una base de datos creada antes), se eliminan al iniciar la aplicación.
5. **CA5.** Sin el perfil `demo`, el registrador y el aprobador iniciales se crean con `TITULOS_REGISTRADOR_USUARIO`, `TITULOS_REGISTRADOR_PASSWORD`, `TITULOS_APROBADOR_USUARIO` y `TITULOS_APROBADOR_PASSWORD`, si todavía no existen.
6. **CA6.** Si falta una de esas variables, si la contraseña tiene menos de 12 caracteres o si el usuario es uno de los de demostración, ese usuario no se crea y el registro de la aplicación (log) muestra una advertencia que dice qué variable revisar; la aplicación arranca igual.
7. **CA7.** La tabla «Variables de entorno» del README incluye `SPRING_PROFILES_ACTIVE` y las cuatro variables nuevas, y «Correr la aplicación» explica cómo trabajar con el modo demostración.

## Reglas de negocio

- El modo demostración es para desarrollar y presentar; la versión oficial es la que corre sin el perfil `demo`.
- Las contraseñas de los usuarios iniciales nunca se escriben en el repositorio: llegan por variables de entorno y se guardan cifradas, como la de Prometheus.
- Los usuarios iniciales solo se crean si no existen: cambiar las variables después no cambia una contraseña ya guardada.
- Los usuarios de demostración no pueden usarse como usuarios oficiales (CA6).
- El usuario técnico de Prometheus se sigue creando igual, con o sin el perfil `demo`.

## Validaciones

| Dato | Regla |
|---|---|
| Perfil | Solo el nombre exacto `demo` activa el modo demostración. |
| Parámetro `demo` de `/login` | Solo `registrador` o `aprobador`, y solo con el perfil `demo`; cualquier otro valor se ignora. |
| Contraseña de un usuario inicial | Al menos 12 caracteres, igual que `PROMETHEUS_PASSWORD`. |
| Usuario inicial | Obligatorio; distinto de `registrador1` y `aprobador1`. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Credenciales de prueba en la versión oficial | Usuarios de demostración existentes, y botones o franja visibles, al iniciar sin el perfil `demo`. | JUnit (con y sin el perfil) | 0 |
| Adecuación funcional | Casos de CA2 a CA6 que se comportan como dicen ÷ casos probados. | JUnit | 100 % |
| Regresión | Pruebas existentes que usan los usuarios de prueba, en verde. | JUnit y CircleCI | 100 % |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Pruebas existentes que inician sesión con `registrador1` o `aprobador1` dejan de encontrarlos. | Esas pruebas activan el perfil `demo` o crean sus propios usuarios; CircleCI en verde antes de fusionar. |
| Un integrante corre la aplicación sin el perfil `demo` y sin las variables: no puede iniciar sesión. | CA6 (advertencia clara en el log) y CA7 (instrucciones en el README). «Acción requerida para el equipo» en el PR: agregar `SPRING_PROFILES_ACTIVE=demo` en IntelliJ. |
| Se activa el modo demostración en una instalación oficial por error. | Apagado por defecto (CA1) y la franja «Modo demostración» visible en la página de inicio de sesión. |
| Eliminar los usuarios de prueba borra datos importantes. | Solo se eliminan las filas de usuario; los títulos y la auditoría guardan el nombre de usuario como texto, así que el historial no se pierde. |

## Dependencias

- Necesita la página de inicio de sesión propia de [HU-1.5](HU-1.5.md), donde van la franja y los botones. Para la planificación: al mismo responsable o en un sprint posterior.
- Debe estar lista antes de la entrega del producto (primera semana de diciembre de 2026).

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.6.1 | Usuarios: los de prueba solo con el perfil `demo` (y su eliminación sin él) y los iniciales por variables de entorno, con sus advertencias. Pruebas con y sin el perfil (CA1, CA2, CA4 a CA6). | — | — |
| T-4.6.2 | Franja y botones en la página de inicio de sesión (CA3); README (CA7) y «Acción requerida para el equipo» en el PR. Evidencia: capturas de la página con y sin el perfil, y del log sin las variables. | — | — |

## Estado

Pendiente.
