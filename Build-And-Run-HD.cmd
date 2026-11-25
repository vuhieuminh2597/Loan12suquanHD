@echo off
setlocal
set "ROOT=%~dp0"
if exist "C:\Users\PC\.jdks\corretto-19.0.2\bin\java.exe" (
    set "JAVA_HOME=C:\Users\PC\.jdks\corretto-19.0.2"
)

if /i "%~1"=="--build-only" (
    call "%ROOT%gradlew.bat" jar --no-daemon
    exit /b %ERRORLEVEL%
)

call "%ROOT%gradlew.bat" run --no-daemon
exit /b %ERRORLEVEL%
