<script setup lang="ts">
import type { DotGiamGia } from '../api/DotGiamGiaApi'

defineProps<{ item?: DotGiamGia }>()
</script>

<template>
  <div v-if="item" class="hop-xem-truoc">

    <h3>Chi tiết đợt giảm giá</h3>

    <p><strong>ID:</strong> {{ item.idDotGiamGia }}</p>
    <p><strong>Tên:</strong> {{ item.tenChuongTrinh }}</p>
    <p><strong>Bắt đầu:</strong> {{ item.ngayBatDau }}</p>
    <p><strong>Kết thúc:</strong> {{ item.ngayKetThuc }}</p>

  </div>
</template>

<style scoped>
.hop-xem-truoc {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(248,212,106,.08);
  border: 1px solid rgba(248,212,106,.15);
  color: #d8d8d8;
}

h3 {
  color: #f8d46a;
}
</style>