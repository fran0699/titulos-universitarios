# HU-3.5 · Verificación para otros sistemas

**Dueño: por asignar · Funcional · Verificabilidad internacional · Prioridad Alta · 3 pts · Sprint por planificar**

## Redacción

> Como sistema de otra institución o empresa (por ejemplo, de contratación o admisión), quiero consultar un título con su código de verificación en una dirección que responda en un formato que mi sistema pueda leer, para verificar títulos de forma automática sin abrir la página.

**Aporte al producto:** Es la innovación de visión del proyecto. La verificación pública ([HU-3.1](HU-3.1.md)) sirve a una persona con un navegador; esta dirección sirve a otros sistemas, y responde directamente cómo se garantiza la verificabilidad internacional: cualquier institución del mundo puede comprobar un título desde su propio sistema, sin cuenta y sin pedirle nada a la universidad.

## Qué se ve en la demostración

Se abre `/api/verificar/{código}` con el código de una credencial y aparece el JSON del título, con el documento enmascarado. Con un código inventado aparece el mensaje «No encontrado».

## Criterios de aceptación

1. **CA1.** `GET /api/verificar/{código}` funciona sin sesión. Para un título APROBADO responde 200 con JSON: graduado, tipo de documento, documento enmascarado, título, institución, fecha de emisión (AAAA-MM-DD), código y fecha de la consulta (ISO 8601, hora de Panamá).
2. **CA2.** Los datos y el enmascarado son los mismos de la verificación pública (el mismo servicio). No aparecen el documento completo, quién aprobó ni el id interno.
3. **CA3.** Un código con formato inválido, que no existe o de un título que no está APROBADO responde 404 con `{"valido":false,"mensaje":"No encontrado · Not found"}`, sin repetir el código recibido.
4. **CA4.** La respuesta lleva `Cache-Control: no-store` y `X-Robots-Tag: noindex`. Solo acepta GET y no habilita CORS.
5. **CA5.** Sin sesión no redirige al inicio de sesión, y `/titulos` sigue pidiendo sesión.
6. **CA6.** Para un mismo título, la API y la página `/verificar/{código}` muestran los mismos datos.
7. **CA7.** `docs/tecnico/` explica la dirección, con un ejemplo de respuesta y los códigos de estado.

## Reglas de negocio

- Reutiliza `VerificacionService.consultar`: lo que la verificación pública decide (formato del código, solo APROBADOS, enmascarado) lo decide igual la API.
- La ruta nueva se declara pública en `SecurityConfig` (`permitAll()` antes de `anyRequest()`).
- Cuando [HU-3.2](HU-3.2.md) (revocado y sustituido) y [HU-2.8](HU-2.8.md) (huella de integridad) entren a main, cada una agrega su dato a la respuesta de la API.

## Dependencias

- Necesita la verificación pública ([HU-3.1](HU-3.1.md)), ya completa.
- Comparte `VerificacionService` con [HU-4.12](HU-4.12.md) (que cuenta las verificaciones): archivo compartido, no dependencia; quien fusione después trae main.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-3.5.1 | Ruta `/api/verificar/{código}`, respuesta JSON con las fechas en formato ISO, encabezados y regla pública en `SecurityConfig` (CA1 a CA5). | — | — |
| T-3.5.2 | Pruebas con MockMvc (CA1 a CA6), documentación en `docs/tecnico/` (CA7) y evidencia: captura del JSON y del 404 en el navegador. | — | — |

## Estado

Pendiente.
