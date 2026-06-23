import ApiClient from "./ApiClient";
const TaiKhoanApi= {
    getAll(){
        return ApiClient.get("/api/tai-khoan/hien-thi")
    },
    findById(id: number){
        return ApiClient.get(`/api/tai-khoan/detail/${id}`)
    },
    add(data: any){
        return ApiClient.post("/api/tai-khoan/add", data)
    },
    update(id: number, data: any){
        return ApiClient.put(`/api/tai-khoan/update/${id}`, data)
    },
    delete(id: number){
        return ApiClient.delete(`/api/tai-khoan/delete/${id}`)
    }
}
export default TaiKhoanApi
