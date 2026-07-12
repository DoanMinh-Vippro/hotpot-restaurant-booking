<script setup lang="ts">
import { ref, watch } from 'vue'
import BanApi from '@/api/BanApi'

/**
 * =========================
 * 1. PROPS (DỮ LIỆU TỪ CHA)
 * =========================
 * visible:
 *   - true => mở popup
 *   - false => đóng popup
 *
 * banSelected:
 *   - object => đang EDIT
 *   - null => đang ADD mới
 *
 * idKhuVuc:
 *   - khu vực mặc định khi tạo bàn mới
 */
const props = defineProps<{
  visible: boolean
  banSelected: any | null
  idKhuVuc: number | null
}>()

/**
 * =========================
 * 2. EMIT (GỬI NGƯỢC VỀ CHA)
 * =========================
 * close:
 *   - yêu cầu cha đóng popup
 *
 * success:
 *   - báo cha reload list sau khi add/update xong
 */
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

/**
 * =========================
 * 3. FORM STATE
 * =========================
 * dữ liệu nhập trong popup
 */
const form = ref({
  idBan: null as number | null,
  tenBan: '',
  loaiBan: '',
  idKhuVuc: props.idKhuVuc,
  trangThai: 'TRONG',
})

/**
 * =========================
 * 4. LOAD DATA KHI EDIT
 * =========================
 * Khi chọn 1 bàn từ list -> đổ data vào form
 */
watch(
  () => props.banSelected,
  (ban) => {
    if (ban) {
      form.value = {
        idBan: ban.idBan,
        tenBan: ban.tenBan,
        loaiBan: ban.loaiBan,
        idKhuVuc: ban.idKhuVuc,
        trangThai: ban.trangThai,
      }
    } else {
      resetForm()
    }
  },
  { immediate: true },
)

/**
 * =========================
 * 5. RESET FORM (DÙNG CHO ADD)
 * =========================
 * xóa sạch dữ liệu cũ
 */
const resetForm = () => {
  form.value = {
    idBan: null,
    tenBan: '',
    loaiBan: '',
    idKhuVuc: props.idKhuVuc,
    trangThai: 'TRONG',
  }
}

/**
 * =========================
 * 6. ADD MỚI
 * =========================
 */
const addBan = async () => {
  await BanApi.add(form.value)

  emit('success')
  emit('close')
}

/**
 * =========================
 * 7. UPDATE BÀN
 * =========================
 */
const updateBan = async () => {
  if (!form.value.idBan) return

  await BanApi.update(form.value.idBan, form.value)

  emit('success')
  emit('close')
}

/**
 * =========================
 * 8. NÚT CHUYỂN SANG ADD MODE
 * =========================
 * dùng khi đang edit mà muốn reset về add
 */
const switchToAdd = () => {
  resetForm()
}
</script>

<template>
  <!-- POPUP -->
  <div v-if="visible" class="overlay" @click.self="emit('close')">
    <div class="modal">
      <!-- TITLE -->
      <h3>
        {{ form.idBan ? 'Đang sửa bàn' : 'Thêm bàn mới' }}
      </h3>

      <!-- FORM -->
      <div class="form">
        <input v-model="form.tenBan" placeholder="Tên bàn" />

        <select v-model="form.loaiBan">
          <option value="HAI_NGUOI">2 Người</option>
          <option value="BON_NGUOI">4 Người</option>
          <option value="SAU_NGUOI">6 Người</option>
        </select>

        <select v-model="form.trangThai">
          <option value="TRONG">Trống</option>
          <option value="DANG_SU_DUNG">Đang dùng</option>
          <option value="DA_DAT">Đã đặt</option>
          <option value="BAO_TRI">Bảo trì</option>
        </select>
      </div>

      <!-- =========================
           9. BUTTONS TÁCH BẠCH
      ========================= -->
      <div class="actions">
        <!-- ADD BUTTON -->
        <button @click="addBan">Thêm mới</button>

        <!-- UPDATE BUTTON -->
        <button @click="updateBan" :disabled="!form.idBan">Cập nhật</button>

        <!-- RESET VỀ ADD -->
        <button @click="switchToAdd">Reset add</button>

        <!-- CLOSE -->
        <button @click="emit('close')">Huỷ</button>
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
}

.modal {
  width: 360px;
  background: #fffaf1;
  border: 1px solid #e6d2aa;
  border-radius: 12px;
  padding: 16px;
  color: #8b5e34;
  box-shadow: 0 16px 40px rgba(103, 72, 32, 0.16);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

input,
select {
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #e6d2aa;
  background: #fffdf8;
  color: #5f3d22;
}

.actions {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

button {
  padding: 6px 12px;
  border: 1px solid #e6d2aa;
  background: #fff3d3;
  color: #8b5e34;
  cursor: pointer;
  border-radius: 8px;
}
</style>
