-- =============================================
-- Quick Test Data - 5 Tournaments
-- Chạy nhanh để test features
-- =============================================

USE badminton_tournament;
GO

-- Tournament 1: ONGOING - Featured
INSERT INTO GIAI_DAU (
    TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, TRANG_THAI, NOI_BAT, HINH_ANH,
    PHI_THAM_GIA, CAP_DO, LUOT_XEM, DANH_GIA_TB
) VALUES (
    N'Giải Cầu Lông TP.HCM Open 2025',
    '2025-11-15', '2025-11-25',
    GETDATE(), GETDATE(), 1,
    N'Giải đấu chuyên nghiệp quy tụ các tay vợt hàng đầu',
    N'Nhà thi đấu Phú Thọ',
    N'TP. Hồ Chí Minh',
    'ongoing', 1,
    'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=400',
    500000, N'Chuyên nghiệp', 1250, 4.5
);

-- Tournament 2: ONGOING - Live
INSERT INTO GIAI_DAU (
    TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, TRANG_THAI, NOI_BAT, HINH_ANH,
    PHI_THAM_GIA, CAP_DO, LUOT_XEM, DANH_GIA_TB
) VALUES (
    N'Giải Vô Địch Hà Nội 2025',
    '2025-11-18', '2025-11-22',
    GETDATE(), GETDATE(), 1,
    N'Giải đấu cấp thành phố dành cho tay vợt nghiệp dư',
    N'Cung Văn hóa Hữu nghị Việt Xô',
    N'Hà Nội',
    'ongoing', 1,
    'https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=400',
    300000, N'Nghiệp dư', 890, 4.2
);

-- Tournament 3: REGISTRATION - Featured
INSERT INTO GIAI_DAU (
    TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, TRANG_THAI, NOI_BAT, HINH_ANH,
    PHI_THAM_GIA, CAP_DO, LUOT_XEM, DANH_GIA_TB
) VALUES (
    N'Giải Cầu Lông Đà Nẵng Championship',
    '2025-12-01', '2025-12-10',
    GETDATE(), GETDATE(), 1,
    N'Giải đấu lớn nhất miền Trung trong năm',
    N'Nhà thi đấu Tiên Sơn',
    N'Đà Nẵng',
    'registration', 1,
    'https://images.unsplash.com/photo-1552667466-07770ae110d0?w=400',
    400000, N'Chuyên nghiệp', 2100, 4.7
);

-- Tournament 4: REGISTRATION
INSERT INTO GIAI_DAU (
    TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, TRANG_THAI, NOI_BAT, HINH_ANH,
    PHI_THAM_GIA, CAP_DO, LUOT_XEM, DANH_GIA_TB
) VALUES (
    N'Giải Sinh Viên Cần Thơ 2025',
    '2025-11-28', '2025-12-02',
    GETDATE(), GETDATE(), 1,
    N'Giải đấu dành cho sinh viên các trường đại học',
    N'Nhà thi đấu Thể dục Thể thao',
    N'Cần Thơ',
    'registration', 0,
    'https://images.unsplash.com/photo-1534258936925-c58bed479fcb?w=400',
    200000, N'Nghiệp dư', 456, 4.0
);

-- Tournament 5: UPCOMING - Featured
INSERT INTO GIAI_DAU (
    TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER,
    MO_TA, DIA_DIEM, TINH_THANH, TRANG_THAI, NOI_BAT, HINH_ANH,
    PHI_THAM_GIA, CAP_DO, LUOT_XEM, DANH_GIA_TB
) VALUES (
    N'Vietnam Open Badminton 2025',
    '2025-12-15', '2025-12-22',
    GETDATE(), GETDATE(), 1,
    N'Giải đấu quốc tế lớn nhất Việt Nam - BWF Tour Super 500',
    N'Cung Thể thao Quần Ngựa',
    N'TP. Hồ Chí Minh',
    'upcoming', 1,
    'https://images.unsplash.com/photo-1609710228159-0fa9bd7c0827?w=400',
    1000000, N'Quốc tế', 5600, 4.9
);

GO

SELECT 
    ID, TEN_GIAI, TRANG_THAI, NOI_BAT, TINH_THANH, LUOT_XEM
FROM GIAI_DAU
ORDER BY NGAY_BD DESC;
