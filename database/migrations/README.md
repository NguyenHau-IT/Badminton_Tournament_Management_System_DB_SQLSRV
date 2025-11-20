# 🚀 Database Migration Execution Guide

## 📋 Tổng quan

Hướng dẫn này sẽ giúp bạn thực thi các migration scripts một cách an toàn để nâng cấp database cho Web Platform.

---

## ⚠️ QUAN TRỌNG - ĐỌC TRƯỚC KHI THỰC HIỆN

### 1. Backup Database
```sql
-- SQL Server: Tạo backup trước khi thực hiện bất kỳ thay đổi nào
BACKUP DATABASE your_database_name 
TO DISK = 'C:\backups\btms_backup_2025_11_18.bak'
WITH FORMAT, COMPRESSION;
```

### 2. Kiểm tra Desktop App vẫn hoạt động
- Chạy Desktop App
- Kiểm tra các chức năng chính
- Đảm bảo không có lỗi

### 3. Database Type
Dự án của bạn đang sử dụng:
- **SQL Server** (cả production và development)

---

## 🔧 CÁCH THỰC HIỆN MIGRATIONS

### Option 1: Sử dụng H2 Console (Recommended for Development)

1. **Start H2 Console**
   ```bash
   # Nếu app đang chạy, truy cập:
   http://localhost:2345/h2-console
   ```

2. **Connect to Database**
   - JDBC URL: `jdbc:h2:file:./database/btms` (hoặc path trong config)
   - Username: `sa` (mặc định)
   - Password: (để trống hoặc theo config)

3. **Execute Migrations theo thứ tự**
   - Mở file `V1.1__enhance_tournaments.sql`
   - Copy toàn bộ nội dung
   - Paste vào H2 Console và click "Run"
   - Kiểm tra kết quả (không có lỗi)
   - Lặp lại với `V1.2`, `V1.3`

### Option 2: Sử dụng SQL Scripts trong IDE

Nếu bạn dùng **IntelliJ IDEA**, **DBeaver**, hoặc **DataGrip**:

1. Connect to database
2. Mở file migration
3. Execute script
4. Verify results

### Option 3: Command Line (Advanced)

Nếu dùng SQL Server:
```bash
# Windows PowerShell
sqlcmd -S localhost -d btms_database -i "database\migrations\V1.1__enhance_tournaments.sql"
sqlcmd -S localhost -d btms_database -i "database\migrations\V1.2__enhance_users.sql"
sqlcmd -S localhost -d btms_database -i "database\migrations\V1.3__create_tournament_gallery.sql"
```

Nếu dùng H2:
```bash
# Dùng H2 command line tool
java -cp h2*.jar org.h2.tools.RunScript -url jdbc:h2:file:./database/btms -script database/migrations/V1.1__enhance_tournaments.sql
```

---

## 📝 THỨ TỰ THỰC HIỆN

### Phase 1: Core Enhancements (BẮT BUỘC)

Thực hiện **TUẦN TỰ** theo thứ tự sau:

#### 1. V1.1 - Enhance Tournaments ✅
```sql
-- File: database/migrations/V1.1__enhance_tournaments.sql
-- Mục đích: Thêm các fields cho web platform vào bảng GIAI_DAU
-- Thời gian: ~30 giây
```

**Sau khi execute:**
- [ ] Kiểm tra không có lỗi
- [ ] Chạy query verification:
```sql
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'GIAI_DAU' 
ORDER BY ORDINAL_POSITION;
```
- [ ] Test Desktop App vẫn chạy bình thường

---

#### 2. V1.2 - Enhance Users ✅
```sql
-- File: database/migrations/V1.2__enhance_users.sql
-- Mục đích: Thêm roles, email, authentication fields cho users
-- Thời gian: ~20 giây
```

**Sau khi execute:**
- [ ] Kiểm tra không có lỗi
- [ ] Chạy query verification:
```sql
SELECT ID, HO_TEN, EMAIL, VAI_TRO, TRANG_THAI FROM NGUOI_DUNG;
```
- [ ] Verify admin user có role 'ADMIN'

---

#### 3. V1.3 - Create Tournament Gallery ✅
```sql
-- File: database/migrations/V1.3__create_tournament_gallery.sql
-- Mục đích: Tạo bảng mới cho media gallery
-- Thời gian: ~10 giây
```

**Sau khi execute:**
- [ ] Kiểm tra không có lỗi
- [ ] Chạy query verification:
```sql
SELECT * FROM TOURNAMENT_GALLERY;
```
- [ ] Verify bảng được tạo thành công

---

### Phase 2: Sample Data (OPTIONAL - For Testing)

#### 4. Insert Sample Tournaments
```sql
-- File: database/migrations/SAMPLE_DATA.sql
-- Mục đích: Thêm dữ liệu mẫu để test
-- Thời gian: ~1 phút
```

**Chỉ chạy nếu:**
- Bạn đang ở môi trường development
- Muốn có data để test web platform
- Database chưa có nhiều dữ liệu thực

**Sau khi execute:**
- [ ] Query kiểm tra:
```sql
SELECT TEN_GIAI, TRANG_THAI, NOI_BAT FROM GIAI_DAU;
```
- [ ] Verify có 5 tournaments mẫu

---

## ✅ VERIFICATION CHECKLIST

### Sau khi hoàn thành TẤT CẢ migrations:

#### 1. Database Structure Check
```sql
-- Kiểm tra GIAI_DAU có đủ columns mới
SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'GIAI_DAU';
-- Kết quả phải >= 31 columns (7 cũ + 24 mới)

-- Kiểm tra NGUOI_DUNG có đủ columns mới
SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'NGUOI_DUNG';
-- Kết quả phải >= 13 columns (3 cũ + 10 mới)

-- Kiểm tra TOURNAMENT_GALLERY tồn tại
SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'TOURNAMENT_GALLERY';
-- Kết quả phải = 1
```

#### 2. Data Integrity Check
```sql
-- Kiểm tra tất cả tournaments có status
SELECT COUNT(*) FROM GIAI_DAU WHERE TRANG_THAI IS NULL;
-- Kết quả phải = 0

-- Kiểm tra tất cả users có role
SELECT COUNT(*) FROM NGUOI_DUNG WHERE VAI_TRO IS NULL;
-- Kết quả phải = 0
```

#### 3. Index Check
```sql
-- Kiểm tra indexes được tạo
SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES 
WHERE TABLE_NAME IN ('GIAI_DAU', 'NGUOI_DUNG', 'TOURNAMENT_GALLERY');
```

#### 4. Desktop App Compatibility Test
- [ ] Khởi động Desktop App
- [ ] Mở màn hình Tournaments
- [ ] Tạo tournament mới
- [ ] Xem danh sách tournaments
- [ ] Kiểm tra scoreboard vẫn hoạt động
- [ ] **QUAN TRỌNG**: Tất cả chức năng cũ phải vẫn hoạt động bình thường!

---

## 🔄 ROLLBACK (Nếu có vấn đề)

Nếu gặp lỗi hoặc Desktop App không hoạt động:

### Option 1: Restore từ Backup
```sql
-- SQL Server
RESTORE DATABASE your_database_name 
FROM DISK = 'C:\backups\btms_backup_2025_11_17.bak'
WITH REPLACE;
```

### Option 2: Execute Rollback Script
```sql
-- File: database/migrations/ROLLBACK_V1.sql
-- WARNING: Sẽ XÓA TẤT CẢ dữ liệu trong các columns mới!
```

**Chỉ dùng Rollback nếu:**
- Desktop App bị lỗi nghiêm trọng
- Migration thất bại
- Cần quay về trạng thái ban đầu

---

## 📊 EXPECTED RESULTS

Sau khi hoàn thành migrations, bạn sẽ có:

### Database Changes:
- ✅ **GIAI_DAU**: 24 columns mới (status, featured, images, location, etc.)
- ✅ **NGUOI_DUNG**: 10 columns mới (email, roles, authentication)
- ✅ **TOURNAMENT_GALLERY**: Bảng mới cho media
- ✅ 9 indexes mới cho performance
- ✅ Sample data (nếu đã insert)

### Application Status:
- ✅ Desktop App vẫn hoạt động bình thường
- ✅ Tất cả features cũ vẫn work
- ✅ Database ready cho Web Platform development
- ✅ API development có thể bắt đầu

---

## 🚀 NEXT STEPS

Sau khi migrations thành công:

### 1. Update JPA Entities (Week 1 - Day 4-5)
```java
// Update: src/main/java/com/example/btms/model/tournament/GiaiDau.java
// Thêm @Entity, @Table annotations
// Thêm getters/setters cho các fields mới
```

### 2. Create DTOs (Week 1 - Day 5)
```java
// Create: src/main/java/com/example/btms/web/dto/TournamentDTO.java
// Create: src/main/java/com/example/btms/web/dto/TournamentDetailDTO.java
```

### 3. Update Services (Week 2)
```java
// Update: TournamentDataService
// Implement: TournamentService
// Add: Filtering, pagination, search logic
```

### 4. Create REST APIs (Week 3)
```java
// Create: TournamentApiController
// Implement: CRUD operations
// Add: Swagger documentation
```

---

## 📞 TROUBLESHOOTING

### Lỗi: "Column already exists"
**Nguyên nhân**: Migration đã được chạy trước đó
**Giải pháp**: Bỏ qua hoặc comment out các dòng ADD COLUMN đã có

### Lỗi: "Foreign key constraint fails"
**Nguyên nhân**: Dữ liệu không hợp lệ hoặc thiếu references
**Giải pháp**: Kiểm tra data integrity, fix dữ liệu trước

### Desktop App không khởi động
**Nguyên nhân**: Migration có thể đã break compatibility
**Giải pháp**: 
1. Check logs
2. Verify entities mapping
3. Rollback nếu cần

### H2 Console không connect được
**Nguyên nhân**: App đang chiếm database file
**Giải pháp**: Stop app trước khi connect to H2 Console

---

## 🎯 SUMMARY

**Thứ tự thực hiện:**
1. ✅ Backup database
2. ✅ Execute V1.1__enhance_tournaments.sql
3. ✅ Execute V1.2__enhance_users.sql
4. ✅ Execute V1.3__create_tournament_gallery.sql
5. ✅ (Optional) Execute SAMPLE_DATA.sql
6. ✅ Verify all changes
7. ✅ Test Desktop App
8. ✅ Ready for Phase 2 (JPA Entities)

**Thời gian dự kiến**: 15-30 phút

**Kết quả**: Database ready cho Web Platform development! 🎉

---

Có bất kỳ câu hỏi nào trong quá trình thực hiện, hãy hỏi tôi nhé! 💪
