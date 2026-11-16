# HU-4.8 · Pruebas de acceso por rol y cabeceras de seguridad

**Dueño: por asignar · Técnica · Seguridad · Prioridad Media · 3 pts · Sprint por planificar**

## Redacción

> Como equipo, queremos pruebas de integración con MockMvc de las reglas por rol y de las cabeceras de seguridad, para detectar automáticamente si un cambio abre un acceso indebido.

**Redacción anterior:** sin cambios.

**Aporte al producto:** Hoy la tabla «Rutas y acceso» de [arquitectura y seguridad](../../tecnico/arquitectura-y-seguridad.md#rutas-y-acceso) se comprobó a mano con curl (HU-4.10 y HU-4.11). Con esta historia, CircleCI la comprueba en cada Pull Request.

## Criterios de aceptación

1. **CA1.** Una prueba parametrizada recorre todas las rutas de la tabla «Rutas y acceso» que existan en main al desarrollar la historia, con los cuatro perfiles (REGISTRADOR, APROBADOR, MONITOREO y sin sesión), y comprueba la respuesta esperada de cada combinación (página, 403, ida al inicio de sesión o 401).
2. **CA2.** Las rutas POST se prueban con token CSRF, y además se comprueba que sin el token se rechazan.
3. **CA3.** Se comprueba que HTTP Basic no sirve para entrar a las pantallas ([HU-4.10](HU-4.10.md)).
4. **CA4.** Se comprueban las cabeceras de seguridad configuradas (CSP, Referrer-Policy, Permissions-Policy, X-Content-Type-Options y X-Frame-Options) en una pantalla, en una página pública y en `/actuator/**`.
5. **CA5.** Los valores esperados están en un solo lugar de la prueba, con el mismo orden de la tabla del documento, para que se puedan comparar fila por fila.
6. **CA6.** Todo corre en CircleCI y con `.\mvnw.cmd clean test` en cada computadora, sin pasos manuales.

## Reglas de negocio

- La tabla del documento y la prueba dicen lo mismo: si una cambia, la otra se cambia en el mismo Pull Request.
- Toda historia que agregue una ruta agrega su caso a esta prueba (se propone sumarlo a la Definición de Terminado cuando esta historia esté en main).

## Validaciones

No aplica: la historia solo agrega pruebas.

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Cobertura de la tabla de acceso | Rutas de la tabla con su prueba ÷ rutas de la tabla. | JUnit | 100 % |
| Combinaciones verificadas | Combinaciones ruta × perfil que responden lo esperado ÷ probadas. | JUnit (MockMvc) | 100 % |
| Cabeceras | Cabeceras esperadas presentes ÷ esperadas, en los tres tipos de página. | JUnit (MockMvc) | 100 % |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Repetir pruebas que ya existen (HU-3.4, HU-4.10 y HU-4.11). | Revisar esas pruebas antes de empezar; esta historia cubre la tabla completa y no las repite. |
| Otras historias del mismo sprint agregan rutas y la prueba queda incompleta. | Regla de negocio: cada historia agrega su caso; quien fusione después trae main. |
| La prueba depende de los usuarios de prueba, que [HU-4.6](HU-4.6.md) deja solo en modo demostración. | Usar usuarios simulados de Spring Security (`@WithMockUser`) o crearlos en la propia prueba. |

## Dependencias

- Ninguna historia pendiente.
- Las rutas que agreguen [HU-2.6](HU-2.6.md), [HU-2.7](HU-2.7.md) y [HU-3.2](HU-3.2.md) se suman a la prueba según la regla de negocio.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.8.1 | Prueba parametrizada de la tabla de acceso, con CSRF y HTTP Basic (CA1 a CA3, CA5 y CA6). | — | — |
| T-4.8.2 | Prueba de las cabeceras (CA4) y evidencia (salida de las pruebas en CircleCI). | — | — |

## Estado

Pendiente.
