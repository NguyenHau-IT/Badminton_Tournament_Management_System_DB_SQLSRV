-- =====================================================
-- Migration: V1.1 - Enhance GIAI_DAU (Tournaments)
-- Purpose: Add web platform fields to tournaments table
-- Date: 2025-11-18
-- Author: BTMS Team
-- Database: SQL Server
-- =====================================================

-- Add new columns to GIAI_DAU table (SQL Server syntax)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'MO_TA')
    ALTER TABLE GIAI_DAU ADD MO_TA NVARCHAR(MAX);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'DIA_DIEM')
    ALTER TABLE GIAI_DAU ADD DIA_DIEM NVARCHAR(500);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'TINH_THANH')
    ALTER TABLE GIAI_DAU ADD TINH_THANH NVARCHAR(100);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'QUOC_GIA')
    ALTER TABLE GIAI_DAU ADD QUOC_GIA NVARCHAR(50) DEFAULT 'VN';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'TRANG_THAI')
    ALTER TABLE GIAI_DAU ADD TRANG_THAI NVARCHAR(20) DEFAULT 'upcoming';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'NOI_BAT')
    ALTER TABLE GIAI_DAU ADD NOI_BAT BIT DEFAULT 0;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'HINH_ANH')
    ALTER TABLE GIAI_DAU ADD HINH_ANH NVARCHAR(500);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'LOGO')
    ALTER TABLE GIAI_DAU ADD LOGO NVARCHAR(500);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'NGAY_MO_DANG_KI')
    ALTER TABLE GIAI_DAU ADD NGAY_MO_DANG_KI DATE;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'NGAY_DONG_DANG_KI')
    ALTER TABLE GIAI_DAU ADD NGAY_DONG_DANG_KI DATE;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'SO_LUONG_TOI_DA')
    ALTER TABLE GIAI_DAU ADD SO_LUONG_TOI_DA INT;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'PHI_THAM_GIA')
    ALTER TABLE GIAI_DAU ADD PHI_THAM_GIA DECIMAL(10,2);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'GIAI_THUONG')
    ALTER TABLE GIAI_DAU ADD GIAI_THUONG NVARCHAR(MAX);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'DIEN_THOAI')
    ALTER TABLE GIAI_DAU ADD DIEN_THOAI NVARCHAR(20);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'EMAIL')
    ALTER TABLE GIAI_DAU ADD EMAIL NVARCHAR(100);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'WEBSITE')
    ALTER TABLE GIAI_DAU ADD WEBSITE NVARCHAR(200);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'CAP_DO')
    ALTER TABLE GIAI_DAU ADD CAP_DO NVARCHAR(50);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'THE_LOAI')
    ALTER TABLE GIAI_DAU ADD THE_LOAI NVARCHAR(50);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'SAN_THI_DAU')
    ALTER TABLE GIAI_DAU ADD SAN_THI_DAU NVARCHAR(MAX);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'QUY_DINH')
    ALTER TABLE GIAI_DAU ADD QUY_DINH NVARCHAR(MAX);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'LUOT_XEM')
    ALTER TABLE GIAI_DAU ADD LUOT_XEM INT DEFAULT 0;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'DANH_GIA_TB')
    ALTER TABLE GIAI_DAU ADD DANH_GIA_TB DECIMAL(3,2);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'GIAI_DAU') AND name = 'TONG_DANH_GIA')
    ALTER TABLE GIAI_DAU ADD TONG_DANH_GIA INT DEFAULT 0;
GO

-- Create indexes for better query performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_giai_dau_trang_thai' AND object_id = OBJECT_ID('GIAI_DAU'))
    CREATE INDEX idx_giai_dau_trang_thai ON GIAI_DAU(TRANG_THAI);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_giai_dau_noi_bat' AND object_id = OBJECT_ID('GIAI_DAU'))
    CREATE INDEX idx_giai_dau_noi_bat ON GIAI_DAU(NOI_BAT);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_giai_dau_ngay_bd' AND object_id = OBJECT_ID('GIAI_DAU'))
    CREATE INDEX idx_giai_dau_ngay_bd ON GIAI_DAU(NGAY_BD);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_giai_dau_ngay_kt' AND object_id = OBJECT_ID('GIAI_DAU'))
    CREATE INDEX idx_giai_dau_ngay_kt ON GIAI_DAU(NGAY_KT);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_giai_dau_tinh_thanh' AND object_id = OBJECT_ID('GIAI_DAU'))
    CREATE INDEX idx_giai_dau_tinh_thanh ON GIAI_DAU(TINH_THANH);
GO

-- Update existing tournaments with appropriate status based on dates
UPDATE GIAI_DAU 
SET TRANG_THAI = CASE 
    WHEN NGAY_BD > CAST(GETDATE() AS DATE) THEN 'upcoming'
    WHEN NGAY_KT < CAST(GETDATE() AS DATE) THEN 'completed'
    WHEN NGAY_BD <= CAST(GETDATE() AS DATE) AND NGAY_KT >= CAST(GETDATE() AS DATE) THEN 'ongoing'
    ELSE 'upcoming'
END
WHERE TRANG_THAI IS NULL OR TRANG_THAI = '';

-- Set default values for required fields
UPDATE GIAI_DAU SET QUOC_GIA = 'VN' WHERE QUOC_GIA IS NULL;
UPDATE GIAI_DAU SET NOI_BAT = 0 WHERE NOI_BAT IS NULL;
UPDATE GIAI_DAU SET LUOT_XEM = 0 WHERE LUOT_XEM IS NULL;
UPDATE GIAI_DAU SET TONG_DANH_GIA = 0 WHERE TONG_DANH_GIA IS NULL;
GO

-- =====================================================
-- Verification Queries (Run these to check)
-- =====================================================

-- Check if all columns were added
-- SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'GIAI_DAU' 
-- ORDER BY ORDINAL_POSITION;

-- Check tournament statuses
-- SELECT ID, TEN_GIAI, NGAY_BD, NGAY_KT, TRANG_THAI FROM GIAI_DAU;

-- Check indexes
-- SELECT name, type_desc FROM sys.indexes WHERE object_id = OBJECT_ID('GIAI_DAU');
