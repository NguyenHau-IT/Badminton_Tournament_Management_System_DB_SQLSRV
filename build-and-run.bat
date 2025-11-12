@echo off
REM ===================================
REM BTMS - Build and Run JAR
REM ===================================

echo.
echo ========================================
echo   BTMS - Build and Run
echo ========================================
echo.

echo [1/3] Cleaning previous build...
call mvn clean

echo.
echo [2/3] Building JAR file...
call mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting application...
echo.
echo ========================================
echo   Server starting on port 8080
echo   Landing page: http://localhost:8080/
echo ========================================
echo.

REM Find the JAR file
for /f %%i in ('dir /b target\*.jar 2^>nul ^| findstr /v "original"') do set JAR_FILE=%%i

if not defined JAR_FILE (
    echo [ERROR] JAR file not found!
    pause
    exit /b 1
)

echo [INFO] Running: %JAR_FILE%
echo.

java -jar target\%JAR_FILE%

pause
