<script setup lang="ts">
import type { Combo } from '../api/ComBoApi'

defineProps<{
  comboDaChon?: Combo
}>()
</script>

<template>
  <div v-if="comboDaChon" class="hop-xem-truoc">
    <h3>Thông tin combo</h3>

    <p><b>Tên:</b> {{ comboDaChon.tenCombo }}</p>
    <p><b>Giá:</b> {{ comboDaChon.giaCombo.toLocaleString('vi-VN') }} đ</p>
    <p><b>Ảnh:</b> {{ comboDaChon.hinhAnh }}</p>
    <p>
      <b>Kho hàng:</b> 
      <span :class="comboDaChon.trangThaiBan === 1 ? 'chu-xanh' : 'chu-do'">
        {{ comboDaChon.trangThaiBan === 1 ? 'Còn hàng' : 'Hết hàng' }}
      </span>
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
  color: white;
}

h3 {
  color: #f8d46a;
}

.chu-xanh {
  color: #52c41a;
  font-weight: 500;
}

.chu-do {
  color: #ff4d4f;
  font-weight: 500;
}
</style>