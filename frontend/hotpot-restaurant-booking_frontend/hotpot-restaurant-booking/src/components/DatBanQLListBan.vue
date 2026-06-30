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
          'ban-dang-dung': ban.trangThai === 'DANG_SU_DUNG',
          'ban-trong': ban.trangThai === 'TRONG',
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
   GRID
===================================================== */

.ban-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

/* =====================================================
   CARD BASE
===================================================== */

.ban-card {
  background: #0f0f11;
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 10px;
  padding: 12px;

  display: flex;
  flex-direction: column;

  gap: 6px;

  cursor: pointer;

  transition: all 0.25s ease;
}

/* hover thường */
.ban-card:hover {
  border-color: rgba(212, 175, 55, 0.5);
  transform: translateY(-2px);
}

/* =====================================================
   ACTIVE (đang click chọn)
===================================================== */

.ban-card.active {
  border-color: #ffd86b;
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.35);
  transform: scale(1.03);
}

/* =====================================================
   🟡 BÀN ĐANG DÙNG (GOLD)
===================================================== */

.ban-card.ban-dang-dung {
  background: linear-gradient(135deg, #facc15, #eab308);
  border: 1px solid #f59e0b;

  color: #1f2937;

  box-shadow:
    0 10px 25px rgba(234, 179, 8, 0.35),
    0 0 18px rgba(234, 179, 8, 0.25);

  transform: scale(1.02);
}

/* text trong bàn vàng */
.ban-card.ban-dang-dung .ban-name,
.ban-card.ban-dang-dung .ban-type,
.ban-card.ban-dang-dung .ban-status {
  color: #1f2937;
  font-weight: 600;
}

/* hover vàng */
.ban-card.ban-dang-dung:hover {
  transform: scale(1.06);
  box-shadow: 0 15px 35px rgba(234, 179, 8, 0.5);
}

/* =====================================================
   🟢 BÀN TRỐNG
===================================================== */

.ban-card.ban-trong {
  background: #0f0f11;
  border: 1px solid rgba(212, 175, 55, 0.12);
}

/* text bàn trống */
.ban-card.ban-trong .ban-name {
  color: #9ca3af;
}

.ban-card.ban-trong .ban-status {
  color: #6b7280;
}

/* =====================================================
   TEXT BASE
===================================================== */

.ban-name {
  font-weight: 600;
  color: #ffd86b;
}

.ban-type,
.ban-status {
  font-size: 12px;
  color: #aaa;
}
</style>
