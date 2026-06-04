import ApiClient from './ApiClient'

export interface DanhMuc {
  idDanhMuc: number
  loaiDanhMuc: string
  moTa: string
}

export interface DanhMucRequest {
  loaiDanhMuc: string
  moTa: string
}

class DanhMucApi {

  getDanhSach() {
    return ApiClient.get<DanhMuc[]>('/hienThiDM')
  }

  detail(loaiDanhMuc: string) {
    return ApiClient.get<DanhMuc>('/detailDM', {
      params: { loaiDanhMuc }
    })
  }

  phanTrang(pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/phanTrangDM', {
      params: { pageNo, pageSize }
    })
  }

  search(loaiDanhMuc?: string, pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/timKiemDM', {
      params: {
        loaiDanhMuc: loaiDanhMuc?.trim() || undefined,
        pageNo,
        pageSize
      }
    })
  }

  add(data: DanhMucRequest) {
    return ApiClient.post('/addDM', data)
  }

  update(idDanhMuc: number, data: DanhMucRequest) {
    return ApiClient.put('/updateDM', data, {
      params: { idDanhMuc }
    })
  }
}

export default new DanhMucApi()