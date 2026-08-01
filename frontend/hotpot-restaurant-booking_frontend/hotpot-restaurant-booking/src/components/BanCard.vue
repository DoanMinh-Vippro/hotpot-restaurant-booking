<!-- src/components/BanCard.vue -->
<template>
  <div 
    class="ban-card"
    :class="{
      'trong': ban.trangThai === 'TRONG',
      'da-dat': ban.trangThai === 'DA_DAT',
      'dang-su-dung': ban.trangThai === 'DANG_SU_DUNG'
    }"
    @click="$emit('click')"
    @dblclick="$emit('dblclick')"
  >
    <div class="ban-card-top">
      <span class="ban-number">{{ ban.tenBan }}</span>
      <span class="ban-capacity">👥 {{ ban.soLuongNguoi }}</span>
    </div>
    
    <div class="ban-status-badge">
      <span class="status-dot" :class="ban.trangThai.toLowerCase()"></span>
      <span class="status-text">{{ getStatusText(ban.trangThai) }}</span>
    </div>
    
    <!-- Thông tin check-in -->
    <div v-if="ban.checkInInfo" class="ban-checkin-info">
      <div class="checkin-customer">
        <span class="customer-icon">👤</span>
        <span class="customer-name">{{ ban.checkInInfo.tenKhachHang || 'KH' }}</span>
      </div>
      <div class="checkin-orders">
        <span v-if="ban.checkInInfo.monDaGoi?.length" class="order-count">
          🍽️ {{ ban.checkInInfo.monDaGoi.length }}
        </span>
        <span v-if="ban.checkInInfo.tienCoc" class="deposit-amount">
          💰 {{ formatCurrency(ban.checkInInfo.tienCoc) }}
        </span>
      </div>
    </div>
    
    <div v-else-if="ban.trangThai === 'DA_DAT' && ban.datBanInfo" class="ban-checkin-info">
      <div class="checkin-customer">
        <span class="customer-icon">👤</span>
        <span class="customer-name">{{ ban.datBanInfo.tenKhachHang || 'KH' }}</span>
      </div>
      <div v-if="ban.datBanInfo.tienCoc" class="checkin-orders">
        <span class="deposit-amount">💰 {{ formatCurrency(ban.datBanInfo.tienCoc) }}</span>
      </div>
    </div>
    
    <!-- Hover hint -->
    <div class="ban-hint">
      <span v-if="ban.trangThai === 'TRONG'">Nhấp đúp để đặt</span>
      <span v-else-if="ban.trangThai === 'DA_DAT'">Nhấp đúp để nhận bàn</span>
      <span v-else>Nhấp đúp để thanh toán</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  ban: any
}>()

defineEmits<{
  (e: 'click'): void
  (e: 'dblclick'): void
}>()

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'TRONG': 'Trống',
    'DA_DAT': 'Đã đặt',
    'DANG_SU_DUNG': 'Đang dùng'
  }
  return map[status] || status
}

const formatCurrency = (value: number) => {
  return value?.toLocaleString() + 'đ' || '0đ'
}
</script>

<style scoped>
.ban-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 2px solid #f0ebe4;
  position: relative;
}

.ban-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.ban-card.trong {
  border-color: #86efac;
  background: linear-gradient(135deg, #f0fdf4, #f0faf0);
}

.ban-card.trong:hover {
  border-color: #22c55e;
  box-shadow: 0 8px 25px rgba(34, 197, 94, 0.15);
}

.ban-card.da-dat {
  border-color: #fcd34d;
  background: linear-gradient(135deg, #fffbeb, #fef5e6);
}

.ban-card.da-dat:hover {
  border-color: #f59e0b;
  box-shadow: 0 8px 25px rgba(245, 158, 11, 0.15);
}

.ban-card.dang-su-dung {
  border-color: #fca5a5;
  background: linear-gradient(135deg, #fef2f2, #fde8e8);
}

.ban-card.dang-su-dung:hover {
  border-color: #ef4444;
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.15);
}

.ban-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ban-number {
  font-weight: 700;
  font-size: 16px;
  color: #2d1f14;
}

.ban-capacity {
  font-size: 13px;
  color: #8a7a6a;
}

.ban-status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.trong { background: #22c55e; }
.status-dot.da-dat { background: #f59e0b; }
.status-dot.dang-su-dung { background: #ef4444; }

.status-text {
  font-size: 12px;
  font-weight: 500;
  color: #4a3520;
}

.ban-checkin-info {
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.checkin-customer {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  color: #2d1f14;
}

.customer-icon {
  font-size: 14px;
}

.customer-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.checkin-orders {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
}

.order-count {
  color: #059669;
  font-weight: 500;
}

.deposit-amount {
  color: #059669;
  font-weight: 600;
}

.ban-hint {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%) translateY(100%);
  background: #2d1f14;
  color: #fff;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 11px;
  white-space: nowrap;
  opacity: 0;
  transition: all 0.2s;
  pointer-events: none;
  font-weight: 500;
}

.ban-card:hover .ban-hint {
  opacity: 1;
  transform: translateX(-50%) translateY(100%);
}
</style>