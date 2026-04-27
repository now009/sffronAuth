@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set APP_NAME=Saffron-auth
set PORT=8090
set HEALTH_URL=http://localhost:%PORT%/.well-known/oauth-authorization-server

echo ============================================
echo  %APP_NAME% status
echo ============================================

REM 1) 포트 / PID
set PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    set PID=%%a
)

if not defined PID (
    echo [PORT] %PORT% : NOT LISTENING
    echo [STATE] STOPPED
    exit /b 1
)

echo [PORT] %PORT% : LISTENING ^(PID !PID!^)

REM 2) 프로세스 정보
for /f "tokens=1,2 delims=," %%a in ('tasklist /fi "PID eq !PID!" /fo csv /nh') do (
    echo [PROC] %%~a
)

REM 3) HTTP 응답 확인
echo [HTTP] %HEALTH_URL%
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -UseBasicParsing -TimeoutSec 3 '%HEALTH_URL%'; Write-Host ('       HTTP ' + $r.StatusCode + ' ' + $r.StatusDescription) -ForegroundColor Green } catch { Write-Host ('       FAIL: ' + $_.Exception.Message) -ForegroundColor Yellow }"

echo [STATE] RUNNING
endlocal
