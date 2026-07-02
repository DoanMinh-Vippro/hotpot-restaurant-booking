<script setup lang="ts">
import { ref, watch } from 'vue'

type KhuVuc = {
  idKhuVuc: number
  tenKhuVuc: string
}

const props = defineProps<{
  listKhuVuc: KhuVuc[]
}>()

const emit = defineEmits<{
  (e: 'change', idKhuVuc: number): void
  (e: 'add'): void
}>()

const themBan = () => {
  emit('add')
}

// tab đang được chọn (id khu vực)
const tabDangChon = ref<number | null>(null)

/**
 * Khi danh sách khu vực load lên:
 * - tự động chọn tab đầu tiên nếu chưa có tab nào được chọn
 */
watch(
  () => props.listKhuVuc,
  (danhSachKhuVucMoi) => {
    if (danhSachKhuVucMoi?.length && tabDangChon.value === null) {
      const firstKhuVuc = danhSachKhuVucMoi[0]
      if (firstKhuVuc?.idKhuVuc != null) {
        tabDangChon.value = firstKhuVuc.idKhuVuc
        emit('change', tabDangChon.value)
      }
    }
  },
  { immediate: true, deep: true },
)

/**
 * Khi user bấm chọn tab khác
 */
const chonTab = (idKhuVuc: number) => {
  tabDangChon.value = idKhuVuc
  emit('change', idKhuVuc)
}
</script>

<template>
  <div class="tab-wrapper">
    <!-- Thanh tab khu vực -->
    <div class="tab-header">
      <div class="tab-list">
        <div
          v-for="khuVuc in listKhuVuc"
          :key="khuVuc.idKhuVuc"
          class="tab-item"
          :class="{ active: tabDangChon === khuVuc.idKhuVuc }"
          @click="chonTab(khuVuc.idKhuVuc)"
        >
          {{ khuVuc.tenKhuVuc }}
        </div>
      </div>

      <button class="btn-add" @click="themBan">+ Thêm bàn</button>
    </div>

    <!-- Nội dung tab (slot để BanList nhét vào) -->
    <div class="tab-content">
      <slot :idKhuVuc="tabDangChon"></slot>
    </div>
  </div>
</template>

<style scoped>
.tab-wrapper {
  background: #0b0b0d;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.25);
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.08);
  backdrop-filter: blur(10px);
}

/* Header */
.tab-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  border-bottom: 1px solid rgba(212, 175, 55, 0.25);
  padding-bottom: 12px;
  margin-bottom: 14px;
}

/* Tab */
.tab-item {
  padding: 10px 18px;

  cursor: pointer;

  border-radius: 10px;

  background: linear-gradient(145deg, #141416, #0e0e10);

  color: #c9c9c9;
  font-weight: 600;
  letter-spacing: 0.4px;

  border: 1px solid rgba(212, 175, 55, 0.15);

  transition: all 0.25s ease;
}

.tab-item:hover {
  color: #ffd86b;
  border-color: rgba(212, 175, 55, 0.45);
  transform: translateY(-1px);
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.18);
}

.tab-item.active {
  background: linear-gradient(145deg, #1b1b1f, #111214);
  color: #ffd86b;

  border-color: rgba(212, 175, 55, 0.75);

  box-shadow: 0 0 14px rgba(212, 175, 55, 0.25);
}

/* Nút thêm */
.btn-add {
  flex-shrink: 0;

  display: flex;
  align-items: center;
  gap: 8px;

  padding: 11px 18px;

  border: none;
  border-radius: 10px;

  background: linear-gradient(135deg, #d4af37, #f0cd63);
  color: #111;

  font-weight: 700;

  cursor: pointer;

  transition: 0.25s;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(212, 175, 55, 0.35);
}

.btn-add:active {
  transform: scale(0.98);
}

/* Nội dung */
.tab-content {
  background: #0f0f11;

  border-radius: 10px;

  padding: 14px;

  border: 1px solid rgba(212, 175, 55, 0.15);
}
.tab-list {
  display: flex;
  gap: 10px;
  flex: 1;

  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
}

.tab-list::-webkit-scrollbar {
  height: 6px;
}

.tab-list::-webkit-scrollbar-thumb {
  background: rgba(212, 175, 55, 0.35);
  border-radius: 20px;
}
</style>
