@echo off
REM JavaCopier Runner Script
REM
REM Usage: run.bat [APIKey]

chcp 65001 >nul

set "JAVAC_BIN=javac"
set "JAVA_BIN=java"

if exist "C:\Tools\jdk17\jdk-17.0.10+7\bin\javac.exe" (
    set "JAVAC_BIN=C:\Tools\jdk17\jdk-17.0.10+7\bin\javac.exe"
    set "JAVA_BIN=C:\Tools\jdk17\jdk-17.0.10+7\bin\java.exe"
) else if defined JAVA_HOME (
    set "JAVAC_BIN=%JAVA_HOME%\bin\javac.exe"
    set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
)

if not exist "%~dp0bin" mkdir "%~dp0bin"

echo Compiling JavaCopier...
"%JAVAC_BIN%" -d "%~dp0bin" "%~dp0src\main\java\pro\mrpc\copier\*.java" "%~dp0examples\QuickStart.java"
if errorlevel 1 (
    echo Compilation failed!
    exit /b 1
)

echo Running JavaCopier QuickStart...
"%JAVA_BIN%" -cp "%~dp0bin" QuickStart %*
