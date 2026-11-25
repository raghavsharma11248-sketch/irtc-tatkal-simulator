@echo off
echo ===================================================================
echo   Compiling and Launching IRCTC Tatkal Concurrency Simulator
echo ===================================================================

if not exist bin (
    mkdir bin
)

echo [1/2] Compiling Java classes...
"C:\Program Files\Java\jdk-26.0.1\bin\javac.exe" -d bin src\com\irctc\model\*.java src\com\irctc\exception\*.java src\com\irctc\database\*.java src\com\irctc\service\*.java src\com\irctc\simulation\*.java src\com\irctc\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Console Application...
echo.
"C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -cp bin com.irctc.Main

pause
