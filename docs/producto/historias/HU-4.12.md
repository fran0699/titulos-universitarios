# HU-4.12 · Indicador de papel evitado

**Dueño: por asignar · Técnica · Verificabilidad internacional · Prioridad Alta · 2 pts · Sprint por planificar**

## Redacción

> Como institución, quiero que el sistema cuente las credenciales emitidas y las verificaciones en línea y calcule las hojas de papel que se evitaron, visible en Prometheus, para medir el impacto ambiental del sistema.

**Aporte al producto:** Es la innovación ecológica del proyecto. Deja un número concreto que mostrar: cuántas hojas y trámites en papel evitó el sistema. Usa Prometheus, que ya está en el proyecto.

## Qué se ve en la demostración

Se descargan credenciales y se verifican títulos; en Prometheus se consulta `titulos_papel_evitado_hojas_total` y el número sube.

## Criterios de aceptación

1. **CA1.** Cada credencial PDF generada con éxito suma 1 a `titulos_credenciales_emitidas_total`.
2. **CA2.** Cada consulta de verificación (la página y, cuando exista, la API de [HU-3.5](HU-3.5.md)) suma a `titulos_verificaciones_total` con la etiqueta `resultado` en `valida` o `no_encontrada`.
3. **CA3.** `titulos_papel_evitado_hojas_total` suma las hojas evitadas por cada credencial emitida y por cada verificación válida. Una credencial que falla o una verificación no encontrada no suman.
4. **CA4.** Los factores salen de `titulos.ecologico.hojas-por-credencial` (1 por defecto) y `titulos.ecologico.hojas-por-verificacion` (2 por defecto), y se cambian sin tocar el código.
5. **CA5.** Las métricas aparecen en `/actuator/prometheus` con el usuario técnico, y `docs/prometheus.md` trae las consultas para verlas, por ejemplo `increase(titulos_papel_evitado_hojas_total[30d])`.
6. **CA6.** Ninguna métrica lleva datos personales ni el código de verificación.

## Reglas de negocio

- Los factores son una estimación del equipo, no una medición: 1 hoja por credencial emitida (la credencial impresa que el PDF reemplaza) y 2 por verificación válida (la carta de solicitud y la constancia en papel que la consulta en línea evita).
- Se cuenta cada PDF generado, no cada título: descargar la misma credencial dos veces cuenta dos veces.
- Los contadores se reinician cuando se reinicia la aplicación; por eso la consulta usa `increase()` y no el valor directo ([Prometheus](../../prometheus.md)).
- Las verificaciones se cuentan dentro de `VerificacionService`, así que la API de HU-3.5 queda contada sin cambios adicionales.

## Dependencias

- Ninguna: usa la credencial PDF ([HU-2.4](HU-2.4.md)), la verificación pública ([HU-3.1](HU-3.1.md)) y Prometheus con Actuator (HU-4.4), ya completos.
- Comparte `VerificacionService` con [HU-3.5](HU-3.5.md): archivo compartido, no dependencia.

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.12.1 | Contadores en la generación de la credencial y en `VerificacionService`, y factores por propiedad (CA1 a CA4 y CA6). | — | — |
| T-4.12.2 | Pruebas con `SimpleMeterRegistry` (CA1 a CA4), consultas en `docs/prometheus.md` (CA5) y evidencia: captura de Prometheus con el número antes y después de usar el sistema. | — | — |

## Estado

Pendiente.
