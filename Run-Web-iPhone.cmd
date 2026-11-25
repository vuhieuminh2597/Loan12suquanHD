@echo off
setlocal
cd /d "%~dp0"
if exist "%USERPROFILE%\.jdks\corretto-19.0.2\bin\java.exe" (
    set "JAVA_HOME=%USERPROFILE%\.jdks\corretto-19.0.2"
)
echo ======================================================================
echo   LOAN 12 SU QUAN HD - MAY CHU LAN WI-FI CHO IPHONE 17 ^& TRINH DUYET
echo ======================================================================
if not exist "dist\web\index.html" (
    echo [1/2] Chua co ban build Web, dang dong goi dist\web...
    call .\gradlew.bat :web:buildWebDist --no-daemon
    if %ERRORLEVEL% NEQ 0 (
        echo [LOI] Khong the dong goi ban Web!
        pause
        exit /b %ERRORLEVEL%
    )
)
echo [2/2] Dang khoi dong may chu HTTP LAN...
start "" "http://localhost:8080"
call .\gradlew.bat :web:serveWebLan --no-daemon
pause
