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
  }
};

export default ThongKeApi;
