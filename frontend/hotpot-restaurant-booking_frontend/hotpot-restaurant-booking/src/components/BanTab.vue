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
}>()

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
      tabDangChon.value = danhSachKhuVucMoi[0].idKhuVuc
      emit('change', tabDangChon.value)
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
}

/* Thanh tab */
.tab-header {
  display: flex;
  gap: 10px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.25);
  padding-bottom: 10px;
  margin-bottom: 12px;
}

/* Tab item */
.tab-item {
  padding: 10px 16px;
  cursor: pointer;
  border-radius: 8px;
  background: linear-gradient(145deg, #141416, #0e0e10);
  color: #c9c9c9;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all 0.25s ease;

  border: 1px solid rgba(212, 175, 55, 0.15);
}

/* Hover cho có cảm giác "đắt tiền" */
.tab-item:hover {
  color: #ffd86b;
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.15);
  transform: translateY(-1px);
}

/* Tab đang active */
.tab-item.active {
  background: linear-gradient(145deg, #1a1a1d, #0f0f11);
  color: #ffd86b;
  border: 1px solid rgba(212, 175, 55, 0.7);
  box-shadow: 0 0 14px rgba(212, 175, 55, 0.25);
}

/* Nội dung */
.tab-content {
  background: #0f0f11;
  border-radius: 10px;
  padding: 14px;
  border: 1px solid rgba(212, 175, 55, 0.15);
}
.tab-wrapper {
  backdrop-filter: blur(10px);
}
</style>
