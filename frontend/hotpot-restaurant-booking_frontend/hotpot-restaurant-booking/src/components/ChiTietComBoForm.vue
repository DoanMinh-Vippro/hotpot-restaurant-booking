<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'

import type { ChiTietComBo, ChiTietComBoRequest } from '../api/ChiTietComBoApi'
import type { Mon } from '../api/MonApi'
import type { Combo } from '../api/ComBoApi'

import MonApi from '../api/MonApi'
import ComboApi from '../api/ComBoApi'

const props = defineProps<{
  danhSach: ChiTietComBo[]
}>()

const emit = defineEmits(['submit'])

const danhSachMon = ref<Mon[]>([])
const danhSachCombo = ref<Combo[]>([])

// Trạng thái modal
const isShowMonModal = ref(false)
const tuKhoaTimMon = ref('')

// Lưu trữ ID để phân biệt Thêm/Sửa
const idChiTietComboHienTai = ref<number | null>(null)

const form = reactive({
  soLuong: 1 as number | string,
  danhSachIdMon: [] as number[], // Mảng chứa danh sách ID món ăn được chọn
  idCombo: '' as number | '',
  moTa: '',
})

const errors = reactive({
  soLuong: '',
  idMon: '',
  idCombo: '',
  moTa: '',
})

// Hiển thị tên món ăn đã chọn lên giao diện nút bấm
const nhanHienThiMon = computed(() => {
  if (form.danhSachIdMon.length === 0) return ''
  
  if (form.danhSachIdMon.length === 1) {
    const mon = danhSachMon.value.find(m => Number(m.idMon) === Number(form.danhSachIdMon[0]))
    return mon ? mon.tenMon : '1 món đã chọn'
  }

  const firstMon = danhSachMon.value.find(m => Number(m.idMon) === Number(form.danhSachIdMon[0]))
  const firstName = firstMon ? firstMon.tenMon : 'Món'
  return `${firstName} (+${form.danhSachIdMon.length - 1} món khác)`
})

// Lọc danh sách món ăn theo từ khóa tìm kiếm
const danhSachMonDaLoc = computed(() => {
  if (!tuKhoaTimMon.value.trim()) return danhSachMon.value
  return danhSachMon.value.filter(m => 
    m.tenMon.toLowerCase().includes(tuKhoaTimMon.value.toLowerCase().trim())
  )
})

// Reset lỗi
const clearErrors = () => {
  errors.soLuong = ''
  errors.idMon = ''
  errors.idCombo = ''
  errors.moTa = ''
}

// Bật/Tắt modal
const openMonModal = () => {
  isShowMonModal.value = true
}

const closeMonModal = () => {
  isShowMonModal.value = false
}

// Lắng nghe sự thay đổi của idCombo để xóa lỗi khi người dùng chọn lại
watch(() => form.idCombo, () => {
  if (form.idCombo) errors.idCombo = ''
})

onMounted(async () => {
  try {
    const [monRes, comboRes] = await Promise.all([
      MonApi.hienThiMon(),
      ComboApi.hienThiComBo(),
    ])
    danhSachMon.value = Array.isArray(monRes.data) ? monRes.data : (monRes.data as any).content || []
    danhSachCombo.value = Array.isArray(comboRes.data) ? comboRes.data : (comboRes.data as any).content || []
  } catch (error) {
    console.error("Lỗi khi tải dữ liệu:", error)
  }
})

// Bật/tắt chọn món ăn trong Modal
const toggleChonMon = (idMon: number) => {
  const index = form.danhSachIdMon.indexOf(idMon)
  if (index > -1) {
    form.danhSachIdMon.splice(index, 1)
  } else {
    form.danhSachIdMon.push(idMon)
  }
  errors.idMon = ''
}

// Chọn hoặc bỏ chọn tất cả món đang hiển thị
const chonTatCa = () => {
  const tatCaIds = danhSachMonDaLoc.value.map(m => m.idMon)
  const tatCaDaChon = tatCaIds.every(id => form.danhSachIdMon.includes(id))

  if (tatCaDaChon) {
    form.danhSachIdMon = form.danhSachIdMon.filter(id => !tatCaIds.includes(id))
  } else {
    const setMoi = new Set([...form.danhSachIdMon, ...tatCaIds])
    form.danhSachIdMon = Array.from(setMoi)
  }
  errors.idMon = ''
}

const validateForm = () => {
  clearErrors()
  let isValid = true

  // Validate Combo
  if (!form.idCombo) {
    errors.idCombo = "Vui lòng chọn combo"
    isValid = false
  }

  // Validate Món ăn
  if (form.danhSachIdMon.length === 0) {
    errors.idMon = "Vui lòng chọn ít nhất 1 món ăn"
    isValid = false
  }

  // Check trùng món trong cùng combo (CHỈ ÁP DỤNG KHI THÊM MỚI TINH)
  if (form.idCombo && form.danhSachIdMon.length > 0 && !idChiTietComboHienTai.value) {
    const monTrung = form.danhSachIdMon.filter(idMonSelected => {
      return props.danhSach.some(item => {
        const itemComboId = (item as any).idCombo ?? (item as any).combo?.idCombo
        
        // Kiểm tra xem combo đã tồn tại chưa
        if (Number(itemComboId) !== Number(form.idCombo)) return false

        // Kiểm tra trong danh sách món gom nhóm
        if (item.danhSachMon && Array.isArray(item.danhSachMon)) {
          return item.danhSachMon.some(m => Number(m.idMon) === Number(idMonSelected))
        }

        // Kiểm tra với dữ liệu đơn
        const itemMonId = (item as any).idMon ?? (item as any).mon?.idMon
        return Number(itemMonId) === Number(idMonSelected)
      })
    })

    if (monTrung.length > 0) {
      errors.idMon = `Có món ăn đã tồn tại sẵn trong Combo này rồi!`
      isValid = false
    }
  }

  // Validate Số lượng
  const chuoiSoLuong = form.soLuong !== null && form.soLuong !== undefined ? form.soLuong.toString().trim() : ''
  if (chuoiSoLuong === '') {
    errors.soLuong = "Số lượng không được để trống"
    isValid = false
  } else if (isNaN(Number(form.soLuong)) || Number(form.soLuong) <= 0) {
    errors.soLuong = "Số lượng phải là số lớn hơn 0"
    isValid = false
  }

  // Validate Mô tả
  const mTa = form.moTa || ''
  if (mTa !== mTa.trim()) {
    errors.moTa = "Mô tả không được chứa khoảng trắng ở đầu hoặc cuối"
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
    idChiTietCombo: idChiTietComboHienTai.value || undefined,
    soLuong: Number(form.soLuong),
    danhSachIdMon: form.danhSachIdMon,
    idMon: form.danhSachIdMon[0],
    idCombo: form.idCombo as number,
    moTa: form.moTa.trim(),
  } as ChiTietComBoRequest)
}

defineExpose({
  fillForm(item?: any) {
    clearErrors()
    
    // 1. Không có item -> Reset về chế độ Thêm mới
    if (!item) {
      idChiTietComboHienTai.value = null
      form.soLuong = 1
      form.danhSachIdMon = []
      form.idCombo = ''
      form.moTa = ''
      return
    }

    const checkIdCombo = item.idCombo ?? item.combo?.idCombo ?? null
    const checkIdChiTiet = item.idChiTietCombo ?? null
    const checkSoLuong = item.soLuong ?? null

    // 2. Click chọn Combo từ bảng danh sách
    if (checkIdCombo !== null && checkIdChiTiet === null && checkSoLuong === null) {
      idChiTietComboHienTai.value = null
      form.soLuong = 1
      form.danhSachIdMon = []
      form.idCombo = Number(checkIdCombo)
      form.moTa = ''
      return
    }

    // 3. Chế độ Cập nhật (Sửa thông tin)
    idChiTietComboHienTai.value = item.idChiTietCombo
    form.soLuong = Number(item.soLuong)
    form.moTa = item.moTa || ''
    form.idCombo = item.idCombo ?? item.combo?.idCombo ?? ''
    
    // Nạp chính xác danh sách các ID món đã thuộc về Combo vào Modal Checkbox
    if (item.danhSachMon && Array.isArray(item.danhSachMon)) {
      form.danhSachIdMon = item.danhSachMon.map((m: any) => Number(m.idMon))
    } else {
      const idMonSingle = item.idMon ?? item.mon?.idMon
      form.danhSachIdMon = idMonSingle ? [Number(idMonSingle)] : []
    }
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Thông tin Chi Tiết Combo</h2>
      <p>{{ idChiTietComboHienTai ? 'Cập nhật món ăn trong Combo' : 'Thêm mới các món cấu thành Combo' }}</p>
    </div>

    <div class="luoi-bieu-mau">
      <!-- Select Combo -->
      <div class="form-group">
        <label>Combo</label>
        <select v-model="form.idCombo" :class="{ 'is-invalid': errors.idCombo }">
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

      <!-- Button mở Modal chọn món -->
      <div class="form-group">
        <label>Món ăn</label>
        <button 
          type="button" 
          class="nut-chon-mon" 
          :class="{ 'is-invalid': errors.idMon, 'da-chon': form.danhSachIdMon.length > 0 }"
          @click="openMonModal"
        >
          <span>{{ nhanHienThiMon || '-- Nhấp để chọn món ăn --' }}</span>
          <span class="icon-moti">📋</span>
        </button>
        <span class="error-text" v-if="errors.idMon">{{ errors.idMon }}</span>
      </div>

      <!-- Số lượng -->
      <div class="form-group">
        <label>Số lượng</label>
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

      <!-- Mô tả -->
      <div class="form-group">
        <label>Mô tả</label>
        <input 
          v-model="form.moTa" 
          type="text" 
          placeholder="Nhập mô tả..." 
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

    <!-- Modal Chọn Món Ăn -->
    <Teleport to="body">
      <div v-if="isShowMonModal" class="modal-overlay" @click.self="closeMonModal">
        <div class="modal-container">
          <div class="modal-header">
            <h3>Chọn món ăn</h3>
            <button class="nut-dong" @click="closeMonModal">✕</button>
          </div>

          <div class="modal-body">
            <div class="thanh-cong-cu-modal">
              <input 
                v-model="tuKhoaTimMon" 
                type="text" 
                class="input-tim-mon" 
                placeholder="🔍 Tìm nhanh tên món..." 
              />
              <button type="button" class="nut-chon-tat-ca" @click="chonTatCa">
                Chọn/Bỏ tất cả
              </button>
            </div>

            <div class="danh-sach-mon-scroll">
              <label 
                v-for="m in danhSachMonDaLoc" 
                :key="m.idMon"
                class="the-mon-checkbox"
                :class="{ active: form.danhSachIdMon.includes(m.idMon) }"
              >
                <div class="khung-trai">
                  <input 
                    type="checkbox" 
                    :value="m.idMon"
                    :checked="form.danhSachIdMon.includes(m.idMon)"
                    @change="toggleChonMon(m.idMon)"
                  />
                  <span class="ten-mon">{{ m.tenMon }}</span>
                </div>
              </label>

              <div v-if="danhSachMonDaLoc.length === 0" class="khong-co-data">
                Không tìm thấy món ăn phù hợp.
              </div>
            </div>

            <div class="modal-footer">
              <span class="thong-ke-da-chon">Đã chọn: <strong>{{ form.danhSachIdMon.length }}</strong> món</span>
              <button type="button" class="nut-xac-nhan" @click="closeMonModal">Xác nhận</button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
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
  font-weight: 500;
}

input,
select,
.nut-chon-mon {
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
  font-size: 14px;
}

input:focus,
select:focus {
  border-color: #f8d46a;
}

.nut-chon-mon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  color: #8f6b46;
  transition: all 0.2s ease;
}

.nut-chon-mon.da-chon {
  color: #8b5e34;
  font-weight: 600;
  border-color: #d8a85c;
  background: #fff3d3;
}

.nut-chon-mon:hover {
  background: #fff8eb;
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
  transition: background 0.2s;
}

.nut-chinh:hover {
  background: #e5bf55;
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

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(61, 40, 20, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-container {
  background: rgba(255, 248, 234, 0.98);
  border: 1px solid #e6d2aa;
  width: 90%;
  max-width: 500px;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(95, 61, 34, 0.15);
  animation: popIn 0.2s ease-out;
}

@keyframes popIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.modal-header h3 {
  margin: 0;
  color: #8b5e34;
  font-size: 18px;
}

.nut-dong {
  background: transparent;
  border: none;
  color: #8f6b46;
  font-size: 18px;
  cursor: pointer;
}

.thanh-cong-cu-modal {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.input-tim-mon {
  flex: 1;
  margin-top: 0;
}

.nut-chon-tat-ca {
  background: #fff3d3;
  border: 1px solid #e6d2aa;
  color: #8b5e34;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.nut-chon-tat-ca:hover {
  background: #efe0c1;
}

.danh-sach-mon-scroll {
  max-height: 280px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 4px;
}

.danh-sach-mon-scroll::-webkit-scrollbar {
  width: 6px;
}
.danh-sach-mon-scroll::-webkit-scrollbar-thumb {
  background: #e6d2aa;
  border-radius: 4px;
}

.the-mon-checkbox {
  padding: 12px 16px;
  border-radius: 12px;
  background: #fffdf8;
  border: 1px solid #efe0c1;
  color: #5f3d22;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
  user-select: none;
}

.khung-trai {
  display: flex;
  align-items: center;
  gap: 12px;
}

.the-mon-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin: 0;
  accent-color: #d8a85c;
  cursor: pointer;
}

.the-mon-checkbox:hover {
  background: #fff8eb;
}

.the-mon-checkbox.active {
  background: #efe0c1;
  border-color: #d8a85c;
  color: #8b5e34;
  font-weight: 600;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #efe0c1;
}

.thong-ke-da-chon {
  color: #8f6b46;
  font-size: 14px;
}

.thong-ke-da-chon strong {
  color: #8b5e34;
}

.nut-xac-nhan {
  background: #d8a85c;
  color: #3d2814;
  border: none;
  padding: 8px 20px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.khong-co-data {
  text-align: center;
  color: #8f6b46;
  padding: 20px 0;
}
</style>