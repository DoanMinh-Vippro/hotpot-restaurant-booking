<script setup lang="ts">
// Nhận "Sổ tay" từ BanView truyền vào
defineProps<{
  tableList: any[]
  loading?: boolean
}>();

// Emit để báo cho BanView biết khi nào cần Sửa/Xóa
const emit = defineEmits(['detail', 'delete']);
</script>


<template>
  <div class="table-wrapper">
    <div v-if="loading" class="loading">Đang tải dữ liệu...</div>
    <table v-else class="table-container">
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
        <tr v-if="tableList.length === 0">
          <td colspan="7" class="no-data">Không có dữ liệu bàn</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>





<style scoped>
.table-wrapper {
  position: relative;
}

.loading {
  text-align: center;
  padding: 40px 20px;
  color: #c5a059;
  font-size: 1.1rem;
  font-weight: 600;
}

.table-container {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin-top: 20px;
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #333;
  color: #fff;
  font-family: sans-serif;
}

th {
  background: #c5a059;
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

.no-data {
  color: #999;
  font-style: italic;
}

tr:hover {
  background: #252525;
}

button {
  padding: 6px 12px;
  margin: 0 4px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.3s;
}

.btn-detail {
  background: #3498db;
  color: #fff;
}

.btn-detail:hover {
  background: #2980b9;
}

.btn-delete {
  background: #e74c3c;
  color: #fff;
}

.btn-delete:hover {
  background: #c0392b;
}
</style>