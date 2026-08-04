<script setup lang="ts">
import type { GiamGia } from '../api/GiamGiaApi'

const props = defineProps<{
  danh_sach_giam_gia: GiamGia[]
  loading: boolean
  selectedId: number | null
  tim_kiem_query: string
}>()

const emit = defineEmits<{
  'select': [id: number | string]
  'edit': [discount: GiamGia]
  'delete': [id: number | string]
  'add': []
  'update:timKiemQuery': [value: string]
}>()

const normalizeLoaiGiam = (value: string | null | undefined) => {
  const normalized = `${value ?? ''}`
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toUpperCase()

  if (['PHANTRAM', 'PERCENT', 'PHNTRAM', 'PHTNTRAM', 'PHTRAM'].includes(normalized)) return 'percent'
  if (normalized.includes('PH') && normalized.includes('TRAM')) return 'percent'
  if (['TIENMAT', 'TIEN', 'VND', 'FIXED', 'MONEY'].includes(normalized)) return 'fixed'
  if (normalized.includes('TIEN') || normalized.includes('MAT') || normalized.includes('GIATRI') || normalized.includes('VALUE')) return 'fixed'

  return 'unknown'
}

const formatLoaiGiam = (value: string | null | undefined) => {
  switch (normalizeLoaiGiam(value)) {
    case 'percent':
      return 'Phần trăm'
    case 'fixed':
      return 'Tiền mặt'
    default:
      return value?.trim() || 'Không xác định'
  }
}

const formatGiaTriGiam = (discount: GiamGia) => {
  const numeric = Number(discount.giaTriGiam ?? 0)

  switch (normalizeLoaiGiam(discount.loaiGiam)) {
    case 'percent':
      return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 2 })}%`
    case 'fixed':
      return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 0 })} đ`
    default:
      return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 0 })}`
  }
}

const getDiscountId = (discount: GiamGia) => {
  const candidates = [
    (discount as GiamGia & Record<string, unknown>).idGiamGia,
    (discount as GiamGia & Record<string, unknown>).id,
    (discount as GiamGia & Record<string, unknown>).code,
  ]

  const numericId = candidates.find((value) => typeof value === 'number' && Number.isFinite(value))
  if (typeof numericId === 'number') return numericId

  const stringId = candidates.find((value) => typeof value === 'string' && value.trim())
  return typeof stringId === 'string' ? stringId : null
}

const handleSelect = (discount: GiamGia) => {
  const discountId = getDiscountId(discount)
  if (typeof discountId === 'number' || typeof discountId === 'string') {
    emit('select', discountId)
  }
  emit('edit', discount)
}

const handleDelete = (discount: GiamGia) => {
  const confirmed = window.confirm('Bạn có chắc muốn xóa mã giảm giá này?')
  if (!confirmed) return

  const discountId = getDiscountId(discount)
  if (typeof discountId === 'number' || typeof discountId === 'string') {
    emit('delete', discountId)
    return
  }

  window.alert('Không tìm thấy mã định danh của mã giảm giá để xóa.')
}

const handleAddNew = () => {
  emit('add')
}

const updateSearch = (e: Event) => {
  emit('update:timKiemQuery', (e.target as HTMLInputElement).value)
}
</script>

<template>
  <section class="danh-sach-panel">
    <div class="tieu-de-panel">
      <div>
        <h2>Danh sách mã</h2>
        <p>Nhấp vào mã để sửa hoặc xóa.</p>
      </div>
      <button class="nut-phu" type="button" @click="handleAddNew">Thêm mới</button>
    </div>

    <div class="thanh-cong-cu">
      <input
        class="o-tim-kiem"
        type="search"
        :value="tim_kiem_query"
        @input="updateSearch"
        placeholder="Tìm mã hoặc điều kiện sử dụng..."
      />
      <span class="chip-trang-thai">
        {{ loading ? 'Đang tải dữ liệu...' : danh_sach_giam_gia.length + ' mã giảm giá' }}
      </span>
    </div>

    <div class="bao-bang">
      <table>
        <thead>
          <tr>
            <th>Mã</th>
            <th>Loại giảm</th>
            <th>Giá trị</th>
            <th>Điều kiện sử dụng</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="discount in danh_sach_giam_gia"
            :key="discount.idGiamGia"
            :class="{ active: discount.idGiamGia === selectedId }"
          >
            <td>{{ discount.maGiamGia }}</td>
            <td>{{ formatLoaiGiam(discount.loaiGiam) }}</td>
            <td>{{ formatGiaTriGiam(discount) }}</td>
            <td>{{ discount.dieuKienSuDung || 'Không có' }}</td>
            <td>{{ discount.trangThai === 1 ? 'Hoạt động' : 'Ngưng' }}</td>
            <td class="hanh-dong">
              <button type="button" class="nut-van-ban" @click="handleSelect(discount)">Sửa</button>
              <button type="button" class="nut-xoa" @click="handleDelete(discount)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!loading && danh_sach_giam_gia.length === 0" class="trang-trong">Không có mã giảm giá nào.</p>
    </div>
  </section>
</template>

<style scoped>
.danh-sach-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  box-shadow: 0 16px 40px rgba(103, 72, 32, 0.08);
  border-radius: 24px;
  padding: 24px;
  grid-column: 1;
  color: #5f3d22;
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.tieu-de-panel h2 {
  margin: 0 0 8px;
  font-size: 1.1rem;
  color: #8b5e34;
}

.tieu-de-panel p {
  margin: 0;
  color: #8f6b46;
  max-width: 420px;
}

.thanh-cong-cu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.o-tim-kiem {
  flex: 1;
  border: 1px solid #e6d2aa;
  background: #fffdf8;
  color: #5f3d22;
  border-radius: 14px;
  padding: 12px 14px;
}

.o-tim-kiem::placeholder {
  color: #b18c62;
}

.chip-trang-thai {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 170px;
  padding: 10px 14px;
  border-radius: 999px;
  background: #fff3d3;
  color: #8b5e34;
  font-size: 0.9rem;
  border: 1px solid #e6d2aa;
}

.bao-bang {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #5f3d22;
}

th {
  text-align: left;
  padding: 12px 14px;
  border-bottom: 1px solid #efe0c1;
  color: #8b5e34;
  font-size: 0.85rem;
  font-weight: 700;
}

td {
  padding: 14px;
  border-bottom: 1px solid #f3e4c7;
}

tr:hover {
  background: #fff8ea;
}

tr.active {
  background: rgba(216, 168, 92, 0.16);
}

.hanh-dong {
  display: flex;
  gap: 8px;
}

.nut-van-ban,
.nut-xoa,
.nut-phu {
  border: none;
  border-radius: 10px;
  padding: 7px 12px;
  font-size: 0.85rem;
  cursor: pointer;
  font-weight: 600;
}

.nut-van-ban {
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
}

.nut-van-ban:hover {
  background: #f2dfb0;
}

.nut-xoa {
  background: #fff0eb;
  color: #b84f3f;
  border: 1px solid #f2b4a3;
}

.nut-xoa:hover {
  background: #ffd7cb;
}

.nut-phu {
  background: #d8a85c;
  color: #3d2814;
}

.nut-phu:hover {
  background: #c99646;
}

.trang-trong {
  text-align: center;
  padding: 40px 20px;
  color: #8f6b46;
}
</style>
