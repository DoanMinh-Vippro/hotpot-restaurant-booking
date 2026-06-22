<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import { ref, watch } from 'vue';
const formData= ref({
     id: null,
  maNhanVien: "",
  tenNhanVien: "",
  gioiTinh: 1,
  soDienThoai: "",
  email: "",
  diaChi: "",
  trangThai: 1,
  idChucVu: null,
  idTaiKhoan: null
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
    gioiTinh: 1,
    soDienThoai:"",
    email:"",
    diaChi:"",
    trangThai: 1,
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
watch(() => props.formData, (newVal) => {
  if (!newVal) {
    formData.value = {
      id: null,
      maNhanVien: "",
      tenNhanVien: "",
      gioiTinh: 1,
      soDienThoai: "",
      email: "",
      diaChi: "",
      trangThai: 1,
      idChucVu: null,
      idTaiKhoan: null,
    };
    return;
  }

  formData.value = {
    ...newVal,
    gioiTinh: Number(newVal.gioiTinh),
    trangThai: Number(newVal.trangThai),
  };
}, { immediate: true, deep: true });
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
  <label>Giới tính:</label>

  <label>
    <input type="radio" :value="1" v-model="formData.gioiTinh">
    Nam
  </label>

  <label>
    <input type="radio" :value="0" v-model="formData.gioiTinh">
    Nữ
  </label>
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
    <input type="radio" :value="1" v-model="formData.trangThai">
    Hoạt động
  </label>
  <label>
    <input type="radio" :value="0" v-model="formData.trangThai">
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
  background: linear-gradient(145deg, #141414, #0f0f0f);
  padding: 24px;
  border-radius: 14px;
  border: 1px solid #2a2a2a;
  max-width: 420px;
  margin: 20px auto;
  color: #fff;
  font-family: "Segoe UI", sans-serif;
  box-shadow: 0 10px 30px rgba(0,0,0,0.6);
}

/* FIELD */
.form-container > div {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
}

/* LABEL - gold sang hơn */
label {
  margin-bottom: 6px;
  font-size: 12px;
  color: #d4af37;   /* GOLD */
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

/* INPUT / SELECT */
input, select {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #333;
  background: #1c1c1c;
  color: white;
  outline: none;
  transition: 0.2s;
}

input:focus, select:focus {
  border-color: #d4af37;
  box-shadow: 0 0 0 2px rgba(212,175,55,0.2);
}

/* RADIO */
input[type="radio"] {
  accent-color: #d4af37;
}

/* BUTTON */
button {
  padding: 10px 14px;
  margin-right: 10px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  text-transform: uppercase;
}

/* ADD BUTTON */
button:first-of-type {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  color: #111;
  border: none;
}

button:first-of-type:hover {
  filter: brightness(1.1);
}

/* UPDATE BUTTON */
button:last-of-type {
  background: transparent;
  border: 1px solid #d4af37;
  color: #d4af37;
}

button:last-of-type:hover {
  background: #d4af37;
  color: #111;
}
</style>
