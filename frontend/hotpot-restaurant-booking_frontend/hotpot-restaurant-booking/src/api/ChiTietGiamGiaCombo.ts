import ApiClient from './ApiClient'

// Các interface bổ sung cho đối tượng gom nhóm (để đồng bộ với Table UI)
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

export interface ChiTietGiamGiaCombo {
  idChiTietGiamGiaCombo: number
  idDotGiamGia?: number
  tenChuongTrinh: string
  mucGiam: number
  loaiGiam: string
  trangThai: number

  // Trường đơn lẻ (Legacy/Đơn lẻ)
  idCombo?: number
  tenCombo?: string
  tenMon?: string
  tenDanhMuc?: string

  // Mảng gom nhóm hỗ trợ hiển thị badge trên Table UI
  danhSachMon?: (MonGiamGia | string)[]
  danhSachCombo?: (ComboGiamGia | string)[]
  danhSachDanhMuc?: (DanhMucGiamGia | string)[]
}

export interface ChiTietGiamGiaComboRequest {
  idCombo?: number
  idMon?: number
  idDanhMuc?: number
  idDotGiamGia: number
  mucGiam: number
  loaiGiam: string
  trangThai: number
}

class ChiTietGiamGiaComboApi {
  hienThiCTGGC() {
    return ApiClient.get<ChiTietGiamGiaCombo[]>('/hienThiCTGGC')
  }

  detailCTGGC(idChiTietGiamGiaCombo: number) {
    return ApiClient.get<ChiTietGiamGiaCombo>('/detailCTGGC', {
      params: { idChiTietGiamGiaCombo }
    })
  }

  phanTrangCTGGC(pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/phanTrangCTGGC', {
      params: { pageNo, pageSize }
    })
  }

  timKiemCTGGC(
    tenChuongTrinh?: string,
    tenCombo?: string,
    mucMin?: number,
    mucMax?: number,
    loaiGiam?: string,
    pageNo = 0,
    pageSize = 5
  ) {
    return ApiClient.get<any>('/timKiemCTGGC', {
      params: {
        tenChuongTrinh: tenChuongTrinh?.trim() || undefined,
        tenCombo: tenCombo?.trim() || undefined,
        mucMin: mucMin ?? undefined,
        mucMax: mucMax ?? undefined,
        loaiGiam: loaiGiam || undefined,
        pageNo,
        pageSize
      }
    })
  }

  addCTGGC(data: ChiTietGiamGiaComboRequest) {
    return ApiClient.post('/addCTGGC', data)
  }

  updateCTGGC(idChiTietGiamGiaCombo: number, data: ChiTietGiamGiaComboRequest) {
    return ApiClient.put('/updateCTGGC', data, {
      params: { idChiTietGiamGiaCombo }
    })
  }
}

export default new ChiTietGiamGiaComboApi()