@echo off
echo ========================================
echo    CÔNG CỤ RESIZE ẢNH AVATAR.PNG
echo ========================================
echo.

echo Đang compile và chạy công cụ resize ảnh...
echo.

REM Compile Java file
javac -cp "lib/*" src/main/java/com/example/badmintoneventtechnology/util/image/ImageResizer.java

if %ERRORLEVEL% NEQ 0 (
    echo Lỗi khi compile! Vui lòng kiểm tra Java và dependencies.
    pause
    exit /b 1
)

echo Compile thành công!
echo.

REM Chạy công cụ resize
java -cp "src/main/java;lib/*" com.example.badmintoneventtechnology.util.image.ImageResizer

echo.
echo ========================================
echo Hoàn thành! Kiểm tra thư mục icons để xem kết quả.
echo ========================================
pause
