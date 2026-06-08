<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
// Đã import các thành phần cần thiết để gọi API
import AuthApi from '@/api/AuthApi'
import { useAuthStore } from '@/stores/AuthStore'

const router = useRouter()
const authStore = useAuthStore() // Khởi tạo store để lưu token sau khi login

const isLoginMode = ref(true)

// [SỬA]: Thay đổi các biến lưu dữ liệu form cho khớp với database
const username = ref('')     // Thay cho email
const password = ref('')
const confirmPassword = ref('')
// [XÓA]: Đã xóa fullName vì database bạn không dùng

const handleSubmit = async () => {
  try {
    if (isLoginMode.value) {
      console.log('Đang xử lý đăng nhập...')
      // SỬA: Phải khớp với tên biến trong class Java (tenDangNhap, matKhau)
      const res = await AuthApi.login({ 
        tenDangNhap: username.value, 
        matKhau: password.value 
      })
      
      authStore.login(res.data.token)
      alert('Đăng nhập thành công!')
      router.push('/')
      
    } else {
      // ... phần đăng ký nếu có
    }
  } catch (error) {
    console.error()
    alert('Đăng nhập thất bại, vui lòng kiểm tra lại tài khoản!')
  }
}

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
  // [SỬA]: Reset lại các biến mới
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
}
</script>

<template>
  <div class="auth-wrapper">
    <div class="back-home" @click="router.push('/')">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="19" y1="12" x2="5" y2="12"></line>
        <polyline points="12 19 5 12 12 5"></polyline>
      </svg>
      QUAY LẠI TRANG CHỦ
    </div>

    <div class="auth-container">
      <div class="auth-image-side">
        <div class="overlay"></div>
        <div class="brand-content">
          <h2 class="brand-logo">CÁI BANG <span class="gold">RESTO</span></h2>
          <p class="brand-slogan">Nơi tinh hoa ẩm thực giao thoa cùng không gian đẳng cấp.</p>
        </div>
      </div>

      <div class="auth-form-side">
        <div class="form-box">
          <div class="form-header">
            <h2>{{ isLoginMode ? 'ĐĂNG NHẬP' : 'TẠO TÀI KHOẢN' }}</h2>
            <p>{{ isLoginMode ? 'Chào mừng bạn trở lại với chúng tôi!' : 'Đăng ký để nhận nhiều ưu đãi đặc quyền.' }}</p>
          </div>

          <form @submit.prevent="handleSubmit" class="main-form">
            <div class="input-group">
              <label>TÊN ĐĂNG NHẬP</label>
              <input v-model="username" type="text" placeholder="Nhập tên đăng nhập" required />
            </div>

            <div class="input-group">
              <label>MẬT KHẨU</label>
              <input v-model="password" type="password" placeholder="••••••••" required />
            </div>

            <div v-if="!isLoginMode" class="input-group">
              <label>NHẬP LẠI MẬT KHẨU</label>
              <input v-model="confirmPassword" type="password" placeholder="••••••••" required />
            </div>

            <div v-if="isLoginMode" class="forgot-password">
              <a href="#forgot">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="btn-auth-submit">
              {{ isLoginMode ? 'ĐĂNG NHẬP NGAY' : 'ĐĂNG KÝ TÀI KHOẢN' }}
            </button>
          </form>

          <div class="form-toggle-footer">
            <span>{{ isLoginMode ? 'Bạn chưa có tài khoản?' : 'Bạn đã có tài khoản rồi?' }}</span>
            <button @click="toggleMode" class="btn-toggle">
              {{ isLoginMode ? 'Đăng ký ngay' : 'Đăng nhập ngay' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Reset và bao bọc toàn màn hình */
.auth-wrapper {
  background-color: #0a0a0a;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  font-family: 'Montserrat', sans-serif;
}

/* Nút quay lại góc trên */
.back-home {
  position: absolute;
  top: 30px;
  left: 40px;
  color: #fff;
  opacity: 0.6;
  font-size: 0.75rem;
  letter-spacing: 2px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: 0.3s;
  z-index: 10;
}
.back-home:hover {
  opacity: 1;
  color: #c5a059;
}

/* Khung Container chính chia đôi */
.auth-container {
  width: 100%;
  max-width: 1100px;
  height: 650px;
  background: #111111;
  display: flex;
  border: 1px solid rgba(197, 160, 89, 0.15);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

/* 1. Thiết kế bên phía Ảnh thương hiệu */
.auth-image-side {
  flex: 1;
  position: relative;
  /* Bạn thay url ảnh không gian nhà hàng luxury của bạn ở đây nhé */
  background-image: url('https://images.unsplash.com/photo-1514933651103-005eec06c04b?q=80&w=1000&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  padding: 40px;
}
.auth-image-side .overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(10, 10, 10, 0.95) 20%, rgba(10, 10, 10, 0.3));
}
.brand-content {
  position: relative;
  z-index: 2;
}
.brand-logo {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  letter-spacing: 3px;
  color: #fff;
  margin-bottom: 10px;
}
.gold {
  color: #c5a059;
}
.brand-slogan {
  color: #ccc;
  font-size: 0.85rem;
  letter-spacing: 1px;
  line-height: 1.6;
}

/* 2. Thiết kế bên phía Form */
.auth-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #121212;
}
.form-box {
  width: 100%;
  max-width: 380px;
}
.form-header h2 {
  color: #fff;
  font-size: 1.4rem;
  letter-spacing: 2px;
  margin-bottom: 8px;
  font-weight: 600;
}
.form-header p {
  color: #777;
  font-size: 0.8rem;
  line-height: 1.5;
  margin-bottom: 30px;
}

/* Input Styling tương thích tone tối */
.main-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.input-group label {
  color: #c5a059;
  font-size: 0.65rem;
  letter-spacing: 1.5px;
  font-weight: 600;
}
.input-group input {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 12px 15px;
  color: #fff;
  font-size: 0.85rem;
  outline: none;
  transition: 0.3s;
}
.input-group input:focus {
  border-color: #c5a059;
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 8px rgba(197, 160, 89, 0.2);
}

/* Quên mật khẩu */
.forgot-password {
  text-align: right;
}
.forgot-password a {
  color: #777;
  font-size: 0.75rem;
  text-decoration: none;
  transition: 0.3s;
}
.forgot-password a:hover {
  color: #c5a059;
}

/* Nút submit chính */
.btn-auth-submit {
  background: #c5a059;
  color: #000;
  border: none;
  padding: 14px;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.4s;
  margin-top: 10px;
}
.btn-auth-submit:hover {
  background: #e2be7a;
  box-shadow: 0 5px 15px rgba(197, 160, 89, 0.3);
}

/* Footer chuyển đổi */
.form-toggle-footer {
  margin-top: 30px;
  text-align: center;
  font-size: 0.8rem;
  color: #777;
}
.btn-toggle {
  background: none;
  border: none;
  color: #c5a059;
  font-weight: 600;
  cursor: pointer;
  padding-left: 5px;
  text-decoration: underline;
}
.btn-toggle:hover {
  color: #e2be7a;
}

/* Responsive cho Mobile & Tablet nhỏ */
@media (max-width: 868px) {
  .auth-image-side {
    display: none; /* Ẩn một bên ảnh để dồn diện tích cho form nhập liệu */
  }
  .auth-container {
    max-width: 500px;
    height: auto;
  }
  .back-home {
    top: 20px;
    left: 20px;
  }
}
</style>
