<script setup lang="ts">
defineProps<{
  tableList: any[]
}>();
const emit =defineEmits(['detail', 'delete'])
</script>
<template>
    <table class="table-container">
        <thead>
            <tr>
                <th>Mã nhân viên</th>
                <th>Tên nhân viên</th>
                <th>Giới tính</th>
                <th>Số điện thoại</th>
                <th>Email</th>
                <th>Địa chỉ</th>
                <th>Trạng thái</th>
                <th>Chức vụ</th>
                <th>Tài khoản</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="nv in tableList" :key="nv.idNhanVien">
                <td>{{ nv.maNhanVien }}</td>
                <td>{{ nv.tenNhanVien }}</td>
                <td>{{ nv.gioiTinh? 'Nam':'Nữ' }}</td>
                <td>{{ nv.soDienThoai }}</td>
                <td>{{ nv.email }}</td>
                <td>{{ nv.diaChi }}</td>
                <td>{{ nv.trangThai? 'Hoạt động':'Ngừng' }}</td>
                <td>{{ nv.tenChucVu }}</td>
                <td>{{ nv.tenDangNhap }}</td>
                <td>
                    <button class="btn-edit" @click="emit('detail', nv)">Chi Tiết</button>
          <button class="btn-delete" @click="emit('delete', nv.id)">Xóa</button>
                </td>
            </tr>
        </tbody>
    </table>
</template>
<style scoped>
.table-container {
  width: 100%;
  border-collapse: collapse;
  background: #1e1e1e;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #2f2f2f;
  color: #e5e5e5;
  font-family: "Segoe UI", sans-serif;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
}

/* HEADER */
th {
  background: linear-gradient(135deg, #c5a059, #b38a3d);
  color: #111;
  padding: 14px;
  text-transform: uppercase;
  font-size: 13px;
  letter-spacing: 0.8px;
  border-bottom: 1px solid #333;
}

/* CELL */
td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #2a2a2a;
  font-size: 14px;
}

/* HOVER ROW */
tr:hover {
  background: #2a2a2a;
  transition: 0.2s ease;
}

/* STRIPED ROW (dễ nhìn hơn) */
tbody tr:nth-child(even) {
  background: #1a1a1a;
}

/* BUTTON COMMON */
button {
  padding: 6px 10px;
  margin: 0 4px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 12px;
  transition: 0.2s ease;
}

/* EDIT BUTTON */
.btn-edit {
  background: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background: #2563eb;
  transform: scale(1.05);
}

/* DELETE BUTTON */
.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
  transform: scale(1.05);
}

/* STATUS STYLE (optional đẹp hơn nếu muốn nâng cấp sau) */
.status-active {
  color: #22c55e;
  font-weight: 600;
}

.status-inactive {
  color: #ef4444;
  font-weight: 600;
}
</style>