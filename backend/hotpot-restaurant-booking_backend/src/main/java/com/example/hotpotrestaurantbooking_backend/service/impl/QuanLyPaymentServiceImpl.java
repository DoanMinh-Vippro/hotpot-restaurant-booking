package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.config.VNPayConfig;
import com.example.hotpotrestaurantbooking_backend.dto.*;
import com.example.hotpotrestaurantbooking_backend.entity.DatBan;
import com.example.hotpotrestaurantbooking_backend.entity.TaiKhoan;
import com.example.hotpotrestaurantbooking_backend.repository.DatBanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TaiKhoanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.TransactionRepository;
import com.example.hotpotrestaurantbooking_backend.service.DatBanQuanLyService;
import com.example.hotpotrestaurantbooking_backend.service.QuanLyPaymentService;
import com.example.hotpotrestaurantbooking_backend.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import com.example.hotpotrestaurantbooking_backend.entity.Transaction;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class QuanLyPaymentServiceImpl implements QuanLyPaymentService {

    private final DatBanRepository datBanRepository;
    private final TransactionRepository transactionRepository;
    private final VNPayConfig vnPayConfig;
    private final DatBanQuanLyService datBanQuanLyService;
    private final TaiKhoanRepository taiKhoanRepository;


    private static final String PREFIX = "QLDATBAN_";

    private final Map<String, PendingQuanLyBooking> pendingBookings = new ConcurrentHashMap<>();

    @Override
    public DTOPaymentResponse createPayment(DTODatBanQuanLyRequest dto) {

        String content = PREFIX + System.currentTimeMillis();

        // Lưu dữ liệu tạm
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Integer idTaiKhoan = ((Long) jwt.getClaim("idTaiKhoan")).intValue();

        PendingQuanLyBooking pendingBooking =
                new PendingQuanLyBooking(idTaiKhoan, dto);

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

    @Override
    @Transactional
    public void handleWebhook(DTOSepayWebhook payload) {

        // Chống callback nhiều lần
        if (transactionRepository.existsByReferenceCode(payload.getReferenceCode())) {
            return;
        }

        PendingQuanLyBooking pendingBooking = pendingBookings.get(payload.getContent());

        if (pendingBooking == null) {
            throw new RuntimeException("Không tìm thấy dữ liệu đặt bàn.");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findById(pendingBooking.getIdTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        DTODatBanQuanLyResponse response = datBanQuanLyService.add(pendingBooking.getBooking(), taiKhoan);
        DatBan datBan = datBanRepository.findById(response.getIdDatBan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt bàn"));
        datBan.setSoTienCoc(
                BigDecimal.valueOf(payload.getTransferAmount()).setScale(0, RoundingMode.HALF_UP)
        );

        datBanRepository.save(datBan);
        Transaction transaction = new Transaction();

        transaction.setReferenceCode(payload.getReferenceCode());
        transaction.setAmount(payload.getTransferAmount());
        transaction.setContent(payload.getContent());
        transaction.setIdDatBan(datBan.getIdDatBan());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        pendingBookings.remove(payload.getContent());
        System.out.println("========== QL SEPAY SUCCESS ==========");
        System.out.println("DatBan ID = " + datBan.getIdDatBan());
    }

    @Override
    public boolean checkPaymentStatus(String content) {
        return transactionRepository.existsByContent(content);
    }

    @Override
    public DTOVNPayResponse createVNPayPayment(DTODatBanQuanLyRequest dto) {

        String txnRef = String.valueOf(System.currentTimeMillis());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Integer idTaiKhoan = ((Long) jwt.getClaim("idTaiKhoan")).intValue();

        pendingBookings.put(
                txnRef,
                new PendingQuanLyBooking(idTaiKhoan, dto)
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

        DTOVNPayResponse response = new DTOVNPayResponse();
        response.setPaymentUrl(paymentUrl);

        return response;
    }

    @Override
    @Transactional
    public void handleVNPayReturn(Map<String, String> params) {

        if (!VNPayUtil.verifyReturnData(params, vnPayConfig.getHashSecret())) {
            throw new RuntimeException("Invalid VNPay Signature");
        }

        if (!"00".equals(params.get("vnp_ResponseCode"))) {
            throw new RuntimeException("Thanh toán VNPay thất bại.");
        }

        String txnRef = params.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            throw new RuntimeException("Không tìm thấy mã giao dịch.");
        }

        if (transactionRepository.existsByReferenceCode(params.get("vnp_TransactionNo"))) {
            return;
        }

        PendingQuanLyBooking pendingBooking = pendingBookings.get(txnRef);

        if (pendingBooking == null) {
            throw new RuntimeException("Không tìm thấy dữ liệu đặt bàn.");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findById(
                pendingBooking.getIdTaiKhoan()
        ).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        DTODatBanQuanLyResponse response =
                datBanQuanLyService.add(
                        pendingBooking.getBooking(),
                        taiKhoan
                );

        DatBan datBan = datBanRepository.findById(response.getIdDatBan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt bàn"));

        datBan.setSoTienCoc(
                BigDecimal.valueOf(
                        Double.parseDouble(params.get("vnp_Amount")) / 100
                ).setScale(0, RoundingMode.HALF_UP)
        );

        datBanRepository.save(datBan);

        Transaction transaction = new Transaction();
        transaction.setReferenceCode(params.get("vnp_TransactionNo"));
        transaction.setAmount(Integer.parseInt(params.get("vnp_Amount")) / 100);
        transaction.setContent(txnRef);
        transaction.setIdDatBan(datBan.getIdDatBan());
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        pendingBookings.remove(txnRef);

        System.out.println("========== QL VNPAY SUCCESS ==========");
        System.out.println("DatBan ID = " + datBan.getIdDatBan());
    }
}