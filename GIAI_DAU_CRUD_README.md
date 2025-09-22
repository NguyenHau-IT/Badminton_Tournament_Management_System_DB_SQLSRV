# Tính năng CRUD Giải đấu - Badminton Event Technology

## 🎯 Tổng quan

Đã thêm tính năng CRUD (Create, Read, Update, Delete) hoàn chỉnh cho quản lý giải đấu, sử dụng bảng `GIAI_DAU` trong SQL Server.

## 📊 Schema Database

### **Bảng GIAI_DAU:**
```sql
CREATE TABLE GIAI_DAU (
    ID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TEN_GIAI NVARCHAR(255) NOT NULL,
    NGAY_BD DATETIME2,
    NGAY_KT DATETIME2,
    NGAY_TAO DATETIME2 NOT NULL DEFAULT GETDATE(),
    NGAY_CAP_NHAT DATETIME2 NOT NULL DEFAULT GETDATE(),
    ID_USER BIGINT
)
```

## 🏗️ Kiến trúc Code

### **1. Model Layer**
- **`GiaiDau.java`**: Entity class cho bảng GIAI_DAU
  - Các thuộc tính: id, tenGiai, ngayBd, ngayKt, ngayTao, ngayCapNhat, idUser
  - Utility methods: isActive(), isUpcoming(), isFinished()

### **2. Repository Layer**
- **`GiaiDauRepository.java`**: CRUD operations với database
  - `create()`: Tạo giải đấu mới
  - `findAll()`: Lấy tất cả giải đấu
  - `findById()`: Lấy giải đấu theo ID
  - `findByName()`: Tìm kiếm theo tên
  - `findByUserId()`: Lấy giải đấu theo User ID
  - `update()`: Cập nhật giải đấu
  - `delete()`: Xóa giải đấu
  - `count()`: Đếm số lượng
  - `exists()`: Kiểm tra tồn tại

### **3. Service Layer**
- **`GiaiDauService.java`**: Business logic
  - Validation rules
  - Error handling
  - Filter methods: getActiveGiaiDau(), getUpcomingGiaiDau(), getFinishedGiaiDau()

### **4. UI Layer**
- **`GiaiDauManagementPanel.java`**: Panel chính cho CRUD operations
- **`GiaiDauDialog.java`**: Dialog để thêm/sửa giải đấu
- **`TournamentTabPanel.java`**: Tab container (đã được refactor)

## ✨ Tính năng

### **1. Quản lý Giải đấu (CRUD)**
- ➕ **Thêm mới**: Tạo giải đấu với thông tin đầy đủ
- 👁️ **Xem danh sách**: Hiển thị tất cả giải đấu trong table
- ✏️ **Sửa**: Cập nhật thông tin giải đấu
- 🗑️ **Xóa**: Xóa giải đấu với xác nhận

### **2. Tìm kiếm và Lọc**
- 🔍 **Tìm kiếm theo tên**: Tìm giải đấu theo tên
- 🏷️ **Lọc theo trạng thái**:
  - Tất cả
  - Đang hoạt động
  - Sắp tới
  - Đã kết thúc

### **3. Trạng thái Giải đấu**
- 🟢 **Đang hoạt động**: Ngày hiện tại nằm giữa ngày bắt đầu và kết thúc
- 🟡 **Sắp tới**: Ngày hiện tại trước ngày bắt đầu
- 🔴 **Đã kết thúc**: Ngày hiện tại sau ngày kết thúc
- ⚪ **Không xác định**: Thiếu thông tin ngày tháng

## 🖥️ Giao diện Người dùng

### **Tab "Quản lý Giải đấu":**
```
┌─────────────────────────────────────────────────────────┐
│ Tìm kiếm: [________________] [Tìm kiếm] Lọc theo: [▼] │
│ [Làm mới] [Thêm mới] [Sửa] [Xóa]                       │
├─────────────────────────────────────────────────────────┤
│ ID │ Tên Giải    │ Ngày Bắt Đầu │ Ngày Kết Thúc │ ... │
├────┼─────────────┼──────────────┼───────────────┼─────┤
│ 1  │ Giải A      │ 01/01/2024   │ 05/01/2024    │ ... │
│ 2  │ Giải B      │ 10/01/2024   │ 15/01/2024    │ ... │
└────┴─────────────┴──────────────┴───────────────┴─────┘
```

### **Dialog Thêm/Sửa:**
```
┌─────────────────────────────────────┐
│ Thêm giải đấu mới                   │
├─────────────────────────────────────┤
│ Tên giải đấu *: [________________] │
│ Ngày bắt đầu:   [dd/MM/yyyy HH:mm] │
│ Ngày kết thúc:  [dd/MM/yyyy HH:mm] │
│ ID User *:      [________]         │
│                                     │
│              [Thêm mới] [Hủy]      │
└─────────────────────────────────────┘
```

## 🔧 Cách sử dụng

### **1. Thêm giải đấu mới:**
1. Nhấn nút "Thêm mới"
2. Điền thông tin:
   - **Tên giải đấu**: Bắt buộc
   - **Ngày bắt đầu**: Tùy chọn (định dạng dd/MM/yyyy HH:mm)
   - **Ngày kết thúc**: Tùy chọn
   - **ID User**: Bắt buộc
3. Nhấn "Thêm mới"

### **2. Sửa giải đấu:**
1. Chọn giải đấu trong bảng
2. Nhấn nút "Sửa"
3. Cập nhật thông tin trong dialog
4. Nhấn "Cập nhật"

### **3. Xóa giải đấu:**
1. Chọn giải đấu trong bảng
2. Nhấn nút "Xóa"
3. Xác nhận trong popup

### **4. Tìm kiếm:**
1. Nhập tên giải đấu vào ô tìm kiếm
2. Nhấn "Tìm kiếm"

### **5. Lọc theo trạng thái:**
1. Chọn trạng thái từ dropdown
2. Bảng sẽ tự động cập nhật

## 📝 Validation Rules

### **Tạo mới:**
- Tên giải đấu không được để trống
- ID User phải là số và không được để trống
- Ngày bắt đầu không được sau ngày kết thúc

### **Cập nhật:**
- Tất cả validation như tạo mới
- ID giải đấu phải tồn tại

### **Xóa:**
- Giải đấu phải tồn tại
- Xác nhận trước khi xóa

## 🗂️ API Methods

### **GiaiDauService:**
```java
// CRUD Operations
GiaiDau createGiaiDau(String tenGiai, LocalDateTime ngayBd, LocalDateTime ngayKt, Long idUser)
List<GiaiDau> getAllGiaiDau()
Optional<GiaiDau> getGiaiDauById(Long id)
List<GiaiDau> searchGiaiDauByName(String tenGiai)
List<GiaiDau> getGiaiDauByUserId(Long userId)
boolean updateGiaiDau(GiaiDau giaiDau)
boolean deleteGiaiDau(Long id)

// Filter Methods
List<GiaiDau> getActiveGiaiDau()
List<GiaiDau> getUpcomingGiaiDau()
List<GiaiDau> getFinishedGiaiDau()

// Utility Methods
long countGiaiDau()
boolean existsGiaiDau(Long id)
```

### **GiaiDauManagementPanel:**
```java
void loadData()
void refreshData()
GiaiDau getSelectedGiaiDau()
```

### **TournamentTabPanel:**
```java
GiaiDau getSelectedGiaiDau()
void refreshGiaiDau()
boolean hasValidSelection()
GiaiDauService getGiaiDauService()
```

## 🔄 Migration từ Tournament

### **Đã xóa:**
- ❌ `Tournament.java` - Entity cũ
- ❌ `TournamentRepository.java` - Repository cũ
- ❌ `TournamentSelectionPanel.java` - UI component cũ

### **Thay thế bằng:**
- ✅ `GiaiDau.java` - Entity mới với schema đầy đủ
- ✅ `GiaiDauRepository.java` - Repository với CRUD operations
- ✅ `GiaiDauManagementPanel.java` - UI component với tính năng đầy đủ

## 🎯 Lợi ích

### **Cho Developer:**
- 🏗️ **Clean Architecture**: Tách biệt rõ ràng các layer
- 🔧 **Maintainable**: Code dễ bảo trì và mở rộng
- 🧪 **Testable**: Có thể unit test từng component

### **Cho User:**
- 🚀 **Đầy đủ tính năng**: CRUD hoàn chỉnh
- 🔍 **Tìm kiếm linh hoạt**: Nhiều cách lọc và tìm kiếm
- 📊 **Hiển thị trạng thái**: Biết được giải đấu đang ở giai đoạn nào
- ✅ **Validation**: Đảm bảo dữ liệu chính xác

## 🔮 Tính năng tương lai

- 📈 **Thống kê**: Biểu đồ số liệu giải đấu
- 📅 **Lịch**: Hiển thị giải đấu theo lịch
- 🔔 **Thông báo**: Nhắc nhở về giải đấu sắp tới
- 📤 **Export**: Xuất danh sách giải đấu ra Excel/PDF

---

**Kết quả**: Hệ thống quản lý giải đấu hoàn chỉnh với CRUD operations, tìm kiếm, lọc và validation đầy đủ! 🎉
