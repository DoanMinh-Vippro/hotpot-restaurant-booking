package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.NotificationResponse;
import com.example.hotpotrestaurantbooking_backend.entity.NhanVien;
import com.example.hotpotrestaurantbooking_backend.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final List<StoredNotification> notifications = new ArrayList<>();
    private final Set<String> eventKeys = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized void notifyCustomer(Integer customerId, String eventKey, String title, String message) {
        if (customerId == null || !eventKeys.add("customer:" + customerId + ":" + eventKey)) {
            return;
        }
        notifications.add(new StoredNotification(
                "customer:" + customerId + ":" + eventKey,
                title,
                message,
                LocalDateTime.now(),
                customerId,
                null
        ));
    }

    @Override
    public synchronized void notifyStaff(String eventKey, String title, String message, List<NhanVien> staff) {
        if (staff == null) {
            return;
        }
        for (NhanVien employee : staff) {
            if (employee == null || employee.getTaiKhoan() == null || !Boolean.TRUE.equals(employee.getTaiKhoan().getTrangThai())) {
                continue;
            }
            Integer accountId = employee.getTaiKhoan().getIdTaiKhoan();
            String key = "staff:" + accountId + ":" + eventKey;
            if (eventKeys.add(key)) {
                notifications.add(new StoredNotification(key, title, message, LocalDateTime.now(), null, accountId));
            }
        }
    }

    @Override
    public synchronized List<NotificationResponse> getForCustomer(Integer customerId) {
        return notifications.stream()
                .filter(item -> customerId != null && customerId.equals(item.customerId()))
                .sorted(Comparator.comparing(StoredNotification::createdAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public synchronized List<NotificationResponse> getForStaff(Integer accountId) {
        return notifications.stream()
                .filter(item -> accountId != null && accountId.equals(item.accountId()))
                .sorted(Comparator.comparing(StoredNotification::createdAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(StoredNotification item) {
        return new NotificationResponse(
                item.id(), item.title(), item.message(), item.createdAt(), false,
                item.customerId(), item.accountId() != null
        );
    }

    private record StoredNotification(
            String id,
            String title,
            String message,
            LocalDateTime createdAt,
            Integer customerId,
            Integer accountId
    ) {
    }
}