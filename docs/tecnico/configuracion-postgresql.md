# Setup de PostgreSQL para correr la app

Guía para dejar lista la base de datos y poder correr **Títulos Universitarios Digitales**
en una máquina nueva. La app usa PostgreSQL en runtime (`jdbc:postgresql://localhost:5432/titulos_db`,
usuario `postgres`, contraseña vía variable de entorno `DB_PASSWORD`). Las tablas se crean solas
(`spring.jpa.hibernate.ddl-auto=update`) y al arrancar se siembran 2 usuarios de prueba.

> Las pruebas (`.\mvnw.cmd clean test`) usan H2 en memoria y **no** necesitan PostgreSQL.

## 1. Verifica que PostgreSQL esté instalado y corriendo

En PowerShell:

```powershell
Get-Service *postgres*
```

Debe aparecer un servicio (ej. `postgresql-x64-17`) en estado **Running**.
Si no está instalado, instala PostgreSQL primero.

## 2. La contraseña del usuario `postgres` — dos casos

**Caso A: la recuerdas** → úsala tal cual (sáltate al paso 3). No necesita ser `postgres`.

**Caso B: NO la recuerdas** → resetéala con [`reset_pg.ps1`](reset_pg.ps1) (necesita
administrador y es solo para tu PostgreSQL local). Ajusta el `17` del script si tu versión es
otra y ejecútalo; acepta el aviso de Windows (UAC):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File docs\tecnico\reset_pg.ps1
```

El script te pide la contraseña nueva (no se escribe en el repositorio), respalda
`pg_hba.conf`, pone la autenticación en `trust` un momento, cambia la clave y restaura
siempre la configuración original, aunque algo falle.

## 3. Crea la base de datos `titulos_db`

En PowerShell (reemplaza `TU_CLAVE` por tu contraseña):

```powershell
$env:PGPASSWORD="TU_CLAVE"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE titulos_db;"
```

## 4. Corre la app pasando `DB_PASSWORD`

En **IntelliJ**: Run → Edit Configurations → Environment variables →

```
DB_PASSWORD=TU_CLAVE
```

En **terminal (cmd)**:

```cmd
set "DB_PASSWORD=TU_CLAVE" && .\mvnw.cmd spring-boot:run
```

## 5. Entra a la app

<http://localhost:8080/login> → `aprobador1` / `clave1234`
(La raíz `/` redirige a `/titulos` gracias al `HomeController`.)

Usuarios sembrados automáticamente:

| Usuario | Contraseña | Rol |
|---|---|---|
| `aprobador1` | `clave1234` | APROBADOR |
| `registrador1` | `clave1234` | REGISTRADOR |

Si el login falla con `aprobador1`/`clave1234`: tu tabla `usuarios` tiene datos viejos.
En pgAdmin/psql sobre `titulos_db`:

```sql
DELETE FROM usuarios;
```

Reinicia la app y se recrean solos.

## Troubleshooting

### La app no arranca: `Unable to establish loopback connection`

Si al correr `.\mvnw.cmd spring-boot:run` falla con algo como:

```
APPLICATION FAILED TO START
...
Caused by: org.apache.catalina.LifecycleException: Protocol handler start failed
Caused by: java.io.IOException: Unable to establish loopback connection
Caused by: java.net.SocketException: Invalid argument: connect
    at sun.nio.ch.PipeImpl$Initializer$LoopbackConnector.run(...)
```

**No es la app ni la base de datos.** En JDK 21 sobre Windows, el selector NIO que Tomcat
necesita crea un pipe interno usando un socket **AF_UNIX**. El JDK ubica ese socket en la
carpeta temporal del usuario (`%LOCALAPPDATA%\Temp`); si esa ruta es larga (usuario con
nombre largo), el `connect` del socket excede el límite de ~108 caracteres de AF_UNIX en
Windows y falla con `Invalid argument`. Rompe el arranque de cualquier servidor Java.

**Fix (verificado):** decirle al JDK que cree ese socket en una ruta corta, vía la variable
de entorno `JAVA_TOOL_OPTIONS`. Todo java (IntelliJ incluido) la lee solo. En PowerShell:

```powershell
if (-not (Test-Path 'C:\Temp')) { New-Item -ItemType Directory -Path 'C:\Temp' | Out-Null }
[Environment]::SetEnvironmentVariable('JAVA_TOOL_OPTIONS','-Djdk.net.unixdomain.tmpdir=C:\Temp','User')
```

Cierra y reabre IntelliJ / la terminal para que tome la variable, y vuelve a correr la app.
(Para una sola corrida sin variable permanente:
`.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=C:\Temp"`.)

> Un `netsh winsock reset` (admin) + reinicio **no** arregla esto, porque no es Winsock;
> es la longitud de la ruta del socket AF_UNIX.

### `Port 8080 was already in use`

Hay otra instancia de la app corriendo. Ciérrala (en IntelliJ: botón Stop) o mátala:

```powershell
Get-Process java | Stop-Process -Force
```

Luego vuelve a arrancar.
