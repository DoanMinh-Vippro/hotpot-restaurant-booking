<!-- src/components/GoiMon.vue -->
<template>
  <div class="goi-mon-container">
    <div class="goi-mon-header">
      <div class="header-left">
        <span class="header-icon">🛒</span>
        <h4>Gọi món</h4>
      </div>
      <span class="so-luong">{{ gioHang.length }} món</span>
    </div>
    
    <div v-if="gioHang.length > 0" class="goi-mon-list">
      <div 
        v-for="(mon, index) in gioHang" 
        :key="index"
        class="mon-item"
      >
        <div class="mon-info">
          <span class="mon-name">{{ mon.tenMon }}</span>
          <span class="mon-qty">×{{ mon.soLuong }}</span>
        </div>
        <div class="mon-right">
          <span class="mon-price">{{ formatCurrency(mon.thanhTien || mon.gia * mon.soLuong) }}</span>
          <button class="btn-remove" @click="removeMon(index)" title="Xóa">✕</button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-gio-hang">
      <span class="empty-icon">🍽️</span>
      <p>Chưa có món nào được gọi</p>
      <p class="empty-hint">Chọn bàn đang sử dụng để xem</p>
    </div>
    
    <div class="goi-mon-footer">
      <div class="footer-row">
        <span class="label">Tạm tính:</span>
        <span class="value">{{ formatCurrency(tongTien) }}</span>
      </div>
      <div class="footer-row">
        <span class="label">Tiền giảm giá:</span>
        <span class="value discount">0đ</span>
      </div>
      <div class="footer-row total">
        <span class="label">Tổng tiền:</span>
        <span class="value total-amount">{{ formatCurrency(tongTien) }}</span>
      </div>
      
      <div class="footer-actions">
        <button class="btn-discount">Chọn mã giảm giá</button>
        <button class="btn-thanh-toan" @click="handleThanhToan">💳 Thanh toán</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  gioHang: any[]
  tongTien: number
}>()

const emit = defineEmits<{
  (e: 'removeMon', index: number): void
  (e: 'thanhToan'): void
}>()

const formatCurrency = (value: number) => {
  if (!value) return '0đ'
  return value.toLocaleString() + 'đ'
}

const removeMon = (index: number) => {
  emit('removeMon', index)
}

const handleThanhToan = () => {
  emit('thanhToan')
}
</script>

<style scoped>
.goi-mon-container {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.goi-mon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0ebe4;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
}

.goi-mon-header h4 {
  margin: 0;
  font-size: 16px;
  color: #2d1f14;
}

.so-luong {
  font-size: 13px;
  color: #8a7a6a;
  background: #f0ebe4;
  padding: 2px 10px;
  border-radius: 12px;
}

.goi-mon-list {
  flex: 1;
  max-height: 320px;
  overflow-y: auto;
  padding: 8px 0;
}

.goi-mon-list::-webkit-scrollbar {
  width: 4px;
}

.goi-mon-list::-webkit-scrollbar-track {
  background: transparent;
}

.goi-mon-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 4px;
}

.mon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #faf8f6;
  border-radius: 8px;
  margin-bottom: 4px;
  transition: all 0.2s;
}

.mon-item:hover {
  background: #f5f0eb;
}

.mon-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mon-name {
  font-weight: 500;
  color: #2d1f14;
}

.mon-qty {
  color: #8a7a6a;
  font-size: 13px;
}

.mon-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mon-price {
  font-weight: 600;
  color: #059669;
}

.btn-remove {
  padding: 2px 6px;
  border: none;
  background: transparent;
  color: #ef4444;
  cursor: pointer;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-remove:hover {
  background: #fee2e2;
}

.empty-gio-hang {
  text-align: center;
  padding: 40px 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.empty-gio-hang p {
  color: #8a7a6a;
  margin: 4px 0;
}

.empty-hint {
  font-size: 13px;
  color: #b0a090;
}

.goi-mon-footer {
  padding-top: 12px;
  border-top: 2px solid #f0ebe4;
}

.footer-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
}

.footer-row .label {
  color: #8a7a6a;
}

.footer-row .value {
  color: #2d1f14;
  font-weight: 500;
}

.footer-row .value.discount {
  color: #ef4444;
}

.footer-row.total {
  padding: 8px 0 12px;
  font-size: 16px;
  font-weight: 700;
}

.footer-row.total .total-amount {
  color: #059669;
  font-size: 18px;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.btn-discount {
  flex: 1;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #4a3520;
  font-weight: 500;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-discount:hover {
  border-color: #f59e0b;
  background: #fffbeb;
}

.btn-thanh-toan {
  flex: 2;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-thanh-toan:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.btn-thanh-toan:active {
  transform: translateY(0);
}
</style>