<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Mon } from '../api/MonApi'
import type { DanhMuc } from '../api/DanhMucApi'

const props = defineProps<{
  danhSachDanhMuc: DanhMuc[]
  danhSachMon: Mon[] 
}>()

const emit = defineEmits(['submit'])

const isEditMode = ref(false)
const idMonHienTai = ref<number | null>(null)

const form = reactive({
  tenMon: '',
  hinhAnh: '', // Lưu tên file ảnh (ví dụ: gado.jpg)
  donGiaHienTai: '',
  idDanhMuc: '',
  trangThai: 0,
  trangThaiBan: 1,
})

const errors = reactive({
  tenMon: '',
  hinhAnh: '',
  donGiaHienTai: '',
  idDanhMuc: '',
})

// Bộ đôi quản lý File vật lý và Link xem trước giống Combo
const fileAnh = ref<File | null>(null)
const anhPreview = ref<string | null>(null)

const clearErrors = () => {
  errors.tenMon = ''
  errors.hinhAnh = ''
  errors.donGiaHienTai = ''
  errors.idDanhMuc = ''
}

// Hàm chọn ảnh cục bộ giống Combo
const chonAnh = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    if (file) {
      fileAnh.value = file
      form.hinhAnh = file.name 
      anhPreview.value = URL.createObjectURL(file)
      errors.hinhAnh = '' 
    }
  }
}

const validateForm = () => {
  clearErrors()
  let isValid = true

  // 1. Validate Tên Món
  const ten = form.tenMon || ''
  if (!ten || ten.trim() === '') {
    errors.tenMon = 'Tên món không được để trống'
    isValid = false
  } else if (ten.length < 3 || ten.length > 40) {
    errors.tenMon = 'Tên món phải từ 3 đến 40 ký tự'
    isValid = false
  } else if (ten !== ten.trim()) {
    errors.tenMon = 'Tên món không được chứa khoảng trắng ở đầu hoặc cuối'
    isValid = false
  } else if (ten.includes('  ')) {
    errors.tenMon = 'Tên món không được chứa nhiều khoảng trắng liên tiếp'
    isValid = false
  } else {
    const chuoiKhongDau = ten.trim().toLowerCase()
      .normalize('NFD')               
      .replace(/[\u0300-\u036f]/g, '') 
      .replace(/đ/g, 'd')

    if (/([a-z])\1{1,}/i.test(chuoiKhongDau)) {
      errors.tenMon = 'Tên món không được chứa các ký tự lặp lại vô nghĩa liên tiếp'
      isValid = false
    } else {
      const tenChuanHoa = ten.trim().toLowerCase()
      const biTrungTen = props.danhSachMon.some(m => {
        if (isEditMode.value && m.idMon === idMonHienTai.value) {
          return false
        }
        return m.tenMon.trim().toLowerCase() === tenChuanHoa
      })

      if (biTrungTen) {
        errors.tenMon = 'Tên món ăn này đã tồn tại trong thực đơn của nhà hàng'
        isValid = false
      }
    }
  }

  // 2. Validate Hình Ảnh
  if (!form.hinhAnh || !form.hinhAnh.trim()) {
    errors.hinhAnh = 'Vui lòng lựa chọn hình ảnh cho món ăn'
    isValid = false
  }

  // 3. Validate Đơn Giá
  const gia = form.donGiaHienTai
  if (gia === '' || gia === null || gia === undefined) {
    errors.donGiaHienTai = 'Đơn giá không được để trống'
    isValid = false
  } else if (Number(gia) <= 0) {
    errors.donGiaHienTai = 'Đơn giá phải lớn hơn 0'
    isValid = false
  }

  // 4. Validate Danh Mục
  if (!form.idDanhMuc) {
    errors.idDanhMuc = 'Danh mục không được để trống'
    isValid = false
  }

  return isValid
}

const gui = () => {
  if (!validateForm()) return

  emit('submit', {
    tenMon: form.tenMon.trim(),
    hinhAnh: form.hinhAnh.trim(),
    donGiaHienTai: Number(form.donGiaHienTai),
    idDanhMuc: Number(form.idDanhMuc),
    trangThai: form.trangThai,
    trangThaiBan: form.trangThaiBan,
    fileThat: fileAnh.value // Đẩy file ảnh vật lý ra tầng ngoài xử lý upload giống Combo
  })
}

defineExpose({
  fillForm(mon?: Mon) {
    clearErrors() 
    fileAnh.value = null
    anhPreview.value = null
    
    const fileInput = document.getElementById('mon-file-upload') as HTMLInputElement
    if (fileInput) fileInput.value = ''
    
    if (!mon) {
      isEditMode.value = false
      idMonHienTai.value = null
      form.tenMon = ''
      form.hinhAnh = ''
      form.donGiaHienTai = ''
      form.idDanhMuc = ''
      form.trangThai = 0
      form.trangThaiBan = 1
      return
    }

    isEditMode.value = true
    idMonHienTai.value = mon.idMon

    form.tenMon = mon.tenMon
    form.hinhAnh = mon.hinhAnh
    form.donGiaHienTai = mon.donGiaHienTai.toString()
    form.idDanhMuc = mon.idDanhMuc.toString()
    form.trangThai = mon.trangThai
    form.trangThaiBan = mon.trangThaiBan
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Món ăn</h2>
      <p>{{ isEditMode ? 'Cập nhật món ăn hệ thống' : 'Thêm mới món ăn vào thực đơn' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Tên món</label>
        <input
          v-model="form.tenMon"
          type="text"
          placeholder="Nhập tên món..."
          :class="{ 'is-invalid': errors.tenMon }"
          @input="errors.tenMon = ''"
        />
        <span class="error-text" v-if="errors.tenMon">{{ errors.tenMon }}</span>
      </div>

      <div class="form-group">
        <label>Hình ảnh</label>
        <input
          id="mon-file-upload"
          type="file"
          accept="image/*"
          @change="chonAnh"
          class="input-file"
          :class="{ 'is-invalid': errors.hinhAnh }"
        />
        <span class="error-text" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
      </div>

      <div class="khung-xem-anh" v-if="anhPreview || form.hinhAnh">
        <p class="nhan-anh">Ảnh hiển thị:</p>
        <img
          :src="anhPreview || `http://localhost:8080/uploads/${form.hinhAnh}`"
          alt="Preview"
        />
      </div>

      <div class="form-group">
        <label>Đơn giá (đ)</label>
        <input
          v-model="form.donGiaHienTai"
          type="number"
          placeholder="Nhập đơn giá"
          :class="{ 'is-invalid': errors.donGiaHienTai }"
          @input="errors.donGiaHienTai = ''"
        />
        <span class="error-text" v-if="errors.donGiaHienTai">{{ errors.donGiaHienTai }}</span>
      </div>

      <div class="form-group">
        <label>Danh mục</label>
        <select v-model="form.idDanhMuc" :class="{ 'is-invalid': errors.idDanhMuc }" @change="errors.idDanhMuc = ''">
          <option value="">Chọn danh mục</option>
          <option v-for="dm in danhSachDanhMuc" :key="dm.idDanhMuc" :value="dm.idDanhMuc">
            {{ dm.loaiDanhMuc }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idDanhMuc">{{ errors.idDanhMuc }}</span>
      </div>

      <div class="form-group">
        <label>Trạng thái kinh doanh</label>
        <select v-model.number="form.trangThai">
          <option :value="0">Còn bán</option>
          <option :value="1">Ngưng bán</option>
        </select>
      </div>

      <div class="form-group">
        <label>Kho hàng</label>
        <select v-model.number="form.trangThaiBan" :disabled="form.trangThai === 1">
          <option :value="1">Còn hàng</option>
          <option :value="0">Hết hàng</option>
        </select>
      </div>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="gui">Lưu thông tin</button>
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

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  color: #d8d8d8;
  margin-bottom: 6px;
}

input,
select {
  margin-top: 6px;
  border: 1px solid rgba(255,255,255,.08);
  background: rgba(255,255,255,.04);
  color: white;
  border-radius: 16px;
  padding: 14px 16px;
  outline: none;
  box-sizing: border-box;
  width: 100%;
}

input:focus,
select:focus {
  border-color: #f8d46a;
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

.nut-chinh {
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

.error-text {
  color: #ff6b6b;
  font-size: 13px;
  margin-top: 6px;
  margin-left: 8px;
}

.is-invalid {
  border: 1px solid #ff6b6b !important;
  background: rgba(255, 107, 107, 0.05) !important;
}

select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>