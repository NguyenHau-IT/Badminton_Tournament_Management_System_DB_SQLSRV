@echo off
REM ===================================
REM BTMS Landing Page - Quick Start
REM ===================================

echo.
echo ========================================
echo   BTMS - Landing Page Test Server
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH!
    echo.
    echo Please install Maven first:
    echo https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo [INFO] Maven found!
echo.

REM Check if Java 21 is installed
java -version 2>&1 | findstr /i "version \"21" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Java 21 not found!
    echo [INFO] Current Java version:
    java -version
    echo.
    echo [INFO] This project requires Java 21+
    echo Please install Java 21 or higher
    echo https://adoptium.net/
    echo.
    pause
)

echo [INFO] Starting BTMS application...
echo.
echo ========================================
echo   Server will start on port 8080
echo   Landing page: http://localhost:8080/
echo ========================================
echo.
echo Press Ctrl+C to stop the server
echo.

REM Run Spring Boot application
mvn spring-boot:run

pause
