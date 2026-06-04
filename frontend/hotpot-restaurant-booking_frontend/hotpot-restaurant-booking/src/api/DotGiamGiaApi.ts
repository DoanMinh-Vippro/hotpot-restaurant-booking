import ApiClient from './ApiClient'

export interface DotGiamGia {
  idDotGiamGia: number
  tenChuongTrinh: string
  ngayBatDau: string
  ngayKetThuc: string
}

export interface DotGiamGiaRequest {
  tenChuongTrinh: string
  ngayBatDau: string
  ngayKetThuc: string
}

class DotGiamGiaApi {

  getDanhSach() {
    return ApiClient.get<DotGiamGia[]>('/hienThiDGG')
  }

  detail(tenChuongTrinh: string) {
    return ApiClient.get<DotGiamGia>('/detailDGG', {
      params: { tenChuongTrinh }
    })
  }

  phanTrang(pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/phanTrangDGG', {
      params: { pageNo, pageSize }
    })
  }

  // Đổi kiểu nhận về sang <any> để bóc tách .content và .totalPages động
  search(tenChuongTrinh?: string, tuNgay?: string, denNgay?: string, pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/timKiemDGG', {
      params: {
        tenChuongTrinh: tenChuongTrinh?.trim() || undefined,
        tuNgay: tuNgay || undefined,
        denNgay: denNgay || undefined,
        pageNo,
        pageSize
      }
    })
  }

  add(data: DotGiamGiaRequest) {
    return ApiClient.post('/addDGG', data)
  }

  update(idDotGiamGia: number, data: DotGiamGiaRequest) {
    return ApiClient.put('/updateDGG', data, {
      params: { idDotGiamGia }
    })
  }
}

export default new DotGiamGiaApi()