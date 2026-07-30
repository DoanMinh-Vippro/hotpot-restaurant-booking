import ApiClient from './ApiClient'

// Các interface bổ sung cho đối tượng gom nhóm
export interface MonGiamGia {
  idMon?: number
  tenMon: string
}

export interface ComboGiamGia {
  idCombo?: number
  tenCombo: string
}

export interface DanhMucGiamGia {
  idDanhMuc?: number
  tenDanhMuc: string
}

export interface ChiTietGiamGiaMon {
  idChiTietGiamGiaMon: number
  tenChuongTrinh: string
  mucGiam: number
  loaiGiam: string
  trangThai: number

  // Trường đơn lẻ (Legacy/Đơn lẻ)
  tenMon?: string
  tenCombo?: string
  tenDanhMuc?: string

  // Mảng gom nhóm hiển thị badge trên table
  danhSachMon?: (MonGiamGia | string)[]
  danhSachCombo?: (ComboGiamGia | string)[]
  danhSachDanhMuc?: (DanhMucGiamGia | string)[]
}

export interface ChiTietGiamGiaMonRequest {
  idMon?: number
  idCombo?: number
  idDanhMuc?: number
  idDotGiamGia: number
  mucGiam: number
  loaiGiam: string
  trangThai: number
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

  search(
    tenChuongTrinh?: string,
    tenMon?: string,
    mucMin?: number,
    mucMax?: number,
    loaiGiam?: string,
    pageNo = 0,
    pageSize = 5
  ) {
    return ApiClient.get<any>('/timKiemCTGGM', {
      params: {
        tenChuongTrinh: tenChuongTrinh?.trim() || undefined,
        tenMon: tenMon?.trim() || undefined,
        mucMin: mucMin ?? undefined,
        mucMax: mucMax ?? undefined,
        loaiGiam: loaiGiam || undefined,
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

  delete(id: number) {
    return ApiClient.delete('/deleteCTGGM', {
      params: { idChiTietGiamGiaMon: id }
    })
  }
}

export default new ChiTietGiamGiaMonApi()