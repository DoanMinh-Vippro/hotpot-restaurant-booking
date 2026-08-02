<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { getAllKhachHang } from '@/api/khachhang'
import HoaDonApi from '@/api/HoaDonApi'

const router = useRouter()
const authStore = useAuthStore()
const showMenu = ref(false)
const customerInfo = ref<any>(null)
const recentInvoices = ref<any[]>([])
const loading = ref(false)

const displayName = computed(() => {
  return authStore.customerInfo.tenKhachHang || authStore.accountName || 'Người dùng'
})

const displayRole = computed(() => {
  const role = String(authStore.userRole || '').toUpperCase()
  if (role.includes('ADMIN')) return 'Quản trị viên'
  if (role.includes('STAFF')) return 'Nhân viên'
  if (role.includes('CASHIER') || role.includes('THUNGAN')) return 'Thu ngân'
  if (role === 'USER' || role === 'ROLE_USER') return 'Khách hàng'
  return authStore.userRole || 'Tài khoản'
})

const loadUserData = async () => {
  if (!authStore.customerInfo.khachHangId) return

  loading.value = true
  try {
    const customersRes = await getAllKhachHang()
    const allCustomers = customersRes.data || []
    customerInfo.value = allCustomers.find((kh: any) => kh.idKhachHang === authStore.customerInfo.khachHangId)

    const invoicesRes = await HoaDonApi.getByKhachHangId(authStore.customerInfo.khachHangId)
    recentInvoices.value = (invoicesRes.data || []).slice(0, 4)
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu tài khoản:', error)
  } finally {
    loading.value = false
  }
}

const toggleMenu = () => {
  showMenu.value = !showMenu.value
  if (showMenu.value) {
    loadUserData()
  }
}

const handleLogout = () => {
  authStore.logout()
  showMenu.value = false
  router.push('/')
}

const formatCurrency = (value: any) => {
  if (!value) return '0 đ'
  const num = parseFloat(value)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}

const formatDate = (date: any) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    const [year, month, day] = date
    return `${day}/${month}/${year}`
  }
  return new Date(date).toLocaleDateString('vi-VN')
}

const getStatusClass = (status: number) => (status === 1 ? 'status-paid' : 'status-pending')
const getStatusText = (status: number) => (status === 1 ? 'Đã thanh toán' : 'Chưa thanh toán')

onMounted(() => {
  loadUserData()
})
</script>

<template>
  <div class="admin-account-panel">
    <button class="account-trigger" @click="toggleMenu">
      <div class="account-avatar">{{ displayName.charAt(0).toUpperCase() }}</div>
      <div class="account-text">
        <div class="account-name">{{ displayName }}</div>
        <div class="account-role">{{ displayRole }}</div>
      </div>
      <span class="account-chevron">▾</span>
    </button>

    <div v-if="showMenu" class="account-menu">
      <div class="account-menu-header">
        <div class="account-menu-avatar">{{ displayName.charAt(0).toUpperCase() }}</div>
        <div class="account-menu-info">
          <div class="account-menu-name">{{ displayName }}</div>
          <div class="account-menu-phone">{{ authStore.customerInfo.soDienThoai || authStore.accountName || '-' }}</div>
        </div>
      </div>

      <div class="account-history">
        <div class="history-title">Lịch sử hóa đơn gần đây</div>
        <div v-if="loading" class="history-loading">Đang tải...</div>
        <div v-else-if="recentInvoices.length === 0" class="history-empty">Chưa có hóa đơn nào</div>
        <div v-else class="history-list">
          <div v-for="invoice in recentInvoices" :key="invoice.idHoaDon" class="history-item">
            <div class="history-item-top">
              <span class="history-id">{{ invoice.maHoaDon || `#${invoice.idHoaDon}` }}</span>
              <span :class="['history-status', getStatusClass(invoice.trangThaiThanhToan)]">{{ getStatusText(invoice.trangThaiThanhToan) }}</span>
            </div>
            <div class="history-meta">{{ formatDate(invoice.thoiGianXuat) }}</div>
            <div class="history-meta">{{ formatCurrency(invoice.tongTien) }}</div>
          </div>
        </div>
      </div>

      <button class="logout-btn" @click="handleLogout">🚪 Đăng xuất</button>
    </div>
  </div>
</template>

<style scoped>
.admin-account-panel {
  position: relative;
}

.account-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff6df;
  border: 1px solid #e2cfa6;
  color: #5f3d22;
  padding: 10px 12px;
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  margin-top: 6px;
}

.account-avatar,
.account-menu-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #c97d32, #f3cf85);
  color: #4d2d16;
  font-weight: 800;
  flex-shrink: 0;
}

.account-text {
  flex: 1;
  min-width: 0;
}

.account-name,
.account-menu-name {
  font-size: 0.9rem;
  font-weight: 700;
  color: #5f3d22;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.account-role,
.account-menu-phone {
  font-size: 0.74rem;
  color: #8d6844;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.account-chevron {
  color: #8d6844;
}

.account-menu {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 0;
  right: 0;
  background: #fffaf1;
  border: 1px solid #e4c78b;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: 0 10px 30px rgba(103, 72, 32, 0.16);
  z-index: 20;
}

.account-menu-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.account-menu-info {
  min-width: 0;
}

.account-history {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-title {
  font-size: 0.78rem;
  color: #8d6844;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.history-loading,
.history-empty {
  font-size: 0.82rem;
  color: #8a6a4a;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-item {
  padding: 8px;
  border-radius: 8px;
  background: rgba(255, 244, 220, 0.85);
}

.history-item-top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.history-id {
  font-size: 0.8rem;
  font-weight: 700;
  color: #5f3d22;
}

.history-status {
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 999px;
}

.status-paid {
  background: rgba(76, 175, 80, 0.16);
  color: #357a38;
}

.status-pending {
  background: rgba(255, 193, 7, 0.2);
  color: #9a6b07;
}

.history-meta {
  font-size: 0.74rem;
  color: #7c6042;
}

.logout-btn {
  border: none;
  border-radius: 8px;
  padding: 8px 10px;
  cursor: pointer;
  text-align: left;
  background: #232327;
  color: #fff;
}

.logout-btn:hover {
  background: #2f2f35;
}
</style>
