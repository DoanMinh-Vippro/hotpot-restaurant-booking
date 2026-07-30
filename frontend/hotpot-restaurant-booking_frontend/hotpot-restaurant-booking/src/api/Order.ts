import apiClient from './ApiClient'

export default {
  getBan() {
    return apiClient.get('/api/order/ban')
  },

  chonBan(idBan: number) {
    return apiClient.get('/api/order/hoa-don', {
      params: {
        idBan,
      },
    })
  },

  getChiTietHoaDon(idHoaDon: number) {
    return apiClient.get(`/api/order/hoa-don/${idHoaDon}/chi-tiet`)
  },

  getMenu() {
    return apiClient.get('/api/order/menu')
  },

  themMon(data: any) {
    return apiClient.post('/api/order/them-mon', data)
  },

  themCombo(data: any) {
    return apiClient.post('/api/order/them-combo', data)
  },
}
