-- =============================================
-- Insert Sample Tournament Data
-- For BTMS Web Platform Testing
-- Database: SQL Server 2016+
-- Created: 2025-11-19
-- =============================================

USE badminton_tournament;
GO

-- Insert sample user first (if not exists)
IF NOT EXISTS (SELECT 1 FROM NGUOI_DUNG WHERE ID = 1)
BEGIN
    SET IDENTITY_INSERT NGUOI_DUNG ON;
    INSERT INTO NGUOI_DUNG (ID, HO_TEN, EMAIL, MAT_KHAU, THOI_GIAN_TAO)
    VALUES (1, N'Admin BTMS', 'admin@btms.vn', '$2a$10$dummyHashForTesting', GETDATE());
    SET IDENTITY_INSERT NGUOI_DUNG OFF;
END
GO

-- Clear existing tournament data
DELETE FROM GIAI_DAU;
GO

-- Insert 10 sample tournaments
SET IDENTITY_INSERT GIAI_DAU ON;
GO

-- Tournament 1: Ongoing tournament
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    1, 
    N'Giải Cầu Lông Vô Địch TP.HCM 2025', 
    '2025-11-15', 
    '2025-11-25', 
    GETDATE(), 
    GETDATE(), 
    1,
    N'Giải đấu cầu lông chuyên nghiệp quy tụ các tay vợt hàng đầu TP.HCM. Tổng giải thưởng lên đến 500 triệu đồng.',
    N'Nhà thi đấu Phú Thọ',
    N'TP. Hồ Chí Minh',
    'VN',
    'ongoing',
    1,
    '/icons/tournaments/tournament-1.jpg',
    '2025-10-01',
    '2025-11-10',
    128,
    96,
    500000,
    N'Chuyên nghiệp',
    'open',
    1245,
    4.5,
    89
);

-- Tournament 2: Registration open
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    2,
    N'Giải Cầu Lông Thiếu Niên Hà Nội Open 2025',
    '2025-12-01',
    '2025-12-10',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu dành cho lứa tuổi thiếu niên (U15, U17, U19), tạo sân chơi bổ ích cho thế hệ trẻ.',
    N'Cung thể thao Quần Ngựa',
    N'Hà Nội',
    'VN',
    'registration',
    1,
    '/icons/tournaments/tournament-2.jpg',
    '2025-11-01',
    '2025-11-25',
    200,
    67,
    200000,
    N'Thiếu niên',
    'open',
    892,
    4.2,
    54
);

-- Tournament 3: Upcoming tournament
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    3,
    N'Giải Cầu Lông Doanh Nghiệp Đà Nẵng Cup',
    '2025-12-15',
    '2025-12-20',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu dành cho các vận động viên đang làm việc tại các doanh nghiệp khu vực Đà Nẵng.',
    N'Nhà thi đấu Tiên Sơn',
    N'Đà Nẵng',
    'VN',
    'upcoming',
    0,
    '/icons/tournaments/tournament-3.jpg',
    '2025-11-20',
    '2025-12-10',
    64,
    0,
    300000,
    N'Nghiệp dư',
    'open',
    456,
    NULL,
    0
);

-- Tournament 4: Featured ongoing
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    4,
    N'Vietnam International Series 2025',
    '2025-11-18',
    '2025-11-30',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải quốc tế có sự tham gia của các tay vợt từ nhiều quốc gia trong khu vực ASEAN.',
    N'Cung thể thao Rạch Miễu',
    N'TP. Hồ Chí Minh',
    'VN',
    'ongoing',
    1,
    '/icons/tournaments/tournament-4.jpg',
    '2025-09-01',
    '2025-11-01',
    256,
    256,
    1000000,
    N'Quốc tế',
    'invitational',
    3456,
    4.8,
    156
);

-- Tournament 5: Registration closing soon
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    5,
    N'Giải Cầu Lông Cúp Các CLB Cần Thơ',
    '2025-11-28',
    '2025-12-05',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu giao hữu giữa các câu lạc bộ cầu lông tại Cần Thơ và các tỉnh lân cận.',
    N'Nhà văn hóa Ninh Kiều',
    N'Cần Thơ',
    'VN',
    'registration',
    0,
    '/icons/tournaments/tournament-5.jpg',
    '2025-10-15',
    '2025-11-20',
    96,
    78,
    150000,
    N'Câu lạc bộ',
    'open',
    623,
    4.0,
    32
);

-- Tournament 6: Completed tournament
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    6,
    N'Giải Cầu Lông Học Sinh Sinh Viên TP.HCM 2025',
    '2025-10-10',
    '2025-10-20',
    DATEADD(day, -40, GETDATE()),
    GETDATE(),
    1,
    N'Giải đấu dành cho học sinh sinh viên các trường đại học, cao đẳng tại TP.HCM.',
    N'Nhà thi đấu Phan Đình Phùng',
    N'TP. Hồ Chí Minh',
    'VN',
    'completed',
    0,
    '/icons/tournaments/tournament-6.jpg',
    '2025-09-01',
    '2025-10-01',
    150,
    142,
    100000,
    N'Sinh viên',
    'open',
    2134,
    4.6,
    98
);

-- Tournament 7: Free tournament (registration open)
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    7,
    N'Giải Cầu Lông Vui Cộng Đồng Quận 1',
    '2025-12-08',
    '2025-12-09',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu miễn phí dành cho cộng đồng yêu cầu lông tại Quận 1, khuyến khích phong trào tập luyện.',
    N'Sân cầu lông Công viên 23/9',
    N'TP. Hồ Chí Minh',
    'VN',
    'registration',
    1,
    '/icons/tournaments/tournament-7.jpg',
    '2025-11-10',
    '2025-12-01',
    48,
    23,
    0,
    N'Nghiệp dư',
    'open',
    234,
    NULL,
    0
);

-- Tournament 8: Upcoming professional
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    8,
    N'Giải Vô Địch Cầu Lông Quốc Gia 2026',
    '2026-01-15',
    '2026-01-30',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu cấp quốc gia, tìm kiếm những tay vợt xuất sắc nhất để tham dự các giải khu vực và thế giới.',
    N'Cung thể thao Quần Ngựa',
    N'Hà Nội',
    'VN',
    'upcoming',
    1,
    '/icons/tournaments/tournament-8.jpg',
    '2025-12-01',
    '2026-01-05',
    512,
    0,
    800000,
    N'Quốc gia',
    'invitational',
    567,
    NULL,
    0
);

-- Tournament 9: Registration open (mid-size)
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    9,
    N'Giải Cầu Lông Mở Rộng Vũng Tàu Open',
    '2025-12-22',
    '2025-12-26',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu mở rộng với sự tham gia của các tay vợt phong trào và chuyên nghiệp tại khu vực miền Đông Nam Bộ.',
    N'Nhà thi đấu Bãi Trước',
    N'Vũng Tàu',
    'VN',
    'registration',
    0,
    '/icons/tournaments/tournament-9.jpg',
    '2025-11-15',
    '2025-12-15',
    128,
    45,
    250000,
    N'Mở rộng',
    'open',
    789,
    3.8,
    21
);

-- Tournament 10: Upcoming charity event
INSERT INTO GIAI_DAU (
    ID, TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, QUOC_GIA, TRANG_THAI, NOI_BAT, HINH_ANH,
    NGAY_MO_DANG_KI, NGAY_DONG_DANG_KI, SO_LUONG_TOI_DA, SO_LUONG_DA_DANG_KY,
    PHI_THAM_GIA, CAP_DO, THE_LOAI, LUOT_XEM, DANH_GIA_TB, TONG_DANH_GIA
) VALUES (
    10,
    N'Giải Cầu Lông Từ Thiện Vì Trẻ Em Nghèo',
    '2026-02-14',
    '2026-02-15',
    GETDATE(),
    GETDATE(),
    1,
    N'Giải đấu từ thiện với mục đích gây quỹ hỗ trợ trẻ em có hoàn cảnh khó khăn. Toàn bộ lệ phí đăng ký sẽ được dùng cho mục đích từ thiện.',
    N'Nhà văn hóa Lao động Quận 1',
    N'TP. Hồ Chí Minh',
    'VN',
    'upcoming',
    1,
    '/icons/tournaments/tournament-10.jpg',
    '2026-01-01',
    '2026-02-10',
    100,
    0,
    200000,
    N'Từ thiện',
    'open',
    123,
    NULL,
    0
);

SET IDENTITY_INSERT GIAI_DAU OFF;
GO

-- Verify inserted data
SELECT 
    ID,
    TEN_GIAI,
    NGAY_BD,
    NGAY_KT,
    TRANG_THAI,
    NOI_BAT,
    TINH_THANH,
    SO_LUONG_DA_DANG_KY,
    SO_LUONG_TOI_DA,
    PHI_THAM_GIA,
    LUOT_XEM
FROM GIAI_DAU
ORDER BY 
    CASE TRANG_THAI
        WHEN 'ongoing' THEN 1
        WHEN 'registration' THEN 2
        WHEN 'upcoming' THEN 3
        WHEN 'completed' THEN 4
        ELSE 5
    END,
    NGAY_BD;
GO

PRINT 'Successfully inserted 10 sample tournaments!';
PRINT 'Summary:';
PRINT '- Ongoing: 2 tournaments';
PRINT '- Registration: 4 tournaments';
PRINT '- Upcoming: 3 tournaments';
PRINT '- Completed: 1 tournament';
GO
