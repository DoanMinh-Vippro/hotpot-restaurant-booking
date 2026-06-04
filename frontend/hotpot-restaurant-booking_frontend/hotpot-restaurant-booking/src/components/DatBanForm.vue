<script setup lang="ts">
import DatBanApi from '@/api/DatBanApi';
import { ref, watch } from 'vue';


// tao object luu data vao form
const formData = ref({
    idDatBan: 0,
    ngayDat: '',
    gioDat: '',
    sdtKhachHang: '',
    trangThai: 0,
    soNguoi: 0,
    ghiChu: '',
    thoiGianDenDuKien: '',
    soTienCoc: 0,
    phuongThucThanhToan: 0    
})
//cong de nhanh du lieu tu DatBanView
const props = defineProps(['datBanForm'])

//watch dung de lay du lieu ma datBanForm nhan doc dua vao object formData
watch(() => props.datBanForm,(newData) => {
    if(newData){
        formData.value = {...newData}
    }
})

// bien dung de bao cho table load lai bang
const emit = defineEmits(['refresh'])

const add = async () =>{
    try {
        await DatBanApi.add(formData.value)
        alert('them thanh cong')
        emit('refresh')
    } catch (error) {
        console.error('them that bai', error)
    }
}

const update = async () =>{
    try {
        await DatBanApi.update(formData.value.idDatBan ,formData.value)
        alert('sua thanh cong')
        emit('refresh')
    } catch (error) {
        console.error('sua that bai', error)
    }
}
</script>

<template>
<div class="form-container">
    <h3>Thông Tin Đặt Bàn</h3>
    
    <div class="form-group">
      <label>SĐT Khách Hàng</label>
      <input v-model="formData.sdtKhachHang" type="text" placeholder="Nhập SĐT..." />
    </div>

    <div class="row">
      <div class="form-group">
        <label>Ngày Đặt</label>
        <input v-model="formData.ngayDat" type="date" />
      </div>
      <div class="form-group">
        <label>Giờ Đặt</label>
        <input v-model="formData.gioDat" type="time" />
      </div>
    </div>

    <div class="row">
      <div class="form-group">
        <label>Số Người</label>
        <input v-model.number="formData.soNguoi" type="number" />
      </div>
      <div class="form-group">
        <label>Tiền Cọc</label>
        <input v-model.number="formData.soTienCoc" type="number" />
      </div>
    </div>

    <div class="form-group">
      <label>Thời Gian Đến Dự Kiến</label>
      <input v-model="formData.thoiGianDenDuKien" type="datetime-local" />
    </div>

    <div class="row">
      <div class="form-group">
        <label>Thanh Toán</label>
        <select v-model="formData.phuongThucThanhToan">
          <option value="CHUYEN_KHOAN">Chuyển khoản</option>
          <option value="VNPAY">VNPAY</option>
        </select>
      </div>
      <div class="form-group">
        <label>Trạng Thái</label>
        <select v-model="formData.trangThai">
          <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
          <option value="DA_XAC_NHAN">Đã xác nhận</option>
          <option value="DA_NHAN_BAN">Đã nhận bàn</option>
          <option value="DA_HUY">Đã hủy</option>
          <option value="HOAN_THANH">hoàn thành</option>
        </select>
      </div>
    </div>

    <div class="form-group">
      <label>Ghi Chú</label>
      <textarea v-model="formData.ghiChu" rows="2"></textarea>
    </div>

    <div class="button-group">
      <button class="btn-add" @click.prevent="add()">Thêm Mới</button>
      <button class="btn-update" @click.prevent="update()">Cập Nhật</button>
    </div>
  </div>
</template>

<style scoped>
.form-container {
  background: #1a1a1a;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #333;
  color: #fff;
  font-family: sans-serif;
  max-width: 500px;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

label {
  color: #c5a059;
  font-size: 0.8rem;
  margin-bottom: 5px;
  text-transform: uppercase;
}

input, select, textarea {
  padding: 10px;
  background: #2a2a2a;
  border: 1px solid #444;
  color: #fff;
  border-radius: 6px;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  text-transform: uppercase;
}

.btn-add { background: #c5a059; color: #000; }
.btn-update { border: 1px solid #c5a059; color: #c5a059; background: transparent; }
</style>