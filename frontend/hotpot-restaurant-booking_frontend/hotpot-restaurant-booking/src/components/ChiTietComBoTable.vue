<script setup lang="ts">
import { ref } from 'vue'
import type { ChiTietComBo } from '../api/ChiTietComBoApi'

defineProps<{
  danhSach: ChiTietComBo[]
  loading?: boolean
  selectedId: number | null
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset'])

const searchTenCombo = ref('')
const searchTenMon = ref('')

const kichHoatTimKiem = () => {
  emit('search', {
    tenCombo: searchTenCombo.value,
    tenMon: searchTenMon.value
  })
}

const kichHoatLamMoi = () => {
  searchTenCombo.value = ''
  searchTenMon.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  if (confirm('Ngưng combo này?')) {
    emit('delete', id)
  }
}
</script>

<template>
  <div class="khu-vuc-danh-sach">
    
    <!-- Bộ lọc -->
    <div class="bo-loc-panel">
      <input 
        v-model="searchTenCombo" 
        type="text" 
        placeholder="🔍 Tìm tên combo..." 
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

    <!-- Panel Danh sách -->
    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <div>
          <h2>Danh sách combo</h2>
          <p>Quản lý chi tiết món ăn trong combo.</p>
        </div>

        <button class="nut-phu" @click="$emit('add')">
          + Thêm món vào combo
        </button>
      </div>

      <div class="khung-cuon-bang">
        <table>
          <thead>
            <tr>
              <th>Combo</th>
              <th>Món ăn thành phần</th>
              <th class="canh-giua">Số lượng</th>
              <th class="canh-giua">Giá combo</th>
              <th>Mô tả</th>
              <th class="canh-giua">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr 
              v-for="item in danhSach" 
              :key="item.idChiTietCombo"
              :class="{ active: item.idChiTietCombo === selectedId }"
            >
              <td>
                <span class="ten-combo-highlight">{{ item.tenCombo }}</span>
              </td>
              <td>
                <!-- Trường hợp 1: BE gom nhóm món thành mảng danhSachMon -->
                <div v-if="item.danhSachMon && item.danhSachMon.length > 0" class="danh-sach-badge-mon">
                  <span 
                    v-for="mon in item.danhSachMon" 
                    :key="mon.idMon" 
                    class="badge-ten-mon"
                  >
                    {{ mon.tenMon }} <small v-if="mon.soLuong">(x{{ mon.soLuong }})</small>
                  </span>
                </div>

                <!-- Trường hợp 2: Dữ liệu phẳng đơn lẻ cũ -->
                <span v-else class="badge-ten-mon">
                  {{ item.tenMon }}
                </span>
              </td>
              <td class="canh-giua"><strong>{{ item.soLuong }}</strong></td>
              <td class="canh-giua">{{ Number(item.giaCombo).toLocaleString('vi-VN') }} đ</td>
              <td>{{ item.moTa || '---' }}</td>

              <td class="canh-giua">
                <div class="hanh-dong">
                  <button class="nut-sua" @click="$emit('edit', item)">
                    Sửa
                  </button>
                  <button class="nut-xoa" @click="xoa(item.idChiTietCombo)">
                    Xoá
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="danhSach.length === 0">
              <td colspan="6" class="no-data">
                Không tìm thấy thành phần combo nào phù hợp.
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

.khung-cuon-bang {
  width: 100%;
  overflow-x: auto;
}

/* =========================================================
   🔥 THAY ĐỔI CSS CHÍNH XỬ LÝ LỖI LỆCH HÀNG & KẺ VIỀN ĐỨT ĐOẠN
   ========================================================= */
table {
  width: 100%;
  border-collapse: collapse;
  color: white;
  table-layout: fixed; /* Bắt buộc bảng cố định tỉ lệ giúp đường kẻ viền chạy full 100% */
}

/* Phân chia kích thước tỉ lệ các cột */
table th:nth-child(1), table td:nth-child(1) { width: 14%; } /* Combo */
table th:nth-child(2), table td:nth-child(2) { width: 38%; } /* Món ăn thành phần */
table th:nth-child(3), table td:nth-child(3) { width: 10%; } /* Số lượng */
table th:nth-child(4), table td:nth-child(4) { width: 13%; } /* Giá combo */
table th:nth-child(5), table td:nth-child(5) { width: 11%; } /* Mô tả */
table th:nth-child(6), table td:nth-child(6) { width: 14%; } /* Hành động */

th, td {
  padding: 14px;
  border-bottom: 1px solid rgba(255,255,255,.06); /* Viền kẻ ngang tràn full bảng */
  vertical-align: middle !important; /* Gióng thẳng lề dọc tất cả tiêu đề và dữ liệu */
  box-sizing: border-box;
}

th {
  text-align: left;
  color: #f8d46a;
}

.canh-giua {
  text-align: center !important;
}

tr.active {
  background: rgba(248, 212, 106, 0.06);
}

.ten-combo-highlight {
  font-weight: 600;
  color: #fff;
  word-break: break-word;
}

/* Khung hiển thị danh sách nhiều món dạng gom nhóm */
.danh-sach-badge-mon {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.badge-ten-mon {
  background: rgba(255, 255, 255, 0.08);
  color: #f8d46a;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
  display: inline-block;
  border: 1px solid rgba(248, 212, 106, 0.2);
  white-space: nowrap;
}

.badge-ten-mon small {
  color: #fff;
  opacity: 0.8;
  margin-left: 2px;
}

.hanh-dong {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.nut-sua {
  background: rgba(248,212,106,.15);
  color: #f8d46a;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-sua:hover {
  background: rgba(248,212,106,.3);
}

.nut-xoa {
  background: rgba(255,107,107,.15);
  color: #ff6b6b;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-xoa:hover {
  background: rgba(255,107,107,.3);
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

.no-data {
  text-align: center;
  color: #a0a0a0;
  padding: 20px;
}

@media (max-width: 1200px) {
  .bo-loc-panel {
    flex-wrap: wrap;
  }
}
</style>