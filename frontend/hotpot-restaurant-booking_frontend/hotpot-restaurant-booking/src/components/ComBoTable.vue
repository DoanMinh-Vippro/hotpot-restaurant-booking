<script setup lang="ts">
import { ref } from 'vue'
import type { Combo } from '../api/ComBoApi'

defineProps<{
  danhSachCombo: Combo[]
  loading?: boolean
  selectedId?: number | null
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
        <div>
          <h2>Danh sách combo</h2>
          <p>Quản lý các combo món ăn hiện có.</p>
        </div>
        <button class="nut-phu" type="button" @click="$emit('add')">Thêm combo</button>
      </div>

      <div v-if="loading" class="trang-thai-tai">Đang tải dữ liệu combo...</div>

      <div v-else class="bang-bao-boc">
        <table>
          <thead>
            <tr>
              <th>Hình ảnh</th>
              <th>Tên combo</th>
              <th>Giá</th>
              <th>Khuyến mãi</th>
              <th>Trạng thái kinh doanh</th>
              <th>Trạng thái kho</th>
              <th>Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr 
              v-for="cb in danhSachCombo" 
              :key="cb.idCombo"
              :class="{ active: selectedId === cb.idCombo }"
            >
              <td class="o-anh">
                <img v-if="cb.hinhAnh" :src="cb.hinhAnh" class="img-combo" />
                <span v-else class="chua-co-anh">Không có ảnh</span>
              </td>

              <td class="o-chu-thuong text-dam">{{ cb.tenCombo }}</td>

              <!-- HIỂN THỊ GIÁ TIỀN GIỐNG FILE MÓN -->
              <td class="o-chu-thuong">
                <template v-if="cb.soTienDuocGiam && cb.soTienDuocGiam > 0">
                  <div class="gia-goc">
                    {{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ
                  </div>
                  <div class="gia-giam">
                    {{ Number(cb.giaSauGiam).toLocaleString('vi-VN') }} đ
                  </div>
                </template>
                <template v-else>
                  {{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ
                </template>
              </td>

              <!-- CỘT KHUYẾN MÃI -->
              <td class="o-chu-thuong">{{ cb.tenChuongTrinhGiamGia || '---' }}</td>

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
                  :class="
                    cb.trangThaiBan === 0 || cb.trangThai === 0
                      ? 'trang-thai-het'
                      : 'trang-thai-con'
                  "
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
                  <button class="nut-xoa" @click="xoa(cb.idCombo!)">Ngưng bán</button>
                </div>
              </td>
            </tr>

            <tr v-if="danhSachCombo.length === 0">
              <td colspan="7" style="text-align: center; color: #a0a0a0; padding: 20px">
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
  padding: 24px;
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
  margin: 0;
}

.tieu-de-panel p {
  color: #8f6b46;
  margin: 4px 0 0;
}

.bang-bao-boc {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #5f3d22;
}

th {
  text-align: left;
  border-bottom: 1px solid #efe0c1;
  font-weight: 600;
  padding: 12px;
  color: #8b5e34;
}

td {
  padding: 14px;
  text-align: left;
  vertical-align: middle;
  border-bottom: 1px solid #efe0c1;
}

tr.active {
  background: rgba(216, 168, 92, 0.14);
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

.nut-xem-ct {
  background: #eef4ff;
  color: #345fb0;
}

.nut-sua {
  background: rgba(248, 212, 106, 0.15);
  color: #8b5e34;
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

/* STYLE HIỂN THỊ GIÁ GỐC VÀ GIÁ GIẢM ĐỒNG BỘ MÓN ÁN */
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
  color: #8b5e34;
}
</style>