@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ======================================================================
echo   LOAN 12 SU QUAN HD — DONG GOI BAN WEB / IPHONE 17 PWA (dist/web)
echo ======================================================================
call .\gradlew.bat :web:buildWebDist --no-daemon
if %ERRORLEVEL% NEQ 0 (
    echo [LOI] Dong goi ban Web that bai!
    pause
    exit /b %ERRORLEVEL%
)
echo.
echo [HOAN TAT] Goi Web tinh da san sang tai: "%~dp0dist\web"
echo Ban co the keo tha thu muc "dist\web" len Cloudflare Pages / Netlify / GitHub Pages,
echo hoac chay "Run-Web-iPhone.cmd" de choi ngay tren iPhone 17 qua Wi-Fi!
if not "%~1"=="--no-pause" pause
