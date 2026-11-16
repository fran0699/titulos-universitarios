# HU-3.2 · Revocar o sustituir un título

**Dueño: por asignar · Funcional · Verificabilidad internacional · Prioridad Media · 5 pts (antes 3) · Sprint por planificar**

## Redacción

> Como aprobador, quiero revocar un título emitido, o marcarlo como sustituido por otro corregido, indicando el motivo, para que la verificación pública muestre su estado real.

**Redacción anterior:** Como aprobador, quiero revocar o sustituir un título emitido para que la verificación muestre su estado real.

**Por qué cambió:** Se aclara qué es sustituir: el título viejo se marca como SUSTITUIDO, y el corregido se registra y se aprueba como uno nuevo, por el camino normal. Para que eso sea posible, la regla de duplicados deja de contar los títulos rechazados, revocados y sustituidos. La verificación del título viejo no enlaza al nuevo (decisión de alcance del Sprint 7). La estimación sube de 3 a 5 pts con los criterios a la vista: dos acciones, campos y estados nuevos, la regla de duplicados, la auditoría y dos estados en la verificación pública.

**Aporte al producto:** Sin esta historia, un título revocado o con datos equivocados sigue apareciendo como «Válido» en la verificación pública.

## Criterios de aceptación

1. **CA1.** En el detalle (P5) de un título APROBADO, el aprobador ve «Revocar» y «Sustituir», con el estilo de peligro. Cada uno lleva a la página de confirmación de [HU-2.7](HU-2.7.md), que en estos casos pide el motivo, obligatorio.
2. **CA2.** Al confirmar, el título pasa a REVOCADO o a SUSTITUIDO, y guarda quién lo hizo, la fecha y el motivo. El detalle muestra el estado nuevo con texto e icono, y esos tres datos.
3. **CA3.** Solo un título APROBADO se puede revocar o sustituir, y el cambio no se puede deshacer: un título REVOCADO o SUSTITUIDO no muestra acciones, y el servidor rechaza cualquier intento.
4. **CA4.** Cada cambio queda en la auditoría con las acciones nuevas «Revocación» y «Sustitución», con el motivo en el detalle; la consulta de la auditoría (P6) permite filtrarlas.
5. **CA5.** La verificación pública de un título revocado muestra «Revocado · Revoked», y la de uno sustituido, «Sustituido · Replaced» con el aviso «Pida al graduado la credencial vigente · Ask the graduate for the current credential». Las dos muestran, con icono, los datos mínimos y la fecha del cambio en los dos idiomas, y nunca el motivo.
6. **CA6.** La credencial de un título revocado o sustituido no se descarga (responde como hoy con un título no aprobado).
7. **CA7.** Al registrar, un título solo se considera duplicado si el otro está PENDIENTE o APROBADO: el corregido de un título sustituido, o uno rechazado antes, se pueden registrar.
8. **CA8.** Los cambios de las restricciones de la base de datos por los valores nuevos (estados y acciones de auditoría) tienen su entrada en «Cambios manuales en la base de datos local» del README.

## Reglas de negocio

- Revocar: el título deja de valer (por ejemplo, por fraude o por un error grave). Sustituir: lo reemplaza otro título corregido, que se registra y aprueba como uno nuevo, con su propio identificador.
- Solo el APROBADOR revoca o sustituye.
- El motivo es interno: se ve en el detalle y en la auditoría, nunca en la verificación pública.
- Si el título está revocado o sustituido, la verificación muestra ese estado antes que cualquier otra comprobación (incluida la huella de [HU-2.8](HU-2.8.md)).
- Los estados nuevos aparecen solos en el filtro de la lista de títulos ([HU-1.8](HU-1.8.md)), porque el filtro sale de la lista de estados.

## Validaciones

| Dato | Regla |
|---|---|
| Motivo | Obligatorio; de 10 a 500 caracteres, sin espacios al inicio o al final ([guía de pantallas](../guia-de-pantallas.md#validación-de-datos-hu-47)). Se muestra con `th:text`. |
| Usuario, fecha y estado del cambio | Los pone el servidor, nunca el formulario. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Transiciones de estado | Cambios permitidos que funcionan y prohibidos que se rechazan (desde PENDIENTE, RECHAZADO, REVOCADO y SUSTITUIDO) ÷ casos probados. | JUnit | 100 % |
| Exactitud de la verificación | Títulos revocados y sustituidos que la verificación muestra con su estado real ÷ probados. | JUnit (MockMvc) | 100 % |
| Confidencialidad del motivo | Veces que el motivo aparece en la verificación pública. | JUnit (MockMvc) | 0 |
| Trazabilidad | Cambios de estado con su evento de auditoría ÷ cambios realizados. | JUnit | 100 % |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Las bases de datos locales rechazan los estados y acciones nuevos por sus restricciones CHECK (pasa con todo enum guardado como texto). | CA8: entrada en «Cambios manuales» y «Acción requerida para el equipo» en el PR. |
| Revocar por error. | Página de confirmación con motivo (HU-2.7), auditoría y CA3. |
| [HU-2.8](HU-2.8.md) también cambia los estados de la verificación pública. | Para la planificación: al mismo responsable o en sprints distintos. |
| Cambiar la regla de duplicados permite registrar dos veces lo mismo. | CA7 solo deja fuera los rechazados, revocados y sustituidos; pruebas con cada estado. |

## Dependencias

- Necesita el detalle de [HU-2.6](HU-2.6.md) (donde van los botones) y la página de confirmación de [HU-2.7](HU-2.7.md). Para la planificación: después de las dos, o al mismo responsable.
- Comparte la verificación pública con [HU-2.8](HU-2.8.md) (ver Riesgos).

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-3.2.1 | Revocar y sustituir en el servicio: estados, campos del cambio, acciones de auditoría y regla de duplicados; entrada en «Cambios manuales» (CA2 a CA4, CA7 y CA8). Pruebas. | — | — |
| T-3.2.2 | Botones en el detalle y página de confirmación con motivo (CA1). Pruebas con MockMvc. | — | — |
| T-3.2.3 | Estados revocado y sustituido en la verificación pública y credencial no descargable (CA5 y CA6). Pruebas y evidencia (capturas del detalle y de la verificación en los dos estados). | — | — |

## Estado

Pendiente.
