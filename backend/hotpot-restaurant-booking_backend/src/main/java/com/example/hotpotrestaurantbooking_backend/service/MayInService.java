package com.example.hotpotrestaurantbooking_backend.service;

import com.example.hotpotrestaurantbooking_backend.dto.MayInRequest;
import com.example.hotpotrestaurantbooking_backend.util.VNCharacterUtils;
import org.springframework.stereotype.Service;

import javax.print.*;
import java.awt.*;
import java.awt.print.*;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MayInService {

    // 1. IN MÁY VĂN PHÒNG THƯỜNG / CANON LBP2900 / MICROSOFT PRINT TO PDF
    public void printToStandardPrinter(String printerNameKeyword, MayInRequest request) {
        // Cho phép AWT tương tác với Driver Windows ngay cả khi chạy dạng Service
        System.setProperty("java.awt.headless", "false");

        try {
            // Bước 1: Chuẩn bị dữ liệu in
            List<String> lines = new ArrayList<>();
            lines.add("========================================");
            lines.add("          PHIẾU BÁO CHẾ BIẾN            ");
            lines.add("              [" + (request.getTenQuay() != null ? request.getTenQuay() : "BẾP") + "]             ");
            lines.add("========================================");
            lines.add("Bàn: " + (request.getTenBan() != null ? request.getTenBan() : "---"));
            lines.add("Mã HD: " + (request.getMaHoaDon() != null ? request.getMaHoaDon() : "---"));
            lines.add("NV Phục Vụ: " + (request.getTenNhanVien() != null ? request.getTenNhanVien() : "---"));
            lines.add("Thời gian: " + (request.getThoiGian() != null ? request.getThoiGian() : "---"));
            lines.add("----------------------------------------");
            lines.add(String.format("%-28s %s", "Tên món", "SL"));
            lines.add("----------------------------------------");

            if (request.getDanhSachMon() != null) {
                for (MayInRequest.MonIn mon : request.getDanhSachMon()) {
                    lines.add(String.format("%-28s x%d", mon.getTenMon(), mon.getSoLuong()));
                }
            }

            lines.add("----------------------------------------");
            lines.add("     Vui lòng chế biến theo thứ tự!     ");

            // Bước 2: Lấy danh sách máy in từ hệ điều hành Windows
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService selectedPrinter = null;

            if (printerNameKeyword != null && !printerNameKeyword.isBlank()) {
                for (PrintService service : services) {
                    if (service.getName().toLowerCase().contains(printerNameKeyword.toLowerCase().trim())) {
                        selectedPrinter = service;
                        break;
                    }
                }
            }

            if (selectedPrinter == null) {
                selectedPrinter = PrintServiceLookup.lookupDefaultPrintService();
            }

            if (selectedPrinter == null) {
                System.err.println("❌ [Office Printer] Không tìm thấy máy in phù hợp nào trên Windows!");
                return;
            }

            System.out.println("🖨️ [Office Printer] Đang gửi dữ liệu tới máy in: " + selectedPrinter.getName());

            // Bước 3: Tạo Job in đồ họa cho Canon LBP2900
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(selectedPrinter);

            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Đặt Font Monospaced để căn lề các dòng đều đặn
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g2d.setColor(Color.BLACK);

                    int y = 20;
                    for (String line : lines) {
                        g2d.drawString(line, 10, y);
                        y += 14;
                    }

                    return PAGE_EXISTS;
                }
            });

            // Thực hiện in ngầm (Silent Print)
            job.print();
            System.out.println("✅ [Office Printer] Đã gửi lệnh in thành công!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi xảy ra khi in máy in Canon/Văn phòng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 2. IN MÁY IN NHIỆT K80 LAN/WIFI (Mã lệnh ESC/POS + Cắt giấy)
    public void printToEscPosPrinter(String ipAddress, int port, MayInRequest request) {
        try (Socket socket = new Socket(ipAddress, port);
             OutputStream out = socket.getOutputStream()) {

            StringBuilder sb = new StringBuilder();

            sb.append("\u001B\u0040"); // Clear buffer
            sb.append("\u001B\u0061\u0001"); // Align Center
            sb.append("\u001D\u0021\u0011"); // Large font
            sb.append("PHIEU BAO CHE BIEN\n");

            sb.append("\u001D\u0021\u0000"); // Normal font
            sb.append("[").append(VNCharacterUtils.removeAccent(request.getTenQuay() != null ? request.getTenQuay() : "BEP")).append("]\n\n");

            sb.append("\u001B\u0061\u0000"); // Align Left
            sb.append("Ban: ").append(VNCharacterUtils.removeAccent(request.getTenBan() != null ? request.getTenBan() : "")).append("\n");
            sb.append("Ma HD: ").append(request.getMaHoaDon() != null ? request.getMaHoaDon() : "").append("\n");
            sb.append("NV: ").append(VNCharacterUtils.removeAccent(request.getTenNhanVien() != null ? request.getTenNhanVien() : "")).append("\n");
            sb.append("Thoi gian: ").append(request.getThoiGian() != null ? request.getThoiGian() : "").append("\n");
            sb.append("--------------------------------\n");

            if (request.getDanhSachMon() != null) {
                for (MayInRequest.MonIn mon : request.getDanhSachMon()) {
                    String tenMonKhongDau = VNCharacterUtils.removeAccent(mon.getTenMon());
                    sb.append(String.format("%-24s x%d\n", tenMonKhongDau, mon.getSoLuong()));
                }
            }

            sb.append("--------------------------------\n");
            sb.append("\u001B\u0061\u0001");
            sb.append("Vui long che bien theo thu tu!\n\n\n");

            // Lệnh CẮT GIẤY TỰ ĐỘNG
            sb.append("\u001D\u0056\u0042\u0000");

            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            System.out.println("✅ [ESC/POS K80] Đã gửi lệnh in tới IP: " + ipAddress);

        } catch (Exception e) {
            System.err.println("❌ Lỗi không kết nối được máy in nhiệt IP " + ipAddress + ": " + e.getMessage());
        }
    }
}