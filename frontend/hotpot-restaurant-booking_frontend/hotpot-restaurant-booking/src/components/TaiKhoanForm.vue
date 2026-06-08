<script setup lang="ts">
import TaiKhoanApi from '@/api/TaiKhoanApi';
import { ref, watch } from 'vue';
const selected= ref(null)
const formData= ref({
    id: null,
    maTaiKhoan:"",
    tenDangNhap:"",
    matKhau:"",
    trangThai: true
})
const emit = defineEmits(['refresh']) 
const add= async ()=>{
    try{
     await TaiKhoanApi.add(formData.value)
    alert('Them thanh cong!')
    emit('refresh')
    formData.value={
        id: null,
    maTaiKhoan:"",
    tenDangNhap:"",
    matKhau:"",
    trangThai: true,
    }
    }catch (error){
        console.error('them that bai:', error)
    };
}
const update = async () => {
  try {
    if (!formData.value.id) {
      alert("Chưa chọn tài khoản!")
      return
    }

    console.log("UPDATE DATA:", formData.value)

    await TaiKhoanApi.update(
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
      ...newTableData
    };
  }
}, { immediate: true });
</script>
<template>
    <div class="form-container">
        <div>
        <label>Mã tài khoản: </label>
        <input type="text" v-model="formData.maTaiKhoan">
        </div>

        <div>
        <label>Tên đăng nhập: </label>
        <input type="text" v-model="formData.tenDangNhap">
        </div>

        <div>
        <label>Mật khẩu: </label>
        <input type="number" v-model="formData.matKhau">
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
    </div>
         <button @click.prevent="add()">ADD</button> 
    <button @click.prevent="update()">UPDATE</button>
</template>
<style scoped>
.form-container {
  max-width: 420px;
  margin: 30px auto;
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(145deg, #1b1b2a, #141421);
  box-shadow: 0 12px 35px rgba(0,0,0,0.5);
  color: #fff;
  font-family: sans-serif;
  border: 1px solid rgba(255,255,255,0.08);
}

/* mỗi field */
.form-container > div {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

/* label */
label {
  margin-bottom: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #8ab4ff;
  letter-spacing: 0.5px;
}

/* input */
input {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.1);
  background: rgba(255,255,255,0.05);
  color: #fff;
  outline: none;
  transition: 0.25s;
}

/* focus input */
input:focus {
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79,172,254,0.2);
}

/* radio group */
.form-container label input {
  margin-right: 6px;
}

/* button group */
button {
  width: 48%;
  padding: 10px;
  margin-top: 10px;
  border: none;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.25s;
}

/* ADD */
button:first-of-type {
  background: linear-gradient(135deg, #00c6ff, #0072ff);
  color: #fff;
}

/* UPDATE */
button:last-of-type {
  background: linear-gradient(135deg, #ff416c, #ff4b2b);
  color: #fff;
}

/* hover */
button:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}
</style>