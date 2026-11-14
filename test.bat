@echo off
title Qveto Online Examination System - Test Runner
cd /d "%~dp0"

echo ========================================================
echo   Running Qveto Online Examination System Unit Tests
echo ========================================================

REM Ensure bin directory exists
if not exist bin mkdir bin

REM Compile all source and test modules
javac -encoding UTF-8 -d bin ^
    src/main/java/com/qveto/exam/model/*.java ^
    src/main/java/com/qveto/exam/util/*.java ^
    src/main/java/com/qveto/exam/validation/*.java ^
    src/main/java/com/qveto/exam/dao/*.java ^
    src/main/java/com/qveto/exam/service/*.java ^
    src/main/java/com/qveto/exam/ui/*.java ^
    src/main/java/com/qveto/exam/Main.java ^
    src/test/java/com/qveto/exam/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

set "CP=bin;src/main/resources"
if exist "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" (
    set "CP=bin;src/main/resources;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
)

java -cp "%CP%" com.qveto.exam.TestRunner

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [FAILURE] One or more unit tests failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
pause
