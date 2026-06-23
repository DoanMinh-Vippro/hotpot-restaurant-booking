CREATE DATABASE [hotpot_restaurant_booking3];
GO
USE [hotpot_restaurant_booking3];
GO

-- 1. B?ng ??c l?p (không khóa ngo?i)
CREATE TABLE [dbo].[ChucVu] (
    [id_chuc_vu] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_chuc_vu] VARCHAR(15),
    [ten_chuc_vu] NVARCHAR(10)
);

CREATE TABLE [dbo].[DanhMuc] (
    [id_danh_muc] INT IDENTITY(1,1) PRIMARY KEY,
    [loai_danh_muc] NVARCHAR(50),
    [mo_ta] NVARCHAR(100)
);

CREATE TABLE [dbo].[KhuVuc] (
    [id_khu_vuc] INT IDENTITY(1,1) PRIMARY KEY,
    [mo_ta] NVARCHAR(100),
    [ten_khu_vuc] NVARCHAR(30),
    [trang_thai] TINYINT
);

CREATE TABLE [dbo].[DotGiamGia] (
    [id_dot_giam_gia] INT IDENTITY(1,1) PRIMARY KEY,
    [ten_chuong_trinh] NVARCHAR(40),
    [ngay_bat_dau] DATE,
    [ngay_ket_thuc] DATE
);

CREATE TABLE [dbo].[Combo] (
    [id_combo] INT IDENTITY(1,1) PRIMARY KEY,
    [ten_combo] NVARCHAR(50),
    [gia_combo] DECIMAL(18, 2),
    [hinh_anh] VARCHAR(500),
    [trang_thai] TINYINT
);

CREATE TABLE [dbo].[GiamGia] (
    [id_giam_gia] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_giam_gia] VARCHAR(20),
    [ngay_tao] DATE,
    [ngay_ket_thuc] DATE,
    [dieu_kien_su_dung] NVARCHAR(100),
    [gia_tri_giam_toi_da] DECIMAL(18, 2),
    [gia_tri_giam] DECIMAL(18, 2),
    [loai_giam] VARCHAR(20),
    [so_luong_ma_giam_gia] INT,
    [so_luong_dung] INT,
    [trang_thai] TINYINT
);

-- 2. B?ng có khóa ngo?i (?ã s?p x?p th? t? ?? t?o thành công)
CREATE TABLE [dbo].[TaiKhoan] (
    [id_tai_khoan] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_tai_khoan] VARCHAR(20) UNIQUE,
    [ten_dang_nhap] VARCHAR(20) UNIQUE,
    [mat_khau] VARCHAR(255),
    [trang_thai] BIT,
    [id_chuc_vu] INT NOT NULL,
    FOREIGN KEY ([id_chuc_vu]) REFERENCES [dbo].[ChucVu]([id_chuc_vu])
);

CREATE TABLE [dbo].[Mon] (
    [id_mon] INT IDENTITY(1,1) PRIMARY KEY,
    [ten_mon] NVARCHAR(30),
    [don_gia_hien_tai] DECIMAL(18, 2),
    [id_danh_muc] INT,
    [trang_thai] TINYINT DEFAULT 0,
    FOREIGN KEY ([id_danh_muc]) REFERENCES [dbo].[DanhMuc]([id_danh_muc])
);

CREATE TABLE [dbo].[Ban] (
    [id_ban] INT IDENTITY(1,1) PRIMARY KEY,
    [loai_ban] VARCHAR(20),
    [so_luong_ban] INT,
    [id_khu_vuc] INT,
    [trang_thai] TINYINT,
    FOREIGN KEY ([id_khu_vuc]) REFERENCES [dbo].[KhuVuc]([id_khu_vuc])
);

CREATE TABLE [dbo].[KhachHang] (
    [id_khach_hang] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_khach_hang] VARCHAR(15) UNIQUE,
    [ten_khach_hang] NVARCHAR(25),
    [gioi_tinh] BIT,
    [dia_chi] NVARCHAR(50),
    [so_dien_thoai] VARCHAR(10) UNIQUE,
    [id_tai_khoan] INT,
    [email] VARCHAR(50),
    [trang_thai] BIT,
    FOREIGN KEY ([id_tai_khoan]) REFERENCES [dbo].[TaiKhoan]([id_tai_khoan])
);

CREATE TABLE [dbo].[NhanVien] (
    [id_nhan_vien] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_nhan_vien] VARCHAR(15) UNIQUE,
    [ten_nhan_vien] NVARCHAR(25),
    [gioi_tinh] BIT,
    [so_dien_thoai] VARCHAR(10),
    [email] VARCHAR(30),
    [id_chuc_vu] INT,
    [id_tai_khoan] INT,
    [dia_chi] NVARCHAR(50),
    [trang_thai] BIT,
    FOREIGN KEY ([id_chuc_vu]) REFERENCES [dbo].[ChucVu]([id_chuc_vu]),
    FOREIGN KEY ([id_tai_khoan]) REFERENCES [dbo].[TaiKhoan]([id_tai_khoan])
);

CREATE TABLE [dbo].[ChiTietCombo] (
    [id_chi_tiet_combo] INT IDENTITY(1,1) PRIMARY KEY,
    [so_luong] INT,
    [id_mon] INT,
    [id_combo] INT,
    [mo_ta] NVARCHAR(100),
    FOREIGN KEY ([id_mon]) REFERENCES [dbo].[Mon]([id_mon]),
    FOREIGN KEY ([id_combo]) REFERENCES [dbo].[Combo]([id_combo])
);

CREATE TABLE [dbo].[ChiTietGiamGiaMon] (
    [id_chi_tiet_giam_gia_mon] INT IDENTITY(1,1) PRIMARY KEY,
    [id_dot_giam_gia] INT,
    [id_mon] INT,
    [muc_giam] DECIMAL(18, 2),
    FOREIGN KEY ([id_dot_giam_gia]) REFERENCES [dbo].[DotGiamGia]([id_dot_giam_gia]),
    FOREIGN KEY ([id_mon]) REFERENCES [dbo].[Mon]([id_mon])
);

CREATE TABLE [dbo].[DatBan] (
    [id_dat_ban] INT IDENTITY(1,1) PRIMARY KEY,
    [id_ban] INT,
    [id_khach_hang] INT,
    [ngay_dat] DATE,
    [gio_dat] TIME,
    [sdt_khach_hang] VARCHAR(10),
    [so_nguoi] INT,
    [trang_thai] TINYINT,
    [ghi_chu] NVARCHAR(100),
    [thoi_gian_den_du_kien] DATETIME2,
    [so_tien_coc] DECIMAL(18, 2),
    [trang_thai_coc] TINYINT,
    [phuong_thuc_thanh_toan] TINYINT,
    [id_combo] INT,
    FOREIGN KEY ([id_ban]) REFERENCES [dbo].[Ban]([id_ban]),
    FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[KhachHang]([id_khach_hang]),
    CONSTRAINT [FK_DatBan_Combo] FOREIGN KEY ([id_combo]) REFERENCES [dbo].[Combo]([id_combo])
);

CREATE TABLE [dbo].[HoaDon] (
    [id_hoa_don] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_hoa_don] VARCHAR(20) UNIQUE,
    [ma_giao_dich] VARCHAR(50),
    [trang_thai_hoa_don] TINYINT,
    [sdt_khach_hang] VARCHAR(10),
    [tien_truoc_giam] DECIMAL(18, 2),
    [tien_coc] DECIMAL(18, 2),
    [tien_giam_gia] DECIMAL(18, 2),
    [tong_tien] DECIMAL(18, 2),
    [thoi_gian_xuat] DATETIME2,
    [id_ban] INT,
    [id_dat_ban] INT,
    [id_giam_gia] INT,
    [id_khach_hang] INT,
    [id_nhan_vien] INT,
    [trang_thai_thanh_toan] TINYINT,
    [phuong_thuc_thanh_toan] TINYINT,
    FOREIGN KEY ([id_ban]) REFERENCES [dbo].[Ban]([id_ban]),
    FOREIGN KEY ([id_dat_ban]) REFERENCES [dbo].[DatBan]([id_dat_ban]),
    FOREIGN KEY ([id_giam_gia]) REFERENCES [dbo].[GiamGia]([id_giam_gia]),
    FOREIGN KEY ([id_khach_hang]) REFERENCES [dbo].[KhachHang]([id_khach_hang]),
    FOREIGN KEY ([id_nhan_vien]) REFERENCES [dbo].[NhanVien]([id_nhan_vien])
);

CREATE TABLE [dbo].[HoaDonChiTiet] (
    [id_hoa_don_chi_tiet] INT IDENTITY(1,1) PRIMARY KEY,
    [ma_hoa_don_chi_tiet] VARCHAR(20) UNIQUE,
    [id_mon] INT,
    [id_combo] INT,
    [id_hoa_don] INT,
    [so_luong] INT,
    [gia_ban_tai_thoi_dien] DECIMAL(18, 2),
    [tien_giam_gia_mon] DECIMAL(18, 2),
    [thanh_tien] DECIMAL(18, 2),
    FOREIGN KEY ([id_mon]) REFERENCES [dbo].[Mon]([id_mon]),
    FOREIGN KEY ([id_combo]) REFERENCES [dbo].[Combo]([id_combo]),
    FOREIGN KEY ([id_hoa_don]) REFERENCES [dbo].[HoaDon]([id_hoa_don])
);

-- D? li?u b?ng ??c l?p
INSERT INTO [dbo].[ChucVu] (ma_chuc_vu, ten_chuc_vu) VALUES ('CV01', N'ADMIN'), ('CV02', N'STAFF'), ('CV03', N'USER');
INSERT INTO [dbo].[DanhMuc] (loai_danh_muc, mo_ta) VALUES (N'L?u', N'Các lo?i l?u'), (N'?? nhúng', N'Th?t, rau, n?m'), (N'?? u?ng', N'?? u?ng có gas');
INSERT INTO [dbo].[KhuVuc] (mo_ta, ten_khu_vuc, trang_thai) VALUES (N'S?nh chính', N'T?ng 1', 1), (N'Phòng VIP', N'T?ng 2', 1);
INSERT INTO [dbo].[DotGiamGia] (ten_chuong_trinh, ngay_bat_dau, ngay_ket_thuc) VALUES (N'Khuy?n mãi hè', '2026-06-01', '2026-07-01');
INSERT INTO [dbo].[Combo] (ten_combo, gia_combo, hinh_anh, trang_thai) VALUES (N'Combo L?u 1', 299000, 'lau1.jpg', 1), (N'Combo L?u 2', 499000, 'lau2.jpg', 1);
INSERT INTO [dbo].[GiamGia] (ma_giam_gia, ngay_tao, ngay_ket_thuc, dieu_kien_su_dung, gia_tri_giam_toi_da, gia_tri_giam, loai_giam, so_luong_ma_giam_gia, so_luong_dung, trang_thai) 
VALUES ('KM2026', '2026-06-01', '2026-06-30', N'??n t? 500k', 100000, 50000, 'PHANTRAM', 100, 0, 1);

-- D? li?u b?ng ph? thu?c (Tài kho?n)
INSERT INTO [dbo].[TaiKhoan] (ma_tai_khoan, ten_dang_nhap, mat_khau, trang_thai, id_chuc_vu) 
VALUES ('TK001', 'admin', '123456', 1, 1), ('TK002', 'khachhang01', '123456', 1, 3);

-- D? li?u b?ng Món, Bàn
INSERT INTO [dbo].[Mon] (ten_mon, don_gia_hien_tai, id_danh_muc, trang_thai) VALUES (N'L?u Thái', 200000, 1, 1), (N'Bò M?', 150000, 2, 1);
INSERT INTO [dbo].[Ban] (loai_ban, so_luong_ban, id_khu_vuc, trang_thai) VALUES ('VIP', 1, 2, 1), ('Thuong', 10, 1, 1);

-- D? li?u Khách Hàng & Nhân Viên (Liên k?t qua id_tai_khoan)
INSERT INTO [dbo].[KhachHang] (ma_khach_hang, ten_khach_hang, gioi_tinh, dia_chi, so_dien_thoai, id_tai_khoan, email, trang_thai) 
VALUES ('KH001', N'Nguy?n V?n A', 1, N'Hà N?i', '0901234567', 2, 'a@gmail.com', 1);
INSERT INTO [dbo].[NhanVien] (ma_nhan_vien, ten_nhan_vien, gioi_tinh, so_dien_thoai, email, id_chuc_vu, id_tai_khoan, dia_chi, trang_thai) 
VALUES ('NV001', N'Tr?n V?n B', 1, '0987654321', 'b@gmail.com', 2, 1, N'?à N?ng', 1);

-- D? li?u chi ti?t (Combo, GiamGia, DatBan, HoaDon)
INSERT INTO [dbo].[ChiTietCombo] (so_luong, id_mon, id_combo, mo_ta) VALUES (1, 1, 1, N'Combo g?m l?u');
INSERT INTO [dbo].[ChiTietGiamGiaMon] (id_dot_giam_gia, id_mon, muc_giam) VALUES (1, 1, 10000);
INSERT INTO [dbo].[DatBan] (id_ban, id_khach_hang, ngay_dat, gio_dat, sdt_khach_hang, so_nguoi, trang_thai, thoi_gian_den_du_kien, so_tien_coc, trang_thai_coc, phuong_thuc_thanh_toan, id_combo) 
VALUES (1, 1, '2026-06-14', '18:00:00', '0901234567', 4, 1, '2026-06-14 18:00:00', 100000, 1, 1, 1);

INSERT INTO [dbo].[HoaDon] (ma_hoa_don, ma_giao_dich, trang_thai_hoa_don, sdt_khach_hang, tien_truoc_giam, tien_coc, tien_giam_gia, tong_tien, thoi_gian_xuat, id_ban, id_dat_ban, id_giam_gia, id_khach_hang, id_nhan_vien, trang_thai_thanh_toan, phuong_thuc_thanh_toan) 
VALUES ('HD001', 'TXN001', 1, '0901234567', 500000, 100000, 50000, 350000, '2026-06-14 20:00:00', 1, 1, 1, 1, 1, 1, 1);

INSERT INTO [dbo].[HoaDonChiTiet] (ma_hoa_don_chi_tiet, id_mon, id_combo, id_hoa_don, so_luong, gia_ban_tai_thoi_dien, tien_giam_gia_mon, thanh_tien) 
VALUES ('HDCT001', 1, 1, 1, 1, 200000, 10000, 190000);

-- Ép thêm cột loai_giam trực tiếp (bỏ qua kiểm tra IF)
ALTER TABLE [dbo].[ChiTietGiamGiaMon] ADD [loai_giam] VARCHAR(20) NULL;
GO

-- Ép thêm cột trang_thai trực tiếp
ALTER TABLE [dbo].[ChiTietGiamGiaMon] ADD [trang_thai] TINYINT NULL;
GO

-- Cập nhật dữ liệu mặc định cho các bản ghi cũ đỡ bị bốc lỗi NULL
UPDATE [dbo].[ChiTietGiamGiaMon] SET [loai_giam] = 'TIEN' WHERE [loai_giam] IS NULL;
UPDATE [dbo].[ChiTietGiamGiaMon] SET [trang_thai] = 1 WHERE [trang_thai] IS NULL;
GO