import axiosClient from "./axiosClient";

const ThongKeApi = {
  dashboard() {
    return axiosClient.get("/thong-ke/dashboard");
  },

  theoNgay(from: string, to: string) {
    return axiosClient.get(`/thong-ke/theo-ngay?from=${from}&to=${to}`);
  },

  theoThang() {
    return axiosClient.get("/thong-ke/theo-thang");
  },

  theoNam() {
    return axiosClient.get("/thong-ke/theo-nam");
  },

  topMon(page = 0, size = 5) {
    return axiosClient.get(`/thong-ke/top-mon?page=${page}&size=${size}`);
  },

  topNhanVien() {
    return axiosClient.get("/thong-ke/top-nhan-vien");
  }
};

export default ThongKeApi;