<script setup lang="ts">
import ChucVuApi from '@/api/ChucVuApi';
import { ref, watch } from 'vue';
const selected= ref(null)
const formData= ref({
    id: null,
    maChucVu:"",
    tenChucVu:"",
})
const emit = defineEmits(['refresh']) 
const add= async ()=>{
    try{
     await ChucVuApi.add(formData.value)
    alert('Them thanh cong!')
    emit('refresh')
    formData.value={
       id: null,
     maChucVu:"",
    tenChucVu:"",
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

    await ChucVuApi.update(
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
        <input type="text" v-model="formData.maChucVu">
        </div>

        <div>
        <label>Tên đăng nhập: </label>
        <input type="text" v-model="formData.tenChucVu">
        </div>

    </div>
         <button @click.prevent="add()">ADD</button> 
    <button @click.prevent="update()">UPDATE</button>
</template>
<style scoped>
.form-container {
  max-width: 460px;
  margin: 40px auto;
  padding: 28px;
  border-radius: 18px;

  background: linear-gradient(160deg, #0f172a, #111827);
  border: 1px solid rgba(255, 255, 255, 0.08);

  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6);
  color: #fff;
  font-family: system-ui, sans-serif;
}

/* mỗi input block */
.form-container > div {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* label */
label {
  font-size: 0.82rem;
  font-weight: 600;
  letter-spacing: 0.6px;
  color: #60a5fa;
  text-transform: uppercase;
}

/* input */
input {
  padding: 11px 12px;
  border-radius: 10px;

  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);

  color: #fff;
  outline: none;

  transition: 0.25s ease;
}

/* focus */
input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
}

/* button container */
button {
  width: 48%;
  padding: 11px;
  margin-top: 12px;
  margin-right: 4%;
  border: none;
  border-radius: 10px;

  font-weight: 700;
  cursor: pointer;
  transition: 0.2s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* ADD */
button:first-of-type {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: white;
}

/* UPDATE */
button:last-of-type {
  background: linear-gradient(135deg, #f59e0b, #ea580c);
  color: white;
}

/* hover effect */
button:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

/* active click */
button:active {
  transform: scale(0.98);
}
</style>