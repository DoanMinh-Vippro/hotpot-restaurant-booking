package com.example.hotpotrestaurantbooking_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTODoiBanRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một bàn")
    private List<Integer> dsBan;

}
