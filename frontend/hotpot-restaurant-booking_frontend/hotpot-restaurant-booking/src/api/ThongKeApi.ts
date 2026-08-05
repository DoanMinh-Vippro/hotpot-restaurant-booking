import ApiClient from "./ApiClient";

const ThongKeApi = {
  dashboard() {
    return ApiClient.get("/api/thong-ke/dashboard");
  },

  theoNgay(from: string, to: string) {
    return ApiClient.get(`/api/thong-ke/theo-ngay?from=${from}&to=${to}`);
  },

  theoThang() {
    return ApiClient.get("/api/thong-ke/theo-thang");
  },

  theoNam() {
    return ApiClient.get("/api/thong-ke/theo-nam");
  },

  topMon(page = 0, size = 5) {
    return ApiClient.get(`/api/thong-ke/top-mon?page=${page}&size=${size}`);
  },

  topNhanVien() {
    return ApiClient.get("/api/thong-ke/top-nhan-vien");
  },

  tienCocTheoNgay() {
    return ApiClient.get("/api/thong-ke/tien-coc-theo-ngay");
  },

  trangThaiCoc() {
    return ApiClient.get("/api/thong-ke/trang-thai-coc");
  },
 doanhThuTheoKhuVuc()
   {
    return ApiClient.get("/api/thong-ke/doanh-thu-theo-khu-vuc");
  },
 doanhThuTheoGio()
 {
  return ApiClient.get("/api/thong-ke/doanh-thu-theo-gio");
},
 doanhThuTheoDanhMuc() { return ApiClient.get("/api/thong-ke/doanh-thu-theo-danh-muc");
 },
 topKhachHangThanThiet()
 {
  return ApiClient.get("/api/thong-ke/top-khach-hang-than-thiet");
 },
 topSanPhamBanChay() { return ApiClient.get("/api/thong-ke/top-san-pham-ban-chay"); },
 hieuSuatBan() { return ApiClient.get("/api/thong-ke/hieu-suat-ban"); },
 hieuQuaKhuyenMai() { return ApiClient.get("/api/thong-ke/hieu-qua-khuyen-mai"); },
 tyLeHuyDatBan(from: string, to: string) { return ApiClient.get( `/api/thong-ke/ty-le-huy-dat-ban?from=${from}&to=${to}` ); },
};

export default ThongKeApi;
