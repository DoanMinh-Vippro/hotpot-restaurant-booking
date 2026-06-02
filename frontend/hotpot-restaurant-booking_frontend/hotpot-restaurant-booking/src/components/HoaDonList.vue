<script setup lang="ts">
import { computed, ref } from 'vue'
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

const filteredHoaDons = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return props.hoaDons

  return props.hoaDons.filter((item) => {
    return [
      item.maHoaDon,
      item.tenKhachHang,
      item.sdtKhachHang,
      item.maGiaoDich,
    ]
      .filter(Boolean)
      .some((field) => String(field).toLowerCase().includes(q))
  })
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
  border: 1px solid rgba(215, 180, 106, 0.2);
  background: rgba(14, 14, 14, 0.88);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 220px);
  overflow: auto;
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
}

.tieu-de-panel span {
  color: #d7b46a;
  font-size: 0.82rem;
}

.o-tim-kiem {
  width: 100%;
  margin-bottom: 14px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.05);
  color: #f7f2e9;
}

.o-tim-kiem::placeholder {
  color: rgba(247, 242, 233, 0.6);
}

.hang-hoa-don {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  width: 100%;
  min-height: 76px;
  border-radius: 6px;
  margin-bottom: 10px;
  padding: 14px;
  text-align: left;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(215, 180, 106, 0.45);
  color: #f7f2e9;
  cursor: pointer;
}

.hang-hoa-don.active {
  border-color: #d7b46a;
  background: rgba(215, 180, 106, 0.14);
}

.hang-hoa-don strong,
.hang-hoa-don b,
.hang-hoa-don small {
  display: block;
}

.hang-hoa-don small {
  margin-top: 6px;
  color: #b7ad9c;
}

.hang-phai {
  text-align: right;
}

.trang-trong {
  color: #cfc3ae;
  margin: 14px 0;
}

@media (max-width: 900px) {
  .danh-sach-hoa-don {
    max-height: none;
  }
}
</style>
