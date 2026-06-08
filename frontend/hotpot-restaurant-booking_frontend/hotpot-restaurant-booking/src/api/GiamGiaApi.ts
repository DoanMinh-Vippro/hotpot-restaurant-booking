import ApiClient from "./ApiClient"
export interface GiamGia {
  idGiamGia: number
  maGiamGia: string
  ngayTao: string | null
  ngayKetThuc: string | null
  dieuKienSuDung: string | null
  giaTriGiamToiDa: number | null
  giaTriGiam: number | null
  loaiGiam: string | null
  soLuongMaGiamGia: number | null
  soLuongDung: number | null
  trangThai: number | null
}

class GiamGiaApi {
  /**
   * Lấy danh sách tất cả mã giảm giá
   */
  getDanhSach() {
    return ApiClient.get<GiamGia[]>('/giam-gia')
  }

  /**
   * Lấy mã giảm giá theo ID
   */
  getById(idGiamGia: number) {
    return ApiClient.get<GiamGia>(`/giam-gia/${idGiamGia}`)
  }

  /**
   * Tạo mã giảm giá mới
   */
  create(data: Omit<GiamGia, 'idGiamGia'>) {
    return ApiClient.post<GiamGia>('/giam-gia', data)
  }

  /**
   * Cập nhật mã giảm giá
   */
  update(idGiamGia: number, data: Partial<Omit<GiamGia, 'idGiamGia'>>) {
    return ApiClient.put<GiamGia>(`/giam-gia/${idGiamGia}`, data)
  }

  /**
   * Xóa mã giảm giá
   */
  delete(idGiamGia: number) {
    return ApiClient.delete(`/giam-gia/${idGiamGia}`)
  }

  /**
   * Tìm kiếm mã giảm giá
   */
  search(keyword: string) {
    return ApiClient.get<GiamGia[]>('/giam-gia/search', {
      params: { key: keyword },
    })
  }
}

export default new GiamGiaApi()
