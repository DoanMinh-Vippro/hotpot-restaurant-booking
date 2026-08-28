<script setup lang="ts">
defineProps<{
  show: boolean
  message?: string
  email?: string
}>()

const emit = defineEmits(['close', 'view-history'])
</script>

<template>
  <Transition name="fade">
    <div v-if="show" class="overlay">
      <div class="dialog">
        <div class="icon">✓</div>

        <h2>Tạo đơn đặt bàn thành công!</h2>
        <p>
          {{
            message || 'Nhà hàng đã ghi nhận đơn đặt bàn của bạn. Vui lòng chờ nhà hàng xác nhận.'
          }}
        </p>

        <!-- Khối lưu ý kiểm tra email/spam -->
        <div class="email-note">
          <div class="note-title">
            <span>📧</span> Thông báo qua Email
          </div>
          <p class="note-content">
            Thông tin đơn hàng sẽ được gửi tới <strong v-if="email">{{ email }}</strong><span v-else>email của bạn</span>. 
            Vui lòng kiểm tra hộp thư (bao gồm cả mục <strong>Spam / Thư rác</strong>) để cập nhật trạng thái mới nhất!
          </p>
        </div>

        <div class="actions">
          <button class="secondary-button" @click="emit('close')">Đóng</button>
          <button @click="emit('view-history')">Xem lịch sử đặt bàn</button>
        </div>
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
  width: 400px;
  background: white;
  border-radius: 20px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  animation: scale 0.25s ease;
}

.icon {
  width: 65px;
  height: 65px;
  margin: auto;
  border-radius: 50%;
  background: #22c55e;
  color: white;
  font-size: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

h2 {
  margin-top: 15px;
  font-size: 22px;
  color: #16a34a;
}

p {
  color: #555;
  margin: 12px 0 0;
  font-size: 14.5px;
  line-height: 1.5;
}

/* Style cho khối lưu ý Email */
.email-note {
  margin: 20px 0;
  padding: 12px 14px;
  background: #f0fdf4;
  border: 1px dashed #86efac;
  border-radius: 12px;
  text-align: left;
}

.note-title {
  font-weight: 600;
  font-size: 13.5px;
  color: #15803d;
  display: flex;
  align-items: center;
  gap: 6px;
}

.note-content {
  margin: 6px 0 0;
  font-size: 13px;
  color: #374151;
  line-height: 1.45;
}

.note-content strong {
  color: #166534;
}

button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 12px;
  background: #16a34a;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.secondary-button {
  background: #e5e7eb;
  color: #374151;
}

.secondary-button:hover {
  background: #d1d5db;
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
    transform: scale(0.85);
  }

  to {
    transform: scale(1);
  }
}
</style>