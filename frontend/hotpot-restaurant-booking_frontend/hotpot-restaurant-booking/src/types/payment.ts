export interface PaymentRequest {
  sdtKhachHang: string
  idCombo: number | null
  soNguoi: number
  thoiGianDenDuKien: string
  soTienCoc: number
  phuongThucThanhToan: 'CHUYEN_KHOAN' | 'VNPAY' | 'CHUA_THANH_TOAN'
  ghiChu: string
}

export interface PaymentResponse {
  qrUrl: string
  amount: number
  content: string
}
