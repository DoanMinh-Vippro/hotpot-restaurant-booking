// src/stores/menuStore.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import MonApi from '../api/MonApi'
import ComBoApi from '../api/ComBoApi'
import type { Mon } from '../api/MonApi'
import type { Combo } from '../api/ComBoApi'

export const useMenuStore = defineStore('menu', () => {
  const danhSachMon = ref<Mon[]>([])
  const danhSachCombo = ref<Combo[]>([])
  const loading = ref(false)

  // 1. Tải danh sách món ăn từ API
  const taiDanhSachMon = async (tenMon = '', loaiDanhMuc = '') => {
    loading.value = true
    try {
      const res = await MonApi.searchMon(tenMon, undefined, undefined, loaiDanhMuc, 0, 100)
      // Bảo vệ dữ liệu: ưu tiên content (nếu phân trang), sau đó là mảng direct, cuối cùng là mảng rỗng
      danhSachMon.value = res?.data?.content || (Array.isArray(res?.data) ? res.data : [])
    } catch (error) {
      console.error('MenuStore: Lỗi tải danh sách món ăn:', error)
      danhSachMon.value = [] // Reset về mảng rỗng nếu lỗi để tránh crash giao diện
    } finally {
      loading.value = false
    }
  }

  // 2. Tải danh sách combo từ API
  const taiDanhSachCombo = async (tenCombo = '') => {
    loading.value = true
    try {
      const res = await ComBoApi.timKiemComBo({
        tenCombo: tenCombo,
        pageNo: 0,
        pageSize: 100
      })
      const responseData = res?.data
      danhSachCombo.value = responseData?.content || (Array.isArray(responseData) ? responseData : [])
    } catch (error) {
      console.error('MenuStore: Lỗi nạp danh sách combo:', error)
      danhSachCombo.value = [] // Reset về mảng rỗng nếu lỗi
    } finally {
      loading.value = false
    }
  }

  // 3. Hàm tải lại đồng thời cả món lẻ và combo (Quản lý loading tập trung)
  const lamMoiToanBoThucDon = async () => {
    loading.value = true
    try {
      // Tách logic fetch riêng biệt ra khỏi hàm lẻ để tránh việc loading bị tắt sớm giữa chừng
      await Promise.all([
        (async () => {
          const res = await MonApi.searchMon('', undefined, undefined, '', 0, 100)
          danhSachMon.value = res?.data?.content || (Array.isArray(res?.data) ? res.data : [])
        })(),
        (async () => {
          const res = await ComBoApi.timKiemComBo({ tenCombo: '', pageNo: 0, pageSize: 100 })
          const responseData = res?.data
          danhSachCombo.value = responseData?.content || (Array.isArray(responseData) ? responseData : [])
        })()
      ])
    } catch (error) {
      console.error('MenuStore: Lỗi làm mới toàn bộ thực đơn:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    danhSachMon,
    danhSachCombo,
    loading,
    taiDanhSachMon,
    taiDanhSachCombo,
    lamMoiToanBoThucDon
  }
})