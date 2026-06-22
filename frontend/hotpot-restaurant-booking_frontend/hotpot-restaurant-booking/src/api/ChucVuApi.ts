import ApiClient from "./ApiClient";
const ChucVuApi= {
    getAll(){
        return ApiClient.get("/api/chuc-vu/hien-thi")
    },
    findById(id: number){
        return ApiClient.get(`/api/chuc-vu/detail/${id}`)
    },
    add(data: any){
        return ApiClient.post("/api/chuc-vu/add", data)
    },
    update(id: number, data: any){
        return ApiClient.put(`/api/chuc-vu/update/${id}`, data)
    },
    delete(id: number){
        return ApiClient.delete(`/api/chuc-vu/delete/${id}`)
    }
}
export default ChucVuApi
