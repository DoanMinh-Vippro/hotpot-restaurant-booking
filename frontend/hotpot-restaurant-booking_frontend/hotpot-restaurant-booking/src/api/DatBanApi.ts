import ApiClient from './ApiClient'
import type { DTOCheckBanResponse } from '@/types/DatBanType'

const DatBanApi = {
  getAll() {
    return ApiClient.get('/api/dat-bans')
  },

  findById(id: number) {
    return ApiClient.get(`/api/dat-bans/${id}`)
  },

  add(data: any) {
    return ApiClient.post('/api/dat-bans', data)
  },

  update(id: number, data: any) {
    return ApiClient.put(`/api/dat-bans/${id}`, data)
  },

  delete(id: number) {
    return ApiClient.delete(`/api/dat-bans/${id}`)
  },
  checkBan(data: any) {
    return ApiClient.post<DTOCheckBanResponse>('/api/dat-bans/check-ban', data)
  },
}

export default DatBanApi
