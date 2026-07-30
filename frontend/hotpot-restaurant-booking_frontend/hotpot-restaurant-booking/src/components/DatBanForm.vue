<script setup lang="ts">
import DatBanApi from '@/api/DatBanApi'
import { ref, watch } from 'vue'
import ComBoInDatBan from './ComBoInDatBan.vue'
import MonInDatBan from './MonInDatBan.vue'
import { paymentApi } from '@/api/PaymentApi.ts'
import PaymentDialog from './PaymentDialog.vue'
import ConfirmBanDialog from './ConfirmBanDialog.vue'
import PopupDatBanThanhCong from './PopupDatBanThanhCong.vue'
import { useAuthStore } from '@/stores/AuthStore'
import VueFlatPickr from 'vue-flatpickr-component'
import 'flatpickr/dist/flatpickr.css'
import { Vietnamese } from 'flatpickr/dist/l10n/vn.js'

type PaymentMethod = 'CHUYEN_KHOAN' | 'VNPAY' | 'CHUA_THANH_TOAN'

const showPayment = ref(false)
const showConfirmBan = ref(false) // xác nhận của check bàn
const dsBanDeXuat = ref<any[]>([])
const checkBanResult = ref<any>(null)
const authStore = useAuthStore() // lấy tên khách bỏ form
const errors = ref({
  sdtKhachHang: '',
  soNguoi: '',
  thoiGianDenDuKien: '',
})
const isResetting = ref(false)
const datBanThanhCong = ref(false)

//validate
const validatePhone = () => {
  const regex = /^[0][1-9][0-9]{8}$/
  if (!formData.value.sdtKhachHang.trim()) {
    errors.value.sdtKhachHang = 'Không được để trống số điện thoại'
    return false
  }
  if (!regex.test(formData.value.sdtKhachHang)) {
    errors.value.sdtKhachHang = 'Số điện thoại không đúng định dạng'
    return false
  }
  errors.value.sdtKhachHang = ''
  return true
}

const validateSoNguoi = () => {
  if (!formData.value.soNguoi) {
    errors.value.soNguoi = 'Vui lòng nhập số người'
    return false
  }

  if (formData.value.soNguoi <= 0) {
    errors.value.soNguoi = 'Số người phải lớn hơn 0'
    return false
  }

  errors.value.soNguoi = ''
  return true
}

const validateDate = () => {
  const value = formData.value.thoiGianDenDuKien

  if (!value) {
    errors.value.thoiGianDenDuKien = 'Vui lòng chọn thời gian'
    return false
  }

  const date = new Date(value)

  const now = new Date()

  if (date < now) {
    errors.value.thoiGianDenDuKien = 'Không được chọn thời gian quá khứ'
    return false
  }

  const hour = date.getHours()

  if (!((hour >= 10 && hour < 14) || (hour >= 18 && hour <= 23))) {
    errors.value.thoiGianDenDuKien = 'Nhà hàng chỉ phục vụ từ 10:00-14:00 và 18:00-24:00'
    return false
  }

  errors.value.thoiGianDenDuKien = ''
  return true
}

const validateForm = () => {
  return validatePhone() && validateSoNguoi() && validateDate()
}
//=================================================================================

// hàm format tiền cọc
const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('vi-VN').format(value)
}

const paymentData = ref({
  qrUrl: '',
  amount: 0,
  content: '',
})

let paymentTimer: ReturnType<typeof setInterval> | null = null

// tao object luu data vao form
const formData = ref({
  idDatBan: null as number | null,
  dsBan: [] as number[],
  sdtKhachHang: '',
  soNguoi: 0,
  ghiChu: '',
  thoiGianDenDuKien: null as Date | string | null,
  soTienCoc: 0,
  phuongThucThanhToan: 'CHUA_THANH_TOAN' as PaymentMethod,

  // Danh sách combo khách hàng đã chọn
  dsCombo: [] as any[],
  dsMon: [] as any[],
})
const flatpickrConfig = {
  enableTime: true,
  time_24hr: true,
  dateFormat: 'Y-m-d H:i',
  minuteIncrement: 30,
  locale: Vietnamese,

  minDate: new Date(),

  disable: [
    function (date: Date) {
      const today = new Date()
      today.setHours(0, 0, 0, 0)

      return date < today
    },
  ],
}
watch(
  () => formData.value.sdtKhachHang,
  () => {
    if (!isResetting.value) {
      validatePhone()
    }
  },
)

watch(
  () => formData.value.soNguoi,
  () => {
    if (!isResetting.value) {
      validateSoNguoi()
    }
  },
)

watch(
  () => formData.value.thoiGianDenDuKien,
  () => {
    if (!isResetting.value) {
      validateDate()
    }
  },
)

//cong de nhanh du lieu tu DatBanView
const props = defineProps(['datBanForm'])

// 1. Thêm biến trạng thái để chặn watch
const isAdding = ref(false)

//watch dung de lay du lieu ma datBanForm nhan doc dua vao object formData
watch(
  () => props.datBanForm,
  (newData) => {
    if (isAdding.value || !newData) return

    if (newData.idDatBan !== formData.value.idDatBan) {
      formData.value = {
        ...newData,
        dsCombo: newData.dsCombo || [],
        dsMon: newData.dsMon || [],
        phuongThucThanhToan: newData.phuongThucThanhToan as PaymentMethod,
      }
    }
  },
  { deep: true },
)

// bien dung de bao cho table load lai bang
const emit = defineEmits(['refresh'])

//hàm xử lý tiền cọc khi chọn combo và món
const TI_LE_COC = 0.3

const tinhTienCoc = () => {
  const tongTienCombo = formData.value.dsCombo.reduce(
    (tong, item) => tong + item.giaCombo * item.soLuong,
    0,
  )

  const tongTienMon = formData.value.dsMon.reduce(
    (tong, item) => tong + item.donGiaHienTai * item.soLuong,
    0,
  )

  const tongTien = tongTienCombo + tongTienMon

  if (tongTien === 0) {
    formData.value.soTienCoc = 0
    formData.value.phuongThucThanhToan = 'CHUA_THANH_TOAN'
    return
  }

  formData.value.soTienCoc = Math.round(tongTien * TI_LE_COC)
}
const chonCombo = (dsCombo: any[]) => {
  formData.value.dsCombo = dsCombo
  tinhTienCoc()
}
const chonMon = (dsMon: any[]) => {
  formData.value.dsMon = dsMon
  tinhTienCoc()
}

//==========================================
const checkBan = async () => {
  if (!validateForm()) return
  try {
    const res = await DatBanApi.checkBan({
      soNguoi: formData.value.soNguoi,
      thoiGianDenDuKien: formData.value.thoiGianDenDuKien
        ? String(formData.value.thoiGianDenDuKien).replace(' ', 'T') + ':00'
        : null,
    })

    checkBanResult.value = res.data
    dsBanDeXuat.value = res.data.dsBan || []

    showConfirmBan.value = true
  } catch (error) {
    console.error('Lỗi kiểm tra bàn:', error)
  }
}

const confirmBan = () => {
  if (!checkBanResult.value) return
  formData.value.dsBan = checkBanResult.value.dsBan.map((b: any) => b.idBan)
  showConfirmBan.value = false
  createBooking()
}
const cancelBan = () => {
  showConfirmBan.value = false
  checkBanResult.value = null
  dsBanDeXuat.value = []
  formData.value.dsBan = []
}

const createBooking = async () => {
  isAdding.value = true // dùng để báo chặn watch không đè dữ liệu khi lỡ ở cha có emit watch sẽ đẩy data cũ làm hỏng luồng

  try {
    // Có combo => phải thanh toán tiền cọc
    if (formData.value.dsCombo.length > 0 || formData.value.dsMon.length > 0) {
      if (formData.value.phuongThucThanhToan === 'CHUYEN_KHOAN') {
        const paymentRes = await paymentApi.createPayment({
          ...formData.value,
          thoiGianDenDuKien: formData.value.thoiGianDenDuKien
            ? String(formData.value.thoiGianDenDuKien).replace(' ', 'T') + ':00'
            : null,
        })

        paymentData.value = {
          qrUrl: paymentRes.data.qrUrl,
          amount: paymentRes.data.amount,
          content: paymentRes.data.content,
        }

        showPayment.value = true

        if (paymentTimer) {
          clearInterval(paymentTimer)
        }

        paymentTimer = setInterval(async () => {
          try {
            const res = await paymentApi.checkPaymentStatus(paymentData.value.content)

            if (res.data) {
              if (paymentTimer) {
                clearInterval(paymentTimer)
                paymentTimer = null
              }

              showPayment.value = false

              paymentData.value = {
                qrUrl: '',
                amount: 0,
                content: '',
              }

              resetForm()
              emit('refresh')

              datBanThanhCong.value = true
            }
          } catch (e) {
            console.error(e)

            if (paymentTimer) {
              clearInterval(paymentTimer)
              paymentTimer = null
            }
          }
        }, 2000)
      } else if (formData.value.phuongThucThanhToan === 'VNPAY') {
        const res = await paymentApi.createVNPayPayment({
          ...formData.value,
          thoiGianDenDuKien: formData.value.thoiGianDenDuKien
            ? String(formData.value.thoiGianDenDuKien).replace(' ', 'T') + ':00'
            : null,
        })

        window.location.href = res.data.paymentUrl
      }
    }
    // Không có combo => không cần cọc
    else {
      formData.value.soTienCoc = 0

      await DatBanApi.add({
        ...formData.value,
        thoiGianDenDuKien: formData.value.thoiGianDenDuKien
          ? String(formData.value.thoiGianDenDuKien).replace(' ', 'T') + ':00'
          : null,
      })

      resetForm()
      emit('refresh')

      datBanThanhCong.value = true
    }
  } catch (error) {
    console.error('Lỗi:', error)
  } finally {
    isAdding.value = false
  }
}

// const update = async () => {
//   console.log('FORM DATA:', formData.value)

//   if (formData.value.idDatBan == null) {
//     console.error('Không có idDatBan để update')
//     return
//   }

//   try {
//     await DatBanApi.update(formData.value.idDatBan, formData.value)
//     alert('sửa thành công')
//     emit('refresh')
//     resetForm()
//   } catch (error) {
//     console.error('sửa thất bại', error)
//   }
// }

const resetForm = () => {
  isResetting.value = true

  formData.value = {
    idDatBan: null,
    dsBan: [],
    sdtKhachHang: '',
    soNguoi: 0,
    ghiChu: '',
    thoiGianDenDuKien: null,
    soTienCoc: 0,
    phuongThucThanhToan: 'CHUA_THANH_TOAN',
    dsCombo: [],
    dsMon: [],
  }

  errors.value = {
    sdtKhachHang: '',
    soNguoi: '',
    thoiGianDenDuKien: '',
  }

  setTimeout(() => {
    isResetting.value = false
  }, 0)
}

const closePaymentDialog = () => {
  showPayment.value = false

  paymentData.value = {
    qrUrl: '',
    amount: 0,
    content: '',
  }

  if (paymentTimer) {
    clearInterval(paymentTimer)
    paymentTimer = null
  }
}
</script>

<template>
  <div class="page-container">
    <div class="form-container">
      <h3>Thông Tin Đặt Bàn</h3>

      <div class="welcome-box">
        <span class="welcome-text">
          👋 Xin chào,
          <span class="customer-name">
            {{ authStore.tenKhachHang }}
          </span>
          ! Chúc bạn có một bữa ăn thật ngon tại nhà hàng.
        </span>
      </div>

      <div class="form-group">
        <label>SĐT Khách Hàng</label>

        <input v-model="formData.sdtKhachHang" type="text" placeholder="Nhập SĐT..." />

        <p v-if="errors.sdtKhachHang" class="error-text">
          {{ errors.sdtKhachHang }}
        </p>
      </div>

      <div class="row">
        <div class="form-group">
          <label>Số Người</label>

          <input v-model.number="formData.soNguoi" type="number" />

          <p v-if="errors.soNguoi" class="error-text">
            {{ errors.soNguoi }}
          </p>
        </div>

        <div class="form-group">
          <label>Thời Gian Đến Dự Kiến</label>

          <VueFlatPickr
            v-model="formData.thoiGianDenDuKien"
            :config="flatpickrConfig"
            placeholder="Chọn ngày giờ đến"
          />

          <p v-if="errors.thoiGianDenDuKien" class="error-text">
            {{ errors.thoiGianDenDuKien }}
          </p>
        </div>
      </div>

      <div class="row">
        <div class="form-group">
          <label>Phương Thức Thanh Toán</label>

          <select v-model="formData.phuongThucThanhToan" :disabled="formData.soTienCoc === 0">
            <option value="CHUYEN_KHOAN">Chuyển khoản</option>

            <option value="VNPAY">VNPAY</option>
          </select>
        </div>

        <div class="form-group">
          <label>Tiền Cọc</label>

          <input :value="`${formatCurrency(formData.soTienCoc)} VNĐ`" type="text" readonly />
        </div>
      </div>

      <div class="form-group">
        <label>Ghi Chú</label>

        <textarea v-model="formData.ghiChu" rows="2" />
      </div>

      <div class="button-group">
        <button class="btn-add" @click.prevent="checkBan()">Kiểm tra bàn</button>
      </div>
    </div>

    <div class="combo-section">
      <div class="menu-wrapper">
        <ComBoInDatBan v-model="formData.dsCombo" @selectedCombo="chonCombo" />
        <MonInDatBan v-model="formData.dsMon" @selectedMon="chonMon" />
      </div>
    </div>
  </div>

  <PaymentDialog
    :show="showPayment"
    :qr-url="paymentData.qrUrl"
    :amount="paymentData.amount"
    :content="paymentData.content"
    @close="closePaymentDialog"
  />

  <ConfirmBanDialog
    :show="showConfirmBan"
    :result="checkBanResult"
    @confirm="confirmBan"
    @cancel="cancelBan"
  />

  <PopupDatBanThanhCong :show="datBanThanhCong" @close="datBanThanhCong = false" />
</template>

<style scoped>
.form-container {
  width: 100%;
  max-width: 520px;
  margin: 0;
  padding: 38px;
  background: linear-gradient(180deg, #181818 0%, #101010 100%);
  border: 1px solid rgba(212, 175, 55, 0.25);
  border-radius: 18px;
  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.45),
    inset 0 1px rgba(255, 255, 255, 0.04);
}

.error-text {
  margin-top: 6px;
  color: #ff6b6b;
  font-size: 13px;
  font-weight: 500;
}

h3 {
  color: #f2d57c;
  font-size: 30px;
  font-weight: 600;
  text-align: center;
  letter-spacing: 2px;
  margin-bottom: 35px;
}

/* Quan trọng: Tạo khoảng cách giữa các nhóm */
.form-group {
  margin-bottom: 25px;
}

/* Xử lý hàng đôi */
.row {
  display: flex;
  gap: 20px;
}
.row > .form-group {
  flex: 1;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #d8c38d;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 1px;
}

input,
select,
textarea {
  width: 100%;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #3b3b3b;
  background: #222;
  color: #fff;
  transition: 0.25s;
  font-size: 15px;
  box-sizing: border-box;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #d4af37;
  box-shadow: 0 0 12px rgba(212, 175, 55, 0.18);
  background: #272727;
}

.button-group {
  margin-top: 40px;
  display: flex;
  gap: 15px;
}

button {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: 1px;
  transition: 0.25s;
}

.btn-add {
  background: linear-gradient(135deg, #c79b33, #e8cf84);
  color: #2f2308;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(212, 175, 55, 0.35);
}

button:hover {
  background: #d4af37;
  color: #1a1a1a;
}
/* Ép màu cho danh sách xổ xuống */
select option {
  background-color: #1a1a1a; /* Màu nền tối của form */
  color: #fff; /* Màu chữ trắng */
  padding: 10px;
}

/* Loại bỏ cái border-bottom không cần thiết trong select */
select {
  appearance: none; /* Loại bỏ mũi tên mặc định của trình duyệt */
  -webkit-appearance: none;
  -moz-appearance: none;
  cursor: pointer;
  background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%23d4af37%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E');
  background-repeat: no-repeat;
  background-position: right 0px top 50%;
  background-size: 10px auto;
}

input[type='datetime-local']::-webkit-calendar-picker-indicator {
  filter: invert(1); /* Đảo màu icon lịch để hợp với nền đen */
  cursor: pointer;
}
/* Container cha chứa cả Form và Combo */
.page-container {
  width: 100%;
  max-width: 1400px;
  margin: auto;
  display: flex;
  gap: 35px;
  align-items: flex-start;
  justify-content: center;
  padding: 35px;
}

/* Sửa lại form-container một chút */
.form-container {
  background: #1a1a1a;
  padding: 35px;
  border-radius: 4px;
  width: 100%;
  max-width: 500px; /* Giới hạn chiều rộng form */
  border: 1px solid #3d3d3d;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
  margin: 0; /* Bỏ margin auto để dùng Flex căn giữa */
}

/* Định dạng cột bên phải */
.combo-section {
  width: 420px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (max-width: 768px) {
  .page-container {
    flex-direction: column; /* Chuyển từ ngang sang dọc */
  }

  .form-container,
  .combo-section {
    max-width: 100%; /* Chiếm hết chiều rộng màn hình */
  }
}

input::placeholder,
textarea::placeholder {
  color: #777;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.welcome-box {
  /* css của phần tên khách chào mừng*/
  margin-bottom: 24px;
  padding: 14px 18px;
  border-left: 4px solid #d4af37;
  background: rgba(212, 175, 55, 0.08);
  border-radius: 10px;
}

.welcome-text {
  color: #e8dfc3;
  font-size: 15px;
  line-height: 1.6;
}

.customer-name {
  color: #f2d57c;
  font-weight: 700;
  font-size: 17px;
}
.menu-wrapper {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.tong-coc-box {
  background: #1a1a1a;
  border: 1px solid rgba(212, 175, 55, 0.25);
  border-radius: 10px;
  padding: 16px 18px;
}

.tong-coc-box .dong {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tong-coc-box span {
  color: #cfcfcf;
  font-size: 14px;
}

.tong-coc-box strong {
  color: #d4af37;
  font-size: 18px;
  font-weight: 700;
}
.mon-select-box {
  background: #222;
  border: 1px solid #333;
  border-radius: 10px;
  padding: 14px;
  margin-top: 0;
}

.mon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-left: 3px solid #c5a059;
  padding-left: 8px;
}

.mon-header span {
  color: #c5a059;
  font-size: 0.85rem;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
</style>
