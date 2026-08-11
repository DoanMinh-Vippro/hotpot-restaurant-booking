import ApiClient from "./ApiClient";

const ThongKeApi = {
  // =========================
  // DASHBOARD
  // =========================
  dashboard(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/dashboard?from=${from}&to=${to}`
    );
  },

  // =========================
  // DOANH THU
  // =========================
  theoNgay(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/theo-ngay?from=${from}&to=${to}`
    );
  },

  theoThang(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/theo-thang?from=${from}&to=${to}`
    );
  },

  theoNam(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/theo-nam?from=${from}&to=${to}`
    );
  },

  // =========================
  // TOP MÓN
  // =========================
  topMon(page = 0, size = 5, from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/top-mon?page=${page}&size=${size}&from=${from}&to=${to}`
    );
  },

  // =========================
  // TOP NHÂN VIÊN
  // =========================
  topNhanVien(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/top-nhan-vien?from=${from}&to=${to}`
    );
  },

  // =========================
  // TIỀN CỌC
  // =========================
  tienCocTheoNgay(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/tien-coc-theo-ngay?from=${from}&to=${to}`
    );
  },

  // =========================
  // TRẠNG THÁI CỌC
  // =========================
  trangThaiCoc(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/trang-thai-coc?from=${from}&to=${to}`
    );
  },

  // =========================
  // KHU VỰC
  // =========================
  doanhThuTheoKhuVuc(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/doanh-thu-theo-khu-vuc?from=${from}&to=${to}`
    );
  },

  // =========================
  // DOANH THU THEO GIỜ
  // =========================
  doanhThuTheoGio(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/doanh-thu-theo-gio?from=${from}&to=${to}`
    );
  },

  // =========================
  // DOANH THU THEO DANH MỤC
  // =========================
  doanhThuTheoDanhMuc(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/doanh-thu-theo-danh-muc?from=${from}&to=${to}`
    );
  },

  // =========================
  // TOP KHÁCH HÀNG
  // =========================
  topKhachHangThanThiet(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/top-khach-hang-than-thiet?from=${from}&to=${to}`
    );
  },

  // =========================
  // TOP SẢN PHẨM
  // =========================
  topSanPhamBanChay(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/top-san-pham-ban-chay?from=${from}&to=${to}`
    );
  },

  // =========================
  // HIỆU SUẤT BÀN
  // =========================
  hieuSuatBan(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/hieu-suat-ban?from=${from}&to=${to}`
    );
  },

  // =========================
  // HIỆU QUẢ KHUYẾN MÃI
  // =========================
  hieuQuaKhuyenMai(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/hieu-qua-khuyen-mai?from=${from}&to=${to}`
    );
  },

  // =========================
  // TỈ LỆ HỦY ĐẶT BÀN
  // =========================
  tyLeHuyDatBan(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/ty-le-huy-dat-ban?from=${from}&to=${to}`
    );
  },
    exportExcel(from: string, to: string) {
    return ApiClient.get(
      `/api/thong-ke/export-excel?from=${from}&to=${to}`,
      {
        responseType: "blob",
      }
    );
  },
};


export default ThongKeApi;
