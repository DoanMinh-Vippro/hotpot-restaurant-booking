import ApiClient from './ApiClient'

const DatBanQuanLyApi = {
  // Danh sách
  getAll(trangThai?: string) {
    return ApiClient.get('/api/dat-ban-quan-ly', {
      params: {
        trangThai,
      },
    })
  },

  // Chi tiết
  findById(id: number) {
    return ApiClient.get(`/api/dat-ban-quan-ly/${id}`)
  },

  // Theo trạng thái
  findByTrangThai(trangThai: string) {
    return ApiClient.get(`/api/dat-ban-quan-ly/trang-thai/${trangThai}`)
  },

  // Thêm
  add(data: any) {
    return ApiClient.post('/api/dat-ban-quan-ly', data)
  },

  // Sửa
  update(id: number, data: any) {
    return ApiClient.put(`/api/dat-ban-quan-ly/${id}`, data)
  },

  // Hủy đơn
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

  // Lấy bàn trống
  getDanhSachBanTrong(thoiGianDenDuKien: string, soNguoi: number, idDatBan?: number) {
    return ApiClient.get('/api/dat-ban-quan-ly/ban-trong', {
      params: {
        thoiGianDenDuKien,
        soNguoi,
        idDatBan,
      },
    })
  },

  // Lọc theo thời gian
  findByThoiGian(tuNgay: string, denNgay: string) {
    return ApiClient.get('/api/dat-ban-quan-ly/thoi-gian', {
      params: {
        tuNgay,
        denNgay,
      },
    })
  },
  // 🔍 Tìm theo tên khách hoặc SĐT
  searchByKeyword(keyword: string) {
    return ApiClient.get('/api/dat-ban-quan-ly/search', {
      params: {
        keyword,
      },
    })
  },
}

export default DatBanQuanLyApi
