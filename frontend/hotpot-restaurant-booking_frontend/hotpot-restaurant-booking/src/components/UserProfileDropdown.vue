<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { getAllKhachHang } from '@/api/khachhang'
import HoaDonApi from '@/api/HoaDonApi'

const router = useRouter()
const authStore = useAuthStore()
const showDropdown = ref(false)
const customerInfo = ref<any>(null)
const recentInvoices = ref<any[]>([])
const loading = ref(false)

// Load dữ liệu khi dropdown mở
const loadUserData = async () => {
  if (showDropdown.value && !customerInfo.value && authStore.customerInfo.khachHangId) {
    loading.value = true
    try {
      // Lấy thông tin khách hàng
      const customersRes = await getAllKhachHang()
      const allCustomers = customersRes.data || []
      customerInfo.value = allCustomers.find(kh => kh.idKhachHang === authStore.customerInfo.khachHangId)

      // Lấy lịch sử hoá đơn
      const invoicesRes = await HoaDonApi.getByKhachHangId(authStore.customerInfo.khachHangId)
      recentInvoices.value = (invoicesRes.data || []).slice(0, 5) // Lấy 5 hoá đơn gần đây nhất
    } catch (error) {
      console.error('Lỗi khi tải dữ liệu:', error)
    } finally {
      loading.value = false
    }
  }
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    loadUserData()
  }
}

const handleLogout = () => {
  authStore.logout()
  showDropdown.value = false
  router.push('/')
}

const goToProfile = () => {
  router.push('/customer-profile')
  showDropdown.value = false
}

const formatCurrency = (value: any) => {
  if (!value) return '0 đ'
  const num = parseFloat(value)
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(num)
}

const formatDate = (date: any) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    const [year, month, day] = date
    return `${day}/${month}/${year}`
  }
  return new Date(date).toLocaleDateString('vi-VN')
}

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-paid' : 'status-pending'
}

const getStatusText = (status: number) => {
  return status === 1 ? 'Đã thanh toán' : 'Chưa thanh toán'
}
</script>

<template>
  <div class="user-profile-dropdown">
    <!-- Profile Trigger Button -->
    <div class="profile-trigger" @click="toggleDropdown">
      <div class="profile-avatar">
        {{ authStore.customerInfo.tenKhachHang?.charAt(0).toUpperCase() || 'U' }}
      </div>
      <span class="profile-name">{{ authStore.customerInfo.tenKhachHang || 'Khách hàng' }}</span>
      <svg 
        xmlns="http://www.w3.org/2000/svg" 
        width="16" 
        height="16" 
        viewBox="0 0 24 24" 
        fill="none" 
        stroke="currentColor" 
        stroke-width="2"
        :class="['dropdown-arrow', { 'open': showDropdown }]"
      >
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>

    <!-- Dropdown Menu -->
    <div v-if="showDropdown" class="dropdown-menu">
      <!-- User Info Header -->
      <div class="dropdown-header">
        <div class="user-info">
          <div class="avatar-large">{{ authStore.customerInfo.tenKhachHang?.charAt(0).toUpperCase() || 'U' }}</div>
          <div class="user-details">
            <p class="user-name">{{ authStore.customerInfo.tenKhachHang }}</p>
            <p class="user-phone">{{ authStore.customerInfo.soDienThoai || '-' }}</p>
          </div>
        </div>
        <button class="btn-profile" @click="goToProfile" title="Xem hồ sơ">
          ⚙️
        </button>
      </div>

      <!-- Divider -->
      <div class="dropdown-divider"></div>

      <!-- Recent Invoices Section -->
      <div class="invoices-section">
        <h4>Lịch Sử Hoá Đơn Gần Đây</h4>
        
        <div v-if="loading" class="loading">Đang tải...</div>
        <div v-else-if="recentInvoices.length === 0" class="empty-invoices">
          Chưa có hoá đơn nào
        </div>
        <div v-else class="invoices-list">
          <div v-for="invoice in recentInvoices" :key="invoice.idHoaDon" class="invoice-card">
            <div class="invoice-header">
              <span class="invoice-id">{{ invoice.maHoaDon }}</span>
              <span :class="['status-badge', getStatusClass(invoice.trangThaiThanhToan)]">
                {{ getStatusText(invoice.trangThaiThanhToan) }}
              </span>
            </div>
            <div class="invoice-details">
              <div class="detail-row">
                <span class="label">Ngày:</span>
                <span class="value">{{ formatDate(invoice.thoiGianXuat) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Bàn:</span>
                <span class="value">{{ invoice.loaiBan || '-' }}</span>
              </div>
              <div class="detail-row">
                <span class="label">Tổng tiền:</span>
                <span class="value total">{{ formatCurrency(invoice.tongTien) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- View All Link -->
        <button class="btn-view-all" @click="goToProfile">
          Xem tất cả lịch sử →
        </button>
      </div>

      <!-- Divider -->
      <div class="dropdown-divider"></div>

      <!-- Logout Button -->
      <button class="btn-logout" @click="handleLogout">
        🚪 ĐĂNG XUẤT
      </button>
    </div>

    <!-- Backdrop -->
    <div v-if="showDropdown" class="dropdown-backdrop" @click="showDropdown = false"></div>
  </div>
</template>

<style scoped>
.user-profile-dropdown {
  position: relative;
}

/* Trigger Button */
.profile-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
  background: rgba(197, 160, 89, 0.1);
  border: 1px solid rgba(197, 160, 89, 0.3);
}

.profile-trigger:hover {
  background: rgba(197, 160, 89, 0.2);
  border-color: rgba(197, 160, 89, 0.6);
}

.profile-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  color: white;
}

.profile-name {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.5px;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-arrow {
  transition: transform 0.3s ease;
  color: #c5a059;
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

/* Dropdown Menu */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  z-index: 10001;
  min-width: 320px;
  max-width: 380px;
  overflow: hidden;
  animation: slideIn 0.2s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Header */
.dropdown-header {
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.avatar-large {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  color: white;
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;
}

.user-name {
  margin: 0;
  font-weight: 600;
  font-size: 14px;
}

.user-phone {
  margin: 0;
  font-size: 12px;
  opacity: 0.9;
}

.btn-profile {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: background 0.3s;
  flex-shrink: 0;
}

.btn-profile:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Divider */
.dropdown-divider {
  height: 1px;
  background: #e0e0e0;
}

/* Invoices Section */
.invoices-section {
  padding: 15px;
  max-height: 360px;
  overflow-y: auto;
}

.invoices-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
}

.empty-invoices {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
}

.invoices-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
}

.invoice-card {
  background: #f9f9f9;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 10px;
  transition: all 0.3s ease;
}

.invoice-card:hover {
  background: #f5f5f5;
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
}

.invoice-id {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.status-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-paid {
  background: #c8e6c9;
  color: #2e7d32;
}

.status-pending {
  background: #ffccbc;
  color: #d84315;
}

.invoice-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.detail-row .label {
  font-weight: 500;
  color: #999;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
}

.detail-row .total {
  color: #667eea;
  font-weight: 600;
}

.btn-view-all {
  width: 100%;
  padding: 8px;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  color: #667eea;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-view-all:hover {
  background: #f5f5f5;
  border-color: #667eea;
}

/* Logout Button */
.btn-logout {
  width: 100%;
  padding: 12px 15px;
  background: #f44336;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: background 0.3s;
}

.btn-logout:hover {
  background: #da190b;
}

/* Backdrop */
.dropdown-backdrop {
  position: fixed;
  inset: 0;
  z-index: 10000;
}

/* Scrollbar styling */
.invoices-section::-webkit-scrollbar {
  width: 6px;
}

.invoices-section::-webkit-scrollbar-track {
  background: transparent;
}

.invoices-section::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.invoices-section::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}

/* Responsive */
@media (max-width: 768px) {
  .dropdown-menu {
    min-width: 280px;
    right: -20px;
  }

  .profile-name {
    display: none;
  }

  .invoices-section {
    max-height: 300px;
  }
}
</style>
