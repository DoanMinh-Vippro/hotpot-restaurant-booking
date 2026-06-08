<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import { ref, watch } from 'vue';
const selected= ref(null)
const formData= ref({
    id: null,
    maNhanVien:"",
    tenNhanVien:"",
    gioiTinh:true,
    soDienThoai:"",
    email:"",
    diaChi:"",
    trangThai: true,
    idChucVu: null,
    idTaiKhoan: null,
})
const emit = defineEmits(['refresh']) 
const add= async ()=>{
    try{
     await NhanVienApi.add(formData.value)
    alert('Them thanh cong!')
    emit('refresh')
    formData.value={
        id: null,
    maNhanVien:"",
    tenNhanVien:"",
    gioiTinh:true,
    soDienThoai:"",
    email:"",
    diaChi:"",
    trangThai: true,
    idChucVu: null,
    idTaiKhoan: null
    }
    }catch (error){
        console.error('them that bai:', error)
    };
}
const update = async () => {
  try {
    if (!formData.value.id) {
      alert("Chưa chọn nhân viên!")
      return
    }

    console.log("UPDATE DATA:", formData.value)

    await NhanVienApi.update(
      formData.value.id,
      formData.value
    )

    alert('Cập nhật thành công!')
    emit('refresh')

  } catch (error: any) {
    console.log("UPDATE ERROR:", error)
    console.log("SERVER:", error?.response?.data)
  }
}
const props = defineProps(['formData']);
watch(() => props.formData, (newTableData) => {
  if (newTableData) {
    formData.value = {
      ...newTableData,
      idChucVu: newTableData.idChucVu,
      idTaiKhoan: newTableData.idTaiKhoan
    };
  }
}, { immediate: true });
</script>
<template>
    <div class="form-container">
        <div>
        <label>Mã nhân viên: </label>
        <input type="text" v-model="formData.maNhanVien">
        </div>

        <div>
        <label>Tên nhân viên: </label>
        <input type="text" v-model="formData.tenNhanVien">
        </div>

        <div>
        <label>Số điện thoại: </label>
        <input type="text" v-model="formData.soDienThoai">
        </div>

        <div>
        <label>Email: </label>
        <input type="text" v-model="formData.email">
        </div>

        <div>
        <label>Địa chỉ: </label>
        <input type="text" v-model="formData.diaChi">
        </div>

        
        <div>
  <label>Trạng thái:</label>

  <label>
    <input type="radio" :value="true" v-model="formData.trangThai">
    Hoạt động
  </label>

  <label>
    <input type="radio" :value="false" v-model="formData.trangThai">
    Ngừng
  </label>
</div>

        <div>
        <label>Chức vụ: </label>
        <select v-model.number="formData.idChucVu">
            <option :value="1">Quản lý</option>
            <option :value="2">Thu ngân</option>
            <option :value="3">Nhân viên</option>
        </select>
        </div>

      <div>
        <label>Tài khoản: </label>
        <select v-model.number="formData.idTaiKhoan">
            <option :value="1">Admin</option>
            <option :value="2">Thungan01</option>
            <option :value="3">Nhanvien01</option>
        </select>
        </div>
    </div>
    <div>
        <button @click.prevent="add()">ADD</button> 
    <button @click.prevent="update()">UPDATE</button>
    </div>
</template>
<style scoped>
.form-container {
  background: #1b1b1b;
  padding: 24px;
  border-radius: 14px;
  border: 1px solid #2f2f2f;
  max-width: 420px;
  margin: 20px auto;
  color: #fff;
  font-family: "Segoe UI", sans-serif;
  box-shadow: 0 8px 25px rgba(0,0,0,0.4);
}

.form-container > div {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 6px;
  font-size: 12px;
  color: #c5a059;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

input, select {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #3a3a3a;
  background: #242424;
  color: white;
  outline: none;
  transition: 0.2s;
}

input:focus, select:focus {
  border-color: #c5a059;
  box-shadow: 0 0 0 2px rgba(197,160,89,0.2);
}

/* RADIO */
input[type="radio"] {
  accent-color: #c5a059;
}

/* BUTTON AREA */
button {
  padding: 10px 14px;
  margin-right: 10px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  text-transform: uppercase;
}

/* ADD */
button:first-of-type {
  background: #c5a059;
  color: #000;
  border: none;
}

button:first-of-type:hover {
  filter: brightness(1.1);
}

/* UPDATE */
button:last-of-type {
  background: transparent;
  border: 1px solid #c5a059;
  color: #c5a059;
}

button:last-of-type:hover {
  background: #c5a059;
  color: #000;
}
</style>