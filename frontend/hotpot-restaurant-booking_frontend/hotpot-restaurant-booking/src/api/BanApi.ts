import ApiClient from "./ApiClient"
    const BanApi = {
        getAll(){
            return ApiClient.get("/api/bans")
        },

        findById(id: number){
            return ApiClient.get(`/api/bans/${id}`)
        },

        add(data: any){
            return ApiClient.post("/api/bans",data)
        },

        update(id: number, data: any){
            return ApiClient.put(`/api/bans/${id}`,data)
        },

        delete(id: number){
            return ApiClient.delete(`/api/bans/${id}`)
        }

    }

        export default BanApi;
