<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import HoaDonApi from '@/api/HoaDonApi'

const props = withDefaults(
  defineProps<{
    show: boolean
    idHoaDon?: number
    maHoaDon?: string
    tongTien?: number
    tenBan?: string
    amount?: number
    bankId?: string
    accountNo?: string
    accountName?: string
    description?: string
  }>(),
  {
    tongTien: 0,
    amount: 0,
    bankId: 'MSB',
    accountNo: '80000739235',
    accountName: 'HOTPOT RESTAURANT',
    description: '',
    maHoaDon: '',
    tenBan: 'N/A',
    idHoaDon: 0,
  },
)

const emit = defineEmits(['close', 'payment-success'])

const BANK_INFO = {
  ACCOUNT_NO: props.accountNo || '80000739235',
  BANK_NAME: props.bankId || 'MSB',
  ACCOUNT_NAME: props.accountName || 'HOTPOT RESTAURANT',
}

const qrAmount = computed(() => Number(props.amount || props.tongTien || 0))
const qrDescription = computed(() => props.description || props.maHoaDon || 'THANH_TOAN')
const sepayQrUrl = computed(() => {
  const amount = Number(qrAmount.value || 0)
  const bankId = (BANK_INFO.BANK_NAME || 'MSB').toUpperCase()
  const accountNo = BANK_INFO.ACCOUNT_NO
  const accountName = encodeURIComponent(BANK_INFO.ACCOUNT_NAME)
  const addInfo = encodeURIComponent(qrDescription.value)

  return `https://img.vietqr.io/image/${bankId}-${accountNo}-compact2.png?amount=${amount}&addInfo=${addInfo}&accountName=${accountName}`
})

const finishPayment = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  emit('payment-success')
}

let pollingTimer: any = null

onMounted(() => {
  if (props.idHoaDon && props.idHoaDon > 0) {
    startPollingStatus()
  }
})

const startPollingStatus = () => {
  if (pollingTimer) clearInterval(pollingTimer)

  pollingTimer = setInterval(async () => {
    try {
      const res = await HoaDonApi.getById(props.idHoaDon as number)
      if (res.data && res.data.trangThaiThanhToan === 1) {
        clearInterval(pollingTimer)
        emit('payment-success')
      }
    } catch (error) {
      console.error('Lỗi check số dư hóa đơn tự động:', error)
    }
  }, 3000)
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
      <h3 class="title">Mã QR VietQR</h3>
      <div class="ban-badge">BÀN: {{ props.tenBan }}</div>

      <div class="qr-body">
        <img :src="sepayQrUrl" alt="Mã QR VietQR" class="qr-image" />

        <div class="thong-tin-ck">
          <div class="row">
            <label>Ngân hàng</label>
            <div class="value-text">{{ BANK_INFO.BANK_NAME }}</div>
          </div>
          <div class="row">
            <label>STK</label>
            <div class="value-text">{{ BANK_INFO.ACCOUNT_NO }}</div>
          </div>
          <div class="row">
            <label>Tên tài khoản</label>
            <div class="value-text">{{ BANK_INFO.ACCOUNT_NAME }}</div>
          </div>
          <div class="row">
            <label>Số tiền</label>
            <div class="money">{{ qrAmount.toLocaleString('vi-VN') }} đ</div>
          </div>
          <div class="row">
            <label>Nội dung CK</label>
            <div class="code-box">{{ qrDescription }}</div>
          </div>
        </div>
      </div>

      <div class="btn-row">
        <button class="btn-secondary" @click="stopAndClose">Quay lại</button>
        <button class="btn-primary" @click="finishPayment">Hoàn tất thanh toán</button>
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

.btn-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 20px;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #ffd86b, #d4af37);
  color: #111;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 12px rgba(255, 216, 107, 0.28);
}

.btn-secondary {
  background: #3a3a3a;
  color: white;
}

.btn-secondary:hover {
  background: #4a4a4a;
  transform: translateY(-2px);
}
</style>