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

Prometheus queda en `http://localhost:9090`, accesible solo desde el propio equipo. Abre **Status > Targets**: el job `titulos-universitarios` debe aparecer `UP`. Luego consulta en **Graph**:

```promql
jvm_memory_used_bytes{area="heap"}
```

```promql
rate(http_server_requests_seconds_count{uri="/actuator/prometheus"}[1m])
```

El scrape va a `host.docker.internal:8080/actuator/prometheus` cada 15 segundos, con HTTP Basic. La contraseña no se guarda en el YAML versionado. Para detener el servicio: `docker compose -f docker-compose.prometheus.yml down`; los datos quedan en el volumen `prometheus_data`.

## Quién puede leer las métricas (HU-4.10)

| Usuario | `/actuator/**` | Pantallas (`/titulos`) |
|---|---|---|
| `prometheus` (MONITOREO) | 200 con HTTP Basic | 403 |
| `aprobador1`, `registrador1` | 403 | Solo con el formulario de inicio de sesión |

HTTP Basic solo funciona en `/actuator/**`; en las pantallas no se acepta y redirige al inicio de sesión. Evidencia: `docs/evidencias/HU-4.10-matriz-acceso.txt` y `docs/evidencias/HU-4.10-prometheus-target-up.png`.

## Si ya usabas Prometheus antes de HU-4.10

Vuelve a ejecutar el script con el usuario `prometheus`. Puedes borrar `monitoring/prometheus/.secrets/aprobador.password`, que ya no se usa.