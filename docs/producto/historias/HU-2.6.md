# HU-2.6 · Detalle del título

**Dueño: por asignar · Funcional · Precisión · Prioridad Alta · 3 pts · Sprint por planificar**

## Redacción

> Como personal autorizado (registrador o aprobador), quiero ver en una sola pantalla todos los datos de un título, su revisión, su identificador y su historial, para revisarlo antes de actuar sobre él.

**Redacción anterior:** Como personal autorizado, quiero ver el detalle de un título (estado, revisión, identificador e historial) para revisarlo antes de actuar sobre él.

**Por qué cambió:** Solo se aclara quién es el personal autorizado y qué significa «el detalle». El alcance es el de la pantalla P5 de la [guía de pantallas](../guia-de-pantallas.md#p5--detalle-del-título), sin dos elementos (ver «Fuera de esta historia»).

**Aporte al producto:** Hoy no hay dónde ver un título completo: la lista muestra pocos datos y la credencial se descarga desde la lista. El detalle reúne los datos, la revisión y el historial, y es la pantalla donde después se revoca o sustituye un título (HU-3.2).

## Criterios de aceptación

1. **CA1.** `/titulos/{id}` muestra, para el registrador y el aprobador: nombre del graduado, estado con texto e icono, tipo y número de documento completo, título, fecha de registro, revisado por y fecha de revisión («—» si está pendiente) e identificador («—» si no está aprobado).
2. **CA2.** Si el título está APROBADO, la pantalla tiene «Descargar PDF», que baja la misma credencial de HU-2.4 y HU-2.5. Si no está aprobado, el botón no aparece.
3. **CA3.** La pantalla muestra el historial del título (registro, aprobación o rechazo) con fecha y hora, acción, usuario y detalle, del más antiguo al más reciente.
4. **CA4.** Si el título está PENDIENTE, el aprobador ve «Aprobar» y «Rechazar» (formularios POST con CSRF); al usarlos vuelve al detalle, con el mensaje de éxito y el estado nuevo. El registrador no ve esos botones, y si envía el formulario igual, el servidor lo rechaza.
5. **CA5.** Se llega al detalle desde «Ver» en la lista de títulos (P2) y en la bandeja de revisión (P4), desde «Título afectado» en la auditoría (P6) y desde el aviso de título duplicado al registrar (P3).
6. **CA6.** En la lista de títulos (P2), la columna «Credencial» se reemplaza por «Ver»: la credencial se descarga desde el detalle, como dice la guía.
7. **CA7.** Si el título no existe o el número de la dirección no es válido, se vuelve a la lista de títulos con el mensaje «El título solicitado no existe», sin la «Whitelabel Error Page».
8. **CA8.** El usuario técnico de Prometheus recibe 403 y un visitante sin sesión es enviado al inicio de sesión.

## Fuera de esta historia

- El botón para copiar el identificador: necesita JavaScript, que el proyecto todavía no usa. El identificador se puede seleccionar y copiar a mano.
- La vista del QR en la pantalla: necesita una dirección que devuelva la imagen. El QR ya está en la credencial.
- «Revocar» y «Sustituir» llegan con [HU-3.2](HU-3.2.md), y la confirmación antes de rechazar, con [HU-2.7](HU-2.7.md).

Los dos primeros siguen en la guía como diseño objetivo y se anotan en su tabla de estado.

## Reglas de negocio

- Los dos roles ven el detalle completo, con el documento sin enmascarar: es una pantalla interna.
- Las acciones dependen del rol y del estado: solo el APROBADOR aprueba o rechaza, y solo un título PENDIENTE.
- Después de aprobar o rechazar, la vuelta es a la bandeja o al detalle, según desde dónde se hizo. Es un valor fijo de la aplicación, nunca una dirección que llegue en el formulario (así nadie puede usar el formulario para mandar al usuario a otro sitio).
- El historial es de solo lectura y sale del registro de auditoría ([HU-3.3](HU-3.3.md)).

## Validaciones

| Dato | Regla |
|---|---|
| Número del título en la dirección | Número entero positivo; si no lo es o no existe, CA7. |
| Origen de la acción (bandeja o detalle) | Solo esos dos valores; cualquier otro vuelve a la bandeja. |
| Estado, revisor, fechas e identificador | Los pone el servidor, nunca el formulario. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Adecuación funcional | Elementos de P5 dentro del alcance presentes ÷ requeridos (CA1 a CA3). | JUnit (MockMvc) y revisión contra la guía | 100 % |
| Control de acceso | Casos de acceso que responden lo esperado: aprobador y registrador 200; usuario técnico 403; sin sesión, al inicio de sesión; registrador que envía «Aprobar», rechazado. | JUnit (MockMvc) | 100 % |
| Exactitud del historial | Eventos mostrados ÷ eventos del título en la auditoría. | JUnit | 100 % |
| Eficiencia de rendimiento | Tiempo de `/titulos/{id}`. | Prometheus (`http_server_requests`) | Menos de 1 s |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Para leer el historial se agrega un método al repositorio de la auditoría y, por error, se hereda uno que permite editar o borrar eventos (pasó en el Sprint 6). | El repositorio sigue heredando solo de `Repository`; la prueba de HU-3.4 que verifica que no hay métodos para editar ni borrar sigue en verde. |
| Volver a una dirección que llega en el formulario permitiría redirigir a otro sitio. | Regla de negocio: la vuelta es un valor fijo (bandeja o detalle). |
| La lista de títulos (P2) también la cambia [HU-1.8](HU-1.8.md) (búsqueda y paginación). | Archivo compartido, no dependencia: esta historia solo cambia la columna «Credencial» por «Ver»; quien fusione después trae main. |

## Dependencias

- Usa la credencial (HU-2.4 y HU-2.5) y el registro de auditoría (HU-3.3 y HU-3.4), ya completos.
- [HU-3.2](HU-3.2.md) (revocar y sustituir) y [HU-2.7](HU-2.7.md) (confirmación) agregan botones a esta pantalla. Para la planificación: esta historia primero; las otras dos, al mismo responsable o en un sprint posterior.
- Comparte con HU-1.8 la plantilla de la lista de títulos (ver Riesgos).
- El enlace desde el aviso de duplicado (P3) cambia el registro de títulos, que también cambia [HU-1.11](HU-1.11.md) (el programa sale del catálogo). Para la planificación: al mismo responsable o en sprints distintos.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-2.6.1 | Pantalla de detalle con sus datos, la descarga de la credencial y el historial; título inexistente (CA1 a CA3, CA7 y CA8). | — | — |
| T-2.6.2 | «Aprobar» y «Rechazar» en el detalle, con la vuelta según el origen (CA4). | — | — |
| T-2.6.3 | Enlaces al detalle desde P2, P4, P6 y P3, y «Ver» en lugar de «Credencial» en P2 (CA5 y CA6). Pruebas, evidencia (capturas del detalle pendiente y aprobado con cada rol) y la fila de P5 en la tabla de estado de la guía. | — | — |

## Estado

Pendiente.
