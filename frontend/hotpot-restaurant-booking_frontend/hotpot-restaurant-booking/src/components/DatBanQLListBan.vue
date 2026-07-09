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
          'ban-da-dat': ban.trangThai === 'DA_DAT',
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
            {{ ban.trangThai === 'DA_DAT' ? 'ĐÃ ĐẶT BÀN' : ban.trangThai === 'DANG_SU_DUNG' ? 'ĐANG SỬ DỤNG' : 'TRỐNG' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ban-wrapper {
  padding: 12px;
  background: #fffdf8;
  border-radius: 12px;
  border: 1px solid #e6d2aa;
}

.ban-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.ban-card {
  background: #fff8ea;
  border: 1px solid #e6d2aa;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.ban-card:hover {
  border-color: #d8a85c;
  transform: translateY(-2px);
}

.ban-card.active {
  border-color: #d8a85c;
  box-shadow: 0 0 10px rgba(216, 168, 92, 0.25);
  transform: scale(1.02);
}

.ban-card.ban-dang-dung {
  background: linear-gradient(135deg, #facc15, #eab308);
  border: 1px solid #f59e0b;
  color: #1f2937;
  box-shadow: 0 10px 25px rgba(234, 179, 8, 0.2);
  transform: scale(1.02);
}

.ban-card.ban-dang-dung .ban-name,
.ban-card.ban-dang-dung .ban-type,
.ban-card.ban-dang-dung .ban-status {
  color: #1f2937;
  font-weight: 600;
}

.ban-card.ban-da-dat {
  background: linear-gradient(135deg, #f7b7a9, #e88b74);
  border: 1px solid #dd7f69;
  color: #5f3d22;
  box-shadow: 0 10px 25px rgba(232, 139, 116, 0.2);
  transform: scale(1.02);
}

.ban-card.ban-da-dat .ban-name,
.ban-card.ban-da-dat .ban-type,
.ban-card.ban-da-dat .ban-status {
  color: #5f3d22;
  font-weight: 600;
}

.ban-card.ban-trong {
  background: #fff8ea;
  border: 1px solid #e6d2aa;
}

.ban-name {
  font-weight: 600;
  color: #8b5e34;
}

.ban-type,
.ban-status {
  font-size: 12px;
  color: #8f6b46;
}
</style>
