<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'

import type { ChiTietComBo, ChiTietComBoRequest } from '../api/ChiTietComBoApi'
import type { Mon } from '../api/MonApi'
import type { Combo } from '../api/ComBoApi'

import MonApi from '../api/MonApi'
import ComboApi from '../api/ComBoApi'

// Nhận danh sách chi tiết combo hiện tại từ view cha để validate chống trùng món trong cùng 1 combo
const props = defineProps<{
  danhSach: ChiTietComBo[]
}>()

const emit = defineEmits(['submit'])

const danhSachMon = ref<Mon[]>([])
const danhSachCombo = ref<Combo[]>([])

// Biến cờ nhận biết trạng thái Form
const isEditMode = ref(false)
const idChiTietComboHienTai = ref<number | null>(null)

const form = reactive({
  soLuong: 1 as number | string, // Cho phép nhận chuỗi rỗng tạm thời khi xóa ô nhập liệu trên giao diện
  idMon: '' as number | '',
  idCombo: '' as number | '',
  moTa: '',
})

// Trạng thái lưu trữ thông báo lỗi validate hiển thị lên giao diện
const errors = reactive({
  soLuong: '',
  idMon: '',
  idCombo: '',
  moTa: '',
})

// Xóa sạch thông báo lỗi cũ khi người dùng thao tác lại
const clearErrors = () => {
  errors.soLuong = ''
  errors.idMon = ''
  errors.idCombo = ''
  errors.moTa = ''
}

onMounted(async () => {
  try {
    const [monRes, comboRes] = await Promise.all([
      MonApi.hienThiMon(),
      ComboApi.hienThiComBo(),
    ])
    danhSachMon.value = Array.isArray(monRes.data) ? monRes.data : (monRes.data as any).content || []
    danhSachCombo.value = Array.isArray(comboRes.data) ? comboRes.data : (comboRes.data as any).content || []
  } catch (error) {
    console.error("Lỗi khi tải dữ liệu cấu hình:", error)
  }
})

// Hàm xử lý Validate nâng cao
const validateForm = () => {
  clearErrors()
  let isValid = true

  // 1. Validate Combo
  if (!form.idCombo) {
    errors.idCombo = "Vui lòng chọn gói Combo"
    isValid = false
  }

  // 2. Validate Món Ăn
  if (!form.idMon) {
    errors.idMon = "Vui lòng chọn món ăn muốn đưa vào Combo"
    isValid = false
  }

  // 3. Kiểm tra trùng lặp: Nếu đã chọn cả Combo và Món, tiến hành quét xem món này đã tồn tại trong Combo chưa
  if (form.idCombo && form.idMon) {
    const biTrungCap = props.danhSach.some(item => {
      const itemComboId = (item as any).idCombo ?? (item as any).combo?.idCombo;
      const itemMonId = (item as any).idMon ?? (item as any).mon?.idMon;

      if (isEditMode.value && item.idChiTietCombo === idChiTietComboHienTai.value) {
        return false
      }

      return Number(itemComboId) === Number(form.idCombo) && Number(itemMonId) === Number(form.idMon)
    })

    if (biTrungCap) {
      errors.idMon = "Món ăn này đã có sẵn trong Combo này rồi. Vui lòng chọn món khác hoặc nhấn nút 'Sửa' dòng cũ để tăng số lượng."
      isValid = false
    }
  }

  // 4. Validate Số Lượng (SỬA ĐỂ TRÁNH LỖI TS2367: Chuyển đổi toString để kiểm tra chuỗi rỗng an toàn)
  const chuoiSoLuong = form.soLuong !== null && form.soLuong !== undefined ? form.soLuong.toString().trim() : '';
  if (chuoiSoLuong === '') {
    errors.soLuong = "Số lượng không được để trống"
    isValid = false
  } else if (isNaN(Number(form.soLuong)) || Number(form.soLuong) <= 0) {
    errors.soLuong = "Số lượng món ăn cấu thành phải lớn hơn 0"
    isValid = false
  }

  // 5. Validate Mô Tả
  const mTa = form.moTa || ''
  if (mTa !== mTa.trim()) {
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
    soLuong: Number(form.soLuong),
    idMon: form.idMon as number,
    idCombo: form.idCombo as number,
    moTa: form.moTa.trim(),
  } as ChiTietComBoRequest)
}

defineExpose({
  fillForm(item?: any) {
    clearErrors() 
    
    if (!item) {
      isEditMode.value = false
      idChiTietComboHienTai.value = null
      form.soLuong = 1
      form.idMon = ''
      form.idCombo = ''
      form.moTa = ''
      return
    }

    // Xử lý an toàn bằng cách bóc tách giá trị cụ thể, tránh dùng toán tử so sánh trực tiếp đối tượng lồng với undefined
    const checkIdCombo = item.idCombo ?? null;
    const checkIdChiTiet = item.idChiTietCombo ?? null;
    const checkSoLuong = item.soLuong ?? null;

    if (checkIdCombo !== null && checkIdChiTiet === null && checkSoLuong === null) {
      isEditMode.value = false
      idChiTietComboHienTai.value = null
      form.soLuong = 1
      form.idMon = ''
      form.idCombo = Number(checkIdCombo)
      form.moTa = ''
      return
    }

    // Thiết lập dữ liệu và bật chế độ Cập nhật (Sửa dòng)
    isEditMode.value = true
    idChiTietComboHienTai.value = item.idChiTietCombo

    form.soLuong = Number(item.soLuong)
    form.moTa = item.moTa || ''

    // Cơ chế bóc tách dữ liệu phẳng hoặc thực thể lồng của JPA Hibernate trả về khi nhấn nút Sửa
    form.idMon = item.idMon ?? item.mon?.idMon ?? ''
    form.idCombo = item.idCombo ?? item.combo?.idCombo ?? ''
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Thông tin Chi Tiết Combo</h2>
      <p>{{ isEditMode ? 'Cập nhật thành phần trong Combo' : 'Thêm mới món ăn cấu thành Combo' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Combo gói</label>
        <select v-model="form.idCombo" :class="{ 'is-invalid': errors.idCombo }" @change="errors.idCombo = ''">
          <option value="">-- Chọn combo --</option>
          <option
            v-for="c in danhSachCombo"
            :key="c.idCombo"
            :value="c.idCombo"
          >
            {{ c.tenCombo }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idCombo">{{ errors.idCombo }}</span>
      </div>

      <div class="form-group">
        <label>Món ăn áp dụng</label>
        <select v-model="form.idMon" :class="{ 'is-invalid': errors.idMon }" @change="errors.idMon = ''">
          <option value="">-- Chọn món --</option>
          <option
            v-for="m in danhSachMon"
            :key="m.idMon"
            :value="m.idMon"
          >
            {{ m.tenMon }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idMon">{{ errors.idMon }}</span>
      </div>

      <div class="form-group">
        <label>Số lượng thành phần</label>
        <input 
          v-model.number="form.soLuong" 
          type="number" 
          min="1" 
          placeholder="Nhập số lượng..." 
          :class="{ 'is-invalid': errors.soLuong }"
          @input="errors.soLuong = ''"
        />
        <span class="error-text" v-if="errors.soLuong">{{ errors.soLuong }}</span>
      </div>

      <div class="form-group">
        <label>Mô tả ghi chú</label>
        <input 
          v-model="form.moTa" 
          type="text" 
          placeholder="Nhập mô tả không bắt buộc..." 
          :class="{ 'is-invalid': errors.moTa }"
          @input="errors.moTa = ''"
        />
        <span class="error-text" v-if="errors.moTa">{{ errors.moTa }}</span>
      </div>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="gui">
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
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,.08);
  background: rgba(255,255,255,.04);
  color: white;
  outline: none;
  box-sizing: border-box;
  width: 100%;
}

input:focus,
select:focus {
  border-color: #f8d46a;
}

.nhom-nut {
  margin-top: 18px;
}

.nut-chinh {
  width: 100%;
  padding: 12px;
  border-radius: 16px;
  background: #f8d46a;
  color: #1a1410;
  border: none;
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