<script setup lang="ts">
/**
 * =====================================================
 * POPUP KIỂM TRA ĐƠN ĐẶT BÀN
 * =====================================================
 * Chức năng:
 * - Hiện khi nhân viên double click vào bàn
 * - Hỏi khách đã có đơn đặt bàn trước hay chưa
 * - Nếu "Có"  -> mở danh sách đơn đã xác nhận
 * - Nếu "Không" -> tạo luồng nhận bàn trực tiếp
 * =====================================================
 */

/**
 * DATA TỪ COMPONENT CHA (BanHang.vue)
 *
 * ban:
 * Bàn vừa được double click
 */
const props = defineProps<{
  ban: any
}>()

/**
 * EMIT SỰ KIỆN RA CHA
 *
 * close:
 * Đóng popup
 */
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'coDonDatBan'): void //mở popup dánh sách đơn đặt bàn ở trạng thái Đã xác nhận
  (e: 'khongCoDonDatBan'): void // luồng gọi báo cha khi khách bấm không
}>()

/**
 * Đóng popup
 */
const dongPopup = () => {
  emit('close')
}

/**
 * Khách đã có đơn đặt bàn
 *
 * TODO:
 * Sau này sẽ mở popup chọn đơn đặt bàn
 */
const daCoDonDatBan = () => {
  emit('coDonDatBan')
}

/**
 * Khách chưa có đơn đặt bàn
 *
 * TODO:
 * Sau này sẽ tạo DatBan mới
 * rồi chuyển sang màn hình order món
 */
const chuaCoDonDatBan = () => {
  emit('khongCoDonDatBan')
}
</script>

<template>
  <!--
    Overlay nền tối phía sau popup

    Click ra ngoài popup => đóng popup
  -->
  <div class="overlay" @click="dongPopup">
    <!--
      Khung popup chính
      stopPropagation:
      tránh click trong popup bị đóng
    -->
    <div class="popup" @click.stop>
      <!-- HEADER -->
      <div class="popup-header">
        <!-- Tên bàn đang chọn -->
        {{ props.ban?.tenBan }}
      </div>

      <!-- BODY -->
      <div class="popup-body">
        <p class="question">Bạn đã có đơn đặt bàn?</p>
      </div>

      <!-- FOOTER -->
      <div class="popup-footer">
        <!-- Đã có đơn -->
        <button class="btn btn-yes" @click="daCoDonDatBan">Có</button>

        <!-- Chưa có đơn -->
        <button class="btn btn-no" @click="chuaCoDonDatBan">Không</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* =====================================================
   OVERLAY
===================================================== */

.overlay {
  position: fixed;
  inset: 0;

  background: rgba(0, 0, 0, 0.6);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 999;
}

/* =====================================================
   POPUP
===================================================== */

.popup {
  width: 400px;

  background: #151515;

  border-radius: 12px;

  overflow: hidden;

  border: 1px solid rgba(212, 175, 55, 0.3);

  box-shadow: 0 0 25px rgba(0, 0, 0, 0.5);
}

/* =====================================================
   HEADER
===================================================== */

.popup-header {
  padding: 16px;

  text-align: center;

  font-size: 20px;
  font-weight: 600;

  color: #ffd86b;

  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* =====================================================
   BODY
===================================================== */

.popup-body {
  padding: 32px;

  text-align: center;
}

.question {
  color: white;

  font-size: 16px;

  margin: 0;
}

/* =====================================================
   FOOTER
===================================================== */

.popup-footer {
  padding: 20px;

  display: flex;
  justify-content: center;

  gap: 16px;
}

/* =====================================================
   BUTTON CHUNG
===================================================== */

.btn {
  min-width: 120px;

  padding: 10px 20px;

  border: none;

  border-radius: 8px;

  cursor: pointer;

  font-weight: 600;

  transition: 0.3s;
}

/* =====================================================
   BUTTON CÓ
===================================================== */

.btn-yes {
  background: #ffd86b;
}

.btn-yes:hover {
  transform: translateY(-2px);
}

/* =====================================================
   BUTTON KHÔNG
===================================================== */

.btn-no {
  background: #444;

  color: white;
}

.btn-no:hover {
  background: #555;

  transform: translateY(-2px);
}
</style>
