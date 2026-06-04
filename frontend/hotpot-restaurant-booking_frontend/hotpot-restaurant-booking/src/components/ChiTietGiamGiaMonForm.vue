<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'

import type {
  ChiTietGiamGiaMon,
  ChiTietGiamGiaMonRequest,
} from '../api/ChiTietGiamGiaMonApi'

import type { Mon } from '../api/MonApi'
import type { DotGiamGia } from '../api/DotGiamGiaApi'

import MonApi from '../api/MonApi'
import DotGiamGiaApi from '../api/DotGiamGiaApi'

// CẬP NHẬT: Nhận danh sách chi tiết giảm giá hiện tại từ view cha để validate chống trùng món trong cùng 1 chương trình
const props = defineProps<{
  danhSach: ChiTietGiamGiaMon[]
}>()

const emit = defineEmits(['submit'])

const danhSachMon = ref<Mon[]>([])
const danhSachDGG = ref<DotGiamGia[]>([])

// Biến cờ nhận biết trạng thái Form
const isEditMode = ref(false)
const idChiTietGiamGiaMonHienTai = ref<number | null>(null)

const form = reactive({
  mucGiam: '' as number | string, // Cho phép nhận chuỗi rỗng tạm thời khi xóa ô nhập liệu trên giao diện
  idMon: '' as number | '',
  idDotGiamGia: '' as number | '',
})

// Trạng thái lưu trữ thông báo lỗi validate hiển thị lên giao diện
const errors = reactive({
  mucGiam: '',
  idMon: '',
  idDotGiamGia: '',
})

// Xóa sạch thông báo lỗi cũ khi người dùng thao tác lại
const clearErrors = () => {
  errors.mucGiam = ''
  errors.idMon = ''
  errors.idDotGiamGia = ''
}

onMounted(async () => {
  try {
    const [monRes, dggRes] = await Promise.all([
      MonApi.hienThiMon(),
      DotGiamGiaApi.getDanhSach(),
    ])
    danhSachMon.value = Array.isArray(monRes.data) ? monRes.data : (monRes.data as any).content || []
    danhSachDGG.value = Array.isArray(dggRes.data) ? dggRes.data : (dggRes.data as any).content || []
  } catch (error) {
    console.error("Lỗi khi tải cấu hình danh mục lựa chọn:", error)
  }
})

// Hàm xử lý Validate nâng cao cho Chi Tiết Giảm Giá Món
const validateForm = () => {
  clearErrors()
  let isValid = true

  // 1. Validate Chương trình giảm giá
  if (!form.idDotGiamGia) {
    errors.idDotGiamGia = "Vui lòng chọn chương trình giảm giá áp dụng"
    isValid = false
  }

  // 2. Validate Món ăn
  if (!form.idMon) {
    errors.idMon = "Vui lòng chọn món ăn muốn áp dụng giảm giá"
    isValid = false
  }

  // 3. Kiểm tra trùng lặp: Nếu chọn đầy đủ cả Chương trình và Món, tiến hành quét xem món này đã được cấu hình giảm giá trong đợt này chưa
  if (form.idDotGiamGia && form.idMon && props.danhSach && props.danhSach.length > 0) {
    const biTrungCap = props.danhSach.some(item => {
      // Bóc tách ID phẳng hoặc lồng đối tượng thực thể JPA Hibernate trả về từ API
      const itemDggId = (item as any).idDotGiamGia ?? (item as any).dotGiamGia?.idDotGiamGia;
      const itemMonId = (item as any).idMon ?? (item as any).mon?.idMon;

      // Nếu đang ở chế độ Sửa dòng hiện tại, bỏ qua không đối chiếu trùng với chính bản thân nó
      if (isEditMode.value && item.idChiTietGiamGiaMon === idChiTietGiamGiaMonHienTai.value) {
        return false
      }

      return Number(itemDggId) === Number(form.idDotGiamGia) && Number(itemMonId) === Number(form.idMon)
    })

    if (biTrungCap) {
      errors.idMon = "Món ăn này đã được thêm vào đợt giảm giá này rồi. Vui lòng chọn món khác hoặc bấm nút 'Sửa' dòng cũ để cập nhật mức giảm."
      isValid = false
    }
  }

  // 4. Validate Mức giảm (%)
  const chuoiMucGiam = form.mucGiam !== null && form.mucGiam !== undefined ? form.mucGiam.toString().trim() : '';
  if (chuoiMucGiam === '') {
    errors.mucGiam = "Mức giảm phần trăm không được để trống"
    isValid = false
  } else {
    const giaTriMucGiam = Number(form.mucGiam)
    if (isNaN(giaTriMucGiam) || giaTriMucGiam <= 0) {
      errors.mucGiam = "Mức giảm giá phải lớn hơn 0%"
      isValid = false
    } else if (giaTriMucGiam > 100) {
      errors.mucGiam = "Mức giảm tối đa của một món ăn không được vượt quá 100%"
      isValid = false
    }
  }

  return isValid
}

const gui = () => {
  if (!validateForm()) return

  emit('submit', {
    mucGiam: Number(form.mucGiam),
    idMon: form.idMon as number,
    idDotGiamGia: form.idDotGiamGia as number,
  })
}

defineExpose({
  fillForm(item?: any) {
    clearErrors() // Dọn sạch lỗi cũ
    
    if (!item) {
      isEditMode.value = false
      idChiTietGiamGiaMonHienTai.value = null
      form.mucGiam = ''
      form.idMon = ''
      form.idDotGiamGia = ''
      return
    }

    // Trường hợp chuyển giao đặc biệt: Nhấp từ nút "Xem chi tiết" ở màn hình Đợt Giảm Giá sang (Chỉ truyền idDotGiamGia)
    const checkIdDgg = item.idDotGiamGia ?? null;
    const checkIdChiTiet = item.idChiTietGiamGiaMon ?? null;
    const checkMucGiam = item.mucGiam ?? null;

    if (checkIdDgg !== null && checkIdChiTiet === null && checkMucGiam === null) {
      isEditMode.value = false
      idChiTietGiamGiaMonHienTai.value = null
      form.mucGiam = ''
      form.idMon = ''
      form.idDotGiamGia = Number(checkIdDgg)
      return
    }

    // Thiết lập dữ liệu và bật chế độ Cập nhật (Sửa dòng)
    isEditMode.value = true
    idChiTietGiamGiaMonHienTai.value = item.idChiTietGiamGiaMon

    form.mucGiam = item.mucGiam !== undefined && item.mucGiam !== null ? item.mucGiam.toString() : ''
    form.idMon = item.idMon ?? item.mon?.idMon ?? ''
    form.idDotGiamGia = item.idDotGiamGia ?? item.dotGiamGia?.idDotGiamGia ?? ''
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Chi tiết giảm giá món</h2>
      <p>{{ isEditMode ? 'Cập nhật cấu hình món giảm giá' : 'Áp dụng món ăn vào chương trình giảm giá' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <div class="form-group">
        <label>Chương trình giảm giá</label>
        <select v-model="form.idDotGiamGia" :class="{ 'is-invalid': errors.idDotGiamGia }" @change="errors.idDotGiamGia = ''">
          <option value="">-- Chọn chương trình --</option>
          <option
            v-for="d in danhSachDGG"
            :key="d.idDotGiamGia"
            :value="d.idDotGiamGia"
          >
            {{ d.tenChuongTrinh }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idDotGiamGia">{{ errors.idDotGiamGia }}</span>
      </div>

      <div class="form-group">
        <label>Món ăn áp dụng</label>
        <select v-model="form.idMon" :class="{ 'is-invalid': errors.idMon }" @change="errors.idMon = ''">
          <option value="">-- Chọn món --</option>
          <option v-for="m in danhSachMon" :key="m.idMon" :value="m.idMon">
            {{ m.tenMon }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idMon">{{ errors.idMon }}</span>
      </div>

      <div class="form-group">
        <label>Mức giảm (%)</label>
        <input 
          v-model="form.mucGiam" 
          type="number" 
          min="1" 
          max="100"
          placeholder="Nhập phần trăm giảm..." 
          :class="{ 'is-invalid': errors.mucGiam }"
          @input="errors.mucGiam = ''"
        />
        <span class="error-text" v-if="errors.mucGiam">{{ errors.mucGiam }}</span>
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
  color: #fff;
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