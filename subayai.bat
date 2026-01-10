@echo off
setlocal

set PROJECT_ROOT=%~dp0
set BIN_DIR=%PROJECT_ROOT%bin
set SRC_DIR=%PROJECT_ROOT%src
set MAIN_CLASS=site.meowcat.subayai.lang.Main

if "%1"=="build" goto build
if "%1"=="clean" goto clean
if "%1"=="run" goto run
if "%1"=="help" goto help
goto help

:build
echo Building Subayai...
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
dir /s /B "%SRC_DIR%\*.java" > sources.txt
javac -d "%BIN_DIR%" @sources.txt
del sources.txt
if %ERRORLEVEL% EQU 0 (
    echo Build successful.
) else (
    echo Build failed.
    exit /b 1
)
goto :eof

:clean
if exist "%BIN_DIR%" rmdir /s /q "%BIN_DIR%"
echo Cleaned build artifacts.
goto :eof

:run
if not exist "%BIN_DIR%\site\meowcat\subayai\lang\Main.class" call :build
:: Pass all arguments to Main class
java -cp "%BIN_DIR%" %MAIN_CLASS% %*
goto :eof

:help
echo Subayai CLI Tool (Windows)
echo Usage:
echo   subayai build            Compile the Subayai interpreter
echo   subayai run ^<file.yai^>   Run a Subayai source file
echo   subayai clean            Remove build artifacts
goto :eof
