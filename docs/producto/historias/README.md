# Historias de usuario detalladas

Aquí vive el detalle de cada historia que se corrige para entrar a un sprint: su redacción, sus **criterios de aceptación** y lo que el equipo acuerda para construirla y revisarla. El resumen de todas las historias está en el [Product Backlog](../product-backlog.md).

## Por qué están en el repositorio

Los criterios de aceptación son el acuerdo de qué significa «terminado» para cada historia. Si solo los conoce quien los escribió, nadie más puede construir ni revisar contra ellos. En el repositorio los ve todo el equipo, quedan en el historial y no dependen de la cuenta ni de la computadora de nadie.

## Cómo se usan

1. **Antes de planificar:** quien corrige una historia la escribe aquí con la plantilla de abajo. El Product Owner la aprueba contra la Definición de Listo y se sube en un Pull Request de documentación, que no necesita aprobación ([cómo trabajamos](../../proceso/como-trabajamos.md#pull-requests-de-documentación)). El equipo la recibe en la planificación.
2. **Al desarrollar:** cada Pull Request de la historia dice qué criterios cumple (CA1, CA2…).
3. **Al revisar:** el revisor comprueba cada criterio con sí o no.
4. **Al cerrar:** se actualizan la sección «Estado», las evidencias y el estado en el Product Backlog.

## Índice

| Historia | Título | Estado |
|---|---|---|
| [HU-4.7](HU-4.7.md) | Validación en el servidor | Completo (Sprint 5) |
| [HU-3.3](HU-3.3.md) | Registro de auditoría | Completo (Sprint 5) |
| [HU-4.10](HU-4.10.md) | Usuario técnico de Prometheus | Completo (Sprint 5) |
| [HU-2.4](HU-2.4.md) | Credencial PDF | Completo (Sprint 5) |
| [HU-2.5](HU-2.5.md) | QR en la credencial | Completo (Sprint 6) |
| [HU-3.1](HU-3.1.md) | Verificación pública | Completo (Sprint 6) |
| [HU-3.4](HU-3.4.md) | Consulta de la auditoría | Completo (Sprint 6) |
| [HU-4.11](HU-4.11.md) | Inicio de sesión sin error 404 | Completo (Sprint 6) |

Las historias completadas antes del Sprint 5 no tienen archivo propio: su redacción está en el Product Backlog. Se agregan aquí cuando se vuelvan a tocar.

## Plantilla

Copia este bloque en `HU-X.Y.md` y completa cada sección:

```markdown
# HU-X.Y · Título corto

**Tipo · Pilar · Prioridad · Pts · Sprint · Dueño**

## Redacción
> Como [actor], quiero [qué] para [para qué].

**Redacción anterior:** (si cambió) · **Por qué cambió:** · **Aporte al MVP:**

## Criterios de aceptación
1. **CA1.** Algo que se comprueba con sí o no.

## Reglas de negocio
- …

## Validaciones
| Dato | Regla |
|---|---|

## Métricas QA
| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|

## Riesgos
| Riesgo | Mitigación |
|---|---|

## Tareas
| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|

## Estado
Pendiente / En curso / Completo (Sprint N), con sus evidencias en docs/evidencias/.
```
