# Prometheus local

## Requisitos

- La aplicación Spring Boot debe estar activa en `http://localhost:8080`.
- En Docker Desktop debe estar habilitado Docker Compose.
- La base de datos debe contener un usuario con rol `APROBADOR` que pueda autenticarse en Actuator.

## Inicio en Windows

Desde PowerShell, en la raíz del repositorio, inicia Prometheus con el script. El script solicita el usuario APROBADOR y la contraseña de forma oculta, guarda la contraseña en un archivo ignorado por Git y levanta el contenedor:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\monitoring\prometheus\Start-Prometheus.ps1
```

El usuario sugerido es `aprobador1`, creado por `DataInitializer` únicamente cuando la tabla de usuarios está vacía. Si la base ya contiene cuentas, escribe el nombre y contraseña de un usuario existente con rol `APROBADOR`.

Prometheus queda en `http://localhost:9090`. Abre **Status > Targets**; el job `titulos-universitarios` debe aparecer `UP`. Luego consulta en **Graph**:

```promql
jvm_memory_used_bytes{area="heap"}
```

```promql
rate(http_server_requests_seconds_count{uri="/actuator/prometheus"}[1m])
```

El scrape va a `host.docker.internal:8080/actuator/prometheus` con HTTP Basic y está configurado cada 15 segundos. La contraseña no se almacena en el YAML versionado. Para detener el servicio: `docker compose -f docker-compose.prometheus.yml down`; los datos quedan en el volumen `prometheus_data`.
