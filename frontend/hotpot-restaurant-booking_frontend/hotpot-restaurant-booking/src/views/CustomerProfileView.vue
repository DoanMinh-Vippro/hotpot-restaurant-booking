<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { getAllKhachHang, updateKhachHang } from '@/api/khachhang'
import HoaDonApi from '@/api/HoaDonApi'

const authStore = useAuthStore()
const router = useRouter()

// Dữ liệu khách hàng
const customerInfo = ref<any>(null)
const invoiceHistory = ref<any[]>([])
const loading = ref(true)
const isEditing = ref(false)
const editForm = ref<any>(null)

// Dữ liệu từ form
const formData = ref({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  gioiTinh: true
})

const pageSize = ref(10)
const currentPage = ref(1)

// Computed properties
const paginatedInvoices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return invoiceHistory.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(invoiceHistory.value.length / pageSize.value)
})

// Load dữ liệu
const loadCustomerInfo = async () => {
  try {
    if (!authStore.customerInfo.khachHangId) {
      console.error('Khách hàng ID không tồn tại')
      return
    }
    
    // Lấy từ localStorage/store hoặc từ API nếu chưa có
    if (!customerInfo.value) {
      const res = await getAllKhachHang()
      const allCustomers = res.data || []
      customerInfo.value = allCustomers.find(kh => kh.idKhachHang === authStore.customerInfo.khachHangId)
    }
    
    // Cập nhật form từ store info (thông tin khi vừa đăng ký)
    formData.value = {
      tenKhachHang: authStore.customerInfo.tenKhachHang || customerInfo.value?.tenKhachHang || '',
      soDienThoai: authStore.customerInfo.soDienThoai || customerInfo.value?.soDienThoai || '',
      email: authStore.customerInfo.email || customerInfo.value?.email || '',
      diaChi: authStore.customerInfo.diaChi || customerInfo.value?.diaChi || '',
      gioiTinh: (authStore.customerInfo.gioiTinh ?? customerInfo.value?.gioiTinh) ?? true
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin khách hàng:', error)
  }
}

const loadInvoiceHistory = async () => {
  try {
    if (!authStore.customerInfo.khachHangId) {
      console.error('Khách hàng ID không tồn tại')
      return
    }
    
    const res = await HoaDonApi.getByKhachHangId(authStore.customerInfo.khachHangId)
    invoiceHistory.value = res.data || []
  } catch (error) {
    console.error('Lỗi khi tải lịch sử hoá đơn:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([loadCustomerInfo(), loadInvoiceHistory()])
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!authStore.isAuthenticated || !authStore.customerInfo.khachHangId) {
    router.push('/auth')
    return
  }
  loadData()
})

// Handle edit
const toggleEdit = () => {
  isEditing.value = !isEditing.value
}

const handleSave = async () => {
  if (!formData.value.tenKhachHang || !formData.value.soDienThoai) {
    alert('Vui lòng nhập đầy đủ tên và số điện thoại!')
    return
  }

  try {
    const payload = {
      tenKhachHang: formData.value.tenKhachHang,
      soDienThoai: formData.value.soDienThoai,
      email: formData.value.email || null,
      diaChi: formData.value.diaChi || null,
      gioiTinh: formData.value.gioiTinh
    }
    
    await updateKhachHang(authStore.customerInfo.khachHangId!, payload)
    
    // Cập nhật store
    authStore.setCustomerInfo({
      tenKhachHang: formData.value.tenKhachHang,
      soDienThoai: formData.value.soDienThoai,
      email: formData.value.email,
      diaChi: formData.value.diaChi,
      gioiTinh: formData.value.gioiTinh
    })
    
    alert('Cập nhật thông tin thành công!')
    isEditing.value = false
    await loadCustomerInfo()
  } catch (error) {
    console.error('Lỗi khi cập nhật:', error)
    alert('Cập nhật thất bại!')
  }
}

const handleCancel = () => {
  isEditing.value = false
  // Reset form lại
  formData.value = {
    tenKhachHang: authStore.customerInfo.tenKhachHang || customerInfo.value?.tenKhachHang || '',
    soDienThoai: authStore.customerInfo.soDienThoai || customerInfo.value?.soDienThoai || '',
    email: authStore.customerInfo.email || customerInfo.value?.email || '',
    diaChi: authStore.customerInfo.diaChi || customerInfo.value?.diaChi || '',
    gioiTinh: (authStore.customerInfo.gioiTinh ?? customerInfo.value?.gioiTinh) ?? true
  }
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
    const [year, month, day, hour, minute] = date
    return `${day}/${month}/${year} ${hour}:${String(minute).padStart(2, '0')}`
  }
  return new Date(date).toLocaleDateString('vi-VN')
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}
</script>

<template>
  <div class="profile-container">
    <!-- Back Button -->
    <div class="back-button" @click="router.push('/')">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="19" y1="12" x2="5" y2="12"></line>
        <polyline points="12 19 5 12 12 5"></polyline>
      </svg>
      QUAY LẠI TRANG CHỦ
    </div>

    <!-- Header -->
    <div class="profile-header">
      <div class="header-content">
        <h1>HỒ SƠ CÁ NHÂN</h1>
        <p>Quản lý thông tin và lịch sử giao dịch của bạn</p>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <p>Đang tải dữ liệu...</p>
    </div>

    <!-- Main content -->
    <div v-else class="profile-content">
      <div class="profile-main">
        <!-- Customer Info Section -->
        <section class="info-section">
          <div class="section-header">
            <h2>THÔNG TIN CÁ NHÂN</h2>
            <button 
              v-if="!isEditing"
              @click="toggleEdit" 
              class="btn-edit"
            >
              ✏️ CHỈNH SỬA
            </button>
          </div>

          <!-- View Mode -->
          <div v-if="!isEditing && customerInfo" class="info-display">
            <div class="info-grid">
              <div class="info-box">
                <span class="label">Mã Khách Hàng</span>
                <span class="value">{{ authStore.customerInfo.maKhachHang || customerInfo.maKhachHang || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Tên</span>
                <span class="value">{{ authStore.customerInfo.tenKhachHang || customerInfo.tenKhachHang || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Số Điện Thoại</span>
                <span class="value">{{ authStore.customerInfo.soDienThoai || customerInfo.soDienThoai || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Email</span>
                <span class="value">{{ authStore.customerInfo.email || customerInfo.email || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Giới Tính</span>
                <span class="value">{{ (authStore.customerInfo.gioiTinh ?? customerInfo.gioiTinh) ? 'Nam' : 'Nữ' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Địa Chỉ</span>
                <span class="value">{{ authStore.customerInfo.diaChi || customerInfo.diaChi || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Edit Mode -->
          <div v-else class="info-edit-form">
            <div class="form-group">
              <label>Tên Khách Hàng *</label>
              <input 
                v-model="formData.tenKhachHang" 
                type="text" 
                placeholder="Nhập tên"
                required
              />
            </div>

            <div class="form-group">
              <label>Số Điện Thoại *</label>
              <input 
                v-model="formData.soDienThoai" 
                type="tel" 
                placeholder="Nhập số điện thoại"
                required
              />
            </div>

            <div class="form-group">
              <label>Email</label>
              <input 
                v-model="formData.email" 
                type="email" 
                placeholder="Nhập email"
              />
            </div>

            <div class="form-group">
              <label>Giới Tính</label>
              <select v-model="formData.gioiTinh">
                <option :value="true">Nam</option>
                <option :value="false">Nữ</option>
              </select>
            </div>

            <div class="form-group">
              <label>Địa Chỉ</label>
              <textarea 
                v-model="formData.diaChi" 
                placeholder="Nhập địa chỉ"
                rows="3"
              ></textarea>
            </div>

            <div class="form-actions">
              <button @click="handleSave" class="btn-save">💾 LƯU</button>
              <button @click="handleCancel" class="btn-cancel">❌ HỦY</button>
            </div>
          </div>
        </section>

        <!-- Invoice History Section -->
        <section class="invoice-section">
          <div class="section-header">
            <h2>LỊCH SỬ HÓA ĐƠN</h2>
            <span class="invoice-count">{{ invoiceHistory.length }} hoá đơn</span>
          </div>

          <!-- Empty state -->
          <div v-if="invoiceHistory.length === 0" class="empty-state">
            <p>Bạn chưa có hoá đơn nào</p>
          </div>

          <!-- Invoice Cards -->
          <div v-else class="invoices-container">
            <div v-for="invoice in paginatedInvoices" :key="invoice.idHoaDon" class="invoice-card">
              <div class="card-top">
                <div class="invoice-info">
                  <h4 class="invoice-code">{{ invoice.maHoaDon }}</h4>
                  <p class="invoice-date">{{ formatDate(invoice.thoiGianXuat) }}</p>
                </div>
                <span :class="['status-badge', `status-${invoice.trangThaiThanhToan}`]">
                  {{ invoice.trangThaiThanhToan === 1 ? 'Đã TT' : 'Chưa TT' }}
                </span>
              </div>

              <div class="card-divider"></div>

              <div class="card-details">
                <div class="detail-item">
                  <span class="label">Bàn:</span>
                  <span class="value">{{ invoice.loaiBan || '-' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">Tiền trước:</span>
                  <span class="value">{{ formatCurrency(invoice.tienTruocGiam) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">Giảm giá:</span>
                  <span class="value">{{ formatCurrency(invoice.tienGiamGia) }}</span>
                </div>
                <div class="detail-item total">
                  <span class="label">Tổng:</span>
                  <span class="value">{{ formatCurrency(invoice.tongTien) }}</span>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="pagination">
              <button 
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 1"
                class="btn-page"
              >
                ← Trước
              </button>
              
              <span class="page-info">
                Trang {{ currentPage }} / {{ totalPages }}
              </span>
              
              <button 
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="btn-page"
              >
                Sau →
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  min-height: 100vh;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
  position: relative;
}

.back-button {
  position: absolute;
  top: 30px;
  left: 40px;
  color: #fff;
  opacity: 0.6;
  font-size: 0.75rem;
  letter-spacing: 2px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: 0.3s;
  z-index: 10;
}

.back-button:hover {
  opacity: 1;
  color: #c5a059;
}

.profile-header {
  background: linear-gradient(135deg, #c5a059 0%, #a67b3f 100%);
  color: #000;
  padding: 40px;
  border-radius: 8px;
  margin: 60px auto 40px;
  max-width: 1200px;
  text-align: center;
}

.profile-header h1 {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
}

.profile-header p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  letter-spacing: 0.5px;
}

.loading-container {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.profile-main {
  display: grid;
  gap: 30px;
}

/* Section Styles */
.info-section,
.invoice-section {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.2);
  padding: 30px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(197, 160, 89, 0.3);
}

.section-header h2 {
  margin: 0;
  color: #c5a059;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
}

.invoice-count {
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.btn-edit {
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  border: 1px solid rgba(197, 160, 89, 0.5);
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.btn-edit:hover {
  background: rgba(197, 160, 89, 0.3);
  border-color: #c5a059;
}

/* Info Display */
.info-display {
  padding: 10px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 15px;
}

.info-box {
  background: rgba(197, 160, 89, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.15);
  padding: 15px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-box .label {
  color: #c5a059;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

.info-box .value {
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

/* Edit Form */
.info-edit-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  color: #c5a059;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.form-group input,
.form-group select,
.form-group textarea {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.3);
  color: #e0e0e0;
  padding: 10px 12px;
  border-radius: 5px;
  font-size: 14px;
  font-family: 'Montserrat', sans-serif;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  border-color: #c5a059;
  box-shadow: 0 0 0 3px rgba(197, 160, 89, 0.1);
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 15px;
}

.btn-save,
.btn-cancel {
  padding: 10px 24px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.btn-save {
  background: #4caf50;
  color: white;
}

.btn-save:hover {
  background: #45a049;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-cancel {
  background: rgba(244, 67, 54, 0.3);
  color: #ff6b6b;
  border: 1px solid rgba(244, 67, 54, 0.5);
}

.btn-cancel:hover {
  background: rgba(244, 67, 54, 0.4);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

/* Invoice Cards */
.invoices-container {
  display: grid;
  gap: 12px;
  margin-bottom: 20px;
}

.invoice-card {
  background: rgba(197, 160, 89, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.15);
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s;
}

.invoice-card:hover {
  background: rgba(197, 160, 89, 0.08);
  border-color: rgba(197, 160, 89, 0.3);
  box-shadow: 0 4px 12px rgba(197, 160, 89, 0.1);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.invoice-info {
  flex: 1;
}

.invoice-code {
  margin: 0;
  color: #c5a059;
  font-size: 14px;
  font-weight: 600;
}

.invoice-date {
  margin: 3px 0 0;
  color: #999;
  font-size: 12px;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  white-space: nowrap;
}

.status-1 {
  background: rgba(76, 175, 80, 0.2);
  color: #81c784;
}

.status-0 {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
}

.card-divider {
  height: 1px;
  background: rgba(197, 160, 89, 0.15);
  margin: 12px 0;
}

.card-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.detail-item .label {
  color: #999;
  font-weight: 500;
}

.detail-item .value {
  color: #e0e0e0;
  font-weight: 600;
}

.detail-item.total {
  grid-column: 1 / -1;
  border-top: 1px solid rgba(197, 160, 89, 0.15);
  padding-top: 8px;
  margin-top: 4px;
}

.detail-item.total .value {
  color: #c5a059;
  font-size: 14px;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 20px 0;
  border-top: 1px solid rgba(197, 160, 89, 0.15);
}

.btn-page {
  padding: 8px 16px;
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  border: 1px solid rgba(197, 160, 89, 0.4);
  border-radius: 5px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-page:hover:not(:disabled) {
  background: rgba(197, 160, 89, 0.3);
  border-color: #c5a059;
}

.btn-page:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-info {
  color: #999;
  font-size: 13px;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .profile-container {
    padding: 15px;
  }

  .back-button {
    top: 15px;
    left: 15px;
    font-size: 0.65rem;
  }

  .profile-header {
    margin-top: 50px;
    padding: 25px;
  }

  .profile-header h1 {
    font-size: 24px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-edit-form {
    grid-template-columns: 1fr;
  }

  .invoice-card {
    padding: 12px;
  }

  .card-details {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>

