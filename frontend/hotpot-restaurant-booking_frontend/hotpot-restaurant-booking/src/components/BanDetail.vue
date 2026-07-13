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

  background: rgba(120, 100, 70, 0.28);

  display: flex;

  justify-content: center;

  align-items: center;

  z-index: 999;
}

.modal {
  width: 420px;

  max-width: 90vw;

  background: #fffdf8;

  border: 1px solid #eadfc9;

  border-radius: 18px;

  padding: 24px;

  color: #756044;

  box-shadow: 0 20px 50px rgba(120, 100, 70, 0.15);
}

.modal h2 {
  margin: 0 0 18px;

  color: #6f5a3a;

  font-size: 22px;

  font-weight: 700;
}

.form {
  display: flex;

  flex-direction: column;

  gap: 14px;

  margin-top: 12px;
}

input,
select {
  width: 100%;

  box-sizing: border-box;

  padding: 12px 14px;

  border-radius: 12px;

  border: 1px solid #e6d8bc;

  background: #fffaf2;

  color: #6b5738;

  font-size: 14px;

  transition: all 0.25s ease;
}

input:focus,
select:focus {
  outline: none;

  border-color: #d8b77a;

  box-shadow: 0 0 0 3px rgba(216, 183, 122, 0.18);
}

.actions {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  margin-top: 22px;
}

button {
  padding: 10px 18px;

  border-radius: 10px;

  border: none;

  background: #d8b77a;

  color: white;

  font-size: 14px;

  font-weight: 600;

  cursor: pointer;

  transition: all 0.25s ease;
}

button:hover {
  background: #caa466;

  transform: translateY(-2px);
}

button:first-child {
  background: #eee6d8;

  color: #756044;
}

button:first-child:hover {
  background: #e1d5c1;
}
</style>
