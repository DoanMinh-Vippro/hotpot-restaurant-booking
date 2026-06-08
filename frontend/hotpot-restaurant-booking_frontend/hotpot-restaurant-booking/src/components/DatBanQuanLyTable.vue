<script setup lang="ts">
defineProps<{
  list: any[]
}>()

const emit = defineEmits(['detail', 'delete'])
</script>

<template>
  <div class="table-container">
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>LOẠI BÀN</th>
          <th>KHÁCH HÀNG</th>
          <th>SĐT</th>
          <th>NGÀY ĐẶT</th>
          <th>GIỜ ĐẶT</th>
          <th>SỐ NGƯỜI</th>
          <th>DỰ KIẾN ĐẾN</th>
          <th>TIỀN CỌC</th>
          <th>TRẠNG THÁI</th>
          <th>TRẠNG THÁI CỌC</th>
          <th>THANH TOÁN</th>
          <th>GHI CHÚ</th>
          <th>THAO TÁC</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in list" :key="d.idDatBan">
          <td>{{ d.idDatBan }}</td>
          <td>{{ d.loaiBan }}</td>
          <td>{{ d.tenKhachHang }}</td>
          <td>{{ d.sdtKhachHang }}</td>
          <td>{{ d.ngayDat }}</td>    
          <td>{{ d.gioDat }}</td>
          <td>{{ d.soNguoi }}</td>
          <td>{{ d.thoiGianDenDuKien }}</td>
          <td>{{ d.soTienCoc }}</td>
          <td>{{ d.trangThai }}</td>
          <td>{{ d.trangThaiCoc }}</td>
          <td>{{ d.phuongThucThanhToan }}</td>
          <td>{{ d.ghiChu }}</td>
          <td>
            <div class="vertical-actions">
              <button class="btn-action btn-detail" @click="emit('detail', d)">DETAIL</button>
              <button class="btn-action btn-delete" @click="emit('delete', d.idDatBan)">DELETE</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-container {
  width: 100%;
  overflow-x: auto;
  background: #0d0d0d;
  border-radius: 12px;
  border: 1px solid #222;
  padding: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #fff;
  white-space: nowrap;
}

th {
  color: #c5a059;
  text-transform: uppercase;
  font-size: 0.7rem;
  letter-spacing: 1px;
  padding: 15px;
  border-bottom: 2px solid #222;
}

td {
  padding: 12px 15px;
  text-align: center;
  border-bottom: 1px solid #1a1a1a;
  font-size: 0.85rem;
}

/* Vertical Action Container */
.vertical-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
}

/* Button Styling */
.btn-action {
  width: 85px; /* Cố định chiều rộng để nút cân đối */
  padding: 6px 0;
  font-size: 0.65rem;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.3s ease;
  border-radius: 4px;
}

/* Detail Button (Gold) */
.btn-detail {
  background: transparent;
  border-color: #c5a059;
  color: #c5a059;
}
.btn-detail:hover {
  background: #c5a059;
  color: #000;
}

/* Delete Button (Red) */
.btn-delete {
  background: transparent;
  border-color: #8b0000;
  color: #ff4d4d;
}
.btn-delete:hover {
  background: #8b0000;
  color: #fff;
}
</style>