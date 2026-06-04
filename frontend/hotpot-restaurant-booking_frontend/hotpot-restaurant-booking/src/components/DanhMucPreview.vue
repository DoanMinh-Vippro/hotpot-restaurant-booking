<script setup lang="ts">
import type { DanhMuc } from '../api/DanhMucApi'

defineProps<{ item?: DanhMuc }>()
</script>

<template>
  <div v-if="item" class="hop-xem-truoc">

    <h3>Chi tiết danh mục</h3>

    <p><strong>ID:</strong> {{ item.idDanhMuc }}</p>
    <p><strong>Loại:</strong> {{ item.loaiDanhMuc }}</p>
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