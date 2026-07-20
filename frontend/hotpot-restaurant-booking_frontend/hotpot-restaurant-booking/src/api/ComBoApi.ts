import { useAuthStore } from '@/stores/AuthStore'
import ApiClient from './ApiClient'

export interface Combo {
  idCombo: number
  tenCombo: string
  giaCombo: number
  hinhAnh: string
  trangThai: number
  trangThaiBan: number
}

export interface ComboRequest {
  idCombo?: number
  tenCombo: string
  giaCombo: number
  hinhAnh: string
  trangThai: number
  trangThaiBan: number
  fileThat?: File | null 
}

class ComboApi {

  hienThiComBo() {
    return ApiClient.get<Combo[]>('/hienThiComBo')
  }

  detailComBo(tenCombo: string) {
    return ApiClient.get<Combo>('/detailComBo', {
      params: { tenCombo }
    })
  }

  phanTrangComBo(pageNo = 0, pageSize = 5) {
    return ApiClient.get<any>('/phanTrangComBo', {
      params: { pageNo, pageSize }
    })
  }

  // ÉP SẠCH CHUỖI RỖNG ĐỂ TRÁNH LỖI ĐIỀU KIỆN BACKEND
  timKiemComBo(params: any) {
    return ApiClient.get<any>('/timKiemComBo', {
      params: {
        tenCombo: params.tenCombo?.trim() || undefined,
        pageNo: params.pageNo ?? 0,
        pageSize: params.pageSize ?? 5
      }
    })
  }

  addComBo(data: ComboRequest) {
    return ApiClient.post('/addComBo', data)
  }

  updateComBo(idCombo: number, data: ComboRequest) {
    return ApiClient.put('/updateComBo', data, {
      params: { idCombo }
    })
  }

  deleteComBo(idCombo: number) {
    return ApiClient.delete('/deleteComBo', {
      params: { idCombo }
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


export default new ComboApi()