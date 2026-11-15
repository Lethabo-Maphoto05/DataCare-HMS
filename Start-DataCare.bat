@echo off
title DataCare HMS Launcher
cd /d "%~dp0api"
start "DataCare API" cmd /k "npm.cmd start"
cd /d "%~dp0web"
start "DataCare Web" cmd /k "node server.js"
timeout /t 3 /nobreak >nul
start "" "http://127.0.0.1:5500/index.html"