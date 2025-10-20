# H2 TCP Server Configuration

## Tổng quan

Ứng dụng BTMS hiện đã được cấu hình để khởi động H2 TCP Server, cho phép máy khác kết nối đến database H2 qua mạng.

⚠️ **Lưu ý quan trọng**: H2 TCP Server sẽ bind tất cả network interfaces (0.0.0.0) do phiên bản H2 hiện tại không hỗ trợ bind địa chỉ IP cụ thể. Điều này có nghĩa là server có thể được truy cập từ bất kỳ IP nào của máy host.

## Cách hoạt động

1. **Chọn Network Interface**: Khi khởi động ứng dụng, bạn sẽ chọn network interface với địa chỉ IPv4
2. **Auto-start TCP Server**: H2 TCP Server sẽ tự động khởi động và bind tất cả interfaces trên port được chỉ định
3. **Remote Access**: Máy khác có thể kết nối đến database qua TCP sử dụng bất kỳ IP nào của máy server

## Thông tin kết nối

### Mặc định
- **Server Binding**: Tất cả interfaces (0.0.0.0)
- **Port**: 9092 (default H2 TCP port)
- **Client IP**: Có thể sử dụng bất kỳ IP nào của máy server (WiFi, LAN, etc.)
- **Username**: sa
- **Password**: (để trống)
- **Database Directory**: ./database/

### Connection URL Format
```
jdbc:h2:tcp://[ANY_SERVER_IP]:9092/[DATABASE_NAME]
```

### Ví dụ kết nối
```
# Sử dụng IP WiFi
jdbc:h2:tcp://192.168.1.100:9092/badminton_tournament

# Sử dụng IP LAN
jdbc:h2:tcp://10.0.0.50:9092/badminton_tournament

# Từ localhost
jdbc:h2:tcp://localhost:9092/badminton_tournament
```

## Kết nối từ máy khác

### 1. Sử dụng H2 Console (Web-based)
```bash
# Download H2 database từ http://h2database.com
java -cp h2*.jar org.h2.tools.Console

# Hoặc nếu đã cài đặt H2
h2console
```

Trong H2 Console:
- **JDBC URL**: `jdbc:h2:tcp://[IP_SERVER]:9092/badminton_tournament`
- **User Name**: `sa`
- **Password**: (để trống)

### 2. Sử dụng JDBC Client khác
```java
// Java example
String url = "jdbc:h2:tcp://192.168.1.100:9092/badminton_tournament";
Connection conn = DriverManager.getConnection(url, "sa", "");
```

### 3. Sử dụng DBeaver hoặc tools tương tự
1. Tạo kết nối mới
2. Database type: H2
3. Host: [IP của máy chạy BTMS]
4. Port: 9092
5. Database: badminton_tournament
6. Username: sa
7. Password: (để trống)

## Firewall Configuration

### Windows Firewall
```cmd
# Cho phép inbound connection trên port 9092
netsh advfirewall firewall add rule name="H2 TCP Server" dir=in action=allow protocol=TCP localport=9092
```

### Linux UFW
```bash
sudo ufw allow 9092/tcp
```

## Kiểm tra kết nối

### Từ máy server (local)
```cmd
netstat -an | findstr 9092
```

### Test connection
```bash
# Sử dụng telnet để test port
telnet [SERVER_IP] 9092
```

## Troubleshooting

### 1. Server không khởi động
- Kiểm tra port 9092 có bị chiếm dụng không
- Kiểm tra quyền ghi vào thư mục ./database/
- Xem log console khi khởi động ứng dụng

### 2. Không kết nối được từ máy khác
- Kiểm tra firewall settings
- Ping IP server từ máy client
- Kiểm tra H2 TCP Server có đang chạy không

### 3. Database không tìm thấy
- Database file sẽ được tạo tự động
- Kiểm tra thư mục ./database/ trên máy server
- Đảm bảo tên database trong connection URL đúng

## Configuration Code

### H2TcpServerConfig.java
- Quản lý lifecycle của H2 TCP Server
- Auto-start với IP từ NetworkConfig
- Provide connection information

### Integration với Main Application
- Tự động khởi động sau khi chọn network
- Hiển thị connection info trong console
- Graceful shutdown khi thoát ứng dụng

## Database Schema

Database sẽ sử dụng schema được định nghĩa trong:
- `./database/script.sql`
- `src/main/resources/database/script.sql`

Các table chính:
- Tournaments (Giải đấu)
- Clubs (Câu lạc bộ)  
- Players (Vận động viên)
- Matches (Trận đấu)
- Results (Kết quả)

## Security Notes

- ⚠️ H2 TCP Server chạy mà không có authentication mạnh
- 📍 Chỉ nên sử dụng trong mạng LAN tin cậy
- 🔒 Không expose ra Internet public
- 🔧 Có thể cấu hình password protection nếu cần

## Performance

- H2 TCP Server hỗ trợ multiple connections
- File-based database, performance phù hợp cho ứng dụng vừa và nhỏ
- Backup database bằng cách copy files trong ./database/

## Support

Nếu gặp vấn đề, kiểm tra:
1. Console output khi khởi động ứng dụng
2. H2 server logs
3. Network connectivity
4. Firewall configuration