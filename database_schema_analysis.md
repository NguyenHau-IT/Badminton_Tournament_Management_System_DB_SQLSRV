# Phân tích Database Schema - Giải Cầu Lông

## Tổng quan
Database `giai_cau_long` được thiết kế cho hệ thống quản lý giải đấu cầu lông với MySQL 8.0+, hỗ trợ đầy đủ các chức năng từ đăng ký đến tổng hợp kết quả.

## Cấu trúc Database

### 1. **Bảng Cơ bản**

#### `clb` - Câu lạc bộ
- `clb_id`: ID tự tăng
- `ten`: Tên câu lạc bộ (120 ký tự)
- `thanh_pho`: Thành phố
- `quoc_gia`: Mã quốc gia (mặc định 'VN')

#### `giai` - Giải đấu
- `giai_id`: ID tự tăng
- `ten`: Tên giải đấu (160 ký tự)
- `cap_do`: Cấp độ (Mở rộng, Tỉnh, Toàn quốc)
- `dia_diem`: Địa điểm tổ chức
- `ngay_bd`, `ngay_kt`: Ngày bắt đầu/kết thúc
- `created_at`: Timestamp tạo

#### `san` - Sân đấu
- `san_id`: ID tự tăng
- `giai_id`: Liên kết với giải đấu
- `ten`: Tên sân (Sân 1, Sân 2...)

### 2. **Bảng Vận động viên**

#### `vdv` - Vận động viên
- `vdv_id`: ID tự tăng
- `ho`, `ten`: Họ và tên
- `gioi_tinh`: ENUM('M','F')
- `ngay_sinh`: Ngày sinh
- `clb_id`: Liên kết với câu lạc bộ
- `quoc_gia`: Mã quốc gia
- `sdt`, `email`: Thông tin liên lạc
- `xep_hang`: Xếp hạng hiện tại

### 3. **Bảng Sự kiện & Đăng ký**

#### `su_kien` - Sự kiện trong giải
- `su_kien_id`: ID tự tăng
- `giai_id`: Liên kết với giải đấu
- `ma`: ENUM('DNM','DNu','DoNam','DoNu','DoNamNu')
- `ten`: Tên sự kiện
- `nhom_tuoi`: Nhóm tuổi
- `trinh_do`: Trình độ
- `so_luong`: Số lượng tham gia (mặc định 64)
- `luat_thi_dau`: Luật thi đấu (mặc định '3x21 rally')
- `loai_bang`: ENUM('LOAI_TRUC_TIEP','VONG_TRON')

#### `capdoi` - Cặp đôi
- `capdoi_id`: ID tự tăng
- `su_kien_id`: Liên kết với sự kiện
- `vdv1_id`, `vdv2_id`: Hai vận động viên
- Constraint: `vdv1_id <> vdv2_id`

#### `dangky` - Đăng ký
- `dangky_id`: ID tự tăng
- `su_kien_id`: Liên kết với sự kiện
- `loai`: ENUM('DON','DOI')
- `vdv_id`: VĐV đơn (NULL nếu đôi)
- `capdoi_id`: Cặp đôi (NULL nếu đơn)
- `hat_giong`: Hạt giống
- `trang_thai`: ENUM('CHO','DUYET','HUY')

### 4. **Bảng Bốc thăm & Sơ đồ**

#### `boc_tham` - Bốc thăm
- `boc_tham_id`: ID tự tăng
- `su_kien_id`: Liên kết với sự kiện
- `ten`: Tên bảng (mặc định 'Bảng chính')
- `kieu`: ENUM('AUTO','TAY')

#### `vi_tri` - Vị trí trong bảng đấu
- `vitri_id`: ID tự tăng
- `boc_tham_id`: Liên kết với bốc thăm
- `so_thu_tu`: Số thứ tự trong bảng
- `dangky_id`: Đăng ký tại vị trí này
- `hat_giong`: Hạt giống
- `bye`: BOOLEAN (có bye hay không)

### 5. **Bảng Thi đấu & Kết quả**

#### `tran` - Trận đấu
- `tran_id`: ID tự tăng
- `boc_tham_id`: Liên kết với bốc thăm
- `vong`: Vòng đấu
- `so_tran`: Số trận
- `vitri_tren`, `vitri_duoi`: Vị trí trên/dưới
- `dangky_tren_id`, `dangky_duoi_id`: Đăng ký trên/dưới
- `thang_id`: Đăng ký thắng
- `gio_bd`: Giờ bắt đầu
- `san_id`: Sân đấu
- `trang_thai`: ENUM('CHO','DANG','WO','XONG')

#### `ty_so` - Tỷ số game
- `tyso_id`: ID tự tăng
- `tran_id`: Liên kết với trận đấu
- `so_game`: Số game
- `diem_tren`, `diem_duoi`: Điểm trên/dưới

## Đặc điểm kỹ thuật

### 1. **Engine & Character Set**
- Engine: InnoDB (hỗ trợ transaction, foreign key)
- Character Set: utf8mb4
- Collation: utf8mb4_unicode_ci

### 2. **Constraints & Indexes**
- Primary Keys: AUTO_INCREMENT
- Foreign Keys: CASCADE/SET NULL/RESTRICT phù hợp
- Unique Keys: Ngăn duplicate đăng ký
- Check Constraints: Đảm bảo logic nghiệp vụ

### 3. **ENUM Values**
- Giới tính: 'M','F'
- Loại sự kiện: 'DNM','DNu','DoNam','DoNu','DoNamNu'
- Trạng thái: 'CHO','DUYET','HUY','DANG','WO','XONG'

## Workflow nghiệp vụ

1. **Tạo giải đấu** → `giai`
2. **Tạo sân đấu** → `san`
3. **Tạo sự kiện** → `su_kien`
4. **Đăng ký VĐV** → `vdv`, `capdoi`, `dangky`
5. **Bốc thăm** → `boc_tham`, `vi_tri`
6. **Tạo sơ đồ** → `tran`
7. **Thi đấu** → `ty_so`
8. **Tổng hợp kết quả** → Query từ các bảng

## So sánh với schema cũ

Schema mới này hoàn toàn khác với schema cũ trong code:
- **Cũ**: `su_kien`, `vdv`, `dangky`, `capdoi` (đơn giản)
- **Mới**: Thêm `giai`, `san`, `boc_tham`, `vi_tri`, `tran`, `ty_so` (đầy đủ)

Cần cập nhật toàn bộ code để tương thích với schema mới này.
