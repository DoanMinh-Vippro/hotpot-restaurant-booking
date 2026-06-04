<script setup lang="ts">
import type { ChiTietGiamGiaMon } from '../api/ChiTietGiamGiaMonApi'

defineProps<{
  item?: ChiTietGiamGiaMon
}>()
</script>

<template>
  <div v-if="item" class="hop-xem-truoc">
    <h3>Chi tiết giảm giá</h3>

    <p><strong>Món:</strong> {{ item.tenMon }}</p>
    <p><strong>Chương trình:</strong> {{ item.tenChuongTrinh }}</p>
    <p><strong>Mức giảm:</strong> {{ item.mucGiam }}%</p>
  </div>
</template>

<style scoped>
.hop-xem-truoc {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(248, 212, 106, 0.08);
  border: 1px solid rgba(248, 212, 106, 0.15);
  color: #d8d8d8;
}

h3 {
  color: #f8d46a;
}
</style>