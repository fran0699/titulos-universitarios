# HU-4.11 · Inicio de sesión sin error 404

**Técnica · Seguridad · Prioridad Alta · 1 pt · Propuesta para el Sprint 6 · Pendiente (nueva)**

## Redacción

> Como usuario del sistema, quiero que al iniciar sesión la aplicación me lleve a una pantalla válida aunque el navegador haya pedido archivos automáticos, para no ver un error 404 después de entrar.

## Origen

Se detectó en la verificación de cierre del Sprint 5 (25/10/2026). Con las herramientas de desarrollo del navegador abiertas (F12), después de iniciar sesión la aplicación muestra una página de error 404, o 403 para el usuario técnico.

**Causa confirmada:** Chrome pide por su cuenta el archivo `/.well-known/appspecific/com.chrome.devtools.json`. Como todavía no hay sesión, Spring Security lo redirige al inicio de sesión y guarda esa dirección como la página a la que debe volver. Al iniciar sesión, redirige a ese archivo, que no existe: la barra del navegador muestra esa dirección.

**Por qué es prioritaria:** le pasa a cualquier persona que inicie sesión con las herramientas de desarrollo abiertas (desarrollo, pruebas y soporte). Además, en la demostración del MVP (Sprint 6) se abrirá la consola para mostrar que no hay errores de CSP, y el primer inicio de sesión terminaría en un error.

## Criterios de aceptación

Se definen y aprueban en la planificación del Sprint 6, según la Definición de Listo. Antes de planificar, este archivo se actualiza en un Pull Request para que el equipo los revise.

## Notas técnicas para la planificación

- La corrección está en `SecurityConfig`: indicar a Spring Security que no guarde como «página a la que volver» las peticiones automáticas del navegador (por ejemplo, las de `/.well-known/`).
- Conviene una prueba de integración con MockMvc que reproduzca el caso.
- HU-4.5 (páginas de error propias) es independiente: trata del aspecto de la página de error, no de esta causa.
