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
    echo [INFO] %PORT% 포트에서 실행 중인 프로세스가 없습니다.
    exit /b 0
)

echo [INFO] %APP_NAME% 종료 ^(PID !PID!^)
taskkill /F /PID !PID!
if errorlevel 1 (
    echo [ERROR] 프로세스 종료 실패
    exit /b 1
)
echo [INFO] 종료 완료
endlocal
