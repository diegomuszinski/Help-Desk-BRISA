@echo off
echo ========================================
echo  Iniciando Sistema HelpDesk
echo ========================================
echo.

REM Configurar Java 21
echo [1/3] Configurando Java 21...
set JAVA21_PATH=C:\Users\ResTIC55\scoop\apps\openjdk21\21.0.2-13
set JAVA_HOME=%JAVA21_PATH%
set PATH=%JAVA_HOME%\bin;%PATH%
java -version
echo.

REM Iniciar Backend
echo [2/3] Iniciando Backend (porta 8080)...
cd helpdesk-backend\helpdesk-api
start "HelpDesk Backend" cmd /k "gradlew bootRun --args=--spring.profiles.active=dev"
echo Backend iniciando em janela separada...
echo Aguarde 30 segundos para o backend inicializar...
timeout /t 30 /nobreak
echo.

REM Iniciar Frontend
echo [3/3] Iniciando Frontend (porta 5173)...
cd ..\..\help-desk-frontend
start "HelpDesk Frontend" cmd /k "npm run dev"
echo Frontend iniciando em janela separada...
echo.

echo ========================================
echo  Sistema Iniciado!
echo ========================================
echo.
echo  Backend:  http://localhost:8080
echo  Frontend: http://localhost:5173
echo.
echo  Mantenha as janelas do Backend e Frontend abertas!
echo  Para fechar o sistema, feche as janelas separadas.
echo.
pause
