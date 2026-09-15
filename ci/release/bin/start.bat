@echo off
setlocal enabledelayedexpansion

set "APP_HOME=%~dp0.."
for %%I in ("%APP_HOME%") do set "APP_HOME=%%~fI"

if not defined AFT_ENV_FILE set "AFT_ENV_FILE=%APP_HOME%\config\aft.env"
if not defined AFT_PROFILE set "AFT_PROFILE=prod"
if not defined AFT_MAX_RAM_PERCENT set "AFT_MAX_RAM_PERCENT=75"

if not exist "%AFT_ENV_FILE%" (
    echo Ortam dosyasi bulunamadi: %AFT_ENV_FILE%
    echo config\aft.env.example dosyasini kopyalayip doldurun.
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%AFT_ENV_FILE%") do (
    set "line=%%A"
    if not "!line:~0,1!"=="#" if not "%%A"=="" set "%%A=%%B"
)

if not defined AFT_JWT_SECRET (
    echo AFT_JWT_SECRET tanimli degil.
    exit /b 1
)

if not exist "%APP_HOME%\logs" mkdir "%APP_HOME%\logs"

set "JAVA_BIN=java"
if defined JAVA_HOME set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"

"%JAVA_BIN%" -XX:MaxRAMPercentage=%AFT_MAX_RAM_PERCENT% ^
  -Dspring.profiles.active=%AFT_PROFILE% ^
  -Dspring.config.additional-location=file:%APP_HOME%\config\ ^
  %AFT_JAVA_OPTS% ^
  -jar "%APP_HOME%\app.jar"

endlocal
