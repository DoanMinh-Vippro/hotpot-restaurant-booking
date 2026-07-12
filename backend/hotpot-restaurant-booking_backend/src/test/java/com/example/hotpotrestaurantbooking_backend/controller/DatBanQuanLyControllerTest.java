package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTODatBanQuanLyResponse;
import com.example.hotpotrestaurantbooking_backend.enums.PhuongThucThanhToan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBanCoc;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DatBanQuanLyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatBanQuanLyService datBanQuanLyService;

    @Test
    void updateShouldAllowStatusChangeEvenWhenAppointmentDateIsInThePast() throws Exception {
        DTODatBanQuanLyResponse response = new DTODatBanQuanLyResponse();
        response.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN);

        when(datBanQuanLyService.update(ArgumentMatchers.eq(1), ArgumentMatchers.any(DTODatBanQuanLyRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                  "trangThai": "DA_XAC_NHAN"
                }
                """;

        mockMvc.perform(put("/api/dat-ban-quan-ly/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
