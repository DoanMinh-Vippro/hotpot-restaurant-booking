// ===========================================
// HoaDonServiceImpl
// ===========================================
package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.HoaDonChiTietDTO;
import com.example.hotpotrestaurantbooking_backend.dto.HoaDonDTO;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDonChiTiet;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonChiTietRepository;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonRepository;
import com.example.hotpotrestaurantbooking_backend.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<HoaDonDTO> getAll() {
        String sql = """
                SELECT
                    hd.id_hoa_don,
                    hd.ma_hoa_don,
                    hd.ma_giao_dich,
                    hd.trang_thai_hoa_don,
                    hd.sdt_khach_hang,
                    hd.tien_truoc_giam,
                    hd.tien_coc,
                    hd.tien_giam_gia,
                    hd.tong_tien,
                    hd.thoi_gian_xuat,
                    hd.id_ban,
                    b.loai_ban,
                    hd.id_dat_ban,
                    hd.id_giam_gia,
                    hd.id_khach_hang,
                    kh.ten_khach_hang,
                    hd.id_nhan_vien,
                    nv.ten_nhan_vien,
                    hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan
                FROM [HoaDon] hd
                LEFT JOIN [Ban] b ON b.id_ban = hd.id_ban
                LEFT JOIN [KhachHang] kh ON kh.id_khach_hang = hd.id_khach_hang
                LEFT JOIN [NhanVien] nv ON nv.id_nhan_vien = hd.id_nhan_vien
                ORDER BY hd.id_hoa_don DESC
                """;

        return jdbcTemplate.query(sql, this::mapHoaDonDTO);
    }

    @Override
    public HoaDonDTO getById(Integer id) {
        String sql = """
                SELECT
                    hd.id_hoa_don,
                    hd.ma_hoa_don,
                    hd.ma_giao_dich,
                    hd.trang_thai_hoa_don,
                    hd.sdt_khach_hang,
                    hd.tien_truoc_giam,
                    hd.tien_coc,
                    hd.tien_giam_gia,
                    hd.tong_tien,
                    hd.thoi_gian_xuat,
                    hd.id_ban,
                    b.loai_ban,
                    hd.id_dat_ban,
                    hd.id_giam_gia,
                    hd.id_khach_hang,
                    kh.ten_khach_hang,
                    hd.id_nhan_vien,
                    nv.ten_nhan_vien,
                    hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan
                FROM [HoaDon] hd
                LEFT JOIN [Ban] b ON b.id_ban = hd.id_ban
                LEFT JOIN [KhachHang] kh ON kh.id_khach_hang = hd.id_khach_hang
                LEFT JOIN [NhanVien] nv ON nv.id_nhan_vien = hd.id_nhan_vien
                WHERE hd.id_hoa_don = ?
                """;

        List<HoaDonDTO> result = jdbcTemplate.query(sql, this::mapHoaDonDTO, id);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<HoaDonChiTietDTO> getChiTietByHoaDonId(Integer idHoaDon) {
        String sql = """
                SELECT
                    hdct.id_hoa_don_chi_tiet,
                    hdct.ma_hoa_don_chi_tiet,
                    hdct.id_mon,
                    m.ten_mon,
                    hdct.id_combo,
                    c.ten_combo,
                    hdct.id_hoa_don,
                    hdct.so_luong,
                    hdct.gia_ban_tai_thoi_dien,
                    hdct.tien_giam_gia_mon,
                    hdct.thanh_tien
                FROM [HoaDonChiTiet] hdct
                LEFT JOIN [Mon] m ON m.id_mon = hdct.id_mon
                LEFT JOIN [Combo] c ON c.id_combo = hdct.id_combo
                WHERE hdct.id_hoa_don = ?
                ORDER BY hdct.id_hoa_don_chi_tiet
                """;

        return jdbcTemplate.query(sql, this::mapHoaDonChiTietDTO, idHoaDon);
    }

    @Override
    public HoaDon add(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }

    @Override
    public HoaDon update(Integer id, HoaDon hoaDon) {

        HoaDon old = hoaDonRepository.findById(id).orElse(null);
        if (old == null) {
            return null;
        }

        old.setMaHoaDon(hoaDon.getMaHoaDon());
        old.setTongTien(hoaDon.getTongTien());
        old.setTrangThaiHoaDon(hoaDon.getTrangThaiHoaDon());

        return hoaDonRepository.save(old);
    }

    @Override
    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    private HoaDonDTO mapHoaDonDTO(ResultSet rs, int rowNum) throws SQLException {
        return HoaDonDTO.builder()
                .idHoaDon(rs.getInt("id_hoa_don"))
                .maHoaDon(rs.getString("ma_hoa_don"))
                .maGiaoDich(rs.getString("ma_giao_dich"))
                .trangThaiHoaDon(getInteger(rs, "trang_thai_hoa_don"))
                .sdtKhachHang(rs.getString("sdt_khach_hang"))
                .tienTruocGiam(rs.getBigDecimal("tien_truoc_giam"))
                .tienCoc(rs.getBigDecimal("tien_coc"))
                .tienGiamGia(rs.getBigDecimal("tien_giam_gia"))
                .tongTien(rs.getBigDecimal("tong_tien"))
                .thoiGianXuat(rs.getTimestamp("thoi_gian_xuat") == null
                        ? null
                        : rs.getTimestamp("thoi_gian_xuat").toLocalDateTime())
                .idBan(getInteger(rs, "id_ban"))
                .loaiBan(rs.getString("loai_ban"))
                .idDatBan(getInteger(rs, "id_dat_ban"))
                .idGiamGia(getInteger(rs, "id_giam_gia"))
                .idKhachHang(getInteger(rs, "id_khach_hang"))
                .tenKhachHang(rs.getString("ten_khach_hang"))
                .idNhanVien(getInteger(rs, "id_nhan_vien"))
                .tenNhanVien(rs.getString("ten_nhan_vien"))
                .trangThaiThanhToan(getInteger(rs, "trang_thai_thanh_toan"))
                .phuongThucThanhToan(getInteger(rs, "phuong_thuc_thanh_toan"))
                .build();
    }

    private HoaDonChiTietDTO mapHoaDonChiTietDTO(ResultSet rs, int rowNum) throws SQLException {
        return HoaDonChiTietDTO.builder()
                .idHoaDonChiTiet(rs.getInt("id_hoa_don_chi_tiet"))
                .maHoaDonChiTiet(rs.getString("ma_hoa_don_chi_tiet"))
                .idMon(getInteger(rs, "id_mon"))
                .tenMon(rs.getString("ten_mon"))
                .idCombo(getInteger(rs, "id_combo"))
                .tenCombo(rs.getString("ten_combo"))
                .idHoaDon(getInteger(rs, "id_hoa_don"))
                .soLuong(getInteger(rs, "so_luong"))
                .giaBanTaiThoiDiem(rs.getBigDecimal("gia_ban_tai_thoi_dien"))
                .tienGiamGiaMon(rs.getBigDecimal("tien_giam_gia_mon"))
                .thanhTien(rs.getBigDecimal("thanh_tien"))
                .build();
    }

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        Number value = (Number) rs.getObject(columnName);
        return value == null ? null : value.intValue();
    }

    private HoaDonDTO toHoaDonDTO(HoaDon hoaDon) {
        return HoaDonDTO.builder()
                .idHoaDon(hoaDon.getIdHoaDon())
                .maHoaDon(hoaDon.getMaHoaDon())
                .maGiaoDich(hoaDon.getMaGiaoDich())
                .trangThaiHoaDon(hoaDon.getTrangThaiHoaDon())
                .sdtKhachHang(hoaDon.getSdtKhachHang())
                .tienTruocGiam(hoaDon.getTienTruocGiam())
                .tienCoc(hoaDon.getTienCoc())
                .tienGiamGia(hoaDon.getTienGiamGia())
                .tongTien(hoaDon.getTongTien())
                .thoiGianXuat(hoaDon.getThoiGianXuat())
                .idBan(hoaDon.getBan() == null ? null : hoaDon.getBan().getIdBan())
                .loaiBan(hoaDon.getBan() == null ? null : hoaDon.getBan().getLoaiBan())
                .idDatBan(hoaDon.getDatBan() == null ? null : hoaDon.getDatBan().getIdDatBan())
                .idGiamGia(hoaDon.getGiamGia() == null ? null : hoaDon.getGiamGia().getIdGiamGia())
                .idKhachHang(hoaDon.getKhachHang() == null ? null : hoaDon.getKhachHang().getIdKhachHang())
                .tenKhachHang(hoaDon.getKhachHang() == null ? null : hoaDon.getKhachHang().getTenKhachHang())
                .idNhanVien(hoaDon.getNhanVien() == null ? null : hoaDon.getNhanVien().getIdNhanVien())
                .tenNhanVien(hoaDon.getNhanVien() == null ? null : hoaDon.getNhanVien().getTenNhanVien())
                .trangThaiThanhToan(hoaDon.getTrangThaiThanhToan())
                .phuongThucThanhToan(hoaDon.getPhuongThucThanhToan())
                .build();
    }

    private HoaDonChiTietDTO toHoaDonChiTietDTO(HoaDonChiTiet chiTiet) {
        return HoaDonChiTietDTO.builder()
                .idHoaDonChiTiet(chiTiet.getIdHoaDonChiTiet())
                .maHoaDonChiTiet(chiTiet.getMaHoaDonChiTiet())
                .idMon(chiTiet.getMon() == null ? null : chiTiet.getMon().getIdMon())
                .tenMon(chiTiet.getMon() == null ? null : chiTiet.getMon().getTenMon())
                .idCombo(chiTiet.getCombo() == null ? null : chiTiet.getCombo().getIdCombo())
                .tenCombo(chiTiet.getCombo() == null ? null : chiTiet.getCombo().getTenCombo())
                .idHoaDon(chiTiet.getHoaDon() == null ? null : chiTiet.getHoaDon().getIdHoaDon())
                .soLuong(chiTiet.getSoLuong())
                .giaBanTaiThoiDiem(chiTiet.getGiaBanTaiThoiDien())
                .tienGiamGiaMon(chiTiet.getTienGiamGiaMon())
                .thanhTien(chiTiet.getThanhTien())
                .build();
    }
}
