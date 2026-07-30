<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { OrderBan } from '@/types/Order'
import OrderSidebar from '@/components/Order/OrderSidebar.vue'
import OrderCategoryTabs from '@/components/Order/OrderCategoryTabs.vue'
import Order from '@/api/Order.ts'
import OrderMenu from '@/components/Order/OrderMenu.vue'
import OrderDetail from '@/components/Order/OrderDetail.vue'

const currentTab = ref<'MON' | 'COMBO' | 'DO_UONG'>('MON')

const selectedBan = ref<OrderBan | null>(null)

const hoaDon = ref<any>(null)

const chiTiet = ref<any>({
  dsMon: [],
  dsCombo: [],
})

const menu = ref<any>({
  dsMon: [],
  dsCombo: [],
})

async function handleSelectBan(ban: OrderBan) {
  selectedBan.value = ban

  await loadHoaDon()
}

async function loadMenu() {
  try {
    const res = await Order.getMenu()
    menu.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function handleSelectItem(item: any) {
  if (currentTab.value === 'MON') {
    await themMon(item)
  }

  if (currentTab.value === 'COMBO') {
    await themCombo(item)
  }
}

async function themMon(item: any) {
  if (!hoaDon.value) return

  await Order.themMon({
    idHoaDon: hoaDon.value.idHoaDon,
    idMon: item.idMon,
    soLuong: 1,
  })

  await loadHoaDon()
}

async function themCombo(item: any) {
  if (!hoaDon.value) return

  await Order.themCombo({
    idHoaDon: hoaDon.value.idHoaDon,
    idCombo: item.idCombo,
    soLuong: 1,
  })

  await loadHoaDon()
}

async function loadHoaDon() {
  if (!selectedBan.value) {
    hoaDon.value = null

    chiTiet.value = {
      dsMon: [],
      dsCombo: [],
    }

    return
  }

  const res = await Order.chonBan(selectedBan.value.idBan)

  hoaDon.value = res.data

  const ct = await Order.getChiTietHoaDon(hoaDon.value.idHoaDon)

  chiTiet.value = ct.data
}

onMounted(() => {
  loadMenu()
})
</script>

<template>
  <div class="order-page">
    <header class="order-header">
      <h2>ORDER NHÂN VIÊN</h2>

      <input placeholder="🔍 Tìm món..." class="search" />
    </header>

    <div class="order-body">
      <aside class="sidebar">
        <OrderSidebar @select-ban="handleSelectBan" />
      </aside>

      <main class="content">
        <OrderCategoryTabs v-model="currentTab" />

        <div class="content-body">
          <!-- MENU -->
          <div class="menu-content">
            <OrderMenu v-if="currentTab === 'MON'" :items="menu.dsMon" @select="handleSelectItem" />

            <OrderMenu
              v-else-if="currentTab === 'COMBO'"
              :items="menu.dsCombo"
              @select="handleSelectItem"
            />

            <div v-else class="coming-soon">Đồ uống đang phát triển...</div>
          </div>

          <!-- HÓA ĐƠN -->
          <div class="order-detail">
            <OrderDetail :chiTiet="chiTiet" />
          </div>
        </div>
      </main>
    </div>

    <footer class="order-footer">
      <div class="selected-info">
        <span v-if="!selectedBan"> Chưa chọn bàn </span>

        <span v-else>
          Đã chọn bàn:
          <strong>{{ selectedBan.tenBan }}</strong>
        </span>
      </div>

      <button class="order-btn" :disabled="!selectedBan">ORDER</button>
    </footer>
  </div>
</template>

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f7f3eb;
}

/* ================= HEADER ================= */

.order-header {
  height: 72px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 0 24px;

  background: #fffdf9;

  border-bottom: 1px solid #e6dac9;
}

.order-header h2 {
  color: #5a4634;
  font-size: 24px;
  font-weight: 700;
}

.search {
  width: 320px;
  height: 44px;

  padding: 0 18px;

  border: 1px solid #d8c9b7;
  border-radius: 14px;

  background: white;

  font-size: 14px;

  outline: none;

  transition: 0.25s;
}

.search::placeholder {
  color: #9b8b7b;
}

.search:focus {
  border-color: #b7793f;
  box-shadow: 0 0 0 4px rgba(183, 121, 63, 0.12);
}

/* ================= BODY ================= */

.order-body {
  flex: 1;

  display: flex;

  overflow: hidden;
}

.sidebar {
  width: 290px;
  min-width: 290px;

  background: #f5efe7;

  border-right: 1px solid #e7ddcf;
}

.content {
  flex: 1;

  display: flex;
  flex-direction: column;
}

.content-body {
  flex: 1;

  display: flex;

  overflow: hidden;
}

/* ================= MENU ================= */

.menu-content {
  flex: 1;

  overflow-y: auto;

  padding: 24px;

  background: #faf8f4;
}

/* ================= DETAIL ================= */

.order-detail {
  width: 400px;
  min-width: 400px;

  background: #fffdf9;

  border-left: 1px solid #e6dac9;

  box-shadow: -6px 0 18px rgba(0, 0, 0, 0.04);

  overflow-y: auto;
}

/* ================= FOOTER ================= */

.order-footer {
  height: 72px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 0 26px;

  background: #fffdf9;

  border-top: 1px solid #e6dac9;
}

.selected-info {
  color: #6d5b49;
  font-size: 15px;
  font-weight: 600;
}

.selected-info strong {
  color: #b7793f;
}

.order-btn {
  border: none;

  padding: 12px 34px;

  border-radius: 14px;

  background: #b7793f;

  color: white;

  font-size: 15px;
  font-weight: 700;

  cursor: pointer;

  transition: 0.25s;

  box-shadow: 0 8px 18px rgba(183, 121, 63, 0.25);
}

.order-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: #9f6735;
}

.order-btn:disabled {
  background: #d8c9b7;
  box-shadow: none;
  cursor: not-allowed;
}

/* ================= EMPTY ================= */

.coming-soon {
  display: flex;
  justify-content: center;
  align-items: center;

  height: 100%;

  color: #8b7355;

  font-size: 18px;
  font-weight: 600;
}

/* ================= SCROLL ================= */

::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-thumb {
  background: #d7c7b5;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bea68d;
}
</style>
