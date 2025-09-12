-- Migration Script: Chuyển đổi từ schema cũ sang schema mới
-- Database: giai_cau_long

USE giai_cau_long;

-- 1. Tạo giải đấu mặc định từ dữ liệu cũ
INSERT INTO giai (ten, cap_do, dia_diem, thanh_pho, ngay_bd, ngay_kt)
SELECT 
    'Giải Cầu Lông Tổng Hợp' as ten,
    'Mở rộng' as cap_do,
    'Nhà thi đấu' as dia_diem,
    'TP.HCM' as thanh_pho,
    CURDATE() as ngay_bd,
    DATE_ADD(CURDATE(), INTERVAL 5 DAY) as ngay_kt
WHERE NOT EXISTS (SELECT 1 FROM giai LIMIT 1);

-- 2. Tạo sân đấu mặc định
INSERT INTO san (giai_id, ten)
SELECT 
    giai_id,
    CONCAT('Sân ', ROW_NUMBER() OVER (ORDER BY giai_id))
FROM giai
WHERE NOT EXISTS (SELECT 1 FROM san LIMIT 1)
LIMIT 4;

-- 3. Tạo câu lạc bộ mặc định
INSERT INTO clb (ten, thanh_pho, quoc_gia)
VALUES 
    ('CLB Cầu Lông Tổng Hợp', 'TP.HCM', 'VN')
WHERE NOT EXISTS (SELECT 1 FROM clb LIMIT 1);

-- 4. Migrate dữ liệu vận động viên (nếu có bảng cũ)
-- Giả sử có bảng vdv_cu với cấu trúc: vdv_id, ho, ten, gioi_tinh
-- INSERT INTO vdv (vdv_id, ho, ten, gioi_tinh, clb_id, quoc_gia, created_at)
-- SELECT 
--     vdv_id,
--     ho,
--     ten,
--     COALESCE(gioi_tinh, 'M') as gioi_tinh,
--     (SELECT clb_id FROM clb LIMIT 1) as clb_id,
--     'VN' as quoc_gia,
--     NOW() as created_at
-- FROM vdv_cu
-- WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'vdv_cu');

-- 5. Migrate dữ liệu sự kiện (nếu có bảng cũ)
-- Giả sử có bảng su_kien_cu với cấu trúc: su_kien_id, ten, ma
-- INSERT INTO su_kien (su_kien_id, giai_id, ma, ten, so_luong, luat_thi_dau, loai_bang, created_at)
-- SELECT 
--     su_kien_id,
--     (SELECT giai_id FROM giai LIMIT 1) as giai_id,
--     COALESCE(ma, 'DNM') as ma,
--     ten,
--     64 as so_luong,
--     '3x21 rally' as luat_thi_dau,
--     'LOAI_TRUC_TIEP' as loai_bang,
--     NOW() as created_at
-- FROM su_kien_cu
-- WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'su_kien_cu');

-- 6. Migrate dữ liệu cặp đôi (nếu có bảng cũ)
-- INSERT INTO capdoi (capdoi_id, su_kien_id, vdv1_id, vdv2_id, created_at)
-- SELECT 
--     capdoi_id,
--     su_kien_id,
--     vdv1_id,
--     vdv2_id,
--     NOW() as created_at
-- FROM capdoi_cu
-- WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'capdoi_cu');

-- 7. Migrate dữ liệu đăng ký (nếu có bảng cũ)
-- INSERT INTO dangky (dangky_id, su_kien_id, loai, vdv_id, capdoi_id, trang_thai, created_at)
-- SELECT 
--     dangky_id,
--     su_kien_id,
--     COALESCE(loai, 'DON') as loai,
--     vdv_id,
--     capdoi_id,
--     COALESCE(trang_thai, 'DUYET') as trang_thai,
--     NOW() as created_at
-- FROM dangky_cu
-- WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'dangky_cu');

-- 8. Tạo dữ liệu mẫu cho testing
INSERT INTO vdv (ho, ten, gioi_tinh, clb_id, quoc_gia, created_at)
SELECT 
    'Nguyễn' as ho,
    'Văn A' as ten,
    'M' as gioi_tinh,
    (SELECT clb_id FROM clb LIMIT 1) as clb_id,
    'VN' as quoc_gia,
    NOW() as created_at
WHERE NOT EXISTS (SELECT 1 FROM vdv LIMIT 1);

INSERT INTO vdv (ho, ten, gioi_tinh, clb_id, quoc_gia, created_at)
SELECT 
    'Trần' as ho,
    'Thị B' as ten,
    'F' as gioi_tinh,
    (SELECT clb_id FROM clb LIMIT 1) as clb_id,
    'VN' as quoc_gia,
    NOW() as created_at
WHERE (SELECT COUNT(*) FROM vdv) = 1;

INSERT INTO su_kien (giai_id, ma, ten, so_luong, luat_thi_dau, loai_bang, created_at)
SELECT 
    (SELECT giai_id FROM giai LIMIT 1) as giai_id,
    'DNM' as ma,
    'Đơn Nam' as ten,
    32 as so_luong,
    '3x21 rally' as luat_thi_dau,
    'LOAI_TRUC_TIEP' as loai_bang,
    NOW() as created_at
WHERE NOT EXISTS (SELECT 1 FROM su_kien LIMIT 1);

INSERT INTO su_kien (giai_id, ma, ten, so_luong, luat_thi_dau, loai_bang, created_at)
SELECT 
    (SELECT giai_id FROM giai LIMIT 1) as giai_id,
    'DNu' as ma,
    'Đơn Nữ' as ten,
    32 as so_luong,
    '3x21 rally' as luat_thi_dau,
    'LOAI_TRUC_TIEP' as loai_bang,
    NOW() as created_at
WHERE (SELECT COUNT(*) FROM su_kien) = 1;

-- 9. Tạo đăng ký mẫu
INSERT INTO dangky (su_kien_id, loai, vdv_id, trang_thai, created_at)
SELECT 
    (SELECT su_kien_id FROM su_kien WHERE ma = 'DNM' LIMIT 1) as su_kien_id,
    'DON' as loai,
    (SELECT vdv_id FROM vdv WHERE gioi_tinh = 'M' LIMIT 1) as vdv_id,
    'DUYET' as trang_thai,
    NOW() as created_at
WHERE NOT EXISTS (SELECT 1 FROM dangky LIMIT 1);

INSERT INTO dangky (su_kien_id, loai, vdv_id, trang_thai, created_at)
SELECT 
    (SELECT su_kien_id FROM su_kien WHERE ma = 'DNu' LIMIT 1) as su_kien_id,
    'DON' as loai,
    (SELECT vdv_id FROM vdv WHERE gioi_tinh = 'F' LIMIT 1) as vdv_id,
    'DUYET' as trang_thai,
    NOW() as created_at
WHERE (SELECT COUNT(*) FROM dangky) = 1;

-- 10. Tạo bốc thăm mẫu
INSERT INTO boc_tham (su_kien_id, ten, kieu, created_at)
SELECT 
    su_kien_id,
    'Bảng chính' as ten,
    'AUTO' as kieu,
    NOW() as created_at
FROM su_kien
WHERE NOT EXISTS (SELECT 1 FROM boc_tham LIMIT 1);

-- 11. Tạo vị trí trong bảng đấu
INSERT INTO vi_tri (boc_tham_id, so_thu_tu, dangky_id, bye)
SELECT 
    bt.boc_tham_id,
    ROW_NUMBER() OVER (ORDER BY bt.boc_tham_id) as so_thu_tu,
    dk.dangky_id,
    FALSE as bye
FROM boc_tham bt
JOIN su_kien sk ON sk.su_kien_id = bt.su_kien_id
JOIN dangky dk ON dk.su_kien_id = sk.su_kien_id
WHERE NOT EXISTS (SELECT 1 FROM vi_tri LIMIT 1);

-- 12. Tạo trận đấu mẫu
INSERT INTO tran (boc_tham_id, vong, so_tran, vitri_tren, vitri_duoi, trang_thai)
SELECT 
    bt.boc_tham_id,
    1 as vong,
    1 as so_tran,
    1 as vitri_tren,
    2 as vitri_duoi,
    'CHO' as trang_thai
FROM boc_tham bt
WHERE NOT EXISTS (SELECT 1 FROM tran LIMIT 1);

-- Kiểm tra dữ liệu đã migrate
SELECT 'Migration completed successfully!' as status;
SELECT 'Tables created:' as info;
SELECT table_name FROM information_schema.tables WHERE table_schema = 'giai_cau_long' ORDER BY table_name;
SELECT 'Sample data counts:' as info;
SELECT 'giai' as table_name, COUNT(*) as count FROM giai
UNION ALL SELECT 'san', COUNT(*) FROM san
UNION ALL SELECT 'clb', COUNT(*) FROM clb
UNION ALL SELECT 'vdv', COUNT(*) FROM vdv
UNION ALL SELECT 'su_kien', COUNT(*) FROM su_kien
UNION ALL SELECT 'capdoi', COUNT(*) FROM capdoi
UNION ALL SELECT 'dangky', COUNT(*) FROM dangky
UNION ALL SELECT 'boc_tham', COUNT(*) FROM boc_tham
UNION ALL SELECT 'vi_tri', COUNT(*) FROM vi_tri
UNION ALL SELECT 'tran', COUNT(*) FROM tran
UNION ALL SELECT 'ty_so', COUNT(*) FROM ty_so;
