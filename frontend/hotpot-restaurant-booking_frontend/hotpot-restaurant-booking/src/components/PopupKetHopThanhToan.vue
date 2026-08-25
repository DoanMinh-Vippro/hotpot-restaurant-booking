<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{
  tongTien: number
  isProcessing?: boolean
}>()

const emit = defineEmits(['close', 'xacNhan'])

const tienChuyenKhoan = ref(0)
const tienTienMat = ref(0)

const tongDaNhap = computed(() => Number(tienChuyenKhoan.value || 0) + Number(tienTienMat.value || 0))
const conThieu = computed(() => Number(props.tongTien || 0) - tongDaNhap.value)

const setQuickSplit = (ratio: number) => {
  const total = Number(props.tongTien || 0)
  const transfer = Math.round(total * ratio)
  const cash = total - transfer
  tienChuyenKhoan.value = transfer
  tienTienMat.value = cash
}

const xacNhan = () => {
  if (tienChuyenKhoan.value < 0 || tienTienMat.value < 0) {
    alert('Số tiền không hợp lệ')
    return
  }

  if (tongDaNhap.value !== Number(props.tongTien || 0)) {
    alert('Tổng số tiền chuyển khoản và tiền mặt phải bằng tổng thanh toán.')
    return
  }

  emit('xacNhan', {
    tienChuyenKhoan: Number(tienChuyenKhoan.value || 0),
    tienTienMat: Number(tienTienMat.value || 0),
  })
}
</script>

<template>
  <div class="popup-overlay">
    <div class="container-mixed">
      <h3>Thanh toán kết hợp</h3>

      <div class="money">{{ props.tongTien.toLocaleString('vi-VN') }} đ</div>

      <div class="split-row">
        <label>Chuyển khoản</label>
        <input v-model.number="tienChuyenKhoan" type="number" min="0" class="money-input" />
      </div>

      <div class="split-row">
        <label>Tiền mặt</label>
        <input v-model.number="tienTienMat" type="number" min="0" class="money-input" />
      </div>

      <div class="quick-actions">
        <button type="button" class="quick-btn" @click="setQuickSplit(0.5)">50% / 50%</button>
        <button type="button" class="quick-btn" @click="setQuickSplit(0.7)">70% / 30%</button>
        <button type="button" class="quick-btn" @click="setQuickSplit(0.3)">30% / 70%</button>
      </div>

      <div class="summary" :class="{ error: conThieu !== 0 }">
        <span>Đã nhập:</span>
        <strong>{{ tongDaNhap.toLocaleString('vi-VN') }} đ</strong>
      </div>

      <div v-if="conThieu !== 0" class="summary error">
        <span>Còn thiếu:</span>
        <strong>{{ Math.abs(conThieu).toLocaleString('vi-VN') }} đ</strong>
      </div>

      <div class="btn-row">
        <button class="btn-cancel" :disabled="props.isProcessing" @click="emit('close')">Hủy</button>
        <button class="btn-confirm" :disabled="props.isProcessing || conThieu !== 0" @click="xacNhan">
          {{ props.isProcessing ? 'Đang xử lý...' : 'Xác nhận' }}
        </button>
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

.container-mixed {
  width: 520px;
  background: linear-gradient(180deg, #2a2a2a, #1d1d1d);
  border-radius: 18px;
  padding: 28px;
  border: 1px solid rgba(124, 77, 255, 0.35);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.6);
}

h3 {
  text-align: center;
  color: #ffd86b;
  margin-bottom: 16px;
}

.money {
  text-align: center;
  color: #ffd86b;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 20px;
}

.split-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  color: white;
}

.money-input {
  width: 220px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 216, 107, 0.28);
  background: #111;
  color: #ffd86b;
  font-size: 16px;
  outline: none;
}

.quick-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin: 16px 0 18px;
  flex-wrap: wrap;
}

.quick-btn {
  border: none;
  background: #4a3b7a;
  color: white;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
}

.summary {
  display: flex;
  justify-content: space-between;
  color: #fff;
  margin-bottom: 8px;
}

.summary.error {
  color: #ff9a9a;
}

.btn-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.btn-cancel,
.btn-confirm {
  border: none;
  border-radius: 10px;
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
}

.btn-cancel {
  background: transparent;
  color: #d9d9d9;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.btn-confirm {
  background: linear-gradient(135deg, #7c4dff, #5d4de6);
  color: white;
}
</style>
