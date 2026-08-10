import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/AuthStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/auth',
      name: 'auth',
      component: () => import('@/views/AuthView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/customer-profile',
      name: 'customer-profile',
      component: () => import('@/views/CustomerProfileView.vue'),
    },
    {
      path: '/hoa-don',
      name: 'hoa-don',
      component: () => import('@/views/HoaDonView.vue'),
    },
    {
      path: '/giam-gia',
      name: 'giam-gia',
      component: () => import('@/views/GiamGiaView.vue'),
    },
    {
      path: '/mon',
      name: 'mon',
      component: () => import('@/views/MonView.vue'),
    },
    {
      path: '/combo',
      name: 'combo',
      component: () => import('../views/ComBoView.vue'),
    },
    {
      path: '/CTCB',
      name: 'CTCB',
      component: () => import('../views/ChiTietComBoView.vue'),
    },
    {
      path: '/danhMuc',
      name: 'danhMuc',
      component: () => import('../views/DanhMucView.vue'),
    },
    {
      path: '/CTGGM',
      name: 'CTGGM',
      component: () => import('../views/ChiTietGiamGiaMonView.vue'),
    },
    {
      path: '/GGM',
      name: 'GGM',
      component: () => import('../views/DotGiamGiaView.vue'),
    },
    {
      path: '/thucDon',
      name: 'thucDon',
      component: () => import('../views/ThucDonView.vue'),
    },
    {
      path: '/ban',
      name: 'ban',
      component: () => import('@/views/BanView.vue'),
    },
    {
      path: '/dat-ban-quan-ly',
      name: 'dat-ban-quan-ly',
      component: () => import('@/views/DatBanQuanLyView.vue'),
    },
    {
      path: '/shift-management',
      name: 'shift-management',
      component: () => import('@/views/ShiftManagementView.vue'),
    },
    {
      path: '/khach-hang',
      name: 'khach-hang',
      redirect: { name: 'thucDon' },
    },
    {
      path: '/khu-vuc',
      name: 'khu-vuc',
      component: () => import('@/views/KhuVucList.vue'),
    },
    {
      path: '/coc',
      name: 'coc',
      component: () => import('@/views/TienCocList.vue'),
    },
    {
      path: '/nhan-vien',
      name: 'nhan-vien',
      redirect: { name: 'thucDon' },
    },
    {
      path: '/tai-khoan',
      name: 'tai-khoan',
      component: () => import('@/views/QuanLyTaiKhoanView.vue'),
    },
    {
      path: '/chuc-vu',
      name: 'chuc-vu',
      redirect: { name: 'thucDon' },
    },
    {
      path: '/tin-nhan',
      name: 'tin-nhan',
      component: () => import('@/views/TinNhanView.vue'),
    },
    {
      path: '/thong-ke',
      name: 'thong-ke',
      component: () => import('@/views/ThongKeView.vue'),
    },
    {
      path: '/ban-hang',
      name: 'ban-hang',
      component: () => import('@/views/BanHang.vue'),
    },
    {
      path: '/payment-success',
      name: 'payment-success',
      component: () => import('@/views/PaymentSuccess.vue'),
    },
    {
      path: '/payment-failed',
      name: 'payment-failed',
      component: () => import('@/views/PaymentFailed.vue'),
    },
    {
      path: '/order',
      name: 'order',
      component: () => import('@/views/OrderView.vue'),
    },
    {
      path: '/bar',
      name: 'bar',
      component: () => import('../views/BarDisplay.vue'),
    },
    {
      path: '/bep',
      name: 'bep',
      component: () => import('../views/BepDisplay.vue'),
    },
  ],
})

// Cập nhật Navigation Guard sử dụng 'return' thay vì callback 'next()'
router.beforeEach((to) => {
  const authStore = useAuthStore()
  const authRoutes = ['auth', 'register']
  const internalPages = [
    'thucDon',
    'combo',
    'mon',
    'danhMuc',
    'hoa-don',
    'ban-hang',
    'giam-gia',
    'ban',
    'CTGGM',
    'CTCB',
    'dat-ban-quan-ly',
    'shift-management',
    'tai-khoan',
    'tin-nhan',
    'thong-ke',
    'khu-vuc',
    'coc',
    'order',
    'bar',
    'bep',
  ]

  const isAuthenticated = authStore.isAuthenticated
  const isUser = authStore.isUser

  // Trang đăng nhập/đăng ký cho phép truy cập
  if (authRoutes.includes(to.name as string)) {
    return
  }

  // Chưa đăng nhập: Cho phép truy cập các trang public, chặn trang nội bộ
  if (!isAuthenticated) {
    if (internalPages.includes(to.name as string)) {
      return { name: 'auth', query: { redirect: to.fullPath } }
    }
    return
  }

  // Đã đăng nhập tài khoản khách hàng (isUser)
  if (isUser) {
    if (internalPages.includes(to.name as string)) {
      return { name: 'home' }
    }
  } else {
    // Tài khoản nhân viên/quản trị
    const allowPages = ['home', 'payment-success', 'payment-failed']

    if (!internalPages.includes(to.name as string) && !allowPages.includes(to.name as string)) {
      return { name: 'dat-ban-quan-ly' }
    }
  }
})

export default router