<script setup lang="ts">
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import MonApi from '@/api/MonApi'
import { ref, watch, computed, onMounted } from 'vue'
import router from '@/router'

const props = defineProps(['datBanQuanLy', 'listBan'])
const emit = defineEmits(['refresh'])

const errors = ref<Record<string, string>>({})

type MenuSelection = {
  idMon: number
  tenMon: string
  donGiaHienTai: number
  quantity: number
}

// =============================
// INIT FORM
// =============================
const initForm = () => ({
  idDatBan: 0,

  // =============================
  // BAN - TẠM THỜI KHÔNG DÙNG
  // =============================
  idBan: null as number | null,

  idkhachHang: null as number | null,
  sdtKhachHang: '',
  soNguoi: 1,
  trangThai: 'CHO_XAC_NHAN',
  trangThaiCoc: 'CHUA_COC',
  ghiChu: '',
  thoiGianDenDuKien: '',
  soTienCoc: 0,
  phuongThucThanhToan: 'TIEN_MAT',
  datTaiQuay: false,
})

const formData = ref(initForm())
const availableMonList = ref<any[]>([])
const selectedMonItems = ref<MenuSelection[]>([])
const selectedMonId = ref<number | null>(null)
const selectedMonQty = ref(1)
const depositPercent = ref(30)

const isEditing = computed(() => formData.value.idDatBan > 0)
const isWalkInReservation = computed(() => Boolean(formData.value.datTaiQuay))

const formatCurrency = (value: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)

const orderTotal = computed(() =>
  selectedMonItems.value.reduce((sum, item) => sum + item.donGiaHienTai * item.quantity, 0),
)

const recommendedDeposit = computed(() => {
  if (!isWalkInReservation.value) return 0
  if (orderTotal.value <= 0) return 0
  return Math.round(orderTotal.value * (depositPercent.value / 100))
})

const effectiveDeposit = computed(() => {
  const manualDeposit = Number(formData.value.soTienCoc || 0)
  if (!isWalkInReservation.value) return manualDeposit
  return manualDeposit + recommendedDeposit.value
})

const addSelectedMon = () => {
  const selectedMon = availableMonList.value.find((item) => item.idMon === selectedMonId.value)
  if (!selectedMon) return

  const existing = selectedMonItems.value.find((item) => item.idMon === selectedMon.idMon)
  const unitPrice = selectedMon.giaSauGiam > 0 ? selectedMon.giaSauGiam : selectedMon.donGiaHienTai

  if (existing) {
    existing.quantity += Number(selectedMonQty.value || 1)
  } else {
    selectedMonItems.value.push({
      idMon: selectedMon.idMon,
      tenMon: selectedMon.tenMon,
      donGiaHienTai: unitPrice,
      quantity: Number(selectedMonQty.value || 1),
    })
  }

  selectedMonId.value = null
  selectedMonQty.value = 1
}

const removeSelectedMon = (idMon: number) => {
  selectedMonItems.value = selectedMonItems.value.filter((item) => item.idMon !== idMon)
}

const syncDepositFromSelection = () => {
  if (!isWalkInReservation.value) {
    if (formData.value.soTienCoc == null || formData.value.soTienCoc === 0) {
      formData.value.soTienCoc = 0
    }
    return
  }

  const currentDeposit = Number(formData.value.soTienCoc || 0)
  if (currentDeposit < 0) {
    formData.value.soTienCoc = 0
  }
}

const buildPayload = (payload: any) => {
  const data = { ...payload }

  if (data.soNguoi == null || data.soNguoi === '' || Number(data.soNguoi) <= 0) {
    data.soNguoi = 1
  }

  if (data.datTaiQuay) {
    data.idkhachHang = null
    data.trangThai = 'DA_NHAN_BAN'
    data.trangThaiCoc = data.trangThaiCoc || 'CHUA_COC'
    const manualDeposit = Number(data.soTienCoc || 0)
    if (data.soTienCoc == null || data.soTienCoc === '') {
      data.soTienCoc = 0
    }

    data.soTienCoc = manualDeposit + (selectedMonItems.value.length > 0 ? recommendedDeposit.value : 0)

    const note = (data.ghiChu || '').trim()
    const menuSummary = selectedMonItems.value.length > 0
      ? ` | Món đặt trước: ${selectedMonItems.value.map((item) => `${item.tenMon} x${item.quantity}`).join(', ')}`
      : ''
    data.ghiChu = note ? `${note}${menuSummary}` : (menuSummary ? menuSummary.slice(2) : 'Đặt bàn tại quầy')
  }

  if (data.thoiGianDenDuKien === '' || data.thoiGianDenDuKien == null) {
    delete data.thoiGianDenDuKien
  }

  if (data.sdtKhachHang === '' || data.sdtKhachHang == null) {
    delete data.sdtKhachHang
  }

  if (data.ghiChu === '' || data.ghiChu == null) {
    delete data.ghiChu
  }

  delete data.datTaiQuay
  return data
}

watch(
  [selectedMonItems, depositPercent, isWalkInReservation],
  syncDepositFromSelection,
  { deep: true },
)

// =============================
// WATCH DATA FROM PARENT
// =============================
watch(
  () => props.datBanQuanLy,
  (newData) => {
    if (newData) {
      const walkInDetected = Boolean((newData as any).datTaiQuay) ||
        String((newData as any).ghiChu || '').toLowerCase().includes('đặt bàn tại quầy')
      formData.value = {
        ...initForm(),
        ...newData,
        datTaiQuay: walkInDetected,
      }
    } else {
      formData.value = initForm()
    }
  },
  { immediate: true },
)

// =============================
// SAVE
// =============================
const save = async () => {
  errors.value = {}

  if (!formData.value.phuongThucThanhToan) {
    errors.value.phuongThucThanhToan = 'Vui lòng chọn phương thức thanh toán'
    return
  }

  try {
    const payload = buildPayload(formData.value)

    if (isEditing.value) {
      await DatBanQuanLyApi.update(formData.value.idDatBan, payload)
      alert('Sửa thành công')
    } else {
      await DatBanQuanLyApi.add(payload)
      alert('Thêm thành công')
    }

    emit('refresh')
    resetForm()
  } catch (error: any) {
    console.error('Lỗi thực hiện:', error)
    const message = error?.response?.data?.message || error?.response?.data?.error || 'Không thể lưu đặt bàn. Vui lòng kiểm tra lại dữ liệu.'
    alert(message)
  }
}

const resetForm = () => {
  formData.value = initForm()
  selectedMonItems.value = []
  selectedMonId.value = null
  selectedMonQty.value = 1
  depositPercent.value = 30
}

onMounted(async () => {
  try {
    const response = await MonApi.hienThiMon()
    availableMonList.value = (response.data || []).filter((item: any) => item.trangThai !== 0)
  } catch (error) {
    console.error('Lỗi khi tải thực đơn:', error)
  }
})
</script>

<template>
  <div class="form-wrapper">
    <button class="back-home-btn" @click="router.push('/')">&larr; TRANG CHỦ</button>

    <h2 class="form-title">QUẢN LÝ ĐẶT BÀN</h2>

    <div class="walkin-toggle">
      <label class="toggle-chip">
        <input v-model="formData.datTaiQuay" type="checkbox" />
        <span>🪑 Đặt bàn tại quầy</span>
      </label>
      <p v-if="isWalkInReservation" class="walkin-hint">
        Hệ thống sẽ ghi nhận là khách vãng lai, tự động chuyển trạng thái sang “Đã nhận bàn” và không cần mã khách hàng.
      </p>
    </div>

    <div class="form-grid">
      <div class="input-field">
        <label>{{ isWalkInReservation ? 'SĐT khách hàng (tùy chọn)' : 'SĐT Khách Hàng' }}</label>
        <input v-model="formData.sdtKhachHang" type="text" />
        <span v-if="errors.sdtKhachHang" class="error-msg">{{ errors.sdtKhachHang }}</span>
      </div>

      <div class="input-field">
        <label>{{ isWalkInReservation ? 'ID khách hàng (không bắt bu)' : 'ID Khách Hàng' }}</label>
        <input v-model.number="formData.idkhachHang" type="number" />
      </div>

      <!-- =============================
           BAN - TẠM THỜI DISABLE UI
           =============================
      <div class="input-field">
        <label>Loại Bàn</label>
        <select v-model="formData.idBan">
          <option :value="null" disabled>-- Chọn loại bàn --</option>
          <option v-for="b in listBan" :key="b.idBan" :value="b.idBan">
            {{ b.loaiBan }}
          </option>
        </select>
      </div>
      -->

      <div class="input-field">
        <label>Số Người</label>
        <input v-model.number="formData.soNguoi" type="number" />
      </div>

      <div class="input-field">
        <label>Tiền Cọc</label>
        <input v-model.number="formData.soTienCoc" type="number" class="highlight-gold" />
        <span v-if="isWalkInReservation" class="walkin-hint">
          Tiền cọc gửi lên: {{ formatCurrency(effectiveDeposit) }}
        </span>
      </div>

      <div class="input-field">
        <label>Trạng Thái Cọc</label>
        <select v-model="formData.trangThaiCoc">
          <option :value="null">-- Chọn trạng thái cọc --</option>
          <option value="CHUA_COC">Chưa cọc</option>
          <option value="DA_COC">Đã cọc</option>
          <option value="DA_HOAN_COC">Đã hoàn cọc</option>
          <option value="KHONG_HOAN_COC">Không hoàn cọc</option>
        </select>
      </div>

      <div class="input-field">
        <label>Thời Gian Đến</label>
        <input v-model="formData.thoiGianDenDuKien" type="datetime-local" />
      </div>

      <div class="input-field">
        <label>Thanh Toán</label>
        <select v-model="formData.phuongThucThanhToan">
          <option value="CHUYEN_KHOAN">Chuyển khoản</option>
          <option value="VNPAY">VNPAY</option>
          <option value="TIEN_MAT">Tiền mặt</option>
        </select>
      </div>

      <div class="input-field">
        <label>Trạng Thái</label>
        <select v-model="formData.trangThai">
          <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
          <option value="DA_XAC_NHAN">Đã xác nhận</option>
          <option value="DA_NHAN_BAN">Đã nhận bàn</option>
          <option value="DA_HUY">Đã hủy</option>
          <option value="HOAN_THANH">Hoàn thành</option>
        </select>
      </div>
    </div>

    <div v-if="isWalkInReservation" class="menu-section full-width">
      <div class="section-title">🧾 Chọn món đặt trước tại quầy</div>

      <div class="menu-selector">
        <select v-model="selectedMonId">
          <option :value="null">-- Chọn món --</option>
          <option v-for="item in availableMonList" :key="item.idMon" :value="item.idMon">
            {{ item.tenMon }} - {{ formatCurrency(item.giaSauGiam > 0 ? item.giaSauGiam : item.donGiaHienTai) }}
          </option>
        </select>

        <input v-model.number="selectedMonQty" type="number" min="1" />

        <button class="btn-secondary small-btn" @click.prevent="addSelectedMon">THÊM MÓN</button>
      </div>

      <div v-if="selectedMonItems.length" class="selected-items">
        <div v-for="item in selectedMonItems" :key="item.idMon" class="selected-item">
          <span>{{ item.tenMon }} x{{ item.quantity }}</span>
          <span>{{ formatCurrency(item.donGiaHienTai * item.quantity) }}</span>
          <button class="remove-btn" @click.prevent="removeSelectedMon(item.idMon)">×</button>
        </div>
      </div>

      <div class="deposit-summary">
        <div><strong>Tổng món:</strong> {{ formatCurrency(orderTotal) }}</div>
        <div class="deposit-percent-row">
          <label>Phần trăm cọc</label>
          <input v-model.number="depositPercent" type="number" min="0" max="100" />
          <span>%</span>
        </div>
        <div><strong>Tiền cọc đề xuất:</strong> {{ formatCurrency(recommendedDeposit) }}</div>
      </div>
    </div>

    <div class="input-field full-width">
      <label>Ghi Chú</label>
      <textarea v-model="formData.ghiChu" rows="2"></textarea>
    </div>

    <div class="button-bar">
      <button class="btn-primary" @click.prevent="save()">
        {{ isEditing ? 'CẬP NHẬT THÔNG TIN' : 'THÊM MỚI ĐẶT BÀN' }}
      </button>

      <button v-if="isEditing" class="btn-secondary" @click.prevent="resetForm()">HỦY CHỌN</button>
    </div>
  </div>
</template>

<style scoped>
.menu-section {
  border: 1px solid rgba(197, 160, 89, 0.25);
  border-radius: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  margin-top: 10px;
}

.section-title {
  color: #c5a059;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 12px;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}

.menu-selector {
  display: grid;
  grid-template-columns: 2fr 0.8fr auto;
  gap: 10px;
  align-items: center;
}

.small-btn {
  padding: 10px 14px;
  font-size: 0.65rem;
  white-space: nowrap;
}

.selected-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.selected-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: rgba(197, 160, 89, 0.08);
  border-radius: 8px;
  color: #fff;
}

.remove-btn {
  background: transparent;
  border: none;
  color: #ff7b7b;
  font-size: 1rem;
  cursor: pointer;
}

.deposit-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  color: #f3dca1;
}

.deposit-percent-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.deposit-percent-row label {
  margin: 0;
  min-width: 100px;
}

.deposit-percent-row input {
  max-width: 90px;
}

.walkin-toggle {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.toggle-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 8px 14px;
  border: 1px solid #c5a059;
  border-radius: 999px;
  color: #fff;
  background: rgba(197, 160, 89, 0.12);
  cursor: pointer;
}

.toggle-chip input {
  width: auto;
  margin: 0;
}

.walkin-hint {
  margin: 0;
  color: #f2c96d;
  font-size: 0.82rem;
}

.form-wrapper {
  background: #0d0d0d;
  padding: 40px;
  max-width: 650px;
  margin: 40px auto;
  border: 1px solid #222;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.9);
  border-radius: 12px;
}

.form-title {
  color: #c5a059;
  font-size: 1.1rem;
  text-align: center;
  margin-bottom: 40px;
  letter-spacing: 5px;
  text-transform: uppercase;
  font-weight: 300;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px 20px;
}

.input-field {
  display: flex;
  flex-direction: column;
  width: 100%;
}

label {
  color: #888;
  font-size: 0.65rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 10px;
  transition: 0.3s;
}

/* INPUT & SELECT "GẠCH CHÂN" */
input,
select,
textarea {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-bottom: 1px solid #333;
  background: transparent;
  color: #fff;
  font-size: 0.95rem;
  transition: 0.4s;
  appearance: none; /* Bỏ icon mũi tên mặc định để trông sang hơn */
  -webkit-appearance: none;
}

/* Đảm bảo option hiển thị rõ ràng */
select option {
  background: #1a1a1a;
  color: #fff;
  padding: 10px;
}

input:focus,
select:focus,
textarea:focus {
  border-bottom: 1px solid #c5a059;
  outline: none;
}

.input-field:focus-within label {
  color: #c5a059;
}

.full-width {
  grid-column: span 2;
  margin-top: 10px;
}

.button-bar {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 50px;
}

.btn-primary,
.btn-secondary {
  padding: 12px 40px;
  font-size: 0.7rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  cursor: pointer;
  transition: 0.4s;
  border: 1px solid #c5a059;
}

.btn-primary {
  background: #c5a059;
  color: #000;
  font-weight: bold;
}
.btn-primary:hover {
  background: #fff;
  border-color: #fff;
}

.btn-secondary {
  background: transparent;
  color: #c5a059;
}
.btn-secondary:hover {
  background: #333;
  border-color: #fff;
  color: #fff;
}
.form-wrapper {
  position: relative; /* Rất quan trọng để nút absolute nằm trong form */
  /* ... các style cũ của bạn giữ nguyên ... */
}

.back-home-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  background: transparent;
  border: none;
  color: #666; /* Màu xám tối để hài hòa với theme */
  font-size: 0.7rem;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  transition: 0.3s;
}

.back-home-btn:hover {
  color: #c5a059; /* Đổi màu vàng gold khi hover */
}
.error-msg {
  color: red;
  font-size: 0.75rem;
  margin-top: 4px;
  display: block;
}
</style>
