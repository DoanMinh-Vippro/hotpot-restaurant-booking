<script setup lang="ts">
import { computed, ref } from 'vue'

/**
 * 1. DATA TỪ CHA COMPONENT
 * - danhSachBan: toàn bộ danh sách bàn
 * - idKhuVuc: tab khu vực đang chọn để lọc bàn
 */
const props = defineProps<{
  danhSachBan: any[]
  idKhuVuc: number | null
}>()

/**
 * 2. EMIT RA CHA
 * - delete: xóa bàn
 * - select: chọn bàn (click 1 lần)
 * - openDetail: mở popup BanDetail (double click)
 */
const emit = defineEmits<{
  (e: 'delete', id: number): void
  (e: 'select', ban: any): void
  (e: 'openDetail', ban: any | null): void
}>()

/**
 * 3. STATE LOCAL
 * - bàn đang được chọn để highlight UI
 */
const banDangChon = ref<any | null>(null)

/**
 * 4. FILTER BÀN THEO KHU VỰC
 * - chỉ show bàn thuộc tab hiện tại
 */
const danhSachBanHienThi = computed(() => {
  if (!props.idKhuVuc) return []

  return props.danhSachBan.filter((ban) => {
    return ban.idKhuVuc === props.idKhuVuc
  })
})

/**
 * 5. CLICK 1 LẦN = CHỌN BÀN
 * - highlight card
 * - báo cho cha biết bàn đang được chọn
 */
const chonBan = (ban: any) => {
  banDangChon.value = ban
  emit('select', ban)
}

/**
 * 6. DOUBLE CLICK = MỞ DETAIL
 * - mở mini UI chỉnh sửa bàn
 */
const moBanDetail = (ban: any) => {
  emit('openDetail', ban)
}
</script>

<template>
  <div class="ban-wrapper">
    <div class="ban-grid">
      <div
        v-for="ban in danhSachBanHienThi"
        :key="ban.idBan"
        class="ban-card"
        :class="{ active: ban.idBan === banDangChon?.idBan }"
        @click="chonBan(ban)"
        @dblclick="moBanDetail(ban)"
      >
        <!-- INFO -->
        <div class="ban-info">
          <div class="ban-name">{{ ban.tenBan }}</div>
          <div class="ban-type">{{ ban.loaiBan }}</div>
          <div class="ban-status">{{ ban.trangThai }}</div>
        </div>

        <!-- ACTION (chỉ delete) -->
        <div class="ban-actions" @click.stop>
          <button class="btn-delete" @click="emit('delete', ban.idBan)">Xóa</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ban-wrapper {
  padding: 12px;
  background: #0b0b0d;
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

/* 1. Chia 4 cột ở đây */
.ban-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

/* 2. Style cho mỗi ô bàn */
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

.ban-card.active {
  border-color: #ffd86b;
  box-shadow: 0 0 8px rgba(212, 175, 55, 0.3);
}

/* 3. Style text bên trong ô */
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

/* 4. Nút xóa nhỏ gọn */
.btn-delete {
  background: transparent;
  border: 1px solid rgba(255, 80, 80, 0.3);
  color: #ff6b6b;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  cursor: pointer;
}

.btn-delete:hover {
  background: #ff6b6b;
  color: white;
}
</style>
