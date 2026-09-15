@echo off
setlocal

set "APP_HOME=%~dp0.."
for %%I in ("%APP_HOME%") do set "APP_HOME=%%~fI"
set "PID_FILE=%APP_HOME%\app.pid"

if not exist "%PID_FILE%" (
    echo PID dosyasi yok, uygulama calismiyor.
    exit /b 0
)

set /p PID=<"%PID_FILE%"
taskkill /PID %PID% /T /F >nul 2>&1
del "%PID_FILE%"
echo Durduruldu.

endlocal
