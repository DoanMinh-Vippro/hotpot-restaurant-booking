<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthApi from '@/api/AuthApi'
import { useAuthStore } from '@/stores/AuthStore'

const router = useRouter()
const authStore = useAuthStore()

// Form data
const formData = ref({
  tenDangNhap: '',
  matKhau: '',
  confirmPassword: '',
  tenKhachHang: '',
  gioiTinh: true,
  soDienThoai: '',
  email: '',
  diaChi: ''
})

const errors = ref<Record<string, string>>({})
const isLoading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const validateForm = () => {
  errors.value = {}

  if (!formData.value.tenDangNhap.trim()) {
    errors.value.tenDangNhap = 'Tên đăng nhập không được bỏ trống'
  } else if (formData.value.tenDangNhap.length < 3) {
    errors.value.tenDangNhap = 'Tên đăng nhập tối thiểu 3 ký tự'
  }

  if (!formData.value.matKhau) {
    errors.value.matKhau = 'Mật khẩu không được bỏ trống'
  } else if (formData.value.matKhau.length < 6) {
    errors.value.matKhau = 'Mật khẩu tối thiểu 6 ký tự'
  }

  if (formData.value.matKhau !== formData.value.confirmPassword) {
    errors.value.confirmPassword = 'Mật khẩu không khớp'
  }

  if (!formData.value.tenKhachHang.trim()) {
    errors.value.tenKhachHang = 'Tên khách hàng không được bỏ trống'
  }

  if (!formData.value.soDienThoai.trim()) {
    errors.value.soDienThoai = 'Số điện thoại không được bỏ trống'
  } else if (!/^[0-9]{10}$/.test(formData.value.soDienThoai)) {
    errors.value.soDienThoai = 'Số điện thoại phải là 10 chữ số'
  }

  if (!formData.value.email.trim()) {
    errors.value.email = 'Email không được bỏ trống'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = 'Email không hợp lệ'
  }

  if (!formData.value.diaChi.trim()) {
    errors.value.diaChi = 'Địa chỉ không được bỏ trống'
  }

  return Object.keys(errors.value).length === 0
}

const handleRegister = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  try {
    const res = await AuthApi.registerCustomer({
      tenDangNhap: formData.value.tenDangNhap,
      matKhau: formData.value.matKhau,
      tenKhachHang: formData.value.tenKhachHang,
      gioiTinh: formData.value.gioiTinh,
      soDienThoai: formData.value.soDienThoai,
      email: formData.value.email,
      diaChi: formData.value.diaChi
    })

    console.log('Đăng ký thành công!', res.data)
    
    // Lưu thông tin khách hàng vào store
    if (res.data) {
      authStore.setCustomerInfo({
        khachHangId: res.data.idKhachHang,
        tenKhachHang: res.data.tenKhachHang,
        soDienThoai: res.data.soDienThoai,
        email: res.data.email,
        diaChi: res.data.diaChi,
        gioiTinh: res.data.gioiTinh,
        maKhachHang: res.data.maKhachHang
      })
    }
    
    alert('Đăng ký thành công! Vui lòng đăng nhập.')

    // Chuyển đến trang login
    router.push('/auth')
  } catch (error: any) {
    console.error('Lỗi đăng ký:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Đăng ký thất bại'
    alert(errorMessage)
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

const goToLogin = () => {
  router.push('/auth')
}
</script>

<template>
  <div class="register-wrapper">
    <div class="back-home" @click="goBack">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="19" y1="12" x2="5" y2="12"></line>
        <polyline points="12 19 5 12 12 5"></polyline>
      </svg>
      QUAY LẠI TRANG CHỦ
    </div>

    <div class="register-container">
      <div class="register-form-side">
        <div class="form-box">
          <div class="form-header">
            <h2>TẠO TÀI KHOẢN MỚI</h2>
            <p>Đăng ký để thưởng thức những trải nghiệm độc đáo tại CÁI BANG</p>
          </div>

          <form @submit.prevent="handleRegister" class="register-form">
            <!-- Thông tin tài khoản -->
            <div class="section-title">Thông tin tài khoản</div>

            <div class="form-group">
              <label>TÊN ĐĂNG NHẬP</label>
              <input
                v-model="formData.tenDangNhap"
                type="text"
                placeholder="Nhập tên đăng nhập"
                :class="{ 'is-error': errors.tenDangNhap }"
              />
              <span v-if="errors.tenDangNhap" class="error-message">{{ errors.tenDangNhap }}</span>
            </div>

            <div class="form-group">
              <label>MẬT KHẨU</label>
              <div class="password-input-wrapper">
                <input
                  v-model="formData.matKhau"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  :class="{ 'is-error': errors.matKhau }"
                />
                <button
                  type="button"
                  class="toggle-password"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </button>
              </div>
              <span v-if="errors.matKhau" class="error-message">{{ errors.matKhau }}</span>
            </div>

            <div class="form-group">
              <label>NHẬP LẠI MẬT KHẨU</label>
              <div class="password-input-wrapper">
                <input
                  v-model="formData.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  :class="{ 'is-error': errors.confirmPassword }"
                />
                <button
                  type="button"
                  class="toggle-password"
                  @click="showConfirmPassword = !showConfirmPassword"
                >
                  <svg v-if="!showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </button>
              </div>
              <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
            </div>

            <!-- Thông tin khách hàng -->
            <div class="section-title">Thông tin cá nhân</div>

            <div class="form-group">
              <label>TÊN KHÁCH HÀNG</label>
              <input
                v-model="formData.tenKhachHang"
                type="text"
                placeholder="Nhập tên của bạn"
                :class="{ 'is-error': errors.tenKhachHang }"
              />
              <span v-if="errors.tenKhachHang" class="error-message">{{ errors.tenKhachHang }}</span>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>GIỚI TÍNH</label>
                <select v-model.boolean="formData.gioiTinh" class="select-input">
                  <option :value="true">Nam</option>
                  <option :value="false">Nữ</option>
                </select>
              </div>

              <div class="form-group">
                <label>SỐ ĐIỆN THOẠI</label>
                <input
                  v-model="formData.soDienThoai"
                  type="tel"
                  placeholder="0912345678"
                  :class="{ 'is-error': errors.soDienThoai }"
                />
                <span v-if="errors.soDienThoai" class="error-message">{{ errors.soDienThoai }}</span>
              </div>
            </div>

            <div class="form-group">
              <label>EMAIL</label>
              <input
                v-model="formData.email"
                type="email"
                placeholder="your.email@example.com"
                :class="{ 'is-error': errors.email }"
              />
              <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
            </div>

            <div class="form-group">
              <label>ĐỊA CHỈ</label>
              <textarea
                v-model="formData.diaChi"
                placeholder="Nhập địa chỉ của bạn"
                rows="3"
                :class="{ 'is-error': errors.diaChi }"
              ></textarea>
              <span v-if="errors.diaChi" class="error-message">{{ errors.diaChi }}</span>
            </div>

            <button type="submit" class="btn-register" :disabled="isLoading">
              {{ isLoading ? 'ĐANG XỬ LÝ...' : 'ĐĂNG KÝ NGAY' }}
            </button>
          </form>

          <div class="form-footer">
            <span>Bạn đã có tài khoản rồi?</span>
            <button @click="goToLogin" class="btn-login-link">
              Đăng nhập tại đây
            </button>
          </div>
        </div>
      </div>

      <div class="register-image-side">
        <div class="overlay"></div>
        <div class="brand-content">
          <h2 class="brand-logo">CÁI BANG <span class="gold">RESTO</span></h2>
          <p class="brand-slogan">Nơi tinh hoa ẩm thực giao thoa cùng không gian đẳng cấp.</p>
          <div class="benefits">
            <div class="benefit-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span>Đặt bàn dễ dàng</span>
            </div>
            <div class="benefit-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span>Nhận ưu đãi độc quyền</span>
            </div>
            <div class="benefit-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span>Tích điểm thành viên</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-wrapper {
  background-color: #0a0a0a;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  font-family: 'Montserrat', sans-serif;
}

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
  border: none;
  background: none;
  padding: 0;
}

.back-home:hover {
  opacity: 1;
  color: #c5a059;
}

.register-container {
  width: 100%;
  max-width: 1400px;
  background: #111111;
  display: flex;
  border: 1px solid rgba(197, 160, 89, 0.15);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  border-radius: 8px;
  min-height: 700px;
}

.register-form-side {
  flex: 1;
  padding: 60px 50px;
  overflow-y: auto;
  max-height: 700px;
}

.register-image-side {
  flex: 1;
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1514933651103-005eec06c04b?q=80&w=1000&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  padding: 40px;
  min-height: 700px;
}

.register-image-side .overlay {
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
  margin-bottom: 15px;
}

.gold {
  color: #c5a059;
}

.brand-slogan {
  color: #ccc;
  font-size: 0.9rem;
  letter-spacing: 1px;
  line-height: 1.6;
  margin-bottom: 30px;
}

.benefits {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #c5a059;
  font-size: 0.9rem;
}

.benefit-item svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.form-box {
  width: 100%;
}

.form-header {
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 1.8rem;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.form-header p {
  color: #999;
  font-size: 0.9rem;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 0.85rem;
  letter-spacing: 2px;
  color: #c5a059;
  text-transform: uppercase;
  margin-top: 10px;
  margin-bottom: 10px;
  font-weight: 600;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group label {
  font-size: 0.8rem;
  letter-spacing: 1px;
  color: #ccc;
  text-transform: uppercase;
  font-weight: 600;
}

.form-group input,
.form-group textarea,
.select-input {
  padding: 12px 15px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.2);
  color: #fff;
  font-size: 0.95rem;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus,
.select-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  border-color: #c5a059;
  box-shadow: 0 0 10px rgba(197, 160, 89, 0.2);
}

.form-group input.is-error,
.form-group textarea.is-error {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-wrapper input {
  width: 100%;
}

.toggle-password {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s;
}

.toggle-password:hover {
  color: #c5a059;
}

.select-input {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23c5a059' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  padding-right: 30px;
}

.select-input option {
  background: #1a1a1a;
  color: #fff;
}

.error-message {
  font-size: 0.8rem;
  color: #ff4757;
  margin-top: 4px;
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.btn-register {
  padding: 14px;
  background: linear-gradient(135deg, #c5a059, #d4b896);
  color: #000;
  border: none;
  border-radius: 4px;
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  margin-top: 20px;
}

.btn-register:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(197, 160, 89, 0.3);
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  color: #999;
  font-size: 0.9rem;
}

.btn-login-link {
  background: none;
  border: none;
  color: #c5a059;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
  font-size: inherit;
  transition: color 0.3s;
}

.btn-login-link:hover {
  color: #d4b896;
}

@media (max-width: 1024px) {
  .register-container {
    flex-direction: column;
    min-height: auto;
  }

  .register-image-side {
    display: none;
  }

  .register-form-side {
    padding: 40px 30px;
    max-height: none;
  }
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .register-form-side {
    padding: 30px 20px;
  }

  .form-header h2 {
    font-size: 1.5rem;
  }
}
</style>
