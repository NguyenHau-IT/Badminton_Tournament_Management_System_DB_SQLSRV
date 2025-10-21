@echo off
echo ====================================
echo H2 TCP Server - Firewall Setup (LAN Only)
echo ====================================
echo.

echo Dang mo port 9092 cho H2 TCP Server (CHI may cung mang LAN)...
netsh advfirewall firewall add rule name="H2 TCP Server - BTMS (LAN Only)" dir=in action=allow protocol=TCP localport=9092

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ THANH CONG: Da mo port 9092 trong Windows Firewall
    echo.
    echo � BAO MAT: CHI may cung mang LAN co the ket noi
    echo 🔗 May cung LAN ket noi den H2 TCP Server qua:
    echo    - jdbc:h2:tcp://[IP_LAN_MAY_NAY]:9092/badminton_tournament
    echo    - Username: sa
    echo    - Password: (de trong)
    echo.
    echo �️  H2 Server chi bind to IP LAN cu the (khong phai 0.0.0.0)
    echo �💡 De tim IP LAN may nay, chay lenh: ipconfig
) else (
    echo.
    echo ❌ LOI: Khong the mo port trong Windows Firewall
    echo    Vui long chay script nay voi quyen Administrator
    echo.
    echo 🔧 Hoac chay thu cong lenh sau:
    echo    netsh advfirewall firewall add rule name="H2 TCP Server - BTMS (LAN Only)" dir=in action=allow protocol=TCP localport=9092
)

echo.
echo ====================================
echo Hien thi IP cua may nay:
echo ====================================
ipconfig | findstr "IPv4"

echo.
pause