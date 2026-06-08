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
                <th>Mã tài khoản</th>
                <th>Tên đăng nhập</th>
                <th>Mật khẩu</th>
                <th>Trạng thái</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="nv in tableList" :key="nv.idTaiKhoan">
                <td>{{ nv.maTaiKhoan }}</td>
                <td>{{ nv.tenDangNhap }}</td>
                <td>{{ nv.matKhau }}</td>
                <td>{{ nv.trangThai? 'Hoạt động':'Ngừng' }}</td>
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
  margin-top: 20px;
  background: linear-gradient(145deg, #1e1e2f, #141420);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.4);
  font-family: sans-serif;
  color: #fff;
}

/* Header */
th {
  padding: 14px;
  background: linear-gradient(90deg, #6a11cb, #2575fc);
  color: #fff;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
}

/* Body */
td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  font-size: 0.95rem;
}

/* Hover row */
tr:hover {
  background: rgba(255,255,255,0.06);
  transition: 0.3s;
}

/* Action buttons */
button {
  padding: 6px 10px;
  margin: 0 4px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.25s;
  font-size: 0.8rem;
}

/* Chi tiết */
.btn-edit {
  background: linear-gradient(135deg, #00c6ff, #0072ff);
  color: white;
}
.btn-edit:hover {
  transform: scale(1.05);
  filter: brightness(1.1);
}

/* Xóa */
.btn-delete {
  background: linear-gradient(135deg, #ff416c, #ff4b2b);
  color: white;
}
.btn-delete:hover {
  transform: scale(1.05);
  filter: brightness(1.1);
}
</style>