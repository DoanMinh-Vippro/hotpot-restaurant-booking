<script setup lang="ts">
import { computed } from 'vue'

export interface MenuItem {
  idMon?: number
  idCombo?: number

  tenMon?: string
  tenCombo?: string

  hinhAnh?: string
  anhMon?: string
  anhCombo?: string

  donGiaHienTai?: number
  giaCombo?: number
  giaSauGiam?: number

  // trạng thái kinh doanh
  trangThai?: number

  // trạng thái bán
  trangThaiBan?: number
}

const props = defineProps<{
  items: MenuItem[]
}>()

const emit = defineEmits<{
  (e: 'select', item: MenuItem): void
}>()

const isMon = (item: MenuItem) => item.idMon != null

// ==============================
// Kinh doanh
// ==============================

const isDangKinhDoanh = (item: MenuItem) => {
  return isMon(item) ? item.trangThai === 0 : item.trangThai === 1
}

// ==============================
// Hết hàng
// ==============================

const isOutOfStock = (item: MenuItem) => {
  return item.trangThaiBan === 0
}

// ==============================
// Danh sách hiển thị
// ==============================

const activeItems = computed(() => (props.items ?? []).filter(isDangKinhDoanh))

// ==============================
// Helpers
// ==============================

const getName = (item: MenuItem) => item.tenMon ?? item.tenCombo ?? 'Chưa có tên'

const getImage = (item: MenuItem) =>
  item.hinhAnh ?? item.anhMon ?? item.anhCombo ?? '/images/no-image.png'

const getPrice = (item: MenuItem) => {
  if ((item.giaSauGiam ?? 0) > 0) {
    return Number(item.giaSauGiam)
  }

  return Number(item.donGiaHienTai ?? item.giaCombo ?? 0)
}

const formatMoney = (value?: number) => new Intl.NumberFormat('vi-VN').format(value ?? 0) + ' đ'

// ==============================
// Events
// ==============================

function selectItem(item: MenuItem) {
  console.log('CLICK MENU', item)

  if (isOutOfStock(item)) return

  emit('select', item)
}
</script>

<template>
  <div class="menu-grid">
    <div
      v-for="item in activeItems"
      :key="item.idMon ? `mon-${item.idMon}` : `combo-${item.idCombo}`"
      class="card"
      :class="{ disabled: isOutOfStock(item) }"
      @click="selectItem(item)"
    >
      <div class="image-wrapper">
        <img :src="getImage(item)" :alt="getName(item)" class="image" loading="lazy" />

        <div class="overlay" v-if="isOutOfStock(item)">HẾT HÀNG</div>

        <div class="type">
          {{ item.idCombo ? 'COMBO' : 'MÓN' }}
        </div>
      </div>

      <div class="content">
        <div class="name">
          {{ getName(item) }}
        </div>

        <div class="bottom">
          <div class="price">
            {{ formatMoney(getPrice(item)) }}
          </div>

          <button class="add-btn" :disabled="isOutOfStock(item)" @click.stop="selectItem(item)">
            +
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;

  display: flex;
  flex-direction: column;

  cursor: pointer;

  border: 2px solid transparent;

  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);

  transition:
    transform 0.25s,
    box-shadow 0.25s,
    border-color 0.25s;
}

.card:hover {
  transform: translateY(-3px);

  border-color: #b7793f;

  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

/* ========================= */

.image-wrapper {
  position: relative;
}

.image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.type {
  position: absolute;
  top: 10px;
  left: 10px;

  background: rgba(0, 0, 0, 0.65);
  color: white;

  padding: 4px 10px;

  border-radius: 999px;

  font-size: 10px;
  font-weight: 700;
}

.overlay {
  position: absolute;
  inset: 0;

  background: rgba(0, 0, 0, 0.55);

  display: flex;
  justify-content: center;
  align-items: center;

  color: white;

  font-size: 18px;
  font-weight: 800;

  letter-spacing: 1px;
}

/* ========================= */

.content {
  flex: 1;

  display: flex;
  flex-direction: column;

  padding: 14px;
}

.name {
  font-size: 15px;
  font-weight: 700;

  color: #5a4634;

  min-height: 40px;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;

  overflow: hidden;
}

.bottom {
  margin-top: auto;

  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 17px;
  font-weight: 800;

  color: #b7793f;
}

.add-btn {
  width: 38px;
  height: 38px;

  border: none;
  border-radius: 50%;

  background: #b7793f;
  color: white;

  font-size: 20px;
  font-weight: 700;

  cursor: pointer;

  transition: 0.2s;
}

.add-btn:hover:not(:disabled) {
  transform: scale(1.08);
  background: #9f6735;
}

.add-btn:disabled {
  background: #bfbfbf;
  cursor: not-allowed;
}

/* ========================= */

.card.disabled {
  opacity: 0.7;
  filter: grayscale(0.45);
}

.card.disabled:hover {
  transform: none;
  border-color: transparent;
}
/* ========================= */
/* TABLET */
/* ========================= */

@media (max-width: 1200px) {
  .menu-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;
  }

  .image {
    height: 160px;
  }

  .content {
    padding: 15px;
  }

  .name {
    font-size: 15px;
    min-height: 42px;
  }

  .price {
    font-size: 17px;
  }

  .add-btn {
    width: 40px;
    height: 40px;
    font-size: 22px;
  }
}

/* ========================= */
/* MOBILE */
/* ========================= */

@media (max-width: 768px) {
  .menu-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .card {
    border-radius: 16px;
  }

  .image {
    height: 120px;
  }

  .content {
    padding: 12px;
  }

  .name {
    font-size: 14px;
    min-height: 36px;
  }

  .price {
    font-size: 15px;
  }

  .add-btn {
    width: 34px;
    height: 34px;
    font-size: 18px;
  }

  .type {
    top: 8px;
    left: 8px;
    font-size: 10px;
    padding: 4px 8px;
  }

  .overlay {
    font-size: 16px;
    letter-spacing: 1px;
  }
}

/* ========================= */
/* ĐIỆN THOẠI NHỎ */
/* ========================= */

@media (max-width: 480px) {
  .menu-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .card {
    border-radius: 14px;
  }

  .image {
    height: 100px;
  }

  .content {
    padding: 10px;
  }

  .name {
    font-size: 13px;
    min-height: 32px;
  }

  .price {
    font-size: 14px;
  }

  .add-btn {
    width: 30px;
    height: 30px;
    font-size: 16px;
  }

  .type {
    font-size: 9px;
    padding: 3px 7px;
  }

  .overlay {
    font-size: 14px;
  }
}

/* ========================= */
/* SCROLLBAR */
/* ========================= */

::-webkit-scrollbar {
  width: 7px;
}

::-webkit-scrollbar-thumb {
  background: #d3c2ae;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bea58a;
}
</style>
