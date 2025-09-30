# 🏸 HƯỚNG DẪN SỬ DỤNG HỆ THỐNG QUẢN LÝ ĐA SÂN CẦU LÔNG

## 📋 Mục lục
1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Cài đặt và khởi động](#cài-đặt-và-khởi-động)
3. [Quản lý sân](#quản-lý-sân)
4. [Điều khiển trận đấu](#điều-khiển-trận-đấu)
5. [Điều khiển từ xa qua web](#điều-khiển-từ-xa-qua-web)
6. [Giám sát và theo dõi](#giám-sát-và-theo-dõi)
7. [Tối ưu hiệu suất](#tối-ưu-hiệu-suất)
8. [Xử lý sự cố](#xử-lý-sự-cố)

---

## 🎯 Tổng quan hệ thống

Hệ thống quản lý đa sân cầu lông cho phép:
- **Quản lý nhiều sân** trên cùng một máy tính
- **Điều khiển trận đấu** từ giao diện desktop
- **Điều khiển từ xa** qua web interface với mã PIN
- **Giám sát real-time** tất cả các sân
- **Đồng bộ dữ liệu** giữa app và web

### 🏗️ Kiến trúc hệ thống
```
Desktop App ←→ CourtManagerService ←→ Multiple CourtSessions
     ↓              ↓                        ↓
Web Interface ←→ ScoreboardPinController ←→ BadmintonMatch
```

---

## 🚀 Cài đặt và khởi động

### Yêu cầu hệ thống
- **Java 17+** (bắt buộc cho Spring Boot 3.2.x)
- **RAM**: Tối thiểu 4GB, khuyến nghị 8GB+
- **Mạng LAN**: Để kết nối giữa các thiết bị

### Khởi động ứng dụng
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

### Mở giao diện quản lý sân
1. **Khởi động ứng dụng** → Màn hình chính
2. **Chọn tab "Quản lý sân"** trong giao diện chính
3. **Giao diện MultiCourtControlPanel** sẽ hiển thị

### Thêm sân mới
1. **Chọn sân** từ dropdown "Sân 1", "Sân 2", ..., "Sân 5"
2. **Nhập tiêu đề** cho sân (ví dụ: "Giải vô địch quốc gia")
3. **Click "Thêm sân"**
4. **Sân mới** sẽ xuất hiện trong danh sách

### Quản lý sân hiện có
- **Xem tổng quan**: Thông tin cơ bản của sân
- **Mở điều khiển**: Click vào tab sân để mở BadmintonControlPanel
- **Xóa sân**: Click nút "Xóa" để loại bỏ sân

---

## 🎮 Điều khiển trận đấu

### Mở bảng điều khiển sân
1. **Chọn tab sân** trong MultiCourtControlPanel
2. **BadmintonControlPanel** sẽ mở với kích thước tối ưu
3. **Giao diện responsive** tự động điều chỉnh theo màn hình

### Thiết lập trận đấu
1. **Chọn loại trận**: Đơn hoặc Đôi
2. **Chọn số ván**: Best of 1, Best of 3
3. **Nhập tên cầu thủ/đội**:
   - **Đơn**: Tên A vs Tên B
   - **Đôi**: Đội A vs Đội B
4. **Chọn kiểu hiển thị**: HORIZONTAL hoặc VERTICAL

### Điều khiển điểm số
- **Tăng điểm A**: Click nút "+" bên cạnh điểm đội A
- **Tăng điểm B**: Click nút "+" bên cạnh điểm đội B
- **Giảm điểm**: Click nút "-" để giảm điểm
- **Reset**: Đặt lại điểm số về 0-0
- **Đổi sân**: Hoán đổi vị trí hai đội
- **Đổi Giao cầu**: đổi ng giao cầu

### Hiển thị bảng điểm
- **Mở bảng điểm**: Click "Mở bảng điểm"
- **Bảng điểm** sẽ hiển thị trên màn hình riêng
- **Cập nhật real-time** khi thay đổi điểm số

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

### Truy cập web interface

#### Cách 1: Trang nhập PIN (Khuyến nghị)
1. **Lấy IP máy chủ** từ giao diện ứng dụng
2. **Mở trình duyệt** trên thiết bị di động/tablet
3. **Nhập URL chính**: `http://IP:2345/` hoặc `http://IP:2345/pin` (port mặc định 2345)
   - Ví dụ: `http://192.168.1.100:2345/`
4. **Giao diện PIN entry** sẽ hiển thị:
   - **QR Code**: Quét bằng camera để truy cập nhanh
   - **Link truy cập**: Copy và chia sẻ cho người khác
   - **Bàn phím số**: Nhập PIN 4 chữ số
5. **Nhập mã PIN** 4 chữ số vào giao diện
6. **Click "Truy Cập Bảng Điểm"** để vào trang điều khiển

#### Cách 2: Truy cập trực tiếp
1. **Nhập URL trực tiếp**: `http://IP:2345/scoreboard/PIN`
   - Ví dụ: `http://192.168.1.100:2345/scoreboard/1234`


### Điều khiển từ web
- **Giao diện responsive** tối ưu cho mobile
- **Nút điều khiển điểm số**:
  - Tăng/giảm điểm đội A
  - Tăng/giảm điểm đội B
  - Reset trận đấu
  - Đổi sân
- **Cập nhật real-time** với desktop app

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

### MonitorTab - Giám sát tổng thể
1. **Mở tab "Giám sát"** trong giao diện chính
2. **Xem tất cả sân** đang hoạt động
3. **Thông tin real-time**:
   - Tên cầu thủ/đội
   - Điểm số hiện tại
   - Số ván đã thắng
   - Thời gian cập nhật cuối

### Chế độ Admin vs Client
- **Admin mode**: Xem tất cả sân trên mạng
- **Client mode**: Chỉ xem sân của mình
- **Chuyển đổi mode** bằng nút trong header

### Tính năng giám sát
- **Auto-refresh**: Tự động cập nhật mỗi 5 giây
- **Debounced updates**: Tránh nhảy liên tục
- **Stable display**: Nội dung ổn định, dễ đọc
- **Floating window**: Tách ra cửa sổ riêng nếu cần

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
- **SSE throttling**: Giảm tần suất gửi events
- **Auto-refresh interval**: Tăng thời gian refresh
- **Efficient broadcasting**: Sử dụng thread pool

---

## 🔧 Xử lý sự cố

### Vấn đề thường gặp

#### 1. Ứng dụng chạy chậm
**Triệu chứng**: Giao diện lag, không phản hồi
**Giải pháp**:
- Tăng heap size: `java -Xmx4g -jar app.jar`
- Kiểm tra RAM usage
- Đóng các ứng dụng không cần thiết

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

#### 4. Không thể kết nối từ thiết bị khác
**Triệu chứng**: Không mở được web interface
**Giải pháp**:
- Kiểm tra firewall
- Kiểm tra IP address
- Đảm bảo cùng mạng LAN

### Log và Debug
- **Console output**: Xem log trong terminal
- **Health check**: `/health` endpoint
- **Test endpoint**: `/test` để kiểm tra kết nối

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
- **Developer**: Badminton Event Technology Team
- **Version**: 2.0 (Multi-Court Edition)
- **Last Updated**: 2024

### Tài liệu bổ sung
- `PERFORMANCE_OPTIMIZATION.md` - Hướng dẫn tối ưu hiệu suất
- `MULTI_COURT_ARCHITECTURE.md` - Kiến trúc hệ thống
- `API_DOCUMENTATION.md` - Tài liệu API

### Báo cáo lỗi
Khi gặp vấn đề, vui lòng cung cấp:
1. **Mô tả lỗi** chi tiết
2. **Các bước** để tái hiện lỗi
3. **Screenshot** nếu có thể
4. **Thông tin hệ thống** (OS, Java version, RAM)

---

**🎉 Chúc bạn sử dụng hệ thống hiệu quả! 🏸**
