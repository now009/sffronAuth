@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set APP_NAME=Saffron-auth
set PORT=8090

set PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    set PID=%%a
)

if not defined PID (
    echo [INFO] No process listening on port %PORT%.
    exit /b 0
)

echo [INFO] Stopping %APP_NAME% ^(PID !PID!^)
taskkill /F /PID !PID!
if errorlevel 1 (
    echo [ERROR] Failed to terminate process
    exit /b 1
)
echo [INFO] Stopped
endlocal
