<script setup lang="ts"></script>
<template>
  <section id="contact" class="contact-parallax">
    <div class="contact-overlay"></div>
    <div class="contact-container">
      <div class="contact-box">
        <p class="subtitle">LIÊN HỆ</p>
        <h2 class="gold-italic">Đặt Bàn Ngay</h2>
        <div class="info-grid">
          <div class="info-item">
            <h3>ĐỊA CHỈ</h3>
            <p>123 Đường Láng, Đống Đa, Hà Nội</p>
          </div>
          <div class="info-item">
            <h3>HOTLINE</h3>
            <p>0912 345 678</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.contact-parallax {
  padding: 180px 0; /* Tăng nhẹ padding để tạo không gian cho ảnh lướt rõ hơn */
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80');

  /* Cấu hình giúp tạo hiệu ứng lướt mượt ở chính giữa */
  background-attachment: fixed;
  background-position: center center;
  background-repeat: no-repeat;
  background-size: cover;

  /* Kích hoạt tăng tốc phần cứng GPU giúp trình duyệt xử lý hiệu ứng cuộn lướt mượt mà, không bị khựng */
  will-change: background-position;

  display: flex;
  justify-content: center;
  overflow: hidden;
}

.contact-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(12, 12, 12, 0.9) 0%,
    rgba(10, 10, 10, 0.75) 50%,
    rgba(12, 12, 12, 0.9) 100%
  ); /* Thay bằng gradient giúp phần ảnh ở giữa lộ ra sâu và lướt đẹp hơn */
}

.contact-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 800px;
  padding: 0 20px; /* Thêm padding tránh bị lỗi hiển thị trên thiết bị nhỏ */
}
.contact-box {
  text-align: center;
  border: 1px solid rgba(197, 160, 89, 0.2);
  padding: 60px;
  background: rgba(10, 10, 10, 0.6);
  backdrop-filter: blur(
    8px
  ); /* Thêm hiệu ứng kính mờ nhẹ giúp hộp nội dung tách biệt với nền lướt phía sau */
  -webkit-backdrop-filter: blur(8px);
}
.subtitle {
  color: #c5a059;
  letter-spacing: 5px;
}
.gold-italic {
  color: #c5a059;
  font-style: italic;
  font-family: 'Playfair Display', serif;
  font-size: 3rem;
  margin-bottom: 40px;
}
.info-grid {
  display: grid;
  grid-template-cols: 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}
@media (max-width: 600px) {
  .info-grid {
    grid-template-cols: 1fr; /* Tự động xuống dòng trên điện thoại để không bị vỡ khung */
    gap: 20px;
  }
}
.info-item h3 {
  font-size: 0.9rem;
  letter-spacing: 2px;
  color: #fff;
}
.info-item p {
  color: #888;
  margin-top: 10px;
}
.btn-gold {
  background: #c5a059;
  border: none;
  padding: 15px 40px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}
.btn-gold:hover {
  background: #fff;
  color: #000;
}
</style>
