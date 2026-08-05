<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { OrderBan } from '@/types/Order'
import OrderSidebar from '@/components/Order/OrderSidebar.vue'
import OrderCategoryTabs from '@/components/Order/OrderCategoryTabs.vue'
import Order from '@/api/Order.ts'
import OrderMenu from '@/components/Order/OrderMenu.vue'
import OrderDetail from '@/components/Order/OrderDetail.vue'
import { useOrderStore } from '@/stores/OrderStore'

const orderStore = useOrderStore()
const currentTab = ref<'MON' | 'COMBO' | 'DO_UONG'>('MON')
const selectedBan = ref<OrderBan | null>(null)
const hoaDon = ref<any>(null)
const showConfirmOrder = ref(false)
const showSuccessPopup = ref(false)
const pendingItems = ref<any[]>([])

async function confirmOrder() {
  if (!hoaDon.value) return

  showConfirmOrder.value = false

  for (const item of pendingItems.value) {
    if (item.loai === 'MON') {
      await Order.themMon({
        idHoaDon: hoaDon.value.idHoaDon,
        idMon: item.idMon,
        soLuong: item.soLuong,
      })
    } else {
      await Order.themCombo({
        idHoaDon: hoaDon.value.idHoaDon,
        idCombo: item.idCombo,
        soLuong: item.soLuong,
      })
    }
  }

  pendingItems.value = []

  await loadHoaDon()
  showSuccessPopup.value = true
  orderStore.updateItems(selectedBan.value!.idBan, convertChiTietToStore())
  console.log('ORDER SUCCESS')
}

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

function handleSelectItem(item: any) {
  if (currentTab.value === 'MON') {
    themMon(item)
  } else if (currentTab.value === 'COMBO') {
    themCombo(item)
  }
}

function themMon(item: any) {
  const exist = pendingItems.value.find((i) => i.loai === 'MON' && i.idMon === item.idMon)

  if (exist) {
    exist.soLuong++
  } else {
    pendingItems.value.push({
      loai: 'MON',
      idMon: item.idMon,
      tenMon: item.tenMon,
      gia: item.donGiaHienTai,
      soLuong: 1,
    })
  }

  // chỉ update UI
}

function themCombo(item: any) {
  const exist = pendingItems.value.find((i) => i.loai === 'COMBO' && i.idCombo === item.idCombo)

  if (exist) {
    exist.soLuong++
  } else {
    pendingItems.value.push({
      loai: 'COMBO',
      idCombo: item.idCombo,
      tenCombo: item.tenCombo,
      gia: item.giaCombo,
      soLuong: 1,
    })
  }
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

function convertChiTietToStore() {
  const result: any[] = []

  chiTiet.value.dsMon.forEach((m: any) => {
    result.push({
      idMon: m.idMon,
      tenMon: m.tenMon,
      gia: Number(m.donGia),
      soLuong: m.soLuong,
      loai: 'MON',
      tenQuay: 'Quầy Bếp',
      daLen: 0,
    })
  })

  chiTiet.value.dsCombo.forEach((c: any) => {
    result.push({
      idCombo: c.idCombo,
      tenCombo: c.tenCombo,
      gia: Number(c.donGia),
      soLuong: c.soLuong,
      loai: 'COMBO',
      tenQuay: 'Quầy Bếp',
      daLen: 0,
    })
  })

  return result
}

onMounted(() => {
  loadMenu()
})
</script>

<template>
  <div class="order-wrapper">
    <div class="order-page">
      <!-- ================= HEADER ================= -->

      <header class="order-header">
        <div class="header-left">
          <div class="logo">🍽</div>

          <div class="header-info">
            <h2>ORDER APP</h2>

            <div class="table-info">
              <template v-if="selectedBan">
                <span class="status">🟢 Đang phục vụ</span>
                <strong>{{ selectedBan.tenBan }}</strong>
              </template>

              <template v-else>
                <span class="status waiting">⚪ Chưa chọn bàn</span>
              </template>
            </div>
          </div>
        </div>

        <div class="header-right">
          <input class="search" placeholder="🔍 Tìm món..." />
        </div>
      </header>

      <!-- ================= BODY ================= -->

      <div class="order-body">
        <!-- Sidebar -->

        <aside class="sidebar">
          <OrderSidebar @select-ban="handleSelectBan" />
        </aside>

        <!-- Main -->

        <main class="content">
          <OrderCategoryTabs v-model="currentTab" />

          <div class="content-body">
            <!-- Menu -->

            <section class="menu-content">
              <OrderMenu
                v-if="currentTab === 'MON'"
                :items="menu.dsMon"
                @select="handleSelectItem"
              />

              <OrderMenu
                v-else-if="currentTab === 'COMBO'"
                :items="menu.dsCombo"
                @select="handleSelectItem"
              />

              <div v-else class="coming-soon">🥤 Đồ uống đang phát triển...</div>
            </section>

            <!-- Cart -->

            <aside class="order-detail">
              <OrderDetail :chiTiet="chiTiet" :pendingItems="pendingItems" />
            </aside>
          </div>
        </main>
      </div>

      <!-- ================= FOOTER ================= -->

      <footer class="order-footer">
        <button class="order-btn" :disabled="!selectedBan" @click="showConfirmOrder = true">
          XÁC NHẬN ORDER
        </button>
      </footer>
    </div>
    <div v-if="showConfirmOrder" class="popup-overlay">
      <div class="popup">
        <h3>Xác nhận Order</h3>

        <p>
          Bạn có chắc muốn gửi Order cho
          <strong>{{ selectedBan?.tenBan }}</strong>
          ?
        </p>

        <div class="popup-actions">
          <button class="cancel-btn" @click="showConfirmOrder = false">Hủy</button>

          <button class="confirm-btn" @click="confirmOrder">Đồng ý</button>
        </div>
      </div>
    </div>
    <div v-if="showSuccessPopup" class="popup-overlay">
      <div class="popup success-popup">
        <div class="success-icon">✅</div>

        <h3>Order thành công</h3>

        <p>Đơn gọi món đã được gửi xuống bếp.</p>

        <button class="confirm-btn" @click="showSuccessPopup = false">OK</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* =========================================================
   APP
========================================================= */

.order-wrapper {
  width: 100%;
  min-height: 100vh;

  display: flex;
  justify-content: center;
  align-items: center;

  background: #ececec;

  padding: 18px;

  box-sizing: border-box;
}

.order-page {
  width: 100%;
  max-width: 1550px;
  height: 95vh;

  display: flex;
  flex-direction: column;

  background: #f5f3ef;

  border-radius: 28px;

  overflow: hidden;

  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.15),
    0 10px 20px rgba(0, 0, 0, 0.08);
}

/* =========================================================
   HEADER
========================================================= */

.order-header {
  flex-shrink: 0;

  min-height: 84px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  gap: 20px;

  padding: 18px 28px;

  background: white;

  border-bottom: 1px solid #ece5db;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;

  min-width: 0;
}

.logo {
  width: 56px;
  height: 56px;

  flex-shrink: 0;

  border-radius: 18px;

  background: #b7793f;

  display: flex;
  justify-content: center;
  align-items: center;

  color: white;

  font-size: 28px;

  box-shadow: 0 8px 18px rgba(183, 121, 63, 0.25);
}

.header-info {
  min-width: 0;
}

.header-info h2 {
  margin: 0;

  color: #5b4635;

  font-size: 24px;
  font-weight: 800;

  white-space: nowrap;
}

.table-info {
  margin-top: 4px;

  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  font-size: 14px;
}

.status {
  color: #35c759;
  font-weight: 700;
}

.status.waiting {
  color: #999;
}

.table-info strong {
  color: #b7793f;
}

.header-right {
  flex-shrink: 0;
}

.search {
  width: 320px;
  max-width: 100%;

  height: 46px;

  border: 1px solid #ddd3c5;
  border-radius: 16px;

  padding: 0 18px;

  background: #faf8f4;

  outline: none;

  transition: 0.25s;

  box-sizing: border-box;
}

.search:focus {
  background: white;
  border-color: #b7793f;
}

.search::placeholder {
  color: #9b8b7b;
}

/* =========================================================
   BODY
========================================================= */

.order-body {
  flex: 1;

  display: flex;

  min-height: 0;

  overflow: hidden;
}

/* =========================================================
   SIDEBAR
========================================================= */

.sidebar {
  width: 260px;
  min-width: 260px;

  background: white;

  border-right: 1px solid #ece5db;

  overflow: hidden;
}

/* =========================================================
   CONTENT
========================================================= */

.content {
  flex: 1;

  display: flex;
  flex-direction: column;

  min-width: 0;
  min-height: 0;

  overflow: hidden;
}

.content-body {
  flex: 1;

  display: flex;

  min-width: 0;
  min-height: 0;

  overflow: hidden;
}

/* =========================================================
   MENU
========================================================= */

.menu-content {
  flex: 1;

  background: #faf8f4;

  padding: 20px;

  overflow-y: auto;
  overflow-x: hidden;

  min-width: 0;
}

/* =========================================================
   CART
========================================================= */

.order-detail {
  width: 380px;
  min-width: 380px;

  display: flex;
  flex-direction: column;

  background: white;

  border-left: 1px solid #ece5db;

  overflow: hidden;
}

/* =========================================================
   FOOTER
========================================================= */

.order-footer {
  flex-shrink: 0;

  height: 92px;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 0 24px;

  background: white;

  border-top: 1px solid #ece5db;
}

.order-btn {
  width: 100%;
  height: 62px;

  border: none;
  border-radius: 18px;

  background: #b7793f;

  color: white;

  font-size: 20px;
  font-weight: 800;
  letter-spacing: 1px;

  cursor: pointer;

  transition: 0.25s;

  box-shadow: 0 10px 22px rgba(183, 121, 63, 0.25);
}

.order-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: #a66c35;
}

.order-btn:disabled {
  background: #d9cab8;

  box-shadow: none;

  cursor: not-allowed;
}

/* =========================================================
   EMPTY
========================================================= */

.coming-soon {
  height: 100%;

  display: flex;
  justify-content: center;
  align-items: center;

  color: #8c7357;

  font-size: 18px;
  font-weight: 600;
}

/* =========================================================
   POPUP
========================================================= */

.popup-overlay {
  position: fixed;
  inset: 0;

  background: rgba(0, 0, 0, 0.45);

  display: flex;
  justify-content: center;
  align-items: center;

  padding: 20px;

  z-index: 9999;
}

.popup {
  width: 420px;
  max-width: 100%;

  background: white;

  border-radius: 20px;

  padding: 26px;

  box-sizing: border-box;

  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);

  animation: popupShow 0.2s ease;
}

.popup h3 {
  margin: 0 0 14px;

  color: #5b4635;

  font-size: 24px;
}

.popup p {
  margin: 0;

  color: #6f5b49;

  line-height: 1.6;

  font-size: 16px;
}

.popup-actions {
  margin-top: 28px;

  display: flex;
  justify-content: flex-end;

  gap: 12px;
}

.cancel-btn,
.confirm-btn {
  border: none;

  border-radius: 12px;

  padding: 12px 22px;

  cursor: pointer;

  font-size: 15px;
  font-weight: 700;

  transition: 0.2s;
}

.cancel-btn {
  background: #ececec;
  color: #555;
}

.cancel-btn:hover {
  background: #dddddd;
}

.confirm-btn {
  background: #b7793f;
  color: white;
}

.confirm-btn:hover {
  background: #a56a35;
}

.success-popup {
  text-align: center;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 12px;
}

@keyframes popupShow {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* =========================================================
   TABLET (1200px)
========================================================= */

@media (max-width: 1200px) {
  .order-page {
    height: 96vh;
  }

  .sidebar {
    width: 220px;
    min-width: 220px;
  }

  .order-detail {
    width: 310px;
    min-width: 310px;
  }

  .search {
    width: 240px;
  }

  .menu-content {
    padding: 16px;
  }

  .header-info h2 {
    font-size: 22px;
  }

  .logo {
    width: 52px;
    height: 52px;

    font-size: 24px;
  }
}

/* =========================================================
   TABLET (992px)
========================================================= */

@media (max-width: 992px) {
  .order-wrapper {
    padding: 10px;
  }

  .order-page {
    height: calc(100vh - 20px);

    border-radius: 18px;
  }

  .order-header {
    padding: 16px 18px;
  }

  .logo {
    width: 48px;
    height: 48px;

    font-size: 22px;
  }

  .header-info h2 {
    font-size: 20px;
  }

  .search {
    width: 180px;
  }

  .sidebar {
    width: 190px;
    min-width: 190px;
  }

  .order-detail {
    width: 270px;
    min-width: 270px;
  }

  .menu-content {
    padding: 14px;
  }

  .order-btn {
    height: 56px;

    font-size: 18px;
  }
}
/* =========================================================
   MOBILE
========================================================= */

@media (max-width: 768px) {
  .order-wrapper {
    padding: 0;

    height: auto;
    min-height: 100vh;

    display: block;
  }

  .order-page {
    width: 100%;
    max-width: 100%;

    min-height: 100vh;
    height: auto;

    border-radius: 0;

    overflow: visible;

    box-shadow: none;
  }

  /* ---------- HEADER ---------- */

  .order-header {
    display: flex;
    flex-direction: column;
    align-items: stretch;

    gap: 12px;

    height: auto;

    padding: 14px;
  }

  .header-left {
    width: 100%;
  }

  .header-right {
    width: 100%;
  }

  .search {
    width: 100%;
  }

  .header-info h2 {
    font-size: 18px;
  }

  .table-info {
    font-size: 13px;
  }

  /* ---------- BODY ---------- */

  .order-body {
    display: flex;
    flex-direction: column;

    overflow: visible;

    height: auto;
  }

  /* ---------- SIDEBAR ---------- */

  .sidebar {
    width: 100%;
    min-width: 100%;
    height: auto;

    border-right: none;
    border-bottom: 1px solid #ece5db;

    overflow: visible;
  }

  /* ---------- CONTENT ---------- */

  .content {
    overflow: visible;

    min-height: auto;
  }

  .content-body {
    display: flex;
    flex-direction: column;

    overflow: visible;

    min-height: auto;
  }

  /* ---------- MENU ---------- */

  .menu-content {
    width: 100%;

    flex: none;

    overflow: visible;

    min-height: auto;

    padding: 12px;
  }

  /* ---------- CART ---------- */

  .order-detail {
    width: 100%;
    min-width: 100%;

    max-height: none;
    height: auto;

    overflow: visible;

    border-left: none;
    border-top: 1px solid #ece5db;
  }

  /* ---------- FOOTER ---------- */

  .order-footer {
    height: auto;

    padding: 12px;
  }

  .order-btn {
    height: 54px;

    font-size: 18px;
  }

  /* ---------- POPUP ---------- */

  .popup {
    width: calc(100% - 32px);
  }
}

/* =========================================================
   SMALL MOBILE
========================================================= */

@media (max-width: 480px) {
  .logo {
    width: 42px;
    height: 42px;

    font-size: 20px;
  }

  .header-info h2 {
    font-size: 17px;
  }

  .table-info {
    font-size: 12px;
  }

  .menu-content {
    padding: 10px;
  }

  .popup {
    padding: 18px;
  }

  .popup h3 {
    font-size: 20px;
  }

  .popup p {
    font-size: 14px;
  }
}

/* =========================================================
   SCROLLBAR
========================================================= */

::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-thumb {
  background: #d5c6b5;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bea58c;
}

html,
body {
  overflow-x: hidden;
}

* {
  box-sizing: border-box;
}
</style>
