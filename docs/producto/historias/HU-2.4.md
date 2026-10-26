# HU-2.4 · Credencial PDF

**Responsable 4: Edinson Ramirez · Funcional · Verificabilidad internacional · Prioridad Alta · 5 pts**

## Redacción

> Como personal autorizado, quiero descargar en PDF la credencial de un título aprobado, con el nombre y el documento del graduado, para entregarla al graduado y que este la presente ante terceros.

**Redacción anterior:** Como graduado, quiero obtener mi credencial en PDF, con mi nombre y documento de identidad, para presentarla ante terceros.

**Por qué cambió:** El graduado no tiene cuenta en el sistema, así que no puede descargarla. El acceso público llega con HU-3.1.

**Aporte al MVP:** Verificabilidad: primera credencial que se puede entregar a un tercero; HU-2.5 le agrega el QR y el hash.

## Criterios de aceptación

1. **CA1.** El enlace «Descargar PDF» aparece solo en las filas APROBADAS de la lista, para ambos roles.
2. **CA2.** El PDF contiene: la institución, «Credencial de título universitario», el nombre con sus tildes correctas, el tipo y número de documento completos, el título, la fecha de emisión, el identificador y «Verifique este título en …/verificar».
3. **CA3.** Un título PENDIENTE o RECHAZADO, o un id que no existe, no genera PDF y muestra un mensaje de error.
4. **CA4.** El usuario técnico recibe 403, y quien no tiene sesión es enviado al inicio de sesión.
5. **CA5.** El PDF ocupa una página tamaño carta, con las fuentes estándar (Times y Helvetica).

## Reglas de negocio

- Solo un título APROBADO tiene credencial.
- La fecha de emisión es la fecha de aprobación.
- El documento completo solo aparece en el PDF y en las páginas internas; en las públicas va enmascarado.
- El QR y el hash llegan con HU-2.5.

## Validaciones

| Dato | Regla |
|---|---|
| Id del título | Numérico y existente. |
| Estado | APROBADO. |
| Identificador | Presente. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Adecuación funcional | Elementos del diseño D1 presentes ÷ requeridos, sin contar el QR ni el hash. | JUnit (lee el texto del PDF) | 100 % |
| Eficiencia de rendimiento | Tiempo de generación del PDF. | Prometheus (http_server_requests) | Menos de 2 s (acordado el 21/10/2026) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Es la historia más grande y la última en fusionarse. | Subir T-2.4.1 y T-2.4.2 temprano, en un PR propio. |
| Las fuentes estándar no imprimen algunos caracteres poco comunes. | HU-4.7 limita el nombre a letras con tildes, ñ y ü, que sí se imprimen; agregar una prueba con «Núñez». |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-2.4.1 | PDFBox 3.0.8 y `CredencialPdfService`, que genera la credencial de un título APROBADO según el diseño D1, sin QR ni hash. | Edinson Ramirez | #26 |
| T-2.4.2 | Pruebas JUnit del contenido del PDF (nombre con tildes, ñ y ü, documento, título, fecha de emisión, identificador y enlace de verificación). | Edinson Ramirez | #26 |
| T-2.4.3 | Ruta `GET /titulos/{id}/credencial` para ambos roles, solo títulos APROBADOS, con 403 para el usuario técnico; enlace «Descargar PDF» en la lista. | Edinson Ramirez | #26 |

## Estado

Completo (Sprint 5). Evidencias: `docs/evidencias/HU-2.4.3-pruebas.txt`, `HU-2.4.3-credencial-descargada.pdf` y `.png`. Defecto conocido para HU-2.5: un nombre que ocupa tres líneas se monta sobre la línea separadora, porque esa posición es fija. La URL de verificación sale de la propiedad `titulos.verificacion.url`.

_Criterios aprobados por el Product Owner en la planificación del Sprint 5 (21/10/2026)._
