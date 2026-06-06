<script setup lang="ts">
import { ref } from 'vue'
import type { Mon } from '../api/MonApi'
import type { DanhMuc } from '../api/DanhMucApi'

defineProps<{
  danhSachMon: Mon[]
  loading: boolean
  selectedId: number | null
  danhSachDanhMuc: DanhMuc[]
}>()

const emit = defineEmits([
  'edit',
  'delete',
  'add',
  'search',
  'reset',
  'go-to-category' // Khai báo sự kiện chuyển màn danh mục
])

// Quản lý trạng thái bộ lọc nội bộ
const searchTenMon = ref('')
const searchLoaiDanhMuc = ref('')

const kichHoatTimKiem = () => {
  emit('search', {
    tenMon: searchTenMon.value,
    loaiDanhMuc: searchLoaiDanhMuc.value
  })
}

const kichHoatLamMoi = () => {
  searchTenMon.value = ''
  searchLoaiDanhMuc.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  emit('delete', id)
}
</script>

<template>
  <div class="khu-vuc-danh-sach">
    
    <div class="bo-loc-panel">
      <input 
        v-model="searchTenMon" 
        type="text" 
        placeholder="🔍 Tìm theo tên món..." 
        @keyup.enter="kichHoatTimKiem"
      />
      
      <select v-model="searchLoaiDanhMuc" @change="kichHoatTimKiem">
        <option value="">Tất cả danh mục</option>
        <option 
          v-for="dm in danhSachDanhMuc" 
          :key="dm.idDanhMuc" 
          :value="dm.loaiDanhMuc"
        >
          {{ dm.loaiDanhMuc }}
        </option>
      </select>
      
      <button class="nut-tim" @click="kichHoatTimKiem">Tìm kiếm</button>
      <button class="nut-lam-moi" @click="kichHoatLamMoi">Làm mới</button>
      <button class="nut-danh-muc" @click="$emit('go-to-category')">📂 Quản lý danh mục</button>
    </div>

    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <div>
          <h2>Danh sách món</h2>
          <p>Quản lý các món ăn hiện có.</p>
        </div>

        <button
          class="nut-phu"
          type="button"
          @click="$emit('add')"
        >
          Thêm món
        </button>
      </div>

      <div v-if="loading" class="trang-thai-tai">Đang tải dữ liệu thực đơn...</div>

      <table v-else>
        <thead>
          <tr>
            <th>Tên món</th>
            <th>Giá</th>
            <th>Danh mục</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="mon in danhSachMon"
            :key="mon.idMon"
            :class="{ active: mon.idMon === selectedId }"
          >
            <td>{{ mon.tenMon }}</td>
            <td>{{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }} đ</td>
            <td>{{ mon.loaiDanhMuc }}</td>
            
            <td>
              <span :class="mon.trangThai === 0 ? 'trang-thai-con' : 'trang-thai-ngung'">
                {{ mon.trangThai === 0 ? 'Còn bán' : 'Ngưng bán' }}
              </span>
            </td>

            <td class="hanh-dong">
              <button class="nut-sua" @click="$emit('edit', mon)">Sửa</button>
              <button class="nut-xoa" @click="xoa(mon.idMon)">Ngưng bán</button>
            </td>
          </tr>
          <tr v-if="danhSachMon.length === 0">
            <td colspan="5" style="text-align: center; color: #a0a0a0; padding: 20px;">
              Không tìm thấy món ăn nào phù hợp.
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

.bo-loc-panel input,
.bo-loc-panel select {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #f5f5f5;
  padding: 10px 14px;
  border-radius: 12px;
  outline: none;
  flex: 1;
}

.bo-loc-panel input:focus,
.bo-loc-panel select:focus {
  border-color: #f8d46a;
}

.bo-loc-panel select option {
  background: #151515;
  color: #fff;
}

.nut-tim,
.nut-lam-moi,
.nut-danh-muc {
  padding: 10px 18px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: 0.3s;
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

.nut-danh-muc {
  background: rgba(100, 149, 237, 0.15);
  color: #6495ed;
  border: 1px solid rgba(100, 149, 237, 0.2);
}

.nut-danh-muc:hover {
  background: rgba(100, 149, 237, 0.25);
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

.trang-thai-tai {
  text-align: center;
  padding: 30px;
  color: #f8d46a;
}

.hanh-dong {
  display: flex;
  gap: 8px;
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

/* CẬP NHẬT: Thêm CSS định dạng màu sắc trạng thái đồng bộ với Combo */
.trang-thai-con {
  color: #52c41a; /* Màu xanh lá cây */
}

.trang-thai-ngung {
  color: #ff4d4f; /* Màu đỏ */
}

@media (max-width: 1200px) {
  .bo-loc-panel {
    flex-wrap: wrap;
  }
}
</style>