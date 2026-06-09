import axiosClient from "./axiosClient";
const NhanVienApi= {
    getAll(){
        return axiosClient.get("/nhan-vien/hien-thi")
    },
    findById(id: number){
        return axiosClient.get(`/nhan-vien/detail/${id}`)
    },
    add(data: any){
        return axiosClient.post("/nhan-vien/add", data)
    },
    update(id: number, data: any){
        return axiosClient.put(`/nhan-vien/update/${id}`, data)
    },
    delete(id: number){
        return axiosClient.delete(`/nhan-vien/delete/${id}`)
    }
}
export default NhanVienApi