<script setup lang="ts">
import type { ChiTietComBo } from '../api/ChiTietComBoApi'

defineProps<{
  item?: ChiTietComBo
}>()
</script>

<template>
  <div v-if="item" class="hop-xem-truoc">
    <h3>Chi tiết combo</h3>

    <p><strong>Combo:</strong> {{ item.tenCombo }}</p>
    <p><strong>Món:</strong> {{ item.tenMon }}</p>
    <p><strong>Số lượng:</strong> {{ item.soLuong }}</p>
    <p><strong>Giá:</strong> {{ Number(item.giaCombo).toLocaleString('vi-VN') }} đ</p>
    <p><strong>Mô tả:</strong> {{ item.moTa }}</p>
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