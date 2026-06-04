<script setup lang="ts">
import { ref } from 'vue'
import type { DotGiamGia } from '../api/DotGiamGiaApi'

defineProps<{
  danhSach: DotGiamGia[]
  selectedId: number | null
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset', 'view-detail'])

// Quản lý trạng thái bộ lọc khoảng thời gian nội bộ của Table
const searchTenChuongTrinh = ref('')
const searchTuNgay = ref('')
const searchDenNgay = ref('')

const kichHoatTimKiem = () => {
  emit('search', {
    tenChuongTrinh: searchTenChuongTrinh.value,
    tuNgay: searchTuNgay.value,
    denNgay: searchDenNgay.value
  })
}

const kichHoatLamMoi = () => {
  searchTenChuongTrinh.value = ''
  searchTuNgay.value = ''
  searchDenNgay.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  if (confirm('Ngưng đợt giảm giá này?')) {
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
      
      <div class="khoang-ngay-wrapper">
        <input 
          v-model="searchTuNgay" 
          type="date" 
          title="Từ ngày"
          @change="kichHoatTimKiem"
        />
        <span class="ky-tu-noi">đến</span>
        <input 
          v-model="searchDenNgay" 
          type="date" 
          title="Đến ngày"
          @change="kichHoatTimKiem"
        />
      </div>

      <button class="nut-tim" @click="kichHoatTimKiem">Tìm kiếm</button>
      <button class="nut-lam-moi" @click="kichHoatLamMoi">Làm mới</button>
    </div>

    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <div>
          <h2>Danh sách đợt giảm giá</h2>
          <p>Quản lý chương trình khuyến mãi</p>
        </div>

        <button class="nut-phu" @click="$emit('add')">
          Thêm đợt giảm giá
        </button>
      </div>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên chương trình</th>
            <th>Bắt đầu</th>
            <th>Kết thúc</th>
            <th>Hành động</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="item in danhSach"
            :key="item.idDotGiamGia"
            :class="{ active: item.idDotGiamGia === selectedId }"
          >
            <td>{{ item.idDotGiamGia }}</td>
            <td>{{ item.tenChuongTrinh }}</td>
            <td>{{ item.ngayBatDau }}</td>
            <td>{{ item.ngayKetThuc }}</td>

            <td class="hanh-dong">
              <button class="nut-xem-ct" @click="$emit('view-detail', item)">
                Xem chi tiết
              </button>
              <button class="nut-sua" @click="$emit('edit', item)">
                Sửa
              </button>
              <button class="nut-xoa" @click="xoa(item.idDotGiamGia)">
                Xoá
              </button>
            </td>
          </tr>
          <tr v-if="danhSach.length === 0">
            <td colspan="5" style="text-align: center; color: #a0a0a0; padding: 20px;">
              Không tìm thấy chương trình giảm giá nào phù hợp.
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

.bo-loc-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.bo-loc-panel > input {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #f5f5f5;
  padding: 10px 14px;
  border-radius: 12px;
  outline: none;
  flex: 1.2;
  transition: border-color 0.2s;
}

.bo-loc-panel input:focus {
  border-color: #f8d46a;
}

.khoang-ngay-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 4px 10px;
  border-radius: 14px;
}

.khoang-ngay-wrapper input[type="date"] {
  background: transparent;
  border: none;
  color: #f5f5f5;
  outline: none;
  padding: 6px;
  cursor: pointer;
}

.ky-tu-noi {
  color: #a0a0a0;
  font-size: 13px;
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
  background: rgba(15,15,15,.94);
  border: 1px solid rgba(255,255,255,.06);
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
  border-bottom: 1px solid rgba(255,255,255,.06);
}

tr.active {
  background: rgba(248, 212, 106, 0.06);
}

.hanh-dong {
  display: flex;
  gap: 8px;
}

.nut-xem-ct {
  background: rgba(100, 149, 237, 0.15);
  color: #6495ed;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.nut-sua {
  background: rgba(248,212,106,.15);
  color: #f8d46a;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.nut-xoa {
  background: rgba(255,107,107,.15);
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
  .khoang-ngay-wrapper {
    width: 100%;
    justify-content: space-between;
  }
}
</style>