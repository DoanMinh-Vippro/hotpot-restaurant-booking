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
      path: '/ban',
      name: 'ban',
      component: () => import('@/views/BanView.vue'),
    }
  ],
})

export default router
