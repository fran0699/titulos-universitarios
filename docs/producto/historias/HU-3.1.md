# HU-3.1 · Verificación pública

**Henry Birminghan · Funcional · Verificabilidad internacional · Prioridad Alta · 5 pts · Sprint 6**

## Redacción

> Como empleador, institución o el propio graduado, quiero consultar un título con su código de verificación, escribiéndolo o desde el QR de la credencial, sin iniciar sesión, para comprobar que es auténtico y válido.

**Redacción anterior:** Como graduado, empleador o institución, quiero consultar un título por QR o código, sin iniciar sesión, para conocer su estado; el documento del graduado se muestra enmascarado.

**Por qué cambió:** «Para conocer su estado» no decía para qué. El enmascarado del documento pasa a ser un criterio de aceptación.

**Aporte al MVP:** Completa el eslabón «verificarla públicamente»: un tercero comprueba el título sin cuenta y sin pedirle nada a la universidad.

## Criterios de aceptación

1. **CA1.** `/verificar` (P7) se abre sin sesión, con el nombre de la institución, el campo «Código de verificación» y el botón «Verificar». Al enviar un código lleva a `/verificar/{código}`; si el campo está vacío, el error aparece bajo el campo.
2. **CA2.** Para un título APROBADO, `/verificar/{código}` (P8) muestra «Título válido · Valid degree» con icono y solo estos datos: graduado, tipo de documento con el número enmascarado, título, institución, fecha de emisión, código, y fecha y hora de la consulta.
3. **CA3.** Enmascarado: se ocultan todos los caracteres salvo los últimos 4 que no sean guion, y los guiones quedan en su lugar. Ejemplos: `8-123-4567` → `•-•••-4567`, `PE-5-614` → `••-5-614`, `PA1234567` → `•••••4567`.
4. **CA4.** En ninguna parte de la respuesta, tampoco en el HTML, aparecen el documento completo, quién aprobó ni el id interno del título.
5. **CA5.** Un código que no es un UUID de 36 caracteres muestra «No encontrado · Not found» sin consultar la base de datos. Un código que no existe, o de un título que no está APROBADO, muestra el mismo mensaje. En los dos casos la respuesta es 404.
6. **CA6.** P7 y P8 muestran cada texto en español y en inglés a la vez, sin selector de idioma.
7. **CA7.** Las pantallas están pensadas para celular: sin menú interno ni datos del personal, sin desplazamiento horizontal a 360 px de ancho y con botones de al menos 44 px de alto.
8. **CA8.** Sin sesión, las páginas responden sin redirigir al inicio de sesión, y `/titulos` sigue pidiendo sesión. No hay estilos ni scripts en línea ni errores en la consola del navegador.

## Reglas de negocio

- Solo un título APROBADO aparece como válido. Los estados revocado y sustituido llegan con HU-3.2.
- La fecha de emisión es la fecha de aprobación, igual que en la credencial.
- La institución se escribe igual que en la credencial.
- La consulta es de solo lectura y no necesita cuenta.

## Validaciones

| Dato | Regla |
|---|---|
| Código de verificación | Antes de buscar, sin espacios al inicio o al final y en minúsculas. UUID de 36 caracteres (8-4-4-4-12 hexadecimales); si no cumple, «No encontrado» sin consultar la base de datos. |
| Campo del formulario (P7) | Obligatorio: vacío → «Escriba el código de verificación · Enter the verification code» bajo el campo. |
| Fechas (P8) | Como en la credencial, en los dos idiomas: «10 de octubre de 2026 · October 10, 2026». La consulta lleva la hora y la zona: «(hora de Panamá, UTC−5) · (Panama time, UTC−5)» (acordado el 26/10/2026). |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Exactitud de la verificación | Casos con el resultado esperado ÷ casos: válido, inexistente, formato inválido, pendiente y rechazado. | MockMvc | 100 % |
| Protección de datos | Respuestas públicas que contienen el documento completo, el revisor o el id interno. | MockMvc (busca en el HTML) | 0 |
| Eficiencia de rendimiento | Tiempo de respuesta de `/verificar/{código}`. | Prometheus (`http_server_requests`) | Menos de 1 s (acordado el 26/10/2026) |
| Seguridad | Hallazgos de Nikto con la nueva ruta pública. | Nikto 2.6.1 (cierre del sprint) | 2 o menos |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Exponer datos personales en una página pública. | Datos mínimos y la prueba de CA4. Además, `<meta name="robots" content="noindex">` para que los buscadores no guarden los resultados. |
| Que alguien adivine códigos. | El UUID es aleatorio, y un código inexistente y uno no aprobado dan el mismo mensaje. Limitar la cantidad de intentos queda fuera del alcance. |
| Abrir sin querer otras rutas. | `/verificar/**` ya es pública en `SecurityConfig`: no hace falta tocarla. CA8 comprueba que `/titulos` siga protegido. |
| Inyectar código desde la URL. | El código se valida como UUID antes de usarse y se muestra con `th:text`. |
| En la demostración, el teléfono no puede abrir `localhost`. | Ver HU-2.5: hotspot del teléfono y `TITULOS_VERIFICACION_URL` con la IP de la computadora. |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-3.1.1 | P7 y la ruta `/verificar/{código}`: validación y normalización del código, búsqueda por identificador, solo títulos APROBADOS. | Henry Birminghan | #32 |
| T-3.1.2 | P8: datos mínimos, enmascarado, textos en dos idiomas, fecha de la consulta y «No encontrado». | Henry Birminghan | #32 |
| T-3.1.3 | Pruebas con MockMvc (casos de CA5, acceso sin sesión y ausencia del documento completo) y evidencia con capturas en el celular. | Henry Birminghan | #32 |

## Estado

En revisión (Sprint 6, PR #32). Evidencias: `docs/evidencias/HU-3.1-pruebas.txt` y las capturas `HU-3.1-*` en la misma carpeta.

_Criterios aprobados por el Product Owner en la planificación del Sprint 6 (26/10/2026)._
