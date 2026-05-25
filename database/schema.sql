create database hotpot_restaurant_booking;
go

use hotpot_restaurant_booking;
go

create table ChucVu(
	id_chuc_vu int primary key identity(1,1),
	ma_chuc_vu varchar(15),
	ten_chuc_vu nvarchar(10)
)
go

create table TaiKhoan(
	id_tai_khoan int primary key identity(1,1),
	ma_tai_khoan varchar(20) unique,
	ten_dang_nhap varchar(20) unique,
	mat_khau varchar(255),
	trang_thai bit
)
go

create table NhanVien(
	id_nhan_vien int primary key identity(1,1),
	ma_nhan_vien varchar(15) unique,
	ten_nhan_vien nvarchar(25),
	gioi_tinh bit,
	so_dien_thoai varchar(10),
	email varchar(30),
	id_chuc_vu int,
	id_tai_khoan int,
	dia_chi nvarchar(50),
	trang_thai bit,

	foreign key (id_chuc_vu) references ChucVu(id_chuc_vu),
	foreign key (id_tai_khoan) references TaiKhoan(id_tai_khoan)
)
go

create table KhachHang(
	id_khach_hang int primary key identity(1,1),
	ma_khach_hang varchar(15) unique,
	ten_khach_hang nvarchar(25),
	gioi_tinh bit,
	dia_chi nvarchar(50),
	so_dien_thoai varchar(10) unique,
	id_tai_khoan int,
	email varchar(50),
	trang_thai bit,

	foreign key (id_tai_khoan) references TaiKhoan(id_tai_khoan)
)
go

create table KhuVuc(
	id_khu_vuc int primary key identity(1,1),
	mo_ta nvarchar(100),
	ten_khu_vuc nvarchar(30),
	trang_thai tinyint
)
go

create table Ban(
	id_ban int primary key identity(1,1),
	loai_ban varchar(20),
	so_luong_ban int,
	id_khu_vuc int,
	trang_thai tinyint,

	foreign key (id_khu_vuc) references KhuVuc(id_khu_vuc)
)
go

create table DatBan(
	id_dat_ban int primary key identity(1,1),
	id_ban int,
	id_khach_hang int,
	ngay_dat date,
	gio_dat time,
	sdt_khach_hang varchar(10),
	so_nguoi int,
	trang_thai tinyint,
	ghi_chu nvarchar(100),
	thoi_gian_den_du_kien datetime2,
	so_tien_coc decimal(18,2),
	trang_thai_coc tinyint,
	phuong_thuc_thanh_toan tinyint,

	foreign key (id_ban) references Ban(id_ban),
	foreign key (id_khach_hang) references KhachHang(id_khach_hang)
)
go

create table GiamGia(
	id_giam_gia int primary key identity(1,1),
	ma_giam_gia varchar(20),
	ngay_tao date,
	ngay_ket_thuc date,
	dieu_kien_su_dung nvarchar(100),
	gia_tri_giam_toi_da decimal(18,2),
	gia_tri_giam decimal(18,2),
	loai_giam varchar(20),
	so_luong_ma_giam_gia int,
	so_luong_dung int,
	trang_thai tinyint
)
go

create table HoaDon(
	id_hoa_don int primary key identity(1,1),
	ma_hoa_don varchar(20) unique,
	ma_giao_dich varchar(50),
	trang_thai_hoa_don tinyint,
	sdt_khach_hang varchar(10),
	tien_truoc_giam decimal(18,2),
	tien_coc decimal(18,2),
	tien_giam_gia decimal(18,2),
	tong_tien decimal(18,2),
	thoi_gian_xuat datetime2,
	id_ban int,
	id_dat_ban int,
	id_giam_gia int,
	id_khach_hang int,
	id_nhan_vien int,
	trang_thai_thanh_toan tinyint,
	phuong_thuc_thanh_toan tinyint,

	foreign key (id_ban) references Ban(id_ban),
	foreign key (id_dat_ban) references DatBan(id_dat_ban),
	foreign key (id_giam_gia) references GiamGia(id_giam_gia),
	foreign key (id_khach_hang) references KhachHang(id_khach_hang),
	foreign key (id_nhan_vien) references NhanVien(id_nhan_vien)
)
go

create table Combo(
	id_combo int primary key identity(1,1),
	ten_combo nvarchar(50),
	gia_combo decimal(18,2),
	hinh_anh varchar(500),
	trang_thai tinyint
)
go

create table DanhMuc(
	id_danh_muc int primary key identity(1,1),
	loai_danh_muc nvarchar(50),
	mo_ta nvarchar(100)
)
go

create table Mon(
	id_mon int primary key identity(1,1),
	ten_mon nvarchar(30),
	don_gia_hien_tai decimal(18,2),
	id_danh_muc int,

	foreign key (id_danh_muc) references DanhMuc(id_danh_muc)
)
go

create table HoaDonChiTiet(
	id_hoa_don_chi_tiet int primary key identity(1,1),
	ma_hoa_don_chi_tiet varchar(20) unique,
	id_mon int,
	id_combo int,
	id_hoa_don int,
	so_luong int,
	gia_ban_tai_thoi_dien decimal(18,2),
	tien_giam_gia_mon decimal(18,2),
	thanh_tien decimal(18,2),

	foreign key (id_mon) references Mon(id_mon),
	foreign key (id_hoa_don) references HoaDon(id_hoa_don),
	foreign key (id_combo) references Combo(id_combo)
)
go

create table ChiTietCombo(
	id_chi_tiet_combo int primary key identity(1,1),
	so_luong int,
	id_mon int,
	id_combo int,
	mo_ta nvarchar(100),

	foreign key (id_mon) references Mon(id_mon),
	foreign key (id_combo) references Combo(id_combo)
)
go

create table DotGiamGia(
	id_dot_giam_gia int primary key identity(1,1),
	ten_chuong_trinh nvarchar(40),
	ngay_bat_dau date,
	ngay_ket_thuc date
)
go

create table ChiTietGiamGiaMon(
	id_chi_tiet_giam_gia_mon int primary key identity(1,1),
	id_dot_giam_gia int,
	id_mon int,
	muc_giam decimal(18,2),

	foreign key (id_mon) references Mon(id_mon),
	foreign key (id_dot_giam_gia) references DotGiamGia(id_dot_giam_gia)
)
go

-- =========================================
-- CHUC VU
-- =========================================
insert into ChucVu(ma_chuc_vu, ten_chuc_vu)
values
('CV001', N'Quản lý'),
('CV002', N'Thu ngân'),
('CV003', N'Nhân viên');

-- =========================================
-- TAI KHOAN
-- =========================================
insert into TaiKhoan(ma_tai_khoan, ten_dang_nhap, mat_khau, trang_thai)
values
('TK001', 'admin', '123456', 1),
('TK002', 'thungan01', '123456', 1),
('TK003', 'nhanvien01', '123456', 1),
('TK004', 'khachhang01', '123456', 1),
('TK005', 'khachhang02', '123456', 1),
('TK006', 'khachhang03', '123456', 1);

-- =========================================
-- NHAN VIEN
-- =========================================
insert into NhanVien(
	ma_nhan_vien,
	ten_nhan_vien,
	gioi_tinh,
	so_dien_thoai,
	email,
	id_chuc_vu,
	id_tai_khoan,
	dia_chi,
	trang_thai
)
values
(
	'NV001',
	N'Nguyễn Minh Quân',
	1,
	'0911111111',
	'quan@gmail.com',
	1,
	1,
	N'Quận 1',
	1
),
(
	'NV002',
	N'Trần Quốc Huy',
	1,
	'0922222222',
	'huy@gmail.com',
	2,
	2,
	N'Thủ Đức',
	1
),
(
	'NV003',
	N'Phạm Hoàng Long',
	1,
	'0933333333',
	'long@gmail.com',
	3,
	3,
	N'Bình Thạnh',
	1
);

-- =========================================
-- KHACH HANG
-- =========================================
insert into KhachHang(
	ma_khach_hang,
	ten_khach_hang,
	gioi_tinh,
	dia_chi,
	so_dien_thoai,
	id_tai_khoan,
	email,
	trang_thai
)
values
(
	'KH001',
	N'Lê Thị Mai',
	0,
	N'Quận 7',
	'0901111111',
	4,
	'mai@gmail.com',
	1
),
(
	'KH002',
	N'Nguyễn Quốc Bảo',
	1,
	N'Gò Vấp',
	'0902222222',
	5,
	'bao@gmail.com',
	1
),
(
	'KH003',
	N'Trần Minh Đức',
	1,
	N'Tân Bình',
	'0903333333',
	6,
	'duc@gmail.com',
	1
);

-- =========================================
-- KHU VUC
-- =========================================
insert into KhuVuc(
	mo_ta,
	ten_khu_vuc,
	trang_thai
)
values
(
	N'Không gian máy lạnh tầng 1',
	N'Tầng 1',
	1
),
(
	N'Không gian VIP riêng tư',
	N'Phòng VIP',
	1
),
(
	N'Khu vực ngoài trời',
	N'Sân Vườn',
	1
);

-- =========================================
-- BAN
-- =========================================
insert into Ban(
	loai_ban,
	so_luong_ban,
	id_khu_vuc,
	trang_thai
)
values
('4 nguoi', 4, 1, 1),
('6 nguoi', 6, 1, 1),
('8 nguoi', 8, 2, 1);

-- =========================================
-- DAT BAN
-- =========================================
insert into DatBan(
	id_ban,
	id_khach_hang,
	ngay_dat,
	gio_dat,
	sdt_khach_hang,
	so_nguoi,
	trang_thai,
	ghi_chu,
	thoi_gian_den_du_kien,
	so_tien_coc,
	trang_thai_coc,
	phuong_thuc_thanh_toan
)
values
(
	1,
	1,
	'2026-05-25',
	'18:00:00',
	'0901111111',
	4,
	1,
	N'Bàn gần cửa sổ',
	'2026-05-25 18:00:00',
	200000,
	1,
	1
),
(
	2,
	2,
	'2026-05-26',
	'19:00:00',
	'0902222222',
	6,
	1,
	N'Sinh nhật bạn',
	'2026-05-26 19:00:00',
	300000,
	1,
	2
),
(
	3,
	3,
	'2026-05-27',
	'20:00:00',
	'0903333333',
	8,
	0,
	N'Đặt tiệc công ty',
	'2026-05-27 20:00:00',
	500000,
	0,
	1
);

-- =========================================
-- GIAM GIA
-- =========================================
insert into GiamGia(
	ma_giam_gia,
	ngay_tao,
	ngay_ket_thuc,
	dieu_kien_su_dung,
	gia_tri_giam_toi_da,
	gia_tri_giam,
	loai_giam,
	so_luong_ma_giam_gia,
	so_luong_dung,
	trang_thai
)
values
(
	'GG001',
	'2026-05-01',
	'2026-06-01',
	N'Hóa đơn từ 500K',
	100000,
	10,
	'PHAN_TRAM',
	100,
	10,
	1
),
(
	'GG002',
	'2026-05-01',
	'2026-06-15',
	N'Hóa đơn từ 1 triệu',
	200000,
	15,
	'PHAN_TRAM',
	50,
	5,
	1
),
(
	'GG003',
	'2026-05-01',
	'2026-06-30',
	N'Áp dụng toàn menu',
	50000,
	50000,
	'TIEN_MAT',
	200,
	20,
	1
);

-- =========================================
-- HOA DON
-- =========================================
insert into HoaDon(
	ma_hoa_don,
	ma_giao_dich,
	trang_thai_hoa_don,
	sdt_khach_hang,
	tien_truoc_giam,
	tien_coc,
	tien_giam_gia,
	tong_tien,
	thoi_gian_xuat,
	id_ban,
	id_dat_ban,
	id_giam_gia,
	id_khach_hang,
	id_nhan_vien,
	trang_thai_thanh_toan,
	phuong_thuc_thanh_toan
)
values
(
	'HD001',
	'GD001',
	1,
	'0901111111',
	1200000,
	200000,
	100000,
	900000,
	getdate(),
	1,
	1,
	1,
	1,
	2,
	1,
	1
),
(
	'HD002',
	'GD002',
	1,
	'0902222222',
	1800000,
	300000,
	200000,
	1300000,
	getdate(),
	2,
	2,
	2,
	2,
	2,
	1,
	2
),
(
	'HD003',
	'GD003',
	0,
	'0903333333',
	900000,
	500000,
	50000,
	350000,
	getdate(),
	3,
	3,
	3,
	3,
	3,
	0,
	1
);

-- =========================================
-- DANH MUC
-- =========================================
insert into DanhMuc(
	loai_danh_muc,
	mo_ta
)
values
(
	N'Thịt',
	N'Các món thịt nhúng lẩu'
),
(
	N'Hải sản',
	N'Hải sản tươi sống'
),
(
	N'Nước uống',
	N'Các loại đồ uống'
);

-- =========================================
-- MON
-- =========================================
insert into Mon(
	ten_mon,
	don_gia_hien_tai,
	id_danh_muc
)
values
(
	N'Ếch Đồng',
	180000,
	1
),
(
	N'Bò Mỹ',
	150000,
	1
),
(
	N'Tôm Sú',
	220000,
	2
),
(
	N'Mực Tươi',
	190000,
	2
),
(
	N'Coca Cola',
	20000,
	3
),
(
	N'Trà Chanh',
	15000,
	3
);

-- =========================================
-- COMBO
-- =========================================
insert into Combo(
	ten_combo,
	gia_combo,
	hinh_anh,
	trang_thai
)
values
(
	N'Combo Gia Đình',
	499000,
	'combo_giadinh.jpg',
	1
),
(
	N'Combo Hải Sản',
	699000,
	'combo_haisan.jpg',
	1
),
(
	N'Combo Tiệc Nhóm',
	999000,
	'combo_tiec.jpg',
	1
);

-- =========================================
-- CHI TIET COMBO
-- =========================================
insert into ChiTietCombo(
	so_luong,
	id_mon,
	id_combo,
	mo_ta
)
values
(2, 1, 1, N'2 phần ếch đồng'),
(1, 5, 1, N'1 coca cola'),

(2, 3, 2, N'2 phần tôm sú'),
(1, 4, 2, N'1 phần mực tươi'),

(3, 1, 3, N'3 phần ếch đồng'),
(2, 2, 3, N'2 phần bò Mỹ');

-- =========================================
-- HOA DON CHI TIET
-- =========================================
insert into HoaDonChiTiet(
	ma_hoa_don_chi_tiet,
	id_mon,
	id_combo,
	id_hoa_don,
	so_luong,
	gia_ban_tai_thoi_dien,
	tien_giam_gia_mon,
	thanh_tien
)
values
(
	'HDCT001',
	1,
	null,
	1,
	2,
	180000,
	0,
	360000
),
(
	'HDCT002',
	null,
	1,
	1,
	1,
	499000,
	0,
	499000
),
(
	'HDCT003',
	3,
	null,
	2,
	2,
	220000,
	20000,
	420000
),
(
	'HDCT004',
	null,
	2,
	2,
	1,
	699000,
	0,
	699000
),
(
	'HDCT005',
	2,
	null,
	3,
	1,
	150000,
	0,
	150000
);

-- =========================================
-- DOT GIAM GIA
-- =========================================
insert into DotGiamGia(
	ten_chuong_trinh,
	ngay_bat_dau,
	ngay_ket_thuc
)
values
(
	N'Khai Trương',
	'2026-05-01',
	'2026-05-31'
),
(
	N'Cuối Tuần Vui Vẻ',
	'2026-06-01',
	'2026-06-30'
),
(
	N'Tri Ân Khách Hàng',
	'2026-07-01',
	'2026-07-31'
);

-- =========================================
-- CHI TIET GIAM GIA MON
-- =========================================
insert into ChiTietGiamGiaMon(
	id_dot_giam_gia,
	id_mon,
	muc_giam
)
values
(1, 1, 20000),
(2, 3, 30000),
(3, 2, 15000);