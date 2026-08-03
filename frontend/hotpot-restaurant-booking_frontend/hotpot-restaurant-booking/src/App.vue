<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import AdminAccountPanel from '@/components/AdminAccountPanel.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const adminRouteNames = ['thucDon', 'hoa-don', 'ban-hang', 'giam-gia', 'ban', 'dat-ban-quan-ly', 'tai-khoan', 'thong-ke', 'khu-vuc', 'coc', 'shift-management']

const menuItems = [
  { label: 'Bán hàng', routeName: 'ban-hang', permission: 'pos', icon: '🛒' },
  { label: 'Quản lý thực đơn', routeName: 'thucDon', permission: 'menu', icon: '🍜' },
  { label: 'Hóa đơn', routeName: 'hoa-don', permission: 'invoice', icon: '🧾' },
  { label: 'Giảm giá', routeName: 'giam-gia', permission: 'discount', icon: '🏷️' },
  { label: 'Bàn', routeName: 'ban', permission: 'table', icon: '🪑' },
  { label: 'Đặt bàn quản lý', routeName: 'dat-ban-quan-ly', permission: 'reservation', icon: '📋' },
  { label: 'Quản lý ca', routeName: 'shift-management', permission: 'shift', icon: '🕒' },
  { label: 'Quản lý tài khoản', routeName: 'tai-khoan', permission: 'account', icon: '👤' },
  { label: 'Thống kê', routeName: 'thong-ke', permission: 'statistics', icon: '📈' },
  { label: 'Khu vực', routeName: 'khu-vuc', permission: 'area', icon: '📍' },
  { label: 'Tiền cọc', routeName: 'coc', permission: 'deposit', icon: '💳' },
]

const permittedMenuItems = computed(() => {
  if (!authStore.isAuthenticated || authStore.isUser) return []
  if (!authStore.permissions.length) return menuItems
  return menuItems.filter((item) => authStore.permissions.includes(item.permission))
})

const isAdminLayout = computed(
  () =>
    authStore.isAuthenticated &&
    !authStore.isUser &&
    !['auth', 'register', 'home'].includes(String(route.name)),
)

const goTo = (routeName: string) => {
  router.push({ name: routeName })
}
</script>

<template>
  <div v-if="isAdminLayout" class="admin-shell">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-heading">
          <div class="sidebar-brand">CÁI BANG</div>
          <div class="sidebar-subtitle">Bảng điều khiển quản trị</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button
          v-for="item in permittedMenuItems"
          :key="item.routeName"
          class="nav-item"
          :class="{ active: route.name === item.routeName }"
          @click="goTo(item.routeName)"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </button>

        <div class="sidebar-account-panel">
          <AdminAccountPanel />
        </div>
      </nav>
    </aside>

    <main class="admin-content">
      <RouterView />
    </main>
  </div>

  <RouterView v-else />
</template>

<style>
body {
  margin: 0;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  color: #4d3422;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

#app {
  min-height: 100vh;
  background: transparent;
}

button,
input,
select,
textarea {
  font: inherit;
}

.admin-shell {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  color: #4d3422;
}

.admin-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: rgba(255, 248, 234, 0.92);
  border-right: 1px solid #e2cfa6;
  padding: 20px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 2px 0 18px rgba(103, 72, 32, 0.08);
}

.sidebar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 6px 6px 12px;
  border-bottom: 1px solid #e6d2aa;
}

.sidebar-heading {
  flex: 1;
  min-width: 0;
}

.sidebar-brand {
  font-size: 1.05rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: #8b5e34;
}

.sidebar-subtitle {
  margin-top: 4px;
  font-size: 0.82rem;
  color: #8f6b46;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e6d2aa;
  background: #fff8ea;
  color: #6b4728;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  text-align: left;
  font-size: 0.92rem;
  transition: all 0.2s ease;
}

.nav-item:hover,
.nav-item.active {
  background: #d8a85c;
  color: #3d2814;
  border-color: #d8a85c;
  font-weight: 700;
  box-shadow: 0 8px 16px rgba(103, 72, 32, 0.12);
}

.nav-icon {
  width: 20px;
  text-align: center;
}

.sidebar-toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-action-btn {
  width: 42px;
  height: 42px;
  border: 1px solid #d7b470;
  background: #fff3d3;
  color: #7b4d1f;
  padding: 0;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-action-btn:hover {
  background: #f2dfb0;
}

.admin-notifications-panel {
  position: relative;
  margin-top: 4px;
}

.admin-notifications-panel .notification-panel {
  position: relative;
  inset: auto;
  top: auto;
  right: auto;
  width: 100%;
  border-color: #e4c78b;
  background: #fffaf1;
  color: #4d3422;
}

.sidebar-account-section {
  margin-top: 12px;
  padding-top: 8px;
}

.sidebar-account-panel {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid rgba(103, 72, 32, 0.12);
}

.admin-content {
  flex: 1;
  min-width: 0;
  overflow: auto;
  background: transparent;
}

.admin-content .page-shell,
.admin-content .table-card,
.admin-content .walkin-card,
.admin-content .detail-modal,
.admin-content .card,
.admin-content .panel {
  background: rgba(255, 248, 234, 0.97);
  border: 1px solid #e6d2aa;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.admin-content > * {
  min-height: 100%;
}

.admin-shell .back-home-btn,
.admin-shell .btn-back-home,
.admin-shell .btn-back,
.admin-shell .nut-ma,
.admin-shell .page-top {
  display: none !important;
}

@media (max-width: 900px) {
  .admin-shell {
    flex-direction: column;
  }

  .admin-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e2cfa6;
  }

  .sidebar-nav {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
}
</style>
