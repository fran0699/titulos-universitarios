# HU-4.5 · Páginas de error propias

**Dueño: Carol · Técnica · Seguridad · Prioridad Media · 2 pts · Sprint 7**

## Redacción

> Como institución, necesito páginas de error propias (403, 404 y 500) que no muestren detalles técnicos del servidor.

**Redacción anterior:** sin cambios.

**Aporte al producto:** Hoy un error muestra la «Whitelabel Error Page» de Spring: no dice qué hacer, se ve distinta al resto y puede revelar que la aplicación usa Spring Boot.

## Criterios de aceptación

1. **CA1.** Los errores 403, 404 y 500 muestran la página P9 de la [guía de pantallas](../guia-de-pantallas.md#p9--página-de-error): el código, un mensaje en lenguaje simple y qué puede hacer el usuario («Volver a Títulos» si tiene sesión; «Iniciar sesión» si no).
2. **CA2.** Con sesión, la página lleva el encabezado común; sin sesión, no lleva menú interno.
3. **CA3.** Ninguna página de error muestra trazas, nombres de clases, mensajes de excepción ni la versión del servidor.
4. **CA4.** Cada página responde con su código real (404 responde 404, no 200).
5. **CA5.** La «Whitelabel Error Page» no aparece en ninguna de estas pruebas: una ruta que no existe, una pantalla prohibida para el rol, la credencial de un título no aprobado y un error interno provocado en la prueba.
6. **CA6.** El usuario técnico de Prometheus que inicia sesión en las pantallas ve el 403 sin enlaces a pantallas que no puede usar.
7. **CA7.** La verificación pública conserva su propia página de «No encontrado · Not found» ([HU-3.1](HU-3.1.md)).
8. **CA8.** Las páginas no tienen estilos ni scripts en línea.

## Reglas de negocio

- Los mensajes no dicen si un título existe o no: el 404 de la credencial es el mismo que el de cualquier ruta inexistente.
- La configuración deja explícito que nunca se incluyen la traza ni el mensaje de la excepción en la respuesta.

## Validaciones

No hay datos de entrada: la página solo usa el código de error y si hay sesión.

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Fugas de información | Respuestas de error con traza, nombre de clase o «Whitelabel». | JUnit (MockMvc) y Nikto | 0 |
| Códigos correctos | Errores probados que responden con su código real ÷ probados. | JUnit (MockMvc) | 100 % |
| Adecuación funcional | Elementos de P9 presentes en cada código ÷ requeridos. | JUnit y revisión contra la guía | 100 % |
| Hallazgos de Nikto | Comparados con el sprint anterior. | Nikto 2.6.1 | Iguales o menos (Sprint 6: 2) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| La página de error falla a su vez (por ejemplo, al armar el menú o el contador de pendientes) y aparece un error en blanco. | Plantillas simples; pruebas de cada código con cada rol y sin sesión. |
| Una regla nueva de seguridad deja `/error` sin acceso público y la página de error termina en el inicio de sesión. | `/error` sigue con `permitAll()`; prueba sin sesión. |

## Dependencias

- Ninguna historia pendiente.
- Comparte el encabezado común con las demás pantallas: archivo compartido, no dependencia.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.5.1 | Plantillas de 403, 404 y 500 según P9 y configuración sin trazas (CA1 a CA4, CA6 a CA8). | Carol | — |
| T-4.5.2 | Pruebas de CA5 con cada rol y sin sesión; evidencia (capturas de cada página) y la fila de P9 en la tabla de estado de la guía. | Carol | — |

## Estado

Pendiente.
