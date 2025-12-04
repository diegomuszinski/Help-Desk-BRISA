# Script para reiniciar o backend
# Parar processos Java rodando na porta 8080

Write-Host "🔍 Procurando processos na porta 8080..." -ForegroundColor Cyan

$process = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | 
    Select-Object -ExpandProperty OwningProcess -First 1

if ($process) {
    Write-Host "⚠️  Encontrado processo PID: $process" -ForegroundColor Yellow
    Write-Host "🛑 Parando processo..." -ForegroundColor Red
    Stop-Process -Id $process -Force
    Start-Sleep -Seconds 2
    Write-Host "✅ Processo parado!" -ForegroundColor Green
} else {
    Write-Host "✅ Porta 8080 livre!" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔧 Configurando Java 17..." -ForegroundColor Cyan
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "🚀 Iniciando backend..." -ForegroundColor Cyan
Write-Host ""

.\gradlew.bat bootRun
