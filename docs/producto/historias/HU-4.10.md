# HU-4.10 · Usuario técnico de Prometheus

**Responsable 3: Francisco Lata · Técnica · Seguridad · Prioridad Alta · 2 pts**

## Redacción

> Como institución, necesito que las métricas solo las lea un usuario técnico de Prometheus, sin acceso a las pantallas, y que HTTP Basic solo funcione en `/actuator/**`, para que una contraseña filtrada de Prometheus no permita registrar ni aprobar títulos.

**Redacción anterior:** Como institución, necesito que Prometheus use un usuario técnico que solo pueda leer métricas, y que la autenticación HTTP Basic se limite a `/actuator/**`, para aplicar el principio de mínimo privilegio.

**Por qué cambió:** Se nombra el riesgo concreto que se evita y se incluye la decisión del equipo (20/10/2026): el APROBADOR deja de ver las métricas.

**Aporte al MVP:** Seguridad: una credencial de monitoreo filtrada no da acceso a registrar ni aprobar títulos.

## Criterios de aceptación

1. **CA1.** Prometheus muestra el target en estado UP usando el usuario técnico.
2. **CA2.** El usuario técnico recibe 403 en cualquier pantalla de /titulos.
3. **CA3.** El APROBADOR y el REGISTRADOR reciben 403 en /actuator/prometheus.
4. **CA4.** HTTP Basic no da acceso a /titulos: curl -u con un usuario válido no devuelve 200.
5. **CA5.** La contraseña no aparece en el repositorio (git grep no la encuentra).
6. **CA6.** El usuario técnico se crea también en una base de datos que ya tiene usuarios.

## Reglas de negocio

- Mínimo privilegio: cada cuenta tiene solo el acceso que necesita.
- Ninguna credencial se guarda en el repositorio.
- El usuario técnico solo lee métricas; el personal (APROBADOR y REGISTRADOR) no accede a `/actuator/**`.

## Validaciones

| Dato | Regla |
|---|---|
| Contraseña del usuario técnico | Se toma de una variable de entorno; obligatoria, de al menos 12 caracteres (acordado el 21/10/2026). |
| Variable ausente | El usuario no se crea y se registra un aviso en el log. |
| Usuario técnico | Se crea al iniciar solo si no existe. |

## Métricas QA

| Indicador | Cómo se mide | Herramienta | Meta |
|---|---|---|---|
| Seguridad – matriz de acceso | Combinaciones rol × ruta que responden lo esperado ÷ combinaciones totales. | curl | 100 % |
| Seguridad – hallazgos de Nikto | Hallazgos del escaneo comparados con el Sprint 4. | Nikto 2.6.1 | ≤ 2 hallazgos |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Las dos SecurityFilterChain quedan mal configuradas: se bloquea el login o una ruta queda abierta. | Probar la matriz de acceso completa antes de pedir revisión. |
| El resto del equipo debe definir la variable de entorno. | Explicarlo como «Acción requerida para el equipo» en el PR y en docs/prometheus.md. |

## Tareas

| Tarea | Qué se hace | Responsable | PR |
|---|---|---|---|
| T-4.10.1 | Rol `MONITOREO` y usuario técnico `prometheus`, creado al iniciar si no existe, con la contraseña desde `PROMETHEUS_PASSWORD`. | Francisco Lata | #24 |
| T-4.10.2 | Dos cadenas de seguridad: HTTP Basic solo en `/actuator/**` para `MONITOREO`; las pantallas, solo con formulario y denegadas por defecto al usuario técnico. | Francisco Lata | #24 |
| T-4.10.3 | Prometheus con el usuario técnico (`prometheus.yml`, `Start-Prometheus.ps1` y `docs/prometheus.md`) y target en estado UP. | Francisco Lata | #24 |

## Estado

Completo (Sprint 5). Evidencias: `docs/evidencias/HU-4.10-matriz-acceso.txt` (12 de 12) y `HU-4.10-prometheus-target-up.png`. Acción manual para bases existentes: README, sección «Cambios manuales en la base de datos local».

_Criterios aprobados por el Product Owner en la planificación del Sprint 5 (21/10/2026)._
