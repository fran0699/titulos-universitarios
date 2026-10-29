# Restablece la contrasena del usuario postgres de la PostgreSQL LOCAL (solo para desarrollo).
# Pide la contrasena nueva al ejecutarse: no se escribe en el repositorio.
# Ajusta la version (17) si tu PostgreSQL es otra.
if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Start-Process powershell.exe "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs; exit
}
$ErrorActionPreference = "Stop"
$data = "C:\Program Files\PostgreSQL\17\data"
$hba  = "$data\pg_hba.conf"; $bak = "$data\pg_hba.conf.reset-bak"
$psql = "C:\Program Files\PostgreSQL\17\bin\psql.exe"; $svc = "postgresql-x64-17"

$segura = Read-Host "Contrasena nueva para el usuario postgres" -AsSecureString
$nueva  = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura))
if ([string]::IsNullOrWhiteSpace($nueva)) { throw "La contrasena no puede estar vacia." }
$nuevaSql = $nueva.Replace("'", "''")

Copy-Item $hba $bak -Force
try {
    (Get-Content $hba) -replace 'scram-sha-256','trust' | Set-Content $hba -Encoding ascii
    Restart-Service $svc; Start-Sleep 4
    & $psql -U postgres -h localhost -c "ALTER USER postgres WITH PASSWORD '$nuevaSql';"
} finally {
    # Pase lo que pase, se restaura la configuracion original de autenticacion.
    Copy-Item $bak $hba -Force; Restart-Service $svc; Start-Sleep 4; Remove-Item $bak -Force
}
Write-Host "LISTO: la contrasena de postgres cambio. Usala en DB_PASSWORD."
Read-Host "Enter para cerrar"
