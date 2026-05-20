<script setup lang="ts"></script>
<template>
  <section id="about" class="about-section">
    <!-- Lớp phủ tối giúp text hiển thị rõ ràng trên nền ảnh lướt -->
    <div class="about-bg-overlay"></div>

    <div class="about-content-wrapper">
      <!-- KHỐI CŨ -->
      <div class="about-container">
        <div class="about-text">
          <p class="subtitle">CÂU CHUYỆN CỦA CHÚNG TÔI</p>
          <h2>
            Di Sản Ẩm Thực <br />
            <span class="gold-italic">Đồng Quê</span>
          </h2>
          <p class="description">
            Tại Cái Bang Resto, mỗi món ăn là một tác phẩm nghệ thuật. Chúng tôi gìn giữ những giá
            trị truyền thống và kết hợp tinh hoa đương đại để mang lại trải nghiệm độc bản cho thực
            khách.
          </p>
        </div>
        <div class="about-image">
          <img
            src="https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?q=80"
            alt="About"
          />
        </div>
      </div>

      <!-- KHỐI BỔ SUNG -->
      <div class="about-container about-extra">
        <div class="about-image">
          <img
            src="https://images.unsplash.com/photo-1544025162-d76694265947?q=80"
            alt="Our Ingredients"
          />
        </div>
        <div class="about-text">
          <p class="subtitle">TỪ TRANG TRẠI ĐẾN BÀN ĂN</p>
          <h2>
            Nguyên Liệu <br />
            <span class="gold-italic">Tinh Tuyển</span>
          </h2>
          <p class="description">
            Chúng tôi tin rằng món ăn ngon bắt nguồn từ nguyên liệu sạch. Cái Bang Resto tự hào hợp
            tác với các trang trại địa phương để mang đến những nông sản tươi ngon nhất trong ngày,
            bảo tồn trọn vẹn hương vị tự nhiên của đất trời.
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.about-section {
  padding: 120px 10%;
  position: relative;
  color: #fff;

  /* ĐÃ THAY ĐỔI: Ảnh nền mới mang phong cách hầm rượu / bếp gỗ mộc mạc ấm cúng khác biệt với trang chủ */
  background-image: url('https://images.unsplash.com/photo-1555396273-367ea4eb4db5?q=80');

  background-attachment: fixed;
  background-position: center center;
  background-repeat: no-repeat;
  background-size: cover;
  will-change: background-position;
  overflow: hidden;
}

/* LỚP PHỦ GIÚP NỀN TỐI XUỐNG ĐỂ CHỮ NỔI BẬT */
.about-bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(
    12,
    12,
    12,
    0.9
  ); /* Tăng nhẹ độ tối lên 90% giúp chữ của bạn cực kỳ nét và nổi bật */
  z-index: 1;
}

/* Đảm bảo nội dung luôn nổi lên trên lớp nền lướt */
.about-content-wrapper {
  position: relative;
  z-index: 2;
}

.about-container {
  display: flex;
  align-items: center;
  gap: 80px;
  max-width: 1400px;
  margin: 0 auto;
}
.about-text {
  flex: 1;
}
.subtitle {
  color: #c5a059;
  letter-spacing: 5px;
  font-size: 0.8rem;
  margin-bottom: 20px;
}
.about-text h2 {
  font-size: 3.5rem;
  font-family: 'Playfair Display', serif;
  line-height: 1.2;
  margin-bottom: 30px;
}
.gold-italic {
  color: #c5a059;
  font-style: italic;
}
.description {
  color: #ccc;
  line-height: 1.8;
  margin-bottom: 40px;
}
.about-image {
  flex: 1;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}
.about-image img {
  width: 100%;
  height: 600px;
  object-fit: cover;
  transition: 1s;
}
.about-image img:hover {
  transform: scale(1.05);
}
.btn-outline {
  background: transparent;
  border: 1px solid #c5a059;
  color: #c5a059;
  padding: 15px 40px;
  cursor: pointer;
  transition: 0.3s;
}
.btn-outline:hover {
  background: #c5a059;
  color: #000;
}

/* CSS BỔ SUNG: Khoảng cách giữa 2 khối */
.about-extra {
  margin-top: 120px;
}

/* Responsive cho thiết bị di động */
@media (max-width: 992px) {
  .about-container {
    flex-direction: column;
    gap: 40px;
  }
  .about-extra {
    flex-direction: column-reverse;
    margin-top: 80px;
  }
  .about-text h2 {
    font-size: 2.5rem;
  }
}
</style>
