    import axiosClient from "./axiosClient";

    const BanApi = {
        getAll(){
            return axiosClient.get("/bans")
        },

        findById(id: number){
            return axiosClient.get(`/bans/${id}`)
        },

        add(data: any){
            return axiosClient.post("/bans",data)
        },

        update(id: number, data: any){
            return axiosClient.put(`/bans/${id}`,data)
        },

        delete(id: number){
            return axiosClient.delete(`/bans/${id}`)
        }

    }

        export default BanApi;
