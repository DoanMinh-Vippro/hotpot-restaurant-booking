<!-- src/components/PopupChiTietDonHang.vue -->
<template>
  <div class="popup-overlay" @click.self="handleClose">
    <div class="popup-content">
      <!-- Header -->
      <div class="popup-header">
        <div class="header-left">
          <span class="table-icon">🍽️</span>
          <div>
            <h3 class="table-name">{{ ban?.tenBan || 'Bàn' }}</h3>
            <span class="table-status" :class="ban?.trangThai?.toLowerCase()">
              {{ ban?.trangThai === 'DANG_SU_DUNG' ? 'Đang dùng' : 'Đã đặt' }}
            </span>
          </div>
        </div>
        <button class="btn-close" @click="handleClose">✕</button>
      </div>

      <!-- Thông tin -->
      <div class="order-info">
        <div class="info-row">
          <span class="info-label">🕐 Thời gian:</span>
          <span class="info-value">{{ formatTime(hoaDon?.thoiGianTao) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">👤 Số khách:</span>
          <span class="info-value">{{ ban?.soLuongNguoi || 0 }} người</span>
        </div>
        <div class="info-row">
          <span class="info-label">📋 Mã đơn:</span>
          <span class="info-value">#{{ hoaDon?.idHoaDon || '---' }}</span>
        </div>
        <div class="info-row" v-if="hoaDon?.maHoaDon">
          <span class="info-label">🔖 Mã hóa đơn:</span>
          <span class="info-value">{{ hoaDon?.maHoaDon }}</span>
        </div>
      </div>

      <!-- Danh sách món -->
      <div class="order-items">
        <div class="items-header">
          <span>Món ăn</span>
          <span>Số lượng</span>
          <span>Đơn giá</span>
          <span>Giảm giá</span>
          <span>Thành tiền</span>
        </div>
        <div 
          v-for="(item, index) in chiTietHoaDon" 
          :key="item.idHoaDonChiTiet || index"
          class="item-row"
        >
          <span class="item-name">{{ getTenMon(item) }}</span>
          <span class="item-qty">{{ item.soLuong || 0 }}</span>
          <span class="item-price">{{ formatCurrency(item.giaBanTaiThoiDiem || 0) }}</span>
          <span class="item-discount">{{ formatCurrency(item.tienGiamGiaMon || 0) }}</span>
          <span class="item-total">{{ formatCurrency(item.thanhTien || 0) }}</span>
        </div>
        <div v-if="isLoading" class="loading-items">
          <span>⏳ Đang tải...</span>
        </div>
        <div v-else-if="!chiTietHoaDon?.length" class="empty-items">
          <span>📭 Chưa có món nào được gọi</span>
        </div>
      </div>

      <!-- Tổng cộng -->
      <div class="order-total">
        <div class="total-row">
          <span>Tạm tính</span>
          <span>{{ formatCurrency(tongTien) }}</span>
        </div>
        <div class="total-row discount" v-if="tongGiamGia > 0">
          <span>Giảm giá</span>
          <span>- {{ formatCurrency(tongGiamGia) }}</span>
        </div>
        <div class="total-row grand-total">
          <span>Tổng cộng</span>
          <span>{{ formatCurrency(tongTien - tongGiamGia) }}</span>
        </div>
      </div>

      <!-- Actions -->
      <div class="popup-actions">
        <button class="btn-secondary" @click="handleClose">Đóng</button>
        <button class="btn-primary" @click="handleThemMon">
          ➕ Thêm món
        </button>
        <button class="btn-primary btn-thanh-toan" @click="handleThanhToan">
          💳 Thanh toán
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import HoaDonApi from '@/api/HoaDonApi'
import HoaDonChiTietApi from '@/api/HoaDonChiTietApi'

const props = defineProps<{
  ban: any
}>()

const emit = defineEmits<{
  close: []
  thanhToan: [ban: any]
  themMon: [ban: any]
}>()

const hoaDon = ref<any>(null)
const chiTietHoaDon = ref<any[]>([])
const isLoading = ref(true)

// Tính tổng tiền
const tongTien = computed(() => {
  return chiTietHoaDon.value.reduce((sum, item) => {
    return sum + (item.thanhTien || 0)
  }, 0)
})

// Tính tổng giảm giá
const tongGiamGia = computed(() => {
  return chiTietHoaDon.value.reduce((sum, item) => {
    return sum + (item.tienGiamGiaMon || 0)
  }, 0)
})

// Lấy tên món (có thể là món hoặc combo)
const getTenMon = (item: any) => {
  if (item.tenMon) return item.tenMon
  if (item.tenCombo) return item.tenCombo
  if (item.mon?.tenMon) return item.mon.tenMon
  if (item.combo?.tenCombo) return item.combo.tenCombo
  return `Món #${item.idHoaDonChiTiet || '?'}`
}

const loadHoaDon = async () => {
  if (!props.ban?.idBan) {
    isLoading.value = false
    return
  }
  
  isLoading.value = true
  try {
    // Cách 1: Lấy hóa đơn theo bàn và trạng thái
    const res = await HoaDonApi.findByBanAndStatus(props.ban.idBan, 0)
    
    if (res?.data) {
      hoaDon.value = res.data
      
      // Lấy tất cả chi tiết hóa đơn và lọc theo idHoaDon
      const ctRes = await HoaDonChiTietApi.getAll()
      if (ctRes?.data && Array.isArray(ctRes.data)) {
        chiTietHoaDon.value = ctRes.data.filter(
          (item: any) => item.idHoaDon === hoaDon.value.idHoaDon
        )
      }
    }
  } catch (error) {
    console.error('Lỗi tải hóa đơn (cách 1):', error)
    
    // Cách 2: Fallback - lấy tất cả hóa đơn và tìm
    try {
      const allHoaDonRes = await HoaDonApi.getDanhSach()
      if (allHoaDonRes?.data && Array.isArray(allHoaDonRes.data)) {
        const found = allHoaDonRes.data.find(
          (hd: any) => hd.idBan === props.ban.idBan && hd.trangThaiHoaDon === 0
        )
        if (found) {
          hoaDon.value = found
          const ctRes = await HoaDonChiTietApi.getAll()
          if (ctRes?.data && Array.isArray(ctRes.data)) {
            chiTietHoaDon.value = ctRes.data.filter(
              (item: any) => item.idHoaDon === found.idHoaDon
            )
          }
        }
      }
    } catch (fallbackError) {
      console.error('Lỗi fallback:', fallbackError)
    }
  } finally {
    isLoading.value = false
  }
}

const formatCurrency = (value: number) => {
  if (!value && value !== 0) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(value)
}

const formatTime = (time: string) => {
  if (!time) return '---'
  try {
    const date = new Date(time)
    return date.toLocaleString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch {
    return time
  }
}

const handleClose = () => {
  emit('close')
}

const handleThanhToan = () => {
  emit('thanhToan', props.ban)
}

const handleThemMon = () => {
  emit('themMon', props.ban)
}

onMounted(() => {
  loadHoaDon()
})
</script>

<style scoped>
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.2s ease;
}

.popup-content {
  background: white;
  border-radius: 20px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Scrollbar styling */
.popup-content::-webkit-scrollbar {
  width: 6px;
}

.popup-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.popup-content::-webkit-scrollbar-thumb {
  background: #c4b5a5;
  border-radius: 10px;
}

.popup-content::-webkit-scrollbar-thumb:hover {
  background: #a89787;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  background: #faf6f0;
  border-radius: 20px 20px 0 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-icon {
  font-size: 28px;
}

.table-name {
  margin: 0;
  font-size: 18px;
  color: #4a3520;
}

.table-status {
  font-size: 12px;
  padding: 2px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.table-status.dang-su-dung {
  background: #FFEBEE;
  color: #C62828;
}

.table-status.da-dat {
  background: #FFF3E0;
  color: #E65100;
}

.btn-close {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  background: rgba(0, 0, 0, 0.1);
  transform: rotate(90deg);
}

.order-info {
  padding: 16px 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 24px;
  background: #f9f6f0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.info-label {
  color: #8a7a6a;
}

.info-value {
  font-weight: 500;
  color: #4a3520;
}

.order-items {
  padding: 16px 24px;
}

.items-header {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1.2fr 1.2fr 1.2fr;
  gap: 8px;
  font-weight: 600;
  font-size: 12px;
  color: #8a7a6a;
  padding-bottom: 8px;
  border-bottom: 2px solid #eee;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.item-row {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1.2fr 1.2fr 1.2fr;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f0ea;
  font-size: 14px;
  align-items: center;
}

.item-row:last-child {
  border-bottom: none;
}

.item-name {
  color: #4a3520;
  font-weight: 500;
  word-break: break-word;
}

.item-qty {
  text-align: center;
  color: #4a3520;
}

.item-price {
  text-align: right;
  color: #6a5a4a;
  font-size: 13px;
}

.item-discount {
  text-align: right;
  color: #e65100;
  font-size: 13px;
}

.item-total {
  text-align: right;
  font-weight: 600;
  color: #4a3520;
}

.empty-items, .loading-items {
  text-align: center;
  padding: 24px 0;
  color: #a09080;
  font-size: 14px;
}

.order-total {
  padding: 16px 24px;
  background: #faf6f0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.total-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
  color: #4a3520;
}

.total-row.discount {
  color: #e65100;
}

.total-row.grand-total {
  font-size: 18px;
  font-weight: 700;
  padding-top: 8px;
  border-top: 2px solid #ddd;
  margin-top: 4px;
}

.popup-actions {
  display: flex;
  gap: 10px;
  padding: 16px 24px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.popup-actions button {
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary {
  background: #f0ebe5;
  color: #4a3520;
}

.btn-secondary:hover {
  background: #e5ddd5;
}

.btn-primary {
  background: linear-gradient(135deg, #8B6B4A, #6B4F3A);
  color: white;
  box-shadow: 0 2px 12px rgba(139, 107, 74, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(139, 107, 74, 0.4);
}

.btn-primary.btn-thanh-toan {
  background: linear-gradient(135deg, #F44336, #C62828);
  box-shadow: 0 2px 12px rgba(244, 67, 54, 0.3);
}

.btn-primary.btn-thanh-toan:hover {
  box-shadow: 0 4px 20px rgba(244, 67, 54, 0.4);
}

/* ========== RESPONSIVE ========== */
@media (max-width: 600px) {
  .popup-content {
    max-width: 100%;
    margin: 10px;
    border-radius: 16px;
  }

  .popup-header {
    padding: 16px 20px;
    border-radius: 16px 16px 0 0;
  }

  .order-info {
    grid-template-columns: 1fr;
    gap: 4px;
    padding: 12px 20px;
  }

  .order-items {
    padding: 12px 20px;
  }

  .items-header, .item-row {
    grid-template-columns: 2fr 0.8fr 1fr 1fr 1.2fr;
    font-size: 12px;
    gap: 4px;
  }

  .item-price, .item-discount, .item-total {
    font-size: 11px;
  }

  .order-total {
    padding: 12px 20px;
  }

  .popup-actions {
    padding: 12px 20px;
    flex-direction: column;
  }

  .popup-actions button {
    width: 100%;
    justify-content: center;
  }

  .table-name {
    font-size: 16px;
  }
}

@media (max-width: 400px) {
  .items-header, .item-row {
    grid-template-columns: 1.5fr 0.6fr 0.8fr 0.8fr 1fr;
    font-size: 11px;
    gap: 2px;
  }

  .popup-header {
    padding: 12px 16px;
  }

  .order-info, .order-items, .order-total, .popup-actions {
    padding: 10px 16px;
  }

  .total-row.grand-total {
    font-size: 16px;
  }
}
</style>