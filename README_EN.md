# 🏸 Badminton Tournament Management System (BTMS) v1.0.0

> **Professional badminton tournament management system with desktop and web interface**

## 📋 Overview

BTMS is a Java Desktop application combined with web interface for managing badminton tournaments. The system supports multi-court management, real-time score display, and provides API for external devices.

### ✨ Key Features

- 🏆 **Tournament Management**: Create and manage singles/doubles tournaments
- 🏟️ **Multi-Court**: Control multiple badminton courts simultaneously
- 📱 **Web Interface**: Display scoreboards via web browser
- 🔐 **PIN System**: Secure access control for each court
- ⚡ **Real-time**: Instant score updates via SSE
- 🗄️ **Database**: SQL Server with H2 TCP for remote access
- 📊 **Reports**: Export detailed reports and statistics

### 🛠️ Technology Stack

- **Java 21** (LTS) with Spring Boot 3.4.0
- **Maven** build system with jpackage
- **SQL Server** primary database
- **H2 TCP Server** for remote connections
- **Swing UI** for desktop application
- **Thymeleaf** for web templates
- **Server-Sent Events (SSE)** for real-time updates

## 🚀 Installation and Setup

### 📋 System Requirements

- **Java 21** or higher
- **SQL Server** (LocalDB or full version)
- **Windows 10/11** (recommended)
- **RAM**: Minimum 4GB
- **Disk**: 500MB free space

### 💿 Installation

#### 1. From Source Code:
```bash
# Clone repository
git clone https://github.com/NguyenHau-IT/Badminton_Tournament_Management_System_DB_SQLSRV.git
cd Badminton_Tournament_Management_System_DB_SQLSRV

# Build project
mvn clean package

# Run application
java -jar target/btms-1.0.0.jar
```

#### 2. Run with JVM optimization:
```bash
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar target/btms-1.0.0.jar
```

#### 3. From MSI installer:
```bash
# Build MSI package
mvn clean package jpackage:jpackage

# Install from generated MSI file
```

### 🎛️ JVM Optimization (Optional)
```bash
# Run with memory optimization
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar btms-1.0.0.jar

# Or use jvm-optimization.conf configuration file
```

## 🌐 Web Interface Usage

### 📱 Accessing Scoreboards

1. **With PIN (Secure)**:
   - Access: `http://localhost:8080/pin`
   - Enter court PIN (e.g., 1234)
   - View scoreboard: `http://localhost:8080/scoreboard/1234`

2. **Without PIN (Public)**:
   - Direct access: `http://localhost:8080/scoreboard`
   - View all active courts

### 🔌 API Endpoints

#### PIN-based API:
```http
GET /api/court/{pin}/score          # Get current score
POST /api/court/{pin}/score         # Update score
GET /api/court/{pin}/match-info     # Match information
POST /api/court/{pin}/timer         # Timer control
```

#### No-PIN API:
```http
GET /api/scoreboard/all-courts      # All courts
GET /api/scoreboard/active-courts   # Active courts
GET /api/scoreboard/events          # SSE stream
```

## 🏗️ Project Structure

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

### 🎯 Core Components

#### Desktop UI (Swing)
- **MainFrame**: Main application with menu and navigation
- **MultiCourtControlPanel**: Manage multiple courts simultaneously
- **BadmintonControlPanel**: Control individual courts
- **MonitorTab**: Real-time monitoring of all courts

#### Web Interface
- **ScoreboardPinController**: REST API with PIN authentication
- **ScoreboardController**: No-PIN REST API
- **ScoreboardViewController**: Thymeleaf views and static content
- **SSE Integration**: Server-Sent Events for real-time updates

#### H2 TCP Server (v1.0.0)
- **H2TcpServerConfig**: Auto-start H2 TCP server on port 9092
- **Remote Database Access**: Allow other machines to connect to database
- **IPv4 Network Filtering**: Accept only IPv4 interfaces
- **Network Interface Selector**: Dialog for interface selection on startup
- **UDP Multicast Broadcasting**: ScoreboardBroadcaster for monitoring

#### Data Management
- **PlayerRepository**: CRUD operations for players
- **TournamentService**: Tournament business logic
- **ScoreboardService**: Score and match processing
- **AuthenticationService**: PIN and security management

## 🎮 User Guide

### 🎯 Tournament Setup

1. **Open desktop application**
2. **Create new tournament**: File → New Tournament
3. **Configure**: Select type (singles/doubles), rounds, rules
4. **Add players**: Import from CSV or manual entry
5. **Start**: Activate tournament and assign courts

### 🏟️ Court Management

1. **Create new court**: Court → Add New Court
2. **Configure PIN**: Security → Set Court PIN
3. **Assign matches**: Drag & drop from tournament tree
4. **Control**: Start/Pause/Reset timer and score

### 📱 Web Display

1. **Enable web server**: Settings → Enable Web Interface
2. **Share URL**: Copy link to share
3. **Manage PINs**: Security → Manage PINs
4. **Monitor**: Automatic real-time updates

## 🔧 Configuration

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

## 🐛 Troubleshooting

### 1. Database connection error
```bash
# Check SQL Server service
net start MSSQLSERVER

# Test connection
sqlcmd -S localhost -E -Q "SELECT @@VERSION"
```

### 2. Port already in use
```bash
# Check port 8080
netstat -ano | findstr :8080

# Change port in application.properties
server.port=8081
```

### 3. Memory issues
```bash
# Increase heap size
java -Xmx4g -jar btms-1.0.0.jar

# Enable G1 garbage collector
java -XX:+UseG1GC -jar btms-1.0.0.jar

# Full optimization
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar btms-1.0.0.jar
```

### 4. H2 TCP Server won't start
```bash
# Check port 9092
netstat -ano | findstr :9092

# Restart with different port
java -Dh2.tcp.port=9093 -jar btms-1.0.0.jar
```

### 5. Web interface not loading
```bash
# Clear browser cache
# Check firewall settings
# Restart application
```

## 📚 Technical Documentation

### 📖 Documentation Files
- [`API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md) - Detailed API endpoints
- [`BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md`](docs/BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md) - Comprehensive technical report
- [`HUONG_DAN_SU_DUNG.md`](docs/HUONG_DAN_SU_DUNG.md) - Detailed user guide
- [`SETTINGS.md`](docs/SETTINGS.md) - Configuration and customization

### 🏸 Tournament Rules
- [`LUAT_THI_DAU_CAU_LONG_BWF.md`](docs/LUAT_THI_DAU_CAU_LONG_BWF.md) - BWF tournament rules
- [`CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md`](docs/CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md) - Tournament bracket formulas

## 🤝 Contributing

1. Fork the project
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 Contact

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