<script setup lang="ts">
// Dùng ../ để quay lại thư mục src, sau đó đi vào components
import Navbar from '../components/NavbarSection.vue'
import About from '../components/AboutSection.vue'
import Menu from '../components/MenuSection.vue'
import Contact from '../components/ContactSection.vue'
import Hero from '../components/HeroSection.vue'
</script>

<template>
  <div class="home-view">
    <Navbar />

    <Hero />

    <About />
    <Menu />

    <Contact />

    <footer class="simple-copyright">
      <p>© 2026 CÁI BANG RESTO - Thiết kế bởi nx_duxng</p>
    </footer>
  </div>
</template>

<style scoped>
/* Bạn có thể xóa bớt CSS của Hero và Contact ở đây
   vì chúng đã nằm trong file component riêng rồi */
.simple-copyright {
  padding: 40px;
  text-align: center;
  font-size: 0.7rem;
  color: #444;
  background: #050505;
}
</style>
