-- =====================================================
-- Query để kiểm tra schema hiện tại sau khi migrations
-- Chạy trong SSMS để xem cấu trúc database hiện tại
-- Updated: 2025-11-19 - Bao gồm tất cả bảng trong DATABASE_ENHANCEMENT_PLAN.md
-- =====================================================

PRINT '========================================';
PRINT '1. GIAI_DAU TABLE STRUCTURE';
PRINT '========================================';
SELECT 
    ORDINAL_POSITION as [#],
    COLUMN_NAME as [Column],
    DATA_TYPE as [Type],
    CHARACTER_MAXIMUM_LENGTH as [Length],
    IS_NULLABLE as [Nullable],
    COLUMN_DEFAULT as [Default]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'GIAI_DAU'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';
PRINT '2. NGUOI_DUNG TABLE STRUCTURE';
PRINT '========================================';
SELECT 
    ORDINAL_POSITION as [#],
    COLUMN_NAME as [Column],
    DATA_TYPE as [Type],
    CHARACTER_MAXIMUM_LENGTH as [Length],
    IS_NULLABLE as [Nullable],
    COLUMN_DEFAULT as [Default]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'NGUOI_DUNG'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';
PRINT '3. TOURNAMENT_GALLERY TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'TOURNAMENT_GALLERY' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'TOURNAMENT_GALLERY'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet';

PRINT '';
PRINT '========================================';
PRINT '4. VAN_DONG_VIEN TABLE STRUCTURE';
PRINT '========================================';
SELECT 
    ORDINAL_POSITION as [#],
    COLUMN_NAME as [Column],
    DATA_TYPE as [Type],
    CHARACTER_MAXIMUM_LENGTH as [Length],
    IS_NULLABLE as [Nullable],
    COLUMN_DEFAULT as [Default]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'VAN_DONG_VIEN'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';
PRINT '5. CAU_LAC_BO TABLE STRUCTURE';
PRINT '========================================';
SELECT 
    ORDINAL_POSITION as [#],
    COLUMN_NAME as [Column],
    DATA_TYPE as [Type],
    CHARACTER_MAXIMUM_LENGTH as [Length],
    IS_NULLABLE as [Nullable],
    COLUMN_DEFAULT as [Default]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'CAU_LAC_BO'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';
PRINT '6. CHI_TIET_TRAN_DAU TABLE STRUCTURE';
PRINT '========================================';
SELECT 
    ORDINAL_POSITION as [#],
    COLUMN_NAME as [Column],
    DATA_TYPE as [Type],
    CHARACTER_MAXIMUM_LENGTH as [Length],
    IS_NULLABLE as [Nullable],
    COLUMN_DEFAULT as [Default]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'CHI_TIET_TRAN_DAU'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';
PRINT '7. NEWS_ARTICLES TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'NEWS_ARTICLES' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'NEWS_ARTICLES'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '8. TOURNAMENT_RATINGS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'TOURNAMENT_RATINGS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'TOURNAMENT_RATINGS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '9. NOTIFICATIONS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'NOTIFICATIONS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'NOTIFICATIONS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '10. MATCH_COMMENTS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'MATCH_COMMENTS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'MATCH_COMMENTS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '11. PLAYER_STATISTICS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'PLAYER_STATISTICS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'PLAYER_STATISTICS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '12. TAGS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'TAGS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'TAGS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '13. TOURNAMENT_TAGS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'TOURNAMENT_TAGS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'TOURNAMENT_TAGS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '14. ARTICLE_TAGS TABLE STRUCTURE';
PRINT '========================================';
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'ARTICLE_TAGS' AND type = 'U')
BEGIN
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'ARTICLE_TAGS'
    ORDER BY ORDINAL_POSITION;
END
ELSE
    PRINT 'Table does not exist yet (Phase 3)';

PRINT '';
PRINT '========================================';
PRINT '15. ALL INDEXES ON WEB PLATFORM TABLES';
PRINT '========================================';
SELECT 
    OBJECT_NAME(i.object_id) as [Table],
    i.name as [Index_Name],
    i.type_desc as [Type],
    i.is_unique as [Unique],
    COL_NAME(ic.object_id, ic.column_id) as [Column]
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
WHERE OBJECT_NAME(i.object_id) IN (
    'GIAI_DAU', 'NGUOI_DUNG', 'TOURNAMENT_GALLERY', 
    'VAN_DONG_VIEN', 'CAU_LAC_BO', 'CHI_TIET_TRAN_DAU',
    'NEWS_ARTICLES', 'TOURNAMENT_RATINGS', 'NOTIFICATIONS',
    'MATCH_COMMENTS', 'PLAYER_STATISTICS', 'TAGS',
    'TOURNAMENT_TAGS', 'ARTICLE_TAGS'
)
ORDER BY OBJECT_NAME(i.object_id), i.name, ic.key_ordinal;

PRINT '';
PRINT '========================================';
PRINT '16. FOREIGN KEYS ON WEB PLATFORM TABLES';
PRINT '========================================';
SELECT 
    OBJECT_NAME(f.parent_object_id) as [From_Table],
    COL_NAME(fc.parent_object_id, fc.parent_column_id) as [From_Column],
    OBJECT_NAME(f.referenced_object_id) as [To_Table],
    COL_NAME(fc.referenced_object_id, fc.referenced_column_id) as [To_Column],
    f.name as [Constraint_Name],
    f.delete_referential_action_desc as [On_Delete]
FROM sys.foreign_keys f
INNER JOIN sys.foreign_key_columns fc ON f.object_id = fc.constraint_object_id
WHERE OBJECT_NAME(f.parent_object_id) IN (
    'GIAI_DAU', 'NGUOI_DUNG', 'TOURNAMENT_GALLERY',
    'VAN_DONG_VIEN', 'CAU_LAC_BO', 'CHI_TIET_TRAN_DAU',
    'NEWS_ARTICLES', 'TOURNAMENT_RATINGS', 'NOTIFICATIONS',
    'MATCH_COMMENTS', 'PLAYER_STATISTICS', 'TAGS',
    'TOURNAMENT_TAGS', 'ARTICLE_TAGS'
)
   OR OBJECT_NAME(f.referenced_object_id) IN (
    'GIAI_DAU', 'NGUOI_DUNG', 'TOURNAMENT_GALLERY',
    'VAN_DONG_VIEN', 'CAU_LAC_BO', 'CHI_TIET_TRAN_DAU',
    'NEWS_ARTICLES', 'TOURNAMENT_RATINGS', 'NOTIFICATIONS',
    'MATCH_COMMENTS', 'PLAYER_STATISTICS', 'TAGS',
    'TOURNAMENT_TAGS', 'ARTICLE_TAGS'
);

PRINT '';
PRINT '========================================';
PRINT '17. TABLE EXISTENCE CHECK';
PRINT '========================================';
SELECT 
    'GIAI_DAU' as [Table_Name],
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'GIAI_DAU' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END as [Status],
    'Phase 1' as [Phase]
UNION ALL SELECT 'NGUOI_DUNG', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'NGUOI_DUNG' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 1'
UNION ALL SELECT 'TOURNAMENT_GALLERY', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'TOURNAMENT_GALLERY' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 1'
UNION ALL SELECT 'VAN_DONG_VIEN', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'VAN_DONG_VIEN' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 2'
UNION ALL SELECT 'CAU_LAC_BO', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'CAU_LAC_BO' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 2'
UNION ALL SELECT 'CHI_TIET_TRAN_DAU', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'CHI_TIET_TRAN_DAU' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 2'
UNION ALL SELECT 'NEWS_ARTICLES', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'NEWS_ARTICLES' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'TOURNAMENT_RATINGS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'TOURNAMENT_RATINGS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'NOTIFICATIONS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'NOTIFICATIONS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'MATCH_COMMENTS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'MATCH_COMMENTS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'PLAYER_STATISTICS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'PLAYER_STATISTICS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'TAGS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'TAGS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'TOURNAMENT_TAGS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'TOURNAMENT_TAGS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3'
UNION ALL SELECT 'ARTICLE_TAGS', 
    CASE WHEN EXISTS (SELECT * FROM sys.objects WHERE name = 'ARTICLE_TAGS' AND type = 'U') 
        THEN 'EXISTS ✓' ELSE 'NOT EXISTS ✗' END, 'Phase 3';

PRINT '';
PRINT '========================================';
PRINT '18. COLUMN COUNT SUMMARY';
PRINT '========================================';
SELECT 
    TABLE_NAME as [Table],
    COUNT(*) as [Total_Columns]
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME IN (
    'GIAI_DAU', 'NGUOI_DUNG', 'TOURNAMENT_GALLERY',
    'VAN_DONG_VIEN', 'CAU_LAC_BO', 'CHI_TIET_TRAN_DAU',
    'NEWS_ARTICLES', 'TOURNAMENT_RATINGS', 'NOTIFICATIONS',
    'MATCH_COMMENTS', 'PLAYER_STATISTICS', 'TAGS',
    'TOURNAMENT_TAGS', 'ARTICLE_TAGS'
)
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;

PRINT '';
PRINT '========================================';
PRINT '19. PHASE 1 MIGRATION STATUS';
PRINT '========================================';
-- Check if Phase 1 migrations completed successfully
DECLARE @GiaiDauColumns INT, @NguoiDungColumns INT, @TournamentGalleryExists INT;

SELECT @GiaiDauColumns = COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'GIAI_DAU';
SELECT @NguoiDungColumns = COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'NGUOI_DUNG';
SELECT @TournamentGalleryExists = COUNT(*) FROM sys.objects WHERE name = 'TOURNAMENT_GALLERY' AND type = 'U';

PRINT 'GIAI_DAU columns: ' + CAST(@GiaiDauColumns AS VARCHAR) + ' (Expected: 30+)';
PRINT 'NGUOI_DUNG columns: ' + CAST(@NguoiDungColumns AS VARCHAR) + ' (Expected: 14+)';
PRINT 'TOURNAMENT_GALLERY exists: ' + CASE WHEN @TournamentGalleryExists > 0 THEN 'YES ✓' ELSE 'NO ✗' END;

IF @GiaiDauColumns >= 30 AND @NguoiDungColumns >= 14 AND @TournamentGalleryExists > 0
    PRINT CHAR(10) + '✓✓✓ Phase 1 migrations COMPLETED successfully! ✓✓✓'
ELSE
    PRINT CHAR(10) + '⚠⚠⚠ Phase 1 migrations INCOMPLETE or NOT STARTED ⚠⚠⚠';

PRINT '';
PRINT '========================================';
PRINT 'DONE - Schema check completed!';
PRINT '========================================';
