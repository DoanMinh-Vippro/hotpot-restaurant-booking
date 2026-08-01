<!-- src/components/AllBillsView.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import BanApi from '@/api/BanApi'
import { getAllKhuVuc } from '@/api/khuvuc'

const danhSachHoaDon = ref<any[]>([])
const danhSachBan = ref<any[]>([])
const danhSachKhuVuc = ref<any[]>([])
const isLoading = ref(false)
const searchKeyword = ref('')
const selectedKhuVuc = ref<number | null>(null)
const sortBy = ref('thoiGian')

// ======================== COMPUTED ========================
const filteredHoaDon = computed(() => {
  let list = [...danhSachHoaDon.value]
  
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    list = list.filter(hd => {
      const ban = danhSachBan.value.find(b => b.idBan === hd.idBan)
      return ban?.tenBan?.toLowerCase().includes(keyword) ||
             hd.maHoaDon?.toLowerCase().includes(keyword) ||
             hd.tenKhachHang?.toLowerCase().includes(keyword)
    })
  }
  
  if (selectedKhuVuc.value) {
    list = list.filter(hd => {
      const ban = danhSachBan.value.find(b => b.idBan === hd.idBan)
      return ban?.idKhuVuc === selectedKhuVuc.value
    })
  }
  
  list.sort((a, b) => {
    if (sortBy.value === 'thoiGian') {
      return new Date(b.thoiGianXuat).getTime() - new Date(a.thoiGianXuat).getTime()
    } else if (sortBy.value === 'tongTien') {
      return Number(b.tongTien || 0) - Number(a.tongTien || 0)
    } else if (sortBy.value === 'tenBan') {
      const banA = danhSachBan.value.find(b => b.idBan === a.idBan)
      const banB = danhSachBan.value.find(b => b.idBan === b.idBan)
      return (banA?.tenBan || '').localeCompare(banB?.tenBan || '')
    }
    return 0
  })
  
  return list
})

const tongSoBanDangSuDung = computed(() => {
  return danhSachHoaDon.value.length
})

const tongDoanhThu = computed(() => {
  return danhSachHoaDon.value.reduce((sum, hd) => sum + Number(hd.tongTien || 0), 0)
})

// ======================== METHODS ========================
const loadHoaDon = async () => {
  isLoading.value = true
  try {
    const res = await DatBanQuanLyApi.getAll('DA_NHAN_BAN')
    if (res?.data) {
      danhSachHoaDon.value = res.data.filter((item: any) => item?.trangThai === 'DA_NHAN_BAN')
    } else {
      danhSachHoaDon.value = []
    }
  } catch (error) {
    console.error('Không thể tải đơn check-in:', error)
    danhSachHoaDon.value = []
  } finally {
    isLoading.value = false
  }
}

const loadBan = async () => {
  try {
    const res = await BanApi.getAll()
    danhSachBan.value = Array.isArray(res?.data) ? res.data : []
  } catch (error) {
    console.error('Không thể tải bàn:', error)
  }
}

const loadKhuVuc = async () => {
  try {
    const res = await getAllKhuVuc()
    danhSachKhuVuc.value = res.data || []
  } catch (error) {
    console.error('Không thể tải khu vực:', error)
  }
}

const refreshData = async () => {
  await Promise.all([loadHoaDon(), loadBan(), loadKhuVuc()])
}

const getBanName = (idBan: number | null | undefined) => {
  if (!idBan) return 'Chưa có bàn'
  const ban = danhSachBan.value.find(b => b.idBan === idBan)
  return ban?.tenBan || `Bàn #${idBan}`
}

const getKhuVucName = (idBan: number | null | undefined) => {
  if (!idBan) return 'Không xác định'
  const ban = danhSachBan.value.find(b => b.idBan === idBan)
  const kv = danhSachKhuVuc.value.find(k => k.idKhuVuc === ban?.idKhuVuc)
  return kv?.tenKhuVuc || 'Không xác định'
}

const formatCurrency = (amount: number | string | null | undefined) => {
  const numericAmount = Number(amount || 0)
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(numericAmount)
}

const formatTime = (dateString: string | number[] | null) => {
  if (!dateString) return 'Chưa xác định'
  
  try {
    let date: Date
    
    if (Array.isArray(dateString)) {
      // Nếu là mảng [year, month, day, hour, minute]
      // Ví dụ: [2024, 1, 15, 14, 30]
      const year = dateString[0] || 0
      const month = dateString[1] || 1
      const day = dateString[2] || 1
      const hour = dateString[3] || 0
      const minute = dateString[4] || 0
      
      // month trong JS bắt đầu từ 0, nên phải trừ 1
      date = new Date(year, month - 1, day, hour, minute)
    } else {
      // Nếu là string, parse bình thường
      date = new Date(dateString)
    }
    
    // Kiểm tra nếu date invalid
    if (isNaN(date.getTime())) {
      return 'Chưa xác định'
    }
    
    return date.toLocaleString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch (error) {
    return 'Chưa xác định'
  }
}

// ======================== HOOKS ========================
let refreshInterval: number | null = null

onMounted(async () => {
  await refreshData()
  refreshInterval = window.setInterval(refreshData, 30000)
})

import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
})
</script>

<template>
  <div class="all-bills-container">
    <!-- Thống kê nhanh -->
    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-value">{{ tongSoBanDangSuDung }}</span>
        <span class="stat-label">Bàn đang dùng</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ formatCurrency(tongDoanhThu) }}</span>
        <span class="stat-label">Tổng doanh thu</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ danhSachHoaDon.length }}</span>
        <span class="stat-label">Đơn đã check-in</span>
      </div>
    </div>

    <!-- Filter -->
    <div class="filter-row">
      <input 
        type="text" 
        class="search-input"
        v-model="searchKeyword"
        placeholder="🔍 Tìm kiếm theo bàn, mã hóa đơn..."
      />
      <select class="filter-select" v-model="selectedKhuVuc">
        <option :value="null">Tất cả khu vực</option>
        <option 
          v-for="kv in danhSachKhuVuc" 
          :key="kv.idKhuVuc" 
          :value="kv.idKhuVuc"
        >
          {{ kv.tenKhuVuc }}
        </option>
      </select>
      <select class="filter-select" v-model="sortBy">
        <option value="thoiGian">Theo thời gian</option>
        <option value="tongTien">Theo tổng tiền</option>
        <option value="tenBan">Theo tên bàn</option>
      </select>
      <button class="btn-refresh" @click="refreshData" :disabled="isLoading">
        🔄 Làm mới
      </button>
    </div>

    <!-- Danh sách hóa đơn -->
    <div class="bills-list">
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
      
      <div v-else-if="filteredHoaDon.length === 0" class="empty-state">
        <span class="empty-icon">🎉</span>
        <p>Không có đơn nào đã check-in</p>
      </div>
      
      <div v-else class="bill-cards">
        <div 
          v-for="hd in filteredHoaDon" 
          :key="hd.idHoaDon"
          class="bill-card"
        >
          <div class="bill-card-header">
            <div class="bill-info-left">
              <span class="bill-ban">{{ getBanName(hd.dsBan?.[0]?.idBan || hd.idBan) }}</span>
              <span class="bill-khuvuc">{{ getKhuVucName(hd.dsBan?.[0]?.idBan || hd.idBan) }}</span>
            </div>
            <span class="bill-time">{{ formatTime(hd.thoiGianDenDuKien || hd.ngayDat) }}</span>
          </div>
          
          <div class="bill-card-body">
            <div class="bill-details">
              <span class="bill-customer">👤 {{ hd.tenKhachHang || 'Khách lẻ' }}</span>
              <span class="bill-code">📝 Đơn #{{ hd.idDatBan }}</span>
            </div>
            <div class="bill-total">
              <span class="total-amount">{{ formatCurrency(hd.soTienCoc || 0) }}</span>
            </div>
          </div>
          
          <div class="bill-card-footer">
            <span class="bill-items">👥 {{ hd.soNguoi || 0 }} người</span>
            <button class="btn-view" @click="$emit('viewBill', hd)">
              Xem chi tiết →
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.all-bills-container {
  padding: 16px 20px;
  background: white;
}

/* ========== STATS ========== */
.stats-row {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #faf6f0;
  border-radius: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-weight: 700;
  font-size: 18px;
  color: #4a3520;
}

.stat-label {
  font-size: 14px;
  color: #8a7a6a;
}

/* ========== FILTER ========== */
.filter-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input,
.filter-select {
  padding: 8px 14px;
  border: 1px solid #e8ddd0;
  border-radius: 8px;
  font-size: 13px;
  background: white;
  color: #4a3520;
  outline: none;
  flex: 1;
  min-width: 150px;
}

.search-input:focus,
.filter-select:focus {
  border-color: #8B6B4A;
}

.btn-refresh {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: #f5efe6;
  color: #4a3520;
  cursor: pointer;
  font-size: 13px;
  transition: 0.2s;
  white-space: nowrap;
}

.btn-refresh:hover {
  background: #e8ddd0;
}

/* ========== BILLS LIST ========== */
.bills-list {
  min-height: 150px;
}

.bill-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.bill-card {
  background: #faf6f0;
  border-radius: 10px;
  padding: 12px 16px;
  transition: 0.2s;
  border: 1px solid #f0ebe4;
}

.bill-card:hover {
  background: #f5efe6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.bill-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.bill-info-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bill-ban {
  font-weight: 600;
  color: #4a3520;
}

.bill-khuvuc {
  font-size: 12px;
  color: #8a7a6a;
  background: white;
  padding: 1px 10px;
  border-radius: 10px;
}

.bill-time {
  font-size: 12px;
  color: #8a7a6a;
}

.bill-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.bill-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bill-customer {
  font-size: 14px;
  color: #4a3520;
}

.bill-code {
  font-size: 12px;
  color: #8a7a6a;
}

.total-amount {
  font-size: 18px;
  font-weight: 700;
  color: #8B6B4A;
}

.bill-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #e8ddd0;
}

.bill-items {
  font-size: 13px;
  color: #8a7a6a;
}

.btn-view {
  padding: 4px 14px;
  border: none;
  border-radius: 6px;
  background: #8B6B4A;
  color: white;
  font-size: 12px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-view:hover {
  background: #6B4F3A;
}

/* ========== STATES ========== */
.loading-state,
.empty-state {
  text-align: center;
  padding: 30px 20px;
  color: #8a7a6a;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #e8ddd0;
  border-top-color: #8B6B4A;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-icon {
  font-size: 40px;
  display: block;
  margin-bottom: 8px;
}

/* ========== RESPONSIVE ========== */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
  }
  
  .search-input,
  .filter-select {
    min-width: 100%;
  }
  
  .bill-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .all-bills-container {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .all-bills-container {
    padding: 8px 12px;
  }
  
  .stat-item {
    font-size: 13px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .total-amount {
    font-size: 16px;
  }
}
</style>