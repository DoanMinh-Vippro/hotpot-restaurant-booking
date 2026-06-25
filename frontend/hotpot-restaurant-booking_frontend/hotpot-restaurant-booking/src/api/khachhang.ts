import ApiClient from './ApiClient'

// Vì ApiClient đã ôm sẵn "http://localhost:8080/api" nên ở đây bạn chỉ cần điền phần đuôi
// Bạn check lại xem ở Backend Spring Boot đang viết gạch dưới /khach_hang hay gạch ngang /khach-hang nhé!
const API_URL = '/api/khach_hang'

// 1. Hàm trợ giúp kiểm tra số điện thoại (10 số, bắt đầu bằng 0)
const validateSoDienThoai = (soDienThoai: string) => {
  const sdtRegex = /^0\d{9}$/
  if (!soDienThoai) {
    throw new Error('Số điện thoại không được để trống!')
  }
  if (!sdtRegex.test(soDienThoai)) {
    throw new Error('Số điện thoại phải có đúng 10 chữ số và bắt đầu bằng số 0!')
  }
}

// 2. Hàm trợ giúp kiểm tra định dạng Email @gmail.com
const validateEmail = (email: string) => {
  // Vì email là "Nếu có" (không bắt buộc), nên nếu để trống hoặc null thì bỏ qua, không bắt lỗi
  if (!email || email.trim() === '') {
    return
  }
  
  const emailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/
  if (!emailRegex.test(email)) {
    throw new Error('Email phải đúng định dạng và có đuôi @gmail.com!')
  }
}

// 1. Xem danh sách tất cả khách hàng
export const getAllKhachHang = () => {
  return ApiClient.get(API_URL)
}

// 2. Xem chi tiết khách hàng theo ID
export const getKhuVucById = (id: number) => {
  return ApiClient.get(`${API_URL}/${id}`)
}

// 3. Thêm mới khách hàng
export const createKhachHang = (data: any) => {
  return ApiClient.post(API_URL, data)
}

// 4. Cập nhật thông tin khách hàng
export const updateKhachHang = (id: number, data: any) => {
  return ApiClient.put(`${API_URL}/${id}`, data)
}

// 5. Xóa khách hàng
export const deleteKhachHang = (id: number) => {
  return ApiClient.delete(`${API_URL}/${id}`)
}

// 6. Tìm kiếm khách hàng theo từ khóa
export const searchKhachHang = (keyword: string) => {
  return ApiClient.get(`${API_URL}/search`, { params: { keyword } })
}