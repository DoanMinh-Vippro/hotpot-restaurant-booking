<script setup lang="ts">
type MenuItem = {
  idMon?: number
  idCombo?: number

  tenMon?: string
  tenCombo?: string

  donGiaHienTai?: number
  giaCombo?: number

  anhMon?: string
  anhCombo?: string

  trangThai?: number
}

const props = defineProps<{
  items: MenuItem[]
}>()

const emit = defineEmits<{
  (e: 'select', item: MenuItem): void
}>()

const formatMoney = (value?: number) => {
  return (value ?? 0).toLocaleString('vi-VN') + ' đ'
}
function selectItem(item: MenuItem) {
  if (isDisabled(item)) return

  emit('select', item)
}

const getName = (item: MenuItem) => item.tenMon || item.tenCombo || ''
const getPrice = (item: MenuItem) => item.donGiaHienTai ?? item.giaCombo ?? 0
const getImage = (item: MenuItem) => item.anhMon || item.anhCombo || '/images/no-image.png'
const isDisabled = (item: MenuItem) => item.trangThai !== undefined && item.trangThai === 0
</script>

<template>
  <div class="menu-grid">
    <div
      v-for="item in items"
      :key="item.idMon ?? item.idCombo"
      class="card"
      :class="{ disabled: isDisabled(item) }"
      @click="selectItem(item)"
    >
      <div class="image-wrapper">
        <img :src="getImage(item)" class="image" loading="lazy" />

        <span v-if="isDisabled(item)" class="badge"> Ngừng bán </span>
      </div>

      <div class="info">
        <div class="name">
          {{ getName(item) }}
        </div>

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

  transition: 0.25s;

  border: 1px solid #eee4d6;

  box-shadow: 0 6px 18px rgba(70, 45, 20, 0.08);
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

  background: rgba(180, 40, 40, 0.9);
  color: white;

  padding: 4px 10px;
  border-radius: 999px;

  font-size: 12px;
  font-weight: 700;
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

.card.disabled {
  opacity: 0.55;
  cursor: not-allowed;
  filter: grayscale(0.3);
}

.card.disabled:hover {
  transform: none;
  box-shadow: 0 6px 18px rgba(70, 45, 20, 0.08);
}
</style>
