package org.example.datlich.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTOTaiKhoanRequest {
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private Boolean trangThai;
}
