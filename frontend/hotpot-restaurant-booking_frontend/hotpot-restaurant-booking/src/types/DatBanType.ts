export interface DTOBanResponse {
  idBan: number
  tenBan: string
  loaiBan: string
  idKhuVuc: number
  tenKhuVuc: string
  trangThai: string
}

export interface DTOCheckBanResponse {
  trangThai: string

  message: string

  canGhep: boolean

  tongSucChua: number

  dsBan: DTOBanResponse[]
}
export interface DTOTinhTrangBanResponse {
  soBanConLai: number
  tongSucChua: number
}
