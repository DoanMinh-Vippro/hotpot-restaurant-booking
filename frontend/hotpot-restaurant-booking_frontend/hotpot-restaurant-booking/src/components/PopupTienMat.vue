<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  tongTien: number
  isProcessing?: boolean
}>()

const tienNhan = ref(0)

const tienThua = computed(() => {
  return Math.max(0, tienNhan.value - props.tongTien)
})

const emit = defineEmits(['close', 'xacNhan'])

const huy = () => {
  emit('close')
}

const xacNhan = () => {
  if (tienNhan.value < props.tongTien) {
    alert('Khách đưa chưa đủ tiền')
    return
  }

  emit('xacNhan')
}
</script>

<template>
  <div class="popup-overlay">
    <div class="container-tienmat">
      <h3 class="title">Thanh Toán Tiền Mặt</h3>

      <div class="thong-tin">
        <div class="row">
          <label>Tiền khách đưa</label>

          <input
            v-model.number="tienNhan"
            type="number"
            class="input-money"
            placeholder="Nhập số tiền..."
            :disabled="props.isProcessing"
          />
        </div>

        <div class="row">
          <label>Tổng thanh toán</label>

          <div class="money">{{ props.tongTien.toLocaleString('vi-VN') }} đ</div>
        </div>

        <div class="row">
          <label>Tiền thừa</label>

          <div class="money">{{ tienThua.toLocaleString('vi-VN') }} đ</div>
        </div>
      </div>

      <div class="btn">
        <button class="btn-cancel" :disabled="props.isProcessing" @click="huy">Hủy</button>

        <button class="btn-confirm" :disabled="props.isProcessing" @click="xacNhan">{{ props.isProcessing ? 'Đang xử lý...' : 'Thanh toán' }}</button>      </div>
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

.container-tienmat {
  width: 520px;

  background: linear-gradient(180deg, #2a2a2a, #1d1d1d);

  border-radius: 18px;

  padding: 28px;

  border: 1px solid rgba(255, 216, 107, 0.25);

  box-shadow:
    0 15px 40px rgba(0, 0, 0, 0.6),
    0 0 20px rgba(255, 216, 107, 0.1);

  animation: popupShow 0.25s ease;
}

@keyframes popupShow {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.97);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.title {
  text-align: center;

  color: #ffd86b;

  font-size: 24px;

  font-weight: bold;

  margin-bottom: 25px;
}

.thong-tin {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.row label {
  color: white;
  font-size: 15px;
}

.input-money {
  width: 220px;

  padding: 12px 14px;

  border-radius: 10px;

  border: 1px solid rgba(255, 216, 107, 0.25);

  background: #111;

  color: #ffd86b;

  font-size: 16px;

  outline: none;

  transition: all 0.25s ease;
}

.input-money:focus {
  border-color: #ffd86b;

  box-shadow: 0 0 12px rgba(255, 216, 107, 0.35);
}

.money {
  min-width: 220px;

  text-align: right;

  font-size: 18px;

  font-weight: 700;

  color: #ffd86b;
}

.btn {
  margin-top: 25px;

  display: flex;

  gap: 12px;
}

.btn button {
  flex: 1;

  padding: 14px;

  border: none;

  border-radius: 12px;

  font-size: 15px;

  font-weight: bold;

  cursor: pointer;

  transition: all 0.25s ease;
}

.btn-cancel {
  background: #3a3a3a;

  color: white;
}

.btn-cancel:hover {
  background: #4a4a4a;

  transform: translateY(-2px);
}

.btn-cancel:disabled,
.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-confirm {
  background: linear-gradient(135deg, #ffd86b, #d4af37);

  color: #111;
}

.btn-confirm:hover {
  transform: translateY(-2px);

  box-shadow: 0 0 15px rgba(255, 216, 107, 0.4);
}
</style>
