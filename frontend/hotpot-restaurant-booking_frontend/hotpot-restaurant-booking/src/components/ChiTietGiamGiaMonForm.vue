<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'

import type { ChiTietGiamGiaMon } from '../api/ChiTietGiamGiaMonApi'
import type { Mon } from '../api/MonApi'
import type { DotGiamGia } from '../api/DotGiamGiaApi'

import MonApi from '../api/MonApi'
import DotGiamGiaApi from '../api/DotGiamGiaApi'

import ComboApi, { type Combo } from '../api/ComBoApi' 
import DanhMucApi, { type DanhMuc } from '../api/DanhMucApi'
// Prop nhận danh sách hiện tại từ view cha
const props = defineProps<{
  danhSach?: ChiTietGiamGiaMon[]
}>()

const emit = defineEmits(['submit'])

// Danh sách dữ liệu từ API & giả lập
const danhSachMon = ref<Mon[]>([])
const danhSachCombo = ref<Combo[]>([])
const danhSachDanhMuc = ref<DanhMuc[]>([])
const danhSachDGG = ref<DotGiamGia[]>([])


// --- TRẠNG THÁI MODAL CHỌN MÓN/COMBO/DANH MỤC ---
const showModal = ref(false)
const activeTab = ref<'mon' | 'combo' | 'danhmuc'>('mon')
const tuKhoaTimKiem = ref('')

// Mảng lưu ID được chọn
const selectedMonIds = ref<number[]>([])
const selectedComboIds = ref<number[]>([])
const selectedDanhMucIds = ref<number[]>([])

// Biến cờ nhận biết trạng thái Form
const isEditMode = ref(false)
const idChiTietGiamGiaMonHienTai = ref<number | null>(null)

const form = reactive({
  mucGiam: '' as number | string,
  idDotGiamGia: '' as number | '',
  loaiGiam: 'PHANTRAM' as string, // Mặc định là PHANTRAM
  trangThai: 0,
})

const errors = reactive({
  mucGiam: '',
  idMon: '',
  idDotGiamGia: '',
  loaiGiam: '',
  trangThai: '',
})

const clearErrors = () => {
  errors.mucGiam = ''
  errors.idMon = ''
  errors.idDotGiamGia = ''
  errors.loaiGiam = ''
  errors.trangThai = ''
}

// BỔ SUNG LOGIC: Khi chọn/bỏ chọn Danh mục -> Tự động tích chọn toàn bộ món thuộc danh mục đó & Ép loại giảm là PHANTRAM
watch(
  selectedDanhMucIds,
  (newDanhMucIds) => {
    if (newDanhMucIds.length > 0) {
      // Ép loại giảm về PHANTRAM
      form.loaiGiam = 'PHANTRAM'
      errors.loaiGiam = ''

      // Tự động tìm tất cả món thuộc các Danh mục đang chọn và add vào selectedMonIds
      if (danhSachMon.value.length > 0) {
        const cacMonThuocDanhMuc = danhSachMon.value
          .filter((mon: any) => newDanhMucIds.includes(mon.idDanhMuc ?? mon.danhMuc?.idDanhMuc ?? mon.danhmuc?.idDanhMuc))
          .map(m => m.idMon)

        // Merge không trùng lặp
        selectedMonIds.value = Array.from(new Set([...selectedMonIds.value, ...cacMonThuocDanhMuc]))
      }
    }
  },
  { deep: true }
)

onMounted(async () => {
  try {
    const [monRes, dggRes, comboRes, danhMucRes] = await Promise.allSettled([
      MonApi.hienThiMon(), 
      DotGiamGiaApi.getDanhSach(),
      ComboApi.hienThiComBo(),
      DanhMucApi.getDanhSach()
    ])
    
    if (monRes.status === 'fulfilled') {
      danhSachMon.value = Array.isArray(monRes.value.data)
        ? monRes.value.data
        : (monRes.value.data as any).content || []
    }

    if (dggRes.status === 'fulfilled') {
      danhSachDGG.value = Array.isArray(dggRes.value.data)
        ? dggRes.value.data
        : (dggRes.value.data as any).content || []
    }

    if (comboRes.status === 'fulfilled') {
      danhSachCombo.value = Array.isArray(comboRes.value.data)
        ? comboRes.value.data
        : (comboRes.value.data as any).content || []
    }

    if (danhMucRes.status === 'fulfilled') {
      danhSachDanhMuc.value = Array.isArray(danhMucRes.value.data)
        ? danhMucRes.value.data
        : (danhMucRes.value.data as any).content || []
    }
  } catch (error) {
    console.error('Lỗi khi tải cấu hình danh mục lựa chọn:', error)
  }
})

// Lọc dữ liệu theo từ khóa tìm kiếm
const monFiltered = computed(() =>
  danhSachMon.value.filter(m => m.tenMon?.toLowerCase().includes(tuKhoaTimKiem.value.toLowerCase()))
)
const comboFiltered = computed(() =>
  danhSachCombo.value.filter(c => c.tenCombo?.toLowerCase().includes(tuKhoaTimKiem.value.toLowerCase()))
)
const danhmucFiltered = computed(() =>
  danhSachDanhMuc.value.filter(d => d.loaiDanhMuc?.toLowerCase().includes(tuKhoaTimKiem.value.toLowerCase()))
)

// Tính tổng số mục đã chọn ở cả 3 tab
const tongMucDaChon = computed(() => {
  return selectedMonIds.value.length + selectedComboIds.value.length + selectedDanhMucIds.value.length
})

// Cờ kiểm tra xem người dùng có đang chọn theo Danh Mục hay không
const isSelectedDanhMuc = computed(() => selectedDanhMucIds.value.length > 0)

// Kiểm tra trạng thái "Chọn tất cả" của tab hiện tại
const isAllChecked = computed(() => {
  if (activeTab.value === 'mon') {
    return monFiltered.value.length > 0 && monFiltered.value.every(m => selectedMonIds.value.includes(m.idMon))
  } else if (activeTab.value === 'combo') {
    return comboFiltered.value.length > 0 && comboFiltered.value.every(c => selectedComboIds.value.includes(c.idCombo))
  } else if (activeTab.value === 'danhmuc') {
    return danhmucFiltered.value.length > 0 && danhmucFiltered.value.every(d => selectedDanhMucIds.value.includes(d.idDanhMuc))
  }
  return false
})

const toggleSelectAll = (e: Event) => {
  const checked = (e.target as HTMLInputElement).checked
  if (activeTab.value === 'mon') {
    if (checked) {
      const ids = monFiltered.value.map(m => m.idMon)
      selectedMonIds.value = Array.from(new Set([...selectedMonIds.value, ...ids]))
    } else {
      const idsToRemove = new Set(monFiltered.value.map(m => m.idMon))
      selectedMonIds.value = selectedMonIds.value.filter(id => !idsToRemove.has(id))
    }
  } else if (activeTab.value === 'combo') {
    if (checked) {
      const ids = comboFiltered.value.map(c => c.idCombo)
      selectedComboIds.value = Array.from(new Set([...selectedComboIds.value, ...ids]))
    } else {
      const idsToRemove = new Set(comboFiltered.value.map(c => c.idCombo))
      selectedComboIds.value = selectedComboIds.value.filter(id => !idsToRemove.has(id))
    }
  } else if (activeTab.value === 'danhmuc') {
    if (checked) {
      const ids = danhmucFiltered.value.map(d => d.idDanhMuc)
      selectedDanhMucIds.value = Array.from(new Set([...selectedDanhMucIds.value, ...ids]))
    } else {
      const idsToRemove = new Set(danhmucFiltered.value.map(d => d.idDanhMuc))
      selectedDanhMucIds.value = selectedDanhMucIds.value.filter(id => !idsToRemove.has(id))
    }
  }
}

// Validation
const validateForm = () => {
  clearErrors()
  let isValid = true

  if (!form.idDotGiamGia) {
    errors.idDotGiamGia = 'Vui lòng chọn chương trình giảm giá áp dụng'
    isValid = false
  }

  // 🔥 FIX LỖI: Kiểm tra có chọn ít nhất 1 mục trong 3 tab hay chưa
  if (tongMucDaChon.value === 0) {
    errors.idMon = 'Vui lòng chọn ít nhất 1 món, combo hoặc danh mục'
    isValid = false
  }

  if (!form.loaiGiam) {
    errors.loaiGiam = 'Vui lòng chọn loại giảm'
    isValid = false
  }

  if (isSelectedDanhMuc.value && form.loaiGiam !== 'PHANTRAM') {
    errors.loaiGiam = 'Khi chọn giảm giá theo Danh mục, loại giảm bắt buộc phải là Phần trăm'
    isValid = false
  }

  const chuoiMucGiam = form.mucGiam !== null && form.mucGiam !== undefined ? form.mucGiam.toString().trim() : ''
  if (chuoiMucGiam === '') {
    errors.mucGiam = form.loaiGiam === 'TIEN' ? 'Số tiền giảm không được để trống' : 'Mức giảm phần trăm không được để trống'
    isValid = false
  } else {
    const giaTriMucGiam = Number(form.mucGiam)
    if (isNaN(giaTriMucGiam) || giaTriMucGiam <= 0) {
      errors.mucGiam = form.loaiGiam === 'TIEN' ? 'Số tiền giảm phải lớn hơn 0' : 'Mức giảm phần trăm phải lớn hơn 0'
      isValid = false
    } else if (form.loaiGiam === 'PHANTRAM' && giaTriMucGiam > 100) {
      errors.mucGiam = 'Mức giảm tối đa không được vượt quá 100%'
      isValid = false
    }
  }

  return isValid
}

const gui = () => {
  if (!validateForm()) return

  // 🔥 FIX LỖI SUBMIT: Đóng gói đẩy đủ các alias key truyền lên View/Backend
  const payload = {
    idChiTietGiamGiaMon: idChiTietGiamGiaMonHienTai.value,
    idDotGiamGia: Number(form.idDotGiamGia),
    loaiGiam: form.loaiGiam,
    mucGiam: Number(form.mucGiam),
    trangThai: form.trangThai,

    // Mảng danh sách
    danhSachMonId: selectedMonIds.value,
    danhSachComboId: selectedComboIds.value,
    danhSachDanhMucId: selectedDanhMucIds.value,

    // Tương thích ngược với Backend mong đợi đơn lẻ
    idMon: selectedMonIds.value.length > 0 ? selectedMonIds.value[0] : null,
    idCombo: selectedComboIds.value.length > 0 ? selectedComboIds.value[0] : null,
    idDanhMuc: selectedDanhMucIds.value.length > 0 ? selectedDanhMucIds.value[0] : null
  }

  emit('submit', payload)
}

defineExpose({
  fillForm(item?: any) {
    clearErrors()
    
    // Trường hợp Reset mới hoàn toàn
    if (!item) {
      isEditMode.value = false
      idChiTietGiamGiaMonHienTai.value = null
      form.mucGiam = ''
      form.idDotGiamGia = ''
      form.loaiGiam = 'PHANTRAM'
      form.trangThai = 0
      selectedMonIds.value = []
      selectedComboIds.value = []
      selectedDanhMucIds.value = []
      return
    }

    const checkIdDgg = item.idDotGiamGia ?? item.dotGiamGia?.idDotGiamGia ?? null
    const checkIdChiTiet = item.idChiTietGiamGiaMon ?? null
    const checkMucGiam = item.mucGiam ?? null

    // Trường hợp chuyển từ trang Đợt giảm giá sang (chỉ có idDotGiamGia)
    if (checkIdDgg !== null && checkIdChiTiet === null && checkMucGiam === null) {
      isEditMode.value = false
      idChiTietGiamGiaMonHienTai.value = null
      form.mucGiam = ''
      form.idDotGiamGia = Number(checkIdDgg)
      form.loaiGiam = 'PHANTRAM'
      selectedMonIds.value = []
      selectedComboIds.value = []
      selectedDanhMucIds.value = []
      return
    }

    // 🔥 FIX LỖI SỬA: Đọc đầy đủ các danh sách món/combo/danh mục khi sửa
    isEditMode.value = true
    idChiTietGiamGiaMonHienTai.value = item.idChiTietGiamGiaMon
    form.mucGiam = item.mucGiam !== undefined && item.mucGiam !== null ? item.mucGiam.toString() : ''
    form.idDotGiamGia = checkIdDgg ? Number(checkIdDgg) : ''
    form.loaiGiam = item.loaiGiam ?? 'PHANTRAM'
    form.trangThai = item.trangThai ?? 0

    // Gán lại Món
    if (Array.isArray(item.danhSachMon) && item.danhSachMon.length > 0) {
      selectedMonIds.value = item.danhSachMon.map((m: any) => m.idMon)
    } else if (item.idMon || item.mon?.idMon) {
      selectedMonIds.value = [item.idMon ?? item.mon?.idMon]
    } else {
      selectedMonIds.value = []
    }

    // Gán lại Combo
    if (Array.isArray(item.danhSachCombo) && item.danhSachCombo.length > 0) {
      selectedComboIds.value = item.danhSachCombo.map((c: any) => c.idCombo)
    } else if (item.idCombo || item.combo?.idCombo) {
      selectedComboIds.value = [item.idCombo ?? item.combo?.idCombo]
    } else {
      selectedComboIds.value = []
    }

    // Gán lại Danh mục
    if (Array.isArray(item.danhSachDanhMuc) && item.danhSachDanhMuc.length > 0) {
      selectedDanhMucIds.value = item.danhSachDanhMuc.map((d: any) => d.idDanhMuc)
    } else if (item.idDanhMuc || item.danhMuc?.idDanhMuc) {
      selectedDanhMucIds.value = [item.idDanhMuc ?? item.danhMuc?.idDanhMuc]
    } else {
      selectedDanhMucIds.value = []
    }
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Chi tiết giảm giá món</h2>
      <p>
        {{ isEditMode ? 'Cập nhật cấu hình món giảm giá' : 'Áp dụng món ăn vào chương trình giảm giá' }}
      </p>
    </div>

    <div class="luoi-bieu-mau">
      <!-- Select Chương trình -->
      <div class="form-group">
        <label>Chương trình giảm giá</label>
        <select
          v-model="form.idDotGiamGia"
          :class="{ 'is-invalid': errors.idDotGiamGia }"
          @change="errors.idDotGiamGia = ''"
        >
          <option value="">-- Chọn chương trình --</option>
          <option v-for="d in danhSachDGG" :key="d.idDotGiamGia" :value="d.idDotGiamGia">
            {{ d.tenChuongTrinh }}
          </option>
        </select>
        <span class="error-text" v-if="errors.idDotGiamGia">{{ errors.idDotGiamGia }}</span>
      </div>

      <!-- Nút bật Modal Chọn món -->
      <div class="form-group">
        <label>Áp dụng cho</label>
        <button type="button" class="btn-chon-mon" :class="{ 'is-invalid': errors.idMon }" @click="showModal = true; errors.idMon = ''">
          <span v-if="tongMucDaChon === 0">-- Bấm để chọn Món / Combo / Danh mục --</span>
          <span v-else class="da-chon-text">
            Đã chọn: {{ tongMucDaChon }} mục
            <span v-if="isSelectedDanhMuc" style="font-size: 12px; color: #f8d46a;"> (Áp dụng toàn bộ món trong danh mục)</span>
          </span>
        </button>
        <span class="error-text" v-if="errors.idMon">{{ errors.idMon }}</span>
      </div>

      <!-- Select Loại Giảm (Tự động khóa nếu có chọn Danh Mục) -->
      <div class="form-group">
        <label>
          Loại Giảm
          <span v-if="isSelectedDanhMuc" class="disabled-hint">(Cố định % khi chọn Danh mục)</span>
        </label>
        <select 
          v-model="form.loaiGiam" 
          :disabled="isSelectedDanhMuc"
          :class="{ 'is-invalid': errors.loaiGiam, 'input-disabled': isSelectedDanhMuc }"
          @change="errors.loaiGiam = ''"
        >
          <option value="">-- Chọn loại giảm --</option>
          <option value="PHANTRAM">Phần trăm (%)</option>
          <option value="TIEN">Tiền mặt (VNĐ)</option>
        </select>
        <span class="error-text" v-if="errors.loaiGiam">{{ errors.loaiGiam }}</span>
      </div>

      <!-- Input Mức giảm -->
      <div class="form-group">
        <label>{{ form.loaiGiam === 'TIEN' ? 'Số tiền giảm (VNĐ)' : 'Mức giảm (%)' }}</label>
        <input
          v-model="form.mucGiam"
          type="number"
          min="0"
          :max="form.loaiGiam === 'TIEN' ? undefined : 100"
          :placeholder="form.loaiGiam === 'TIEN' ? 'Nhập số tiền giảm...' : 'Nhập phần trăm giảm... (0-100)'"
          :class="{ 'is-invalid': errors.mucGiam }"
          @input="errors.mucGiam = ''"
        />
        <span class="help-text" v-if="form.loaiGiam === 'PHANTRAM'">Giá trị từ 0% đến 100%</span>
        <span class="error-text" v-if="errors.mucGiam">{{ errors.mucGiam }}</span>
      </div>

      <!-- Select Trạng thái -->
      <div class="form-group">
        <label>Trạng thái</label>
        <select v-model.number="form.trangThai">
          <option :value="0">Còn hiệu Lực</option>
          <option :value="1">Hết Hiệu Lực</option>
        </select>
      </div>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="gui">Lưu thông tin</button>
    </div>

    <!-- ==================== POPUP CHECKBOX MODAL CỐ ĐỊNH ==================== -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Chọn áp dụng giảm giá</h3>
          <button class="btn-close" @click="showModal = false">&times;</button>
        </div>

        <!-- Thanh Tab -->
        <div class="tab-header">
          <button
            type="button"
            :class="['tab-btn', { active: activeTab === 'mon' }]"
            @click="activeTab = 'mon'"
          >
            Món ăn ({{ selectedMonIds.length }})
          </button>
          <button
            type="button"
            :class="['tab-btn', { active: activeTab === 'combo' }]"
            @click="activeTab = 'combo'"
          >
            Combo ({{ selectedComboIds.length }})
          </button>
          <button
            type="button"
            :class="['tab-btn', { active: activeTab === 'danhmuc' }]"
            @click="activeTab = 'danhmuc'"
          >
            Danh mục ({{ selectedDanhMucIds.length }})
          </button>
        </div>

        <!-- Ô Tìm kiếm & Chọn tất cả -->
        <div class="modal-filter">
          <input
            v-model="tuKhoaTimKiem"
            type="text"
            placeholder="Tìm kiếm nhanh..."
            class="input-tim-kiem"
          />
          <label class="chon-tat-ca">
            <input type="checkbox" :checked="isAllChecked" @change="toggleSelectAll" />
            Chọn tất cả
          </label>
        </div>

        <!-- Danh sách Checkbox cuộn cố định -->
        <div class="modal-body">
          <!-- TAB 1: MÓN ĂN -->
          <div v-if="activeTab === 'mon'" class="check-list">
            <label v-for="m in monFiltered" :key="m.idMon" class="check-item">
              <input type="checkbox" :value="m.idMon" v-model="selectedMonIds" @change="errors.idMon = ''" />
              <span>{{ m.tenMon }}</span>
            </label>
            <p v-if="monFiltered.length === 0" class="no-data">Không tìm thấy món ăn nào</p>
          </div>

          <!-- TAB 2: COMBO -->
          <div v-if="activeTab === 'combo'" class="check-list">
            <label v-for="c in comboFiltered" :key="c.idCombo" class="check-item">
              <input type="checkbox" :value="c.idCombo" v-model="selectedComboIds" @change="errors.idMon = ''" />
              <span>{{ c.tenCombo }}</span>
            </label>
            <p v-if="comboFiltered.length === 0" class="no-data">Không tìm thấy combo nào</p>
          </div>

          <!-- TAB 3: DANH MỤC -->
          <div v-if="activeTab === 'danhmuc'" class="check-list">
            <label v-for="d in danhmucFiltered" :key="d.idDanhMuc" class="check-item">
              <input type="checkbox" :value="d.idDanhMuc" v-model="selectedDanhMucIds" @change="errors.idMon = ''" />
              <span>{{ d.loaiDanhMuc }}</span>
            </label>
            <p v-if="danhmucFiltered.length === 0" class="no-data">Không tìm thấy danh mục nào</p>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="nut-xac-nhan" @click="showModal = false">
            Xác nhận ({{ tongMucDaChon }} mục)
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.bieu-mau-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 28px;
  padding: 26px;
}

.tieu-de-panel h2 {
  color: #8b5e34;
  margin-bottom: 10px;
}

.tieu-de-panel p {
  color: #8f6b46;
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
  color: #5f3d22;
  margin-bottom: 6px;
}

.disabled-hint {
  font-size: 11px;
  color: #8b5e34;
  margin-left: 6px;
}

input,
select,
.btn-chon-mon {
  margin-top: 6px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid #e6d2aa;
  background: #fffdf8;
  color: #5f3d22;
  outline: none;
  box-sizing: border-box;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.input-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #efe0c1 !important;
}

.da-chon-text {
  color: #8b5e34;
  font-weight: 600;
}

input:focus,
select:focus {
  border-color: #d8a85c;
}

.help-text {
  color: #8f6b46;
  font-size: 12px;
  margin-top: 4px;
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
  background: #fffdf8;
  color: #5f3d22;
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

/* ==================== STYLES MODAL POPUP CỐ ĐỊNH ==================== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(61, 40, 20, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: rgba(255, 248, 234, 0.98);
  border: 1px solid #e6d2aa;
  border-radius: 20px;
  width: 520px;
  height: 580px;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 10px 30px rgba(95, 61, 34, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.modal-header h3 {
  color: #8b5e34;
  margin: 0;
  font-size: 18px;
}

.btn-close {
  background: none;
  border: none;
  color: #8f6b46;
  font-size: 24px;
  cursor: pointer;
}

.tab-header {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #efe0c1;
  padding-bottom: 10px;
  margin-bottom: 14px;
  flex-shrink: 0;
}

.tab-btn {
  flex: 1;
  padding: 10px;
  border-radius: 10px;
  background: #fffdf8;
  border: 1px solid #efe0c1;
  color: #8f6b46;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  font-size: 13px;
}

.tab-btn.active {
  background: #fff3d3;
  color: #8b5e34;
  border-color: #d8a85c;
  font-weight: 600;
}

.modal-filter {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.input-tim-kiem {
  flex: 1;
  margin: 0;
  padding: 8px 12px;
  font-size: 14px;
  border-radius: 10px;
}

.chon-tat-ca {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
  margin: 0;
  white-space: nowrap;
  color: #5f3d22;
}

/* KHU VỰC CUỘN DỮ LIỆU CỐ ĐỊNH */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 6px;
  min-height: 0;
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fffdf8;
  border: 1px solid #efe0c1;
  color: #5f3d22;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.check-item:hover {
  background: #fff8eb;
}

.check-item input[type='checkbox'] {
  width: 18px;
  height: 18px;
  margin: 0;
  cursor: pointer;
  accent-color: #d8a85c;
}

.no-data {
  text-align: center;
  color: #8f6b46;
  padding: 20px 0;
}

.modal-footer {
  margin-top: 16px;
  flex-shrink: 0;
}

.nut-xac-nhan {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  background: #d8a85c;
  color: #3d2814;
  border: none;
  font-weight: bold;
  cursor: pointer;
}
</style>