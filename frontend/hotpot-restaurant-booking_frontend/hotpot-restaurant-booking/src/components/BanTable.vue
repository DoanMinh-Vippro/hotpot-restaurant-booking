<script setup lang="ts">
// Nhận "Sổ tay" từ BanView truyền vào
defineProps<{
  tableList: any[]
}>();

// Emit để báo cho BanView biết khi nào cần Sửa/Xóa
const emit = defineEmits(['detail', 'delete']);
</script>


<template>
  <table class="table-container">
    <thead>
      <tr>
        <th>Id Bàn</th>
        <th>Loại Bàn</th>
        <th>Số Lượng</th>
        <th>Id Khu Vực</th>
        <th>Tên Khu Vực</th>
        <th>Trạng Thái</th>
        <th>Thao Tác</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="b in tableList" :key="b.idBan">
        <td>{{ b.idBan }}</td>
        <td>{{ b.loaiBan }}</td>
        <td>{{ b.soLuongBan }}</td>
        <td>{{ b.idKhuVuc }}</td>
        <td>{{ b.tenKhuVuc }}</td>
        <td>{{ b.trangThai }}</td>
        <td>
          <button class="btn-detail" @click="emit('detail', b)">Detail</button>
          <button class="btn-delete" @click="emit('delete', b.idBan)">Delete</button>
        </td>
      </tr>
    </tbody>
  </table>
</template>





<style scoped>
.table-container {
  width: 100%;
  border-collapse: separate; /* Dùng separate để bo góc */
  border-spacing: 0;
  margin-top: 20px;
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden; /* Cắt góc cho các hàng bên trong */
  border: 1px solid #333;
  color: #fff;
  font-family: sans-serif;
}

th {
  background: #c5a059; /* Màu Vàng Gold thương hiệu */
  color: #000;
  padding: 15px;
  text-transform: uppercase;
  font-size: 0.9rem;
  letter-spacing: 1px;
}

td {
  padding: 12px;
  border-top: 1px solid #333;
  text-align: center;
  font-size: 0.95rem;
}

tr:hover {
  background: #252525; /* Hiệu ứng sáng lên khi di chuột qua */
}

/* Style cho các nút bấm trong bảng */
button {
  padding: 6px 12px;
  margin: 0 4px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.3s;
}

.btn-edit { background: #3498db; color: #fff; }
.btn-edit:hover { background: #2980b9; }

.btn-delete { background: #e74c3c; color: #fff; }
.btn-delete:hover { background: #c0392b; }
</style>