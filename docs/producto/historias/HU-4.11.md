# HU-4.11 · Inicio de sesión sin error 404

**Responsable por definir · Técnica · Seguridad · Prioridad Alta · 1 pt · Sprint 6**

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

## Reglas de negocio

- Al iniciar sesión solo se recuerda una página que el navegador pidió como página (HTML) y nunca una dirección de `/.well-known/` (acordado el 26/10/2026).
- Si no hay una página recordada, se llega a `/titulos`, como hoy.

## Validaciones

No tiene datos de entrada nuevos.

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Defectos | Defecto del 404 al iniciar sesión, abierto el 25/10/2026. | Revisión del PR y evidencia | Cerrado (se registra su edad) |
| Exactitud | Casos de CA1 a CA4 con el resultado esperado. | MockMvc | 100 % |
| Control de acceso | Matriz de acceso de HU-4.10. | MockMvc y `curl` | 12 de 12 |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Romper la vuelta a la página que el usuario pidió antes de iniciar sesión. | CA3 y su prueba. |
| Otra historia del sprint (HU-3.4) también cambia `SecurityConfig`. | Cambian partes distintas del archivo; quien fusione después trae main a su rama. |

## Notas técnicas

- La corrección está en `SecurityConfig`: la regla que decide qué petición se guarda como «página a la que volver».
- HU-4.5 (páginas de error propias) es independiente: trata del aspecto de la página de error, no de esta causa.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.11.1 | En `SecurityConfig`, la regla que decide qué página se recuerda al iniciar sesión. | Por definir | — |
| T-4.11.2 | Pruebas con MockMvc (CA1 a CA4) y evidencia con F12 abierto. | Por definir | — |

## Estado

Pendiente (Sprint 6).

_Criterios aprobados por el Product Owner en la planificación del Sprint 6 (26/10/2026)._
