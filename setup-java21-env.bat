@echo off
REM 🔧 IDE Java 21 Environment Setup
REM Helps configure development environment for Java 21

echo.
echo ==============================================
echo 🔧 Setting up Java 21 Development Environment
echo ==============================================
echo.

REM Set Java 21 as system default
set JAVA21_HOME=C:\Program Files\Java\jdk-21

echo 📍 Setting JAVA_HOME to Java 21...
setx JAVA_HOME "%JAVA21_HOME%" /M 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ JAVA_HOME set to: %JAVA21_HOME%
) else (
    echo ⚠️  Could not set JAVA_HOME system-wide ^(requires admin^)
    echo    Setting for current session only...
    set JAVA_HOME=%JAVA21_HOME%
)

echo.
echo 📍 Updating PATH with Java 21...
setx PATH "%JAVA21_HOME%\bin;%PATH%" /M 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ PATH updated with Java 21 bin directory
) else (
    echo ⚠️  Could not update PATH system-wide ^(requires admin^)
    echo    Setting for current session only...
    set PATH=%JAVA21_HOME%\bin;%PATH%
)

echo.
echo 🔍 Verifying Java setup...
echo.
echo Current JAVA_HOME: %JAVA_HOME%
echo.
java -version
echo.

echo 📋 IDE Configuration Recommendations:
echo.
echo 💡 IntelliJ IDEA:
echo    - File ^> Project Structure ^> Project ^> Project SDK: Java 21
echo    - File ^> Settings ^> Build ^> Build Tools ^> Maven ^> Runner ^> JRE: Java 21
echo.
echo 💡 Eclipse:
echo    - Window ^> Preferences ^> Java ^> Installed JREs ^> Add Java 21
echo    - Project Properties ^> Java Build Path ^> Libraries ^> Modulepath/Classpath ^> JRE System Library
echo.
echo 💡 VS Code:
echo    - Settings ^> Extensions ^> Java ^> Configuration ^> Java Home: %JAVA21_HOME%
echo    - Or add to settings.json: "java.home": "%JAVA21_HOME%"
echo.

echo ✨ Environment setup complete!
echo.
echo 🎯 Next Steps:
echo    1. Restart your IDE
echo    2. Refresh/reimport the project
echo    3. Run: build-java21.bat
echo    4. Start app: run-java21.bat
echo.

pause