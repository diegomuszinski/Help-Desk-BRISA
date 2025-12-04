# Script para configurar Java 17 para este terminal
# Execute antes de rodar o projeto: .\configurar-java.ps1

$javaHome = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'

Write-Host "🔧 Configurando Java 17..." -ForegroundColor Cyan

# Configurar para sessão atual
$env:JAVA_HOME = $javaHome
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Configurar para usuário (permanente sem admin)
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')

Write-Host "✅ Java configurado com sucesso!" -ForegroundColor Green
Write-Host ""

# Verificar versão
Write-Host "📋 Versão do Java:" -ForegroundColor Yellow
java -version

Write-Host ""
Write-Host "💡 Agora você pode executar:" -ForegroundColor Cyan
Write-Host "   .\gradlew.bat bootRun" -ForegroundColor White
