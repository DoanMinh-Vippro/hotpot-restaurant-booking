<script setup lang="ts">
import type { ChiTietComBo } from '../api/ChiTietComBoApi'

// Khai báo kiểu dữ liệu cho danh sách món ăn chọn từ Form
export interface MonDaChon {
  idMon: number
  tenMon: string
  soLuong?: number
}

// Cập nhật Props linh hoạt hơn
defineProps<{
  item?: ChiTietComBo
  danhSachMonDaChon?: MonDaChon[]
}>()
</script>

<template>
  <div 
    v-if="item || (danhSachMonDaChon && danhSachMonDaChon.length > 0)" 
    class="hop-xem-truoc"
  >
    <h3>Chi tiết combo xem trước</h3>

    <!-- Tên Combo -->
    <p v-if="item?.tenCombo">
      <strong>Combo:</strong> <span class="highlight-text">{{ item.tenCombo }}</span>
    </p>

    <!-- Khung hiển thị danh sách món ăn -->
    <div class="khung-danh-sach-mon">
      <strong>Món ăn chọn:</strong>

      <!-- Trường hợp 1: Truyền danh sách món đang chọn từ Form (danhSachMonDaChon) -->
      <div v-if="danhSachMonDaChon && danhSachMonDaChon.length > 0" class="danh-sach-badge">
        <span 
          v-for="m in danhSachMonDaChon" 
          :key="m.idMon" 
          class="badge-mon"
        >
          {{ m.tenMon }}
          <small v-if="m.soLuong && m.soLuong > 1">(x{{ m.soLuong }})</small>
        </span>
      </div>

      <!-- Trường hợp 2: Truyền từ item có mảng gom nhóm danhSachMon của BE -->
      <div v-else-if="item?.danhSachMon && item.danhSachMon.length > 0" class="danh-sach-badge">
        <span 
          v-for="m in item.danhSachMon" 
          :key="m.idMon" 
          class="badge-mon"
        >
          {{ m.tenMon }}
          <small v-if="m.soLuong">(x{{ m.soLuong }})</small>
        </span>
      </div>

      <!-- Trường hợp 3: Truyền từ item dữ liệu phẳng đơn lẻ cũ -->
      <div v-else-if="item?.tenMon" class="danh-sach-badge">
        <span class="badge-mon">
          {{ item.tenMon }}
          <small v-if="item.soLuong && item.soLuong > 1">(x{{ item.soLuong }})</small>
        </span>
      </div>

      <span v-else class="text-trang-thai">Chưa chọn món nào</span>
    </div>

    <!-- Số lượng (Hiển thị khi truyền item đơn lẻ) -->
    <p v-if="item && !item.danhSachMon && item.soLuong">
      <strong>Số lượng:</strong> {{ item.soLuong }}
    </p>

    <!-- Giá Combo -->
    <p v-if="item?.giaCombo !== undefined && item?.giaCombo !== null">
      <strong>Giá combo:</strong> 
      <span class="gia-text">
        {{ Number(item.giaCombo).toLocaleString('vi-VN') }} đ
      </span>
    </p>

    <!-- Mô tả -->
    <p v-if="item?.moTa">
      <strong>Mô tả:</strong> <em>{{ item.moTa }}</em>
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

.khung-danh-sach-mon {
  margin: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.danh-sach-badge {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.badge-mon {
  background: rgba(248, 212, 106, 0.15);
  color: #f8d46a;
  border: 1px solid rgba(248, 212, 106, 0.35);
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.badge-mon small {
  color: #fff;
  opacity: 0.85;
  font-weight: normal;
}

.gia-text {
  color: #6bffb8;
  font-weight: 600;
}

.text-trang-thai {
  color: #888;
  font-style: italic;
  font-size: 13px;
}
</style>