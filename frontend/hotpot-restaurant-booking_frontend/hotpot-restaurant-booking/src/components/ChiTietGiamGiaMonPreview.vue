<script setup lang="ts">
import type { ChiTietGiamGiaMon } from '../api/ChiTietGiamGiaMonApi'

// Khai báo kiểu dữ liệu chọn linh hoạt từ Form
export interface DoiTuongGiamGiaOption {
  id: number | string
  ten: string
}

// Props linh hoạt nhận cả dữ liệu BE lẫn dữ liệu đang chọn ở Form
defineProps<{
  item?: ChiTietGiamGiaMon
  danhSachMonDaChon?: DoiTuongGiamGiaOption[]
  danhSachComboDaChon?: DoiTuongGiamGiaOption[]
  danhSachDanhMucDaChon?: DoiTuongGiamGiaOption[]
}>()

// --- HELPER FUNCTIONS XỬ LÝ TYPE SAFE TƯƠNG THÍCH MỌI DẠNG DỮ LIỆU ---
const getTenItem = (item: any, keyTen: string): string => {
  if (typeof item === 'string') return item
  if (item && typeof item === 'object' && keyTen in item) {
    return item[keyTen]
  }
  return String(item || '')
}

const getKeyItem = (item: any, keyId: string, index: number): string | number => {
  if (item && typeof item === 'object' && keyId in item && item[keyId] !== undefined) {
    return item[keyId]
  }
  return typeof item === 'string' ? `${item}-${index}` : index
}
</script>

<template>
  <div 
    v-if="
      item || 
      (danhSachMonDaChon && danhSachMonDaChon.length > 0) ||
      (danhSachComboDaChon && danhSachComboDaChon.length > 0) ||
      (danhSachDanhMucDaChon && danhSachDanhMucDaChon.length > 0)
    " 
    class="hop-xem-truoc"
  >
    <h3>Chi tiết giảm giá xem trước</h3>

    <!-- Tên Chương trình -->
    <p v-if="item?.tenChuongTrinh">
      <strong>Chương trình:</strong> 
      <span class="highlight-text">{{ item.tenChuongTrinh }}</span>
    </p>

    <!-- Khung hiển thị Danh sách đối tượng áp dụng -->
    <div class="khung-danh-sach-doi-tuong">
      <strong>Đối tượng áp dụng:</strong>

      <!-- 1. KHỐI MÓN ÁP DỤNG -->
      <!-- Ưu tiên 1: Danh sách chọn từ Form -->
      <div v-if="danhSachMonDaChon && danhSachMonDaChon.length > 0" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-mon">Món:</span>
        <div class="danh-sach-badge">
          <span v-for="m in danhSachMonDaChon" :key="m.id" class="badge-item badge-mon">
            {{ m.ten }}
          </span>
        </div>
      </div>
      <!-- Ưu tiên 2: Truyền từ item mảng gom nhóm BE -->
      <div v-else-if="item?.danhSachMon && item.danhSachMon.length > 0" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-mon">Món:</span>
        <div class="danh-sach-badge">
          <span 
            v-for="(m, idx) in item.danhSachMon" 
            :key="getKeyItem(m, 'idMon', idx)" 
            class="badge-item badge-mon"
          >
            {{ getTenItem(m, 'tenMon') }}
          </span>
        </div>
      </div>
      <!-- Ưu tiên 3: Truyền từ item dữ liệu phẳng đơn lẻ -->
      <div v-else-if="item?.tenMon" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-mon">Món:</span>
        <div class="danh-sach-badge">
          <span class="badge-item badge-mon">{{ item.tenMon }}</span>
        </div>
      </div>


      <!-- 2. KHỐI COMBO ÁP DỤNG -->
      <!-- Ưu tiên 1: Danh sách chọn từ Form -->
      <div v-if="danhSachComboDaChon && danhSachComboDaChon.length > 0" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-combo">Combo:</span>
        <div class="danh-sach-badge">
          <span v-for="c in danhSachComboDaChon" :key="c.id" class="badge-item badge-combo">
            {{ c.ten }}
          </span>
        </div>
      </div>
      <!-- Ưu tiên 2: Truyền từ item mảng gom nhóm BE -->
      <div v-else-if="item?.danhSachCombo && item.danhSachCombo.length > 0" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-combo">Combo:</span>
        <div class="danh-sach-badge">
          <span 
            v-for="(c, idx) in item.danhSachCombo" 
            :key="getKeyItem(c, 'idCombo', idx)" 
            class="badge-item badge-combo"
          >
            {{ getTenItem(c, 'tenCombo') }}
          </span>
        </div>
      </div>
      <!-- Ưu tiên 3: Dữ liệu phẳng -->
      <div v-else-if="item?.tenCombo" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-combo">Combo:</span>
        <div class="danh-sach-badge">
          <span class="badge-item badge-combo">{{ item.tenCombo }}</span>
        </div>
      </div>


      <!-- 3. KHỐI DANH MỤC ÁP DỤNG -->
      <!-- Ưu tiên 1: Danh sách chọn từ Form -->
      <div v-if="danhSachDanhMucDaChon && danhSachDanhMucDaChon.length > 0" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-danhmuc">Danh mục:</span>
        <div class="danh-sach-badge">
          <span v-for="d in danhSachDanhMucDaChon" :key="d.id" class="badge-item badge-danhmuc">
            {{ d.ten }}
          </span>
        </div>
      </div>
      <!-- Ưu tiên 2: Truyền từ item mảng gom nhóm BE -->
      <div v-else-if="item?.danhSachDanhMuc && item.danhSachDanhMuc.length > 0" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-danhmuc">Danh mục:</span>
        <div class="danh-sach-badge">
          <span 
            v-for="(dm, idx) in item.danhSachDanhMuc" 
            :key="getKeyItem(dm, 'idDanhMuc', idx)" 
            class="badge-item badge-danhmuc"
          >
            {{ getTenItem(dm, 'tenDanhMuc') }}
          </span>
        </div>
      </div>
      <!-- Ưu tiên 3: Dữ liệu phẳng -->
      <div v-else-if="item?.tenDanhMuc" class="danh-sach-badge-group">
        <span class="nhan-phan-loai nhan-danhmuc">Danh mục:</span>
        <div class="danh-sach-badge">
          <span class="badge-item badge-danhmuc">{{ item.tenDanhMuc }}</span>
        </div>
      </div>

      <!-- Không chọn đối tượng nào -->
      <span 
        v-if="
          !item?.tenMon && !item?.tenCombo && !item?.tenDanhMuc &&
          (!item?.danhSachMon || item.danhSachMon.length === 0) &&
          (!item?.danhSachCombo || item.danhSachCombo.length === 0) &&
          (!item?.danhSachDanhMuc || item.danhSachDanhMuc.length === 0) &&
          (!danhSachMonDaChon || danhSachMonDaChon.length === 0) &&
          (!danhSachComboDaChon || danhSachComboDaChon.length === 0) &&
          (!danhSachDanhMucDaChon || danhSachDanhMucDaChon.length === 0)
        " 
        class="text-trang-thai"
      >
        Chưa chọn đối tượng áp dụng
      </span>
    </div>

    <!-- Mức giảm & Loại giảm -->
    <p v-if="item?.mucGiam !== undefined && item?.mucGiam !== null">
      <strong>Mức giảm:</strong> 
      <span class="muc-giam-text">
        {{ item.loaiGiam === 'TIEN' ? Number(item.mucGiam).toLocaleString('vi-VN') + ' đ' : item.mucGiam + '%' }}
      </span>
    </p>

    <!-- Trạng thái -->
    <p v-if="item?.trangThai !== undefined">
      <strong>Trạng thái:</strong> 
      <span :class="item.trangThai === 0 ? 'status-active' : 'status-inactive'">
        {{ item.trangThai === 0 ? 'Còn hiệu lực' : 'Hết hiệu lực' }}
      </span>
    </p>
  </div>
</template>

<style scoped>
.hop-xem-truoc {
  margin-top: 18px;
  padding: 16px 20px;
  border-radius: 16px;
  background: rgba(248, 212, 106, 0.06);
  border: 1px solid rgba(248, 212, 106, 0.18);
  color: #d8d8d8;
  font-size: 14px;
  line-height: 1.6;
}

h3 {
  color: #f8d46a;
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px dashed rgba(248, 212, 106, 0.2);
  padding-bottom: 8px;
}

p {
  margin: 6px 0;
}

.highlight-text {
  color: #fff;
  font-weight: 600;
}

.khung-danh-sach-doi-tuong {
  margin: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.danh-sach-badge-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.danh-sach-badge {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* Nhãn phân loại loại đối tượng */
.nhan-phan-loai {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  padding: 2px 6px;
  border-radius: 4px;
}

.nhan-mon { color: #f8d46a; background: rgba(248, 212, 106, 0.12); }
.nhan-combo { color: #69c0ff; background: rgba(105, 192, 255, 0.12); }
.nhan-danhmuc { color: #b7eb8f; background: rgba(183, 235, 143, 0.12); }

/* Item Badges */
.badge-item {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  font-weight: 500;
}

.badge-mon {
  background: rgba(248, 212, 106, 0.15);
  color: #f8d46a;
  border: 1px solid rgba(248, 212, 106, 0.35);
}

.badge-combo {
  background: rgba(105, 192, 255, 0.15);
  color: #69c0ff;
  border: 1px solid rgba(105, 192, 255, 0.35);
}

.badge-danhmuc {
  background: rgba(183, 235, 143, 0.15);
  color: #b7eb8f;
  border: 1px solid rgba(183, 235, 143, 0.35);
}

.muc-giam-text {
  color: #6bffb8;
  font-weight: 600;
  font-size: 15px;
}

.text-trang-thai {
  color: #888;
  font-style: italic;
  font-size: 13px;
}

.status-active {
  color: #52c41a;
  font-weight: 500;
}

.status-inactive {
  color: #ff4d4f;
  font-weight: 500;
}
</style>