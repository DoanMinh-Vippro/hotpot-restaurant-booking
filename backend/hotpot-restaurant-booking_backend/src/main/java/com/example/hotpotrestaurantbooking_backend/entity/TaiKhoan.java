    package com.example.hotpotrestaurantbooking_backend.entity;

    import jakarta.persistence.*;
    import lombok.*;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Entity
    @Table(name = "TaiKhoan")
    public class TaiKhoan {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer idTaiKhoan;
        @Column(nullable = false, unique = true)
        private String maTaiKhoan;
        @Column(nullable = false, unique = true)
        private String tenDangNhap;
        @Column(nullable = false)
        private String matKhau;
        @Column(nullable = false)
        private Boolean trangThai;
        @ManyToOne
        @JoinColumn(name = "id_chuc_vu")
        private ChucVu chucVu;
    }
