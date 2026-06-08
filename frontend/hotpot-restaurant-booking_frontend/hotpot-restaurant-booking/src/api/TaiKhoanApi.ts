import axiosClient from "./axiosClient";
const TaiKhoanApi= {
    getAll(){
        return axiosClient.get("/tai-khoan/hien-thi")
    },
    findById(id: number){
        return axiosClient.get(`/tai-khoan/detail/${id}`)
    },
    add(data: any){
        return axiosClient.post("/tai-khoan/add", data)
    },
    update(id: number, data: any){
        return axiosClient.put(`/tai-khoan/update/${id}`, data)
    },
    delete(id: number){
        return axiosClient.delete(`/tai-khoan/delete/${id}`)
    }
}
export default TaiKhoanApi