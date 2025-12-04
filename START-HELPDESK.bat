@echo off
REM =============================================================================
REM SCRIPT DE INICIO RAPIDO - HELP DESK
REM =============================================================================
REM Este script inicia o ambiente completo (Backend + Frontend)
REM =============================================================================

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║          HELP DESK - Iniciando Ambiente Completo            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0help-desk-frontend"

echo Executando script de inicializacao...
echo.

powershell -ExecutionPolicy Bypass -File ./start-dev.ps1

pause
