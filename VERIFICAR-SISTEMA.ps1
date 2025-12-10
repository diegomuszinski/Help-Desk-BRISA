# Script para verificar status do sistema HelpDesk

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Verificação do Sistema HelpDesk" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
Write-Host "[1/4] Verificando Java..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion -match "21") {
    Write-Host "✓ Java 21 OK" -ForegroundColor Green
} else {
    Write-Host "✗ Java 21 não encontrado!" -ForegroundColor Red
    Write-Host "Solução: Execute configurar-java.ps1" -ForegroundColor Yellow
}
Write-Host ""

# Verificar PostgreSQL
Write-Host "[2/4] Verificando PostgreSQL (porta 5432)..." -ForegroundColor Yellow
$pgResult = Test-NetConnection -ComputerName localhost -Port 5432 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($pgResult) {
    Write-Host "✓ PostgreSQL rodando" -ForegroundColor Green
} else {
    Write-Host "✗ PostgreSQL não está rodando!" -ForegroundColor Red
    Write-Host "Solução: Inicie o serviço PostgreSQL" -ForegroundColor Yellow
}
Write-Host ""

# Verificar Backend
Write-Host "[3/4] Verificando Backend (porta 8080)..." -ForegroundColor Yellow
$backendResult = Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($backendResult) {
    Write-Host "✓ Backend rodando" -ForegroundColor Green
    
    # Testar endpoint de login
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Backend respondendo corretamente" -ForegroundColor Green
        }
    } catch {
        Write-Host "⚠ Backend está na porta 8080 mas não responde" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ Backend não está rodando!" -ForegroundColor Red
    Write-Host "Solução: Execute INICIAR-SISTEMA.bat ou inicie manualmente" -ForegroundColor Yellow
}
Write-Host ""

# Verificar Frontend
Write-Host "[4/4] Verificando Frontend (porta 5173)..." -ForegroundColor Yellow
$frontendResult = Test-NetConnection -ComputerName localhost -Port 5173 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($frontendResult) {
    Write-Host "✓ Frontend rodando" -ForegroundColor Green
} else {
    Write-Host "⚠ Frontend não está rodando na porta 5173" -ForegroundColor Yellow
    
    # Tentar porta alternativa 5174
    $frontend2Result = Test-NetConnection -ComputerName localhost -Port 5174 -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($frontend2Result) {
        Write-Host "✓ Frontend rodando na porta 5174" -ForegroundColor Green
    } else {
        Write-Host "✗ Frontend não encontrado!" -ForegroundColor Red
        Write-Host "Solução: Execute INICIAR-SISTEMA.bat ou inicie manualmente" -ForegroundColor Yellow
    }
}
Write-Host ""

# Resumo
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Resumo" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
$javahealthy = if ($javaVersion -match "21") { "✓" } else { "✗" }
$pghealthy = if ($pgResult) { "✓" } else { "✗" }
$backendhealthy = if ($backendResult) { "✓" } else { "✗" }
$frontendhealthy = if ($frontendResult -or $frontend2Result) { "✓" } else { "✗" }

Write-Host "$javahealthy Java 21"
Write-Host "$pghealthy PostgreSQL"
Write-Host "$backendhealthy Backend (8080)"
Write-Host "$frontendhealthy Frontend (5173/5174)"
Write-Host ""

if ($pgResult -and $backendResult -and ($frontendResult -or $frontend2Result)) {
    Write-Host "Sistema está funcionando corretamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Acesse: http://localhost:5173" -ForegroundColor Cyan
} else {
    Write-Host "Sistema com problemas! Verifique os itens marcados com ✗" -ForegroundColor Red
}
