<script setup lang="ts">
defineProps<{
  show: boolean
  message?: string
}>()

const emit = defineEmits(['close'])
</script>

<template>
  <Transition name="fade">
    <div v-if="show" class="overlay">
      <div class="dialog">
        <div class="icon">✓</div>

        <h2>Đặt bàn thành công!</h2>

        <p>
          {{ message || 'Nhà hàng đã ghi nhận thông tin đặt bàn của bạn.' }}
        </p>

        <button @click="emit('close')">Xác nhận</button>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.dialog {
  width: 380px;
  background: white;
  border-radius: 20px;
  padding: 35px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  animation: scale 0.25s ease;
}

.icon {
  width: 70px;
  height: 70px;
  margin: auto;
  border-radius: 50%;
  background: #22c55e;
  color: white;
  font-size: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

h2 {
  margin-top: 20px;
  font-size: 24px;
  color: #16a34a;
}

p {
  color: #666;
  margin: 15px 0 25px;
}

button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 12px;
  background: #16a34a;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: 0.2s;
}

button:hover {
  background: #15803d;
}

.fade-enter-active,
.fade-leave-active {
  transition: 0.25s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes scale {
  from {
    transform: scale(0.8);
  }

  to {
    transform: scale(1);
  }
}
</style>
