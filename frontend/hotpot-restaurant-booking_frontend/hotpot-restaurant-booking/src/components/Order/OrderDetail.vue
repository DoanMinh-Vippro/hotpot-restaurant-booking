<script setup lang="ts">
import { computed } from 'vue'

interface OrderItem {
  idHoaDonChiTiet: number | string

  idMon?: number
  idCombo?: number

  tenMon?: string
  tenCombo?: string

  soLuong: number
  thanhTien: number
  donGia?: number

  pending?: boolean

  pendingQty?: number
  totalQty?: number
}

interface OrderDetailData {
  dsMon: OrderItem[]
  dsCombo: OrderItem[]
  tongTien: number
}

const props = defineProps<{
  chiTiet: OrderDetailData
  pendingItems: any[]
}>()

const formatMoney = (value: number | null | undefined) => {
  return new Intl.NumberFormat('vi-VN').format(value ?? 0) + ' đ'
}

/* =======================
   MÓN
======================= */

const allMon = computed(() => {
  const map = new Map<number, OrderItem>()

  props.chiTiet.dsMon.forEach((m: any) => {
    map.set(m.idMon, {
      ...m,
      pendingQty: 0,
      totalQty: m.soLuong,
    })
  })

  props.pendingItems
    .filter((i) => i.loai === 'MON')
    .forEach((i) => {
      const exist = map.get(i.idMon)

      if (exist) {
        exist.soLuong += i.soLuong
        exist.totalQty = exist.soLuong
        exist.pendingQty = i.soLuong
        exist.thanhTien = exist.soLuong * Number(exist.donGia)
      } else {
        map.set(i.idMon, {
          idHoaDonChiTiet: 'pending-' + i.idMon,

          idMon: i.idMon,
          tenMon: i.tenMon,

          soLuong: i.soLuong,

          donGia: i.gia,

          thanhTien: i.gia * i.soLuong,

          pending: true,

          pendingQty: i.soLuong,
          totalQty: i.soLuong,
        })
      }
    })

  return [...map.values()]
})

/* =======================
   COMBO
======================= */

const allCombo = computed(() => {
  const map = new Map<number, OrderItem>()

  props.chiTiet.dsCombo.forEach((c: any) => {
    map.set(c.idCombo, {
      ...c,
      pendingQty: 0,
      totalQty: c.soLuong,
    })
  })

  props.pendingItems
    .filter((i) => i.loai === 'COMBO')
    .forEach((i) => {
      const exist = map.get(i.idCombo)

      if (exist) {
        exist.soLuong += i.soLuong
        exist.totalQty = exist.soLuong
        exist.pendingQty = i.soLuong
        exist.thanhTien = exist.soLuong * Number(exist.donGia)
      } else {
        map.set(i.idCombo, {
          idHoaDonChiTiet: 'pending-' + i.idCombo,

          idCombo: i.idCombo,
          tenCombo: i.tenCombo,

          soLuong: i.soLuong,

          donGia: i.gia,

          thanhTien: i.gia * i.soLuong,

          pending: true,

          pendingQty: i.soLuong,
          totalQty: i.soLuong,
        })
      }
    })

  return [...map.values()]
})

/* =======================
   TỔNG TIỀN
======================= */

const tongTien = computed(() => {
  let tong = 0

  allMon.value.forEach((i) => {
    tong += Number(i.thanhTien)
  })

  allCombo.value.forEach((i) => {
    tong += Number(i.thanhTien)
  })

  return tong
})
</script>

<template>
  <div class="detail">
    <div class="header">
      <h3>🛒 Giỏ hàng</h3>

      <span class="count"> {{ allMon.length + allCombo.length }} món </span>
    </div>

    <!-- CHỈ 1 SCROLL -->
    <div v-if="allMon.length || allCombo.length" class="group">
      <!-- ===== MÓN ===== -->

      <template v-if="allMon.length">
        <div class="group-title">MÓN ĂN</div>

        <div
          v-for="item in allMon"
          :key="'mon-' + item.idHoaDonChiTiet"
          class="item"
          :class="{ pendingItem: item.pendingQty && item.pendingQty > 0 }"
        >
          <div class="left">
            <div class="qty">{{ item.soLuong }}x</div>

            <div class="info">
              <div class="name">
                {{ item.tenMon }}
              </div>

              <div v-if="item.pendingQty && item.pendingQty > 0" class="pending">
                {{ item.pendingQty }}/{{ item.totalQty }} Chưa Order
              </div>

              <div class="price-small">
                {{ formatMoney(item.donGia) }}
              </div>
            </div>
          </div>

          <div class="total-item">
            {{ formatMoney(item.thanhTien) }}
          </div>
        </div>
      </template>

      <!-- ===== COMBO ===== -->

      <template v-if="allCombo.length">
        <div class="group-title combo-title">COMBO</div>

        <div
          v-for="item in allCombo"
          :key="'combo-' + item.idHoaDonChiTiet"
          class="item"
          :class="{ pendingItem: item.pendingQty && item.pendingQty > 0 }"
        >
          <div class="left">
            <div class="qty">{{ item.soLuong }}x</div>

            <div class="info">
              <div class="name">
                {{ item.tenCombo }}
              </div>

              <div v-if="item.pendingQty && item.pendingQty > 0" class="pending">
                {{ item.pendingQty }}/{{ item.totalQty }} Chưa Order
              </div>

              <div class="price-small">
                {{ formatMoney(item.donGia) }}
              </div>
            </div>
          </div>

          <div class="total-item">
            {{ formatMoney(item.thanhTien) }}
          </div>
        </div>
      </template>
    </div>

    <!-- EMPTY -->

    <div v-else class="empty">
      <div class="icon">🍽️</div>

      <div>Chưa chọn món</div>

      <span>Chạm vào món bên trái để thêm vào giỏ.</span>
    </div>

    <!-- FOOTER -->

    <div class="footer">
      <div class="tong-label">Tổng tiền</div>

      <div class="tong-value">
        {{ formatMoney(tongTien) }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fffdf9;
}

/* ================= HEADER ================= */

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 16px 18px;

  border-bottom: 1px solid #ece2d4;

  flex-shrink: 0;
}

.header h3 {
  margin: 0;

  font-size: 20px;
  font-weight: 700;

  color: #5a4634;
}

.count {
  white-space: nowrap;

  background: #f3e4d3;
  color: #b7793f;

  padding: 5px 12px;

  border-radius: 999px;

  font-size: 12px;
  font-weight: 700;
}

/* ================= BODY ================= */

.group {
  flex: 1;

  overflow-y: auto;

  padding: 14px;
}

.group-title {
  margin-bottom: 10px;

  font-size: 12px;
  font-weight: 700;

  color: #9d8165;

  letter-spacing: 1px;
}

.combo-title {
  margin-top: 18px;
}

/* ================= ITEM ================= */

.item {
  display: flex;
  align-items: center;
  justify-content: space-between;

  gap: 14px;

  padding: 12px 14px;

  margin-bottom: 10px;

  border-radius: 14px;

  background: #f8f4ef;

  transition: 0.2s;
}

.item:hover {
  background: #f2ebe2;
}

.pendingItem {
  background: #fff8dc;
  border: 1px solid #ffd66b;
}

.pendingItem:hover {
  background: #fff2bf;
}

/* ================= LEFT ================= */

.left {
  flex: 1;

  display: flex;
  align-items: center;

  gap: 12px;

  min-width: 0;
}

.qty {
  width: 34px;
  height: 34px;

  flex-shrink: 0;

  display: flex;
  justify-content: center;
  align-items: center;

  border-radius: 10px;

  background: #b7793f;
  color: white;

  font-size: 13px;
  font-weight: 700;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  color: #5a4634;

  font-size: 15px;
  font-weight: 700;

  line-height: 1.35;

  overflow: hidden;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pending {
  display: inline-flex;
  align-items: center;

  margin-top: 5px;

  padding: 3px 8px;

  border-radius: 999px;

  background: #fff4d8;
  color: #d97706;

  border: 1px solid #ffd98a;

  font-size: 10px;
  font-weight: 700;
}

.price-small {
  margin-top: 4px;

  color: #9b8b7b;

  font-size: 12px;
}

.total-item {
  flex-shrink: 0;

  width: 96px;

  text-align: right;

  color: #b7793f;

  font-size: 16px;
  font-weight: 800;
}

/* ================= EMPTY ================= */

.empty {
  flex: 1;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  gap: 10px;

  padding: 30px;

  text-align: center;

  color: #a1907d;
}

.empty .icon {
  font-size: 56px;
}

.empty span {
  font-size: 13px;
}

/* ================= FOOTER ================= */

.footer {
  flex-shrink: 0;

  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 18px;

  border-top: 1px solid #ece2d4;

  background: white;

  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.04);
}

.tong-label {
  color: #6d5b49;

  font-size: 15px;
  font-weight: 600;
}

.tong-value {
  color: #b7793f;

  font-size: 22px;
  font-weight: 800;
}
/* ================= TABLET ================= */

@media (max-width: 992px) {
  .header {
    padding: 14px 16px;
  }

  .header h3 {
    font-size: 18px;
  }

  .group {
    padding: 12px;
  }

  .item {
    padding: 11px 12px;
    gap: 10px;
  }

  .qty {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }

  .name {
    font-size: 14px;
  }

  .price-small {
    font-size: 11px;
  }

  .total-item {
    width: 82px;
    font-size: 15px;
  }

  .tong-value {
    font-size: 20px;
  }
}

/* ================= MOBILE ================= */

@media (max-width: 768px) {
  .detail {
    height: auto;
    overflow: visible;
  }

  .header {
    padding: 12px 14px;
  }

  .header h3 {
    font-size: 17px;
  }

  .count {
    font-size: 11px;
    padding: 4px 10px;
  }

  .group {
    flex: none;
    overflow: visible;
    padding: 10px;
  }

  .item {
    padding: 10px;
    gap: 10px;
  }

  .left {
    flex: 1;
    min-width: 0;
  }

  .qty {
    width: 30px;
    height: 30px;
    font-size: 11px;
  }

  .name {
    font-size: 13px;
  }

  .pending {
    font-size: 9px;
    padding: 2px 7px;
  }

  .price-small {
    font-size: 10px;
  }

  .total-item {
    width: 72px;
    font-size: 14px;
  }

  .footer {
    padding: 14px;
  }

  .tong-label {
    font-size: 14px;
  }

  .tong-value {
    font-size: 18px;
  }
}

/* ================= SMALL MOBILE ================= */

@media (max-width: 480px) {
  .group {
    padding: 8px;
  }

  .item {
    padding: 8px;
    gap: 8px;
  }

  .qty {
    width: 28px;
    height: 28px;
    font-size: 10px;
  }

  .name {
    font-size: 12px;
  }

  .price-small {
    font-size: 9px;
  }

  .pending {
    font-size: 8px;
  }

  .total-item {
    width: 64px;
    font-size: 13px;
  }

  .footer {
    padding: 12px;
  }

  .tong-label {
    font-size: 13px;
  }

  .tong-value {
    font-size: 17px;
  }
}

/* ================= SCROLL ================= */

.group::-webkit-scrollbar {
  width: 6px;
}

.group::-webkit-scrollbar-thumb {
  background: #d3c2ae;
  border-radius: 999px;
}

.group::-webkit-scrollbar-thumb:hover {
  background: #bea58a;
}

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-thumb {
  background: #d3c2ae;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bea58a;
}
</style>
