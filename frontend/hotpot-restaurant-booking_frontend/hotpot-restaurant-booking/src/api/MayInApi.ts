import ApiClient from './ApiClient'

export interface MonInItem {
  tenMon: string
  soLuong: number
}

export interface MayInRequest {
  tenQuay: string
  maHoaDon: string
  tenBan: string
  tenNhanVien: string
  thoiGian: string
  danhSachMon: MonInItem[]
}

const MayInApi = {
  sendTicket(data: MayInRequest) {
    return ApiClient.post('/api/print/send-ticket', data) //[cite: 3, 4]
  },
}

export default MayInApi