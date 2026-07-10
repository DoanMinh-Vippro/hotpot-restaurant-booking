<script setup lang="ts">
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import HoaDonApi from '@/api/HoaDonApi'
import { onMounted, ref } from 'vue'

/**
 * =====================================================
 * POPUP DANH SÁCH ĐƠN ĐẶT BÀN ĐÃ XÁC NHẬN
 * =====================================================
 */

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'chonDatBan', datBan: any): void
}>()

/**
 * Mock data
 *
 * Sau này:
 * DatBanApi.getByTrangThai('DA_XAC_NHAN')
 */
const danhSachDatBan = ref<any[]>([])

const loadDanhSachDatBan = async () => {
  try {
    const [daRes, hoaDonRes] = await Promise.all([
      DatBanQuanLyApi.findByTrangThai('DA_XAC_NHAN'),
      HoaDonApi.getDanhSach(),
    ])

    const confirmedReservations = Array.isArray(daRes?.data) ? daRes.data : []

    const paidInvoiceDatBanIds = new Set(
      (hoaDonRes?.data ?? [])
        .filter((invoice: any) => invoice?.idDatBan != null && invoice?.trangThaiThanhToan === 1)
        .map((invoice: any) => invoice.idDatBan),
    )

    danhSachDatBan.value = confirmedReservations.filter((item: any) => !paidInvoiceDatBanIds.has(item.idDatBan))
  } catch (e) {
    console.error('Lỗi load danh sách đặt bàn', e)
  }
}

const dongPopup = () => {
  emit('close')
}

const chonDatBan = (datBan: any) => {
  emit('chonDatBan', datBan)
  emit('close')
}
onMounted(() => {
  loadDanhSachDatBan()
})
</script>

<template>
  <div class="overlay" @click="dongPopup">
    <div class="popup" @click.stop>
      <div class="popup-header">Danh sách đơn đã xác nhận</div>

      <div class="popup-body">
        <div v-if="danhSachDatBan.length === 0" class="empty-state">
          Không có đơn đặt bàn phù hợp để chọn.
        </div>

        <div v-for="datBan in danhSachDatBan" :key="datBan.idDatBan" class="dat-ban-card">
          <div class="info">
            <div><strong>Mã đơn:</strong> #{{ datBan.idDatBan }}</div>

            <div><strong>Khách:</strong> {{ datBan.tenKhachHang }}</div>

            <div><strong>Ngày đặt:</strong> {{ datBan.ngayDat }}</div>

            <div><strong>SĐT:</strong> {{ datBan.sdtKhachHang }}</div>

            <div><strong>Trạng thái:</strong> <span class="status-pill">Đã cọc</span></div>
          </div>

          <button class="btn-chon" @click="chonDatBan(datBan)">Chọn</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.popup {
  width: 900px;
  max-height: 80vh;
  overflow-y: auto;
  background: #fffaf1;
  border-radius: 16px;
  border: 1px solid #e6d2aa;
}

.popup-header {
  padding: 20px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: #8b5e34;
  border-bottom: 1px solid #e6d2aa;
}

.popup-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dat-ban-card {
  background: #fff8ea;
  border-radius: 12px;
  padding: 16px;
  color: #5f3d22;
  border: 1px solid #e6d2aa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-pill {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(216, 168, 92, 0.16);
  color: #8b5e34;
  font-size: 12px;
  font-weight: 600;
}

.btn-chon {
  background: #d8a85c;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  color: #3d2814;
}
</style>
