# HU-4.9 · Política de seguridad de contenido sin estilos en línea

**Dueño: por asignar · Técnica · Seguridad · Prioridad Baja · 2 pts · Sprint por planificar**

## Redacción

> Como institución, necesito una política de seguridad de contenido (CSP) que no permita estilos en línea, para reducir el riesgo de inyección de código.

**Redacción anterior:** sin cambios.

**Aporte al producto:** Hoy la CSP permite `style-src 'unsafe-inline'` por dos motivos: la página de inicio de sesión que trae Spring Security y dos estilos en línea de la bandeja de revisión ([arquitectura y seguridad](../../tecnico/arquitectura-y-seguridad.md)). Quitar ese permiso cierra una vía para que un texto inyectado cambie cómo se ve una página (por ejemplo, ocultar un aviso o imitar un botón).

## Criterios de aceptación

1. **CA1.** La cabecera `Content-Security-Policy` ya no incluye `'unsafe-inline'`: `style-src` queda en `'self'`.
2. **CA2.** La CSP agrega `object-src 'none'` (no se cargan complementos como Flash o applets) y `form-action 'self'` (los formularios solo se envían a la propia aplicación), y conserva las demás directivas que ya tenía.
3. **CA3.** La misma CSP llega en las dos cadenas de seguridad: la de las pantallas y la de `/actuator/**`.
4. **CA4.** La bandeja de revisión ya no tiene los dos `style="display:inline;"`: usa una clase de `titulos.css` y se ve igual que antes.
5. **CA5.** Una prueba recorre todas las plantillas y falla si encuentra un atributo `style`, una etiqueta `<style>` o un `<script>` con código dentro, para que nadie vuelva a agregarlos sin darse cuenta.
6. **CA6.** Ninguna pantalla (P1 a P9 y el diálogo de confirmación, las que existan al cerrar) muestra errores de CSP en la consola del navegador (F12), con o sin el modo demostración.

## Reglas de negocio

- Todo estilo va en `titulos.css` o `verificacion.css`, y todo script, si alguna vez hace falta, en un archivo `.js` propio (regla de la [guía de pantallas](../guia-de-pantallas.md#reglas-para-todas-las-pantallas)).
- La CSP se define en un solo lugar del código y la usan las dos cadenas.

## Validaciones

| Dato | Regla |
|---|---|
| Cabecera CSP | Exactamente las directivas de CA1 y CA2 más las que ya existían; ninguna con `'unsafe-inline'` ni `'unsafe-eval'`. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Estilos y scripts en línea en las plantillas | Coincidencias encontradas por la prueba de CA5. | JUnit | 0 |
| Cabecera CSP | Respuestas probadas (pantallas, páginas públicas y `/actuator/**`) con la política esperada ÷ respuestas probadas. | JUnit (MockMvc) | 100 % |
| Errores de CSP en la consola | Errores al recorrer todas las pantallas. | Navegador (F12) | 0 |
| Hallazgos de Nikto | Hallazgos del escaneo del cierre comparados con el sprint anterior. | Nikto 2.6.1 | Iguales o menos (Sprint 6: 2) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Si se fusiona antes que la página de inicio de sesión propia, la de Spring se ve sin estilos. | Dependencia con [HU-1.5](HU-1.5.md) (ver Dependencias). |
| Una plantilla nueva de otra historia del mismo sprint trae un estilo en línea y queda rota. | La prueba de CA5 falla en CircleCI antes de fusionar, en cualquier PR. |
| `form-action 'self'` bloquea un formulario que envíe datos a otro sitio. | Hoy ningún formulario lo hace; CA6 lo comprueba recorriendo las pantallas. |

## Dependencias

- Necesita [HU-1.5](HU-1.5.md) en main: la página de inicio de sesión de Spring usa estilos en línea. Para la planificación: al mismo responsable o en un sprint posterior.
- Comparte la bandeja de revisión con [HU-2.6](HU-2.6.md) («Ver») y [HU-2.7](HU-2.7.md) (confirmación): archivo compartido, no dependencia; quien fusione después trae main.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.9.1 | CSP nueva en las dos cadenas y clase en lugar de los estilos en línea de la bandeja (CA1 a CA4). | — | — |
| T-4.9.2 | Prueba que recorre las plantillas (CA5), pruebas de la cabecera y evidencia: capturas de la consola sin errores en cada pantalla y la cabecera en las herramientas del navegador (CA6). Actualizar la fila «Estilos en línea» de arquitectura y seguridad. | — | — |

## Estado

Pendiente.
