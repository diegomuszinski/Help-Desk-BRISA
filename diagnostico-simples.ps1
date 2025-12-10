# Diagnóstico rápido de chamados
Write-Host "Diagnosticando chamados..." -ForegroundColor Cyan

# Login
$loginBody = '{"email":"admin@admin.net","senha":"admin"}'
$loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method POST -Body $loginBody -ContentType 'application/json'
$token = $loginResponse.accessToken
Write-Host "Login OK" -ForegroundColor Green

# Buscar chamados
$headers = @{Authorization = "Bearer $token"}
$chamados = Invoke-RestMethod -Uri 'http://localhost:8080/api/chamados' -Method GET -Headers $headers

Write-Host "`nTotal de chamados: $($chamados.Count)" -ForegroundColor Yellow

if ($chamados.Count -eq 0) {
    Write-Host "NENHUM CHAMADO ENCONTRADO!" -ForegroundColor Red
    Write-Host "O banco de dados pode estar vazio." -ForegroundColor Yellow
}
else {
    # Agrupar por status
    $statusGroups = $chamados | Group-Object -Property status
    
    Write-Host "`nChamados por Status:" -ForegroundColor Cyan
    foreach ($group in $statusGroups) {
        Write-Host "  $($group.Name): $($group.Count)" -ForegroundColor White
    }
    
    Write-Host "`nExemplos:" -ForegroundColor Cyan
    $chamados | Select-Object -First 5 | ForEach-Object {
        Write-Host "  ID: $($_.id) | Status: [$($_.status)] | Título: $($_.titulo)"
    }
}
