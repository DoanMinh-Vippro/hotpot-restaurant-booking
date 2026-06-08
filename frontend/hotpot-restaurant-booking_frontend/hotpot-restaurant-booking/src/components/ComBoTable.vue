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
              <th>Tên</th>
              <th>Giá</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="cb in danhSachCombo" :key="cb.idCombo">
              <td class="o-anh">
                <img
                  v-if="cb.hinhAnh"
                  :src="`http://localhost:8080/uploads/${cb.hinhAnh}`"
                  class="img-combo"
                />
                <span v-else class="chua-co-anh">Không có ảnh</span>
              </td>
              <td class="o-chu-thuong text-dam">{{ cb.tenCombo }}</td>
              <td class="o-chu-thuong">{{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ</td>
              <td class="o-chu-thuong">
                <span :class="cb.trangThai ? 'trang-thai-con' : 'trang-thai-ngung'">
                  {{ cb.trangThai ? 'Còn bán' : 'Ngưng bán' }}
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
              <td colspan="5" style="text-align: center; color: #a0a0a0; padding: 30px;">
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

/* Hỗ trợ cuộn mượt mà nếu danh sách bị quá dài tràn màn hình */
.bang-bao-boc {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  color: #f8d46a;
  padding: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, .06);
  text-align: left;
  font-weight: 600;
}

td {
  padding: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, .06);
  text-align: left;
  vertical-align: middle; /* Căn giữa nội dung chữ theo chiều dọc dòng */
}

/* Định hình dòng chữ thông thường để cân đối với dòng chứa nút flex */
.o-chu-thuong {
  line-height: 60px; /* Bằng đúng chiều cao ảnh combo để gióng hàng cực chuẩn */
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
  color: #a0a0a0;
  display: block;
  line-height: 60px;
}

/* Cố định khối hành động để các nút không bao giờ bị nhảy lệch hay rớt dòng */
.hanh-dong-o {
  display: flex;
  align-items: center;      /* Căn giữa nút theo chiều dọc */
  justify-content: flex-start; /* Đẩy sát nút về phía tiêu đề */
  gap: 8px;                 /* Khoảng cách chuẩn giữa các nút */
  height: 60px;             /* Khớp hoàn hảo với chiều cao tổng thể của dòng */
}

.hanh-dong-o button {
  white-space: nowrap;      /* Không cho phép chữ trong nút bị bẻ xuống dòng */
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
  background: #f8d46a;
  color: #1a1410;
  padding: 10px 16px;
  font-weight: 600;
  border-radius: 16px;
}

.nut-xem-ct {
  background: rgba(100, 149, 237, 0.15);
  color: #6495ed;
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
</style>