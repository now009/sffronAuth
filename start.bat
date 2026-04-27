@echo off
setlocal
cd /d "%~dp0"

set APP_NAME=Saffron-auth
set JAR_FILE=target\Saffron-auth-0.0.1-SNAPSHOT.jar
set PORT=8090

REM 포트 사용 중인지 확인
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    echo [WARN] %PORT% 포트가 이미 사용 중입니다 ^(PID %%a^).
    echo        먼저 stop.bat 실행 후 다시 시도하세요.
    exit /b 1
)

REM JAR 없으면 빌드
if not exist "%JAR_FILE%" (
    echo [INFO] JAR 파일이 없어 빌드를 시작합니다...
    call mvn clean package
    if errorlevel 1 (
        echo [ERROR] 빌드 실패
        exit /b 1
    )
)

echo [INFO] %APP_NAME% 시작 ^(port %PORT%^)
start "%APP_NAME%" /B javaw -jar "%JAR_FILE%" > app.log 2>&1
echo [INFO] 시작 요청 완료. 로그: app.log / 중지: stop.bat
endlocal
