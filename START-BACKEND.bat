@echo off
REM =============================================================================
REM SCRIPT DE INICIO RAPIDO - HELP DESK
REM =============================================================================
REM Este script inicia o ambiente completo (Backend + Frontend) com Java 21
REM =============================================================================

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║          HELP DESK - Iniciando Ambiente Completo            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Configurar Java 21
echo [1/3] Configurando Java 21...
set JAVA_HOME=C:\Users\ResTIC55\scoop\apps\openjdk21\21.0.2-13
set PATH=%JAVA_HOME%\bin;%PATH%
java -version
echo.

REM Iniciar Backend
echo [2/3] Iniciando Backend (porta 8080)...
cd /d "%~dp0helpdesk-backend\helpdesk-api"
start "HelpDesk Backend" cmd /k "gradlew.bat bootRun --args=--spring.profiles.active=dev"

REM Aguardar backend iniciar
timeout /t 15 /nobreak

REM Iniciar Frontend
echo [3/3] Iniciando Frontend...
cd /d "%~dp0help-desk-frontend"
powershell -ExecutionPolicy Bypass -File ./start-dev.ps1

echo.
echo ========================================
echo    Sistema iniciado com sucesso!
echo ========================================
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo Swagger:  http://localhost:8080/swagger-ui.html
echo.

pause
