package com.example.hotpotrestaurantbooking_backend.controller;

import com.example.hotpotrestaurantbooking_backend.dto.MayInRequest;
import com.example.hotpotrestaurantbooking_backend.service.MayInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/print")
public class PrintController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MayInService printService;

    // Đọc các giá trị cấu hình từ application.properties
    @Value("${printer.mode:VIRTUAL}")
    private String printMode;

    @Value("${printer.office.bep-printer-name:}")
    private String officeBepName;

    @Value("${printer.office.bar-printer-name:}")
    private String officeBarName;

    @Value("${printer.escpos.bep-ip:192.168.1.200}")
    private String escposBepIp;

    @Value("${printer.escpos.bar-ip:192.168.1.201}")
    private String escposBarIp;

    @Value("${printer.escpos.port:9100}")
    private int escposPort;

    @PostMapping("/send-ticket")
    public ResponseEntity<?> sendToVirtualScreen(@RequestBody MayInRequest request) {
        if (request == null || request.getDanhSachMon() == null || request.getDanhSachMon().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Danh sách món in không được rỗng!"));
        }

        try {
            boolean isBar = request.getTenQuay() != null && request.getTenQuay().toUpperCase().contains("BAR");

            switch (printMode.toUpperCase()) {

                // 1. IN QUA MÁY IN VĂN PHÒNG / MICROSOFT PRINT TO PDF
                case "OFFICE_PRINTER":
                    String targetPrinterName = isBar ? officeBarName : officeBepName;
                    printService.printToStandardPrinter(targetPrinterName, request);
                    break;

                // 2. IN QUA MÁY IN NHIỆT K80 LAN/WIFI (ESC/POS)
                case "ESC_POS":
                    String targetIp = isBar ? escposBarIp : escposBepIp;
                    printService.printToEscPosPrinter(targetIp, escposPort, request);
                    break;

                // 3. GIẢ LẬP BẮN WEBSOCKET NỔ MÀN HÌNH VUE KDS
                case "VIRTUAL":
                default:
                    String topic = isBar ? "/topic/bar" : "/topic/bep";
                    messagingTemplate.convertAndSend(topic, request);
                    break;
            }

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Đã xử lý lệnh in thành công!",
                    "mode", printMode
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}