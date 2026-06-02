    import axiosClient from "./axiosClient";

    const BanApi = {
        getAll(){
            return axiosClient.get("/bans")
        },

        findById(id){
            return axiosClient.get(`/bans/${id}`)
        },

        add(data){
            return axiosClient.post("/bans",data)
        },

        update(id,data){
            return axiosClient.put(`/bans/${id}`,data)
        },

        delete(id){
            return axiosClient.delete(`/bans/${id}`)
        }

    }

        export default BanApi;
