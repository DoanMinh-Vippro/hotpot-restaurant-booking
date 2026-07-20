<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import HoaDonApi from '@/api/HoaDonApi' // Nhúng đúng file API của bạn

const props = defineProps<{
  show: boolean
  idHoaDon: number
  maHoaDon: string
  tongTien: number
  tenBan: string
}>()

const emit = defineEmits(['close', 'payment-success'])

// Cấu hình tài khoản ngân hàng nhận tiền của Nhà hàng (Khớp với cấu hình Backend)
const BANK_INFO = {
  ACCOUNT_NO: '88801032006',
  BANK_NAME: 'MBBank'
}

const sepayQrUrl = ref('')
let pollingTimer: any = null

onMounted(() => {
  // Tạo link mã QR động chuẩn VietQR thông qua API của SePay
  // Tự động gán: Số tiền cần thanh toán & Nội dung chuyển khoản là mã hóa đơn động của bàn
  sepayQrUrl.value = `https://qr.sepay.vn/img?acc=${BANK_INFO.ACCOUNT_NO}&bank=${BANK_INFO.BANK_NAME}&amount=${props.tongTien}&des=${props.maHoaDon}`
  
  // Kích hoạt cơ chế kiểm tra ngầm trạng thái hóa đơn định kỳ mỗi 3 giây
  startPollingStatus()
})

const startPollingStatus = () => {
  if (pollingTimer) clearInterval(pollingTimer)

  pollingTimer = setInterval(async () => {
    try {
      // Tận dụng hàm getById có sẵn trong file HoaDonApi.ts của bạn để check trạng thái thay đổi
      const res = await HoaDonApi.getById(props.idHoaDon)
      
      // Khi Webhook của Backend đã nhận được tiền và update trangThaiThanhToan lên 1 (Thành công)
      if (res.data && res.data.trangThaiThanhToan === 1) {
        clearInterval(pollingTimer)
        emit('payment-success') // Phát tín hiệu hoàn tất ra màn hình lớn để tự động chốt đơn
      }
    } catch (error) {
      console.error('Lỗi check số dư hóa đơn tự động:', error)
    }
  }, 3000) // 3 giây kiểm tra 1 lần
}

const stopAndClose = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  emit('close')
}

onBeforeUnmount(() => {
  if (pollingTimer) clearInterval(pollingTimer)
})
</script>

<template>
  <div v-if="show" class="popup-overlay">
    <div class="container-sepay">
      <h3 class="title">Chuyển Khoản Qua QR Code</h3>
      <div class="ban-badge">BÀN: {{ props.tenBan }}</div>

      <div class="qr-body">
        <!-- Ảnh QR động hiển thị sắc nét -->
        <img :src="sepayQrUrl" alt="Mã QR SePay Thanh Toán" class="qr-image" />

        <div class="thong-tin-ck">
          <div class="row">
            <label>Số tiền cần quét</label>
            <div class="money">{{ props.tongTien.toLocaleString('vi-VN') }} đ</div>
          </div>
          <div class="row">
            <label>Nội dung bắt buộc</label>
            <div class="code-box">{{ props.maHoaDon }}</div>
          </div>
        </div>

        <div class="status-waiting">
          <div class="loading-spin"></div>
          <span>Hệ thống đang chờ ngân hàng xác nhận tiền tự động...</span>
        </div>
      </div>

      <div class="btn">
        <button class="btn-cancel" @click="stopAndClose">Hủy Chuyển Khoản / Quay Lại</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.popup-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.container-sepay {
  width: 440px;
  background: linear-gradient(180deg, #2a2a2a, #1d1d1d);
  border-radius: 18px;
  padding: 26px;
  border: 1px solid rgba(255, 216, 107, 0.25);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.6), 0 0 20px rgba(255, 216, 107, 0.1);
  text-align: center;
  animation: popupShow 0.25s ease;
}

@keyframes popupShow {
  from { opacity: 0; transform: translateY(20px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.title {
  color: #ffd86b;
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 6px;
}

.ban-badge {
  background: rgba(255, 216, 107, 0.15);
  color: #ffd86b;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  display: inline-block;
  margin-bottom: 15px;
}

.qr-image {
  width: 240px;
  height: 240px;
  margin: 10px auto;
  display: block;
  border-radius: 12px;
  border: 4px solid #fff;
}

.thong-tin-ck {
  background: #111;
  border: 1px solid rgba(255, 216, 107, 0.15);
  border-radius: 12px;
  padding: 14px;
  margin: 18px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.row label {
  color: #b5b5b5;
  font-size: 14px;
}

.money {
  font-size: 18px;
  font-weight: 700;
  color: #ffd86b;
}

.code-box {
  background: #2a2a2a;
  padding: 4px 10px;
  border-radius: 6px;
  color: white;
  font-family: monospace;
  font-size: 15px;
  font-weight: bold;
  border: 1px solid #444;
  letter-spacing: 0.5px;
}

.status-waiting {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #aaa;
  font-size: 13px;
  margin-bottom: 20px;
}

.loading-spin {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 216, 107, 0.2);
  border-top-color: #ffd86b;
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.btn {
  display: flex;
}

.btn-cancel {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  background: #3a3a3a;
  color: white;
  transition: all 0.25s ease;
}

.btn-cancel:hover {
  background: #bd3a3a;
  transform: translateY(-2px);
}
</style>