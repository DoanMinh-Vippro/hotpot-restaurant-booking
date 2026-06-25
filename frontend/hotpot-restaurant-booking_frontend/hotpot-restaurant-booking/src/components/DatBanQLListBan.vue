<script setup lang="ts">
import { computed, ref } from 'vue'

/**
 * DATA TỪ COMPONENT CHA
 *
 * danhSachBan:
 * Toàn bộ danh sách bàn
 *
 * idKhuVuc:
 * Khu vực đang được chọn trên tab
 */
const props = defineProps<{
  danhSachBan: any[]
  idKhuVuc: number | null
}>()

/**
 * EMIT RA COMPONENT CHA
 *
 * select:
 * Chọn bàn
 *
 * openDetail:
 * Double click bàn
 * Mở DatBanPopupCheck
 */
const emit = defineEmits<{
  (e: 'select', ban: any): void
  (e: 'openDetail', ban: any | null): void
}>()

/**
 * STATE LOCAL
 *
 * Lưu bàn đang được chọn
 * để highlight UI
 */
const banDangChon = ref<any | null>(null)

/**
 * LỌC DANH SÁCH BÀN
 *
 * Chỉ hiển thị bàn thuộc
 * khu vực hiện tại
 */
const danhSachBanHienThi = computed(() => {
  if (!props.idKhuVuc) return []

  return props.danhSachBan.filter((ban) => {
    return ban.idKhuVuc === props.idKhuVuc
  })
})

/**
 * CLICK 1 LẦN
 *
 * - Highlight bàn
 * - Báo cho component cha
 * biết bàn nào đang được chọn
 */
const chonBan = (ban: any) => {
  banDangChon.value = ban

  emit('select', ban)
}

/**
 * DOUBLE CLICK
 *
 * Mở popup kiểm tra:
 * "Đã có đơn đặt bàn chưa?"
 */
const moBanDetail = (ban: any) => {
  emit('openDetail', ban)
}
</script>

<template>
  <div class="ban-wrapper">
    <div class="ban-grid">
      <!-- DANH SÁCH BÀN -->
      <div
        v-for="ban in danhSachBanHienThi"
        :key="ban.idBan"
        class="ban-card"
        :class="{
          active: ban.idBan === banDangChon?.idBan,
          'dang-co-khach': ban.coHoaDon,
        }"
        @click="chonBan(ban)"
        @dblclick="moBanDetail(ban)"
      >
        <div class="ban-info">
          <!-- TÊN BÀN -->
          <div class="ban-name">
            {{ ban.tenBan }}
          </div>

          <!-- LOẠI BÀN -->
          <div class="ban-type">
            {{ ban.loaiBan }}
          </div>

          <!-- TRẠNG THÁI -->
          <div class="ban-status">
            {{ ban.trangThai }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* =====================================================
   WRAPPER
===================================================== */

.ban-wrapper {
  padding: 12px;

  background: #0b0b0d;

  border-radius: 12px;

  border: 1px solid rgba(212, 175, 55, 0.2);
}

/* =====================================================
   GRID BÀN
===================================================== */

.ban-grid {
  display: grid;

  grid-template-columns: repeat(4, 1fr);

  gap: 12px;
}

/* =====================================================
   CARD BÀN
===================================================== */

.ban-card {
  background: #0f0f11;

  border: 1px solid rgba(212, 175, 55, 0.15);

  border-radius: 10px;

  padding: 12px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  transition: 0.3s;

  cursor: pointer;
}

.ban-card:hover {
  border-color: rgba(212, 175, 55, 0.5);
}

/* =====================================================
   BÀN ĐANG CHỌN
===================================================== */

.ban-card.active {
  border-color: #ffd86b;

  box-shadow: 0 0 8px rgba(212, 175, 55, 0.3);
}

/* =====================================================
   THÔNG TIN BÀN
===================================================== */

.ban-info {
  display: flex;

  flex-direction: column;

  gap: 4px;
}

.ban-name {
  color: #ffd86b;

  font-weight: 600;
}

.ban-type,
.ban-status {
  color: #aaa;

  font-size: 12px;
}
/* =====================================================
   BÀN ĐANG CÓ KHÁCH
===================================================== */

.ban-card.dang-co-khach {
  border-color: #00ff88;

  background: linear-gradient(145deg, #123d2a, #0f0f11);

  box-shadow:
    0 0 12px rgba(0, 255, 136, 0.5),
    0 0 25px rgba(0, 255, 136, 0.25);
}

.ban-card.dang-co-khach .ban-status {
  color: #00ff88;
  font-weight: 700;
}
</style>
