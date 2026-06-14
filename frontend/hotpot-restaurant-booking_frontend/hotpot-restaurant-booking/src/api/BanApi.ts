import ApiClient from "./ApiClient"

export interface Ban {
  idBan: number
  loaiBan: string
  soLuongBan: number
  idKhuVuc: number
  tenKhuVuc: string
  trangThai: number
}

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
    },

    search(keyword: string){
        return ApiClient.get<Ban[]>("/api/bans/search", {
            params: { key: keyword }
        })
    },

    sort(){
        return ApiClient.get<Ban[]>("/api/bans/sort")
    }
}

export default BanApi;
