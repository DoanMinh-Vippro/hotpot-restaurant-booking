import ApiClient from './ApiClient'

export interface HoaDonChiTiet {
  idHoaDonChiTiet?: number

  maHoaDonChiTiet: string

  idMon?: number | null

  idCombo?: number | null

  idHoaDon: number

  soLuong: number

  giaBanTaiThoiDiem: number

  tienGiamGiaMon: number

  thanhTien: number

  orderedBy?: string | null

  orderedAt?: string | null
}

const HoaDonChiTietApi = {
  getAll() {
    return ApiClient.get('/api/hoa-don-chi-tiet')
  },

  findById(id: number) {
    return ApiClient.get(`/api/hoa-don-chi-tiet/${id}`)
  },

  add(data: HoaDonChiTiet) {
    return ApiClient.post('/api/hoa-don-chi-tiet', data)
  },

  update(id: number, data: HoaDonChiTiet) {
    return ApiClient.put(`/api/hoa-don-chi-tiet/${id}`, data)
  },

  delete(id: number) {
    return ApiClient.delete(`/api/hoa-don-chi-tiet/${id}`)
  },

  deleteByHoaDon(idHoaDon: number) {
    return ApiClient.delete(`/api/hoa-don-chi-tiet/hoa-don/${idHoaDon}`)
  },
}

export default HoaDonChiTietApi
