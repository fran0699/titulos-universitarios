# HU-3.3 · Registro de auditoría

**Responsable 2: Henry Birminghan · Técnica · Seguridad · Prioridad Alta (antes Media) · 3 pts**

## Redacción

> Como institución, necesito que cada registro, aprobación y rechazo de un título quede en un registro de auditoría que no se pueda modificar, con quién lo hizo y cuándo, para rastrear cualquier cambio en un título.

**Redacción anterior:** Como institución, necesito un registro de auditoría de las acciones críticas para saber quién hizo qué y cuándo.

**Por qué cambió:** No decía cuáles son las «acciones críticas». Sube a prioridad Alta porque la trazabilidad es parte del MVP.

**Aporte al MVP:** Trazabilidad del MVP: muestra quién registró, aprobó o rechazó cada título y cuándo.

## Criterios de aceptación

1. **CA1.** Registrar, aprobar y rechazar crean exactamente 1 evento cada uno, con fecha y hora, usuario, acción, título y detalle.
2. **CA2.** Una acción que falla (título duplicado, título que ya no está PENDIENTE) crea 0 eventos.
3. **CA3.** Si el evento no se puede guardar, la acción tampoco se guarda (misma transacción).
4. **CA4.** La aplicación no tiene ninguna forma de editar ni borrar eventos.
5. **CA5.** El usuario del evento es el de la sesión, aunque el POST traiga otro.

## Reglas de negocio

- Los eventos son inmutables: solo se agregan.
- La fecha y la hora las pone el servidor.
- Las acciones son una lista cerrada: REGISTRAR, APROBAR y RECHAZAR. REVOCAR y SUSTITUIR se agregan con HU-3.2.

## Validaciones

| Dato | Regla |
|---|---|
| Acción | Solo valores de la lista (enum en Java y restricción en la base de datos). |
| Usuario | 3–50 caracteres; se toma de la sesión. |
| Título afectado | Obligatorio. |
| Detalle | Máximo 500 caracteres (acordado el 21/10/2026). |
| Fecha y hora | Las pone el servidor. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Seguridad – responsabilidad (ISO/IEC 25010) | Acciones que dejaron evento ÷ acciones realizadas. | JUnit y conteo en pgAdmin | 100 % |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Choca con HU-4.7 en TituloService.registrar. | Esperar a que se fusione T-4.7.1 y traer main a la rama antes de pedir revisión. |
| No hay pantalla para ver los eventos hasta HU-3.4. | Evidencia desde pgAdmin; HU-3.4 está prevista para el Sprint 6. |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-3.3.1 | Entidad `EventoAuditoria` y `AuditoriaService`, que solo agrega; el repositorio no tiene métodos para editar ni borrar. | Henry Birminghan | #25 |
| T-3.3.2 | Registrar, aprobar y rechazar guardan su evento en la misma transacción, con el usuario de la sesión. | Henry Birminghan | #27 |
| T-3.3.3 | Pruebas unitarias y de integración con H2 (incluida la reversión de la transacción) y evidencia en PostgreSQL. | Henry Birminghan | #27 |

## Estado

Completo (Sprint 5). Evidencias: `docs/evidencias/HU-3.3-tabla-auditoria.txt` y `HU-3.3-eventos-auditoria.txt`. Al agregar valores a `AccionAuditoria` (HU-3.2), hay que actualizar a mano la restricción `eventos_auditoria_accion_check`.

_Criterios aprobados por el Product Owner en la planificación del Sprint 5 (21/10/2026)._
