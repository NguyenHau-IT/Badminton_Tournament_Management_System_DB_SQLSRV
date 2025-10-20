# 🏸 HƯỚNG DẪN SỬ DỤNG HỆ THỐNG QUẢN LÝ ĐA SÂN CẦU LÔNG

## 📋 Mục lục
1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Khởi động sau khi cài đặt](#khởi-động-sau-khi-cài-đặt)
3. [Cài đặt từ source code](#cài-đặt-từ-source-code)
4. [Quản lý sân](#quản-lý-sân)
5. [Điều khiển trận đấu](#điều-khiển-trận-đấu)
6. [Điều khiển từ xa qua web](#điều-khiển-từ-xa-qua-web)
7. [Giám sát và theo dõi](#giám-sát-và-theo-dõi)
8. [Cài đặt hệ thống](#cài-đặt-hệ-thống)
9. [Tối ưu hiệu suất](#tối-ưu-hiệu-suất)
10. [Xử lý sự cố](#xử-lý-sự-cố)

---

## 🎯 Tổng quan hệ thống

Hệ thống quản lý đa sân cầu lông cho phép:
- **Quản lý nhiều sân** trên cùng một máy tính (tối đa 5 sân)
- **Điều khiển trận đấu** từ giao diện desktop với đầy đủ tính năng
- **Điều khiển từ xa** qua web interface với mã PIN bảo mật
- **Giám sát real-time** tất cả các sân từ một màn hình
- **Đồng bộ dữ liệu** tức thì giữa app và web qua SSE
- **Quản lý giải đấu** hoàn chỉnh: câu lạc bộ, vận động viên, nội dung thi đấu
- **Xuất báo cáo** và thống kê chi tiết
- **Hỗ trợ đơn và đôi** với luật thi đấu BWF

### 🏗️ Kiến trúc hệ thống
```
Desktop App (Swing) ←→ CourtManagerService ←→ Multiple CourtSessions
   ↓                  ↓                           ↓
Web Views (Thymeleaf) ←→ ScoreboardViewController  ←→  BadmintonMatch
   ↓
REST API (PIN)       ←→ ScoreboardPinController   (/api/court/**)
REST API (No-PIN)    ←→ ScoreboardController      (/api/scoreboard/**)

Real-time: SSE (SseEmitter)
Screenshots: Ảnh được đọc từ thư mục nội bộ `screenshots` (đã bỏ UDP Receiver)
```

---

## 🚀 Khởi động sau khi cài đặt

### Sau khi cài đặt từ MSI installer

#### Khởi động ứng dụng
1. **Từ Start Menu**:
   - Mở Start Menu → Tìm "Badminton Tournament Management System (BTMS)"
   - Click để khởi động ứng dụng

2. **Từ Desktop Shortcut**:
   - Double-click vào shortcut "BTMS" trên desktop (nếu có)

3. **Từ thư mục cài đặt**:
   - Mở thư mục cài đặt mặc định:
     - `D:\BTMS\` (nếu có ổ D:)
     - `C:\BTMS\` (nếu không có ổ D:)
   - Double-click file `Badminton Tournament Management System (BTMS).exe`

#### Lần khởi động đầu tiên
1. **Chọn Network Interface**:
   - Ứng dụng sẽ hiển thị dialog "Network Interface Selection"
   - **Danh sách network interfaces** sẽ hiển thị (WiFi, Ethernet, VPN, etc.)
   - **Thông tin chi tiết** mỗi interface: tên, IP address, trạng thái
   - **Chọn interface chính**: Thường là WiFi cho laptop hoặc Ethernet cho PC
   - **Lưu ý**: Interface này sẽ được dùng cho web access từ thiết bị khác
   - Click **"OK"** để tiếp tục

2. **Cấu hình Database (lần đầu)**:
   
   **Nếu chưa có cấu hình database:**
   - Màn hình **"Database Connection"** sẽ hiển thị
   - **Chọn loại database**:
     - **SQL Server**: Cho production, nhiều người dùng
     - **H2 Database**: Cho test, demo, sử dụng cá nhân
   
   **Cấu hình SQL Server:**
   - **Server Name**: Nhập tên server (VD: `DESKTOP-ABC\SQLEXPRESS`, `192.168.1.100`, `localhost`)
   - **Port**: 1433 (mặc định) hoặc port custom
   - **Database Name**: Tên database (VD: `badminton_tournament`)
   - **Authentication**:
     - ☑️ **Windows Authentication**: Dùng tài khoản Windows hiện tại
     - ☐ **SQL Server Authentication**: Nhập username/password riêng
   - **Connection Options**:
     - ☑️ **Encrypt**: Bảo mật kết nối (khuyến nghị)
     - ☑️ **Trust Server Certificate**: Tin tưởng chứng chỉ
   - Click **"Test Connection"** → chờ kết quả
   - Nếu thành công: **"✅ Connection successful"**
   - Nếu thất bại: Xem thông báo lỗi và khắc phục
   
   **Cấu hình H2 Database (đơn giản):**
   - **Database Path**: Chọn thư mục lưu file DB
   - **Database Name**: Nhập tên file (VD: `badminton_db`)
   - **Mode**: File-based (lưu trữ lâu dài) hoặc In-Memory (test)
   - Click **"Test Connection"**
   
   - Click **"Connect"** khi test thành công

3. **Chọn giải đấu**:
   - Sau khi kết nối DB thành công, dialog **"Tournament Selection"** hiển thị
   - **Danh sách giải đấu** có sẵn trong database
   - **Thông tin giải**: Tên giải, ngày bắt đầu, ngày kết thúc, trạng thái
   - **Tạo giải mới**: Click "New Tournament" nếu chưa có
   - **Chọn giải**: Click vào giải cần quản lý
   - **Confirm**: Click "Select" để xác nhận
   - **Giao diện chính** sẽ hiển thị với dữ liệu của giải đã chọn

#### Kiểm tra hoạt động
- **IP Address**: Xem IP hiện tại trong status bar phía dưới
- **Database**: Kiểm tra trạng thái kết nối DB
- **Memory**: Theo dõi sử dụng RAM

#### Tắt ứng dụng
- **Cách 1**: Click nút "X" trên cửa sổ chính
- **Cách 2**: File → Exit từ menu
- **Cách 3**: Alt+F4

---

## 🛠️ Cài đặt từ source code

### Yêu cầu hệ thống
- **Java 21+** (bắt buộc cho Spring Boot 3.4.x)
- **Maven 3.6+** (để build từ source)
- **RAM**: Tối thiểu 4GB, khuyến nghị 8GB+
- **Mạng LAN**: Để kết nối giữa các thiết bị

### Khởi động ứng dụng từ source
```bat
:: Cách 1: Chạy JAR (Windows)
mvn clean package -DskipTests
java -jar target\btms-2.0.0.jar

:: Cách 2: Chạy trực tiếp bằng Maven (dev)
mvn spring-boot:run

:: Cách 3: Với JVM optimization
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar target\btms-2.0.0.jar
```

### Cấu hình JVM (tùy chọn)
Tạo file `jvm-optimization.conf`:
```bash
-Xmx4g                    # Heap size tối đa 4GB
-XX:+UseG1GC             # Sử dụng G1 Garbage Collector
-XX:+UseStringDeduplication  # Tối ưu string
-Djava.awt.headless=false    # Hiển thị GUI
```

---

## 🏟️ Quản lý sân

### Giao diện chính (MainFrame)

**Cấu trúc giao diện:**
```
┌─────────────────────────────────────────────────────────┐
│ Menu Bar: File | Edit | View | Tournament | Help        │
├─────────────────────────────────────────────────────────┤
│ Navigation Tree        │ Main Content Area              │
│ ├── 🏠 Trang chủ        │                                │
│ ├── 🏟️ Thi đấu          │                                │
│ │   ├── Quản lý sân      │                                │
│ │   └── Giám sát         │                                │
│ ├── 🏆 Giải đấu         │                                │
│ ├── 📝 Nội dung         │                                │
│ ├── 🏛️ Câu lạc bộ       │                                │
│ ├── ✍️ Đăng ký          │                                │
│ └── 📊 Báo cáo          │                                │
├─────────────────────────────────────────────────────────┤
│ Status Bar: DB: ✅ | IP: 192.168.1.100 | RAM: 256/4096 MB │
└─────────────────────────────────────────────────────────┘
```

### Mở giao diện quản lý sân
1. **Từ Navigation Tree** → Click **"🏟️ Thi đấu"** → **"Quản lý sân"**
2. **Hoặc từ Menu Bar** → **"View"** → **"Court Management"**
3. **Giao diện MultiCourtControlPanel** sẽ hiển thị ở Main Content Area

### Thêm sân mới (Chi tiết từng bước)

**Bước 1: Chọn số sân**
- **Dropdown "Chọn sân"** hiển thị: "Sân 1", "Sân 2", "Sân 3", "Sân 4", "Sân 5"
- **Sân đã sử dụng** sẽ có dấu ✅ và màu xám
- **Sân trống** sẽ có màu trắng và có thể chọn
- Click vào sân trống để chọn

**Bước 2: Nhập thông tin sân**
- **Tiêu đề sân**: Nhập tên mô tả (VD: "Sân chính - Chung kết", "Sân phụ A")
- **Mô tả (tùy chọn)**: Thêm chi tiết về sân (VD: "Dành cho trận chung kết nam đơn")
- **Loại sân**: Indoor/Outdoor (mặc định Indoor)

**Bước 3: Tạo sân**
- Click **"Tạo sân"** hoặc **"Add Court"**
- Hệ thống sẽ:
  - Tạo **mã PIN 4 chữ số** ngẫu nhiên
  - Khởi tạo **BadmintonMatch** object
  - Thêm tab mới vào **MultiCourtControlPanel**
  - Hiển thị thông báo **"✅ Sân [X] đã được tạo thành công"**

**Bước 4: Xác nhận**
- **Tab sân mới** xuất hiện với tên đã nhập
- **Thông tin cơ bản** hiển thị:
  - 🏟️ **Tên sân**: [Tiêu đề đã nhập]
  - 🔐 **PIN**: [4 chữ số]
  - ⏰ **Thời gian tạo**: [Timestamp]
  - 📊 **Trạng thái**: Sẵn sàng
  - 🔗 **Web URL**: `http://[IP]:2345/scoreboard/[PIN]`

### Quản lý sân hiện có

**Xem tổng quan tất cả sân:**
- **Court Overview Panel** hiển thị danh sách tất cả sân
- **Thông tin mỗi sân**:
  ```
  🏟️ Sân 1: Sân chính - Chung kết    🔐 PIN: 1234
  ├── 📊 Trạng thái: Đang thi đấu
  ├── 👥 Trận đấu: Nguyễn A vs Trần B
  ├── 🏸 Loại: Nam đơn | BO3
  ├── 📈 Điểm số: 21-19, 15-12 
  ├── ⏱️ Thời gian: 23:45
  └── 🔗 Web: http://192.168.1.100:2345/scoreboard/1234
  ```

**Mở điều khiển sân cụ thể:**
- **Click vào tab sân** trong MultiCourtControlPanel
- **Hoặc double-click** vào sân trong Court Overview
- **BadmintonControlPanel** sẽ mở với giao diện đầy đủ
- **Kích thước tự động**: Panel tự động resize phù hợp với màn hình

**Các thao tác với sân:**
- **🎮 Mở điều khiển**: Click tab hoặc nút "Open Control"
- **📊 Xem bảng điểm**: Click "Show Scoreboard"
- **⚙️ Cài đặt sân**: Click biểu tượng gear để cấu hình
- **🔄 Reset sân**: Đặt lại trận đấu về trạng thái ban đầu
- **🗑️ Xóa sân**: Click nút "Delete" (có xác nhận)
- **📋 Copy thông tin**: Copy PIN hoặc URL để chia sẻ

---

## 🎮 Điều khiển trận đấu

### Giao diện BadmintonControlPanel

**Layout chính:**
```
┌─────────────────────────────────────────────────────────┐
│                    Thông tin sân                         │
│ 🏟️ Sân 1: Chung kết nam đơn    🔐 PIN: 1234            │
│ 🔗 Web: http://192.168.1.100:2345/scoreboard/1234      │
├─────────────────────────────────────────────────────────┤
│                   Thiết lập trận đấu                     │
│ Loại: ○ Đơn ● Đôi    Số ván: ○ BO1 ● BO3              │
│ Hiển thị: ● Ngang ○ Dọc                                │
├─────────────────────────────────────────────────────────┤
│                    Tên cầu thủ/đội                      │
│ Đội A: [Nguyễn Văn A        ] 🏛️ CLB Hà Nội             │
│ Đội B: [Trần Văn B          ] 🏛️ CLB TP.HCM            │
├─────────────────────────────────────────────────────────┤
│                     Bảng điểm số                        │
│        Đội A          vs          Đội B                 │
│    ┌─────────┐                ┌─────────┐               │
│    │   21    │ ← [+] [-]      │   19    │ [+] [-] →     │
│    └─────────┘                └─────────┘               │
│                                                         │
│    Ván thắng: 2                Ván thắng: 0            │
│    Server: ● Đội A            ○ Đội B                    │
├─────────────────────────────────────────────────────────┤
│                   Điều khiển trận đấu                   │
│ [Reset] [Ván tiếp] [Đổi sân] [Đổi server] [Hoàn tác]    │
├─────────────────────────────────────────────────────────┤
│                    Lịch sử điểm số                      │
│ Ván 1: 21-19 (A thắng)   Ván 2: 21-15 (A thắng)       │
├─────────────────────────────────────────────────────────┤
│              [📊 Mở bảng điểm] [⚙️ Cài đặt]              │
└─────────────────────────────────────────────────────────┘
```

### Mở bảng điều khiển sân
1. **Từ MultiCourtControlPanel** → Click **tab sân** cần điều khiển
2. **BadmintonControlPanel** sẽ mở trong tab mới
3. **Giao diện tự động điều chỉnh** theo kích thước màn hình
4. **Thông tin sân** hiển thị ở đầu panel (tên, PIN, URL)

### Thiết lập trận đấu (Chi tiết)

**Bước 1: Cấu hình cơ bản**
- **Loại trận đấu**:
  - ○ **Đơn (Singles)**: 1 người mỗi bên
  - ○ **Đôi (Doubles)**: 2 người mỗi bên  
  - Click radio button để chọn

- **Số ván thi đấu**:
  - ○ **Best of 1 (BO1)**: Chỉ thi đấu 1 ván, ai thắng trước thắng luôn
  - ○ **Best of 3 (BO3)**: Thi đấu tối đa 3 ván, ai thắng 2 ván trước thắng
  - Mặc định: BO3 cho thi đấu chính thức

- **Kiểu hiển thị bảng điểm**:
  - ● **Horizontal (Ngang)**: Phù hợp màn hình TV 16:9
  - ○ **Vertical (Dọc)**: Phù hợp màn hình dọc hoặc mobile

**Bước 2: Nhập thông tin cầu thủ**

**Cho trận Đơn:**
- **Cầu thủ A**: Nhập họ tên đầy đủ (VD: "Nguyễn Tiến Minh")
- **CLB A**: Chọn câu lạc bộ từ dropdown hoặc nhập mới
- **Cầu thủ B**: Nhập họ tên đầy đủ (VD: "Chen Long")  
- **CLB B**: Chọn câu lạc bộ tương ứng

**Cho trận Đôi:**
- **Đội A**: Nhập tên đôi hoặc "Tên 1 / Tên 2" (VD: "Nguyễn A / Trần B")
- **CLB A**: Câu lạc bộ của đôi A
- **Đội B**: Nhập tên đôi B (VD: "Lê C / Phạm D")
- **CLB B**: Câu lạc bộ của đôi B

**Bước 3: Xác nhận thiết lập**
- Click **"Bắt đầu trận đấu"** hoặc **"Start Match"**
- Hệ thống sẽ:
  - Khởi tạo điểm số 0-0
  - Đặt server mặc định (Đội A)
  - Chuẩn bị ghi nhận điểm số
  - Hiển thị thông báo **"✅ Trận đấu đã sẵn sàng"**

### Điều khiển điểm số (Chi tiết)

**Giao diện điều khiển điểm:**
```
        Đội A (Nguyễn Tiến Minh)          vs          Đội B (Chen Long)
    ┌─────────────┐                              ┌─────────────┐
    │     21      │ ← [+1] [+2] [-1] [Sửa]      │     19      │ [+1] [+2] [-1] [Sửa] →
    └─────────────┘                              └─────────────┘
         ● Server                                     ○ Server
    
    Ván hiện tại: 2/3        Thời gian: 12:34        Trạng thái: Đang chơi
    Lịch sử ván: 21-17 (A) | 15-21 (B) | 21-19 (A đang dẫn)
```

**Các nút điều khiển:**

1. **Tăng điểm (+1)**:
   - Click **[+1]** bên cạnh điểm đội tương ứng
   - Điểm tăng 1, cập nhật ngay lập tức
   - Tự động kiểm tra luật (21 điểm, cách biệt 2 điểm)
   - **Phím tắt**: `A` cho đội A, `B` cho đội B

2. **Tăng nhanh (+2)**:
   - Click **[+2]** để tăng 2 điểm cùng lúc
   - Dùng khi cầu thủ ghi được điểm liên tiếp
   - **Phím tắt**: `Shift+A`, `Shift+B`

3. **Giảm điểm (-1)**:
   - Click **[-1]** để sửa điểm nhầm
   - Chỉ giảm được nếu điểm > 0
   - **Phím tắt**: `Ctrl+A`, `Ctrl+B`

4. **Sửa điểm trực tiếp**:
   - Click **[Sửa]** hoặc click vào số điểm
   - Nhập điểm số mong muốn trong dialog
   - Dùng khi cần sửa điểm lớn

**Quy tắc ghi điểm tự động:**
- **Thắng ván**: 21 điểm và cách biệt ≥2 điểm
- **Deuce**: 20-20 → phải thắng cách biệt 2 điểm
- **Điểm tối đa**: 30 điểm (30-29 cũng thắng)
- **Tự động chuyển ván**: Khi có người thắng ván
- **Thắng trận BO3**: Ai thắng 2 ván trước

### Điều khiển trận đấu nâng cao

**1. Reset trận đấu**:
- Click **[Reset]**
- Hộp thoại xác nhận: **"Bạn có chắc muốn reset toàn bộ trận đấu?"**
- **[Reset All]**: Xóa tất cả (điểm, ván, tên cầu thủ)  
- **[Reset Score]**: Chỉ reset điểm số hiện tại về 0-0
- **[Cancel]**: Hủy bỏ

**2. Ván tiếp theo**:
- Click **[Ván tiếp]** khi kết thúc ván hiện tại
- Tự động chuyển sang ván mới (2/3 hoặc 3/3)
- Điểm số reset về 0-0
- Ghi nhận kết quả ván vừa rồi vào lịch sử

**3. Đổi sân (Change Ends)**:
- Click **[Đổi sân]**
- Hoán đổi vị trí hiển thị Đội A ↔ Đội B
- Dùng khi cầu thủ đổi sân giữa ván (11 điểm ở ván 3)
- **Ghi chú**: Chỉ đổi hiển thị, không ảnh hưởng điểm số

**4. Đổi giao cầu (Change Server)**:
- Click **[Đổi server]**
- Chuyển quyền giao cầu: A → B hoặc B → A
- **Indicator**: Dấu ● hiển thị bên người đang giao cầu
- **Tự động**: Hệ thống tự đổi server theo luật BWF

**5. Hoàn tác (Undo)**:
- Click **[Hoàn tác]**
- Hoàn tác thao tác gần nhất (tăng/giảm điểm, đổi server)
- **Giới hạn**: Chỉ hoàn tác được 1 bước gần nhất
- **Phím tắt**: `Ctrl+Z`

### Hiển thị bảng điểm (Scoreboard)

**Mở bảng điểm:**
1. Click **"📊 Mở bảng điểm"** trong BadmintonControlPanel
2. **Cửa sổ bảng điểm** mở ở màn hình riêng (hoặc màn hình thứ 2)
3. **Fullscreen tự động** để hiển thị rõ ràng cho khán giả

**Giao diện bảng điểm:**
```
┌─────────────────────────────────────────────────────────────┐
│                    CHUNG KẾT NAM ĐƠN                        │
│                                                             │
│     Nguyễn Tiến Minh        vs        Chen Long             │
│        (Việt Nam)                    (Trung Quốc)          │
│                                                             │
│    ┌─────────────────┐              ┌─────────────────┐     │
│    │       21        │              │       19        │     │
│    └─────────────────┘              └─────────────────┘     │
│                                                             │
│     Ván thắng: 2                     Ván thắng: 0          │
│                                                             │
│ ● Server                                                    │
│                                                             │
│           Ván 1: 21-17    Ván 2: 21-19                     │
│                                                             │
│                     Thời gian: 23:45                       │
└─────────────────────────────────────────────────────────────┘
```

**Tính năng bảng điểm:**
- **Cập nhật real-time**: Điểm số thay đổi ngay lập tức khi điều khiển
- **Responsive**: Tự động điều chỉnh font size theo màn hình
- **Kiểu hiển thị**: Horizontal (16:9) hoặc Vertical (9:16)
- **Thông tin đầy đủ**: Tên, CLB, điểm, ván thắng, server, thời gian
- **Always on top**: Luôn hiển thị trên cùng
- **Ẩn/hiện**: Press `F11` để toggle fullscreen

---

## 🌐 Điều khiển từ xa qua web

### Mã PIN cho mỗi sân
- **Mỗi sân có mã PIN duy nhất** 4 chữ số
- **PIN được tạo tự động** khi tạo sân mới
- **Hiển thị trong giao diện** BadmintonControlPanel

### Trang nhập PIN (PIN Entry)
- **URL chính**: `/pin`
- **Giao diện thân thiện** với mobile và desktop
- **QR Code tự động** để truy cập nhanh
- **Link chia sẻ** có thể copy và gửi cho người khác
- **Bàn phím số** để nhập PIN dễ dàng
- **Validation real-time** kiểm tra PIN hợp lệ

### Truy cập web interface (Chi tiết từng bước)

#### Bước chuẩn bị
1. **Lấy thông tin kết nối**:
   - **IP Address**: Xem trong status bar của ứng dụng desktop
   - **Port**: Mặc định 2345 (có thể thay đổi trong Settings)
   - **PIN sân**: Xem trong BadmintonControlPanel của sân cần truy cập

2. **Kiểm tra kết nối mạng**:
   - Đảm bảo thiết bị di động và máy tính **cùng mạng WiFi**
   - Test ping: `ping [IP_ADDRESS]` từ command prompt
   - Kiểm tra firewall không chặn port 2345

#### Cách 1: Trang nhập PIN (Khuyến nghị cho người dùng)

**Bước 1: Truy cập trang chính**
1. **Mở trình duyệt** trên thiết bị di động/tablet (Chrome, Safari, Firefox)
2. **Nhập URL**: 
   - `http://[IP_ADDRESS]:2345/`
   - `http://[IP_ADDRESS]:2345/pin`
   - **Ví dụ**: `http://192.168.1.100:2345/pin`
3. **Press Enter** để truy cập

**Bước 2: Giao diện PIN Entry hiển thị**
```
┌─────────────────────────────────────────────┐
│        🏸 BADMINTON TOURNAMENT SYSTEM        │
│                                            │
│              NHẬP MÃ PIN SÂN                │
│                                            │
│  ┌─────────────────────────────────────┐   │
│  │  [QR Code để truy cập nhanh]       │   │  
│  └─────────────────────────────────────┘   │
│                                            │
│  📱 Quét QR Code hoặc nhập PIN thủ công    │
│                                            │
│  ┌───┬───┬───┬───┐                        │
│  │ □ │ □ │ □ │ □ │  ← Nhập 4 chữ số       │
│  └───┴───┴───┴───┘                        │
│                                            │
│  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │
│  │   1   │   2   │   3   │   4   │   5   │ │
│  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ │
│  │   6   │   7   │   8   │   9   │   0   │ │
│  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ │
│  │     Xóa      │    Truy cập bảng điểm   │ │
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘ │
│                                            │
│  💡 Liên hệ trọng tài để lấy mã PIN        │
└─────────────────────────────────────────────┘
```

**Bước 3: Nhập PIN**
- **Cách 1 - Bàn phím ảo**: Tap vào các số 0-9 trên màn hình
- **Cách 2 - Bàn phím thiết bị**: Tap vào ô nhập PIN và dùng bàn phím
- **Cách 3 - QR Code**: Mở camera và quét QR code từ desktop app
- **Validation**: PIN sai sẽ hiển thị **"❌ PIN không hợp lệ"**
- **PIN đúng**: Hiển thị **"✅ PIN hợp lệ"**

**Bước 4: Truy cập bảng điểm**
- Click **"Truy Cập Bảng Điểm"** hoặc **"Access Scoreboard"**
- **Chuyển hướng tự động** đến `/scoreboard/[PIN]`
- **Giao diện điều khiển** sẽ hiển thị

#### Cách 2: Truy cập trực tiếp (Cho người am hiểu)
1. **Nhập URL trực tiếp**: `http://[IP]:2345/scoreboard/[PIN]`
   - **Ví dụ**: `http://192.168.1.100:2345/scoreboard/1234`
2. **Bỏ qua trang PIN entry**, vào thẳng bảng điều khiển
3. **Lỗi nếu PIN sai**: Hiển thị trang 404 hoặc chuyển về PIN entry

Ghi chú:
- Cổng mặc định: 2345; chạy trong mạng LAN để thiết bị khác truy cập.
- Hệ thống cũng cung cấp No-PIN API tại `/api/scoreboard/**` dành cho tích hợp đặc thù (không yêu cầu PIN).


### Giao diện điều khiển web (Mobile/Tablet)

**Layout responsive:**
```
Mobile Portrait (9:16)               Tablet Landscape (16:9)
┌─────────────────────┐             ┌─────────────────────────────────┐
│    🏸 SÂN 1 - 1234    │             │  🏸 SÂN 1 - PIN: 1234            │
├─────────────────────┤             ├─────────────────────────────────┤
│  Nguyễn A vs Trần B │             │ Nguyễn A (HN)    vs    Trần B (HCM) │
├─────────────────────┤             ├─────────────────────────────────┤
│    Đội A    Đội B   │             │   Đội A        vs        Đội B   │
│  ┌─────┐ ┌─────┐    │             │ ┌───────┐              ┌───────┐ │
│  │  21 │ │  19 │    │             │ │   21  │              │   19  │ │
│  └─────┘ └─────┘    │             │ └───────┘              └───────┘ │
│   [+] [-] [+] [-]   │             │ [+] [-]                [+] [-] │
├─────────────────────┤             ├─────────────────────────────────┤
│ Ván: 2-0  Server: A │             │ Ván: 2-0    Server: A   12:34   │
├─────────────────────┤             ├─────────────────────────────────┤
│     [Reset]         │             │ [Reset] [Đổi sân] [Đổi server]  │
│   [Đổi sân]         │             └─────────────────────────────────┘
│  [Đổi server]       │
└─────────────────────┘
```

### Điều khiển từ web (Chi tiết)

**1. Tăng/Giảm điểm số**:
- **Nút [+]**: Tap để tăng 1 điểm cho đội tương ứng
- **Nút [-]**: Tap để giảm 1 điểm (chỉ khi điểm > 0)
- **Long press [+]**: Giữ để tăng điểm liên tục (mỗi 200ms)
- **Feedback**: Rung nhẹ và hiệu ứng visual khi tap (mobile)
- **Tự động disable**: Nút [-] disable khi điểm = 0

**2. Hiển thị thông tin trận đấu**:
- **Tên cầu thủ/đội**: Rút gọn nếu quá dài trên mobile
- **Câu lạc bộ**: Hiển thị viết tắt (VD: "Hà Nội" → "HN")
- **Điểm số**: Font size lớn, dễ đọc
- **Server indicator**: Dấu ● bên cạnh đội đang giao cầu
- **Số ván thắng**: Hiển thị tỷ số ván (VD: "2-0")
- **Thời gian**: Hiển thị thời gian trận đấu (nếu có)

**3. Điều khiển trận đấu**:
- **[Reset]**: 
  - Tap để reset điểm về 0-0
  - Confirmation dialog: "Bạn có chắc muốn reset?"
  - **[Có]** / **[Không]**
  
- **[Đổi sân]**:
  - Hoán đổi vị trí hiển thị Đội A ↔ Đội B
  - Hiệu ứng animation swap
  - Toast message: "✅ Đã đổi sân"
  
- **[Đổi server]**:
  - Chuyển quyền giao cầu
  - Server indicator (●) chuyển sang đội khác
  - Toast message: "✅ Đã đổi giao cầu"

**4. Real-time updates**:
- **SSE Connection**: Kết nối Server-Sent Events tự động
- **Update indicator**: Dấu 🔄 xoay khi đang cập nhật
- **Connection status**: 
  - 🟢 "Connected" - Kết nối tốt
  - 🟡 "Reconnecting..." - Đang kết nối lại
  - 🔴 "Disconnected" - Mất kết nối
- **Fallback polling**: Nếu SSE fail, tự động chuyển sang polling mỗi 2s

**5. Responsive behaviors**:
- **Portrait mode**: Layout dọc, nút to, dễ tap
- **Landscape mode**: Layout ngang, tận dụng không gian
- **Touch targets**: Tối thiểu 44px cho dễ tap
- **Font scaling**: Tự động điều chỉnh theo kích thước màn hình
- **Safe area**: Tương thích với notch, home indicator

**6. Keyboard shortcuts (cho tablet có bàn phím)**:
- `A` / `ArrowLeft`: Tăng điểm Đội A
- `S` / `ArrowDown`: Giảm điểm Đội A  
- `D` / `ArrowRight`: Tăng điểm Đội B
- `W` / `ArrowUp`: Giảm điểm Đội B
- `R`: Reset trận đấu
- `C`: Đổi sân (Change ends)
- `V`: Đổi server
- `F5`: Refresh trang

### Tính năng mới trong BadmintonControlPanel
- **Hiển thị link `/pin`**: Link để nhập mã PIN
- **Hướng dẫn nhập PIN**: Hướng dẫn chi tiết cách sử dụng
- **Nút copy link PIN**: Copy nhanh link nhập PIN
- **Cập nhật tự động**: Link PIN tự động cập nhật khi thay đổi mạng

### Bảo mật
- **Mã PIN duy nhất** cho mỗi sân
- **Chỉ người biết PIN** mới điều khiển được
- **Không cần đăng nhập** phức tạp

---

## 📊 Giám sát và theo dõi

### MonitorTab - Giám sát tổng thể (Chi tiết)

**Giao diện Monitor Dashboard:**
```
┌─────────────────────────────────────────────────────────────────────────┐
│  🎯 GIÁM SÁT TẤT CẢ SÂN          Mode: ● Admin ○ Client    🔄 Auto: ON    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 🏟️ SÂN 1 - Chung kết nam đơn                    🔐 PIN: 1234     │   │
│  │ ├── 👥 Nguyễn Tiến Minh (VN) vs Chen Long (CN)                   │   │
│  │ ├── 📊 Điểm số: 21-19  (Ván 2/3)                                │   │
│  │ ├── 🏆 Ván thắng: 2-0                                           │   │
│  │ ├── 🏸 Server: Nguyễn Tiến Minh                                  │   │
│  │ ├── ⏱️ Thời gian: 23:45                                          │   │
│  │ ├── 🌐 Web: 3 devices connected                                  │   │
│  │ └── 📊 Trạng thái: 🟢 Đang thi đấu                               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 🏟️ SÂN 2 - Bán kết nữ đôi                      🔐 PIN: 5678     │   │
│  │ ├── 👥 Nguyễn A/Trần B vs Lê C/Phạm D                           │   │
│  │ ├── 📊 Điểm số: 15-12  (Ván 1/3)                                │   │
│  │ ├── 🏆 Ván thắng: 0-0                                           │   │
│  │ ├── 🏸 Server: Nguyễn A                                         │   │
│  │ ├── ⏱️ Thời gian: 08:23                                          │   │
│  │ ├── 🌐 Web: 1 device connected                                  │   │
│  │ └── 📊 Trạng thái: 🟢 Đang thi đấu                               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ 🏟️ SÂN 3 - Không có trận đấu                   🔐 PIN: 9012     │   │
│  │ └── 📊 Trạng thái: ⚪ Sẵn sàng                                   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│ 📊 Tổng quan: 3 sân active | 2 đang thi đấu | 1 sẵn sàng | ⏱️ 15:42:33  │
│ 🌐 Network: 192.168.1.100:2345 | 💾 DB: ✅ Connected | 🧠 RAM: 512/4096MB │
└─────────────────────────────────────────────────────────────────────────┘
```

### Mở giao diện giám sát
1. **Từ Navigation Tree** → Click **"🏟️ Thi đấu"** → **"Giám sát"**
2. **Hoặc từ Menu Bar** → **"View"** → **"Monitor Dashboard"**
3. **Hoặc phím tắt** → `Ctrl+M`
4. **MonitorTab** sẽ hiển thị trong Main Content Area

### Chế độ giám sát

**Admin Mode (Quản trị viên)**:
- **Quyền**: Xem tất cả sân trong hệ thống
- **Thông tin đầy đủ**: PIN, web connections, chi tiết trận đấu
- **Điều khiển**: Có thể reset sân, thay đổi cài đặt
- **Network scan**: Tự động tìm các instance BTMS khác trên mạng
- **Indicator**: ● Admin hiển thị màu xanh

**Client Mode (Người dùng)**:
- **Quyền**: Chỉ xem sân mà mình có quyền truy cập
- **Thông tin hạn chế**: Không hiện PIN, ít chi tiết hơn
- **Chỉ xem**: Không thể điều khiển hoặc thay đổi
- **Indicator**: ○ Client hiển thị màu xám

**Chuyển đổi Mode:**
- Click **"● Admin"** hoặc **"○ Client"** trong header
- Yêu cầu xác nhận quyền Admin (nếu có bảo mật)
- Auto-refresh lại dữ liệu theo mode mới

### Tính năng giám sát nâng cao

**1. Auto-refresh thông minh**:
- **Interval**: Mặc định 5 giây, có thể điều chỉnh (2s-30s)
- **Smart refresh**: Chỉ cập nhật khi có thay đổi thực sự
- **Pause when inactive**: Tạm dừng khi tab không active
- **Visual indicator**: 🔄 icon xoay khi đang refresh
- **Toggle**: Click 🔄 để bật/tắt auto-refresh

**2. Debounced updates (Tránh nhảy hình)**:
- **Delay 500ms**: Chờ 500ms trước khi cập nhật UI
- **Batch updates**: Gom nhiều thay đổi thành 1 lần update
- **Stable positioning**: Các card sân không nhảy liên tục
- **Smooth animations**: Hiệu ứng mượt mà khi thay đổi

**3. Chi tiết thông tin mỗi sân**:
- **Tên sân**: Tiêu đề do người dùng đặt
- **PIN**: Mã 4 chữ số (chỉ Admin thấy)
- **Tên cầu thủ**: Đầy đủ hoặc rút gọn nếu quá dài
- **Câu lạc bộ**: Trong ngoặc sau tên
- **Điểm số hiện tại**: Format "21-19"  
- **Ván hiện tại**: Format "Ván 2/3"
- **Số ván thắng**: Format "2-0"
- **Server**: Người đang giao cầu
- **Thời gian trận**: Đếm từ lúc bắt đầu
- **Web connections**: Số thiết bị đang kết nối web
- **Trạng thái**: 
  - 🟢 **Đang thi đấu**: Có trận đấu đang diễn ra
  - 🟡 **Tạm dừng**: Trận đấu bị pause
  - ⚪ **Sẵn sàng**: Không có trận đấu
  - 🔴 **Lỗi**: Có vấn đề với sân

**4. Floating window (Cửa sổ nổi)**:
- **Detach**: Click biểu tượng 📌 để tách ra cửa sổ riêng
- **Always on top**: Luôn hiển thị trên cùng
- **Resize**: Có thể thay đổi kích thước
- **Multi-monitor**: Có thể đưa sang màn hình thứ 2
- **Re-dock**: Drag về để gắn lại vào MainFrame

**5. Tương tác với sân**:
- **Double-click sân**: Mở BadmintonControlPanel của sân đó
- **Right-click menu**:
  - 🎮 "Open Control Panel"
  - 📊 "Show Scoreboard"  
  - ⚙️ "Court Settings"
  - 🔄 "Reset Court"
  - 🗑️ "Delete Court" (Admin only)
  - 📋 "Copy PIN" (Admin only)
  - 🔗 "Copy Web URL"

**6. Bộ lọc và tìm kiếm**:
- **Filter by status**: Chỉ hiện sân đang thi đấu/sẵn sàng
- **Search**: Tìm theo tên sân, tên cầu thủ
- **Sort**: Sắp xếp theo tên sân, thời gian, trạng thái
- **Group**: Nhóm theo trạng thái hoặc loại trận

### Troubleshooting Monitor

**Sân không hiển thị:**
- Kiểm tra chế độ Admin/Client
- Refresh manual bằng `F5`
- Kiểm tra kết nối mạng

**Thông tin không cập nhật:**
- Kiểm tra auto-refresh có bật không
- Xem connection status trong status bar
- Test ping đến IP của máy chủ

**Performance chậm:**
- Tăng refresh interval lên 10-15s
- Giảm số sân hiển thị cùng lúc
- Tắt animations nếu cần

---

## ⚙️ Cài đặt hệ thống

### Truy cập trang Settings
1. **Từ giao diện chính** → Click menu "Settings" hoặc biểu tượng cài đặt
2. **Trang Settings** sẽ hiển thị với các tab cấu hình

### Tab Database (Cấu hình cơ sở dữ liệu)
#### SQL Server Configuration
- **Server**: Địa chỉ SQL Server (ví dụ: `GODZILLA\SQLDEV`, `localhost`)
- **Port**: Cổng kết nối (mặc định: 1433)
- **Database**: Tên database (ví dụ: `badminton_tournament`)
- **Username**: Tên đăng nhập SQL Server
- **Password**: Mật khẩu
- **Connection Options**:
  - ☑️ Encrypt: Mã hóa kết nối
  - ☑️ Trust Server Certificate: Tin tương chứng chỉ server
  - ☑️ Integrated Security: Sử dụng Windows Authentication (nếu có)

#### H2 Database (cho testing)
- **File Path**: Đường dẫn file database H2
- **Mode**: In-memory hoặc file-based
- **Auto Server**: Cho phép kết nối từ nhiều ứng dụng

#### Test Connection
- Click **"Test Connection"** để kiểm tra kết nối
- Nếu thành công: hiển thị "✅ Connection successful"
- Nếu thất bại: hiển thị lỗi chi tiết và cách khắc phục

### Tab Network (Cấu hình mạng)
#### Network Interface Selection
- **Available Interfaces**: Danh sách các card mạng có sẵn
- **Current Interface**: Card mạng đang sử dụng
- **IP Address**: Địa chỉ IP hiện tại
- **Status**: Trạng thái kết nối

#### Server Configuration  
- **Server Port**: Cổng web server (mặc định: 2345)
- **Bind Address**: Địa chỉ bind (mặc định: 0.0.0.0 - tất cả interface)
- **CORS Settings**: Cấu hình CORS cho API

### Tab Display (Cài đặt hiển thị)
#### Theme Settings
- **Light Theme**: Giao diện sáng (mặc định)
- **Dark Theme**: Giao diện tối
- **System**: Theo hệ thống

#### Font & Scaling
- **Font Size**: Kích thước chữ (Small, Medium, Large)
- **UI Scale**: Tỷ lệ giao diện (100%, 125%, 150%)
- **Font Family**: Chọn font chữ

#### Scoreboard Display
- **Default Layout**: Horizontal hoặc Vertical
- **Auto-fit Screen**: Tự động điều chỉnh kích thước
- **Show Clock**: Hiển thị đồng hồ

### Tab Performance (Hiệu suất)
#### Memory Settings
- **Heap Size**: Kích thước bộ nhớ heap (MB)
- **GC Type**: Loại Garbage Collector (G1GC, Parallel, etc.)
- **Memory Monitoring**: Theo dõi sử dụng RAM

#### Database Performance
- **Connection Pool Size**: Số kết nối tối đa (mặc định: 10)
- **Query Timeout**: Timeout cho truy vấn (giây)
- **Cache Settings**: Cấu hình cache

#### Network Performance
- **SSE Update Interval**: Tần suất cập nhật SSE (ms)
- **Thread Pool Size**: Số thread cho SSE broadcasting
- **Connection Timeout**: Timeout kết nối mạng

### Tab Security (Bảo mật)
#### PIN Management
- **Auto-generate PINs**: Tự động tạo PIN cho sân mới
- **PIN Length**: Độ dài PIN (4-8 chữ số)
- **PIN Expiry**: Thời gian hết hạn PIN

#### Access Control
- **Admin Mode**: Chế độ quản trị viên
- **Client Restrictions**: Giới hạn quyền client
- **IP Whitelist**: Danh sách IP được phép truy cập

### Tab Advanced (Nâng cao)
#### Logging
- **Log Level**: DEBUG, INFO, WARN, ERROR
- **Log File Path**: Đường dẫn file log
- **Max Log Size**: Kích thước tối đa file log

#### Backup & Recovery
- **Auto Backup**: Tự động sao lưu database
- **Backup Interval**: Tần suất sao lưu
- **Backup Location**: Thư mục lưu backup

#### Developer Options
- **Debug Mode**: Chế độ debug
- **API Documentation**: Hiển thị API docs
- **Performance Metrics**: Hiển thị metrics hiệu suất

### Áp dụng cài đặt
1. **Save Settings**: Click "Save" để lưu cài đặt
2. **Apply**: Áp dụng cài đặt mà không cần restart
3. **Restart Required**: Một số cài đặt cần restart ứng dụng
4. **Reset to Default**: Khôi phục cài đặt mặc định

### Export/Import Settings
- **Export**: Xuất cài đặt ra file JSON
- **Import**: Nhập cài đặt từ file JSON
- **Backup**: Sao lưu cài đặt hiện tại

---

## ⚡ Tối ưu hiệu suất

### Cấu hình JVM
```bash
# Tăng heap size
java -Xmx4g -jar BadmintonEventTechnology.jar

# Sử dụng G1GC
java -XX:+UseG1GC -jar BadmintonEventTechnology.jar

# Kết hợp nhiều options
java -Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication -jar BadmintonEventTechnology.jar
```

### Tối ưu ứng dụng
- **Giảm tần suất refresh**: Timer từ 2s → 5s
- **Debouncing**: Delay 500ms để tránh update quá nhiều
- **Smart updates**: Chỉ rebuild UI khi cần thiết
- **In-place data updates**: Không tạo objects mới

### Tối ưu web interface
- **SSE throttling**: Giới hạn tần suất gửi events (~80ms)
- **Auto-refresh interval**: Tăng thời gian refresh
- **Efficient broadcasting**: Sử dụng thread pool (8 threads)

---

## 🔧 Xử lý sự cố

### Vấn đề thường gặp sau khi cài đặt

#### 1. Không tìm thấy ứng dụng sau khi cài đặt
**Triệu chứng**: Không có shortcut hoặc menu item
**Giải pháp**:
- Kiểm tra thư mục cài đặt: `D:\BTMS\` hoặc `C:\BTMS\`
- Tìm file exe trong thư mục cài đặt
- Tạo shortcut thủ công từ file exe
- Kiểm tra Windows Start Menu → "Badminton Tournament"

#### 2. Lỗi "Java not found" khi khởi động
**Triệu chứng**: Thông báo thiếu Java runtime
**Giải pháp**:
- MSI đã bao gồm JRE, kiểm tra thư mục cài đặt có folder `runtime\`
- Nếu thiếu JRE, cài đặt lại từ MSI installer
- Hoặc cài đặt Java 21+ từ trang chủ Oracle/OpenJDK

#### 3. Ứng dụng không khởi động được
**Triệu chứng**: Double-click không phản hồi hoặc tắt ngay
**Giải pháp**:
- Chạy với quyền Administrator
- Kiểm tra Windows Defender/Antivirus đã block chưa
- Xem Windows Event Viewer để tìm lỗi chi tiết
- Chạy từ Command Prompt để xem lỗi: `cd "C:\BTMS" && "Badminton Tournament Management System (BTMS).exe"`

#### 4. Cổng 2345 đã được sử dụng
**Triệu chứng**: Lỗi "Port 2345 already in use"
**Giải pháp**:
- Kiểm tra ứng dụng khác đang dùng cổng: `netstat -ano | findstr 2345`
- Đóng ứng dụng đang chiếm cổng
- Hoặc thay đổi cổng trong Settings → Network → Server Port

#### 5. Ứng dụng chạy chậm sau khi cài đặt
**Triệu chứng**: Giao diện lag, không phản hồi
**Giải pháp**:
- Kiểm tra RAM available (cần tối thiểu 4GB free)
- Vào Settings → Performance → tăng Heap Size
- Đóng các ứng dụng không cần thiết
- Thay đổi GC Type thành G1GC nếu có nhiều RAM

#### 2. Web interface không cập nhật
**Triệu chứng**: Điểm số trên web không thay đổi
**Giải pháp**:
- Kiểm tra PIN có đúng không
- Kiểm tra kết nối mạng
- Restart ứng dụng

#### 3. Bảng điểm bị nhảy liên tục
**Triệu chứng**: Nội dung các ô nhảy liên tục
**Giải pháp**:
- Đã được sửa trong phiên bản mới
- Sử dụng debouncing và smart updates
- Tái sử dụng Row objects

#### 6. Lỗi kết nối Database lần đầu
**Triệu chứng**: "Database connection failed"  
**Giải pháp**:
- Vào Settings → Database tab
- Kiểm tra thông tin SQL Server (server, port, database name)
- Click "Test Connection" để kiểm tra
- Nếu dùng SQL Server: đảm bảo SQL Server đang chạy và cho phép TCP/IP
- Nếu dùng H2: kiểm tra đường dẫn file database

#### 7. Không thể kết nối từ thiết bị khác
**Triệu chứng**: Không mở được web interface từ phone/tablet
**Giải pháp**:
- Kiểm tra Windows Firewall đã cho phép ứng dụng chưa
- Vào Settings → Network → kiểm tra IP address hiện tại
- Đảm bảo thiết bị di động cùng mạng WiFi với máy tính
- Test bằng ping từ phone đến IP máy tính
- Thử truy cập `http://[IP]:2345/pin` từ trình duyệt di động

### Gỡ cài đặt ứng dụng
#### Cách 1: Từ Windows Settings
1. **Windows 11**: Settings → Apps → Installed apps
2. **Windows 10**: Settings → Apps & features
3. Tìm "Badminton Tournament Management System (BTMS)"
4. Click "Uninstall"

#### Cách 2: Từ Control Panel
1. Control Panel → Programs → Programs and Features
2. Tìm "Badminton Tournament Management System (BTMS)"
3. Right-click → Uninstall

#### Cách 3: Từ Start Menu
1. Tìm ứng dụng trong Start Menu
2. Right-click → Uninstall

### Cập nhật ứng dụng
1. **Tải MSI mới** từ trang phát hành
2. **Chạy MSI mới** → sẽ tự động ghi đè phiên bản cũ
3. **Dữ liệu và cài đặt** sẽ được giữ lại
4. **Khởi động lại** ứng dụng để áp dụng cập nhật

### Log và Debug
- **Application Logs**: Xem trong Settings → Advanced → Logging
- **Windows Event Viewer**: Applications and Services Logs
- **Health check**: `http://[IP]:2345/api/court/health`
- **Web test**: `http://[IP]:2345/pin`
- **Command line debug**: Chạy exe từ Command Prompt để xem lỗi real-time

---

## 📱 Tính năng nâng cao

### Multi-court synchronization
- **Đồng bộ real-time** giữa các sân
- **Shared data** cho giải đấu lớn
- **Centralized control** từ một điểm

### Responsive design
- **Tự động điều chỉnh** kích thước theo màn hình
- **Mobile-friendly** web interface
- **Touch-optimized** controls

### Performance monitoring
- **Memory usage** tracking
- **Network performance** monitoring
- **UI responsiveness** metrics

---

## 🎯 Lời khuyên sử dụng

### Cho người quản lý
1. **Tạo sân trước** khi bắt đầu giải đấu
2. **Ghi nhớ PIN** của từng sân
3. **Kiểm tra kết nối mạng** trước khi sử dụng
4. **Monitor performance** khi có nhiều sân

### Cho trọng tài
1. **Sử dụng desktop app** để điều khiển chính
2. **Web interface** để điều khiển từ xa
3. **Kiểm tra PIN** trước khi sử dụng web
4. **Test điều khiển** trước trận đấu

### Cho khán giả
1. **Truy cập web interface** để xem điểm số
2. **Refresh trang** nếu cần cập nhật
3. **Sử dụng mobile** để dễ xem

---

## 📞 Hỗ trợ kỹ thuật

### Thông tin liên hệ
- **Developer**: Nguyen Viet Hau
- **Version**: 2.0.0 (Multi-Court Edition)
- **Last Updated**: 2025

### Tài liệu bổ sung
- `README.md` - Tổng quan dự án và cài đặt
- `BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md` - Báo cáo kỹ thuật & công nghệ
- `SETTINGS.md` - Cấu hình chi tiết trong ứng dụng

### Báo cáo lỗi
Khi gặp vấn đề, vui lòng cung cấp:
1. **Mô tả lỗi** chi tiết
2. **Các bước** để tái hiện lỗi
3. **Screenshot** nếu có thể
4. **Thông tin hệ thống** (OS, Java version, RAM)

---

**🎉 Chúc bạn sử dụng hệ thống hiệu quả! 🏸**
