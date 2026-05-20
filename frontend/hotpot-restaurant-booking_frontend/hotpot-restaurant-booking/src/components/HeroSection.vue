<script setup lang="ts"></script>
<template>
  <section id="home" class="hero-parallax">
    <div class="hero-overlay"></div>
    <div class="hero-content">
      <p class="est">ESTABLISHED 2026</p>
      <h1 class="title">
        LẨU ẾCH <br />
        <span class="gold-italic">Cái Bang</span>
      </h1>
      <div class="line"></div>
      <p class="slogan">Hương vị đồng quê trong không gian đương đại</p>
    </div>
  </section>
</template>

<style scoped>
.hero-parallax {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
  color: #fff;
  /* HIỆU ỨNG TRÔI: */
  background-image: url('https://images.unsplash.com/photo-1544148103-0773bf10d330?q=80');
  background-attachment: fixed; /* Giữ ảnh cố định khi cuộn */
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6); /* Làm tối ảnh nền để nổi bật chữ */
}

.hero-content {
  position: relative;
  z-index: 1;
}
.title {
  font-size: 6rem;
  font-family: 'Playfair Display', serif;
  line-height: 1.1;
}
.gold-italic {
  color: #c5a059;
  font-style: italic;
}
.line {
  width: 60px;
  height: 1px;
  background: #c5a059;
  margin: 30px auto;
}
.est {
  color: #c5a059;
  letter-spacing: 8px;
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .title {
    font-size: 3.5rem;
  }
  /* Trên mobile background-attachment: fixed thường bị lỗi nên ta tắt đi */
  .hero-parallax {
    background-attachment: scroll;
  }
}
</style>
