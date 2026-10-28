@echo off
title Nexus - Developer Workspace Manager
cd /d "%~dp0"
echo Starting Nexus Developer Environment Validation Tool...
java -jar target\nexus-core-0.0.1-SNAPSHOT.jar
pause
