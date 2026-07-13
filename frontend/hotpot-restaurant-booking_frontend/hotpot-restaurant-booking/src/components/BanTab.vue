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
  background: #fffdf8;

  padding: 16px;

  border-radius: 18px;

  border: 1px solid #eee2cf;

  box-shadow: 0 10px 28px rgba(150, 120, 80, 0.06);

  backdrop-filter: blur(10px);
}

/* HEADER */
.tab-header {
  display: flex;

  align-items: center;

  justify-content: space-between;

  gap: 16px;

  padding-bottom: 14px;

  margin-bottom: 16px;

  border-bottom: 1px solid #eee2cf;
}

/* DANH SÁCH TAB */
.tab-list {
  display: flex;

  align-items: center;

  gap: 12px;

  flex: 1;

  overflow-x: auto;

  overflow-y: hidden;

  scrollbar-width: thin;
}

.tab-list::-webkit-scrollbar {
  height: 6px;
}

.tab-list::-webkit-scrollbar-thumb {
  background: #dfc99f;

  border-radius: 20px;
}

/* TAB ITEM */
.tab-item {
  flex-shrink: 0;

  padding: 11px 20px;

  cursor: pointer;

  border-radius: 999px;

  background: #fffaf3;

  color: #806746;

  font-weight: 700;

  letter-spacing: 0.3px;

  border: 1px solid #eadfc9;

  transition: all 0.25s ease;
}

.tab-item:hover {
  background: #f9efdf;

  color: #6f5635;

  border-color: #d9b97d;

  transform: translateY(-2px);
}

.tab-item.active {
  background: #d8b77a;

  color: white;

  border-color: #d8b77a;

  box-shadow: 0 8px 18px rgba(216, 183, 122, 0.25);
}

/* BUTTON THÊM */
.btn-add {
  flex-shrink: 0;

  display: flex;

  align-items: center;

  gap: 8px;

  padding: 12px 20px;

  border: none;

  border-radius: 12px;

  background: #d8b77a;

  color: white;

  font-weight: 700;

  cursor: pointer;

  transition: all 0.25s ease;

  box-shadow: 0 6px 14px rgba(216, 183, 122, 0.2);
}

.btn-add:hover {
  background: #caa565;

  transform: translateY(-3px);

  box-shadow: 0 10px 22px rgba(150, 120, 80, 0.15);
}

.btn-add:active {
  transform: scale(0.97);
}

/* CONTENT */
.tab-content {
  background: #fffdf8;

  border-radius: 16px;

  padding: 16px;

  border: 1px solid #eee2cf;

  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

/* RESPONSIVE */
@media (max-width: 768px) {
  .tab-header {
    flex-direction: column;

    align-items: stretch;
  }

  .btn-add {
    justify-content: center;
  }
}
</style>
