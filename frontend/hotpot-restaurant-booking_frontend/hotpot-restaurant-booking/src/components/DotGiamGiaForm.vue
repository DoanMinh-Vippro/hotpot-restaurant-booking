<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { DotGiamGia, DotGiamGiaRequest } from '../api/DotGiamGiaApi'

// Nhận danh sách hiện có từ View cha để thực hiện check trùng tên chương trình
const props = defineProps<{
  danhSach: DotGiamGia[]
}>()

const emit = defineEmits(['submit'])

// Biến cờ nhận biết trạng thái Form
const isEditMode = ref(false)
const idDotGiamGiaHienTai = ref<number | null>(null)

const form = reactive({
  tenChuongTrinh: '',
  ngayBatDau: '',
  ngayKetThuc: '',
})

// Trạng thái lưu trữ thông báo lỗi hiển thị dưới các ô nhập liệu
const errors = reactive({
  tenChuongTrinh: '',
  ngayBatDau: '',
  ngayKetThuc: '',
})

// Hàm dọn sạch thông báo lỗi cũ
const clearErrors = () => {
  errors.tenChuongTrinh = ''
  errors.ngayBatDau = ''
  errors.ngayKetThuc = ''
}

// Hàm xử lý Validate nâng cao cho Đợt Giảm Giá
const validateForm = () => {
  clearErrors()
  let isValid = true

  // =============== 1. VALIDATE TÊN CHƯƠNG TRÌNH ===============
  const ten = form.tenChuongTrinh || ''
  if (!ten || ten.trim() === '') {
    errors.tenChuongTrinh = 'Tên chương trình không được để trống'
    isValid = false
  } else if (ten.length < 3 || ten.length > 50) {
    errors.tenChuongTrinh = 'Tên chương trình phải từ 3 đến 50 ký tự'
    isValid = false
  } else if (ten !== ten.trim()) {
    errors.tenChuongTrinh = 'Tên chương trình không được chứa khoảng trắng ở đầu hoặc cuối'
    isValid = false
  } else if (ten.includes('  ')) {
    errors.tenChuongTrinh = 'Tên chương trình không được chứa nhiều khoảng trắng liên tiếp'
    isValid = false
  } else {
    // Thuật toán bóc tách lột sạch dấu tiếng Việt để bắt lỗi gõ thừa chữ lách luật
    const chuoiKhongDau = ten.trim().toLowerCase()
      .normalize('NFD')               
      .replace(/[\u0300-\u036f]/g, '') 
      .replace(/đ/g, 'd');            

    if (/([a-z])\1{1,}/i.test(chuoiKhongDau)) {
      errors.tenChuongTrinh = 'Tên chương trình không được chứa các ký tự lặp lại vô nghĩa liên tiếp'
      isValid = false
    } 
    // Kiểm tra trùng tên trong mảng danh sách từ View cha gửi xuống
    else {
      const tenChuanHoa = ten.trim().toLowerCase()
      const biTrungTen = props.danhSach.some(d => {
        if (isEditMode.value && d.idDotGiamGia === idDotGiamGiaHienTai.value) {
          return false // Bỏ qua bản ghi đang chỉnh sửa
        }
        return d.tenChuongTrinh.trim().toLowerCase() === tenChuanHoa
      })

      if (biTrungTen) {
        errors.tenChuongTrinh = 'Chương trình giảm giá này đã tồn tại trong hệ thống'
        isValid = false
      }
    }
  }

  // =============== 2. VALIDATE THỜI GIAN ===============
  if (!form.ngayBatDau) {
    errors.ngayBatDau = 'Vui lòng chọn ngày bắt đầu chương trình'
    isValid = false
  }

  if (!form.ngayKetThuc) {
    errors.ngayKetThuc = 'Vui lòng chọn ngày kết thúc chương trình'
    isValid = false
  }

  // Nếu cả hai ngày đều đã được chọn, tiến hành kiểm tra logic bắc cầu thời gian
  if (form.ngayBatDau && form.ngayKetThuc) {
    const batDau = new Date(form.ngayBatDau)
    const ketThuc = new Date(form.ngayKetThuc)

    if (ketThuc <= batDau) {
      errors.ngayKetThuc = 'Ngày kết thúc chương trình phải lớn hơn ngày bắt đầu'
      isValid = false
    }
  }

  return isValid
}

const gui = () => {
  if (!validateForm()) return

  emit('submit', {
    tenChuongTrinh: form.tenChuongTrinh.trim(),
    ngayBatDau: form.ngayBatDau,
    ngayKetThuc: form.ngayKetThuc,
  } as DotGiamGiaRequest)
}

defineExpose({
  fillForm(item?: DotGiamGia) {
    clearErrors() // Xóa vết lỗi cũ
    
    if (!item) {
      isEditMode.value = false
      idDotGiamGiaHienTai.value = null
      form.tenChuongTrinh = ''
      form.ngayBatDau = ''
      form.ngayKetThuc = ''
      return
    }

    isEditMode.value = true
    idDotGiamGiaHienTai.value = item.idDotGiamGia

    form.tenChuongTrinh = item.tenChuongTrinh
    form.ngayBatDau = item.ngayBatDau
    form.ngayKetThuc = item.ngayKetThuc
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Đợt giảm giá</h2>
      <p>{{ isEditMode ? 'Cập nhật thông tin chương trình' : 'Thêm mới chương trình khuyến mãi' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Tên chương trình <span class="bat-buoc">*</span></label>
        <input
          v-model="form.tenChuongTrinh"
          type="text"
          placeholder="Ví dụ: Khuyến mãi hè, Chào năm mới..."
          :class="{ 'is-invalid': errors.tenChuongTrinh }"
          @input="errors.tenChuongTrinh = ''"
        />
        <span class="error-text" v-if="errors.tenChuongTrinh">{{ errors.tenChuongTrinh }}</span>
      </div>

      <div class="form-group">
        <label>Ngày bắt đầu <span class="bat-buoc">*</span></label>
        <input
          v-model="form.ngayBatDau"
          type="date"
          :class="{ 'is-invalid': errors.ngayBatDau }"
          @change="errors.ngayBatDau = ''"
        />
        <span class="error-text" v-if="errors.ngayBatDau">{{ errors.ngayBatDau }}</span>
      </div>

      <div class="form-group">
        <label>Ngày kết thúc <span class="bat-buoc">*</span></label>
        <input
          v-model="form.ngayKetThuc"
          type="date"
          :class="{ 'is-invalid': errors.ngayKetThuc }"
          @change="errors.ngayKetThuc = ''"
        />
        <span class="error-text" v-if="errors.ngayKetThuc">{{ errors.ngayKetThuc }}</span>
      </div>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="gui">
        Lưu chương trình
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
  color: white;
}

.tieu-de-panel h2 {
  color: #f8d46a;
  margin-bottom: 10px;
}

.tieu-de-panel p {
  color: #c7c7c7;
  font-size: 14px;
}

.luoi-bieu-mau {
  display: grid;
  gap: 14px;
  margin-top: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  color: #d8d8d8;
  margin-bottom: 6px;
  font-size: 14px;
}

.bat-buoc {
  color: #ff6b6b;
}

input {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #f5f5f5;
  border-radius: 16px;
  padding: 14px 16px;
  width: 100%;
  box-sizing: border-box;
  outline: none;
}

input:focus {
  border-color: #f8d46a;
}

/* Định phom giao diện cho lịch Date Picker trên Dark Mode */
input[type="date"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
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
  transition: all 0.2s;
}

.nut-chinh:hover {
  background: #e5c158;
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