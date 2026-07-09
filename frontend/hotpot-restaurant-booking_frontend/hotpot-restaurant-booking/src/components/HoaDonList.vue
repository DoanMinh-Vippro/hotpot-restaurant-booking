<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { HoaDon } from '../api/HoaDonApi'
const props = defineProps<{
  hoaDons: HoaDon[]
  loading: boolean
  selectedId: number | null
}>()
const emit = defineEmits<{
  'select': [id: number]
}>()
const searchQuery = ref('')
const filterPaymentStatus = ref<string>('all')
const filterPaymentMethod = ref<string>('all')
const filterTableType = ref<string>('all')
const sortBy = ref<string>('newest')
const getTimestamp = (value: string | number[] | null | undefined): number => {
  if (!value) return 0
  if (Array.isArray(value)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value
    return new Date(year, month - 1, day, hour, minute, second).getTime()
  }
  const parsed = new Date(value).getTime()
  return isNaN(parsed) ? 0 : parsed
}
const filteredHoaDons = computed(() => {
  // 1. Filter
  let result = props.hoaDons.filter((item) => {
    // Search Query filter
    const q = searchQuery.value.trim().toLowerCase()
    if (q) {
      const matchSearch = [
        item.maHoaDon,
        item.tenKhachHang,
        item.sdtKhachHang,
        item.maGiaoDich,
        item.tenNhanVien
      ]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(q))
      if (!matchSearch) return false
    }
    // Payment Status filter
      if (filterPaymentStatus.value !== 'all') {
        const statusNum = Number(filterPaymentStatus.value)
        if (item.trangThaiThanhToan !== statusNum) return false
      }
      // Payment Method filter
      if (filterPaymentMethod.value !== 'all') {
        const methodNum = Number(filterPaymentMethod.value)
        if (item.phuongThucThanhToan !== methodNum) return false
      }
      // Table Type (loaiBan) filter
      if (filterTableType.value !== 'all') {
        if (filterTableType.value === 'MANG_VE') {
          if (item.loaiBan !== null && item.loaiBan !== '') return false
        } else {
          if (item.loaiBan !== filterTableType.value) return false
        }
      }
      return true
    })
    // 2. Sort
    result.sort((a, b) => {
      if (sortBy.value === 'newest') {
        const timeA = getTimestamp(a.thoiGianXuat)
        const timeB = getTimestamp(b.thoiGianXuat)
        return timeB - timeA || b.idHoaDon - a.idHoaDon
      } else if (sortBy.value === 'oldest') {
        const timeA = getTimestamp(a.thoiGianXuat)
        const timeB = getTimestamp(b.thoiGianXuat)
        return timeA - timeB || a.idHoaDon - b.idHoaDon
      } else if (sortBy.value === 'total_desc') {
        const totalA = Number(a.tongTien ?? 0)
        const totalB = Number(b.tongTien ?? 0)
        return totalB - totalA
      } else if (sortBy.value === 'total_asc') {
        const totalA = Number(a.tongTien ?? 0)
        const totalB = Number(b.tongTien ?? 0)
        return totalA - totalB
      }
      return 0
    })
    return result
  })
// Auto-select the first filtered invoice if the currently selected is no longer in the list
  watch(filteredHoaDons, (newList) => {
    if (newList.length > 0) {
      const exists = newList.some((item) => item.idHoaDon === props.selectedId)
      const firstItem = newList[0]
      if (!exists && firstItem?.idHoaDon != null) {
        emit('select', firstItem.idHoaDon)
      }
    }
  })
  const formatCurrency = (value: number | string | null) =>
    new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(Number(value ?? 0))
  const paymentStatusLabel = (status: number | null) => {
    if (status === 1) return 'Đã thanh toán'
    if (status === 0) return 'Chưa thanh toán'
    return 'Không rõ'
  }
  const handleSelect = (id: number) => {
    emit('select', id)
  }
</script>
<template>
  <aside class="danh-sach-hoa-don" aria-label="Danh sách hóa đơn">
    <div class="tieu-de-panel">
      <h2>Tìm kiếm hóa đơn</h2>
      <span v-if="loading">Đang tải...</span>
    </div>
    <input
      class="o-tim-kiem"
      type="search"
      v-model="searchQuery"
      placeholder="Nhập mã hóa đơn, tên khách, số điện thoại..."
    />
    <div class="bo-loc-hoa-don">
      <div class="row-bo-loc">
        <div class="nhom-loc">
          <label for="filter-payment-status">Thanh toán</label>
          <select id="filter-payment-status" v-model="filterPaymentStatus">
            <option value="all">Tất cả</option>
            <option value="1">Đã thanh toán</option>
            <option value="0">Chưa thanh toán</option>
          </select>
        </div>
        <div class="nhom-loc">
          <label for="filter-table-type">Phân loại bàn</label>
          <select id="filter-table-type" v-model="filterTableType">
            <option value="all">Tất cả</option>
            <option value="HAI_NGUOI">Bàn 2 người</option>
            <option value="BON_NGUOI">Bàn 4 người</option>
            <option value="SAU_NGUOI">Bàn 6 người</option>
            <option value="MANG_VE">Mang về/Khác</option>
          </select>
        </div>
      </div>
      <div class="row-bo-loc">
        <div class="nhom-loc">
          <label for="filter-payment-method">Phương thức</label>
          <select id="filter-payment-method" v-model="filterPaymentMethod">
            <option value="all">Tất cả</option>
            <option value="1">Tiền mặt</option>
            <option value="2">Chuyển khoản</option>
            <option value="3">Thẻ</option>
          </select>
        </div>
        <div class="nhom-loc">
          <label for="sort-by">Sắp xếp</label>
          <select id="sort-by" v-model="sortBy">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="total_desc">Tổng tiền (Cao → Thấp)</option>
            <option value="total_asc">Tổng tiền (Thấp → Cao)</option>
          </select>
        </div>
      </div>
    </div>
    <button
      v-for="hoaDon in filteredHoaDons"
      :key="hoaDon.idHoaDon"
      :class="['hang-hoa-don', { active: hoaDon.idHoaDon === selectedId }]"
      type="button"
      @click="handleSelect(hoaDon.idHoaDon)"
    >
      <span>
        <strong>{{ hoaDon.maHoaDon }}</strong>
        <small>{{ hoaDon.tenKhachHang ?? hoaDon.sdtKhachHang ?? 'Khách lẻ' }}</small>
      </span>
      <span class="hang-phai">
        <b>{{ formatCurrency(hoaDon.tongTien) }}</b>
        <small>{{ paymentStatusLabel(hoaDon.trangThaiThanhToan) }}</small>
      </span>
    </button>
    <p v-if="!loading && filteredHoaDons.length === 0" class="trang-trong">
      Không tìm thấy hóa đơn phù hợp.
    </p>
  </aside>
</template>
<style scoped>
.danh-sach-hoa-don {
  border: 1px solid #e6d2aa;
  background: rgba(255, 248, 234, 0.96);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 220px);
  overflow: auto;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}
.tieu-de-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.tieu-de-panel h2 {
  margin: 0;
  font-size: 1rem;
  letter-spacing: 0;
  color: #8b5e34;
}
.tieu-de-panel span {
  color: #8f6b46;
  font-size: 0.82rem;
}
.o-tim-kiem {
  width: 100%;
  margin-bottom: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #e6d2aa;
  background: #fffdf8;
  color: #5f3d22;
}
.o-tim-kiem::placeholder {
  color: #8f6b46;
}
.hang-hoa-don {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  width: 100%;
  min-height: 76px;
  border-radius: 10px;
  margin-bottom: 10px;
  padding: 14px;
  text-align: left;
  background: #fff8ea;
  border: 1px solid #e6d2aa;
  color: #5f3d22;
  cursor: pointer;
}
.hang-hoa-don.active {
  border-color: #d8a85c;
  background: rgba(216, 168, 92, 0.16);
}
.hang-hoa-don strong,
.hang-hoa-don b,
.hang-hoa-don small {
  display: block;
}
.hang-hoa-don small {
  margin-top: 6px;
  color: #8f6b46;
}
.hang-phai {
  text-align: right;
}
.trang-trong {
  color: #8f6b46;
  margin: 14px 0;
}
.bo-loc-hoa-don {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.row-bo-loc {
  display: flex;
  gap: 10px;
}
.nhom-loc {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nhom-loc label {
  font-size: 0.72rem;
  color: #8f6b46;
  font-weight: 600;
  text-transform: uppercase;
}
.nhom-loc select {
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #e6d2aa;
  background: #fffdf8;
  color: #5f3d22;
  font-size: 0.85rem;
  outline: none;
  cursor: pointer;
}
.nhom-loc select option {
  background: #fffdf8;
  color: #5f3d22;
}
.nhom-loc select:focus {
  border-color: #d8a85c;
}
@media (max-width: 900px) {
  .danh-sach-hoa-don {
    max-height: none;
  }
}
</style>
