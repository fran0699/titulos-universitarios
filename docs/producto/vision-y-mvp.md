# Visión del producto y MVP

## Qué es

**Títulos Universitarios Digitales** es una aplicación web para que una universidad registre, apruebe y emita títulos universitarios digitales. Además, cualquier persona (un empleador u otra institución) podrá comprobar si un título es auténtico y está vigente.

## La necesidad

Un título en papel es difícil de comprobar: quien lo recibe, sobre todo en otro país, depende de consultar a la universidad, y un documento alterado puede pasar por bueno. Del lado de la universidad, cada título debe registrarse con datos correctos y solo el personal autorizado puede emitirlo.

El cliente necesita garantizar tres cosas, y cada historia del [Product Backlog](product-backlog.md) responde a una o más:

| Pilar | Qué significa en el producto | Ejemplos |
|---|---|---|
| **Precisión** | Los datos del título son correctos, están normalizados y no se duplican. | Graduado identificado por documento (HU-1.10), validación en el servidor (HU-4.7) |
| **Seguridad** | Solo el personal autorizado actúa, cada acción queda registrada y la aplicación resiste ataques comunes. | Roles separados (HU-1.2, HU-1.9), registro de auditoría (HU-3.3), usuario técnico de Prometheus (HU-4.10) |
| **Verificabilidad internacional** | Un tercero puede comprobar el título sin pedirle nada a la universidad, también en inglés. | Identificador único (HU-2.2), credencial PDF (HU-2.4), QR y hash (HU-2.5), verificación pública bilingüe (HU-3.1) |

## Usuarios y roles

| Rol | Quién es | Qué puede hacer |
|---|---|---|
| `REGISTRADOR` | Personal de la universidad que registra títulos | Registrar títulos, ver la lista y descargar la credencial de un título aprobado |
| `APROBADOR` | Personal de la universidad que revisa | Ver la lista, aprobar o rechazar títulos pendientes y descargar credenciales |
| `MONITOREO` | Usuario técnico de Prometheus (no es una persona) | Solo leer las métricas en `/actuator/**` |
| Verificador (sin cuenta) | Empleador, institución o el propio graduado | Consultar un título en la página pública (HU-3.1, pendiente) |

**Separación de funciones:** quien registra un título no puede aprobarlo (HU-1.9).

## MVP

Se definió en la planificación del Sprint 5 (20/10/2026) como la cadena mínima que cubre los tres requisitos del cliente:

**registrar → aprobar → emitir la credencial → verificarla públicamente → saber quién hizo qué**

| Eslabón | Historias | Estado al cierre del Sprint 5 |
|---|---|---|
| Registrar | HU-1.1, HU-1.2, HU-1.3, HU-1.9, HU-1.10 y HU-4.7 | Completo |
| Aprobar | HU-2.1 y HU-2.2 | Completo |
| Emitir la credencial | HU-2.4 (PDF) y HU-2.5 (QR y hash) | PDF completo; QR y hash en el Sprint 6 |
| Verificar públicamente | HU-3.1 | Sprint 6 |
| Saber quién hizo qué | HU-3.3 (registro) y HU-3.4 (consulta) | Registro completo; consulta en el Sprint 6 |
| Seguridad transversal | HU-4.3 y HU-4.10 | Completo |

El MVP completo se demuestra al cierre del **Sprint 6**, con sus métricas de calidad, seguridad, trazabilidad y operación.

## Decisiones de alcance

- **Se priorizó en el Sprint 5** lo que protege y completa la cadena: el usuario técnico de Prometheus (HU-4.10), la validación en el servidor (HU-4.7), la credencial PDF (HU-2.4) y el registro de auditoría (HU-3.3).
- **Se postergó para después del MVP:** HU-1.11 (catálogo de programas), HU-2.6 (detalle del título; mientras tanto, el PDF se descarga desde la lista), HU-1.5, HU-4.6, HU-1.8, HU-2.7, HU-3.2, HU-4.5, HU-4.8 y HU-4.9.
- **HU-2.4 cambió de actor:** la descarga la hace el personal autorizado, porque el graduado no tiene cuenta; el acceso público llega con HU-3.1.
- **HU-3.3 subió a prioridad Alta,** porque la trazabilidad es parte del MVP.
- **El APROBADOR dejó de ver las métricas** (mínimo privilegio, 20/10/2026). Las métricas se consultan en Prometheus.
- **Nikto es una actividad fija del cierre** de cada sprint desde el Sprint 4 (por eso se retiró HU-4.2).

## Calendario

Son 10 sprints de una semana. El MVP se completa en el Sprint 6 y la entrega del producto es en la primera semana de diciembre de 2026. El historial está en [sprints/historial.md](../sprints/historial.md).
