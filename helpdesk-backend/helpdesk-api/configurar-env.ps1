# =============================================================================
# SCRIPT DE CONFIGURACAO DE VARIAVEIS DE AMBIENTE
# =============================================================================
# Este script configura as variaveis de ambiente do arquivo .env
# para a sessao atual do PowerShell
# =============================================================================

Write-Host "Configurando variaveis de ambiente do Help Desk API..." -ForegroundColor Cyan

$envFile = Join-Path $PSScriptRoot ".env"

if (-not (Test-Path $envFile)) {
    Write-Host "Arquivo .env nao encontrado!" -ForegroundColor Red
    Write-Host "Copie .env.example para .env e configure os valores." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Comando: Copy-Item .env.example .env" -ForegroundColor White
    exit 1
}

Write-Host "Lendo arquivo: $envFile" -ForegroundColor Gray

$envContent = Get-Content $envFile -ErrorAction Stop

$varsCount = 0
foreach ($line in $envContent) {
    # Ignorar linhas vazias e comentarios
    if ($line -match '^\s*#' -or $line -match '^\s*$') {
        continue
    }
    
    # Parse linha no formato KEY=VALUE
    if ($line -match '^([^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        # Remover aspas se existirem
        $value = $value -replace '^"(.*)"$', '$1'
        $value = $value -replace "^'(.*)'$", '$1'
        
        # Definir variavel de ambiente
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
        $varsCount++
        
        # Mostrar variavel (mascarar valores sensiveis)
        if ($key -match "PASSWORD|SECRET|TOKEN|KEY") {
            Write-Host "  OK $key = ********" -ForegroundColor Green
        } else {
            Write-Host "  OK $key = $value" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "$varsCount variaveis de ambiente configuradas com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Para iniciar o backend com as variaveis, execute:" -ForegroundColor Cyan
Write-Host "   .\gradlew.bat bootRun" -ForegroundColor White
Write-Host ""

# Opcional: Tambem definir JAVA_HOME se necessario
if ($env:JAVA_HOME) {
    Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
} else {
    Write-Host "JAVA_HOME nao esta definido!" -ForegroundColor Yellow
    Write-Host "Configure com: `$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'" -ForegroundColor White
}
