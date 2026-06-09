import axiosClient from "./axiosClient";
const ChucVuApi= {
    getAll(){
        return axiosClient.get("/chuc-vu/hien-thi")
    },
    findById(id: number){
        return axiosClient.get(`/chuc-vu/detail/${id}`)
    },
    add(data: any){
        return axiosClient.post("/chuc-vu/add", data)
    },
    update(id: number, data: any){
        return axiosClient.put(`/chuc-vu/update/${id}`, data)
    },
    delete(id: number){
        return axiosClient.delete(`/chuc-vu/delete/${id}`)
    }
}
export default ChucVuApi