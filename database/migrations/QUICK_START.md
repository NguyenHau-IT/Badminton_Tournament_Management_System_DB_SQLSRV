# ⚡ Quick Start - Database Migrations# 📁 Database Migrations Folder



## 📁 Files trong thư mục nàyThis folder contains all database migration scripts for the BTMS Web Platform.



```## 📂 Files

database/migrations/

├── V1.1__enhance_tournaments.sql    # Thêm 24 fields vào GIAI_DAU| File | Purpose | Status |

├── V1.2__enhance_users.sql          # Thêm 10 fields vào NGUOI_DUNG  |------|---------|--------|

├── V1.3__create_tournament_gallery.sql  # Tạo bảng TOURNAMENT_GALLERY| `V1.1__enhance_tournaments.sql` | Add web platform fields to GIAI_DAU table | ✅ Ready |

├── SAMPLE_DATA.sql                  # Dữ liệu mẫu (optional)| `V1.2__enhance_users.sql` | Add authentication & role fields to NGUOI_DUNG | ✅ Ready |

├── ROLLBACK_V1.sql                  # Script rollback nếu cần| `V1.3__create_tournament_gallery.sql` | Create new TOURNAMENT_GALLERY table | ✅ Ready |

├── README.md                        # Hướng dẫn chi tiết| `ROLLBACK_V1.sql` | Rollback all Phase 1 changes if needed | ✅ Ready |

└── QUICK_START.md                   # File này| `SAMPLE_DATA.sql` | Insert test tournament data (optional) | ✅ Ready |

```| `README.md` | Complete migration execution guide | ✅ Ready |



---## 🚀 Quick Start



## 🚀 3 Bước Thực Hiện (SQL Server)### 1. Backup First!

```bash

### 1️⃣ Backup Database# Copy your database file

```sqlcp ../../database/btms.mv.db ../../backups/btms_backup.mv.db

-- Chạy trong SSMS```

BACKUP DATABASE your_database_name 

TO DISK = 'C:\backups\btms_backup_2025_11_18.bak'### 2. Execute Migrations (In Order!)

WITH FORMAT, COMPRESSION;```sql

```-- Run in H2 Console or SQL client

-- Order is important!

### 2️⃣ Execute Migrations (Tuần tự từ V1.1 → V1.2 → V1.3)

1. V1.1__enhance_tournaments.sql

**Trong SSMS:**2. V1.2__enhance_users.sql

- Mở file `V1.1__enhance_tournaments.sql`3. V1.3__create_tournament_gallery.sql

- Click Execute (F5)```

- Kiểm tra "Commands completed successfully"

- Lặp lại với V1.2, V1.3### 3. Optional: Add Test Data

```sql

**Hoặc dùng sqlcmd:**-- Only for development/testing

```powershellSAMPLE_DATA.sql

sqlcmd -S localhost -d your_database_name -i "database\migrations\V1.1__enhance_tournaments.sql"```

sqlcmd -S localhost -d your_database_name -i "database\migrations\V1.2__enhance_users.sql"

sqlcmd -S localhost -d your_database_name -i "database\migrations\V1.3__create_tournament_gallery.sql"## 📖 Full Documentation

```

For complete instructions, see: `README.md` in this folder

### 3️⃣ Verify Changes

## ⚠️ Important Notes

```sql

-- Kiểm tra GIAI_DAU có đủ columns mới- Always backup before migrating

SELECT COUNT(*) as TOTAL_COLUMNS - Run migrations in order (V1.1 → V1.2 → V1.3)

FROM INFORMATION_SCHEMA.COLUMNS - Verify each step before proceeding

WHERE TABLE_NAME = 'GIAI_DAU';- Test Desktop App after migrations

-- Expected: 30+ columns (7 cũ + 23 mới)- Rollback script available if needed



-- Kiểm tra NGUOI_DUNG## 🔗 Related Documentation

SELECT COUNT(*) as TOTAL_COLUMNS 

FROM INFORMATION_SCHEMA.COLUMNS - Full migration guide: `README.md`

WHERE TABLE_NAME = 'NGUOI_DUNG';- Database enhancement plan: `../../docs/DATABASE_ENHANCEMENT_PLAN.md`

-- Expected: 14+ columns (4 cũ + 10 mới)- Getting started: `../../docs/GETTING_STARTED.md`

- Phase 1 checklist: `../../docs/PHASE_1_CHECKLIST.md`

-- Kiểm tra TOURNAMENT_GALLERY tồn tại
SELECT * FROM TOURNAMENT_GALLERY;
-- Expected: Empty table hoặc có data nếu đã chạy SAMPLE_DATA.sql
```

---

## ⚠️ Nếu có lỗi

1. **Đọc error message** - Thường nó rất cụ thể
2. **Kiểm tra database connection** - SSMS có kết nối đúng DB không?
3. **Xem README.md** → Phần "Troubleshooting"
4. **Rollback nếu cần:**
   ```sql
   -- Chạy file ROLLBACK_V1.sql để undo tất cả
   ```

---

## 📚 Next Steps

Sau khi migrations thành công:
1. ✅ Test Desktop App vẫn hoạt động bình thường
2. ✅ Đọc `docs/PHASE_1_CHECKLIST.md` → Day 2: Update JPA Entities
3. ✅ Bắt đầu code: Thêm annotations vào `GiaiDau.java`

---

## 🔗 Links

- [README.md](./README.md) - Hướng dẫn chi tiết
- [docs/DATABASE_ENHANCEMENT_PLAN.md](../../docs/DATABASE_ENHANCEMENT_PLAN.md) - Kế hoạch database
- [docs/PHASE_1_CHECKLIST.md](../../docs/PHASE_1_CHECKLIST.md) - Checklist 2 tuần
