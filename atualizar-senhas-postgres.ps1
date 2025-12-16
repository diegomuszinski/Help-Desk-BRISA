# Script para atualizar senhas diretamente no PostgreSQL
param(
    [string]$PgBinPath = "C:\Program Files\PostgreSQL\16\bin",
    [string]$DbHost = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "helpdesk",
    [string]$DbUser = "postgres"
)

Write-Host "=== Atualizacao de Senhas no PostgreSQL ===" -ForegroundColor Cyan
Write-Host ""

# Hash BCrypt correto para senha "123456"
$hashCorreto = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'

# Verificar se psql existe
$psqlPath = Join-Path $PgBinPath "psql.exe"
if (-not (Test-Path $psqlPath)) {
    Write-Host "ERRO: psql.exe nao encontrado em: $psqlPath" -ForegroundColor Red
    Write-Host "Por favor, ajuste o parametro -PgBinPath" -ForegroundColor Yellow
    Write-Host "Exemplo: .\atualizar-senhas-postgres.ps1 -PgBinPath 'C:\Program Files\PostgreSQL\15\bin'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Conectando ao banco de dados..." -ForegroundColor Yellow
Write-Host "  Host: $DbHost" -ForegroundColor Gray
Write-Host "  Porta: $DbPort" -ForegroundColor Gray
Write-Host "  Banco: $DbName" -ForegroundColor Gray
Write-Host "  Usuario: $DbUser" -ForegroundColor Gray
Write-Host ""

# Criar script SQL temporario
$sqlScript = @"
-- Atualizar senhas dos usuarios de teste
UPDATE usuarios SET senha = '$hashCorreto' WHERE email = 'admin@admin.net';
UPDATE usuarios SET senha = '$hashCorreto' WHERE email = 'mariana@tecnico.net';
UPDATE usuarios SET senha = '$hashCorreto' WHERE email = 'usuario@teste.net';

-- Verificar atualizacao
SELECT email, perfil, LEFT(senha, 30) || '...' as senha_hash 
FROM usuarios 
WHERE email IN ('admin@admin.net', 'mariana@tecnico.net', 'usuario@teste.net')
ORDER BY id;
"@

$tempSqlFile = Join-Path $env:TEMP "update_passwords.sql"
$sqlScript | Out-File -FilePath $tempSqlFile -Encoding UTF8

try {
    # Executar SQL
    Write-Host "Executando atualizacao..." -ForegroundColor Yellow
    $env:PGPASSWORD = "teste"
    & $psqlPath -h $DbHost -p $DbPort -U $DbUser -d $DbName -f $tempSqlFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Senhas atualizadas com sucesso!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Credenciais de teste:" -ForegroundColor Cyan
        Write-Host "  admin@admin.net      / 123456 (ADMIN)" -ForegroundColor White
        Write-Host "  mariana@tecnico.net  / 123456 (TECHNICIAN)" -ForegroundColor White
        Write-Host "  usuario@teste.net    / 123456 (USER)" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "Erro ao atualizar senhas!" -ForegroundColor Red
        exit 1
    }
} finally {
    # Limpar
    Remove-Item $tempSqlFile -ErrorAction SilentlyContinue
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}
