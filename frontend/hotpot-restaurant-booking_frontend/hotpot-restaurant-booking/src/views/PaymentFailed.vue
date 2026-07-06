<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="payment-page">
    <h1>❌ Thanh toán thất bại</h1>

    <p>Giao dịch chưa được hoàn tất.</p>

    <button @click="goHome">Quay lại</button>
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
