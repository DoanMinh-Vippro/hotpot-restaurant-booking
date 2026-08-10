package com.example.hotpotrestaurantbooking_backend.service.Impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOSepayWebhook;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.HoaDon;
import com.example.hotpotrestaurantbooking_backend.entity.Transaction;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiBan;
import com.example.hotpotrestaurantbooking_backend.enums.TrangThaiDatBan;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.HoaDonRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TransactionRepository;
import com.example.hotpotrestaurantbooking_backend.service.SePayHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SePayHoaDonServiceImpl implements SePayHoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final BanRepository banRepository;
    private final DatBanRepository datBanRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void handleHoaDonWebhook(DTOSepayWebhook payload) {
        System.out.println("========== SEPAY HOADON WEBHOOK HIT ==========");

        // 1. Kiểm tra trùng lặp giao dịch dựa trên mã tham chiếu của SePay (Reference Code)
        if (transactionRepository.existsByReferenceCode(payload.getReferenceCode())) {
            System.out.println("Giao dịch trùng lặp (" + payload.getReferenceCode() + ") -> Bỏ qua.");
            return;
        }

        // 2. Lấy nội dung chuyển khoản (chính là maHoaDon, ví dụ: HD1234567)
        String maHoaDon = payload.getContent();
        if (maHoaDon == null || maHoaDon.isBlank()) {
            System.out.println("Nội dung chuyển khoản bị trống!");
            return;
        }

        // 3. Truy vấn tìm hóa đơn tương ứng trong DB theo maHoaDon
        HoaDon hoaDon = hoaDonRepository.findAll().stream()
                .filter(hd -> maHoaDon.equalsIgnoreCase(hd.getMaHoaDon()))
                .findFirst()
                .orElse(null);

        if (hoaDon == null) {
            System.out.println("Không tìm thấy hóa đơn khớp với nội dung chuyển khoản: " + maHoaDon);
            return;
        }

        // 4. Cập nhật trạng thái Hóa đơn thành ĐÃ THANH TOÁN
        hoaDon.setTrangThaiHoaDon(1);       // Hoàn thành hóa đơn
        hoaDon.setTrangThaiThanhToan(1);    // Đã thanh toán thành công
        hoaDon.setPhuongThucThanhToan(2);   // Chuyển khoản ngân hàng (quy ước riêng của quán)
        hoaDon.setMaGiaoDich(payload.getReferenceCode()); // Gắn mã giao dịch từ SePay

        // 5. Nếu có đặt bàn liên quan, hoàn thành đặt bàn và giải phóng toàn bộ bàn liên quan.
        DatBan datBan = hoaDon.getDatBan();
        if (datBan != null) {
            if (datBan.getTrangThai() == null || datBan.getTrangThai() != TrangThaiDatBan.HOAN_THANH) {
                datBan.setTrangThai(TrangThaiDatBan.HOAN_THANH);
            }
            if (datBan.getChiTietDatBanBans() != null) {
                datBan.getChiTietDatBanBans().forEach(ct -> {
                    Ban relatedBan = ct.getBan();
                    if (relatedBan != null && relatedBan.getTrangThai() != TrangThaiBan.TRONG) {
                        relatedBan.setTrangThai(TrangThaiBan.TRONG);
                        banRepository.save(relatedBan);
                    }
                });
            }
            datBanRepository.save(datBan);
        }

        // 6. Giải phóng trạng thái bàn ăn từ ĐANG_SU_DUNG về TRONG nếu hóa đơn có bàn riêng
        if (hoaDon.getBan() != null) {
            hoaDon.getBan().setTrangThai(TrangThaiBan.TRONG);
            banRepository.save(hoaDon.getBan());
        }

        hoaDonRepository.save(hoaDon);

        // 6. Ghi lịch sử giao dịch thành công vào bảng Transaction để quản lý đối soát dòng tiền
        Transaction transaction = new Transaction();
        transaction.setReferenceCode(payload.getReferenceCode());
        transaction.setAmount(payload.getTransferAmount());
        transaction.setContent(maHoaDon); // Lưu nội dung giao dịch là mã hóa đơn
        transaction.setIdDatBan(hoaDon.getDatBan() != null ? hoaDon.getDatBan().getIdDatBan() : null); // Liên kết nếu có đặt bàn trước
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        System.out.println("========== TỰ ĐỘNG HOÀN TẤT HÓA ĐƠN THÀNH CÔNG: " + maHoaDon + " ==========");
    }
}
