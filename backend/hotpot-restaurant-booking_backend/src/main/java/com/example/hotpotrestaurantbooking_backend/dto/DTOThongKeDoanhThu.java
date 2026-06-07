package org.example.datlich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTOThongKeDoanhThu {
    private String thoiGian;
    private Double doanhThu;
}
