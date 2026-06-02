import axiosClient from './axiosClient'

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
  idBan: number | null
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
}

class HoaDonApi {
  /**
   * Lấy danh sách tất cả hóa đơn
   */
  getDanhSach() {
    return axiosClient.get<HoaDon[]>('/hoa-don')
  }

  /**
   * Lấy chi tiết hóa đơn (danh sách items)
   */
  getChiTiet(idHoaDon: number) {
    return axiosClient.get<HoaDonChiTiet[]>(`/hoa-don/${idHoaDon}/chi-tiet`)
  }

  /**
   * Lấy hóa đơn theo ID
   */
  getById(idHoaDon: number) {
    return axiosClient.get<HoaDon>(`/hoa-don/${idHoaDon}`)
  }

  /**
   * Tạo hóa đơn mới
   */
  create(data: Omit<HoaDon, 'idHoaDon' | 'ngayTao' | 'ngayCapNhat' | 'gioTao' | 'gioCapNhat'>) {
    return axiosClient.post<HoaDon>('/hoa-don', data)
  }

  /**
   * Cập nhật hóa đơn
   */
  update(idHoaDon: number, data: Partial<HoaDon>) {
    return axiosClient.put<HoaDon>(`/hoa-don/${idHoaDon}`, data)
  }

  /**
   * Xóa hóa đơn
   */
  delete(idHoaDon: number) {
    return axiosClient.delete(`/hoa-don/${idHoaDon}`)
  }

  /**
   * Tìm kiếm hóa đơn
   */
  search(keyword: string) {
    return axiosClient.get<HoaDon[]>('/hoa-don/search', {
      params: { key: keyword },
    })
  }
}

export default new HoaDonApi()
