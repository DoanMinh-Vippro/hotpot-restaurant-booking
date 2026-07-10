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
  <strong>Giá gốc:</strong>
  {{ Number(monDaChon.donGiaHienTai).toLocaleString('vi-VN') }} đ
</p>

<p>
  <strong>Giá sau giảm:</strong>
  {{ Number(monDaChon.giaSauGiam).toLocaleString('vi-VN') }} đ
</p>

<p>
  <strong>Số tiền giảm:</strong>
  {{ Number(monDaChon.soTienDuocGiam).toLocaleString('vi-VN') }} đ
</p>

<p>
  <strong>Khuyến mãi:</strong>
  {{ monDaChon.tenChuongTrinhGiamGia }}
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
  padding: 16px;
  border-radius: 14px;
  background: #fff8ea;
  border: 1px solid #e6d2aa;
  color: #5f3d22;
}

.hop-xem-truoc h3 {
  color: #8b5e34;
  margin-top: 0;
}
</style>