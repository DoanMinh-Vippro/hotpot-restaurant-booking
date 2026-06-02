<script setup lang="ts">
import type { GiamGia } from '../api/GiamGiaApi'

defineProps<{
  giam_gia_da_chon: GiamGia | undefined
}>()

const dinh_dang_ngay = (value: string | null) => {
  if (!value) return 'Chưa có'
  return new Date(value).toLocaleDateString('vi-VN')
}
</script>

<template>
  <div v-if="giam_gia_da_chon" class="hop-xem-truoc">
    <h3>Thông tin mã đang chọn</h3>
    <p><strong>Mã:</strong> {{ giam_gia_da_chon?.maGiamGia }}</p>
    <p><strong>Ngày tạo:</strong> {{ dinh_dang_ngay(giam_gia_da_chon?.ngayTao ?? null) }}</p>
    <p><strong>Ngày kết thúc:</strong> {{ dinh_dang_ngay(giam_gia_da_chon?.ngayKetThuc ?? null) }}</p>
    <p><strong>Số đã dùng:</strong> {{ giam_gia_da_chon?.soLuongDung ?? 0 }}</p>
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

.hop-xem-truoc h3 {
  margin: 0 0 12px;
  font-size: 0.95rem;
  color: #f8d46a;
}

.hop-xem-truoc p {
  margin: 8px 0;
  font-size: 0.9rem;
}

.hop-xem-truoc strong {
  color: #c7c7c7;
}
</style>
