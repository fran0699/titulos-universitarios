$ErrorActionPreference = 'Stop'

$docker = Get-Command docker -ErrorAction SilentlyContinue
if (-not $docker) {
    $dockerCandidates = @(
        (Join-Path $env:LOCALAPPDATA 'Programs\DockerDesktop\resources\bin\docker.exe'),
        'C:\Program Files\Docker\Docker\resources\bin\docker.exe'
    )
    $dockerPath = $dockerCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
    if ($dockerPath) {
        $docker = Get-Item -LiteralPath $dockerPath
    }
}
if (-not $docker) {
    throw 'No se encontro Docker CLI. Inicia Docker Desktop o instala Docker Desktop.'
}
$dockerExecutable = if ($docker.Source) { $docker.Source } else { $docker.FullName }
$dockerDirectory = Split-Path -Parent $dockerExecutable
$env:Path = "$dockerDirectory;$env:Path"

$username = Read-Host 'Usuario APROBADOR existente en la aplicacion (por defecto aprobador1)'
if ([string]::IsNullOrWhiteSpace($username)) {
    $username = 'aprobador1'
}
if ($username -notmatch '^[a-zA-Z0-9._-]+$') {
    throw 'El nombre de usuario solo puede contener letras, numeros, punto, guion y guion bajo.'
}
$securePassword = Read-Host "Contrasena de $username" -AsSecureString
if ($securePassword.Length -eq 0) {
    throw 'La contrasena no puede estar vacia.'
}
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$secretDirectory = Join-Path $PSScriptRoot '.secrets'
$secretPath = Join-Path $secretDirectory 'aprobador.password'

try {
    $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    New-Item -ItemType Directory -Path $secretDirectory -Force | Out-Null
    [IO.File]::WriteAllText($secretPath, $password, [Text.UTF8Encoding]::new($false))
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    Remove-Variable password -ErrorAction SilentlyContinue
}

$configPath = Join-Path $PSScriptRoot 'prometheus.yml'
$runtimeConfigPath = Join-Path $secretDirectory 'prometheus.yml'
$config = Get-Content -LiteralPath $configPath -Raw
$config = $config.Replace('username: aprobador1', "username: $username")
[IO.File]::WriteAllText($runtimeConfigPath, $config, [Text.UTF8Encoding]::new($false))

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $projectRoot
try {
    & $dockerExecutable compose -f docker-compose.prometheus.yml up -d
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose termino con codigo $LASTEXITCODE. Verifica que Docker Desktop este iniciado."
    }
}
finally {
    Pop-Location
}

Write-Host 'Prometheus esta iniciando en http://localhost:9090. Espera un scrape (15 s) y revisa Status > Targets.'
