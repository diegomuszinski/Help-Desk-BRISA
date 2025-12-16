# Script para fazer commit sempre no branch em-desenvolvimento
# Uso: .\commit-dev.ps1 "mensagem do commit"

param(
    [Parameter(Mandatory=$true)]
    [string]$mensagem
)

Write-Host "=== Script de Commit para Branch em-desenvolvimento ===" -ForegroundColor Cyan

# Verifica se está no branch correto
$branchAtual = git rev-parse --abbrev-ref HEAD

if ($branchAtual -ne "em-desenvolvimento") {
    Write-Host "Você está no branch: $branchAtual" -ForegroundColor Yellow
    Write-Host "Mudando para o branch em-desenvolvimento..." -ForegroundColor Yellow
    
    # Verifica se há mudanças não commitadas
    $status = git status --porcelain
    if ($status) {
        Write-Host "Salvando mudanças temporariamente..." -ForegroundColor Yellow
        git stash
        $stashed = $true
    }
    
    # Muda para o branch em-desenvolvimento
    git checkout em-desenvolvimento
    
    # Se tiver criado stash, recupera as mudanças
    if ($stashed) {
        Write-Host "Recuperando mudanças..." -ForegroundColor Yellow
        git stash pop
    }
}

Write-Host "Branch atual: em-desenvolvimento" -ForegroundColor Green

# Adiciona todas as mudanças
Write-Host "Adicionando arquivos modificados..." -ForegroundColor Cyan
git add .

# Mostra o que será commitado
Write-Host "`nArquivos que serão commitados:" -ForegroundColor Cyan
git status --short

# Faz o commit
Write-Host "`nFazendo commit..." -ForegroundColor Cyan
git commit -m $mensagem

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ Commit realizado com sucesso no branch em-desenvolvimento!" -ForegroundColor Green
    Write-Host "`nPara enviar para o repositório remoto, execute:" -ForegroundColor Yellow
    Write-Host "git push origin em-desenvolvimento" -ForegroundColor White
} else {
    Write-Host "`n✗ Erro ao fazer commit" -ForegroundColor Red
}
