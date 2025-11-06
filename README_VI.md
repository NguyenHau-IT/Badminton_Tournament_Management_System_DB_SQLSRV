# 🏸 Hệ Thống Quản Lý Giải Đấu Cầu Lông (BTMS) v1.0.0

> **Hệ thống quản lý giải đấu cầu lông chuyên nghiệp với giao diện desktop và web**

## 📋 Tổng Quan

BTMS là một ứng dụng Java Desktop kết hợp với web interface để quản lý giải đấu cầu lông. Hệ thống hỗ trợ quản lý nhiều sân đồng thời, hiển thị điểm số real-time, và cung cấp API cho các thiết bị bên ngoài.

### ✨ Tính Năng Chính

- 🏆 **Quản lý giải đấu**: Tạo và quản lý các giải đấu đơn/đôi
- 🏟️ **Đa sân**: Điều khiển nhiều sân cầu lông cùng lúc
- 📱 **Web Interface**: Hiển thị bảng điểm qua trình duyệt
- 🔐 **Hệ thống PIN**: Bảo mật truy cập cho từng sân
- ⚡ **Real-time**: Cập nhật điểm số tức thời qua SSE
- 🗄️ **Cơ sở dữ liệu**: SQL Server với H2 TCP cho remote access
- 📊 **Báo cáo**: Xuất báo cáo và thống kê chi tiết

### 🛠️ Công Nghệ Sử Dụng

- **Java 21** (LTS) với Spring Boot 3.4.0
- **Maven** build system với jpackage
- **SQL Server** database chính
- **H2 TCP Server** cho kết nối từ xa
- **Swing UI** cho desktop application
- **Thymeleaf** cho web templates
- **Server-Sent Events (SSE)** cho real-time updates

## 🚀 Cài Đặt và Chạy

### 📋 Yêu Cầu Hệ Thống

- **Java 21** hoặc cao hơn
- **SQL Server** (LocalDB hoặc full version)
- **Windows 10/11** (khuyến nghị)
- **RAM**: Tối thiểu 4GB
- **Disk**: 500MB trống

### 💿 Cài Đặt

#### 1. Từ Source Code:
```bash
# Clone repository
git clone https://github.com/NguyenHau-IT/Badminton_Tournament_Management_System_DB_SQLSRV.git
cd Badminton_Tournament_Management_System_DB_SQLSRV

# Build project
mvn clean package

# Chạy ứng dụng
java -jar target/btms-1.0.0.jar
```

#### 2. Chạy với JVM tối ưu:
```bash
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar target/btms-1.0.0.jar
```

#### 3. Từ MSI installer:
```bash
# Build MSI package
mvn clean package jpackage:jpackage

# Cài đặt từ file MSI được tạo
```

### 🎛️ Tối Ưu JVM (tùy chọn)
```bash
# Chạy với memory optimization
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar btms-1.0.0.jar

# Hoặc sử dụng file cấu hình jvm-optimization.conf
```

## 🌐 Sử Dụng Web Interface

### 📱 Truy Cập Bảng Điểm

1. **Với PIN (bảo mật)**:
   - Truy cập: `http://localhost:8080/pin`
   - Nhập PIN của sân (ví dụ: 1234)
   - Xem bảng điểm: `http://localhost:8080/scoreboard/1234`

2. **Không PIN (công khai)**:
   - Truy cập trực tiếp: `http://localhost:8080/scoreboard`
   - Xem tất cả sân đang hoạt động

### 🔌 API Endpoints

#### API với PIN:
```http
GET /api/court/{pin}/score          # Lấy điểm hiện tại
POST /api/court/{pin}/score         # Cập nhật điểm
GET /api/court/{pin}/match-info     # Thông tin trận đấu
POST /api/court/{pin}/timer         # Điều khiển đồng hồ
```

#### API không PIN:
```http
GET /api/scoreboard/all-courts      # Tất cả sân
GET /api/scoreboard/active-courts   # Sân đang hoạt động
GET /api/scoreboard/events          # SSE stream
```

## 🏗️ Cấu Trúc Dự Án

```
src/main/java/com/example/btms/
├── BadmintonTournamentManagementSystemApplication.java  # Main class
├── config/                              # Configuration classes
│   └── ConnectionConfig.java            # Database connection config
├── controller/scoreBoard/               # REST API controllers
│   ├── ScoreboardPinController.java     # PIN-based API (/api/court/**)
│   ├── ScoreboardController.java        # No-PIN API (/api/scoreboard/**)
│   └── ScoreboardViewController.java    # Web views (/pin, /scoreboard/{pin})
├── infrastructure/                      # External integrations
├── model/                               # Data models & entities
├── repository/                          # Data access layer
├── service/                             # Business logic
│   ├── auth/                            # Authentication services
│   ├── category/                        # Content category management
│   ├── club/                            # Club management
│   ├── player/                          # Player management
│   └── scoreboard/                      # Scoreboard & match services
├── ui/                                  # Swing UI components
│   ├── main/MainFrame.java              # Main desktop window
│   ├── control/                         # Match control panels
│   ├── monitor/                         # Monitoring interfaces
│   ├── tournament/                      # Tournament management
│   └── auth/LoginTab.java               # Authentication UI
└── util/                                # Utilities & helpers
```

### 🎯 Thành Phần Chính

#### Desktop UI (Swing)
- **MainFrame**: Chương trình chính với menu và navigation
- **MultiCourtControlPanel**: Quản lý nhiều sân đồng thời
- **BadmintonControlPanel**: Điều khiển từng sân cụ thể
- **MonitorTab**: Giám sát tất cả sân real-time

#### Web Interface
- **ScoreboardPinController**: REST API với PIN authentication
- **ScoreboardController**: REST API không cần PIN
- **ScoreboardViewController**: Thymeleaf views và static content
- **SSE Integration**: Server-Sent Events for real-time updates

#### H2 TCP Server (v1.0.0)
- **H2TcpServerConfig**: Auto-start H2 TCP server trên port 9092
- **Remote Database Access**: Cho phép máy khác kết nối database
- **IPv4 Network Filtering**: Chỉ chấp nhận IPv4 interfaces
- **Network Interface Selector**: Dialog chọn interface khi khởi động
- **UDP Multicast Broadcasting**: ScoreboardBroadcaster cho monitoring

#### Quản Lý Dữ Liệu
- **PlayerRepository**: CRUD operations cho người chơi
- **TournamentService**: Logic nghiệp vụ giải đấu
- **ScoreboardService**: Xử lý điểm số và trận đấu
- **AuthenticationService**: Quản lý PIN và bảo mật

## 🎮 Hướng Dẫn Sử Dụng

### 🎯 Khởi Tạo Giải Đấu

1. **Mở ứng dụng desktop**
2. **Tạo giải đấu mới**: File → New Tournament
3. **Cấu hình**: Chọn loại (đơn/đôi), số vòng, quy tắc
4. **Thêm người chơi**: Import từ CSV hoặc nhập thủ công
5. **Bắt đầu**: Activate tournament và assign courts

### 🏟️ Quản Lý Sân

1. **Tạo sân mới**: Court → Add New Court
2. **Cấu hình PIN**: Security → Set Court PIN
3. **Assign trận đấu**: Drag & drop từ tournament tree
4. **Điều khiển**: Start/Pause/Reset timer và score

### 📱 Hiển Thị Web

1. **Bật web server**: Settings → Enable Web Interface
2. **Chia sẻ URL**: Copy link để chia sẻ
3. **Quản lý PIN**: Security → Manage PINs
4. **Monitor**: Real-time updates tự động

## 🔧 Cấu Hình

### ⚙️ Database Configuration
```properties
# SQL Server (Primary)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BadmintonTournament;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password

# H2 TCP Server (Secondary - for remote access)
spring.h2.console.enabled=true
h2.tcp.port=9092
h2.tcp.allowOthers=true
```

### 🌐 Web Configuration
```properties
# Server settings
server.port=8080
server.servlet.context-path=/

# Thymeleaf templates
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.cache=false
```

### 🔐 Security Settings
```properties
# PIN settings
court.pin.length=4
court.pin.expiry.hours=24
court.pin.max.attempts=3

# CORS settings
cors.allowed.origins=*
cors.allowed.methods=GET,POST,PUT,DELETE
```

## 🐛 Xử Lý Sự Cố

### 1. Lỗi kết nối database
```bash
# Kiểm tra SQL Server service
net start MSSQLSERVER

# Test connection
sqlcmd -S localhost -E -Q "SELECT @@VERSION"
```

### 2. Port đã được sử dụng
```bash
# Kiểm tra port 8080
netstat -ano | findstr :8080

# Đổi port trong application.properties
server.port=8081
```

### 3. Lỗi memory
```bash
# Tăng heap size
java -Xmx4g -jar btms-1.0.0.jar

# Enable G1 garbage collector
java -XX:+UseG1GC -jar btms-1.0.0.jar

# Full optimization
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar btms-1.0.0.jar
```

### 4. H2 TCP Server không start
```bash
# Kiểm tra port 9092
netstat -ano | findstr :9092

# Restart với port khác
java -Dh2.tcp.port=9093 -jar btms-1.0.0.jar
```

### 5. Web interface không load
```bash
# Clear browser cache
# Kiểm tra firewall settings
# Restart application
```

## 📚 Tài Liệu Kỹ Thuật

### 📖 Documentation Files
- [`API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md) - Chi tiết API endpoints
- [`BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md`](docs/BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md) - Báo cáo kỹ thuật đầy đủ
- [`HUONG_DAN_SU_DUNG.md`](docs/HUONG_DAN_SU_DUNG.md) - Hướng dẫn sử dụng chi tiết
- [`SETTINGS.md`](docs/SETTINGS.md) - Cấu hình và tùy chỉnh

### 🏸 Tournament Rules
- [`LUAT_THI_DAU_CAU_LONG_BWF.md`](docs/LUAT_THI_DAU_CAU_LONG_BWF.md) - Luật thi đấu BWF
- [`CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md`](docs/CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md) - Công thức sơ đồ thi đấu

## 🤝 Đóng Góp

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 Liên Hệ

- **Developer**: NguyenHau-IT
- **GitHub**: [Badminton_Tournament_Management_System_DB_SQLSRV](https://github.com/NguyenHau-IT/Badminton_Tournament_Management_System_DB_SQLSRV)
- **Email**: Contact via GitHub Issues

---

## 🔄 Change Log

### v1.0.0 (2025-11-06)
- ✅ Initial release
- ✅ Multi-court management
- ✅ Web interface with PIN security
- ✅ Real-time scoreboard updates
- ✅ H2 TCP Server integration
- ✅ MSI installer support

---

**Made with ❤️ for badminton community**