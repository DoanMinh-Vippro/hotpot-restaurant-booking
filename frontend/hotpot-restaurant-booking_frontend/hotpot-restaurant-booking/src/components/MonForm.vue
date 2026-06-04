<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Mon } from '../api/MonApi'
import type { DanhMuc } from '../api/DanhMucApi'

// Nhận dữ liệu danh sách món ăn từ View cha để thực hiện kiểm tra trùng tên
const props = defineProps<{
  danhSachDanhMuc: DanhMuc[]
  danhSachMon: Mon[] 
}>()

const emit = defineEmits(['submit'])

// Quản lý trạng thái Form: Thêm mới hoặc Cập nhật
const isEditMode = ref(false)
const idMonHienTai = ref<number | null>(null)

const form = reactive({
  tenMon: '',
  donGiaHienTai: '',
  idDanhMuc: '',
  trangThai: 0,
})

// Trạng thái lưu trữ thông báo lỗi hiển thị trên giao diện
const errors = reactive({
  tenMon: '',
  donGiaHienTai: '',
  idDanhMuc: '',
})

// Hàm xóa sạch vết thông báo lỗi cũ
const clearErrors = () => {
  errors.tenMon = ''
  errors.donGiaHienTai = ''
  errors.idDanhMuc = ''
}

// Validate form trước khi submit dữ liệu lên hệ thống
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
    // ----------------------------------------------------------------------
    // THUẬT TOÁN MỚI: CHUẨN HÓA CHUỖI ĐỂ CHẶN CỐ TÌNH LẶP KÝ TỰ (LÁCH LUẬT)
    // ----------------------------------------------------------------------
    
    // Bước A: Chuyển về chữ thường, lột sạch toàn bộ dấu tiếng Việt (mực tươii -> muc tuoiii)
    const chuoiKhongDau = ten.trim().toLowerCase()
      .normalize('NFD')               // Tách dấu ra khỏi chữ cái gốc
      .replace(/[\u0300-\u036f]/g, '') // Xóa các ký tự dấu vừa tách
      .replace(/đ/g, 'd');            // Đổi riêng chữ đ thành d

    // Bước B: Quét Regex kiểm tra lặp từ liên tiếp quá 2 lần (3 chữ giống nhau sát cạnh nhau trở lên)
    if (/([a-z])\1{1,}/i.test(chuoiKhongDau)) {
      errors.tenMon = 'Tên món không được chứa các ký tự lặp lại vô nghĩa liên tiếp'
      isValid = false
    } 
    
    // Bước C: Nếu qua được bộ lọc ký tự, tiến hành kiểm tra trùng tên món trong Database thực tế
    else {
      const tenChuanHoa = ten.trim().toLowerCase()
      const biTrungTen = props.danhSachMon.some(m => {
        // Nếu ở chế độ sửa, bỏ qua không so sánh trùng với chính bản ghi hiện tại
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
  // Nếu validate không qua, dừng xử lý ngay lập tức
  if (!validateForm()) return

  emit('submit', {
    tenMon: form.tenMon.trim(),
    donGiaHienTai: Number(form.donGiaHienTai),
    idDanhMuc: Number(form.idDanhMuc),
    trangThai: form.trangThai,
  })
}

defineExpose({
  fillForm(mon?: Mon) {
    clearErrors() // Reset lỗi cũ
    
    if (!mon) {
      isEditMode.value = false
      idMonHienTai.value = null
      form.tenMon = ''
      form.donGiaHienTai = ''
      form.idDanhMuc = ''
      form.trangThai = 0
      return
    }

    // Gán trạng thái và ID để phục vụ chế độ Cập nhật
    isEditMode.value = true
    idMonHienTai.value = mon.idMon

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
        <p>{{ isEditMode ? 'Cập nhật món ăn hệ thống' : 'Thêm mới món ăn vào thực đơn' }}</p>
      </div>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Tên món</label>
        <input
          v-model="form.tenMon"
          type="text"
          placeholder="Nhập tên món (Ví dụ: Trà chanh, Lẩu bò...)"
          :class="{ 'is-invalid': errors.tenMon }"
          @input="errors.tenMon = ''"
        />
        <span class="error-text" v-if="errors.tenMon">{{ errors.tenMon }}</span>
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
        Lưu thông tin
      </button>
    </div>
  </section>
</template>

<style scoped>
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
  outline: none;
}

input:focus,
select:focus {
  border-color: #f8d46a;
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