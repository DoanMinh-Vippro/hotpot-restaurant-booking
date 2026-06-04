    import axiosClient from "./axiosClient";

    const DatBanQuanLyApi = {
        getAll(){
            return axiosClient.get("/dat-ban-quan-ly")
        },

        findById(id: number){
            return axiosClient.get(`/dat-ban-quan-ly/${id}`)
        },

        add(data: any){
            return axiosClient.post("/dat-ban-quan-ly",data)
        },

        update(id: number, data: any){
            return axiosClient.put(`/dat-ban-quan-ly/${id}`,data)
        },

        delete(id: number){
            return axiosClient.delete(`/dat-ban-quan-ly/${id}`)
        }

    }

        export default DatBanQuanLyApi;
