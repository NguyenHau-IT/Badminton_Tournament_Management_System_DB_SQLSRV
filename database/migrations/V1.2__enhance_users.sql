-- =====================================================-- =====================================================

-- Migration: V1.2 - Enhance NGUOI_DUNG (Users)-- Migration: V1.2 - Enhance NGUOI_DUNG (Users)

-- Purpose: Add web platform fields to users table-- Purpose: Add web platform fields to users table

-- Date: 2025-11-18-- Date: 2025-11-17

-- Author: BTMS Team-- Author: BTMS Team

-- Database: SQL Server-- =====================================================

-- =====================================================

-- Add new columns to NGUOI_DUNG table

-- Add new columns to NGUOI_DUNG table (SQL Server syntax)ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(100);

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'EMAIL')ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS DIEN_THOAI VARCHAR(20);

    ALTER TABLE NGUOI_DUNG ADD EMAIL NVARCHAR(100);ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS VAI_TRO VARCHAR(20) DEFAULT 'CLIENT';

ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS TRANG_THAI VARCHAR(20) DEFAULT 'active';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'DIEN_THOAI')ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS LAN_DANG_NHAP_CUOI TIMESTAMP;

    ALTER TABLE NGUOI_DUNG ADD DIEN_THOAI NVARCHAR(20);ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS ANH_DAI_DIEN VARCHAR(500);

ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS XAC_THUC_EMAIL BOOLEAN DEFAULT FALSE;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'VAI_TRO')ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS MA_XAC_THUC VARCHAR(100);

    ALTER TABLE NGUOI_DUNG ADD VAI_TRO NVARCHAR(20) DEFAULT 'CLIENT';ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS MA_DAT_LAI_MK VARCHAR(100);

ALTER TABLE NGUOI_DUNG ADD COLUMN IF NOT EXISTS NGAY_HET_HAN_TOKEN TIMESTAMP;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'TRANG_THAI')

    ALTER TABLE NGUOI_DUNG ADD TRANG_THAI NVARCHAR(20) DEFAULT 'active';-- Create indexes

CREATE UNIQUE INDEX IF NOT EXISTS idx_nguoi_dung_email ON NGUOI_DUNG(EMAIL) WHERE EMAIL IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'LAN_DANG_NHAP_CUOI')CREATE INDEX IF NOT EXISTS idx_nguoi_dung_vai_tro ON NGUOI_DUNG(VAI_TRO);

    ALTER TABLE NGUOI_DUNG ADD LAN_DANG_NHAP_CUOI DATETIME2;CREATE INDEX IF NOT EXISTS idx_nguoi_dung_trang_thai ON NGUOI_DUNG(TRANG_THAI);



IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'ANH_DAI_DIEN')-- Update existing users with default values

    ALTER TABLE NGUOI_DUNG ADD ANH_DAI_DIEN NVARCHAR(500);UPDATE NGUOI_DUNG SET VAI_TRO = 'ADMIN' WHERE HO_TEN = 'adminn' AND VAI_TRO IS NULL;

UPDATE NGUOI_DUNG SET VAI_TRO = 'CLIENT' WHERE VAI_TRO IS NULL;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'XAC_THUC_EMAIL')UPDATE NGUOI_DUNG SET TRANG_THAI = 'active' WHERE TRANG_THAI IS NULL;

    ALTER TABLE NGUOI_DUNG ADD XAC_THUC_EMAIL BIT DEFAULT 0;UPDATE NGUOI_DUNG SET XAC_THUC_EMAIL = FALSE WHERE XAC_THUC_EMAIL IS NULL;



IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'MA_XAC_THUC')COMMIT;

    ALTER TABLE NGUOI_DUNG ADD MA_XAC_THUC NVARCHAR(100);

-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'MA_DAT_LAI_MK')-- User Roles Reference:

    ALTER TABLE NGUOI_DUNG ADD MA_DAT_LAI_MK NVARCHAR(100);-- - ADMIN: Full system access

-- - ORGANIZER: Can create and manage tournaments

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NGUOI_DUNG') AND name = 'NGAY_HET_HAN_TOKEN')-- - PLAYER: Can register and participate

    ALTER TABLE NGUOI_DUNG ADD NGAY_HET_HAN_TOKEN DATETIME2;-- - CLIENT: Basic read-only access

GO-- =====================================================



-- Create indexes (SQL Server syntax)-- Verification Queries

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_nguoi_dung_email' AND object_id = OBJECT_ID('NGUOI_DUNG'))-- SELECT ID, HO_TEN, EMAIL, VAI_TRO, TRANG_THAI FROM NGUOI_DUNG;

    CREATE UNIQUE INDEX idx_nguoi_dung_email ON NGUOI_DUNG(EMAIL) WHERE EMAIL IS NOT NULL;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_nguoi_dung_vai_tro' AND object_id = OBJECT_ID('NGUOI_DUNG'))
    CREATE INDEX idx_nguoi_dung_vai_tro ON NGUOI_DUNG(VAI_TRO);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_nguoi_dung_trang_thai' AND object_id = OBJECT_ID('NGUOI_DUNG'))
    CREATE INDEX idx_nguoi_dung_trang_thai ON NGUOI_DUNG(TRANG_THAI);
GO

-- Update existing users with default values
UPDATE NGUOI_DUNG SET VAI_TRO = 'ADMIN' WHERE HO_TEN = 'adminn' AND VAI_TRO IS NULL;
UPDATE NGUOI_DUNG SET VAI_TRO = 'CLIENT' WHERE VAI_TRO IS NULL;
UPDATE NGUOI_DUNG SET TRANG_THAI = 'active' WHERE TRANG_THAI IS NULL;
UPDATE NGUOI_DUNG SET XAC_THUC_EMAIL = 0 WHERE XAC_THUC_EMAIL IS NULL;
GO

-- =====================================================
-- User Roles Reference:
-- - ADMIN: Full system access
-- - ORGANIZER: Can create and manage tournaments
-- - PLAYER: Can register and participate
-- - CLIENT: Basic read-only access
-- =====================================================

-- Verification Queries
-- SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'NGUOI_DUNG' 
-- ORDER BY ORDINAL_POSITION;

-- SELECT ID, HO_TEN, EMAIL, VAI_TRO, TRANG_THAI FROM NGUOI_DUNG;
