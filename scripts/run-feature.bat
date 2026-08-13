@echo off
setlocal
cd /d "%~dp0\.."
if "%~1"=="" (echo Usage: run-feature.bat homepage & exit /b 1)
mvn clean test -Dgroups=%~1 -Dsuite.name=%~1
endlocal
