import ApiClient from './ApiClient'

export interface ChiTietComBo {
  idChiTietCombo: number
  soLuong: number
  tenMon: string
  tenCombo: string
  giaCombo: number
  hinhAnh: string
  trangThai: number
  moTa: string
}

export interface ChiTietComBoRequest {
  idChiTietCombo?: number
  soLuong: number
  idMon: number
  idCombo: number
  moTa: string
}

class ChiTietComBoApi {

  hienThiCTCB() {
    return ApiClient.get<ChiTietComBo[]>('/hienThiCTCB')
  }

  detailCTCB(idChiTietCombo: number) {
    return ApiClient.get<ChiTietComBo>('/detailCTCB', {
      params: { idChiTietCombo },
    })
  }

  phanTrangCTCB(pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/phanTrangCTCB', {
      params: {
        pageNo,
        pageSize,
      },
    })
  }

  // Đổi kiểu nhận về sang <any> để bóc tách .content và .totalPages
  searchCTCB(
    tenCombo?: string,
    tenMon?: string,
    giaMin?: number,
    giaMax?: number,
    pageNo = 0,
    pageSize = 5,
  ) {
    return ApiClient.get<any>('/timKiemCTCB', {
      params: {
        tenCombo: tenCombo?.trim() || undefined,
        tenMon: tenMon?.trim() || undefined,
        giaMin: giaMin ?? undefined,
        giaMax: giaMax ?? undefined,
        pageNo,
        pageSize,
      },
    })
  }

  addCTCB(data: ChiTietComBoRequest) {
    return ApiClient.post('/addCTCB', data)
  }

  updateCTCB(idChiTietCombo: number, data: ChiTietComBoRequest) {
    return ApiClient.put('/updateCTCB', data, {
      params: { idChiTietCombo },
    })
  }

  deleteCTCB(idChiTietCombo: number) {
    return ApiClient.delete('/deleteCTCB', {
      params: { idChiTietCombo },
    })
  }
}

export default new ChiTietComBoApi()