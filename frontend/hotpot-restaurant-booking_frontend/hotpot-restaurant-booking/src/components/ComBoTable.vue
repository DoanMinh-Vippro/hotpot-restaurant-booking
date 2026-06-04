<script setup lang="ts">
import { ref } from 'vue'
import type { Combo } from '../api/ComBoApi'

defineProps<{
  danhSachCombo: Combo[]
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset'])

// Quản lý ô nhập tìm kiếm nội bộ của Table
const searchTenCombo = ref('')

const kichHoatTimKiem = () => {
  emit('search', searchTenCombo.value)
}

const kichHoatLamMoi = () => {
  searchTenCombo.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  if (confirm('Bạn có chắc chắn muốn ngưng bán combo này?')) {
    emit('delete', id)
  }
}
</script>

<template>
  <div class="khu-vuc-danh-sach">
    
    <div class="bo-loc-panel">
      <input 
        v-model="searchTenCombo" 
        type="text" 
        placeholder="🔍 Nhập tên combo cần tìm..." 
        @keyup.enter="kichHoatTimKiem"
      />
      <button class="nut-tim" @click="kichHoatTimKiem">Tìm kiếm</button>
      <button class="nut-lam-moi" @click="kichHoatLamMoi">Làm mới</button>
    </div>

    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <h2>Danh sách combo</h2>
        <button @click="$emit('add')">Thêm combo</button>
      </div>

      <table>
        <thead>
          <tr>
            <th>Hình ảnh</th>
            <th>Tên</th>
            <th>Giá</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="cb in danhSachCombo" :key="cb.idCombo">
            <td>
              <img
                v-if="cb.hinhAnh"
                :src="`http://localhost:8080/uploads/${cb.hinhAnh}`"
                class="img-combo"
              />
              <span v-else>Không có ảnh</span>
            </td>
            <td>{{ cb.tenCombo }}</td>
            <td>{{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ</td>
            <td>{{ cb.trangThai ? 'Còn bán' : 'Ngưng bán' }}</td>
            <td>
              <button @click="$emit('edit', cb)">Sửa</button>
              <button @click="xoa(cb.idCombo!)">Xóa</button>
            </td>
          </tr>
          <tr v-if="danhSachCombo.length === 0">
            <td colspan="5" style="text-align: center; color: #a0a0a0; padding: 20px;">
              Không tìm thấy combo phù hợp.
            </td>
          </tr>
        </tbody>
      </table>
    </section>

  </div>
</template>

<style scoped>
.khu-vuc-danh-sach {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Thanh bộ lọc thiết kế mượt mà */
.bo-loc-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.bo-loc-panel input {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #f5f5f5;
  padding: 10px 14px;
  border-radius: 12px;
  outline: none;
  flex: 1;
  transition: border-color 0.2s;
}

.bo-loc-panel input:focus {
  border-color: #f8d46a;
}

.nut-tim, .nut-lam-moi {
  padding: 10px 18px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.nut-tim {
  background: #f8d46a;
  color: #1a1410;
}

.nut-lam-moi {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* Thùng bọc danh sách gốc */
.danh-sach-panel {
  background: rgba(15,15,15,.94);
  border: 1px solid rgba(255,255,255,.06);
  border-radius: 28px;
  padding: 26px;
  color: white;
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tieu-de-panel h2 {
  color: #f8d46a;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 14px;
  border-bottom: 1px solid rgba(255,255,255,.06);
  text-align: left;
}

th {
  color: #f8d46a;
}

img.img-combo {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

button {
  margin-right: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 500;
}

.tieu-de-panel button {
  background: #f8d46a;
  color: #1a1410;
  padding: 10px 16px;
  font-weight: 600;
  border-radius: 16px;
}

td button:first-child {
  background: rgba(248,212,106,.15);
  color: #f8d46a;
}

td button:last-child {
  background: rgba(255,107,107,.15);
  color: #ff6b6b;
}
</style>