-- =====================================================-- =====================================================

-- Sample Data for Testing Web Platform-- Sample Data for Testing Web Platform

-- Purpose: Insert test tournaments with web platform fields-- Purpose: Insert test tournaments with web platform fields

-- Date: 2025-11-18-- Date: 2025-11-17

-- Database: SQL Server-- Run this AFTER executing V1.1, V1.2, V1.3 migrations

-- Run this AFTER executing V1.1, V1.2, V1.3 migrations-- =====================================================

-- =====================================================

-- Insert sample tournaments with full web platform data

-- Insert sample tournaments with full web platform data (SQL Server syntax)INSERT INTO GIAI_DAU (

INSERT INTO GIAI_DAU (    TEN_GIAI, 

    TEN_GIAI,     NGAY_BD, 

    NGAY_BD,     NGAY_KT, 

    NGAY_KT,     NGAY_TAO, 

    NGAY_TAO,     NGAY_CAP_NHAT, 

    NGAY_CAP_NHAT,     ID_USER,

    ID_USER,    MO_TA,

    MO_TA,    DIA_DIEM,

    DIA_DIEM,    TINH_THANH,

    TINH_THANH,    QUOC_GIA,

    QUOC_GIA,    TRANG_THAI,

    TRANG_THAI,    NOI_BAT,

    NOI_BAT,    HINH_ANH,

    HINH_ANH,    LOGO,

    LOGO,    NGAY_MO_DANG_KI,

    NGAY_MO_DANG_KI,    NGAY_DONG_DANG_KI,

    NGAY_DONG_DANG_KI,    SO_LUONG_TOI_DA,

    SO_LUONG_TOI_DA,    PHI_THAM_GIA,

    PHI_THAM_GIA,    GIAI_THUONG,

    GIAI_THUONG,    DIEN_THOAI,

    DIEN_THOAI,    EMAIL,

    EMAIL,    WEBSITE,

    WEBSITE,    CAP_DO,

    CAP_DO,    THE_LOAI,

    THE_LOAI,    SAN_THI_DAU,

    SAN_THI_DAU,    QUY_DINH,

    QUY_DINH,    LUOT_XEM,

    LUOT_XEM,    DANH_GIA_TB,

    DANH_GIA_TB,    TONG_DANH_GIA

    TONG_DANH_GIA) VALUES 

) VALUES -- Tournament 1: Featured & Ongoing

-- Tournament 1: Featured & Ongoing(

(    'Giải Vô Địch Cầu Lông TP.HCM 2025',

    N'Giải Vô Địch Cầu Lông TP.HCM 2025',    DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY),

    DATEADD(DAY, -2, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY),

    DATEADD(DAY, 5, CAST(GETDATE() AS DATE)),    CURRENT_TIMESTAMP,

    GETDATE(),    CURRENT_TIMESTAMP,

    GETDATE(),    1,

    1,    'Giải đấu cầu lông chuyên nghiệp quy mô lớn nhất TP.HCM năm 2025. Quy tụ các tay vợt hàng đầu thành phố và cả nước. Giải đấu áp dụng luật thi đấu BWF quốc tế, có trọng tài quốc gia điều khiển.',

    N'Giải đấu cầu lông chuyên nghiệp quy mô lớn nhất TP.HCM năm 2025. Quy tụ các tay vợt hàng đầu thành phố và cả nước. Giải đấu áp dụng luật thi đấu BWF quốc tế, có trọng tài quốc gia điều khiển.',    'Nhà thi đấu Phan Đình Phùng',

    N'Nhà thi đấu Phan Đình Phùng',    'TP. Hồ Chí Minh',

    N'TP. Hồ Chí Minh',    'VN',

    'VN',    'ongoing',

    'ongoing',    TRUE,

    1,    '/images/tournaments/hcm-championship-2025.jpg',

    '/images/tournaments/hcm-championship-2025.jpg',    '/images/tournaments/logos/hcm-logo.png',

    '/images/tournaments/logos/hcm-logo.png',    DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY),

    DATEADD(DAY, -30, CAST(GETDATE() AS DATE)),    DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY),

    DATEADD(DAY, -7, CAST(GETDATE() AS DATE)),    128,

    128,    500000.00,

    500000.00,    'Tổng giải thưởng: 500 triệu VNĐ. Vô địch: 100 triệu. Á quân: 50 triệu. Hạng 3: 25 triệu mỗi VĐV.',

    N'Tổng giải thưởng: 500 triệu VNĐ. Vô địch: 100 triệu. Á quân: 50 triệu. Hạng 3: 25 triệu mỗi VĐV.',    '0283-8234567',

    '0283-8234567',    'giaidau@hcmbadminton.vn',

    'giaidau@hcmbadminton.vn',    'https://hcmbadminton.vn/championship-2025',

    'https://hcmbadminton.vn/championship-2025',    'professional',

    'professional',    'open',

    'open',    '10 sân thi đấu chính thức, 2 sân tập luyện. Hệ thống ánh sáng tiêu chuẩn BWF.',

    N'10 sân thi đấu chính thức, 2 sân tập luyện. Hệ thống ánh sáng tiêu chuẩn BWF.',    'Áp dụng luật BWF 2024. Vợt phải có dây. Không được dùng phụ kiện không được phép. Trang phục phải đúng quy định.',

    N'Áp dụng luật BWF 2024. Vợt phải có dây. Không được dùng phụ kiện không được phép. Trang phục phải đúng quy định.',    2547,

    2547,    4.5,

    4.5,    89

    89),

),

-- Tournament 2: Featured & Upcoming

-- Tournament 2: Featured & Upcoming(

(    'Giải Cầu Lông Hà Nội Open 2025',

    N'Giải Cầu Lông Hà Nội Open 2025',    DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY),

    DATEADD(DAY, 15, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 22 DAY),

    DATEADD(DAY, 22, CAST(GETDATE() AS DATE)),    CURRENT_TIMESTAMP,

    GETDATE(),    CURRENT_TIMESTAMP,

    GETDATE(),    1,

    1,    'Giải cầu lông mở rộng Hà Nội lần thứ 10 - một trong những giải đấu truyền thống và uy tín nhất miền Bắc. Mở rộng cho tất cả các CLB và VĐV cá nhân đăng ký tham gia.',

    N'Giải cầu lông mở rộng Hà Nội lần thứ 10 - một trong những giải đấu truyền thống và uy tín nhất miền Bắc. Mở rộng cho tất cả các CLB và VĐV cá nhân đăng ký tham gia.',    'Cung Thể Thao Quần Ngựa',

    N'Cung Thể Thao Quần Ngựa',    'Hà Nội',

    N'Hà Nội',    'VN',

    'VN',    'registration',

    'registration',    TRUE,

    1,    '/images/tournaments/hanoi-open-2025.jpg',

    '/images/tournaments/hanoi-open-2025.jpg',    '/images/tournaments/logos/hanoi-logo.png',

    '/images/tournaments/logos/hanoi-logo.png',    DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY),

    DATEADD(DAY, -5, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY),

    DATEADD(DAY, 10, CAST(GETDATE() AS DATE)),    96,

    96,    300000.00,

    300000.00,    'Tổng giải thưởng: 300 triệu VNĐ. Vô địch các nội dung: 40 triệu. Á quân: 20 triệu.',

    N'Tổng giải thưởng: 300 triệu VNĐ. Vô địch các nội dung: 40 triệu. Á quân: 20 triệu.',    '024-37466789',

    '024-37466789',    'contact@hanoiopen.vn',

    'contact@hanoiopen.vn',    'https://hanoiopen.vn',

    'https://hanoiopen.vn',    'amateur',

    'amateur',    'open',

    'open',    '8 sân thi đấu. Sân gỗ chuyên dụng. Hệ thống điều hòa và ánh sáng tốt.',

    N'8 sân thi đấu. Sân gỗ chuyên dụng. Hệ thống điều hòa và ánh sáng tốt.',    'Tuân thủ luật BWF. VĐV phải có CMND/CCCD. Đăng ký qua website hoặc trực tiếp tại văn phòng BTC.',

    N'Tuân thủ luật BWF. VĐV phải có CMND/CCCD. Đăng ký qua website hoặc trực tiếp tại văn phòng BTC.',    1823,

    1823,    4.7,

    4.7,    56

    56),

),

-- Tournament 3: Upcoming (Not featured)

-- Tournament 3: Upcoming (Not featured)(

(    'Giải Cầu Lông Cúp Xuân Đà Nẵng',

    N'Giải Cầu Lông Cúp Xuân Đà Nẵng',    DATE_ADD(CURRENT_DATE, INTERVAL 40 DAY),

    DATEADD(DAY, 40, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY),

    DATEADD(DAY, 45, CAST(GETDATE() AS DATE)),    CURRENT_TIMESTAMP,

    GETDATE(),    CURRENT_TIMESTAMP,

    GETDATE(),    1,

    1,    'Giải cầu lông truyền thống chào mừng Tết Nguyên Đán. Dành cho các VĐV và CLB tại Đà Nẵng và các tỉnh lân cận.',

    N'Giải cầu lông truyền thống chào mừng Tết Nguyên Đán. Dành cho các VĐV và CLB tại Đà Nẵng và các tỉnh lân cận.',    'Nhà thi đấu Tiên Sơn',

    N'Nhà thi đấu Tiên Sơn',    'Đà Nẵng',

    N'Đà Nẵng',    'VN',

    'VN',    'upcoming',

    'upcoming',    FALSE,

    0,    '/images/tournaments/danang-spring-cup.jpg',

    '/images/tournaments/danang-spring-cup.jpg',    '/images/tournaments/logos/danang-logo.png',

    '/images/tournaments/logos/danang-logo.png',    DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY),

    DATEADD(DAY, 5, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 35 DAY),

    DATEADD(DAY, 35, CAST(GETDATE() AS DATE)),    64,

    64,    200000.00,

    200000.00,    'Tổng giải thưởng: 150 triệu VNĐ. Vô địch: 25 triệu. Á quân: 15 triệu. Hạng 3: 10 triệu.',

    N'Tổng giải thưởng: 150 triệu VNĐ. Vô địch: 25 triệu. Á quân: 15 triệu. Hạng 3: 10 triệu.',    '0236-3654321',

    '0236-3654321',    'info@danangbadminton.vn',

    'info@danangbadminton.vn',    NULL,

    NULL,    'amateur',

    'amateur',    'open',

    'open',    '6 sân thi đấu chính. Sàn nhựa chuyên dụng.',

    N'6 sân thi đấu chính. Sàn nhựa chuyên dụng.',    'VĐV phải thuộc các CLB đã đăng ký. Không giới hạn độ tuổi.',

    N'VĐV phải thuộc các CLB đã đăng ký. Không giới hạn độ tuổi.',    456,

    456,    NULL,

    NULL,    0

    0),

),

-- Tournament 4: Registration Open

-- Tournament 4: Registration Open(

(    'Giải Cầu Lông Trẻ TP.HCM - U17',

    N'Giải Cầu Lông Trẻ TP.HCM - U17',    DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY),

    DATEADD(DAY, 25, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),

    DATEADD(DAY, 30, CAST(GETDATE() AS DATE)),    CURRENT_TIMESTAMP,

    GETDATE(),    CURRENT_TIMESTAMP,

    GETDATE(),    1,

    1,    'Giải cầu lông dành riêng cho lứa tuổi U17 (dưới 17 tuổi). Nhằm phát hiện và bồi dưỡng tài năng trẻ cầu lông TP.HCM.',

    N'Giải cầu lông dành riêng cho lứa tuổi U17 (dưới 17 tuổi). Nhằm phát hiện và bồi dưỡng tài năng trẻ cầu lông TP.HCM.',    'Nhà thi đấu Đa Năng Rạch Miễu',

    N'Nhà thi đấu Đa Năng Rạch Miễu',    'TP. Hồ Chí Minh',

    N'TP. Hồ Chí Minh',    'VN',

    'VN',    'registration',

    'registration',    TRUE,

    0,    '/images/tournaments/hcm-u17.jpg',

    '/images/tournaments/hcm-u17-2025.jpg',    '/images/tournaments/logos/youth-logo.png',

    '/images/tournaments/logos/hcm-youth-logo.png',    CURRENT_DATE,

    DATEADD(DAY, -2, CAST(GETDATE() AS DATE)),    DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY),

    DATEADD(DAY, 20, CAST(GETDATE() AS DATE)),    48,

    48,    100000.00,

    150000.00,    'Tổng giải thưởng: 100 triệu VNĐ. Vô địch: 15 triệu. Á quân: 10 triệu. Hạng 3: 7 triệu.',

    N'Tổng giải thưởng: 80 triệu VNĐ. Vô địch: 15 triệu. Á quân: 10 triệu. Hạng 3: 5 triệu.',    '0283-9876543',

    '0283-9876543',    'youth@hcmbadminton.vn',

    'youth@hcmbadminton.vn',    'https://hcmbadminton.vn/u17-championship',

    'https://hcmbadminton.vn/youth',    'youth',

    'youth',    'invitational',

    'age_group',    '5 sân thi đấu. Thiết bị tiêu chuẩn quốc gia.',

    N'4 sân thi đấu. Sàn nhựa. Phù hợp cho lứa tuổi trẻ.',    'Chỉ dành cho VĐV sinh năm 2008 trở về sau. Phải có giấy xác nhận từ trường hoặc CLB.',

    N'Chỉ dành cho VĐV U17 (sinh từ 2008 trở về sau). Phải có giấy xác nhận của CLB hoặc trường học.',    892,

    823,    4.8,

    4.2,    34

    34),

),

-- Tournament 5: Completed

-- Tournament 5: Completed(

(    'Giải Cầu Lông Doanh Nhân TP.HCM 2024',

    N'Giải Cầu Lông Cúp Các CLB TP.HCM 2024',    DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY),

    DATEADD(DAY, -45, CAST(GETDATE() AS DATE)),    DATE_SUB(CURRENT_DATE, INTERVAL 55 DAY),

    DATEADD(DAY, -38, CAST(GETDATE() AS DATE)),    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY),

    DATEADD(DAY, -50, GETDATE()),    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 55 DAY),

    GETDATE(),    1,

    1,    'Giải cầu lông giao hữu dành cho các doanh nhân và CEO của các doanh nghiệp tại TP.HCM. Mang tính chất kết nối cộng đồng kinh doanh.',

    N'Giải đấu giao hữu giữa các CLB cầu lông tại TP.HCM. Tạo sân chơi để các CLB gặp gỡ, giao lưu và nâng cao trình độ.',    'CLB Cầu Lông Sài Gòn',

    N'Nhà thi đấu Nguyễn Du',    'TP. Hồ Chí Minh',

    N'TP. Hồ Chí Minh',    'VN',

    'VN',    'completed',

    'completed',    FALSE,

    0,    '/images/tournaments/business-cup-2024.jpg',

    '/images/tournaments/club-cup-2024.jpg',    '/images/tournaments/logos/business-logo.png',

    '/images/tournaments/logos/club-cup-logo.png',    DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY),

    DATEADD(DAY, -70, CAST(GETDATE() AS DATE)),    DATE_SUB(CURRENT_DATE, INTERVAL 65 DAY),

    DATEADD(DAY, -50, CAST(GETDATE() AS DATE)),    32,

    80,    1000000.00,

    250000.00,    'Tổng giải thưởng: 200 triệu VNĐ. Toàn bộ giải thưởng sẽ được quyên góp cho quỹ từ thiện.',

    N'Tổng giải thưởng: 120 triệu VNĐ. Vô địch đồng đội: 40 triệu. Á quân: 25 triệu.',    '0283-7654321',

    '0283-7654321',    'business@sgbadminton.vn',

    'clubs@hcmbadminton.vn',    NULL,

    NULL,    'amateur',

    'club',    'invitational',

    'team',    '4 sân VIP. Đầy đủ tiện nghi.',

    N'6 sân thi đấu. Sân nhựa chuyên dụng.',    'Chỉ dành cho doanh nhân có thư mời. Phí tham gia được dùng cho từ thiện.',

    N'Chỉ dành cho các CLB đã đăng ký chính thức. Mỗi CLB tối đa 12 VĐV.',    1245,

    3456,    4.6,

    4.8,    28

    127);

);

GOCOMMIT;



-- Insert sample tournament gallery images-- =====================================================

INSERT INTO TOURNAMENT_GALLERY (ID_GIAI, LOAI, URL, TIEU_DE, MO_TA, THU_TU)-- Insert sample gallery images for tournaments

VALUES -- =====================================================

    (1, 'image', '/images/gallery/hcm-2025-01.jpg', N'Lễ khai mạc', N'Lễ khai mạc giải đấu với sự tham gia của 500 VĐV', 1),

    (1, 'image', '/images/gallery/hcm-2025-02.jpg', N'Trận chung kết đôi nam', N'Trận chung kết đôi nam đỉnh cao', 2),INSERT INTO TOURNAMENT_GALLERY (ID_GIAI, LOAI, URL, TIEU_DE, MO_TA, THU_TU) VALUES

    (1, 'video', '/videos/highlights/hcm-2025-final.mp4', N'Highlights chung kết', N'Video tổng hợp những pha bóng đẹp', 3),-- Gallery for Tournament 1

    (2, 'image', '/images/gallery/hanoi-open-poster.jpg', N'Poster giải đấu', N'Poster chính thức Hà Nội Open 2025', 1),(1, 'image', '/images/gallery/hcm-2025-ceremony.jpg', 'Lễ khai mạc', 'Lễ khai mạc hoành tráng với sự tham gia của 500+ VĐV', 1),

    (5, 'image', '/images/gallery/club-cup-winner.jpg', N'Đội vô địch', N'CLB vô địch Cúp Các CLB 2024', 1);(1, 'image', '/images/gallery/hcm-2025-match-1.jpg', 'Trận chung kết đơn nam', 'Trận đấu gay cấn giữa 2 tay vợt hàng đầu', 2),

GO(1, 'video', 'https://youtube.com/watch?v=sample1', 'Highlight Day 1', 'Những pha bóng đẹp ngày thi đấu đầu tiên', 3),

(1, 'image', '/images/gallery/hcm-2025-crowd.jpg', 'Khán giả cổ vũ', 'Không khí sôi động tại nhà thi đấu', 4),

-- =====================================================

-- Verification Queries-- Gallery for Tournament 2

-- =====================================================(2, 'image', '/images/gallery/hanoi-open-poster.jpg', 'Poster chính thức', 'Poster quảng bá giải đấu Hà Nội Open 2025', 1),

(2, 'image', '/images/gallery/hanoi-open-venue.jpg', 'Cung thể thao', 'Địa điểm tổ chức - Cung Thể Thao Quần Ngựa', 2),

-- Check inserted tournaments

-- SELECT ID, TEN_GIAI, TRANG_THAI, NOI_BAT, NGAY_BD, NGAY_KT FROM GIAI_DAU ORDER BY ID DESC;-- Gallery for Tournament 4

(4, 'image', '/images/gallery/u17-training.jpg', 'Buổi tập', 'VĐV U17 tập luyện chuẩn bị cho giải', 1),

-- Check tournament gallery(4, 'document', '/documents/u17-rules.pdf', 'Thể lệ giải U17', 'Thể lệ chi tiết giải đấu U17', 2);

-- SELECT g.ID, gd.TEN_GIAI, g.LOAI, g.TIEU_DE 

-- FROM TOURNAMENT_GALLERY g COMMIT;

-- JOIN GIAI_DAU gd ON g.ID_GIAI = gd.ID;

-- =====================================================

-- Count by status-- Verification Queries

-- SELECT TRANG_THAI, COUNT(*) as SO_LUONG FROM GIAI_DAU GROUP BY TRANG_THAI;-- =====================================================


-- Check tournaments
SELECT 
    ID, 
    TEN_GIAI, 
    TRANG_THAI, 
    NOI_BAT, 
    TINH_THANH, 
    NGAY_BD, 
    NGAY_KT,
    LUOT_XEM,
    DANH_GIA_TB
FROM GIAI_DAU
ORDER BY NGAY_BD DESC;

-- Check tournament statuses distribution
SELECT 
    TRANG_THAI,
    COUNT(*) as SO_LUONG
FROM GIAI_DAU
GROUP BY TRANG_THAI;

-- Check featured tournaments
SELECT 
    TEN_GIAI,
    TRANG_THAI,
    TINH_THANH
FROM GIAI_DAU
WHERE NOI_BAT = TRUE
ORDER BY NGAY_BD;

-- Check gallery items
SELECT 
    g.ID,
    t.TEN_GIAI,
    g.LOAI,
    g.TIEU_DE
FROM TOURNAMENT_GALLERY g
JOIN GIAI_DAU t ON g.ID_GIAI = t.ID
ORDER BY g.ID_GIAI, g.THU_TU;

-- =====================================================
-- Success! You now have sample tournament data
-- =====================================================
