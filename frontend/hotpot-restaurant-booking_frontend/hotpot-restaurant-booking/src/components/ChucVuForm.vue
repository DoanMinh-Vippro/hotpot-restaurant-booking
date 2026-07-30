<script setup lang="ts">
import ChucVuApi from '@/api/ChucVuApi';
import { computed, ref, watch } from 'vue';
const selected = ref(null)
const formData = ref({
    id: null,
    maChucVu: "",
    tenChucVu: "",
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
const props = defineProps<{
  formData: any
  mode?: 'create' | 'edit'
}>()

const mode = computed(() => props.mode || (props.formData?.id ? 'edit' : 'create'))

watch(() => props.formData, (newTableData) => {
  if (!newTableData) {
    formData.value = {
      id: null,
      maChucVu: '',
      tenChucVu: '',
    };
    return;
  }

  formData.value = {
    ...newTableData
  };
}, { immediate: true });
</script>
<template>
    <div class="form-container">
        <div class="form-field">
        <label>Mã chức vụ</label>
        <input type="text" v-model="formData.maChucVu" placeholder="Nhập mã chức vụ" />
        </div>

        <div class="form-field">
        <label>Tên chức vụ</label>
        <input type="text" v-model="formData.tenChucVu" placeholder="Nhập tên chức vụ" />
        </div>

      <div class="button-row">
         <button v-if="mode === 'create'" class="btn-primary" @click.prevent="add()">Thêm chức vụ</button>
         <button v-if="mode === 'edit'" class="btn-secondary" @click.prevent="update()">Cập nhật chức vụ</button>
      </div>
    </div>
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
.form-field {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-container .button-row {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
.btn-primary,
.btn-secondary {
  padding: 12px 16px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  font-weight: 700;
  transition: all 0.2s ease;
}
.btn-primary {
  background: linear-gradient(135deg, #f1c56c, #d8a85c);
  color: #3d2814;
}
.btn-primary:hover {
  filter: brightness(0.95);
}
.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #f8f1d7;
  border: 1px solid rgba(255,255,255,0.22);
}
.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.18);
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

/* label */
label {
  font-size: 0.88rem;
  font-weight: 600;
  letter-spacing: 0.6px;
  color: #88b7ff;
  text-transform: uppercase;
}

input {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.06);
  color: #f6f7fb;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
}

button.btn-primary,
button.btn-secondary {
  padding: 12px 18px;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.2s ease;
}

button.btn-primary {
  background: linear-gradient(135deg, #f1c56c, #d8a85c);
  color: #3d2814;
}

button.btn-primary:hover {
  filter: brightness(0.95);
}

button.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #f8f1d7;
  border: 1px solid rgba(255,255,255,0.2);
}

button.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.18);
}
</style>