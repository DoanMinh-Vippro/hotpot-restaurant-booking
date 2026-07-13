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

//map loại bàn
const formatLoaiBan = (loai: string) => {
  switch (loai) {
    case 'HAI_NGUOI':
      return '2 người'
    case 'BON_NGUOI':
      return '4 người'
    case 'SAU_NGUOI':
      return '6 người'
    default:
      return loai
  }
}

const formatTrangThaiBan = (trangThai: string) => {
  if (trangThai === 'BAO_TRI') {
    return 'Bảo trì'
  }

  return 'Hoạt động'
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

          <div class="ban-meta">
            <span class="ban-type">
              {{ formatLoaiBan(ban.loaiBan) }}
            </span>

            <div class="ban-status">
              {{ formatTrangThaiBan(ban.trangThai) }}
            </div>
          </div>
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
  padding: 16px;

  background: #fffdf8;

  border-radius: 16px;

  border: 1px solid #eee2cf;
}

.ban-grid {
  display: grid;

  grid-template-columns: repeat(4, 1fr);

  gap: 16px;
}

.ban-card {
  background: #fffaf3;

  border: 1px solid #eadfc9;

  border-radius: 16px;

  padding: 18px;

  display: flex;

  flex-direction: column;

  gap: 16px;

  transition: all 0.25s ease;

  cursor: pointer;

  box-shadow: 0 4px 12px rgba(150, 120, 80, 0.06);

  min-height: 140px;
}

.ban-card:hover {
  transform: translateY(-4px);

  border-color: #ddbd82;

  box-shadow: 0 10px 24px rgba(150, 120, 80, 0.12);
}

.ban-card.active {
  background: #f8edda;

  border-color: #d8b77a;

  box-shadow: 0 10px 24px rgba(216, 183, 122, 0.2);
}

.ban-info {
  display: flex;

  flex-direction: column;

  gap: 14px;

  flex: 1;
}

.ban-name {
  font-size: 20px;

  font-weight: 700;

  color: #725b3c;
}

.ban-meta {
  display: flex;

  align-items: center;

  gap: 8px;

  flex-wrap: wrap;
}

.ban-type,
.ban-status {
  display: inline-flex;

  align-items: center;

  justify-content: center;

  white-space: nowrap;

  padding: 5px 12px;

  border-radius: 999px;

  font-size: 13px;

  font-weight: 600;
}

.ban-type {
  background: #f8eedc;

  color: #87663a;
}

.ban-status {
  background: #edf5e9;

  color: #3f7d43;
}

.ban-actions {
  display: flex;

  justify-content: flex-end;

  width: 100%;
}

.btn-delete {
  background: #fff3ef;

  color: #c05a45;

  border: 1px solid #f0c9bd;

  border-radius: 10px;

  padding: 7px 16px;

  font-size: 13px;

  font-weight: 600;

  cursor: pointer;

  transition: all 0.25s ease;
}

.btn-delete:hover {
  background: #df7560;

  color: white;

  border-color: #df7560;

  transform: scale(1.05);
}

/* responsive */
@media (max-width: 1200px) {
  .ban-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .ban-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .ban-grid {
    grid-template-columns: 1fr;
  }
}
</style>
