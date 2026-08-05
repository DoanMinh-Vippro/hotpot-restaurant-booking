<script setup lang="ts">
import type { GiamGia } from '../api/GiamGiaApi'

defineProps<{
  giam_gia_da_chon: GiamGia | undefined
}>()

const dinh_dang_ngay = (value: string | null) => {
  if (!value) return 'Chưa có'
  return new Date(value).toLocaleDateString('vi-VN')
}

const normalizeLoaiGiam = (value: string | null | undefined) => {
  const normalized = `${value ?? ''}`
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toUpperCase()

  if (['PHANTRAM', 'PERCENT', 'PHNTRAM', 'PHTNTRAM', 'PHTRAM'].includes(normalized)) return 'percent'
  if (normalized.includes('PH') && normalized.includes('TRAM')) return 'percent'
  if (['TIENMAT', 'TIEN', 'VND', 'FIXED', 'MONEY'].includes(normalized)) return 'fixed'
  if (normalized.includes('TIEN') || normalized.includes('MAT') || normalized.includes('GIATRI') || normalized.includes('VALUE')) return 'fixed'

  return 'unknown'
}

const formatLoaiGiam = (value: string | null | undefined) => {
  switch (normalizeLoaiGiam(value)) {
    case 'percent':
      return 'Phần trăm'
    case 'fixed':
      return 'Tiền mặt'
    default:
      return value?.trim() || 'Không xác định'
  }
}

const dinh_dang_gia_tri = (giamGia: GiamGia | undefined) => {
  if (!giamGia) return 'Chưa có'
  const numeric = Number(giamGia.giaTriGiam ?? 0)

  switch (normalizeLoaiGiam(giamGia.loaiGiam)) {
    case 'percent':
      return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 2 })}%`
    case 'fixed':
      return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 0 })} đ`
    default:
      return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 0 })}`
  }
}
</script>

<template>
  <div v-if="giam_gia_da_chon" class="hop-xem-truoc">
    <h3>Thông tin mã đang chọn</h3>
    <p><strong>Mã:</strong> {{ giam_gia_da_chon?.maGiamGia }}</p>
    <p><strong>Loại giảm:</strong> {{ formatLoaiGiam(giam_gia_da_chon?.loaiGiam) }}</p>
    <p><strong>Giá trị giảm:</strong> {{ dinh_dang_gia_tri(giam_gia_da_chon) }}</p>
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
