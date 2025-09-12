@echo off
echo ========================================
echo    CÔNG CỤ RESIZE ẢNH AVATAR.PNG
echo    (Sử dụng Maven)
echo ========================================
echo.

echo Đang compile và chạy công cụ resize ảnh qua Maven...
echo.

REM Compile và chạy qua Maven
mvn compile exec:java -Dexec.mainClass="com.example.badmintoneventtechnology.util.image.ImageResizerMaven"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Lỗi khi chạy Maven! Vui lòng kiểm tra:
    echo    1. Maven đã được cài đặt chưa?
    echo    2. Java đã được cài đặt chưa?
    echo    3. pom.xml có đúng không?
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ Hoàn thành! Kiểm tra thư mục icons để xem kết quả.
echo ========================================
pause
