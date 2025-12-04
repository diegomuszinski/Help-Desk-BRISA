# =============================================================================
# SCRIPT DE INICIALIZACAO AUTOMATICA - FRONTEND + BACKEND
# =============================================================================
# Este script inicia o backend e o frontend automaticamente
# =============================================================================

Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║          HELP DESK - Iniciando Ambiente Completo            ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Caminho do backend
$backendPath = Join-Path $PSScriptRoot "..\helpdesk-backend\helpdesk-api"

# Verificar se o backend existe
if (-not (Test-Path $backendPath)) {
    Write-Host "❌ Erro: Backend não encontrado em $backendPath" -ForegroundColor Red
    exit 1
}

# Verificar se o backend já está rodando
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/login' -Method OPTIONS -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
    Write-Host "✅ Backend já está rodando na porta 8080" -ForegroundColor Green
    $backendRunning = $true
} catch {
    Write-Host "⏳ Backend não está rodando. Iniciando..." -ForegroundColor Yellow
    $backendRunning = $false
}

# Iniciar o backend se não estiver rodando
if (-not $backendRunning) {
    Write-Host ""
    Write-Host "📦 Iniciando Backend Spring Boot..." -ForegroundColor Cyan
    Write-Host "   Caminho: $backendPath" -ForegroundColor Gray
    
    # Configurar variáveis de ambiente
    $env:SPRING_PROFILES_ACTIVE = 'dev'
    $env:DB_URL = 'jdbc:postgresql://localhost:5432/helpdesk'
    $env:DB_USERNAME = 'postgres'
    $env:DB_PASSWORD = 'teste'
    $env:JWT_SECRET = 'jRF2kiSxvvqIgpE/QphKpnUvTr+e5/8wp5S2thFfVfk='
    $env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173,http://localhost:5174,http://localhost:5175'
    $env:SHOW_SQL = 'true'
    $env:FORMAT_SQL = 'true'
    
    # Iniciar backend em background
    $backendProcess = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; Write-Host 'Iniciando Backend...' -ForegroundColor Cyan; `$env:SPRING_PROFILES_ACTIVE='dev'; `$env:DB_URL='jdbc:postgresql://localhost:5432/helpdesk'; `$env:DB_USERNAME='postgres'; `$env:DB_PASSWORD='teste'; `$env:JWT_SECRET='jRF2kiSxvvqIgpE/QphKpnUvTr+e5/8wp5S2thFfVfk='; `$env:CORS_ALLOWED_ORIGINS='http://localhost:5173,http://localhost:5174,http://localhost:5175'; `$env:SHOW_SQL='true'; `$env:FORMAT_SQL='true'; .\gradlew.bat bootRun" -PassThru -WindowStyle Normal
    
    Write-Host "   Aguardando backend inicializar..." -ForegroundColor Yellow
    
    # Aguardar o backend iniciar (máximo 60 segundos)
    $maxAttempts = 30
    $attempt = 0
    $backendReady = $false
    
    while ($attempt -lt $maxAttempts -and -not $backendReady) {
        Start-Sleep -Seconds 2
        $attempt++
        
        try {
            $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/login' -Method OPTIONS -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
            $backendReady = $true
            Write-Host "   ✅ Backend pronto! ($($attempt * 2) segundos)" -ForegroundColor Green
        } catch {
            Write-Host "   ⏳ Tentativa $attempt de $maxAttempts..." -ForegroundColor Gray
        }
    }
    
    if (-not $backendReady) {
        Write-Host "   ⚠️  Backend demorou muito para iniciar. Verifique o terminal do backend." -ForegroundColor Yellow
        Write-Host "   Continuando mesmo assim..." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🌐 Iniciando Frontend Vue.js..." -ForegroundColor Cyan
Write-Host ""

# Iniciar o frontend (este processo ficará em primeiro plano)
npm run dev

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                    Aplicação Encerrada                       ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
