export interface OrderBan {
  idBan: number
  tenBan: string
  sucChua: number
  trangThai: 'TRONG' | 'DA_DAT' | 'DANG_SU_DUNG'
}

export interface OrderKhuVuc {
  idKhuVuc: number
  tenKhuVuc: string
  dsBan: OrderBan[]
}

export interface OrderMon {
  idMon: number
  tenMon: string
  donGiaHienTai: number
  anhMon?: string
}

export interface OrderCombo {
  idCombo: number
  tenCombo: string
  giaCombo: number
  anhCombo?: string
}
