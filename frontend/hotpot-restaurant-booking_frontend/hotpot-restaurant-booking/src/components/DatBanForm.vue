<script setup lang="ts">
import DatBanApi from '@/api/DatBanApi'
import { ref, watch } from 'vue'
import ComBoInDatBan from './ComBoInDatBan.vue'
import router from '@/router/index.ts'
import { paymentApi } from '@/api/PaymentApi.ts'
import PaymentDialog from './PaymentDialog.vue'

type PaymentMethod = 'CHUYEN_KHOAN' | 'VNPAY'

const showPayment = ref(false)

const paymentData = ref({
  qrUrl: '',
  amount: 0,
  content: '',
})

let paymentTimer: ReturnType<typeof setInterval> | null = null

// tao object luu data vao form
const formData = ref({
  idDatBan: null as number | null,
  sdtKhachHang: '',
  soNguoi: 0,
  ghiChu: '',
  thoiGianDenDuKien: '',
  soTienCoc: 0,
  phuongThucThanhToan: 'CHUYEN_KHOAN' as PaymentMethod,

  // Danh sách combo khách hàng đã chọn
  dsCombo: [] as any[],
})

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
        phuongThucThanhToan: newData.phuongThucThanhToan as PaymentMethod,
      }
    }
  },
  { deep: true },
)

// bien dung de bao cho table load lai bang
const emit = defineEmits(['refresh'])

//hàm xử lý tiền cọc khi chọn combo
const TI_LE_COC = 0.3

const chonCombo = (dsCombo: any[]) => {
  formData.value.dsCombo = dsCombo

  if (dsCombo.length === 0) {
    formData.value.soTienCoc = 0
    return
  }

  const tongTienCombo = dsCombo.reduce((tong, item) => {
    return tong + item.giaCombo * item.soLuong
  }, 0)

  formData.value.soTienCoc = Math.round(tongTienCombo * TI_LE_COC)
}

const add = async () => {
  isAdding.value = true // dùng để báo chặn watch không đè dữ liệu khi lỡ ở cha có emit watch sẽ đẩy data cũ làm hỏng luồng

  try {
    // Có combo => phải thanh toán tiền cọc
    if (formData.value.dsCombo.length > 0) {
      if (formData.value.phuongThucThanhToan === 'CHUYEN_KHOAN') {
        const paymentRes = await paymentApi.createPayment(formData.value)

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

              alert('Đặt bàn thành công!')
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
        const res = await paymentApi.createVNPayPayment(formData.value)

        window.location.href = res.data.paymentUrl
      }
    }
    // Không có combo => không cần cọc
    else {
      formData.value.soTienCoc = 0

      await DatBanApi.add(formData.value)

      resetForm()
      emit('refresh')

      alert('Đặt bàn thành công!')
    }
  } catch (error) {
    console.error('Lỗi:', error)
  } finally {
    isAdding.value = false
  }
}

const update = async () => {
  console.log('FORM DATA:', formData.value)

  if (formData.value.idDatBan == null) {
    console.error('Không có idDatBan để update')
    return
  }

  try {
    await DatBanApi.update(formData.value.idDatBan, formData.value)
    alert('sửa thành công')
    emit('refresh')
    resetForm()
  } catch (error) {
    console.error('sửa thất bại', error)
  }
}

const resetForm = () => {
  formData.value = {
    idDatBan: null,
    sdtKhachHang: '',
    soNguoi: 0,
    ghiChu: '',
    thoiGianDenDuKien: '',
    soTienCoc: 0,
    phuongThucThanhToan: 'CHUYEN_KHOAN',
    dsCombo: [],
  }
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

const quayLai = () => {
  router.push('/')
}
</script>

<template>
  <div class="page-container">
    <div class="form-container">
      <h3>Thông Tin Đặt Bàn</h3>

      <div class="form-group">
        <label>SĐT Khách Hàng</label>
        <input v-model="formData.sdtKhachHang" type="text" placeholder="Nhập SĐT..." />
      </div>

      <div class="row">
        <div class="form-group">
          <label>Số Người</label>
          <input v-model.number="formData.soNguoi" type="number" />
        </div>
        <div class="form-group">
          <label>Thời Gian Đến Dự Kiến</label>
          <input v-model="formData.thoiGianDenDuKien" type="datetime-local" />
        </div>
      </div>

      <div class="row">
        <div class="form-group">
          <label>Phương Thức Thanh Toán</label>
          <select v-model="formData.phuongThucThanhToan">
            <option value="CHUYEN_KHOAN">Chuyển khoản</option>
            <option value="VNPAY">VNPAY</option>
          </select>
        </div>
        <div class="form-group">
          <label>Tiền Cọc</label>
          <input v-model.number="formData.soTienCoc" type="number" readonly />
        </div>
      </div>

      <div class="form-group">
        <label>Ghi Chú</label>
        <textarea v-model="formData.ghiChu" rows="2"></textarea>
      </div>

      <div class="button-group">
        <button class="btn-add" @click.prevent="add()">Thêm Mới</button>
        <button class="btn-update" @click.prevent="update()">Cập Nhật</button>
      </div>
    </div>

    <div class="combo-section">
      <ComBoInDatBan v-model="formData.dsCombo" @selectedCombo="chonCombo" />

      <div class="go-home">
        <button class="btn-back" @click.prevent="quayLai()">Trở về trang chủ</button>
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
</template>

<style scoped>
.form-container {
  background: #1a1a1a;
  padding: 35px;
  border-radius: 4px;
  max-width: 500px;
  margin: 20px auto;
  border: 1px solid #3d3d3d;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}

h3 {
  color: #d4af37;
  text-align: center;
  font-size: 1.4rem;
  letter-spacing: 3px;
  margin-bottom: 35px;
  text-transform: uppercase;
  font-weight: 300;
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
  color: #a0a0a0;
  font-size: 0.65rem;
  letter-spacing: 1.5px;
  margin-bottom: 8px;
  text-transform: uppercase;
  display: block;
}

input,
select,
textarea {
  width: 100%;
  padding: 8px 0;
  background: transparent;
  border: none;
  border-bottom: 1px solid #444;
  color: #fff;
  font-size: 0.9rem;
  transition: all 0.3s;
}

input:focus,
select:focus,
textarea:focus {
  border-bottom: 1px solid #d4af37;
  outline: none;
}

.button-group {
  margin-top: 40px;
  display: flex;
  gap: 15px;
}

button {
  flex: 1;
  padding: 12px;
  border: 1px solid #d4af37;
  background: transparent;
  color: #d4af37;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.4s;
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
  display: flex; /* Chia làm 2 cột */
  justify-content: center; /* Căn giữa */
  align-items: flex-start; /* Căn đều phía trên */
  gap: 30px; /* Khoảng cách giữa 2 cột */
  padding: 20px;
  flex-wrap: wrap; /* Cho phép tự xuống dòng nếu màn hình hẹp */
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
  width: 100%;
  max-width: 400px; /* Tùy chỉnh độ rộng phần combo */
  min-width: 300px;
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
.go-home {
  margin-top: 25px;
}

.btn-back {
  width: 100%;
  padding: 14px 20px;

  background: linear-gradient(135deg, #1f1f1f, #2a2a2a);

  border: 1px solid #d4af37;
  border-radius: 4px;

  color: #d4af37;

  font-size: 0.9rem;
  font-weight: 500;

  letter-spacing: 2px;
  text-transform: uppercase;

  cursor: pointer;

  transition: all 0.3s ease;

  box-shadow:
    0 0 0 rgba(212, 175, 55, 0),
    0 10px 20px rgba(0, 0, 0, 0.3);
}

.btn-back:hover {
  background: #d4af37;
  color: #1a1a1a;

  transform: translateY(-2px);

  box-shadow: 0 8px 25px rgba(212, 175, 55, 0.25);
}

.btn-back:active {
  transform: translateY(0);
}
</style>
