import ApiClient from "./ApiClient";
const NhanVienApi= {
    getAll(){
        return ApiClient.get("/api/nhan-vien/hien-thi")
    },
    findById(id: number){
        return ApiClient.get(`/api/nhan-vien/detail/${id}`)
    },
    getByTaiKhoanId(idTaiKhoan: number){
        return ApiClient.get(`/api/nhan-vien/tai-khoan/${idTaiKhoan}`)
    },
    add(data: any){
        return ApiClient.post("/api/nhan-vien/add", data)
    },
    update(id: number, data: any){
        return ApiClient.put(`/api/nhan-vien/update/${id}`, data)
    },
    delete(id: number){
        return ApiClient.delete(`/api/nhan-vien/delete/${id}`)
    }
}
export default NhanVienApi
