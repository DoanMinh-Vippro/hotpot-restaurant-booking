<script setup lang="ts">
import type { Mon } from '../api/MonApi'

defineProps<{
  monDaChon: Mon | undefined
}>()
</script>

<template>
  <div v-if="monDaChon" class="hop-xem-truoc">
    <h3>Thông tin món</h3>

    <p>
      <strong>Tên món:</strong>
      {{ monDaChon.tenMon }}
    </p>

    <p>
      <strong>Giá:</strong>
      {{ Number(monDaChon.donGiaHienTai).toLocaleString('vi-VN') }} đ
    </p>

    <p>
      <strong>Danh mục:</strong>
      {{ monDaChon.loaiDanhMuc }}
    </p>
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

.hop-xem-truoc h3 {
  color: #f8d46a;
}
</style>