<script setup lang="ts">
import { ref } from 'vue'
import type { Combo } from '../api/ComBoApi'

defineProps<{
  danhSachCombo: Combo[]
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset', 'view-detail'])

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

      <div class="bang-bao-boc">
        <table>
          <thead>
            <tr>
              <th>Hình ảnh</th>
              <th>Tên combo</th>
              <th>Giá tiền</th>
              <th>Trạng thái kinh doanh</th>
              <th>Trạng thái kho</th>
              <th>Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="cb in danhSachCombo" :key="cb.idCombo">
              <td class="o-anh">
                <img v-if="cb.hinhAnh" :src="cb.hinhAnh" class="img-combo" />
                <span v-else class="chua-co-anh">Không có ảnh</span>
              </td>

              <td class="o-chu-thuong text-dam">{{ cb.tenCombo }}</td>
              <td class="o-chu-thuong">{{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ</td>

              <td class="o-chu-thuong">
                <span
                  :class="{
                    'trang-thai-con': cb.trangThai === 1,
                    'trang-thai-ngung': cb.trangThai === 0,
                  }"
                >
                  {{ cb.trangThai === 1 ? 'Còn bán' : 'Ngưng bán' }}
                </span>
              </td>

              <td class="o-chu-thuong">
                <span
                  :class="{
                    'trang-thai-con': cb.trangThaiBan === 1 && cb.trangThai === 1,
                    'trang-thai-het': cb.trangThaiBan === 0 || cb.trangThai === 0,
                  }"
                >
                  {{
                    cb.trangThai === 0
                      ? 'Hết hàng '
                      : cb.trangThaiBan === 1
                        ? 'Còn hàng'
                        : 'Hết hàng'
                  }}
                </span>
              </td>

              <td>
                <div class="hanh-dong-o">
                  <button class="nut-xem-ct" @click="$emit('view-detail', cb)">Xem chi tiết</button>
                  <button class="nut-sua" @click="$emit('edit', cb)">Sửa</button>
                  <button class="nut-xoa" @click="xoa(cb.idCombo!)">Xóa</button>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachCombo.length === 0">
              <td colspan="6" style="text-align: center; color: #a0a0a0; padding: 30px">
                Không tìm thấy combo phù hợp.
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
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  align-items: center;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.bo-loc-panel input {
  background: #fffdf8;
  border: 1px solid #e6d2aa;
  color: #5f3d22;
  padding: 10px 14px;
  border-radius: 12px;
  outline: none;
  flex: 1;
  transition: border-color 0.2s;
}

.bo-loc-panel input:focus {
  border-color: #d8a85c;
}

.nut-tim,
.nut-lam-moi {
  padding: 10px 18px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  cursor: pointer;
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

.danh-sach-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 24px;
  padding: 26px;
  color: #5f3d22;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tieu-de-panel h2 {
  color: #8b5e34;
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
  color: #8b5e34;
  padding: 14px;
  border-bottom: 1px solid #efe0c1;
  text-align: left;
  font-weight: 600;
}

td {
  padding: 14px;
  border-bottom: 1px solid #efe0c1;
  text-align: left;
  vertical-align: middle;
}

.o-chu-thuong {
  line-height: 60px;
  white-space: nowrap;
}

.text-dam {
  font-weight: 600;
}

.o-anh {
  height: 60px;
  width: 60px;
  padding: 14px;
}

img.img-combo {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}

.chua-co-anh {
  font-size: 13px;
  color: #8f6b46;
  display: block;
  line-height: 60px;
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

.tieu-de-panel button {
  background: #d8a85c;
  color: #3d2814;
  padding: 10px 16px;
  font-weight: 600;
  border-radius: 16px;
}

.nut-xem-ct {
  background: #eef4ff;
  color: #345fb0;
}

.nut-sua {
  background: #fff3d3;
  color: #8b5e34;
}

.nut-xoa {
  background: #fff0eb;
  color: #b84f3f;
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
</style>
