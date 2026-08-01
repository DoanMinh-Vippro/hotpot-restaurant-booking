import ApiClient from './ApiClient'

const DatBanQuanLyApi = {
  // Search tổng hợp
  search(params: { keyword?: string; trangThai?: string; tuNgay?: string; denNgay?: string }) {
    return ApiClient.get('/api/dat-ban-quan-ly/search', {
      params,
    })
  },

  // Chi tiết
  findById(id: number) {
    return ApiClient.get(`/api/dat-ban-quan-ly/${id}`)
  },

  // Thêm
  add(data: any) {
    return ApiClient.post('/api/dat-ban-quan-ly', data)
  },

  // Sửa
  update(id: number, data: any) {
    return ApiClient.put(`/api/dat-ban-quan-ly/${id}`, data)
  },

  // Hủy
  delete(id: number) {
    return ApiClient.delete(`/api/dat-ban-quan-ly/${id}`)
  },

  // Xác nhận
  xacNhan(id: number) {
    return ApiClient.put(`/api/dat-ban-quan-ly/${id}/xac-nhan`)
  },

  // Check-in
  checkIn(id: number) {
    return ApiClient.put(`/api/dat-ban-quan-ly/${id}/check-in`)
  },

  // Đổi giờ
  doiGio(id: number, data: any) {
    return ApiClient.put(`/api/dat-ban-quan-ly/${id}/doi-gio`, data)
  },

  // Đổi bàn
  doiBan(id: number, data: any) {
    return ApiClient.put(`/api/dat-ban-quan-ly/${id}/doi-ban`, data)
  },

  // Bàn trống
  getDanhSachBanTrong(thoiGianDenDuKien: string, soNguoi: number, idDatBan?: number) {
    return ApiClient.get('/api/dat-ban-quan-ly/ban-trong', {
      params: {
        thoiGianDenDuKien,
        soNguoi,
        idDatBan,
      },
    })
  },
}

export default DatBanQuanLyApi
