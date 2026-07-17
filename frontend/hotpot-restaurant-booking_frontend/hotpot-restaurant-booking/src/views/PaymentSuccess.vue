<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBooking = () => {
  router.push('/dat-ban')
}
</script>

<template>
  <div class="payment-page">
    <div class="payment-card">
      <div class="success-icon">✓</div>

      <h1>Thanh toán thành công</h1>

      <p class="message">
        Đơn đặt bàn của bạn đã được ghi nhận. Nhà hàng sẽ xác nhận và chuẩn bị bàn cho bạn.
      </p>

      <div class="info-box">
        <p>
          <strong>Trạng thái:</strong>
          <span> Đã thanh toán</span>
        </p>

        <p>
          <strong>Thanh toán:</strong>
          <span> VNPay / Chuyển khoản</span>
        </p>
      </div>

      <div class="actions">
        <button class="btn-home" @click="goHome">Về trang chủ</button>

        <button class="btn-booking" @click="goBooking">Đặt bàn mới</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background: #f5f5f5;
}

.payment-card {
  width: 420px;

  background: white;

  padding: 40px;

  border-radius: 16px;

  text-align: center;

  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.success-icon {
  width: 80px;
  height: 80px;

  margin: 0 auto 20px;

  border-radius: 50%;

  background: #22c55e;

  color: white;

  font-size: 45px;

  display: flex;
  justify-content: center;
  align-items: center;
}

h1 {
  margin-bottom: 15px;

  color: #16a34a;
}

.message {
  color: #555;

  line-height: 1.6;
}

.info-box {
  margin-top: 25px;

  padding: 15px;

  background: #f8fafc;

  border-radius: 10px;

  text-align: left;
}

.info-box p {
  margin: 8px 0;
}

.actions {
  display: flex;

  gap: 15px;

  margin-top: 30px;

  justify-content: center;
}

button {
  padding: 12px 22px;

  border: none;

  border-radius: 8px;

  cursor: pointer;

  font-size: 14px;
}

.btn-home {
  background: #333;

  color: white;
}

.btn-booking {
  background: #ef4444;

  color: white;
}

button:hover {
  opacity: 0.85;
}
</style>
