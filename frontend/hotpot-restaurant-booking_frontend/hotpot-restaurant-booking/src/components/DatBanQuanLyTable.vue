<script setup lang="ts">
defineProps<{
    list: any[]
}>()

const emit = defineEmits(['detail','delete'])
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
            <button class="btn-detail" @click="emit('detail', d)">DETAIL</button>
            <button class="btn-delete" @click="emit('delete', d.idDatBan)">DELETE</button>
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
  background: #1a1a1a;
  border-radius: 12px;
  border: 1px solid #333;
  padding: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #fff;
}

th {
  color: #c5a059;
  text-transform: uppercase;
  font-size: 0.75rem;
  padding: 15px;
  border-bottom: 2px solid #333;
}

td {
  padding: 15px;
  text-align: center;
  border-bottom: 1px solid #222;
  font-size: 0.9rem;
}

.status-badge {
  background: #252525;
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid #c5a059;
  color: #c5a059;
  font-size: 0.7rem;
}

button {
  padding: 6px 12px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-weight: bold;
  margin: 0 4px;
  font-size: 0.8rem;
}

.btn-detail { background: #c5a059; color: #000; }
.btn-delete { background: #333; color: #ff4d4d; border: 1px solid #ff4d4d; }
</style>