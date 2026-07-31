<script setup lang="ts">
import { computed } from 'vue'

export type MenuItem = {
  idMon?: number
  idCombo?: number

  tenMon?: string
  tenCombo?: string

  hinhAnh?: string
  anhMon?: string
  anhCombo?: string

  // Giá gốc (được dùng làm fallback)
  donGiaHienTai?: number
  giaCombo?: number
  
  // Trường dùng chung sau khi áp mã giảm giá
  giaSauGiam?: number

  // Trạng thái kinh doanh
  trangThai?: number
  // Trạng thái kho hàng
  trangThaiBan?: number
}

const props = defineProps<{
  items: MenuItem[]
}>()

const emit = defineEmits<{
  (e: 'select', item: MenuItem): void
}>()

// Phân biệt Món hay Combo dựa vào ID
const isMon = (item: MenuItem) => item.idMon !== undefined

// 1. Kiểm tra TRẠNG THÁI KINH DOANH 
const isDangKinhDoanh = (item: MenuItem) => {
  if (isMon(item)) {
    // Món: 0 là Đang kinh doanh
    return item.trangThai === 0
  }
  // Combo: 1 là Đang kinh doanh
  return item.trangThai === 1
}

// 2. Kiểm tra TRẠNG THÁI BÁN (Hết hàng = 0)
const isOutOfStock = (item: MenuItem) => {
  return item.trangThaiBan === 0
}

// Lọc bỏ món Ngừng bán
const activeItems = computed(() => {
  return (props.items || []).filter((item) => isDangKinhDoanh(item))
})

const getName = (item: MenuItem) => item.tenMon || item.tenCombo || 'Chưa đặt tên'

const getImage = (item: MenuItem) =>
  item.hinhAnh || item.anhMon || item.anhCombo || '/images/no-image.png'

// 3. Lấy trực tiếp giaSauGiam
const getPrice = (item: MenuItem) => {
  if (item.giaSauGiam !== undefined && item.giaSauGiam !== null && Number(item.giaSauGiam) > 0) {
    return Number(item.giaSauGiam)
  }
  return item.donGiaHienTai ?? item.giaCombo ?? 0
}

const formatMoney = (value?: number) => {
  return (value ?? 0).toLocaleString('vi-VN') + ' đ'
}

// 4. Chọn món / combo
function selectItem(item: MenuItem) {
  if (isOutOfStock(item)) {
    const name = getName(item)
    alert(`"${name}" hiện tại đã HẾT HÀNG! Vui lòng chọn món khác hợp lệ.`)
    return
  }

  emit('select', item)
}
</script>

<template>
  <div class="menu-grid">
    <div
      v-for="item in activeItems"
      :key="item.idMon ? `mon-${item.idMon}` : `combo-${item.idCombo}`"
      class="card"
      :class="{ 'out-of-stock': isOutOfStock(item) }"
      @click="selectItem(item)"
    >
      <div class="image-wrapper">
        <img :src="getImage(item)" class="image" loading="lazy" :alt="getName(item)" />

        <span v-if="isOutOfStock(item)" class="badge badge-het-hang"> Hết hàng </span>
      </div>

      <div class="info">
        <div class="name" :title="getName(item)">
          {{ getName(item) }}
        </div>

        <!-- Giá hiển thị luôn là giaSauGiam -->
        <div class="price">
          {{ formatMoney(getPrice(item)) }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.card {
  background: #fffdf9;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid #eee4d6;
  box-shadow: 0 6px 18px rgba(70, 45, 20, 0.08);
  position: relative;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(70, 45, 20, 0.15);
}

.image-wrapper {
  position: relative;
}

.image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.badge-het-hang {
  background: rgba(220, 53, 69, 0.95);
  color: white;
}

.info {
  padding: 14px 16px 18px;
}

.name {
  font-size: 16px;
  font-weight: 700;
  color: #5a4634;
  min-height: 46px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price {
  margin-top: 12px;
  font-size: 17px;
  font-weight: 700;
  color: #b7793f;
}

.card.out-of-stock {
  opacity: 0.65;
  filter: grayscale(0.3);
}

.card.out-of-stock:hover {
  border-color: #dc3545;
}
</style>