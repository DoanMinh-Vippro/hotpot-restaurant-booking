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
  'go-to-category',
])

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

      <div v-else class="bang-bao-boc">
        <table>
          <thead>
            <tr>
              <th>Hình ảnh</th>
              <th>Tên món</th>
              <th>Giá</th>
              <th>Khuyến mãi</th>
              <th>Danh mục</th>
              <th>Trạng thái kinh doanh</th>
              <th>Trạng thái kho</th>
              <th>Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="mon in danhSachMon"
              :key="mon.idMon"
            >
              <td class="o-anh">
                <img
                  v-if="mon.hinhAnh"
                  :src="`http://localhost:8080/uploads/${mon.hinhAnh}`"
                  class="img-mon"
                />
                <span v-else class="chua-co-anh">Không có ảnh</span>
              </td>

              <td class="o-chu-thuong text-dam">{{ mon.tenMon }}</td>
              
              <td class="o-chu-thuong">
                <template v-if="mon.soTienDuocGiam > 0">
                  <div class="gia-goc">{{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }} đ</div>
                  <div class="gia-giam">{{ Number(mon.giaSauGiam).toLocaleString('vi-VN') }} đ</div>
                </template>
                <template v-else>
                  {{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }} đ
                </template>
              </td>
              
              <td class="o-chu-thuong">{{ mon.tenChuongTrinhGiamGia || '---' }}</td>
              <td class="o-chu-thuong">{{ mon.loaiDanhMuc }}</td>

              <td class="o-chu-thuong">
                <span :class="mon.trangThai === 0 ? 'trang-thai-con' : 'trang-thai-ngung'">
                  {{ mon.trangThai === 0 ? 'Còn bán' : 'Ngưng bán' }}
                </span>
              </td>

              <td class="o-chu-thuong">
                <span :class="(mon.trangThaiBan === 0 || mon.trangThai === 1) ? 'trang-thai-het' : 'trang-thai-con'">
                  {{ mon.trangThai === 1 ? 'Hết hàng ' : (mon.trangThaiBan === 1 ? 'Còn hàng' : 'Hết hàng') }}
                </span>
              </td>

              <td>
                <div class="hanh-dong-o">
                  <button class="nut-sua" @click="$emit('edit', mon)">Sửa</button>
                  <button class="nut-xoa" @click="xoa(mon.idMon)">Ngưng bán</button>
                </div>
              </td>
            </tr>
            
            <tr v-if="danhSachMon.length === 0">
              <td colspan="8" style="text-align: center; color: #a0a0a0; padding: 20px">
                Không tìm thấy món ăn nào phù hợp.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
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

.danh-sach-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
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
  margin: 0;
}

.tieu-de-panel p {
  color: #c7c7c7;
  margin: 4px 0 0;
}

.bang-bao-boc {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 14px;
  color: #f8d46a;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  font-weight: 600;
}

td {
  padding: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  text-align: left;
  vertical-align: middle;
}

tr.active {
  background: rgba(248, 212, 106, 0.06);
}

/* ĐỒNG BỘ CSS HIỂN THỊ ẢNH THEO ĐÚNG COMBO */
.o-anh {
  height: 60px;
  width: 60px;
  padding: 14px;
}

img.img-mon {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}

.chua-co-anh {
  font-size: 13px;
  color: #a0a0a0;
  display: block;
  line-height: 60px;
}

.o-chu-thuong {
  line-height: 60px;
  white-space: nowrap;
}

.text-dam {
  font-weight: 600;
}

.hanh-dong-o {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  height: 60px;
}

.hanh-dong-o button {
  white-space: nowrap;
}

button {
  padding: 6px 12px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: opacity 0.2s;
}

button:hover {
  opacity: 0.85;
}

.nut-phu {
  background: #f8d46a;
  color: #1a1410;
  padding: 10px 16px;
  font-weight: 600;
  border-radius: 16px;
}

.nut-sua {
  background: rgba(248, 212, 106, 0.15);
  color: #f8d46a;
}

.nut-xoa {
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
}

.trang-thai-con {
  color: #52c41a;
}

.trang-thai-ngung {
  color: #ff4d4f;
}

.trang-thai-het {
  color: #fa8c16;
}

.gia-goc {
  text-decoration: line-through;
  color: #888;
  font-size: 13px;
  line-height: 20px;
}

.gia-giam {
  color: #ff4d4f;
  font-weight: 700;
  line-height: 20px;
}

.trang-thai-tai {
  text-align: center;
  padding: 30px;
  color: #f8d46a;
}
</style>