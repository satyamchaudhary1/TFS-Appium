@echo off
setlocal
cd /d "%~dp0\.."
echo TFS ANDROID - SMOKE
mvn clean test -Psmoke
endlocal
