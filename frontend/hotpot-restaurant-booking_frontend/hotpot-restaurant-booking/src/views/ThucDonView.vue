<script setup lang="ts">
import { useRouter } from 'vue-router' // Import useRouter để thực hiện điều hướng
// Import 3 view hiện tại vào như các component
import MonView from './MonView.vue'
import ComBoView from './ComBoView.vue'

const router = useRouter()

// Hàm xử lý quay về trang chủ (Khớp name: 'home' trong router/index.ts)
const quayVeTrangChu = () => {
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="thuc-don-tong">
    <div class="thanh-dieu-huong-top">
      <button class="nut-trang-chu" @click="quayVeTrangChu">
        🏠 Quay về Trang chủ
      </button>
    </div>

    <section class="menu-section">
      <h2 class="section-title">QUẢN LÝ MÓN</h2>
      <MonView />
    </section>

    <section class="menu-section">
      <h2 class="section-title">QUẢN LÝ COMBO</h2>
      <ComBoView />
    </section>
  </div>
</template>

<style scoped>
.thuc-don-tong {
  background: #0f0f0f;
  min-height: 100vh;
  padding-top: 100px; /* Bù lại khoảng trống cho Navbar */
  padding-bottom: 50px;
}

/* Khu vực bao bọc nút trang chủ */
.thanh-dieu-huong-top {
  max-width: 1400px; /* Hoặc để tương đương với kích thước các bảng của bạn */
  margin: 0 auto 20px;
  padding: 0 32px;
}

/* Thiết kế nút trang chủ mượt mà theo tông Dark Mode */
.nut-trang-chu {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #c7c7c7;
  padding: 10px 20px;
  border-radius: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-trang-chu:hover {
  background: rgba(197, 160, 89, 0.15); /* Đồng bộ tông vàng mờ #c5a059 của bạn */
  border-color: #c5a059;
  color: #c5a059;
}

.menu-section {
  margin-bottom: 60px;
  border-bottom: 1px solid rgba(197, 160, 89, 0.2);
  padding-bottom: 40px;
}

.menu-section:last-child {
  border-bottom: none;
}

.section-title {
  color: #c5a059;
  text-align: center;
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  letter-spacing: 2px;
  margin-bottom: -60px; /* Kéo component con xích lên một chút */
  z-index: 10;
  position: relative;
}
</style>