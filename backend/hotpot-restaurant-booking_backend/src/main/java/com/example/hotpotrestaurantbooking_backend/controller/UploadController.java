package com.example.hotpotrestaurantbooking_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController

public class UploadController {

    // Thư mục lưu ảnh (nằm ngay ngoài cùng của project hoặc thư mục bạn đã cấu hình)
//    private static final String UPLOAD_DIR = "uploads/";
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body("Vui lòng chọn file!");
//        }
//
//        try {
//            // 1. Tạo thư mục nếu chưa có
//            File directory = new File(UPLOAD_DIR);
//            if (!directory.exists()) {
//                directory.mkdirs();
//            }
//
//            // 2. Đổi tên file để tránh trùng lặp (dùng UUID)
//            String originalFilename = file.getOriginalFilename();
//            String extension = "";
//            if (originalFilename != null && originalFilename.contains(".")) {
//                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            }
//            String newFilename = UUID.randomUUID().toString() + extension;
//
//            // 3. Lưu file vào ổ cứng
//            Path path = Paths.get(UPLOAD_DIR + newFilename);
//            Files.write(path, file.getBytes());
//
//            // 4. Trả về tên file mới cho Frontend (vd: "abc-123.jpg")
//            return ResponseEntity.ok(newFilename);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Lỗi khi upload file!");
//        }
//    }


    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn file!");
        }

        try {
            // Cấu hình lưu vào thư mục tên là hotpot_restaurant trên Cloudinary
            Map params = ObjectUtils.asMap(
                    "folder", "hotpot_restaurant"
            );

            // Đẩy trực tiếp mảng byte của file lên mây
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // Lấy URL bảo mật (https://...) do Cloudinary trả về
            String secureUrl = uploadResult.get("secure_url").toString();

            // Trả thẳng URL này về cho Frontend lưu vào Database
            return ResponseEntity.ok(secureUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi khi upload file lên Cloudinary!");
        }
    }
}