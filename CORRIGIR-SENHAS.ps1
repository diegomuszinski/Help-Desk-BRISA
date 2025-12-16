# Script para corrigir as senhas dos usuarios de teste
param([string]$BackendUrl = "http://localhost:8080")

Write-Host "=== Correcao de Senhas ===" -ForegroundColor Cyan

# Testar backend
Write-Host "Testando backend..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$BackendUrl/actuator/health" -Method Get
    Write-Host "Backend online!" -ForegroundColor Green
} catch {
    Write-Host "Backend nao responde" -ForegroundColor Red
    exit 1
}

# Resetar senhas
Write-Host "Resetando senhas..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BackendUrl/api/test/reset-all-passwords" -Method Post -ContentType "application/json"
    Write-Host "Senhas resetadas!" -ForegroundColor Green
    Write-Host "Usuarios atualizados: $($response.usuariosAtualizados)"
} catch {
    Write-Host "Erro ao resetar: $_" -ForegroundColor Red
    exit 1
}

# Testar login
Write-Host "Testando login..." -ForegroundColor Yellow
$login = @{ email = "usuario@teste.net"; senha = "123456" } | ConvertTo-Json
try {
    $result = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $login
    Write-Host "Login OK!" -ForegroundColor Green
} catch {
    Write-Host "Login falhou: $_" -ForegroundColor Red
    exit 1
}

Write-Host "Tudo pronto!" -ForegroundColor Green