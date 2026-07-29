import { createRouter, createWebHistory } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import HomeView from '@/views/HomeView.vue'

const getStoredRole = (): string => {
  const token = localStorage.getItem('token')
  if (!token) return ''

  try {
    const decoded = jwtDecode<{ scope?: string }>(token)
    return String(decoded.scope || '').toUpperCase()
  } catch {
    return ''
  }
}

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
      // Cách 1: Lazy-loading (Tải chậm khi truy cập - Khuyên dùng cho trang phụ)
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/auth',
      name: 'auth',
      // Tương tự, dùng lazy-loading giúp tối ưu hóa dung lượng ứng dụng ban đầu
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
    //     {
    //   path: '/menu',
    //   name: 'menu',
    //   component: () => import('../views/MenuView.vue'),
    // },
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
      path: '/khach-hang',
      name: 'khach-hang',
      redirect: { name: 'thucDon' },
    },
    {
      path: '/khu-vuc',
      name: 'khu-vuc',
      component: () => import('@/views/KhuVucList.vue'), // Sửa khuvuc/KhuVucList
    },
    {
      path: '/coc',
      name: 'coc',
      component: () => import('@/views/TienCocList.vue'), // Sửa coc/TienCocList
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
      path: '/shift-management',
      name: 'shift-management',
      component: () => import('@/views/ShiftManagementView.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  if (to.name !== 'shift-management') {
    next()
    return
  }

  const role = getStoredRole()
  const isAllowedShiftManager = ['ROLE_ADMIN', 'ADMIN', 'ROLE_STAFF', 'STAFF', 'CASHIER'].includes(role)

  if (!isAllowedShiftManager) {
    alert('Bạn không có quyền truy cập trang này')
    next(false)
    return
  }

  next()
})

export default router
