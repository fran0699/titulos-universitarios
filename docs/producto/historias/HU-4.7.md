# HU-4.7 · Validación en el servidor

**Responsable 1: Anel Gonzalez · Técnica · Seguridad / Precisión · Prioridad Alta · 3 pts**

## Redacción

> Como institución, necesito que el servidor valide y normalice cada campo del formulario de registro y que la base de datos lo respalde con restricciones, para no guardar títulos con datos inválidos, duplicados o manipulados.

**Redacción anterior:** Como institución, necesito que el servidor valide y normalice todos los datos de entrada (longitudes mínimas y máximas, caracteres permitidos y opciones controladas) y que la base de datos los respalde con restricciones, para no guardar datos inválidos, duplicados o manipulados.

**Por qué cambió:** «Todos los datos de entrada» no se puede verificar. Hoy el único formulario que recibe datos es el de registro; los campos que lleguen después (búsqueda, motivo, filtros) se validan en su propia historia con las reglas de la guía de pantallas.

**Aporte al MVP:** Precisión: lo que se registra, y lo que después imprime la credencial, es exactamente lo que validó el servidor.

## Criterios de aceptación

1. **CA1.** Un POST enviado sin pasar por el navegador, con un campo vacío o fuera de sus límites, se rechaza: el error aparece bajo ese campo y no se guarda nada.
2. **CA2.** El mensaje de título duplicado aparece bajo el campo «Nombre del título».
3. **CA3.** Si un POST trae fechaRegistro, estado, revisadoPor o identificador, esos valores se ignoran: la fecha queda en la de hoy y el estado en PENDIENTE.
4. **CA4.** Un INSERT directo en pgAdmin con un campo obligatorio en NULL o más largo que el máximo es rechazado por la base de datos.
5. **CA5.** La ayuda del pasaporte dice: «Letras y números, de 5 a 20 caracteres. Se guarda en mayúsculas, sin espacios ni guiones.»

## Reglas de negocio

- Un documento (tipo + número) corresponde a un solo graduado.
- Un graduado no puede tener dos veces el mismo título.
- La fecha de registro, el estado, el revisor y el identificador los asigna siempre el sistema, nunca el formulario.
- Los datos se normalizan antes de validarlos, compararlos y guardarlos.

## Validaciones

| Dato | Regla |
|---|---|
| Tipo de documento | Cédula o Pasaporte (enum en Java y restricción en la base de datos). |
| Cédula | 5–20 caracteres. Tres bloques de letras o números separados por guion (8-940-1000, PE-5-614, N-17-371). Se guarda en mayúsculas y sin ceros a la izquierda en los bloques numéricos. |
| Pasaporte | 5–20 caracteres. Letras y números; se guarda en mayúsculas, sin espacios ni guiones. |
| Nombre del graduado | 3–120 caracteres. Letras (con tildes, ñ y ü), espacio, apóstrofo, guion y punto; los espacios dobles se reducen a uno. |
| Nombre del título | 5–150 caracteres (acordado el 21/10/2026; mismo límite que tendrá el catálogo de HU-1.11). Los espacios dobles se reducen a uno. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Número y edad de defectos | Cada defecto con su fecha de detección; edad = días entre la detección y la fusión de la corrección. Base: 3 defectos conocidos (2 del 18/10 y 1 del 20/10). | GitHub (PR y revisiones) | 0 defectos abiertos al cierre |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Las bases de datos locales con datos viejos chocan con las nuevas restricciones. | Recrear la base de datos local; va como «Acción requerida para el equipo» en el PR. |
| El cambio a TituloForm rompe pruebas existentes de TituloService. | Actualizar esas pruebas en el mismo PR. |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.7.1 | Clase propia para el formulario (`TituloForm`) con solo los 4 campos, validados con anotaciones; la fecha, el estado y el revisor siempre los pone el servidor. | Anel Gonzalez | #23 |
| T-4.7.1 (complemento) | El controlador ubica cada error en su campo con `TituloValidationException.getCampo()`, sin buscar palabras en el mensaje. | Henry Birminghan | #28 |
| T-4.7.2 | Restricciones en la base de datos (NOT NULL y longitudes) y entrada en el README para las bases existentes. | Anel Gonzalez | #23 |
| T-4.7.3 | Ayuda de cédula y pasaporte según la guía; pruebas de límites y de la fecha manipulada. | Anel Gonzalez | #23 |

## Estado

Completo (Sprint 5). Evidencias: `docs/evidencias/HU-4.7-CA1.png` a `HU-4.7-CA5.png`. Acción manual para bases existentes: README, sección «Cambios manuales en la base de datos local».

_Criterios aprobados por el Product Owner en la planificación del Sprint 5 (21/10/2026)._
