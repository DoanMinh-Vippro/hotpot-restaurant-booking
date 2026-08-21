<script setup lang="ts">
const props = defineProps<{
  tongTien: number
  isProcessing?: boolean
}>()

const emit = defineEmits(['close', 'chonTienMat', 'chonChuyenKhoan', 'chonKetHop'])

const chonTienMat = () => {
  emit('chonTienMat')
}

const chonChuyenKhoan = () => {
  emit('chonChuyenKhoan')
}

const chonKetHop = () => {
  emit('chonKetHop')
}

const dongPopup = () => {
  emit('close')
}
</script>

<template>
  <div class="popup-overlay">
    <div class="container">
      <h3>Chọn hình thức thanh toán</h3>

      <div class="money">{{ props.tongTien.toLocaleString('vi-VN') }} đ</div>

      <div class="option">
        <button class="btn-transfer" :disabled="props.isProcessing" @click="chonChuyenKhoan">Chuyển khoản</button>

        <button class="btn-cash" :disabled="props.isProcessing" @click="chonTienMat">Tiền mặt</button>

        <button class="btn-mixed" :disabled="props.isProcessing" @click="chonKetHop">Kết hợp</button>

        <button class="btn-close" :disabled="props.isProcessing" @click="dongPopup">Đóng</button>
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

.container {
  width: 420px;

  background: linear-gradient(180deg, #2a2a2a, #1d1d1d);

  border-radius: 16px;

  padding: 24px;

  border: 1px solid rgba(255, 216, 107, 0.2);

  box-shadow:
    0 10px 30px rgba(0, 0, 0, 0.5),
    0 0 15px rgba(255, 216, 107, 0.08);
}

h3 {
  color: #ffd86b;

  text-align: center;

  margin-bottom: 12px;

  font-size: 22px;
}

.money {
  text-align: center;

  color: #ffd86b;

  font-size: 28px;

  font-weight: 700;

  margin-bottom: 24px;
}

.option {
  display: flex;

  flex-direction: column;

  gap: 14px;
}

.option button {
  padding: 16px;

  border: none;

  border-radius: 12px;

  font-size: 16px;

  font-weight: 700;

  cursor: pointer;

  transition: all 0.25s ease;
}

.btn-transfer {
  background: linear-gradient(135deg, #ffd86b, #d4af37);

  color: #111;
}

.btn-transfer:hover {
  transform: translateY(-2px);

  box-shadow: 0 0 15px rgba(255, 216, 107, 0.35);
}

.btn-cash {
  background: #3a3a3a;

  color: white;

  border: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-cash:hover {
  background: #4a4a4a;

  transform: translateY(-2px);
}

.btn-mixed {
  background: linear-gradient(135deg, #7c4dff, #5d4de6);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.btn-mixed:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 15px rgba(124, 77, 255, 0.35);
}

.btn-close {
  background: transparent;

  color: #bdbdbd;

  border: 1px solid rgba(255, 255, 255, 0.15);
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.05);
}

.option button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style>
