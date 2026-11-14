@echo off
title Qveto Online Examination System
cd /d "%~dp0"

echo ========================================================
echo   Launching Qveto Online Examination System (Project 3)
echo ========================================================

REM Ensure bin directory exists
if not exist bin mkdir bin

REM Recompile all source modules
javac -encoding UTF-8 -d bin ^
    src/main/java/com/qveto/exam/model/*.java ^
    src/main/java/com/qveto/exam/util/*.java ^
    src/main/java/com/qveto/exam/validation/*.java ^
    src/main/java/com/qveto/exam/dao/*.java ^
    src/main/java/com/qveto/exam/service/*.java ^
    src/main/java/com/qveto/exam/ui/*.java ^
    src/main/java/com/qveto/exam/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Please check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

REM Build Classpath
set "CP=bin;src/main/resources"
if exist "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" (
    set "CP=bin;src/main/resources;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
)

REM Launch Application
java -cp "%CP%" com.qveto.exam.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
