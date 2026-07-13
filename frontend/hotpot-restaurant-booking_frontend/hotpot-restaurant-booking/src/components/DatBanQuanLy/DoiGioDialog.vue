<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  reservation: any | null
}>()

const emit = defineEmits(['close', 'save'])

const thoiGianMoi = ref('')

watch(
  () => props.reservation,
  (value) => {
    if (!value) {
      thoiGianMoi.value = ''
      return
    }

    if (value.thoiGianDenDuKien) {
      thoiGianMoi.value = value.thoiGianDenDuKien.substring(0, 16)
    }
  },
  {
    immediate: true,
  },
)

const close = () => {
  emit('close')
}

const save = () => {
  emit('save', {
    thoiGianMoi: thoiGianMoi.value + ':00',
  })
}

const formatDateTime = (value: string) => {
  if (!value) return '-'

  const date = new Date(value)

  if (isNaN(date.getTime())) {
    return value
  }

  const ngay = date.toLocaleDateString('vi-VN')
  const gio = date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  })

  return `${gio} • ${ngay}`
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="overlay" @click.self="close">
      <div class="dialog">
        <div class="dialog-header">
          <div>
            <h2>Đổi thời gian nhận bàn</h2>
            <p>Đơn #{{ reservation?.idDatBan }}</p>
          </div>

          <button class="close-btn" @click="close">✕</button>
        </div>

        <div class="content">
          <div class="info-grid">
            <div class="info-card">
              <label>Khách hàng</label>
              <strong>{{ reservation?.tenKhachHang }}</strong>
            </div>

            <div class="info-card">
              <label>Số điện thoại</label>
              <strong>{{ reservation?.sdtKhachHang }}</strong>
            </div>

            <div class="info-card">
              <label>Số người</label>
              <strong>{{ reservation?.soNguoi }}</strong>
            </div>
          </div>

          <div class="time-box">
            <label> Thời gian hiện tại </label>

            <div class="old-time">
              {{ formatDateTime(reservation?.thoiGianDenDuKien) }}
            </div>
          </div>

          <div class="time-box">
            <label> Thời gian mới </label>

            <input v-model="thoiGianMoi" type="datetime-local" />
          </div>
        </div>

        <div class="dialog-footer">
          <button class="btn cancel" @click="close">Huỷ</button>

          <button class="btn save" @click="save">Lưu thay đổi</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.dialog {
  width: 720px;
  max-width: 95vw;
  background: #f8f5ef;
  border-radius: 18px;
  border: 1px solid #e6d8bb;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid #e7dcc8;
}

.dialog-header h2 {
  margin: 0;
  color: #3b3124;
}

.dialog-header p {
  margin-top: 6px;
  color: #8c7a60;
}

.close-btn {
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 10px;
  background: #ece3d3;
  cursor: pointer;
  font-size: 18px;
  transition: 0.25s;
}

.close-btn:hover {
  background: #d7c8ad;
}

.content {
  padding: 28px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-bottom: 28px;
}

.info-card {
  background: white;
  border: 1px solid #ebe0cb;
  border-radius: 12px;
  padding: 18px;
}

.info-card label {
  display: block;
  font-size: 13px;
  color: #9b8c71;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.info-card strong {
  color: #2e2b27;
  font-size: 17px;
}

.time-box {
  margin-bottom: 24px;
}

.time-box label {
  display: block;
  margin-bottom: 8px;
  color: #6d5b3f;
  font-weight: 600;
}

.old-time {
  background: #fff;
  border: 1px solid #ebe0cb;
  border-radius: 10px;
  padding: 14px;
  color: #444;
}

.time-box input {
  width: 100%;
  padding: 14px;
  border-radius: 10px;
  border: 1px solid #d7c8ad;
  background: white;
  font-size: 15px;
  transition: 0.25s;
  box-sizing: border-box;
}

.time-box input:focus {
  outline: none;
  border-color: #c5a05a;
  box-shadow: 0 0 0 3px rgba(197, 160, 90, 0.18);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 22px 28px;
  border-top: 1px solid #e7dcc8;
}

.btn {
  border: none;
  border-radius: 10px;
  padding: 12px 24px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.25s;
}

.cancel {
  background: #d9d9d9;
}

.cancel:hover {
  background: #c5c5c5;
}

.save {
  background: #c6a15b;
  color: white;
}

.save:hover {
  background: #b48d45;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .dialog {
    width: 95%;
  }

  .dialog-footer {
    flex-wrap: wrap;
  }

  .btn {
    flex: 1;
  }
}
</style>
