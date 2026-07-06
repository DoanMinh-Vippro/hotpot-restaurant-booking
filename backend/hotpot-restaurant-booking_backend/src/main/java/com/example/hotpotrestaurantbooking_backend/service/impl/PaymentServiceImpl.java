package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.config.VNPayConfig;
import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.Transaction;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhachHangRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TransactionRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanService;
import com.example.hotpotrestaurantbooking_backend.service.PaymentService;
import com.example.hotpotrestaurantbooking_backend.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.example.hotpotrestaurantbooking_backend.dto.PendingBooking;
import com.example.hotpotrestaurantbooking_backend.entity.KhachHang;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final DatBanRepository datBanRepository;
    private final TransactionRepository transactionRepository;
    private final DatBanService datBanService;
    private final VNPayConfig vnPayConfig;

    private static final String PREFIX = "DATBAN_";

    private final Map<String, PendingBooking> pendingBookings = new ConcurrentHashMap<>();
    private final KhachHangRepository khachHangRepository;


    // =========================
    // 1. TẠO THANH TOÁN (QR DATA)
    // =========================
    @Override
    public DTOPaymentResponse createPayment(DTODatBanRequest dto) {

        String content = PREFIX + System.currentTimeMillis();

        // Lấy thông tin người dùng đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Integer idTaiKhoan = ((Long) jwt.getClaim("idTaiKhoan")).intValue();

        KhachHang khachHang = khachHangRepository
                .findByTaiKhoan_IdTaiKhoan(idTaiKhoan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Lưu tạm dữ liệu đặt bàn
        PendingBooking pendingBooking = new PendingBooking(
                khachHang.getIdKhachHang(),
                dto
        );

        pendingBookings.put(content, pendingBooking);

        String qrUrl =
                "https://qr.sepay.vn/img?"
                        + "acc=88801032006"
                        + "&bank=MBBank"
                        + "&amount=" + dto.getSoTienCoc()
                        + "&des=" + content;

        DTOPaymentResponse response = new DTOPaymentResponse();
        response.setContent(content);
        response.setAmount(dto.getSoTienCoc());
        response.setQrUrl(qrUrl);

        return response;
    }

    // =========================
    // 2. WEBHOOK SEPAY (CHƯA SỬA)
    // =========================
    @Transactional
    @Override
    public void handleWebhook(DTOSepayWebhook payload) {

        System.out.println("========== SEPAY WEBHOOK ==========");

        // Chống webhook gửi nhiều lần
        if (transactionRepository.existsByReferenceCode(payload.getReferenceCode())) {
            System.out.println("Duplicate transaction -> Ignore");
            return;
        }

        // Lấy dữ liệu đặt bàn đang chờ thanh toán
        PendingBooking pendingBooking = pendingBookings.get(payload.getContent());

        if (pendingBooking == null) {
            throw new RuntimeException("Không tìm thấy dữ liệu đặt bàn đang chờ thanh toán.");
        }

        // Tạo đơn đặt bàn
        DatBan datBan = datBanService.createBookingAfterPayment(
                pendingBooking.getIdKhachHang(),
                pendingBooking.getBooking()
        );

        // Cập nhật số tiền cọc thực tế
        datBan.setSoTienCoc(
                BigDecimal.valueOf(payload.getTransferAmount())
                        .setScale(0, RoundingMode.HALF_UP)
        );

        datBanRepository.save(datBan);

        // Lưu lịch sử giao dịch
        Transaction transaction = new Transaction();
        transaction.setReferenceCode(payload.getReferenceCode());
        transaction.setAmount(payload.getTransferAmount());
        transaction.setContent(payload.getContent());
        transaction.setIdDatBan(datBan.getIdDatBan());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        // Xóa dữ liệu tạm
        pendingBookings.remove(payload.getContent());

        System.out.println("========== SEPAY SUCCESS ==========");
        System.out.println("DatBan ID = " + datBan.getIdDatBan());
    }

    @Override
    public boolean checkPaymentStatus(String content) {

        return transactionRepository.existsByContent(content);

    }

    @Override
    public DTOVNPayResponse createVNPayPayment(DTODatBanRequest dto) {

        String txnRef = String.valueOf(System.currentTimeMillis());

        // Lấy khách hàng đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Integer idTaiKhoan = ((Long) jwt.getClaim("idTaiKhoan")).intValue();

        KhachHang khachHang = khachHangRepository
                .findByTaiKhoan_IdTaiKhoan(idTaiKhoan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Lưu dữ liệu đặt bàn chờ thanh toán
        pendingBookings.put(
                txnRef,
                new PendingBooking(
                        khachHang.getIdKhachHang(),
                        dto
                )
        );

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String createDate = formatter.format(calendar.getTime());

        calendar.add(Calendar.MINUTE, 15);

        String expireDate = formatter.format(calendar.getTime());

        Map<String, String> vnpParams = new HashMap<>();

        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());

        vnpParams.put(
                "vnp_Amount",
                dto.getSoTienCoc()
                        .multiply(BigDecimal.valueOf(100))
                        .toBigInteger()
                        .toString()
        );

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", txnRef);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        String hashData = VNPayUtil.buildHashData(vnpParams);

        String query = VNPayUtil.buildQuery(vnpParams);

        String secureHash = VNPayUtil.hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData
        );

        String paymentUrl = vnPayConfig.getPayUrl()
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;

        System.out.println("========== CREATE VNPAY ==========");
        System.out.println("TxnRef    = " + txnRef);
        System.out.println("HashData  = " + hashData);
        System.out.println("Hash      = " + secureHash);
        System.out.println("Payment   = " + paymentUrl);

        DTOVNPayResponse response = new DTOVNPayResponse();
        response.setPaymentUrl(paymentUrl);

        return response;
    }

    @Override
    @Transactional
    public void handleVNPayReturn(Map<String, String> params) {

        // Xác thực chữ ký
        if (!VNPayUtil.verifyReturnData(params, vnPayConfig.getHashSecret())) {
            throw new RuntimeException("Invalid VNPay Signature");
        }

        // Kiểm tra trạng thái thanh toán
        if (!"00".equals(params.get("vnp_ResponseCode"))) {
            throw new RuntimeException("Thanh toán VNPay thất bại.");
        }

        String txnRef = params.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            throw new RuntimeException("Không tìm thấy mã giao dịch.");
        }

        // Chống callback nhiều lần
        if (transactionRepository.existsByReferenceCode(params.get("vnp_TransactionNo"))) {
            System.out.println("Duplicate VNPay callback -> Ignore");
            return;
        }

        // Lấy dữ liệu đặt bàn đang chờ
        PendingBooking pendingBooking = pendingBookings.get(txnRef);

        if (pendingBooking == null) {
            throw new RuntimeException("Không tìm thấy dữ liệu đặt bàn đang chờ thanh toán.");
        }

        // Tạo đơn đặt bàn
        DatBan datBan = datBanService.createBookingAfterPayment(
                pendingBooking.getIdKhachHang(),
                pendingBooking.getBooking()
        );

        // Cập nhật số tiền cọc
        datBan.setSoTienCoc(
                BigDecimal.valueOf(
                        Double.parseDouble(params.get("vnp_Amount")) / 100
                ).setScale(0, RoundingMode.HALF_UP)
        );

        datBanRepository.save(datBan);

        // Lưu lịch sử giao dịch
        Transaction transaction = new Transaction();
        transaction.setReferenceCode(params.get("vnp_TransactionNo"));
        transaction.setAmount(Integer.parseInt(params.get("vnp_Amount")) / 100);
        transaction.setContent(txnRef);
        transaction.setIdDatBan(datBan.getIdDatBan());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        // Xóa dữ liệu tạm
        pendingBookings.remove(txnRef);

        System.out.println("========== VNPAY SUCCESS ==========");
        System.out.println("TxnRef     = " + txnRef);
        System.out.println("DatBan ID  = " + datBan.getIdDatBan());
        System.out.println("Amount     = " + transaction.getAmount());

        params.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}