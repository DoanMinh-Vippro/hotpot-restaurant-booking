<script setup lang="ts">
import { ref } from 'vue'
import type { ChiTietGiamGiaMon } from '../api/ChiTietGiamGiaMonApi'

defineProps<{
  danhSach: ChiTietGiamGiaMon[]
  loading?: boolean
  selectedId: number | null
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset'])

// Quản lý trạng thái bộ lọc tìm kiếm nội bộ của Table
const searchTenChuongTrinh = ref('')
const searchTenMon = ref('')
const searchLoaiGiam = ref('')

const kichHoatTimKiem = () => {
  emit('search', {
    tenChuongTrinh: searchTenChuongTrinh.value,
    tenMon: searchTenMon.value,
    loaiGiam: searchLoaiGiam.value
  })
}

const kichHoatLamMoi = () => {
  searchTenChuongTrinh.value = ''
  searchTenMon.value = ''
  searchLoaiGiam.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  if (confirm('Xoá mục giảm giá này?')) {
    emit('delete', id)
  }
}

// --- HELPER FUNCTIONS ĐỂ TYPE CHECK AN TOÀN TRONG TEMPLATE ---

// Lấy tên hiển thị an toàn
const getTenItem = (item: any, keyTen: string): string => {
  if (typeof item === 'string') return item
  if (item && typeof item === 'object' && keyTen in item) {
    return item[keyTen]
  }
  return String(item || '')
}

// Lấy key/id an toàn cho v-for
const getKeyItem = (item: any, keyId: string, index: number): string | number => {
  if (item && typeof item === 'object' && keyId in item && item[keyId] !== undefined) {
    return item[keyId]
  }
  return typeof item === 'string' ? `${item}-${index}` : index
}
</script>

<template>
  <div class="khu-vuc-danh-sach">
    
    <!-- Bộ lọc -->
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
      
      <select v-model="searchLoaiGiam" @change="kichHoatTimKiem">
        <option value="">-- Chọn loại giảm --</option>
        <option value="PHANTRAM">Phần trăm</option>
        <option value="TIEN">Tiền mặt</option>      
      </select>

      <button class="nut-tim" @click="kichHoatTimKiem">Tìm kiếm</button>
      <button class="nut-lam-moi" @click="kichHoatLamMoi">Làm mới</button>
    </div>

    <!-- Panel Danh sách -->
    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <div>
          <h2>Chi tiết giảm giá món</h2>
          <p>Quản lý chương trình giảm giá áp dụng lên từng món, combo và danh mục.</p>
        </div>

        <button class="nut-phu" @click="$emit('add')">
          + Thêm giảm giá mới
        </button>
      </div>

      <div class="khung-cuon-bang">
        <table>
          <thead>
            <tr>
              <th>Chương trình</th>
              <th>Đối tượng áp dụng</th>
              <th class="canh-giua">Mức giảm</th>
              <th class="canh-giua">Loại giảm</th>
              <th class="canh-giua">Trạng thái</th>
              <th class="canh-giua">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr 
              v-for="item in danhSach" 
              :key="item.idChiTietGiamGiaMon"
              :class="{ active: item.idChiTietGiamGiaMon === selectedId }"
            >
              <!-- 1. Tên Chương trình -->
              <td>
                <span class="ten-chuong-trinh-highlight">{{ item.tenChuongTrinh }}</span>
              </td>

              <!-- 2. Đối tượng áp dụng -->
              <td>
                <div class="nhom-doi-tuong-ap-dung">
                  
                  <!-- Khối Món -->
                  <div 
                    v-if="(item.danhSachMon && item.danhSachMon.length > 0) || item.tenMon" 
                    class="danh-sach-badge-group"
                  >
                    <span class="nhan-phan-loai nhan-mon">Món:</span>
                    <template v-if="item.danhSachMon && item.danhSachMon.length > 0">
                      <span 
                        v-for="(mon, idx) in item.danhSachMon" 
                        :key="getKeyItem(mon, 'idMon', idx)" 
                        class="badge-item badge-mon"
                      >
                        {{ getTenItem(mon, 'tenMon') }}
                      </span>
                    </template>
                    <span v-else class="badge-item badge-mon">
                      {{ item.tenMon }}
                    </span>
                  </div>

                  <!-- Khối Combo -->
                  <div 
                    v-if="(item.danhSachCombo && item.danhSachCombo.length > 0) || item.tenCombo" 
                    class="danh-sach-badge-group match-top"
                  >
                    <span class="nhan-phan-loai nhan-combo">Combo:</span>
                    <template v-if="item.danhSachCombo && item.danhSachCombo.length > 0">
                      <span 
                        v-for="(combo, idx) in item.danhSachCombo" 
                        :key="getKeyItem(combo, 'idCombo', idx)" 
                        class="badge-item badge-combo"
                      >
                        {{ getTenItem(combo, 'tenCombo') }}
                      </span>
                    </template>
                    <span v-else class="badge-item badge-combo">
                      {{ item.tenCombo }}
                    </span>
                  </div>

                  <!-- Khối Danh mục -->
                  <div 
                    v-if="(item.danhSachDanhMuc && item.danhSachDanhMuc.length > 0) || item.tenDanhMuc" 
                    class="danh-sach-badge-group match-top"
                  >
                    <span class="nhan-phan-loai nhan-danhmuc">Danh mục:</span>
                    <template v-if="item.danhSachDanhMuc && item.danhSachDanhMuc.length > 0">
                      <span 
                        v-for="(dm, idx) in item.danhSachDanhMuc" 
                        :key="getKeyItem(dm, 'idDanhMuc', idx)" 
                        class="badge-item badge-danhmuc"
                      >
                        {{ getTenItem(dm, 'tenDanhMuc') }}
                      </span>
                    </template>
                    <span v-else class="badge-item badge-danhmuc">
                      {{ item.tenDanhMuc }}
                    </span>
                  </div>

                </div>
              </td>

              <!-- 3. Mức giảm -->
              <td class="canh-giua">
                <strong class="muc-giam-highlight">
                  {{ item.loaiGiam === 'TIEN' ? Number(item.mucGiam).toLocaleString('vi-VN') : item.mucGiam }}
                </strong>
              </td>

              <!-- 4. Loại giảm -->
              <td class="canh-giua">
                <span class="badge-loai-giam">
                  {{ item.loaiGiam === 'PHANTRAM' ? 'Phần trăm (%)' : 'Tiền mặt (đ)' }}
                </span>
              </td>

              <!-- 5. Trạng thái -->
              <td class="canh-giua">
                <span :class="item.trangThai === 0 ? 'trang-thai-con' : 'trang-thai-ngung'">
                  {{ item.trangThai === 0 ? 'Còn hiệu lực' : 'Hết hiệu lực' }}
                </span>
              </td>

              <!-- 6. Hành động -->
              <td class="canh-giua">
                <div class="hanh-dong">
                  <button class="nut-sua" @click="$emit('edit', item)">
                    Sửa
                  </button>
                  <button class="nut-xoa" @click="xoa(item.idChiTietGiamGiaMon)">
                    Xoá
                  </button>
                </div>
              </td>
            </tr>

            <!-- Không có dữ liệu -->
            <tr v-if="danhSach.length === 0">
              <td colspan="6" class="no-data">
                Không tìm thấy chi tiết giảm giá món nào phù hợp.
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

/* Bộ lọc panel */
.bo-loc-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  align-items: center;
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
  transition: border-color 0.2s;
  font-family: inherit;
}

.bo-loc-panel input:focus,
.bo-loc-panel select:focus {
  border-color: #d8a85c;
}

.bo-loc-panel select option {
  background: #fffdf8;
  color: #5f3d22;
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
  background: #efe0c1;
  color: #5f3d22;
}

/* Bảng danh sách */
.danh-sach-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 28px;
  padding: 26px;
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

.khung-cuon-bang {
  width: 100%;
  overflow-x: auto;
}

/* Bảng dữ liệu tỉ lệ cố định */
table {
  width: 100%;
  border-collapse: collapse;
  color: #5f3d22;
  table-layout: fixed;
}

/* Tỉ lệ phân chia các cột */
table th:nth-child(1), table td:nth-child(1) { width: 20%; } /* Chương trình */
table th:nth-child(2), table td:nth-child(2) { width: 36%; } /* Đối tượng áp dụng */
table th:nth-child(3), table td:nth-child(3) { width: 10%; } /* Mức giảm */
table th:nth-child(4), table td:nth-child(4) { width: 12%; } /* Loại giảm */
table th:nth-child(5), table td:nth-child(5) { width: 10%; } /* Trạng thái */
table th:nth-child(6), table td:nth-child(6) { width: 12%; } /* Hành động */

th, td {
  padding: 14px;
  border-bottom: 1px solid #efe0c1;
  vertical-align: middle !important;
  box-sizing: border-box;
}

th {
  text-align: left;
  color: #8b5e34;
}

.canh-giua {
  text-align: center !important;
}

tr.active {
  background: rgba(216, 168, 92, 0.12);
}

.ten-chuong-trinh-highlight {
  font-weight: 600;
  color: #3d2814;
  word-break: break-word;
}

/* CSS Nhóm đối tượng áp dụng */
.nhom-doi-tuong-ap-dung {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.danh-sach-badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.nhan-phan-loai {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 2px;
}

.nhan-mon { color: #8b5e34; background: #fff3d3; }
.nhan-combo { color: #1890ff; background: #e6f7ff; }
.nhan-danhmuc { color: #389e0d; background: #f6ffed; }

/* Badge hiển thị tên các mục */
.badge-item {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
  display: inline-block;
  white-space: nowrap;
}

.badge-mon {
  background: #fffdf8;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
}

.badge-combo {
  background: #f0f5ff;
  color: #2f54eb;
  border: 1px solid #adc6ff;
}

.badge-danhmuc {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.muc-giam-highlight {
  color: #27ae60;
  font-size: 15px;
}

.badge-loai-giam {
  font-size: 12px;
  color: #8f6b46;
}

/* Trạng thái */
.trang-thai-con {
  color: #27ae60;
  font-weight: 500;
}

.trang-thai-ngung {
  color: #e74c3c;
  font-weight: 500;
}

.hanh-dong {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.nut-sua {
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-sua:hover {
  background: #f8d46a;
  color: #1a1410;
}

.nut-xoa {
  background: #fff1f0;
  color: #e74c3c;
  border: 1px solid #ffa39e;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-xoa:hover {
  background: #ff4d4f;
  color: #fff;
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
  color: #8f6b46;
  padding: 20px;
}

@media (max-width: 1200px) {
  .bo-loc-panel {
    flex-wrap: wrap;
  }
}
</style>