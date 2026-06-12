import ApiClient from './ApiClient'

// Vì ApiClient đã có sẵn "http://localhost:8080/api" nên ở đây chỉ cần điền đuôi /khu_vuc hoặc /khu-vuc
// Bạn check lại xem trong Backend viết @RequestMapping là "/api/khu_vuc" (gạch dưới) hay "/api/khu-vuc" (gạch ngang) nhé!
const API_URL = '/api/khu_vuc' 

// 1. Xem danh sách toàn bộ khu vực
export const getAllKhuVuc = () => {
  return ApiClient.get(API_URL)
}

// 2. Xem chi tiết 1 khu vực theo ID
export const getKhuVucById = (id: number) => {
  return ApiClient.get(`${API_URL}/${id}`)
}

// 3. Thêm mới khu vực
export const createKhuVuc = (data: any) => {
  return ApiClient.post(API_URL, data)
}

// 4. Cập nhật thông tin khu vực
export const updateKhuVuc = (id: number, data: any) => {
  return ApiClient.put(`${API_URL}/${id}`, data)
}

// 5. Xóa khu vực
export const deleteKhuVuc = (id: number) => {
  return ApiClient.delete(`${API_URL}/${id}`)
}

// 6. Đổi trạng thái Hoạt động <-> Khóa
export const changeStatusKhuVuc = (id: number) => {
  return ApiClient.patch(`${API_URL}/change-status/${id}`)
}