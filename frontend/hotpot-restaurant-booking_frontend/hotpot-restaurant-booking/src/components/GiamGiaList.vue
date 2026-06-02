<script setup lang="ts">
import { computed } from 'vue'
import type { GiamGia } from '../api/GiamGiaApi'

const props = defineProps<{
  danh_sach_giam_gia: GiamGia[]
  loading: boolean
  selectedId: number | null
  tim_kiem_query: string
}>()

const emit = defineEmits<{
  'select': [id: number]
  'edit': [discount: GiamGia]
  'delete': [id: number]
  'add': []
  'update:timKiemQuery': [value: string]
}>()

const formatCurrency = (value: number | string | null) => {
  const numeric = Number(value ?? 0)
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(numeric)
}

const handleSelect = (discount: GiamGia) => {
  emit('select', discount.idGiamGia)
  emit('edit', discount)
}

const handleDelete = (id: number) => {
  const confirmed = window.confirm('Bạn có chắc muốn xóa mã giảm giá này?')
  if (confirmed) {
    emit('delete', id)
  }
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
            <td>{{ discount.loaiGiam }}</td>
            <td>{{ formatCurrency(discount.giaTriGiam) }}</td>
            <td>{{ discount.trangThai === 1 ? 'Hoạt động' : 'Ngưng' }}</td>
            <td class="hanh-dong">
              <button type="button" class="nut-van-ban" @click="handleSelect(discount)">Sửa</button>
              <button type="button" class="nut-xoa" @click="handleDelete(discount.idGiamGia)">Xóa</button>
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
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  border-radius: 28px;
  padding: 26px;
  grid-column: 1;
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.tieu-de-panel h2 {
  margin: 0 0 10px;
  font-size: 1.15rem;
}

.tieu-de-panel p {
  margin: 0;
  color: #c7c7c7;
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
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #f5f5f5;
  border-radius: 16px;
  padding: 14px 16px;
}

.o-tim-kiem::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.chip-trang-thai {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 170px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  color: #d8d8d8;
  font-size: 0.9rem;
}

.bao-bang {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #f1f1f1;
}

th {
  text-align: left;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #f8d46a;
  font-size: 0.85rem;
  font-weight: 600;
}

td {
  padding: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

tr:hover {
  background: rgba(255, 255, 255, 0.03);
}

tr.active {
  background: rgba(248, 212, 106, 0.08);
}

.hanh-dong {
  display: flex;
  gap: 8px;
}

.nut-van-ban,
.nut-xoa,
.nut-phu {
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 0.85rem;
  cursor: pointer;
  font-weight: 500;
}

.nut-van-ban {
  background: rgba(248, 212, 106, 0.15);
  color: #f8d46a;
}

.nut-van-ban:hover {
  background: rgba(248, 212, 106, 0.25);
}

.nut-xoa {
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
}

.nut-xoa:hover {
  background: rgba(255, 107, 107, 0.25);
}

.nut-phu {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.nut-phu:hover {
  background: rgba(255, 255, 255, 0.15);
}

.trang-trong {
  text-align: center;
  padding: 40px 20px;
  color: #888;
}
</style>
