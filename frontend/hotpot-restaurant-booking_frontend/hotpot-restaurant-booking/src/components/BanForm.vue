<script setup lang="ts">
import BanApi from '@/api/BanApi';
import { ref, watch } from 'vue';

// Khai báo trạng thái của form
const formData = ref({
  idBan: 0,
  loaiBan: '',
  soLuongBan: 0,
  idKhuVuc: 0,
  trangThai: 0
});

const emit = defineEmits(['refresh']) // biến dùng sau khi add/update báo cho BanView load lại bảng
const add = async () =>{
  try {
    await BanApi.add(formData.value)
    alert('Them thanh cong!')
    emit('refresh')
    formData.value = { idBan: 0, loaiBan: '', soLuongBan: 0, idKhuVuc: 0, trangThai: 0 };
  } catch (error) {
    console.error('them that bai:', error)
  }
}

const update = async () =>{
  try {
    await BanApi.update(formData.value.idBan ,formData.value)
    alert('cap nhat thanh cong!')
    emit('refresh')
  } catch (error) {
    console.error('sua that bai:', error)
  }
}
// 1. Khai báo cái cổng nhận dữ liệu từ cha
const props = defineProps(['formData']);

// 2. Khi ông chủ "ném" dữ liệu vào cái cổng đó, 
// mình dùng cái "tai" (watch) để bắt lấy rồi ghi vào tờ giấy (formData)
watch(() => props.formData, (newTableData) => {
  if (newTableData) {
    formData.value = { ...newTableData };
  }
});

</script>

<template>
  <div class="form-container">
    <div>
      <label>Loại Bàn: </label>
      <input type="text" v-model="formData.loaiBan">
    </div>

    <div>
      <label>Số Lượng: </label>
      <input type="number" v-model.number="formData.soLuongBan">
    </div>

    <div>
      <label>Khu Vực: </label>
      <select v-model="formData.idKhuVuc">
        <option value="1">Tầng 1</option>
        <option value="2">Phòng VIP</option>
        <option value="3">Sân Vườn</option>
      </select>
    </div>

    <div>
      <label>Trạng Thái: </label>
  <select v-model="formData.trangThai">
    <option value="TRONG">Trống</option>
    <option value="DANG_SU_DUNG">Đang Sử Dụng</option>
    <option value="DA_DAT">Đã Đặt</option>
    <option value="BAO_TRI">Bảo Trì</option>
  </select>
    </div>

    <button @click.prevent="add()">ADD</button> 
    <button @click.prevent="update()">UPDATE</button>
  </div>
</template>

<style scoped>
.form-container {
  background: #1a1a1a;
  padding: 2rem;
  border-radius: 12px;
  border: 1px solid #333;
  max-width: 400px;
  margin: 2rem auto;
  color: #fff;
  font-family: sans-serif;
}

.form-container > div {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #c5a059; /* Màu vàng Gold đặc trưng của bạn */
  font-weight: bold;
  text-transform: uppercase;
}

input, select {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #444;
  background: #2a2a2a;
  color: white;
  outline: none;
  transition: 0.3s;
}

input:focus, select:focus {
  border-color: #c5a059;
}

button {
  padding: 12px;
  margin-bottom: 10px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
  text-transform: uppercase;
}

button:first-of-type { /* Nút ADD */
  background: #c5a059;
  color: #000;
}

button:last-of-type { /* Nút UPDATE */
  background: transparent;
  border: 1px solid #c5a059;
  color: #c5a059;
}

button:hover {
  filter: brightness(1.2);
}
</style>