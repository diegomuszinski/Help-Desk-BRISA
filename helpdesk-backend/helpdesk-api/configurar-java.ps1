# Script para configurar Java 21 para o projeto HelpDesk
# Execute este script antes de rodar o backend

Write-Host "=== Configurando Java 21 para HelpDesk ===" -ForegroundColor Cyan
Write-Host ""

# Auto-detectar Java 21
Write-Host "Procurando Java 21 instalado..." -ForegroundColor Yellow

# Tentar encontrar Java no PATH atual
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion -match "21") {
        Write-Host "Java 21 encontrado no PATH" -ForegroundColor Green
        $JAVA21_PATH = Split-Path -Parent (Split-Path -Parent $javaCmd.Source)
    } else {
        Write-Host "Java encontrado mas não é versão 21: $javaVersion" -ForegroundColor Yellow
        $javaCmd = $null
    }
}

if (-not $javaCmd) {
    # Tentar caminhos comuns de instalação
    $commonPaths = @(
        "$env:USERPROFILE\scoop\apps\openjdk21\current",
        "$env:USERPROFILE\scoop\apps\openjdk21\21.0.2-13",
        "$env:USERPROFILE\scoop\apps\openjdk\current",
        "C:\Program Files\Eclipse Adoptium\jdk-21*",
        "C:\Program Files\Java\jdk-21*",
        "C:\Program Files\OpenJDK\jdk-21*"
    )

    $JAVA21_PATH = $null
    foreach ($path in $commonPaths) {
        $resolved = Resolve-Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved -and (Test-Path "$resolved\bin\java.exe")) {
            $JAVA21_PATH = $resolved.Path
            Write-Host "Java 21 encontrado em: $JAVA21_PATH" -ForegroundColor Green
            break
        }
    }

    if (-not $JAVA21_PATH) {
        Write-Host ""
        Write-Host "ERRO: Java 21 não encontrado!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Por favor, instale o Java 21:" -ForegroundColor Yellow
        Write-Host "  Opção 1 - Via Scoop (recomendado):" -ForegroundColor White
        Write-Host "    scoop install openjdk21" -ForegroundColor Gray
        Write-Host ""
        Write-Host "  Opção 2 - Via Eclipse Adoptium:" -ForegroundColor White
        Write-Host "    https://adoptium.net/temurin/releases/?version=21" -ForegroundColor Gray
        Write-Host ""
        exit 1
    }
}

# Configurar variáveis de ambiente para a sessão atual
$env:JAVA_HOME = $JAVA21_PATH
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verificar versão
Write-Host "`nJava configurado:" -ForegroundColor Green
java -version

Write-Host "`nJAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "`nAgora você pode executar o backend com: .\gradlew bootRun" -ForegroundColor Cyan
