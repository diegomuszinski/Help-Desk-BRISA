# Script para configurar Java 21 e iniciar o backend
# Uso: .\start-backend-java21.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  HelpDesk Backend - Java 21" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Configurar Java 21
$env:JAVA_HOME = "C:\Users\ResTIC55\scoop\apps\openjdk21\21.0.2-13"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

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
