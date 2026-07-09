<script setup lang="ts">
import { ref, watch } from 'vue'
/**
 * MODEL KHU VỰC
 */
type KhuVuc = {
  idKhuVuc: number
  tenKhuVuc: string
}

/**
 * DATA TỪ COMPONENT CHA
 *
 * listKhuVuc:
 * Danh sách tất cả khu vực
 */
const props = defineProps<{
  listKhuVuc: KhuVuc[]
}>()

/**
 * EMIT RA COMPONENT CHA
 *
 * change:
 * Thông báo khu vực vừa được chọn
 */
const emit = defineEmits<{
  (e: 'change', idKhuVuc: number): void
}>()

/**
 * TAB ĐANG ĐƯỢC CHỌN
 *
 * Lưu id khu vực hiện tại
 */
const tabDangChon = ref<number | null>(null)

/**
 * KHI LOAD DANH SÁCH KHU VỰC
 *
 * Nếu chưa chọn tab nào:
 * -> tự động chọn tab đầu tiên
 */
watch(
  () => props.listKhuVuc,
  (danhSachKhuVucMoi) => {
    if (danhSachKhuVucMoi?.length > 0 && tabDangChon.value === null) {
      const firstKhuVuc = danhSachKhuVucMoi[0]
      if (firstKhuVuc?.idKhuVuc != null) {
        tabDangChon.value = firstKhuVuc.idKhuVuc
        emit('change', tabDangChon.value)
      }
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

/**
 * CHỌN TAB
 *
 * Khi user click tab
 */
const chonTab = (idKhuVuc: number) => {
  tabDangChon.value = idKhuVuc

  emit('change', idKhuVuc)
}
</script>

<template>
  <div class="tab-wrapper">
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

    <!--
      Trả idKhuVuc đang chọn xuống component con

      Ví dụ:
      DatBanQLListBan
      BanList
      ...
    -->
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
  gap: 10px;
  border-bottom: 1px solid #e6d2aa;
  padding-bottom: 10px;
  margin-bottom: 12px;
}

.tab-item {
  padding: 10px 16px;
  cursor: pointer;
  border-radius: 10px;
  background: #fff8ea;
  color: #8b5e34;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: all 0.25s ease;
  border: 1px solid #e6d2aa;
}

.tab-item:hover {
  color: #3d2814;
  border-color: #d8a85c;
  transform: translateY(-1px);
}

.tab-item.active {
  background: #d8a85c;
  color: #3d2814;
  border: 1px solid #d8a85c;
  box-shadow: 0 6px 14px rgba(103, 72, 32, 0.12);
}

.tab-content {
  background: #fffdf8;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid #e6d2aa;
}
</style>
