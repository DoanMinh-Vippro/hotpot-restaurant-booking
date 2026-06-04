<script setup lang="ts">
import { reactive } from 'vue'
import type { Mon } from '../api/MonApi'
import type { DanhMuc } from '../api/DanhMucApi'

const props = defineProps<{
  danhSachDanhMuc: DanhMuc[]
}>()

const emit = defineEmits([
  'submit',
])

const form = reactive({
  tenMon: '',
  donGiaHienTai: '',
  idDanhMuc: '',
  trangThai: 0,
})

// Chứa thông báo lỗi
const errors = reactive({
  tenMon: '',
  donGiaHienTai: '',
  idDanhMuc: '',
})

// Hàm reset lỗi
const clearErrors = () => {
  errors.tenMon = ''
  errors.donGiaHienTai = ''
  errors.idDanhMuc = ''
}

// Validate form trước khi submit
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
  }

  // 2. Validate Đơn Giá
  const gia = form.donGiaHienTai
  if (gia === '' || gia === null || gia === undefined) {
    errors.donGiaHienTai = 'Đơn giá không được để trống'
    isValid = false
  } else if (Number(gia) <= 0) {
    errors.donGiaHienTai = 'Đơn giá phải lớn hơn 0'
    isValid = false
  }

  // 3. Validate Danh Mục
  if (form.idDanhMuc === '' || form.idDanhMuc === null || form.idDanhMuc === undefined) {
    errors.idDanhMuc = 'Danh mục không được để trống'
    isValid = false
  }

  return isValid
}

const gui = () => {
  // Nếu validate không qua, dừng lại không submit
  if (!validateForm()) return

  emit('submit', {
    tenMon: form.tenMon,
    donGiaHienTai: Number(form.donGiaHienTai),
    idDanhMuc: Number(form.idDanhMuc),
    trangThai: form.trangThai,
  })
}

defineExpose({
  fillForm(mon?: Mon) {
    clearErrors() // Xóa lỗi cũ khi mở form mới hoặc chọn món khác
    
    if (!mon) {
      form.tenMon = ''
      form.donGiaHienTai = ''
      form.idDanhMuc = ''
      form.trangThai = 0
      return
    }

    form.tenMon = mon.tenMon
    form.donGiaHienTai = mon.donGiaHienTai.toString()
    form.idDanhMuc = mon.idDanhMuc.toString()
    form.trangThai = mon.trangThai
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <div>
        <h2>Thông tin món</h2>
        <p>Thêm mới hoặc cập nhật món ăn.</p>
      </div>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Tên món</label>
        <input
          v-model="form.tenMon"
          type="text"
          placeholder="Nhập tên món"
          :class="{ 'is-invalid': errors.tenMon }"
          @input="errors.tenMon = ''"
        />
        <span class="error-text" v-if="errors.tenMon">{{ errors.tenMon }}</span>
      </div>

      <div class="form-group">
        <label>Đơn giá</label>
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
        <select 
          v-model="form.idDanhMuc" 
          :class="{ 'is-invalid': errors.idDanhMuc }"
          @change="errors.idDanhMuc = ''"
        >
          <option value="">Chọn danh mục</option>
          <option
            v-for="dm in danhSachDanhMuc"
            :key="dm.idDanhMuc"
            :value="dm.idDanhMuc"
          >
            {{ dm.loaiDanhMuc }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idDanhMuc">{{ errors.idDanhMuc }}</span>
      </div>

      <div class="form-group">
        <label>Trạng thái</label>
        <select v-model.number="form.trangThai">
          <option :value="0">Còn bán</option>
          <option :value="1">Ngưng bán</option>
        </select>
      </div>
    </div>

    <div class="nhom-nut">
      <button
        class="nut-chinh"
        type="button"
        @click="gui"
      >
        Lưu
      </button>
    </div>
  </section>
</template>

<style scoped>
/* Code style cũ của bạn */
.bieu-mau-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 28px;
  padding: 26px;
}

.tieu-de-panel {
  margin-bottom: 18px;
}

.tieu-de-panel h2 {
  color: #f8d46a;
  margin: 0 0 10px;
}

.tieu-de-panel p {
  margin: 0;
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

input[type='text'],
input[type='number'],
select {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #f5f5f5;
  border-radius: 16px;
  padding: 14px 16px;
  width: 100%;
  box-sizing: border-box;
}

select option {
  background: #151515;
  color: #ffffff;
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

/* Thêm CSS cho lỗi (Validation) */
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