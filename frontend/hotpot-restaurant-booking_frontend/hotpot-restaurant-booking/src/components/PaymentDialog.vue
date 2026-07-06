<script setup lang="ts">
const props = defineProps<{
  show: boolean
  qrUrl: string
  amount: number
  content: string
}>()

const emit = defineEmits(['close'])
</script>

<template>
  <div v-if="show" class="overlay">
    <div class="dialog">
      <h2>Thanh toán tiền cọc</h2>

      <img :src="qrUrl" alt="QR Code" class="qr" />

      <p><b>Số tiền:</b> {{ amount.toLocaleString() }} VNĐ</p>

      <p><b>Nội dung:</b> {{ content }}</p>

      <p class="waiting">Đang chờ khách hàng thanh toán...</p>

      <button @click="emit('close')">Đóng</button>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 9999;
}

.dialog {
  width: 420px;
  background: white;
  padding: 25px;
  border-radius: 10px;
  text-align: center;
}

.qr {
  width: 260px;
  margin: 20px auto;
  display: block;
}

.waiting {
  color: #ff9800;
  margin: 20px 0;
}
</style>
