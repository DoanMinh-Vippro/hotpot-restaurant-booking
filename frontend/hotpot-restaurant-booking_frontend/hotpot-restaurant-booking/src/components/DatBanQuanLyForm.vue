<script setup lang="ts">
import DatBanQuanLyApi from '@/api/DatBanQuanLy';
import { ref, watch } from 'vue';

const formData = ref({
    idDatBan: 0,
    loaiBan: '',
    idkhachHang: null as number | null,
    sdtKhachHang: '',
    soNguoi: 0,
    trangThai: null as number | null, 
    ghiChu: '',
    thoiGianDenDuKien: '',           
    soTienCoc: 0,
    trangThaiCoc: null as number | null, 
    phuongThucThanhToan: null as number | null 
});

const props = defineProps(['datBanQuanLy'])

watch(()=>props.datBanQuanLy,(newData)=>{
    if(newData){
        formData.value = {...newData}
    }
})

const emit = defineEmits(['refresh'])

const add = async () => {
    try {
        await DatBanQuanLyApi.add(formData.value)
        alert('them thanh cong')
        emit('refresh')
    } catch (error) {
        console.error('them that bai',error)
    }
}


const update = async () => {
    try {
        await DatBanQuanLyApi.update(formData.value.idDatBan ,formData.value)
        alert('them thanh cong')
        emit('refresh')
    } catch (error) {
        console.error('them that bai',error)
    }
}


</script>

<template>
  <div class="form-container">
    <div class="grid-form">
      <div class="form-group">
        <label>SĐT Khách Hàng</label>
        <input v-model="formData.sdtKhachHang" type="text" placeholder="Nhập số điện thoại...">
      </div>
      <div class="form-group">
        <label>ID Khách Hàng</label>
        <input v-model.number="formData.idkhachHang" type="number" placeholder="ID...">
      </div>

      <div class="form-group">
        <label>Loại Bàn</label>
        <input v-model="formData.loaiBan" type="text" placeholder="VD: Bàn 4 người...">
      </div>
      <div class="form-group">
        <label>Số Người</label>
        <input v-model.number="formData.soNguoi" type="number">
      </div>

      <div class="form-group">
        <label>Tiền Cọc</label>
        <input v-model.number="formData.soTienCoc" type="number">
      </div>
      <div class="form-group">
        <label>Thời Gian Đến Dự Kiến</label>
        <input v-model="formData.thoiGianDenDuKien" type="datetime-local">
      </div>

      <div class="form-group">
        <label>Thanh Toán</label>
        <select v-model.number="formData.phuongThucThanhToan">
          <option value="CHUYEN_KHOAN">Chuyển khoản</option>
          <option value="VNPAY">VNPAY</option>
          <option value="TIEN_MAT">Tiền mặt</option>
        </select>
      </div>
      <div class="form-group">
        <label>Trạng Thái</label>
        <select v-model.number="formData.trangThai">
          <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
          <option value="DA_XAC_NHAN">Đã xác nhận</option>
          <option value="DA_NHAN_BAN">Đã nhận bàn</option>
          <option value="DA_HUY">Đã hủy</option>
          <option value="HOAN_THANH">hoàn thành</option>
        </select>
      </div>
      <div class="form-group">
        <label>Trạng Thái Cọc</label>
        <select v-model.number="formData.trangThaiCoc">
          <option value="CHUA_COC">Chưa cọc</option>
          <option value="DA_COC">Đã cọc</option>
          <option value="DA_HOAN_COC">Đã hoàn cọc</option>
          <option value="KHONG_HOAN_COC">Không hoàn cọc</option>
        </select>
      </div>
    </div>

    <div class="form-group full-width">
      <label>Ghi Chú</label>
      <textarea v-model="formData.ghiChu" rows="2"></textarea>
    </div>

    <div class="button-group">
      <button class="btn-add" @click.prevent="add()">ADD</button>
      <button class="btn-update" @click.prevent="update()">UPDATE</button>
    </div>
  </div>
</template>

<style scoped>
.form-container {
  background: #1a1a1a;
  padding: 30px;
  border-radius: 16px;
  border: 1px solid #333;
  max-width: 600px;
  margin: 20px auto;
  color: #fff;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
}

.grid-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.full-width { grid-column: span 2; }

label {
  color: #c5a059;
  font-size: 0.75rem;
  font-weight: bold;
  text-transform: uppercase;
  margin-bottom: 8px;
}

input, select, textarea {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #444;
  background: #252525;
  color: white;
  transition: 0.3s;
}

input:focus, select:focus { border-color: #c5a059; outline: none; }

.button-group {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.btn-add { 
  background: #c5a059; 
  color: #000; 
  padding: 12px 40px;
  border-radius: 6px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  text-transform: uppercase;
}
</style>