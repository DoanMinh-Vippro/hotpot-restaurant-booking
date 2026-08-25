import ApiClient from './ApiClient'

export interface HoaDon {
  idHoaDon: number
  maHoaDon: string
  maGiaoDich: string | null
  trangThaiHoaDon: number | null
  sdtKhachHang: string | null
  tienTruocGiam: number | string | null
  tienCoc: number | string | null
  tienGiamGia: number | string | null
  tongTien: number | string | null
  thoiGianXuat: string | number[] | null
  gioVaoBan?: string | number[] | null
  gioRoiBan?: string | number[] | null
  idBan: number | null
  tenBan?: string | null
  dsBan?: Array<{ idBan?: number | null; tenBan?: string | null; name?: string | null; ten?: string | null }> | null
  loaiBan: string | null
  idDatBan: number | null
  idGiamGia?: number | null
  maGiamGia?: string | null
  loaiGiam?: string | null
  idKhachHang: number | null
  tenKhachHang: string | null
  idNhanVien: number | null
  tenNhanVien: string | null
  trangThaiThanhToan: number | null
  phuongThucThanhToan: number | null
  soTienChuyenKhoan?: number | string | null
  soTienTienMat?: number | string | null
  shiftId?: string | null

  chiTiet?: HoaDonChiTiet[] | null
}

export interface HoaDonChiTiet {
  idHoaDonChiTiet: number
  maHoaDonChiTiet: string
  tenMon: string | null
  tenCombo: string | null
  comboItems?: string[] | null
  soLuong: number | null
  giaBanTaiThoiDiem: number | string | null
  tienGiamGiaMon: number | string | null
  thanhTien: number | string | null
  orderedBy?: string | null
  orderedAt?: string | number[] | null
}

class HoaDonApi {
  /**
   * Lấy danh sách tất cả hóa đơn
   */
  getDanhSach() {
    return ApiClient.get<HoaDon[]>('/api/hoa-don')
  }

  getActiveBills() {
    return ApiClient.get<HoaDon[]>('/api/hoa-don/active')
  }

  getActiveCount() {
    return ApiClient.get<number>('/api/hoa-don/active/count')
  }

  /**
   * Lấy chi tiết hóa đơn (danh sách items)
   */
  getChiTiet(idHoaDon: number) {
    return ApiClient.get<HoaDonChiTiet[]>(`/api/hoa-don/${idHoaDon}/chi-tiet`)
  }

  /**
   * Lấy hóa đơn theo ID
   */
  getById(idHoaDon: number) {
    return ApiClient.get<HoaDon>(`/api/hoa-don/${idHoaDon}`)
  }

  /**
   * Tạo hóa đơn mới
   */
  create(data: Omit<HoaDon, 'idHoaDon' | 'ngayTao' | 'ngayCapNhat' | 'gioTao' | 'gioCapNhat'>) {
    return ApiClient.post<HoaDon>('/api/hoa-don', data)
  }

  /**
   * Cập nhật hóa đơn
   */
  update(idHoaDon: number, data: Partial<HoaDon>) {
    return ApiClient.put<HoaDon>(`/api/hoa-don/${idHoaDon}`, data)
  }

  /**
   * Xóa hóa đơn
   */
  delete(idHoaDon: number) {
    return ApiClient.delete(`/api/hoa-don/${idHoaDon}`)
  }

  /**
   * Tìm kiếm hóa đơn
   */
  search(keyword: string) {
    return ApiClient.get<HoaDon[]>('/api/hoa-don/search', {
      params: { key: keyword },
    })
  }

  /**
   * Lấy danh sách hóa đơn theo khách hàng ID
   */
  getByKhachHangId(khachHangId: number) {
    return ApiClient.get<HoaDon[]>(`/api/hoa-don/khach-hang/${khachHangId}`)
  }

  /**
   * Tìm hóa đơn theo bàn + trạng thái
   */
  findByBanAndStatus(idBan: number, trangThaiHoaDon: number) {
    return ApiClient.get<HoaDon>(`/api/hoa-don/ban/${idBan}/trang-thai/${trangThaiHoaDon}`)
  }

  split(idHoaDon: number, items: Array<{ idHoaDonChiTiet: number; soLuong: number }>) {
    return ApiClient.post<HoaDon>(`/api/hoa-don/${idHoaDon}/split`, { items })
  }
}

export default new HoaDonApi()
