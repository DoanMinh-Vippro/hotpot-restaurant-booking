<script setup lang="ts">
import { reactive, ref } from 'vue'

// Định nghĩa kiểu dữ liệu nội bộ (Nếu bạn chưa có file API, có thể dùng trực tiếp kiểu này)
interface DanhMucRequest {
  tenDanhMuc: string
  moTa: string
}

const props = defineProps<{
  danhSach: any[] // Nhận danh sách hiện tại từ cha để chống trùng tên danh mục
}>()

const emit = defineEmits(['submit'])

// Biến cờ nhận biết trạng thái Form
const isEditMode = ref(false)
const idDanhMucHienTai = ref<number | null>(null)

const form = reactive({
  tenDanhMuc: '',
  moTa: '',
})

// Trạng thái lưu trữ thông báo lỗi validate hiển thị lên giao diện
const errors = reactive({
  tenDanhMuc: '',
  moTa: '',
})

// Xóa sạch thông báo lỗi cũ khi người dùng thao tác lại
const clearErrors = () => {
  errors.tenDanhMuc = ''
  errors.moTa = ''
}

// Hàm xử lý Validate nâng cao cho Danh Mục
const validateForm = () => {
  clearErrors()
  let isValid = true

  const ten = form.tenDanhMuc ? form.tenDanhMuc.trim() : ''
  const mTa = form.moTa ? form.moTa.trim() : ''

  // =============== 1. VALIDATE TÊN DANH MỤC ===============
  if (ten === '') {
    errors.tenDanhMuc = "Tên danh mục không được để trống"
    isValid = false
  } else if (ten.length < 2 || ten.length > 50) {
    errors.tenDanhMuc = "Tên danh mục phải nằm trong khoảng từ 2 đến 50 ký tự"
    isValid = false
  } else if (ten !== form.tenDanhMuc) {
    errors.tenDanhMuc = "Tên danh mục không được chứa khoảng trắng thừa ở đầu hoặc cuối"
    isValid = false
  } else if (/\s{2,}/.test(ten)) {
    errors.tenDanhMuc = "Tên danh mục không được chứa nhiều khoảng trắng liên tiếp"
    isValid = false
  }
  // Bộ lọc Regex: Chống lách luật gõ lặp ký tự (ví dụ: "Lẩuuuuuuu", "Combooooo")
  else if (/([a-àảãáạăằẳẵắặâầẩẫấậeèẻẽéẹêềểễếệiìỉĩíịoòỏõóọôồổỗốộơờởỡớợuùủũúụưừửữứựyỳỷỹýỵ])\1{3,}/i.test(ten)) {
    errors.tenDanhMuc = "Tên danh mục chứa ký tự lặp lại quá nhiều lần (Spam)"
    isValid = false
  }
  // Bộ lọc Regex: Chống lách luật lặp từ (ví dụ: "Lẩu lẩu lẩu lẩu")
  else if (/\b(\w+)\b(?:\s+\1\b){2,}/i.test(ten)) {
    errors.tenDanhMuc = "Tên danh mục không được lặp lại một từ quá nhiều lần"
    isValid = false
  }
  // Kiểm tra trùng lặp tên danh mục trong hệ thống
  else if (props.danhSach && props.danhSach.length > 0) {
    const biTrungTen = props.danhSach.some(item => {
      // Nếu đang ở chế độ Sửa dòng hiện tại, bỏ qua không đối chiếu trùng với chính nó
      if (isEditMode.value && item.idDanhMuc === idDanhMucHienTai.value) {
        return false
      }
      return item.tenDanhMuc?.toString().trim().toLowerCase() === ten.toLowerCase()
    })

    if (biTrungTen) {
      errors.tenDanhMuc = "Tên danh mục này đã tồn tại trong hệ thống. Vui lòng chọn tên khác."
      isValid = false
    }
  }

  // =============== 2. VALIDATE MÔ TẢ ===============
  if (mTa === '') {
    errors.moTa = "Vui lòng nhập mô tả ngắn cho danh mục này"
    isValid = false
  } else if (mTa.length > 200) {
    errors.moTa = "Mô tả danh mục không được vượt quá 200 ký tự"
    isValid = false
  } else if (mTa !== form.moTa) {
    errors.moTa = "Mô tả không được chứa khoảng trắng dư thừa ở đầu hoặc cuối"
    isValid = false
  } else if (/\s{2,}/.test(mTa)) {
    errors.moTa = "Mô tả không được chứa nhiều khoảng trắng liên tiếp"
    isValid = false
  }

  return isValid
}

const gui = () => {
  if (!validateForm()) return

  emit('submit', {
    tenDanhMuc: form.tenDanhMuc.trim(),
    moTa: form.moTa.trim(),
  } as DanhMucRequest)
}

// Xuất các hàm điều khiển ra ngoài cho View cha gọi thông qua ref
defineExpose({
  fillForm(item?: any) {
    clearErrors() // Dọn sạch lỗi cũ khi chuyển đổi trạng thái
    
    if (!item) {
      isEditMode.value = false
      idDanhMucHienTai.value = null
      form.tenDanhMuc = ''
      form.moTa = ''
      return
    }

    // Thiết lập dữ liệu và bật chế độ Cập nhật (Sửa dòng)
    isEditMode.value = true
    idDanhMucHienTai.value = item.idDanhMuc
    form.tenDanhMuc = item.tenDanhMuc || ''
    form.moTa = item.moTa || ''
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Thông tin Danh Mục</h2>
      <p>{{ isEditMode ? 'Cập nhật danh mục thực đơn' : 'Thêm danh mục món ăn mới' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Tên danh mục <span class="bat-buoc">*</span></label>
        <input 
          v-model="form.tenDanhMuc" 
          type="text" 
          placeholder="Ví dụ: Lẩu đặc sắc, Món nướng..." 
          :class="{ 'is-invalid': errors.tenDanhMuc }"
          @input="errors.tenDanhMuc = ''"
        />
        <span class="error-text" v-if="errors.tenDanhMuc">{{ errors.tenDanhMuc }}</span>
      </div>

      <div class="form-group">
        <label>Mô tả danh mục <span class="bat-buoc">*</span></label>
        <textarea 
          v-model="form.moTa" 
          rows="4"
          placeholder="Nhập mô tả tóm tắt về nhóm danh mục này..." 
          :class="{ 'is-invalid': errors.moTa }"
          @input="errors.moTa = ''"
        ></textarea>
        <span class="error-text" v-if="errors.moTa">{{ errors.moTa }}</span>
      </div>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="gui">
        {{ isEditMode ? 'Cập nhật danh mục' : 'Lưu danh mục mới' }}
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

.tieu-de-panel h2 {
  color: #f8d46a;
  margin-bottom: 10px;
}

.tieu-de-panel p {
  color: #c7c7c7;
  font-size: 14px;
}

.luoi-bieu-mau {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  color: #d8d8d8;
  font-size: 14px;
  margin-bottom: 2px;
}

.bat-buoc {
  color: #ff6b6b;
}

input,
textarea {
  margin-top: 6px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, .08);
  background: rgba(255, 255, 255, .04);
  color: white;
  outline: none;
  box-sizing: border-box;
  width: 100%;
  font-family: inherit;
  font-size: 14px;
  resize: none;
}

input:focus,
textarea:focus {
  border-color: #f8d46a;
}

.nhom-nut {
  margin-top: 22px;
}

.nut-chinh {
  width: 100%;
  padding: 14px;
  border-radius: 16px;
  background: #f8d46a;
  color: #1a1410;
  border: none;
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