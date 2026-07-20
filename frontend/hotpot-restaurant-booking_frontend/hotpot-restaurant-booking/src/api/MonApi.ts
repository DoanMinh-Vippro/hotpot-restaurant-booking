import { useAuthStore } from '@/stores/AuthStore'
import ApiClient from './ApiClient'

export interface Mon {
  idMon: number
  tenMon: string
  hinhAnh: string
  donGiaHienTai: number
  giaSauGiam: number
  soTienDuocGiam: number
  tenChuongTrinhGiamGia: string
  idDanhMuc: number
  loaiDanhMuc: string
  trangThai: number
  trangThaiBan: number
}

export interface MonRequest {
  idMon?: number
  tenMon: string
  hinhAnh: string
  donGiaHienTai: number
  idDanhMuc: number
  trangThai: number
  trangThaiBan: number
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

  uploadImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    // 2. Lấy token trực tiếp từ kho lưu trữ Pinia giống như cách ApiClient đang hoạt động
    const authStore = useAuthStore()
    const headers: any = { 
      'Content-Type': 'multipart/form-data' 
    }
    
    // Nếu có token thì ép đè thẳng vào headers cấu hình của request này
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }

    return ApiClient.post('/upload', formData, { headers })
}
}

export default new MonApi()