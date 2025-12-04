# =============================================================================
# SCRIPT DE TESTE - MODO PRODUCAO
# =============================================================================
# Este script simula o ambiente de producao localmente
# para verificar se o endpoint /api/test esta bloqueado
# =============================================================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  TESTE: Endpoint /api/test em PRODUCAO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Configurar variavel para modo producao
$env:SPRING_PROFILES_ACTIVE = "prod"

Write-Host "[1/3] Configurando profile: PRODUCAO" -ForegroundColor Yellow
Write-Host "      SPRING_PROFILES_ACTIVE = prod`n" -ForegroundColor Gray

# Iniciar backend em background
Write-Host "[2/3] Iniciando backend em modo producao..." -ForegroundColor Yellow
$job = Start-Job -ScriptBlock {
    Set-Location "C:\Users\ResTIC55\Downloads\Arquivos\helpdesk-backend\helpdesk-api"
    $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    $env:SPRING_PROFILES_ACTIVE = "prod"
    
    # Carregar variaveis do .env
    $envContent = Get-Content ".env"
    foreach ($line in $envContent) {
        if ($line -match '^([^=]+)=(.*)$' -and $line -notmatch '^\s*#') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
    
    # Forcar profile prod
    [Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "prod", "Process")
    
    & ".\gradlew.bat" bootRun
}

Write-Host "      Aguardando backend iniciar (30 segundos)..." -ForegroundColor Gray
Start-Sleep -Seconds 30

Write-Host "`n[3/3] Testando endpoint /api/test..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/test/bcrypt?email=admin@admin.net&senha=123456" -Method GET -ErrorAction Stop
    
    Write-Host "`n =====================================" -ForegroundColor Red
    Write-Host "  FALHOU: Endpoint esta ACESSIVEL!" -ForegroundColor Red
    Write-Host " =====================================" -ForegroundColor Red
    Write-Host "`nStatus: $($response.StatusCode)" -ForegroundColor Red
    Write-Host "O endpoint /api/test NAO deveria estar disponivel em producao!`n" -ForegroundColor Red
    
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "`n =====================================" -ForegroundColor Green
        Write-Host "  SUCESSO: Endpoint BLOQUEADO!" -ForegroundColor Green
        Write-Host " =====================================" -ForegroundColor Green
        Write-Host "`nStatus: 404 Not Found" -ForegroundColor Green
        Write-Host "O endpoint /api/test esta corretamente desabilitado em producao!`n" -ForegroundColor Green
    } else {
        Write-Host "`nErro inesperado: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Parar backend
Write-Host "Parando backend..." -ForegroundColor Gray
Stop-Job -Job $job
Remove-Job -Job $job

Write-Host "`nTeste concluido!`n" -ForegroundColor Cyan
