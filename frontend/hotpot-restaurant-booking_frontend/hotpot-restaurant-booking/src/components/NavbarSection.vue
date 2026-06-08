<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import DatBanView from '@/views/DatBanView.vue'

const router = useRouter()
const authStore = useAuthStore()
const isScrolled = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  
  // Đảm bảo trạng thái auth được khôi phục khi F5
  const token = localStorage.getItem('token');
  if (token && !authStore.isAuthenticated) {
    authStore.decodeToken(token);
  }
})

onUnmounted(() => window.removeEventListener('scroll', handleScroll))

const handleLogout = () => {
  authStore.logout()
  router.push('/')
}

const goToAuth = () => {
  router.push('/auth')
}

const showDatBanModal = ref(false)

const openDatBan = () => {
  if (!authStore.isAuthenticated) {
    alert("Vui lòng đăng nhập để đặt bàn!")
    router.push('/auth')
    return
  }
  showDatBanModal.value = true
}
</script>

<template>
  <nav :class="['navbar', { 'navbar-scrolled': isScrolled }]">
    <div class="nav-container">
      <div class="logo" @click="router.push('/')" style="cursor: pointer">
        CÁI BANG <span class="gold">RESTO</span>
      </div>

      <div class="nav-right">
        <ul class="nav-links">
          <li><a href="#home">TRANG CHỦ</a></li>
          <li><a href="#about">GIỚI THIỆU</a></li>
          <li><button class="nav-link-button" @click="router.push('/thucDon')">THỰC ĐƠN</button></li>
          
          <template v-if="authStore.isAdmin">
            <li><button class="nav-link-button" @click="router.push('/dotGiamGia')">ĐỢT GIẢM GIÁ</button></li>
            <li><button class="nav-link-button" @click="router.push('/hoa-don')">HÓA ĐƠN</button></li>
            <li><button class="nav-link-button" @click="router.push('/giam-gia')">GIẢM GIÁ</button></li>
            <li><RouterLink to="/ban" class="nav-link-button">BÀN</RouterLink></li>
            <li><RouterLink to="/dat-ban-quan-ly" class="nav-link-button">ĐẶT BÀN QUẢN LÝ</RouterLink></li>
          </template>
          
          <li><a href="#contact">LIÊN HỆ</a></li>
        </ul>

        <div class="auth-group">
          <div v-if="!authStore.isAuthenticated" class="login-trigger" @click="goToAuth" title="Đăng nhập">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="icon-user">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span class="login-label">ĐĂNG NHẬP</span>
          </div>

          <div v-else class="login-trigger" @click="handleLogout" title="Đăng xuất">
            <span class="login-label">ĐĂNG XUẤT</span>
          </div>

          <button class="btn-reservation" @click="openDatBan">ĐẶT BÀN NGAY</button>

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
.navbar { position: fixed; top: 0; width: 100%; padding: 35px 0; z-index: 1000; transition: 0.5s ease; }
.navbar-scrolled { padding: 15px 0; background: rgba(10, 10, 10, 0.98); border-bottom: 1px solid rgba(197, 160, 89, 0.2); }
.nav-container { max-width: 1400px; margin: 0 auto; padding: 0 60px; display: flex; justify-content: space-between; align-items: center; }
.nav-right { display: flex; align-items: center; gap: 40px; }
.nav-links { display: flex; list-style: none; gap: 30px; margin: 0; padding: 0; }
.nav-links a, .nav-link-button { color: #fff; text-decoration: none; font-size: 0.75rem; letter-spacing: 2px; transition: 0.3s; opacity: 0.7; }
.nav-link-button { border: 0; padding: 0; background: transparent; cursor: pointer; }
.nav-links a:hover, .nav-link-button:hover { opacity: 1; color: #c5a059; }
.auth-group { display: flex; align-items: center; gap: 25px; border-left: 1px solid rgba(255, 255, 255, 0.1); padding-left: 25px; }
.login-trigger { display: flex; align-items: center; gap: 10px; cursor: pointer; color: #fff; transition: 0.3s; }
.login-label { font-size: 0.7rem; letter-spacing: 1px; font-weight: 500; }
.logo { font-family: 'Playfair Display', serif; font-size: 1.5rem; letter-spacing: 3px; font-weight: 700; color: #fff; }
.gold { color: #c5a059; }
.btn-reservation { background: transparent; border: 1px solid #c5a059; color: #c5a059; padding: 10px 22px; cursor: pointer; letter-spacing: 1px; font-size: 0.75rem; font-weight: 600; transition: 0.4s; }
.btn-reservation:hover { background: #c5a059; color: #000; }
@media (max-width: 1024px) { .nav-links { display: none; } .auth-group { border: none; padding: 0; } .login-label { display: none; } }
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.9); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.modal-content {
  background: #111; padding: 20px; width: 90%; max-width: 900px;
  max-height: 90vh; overflow-y: auto; border: 1px solid #c5a059;
}
.close-btn {
  background: #c5a059; border: none; padding: 8px 15px;
  color: #000; font-weight: bold; cursor: pointer; margin-bottom: 15px;
}
</style>