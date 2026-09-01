package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.NotificationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceImplTest {

    @Test
    void shouldFilterNotificationsByCustomerIdAndPhone() {
        NotificationServiceImpl service = new NotificationServiceImpl();

        service.notifyCustomer(101, "0900000001", "booking-created-1", "Đặt bàn thành công", "Bạn đã đặt bàn thành công");
        service.notifyCustomer(102, "0900000002", "booking-created-2", "Đặt bàn thành công", "Bạn đã đặt bàn thành công");

        List<NotificationResponse> byCustomerId = service.getForCustomer(101, "0900000001");
        List<NotificationResponse> byWrongCustomer = service.getForCustomer(101, "0900000002");

        assertEquals(1, byCustomerId.size());
        assertEquals(0, byWrongCustomer.size());
        assertTrue(byCustomerId.get(0).getId().contains("booking-created-1"));
        assertEquals("0900000001", byCustomerId.get(0).getTargetPhone());
    }
}
