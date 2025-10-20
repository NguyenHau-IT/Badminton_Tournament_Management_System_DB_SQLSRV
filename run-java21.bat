@echo off
REM 🚀 BTMS Launcher Script với Java 21 Enhanced Threading
REM Ensures application runs với correct Java version

echo.
echo ================================================
echo 🏸 Badminton Tournament Management System v2.0.0
echo ================================================
echo 🚀 Java 21 Enhanced Threading Version
echo.

REM Set Java 21 path explicitly
set JAVA21_HOME=C:\Program Files\Java\jdk-21
set JAVA_EXECUTABLE=%JAVA21_HOME%\bin\java.exe

REM Check if Java 21 exists
if not exist "%JAVA_EXECUTABLE%" (
    echo ❌ Error: Java 21 not found at %JAVA21_HOME%
    echo Please install Java 21 or update the path in this script
    pause
    exit /b 1
)

REM Display Java version
echo ☕ Using Java version:
"%JAVA_EXECUTABLE%" -version
echo.

REM Check if JAR file exists
if not exist "target\btms-2.0.0.jar" (
    echo ❌ Error: JAR file not found at target\btms-2.0.0.jar
    echo Please build the project first: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo 🚀 Starting BTMS with enhanced threading...
echo 📊 Performance monitoring enabled
echo 🧵 Multi-tier thread pools active
echo 💾 Real-time memory monitoring active
echo.
echo Press Ctrl+C to stop the application
echo.

REM Enhanced JVM arguments for Java 21
"%JAVA_EXECUTABLE%" ^
    -Xmx4g ^
    -XX:+UseG1GC ^
    -XX:+UseStringDeduplication ^
    -XX:MaxGCPauseMillis=200 ^
    -XX:G1HeapRegionSize=16m ^
    --add-opens java.base/java.lang=ALL-UNNAMED ^
    -Dspring.profiles.active=production ^
    -jar target\btms-2.0.0.jar

echo.
echo 👋 BTMS application stopped
pause