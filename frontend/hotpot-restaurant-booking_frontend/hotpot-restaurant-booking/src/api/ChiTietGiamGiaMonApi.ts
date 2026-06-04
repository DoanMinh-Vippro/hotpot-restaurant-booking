import ApiClient from './ApiClient'

// SỬA: Đổi public thành export đúng chuẩn TypeScript
export interface ChiTietGiamGiaMon {
  idChiTietGiamGiaMon: number
  tenChuongTrinh: string
  tenMon: string
  mucGiam: number
}

// SỬA: Đổi public thành export đúng chuẩn TypeScript
export interface ChiTietGiamGiaMonRequest {
  idMon: number
  idDotGiamGia: number
  mucGiam: number
}

class ChiTietGiamGiaMonApi {
  getAll() {
    return ApiClient.get<ChiTietGiamGiaMon[]>('/hienThiCTGGM')
  }

  detail(id: number) {
    return ApiClient.get<ChiTietGiamGiaMon>('/detailCTGGM', {
      params: { idChiTietGiamGiaMon: id }
    })
  }

  search(tenChuongTrinh?: string, tenMon?: string, mucMin?: number, mucMax?: number, pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/timKiemCTGGM', {
      params: {
        tenChuongTrinh: tenChuongTrinh?.trim() || undefined,
        tenMon: tenMon?.trim() || undefined,
        mucMin: mucMin ?? undefined,
        mucMax: mucMax ?? undefined,
        pageNo,
        pageSize
      }
    })
  }

  add(data: ChiTietGiamGiaMonRequest) {
    return ApiClient.post('/addCTGGM', data)
  }

  update(id: number, data: ChiTietGiamGiaMonRequest) {
    return ApiClient.put('/updateCTGGM', data, {
      params: { idChiTietGiamGiaMon: id }
    })
  }
}

// SỬA: Dòng 51 chí mạng đổi từ "public default" thành "export default"
export default new ChiTietGiamGiaMonApi()