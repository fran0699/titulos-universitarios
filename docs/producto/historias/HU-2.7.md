# HU-2.7 · Página de confirmación antes de rechazar

**Dueño: Marleni Barria · Funcional · Precisión · Prioridad Media · 1 pt · Sprint 8**

## Redacción

> Como aprobador, quiero confirmar en una página aparte antes de rechazar un título, para no rechazar uno por error, porque el rechazo no se puede deshacer.

**Redacción anterior:** Como aprobador, quiero confirmar antes de rechazar, revocar o sustituir un título para evitar errores que no se pueden deshacer.

**Por qué cambió:** Revocar y sustituir todavía no existen: llegan con [HU-3.2](HU-3.2.md), que reutiliza esta misma página y le agrega el motivo. Esta historia se queda con el rechazo, que hoy se hace con un solo clic desde la bandeja. La confirmación es una página y no un diálogo con JavaScript: funciona con la política de seguridad de contenido estricta de [HU-4.9](HU-4.9.md) y se prueba con MockMvc, como el resto de la aplicación.

**Aporte al producto:** Evita un error que hoy no tiene arreglo: un título rechazado por equivocación ya no se puede aprobar.

## Criterios de aceptación

1. **CA1.** «Rechazar» en la bandeja de revisión (P4) ya no rechaza: lleva a `GET /titulos/{id}/rechazar`, la página de confirmación. El botón usa el estilo de peligro de la guía.
2. **CA2.** La página muestra la pregunta con el título y el graduado («¿Rechazar el título «…» de …?»), la consecuencia «Esta acción no se puede deshacer», «Cancelar» (secundario, vuelve a la bandeja) y «Rechazar título» (peligro, a la derecha).
3. **CA3.** Al abrir la página, el foco está en «Cancelar» (atributo `autofocus`), para que presionar Enter por costumbre no rechace.
4. **CA4.** «Rechazar título» envía `POST /titulos/{id}/rechazar` con su token CSRF y hace lo mismo que hoy: estado RECHAZADO, revisor, fecha y evento de auditoría. Las pruebas de rechazo existentes siguen en verde.
5. **CA5.** Solo el APROBADOR abre la página. Si el título no existe o ya no está PENDIENTE, se vuelve a la bandeja con el mensaje correspondiente, sin la «Whitelabel Error Page».
6. **CA6.** La página no tiene estilos ni scripts en línea y cumple las reglas comunes de la guía (botones de al menos 44 px, contraste 4.5:1).

## Reglas de negocio

- Abrir la página no cambia nada (es un GET); el rechazo solo ocurre con el POST de CA4.
- El servidor sigue validando el rol y el estado en el POST, aunque alguien lo envíe sin pasar por la página.
- La página se diseña para reutilizarse: [HU-3.2](HU-3.2.md) la usará para revocar y sustituir, con un campo de motivo obligatorio.

## Validaciones

| Dato | Regla |
|---|---|
| Número del título en la dirección | Número entero positivo de un título PENDIENTE; si no, CA5. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Rechazos sin confirmación desde la interfaz | Botones o enlaces que envían el rechazo sin pasar por la página. | JUnit (MockMvc) y revisión | 0 |
| Control de acceso | Registrador y usuario técnico reciben 403; sin sesión, al inicio de sesión. | JUnit (MockMvc) | 100 % |
| Regresión | Pruebas de rechazo y auditoría existentes en verde. | JUnit | 100 % |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Una petición GET que cambiara el estado se podría disparar con un simple enlace. | Regla de negocio: el GET solo muestra la página; el cambio es un POST con CSRF. |
| La pantalla de detalle de [HU-2.6](HU-2.6.md) también tiene «Rechazar». | Si HU-2.6 ya está en main, su botón también lleva a esta página. Si las dos van en el mismo sprint, al mismo responsable (ver Dependencias). |

## Dependencias

- Ninguna historia pendiente: la bandeja (P4) ya existe.
- Comparte con [HU-2.6](HU-2.6.md) el botón «Rechazar» del detalle. Para la planificación: al mismo responsable o en sprints distintos.
- [HU-3.2](HU-3.2.md) depende de esta.
- Comparte la bandeja con [HU-4.9](HU-4.9.md), que quita sus estilos en línea: archivo compartido, no dependencia.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-2.7.1 | Página de confirmación y «Rechazar» de la bandeja con el estilo de peligro (CA1 a CA6). Pruebas con MockMvc y evidencia (capturas de la página y de la bandeja), y las filas de P4 y de la página de confirmación en la tabla de estado de la guía. | Marleni Barria | — |

## Estado

Pendiente.
