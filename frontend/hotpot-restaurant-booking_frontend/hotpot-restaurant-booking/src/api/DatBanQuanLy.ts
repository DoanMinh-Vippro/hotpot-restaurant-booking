import ApiClient from "./ApiClient"
    const DatBanQuanLyApi = {
        getAll(){
            return ApiClient.get("/api/dat-ban-quan-ly")
        },

        findById(id: number){
            return ApiClient.get(`/api/dat-ban-quan-ly/${id}`)
        },

        add(data: any){
            return ApiClient.post("/api/dat-ban-quan-ly",data)
        },

        update(id: number, data: any){
            return ApiClient.put(`/api/dat-ban-quan-ly/${id}`,data)
        },

        delete(id: number){
            return ApiClient.delete(`/api/dat-ban-quan-ly/${id}`)
        }

    }

        export default DatBanQuanLyApi;
