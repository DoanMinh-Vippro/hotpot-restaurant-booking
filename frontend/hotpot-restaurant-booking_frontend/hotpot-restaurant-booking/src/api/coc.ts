import ApiClient from './ApiClient'
const API_URL = '/api/coc' 

export const getAllTienCoc = () => {
  // Đổi từ axiosClient sang ApiClient cho khớp với dòng 1
  return ApiClient.get(API_URL) 
}

export const getTienCocByTrangThai = (trangThaiCoc: number) => {
  return ApiClient.get(`${API_URL}/trang-thai/${trangThaiCoc}`)
}

export const getTienCocByKhachHang = (khachHangId: number) => {
  return ApiClient.get(`${API_URL}/khach-hang/${khachHangId}`)
}

export const getTongTienDaThu = () => {
  return ApiClient.get(`${API_URL}/tong-tien-da-thu`)
}
//cd frontend\hotpot-restaurant-booking_frontend\hotpot-restaurant-booking