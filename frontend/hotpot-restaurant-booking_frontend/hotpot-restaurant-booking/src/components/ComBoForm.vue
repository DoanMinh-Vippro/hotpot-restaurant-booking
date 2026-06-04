<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Combo } from '../api/ComBoApi'

const emit = defineEmits(['submit'])

const form = reactive({
  tenCombo: '',
  giaCombo: '',
  hinhAnh: '',
  trangThai: 1,
})

const fileAnh = ref<File | null>(null)
const anhPreview = ref<string | null>(null)

const chonAnh = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    
    // Thêm dòng if này để TypeScript biết chắc chắn file không bị undefined
    if (file) {
      fileAnh.value = file
      form.hinhAnh = file.name 
      anhPreview.value = URL.createObjectURL(file)
    }
  }
}

const gui = () => {
  const ten = form.tenCombo || ''
  const gia = Number(form.giaCombo)

  // VALIDATE
  if (!ten) return alert("Tên combo không được để trống")
  if (ten.length < 3 || ten.length > 50) return alert("Tên combo phải từ 3 đến 50 ký tự")
  if (ten !== ten.trim()) return alert("Tên combo không được chứa khoảng trắng ở đầu hoặc cuối")
  if (/\s{2,}/.test(ten)) return alert("Tên combo không được chứa nhiều khoảng trắng liên tiếp")
  
  if (!form.giaCombo) return alert("Giá combo không được để trống")
  if (gia <= 0) return alert("Giá combo phải lớn hơn 0")
  
  if (!form.hinhAnh || !form.hinhAnh.trim()) return alert("Vui lòng chọn hình ảnh")

  emit('submit', {
    tenCombo: ten,
    giaCombo: gia,
    hinhAnh: form.hinhAnh.trim(),
    trangThai: form.trangThai,
    fileThat: fileAnh.value // Gửi file thật lên View
  })
}

defineExpose({
  fillForm(combo?: Combo) {
    fileAnh.value = null
    anhPreview.value = null
    const fileInput = document.getElementById('file-upload') as HTMLInputElement
    if (fileInput) fileInput.value = ''

    if (!combo) {
      form.tenCombo = ''
      form.giaCombo = ''
      form.hinhAnh = ''
      form.trangThai = 1
      return
    }

    form.tenCombo = combo.tenCombo
    form.giaCombo = combo.giaCombo.toString()
    form.hinhAnh = combo.hinhAnh
    form.trangThai = combo.trangThai
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Combo</h2>
      <p>Thêm mới hoặc cập nhật combo.</p>
    </div>

    <div class="luoi-bieu-mau">
      <label>
        Tên combo
        <input v-model="form.tenCombo" type="text" placeholder="Nhập tên combo..." />
      </label>

      <label>
        Giá
        <input v-model="form.giaCombo" type="number" placeholder="Nhập giá..." />
      </label>

      <label>
        Hình ảnh
        <input 
          id="file-upload"
          type="file" 
          accept="image/*" 
          @change="chonAnh" 
          class="input-file"
        />
      </label>

      <div class="khung-xem-anh" v-if="anhPreview || form.hinhAnh">
        <p class="nhan-anh">Ảnh hiện tại:</p>
        <img 
          :src="anhPreview || `http://localhost:8080/uploads/${form.hinhAnh}`" 
          alt="Preview" 
        />
      </div>

      <label>
        Trạng thái
        <select v-model.number="form.trangThai">
          <option :value="1">Còn bán</option>
          <option :value="0">Ngưng bán</option>
        </select>
      </label>
    </div>

    <div class="nhom-nut">
      <button type="button" @click="gui">Lưu</button>
    </div>
  </section>
</template>

<style scoped>
.bieu-mau-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 28px;
  padding: 26px;
  color: white;
}

.tieu-de-panel h2 {
  color: #f8d46a;
  margin-bottom: 10px;
}

.tieu-de-panel p {
  color: #c7c7c7;
}

.luoi-bieu-mau {
  display: grid;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  color: #d8d8d8;
}

input,
select {
  margin-top: 6px;
  border: 1px solid rgba(255,255,255,.08);
  background: rgba(255,255,255,.04);
  color: white;
  border-radius: 16px;
  padding: 14px 16px;
}

.input-file {
  padding: 10px 14px;
  cursor: pointer;
}
.input-file::-webkit-file-upload-button {
  background: #f8d46a;
  color: #1a1410;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  font-weight: bold;
  cursor: pointer;
  margin-right: 12px;
}

.khung-xem-anh {
  margin-top: 4px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px dashed rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  text-align: center;
}
.nhan-anh {
  font-size: 13px;
  color: #a0a0a0;
  margin-bottom: 8px;
  text-align: left;
}
.khung-xem-anh img {
  max-width: 100%;
  max-height: 150px;
  object-fit: cover;
  border-radius: 12px;
}

.nhom-nut {
  margin-top: 18px;
}

button {
  width: 100%;
  border: none;
  border-radius: 16px;
  padding: 12px;
  background: #f8d46a;
  color: #1a1410;
  font-weight: 600;
  cursor: pointer;
}
select option {
  background: #151515;
  color: #ffffff;
}
</style>