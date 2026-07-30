<script setup lang="ts">
import type { Combo } from '../api/ComBoApi'

defineProps<{
  comboDaChon: Combo | undefined
}>()
</script>

<template>
  <div v-if="comboDaChon" class="hop-xem-truoc">
    <h3>Xem trước combo</h3>

    <p><b>Ảnh:</b> {{ comboDaChon.hinhAnh }}</p>

    <div class="chi-tiet-thong-tin">
      <p>
        <strong>Tên combo:</strong>
        <span class="gia-tri-highlight">{{ comboDaChon.tenCombo }}</span>
      </p>

      <p>
        <strong>Giá gốc:</strong>
        {{ Number(comboDaChon.giaCombo).toLocaleString('vi-VN') }} đ
      </p>

      <p>
        <strong>Giá sau giảm:</strong>
        <span class="gia-giam-ong">{{ Number(comboDaChon.giaSauGiam).toLocaleString('vi-VN') }} đ</span>
      </p>

      <p>
        <strong>Số tiền giảm:</strong>
        {{ Number(comboDaChon.soTienDuocGiam).toLocaleString('vi-VN') }} đ
      </p>

      <p>
        <strong>Khuyến mãi:</strong>
        {{ comboDaChon.tenChuongTrinhGiamGia || 'Không có áp dụng' }}
      </p>

      <p>
        <strong>Trạng thái kho:</strong>
        <span :class="comboDaChon.trangThaiBan === 1 && comboDaChon.trangThai === 1 ? 'mau-con-hang' : 'mau-het-hang'">
          {{ comboDaChon.trangThai === 0 ? 'Hết hàng (Theo trạng thái ngưng bán)' : (comboDaChon.trangThaiBan === 1 ? 'Còn hàng' : 'Hết hàng') }}
        </span>
      </p>
    </div>
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
  margin-bottom: 14px;
}

.chi-tiet-thong-tin p {
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.5;
}

.gia-tri-highlight {
  color: #8b5e34;
  font-weight: 600;
  margin-left: 6px;
}

.gia-giam-ong {
  color: #ff4d4f;
  font-weight: 700;
}

.mau-con-hang {
  color: #52c41a;
  font-weight: 500;
}

.mau-het-hang {
  color: #ff4d4f;
  font-weight: 500;
}
</style>