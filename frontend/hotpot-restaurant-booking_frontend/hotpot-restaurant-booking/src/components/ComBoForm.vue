<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Combo } from '../api/ComBoApi'

// CẬP NHẬT: Nhận thêm danhSachCombo từ View cha để thực hiện kiểm tra trùng tên combo
const props = defineProps<{
  danhSachCombo: Combo[]
}>()

const emit = defineEmits(['submit'])

// Biến cờ nhận biết Form đang ở chế độ "Thêm mới" hay "Cập nhật"
const isEditMode = ref(false)
const idComboHienTai = ref<number | null>(null)

const form = reactive({
  tenCombo: '',
  giaCombo: '',
  hinhAnh: '',
  trangThai: 1,
})

// Chứa thông báo lỗi hiển thị trực tiếp dưới các ô nhập liệu
const errors = reactive({
  tenCombo: '',
  giaCombo: '',
  hinhAnh: '',
})

const fileAnh = ref<File | null>(null)
const anhPreview = ref<string | null>(null)

// Xóa sạch vết thông báo lỗi cũ khi có thao tác mới
const clearErrors = () => {
  errors.tenCombo = ''
  errors.giaCombo = ''
  errors.hinhAnh = ''
}

const chonAnh = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    if (file) {
      fileAnh.value = file
      form.hinhAnh = file.name 
      anhPreview.value = URL.createObjectURL(file)
      errors.hinhAnh = '' // Xóa lỗi chọn ảnh khi đã chọn thành công
    }
  }
}

// Hàm xử lý Validate nâng cao
const validateForm = () => {
  clearErrors()
  let isValid = true

  // 1. Validate Tên Combo
  const ten = form.tenCombo || ''
  if (!ten || ten.trim() === '') {
    errors.tenCombo = "Tên combo không được để trống"
    isValid = false
  } else if (ten.length < 3 || ten.length > 50) {
    errors.tenCombo = "Tên combo phải từ 3 đến 50 ký tự"
    isValid = false
  } else if (ten !== ten.trim()) {
    errors.tenCombo = "Tên combo không được chứa khoảng trắng ở đầu hoặc cuối"
    isValid = false
  } else if (ten.includes('  ')) {
    errors.tenCombo = "Tên combo không được chứa nhiều khoảng trắng liên tiếp"
    isValid = false
  } else {
    // ----------------------------------------------------------------------
    // THUẬT TOÁN: CHUẨN HÓA CHUỖI KHÔNG DẤU ĐỂ CHẶN CỐ TÌNH GÕ THỪA CHỮ LẶP
    // ----------------------------------------------------------------------
    const chuoiKhongDau = ten.trim().toLowerCase()
      .normalize('NFD')               
      .replace(/[\u0300-\u036f]/g, '') 
      .replace(/đ/g, 'd');            

    // Bắt lỗi nếu có 2 ký tự không dấu giống hệt nhau đứng liền kề (Ví dụ: tháiii -> thaiii -> dính ii)
    if (/([a-z])\1{1,}/i.test(chuoiKhongDau)) {
      errors.tenCombo = "Tên combo không được chứa các ký tự lặp lại vô nghĩa liên tiếp"
      isValid = false
    } 
    // Kiểm tra trùng tên với hệ thống danh sách có sẵn
    else {
      const tenChuanHoa = ten.trim().toLowerCase()
      const biTrungTen = props.danhSachCombo.some(cb => {
        // Nếu đang sửa, bỏ qua không so sánh trùng với chính bản ghi hiện tại
        if (isEditMode.value && cb.idCombo === idComboHienTai.value) {
          return false
        }
        return cb.tenCombo.trim().toLowerCase() === tenChuanHoa
      })

      if (biTrungTen) {
        errors.tenCombo = "Tên gói combo này đã tồn tại trong danh sách của nhà hàng"
        isValid = false
      }
    }
  }
  
  // 2. Validate Giá Combo
  const gia = form.giaCombo
  if (!gia || gia.toString().trim() === '') {
    errors.giaCombo = "Giá combo không được để trống"
    isValid = false
  } else if (Number(gia) <= 0) {
    errors.giaCombo = "Giá combo phải lớn hơn 0"
    isValid = false
  }
  
  // 3. Validate Hình Ảnh
  if (!form.hinhAnh || !form.hinhAnh.trim()) {
    errors.hinhAnh = "Vui lòng lựa chọn hình ảnh cho combo"
    isValid = false
  }

  return isValid
}

const gui = () => {
  if (!validateForm()) return

  emit('submit', {
    tenCombo: form.tenCombo.trim(),
    giaCombo: Number(form.giaCombo),
    hinhAnh: form.hinhAnh.trim(),
    trangThai: form.trangThai,
    fileThat: fileAnh.value 
  })
}

defineExpose({
  fillForm(combo?: Combo) {
    clearErrors() // Xóa sạch thông báo lỗi cũ
    fileAnh.value = null
    anhPreview.value = null
    const fileInput = document.getElementById('file-upload') as HTMLInputElement
    if (fileInput) fileInput.value = ''

    if (!combo) {
      isEditMode.value = false
      idComboHienTai.value = null
      form.tenCombo = ''
      form.giaCombo = ''
      form.hinhAnh = ''
      form.trangThai = 1
      return
    }

    // Gán ID và bật chế độ Cập nhật dữ liệu
    isEditMode.value = true
    idComboHienTai.value = combo.idCombo

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
      <p>{{ isEditMode ? 'Cập nhật gói combo hệ thống' : 'Thêm mới gói combo ăn uống' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Tên combo</label>
        <input 
          v-model="form.tenCombo" 
          type="text" 
          placeholder="Nhập tên combo..." 
          :class="{ 'is-invalid': errors.tenCombo }"
          @input="errors.tenCombo = ''"
        />
        <span class="error-text" v-if="errors.tenCombo">{{ errors.tenCombo }}</span>
      </div>

      <div class="form-group">
        <label>Giá (đ)</label>
        <input 
          v-model="form.giaCombo" 
          type="number" 
          placeholder="Nhập giá..." 
          :class="{ 'is-invalid': errors.giaCombo }"
          @input="errors.giaCombo = ''"
        />
        <span class="error-text" v-if="errors.giaCombo">{{ errors.giaCombo }}</span>
      </div>

      <div class="form-group">
        <label>Hình ảnh</label>
        <input 
          id="file-upload"
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
        <label>Trạng thái</label>
        <select v-model.number="form.trangThai">
          <option :value="1">Còn bán</option>
          <option :value="0">Ngưng bán</option>
        </select>
      </div>
    </div>

    <div class="nhom-nut">
      <button type="button" @click="gui">Lưu thông tin</button>
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
</style>