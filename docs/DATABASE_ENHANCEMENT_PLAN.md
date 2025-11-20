# 🗄️ DATABASE ENHANCEMENT PLAN - BTMS Web Platform

> **Mục tiêu**: Mở rộng database hiện tại để hỗ trợ Web Platform mà KHÔNG phá vỡ Desktop App

**Ngày tạo**: 17/11/2025  
**Chiến lược**: Incremental Enhancement (Thêm dần, không thay đổi cấu trúc cũ)

---

## 📋 MỤC LỤC

1. [Phân tích Database hiện tại](#phân-tích-database-hiện-tại)
2. [Tables cần mở rộng](#tables-cần-mở-rộng)
3. [Tables mới cần tạo](#tables-mới-cần-tạo)
4. [Migration Scripts](#migration-scripts)
5. [JPA Entities Update](#jpa-entities-update)
6. [API Design](#api-design)
7. [Implementation Roadmap](#implementation-roadmap)

---

## 🔍 PHÂN TÍCH DATABASE HIỆN TẠI

### ✅ Tables đã có (Desktop App)

```
NGUOI_DUNG              → User accounts
CAU_LAC_BO              → Clubs
NOI_DUNG                → Tournament categories (singles/doubles, age groups)
GIAI_DAU                → Tournaments
VAN_DONG_VIEN           → Players/Athletes
CHI_TIET_TRAN_DAU       → Match details
DANG_KI_CA_NHAN         → Individual registrations
DANG_KI_DOI             → Team registrations
CHI_TIET_DOI            → Team members
BOC_THAM_CA_NHAN        → Individual draws
BOC_THAM_DOI            → Team draws
CHI_TIET_GIAI_DAU       → Tournament-Category mapping
CHI_TIET_VAN            → Set/game details
SO_DO_CA_NHAN           → Individual bracket positions
SO_DO_DOI               → Team bracket positions
KET_QUA_CA_NHAN         → Individual results
KET_QUA_DOI             → Team results
```

### ✅ Điểm mạnh của schema hiện tại

1. **Cấu trúc tốt**: Normalized, có relationships rõ ràng
2. **Đầy đủ core data**: Tournaments, players, clubs, matches
3. **Hỗ trợ cả singles & doubles**: Thông qua NOI_DUNG
4. **Có bracket system**: SO_DO_CA_NHAN, SO_DO_DOI
5. **Track match details**: CHI_TIET_VAN (sets, scores)

### ⚠️ Điểm cần bổ sung cho Web Platform

1. **Tournament metadata thiếu**:
   - Không có: status, featured, images, description, location details
   - Không có: registration deadline, capacity, prize
   
2. **Player profiles thiếu**:
   - Không có: photos, bio, rankings, statistics
   
3. **Club profiles thiếu**:
   - Không có: logo, description, contact info
   
4. **Match metadata thiếu**:
   - Không có: court name, referee, video links
   
5. **Web-specific features thiếu**:
   - News/Articles
   - Comments/Ratings
   - Notifications
   - Media gallery
   - User roles (beyond basic NGUOI_DUNG)

---

## 🔧 TABLES CẦN MỞ RỘNG

### 1️⃣ GIAI_DAU (Tournaments) - **PRIORITY HIGH**

#### Columns mới cần thêm:

```sql
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS MO_TA TEXT;                          -- Description
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS DIA_DIEM VARCHAR(500);               -- Location
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS TINH_THANH VARCHAR(100);             -- City/Province
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS QUOC_GIA VARCHAR(50) DEFAULT 'VN';  -- Country
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'upcoming'; -- Status
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS NOI_BAT BOOLEAN DEFAULT FALSE;      -- Featured
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS HINH_ANH VARCHAR(500);               -- Cover image URL
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS LOGO VARCHAR(500);                   -- Logo URL
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS NGAY_MO_DANG_KI DATE;                -- Registration open date
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS NGAY_DONG_DANG_KI DATE;              -- Registration deadline
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS SO_LUONG_TOI_DA INT;                 -- Max participants
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS PHI_THAM_GIA DECIMAL(10,2);         -- Entry fee
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS GIAI_THUONG TEXT;                    -- Prize description
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);              -- Contact phone
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);                  -- Contact email
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS WEBSITE VARCHAR(200);                -- Tournament website
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS CAP_DO VARCHAR(50);                  -- Level: professional, amateur, youth
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS THE_LOAI VARCHAR(50);                -- Type: open, invitational, league
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS SAN_THI_DAU TEXT;                    -- Venue details (JSON or text)
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS QUY_DINH TEXT;                       -- Tournament rules/regulations
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS LUOT_XEM INT DEFAULT 0;              -- View count
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS DANH_GIA_TB DECIMAL(3,2);            -- Average rating
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS TONG_DANH_GIA INT DEFAULT 0;         -- Total ratings
```

#### Giá trị TRANG_THAI (Status):
```
- 'draft'         → Nháp (chưa public)
- 'upcoming'      → Sắp diễn ra
- 'registration'  → Đang mở đăng ký
- 'ongoing'       → Đang diễn ra
- 'completed'     → Đã kết thúc
- 'cancelled'     → Đã hủy
```

---

### 2️⃣ VAN_DONG_VIEN (Players) - **PRIORITY MEDIUM**

```sql
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS ANH_DAI_DIEN VARCHAR(500);      -- Profile photo URL
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TIEU_SU TEXT;                   -- Biography
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS CHIEU_CAO INT;                  -- Height (cm)
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS CAN_NANG DECIMAL(5,2);          -- Weight (kg)
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS XEP_HANG_QUOC_GIA INT;          -- National ranking
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS XEP_HANG_THE_GIOI INT;          -- World ranking
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TONG_TRAN_THANG INT DEFAULT 0;  -- Total wins
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TONG_TRAN_THUA INT DEFAULT 0;   -- Total losses
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TI_LE_THANG DECIMAL(5,2);       -- Win rate %
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);         -- Phone
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);             -- Email
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active'; -- active/inactive/retired
```

---

### 3️⃣ CAU_LAC_BO (Clubs) - **PRIORITY MEDIUM**

```sql
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS LOGO VARCHAR(500);                 -- Club logo URL
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS MO_TA TEXT;                        -- Description
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS DIA_CHI VARCHAR(500);              -- Address
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS TINH_THANH VARCHAR(100);           -- City/Province
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);            -- Phone
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);                -- Email
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS WEBSITE VARCHAR(200);              -- Website
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS NGAY_THANH_LAP DATE;               -- Founded date
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS SO_THANH_VIEN INT DEFAULT 0;       -- Member count
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS XEP_HANG INT;                      -- Club ranking
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active';
```

---

### 4️⃣ CHI_TIET_TRAN_DAU (Matches) - **PRIORITY MEDIUM**

```sql
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS TEN_SAN VARCHAR(100);       -- Court name (e.g., "Court 1")
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS TRONG_TAI VARCHAR(200);     -- Referee name
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'scheduled'; -- scheduled/live/completed/cancelled
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS VONG_DAU VARCHAR(50);       -- Round: R32, R16, QF, SF, F
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS LUOT_XEM INT DEFAULT 0;     -- View count
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS VIDEO_URL VARCHAR(500);     -- Video link
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS LIVE_STREAM_URL VARCHAR(500); -- Live stream link
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS GHI_CHU TEXT;               -- Notes
```

---

### 5️⃣ NGUOI_DUNG (Users) - **PRIORITY HIGH**

```sql
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100) UNIQUE;
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS VAI_TRO VARCHAR(20) DEFAULT 'CLIENT'; -- ADMIN/ORGANIZER/PLAYER/CLIENT
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active'; -- active/suspended/deleted
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS LAN_DANG_NHAP_CUOI TIMESTAMP;
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS ANH_DAI_DIEN VARCHAR(500);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS XAC_THUC_EMAIL BOOLEAN DEFAULT FALSE;
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS MA_XAC_THUC VARCHAR(100);          -- Email verification token
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS MA_DAT_LAI_MK VARCHAR(100);        -- Password reset token
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS NGAY_HET_HAN_TOKEN TIMESTAMP;
```

---

## 🆕 TABLES MỚI CẦN TẠO

### 1️⃣ TOURNAMENT_GALLERY (Media cho giải đấu) - **PRIORITY MEDIUM**

```sql
CREATE TABLE TOURNAMENT_GALLERY (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_GIAI INT NOT NULL,
    LOAI VARCHAR(20) NOT NULL,                    -- 'image', 'video', 'document'
    URL VARCHAR(500) NOT NULL,
    TIEU_DE VARCHAR(200),
    MO_TA TEXT,
    THU_TU INT DEFAULT 0,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_GIAI) REFERENCES GIAI_DAU(ID) ON DELETE CASCADE
);

CREATE INDEX idx_tournament_gallery_giai ON TOURNAMENT_GALLERY(ID_GIAI);
```

---

### 2️⃣ NEWS_ARTICLES (Tin tức) - **PRIORITY LOW**

```sql
CREATE TABLE NEWS_ARTICLES (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TIEU_DE VARCHAR(500) NOT NULL,
    NOI_DUNG TEXT NOT NULL,
    TOM_TAT VARCHAR(1000),
    ANH_DAI_DIEN VARCHAR(500),
    ID_TAC_GIA INT NOT NULL,                      -- FK to NGUOI_DUNG
    DANH_MUC VARCHAR(50),                         -- 'tournament', 'player', 'general'
    ID_GIAI_DAU INT,                              -- Optional FK to GIAI_DAU
    TRANG_THAI VARCHAR(20) DEFAULT 'draft',       -- draft/published/archived
    LUOT_XEM INT DEFAULT 0,
    NOI_BAT BOOLEAN DEFAULT FALSE,
    NGAY_XUAT_BAN TIMESTAMP,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_TAC_GIA) REFERENCES NGUOI_DUNG(ID),
    FOREIGN KEY (ID_GIAI_DAU) REFERENCES GIAI_DAU(ID) ON DELETE SET NULL
);

CREATE INDEX idx_news_status ON NEWS_ARTICLES(TRANG_THAI);
CREATE INDEX idx_news_category ON NEWS_ARTICLES(DANH_MUC);
CREATE INDEX idx_news_featured ON NEWS_ARTICLES(NOI_BAT);
```

---

### 3️⃣ TOURNAMENT_RATINGS (Đánh giá giải đấu) - **PRIORITY LOW**

```sql
CREATE TABLE TOURNAMENT_RATINGS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_GIAI INT NOT NULL,
    ID_USER INT NOT NULL,
    DIEM_SO INT NOT NULL CHECK (DIEM_SO BETWEEN 1 AND 5),
    BINH_LUAN TEXT,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_GIAI) REFERENCES GIAI_DAU(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_USER) REFERENCES NGUOI_DUNG(ID) ON DELETE CASCADE,
    UNIQUE KEY unique_user_tournament_rating (ID_GIAI, ID_USER)
);

CREATE INDEX idx_rating_tournament ON TOURNAMENT_RATINGS(ID_GIAI);
```

---

### 4️⃣ NOTIFICATIONS (Thông báo) - **PRIORITY LOW**

```sql
CREATE TABLE NOTIFICATIONS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_USER INT NOT NULL,
    LOAI VARCHAR(50) NOT NULL,                    -- 'tournament_update', 'match_start', 'registration_confirmed'
    TIEU_DE VARCHAR(200) NOT NULL,
    NOI_DUNG TEXT NOT NULL,
    LIEN_KET VARCHAR(500),                        -- URL to related content
    DA_DOC BOOLEAN DEFAULT FALSE,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_USER) REFERENCES NGUOI_DUNG(ID) ON DELETE CASCADE
);

CREATE INDEX idx_notification_user ON NOTIFICATIONS(ID_USER);
CREATE INDEX idx_notification_unread ON NOTIFICATIONS(DA_DOC);
```

---

### 5️⃣ MATCH_COMMENTS (Bình luận trận đấu) - **PRIORITY LOW**

```sql
CREATE TABLE MATCH_COMMENTS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_TRAN_DAU CHAR(36) NOT NULL,
    ID_USER INT NOT NULL,
    NOI_DUNG TEXT NOT NULL,
    ID_CHA INT,                                   -- For nested comments (replies)
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_TRAN_DAU) REFERENCES CHI_TIET_TRAN_DAU(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_USER) REFERENCES NGUOI_DUNG(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_CHA) REFERENCES MATCH_COMMENTS(ID) ON DELETE CASCADE
);

CREATE INDEX idx_comment_match ON MATCH_COMMENTS(ID_TRAN_DAU);
CREATE INDEX idx_comment_parent ON MATCH_COMMENTS(ID_CHA);
```

---

### 6️⃣ PLAYER_STATISTICS (Thống kê VĐV theo giải) - **PRIORITY LOW**

```sql
CREATE TABLE PLAYER_STATISTICS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_VDV INT NOT NULL,
    ID_GIAI INT NOT NULL,
    SO_TRAN_DAU INT DEFAULT 0,
    SO_TRAN_THANG INT DEFAULT 0,
    SO_TRAN_THUA INT DEFAULT 0,
    TONG_DIEM_GIANH INT DEFAULT 0,
    TONG_DIEM_BI_GIANH INT DEFAULT 0,
    SO_VAN_THANG INT DEFAULT 0,
    SO_VAN_THUA INT DEFAULT 0,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_VDV) REFERENCES VAN_DONG_VIEN(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_GIAI) REFERENCES GIAI_DAU(ID) ON DELETE CASCADE,
    UNIQUE KEY unique_player_tournament_stats (ID_VDV, ID_GIAI)
);

CREATE INDEX idx_player_stats_tournament ON PLAYER_STATISTICS(ID_GIAI);
CREATE INDEX idx_player_stats_player ON PLAYER_STATISTICS(ID_VDV);
```

---

### 7️⃣ TAGS (Tags cho tournaments, articles) - **PRIORITY LOW**

```sql
CREATE TABLE TAGS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TEN_TAG VARCHAR(100) NOT NULL UNIQUE,
    SLUG VARCHAR(100) NOT NULL UNIQUE,
    MO_TA VARCHAR(500),
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TOURNAMENT_TAGS (
    ID_GIAI INT NOT NULL,
    ID_TAG INT NOT NULL,
    PRIMARY KEY (ID_GIAI, ID_TAG),
    FOREIGN KEY (ID_GIAI) REFERENCES GIAI_DAU(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_TAG) REFERENCES TAGS(ID) ON DELETE CASCADE
);

CREATE TABLE ARTICLE_TAGS (
    ID_BAI_VIET INT NOT NULL,
    ID_TAG INT NOT NULL,
    PRIMARY KEY (ID_BAI_VIET, ID_TAG),
    FOREIGN KEY (ID_BAI_VIET) REFERENCES NEWS_ARTICLES(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_TAG) REFERENCES TAGS(ID) ON DELETE CASCADE
);
```

---

## 📝 MIGRATION SCRIPTS

### Phase 1A: Tournament Enhancements (EXECUTE FIRST)

```sql
-- File: V1.1__enhance_tournaments.sql

-- Add columns to GIAI_DAU
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS MO_TA TEXT;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS DIA_DIEM VARCHAR(500);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS TINH_THANH VARCHAR(100);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS QUOC_GIA VARCHAR(50) DEFAULT 'VN';
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'upcoming';
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS NOI_BAT BOOLEAN DEFAULT FALSE;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS HINH_ANH VARCHAR(500);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS LOGO VARCHAR(500);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS NGAY_MO_DANG_KI DATE;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS NGAY_DONG_DANG_KI DATE;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS SO_LUONG_TOI_DA INT;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS PHI_THAM_GIA DECIMAL(10,2);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS GIAI_THUONG TEXT;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS WEBSITE VARCHAR(200);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS CAP_DO VARCHAR(50);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS THE_LOAI VARCHAR(50);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS SAN_THI_DAU TEXT;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS QUY_DINH TEXT;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS LUOT_XEM INT DEFAULT 0;
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS DANH_GIA_TB DECIMAL(3,2);
ALTER TABLE GIAI_DAU ADD COLUMN IF NOT EXISTS TONG_DANH_GIA INT DEFAULT 0;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_giai_dau_trang_thai ON GIAI_DAU(TRANG_THAI);
CREATE INDEX IF NOT EXISTS idx_giai_dau_noi_bat ON GIAI_DAU(NOI_BAT);
CREATE INDEX IF NOT EXISTS idx_giai_dau_ngay_bd ON GIAI_DAU(NGAY_BD);
CREATE INDEX IF NOT EXISTS idx_giai_dau_ngay_kt ON GIAI_DAU(NGAY_KT);
CREATE INDEX IF NOT EXISTS idx_giai_dau_tinh_thanh ON GIAI_DAU(TINH_THANH);

-- Update existing tournaments with default status based on dates
UPDATE GIAI_DAU 
SET TRANG_THAI = CASE 
    WHEN NGAY_BD > CURRENT_DATE THEN 'upcoming'
    WHEN NGAY_KT < CURRENT_DATE THEN 'completed'
    ELSE 'ongoing'
END
WHERE TRANG_THAI IS NULL OR TRANG_THAI = 'upcoming';

COMMIT;
```

### Phase 1B: User Enhancements

```sql
-- File: V1.2__enhance_users.sql

ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS VAI_TRO VARCHAR(20) DEFAULT 'CLIENT';
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active';
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS LAN_DANG_NHAP_CUOI TIMESTAMP;
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS ANH_DAI_DIEN VARCHAR(500);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS XAC_THUC_EMAIL BOOLEAN DEFAULT FALSE;
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS MA_XAC_THUC VARCHAR(100);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS MA_DAT_LAI_MK VARCHAR(100);
ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS NGAY_HET_HAN_TOKEN TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS idx_nguoi_dung_email ON NGUOI_DUNG(EMAIL);
CREATE INDEX IF NOT EXISTS idx_nguoi_dung_vai_tro ON NGUOI_DUNG(VAI_TRO);

-- Update existing admin user
UPDATE NGUOI_DUNG SET VAI_TRO = 'ADMIN' WHERE HO_TEN = 'adminn';

COMMIT;
```

### Phase 1C: Tournament Gallery

```sql
-- File: V1.3__create_tournament_gallery.sql

CREATE TABLE IF NOT EXISTS TOURNAMENT_GALLERY (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_GIAI INT NOT NULL,
    LOAI VARCHAR(20) NOT NULL,
    URL VARCHAR(500) NOT NULL,
    TIEU_DE VARCHAR(200),
    MO_TA TEXT,
    THU_TU INT DEFAULT 0,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_GIAI) REFERENCES GIAI_DAU(ID) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tournament_gallery_giai ON TOURNAMENT_GALLERY(ID_GIAI);

COMMIT;
```

### Phase 2A: Players Enhancement

```sql
-- File: V2.1__enhance_players.sql

ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS ANH_DAI_DIEN VARCHAR(500);
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TIEU_SU TEXT;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS CHIEU_CAO INT;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS CAN_NANG DECIMAL(5,2);
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS XEP_HANG_QUOC_GIA INT;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS XEP_HANG_THE_GIOI INT;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TONG_TRAN_THANG INT DEFAULT 0;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TONG_TRAN_THUA INT DEFAULT 0;
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TI_LE_THANG DECIMAL(5,2);
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);
ALTER TABLE VAN_DONG_VIEN ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active';

CREATE INDEX IF NOT EXISTS idx_van_dong_vien_gioi_tinh ON VAN_DONG_VIEN(GIOI_TINH);
CREATE INDEX IF NOT EXISTS idx_van_dong_vien_clb ON VAN_DONG_VIEN(ID_CLB);

COMMIT;
```

### Phase 2B: Clubs Enhancement

```sql
-- File: V2.2__enhance_clubs.sql

ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS LOGO VARCHAR(500);
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS MO_TA TEXT;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS DIA_CHI VARCHAR(500);
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS TINH_THANH VARCHAR(100);
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS WEBSITE VARCHAR(200);
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS NGAY_THANH_LAP DATE;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS SO_THANH_VIEN INT DEFAULT 0;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS XEP_HANG INT;
ALTER TABLE CAU_LAC_BO ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active';

COMMIT;
```

### Phase 2C: Matches Enhancement

```sql
-- File: V2.3__enhance_matches.sql

ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS TEN_SAN VARCHAR(100);
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS TRONG_TAI VARCHAR(200);
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'scheduled';
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS VONG_DAU VARCHAR(50);
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS LUOT_XEM INT DEFAULT 0;
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS VIDEO_URL VARCHAR(500);
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS LIVE_STREAM_URL VARCHAR(500);
ALTER TABLE CHI_TIET_TRAN_DAU ADD COLUMN IF NOT EXISTS GHI_CHU TEXT;

CREATE INDEX IF NOT EXISTS idx_chi_tiet_tran_dau_trang_thai ON CHI_TIET_TRAN_DAU(TRANG_THAI);
CREATE INDEX IF NOT EXISTS idx_chi_tiet_tran_dau_bat_dau ON CHI_TIET_TRAN_DAU(BAT_DAU);

COMMIT;
```

### Phase 3: New Tables (Optional - cho future features)

```sql
-- File: V3.1__create_supporting_tables.sql

-- Tournament Ratings
CREATE TABLE IF NOT EXISTS TOURNAMENT_RATINGS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_GIAI INT NOT NULL,
    ID_USER INT NOT NULL,
    DIEM_SO INT NOT NULL CHECK (DIEM_SO BETWEEN 1 AND 5),
    BINH_LUAN TEXT,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_GIAI) REFERENCES GIAI_DAU(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_USER) REFERENCES NGUOI_DUNG(ID) ON DELETE CASCADE,
    UNIQUE KEY unique_user_tournament_rating (ID_GIAI, ID_USER)
);

-- News Articles
CREATE TABLE IF NOT EXISTS NEWS_ARTICLES (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TIEU_DE VARCHAR(500) NOT NULL,
    NOI_DUNG TEXT NOT NULL,
    TOM_TAT VARCHAR(1000),
    ANH_DAI_DIEN VARCHAR(500),
    ID_TAC_GIA INT NOT NULL,
    DANH_MUC VARCHAR(50),
    ID_GIAI_DAU INT,
    TRANG_THAI VARCHAR(20) DEFAULT 'draft',
    LUOT_XEM INT DEFAULT 0,
    NOI_BAT BOOLEAN DEFAULT FALSE,
    NGAY_XUAT_BAN TIMESTAMP,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    THOI_GIAN_CAP_NHAT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_TAC_GIA) REFERENCES NGUOI_DUNG(ID),
    FOREIGN KEY (ID_GIAI_DAU) REFERENCES GIAI_DAU(ID) ON DELETE SET NULL
);

-- Notifications
CREATE TABLE IF NOT EXISTS NOTIFICATIONS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_USER INT NOT NULL,
    LOAI VARCHAR(50) NOT NULL,
    TIEU_DE VARCHAR(200) NOT NULL,
    NOI_DUNG TEXT NOT NULL,
    LIEN_KET VARCHAR(500),
    DA_DOC BOOLEAN DEFAULT FALSE,
    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_USER) REFERENCES NGUOI_DUNG(ID) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notification_user ON NOTIFICATIONS(ID_USER);
CREATE INDEX IF NOT EXISTS idx_notification_unread ON NOTIFICATIONS(DA_DOC);

COMMIT;
```

---

## 🏗️ JPA ENTITIES UPDATE

### GiaiDau.java Enhancement

```java
package com.example.btms.model.tournament;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "GIAI_DAU")
public class GiaiDau {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "TEN_GIAI", nullable = false, length = 1000)
    private String tenGiai;
    
    @Column(name = "NGAY_BD", nullable = false)
    private LocalDate ngayBd;
    
    @Column(name = "NGAY_KT", nullable = false)
    private LocalDate ngayKt;
    
    @Column(name = "NGAY_TAO", nullable = false)
    private LocalDateTime ngayTao;
    
    @Column(name = "NGAY_CAP_NHAT", nullable = false)
    private LocalDateTime ngayCapNhat;
    
    @Column(name = "ID_USER", nullable = false)
    private Integer idUser;
    
    // ===== NEW FIELDS FOR WEB PLATFORM =====
    
    @Column(name = "MO_TA", columnDefinition = "TEXT")
    private String moTa;
    
    @Column(name = "DIA_DIEM", length = 500)
    private String diaDiem;
    
    @Column(name = "TINH_THANH", length = 100)
    private String tinhThanh;
    
    @Column(name = "QUOC_GIA", length = 50)
    private String quocGia = "VN";
    
    @Column(name = "TRANG_THAI", length = 20)
    private String trangThai = "upcoming"; // upcoming/registration/ongoing/completed/cancelled
    
    @Column(name = "NOI_BAT")
    private Boolean noiBat = false;
    
    @Column(name = "HINH_ANH", length = 500)
    private String hinhAnh;
    
    @Column(name = "LOGO", length = 500)
    private String logo;
    
    @Column(name = "NGAY_MO_DANG_KI")
    private LocalDate ngayMoDangKi;
    
    @Column(name = "NGAY_DONG_DANG_KI")
    private LocalDate ngayDongDangKi;
    
    @Column(name = "SO_LUONG_TOI_DA")
    private Integer soLuongToiDa;
    
    @Column(name = "PHI_THAM_GIA", precision = 10, scale = 2)
    private BigDecimal phiThamGia;
    
    @Column(name = "GIAI_THUONG", columnDefinition = "TEXT")
    private String giaiThuong;
    
    @Column(name = "DIEN_THOAI", length = 20)
    private String dienThoai;
    
    @Column(name = "EMAIL", length = 100)
    private String email;
    
    @Column(name = "WEBSITE", length = 200)
    private String website;
    
    @Column(name = "CAP_DO", length = 50)
    private String capDo; // professional/amateur/youth
    
    @Column(name = "THE_LOAI", length = 50)
    private String theLoai; // open/invitational/league
    
    @Column(name = "SAN_THI_DAU", columnDefinition = "TEXT")
    private String sanThiDau;
    
    @Column(name = "QUY_DINH", columnDefinition = "TEXT")
    private String quyDinh;
    
    @Column(name = "LUOT_XEM")
    private Integer luotXem = 0;
    
    @Column(name = "DANH_GIA_TB", precision = 3, scale = 2)
    private BigDecimal danhGiaTb;
    
    @Column(name = "TONG_DANH_GIA")
    private Integer tongDanhGia = 0;
    
    // Constructors, Getters, Setters...
    // (Keep existing constructors and add new getters/setters)
}
```

### Create DTOs for API responses

```java
// TournamentDTO.java
package com.example.btms.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TournamentDTO {
    private Integer id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String city;
    private String country;
    private String status;
    private boolean featured;
    private String image;
    private String logo;
    private LocalDate registrationOpenDate;
    private LocalDate registrationDeadline;
    private Integer maxParticipants;
    private BigDecimal entryFee;
    private String prize;
    private String level;
    private String type;
    private Integer viewCount;
    private BigDecimal averageRating;
    private Integer totalRatings;
    
    // Getters and Setters
}
```

---

## 🌐 API DESIGN

### Tournament APIs (Phase 1)

```java
@RestController
@RequestMapping("/api/tournaments")
public class TournamentApiController {
    
    // List tournaments with filters
    @GetMapping
    public ResponseEntity<Page<TournamentDTO>> getTournaments(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(defaultValue = "ngayBd,desc") String sort
    ) {
        // Implementation
    }
    
    // Get tournament by ID
    @GetMapping("/{id}")
    public ResponseEntity<TournamentDetailDTO> getTournamentById(@PathVariable Integer id) {
        // Implementation
    }
    
    // Get featured tournaments
    @GetMapping("/featured")
    public ResponseEntity<List<TournamentDTO>> getFeaturedTournaments(
        @RequestParam(defaultValue = "4") int limit
    ) {
        // Implementation
    }
    
    // Get upcoming tournaments
    @GetMapping("/upcoming")
    public ResponseEntity<List<TournamentDTO>> getUpcomingTournaments(
        @RequestParam(defaultValue = "10") int limit
    ) {
        // Implementation
    }
    
    // Get live tournaments
    @GetMapping("/live")
    public ResponseEntity<List<TournamentDTO>> getLiveTournaments() {
        // Implementation
    }
    
    // Get tournament statistics
    @GetMapping("/{id}/statistics")
    public ResponseEntity<TournamentStatsDTO> getTournamentStats(@PathVariable Integer id) {
        // Implementation
    }
    
    // Get tournament participants
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantDTO>> getParticipants(@PathVariable Integer id) {
        // Implementation
    }
    
    // Get tournament matches
    @GetMapping("/{id}/matches")
    public ResponseEntity<List<MatchDTO>> getMatches(
        @PathVariable Integer id,
        @RequestParam(required = false) String round
    ) {
        // Implementation
    }
    
    // Get tournament results
    @GetMapping("/{id}/results")
    public ResponseEntity<TournamentResultsDTO> getResults(@PathVariable Integer id) {
        // Implementation
    }
    
    // Get tournament calendar events
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarEventDTO>> getCalendarEvents(
        @RequestParam int month,
        @RequestParam int year
    ) {
        // Implementation
    }
    
    // Increment view count
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Integer id) {
        // Implementation
    }
}
```

### Sample API Endpoints Overview

```
GET    /api/tournaments                    - List all tournaments (with filters, pagination)
GET    /api/tournaments/{id}               - Get tournament details
GET    /api/tournaments/featured           - Get featured tournaments
GET    /api/tournaments/upcoming           - Get upcoming tournaments
GET    /api/tournaments/live               - Get live tournaments
GET    /api/tournaments/{id}/participants  - Get participants
GET    /api/tournaments/{id}/matches       - Get matches
GET    /api/tournaments/{id}/results       - Get results
GET    /api/tournaments/{id}/standings     - Get standings
GET    /api/tournaments/calendar           - Get calendar events
POST   /api/tournaments/{id}/register      - Register for tournament
POST   /api/tournaments/{id}/rate          - Rate tournament
POST   /api/tournaments/{id}/view          - Increment view count

GET    /api/players                        - List players
GET    /api/players/{id}                   - Get player profile
GET    /api/players/{id}/statistics        - Get player stats
GET    /api/players/rankings               - Get player rankings

GET    /api/clubs                          - List clubs
GET    /api/clubs/{id}                     - Get club profile
GET    /api/clubs/{id}/members             - Get club members

GET    /api/matches                        - List matches
GET    /api/matches/{id}                   - Get match details
GET    /api/matches/live                   - Get live matches
GET    /api/matches/{id}/comments          - Get match comments
POST   /api/matches/{id}/comments          - Post a comment
```

---

## 📅 IMPLEMENTATION ROADMAP

### Week 1: Database Foundation
- [x] Review current schema
- [ ] Day 1: Execute Phase 1A migration (Tournament enhancements)
- [ ] Day 2: Execute Phase 1B migration (User enhancements)
- [ ] Day 3: Execute Phase 1C migration (Tournament Gallery)
- [ ] Day 4: Update JPA Entities (GiaiDau, NguoiDung)
- [ ] Day 5: Create DTOs and Mappers
- [ ] Day 6-7: Testing migrations, rollback scripts

### Week 2: Service Layer & Repository
- [ ] Update existing repositories with new query methods
- [ ] Implement TournamentService with new features
- [ ] Add filtering, sorting, pagination logic
- [ ] Create search functionality
- [ ] Write unit tests

### Week 3: REST API Development
- [ ] Implement TournamentApiController
- [ ] Add validation and error handling
- [ ] Implement PlayerApiController
- [ ] Implement ClubApiController
- [ ] API documentation with Swagger
- [ ] Integration tests

### Week 4: Frontend Integration
- [ ] Connect TournamentController to new services
- [ ] Update tournament pages with real data
- [ ] Implement AJAX calls for dynamic content
- [ ] Add loading states and error handling
- [ ] Browser testing

### Optional Future Weeks:
- [ ] Phase 2 migrations (Players, Clubs, Matches)
- [ ] Phase 3 migrations (News, Ratings, Notifications)
- [ ] Advanced features (Search, Analytics, Admin panel)

---

## 🔒 BACKWARD COMPATIBILITY

### Ensuring Desktop App Still Works

1. **No breaking changes**: Chỉ ADD columns, không DELETE hoặc RENAME
2. **Default values**: Mọi column mới đều có DEFAULT để không break existing code
3. **Nullable fields**: Hầu hết fields mới đều nullable
4. **Keep existing logic**: Desktop app queries vẫn work với old columns
5. **Separate APIs**: Web APIs không ảnh hưởng đến Desktop logic

### Testing Strategy

1. **Before migration**: Backup database
2. **After each phase**: Test desktop app functionality
3. **Integration tests**: Ensure both systems work together
4. **Rollback plan**: Keep rollback scripts for each migration

---

## 🎯 PRIORITY SUMMARY

### MUST HAVE (Phase 1 - Week 1-2)
- ✅ GIAI_DAU enhancements (status, featured, images, location, etc.)
- ✅ NGUOI_DUNG enhancements (roles, email)
- ✅ TOURNAMENT_GALLERY table
- ✅ Updated JPA Entities
- ✅ Basic DTOs

### SHOULD HAVE (Phase 2 - Week 3-4)
- ⚠️ VAN_DONG_VIEN enhancements (profiles, stats)
- ⚠️ CAU_LAC_BO enhancements
- ⚠️ CHI_TIET_TRAN_DAU enhancements
- ⚠️ REST APIs

### NICE TO HAVE (Phase 3 - Future)
- 💡 NEWS_ARTICLES
- 💡 TOURNAMENT_RATINGS
- 💡 NOTIFICATIONS
- 💡 MATCH_COMMENTS
- 💡 PLAYER_STATISTICS
- 💡 TAGS

---

## 🚀 READY TO START?

### Next Immediate Actions:

1. **Review this plan** - Make sure it aligns with your vision
2. **Backup database** - Safety first!
3. **Execute Phase 1A migration** - Tournament enhancements
4. **Test desktop app** - Ensure nothing breaks
5. **Update GiaiDau entity** - Add JPA annotations for new fields
6. **Create sample data** - Populate new fields for testing

**Ready to execute the first migration script?** 🎯

---

## 📞 QUESTIONS & SUPPORT

If you have any questions about:
- Database design decisions
- Migration strategy
- API design
- JPA entity mappings
- Testing approach

Let me know and I'll help clarify or adjust the plan! 💪
