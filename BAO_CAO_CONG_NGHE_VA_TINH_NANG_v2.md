# 📊 BÁO CÁO CÔNG NGHỆ VÀ TÍNH NĂNG · v2

Hệ thống Quản lý Đa sân Cầu lông (Badminton Tech / BTMS)

---

## 📋 Thông tin tổng quan
- Tên dự án: Badminton Tech (BT) · Badminton Tournament Management System (BTMS)
- Phiên bản hiện tại: 2.0.0
- Nhà phát triển: Nguyen Viet Hau
- Kiểu ứng dụng: Desktop (Java Swing) + Web (Spring Boot + Thymeleaf)
- Hệ điều hành mục tiêu: Windows 10/11 64-bit
- Cổng dịch vụ mặc định: 2345 (0.0.0.0:2345)

---

## 🎯 Mục tiêu & phạm vi
- Vận hành giải cầu lông với nhiều sân thi đấu đồng thời trên cùng 1 máy.
- Điều khiển trận đấu trực tiếp từ desktop app; hỗ trợ điều khiển từ xa qua trình duyệt (di động/tablet/PC) bằng mã PIN.
- Theo dõi, giám sát, và đồng bộ trạng thái các sân theo thời gian thực (real-time).
- Quản lý danh mục dữ liệu giải đấu: nội dung thi đấu, câu lạc bộ, đăng ký theo giải,…
- Đóng gói phát hành dạng bộ cài MSI cho Windows (tự động cài đặt JRE kèm ứng dụng).

---

## 🏗️ Kiến trúc hệ thống
```
Java Swing Desktop (MainFrame, các Panel quản trị)
        │
        │ IPC/Service nội bộ
        ▼
Spring Boot (Web + REST API + SSE)  ←→  SQL Server (JDBC/Hikari/JPA)
        │
  ├── Thymeleaf view (/pin, /scoreboard/{pin}) + Static (CSS/JS)
  └── REST API (/api/court/**, /api/scoreboard/**) + SSE stream
```
Đặc điểm:
- Ứng dụng desktop và dịch vụ web đồng quy trình (fat-jar Spring Boot, mở Swing UI trong JVM không headless).
- Giao tiếp real-time qua SSE (Server-Sent Events) và/hoặc polling dự phòng.
- Ứng dụng web chạy trên LAN (0.0.0.0:2345) để thiết bị khác (điện thoại/tablet) truy cập.

---

## 💻 Công nghệ chính (Tech stack)

### Runtime & nền tảng
- Java 17 (maven.compiler.release=17)
- Spring Boot 3.2.6 (parent)
- Desktop UI: Java Swing + FlatLaf 3.4 (flatlaf, flatlaf-extras)

### Web & API
- Spring Web MVC, Thymeleaf (templates: `templates/pin/pin-entry.html`, `templates/scoreboard/scoreboard.html`)
- Static assets: Bootstrap 5.3.3 (CDN), Bootstrap Icons, jQuery 3.7.1, custom JS/CSS (`/js/pin/pin.js`, `/js/scoreboard/scoreboard.js`, `/css/pin/pin.css`, `/css/scoreboard/scoreboard.css`)
- SSE (Server-Sent Events) qua `SseEmitter`

### CSDL & dữ liệu
- Microsoft SQL Server (JDBC driver: `com.microsoft.sqlserver:mssql-jdbc`)
- Cấu hình HikariCP (pool): maximumPoolSize=10, minimumIdle=5, timeout/lifetime tối ưu sẵn
- JPA/Hibernate: `ddl-auto=update`, dialect SQL Server, `spring.jpa.show-sql=false`

### Thư viện bổ trợ
- OkHttp/okhttp-sse (client-side HTTP nếu cần)
- Jackson Databind (JSON)
- ZXing (QR Code)
- jcalendar (chọn ngày cho UI)
- OpenPDF 1.3.39 (xuất PDF)

### Build & phát hành
- Maven + Spring Boot Maven Plugin (fat-jar)
- jpackage-maven-plugin 1.6.6 (đóng gói MSI cho Windows)
- Profiles build tự động chọn thư mục cài đặt dựa theo ổ D:
  - Có ổ D: `install.dir=D:\BTMS`
  - Không có ổ D: `install.dir=C:\Program Files\BTMS`
- Icon, shortcut, menu group Windows được cấu hình trong plugin

---

## ⚙️ Cấu hình mặc định quan trọng
File: `src/main/resources/application.properties`
- Server
  - `server.address=0.0.0.0`
  - `server.port=2345`
  - `spring.main.headless=false` (cho phép mở UI Swing)
- SQL Server (mẫu đi kèm)
  - `spring.datasource.url=jdbc:sqlserver://GODZILLA\\SQLDEV:1433;databaseName=badminton_tournament;encrypt=true;trustServerCertificate=true;`
  - `spring.datasource.username=hau2`
  - `spring.datasource.password=hau123`
  - `spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver`
  - Hikari pool: `maximum-pool-size=10`, `minimum-idle=5`, …
- JPA/Hibernate
  - `spring.jpa.hibernate.ddl-auto=update`
  - `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect`
  - `spring.jpa.show-sql=false`

- Thông tin ứng dụng
  - `spring.application.name=BadmintonEventTechnology`

Desktop auto-connect (MainFrame.autoConnectDatabase):
- Đọc cấu hình runtime (nếu có) hoặc fallback sang `GODZILLA\\SQLDEV:1433`, database `badminton`, user `hau2`.
- Lắp URL JDBC, thiết lập encrypt/trustServerCertificate, loginTimeout, integratedSecurity.

---

## 🧭 Điều hướng & màn hình chính (Desktop)
- Header bar: tiêu đề + phiên bản + nút chuyển theme (Dark/Light)
- Status bar: trạng thái kết nối DB, Network IF, RAM usage realtime
- Tab chính (tùy role sau đăng nhập):
  - Login (xác thực)
  - Chọn giải đấu, Giải đấu
  - Nội dung (quản lý danh mục nội dung thi đấu)
  - Câu lạc bộ
  - Đăng ký nội dung (theo giải đang chọn)
  - Thi đấu (MultiCourtControlPanel): quản lý nhiều sân
  - Giám sát (Monitor): theo dõi trạng thái các sân
  - Kết quả đã thi đấu (Screenshot)
  - Logs

Các tiện ích UI:
- Giao diện FlatLaf hiện đại, icon SVG, trang bị theme chuyển động (FlatAnimatedLafChange)
- Bộ đếm RAM dùng Timer 1s
- Lưu/khôi phục vị trí split panes, dọn tài nguyên khi đóng app

---

## 🏟️ Quản lý đa sân & điều khiển trận đấu
- Mỗi sân có mã PIN 4 chữ số (unique) để truy cập/điều khiển từ web.
- Thiết lập trận đấu: Đơn/Đôi, BO (Best of), tên đội/vận động viên, màn hình hiển thị (dọc/ngang), tiêu đề…
- Điều khiển điểm: tăng/giảm cho A/B, reset, đổi sân (swap ends), đổi giao cầu (change server), ván tiếp theo.
- Màn hình bảng điểm riêng (vertical/horizontal) có thể mở toàn màn hình ở màn hình thứ N.
- Phát sóng trạng thái bảng điểm ra mạng nội bộ (broadcast UDP). Tính năng gửi screenshot về admin (UDP 2346) đã bị loại bỏ.

---

## 🌐 Web interface & PIN flow
- Trang nhập PIN: `GET /pin` (Thymeleaf -> `templates/pin/pin-entry.html`)
  - Hướng dẫn nhập PIN, QR, copy link nhanh.
- Trang bảng điểm: `GET /scoreboard/{pin}` (Thymeleaf -> `templates/scoreboard/scoreboard.html`)
  - Giao diện responsive, tối ưu mobile.
  - Badge hiển thị ván hiện tại, BO, đội đang giao cầu và vị trí giao cầu R/L.
  - Nút: Làm mới, Đặt lại, Đổi sân, Đổi giao cầu, Ván tiếp theo, Fullscreen, quay về trang PIN.

JS client (`/js/scoreboard/scoreboard.js`):
- Kết nối SSE: `/api/court/{pin}/stream` (sự kiện `init` + `update`), fallback sang tự động refresh 3s (`/sync`).
- Xử lý logic R/L theo luật giao cầu (server chẵn = R, lẻ = L), sắp tên ở layout đơn/đôi, gợi ý tự động chuyển ván/đổi sân khi kết thúc ván.
- Phím tắt: N (next), S (swap), G (change server), F (fullscreen), +/- cho điểm.

---

## 🔌 REST API & SSE

Mode 1 — Theo PIN (đa sân)  · Base path: `/api/court` (CORS: `*`)
- GET `/{pin}` → tổng quan (kiểu đơn giản: teamAScore, teamBScore)
- GET `/{pin}/status` → kiểm tra PIN hợp lệ, trả kèm courtId/header nếu có
- GET `/{pin}/sync` → snapshot chi tiết trận đấu
- GET `/{pin}/stream` (text/event-stream) → SSE
- POST `/{pin}/increaseA` | `/{pin}/decreaseA`
- POST `/{pin}/increaseB` | `/{pin}/decreaseB`
- POST `/{pin}/reset` | `/{pin}/next` | `/{pin}/swap` | `/{pin}/change-server` | `/{pin}/undo`
- POST `/{pin}/{action}` → endpoint tổng quát tương thích JS cũ
- GET `/health` → kiểm tra controller

Mode 2 — Không dùng PIN (single scoreboard) · Base path: `/api/scoreboard`
- GET `/` → tổng quan (kiểu đơn giản: teamAScore, teamBScore)
- GET `/sync` → snapshot chi tiết trận đấu
- GET `/stream` (text/event-stream) → SSE
- POST `/increaseA|decreaseA|increaseB|decreaseB|reset|next|swap|change-server|undo`

Cấu trúc snapshot (rút trích từ client/server):
```
{
  names: [string, string],   // Tên đội/vđv A, B (Đôi: "Tên1-Tên2")
  score: [number, number],   // Điểm A, B
  games: [number, number],   // Số ván A, B đã thắng
  gameNumber: number,        // Ván hiện tại (1..BO)
  bestOf: number,            // Số ván tối đa (1/3)
  server: 0|1,               // Bên đang giao cầu (0=A, 1=B)
  doubles: boolean,          // Đánh đôi hay không
  gameScores?: [ [a,b], ...] // Điểm chi tiết các ván đã xong (nếu có)
}
```

Ghi chú:
- SSE phát qua `SseEmitter`:
  - Ở chế độ PIN (`/api/court`): broadcast dùng thread pool riêng (8 threads) để tránh block (xem `ScoreboardPinController`).
  - Ở chế độ không PIN (`/api/scoreboard`): broadcast trực tiếp qua danh sách kết nối (xem `ScoreboardController`).
  - Client áp dụng throttle tối thiểu ~80ms.
- Khi sự kiện quan trọng thay đổi (score/games/gameNumber/server) sẽ tự động broadcast `update`.

---

## 🗃️ Các mô-đun dữ liệu/ghi nhật ký (trích yếu)
- Tournament (GiaiDau, chọn giải, đăng ký nội dung theo giải)
- Danh mục: Nội dung, Câu lạc bộ (service/repository theo kết nối SQLSRV)
- AuthService & LoginTab (phân quyền ADMIN/CLIENT ảnh hưởng tab và chế độ giám sát)
- ScoreboardHub/ScoreboardRemote (quản lý state scoreboard). Tính năng UDP nhận screenshot (port 2346) đã bị loại bỏ; ảnh chỉ lấy từ thư mục `screenshots`.
- LogTab & util.log.Log (ghi log các sự kiện: tăng/giảm điểm, đổi sân, ván tiếp theo…)

---

## 🧪 Kiểm thử & giám sát
- Spring Boot Test/JUnit (khởi tạo dự án; test mẫu có sẵn)
- MonitorTab (desktop) theo dõi tổng thể các sân theo thời gian thực.
- ScreenshotTab: thu thập ảnh chụp màn hình trạng thái phục vụ lưu vết/kết quả thi đấu.

---

## 🔒 Bảo mật & quyền truy cập
- Cơ chế PIN-based cho từng sân (4 chữ số) – ai biết PIN có thể truy cập/điều khiển sân tương ứng.
- CORS `*` cho các endpoint `/api/court/**` (phục vụ điều khiển qua LAN). Có thể siết lại theo môi trường triển khai.
- Dữ liệu lưu cục bộ trong SQL Server nội bộ của đơn vị tổ chức (không phụ thuộc dịch vụ bên thứ ba).

Khuyến nghị triển khai:
- Bật tường lửa theo danh sách trắng (port 2345 TCP). UDP 2346 không còn được sử dụng.
- Dùng HTTPS/nghiên cứu reverse proxy nếu xuất dịch vụ ra ngoài LAN.

---

## ⚡ Hiệu năng & tối ưu
- HikariCP: giới hạn pool hợp lý cho ứng dụng desktop; timeout/lifetime phù hợp để tránh connection leak.
- SSE với broadcastExecutor (fixed thread pool 8) để không block luồng UI hoặc request chính.
- Web client throttle & auto-refresh 3s fallback giúp giảm tải khi SSE bị gián đoạn.
- JVM options gợi ý (file `jvm-optimization.conf`): `-Xmx1024m` (MSI), có thể tăng như `-Xmx4g`, `-XX:+UseG1GC`, `-XX:+UseStringDeduplication` tùy máy.

---

## 📦 Đóng gói & cài đặt (Windows MSI)
- Sử dụng `jpackage-maven-plugin` tạo MSI trong thư mục `target/dist`.
- Tên gói hiển thị và icon tùy biến; tạo shortcut và menu group.
- `install.dir` được set theo profiles (ổ D hoặc Program Files).

Build nhanh (tham khảo):
1) Đóng gói jar: `mvn -q -DskipTests package`
2) Tạo MSI (goal jpackage đã bind phase package): artifact tại `target/dist/`.

---

## 🧭 Hướng dẫn vận hành nhanh
- Mở ứng dụng (jar hoặc MSI đã cài): UI desktop xuất hiện.
- Ứng dụng tự kết nối SQL Server theo `application.properties` (có thể hiện trạng thái ở status bar).
- Đăng nhập → theo role hiển thị tab phù hợp.
- Vào tab "Thi đấu" để tạo sân, lấy PIN.
- Thiết bị di động truy cập `http://<IP-máy-chạy-app>:2345/pin` → nhập PIN → điều khiển bảng điểm.
- Tab "Giám sát" để theo dõi tất cả sân; "Kết quả đã thi đấu" để xem ảnh chụp/snapshot.

---

## 🗺️ Lộ trình mở rộng (gợi ý)
- Đồng bộ cloud (backup/log/analytics), multi-machine sync.
- Ứng dụng mobile native dành cho trọng tài.
- Phân quyền sâu hơn cho API (token theo sân/giải).
- UI scoreboard đa chủ đề, tùy biến branding/ads.
- Tách microservice nếu cần mở rộng quy mô lớn.

---

## 📎 Phụ lục: Tham chiếu mã nguồn chính
- Cấu hình & build: `pom.xml`, `jvm-optimization.conf`, `resize-images*.bat`
- Cấu hình server & DB: `src/main/resources/application.properties`
- Giao diện web: `src/main/resources/templates/pin/pin-entry.html`, `src/main/resources/templates/scoreboard/scoreboard.html`, `src/main/resources/static/js/scoreboard/scoreboard.js`, `src/main/resources/static/css/scoreboard/scoreboard.css`, `src/main/resources/static/css/pin/pin.css`
- Khung desktop: `src/main/java/com/example/btms/ui/main/MainFrame.java` + các Panel: control, monitor, screenshot, tournament, category, club…
- API & SSE: `src/main/java/com/example/btms/controller/scoreBoard/ScoreboardPinController.java`, `src/main/java/com/example/btms/controller/scoreBoard/ScoreboardController.java`, `src/main/java/com/example/btms/controller/scoreBoard/ScoreboardViewController.java`
- Scoreboard service: `service/scoreboard/*` (broadcast UDP, màn hình hiển thị dọc/ngang)

---

## ✅ Kết luận
BT v2 cung cấp giải pháp quản lý – điều khiển – giám sát đa sân cầu lông toàn diện theo thời gian thực, kết hợp sức mạnh của desktop app và web interface trên LAN. Kiến trúc linh hoạt, công nghệ hiện đại, đóng gói MSI thuận tiện, và khả năng mở rộng giúp phù hợp từ giải nhỏ đến sự kiện quy mô lớn.
