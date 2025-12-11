# Script para configurar Java 21 e iniciar o backend
# Uso: .\start-backend-java21.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  HelpDesk Backend - Java 21" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Auto-detectar Java 21
Write-Host "Detectando Java instalado..." -ForegroundColor Yellow

# Tentar encontrar Java no PATH atual
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
    Write-Host "Java encontrado no PATH" -ForegroundColor Green
} else {
    # Tentar caminhos comuns
    $commonPaths = @(
        "$env:USERPROFILE\scoop\apps\openjdk21\current",
        "$env:USERPROFILE\scoop\apps\openjdk21\21.0.2-13",
        "$env:USERPROFILE\scoop\apps\openjdk\current",
        "C:\Program Files\Eclipse Adoptium\jdk-21*",
        "C:\Program Files\Java\jdk-21*"
    )

    $javaFound = $false
    foreach ($path in $commonPaths) {
        $resolved = Resolve-Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved -and (Test-Path "$resolved\bin\java.exe")) {
            $env:JAVA_HOME = $resolved.Path
            $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
            $javaFound = $true
            Write-Host "Java encontrado em: $env:JAVA_HOME" -ForegroundColor Green
            break
        }
    }

    if (-not $javaFound) {
        Write-Host "ERRO: Java 21 não encontrado!" -ForegroundColor Red
        Write-Host "Instale o Java 21:" -ForegroundColor Yellow
        Write-Host "  - Via scoop: scoop install openjdk21" -ForegroundColor White
        Write-Host "  - Via Eclipse Adoptium: https://adoptium.net/" -ForegroundColor White
        exit 1
    }
}

Write-Host ""
Write-Host "Java configurado:" -ForegroundColor Green
java -version
Write-Host ""

# Configurar variáveis de ambiente
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/helpdesk'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = 'teste'
$env:JWT_SECRET = 'jRF2kiSxvvqIgpE/QphKpnUvTr+e5/8wp5S2thFfVfk='
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173,http://localhost:5174,http://localhost:5175'
$env:SHOW_SQL = 'true'
$env:FORMAT_SQL = 'true'

Write-Host "Iniciando backend..." -ForegroundColor Cyan
Write-Host ""

.\gradlew.bat bootRun
