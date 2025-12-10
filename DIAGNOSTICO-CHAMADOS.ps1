# Script de diagnóstico de chamados

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Diagnóstico de Chamados HelpDesk" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se backend está rodando
Write-Host "[1/4] Verificando Backend..." -ForegroundColor Yellow
$backendRunning = Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue
if (-not $backendRunning) {
    Write-Host "✗ Backend não está rodando!" -ForegroundColor Red
    Write-Host "Execute: INICIAR-SISTEMA.bat" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ Backend rodando na porta 8080" -ForegroundColor Green
Write-Host ""

# Fazer login
Write-Host "[2/4] Fazendo login..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = 'admin@admin.net'
        senha = 'admin'
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method POST -Body $loginBody -ContentType 'application/json'
    
    $token = $loginResponse.accessToken
    Write-Host "✓ Login realizado com sucesso" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "✗ Erro ao fazer login: $_" -ForegroundColor Red
    exit 1
}

# Buscar chamados
Write-Host "[3/4] Buscando chamados da API..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $token"
    }
    
    $chamados = Invoke-RestMethod -Uri 'http://localhost:8080/api/chamados' -Method GET -Headers $headers
    
    Write-Host "✓ API retornou $($chamados.Count) chamados" -ForegroundColor Green
    Write-Host ""
    
    if ($chamados.Count -eq 0) {
        Write-Host "⚠ Nenhum chamado encontrado no banco de dados!" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Possíveis causas:" -ForegroundColor Yellow
        Write-Host "  1. Banco de dados está vazio" -ForegroundColor Gray
        Write-Host "  2. Usuário não tem permissão para ver chamados" -ForegroundColor Gray
        Write-Host "  3. Filtros estão bloqueando os resultados" -ForegroundColor Gray
        Write-Host ""
    } else {
        Write-Host "[4/4] Analisando status dos chamados..." -ForegroundColor Yellow
        Write-Host ""
        
        # Agrupar por status
        $statusGroups = $chamados | Group-Object -Property status
        
        Write-Host "Distribuição por Status:" -ForegroundColor Cyan
        Write-Host "------------------------" -ForegroundColor Cyan
        foreach ($group in $statusGroups) {
            $status = if ($group.Name) { $group.Name } else { "(sem status)" }
            Write-Host "  $status : $($group.Count) chamados" -ForegroundColor White
        }
        Write-Host ""
        
        # Mostrar alguns exemplos
        Write-Host "Exemplos de Chamados:" -ForegroundColor Cyan
        Write-Host "---------------------" -ForegroundColor Cyan
        $chamados | Select-Object -First 5 | ForEach-Object {
            Write-Host "  ID: $($_.id)" -ForegroundColor White
            Write-Host "  Título: $($_.titulo)" -ForegroundColor Gray
            Write-Host "  Status: [$($_.status)]" -ForegroundColor $(
                switch ($_.status) {
                    'Aberto' { 'Yellow' }
                    'ABERTO' { 'Yellow' }
                    'Em Andamento' { 'Cyan' }
                    'EM ANDAMENTO' { 'Cyan' }
                    'Resolvido' { 'Green' }
                    'RESOLVIDO' { 'Green' }
                    'Fechado' { 'Green' }
                    'FECHADO' { 'Green' }
                    default { 'White' }
                }
            )
            Write-Host "  Prioridade: $($_.prioridade)" -ForegroundColor Gray
            Write-Host ""
        }
        
        # Verificar formatos de status
        Write-Host "Análise de Formatos:" -ForegroundColor Cyan
        Write-Host "--------------------" -ForegroundColor Cyan
        
        $abertoCount = ($chamados | Where-Object { $_.status -eq 'Aberto' }).Count
        $abertoUpperCount = ($chamados | Where-Object { $_.status -eq 'ABERTO' }).Count
        $emAndamentoCount = ($chamados | Where-Object { $_.status -eq 'Em Andamento' }).Count
        $emAndamentoUpperCount = ($chamados | Where-Object { $_.status -eq 'EM ANDAMENTO' }).Count
        
        Write-Host "  'Aberto' (capitalizado): $abertoCount" -ForegroundColor White
        Write-Host "  'ABERTO' (maiúsculo): $abertoUpperCount" -ForegroundColor White
        Write-Host "  'Em Andamento' (capitalizado): $emAndamentoCount" -ForegroundColor White
        Write-Host "  'EM ANDAMENTO' (maiúsculo): $emAndamentoUpperCount" -ForegroundColor White
        Write-Host ""
        
        if ($abertoUpperCount -gt 0 -or $emAndamentoUpperCount -gt 0) {
            Write-Host "⚠ Encontrados status em MAIÚSCULO!" -ForegroundColor Yellow
            Write-Host "Solução: Execute o script fix-status-format.sql" -ForegroundColor Yellow
        } else {
            Write-Host "Solução: Execute o script fix-status-format.sql" -ForegroundColor Yellow
        }
        else {
            Write-Host "✓ Todos os status estão no formato correto" -ForegroundColor Green
        }
    }
}
catch {
    Write-Host "✗ Erro ao buscar chamados: $_" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Diagnóstico Concluído" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
