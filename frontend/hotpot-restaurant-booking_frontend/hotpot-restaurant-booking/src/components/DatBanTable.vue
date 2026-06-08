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
  margin: 30px auto;
  background: #1a1a1a;
  border-radius: 8px;
  border: 1px solid #333;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

table {
  width: 100%;
  border-collapse: separate; /* Chuyển sang separate để tạo độ thoáng */
  border-spacing: 0;
}

th {
  background: #1a1a1a;
  color: #d4af37; /* Vàng kim */
  padding: 20px 15px;
  text-transform: uppercase;
  font-size: 0.7rem; /* Nhỏ lại để tinh tế hơn */
  letter-spacing: 2px;
  font-weight: 500;
  border-bottom: 1px solid #333;
}

td {
  padding: 18px 15px;
  color: #d1d1d1;
  font-size: 0.85rem;
  border-bottom: 1px solid #252525;
  transition: 0.3s;
}

tr:hover {
  background: #222;
}

/* Nút bấm tinh tế hơn */
button {
  padding: 6px 14px;
  border: 1px solid transparent;
  border-radius: 2px;
  cursor: pointer;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.btn-detail { 
  background: transparent; 
  border-color: #3498db; 
  color: #3498db; 
}
.btn-detail:hover { 
  background: #3498db; 
  color: #fff; 
}

.btn-delete { 
  background: transparent; 
  border-color: #8b0000; 
  color: #8b0000; 
}
.btn-delete:hover { 
  background: #8b0000; 
  color: #fff; 
}
</style>