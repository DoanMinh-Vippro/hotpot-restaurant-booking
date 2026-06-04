<script setup lang="ts">
import { ref } from 'vue'
import type { ChiTietGiamGiaMon } from '../api/ChiTietGiamGiaMonApi'

defineProps<{
  danhSach: ChiTietGiamGiaMon[]
  loading: boolean
  selectedId: number | null
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset'])

// Quản lý trạng thái bộ lọc tìm kiếm nội bộ của Table
const searchTenChuongTrinh = ref('')
const searchTenMon = ref('')

const kichHoatTimKiem = () => {
  emit('search', {
    tenChuongTrinh: searchTenChuongTrinh.value,
    tenMon: searchTenMon.value
  })
}

const kichHoatLamMoi = () => {
  searchTenChuongTrinh.value = ''
  searchTenMon.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  if (confirm('Xoá mục giảm giá này?')) {
    emit('delete', id)
  }
}
</script>

<template>
  <div class="khu-vuc-danh-sach">
    
    <div class="bo-loc-panel">
      <input 
        v-model="searchTenChuongTrinh" 
        type="text" 
        placeholder="🔍 Tìm tên chương trình..." 
        @keyup.enter="kichHoatTimKiem"
      />
      <input 
        v-model="searchTenMon" 
        type="text" 
        placeholder="🔍 Tìm tên món ăn..." 
        @keyup.enter="kichHoatTimKiem"
      />
      <button class="nut-tim" @click="kichHoatTimKiem">Tìm kiếm</button>
      <button class="nut-lam-moi" @click="kichHoatLamMoi">Làm mới</button>
    </div>

    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <div>
          <h2>Chi tiết giảm giá món</h2>
          <p>Quản lý chương trình giảm giá áp dụng lên từng món.</p>
        </div>

        <button class="nut-phu" @click="$emit('add')">
          Thêm mới
        </button>
      </div>

      <table>
        <thead>
          <tr>
            <th>Món</th>
            <th>Chương trình</th>
            <th>Mức giảm</th>
            <th>Hành động</th>
          </tr>
        </thead>

        <tbody>
          <tr 
            v-for="item in danhSach" 
            :key="item.idChiTietGiamGiaMon"
            :class="{ active: item.idChiTietGiamGiaMon === selectedId }"
          >
            <td>{{ item.tenMon }}</td>
            <td>{{ item.tenChuongTrinh }}</td>
            <td>{{ item.mucGiam }}%</td>

            <td class="hanh-dong">
              <button class="nut-sua" @click="$emit('edit', item)">
                Sửa
              </button>
              <button class="nut-xoa" @click="xoa(item.idChiTietGiamGiaMon)">
                Xoá
              </button>
            </td>
          </tr>
          <tr v-if="danhSach.length === 0">
            <td colspan="4" style="text-align: center; color: #a0a0a0; padding: 20px;">
              Không tìm thấy chi tiết giảm giá món nào phù hợp.
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

/* CSS Giao diện bộ lọc đồng bộ Dark Mode */
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

.danh-sach-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 28px;
  padding: 26px;
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.tieu-de-panel h2 {
  color: #f8d46a;
  margin: 0;
}

.tieu-de-panel p {
  color: #c7c7c7;
  margin: 4px 0 0;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: white;
}

th {
  text-align: left;
  padding: 12px;
  color: #f8d46a;
}

td {
  padding: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

tr.active {
  background: rgba(248, 212, 106, 0.06);
}

.hanh-dong {
  display: flex;
  gap: 8px;
}

.nut-sua {
  background: rgba(248, 212, 106, 0.15);
  color: #f8d46a;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.nut-xoa {
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.nut-phu {
  border: none;
  padding: 10px 16px;
  border-radius: 12px;
  background: #f8d46a;
  color: #1a1410;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 1200px) {
  .bo-loc-panel {
    flex-wrap: wrap;
  }
}
</style>