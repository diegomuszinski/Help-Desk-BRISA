# Script para configurar Java 21 para o projeto HelpDesk
# Execute este script antes de rodar o backend

Write-Host "=== Configurando Java 21 para HelpDesk ===" -ForegroundColor Cyan

# Caminho do Java 21
$JAVA21_PATH = "C:\Users\ResTIC55\scoop\apps\openjdk21\21.0.2-13"

# Verificar se Java 21 existe
if (-not (Test-Path $JAVA21_PATH)) {
    Write-Host "ERRO: Java 21 não encontrado em $JAVA21_PATH" -ForegroundColor Red
    Write-Host "Instale o Java 21 usando: scoop install openjdk21" -ForegroundColor Yellow
    exit 1
}

# Configurar variáveis de ambiente para a sessão atual
$env:JAVA_HOME = $JAVA21_PATH
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verificar versão
Write-Host "`nJava configurado:" -ForegroundColor Green
java -version

Write-Host "`nJAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "`nAgora você pode executar o backend com: .\gradlew bootRun" -ForegroundColor Cyan
