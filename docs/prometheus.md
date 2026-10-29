# Prometheus local

## Requisitos

- Docker Desktop con el motor WSL 2 (en Windows) y Docker Compose.
- La aplicación Spring Boot activa en `http://localhost:8080`, arrancada con la variable de entorno `PROMETHEUS_PASSWORD` (mínimo 12 caracteres). Con ella, la aplicación crea al iniciar el usuario técnico `prometheus` (rol `MONITOREO`), el único que puede leer `/actuator/**` (HU-4.10).
- Si tu base de datos se creó antes de HU-4.10, la restricción `usuarios_rol_check` no admite el rol `MONITOREO`. Reemplázala en pgAdmin:

```sql
ALTER TABLE usuarios DROP CONSTRAINT usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('APROBADOR', 'REGISTRADOR', 'MONITOREO'));
```

## Inicio en Windows

Desde PowerShell, en la raíz del repositorio:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\monitoring\prometheus\Start-Prometheus.ps1
```

El script pide el usuario técnico (Enter para usar `prometheus`) y su contraseña de forma oculta (la misma de `PROMETHEUS_PASSWORD`). Guarda la contraseña en `monitoring/prometheus/.secrets/monitoreo.password`, que Git ignora, y levanta el contenedor.

Prometheus queda en `http://localhost:9090`, accesible solo desde el propio equipo. Abre **Status > Target health** (en versiones anteriores, **Status > Targets**): el job `titulos-universitarios` debe aparecer `UP`. Luego consulta en **Query**:

```promql
jvm_memory_used_bytes{area="heap"}
```

```promql
rate(http_server_requests_seconds_count{uri="/actuator/prometheus"}[1m])
```

El scrape va a `host.docker.internal:8080/actuator/prometheus` cada 15 segundos, con HTTP Basic. La contraseña no se guarda en el YAML versionado. Para detener el servicio: `docker compose -f docker-compose.prometheus.yml down`; los datos quedan en el volumen `prometheus_data`.

## Tiempo de respuesta por ruta

Sirve para las metas de rendimiento de las historias (por ejemplo, la verificación pública en menos de 1 s y la credencial en menos de 2 s). Pega cada consulta en **Query** y abre la pestaña **Table**.

Cantidad de peticiones por ruta y estado:

```promql
sum by (uri, status) (http_server_requests_seconds_count{uri=~"/verificar.*|/auditoria|/titulos/.*credencial"})
```

Promedio en milisegundos:

```promql
1000 * sum by (uri, status) (http_server_requests_seconds_sum{uri=~"/verificar.*|/auditoria|/titulos/.*credencial"}) / sum by (uri, status) (http_server_requests_seconds_count{uri=~"/verificar.*|/auditoria|/titulos/.*credencial"})
```

Máximo en milisegundos en las últimas 2 horas:

```promql
1000 * max by (uri, status) (max_over_time(http_server_requests_seconds_max{uri=~"/verificar.*|/auditoria|/titulos/.*credencial"}[2h]))
```

- La etiqueta `uri` es el patrón de la ruta del controlador (`/verificar/{codigo}`, `/titulos/{id}/credencial`). La página de inicio de sesión de Spring Security aparece como `UNKNOWN` y las redirecciones como `REDIRECTION`.
- La cantidad y la suma se acumulan en la aplicación desde que arranca: **se pierden al reiniciarla**. Por eso se miden antes de reiniciar.
- El máximo solo cubre los últimos 2 minutos en la aplicación; `max_over_time` lo busca en lo que Prometheus guardó. Si Prometheus estaba apagado durante las peticiones, el máximo de esas peticiones no se recupera.
- Para medir la primera petición después de arrancar (en frío), deja Prometheus encendido, reinicia la aplicación y usa `[5m]` en lugar de `[2h]`.

Resultados del cierre del Sprint 6: [`docs/evidencias/sprint-6-rendimiento.txt`](evidencias/sprint-6-rendimiento.txt).

## Quién puede leer las métricas (HU-4.10)

| Usuario | `/actuator/**` | Pantallas y demás rutas |
|---|---|---|
| `prometheus` (MONITOREO) | 200 con HTTP Basic | 403 en todas, incluso tras iniciar sesión con el formulario |
| `aprobador1`, `registrador1` | 403 | Solo con el formulario de inicio de sesión |

HTTP Basic solo funciona en `/actuator/**`; en las pantallas no se acepta y redirige al inicio de sesión. Evidencia: `docs/evidencias/HU-4.10-matriz-acceso.txt` y `docs/evidencias/HU-4.10-prometheus-target-up.png`.

**Rutas nuevas:** las pantallas deniegan por defecto. Toda ruta sin una regla propia en `SecurityConfig` queda solo para `REGISTRADOR` y `APROBADOR`. Si una ruta nueva debe ser pública (como `/verificar/**`), se declara con `permitAll()`. Si es de un solo rol, se agrega su regla antes de `anyRequest()`.

## Si ya usabas Prometheus antes de HU-4.10

Vuelve a ejecutar el script con el usuario `prometheus`. Puedes borrar `monitoring/prometheus/.secrets/aprobador.password`, que ya no se usa.