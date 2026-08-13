@echo off
setlocal
cd /d "%~dp0\.."
echo TFS ANDROID - REGRESSION (Smoke + Regression)
mvn clean test -Pregression
endlocal
