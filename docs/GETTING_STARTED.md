# 🎯 GETTING STARTED - Phase 1: Database & Backend Foundation

> **Bạn đang ở đây**: Bắt đầu Phase 1 - Database Migrations  
> **Timeline**: Week 1-2 (Nov 17 - Dec 1, 2025)  
> **Goal**: Complete database foundation và backend services cho Tournament Hub

---

## 📚 TÀI LIỆU BẠN CẦN

Trước khi bắt đầu, hãy đọc qua các tài liệu sau (theo thứ tự):

### 1. **Lộ trình tổng thể** (Đã đọc ✅)
- `docs/LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md`
- Tổng quan 7 phases, timeline, priorities

### 2. **Kế hoạch Database** (Quan trọng 🔴)
- `docs/DATABASE_ENHANCEMENT_PLAN.md`
- Chi tiết về schema changes, new tables, migration strategy

### 3. **Migration Guide** (Đọc ngay ⚡)
- `database/migrations/README.md`
- Hướng dẫn thực thi migration scripts an toàn

### 4. **Phase 1 Checklist** (Follow daily 📅)
- `docs/PHASE_1_CHECKLIST.md`
- Checklist chi tiết cho từng ngày trong 2 tuần

---

## 🚀 QUICK START - 5 BƯỚC ĐỂ BẮT ĐẦU

### Bước 1: Backup Database (5 phút) ⚠️
```powershell
# Windows PowerShell
# Nếu dùng H2, copy database file:
Copy-Item ".\database\btms.mv.db" ".\backups\btms_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').mv.db"

# Hoặc export từ H2 Console:
# 1. Mở http://localhost:2345/h2-console
# 2. Tools → Backup → Chọn vị trí lưu
```

**☑️ Checkpoint**: Có file backup tại folder `backups/`

---

### Bước 2: Execute Migrations (10 phút) 🗄️

#### Option A: H2 Console (Recommended)
```
1. Khởi động app (nếu chưa chạy)
2. Truy cập: http://localhost:2345/h2-console
3. JDBC URL: jdbc:h2:file:./database/btms
4. Connect
5. Copy nội dung file V1.1__enhance_tournaments.sql
6. Paste vào SQL query box
7. Click "Run" (hoặc Ctrl+Enter)
8. Verify: "Update count: X" - không có lỗi
9. Repeat với V1.2 và V1.3
```

#### Option B: Application Properties (Automatic)
Nếu muốn tự động chạy migrations khi app khởi động:
```properties
# Thêm vào application.properties:
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:database/migrations/V1.1__enhance_tournaments.sql,classpath:database/migrations/V1.2__enhance_users.sql,classpath:database/migrations/V1.3__create_tournament_gallery.sql
```

**☑️ Checkpoint**: Chạy verification queries không có lỗi

---

### Bước 3: Verify Database (5 phút) ✅

Chạy các queries sau để kiểm tra:

```sql
-- 1. Kiểm tra số columns của GIAI_DAU
SELECT COUNT(*) as column_count 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'GIAI_DAU';
-- Expected: >= 31 columns

-- 2. Kiểm tra TOURNAMENT_GALLERY tồn tại
SELECT COUNT(*) FROM TOURNAMENT_GALLERY;
-- Expected: 0 rows (table exists but empty)

-- 3. Kiểm tra admin user có role
SELECT HO_TEN, VAI_TRO FROM NGUOI_DUNG;
-- Expected: adminn có VAI_TRO = 'ADMIN'

-- 4. Kiểm tra indexes
SELECT INDEX_NAME, TABLE_NAME 
FROM INFORMATION_SCHEMA.INDEXES 
WHERE TABLE_NAME IN ('GIAI_DAU', 'NGUOI_DUNG', 'TOURNAMENT_GALLERY');
-- Expected: Nhiều indexes mới
```

**☑️ Checkpoint**: Tất cả queries trả về kết quả như mong đợi

---

### Bước 4: Test Desktop App (5 phút) 🖥️

**QUAN TRỌNG**: Desktop App phải vẫn hoạt động bình thường!

```
1. Restart Desktop App
2. Test các chức năng:
   ☑️ Login
   ☑️ Xem danh sách tournaments
   ☑️ Tạo tournament mới
   ☑️ Scoreboard hoạt động
   ☑️ Không có error logs
```

**☑️ Checkpoint**: App chạy bình thường, không có breaking changes

---

### Bước 5: Insert Sample Data (Optional - 5 phút) 📊

Nếu muốn có dữ liệu mẫu để test web platform:

```sql
-- Execute file: database/migrations/SAMPLE_DATA.sql
-- Sẽ insert 5 tournaments với đầy đủ thông tin
-- Và gallery items cho các tournaments
```

**☑️ Checkpoint**: Query `SELECT * FROM GIAI_DAU` trả về 5+ tournaments

---

## 🎯 YOU ARE HERE - Day 1 Complete!

Sau khi hoàn thành 5 bước trên, bạn đã:

- ✅ Database được backup an toàn
- ✅ 3 migration scripts executed thành công
- ✅ Database có đầy đủ fields cho web platform
- ✅ Desktop App vẫn hoạt động bình thường
- ✅ (Optional) Có sample data để test

**🎉 Congratulations! Day 1 hoàn thành!**

---

## 📅 NEXT STEPS - Day 2 và sau đó

### Tomorrow (Day 2 - Nov 18):
**Focus**: Update JPA Entities

**Tasks**:
1. Open `src/main/java/com/example/btms/model/tournament/GiaiDau.java`
2. Add Jakarta Persistence annotations
3. Add new fields with proper types
4. Add getters/setters
5. Update `NguoiDung.java` similarly
6. Create new `TournamentGallery.java` entity

**Time estimate**: 3-4 hours

**Resources**:
- `docs/DATABASE_ENHANCEMENT_PLAN.md` → Section "JPA Entities Update"
- `docs/PHASE_1_CHECKLIST.md` → Day 2 checklist

---

### This Week (Days 3-5):
- Day 3: Create Enums & DTOs
- Day 4: Update Repository layer
- Day 5: Update Service layer

### Next Week (Days 6-12):
- REST API development
- Frontend integration
- Testing & polish

**Follow along**: `docs/PHASE_1_CHECKLIST.md` cho detailed daily tasks

---

## 📁 PROJECT STRUCTURE OVERVIEW

Sau khi setup, cấu trúc project sẽ như sau:

```
BTMS/
├── database/
│   ├── script.sql              (Original schema)
│   └── migrations/             (NEW)
│       ├── V1.1__enhance_tournaments.sql ✅
│       ├── V1.2__enhance_users.sql ✅
│       ├── V1.3__create_tournament_gallery.sql ✅
│       ├── ROLLBACK_V1.sql
│       ├── SAMPLE_DATA.sql
│       └── README.md
│
├── docs/
│   ├── LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md ✅
│   ├── DATABASE_ENHANCEMENT_PLAN.md ✅
│   ├── PHASE_1_CHECKLIST.md ✅
│   └── GETTING_STARTED.md (This file)
│
├── src/main/java/com/example/btms/
│   ├── model/
│   │   ├── tournament/
│   │   │   └── GiaiDau.java (TO UPDATE - Day 2)
│   │   ├── auth/
│   │   │   └── NguoiDung.java (TO UPDATE - Day 2)
│   │   └── (TournamentGallery.java - TO CREATE - Day 2)
│   │
│   ├── repository/
│   │   └── (GiaiDauRepository.java - TO CREATE - Day 4)
│   │
│   ├── service/
│   │   ├── tournamentWebData/
│   │   │   └── TournamentDataService.java (TO UPDATE - Day 5)
│   │   └── tournament/
│   │       └── GiaiDauService.java (Existing)
│   │
│   ├── web/
│   │   ├── dto/ (TO CREATE - Day 3)
│   │   ├── mapper/ (TO CREATE - Day 3)
│   │   ├── controller/
│   │   │   ├── tournament/
│   │   │   │   └── TournamentController.java (TO UPDATE - Day 8)
│   │   │   └── api/
│   │   │       └── TournamentApiController.java (TO CREATE - Day 6)
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java (TO CREATE - Day 6)
│   │
│   └── config/
│       └── (SwaggerConfig.java - TO CREATE - Day 6)
│
├── src/main/resources/
│   ├── templates/tournament/
│   │   ├── tournament-home.html (TO UPDATE - Day 8)
│   │   ├── tournament-list.html (TO UPDATE - Day 8)
│   │   ├── tournament-detail.html (TO UPDATE - Day 8)
│   │   └── tournament-calendar.html (TO UPDATE - Day 8)
│   │
│   └── static/
│       ├── js/tournament/
│       │   ├── tournament-home.js (TO CREATE - Day 9)
│       │   ├── tournament-list.js (TO CREATE - Day 9)
│       │   └── tournament-calendar.js (TO CREATE - Day 9)
│       │
│       └── css/tournament/
│           ├── tournament-home.css (TO COMPLETE - Day 10)
│           ├── tournament-list.css (TO COMPLETE - Day 10)
│           └── tournament-detail.css (TO COMPLETE - Day 10)
│
└── src/test/java/com/example/btms/
    ├── repository/
    │   └── GiaiDauRepositoryTest.java (TO CREATE - Weekend 1)
    ├── service/
    │   └── TournamentDataServiceTest.java (TO CREATE - Weekend 1)
    └── web/controller/
        └── TournamentApiControllerTest.java (TO CREATE - Day 7)
```

---

## 🔧 DEVELOPMENT ENVIRONMENT SETUP

### Required Tools
- ✅ JDK 21 (đã có)
- ✅ Maven (đã có)
- ✅ Spring Boot 3.4.0 (đã có)
- ✅ H2 Database hoặc SQL Server (đã có)
- ✅ IDE: IntelliJ IDEA / VS Code (đã có)

### Recommended Extensions/Plugins
- **IntelliJ IDEA**:
  - JPA Buddy (database & entity management)
  - Spring Boot Tools
  - Database Navigator
  
- **VS Code**:
  - Spring Boot Extension Pack
  - Java Extension Pack
  - Database Client

### Browser Tools
- Chrome DevTools
- Postman (for API testing)
- Thunder Client (VS Code extension alternative)

---

## 🐛 TROUBLESHOOTING

### Issue 1: "Column already exists"
**Cause**: Migration was run before  
**Solution**: Comment out the specific ALTER TABLE statement or use `IF NOT EXISTS`

### Issue 2: Desktop App won't start after migration
**Cause**: Entity mappings may be incorrect  
**Solution**: 
1. Check entity annotations
2. Verify column names match exactly
3. Check application logs
4. Rollback if necessary

### Issue 3: H2 Console connection refused
**Cause**: App is not running or wrong URL  
**Solution**: 
1. Verify app is running: `http://localhost:2345`
2. Check application.properties for H2 configuration
3. Try: `jdbc:h2:file:./database/btms;AUTO_SERVER=TRUE`

### Issue 4: Migration fails with FK constraint error
**Cause**: Data integrity issues  
**Solution**: Check if all referenced records exist in parent tables

**More help**: Check `database/migrations/README.md` → Troubleshooting section

---

## 📊 PROGRESS TRACKING

Update daily as you complete tasks:

```
✅ Day 1 (Nov 17): Database Migrations
⏳ Day 2 (Nov 18): JPA Entities Update
⏳ Day 3 (Nov 19): Enums & DTOs
⏳ Day 4 (Nov 20): Repository Layer
⏳ Day 5 (Nov 21): Service Layer
⏳ Weekend 1: Testing
⏳ Day 6 (Nov 24): REST API
⏳ Day 7 (Nov 25): API Testing
⏳ Day 8-9 (Nov 26-27): Frontend Integration
⏳ Day 10 (Nov 28): Styling
⏳ Day 11-12 (Nov 29-30): E2E Testing
🎯 Dec 1: Phase 1 Complete!
```

**Current Progress**: 8% (Day 1 of 12 completed)

---

## 💬 NEED HELP?

Nếu bạn gặp vấn đề hoặc có câu hỏi:

1. **Check Documentation First**:
   - `database/migrations/README.md` - Database help
   - `docs/DATABASE_ENHANCEMENT_PLAN.md` - Schema details
   - `docs/PHASE_1_CHECKLIST.md` - Task details

2. **Check Logs**:
   - Application console output
   - Database error messages
   - Browser console (for frontend issues)

3. **Ask for Help**:
   - Describe what you were doing
   - Share error messages
   - Share relevant code snippets

---

## 🎉 YOU'RE READY!

Bạn đã có:
- ✅ Lộ trình rõ ràng (7 phases)
- ✅ Database plan chi tiết
- ✅ Migration scripts ready
- ✅ Detailed checklist cho 2 tuần
- ✅ Troubleshooting guide

**Next Action**: Execute the 5 bước Quick Start ở trên! 🚀

---

**Remember**: 
- 📸 Backup trước khi migrate
- ✅ Verify sau mỗi bước
- 🖥️ Test Desktop App thường xuyên
- 📝 Update checklist hàng ngày
- 💪 Một bước một, đừng vội!

**Let's build an amazing web platform together! 🏸💻**
