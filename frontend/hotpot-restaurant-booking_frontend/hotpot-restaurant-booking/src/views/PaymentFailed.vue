<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const retryPayment = () => {
  router.back()
}
</script>

<template>
  <div class="payment-page">
    <div class="payment-card">
      <div class="failed-icon">✕</div>

      <h1>Thanh toán thất bại</h1>

      <p class="message">
        Giao dịch chưa được hoàn tất. Vui lòng kiểm tra lại thông tin thanh toán hoặc thử lại.
      </p>

      <div class="warning-box">
        <p>
          <strong>Lưu ý:</strong>
        </p>

        <p>Nếu tiền đã bị trừ, vui lòng liên hệ nhà hàng để được hỗ trợ.</p>
      </div>

      <div class="actions">
        <button class="btn-retry" @click="retryPayment">Thử lại</button>

        <button class="btn-home" @click="goHome">Về trang chủ</button>
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

.failed-icon {
  width: 80px;
  height: 80px;

  margin: 0 auto 20px;

  border-radius: 50%;

  background: #ef4444;

  color: white;

  font-size: 45px;

  display: flex;
  justify-content: center;
  align-items: center;
}

h1 {
  color: #dc2626;

  margin-bottom: 15px;
}

.message {
  color: #555;

  line-height: 1.6;
}

.warning-box {
  margin-top: 25px;

  padding: 15px;

  background: #fff7ed;

  border-radius: 10px;

  text-align: left;

  color: #9a3412;
}

.warning-box p {
  margin: 8px 0;
}

.actions {
  display: flex;

  justify-content: center;

  gap: 15px;

  margin-top: 30px;
}

button {
  padding: 12px 22px;

  border: none;

  border-radius: 8px;

  cursor: pointer;

  font-size: 14px;
}

.btn-retry {
  background: #f59e0b;

  color: white;
}

.btn-home {
  background: #333;

  color: white;
}

button:hover {
  opacity: 0.85;
}
</style>
