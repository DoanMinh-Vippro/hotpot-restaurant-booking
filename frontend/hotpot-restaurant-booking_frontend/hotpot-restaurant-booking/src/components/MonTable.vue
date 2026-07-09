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
  'go-to-category', // Khai báo sự kiện chuyển màn danh mục
])

// Quản lý trạng thái bộ lọc nội bộ
const searchTenMon = ref('')
const searchLoaiDanhMuc = ref('')

const kichHoatTimKiem = () => {
  emit('search', {
    tenMon: searchTenMon.value,
    loaiDanhMuc: searchLoaiDanhMuc.value,
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
        <option v-for="dm in danhSachDanhMuc" :key="dm.idDanhMuc" :value="dm.loaiDanhMuc">
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

        <button class="nut-phu" type="button" @click="$emit('add')">Thêm món</button>
      </div>

      <div v-if="loading" class="trang-thai-tai">Đang tải dữ liệu thực đơn...</div>

      <table v-else>
        <thead>
          <tr>
            <th>Tên món</th>
            <th>Giá</th>
            <th>Khuyến mãi</th>
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
            <td>
              <template v-if="mon.soTienDuocGiam > 0">
                <div class="gia-goc">{{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }} đ</div>

                <div class="gia-giam">{{ Number(mon.giaSauGiam).toLocaleString('vi-VN') }} đ</div>
              </template>

              <template v-else>
                {{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }} đ
              </template>
            </td>
            <td>
              {{ mon.tenChuongTrinhGiamGia }}
            </td>
            <td>{{ mon.loaiDanhMuc }}</td>

            <td>
              <span
                :class="{
                  'trang-thai-con': mon.trangThai === 0,
                  'trang-thai-ngung': mon.trangThai === 1,
                  'trang-thai-het': mon.trangThai === 2,
                }"
              >
                {{
                  mon.trangThai === 0 ? 'Còn bán' : mon.trangThai === 1 ? 'Ngưng bán' : 'Tạm hết món'
                }}
              </span>
            </td>

            <td class="hanh-dong">
              <button class="nut-sua" @click="$emit('edit', mon)">Sửa</button>
              <button class="nut-xoa" @click="xoa(mon.idMon)">Ngưng bán</button>
            </td>
          </tr>
          <tr v-if="danhSachMon.length === 0">
            <td colspan="5" style="text-align: center; color: #a0a0a0; padding: 20px">
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
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.bo-loc-panel input,
.bo-loc-panel select {
  background: #fffdf8;
  border: 1px solid #e6d2aa;
  color: #5f3d22;
  padding: 10px 14px;
  border-radius: 12px;
  outline: none;
  flex: 1;
}

.bo-loc-panel input:focus,
.bo-loc-panel select:focus {
  border-color: #d8a85c;
}

.bo-loc-panel select option {
  background: #fffdf8;
  color: #5f3d22;
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
  background: #d8a85c;
  color: #3d2814;
}

.nut-lam-moi {
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
}

.nut-danh-muc {
  background: #eef4ff;
  color: #345fb0;
  border: 1px solid #cddaf7;
}

.nut-danh-muc:hover {
  background: #e1ebff;
}

.danh-sach-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.tieu-de-panel h2 {
  color: #8b5e34;
  margin: 0;
}

.tieu-de-panel p {
  color: #8f6b46;
  margin: 4px 0 0;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #5f3d22;
}

th {
  text-align: left;
  padding: 12px;
  color: #8b5e34;
}

td {
  padding: 14px;
  border-bottom: 1px solid #efe0c1;
}

tr.active {
  background: rgba(216, 168, 92, 0.14);
}

.trang-thai-tai {
  text-align: center;
  padding: 30px;
  color: #8b5e34;
}

.hanh-dong {
  display: flex;
  gap: 8px;
}

.nut-sua {
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.nut-xoa {
  background: #fff0eb;
  color: #b84f3f;
  border: 1px solid #f2b4a3;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.nut-phu {
  border: none;
  padding: 10px 16px;
  border-radius: 12px;
  background: #d8a85c;
  color: #3d2814;
  font-weight: 600;
  cursor: pointer;
}

.trang-thai-con {
  color: #2e7d32;
}

.trang-thai-ngung {
  color: #c94f3a;
}
.trang-thai-het {
  color: #c77b1a;
}
.gia-goc {
  text-decoration: line-through;
  color: #8f6b46;
  font-size: 13px;
}

.gia-giam {
  color: #c94f3a;
  font-weight: 700;
}

@media (max-width: 1200px) {
  .bo-loc-panel {
    flex-wrap: wrap;
  }
}
</style>
