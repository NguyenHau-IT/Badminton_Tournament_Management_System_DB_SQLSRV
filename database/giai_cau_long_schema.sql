-- Database Giai Cau Long (MySQL 8.0+)
-- Cac chuc nang: tao giai, tao su kien, them van dong vien, dang ky, boc tham, tao so do thi dau, xep lich, ghi ket qua, tong hop ket qua.

CREATE DATABASE IF NOT EXISTS giai_cau_long
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE giai_cau_long;

-- Cau lac bo
CREATE TABLE clb (
  clb_id INT AUTO_INCREMENT PRIMARY KEY,
  ten VARCHAR(120) NOT NULL,
  thanh_pho VARCHAR(80),
  quoc_gia CHAR(2) DEFAULT 'VN'
) ENGINE=InnoDB;

-- Giai dau
CREATE TABLE giai (
  giai_id INT AUTO_INCREMENT PRIMARY KEY,
  ten VARCHAR(160) NOT NULL,
  cap_do VARCHAR(40), -- Vi du: Mo rong, Tinh, Toan quoc
  dia_diem VARCHAR(160),
  thanh_pho VARCHAR(80),
  ngay_bd DATE NOT NULL,
  ngay_kt DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- San dau
CREATE TABLE san (
  san_id INT AUTO_INCREMENT PRIMARY KEY,
  giai_id INT NOT NULL,
  ten VARCHAR(40) NOT NULL, -- Vi du: San 1
  FOREIGN KEY (giai_id) REFERENCES giai(giai_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- Van dong vien
CREATE TABLE vdv (
  vdv_id INT AUTO_INCREMENT PRIMARY KEY,
  ho VARCHAR(80) NOT NULL,
  ten VARCHAR(80) NOT NULL,
  gioi_tinh ENUM('M','F') NOT NULL,
  ngay_sinh DATE,
  clb_id INT,
  quoc_gia CHAR(2) DEFAULT 'VN',
  sdt VARCHAR(32),
  email VARCHAR(160),
  xep_hang INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (clb_id) REFERENCES clb(clb_id)
    ON DELETE SET NULL
) ENGINE=InnoDB;

-- Su kien trong giai
CREATE TABLE su_kien (
  su_kien_id INT AUTO_INCREMENT PRIMARY KEY,
  giai_id INT NOT NULL,
  ma ENUM('DNM','DNu','DoNam','DoNu','DoNamNu') NOT NULL,
  ten VARCHAR(160) NOT NULL,
  nhom_tuoi VARCHAR(40),
  trinh_do VARCHAR(40),
  so_luong INT DEFAULT 64,
  luat_thi_dau VARCHAR(60) DEFAULT '3x21 rally',
  loai_bang ENUM('LOAI_TRUC_TIEP','VONG_TRON') DEFAULT 'LOAI_TRUC_TIEP',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_su_kien (giai_id, ma, nhom_tuoi, trinh_do),
  FOREIGN KEY (giai_id) REFERENCES giai(giai_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- Cap doi cho noi dung doi
CREATE TABLE capdoi (
  capdoi_id INT AUTO_INCREMENT PRIMARY KEY,
  su_kien_id INT NOT NULL,
  vdv1_id INT NOT NULL,
  vdv2_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_capdoi (su_kien_id, vdv1_id, vdv2_id),
  FOREIGN KEY (su_kien_id) REFERENCES su_kien(su_kien_id) ON DELETE CASCADE,
  FOREIGN KEY (vdv1_id) REFERENCES vdv(vdv_id) ON DELETE RESTRICT,
  FOREIGN KEY (vdv2_id) REFERENCES vdv(vdv_id) ON DELETE RESTRICT,
  CHECK (vdv1_id <> vdv2_id)
) ENGINE=InnoDB;

-- Dang ky
CREATE TABLE dangky (
  dangky_id INT AUTO_INCREMENT PRIMARY KEY,
  su_kien_id INT NOT NULL,
  loai ENUM('DON','DOI') NOT NULL,
  vdv_id INT NULL,
  capdoi_id INT NULL,
  hat_giong INT NULL,
  trang_thai ENUM('CHO','DUYET','HUY') DEFAULT 'DUYET',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_dangky_don (su_kien_id, vdv_id),
  UNIQUE KEY uq_dangky_doi (su_kien_id, capdoi_id),
  FOREIGN KEY (su_kien_id) REFERENCES su_kien(su_kien_id) ON DELETE CASCADE,
  FOREIGN KEY (vdv_id) REFERENCES vdv(vdv_id) ON DELETE RESTRICT,
  FOREIGN KEY (capdoi_id) REFERENCES capdoi(capdoi_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Boc tham (draw)
CREATE TABLE boc_tham (
  boc_tham_id INT AUTO_INCREMENT PRIMARY KEY,
  su_kien_id INT NOT NULL,
  ten VARCHAR(120) DEFAULT 'Bang chinh',
  kieu ENUM('AUTO','TAY') DEFAULT 'AUTO',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (su_kien_id) REFERENCES su_kien(su_kien_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Vi tri trong bang dau
CREATE TABLE vi_tri (
  vitri_id INT AUTO_INCREMENT PRIMARY KEY,
  boc_tham_id INT NOT NULL,
  so_thu_tu INT NOT NULL,
  dangky_id INT NULL,
  hat_giong INT NULL,
  bye BOOLEAN DEFAULT FALSE,
  UNIQUE KEY uq_vitri (boc_tham_id, so_thu_tu),
  FOREIGN KEY (boc_tham_id) REFERENCES boc_tham(boc_tham_id) ON DELETE CASCADE,
  FOREIGN KEY (dangky_id) REFERENCES dangky(dangky_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Tran dau
CREATE TABLE tran (
  tran_id INT AUTO_INCREMENT PRIMARY KEY,
  boc_tham_id INT NOT NULL,
  vong INT NOT NULL,
  so_tran INT NOT NULL,
  vitri_tren INT NOT NULL,
  vitri_duoi INT NOT NULL,
  dangky_tren_id INT NULL,
  dangky_duoi_id INT NULL,
  thang_id INT NULL,
  gio_bd DATETIME NULL,
  san_id INT NULL,
  trang_thai ENUM('CHO','DANG','WO','XONG') DEFAULT 'CHO',
  UNIQUE KEY uq_tran (boc_tham_id, vong, so_tran),
  FOREIGN KEY (boc_tham_id) REFERENCES boc_tham(boc_tham_id) ON DELETE CASCADE,
  FOREIGN KEY (dangky_tren_id) REFERENCES dangky(dangky_id) ON DELETE SET NULL,
  FOREIGN KEY (dangky_duoi_id) REFERENCES dangky(dangky_id) ON DELETE SET NULL,
  FOREIGN KEY (thang_id) REFERENCES dangky(dangky_id) ON DELETE SET NULL,
  FOREIGN KEY (san_id) REFERENCES san(san_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Ty so game
CREATE TABLE ty_so (
  tyso_id INT AUTO_INCREMENT PRIMARY KEY,
  tran_id INT NOT NULL,
  so_game INT NOT NULL,
  diem_tren TINYINT NOT NULL,
  diem_duoi TINYINT NOT NULL,
  UNIQUE KEY uq_tiso (tran_id, so_game),
  FOREIGN KEY (tran_id) REFERENCES tran(tran_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Sample data
INSERT INTO clb (ten, thanh_pho) VALUES 
('CLB Cầu Lông Hà Nội', 'Hà Nội'),
('CLB Cầu Lông TP.HCM', 'TP.HCM'),
('CLB Cầu Lông Đà Nẵng', 'Đà Nẵng');

INSERT INTO giai (ten, cap_do, dia_diem, thanh_pho, ngay_bd, ngay_kt) VALUES 
('Giải Cầu Lông Mở Rộng 2025', 'Mở rộng', 'Nhà thi đấu Quận 1', 'TP.HCM', '2025-01-15', '2025-01-20');

INSERT INTO san (giai_id, ten) VALUES 
(1, 'Sân 1'),
(1, 'Sân 2'),
(1, 'Sân 3'),
(1, 'Sân 4');
