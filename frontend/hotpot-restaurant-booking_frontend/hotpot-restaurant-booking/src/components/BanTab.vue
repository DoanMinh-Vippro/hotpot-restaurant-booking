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
  background: rgba(255, 248, 234, 0.96);
  padding: 12px;
  border-radius: 16px;
  border: 1px solid #e6d2aa;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
  backdrop-filter: blur(10px);
}

.tab-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid #e6d2aa;
  padding-bottom: 12px;
  margin-bottom: 14px;
}

.tab-item {
  padding: 10px 18px;
  cursor: pointer;
  border-radius: 10px;
  background: #fff8ea;
  color: #8b5e34;
  font-weight: 600;
  letter-spacing: 0.4px;
  border: 1px solid #e6d2aa;
  transition: all 0.25s ease;
}

.tab-item:hover {
  color: #3d2814;
  border-color: #d8a85c;
  transform: translateY(-1px);
}

.tab-item.active {
  background: #d8a85c;
  color: #3d2814;
  border-color: #d8a85c;
  box-shadow: 0 6px 14px rgba(103, 72, 32, 0.12);
}

.btn-add {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 18px;
  border: none;
  border-radius: 10px;
  background: #d8a85c;
  color: #3d2814;
  font-weight: 700;
  cursor: pointer;
  transition: 0.25s;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(103, 72, 32, 0.1);
}

.btn-add:active {
  transform: scale(0.98);
}

.tab-content {
  background: #fffdf8;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid #e6d2aa;
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
  background: rgba(216, 168, 92, 0.35);
  border-radius: 20px;
}
</style>
