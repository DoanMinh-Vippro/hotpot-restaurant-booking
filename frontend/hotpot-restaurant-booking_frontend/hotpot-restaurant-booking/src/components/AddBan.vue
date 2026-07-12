<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getAllKhuVuc } from '@/api/khuvuc'
import BanApi from '@/api/BanApi'

const emit = defineEmits(['close', 'success'])

const danhSachKhuVuc = ref<any[]>([])

const formData = ref({
  idKhuVuc: null as number | null,
  tenBan: '',
  loaiBan: 'THUONG',
})

/**
 * Load danh sách khu vực
 */
const loadKhuVuc = async () => {
  try {
    const res = await getAllKhuVuc()

    danhSachKhuVuc.value = res.data

    if (danhSachKhuVuc.value.length > 0) {
      formData.value.idKhuVuc = danhSachKhuVuc.value[0].idKhuVuc
    }
  } catch (e) {
    console.log('Lỗi load khu vực', e)
  }
}

/**
 * Đóng popup
 */
const dong = () => {
  emit('close')
}

/**
 * Validate
 */
const validate = () => {
  if (!formData.value.idKhuVuc) {
    alert('Vui lòng chọn khu vực')
    return false
  }

  if (!formData.value.loaiBan) {
    alert('Vui lòng chọn loại bàn')
    return false
  }

  return true
}

/**
 * Thêm bàn
 */
const themBan = async () => {
  if (!validate()) return

  const payload = {
    tenBan: formData.value.tenBan,
    idKhuVuc: formData.value.idKhuVuc,
    loaiBan: formData.value.loaiBan,
    trangThai: 'TRONG',
  }

  try {
    await BanApi.add(payload)

    alert('Thêm bàn thành công')

    emit('success')
  } catch (e) {
    console.log(e)
    alert('Thêm bàn thất bại')
  }
}

onMounted(() => {
  loadKhuVuc()
})
</script>

<template>
  <div class="overlay">
    <div class="popup">
      <div class="popup-header">
        <h2>Thêm bàn</h2>

        <button class="btn-close" @click="dong">✕</button>
      </div>

      <div class="popup-body">
        <div class="form-group">
          <label>Khu vực</label>

          <select v-model="formData.idKhuVuc">
            <option :value="null">-- Chọn khu vực --</option>

            <option v-for="kv in danhSachKhuVuc" :key="kv.idKhuVuc" :value="kv.idKhuVuc">
              {{ kv.tenKhuVuc }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Tên bàn</label>

          <input
            v-model.trim="formData.tenBan"
            type="text"
            maxlength="50"
            placeholder="Tên Bàn Tự Động Sinh"
            readonly
          />
        </div>

        <div class="form-group">
          <label>Loại bàn</label>

          <select v-model="formData.loaiBan">
            <option value="HAI_NGUOI">2 Người</option>
            <option value="BON_NGUOI">4 Người</option>
            <option value="SAU_NGUOI">6 Người</option>
          </select>
        </div>
      </div>

      <div class="popup-footer">
        <button class="btn btn-cancel" @click="dong">Hủy</button>

        <button class="btn btn-save" @click="themBan">Thêm bàn</button>
      </div>
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

.popup {
  width: 500px;
  max-width: 95%;
  background: #fffaf1;
  border-radius: 14px;
  border: 1px solid #e6d2aa;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(103, 72, 32, 0.16);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
  border-bottom: 1px solid #e6d2aa;
}

.popup-header h2 {
  margin: 0;
  color: #8b5e34;
}

.btn-close {
  background: transparent;
  border: none;
  color: #8f6b46;
  font-size: 22px;
  cursor: pointer;
}

.btn-close:hover {
  color: #5f3d22;
}

.popup-body {
  padding: 22px;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 18px;
}

.form-group label {
  color: #8b5e34;
  margin-bottom: 8px;
  font-weight: 600;
}

.form-group input,
.form-group select {
  padding: 11px 12px;
  background: #fffdf8;
  color: #5f3d22;
  border: 1px solid #e6d2aa;
  border-radius: 8px;
  outline: none;
  transition: 0.25s;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #d8a85c;
  box-shadow: 0 0 10px rgba(216, 168, 92, 0.12);
}

.popup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 18px 22px;
  border-top: 1px solid #e6d2aa;
}

.btn {
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.25s;
}

.btn-cancel {
  background: #fff3d3;
  color: #8b5e34;
}

.btn-save {
  background: #d8a85c;
  color: #3d2814;
}

.btn-save:hover {
  background: #e1bb6b;
}
</style>
