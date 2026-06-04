<script setup lang="ts">
// Import 3 view hiện tại vào như các component
import MonView from './MonView.vue'
import ComBoView from './ComBoView.vue'
import ChiTietComBoView from './ChiTietComBoView.vue'
</script>

<template>
  <div class="thuc-don-tong">
    <section class="menu-section">
      <h2 class="section-title">QUẢN LÝ MÓN</h2>
      <MonView />
    </section>

    <section class="menu-section">
      <h2 class="section-title">QUẢN LÝ COMBO</h2>
      <ComBoView />
    </section>

    <section class="menu-section">
      <h2 class="section-title">CHI TIẾT COMBO</h2>
      <ChiTietComBoView />
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