@echo off
REM 🏗️ BTMS Build Script với Java 21
REM Ensures project builds với correct Java version

echo.
echo ================================================
echo 🏗️ Building BTMS with Java 21 Enhanced Features
echo ================================================
echo.

REM Set Java 21 environment
set JAVA21_HOME=C:\Program Files\Java\jdk-21
set JAVA_HOME=%JAVA21_HOME%
set PATH=%JAVA21_HOME%\bin;%PATH%

REM Display Java version
echo ☕ Using Java version for build:
java -version
echo.

REM Display Maven version
echo 📦 Maven version:
mvn -version
echo.

echo 🏗️ Starting build process...
echo.

REM Clean and build
echo 📝 Phase 1: Cleaning previous build...
mvn clean

echo.
echo 🔧 Phase 2: Compiling with Java 21...
mvn compile

echo.
echo 📦 Phase 3: Packaging JAR with dependencies...
mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ BUILD SUCCESSFUL!
    echo.
    echo 📊 Build Summary:
    if exist "target\btms-2.0.0.jar" (
        for %%I in ("target\btms-2.0.0.jar") do echo    📄 JAR Size: %%~zI bytes ^(~%%~zI:~0,-6%% MB^)
        echo    📍 Location: target\btms-2.0.0.jar
    )
    echo    ☕ Java Version: 21
    echo    🚀 Threading: Enhanced
    echo    📊 Monitoring: Enabled
    echo.
    echo 🎯 Ready to run with: run-java21.bat
    echo    Or manually: java -jar target\btms-2.0.0.jar
) else (
    echo.
    echo ❌ BUILD FAILED!
    echo Please check the error messages above
)

echo.
pause