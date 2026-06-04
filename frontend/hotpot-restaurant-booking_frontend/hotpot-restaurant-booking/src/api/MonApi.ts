import ApiClient from './ApiClient'

export interface Mon {
  idMon: number
  tenMon: string
  donGiaHienTai: number
  idDanhMuc: number
  loaiDanhMuc: string
  trangThai: number
}

export interface MonRequest {
  idMon?: number
  tenMon: string
  donGiaHienTai: number
  idDanhMuc: number
  trangThai: number
}

class MonApi {
  hienThiMon() {
    return ApiClient.get<Mon[]>('/hienThiMon')
  }

  detailMon(tenMon: string) {
    return ApiClient.get<Mon>('/detailMon', {
      params: { tenMon },
    })
  }

  phanTrangMon(pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/phanTrangMon', {
      params: {
        pageNo,
        pageSize,
      },
    })
  }

  // Cập nhật kiểu trả về thành <any> để bóc tách cấu trúc Page JSON động
  searchMon(
    tenMon?: string,
    giaMin?: number,
    giaMax?: number,
    loaiDanhMuc?: string,
    pageNo = 0,
    pageSize = 5,
  ) {
    return ApiClient.get<any>('/searchMon', {
      params: {
        tenMon: tenMon?.trim() || undefined,
        giaMin: giaMin ?? undefined,
        giaMax: giaMax ?? undefined,
        loaiDanhMuc: loaiDanhMuc || undefined,
        pageNo,
        pageSize,
      },
    })
  }

  addMon(data: MonRequest) {
    return ApiClient.post('/addMon', data)
  }

  updateMon(idMon: number, data: MonRequest) {
    return ApiClient.put('/updateMon', data, {
      params: { idMon },
    })
  }

  deleteMon(idMon: number) {
    return ApiClient.delete('/deleteMon', {
      params: { idMon },
    })
  }
}

export default new MonApi()