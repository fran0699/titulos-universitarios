# HU-1.5 · Página de inicio de sesión propia

**Dueño: Maria Gonzalez · Funcional · Seguridad · Prioridad Media · 3 pts · Sprint 8**

## Redacción

> Como usuario del sistema, quiero una página de inicio de sesión propia y coherente con el resto de la aplicación, sin estilos en línea, para iniciar sesión con confianza y permitir una política de seguridad más estricta.

**Redacción anterior:** sin cambios.

**Aporte al producto:** Hoy se ve la página que trae Spring Security por defecto: no tiene el nombre del sistema ni de la institución, no enlaza a la verificación pública y usa estilos en línea, que obligan a que la política de seguridad de contenido (CSP) los permita. Esta historia es la base de [HU-4.6](HU-4.6.md) (modo demostración) y de [HU-4.9](HU-4.9.md) (CSP sin estilos en línea).

## Criterios de aceptación

1. **CA1.** `/login` muestra la página P1 de la [guía de pantallas](../guia-de-pantallas.md#p1--iniciar-sesión): nombre del sistema y de la institución, «Usuario» y «Contraseña» cada uno con su etiqueta, el botón «Ingresar» y el enlace «Verificar un título», que lleva a `/verificar`.
2. **CA2.** Con usuario o contraseña incorrectos, la página vuelve a mostrarse con «Usuario o contraseña incorrectos», sin decir cuál de los dos falló.
3. **CA3.** Después de cerrar sesión, la página muestra «Cerraste sesión correctamente».
4. **CA4.** Al iniciar sesión con un usuario del personal se llega a `/titulos`, y las pruebas de [HU-4.11](HU-4.11.md) (sin error 404 al entrar) siguen en verde.
5. **CA5.** La página no tiene estilos ni scripts en línea: usa solo `titulos.css` y no muestra errores de CSP en la consola del navegador (F12).
6. **CA6.** El formulario se envía por POST con su token CSRF, y los campos tienen `autocomplete="username"` y `autocomplete="current-password"`.
7. **CA7.** Cumple las reglas comunes de la guía: botón de al menos 44 px de alto, contraste mínimo de 4.5:1, y se puede usar en un celular.

## Reglas de negocio

- La página es pública y no lleva el encabezado ni el menú interno.
- El mensaje de error es el mismo para un usuario que no existe y para una contraseña incorrecta, para no revelar qué usuarios existen.
- Iniciar sesión sigue funcionando igual: mismos usuarios, mismos roles y la misma página de llegada.

## Validaciones

| Dato | Regla |
|---|---|
| Usuario y contraseña | Obligatorios en el formulario (`required`); la validación real la hace Spring Security en el servidor. |
| Mensajes `?error` y `?logout` | Solo se muestran los dos textos fijos de la aplicación; nunca un texto que llegue en la dirección. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Adecuación funcional | Elementos de P1 presentes ÷ requeridos (CA1 a CA3). | JUnit (MockMvc) y revisión contra la guía | 100 % |
| Estilos y scripts en línea en la página | Atributos `style`, etiquetas `<style>` y `<script>` en línea en la respuesta de `/login`. | JUnit (MockMvc) | 0 |
| Regresión del inicio de sesión | Pruebas de inicio de sesión existentes (HU-1.2 y HU-4.11) en verde. | JUnit | 100 % |
| Errores de CSP en la consola | Errores en la consola del navegador al abrir `/login`. | Navegador (F12) | 0 |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Cambiar el inicio de sesión rompe la vuelta a una página válida que arregló HU-4.11. | CA4: no se toca la regla de la página guardada; sus pruebas siguen en verde. |
| Si la página propia no se declara pública, nadie puede llegar al inicio de sesión. | `formLogin` con `loginPage("/login")` y `permitAll()`; prueba de que `/login` responde 200 sin sesión. |
| Un mensaje distinto para usuario inexistente y contraseña incorrecta permitiría adivinar usuarios. | CA2 y prueba con los dos casos. |

## Dependencias

- Ninguna historia pendiente.
- [HU-4.6](HU-4.6.md) y [HU-4.9](HU-4.9.md) dependen de esta: para la planificación, al mismo responsable o en un sprint posterior.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-1.5.1 | Página `/login` con su plantilla y sus estilos en `titulos.css`; Spring Security la usa como página de inicio de sesión (CA1 a CA3, CA6 y CA7). | Maria González | #45 |
| T-1.5.2 | Pruebas con MockMvc (página sin sesión, error, cierre de sesión, llegada a `/titulos`, sin estilos en línea) y evidencia (capturas de la página en computadora y celular, y de la consola sin errores), y la fila de P1 en la tabla de estado de la guía (CA4 y CA5). | Maria González | #45 |

## Estado

Completo (Sprint 8). PR #45 (y #48, con la evidencia). Evidencias en `docs/evidencias/`:

- `HU-1.5-login-computadora.png`: CA1, la página P1 completa en la computadora.
- `HU-1.5-login-celular.png`: CA7, la página P1 en vista de celular (F12).
- `HU-1.5-consola-sin-errores.png`: CA5, la consola (F12) de `/login` sin errores de CSP.
- `HU-1.5-pruebas.txt`: las pruebas de login (13 en verde) y el total (169 en verde, incluidas las de HU-4.11, CA4). Tras traer `main`, el CI (CircleCI) corre 205 pruebas, 0 fallos.

_Nota: la página incluye también la parte visual del modo demostración de [HU-4.6](HU-4.6.md) (franja y botones, solo con el perfil `demo`), que se adelantó porque vive en la misma página P1. Los usuarios iniciales y la regla de que fuera de `demo` no existan usuarios de prueba siguen pendientes en HU-4.6._
