@echo off
REM Script para configurar Java 17 (alternativa em Batch)
REM Execute antes de rodar o projeto: configurar-java.bat

echo.
echo ====================================
echo   Configurando Java 17
echo ====================================
echo.

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo [OK] JAVA_HOME configurado para: %JAVA_HOME%
echo.

echo Verificando versao...
java -version

echo.
echo ====================================
echo   Agora voce pode executar:
echo   gradlew.bat bootRun
echo ====================================
echo.
