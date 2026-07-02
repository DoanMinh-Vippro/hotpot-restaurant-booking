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
/* =====================================================
   WRAPPER
===================================================== */

.tab-wrapper {
  background: #0b0b0d;

  padding: 12px;

  border-radius: 12px;

  border: 1px solid rgba(212, 175, 55, 0.25);

  box-shadow: 0 0 20px rgba(212, 175, 55, 0.08);

  backdrop-filter: blur(10px);
}

/* =====================================================
   HEADER TAB
===================================================== */

.tab-header {
  display: flex;

  gap: 10px;

  border-bottom: 1px solid rgba(212, 175, 55, 0.25);

  padding-bottom: 10px;

  margin-bottom: 12px;
}

/* =====================================================
   TAB ITEM
===================================================== */

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

/* Hover */

.tab-item:hover {
  color: #ffd86b;

  border-color: rgba(212, 175, 55, 0.4);

  box-shadow: 0 0 10px rgba(212, 175, 55, 0.15);

  transform: translateY(-1px);
}

/* Active */

.tab-item.active {
  background: linear-gradient(145deg, #1a1a1d, #0f0f11);

  color: #ffd86b;

  border: 1px solid rgba(212, 175, 55, 0.7);

  box-shadow: 0 0 14px rgba(212, 175, 55, 0.25);
}

/* =====================================================
   NỘI DUNG TAB
===================================================== */

.tab-content {
  background: #0f0f11;

  border-radius: 10px;

  padding: 14px;

  border: 1px solid rgba(212, 175, 55, 0.15);
}
</style>
