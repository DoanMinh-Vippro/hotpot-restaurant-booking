    import axiosClient from "./axiosClient";

    const DatBanApi = {
        getAll(){
            return axiosClient.get("/dat-bans")
        },

        findById(id: number){
            return axiosClient.get(`/dat-bans/${id}`)
        },

        add(data: any){
            return axiosClient.post("/dat-bans",data)
        },

        update(id: number, data: any){
            return axiosClient.put(`/dat-bans/${id}`,data)
        },

        delete(id: number){
            return axiosClient.delete(`/dat-bans/${id}`)
        }

    }

        export default DatBanApi;
