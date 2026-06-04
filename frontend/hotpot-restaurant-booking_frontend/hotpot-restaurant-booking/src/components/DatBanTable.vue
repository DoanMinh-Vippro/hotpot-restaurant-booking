<script setup lang="ts">


defineProps<{ // nhận list dât từ Dặt Bàn view truyền vào
    list: any[]
}>();

//báo cho Đặt bàn view biết khi nào cần sửa xóa
const emit = defineEmits(['detail','delete'])
</script>

<template>
    <div class="table-container">
<table>
<thead>
    <tr>
      <th>ID</th>
      <th>SĐT Khách</th>
      <th>Ngày Đặt</th>
      <th>Giờ Đặt</th>
      <th>Số Người</th>
      <th>Thời gian đến dự kiến</th>
      <th>Số Tiền Cọc</th>
      <th>Thanh Toán</th>
      <th>Trạng Thái</th>
      <th>Ghi Chú</th>
      <th>Thao Tác</th>
    </tr>
  </thead>
  <tbody>
    <tr  v-for="d in list" :key="d.idDatBan">
        <td>{{ d.idDatBan }}</td>
        <td>{{ d.sdtKhachHang }}</td>
        <td>{{ d.ngayDat }}</td>
        <td>{{ d.gioDat }}</td>
        <td>{{ d.soNguoi }}</td>
        <td>{{ d.thoiGianDenDuKien }}</td>
        <td>{{ d.soTienCoc }}</td>
        <td>{{ d.phuongThucThanhToan }}</td>
        <td>{{ d.trangThai }}</td>
        <td>{{ d.ghiChu }}</td>
        <td>
            <button class="btn-detail" @click="emit('detail', d)">Detail</button>
            <button class="btn-delete" @click="emit('delete', d.idDatBan)">Delete</button>

        </td>
    </tr>
  </tbody>
</table>
</div>


</template>


<style scoped>
.table-container {
  width: 100%;
  margin-top: 20px;
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden; /* Quan trọng để bo góc bảng */
  border: 1px solid #333;
  color: #fff;
  font-family: sans-serif;
}

table {
  width: 100%;
  border-collapse: collapse; /* Đổi sang collapse cho đồng bộ */
}

th {
  background: #c5a059; /* Màu Vàng Gold */
  color: #000;
  padding: 15px;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
}

td {
  padding: 12px;
  border-top: 1px solid #333;
  text-align: center;
  font-size: 0.9rem;
}

tr:hover {
  background: #252525;
}

/* Style cho các nút bấm */
button {
  padding: 6px 12px;
  margin: 0 4px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.3s;
}

button:hover {
  opacity: 0.8;
}

.btn-detail { background: #3498db; color: #fff; }
.btn-delete { background: #e74c3c; color: #fff; }
</style>