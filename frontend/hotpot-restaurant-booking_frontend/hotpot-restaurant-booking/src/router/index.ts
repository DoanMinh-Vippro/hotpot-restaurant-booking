import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

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
      path: '/khach-hang',
      name: 'khach-hang',
      component: () => import('@/views/KhachHangView.vue'), // Sửa khachhang/KhachHangView
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
      path: '/dat-ban',
      name: 'dat-ban',
      component: () => import('@/views/DatBanView.vue'),
    },
    {
      path: '/nhan-vien',
      name: 'nhan-vien',
      component: () => import('@/views/NhanVienView.vue'),
    },
    {
      path: '/tai-khoan',
      name: 'tai-khoan',
      component: () => import('@/views/TaiKhoanView.vue'),
    },
    {
      path: '/chuc-vu',
      name: 'chuc-vu',
      component: () => import('@/views/ChucVuView.vue'),
    },
    {
      path: '/thong-ke',
      name: 'thong-ke',
      component: () => import('@/views/ThongKeView.vue'),
    },
  ],
})



export default router
