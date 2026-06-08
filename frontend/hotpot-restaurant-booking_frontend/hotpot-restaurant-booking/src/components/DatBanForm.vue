<script setup lang="ts">
import DatBanApi from '@/api/DatBanApi';
import { ref, watch } from 'vue';


// tao object luu data vao form
const formData = ref({
    idDatBan: 0,
    sdtKhachHang: '',
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
        <label>Số Người</label>
        <input v-model.number="formData.soNguoi" type="number" />
      </div>
      <div class="form-group">
      <label>Thời Gian Đến Dự Kiến</label>
      <input v-model="formData.thoiGianDenDuKien" type="datetime-local" />
    </div>
    </div>

    

    <div class="row">
      <div class="form-group">
        <label>Phương Thức Thanh Toán</label>
        <select v-model="formData.phuongThucThanhToan">
          <option value="CHUYEN_KHOAN">Chuyển khoản</option>
          <option value="VNPAY">VNPAY</option>
        </select>
      </div>
      <div class="form-group">
        <label>Tiền Cọc</label>
        <input v-model.number="formData.soTienCoc" type="number" readonly/>
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
  padding: 35px;
  border-radius: 4px;
  max-width: 500px;
  margin: 20px auto;
  border: 1px solid #3d3d3d;
  box-shadow: 0 20px 40px rgba(0,0,0,0.5);
}

h3 {
  color: #d4af37;
  text-align: center;
  font-size: 1.4rem;
  letter-spacing: 3px;
  margin-bottom: 35px;
  text-transform: uppercase;
  font-weight: 300;
}

/* Quan trọng: Tạo khoảng cách giữa các nhóm */
.form-group {
  margin-bottom: 25px; 
}

/* Xử lý hàng đôi */
.row {
  display: flex;
  gap: 20px;
}
.row > .form-group {
  flex: 1;
}

label {
  color: #a0a0a0;
  font-size: 0.65rem;
  letter-spacing: 1.5px;
  margin-bottom: 8px;
  text-transform: uppercase;
  display: block;
}

input, select, textarea {
  width: 100%;
  padding: 8px 0;
  background: transparent;
  border: none;
  border-bottom: 1px solid #444;
  color: #fff;
  font-size: 0.9rem;
  transition: all 0.3s;
}

input:focus, select:focus, textarea:focus {
  border-bottom: 1px solid #d4af37;
  outline: none;
}

.button-group {
  margin-top: 40px;
  display: flex;
  gap: 15px;
}

button {
  flex: 1;
  padding: 12px;
  border: 1px solid #d4af37;
  background: transparent;
  color: #d4af37;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.4s;
}

button:hover {
  background: #d4af37;
  color: #1a1a1a;
}
/* Ép màu cho danh sách xổ xuống */
select option {
  background-color: #1a1a1a; /* Màu nền tối của form */
  color: #fff;              /* Màu chữ trắng */
  padding: 10px;
}

/* Loại bỏ cái border-bottom không cần thiết trong select */
select {
  appearance: none;         /* Loại bỏ mũi tên mặc định của trình duyệt */
  -webkit-appearance: none;
  -moz-appearance: none;
  cursor: pointer;
  background-image: url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%23d4af37%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E");
  background-repeat: no-repeat;
  background-position: right 0px top 50%;
  background-size: 10px auto;
}
input[type="datetime-local"]::-webkit-calendar-picker-indicator {
    filter: invert(1); /* Đảo màu icon lịch để hợp với nền đen */
    cursor: pointer;
}
</style>