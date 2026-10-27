# HU-4.11 · Inicio de sesión sin error 404

**Responsable 4: Francisco Lata · Técnica · Seguridad · Prioridad Alta · 1 pt · Sprint 6**

## Redacción

> Como usuario del sistema, quiero que al iniciar sesión la aplicación me lleve a una pantalla válida aunque el navegador haya pedido archivos automáticos, para no ver un error 404 después de entrar.

**Aporte al MVP:** En la demostración del MVP se abre la consola del navegador (F12) para mostrar que no hay errores de CSP; sin esta historia, el primer inicio de sesión terminaría en un error.

## Origen

Se detectó en la verificación de cierre del Sprint 5 (25/10/2026). Con las herramientas de desarrollo del navegador abiertas (F12), después de iniciar sesión la aplicación muestra una página de error 404, o 403 para el usuario técnico.

**Causa confirmada:** Chrome pide por su cuenta el archivo `/.well-known/appspecific/com.chrome.devtools.json`. Como todavía no hay sesión, Spring Security lo redirige al inicio de sesión y guarda esa dirección como la página a la que debe volver. Al iniciar sesión, redirige a ese archivo, que no existe: la barra del navegador muestra esa dirección.

## Criterios de aceptación

1. **CA1.** Si antes de iniciar sesión Chrome pidió `/.well-known/appspecific/com.chrome.devtools.json`, al iniciar sesión se llega a `/titulos` y no a un 404.
2. **CA2.** Ninguna otra petición automática que no sea una página (por ejemplo, una imagen o un JSON) queda como página a la que volver.
3. **CA3.** Si el usuario abrió sin sesión una página válida (por ejemplo, `/titulos/pendientes`), al iniciar sesión vuelve a esa página.
4. **CA4.** El usuario técnico sigue recibiendo 403 en las pantallas, y la matriz de acceso de [HU-4.10](HU-4.10.md) sigue en 12 de 12.
5. **CA5.** Se comprueba en Chrome con F12 abierto, con los dos roles del personal, y queda una captura como evidencia.
6. **CA6.** Con F12 abierto, la consola no muestra el 404 de `favicon.ico`, con o sin sesión.

**Alcance ampliado (27/10/2026):** CA6 se agregó durante el sprint, por decisión del Product Owner. Al reproducir el defecto, la consola mostraba además `GET /favicon.ico 404` en cada pantalla del personal, porque la aplicación no tenía ícono de pestaña. Tiene el mismo objetivo que la historia (la consola limpia en la demostración del MVP) y no cambia la estimación.

## Reglas de negocio

- Al iniciar sesión solo se recuerda una página que el navegador pidió como página (HTML) y nunca una dirección de `/.well-known/` (acordado el 26/10/2026).
- Si no hay una página recordada, se llega a `/titulos`, como hoy.
- El ícono de la pestaña (`/favicon.ico`) es público: no tiene datos y lo pide el navegador en todas las páginas, también en las públicas.

## Validaciones

No tiene datos de entrada nuevos.

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta | Resultado |
|---|---|---|---|---|
| Defectos | Defecto del 404 al iniciar sesión, abierto el 25/10/2026. | Revisión del PR y evidencia | Cerrado (se registra su edad) | Corregido el 27/10/2026 (2 días); se cierra al fusionar el PR |
| Exactitud | Casos de CA1 a CA4 y CA6 con el resultado esperado. | MockMvc | 100 % | 11 de 11 (100 %); antes de la corrección, 5 de 10 fallaban |
| Control de acceso | Matriz de acceso de HU-4.10. | `curl` y navegador | 12 de 12 | 12 de 12 |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Romper la vuelta a la página que el usuario pidió antes de iniciar sesión. | CA3 y su prueba. |
| Otra historia del sprint (HU-3.4) también cambia `SecurityConfig`. | Cambian partes distintas del archivo; quien fusione después trae main a su rama. HU-3.1 también lo cambia, en la línea de las rutas públicas; la fusión con su rama se comprobó sin conflictos. |

## Solución

- En `SecurityConfig`, la cadena de las pantallas usa su propio `HttpSessionRequestCache` (la «página guardada»). Solo recuerda una petición `GET` cuyo encabezado `Accept` incluya `text/html` y que no vaya a `/.well-known/`. El criterio por defecto de Spring Security ya excluía el favicon y el JSON, pero no el archivo de Chrome, que acepta cualquier formato (`*/*`).
- `src/main/resources/static/favicon.ico`: el birrete del encabezado, en blanco sobre el azul institucional (16, 32 y 48 px), y `/favicon.ico` con `permitAll()`.
- HU-4.5 (páginas de error propias) sigue siendo independiente: trata del aspecto de la página de error, no de esta causa.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.11.1 | En `SecurityConfig`, la regla que decide qué página se recuerda al iniciar sesión; `favicon.ico` público (CA6). | Francisco Lata | — |
| T-4.11.2 | Pruebas con MockMvc (`InicioSesionSin404IntegracionTest`, CA1 a CA4 y CA6) y evidencia con F12 abierto. | Francisco Lata | — |

## Estado

Completo (Sprint 6). Evidencias en `docs/evidencias/`:

- `HU-4.11-antes-404.png`: el defecto reproducido antes de la corrección.
- `HU-4.11-despues-registrador.png` y `HU-4.11-despues-aprobador.png`: CA1, CA3, CA5 y CA6 en Chrome con F12 abierto.
- `HU-4.11-pruebas.txt`: las pruebas antes (5 fallos de 10) y después de la corrección (106 en verde).
- `HU-4.11-matriz-acceso.txt`: CA4, 12 de 12.

_Criterios aprobados por el Product Owner en la planificación del Sprint 6 (26/10/2026); CA6 agregado el 27/10/2026._
