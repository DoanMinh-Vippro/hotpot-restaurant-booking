<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="payment-page">
    <h1>🎉 Thanh toán thành công</h1>

    <p>Đơn đặt bàn của bạn đã được ghi nhận.</p>

    <button @click="goHome">Về trang chủ</button>
  </div>
</template>

<style scoped>
.payment-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  gap: 20px;
}

button {
  padding: 10px 24px;
  cursor: pointer;
}
</style>
