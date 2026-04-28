@echo off
setlocal
cd /d "%~dp0"

set APP_NAME=Saffron-auth
set JAR_FILE=target\Saffron-auth-0.0.1-SNAPSHOT.jar
set PORT=8090

REM Resolve java executable: prefer JAVA_HOME (project requires Java 17)
if defined JAVA_HOME (
    set "JAVA_BIN=%JAVA_HOME%\bin\javaw.exe"
) else (
    set "JAVA_BIN=javaw"
)

REM Check if port is already in use
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    echo [WARN] Port %PORT% is already in use ^(PID %%a^).
    echo        Run stop.bat first.
    exit /b 1
)

REM Build if jar is missing
if not exist "%JAR_FILE%" (
    echo [INFO] JAR not found. Running mvn clean package ...
    call mvn clean package
    if errorlevel 1 (
        echo [ERROR] Build failed
        exit /b 1
    )
)

echo [INFO] Starting %APP_NAME% ^(port %PORT%^)
echo [INFO] Java: %JAVA_BIN%
start "%APP_NAME%" /B "%JAVA_BIN%" -jar "%JAR_FILE%" > app.log 2>&1
echo [INFO] Launched. Log: app.log  /  Stop: stop.bat
endlocal
