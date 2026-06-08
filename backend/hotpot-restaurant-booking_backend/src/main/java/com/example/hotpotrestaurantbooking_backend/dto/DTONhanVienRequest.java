package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DTONhanVienRequest {
    private String maNhanVien;
    private String tenNhanVien;
    private Boolean gioiTinh;
    private String soDienThoai;
    private String email;
    private Integer idChucVu;
    private Integer idTaiKhoan;
    private String diaChi;
    private Boolean trangThai;
}
