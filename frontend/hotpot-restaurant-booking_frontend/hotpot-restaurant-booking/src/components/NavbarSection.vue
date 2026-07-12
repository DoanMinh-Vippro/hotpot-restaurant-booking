<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import DatBanView from '@/views/DatBanView.vue'
import UserProfileDropdown from '@/components/UserProfileDropdown.vue'
import NotificationPanel from '@/components/NotificationPanel.vue'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
const router = useRouter()
const authStore = useAuthStore()
const isScrolled = ref(false)
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}
onMounted(() => {
  window.addEventListener('scroll', handleScroll)

  // Đảm bảo trạng thái auth được khôi phục khi F5
  const token = localStorage.getItem('token')
  if (token && !authStore.isAuthenticated) {
    authStore.decodeToken(token)
  }
  // Load unread count from storage at startup
  loadUnreadCount()
  // update unread count when notifications change in other tabs
  window.addEventListener('storage', loadUnreadCount)
})
onUnmounted(() => {
  window.removeEventListener('storage', loadUnreadCount)
})
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
const goToAuth = () => {
  router.push('/auth')
}
const goToRegister = () => {
  router.push('/register')
}
const goHome = () => {
  if (authStore.isAdmin) {
    router.push('/thucDon')
  } else {
    router.push('/')
  }
}
const showDatBanModal = ref(false)
const showNotifPanel = ref(false)
const unreadCount = ref(0)

const toggleNotif = () => {
  showNotifPanel.value = !showNotifPanel.value
}

const matchesTarget = (notification: any) => {
  if (authStore.isAdmin) return true

  const currentId = authStore.customerInfo?.khachHangId
  const currentPhone = authStore.customerInfo?.soDienThoai
  const targetId = notification?.targetKhachHangId
  const targetPhone = notification?.targetKhachHangPhone || notification?.targetPhone

  if (!currentId && !currentPhone) return true
  if (targetId != null && currentId != null && Number(targetId) === Number(currentId)) return true
  if (targetPhone && currentPhone && String(targetPhone) === String(currentPhone)) return true
  if (targetId == null && targetPhone == null) return true
  return false
}

const loadUnreadCount = () => {
  try {
    const arr = JSON.parse(localStorage.getItem('notifications') || '[]')
    unreadCount.value = Array.isArray(arr) ? arr.filter((n: any) => !n.read && matchesTarget(n)).length : 0
  } catch (e) {
    unreadCount.value = 0
  }
}

const pushNotification = (payload: any) => {
  try {
    const arr = JSON.parse(localStorage.getItem('notifications') || '[]') || []
    arr.unshift({ ...payload, time: new Date().toISOString(), read: false })
    localStorage.setItem('notifications', JSON.stringify(arr))
    loadUnreadCount()
  } catch (e) {
    console.warn('Không thể lưu thông báo:', e)
  }
}

let bookingCheckerInterval: number | null = null
onMounted(() => {
  // periodic check for upcoming bookings for the logged-in customer
  bookingCheckerInterval = window.setInterval(async () => {
    try {
      if (!authStore.isAuthenticated) return
      const custId = authStore.customerInfo?.khachHangId
      const custPhone = authStore.customerInfo?.soDienThoai
      if (!custId && !custPhone) return
      const res = await DatBanQuanLyApi.getAll()
      const all = Array.isArray(res?.data) ? res.data : []
      const now = Date.now()
      for (const b of all) {
        const targetMatch = (custId && Number(b.idKhachHang) === Number(custId)) || (custPhone && String(b.sdtKhachHang) === String(custPhone))
        if (!targetMatch) continue
        const t = b.thoiGianDenDuKien ? new Date(b.thoiGianDenDuKien).getTime() : (b.ngayDat ? new Date(b.ngayDat).getTime() : null)
        if (!t) continue
        const diff = t - now
        const notifiedKey = `notified_booking_${b.idDatBan}`
        const notified = localStorage.getItem(notifiedKey)
        // 15 minutes before
        if (diff <= 15 * 60 * 1000 && diff > 0 && !notified) {
          pushNotification({ title: 'Sắp đến giờ đặt bàn', message: `Đơn #${b.idDatBan} sẽ bắt đầu sau ~${Math.round(diff / 60000)} phút.`, targetKhachHangId: b.idKhachHang })
          localStorage.setItem(notifiedKey, JSON.stringify({ pre15: true }))
        }
        // at time
        if (diff <= 0 && diff > -5 * 60 * 1000 && !notified) {
          pushNotification({ title: 'Đến giờ', message: `Đơn #${b.idDatBan} đã đến giờ. Vui lòng đến cửa hàng.`, targetKhachHangId: b.idKhachHang })
          localStorage.setItem(notifiedKey, JSON.stringify({ atTime: true }))
        }
        // 15 minutes late
        if (diff < -15 * 60 * 1000 && !notified) {
          pushNotification({ title: 'Khách chậm giờ', message: `Đơn #${b.idDatBan} đã chậm hơn 15 phút. Nếu 1 giờ nữa không có mặt, đơn có thể bị hủy.`, targetKhachHangId: b.idKhachHang })
          localStorage.setItem(notifiedKey, JSON.stringify({ late15: true }))
        }
      }
    } catch (e) {
      console.warn('Lỗi kiểm tra đặt bàn:', e)
    }
  }, 60 * 1000)
})
onUnmounted(() => {
  if (bookingCheckerInterval) window.clearInterval(bookingCheckerInterval)
})
const openDatBan = () => {
  if (!authStore.isAuthenticated) {
    alert('Vui lòng đăng nhập để đặt bàn!')
    router.push('/auth')
    return
  }
  showDatBanModal.value = true
}
</script>
<template>
  <nav :class="['navbar', { 'navbar-scrolled': isScrolled, 'navbar-admin': authStore.isAdmin }]">
    <div class="nav-container">
      <div class="logo" @click="goHome" style="cursor: pointer">
        CÁI BANG <span class="gold">RESTO</span>
      </div>
      <div class="nav-right">
        <ul class="nav-links">
          <li>
            <button class="nav-link-button" @click="goHome">TRANG CHỦ</button>
          </li>

          <!-- Customer menu (visible to unauthenticated users and regular customers) -->
          <template v-if="!authStore.isAdmin">
            <li><a href="#about">GIỚI THIỆU</a></li>
            <li>
              <button class="nav-link-button" @click="router.push('/menu')">THỰC ĐƠN</button>
            </li>
            <li><a href="#contact">LIÊN HỆ</a></li>
          </template>

          <!-- Admin menu (visible only to admin/staff) - keep admin-specific pages only -->
          <template v-else>
            <li>
              <button class="nav-link-button" @click="router.push('/thucDon')">QUẢN LÝ THỰC ĐƠN</button>
            </li>
            <li>
              <button class="nav-link-button" @click="router.push('/hoa-don')">HÓA ĐƠN</button>
            </li>
            <li>
              <button class="nav-link-button" @click="router.push('/ban-hang')">Bán Hàng</button>
            </li>
            <li>
              <button class="nav-link-button" @click="router.push('/giam-gia')">GIẢM GIÁ</button>
            </li>
            <li><RouterLink to="/ban" class="nav-link-button">BÀN</RouterLink></li>
            <li>
              <RouterLink to="/dat-ban-quan-ly" class="nav-link-button">ĐẶT BÀN QUẢN LÝ</RouterLink>
            </li>
            <li>
              <RouterLink to="/tai-khoan" class="nav-link-button">QUẢN LÝ TÀI KHOẢN</RouterLink>
            </li>
            <li><RouterLink to="/thong-ke" class="nav-link-button">Thống kê</RouterLink></li>
            <li><RouterLink to="/khu-vuc" class="nav-link-button">KHU VỰC</RouterLink></li>
            <li><RouterLink to="/coc" class="nav-link-button">TIỀN CỌC</RouterLink></li>
          </template>
        </ul>
        <div class="auth-group">
          <div
            v-if="!authStore.isAuthenticated"
            class="login-trigger"
            @click="goToAuth"
            title="Đăng nhập"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="22"
              height="22"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
              class="icon-user"
            >
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span class="login-label">ĐĂNG NHẬP</span>
          </div>
          <button
            v-if="!authStore.isAuthenticated"
            class="btn-register-nav"
            @click="goToRegister"
            title="Đăng ký"
          >
            ĐĂNG KÝ
          </button>
          <UserProfileDropdown v-else />
          <div v-if="authStore.isAuthenticated" class="notif-bell" @click="toggleNotif" title="Thông báo">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="icon-bell">
              <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0 1 18.6 14.6V11a6 6 0 1 0-12 0v3c0 .538-.214 1.055-.595 1.445L4 17h5"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span v-if="unreadCount>0" class="notif-count">{{ unreadCount }}</span>
          </div>
          <button class="btn-reservation" @click="openDatBan">ĐẶT BÀN NGAY</button>
          <div v-if="showNotifPanel" class="notif-panel-wrapper">
            <NotificationPanel @close="showNotifPanel=false" />
          </div>
          <div v-if="showDatBanModal" class="modal-overlay">
            <div class="modal-content">
              <button class="close-btn" @click="showDatBanModal = false">ĐÓNG</button>
              <DatBanView />
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>
<style scoped>
.navbar {
  position: fixed;
  top: 0;
  width: 100%;
  padding: 35px 0;
  z-index: 1000;
  transition: 0.5s ease;
}
.navbar-scrolled {
  padding: 15px 0;
  background: rgba(10, 10, 10, 0.98);
  border-bottom: 1px solid rgba(197, 160, 89, 0.2);
}
.navbar-admin {
  background: rgba(255, 248, 234, 0.96);
  border-bottom: 1px solid #e6d2aa;
  box-shadow: 0 8px 22px rgba(103, 72, 32, 0.08);
}
.navbar-admin .logo,
.navbar-admin .nav-links a,
.navbar-admin .nav-link-button,
.navbar-admin .login-trigger,
.navbar-admin .notif-bell,
.navbar-admin .btn-reservation,
.navbar-admin .btn-register-nav {
  color: #6b4728;
}
.navbar-admin .nav-links a:hover,
.navbar-admin .nav-link-button:hover,
.navbar-admin .nav-link-button.router-link-active {
  color: #8b5e34;
}
.navbar-admin .btn-reservation,
.navbar-admin .btn-register-nav {
  border-color: #d8a85c;
}
.navbar-admin .btn-reservation:hover,
.navbar-admin .btn-register-nav:hover {
  background: #d8a85c;
  color: #3d2814;
}
.navbar-admin .notif-count {
  background: #d8a85c;
  color: #3d2814;
}
.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 40px;
}
.nav-links {
  display: flex;
  list-style: none;
  gap: 30px;
  margin: 0;
  padding: 0;
}
.nav-links a,
.nav-link-button {
  color: #fff;
  text-decoration: none;
  font-size: 0.75rem;
  letter-spacing: 2px;
  transition: 0.3s;
  opacity: 0.7;
}
.nav-link-button {
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
}
.nav-links a:hover,
.nav-link-button:hover {
  opacity: 1;
  color: #c5a059;
}
.auth-group {
  display: flex;
  align-items: center;
  gap: 25px;
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  padding-left: 25px;
}
.login-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #fff;
  transition: 0.3s;
}
.login-label {
  font-size: 0.7rem;
  letter-spacing: 1px;
  font-weight: 500;
}
.logo {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  letter-spacing: 3px;
  font-weight: 700;
  color: #fff;
}
.gold {
  color: #c5a059;
}
.btn-reservation {
  background: transparent;
  border: 1px solid #c5a059;
  color: #c5a059;
  padding: 10px 22px;
  cursor: pointer;
  letter-spacing: 1px;
  font-size: 0.75rem;
  font-weight: 600;
  transition: 0.4s;
}
.btn-reservation:hover {
  background: #c5a059;
  color: #000;
}
.btn-register-nav {
  background: transparent;
  border: 1px solid rgba(197, 160, 89, 0.6);
  color: #c5a059;
  padding: 8px 18px;
  cursor: pointer;
  letter-spacing: 1px;
  font-size: 0.7rem;
  font-weight: 600;
  transition: 0.3s;
}
.btn-register-nav:hover {
  border-color: #c5a059;
  background: rgba(197, 160, 89, 0.1);
}
.notif-bell { position: relative; cursor: pointer; color: #fff }
.notif-bell .icon-bell { opacity: 0.9 }
.notif-count { position: absolute; top: -6px; right: -6px; background:#c5a059; color:#000; padding:2px 6px; border-radius:12px; font-size:11px; font-weight:700 }
.notif-panel-wrapper { position: relative }
@media (max-width: 1024px) {
  .nav-links {
    display: none;
  }
  .auth-group {
    border: none;
    padding: 0;
  }
  .login-label {
    display: none;
  }
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  z-index: 9999;
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  padding: 0;
}
.modal-content {
  width: 100vw;
  height: 100vh;
  max-width: none;
  max-height: none;
  margin: 0;
  border-radius: 0;
  overflow-y: auto;
  background: rgba(10, 10, 10, 0.85);
  border: none;
  padding: 30px;
}
.close-btn {
  position: sticky;
  top: 0;
  background: transparent;
  border: 1px solid #c5a059;
  color: #c5a059;
  padding: 10px 18px;
  margin-bottom: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8%;
}
.close-btn:hover {
  background: #c5a059;
  color: #111;
}
</style>
