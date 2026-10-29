# HU-3.4 · Consulta de la auditoría

**Responsable 3: Anel Gonzalez · Funcional · Seguridad · Prioridad Alta (antes Media) · 3 pts · Sprint 6**

## Redacción

> Como aprobador, quiero consultar el registro de auditoría filtrando por fechas, acción y usuario, para rastrear quién registró, aprobó o rechazó cada título y cuándo.

**Redacción anterior:** Como aprobador, quiero consultar el registro de auditoría con filtros por fecha, acción y usuario para rastrear cualquier cambio.

**Por qué cambió:** El «para qué» ahora nombra las acciones que se auditan. Sube a prioridad Alta porque es parte del MVP («saber quién hizo qué»), igual que [HU-3.3](HU-3.3.md).

**Aporte al MVP:** Completa el eslabón «saber quién hizo qué»: los eventos que guarda HU-3.3 se consultan desde la aplicación, sin pgAdmin.

## Criterios de aceptación

1. **CA1.** Solo el APROBADOR abre `/auditoria` (P6). El REGISTRADOR y el usuario técnico reciben 403, y a quien no tiene sesión se le pide iniciarla.
2. **CA2.** El menú muestra «Auditoría» solo al APROBADOR.
3. **CA3.** La tabla muestra Fecha y hora, Usuario, Acción (Registro, Aprobación, Rechazo), Título afectado (graduado · título) y Detalle, de lo más reciente a lo más antiguo.
4. **CA4.** Los filtros desde, hasta, acción y usuario se combinan, y solo aparecen los eventos que cumplen todos.
5. **CA5.** Un filtro inválido (desde posterior a hasta, usuario de 1–2 o de más de 50 caracteres, acción fuera de la lista) muestra el error junto al filtro y no hace la consulta.
6. **CA6.** Se muestran 20 eventos por página, con «Anterior», «Siguiente» y «Mostrando X–Y de N», y los filtros se conservan al cambiar de página. Sin resultados aparece «No hay eventos con esos filtros».
7. **CA7.** Es de solo lectura: la pantalla solo responde a GET, y el repositorio sigue sin métodos para editar ni borrar eventos (CA4 de HU-3.3).

## Reglas de negocio

- Los eventos solo se leen; nunca se modifican.
- «Título afectado» muestra el nombre del graduado y del título, porque un graduado puede tener más de un título. No lleva enlace hasta que exista el detalle del título (HU-2.6).
- La fecha y la hora se muestran como «dd/mm/aaaa hh:mm», hora de Panamá.

## Validaciones

| Dato | Regla |
|---|---|
| Desde y hasta | Fechas válidas; «desde» no puede ser posterior a «hasta». «Hasta» incluye todo ese día (acordado el 26/10/2026). |
| Acción | Vacía (todas) o un valor de la lista. |
| Usuario | Vacío (todos) o 3–50 caracteres. Busca el texto contenido en el usuario, sin distinguir mayúsculas (acordado el 26/10/2026). |
| Página | Desde 1; 20 eventos por página (acordado el 26/10/2026). |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Exactitud de los filtros | Combinaciones de filtros con el resultado esperado ÷ combinaciones probadas. | Pruebas de integración (H2) | 100 % |
| Control de acceso | Matriz de `/auditoria`: APROBADOR, REGISTRADOR, usuario técnico y sin sesión. | MockMvc | 4 de 4 |
| Integridad de la auditoría | Métodos del repositorio para editar o borrar eventos. | JUnit | 0 |
| Eficiencia de rendimiento | Tiempo de respuesta de `/auditoria`. | Prometheus (`http_server_requests`) | Menos de 1 s (acordado el 26/10/2026) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Abrir la auditoría a otro rol. | Regla propia para `/auditoria` antes de `anyRequest()` en `SecurityConfig`, comprobada con la matriz de acceso. |
| Al agregar consultas, el repositorio gana métodos para borrar. | Declarar solo consultas de lectura. No heredar de `JpaRepository` ni de `JpaSpecificationExecutor` (en versiones recientes incluye un `delete`). Una prueba confirma que no hay métodos para editar ni borrar. |
| Lentitud con muchos eventos. | Paginar en la base de datos, no en memoria. |
| Usar los filtros para inyectar código. | Parámetros enlazados en las consultas, validación previa y `th:text` para mostrar. |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-3.4.1 | Consultas de lectura con filtros y paginación en el repositorio, y validación de los filtros. | Anel Gonzalez | #35 |
| T-3.4.2 | P6: ruta y regla de seguridad, enlace «Auditoría» en el menú solo para el APROBADOR, tabla, filtros, paginación y mensaje sin resultados. | Anel Gonzalez | #35 |
| T-3.4.3 | Pruebas (filtros, validación, matriz de acceso, repositorio sin métodos para editar ni borrar) y evidencia con captura. | Anel Gonzalez | #35 |

## Estado

Completo (Sprint 6, PR #35).

**Pruebas:** `AuditoriaSeguridadIntegrationTest` (matriz de acceso, 4 de 4), `AuditoriaConsultaIntegrationTest` (validación de los filtros: cada error bajo su filtro y sin consultar la base de datos) y `AuditoriaConsultaFiltrosIntegrationTest` (con H2: filtros combinados, «hasta» con todo el día, usuario parcial sin distinguir mayúsculas, orden, 20 por página y repositorio sin métodos para editar ni borrar).

**Evidencias** en `docs/evidencias/`: `HU-3.4-eventos-registrados.png` (tabla y «Mostrando X–Y de N»), `HU-3.4-busqueda.png` (filtros de fecha y usuario aplicados), `HU-3.4-busqueda-vacia.png` («No hay eventos con esos filtros») y `HU-3.4-mensaje-error.png` (error bajo cada filtro).

**Revisión del PR:** la primera versión de la consulta usaba `JpaSpecificationExecutor`, que trae métodos para borrar y editar (CA7). Se reemplazó por una consulta declarada en el repositorio, que no hereda esos métodos, y se agregó la prueba que lo comprueba. El tiempo de respuesta (meta: menos de 1 s) se mide con Prometheus en el cierre del sprint.

_Criterios aprobados por el Product Owner en la planificación del Sprint 6 (26/10/2026)._
