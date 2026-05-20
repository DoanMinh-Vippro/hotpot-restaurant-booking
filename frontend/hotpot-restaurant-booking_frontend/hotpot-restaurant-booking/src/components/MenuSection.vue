<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface MenuItem {
  name: string
  price: string
  desc: string
  image: string
}

const menuItems = ref<MenuItem[]>([])

const fetchMenu = async () => {
  // GIẢ LẬP: Sau này thay bằng axios.get('/api/menu')
  menuItems.value = [
    {
      name: 'Lẩu Ếch Măng Cay',
      price: '350.000đ',
      desc: 'Ếch đồng tươi, măng củ loại 1.',
      image: 'https://images.unsplash.com/photo-1544148103-0773bf10d330?q=80',
    },
    {
      name: 'Ếch Rang Muối Tuyết',
      price: '155.000đ',
      desc: 'Thịt ếch chiên giòn muối mịn.',
      image: 'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?q=80',
    },
    {
      name: 'Ếch Xào Chuối Đậu',
      price: '145.000đ',
      desc: 'Béo ngậy đậu phụ và chuối xanh.',
      image: 'https://images.unsplash.com/photo-1512058560566-427a1bd5a5bf?q=80',
    },
    {
      name: 'Da Ếch Chiên Giòn',
      price: '85.000đ',
      desc: 'Món nhắm giòn tan sốt me.',
      image: 'https://images.unsplash.com/photo-1562967914-608f82629710?q=80',
    },
  ]
}

onMounted(fetchMenu)
</script>

<template>
  <section id="menu" class="menu-section">
    <!-- Lớp phủ tối giúp text và danh sách món ăn hiển thị sắc nét trên nền ảnh lướt -->
    <div class="menu-bg-overlay"></div>

    <div class="menu-content-wrapper">
      <div class="section-header">
        <p class="subtitle">ẨM THỰC</p>
        <h2>THỰC ĐƠN ĐẶC SẮC</h2>
      </div>
      <div class="menu-grid">
        <div v-for="item in menuItems" :key="item.name" class="menu-card">
          <div class="menu-img"><img :src="item.image" :alt="item.name" /></div>
          <div class="menu-info">
            <div class="header">
              <span class="name">{{ item.name }}</span>
              <div class="dots"></div>
              <span class="price">{{ item.price }}</span>
            </div>
            <p class="desc">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.menu-section {
  padding: 120px 10%;
  position: relative;
  color: #fff;

  /* THÊM ẢNH NỀN KHÁC BIỆT VÀ CẤU HÌNH HIỆU ỨNG LƯỚT (PARALLAX) */
  background-image: url('https://images.unsplash.com/photo-1556910103-1c02745aae4d?q=80'); /* Đường dẫn ảnh nền bếp mộc mạc phía sau */
  background-attachment: fixed;
  background-position: center center;
  background-repeat: no-repeat;
  background-size: cover;
  will-change: background-position;
  overflow: hidden;
}

/* LỚP PHỦ NỀN TỐI ĐỂ GIỮ ĐỘ TƯƠNG PHẢN CHO CHỮ */
.menu-bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(10, 10, 10, 0.92); /* Độ tối 92% để các chấm dot và chữ nhỏ luôn dễ đọc */
  z-index: 1;
}

/* Đảm bảo nội dung luôn nổi hẳn lên trên lớp nền */
.menu-content-wrapper {
  position: relative;
  z-index: 2;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}
.section-header h2 {
  font-size: 3rem;
  font-family: 'Playfair Display', serif;
  margin-top: 10px;
}
.subtitle {
  color: #c5a059;
  letter-spacing: 5px;
  font-size: 0.8rem;
}
.menu-grid {
  display: grid;
  grid-template-cols: 1fr 1fr;
  gap: 50px 80px;
  max-width: 1400px;
  margin: 0 auto;
}
.menu-card {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.menu-img {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4); /* Đổ bóng nhẹ cho ảnh món ăn nổi trên nền mờ */
}
.menu-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
/* Thêm hiệu ứng phóng to nhẹ ảnh món ăn khi di chuột vào card */
.menu-card:hover .menu-img img {
  transform: scale(1.08);
}
.menu-info {
  flex: 1;
}
.header {
  display: flex;
  align-items: baseline;
}
.name {
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.dots {
  flex-grow: 1;
  border-bottom: 1px dotted rgba(197, 160, 89, 0.4); /* Tăng nhẹ độ đậm của dấu chấm */
  margin: 0 10px;
}
.price {
  color: #c5a059;
  font-weight: bold;
}
.desc {
  font-size: 0.85rem;
  color: #aaa; /* Tăng nhẹ độ sáng từ #888 lên #aaa giúp đọc thành phần món dễ hơn */
  font-style: italic;
  margin-top: 8px;
}

/* Khử grid trên điện thoại để menu không bị bóp nghẹt */
@media (max-width: 992px) {
  .menu-grid {
    grid-template-cols: 1fr;
    gap: 40px;
  }
}
</style>
