package com.example.hotpotrestaurantbooking_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOSepayWebhook {
    private String content;
    private Integer transferAmount;
    private String referenceCode;
    private String gateway;
    private String transactionDate;
}
