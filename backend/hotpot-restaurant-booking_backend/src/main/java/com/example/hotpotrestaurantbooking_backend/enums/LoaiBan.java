package com.example.hotpotrestaurantbooking_backend.enums;

public enum LoaiBan {

    HAI_NGUOI(2),
    BON_NGUOI(4),
    SAU_NGUOI(6);

    private final int sucChua;

    LoaiBan(int sucChua) {
        this.sucChua = sucChua;
    }

    public int getSucChua() {
        return sucChua;
    }
}