-- =====================================================-- =====================================================

-- Migration: V1.3 - Create TOURNAMENT_GALLERY-- Migration: V1.3 - Create TOURNAMENT_GALLERY

-- Purpose: Store tournament images, videos, and documents-- Purpose: Store tournament images, videos, and documents

-- Date: 2025-11-18-- Date: 2025-11-17

-- Author: BTMS Team-- Author: BTMS Team

-- Database: SQL Server-- =====================================================

-- =====================================================

-- Create tournament gallery table

-- Create tournament gallery table (SQL Server syntax)CREATE TABLE IF NOT EXISTS TOURNAMENT_GALLERY (

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'TOURNAMENT_GALLERY') AND type in (N'U'))    ID INT AUTO_INCREMENT PRIMARY KEY,

BEGIN    ID_GIAI INT NOT NULL,

    CREATE TABLE TOURNAMENT_GALLERY (    LOAI VARCHAR(20) NOT NULL,              -- 'image', 'video', 'document'

        ID INT IDENTITY(1,1) PRIMARY KEY,    URL VARCHAR(500) NOT NULL,

        ID_GIAI INT NOT NULL,    TIEU_DE VARCHAR(200),

        LOAI NVARCHAR(20) NOT NULL,              -- 'image', 'video', 'document'    MO_TA TEXT,

        URL NVARCHAR(500) NOT NULL,    THU_TU INT DEFAULT 0,                   -- Display order

        TIEU_DE NVARCHAR(200),    THOI_GIAN_TAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

        MO_TA NVARCHAR(MAX),    CONSTRAINT fk_tournament_gallery_giai FOREIGN KEY (ID_GIAI) 

        THU_TU INT DEFAULT 0,                    -- Display order        REFERENCES GIAI_DAU(ID) ON DELETE CASCADE

        THOI_GIAN_TAO DATETIME2 DEFAULT GETDATE(),);

        CONSTRAINT fk_tournament_gallery_giai FOREIGN KEY (ID_GIAI) 

            REFERENCES GIAI_DAU(ID) ON DELETE CASCADE-- Create indexes

    );CREATE INDEX IF NOT EXISTS idx_tournament_gallery_giai ON TOURNAMENT_GALLERY(ID_GIAI);

ENDCREATE INDEX IF NOT EXISTS idx_tournament_gallery_loai ON TOURNAMENT_GALLERY(LOAI);

GOCREATE INDEX IF NOT EXISTS idx_tournament_gallery_thu_tu ON TOURNAMENT_GALLERY(THU_TU);



-- Create indexes (SQL Server syntax)COMMIT;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tournament_gallery_giai' AND object_id = OBJECT_ID('TOURNAMENT_GALLERY'))

    CREATE INDEX idx_tournament_gallery_giai ON TOURNAMENT_GALLERY(ID_GIAI);-- =====================================================

-- Media Types Reference:

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tournament_gallery_loai' AND object_id = OBJECT_ID('TOURNAMENT_GALLERY'))-- - image: Tournament photos, posters

    CREATE INDEX idx_tournament_gallery_loai ON TOURNAMENT_GALLERY(LOAI);-- - video: Highlight videos, live streams

-- - document: Rules, schedules, forms

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tournament_gallery_thu_tu' AND object_id = OBJECT_ID('TOURNAMENT_GALLERY'))-- =====================================================

    CREATE INDEX idx_tournament_gallery_thu_tu ON TOURNAMENT_GALLERY(THU_TU);

GO-- Sample insert (for testing)

-- INSERT INTO TOURNAMENT_GALLERY (ID_GIAI, LOAI, URL, TIEU_DE, MO_TA, THU_TU)

-- =====================================================-- VALUES (1, 'image', '/images/tournaments/default.jpg', 'Tournament Banner', 'Main tournament image', 1);

-- Media Types Reference:

-- - image: Tournament photos, posters-- Verification Query

-- - video: Highlight videos, live streams-- SELECT * FROM TOURNAMENT_GALLERY;

-- - document: Rules, schedules, forms
-- =====================================================

-- Sample insert (for testing)
-- INSERT INTO TOURNAMENT_GALLERY (ID_GIAI, LOAI, URL, TIEU_DE, MO_TA, THU_TU)
-- VALUES (1, 'image', '/images/tournaments/default.jpg', N'Tournament Banner', N'Main tournament image', 1);

-- Verification Query
-- SELECT * FROM TOURNAMENT_GALLERY;
