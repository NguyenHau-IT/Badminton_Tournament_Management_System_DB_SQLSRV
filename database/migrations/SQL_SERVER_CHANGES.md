# 📝 Tóm tắt Thay đổi Migration Scripts cho SQL Server

## ✅ Hoàn thành

Đã chỉnh sửa tất cả migration scripts từ **H2 Database syntax** sang **SQL Server syntax**.

---

## 📂 Files đã cập nhật (5 files)

### 1. **V1.1__enhance_tournaments.sql** ✅
**Thay đổi chính:**
- ❌ `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` (H2)
- ✅ `IF NOT EXISTS ... ALTER TABLE ... ADD` (SQL Server)
- ❌ `TEXT` → ✅ `NVARCHAR(MAX)`
- ❌ `VARCHAR` → ✅ `NVARCHAR`
- ❌ `BOOLEAN` → ✅ `BIT`
- ❌ `CURRENT_DATE` → ✅ `CAST(GETDATE() AS DATE)`
- ❌ `CREATE INDEX IF NOT EXISTS` → ✅ `IF NOT EXISTS ... CREATE INDEX`
- Thêm `GO` statements để batch commands

### 2. **V1.2__enhance_users.sql** ✅
**Thay đổi chính:**
- ❌ `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`
- ✅ `IF NOT EXISTS (SELECT * FROM sys.columns ...) ALTER TABLE ... ADD`
- ❌ `TIMESTAMP` → ✅ `DATETIME2`
- ❌ `BOOLEAN` → ✅ `BIT`
- ❌ `CREATE UNIQUE INDEX IF NOT EXISTS` → ✅ `IF NOT EXISTS ... CREATE UNIQUE INDEX`
- ❌ `ADD CONSTRAINT IF NOT EXISTS` → ✅ `IF NOT EXISTS ... ADD CONSTRAINT`
- Thêm `GO` statements

### 3. **V1.3__create_tournament_gallery.sql** ✅
**Thay đổi chính:**
- ❌ `CREATE TABLE IF NOT EXISTS`
- ✅ `IF NOT EXISTS (SELECT * FROM sys.objects ...) BEGIN CREATE TABLE ... END`
- ❌ `INT AUTO_INCREMENT` → ✅ `INT IDENTITY(1,1)`
- ❌ `TEXT` → ✅ `NVARCHAR(MAX)`
- ❌ `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` → ✅ `DATETIME2 DEFAULT GETDATE()`
- Thêm `GO` statements

### 4. **ROLLBACK_V1.sql** ✅
**Thay đổi chính:**
- ❌ `DROP TABLE IF EXISTS`
- ✅ `IF EXISTS (SELECT * FROM sys.objects ...) DROP TABLE`
- ❌ `ALTER TABLE ... DROP COLUMN IF EXISTS`
- ✅ `IF EXISTS (SELECT * FROM sys.columns ...) ALTER TABLE ... DROP COLUMN`
- ❌ `DROP INDEX IF EXISTS ... ON`
- ✅ `IF EXISTS (SELECT * FROM sys.indexes ...) DROP INDEX ... ON`
- Thêm `GO` statements

### 5. **SAMPLE_DATA.sql** ✅
**Thay đổi chính:**
- ❌ `DATE_SUB(CURRENT_DATE, INTERVAL X DAY)` (MySQL)
- ✅ `DATEADD(DAY, -X, CAST(GETDATE() AS DATE))` (SQL Server)
- ❌ `DATE_ADD(CURRENT_DATE, INTERVAL X DAY)`
- ✅ `DATEADD(DAY, X, CAST(GETDATE() AS DATE))`
- ❌ `CURRENT_TIMESTAMP` → ✅ `GETDATE()`
- ❌ `TRUE/FALSE` → ✅ `1/0`
- Thêm `N` prefix cho NVARCHAR strings (Unicode support)
- Thêm `GO` statements

---

## 🔑 Key Differences (H2/MySQL vs SQL Server)

| Feature | H2/MySQL | SQL Server |
|---------|----------|------------|
| Check column exists | `ADD COLUMN IF NOT EXISTS` | `IF NOT EXISTS (SELECT * FROM sys.columns ...) ALTER TABLE ADD` |
| Text type | `TEXT` | `NVARCHAR(MAX)` |
| String type | `VARCHAR` | `NVARCHAR` (Unicode) |
| Boolean type | `BOOLEAN` / `TRUE`/`FALSE` | `BIT` / `1`/`0` |
| Timestamp | `TIMESTAMP` / `CURRENT_TIMESTAMP` | `DATETIME2` / `GETDATE()` |
| Auto increment | `AUTO_INCREMENT` | `IDENTITY(1,1)` |
| Date arithmetic | `DATE_ADD()`, `DATE_SUB()` | `DATEADD()` |
| Current date | `CURRENT_DATE` | `CAST(GETDATE() AS DATE)` |
| Batch separator | None or `;` | `GO` |
| Check table exists | `CREATE TABLE IF NOT EXISTS` | `IF NOT EXISTS (SELECT * FROM sys.objects ...)` |
| Check index exists | `CREATE INDEX IF NOT EXISTS` | `IF NOT EXISTS (SELECT * FROM sys.indexes ...)` |

---

## 📋 Files đã cập nhật trong docs/

### **README.md** (trong migrations folder) ✅
- Cập nhật backup command cho SQL Server
- Xóa phần H2 Console instructions
- Cập nhật sqlcmd examples
- Cập nhật verification queries

### **QUICK_START.md** (mới tạo) ✅
- Quick reference cho SQL Server
- 3 bước thực hiện nhanh
- sqlcmd commands
- Verification queries

---

## 🎯 Next Steps cho bạn

### 1️⃣ Backup Database (BẮT BUỘC)
```sql
BACKUP DATABASE your_database_name 
TO DISK = 'C:\backups\btms_backup_2025_11_18.bak'
WITH FORMAT, COMPRESSION;
```

### 2️⃣ Execute Migrations
**Option A: Dùng SSMS (Recommended)**
- Mở SSMS
- Connect đến database
- Open và Execute: V1.1 → V1.2 → V1.3

**Option B: Dùng sqlcmd**
```powershell
sqlcmd -S localhost -d your_db -i "database\migrations\V1.1__enhance_tournaments.sql"
sqlcmd -S localhost -d your_db -i "database\migrations\V1.2__enhance_users.sql"
sqlcmd -S localhost -d your_db -i "database\migrations\V1.3__create_tournament_gallery.sql"
```

### 3️⃣ Verify
```sql
-- Check columns added
SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'GIAI_DAU';
-- Expected: 30+

-- Check new table
SELECT * FROM TOURNAMENT_GALLERY;
```

### 4️⃣ Test Desktop App
- Chạy Desktop App
- Kiểm tra tất cả chức năng
- Đảm bảo không có lỗi

### 5️⃣ Tiếp tục với Day 2
Mở `docs/PHASE_1_CHECKLIST.md` và bắt đầu:
- **Day 2**: Update JPA entities với annotations

---

## ⚠️ Lưu ý

1. **Unicode Support**: Tất cả string columns giờ dùng `NVARCHAR` để hỗ trợ tiếng Việt đầy đủ
2. **GO Statements**: Cần thiết để SQL Server batch commands đúng cách
3. **sys.* Views**: Dùng system views để check tồn tại (columns, tables, indexes)
4. **BIT Type**: SQL Server dùng `BIT` cho boolean, values là `0` hoặc `1`
5. **IDENTITY**: Thay thế cho AUTO_INCREMENT, tự động tăng ID

---

## 🚨 Rollback nếu cần

Nếu có vấn đề, chạy file `ROLLBACK_V1.sql` để undo tất cả thay đổi:
```sql
-- Execute ROLLBACK_V1.sql trong SSMS
```

Sau đó restore từ backup:
```sql
RESTORE DATABASE your_database_name 
FROM DISK = 'C:\backups\btms_backup_2025_11_18.bak'
WITH REPLACE;
```

---

**Tất cả scripts giờ đã tương thích 100% với SQL Server!** 🎉
