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

dir /b /s "%~dp0src\main\java\*.java" "%~dp0examples\*.java" > "%~dp0sources.txt"

echo Compiling JavaCopier...
"%JAVAC_BIN%" -d "%~dp0bin" @"%~dp0sources.txt"
set "ERR=%ERRORLEVEL%"
if exist "%~dp0sources.txt" del "%~dp0sources.txt"

if not "%ERR%"=="0" (
    echo Compilation failed!
    exit /b %ERR%
)

echo Running JavaCopier QuickStart...
"%JAVA_BIN%" -cp "%~dp0bin" QuickStart %*
