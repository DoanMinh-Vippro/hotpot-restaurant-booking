import apiClient from './ApiClient'
import ComBoApi from './ComBoApi'
import MonApi from './MonApi'

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

  async getMenu() {
    const [resMon, resCombo] = await Promise.all([MonApi.hienThiMon(), ComBoApi.hienThiComBo()])

    return {
      data: {
        dsMon: resMon.data || [],
        dsCombo: resCombo.data || [],
      },
    }
  },

  themMon(data: any) {
    return apiClient.post('/api/order/them-mon', data)
  },

  themCombo(data: any) {
    return apiClient.post('/api/order/them-combo', data)
  },
}
