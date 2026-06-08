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
      path: '/dotGiamGia',
      name: 'dotGiamGia',
      component: () => import('../views/DotGiamGiaView.vue'),
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
      path: '/dat-ban',
      name: 'dat-ban',
      component: () => import('@/views/DatBanView.vue'),
    }
  ],
})

export default router
