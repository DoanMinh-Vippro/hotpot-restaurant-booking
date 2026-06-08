<script setup lang="ts">
import DatBanQuanLyApi from '@/api/DatBanQuanLy';
import { onMounted, ref, watch, computed } from 'vue';
import router from '@/router';


const props = defineProps(['datBanQuanLy','listBan'])
const emit = defineEmits(['refresh'])

const errors = ref<Record<string, string>>({}); //biến chứa lỗi

// 1. Khởi tạo form 
const initForm = () => ({
    idDatBan: 0,
    idBan: null as number | null,
    idkhachHang: null as number | null,
    sdtKhachHang: '',
    soNguoi: 0,
    trangThai: null as number | null,
    ghiChu: '',
    thoiGianDenDuKien: '',
    soTienCoc: 0,
    trangThaiCoc: null as number | null,
    phuongThucThanhToan: null as number | null
});

const formData = ref(initForm());

// 2. Công tắc kiểm tra: Nếu idDatBan > 0 là Đang Sửa, ngược lại là Thêm mới
const isEditing = computed(() => formData.value.idDatBan > 0);

// 3. Watch: Tự động reset khi props thay đổi
watch(() => props.datBanQuanLy, (newData) => {
    if (newData) {
        formData.value = { ...newData };
    } else {
        formData.value = initForm();
    }
}, { immediate: true });


const save = async () => {
              errors.value ={}

    try {
        if (isEditing.value) {
            await DatBanQuanLyApi.update(formData.value.idDatBan, formData.value);
            alert('Sửa thành công');
        } else {
            await DatBanQuanLyApi.add(formData.value);
            alert('Thêm thành công');
            console.log("--- DEBUG SAVE ---");
            console.log("ID hiện tại là:", formData.value.idDatBan);
            console.log("isEditing là:", isEditing.value);
        }
        emit('refresh');
    } catch (error) {
        console.error('Lỗi thực hiện:', error);
    }
};

// Thêm hàm này vào script setup
const resetForm = () => {
    formData.value = initForm();
};

</script>

<template>
  <div class="form-wrapper">
    <button class="back-home-btn" @click="router.push('/')">
      &larr; TRANG CHỦ
    </button>

    <h2 class="form-title">QUẢN LÝ ĐẶT BÀN</h2>
    
    <div class="form-grid">
      <div class="input-field">
        <label>SĐT Khách Hàng</label>
        <input v-model="formData.sdtKhachHang" type="text">
        <span v-if="errors.sdtKhachHang" class="error-msg">{{ errors.sdtKhachHang }}</span>
      </div>

      <div class="input-field">
        <label>ID Khách Hàng</label>
        <input v-model.number="formData.idkhachHang" type="number">
        <span v-if="errors.idkhachHang" class="error-msg">{{ errors.idkhachHang }}</span>
      </div>

      <div class="input-field">
        <label>Loại Bàn</label>
        <select v-model="formData.idBan">
          <option :value="null" disabled>-- Chọn loại bàn --</option>
          <option v-for="b in listBan" :key="b.idBan" :value="b.idBan">
            {{ b.loaiBan }}
          </option>
        </select>
        <span v-if="errors.idBan" class="error-msg">{{ errors.idBan }}</span>
      </div>
      
      <div class="input-field">
        <label>Số Người</label>
        <input v-model.number="formData.soNguoi" type="number">
        <span v-if="errors.soNguoi" class="error-msg">{{ errors.soNguoi }}</span>
      </div>

      <div class="input-field">
        <label>Tiền Cọc</label>
        <input v-model.number="formData.soTienCoc" type="number" class="highlight-gold">
        <span v-if="errors.soTienCoc" class="error-msg">{{ errors.soTienCoc }}</span>
      </div>

      <div class="input-field">
        <label>Thời Gian Đến</label>
        <input v-model="formData.thoiGianDenDuKien" type="datetime-local">
        <span v-if="errors.thoiGianDenDuKien" class="error-msg">{{ errors.thoiGianDenDuKien }}</span>
      </div>

      <div class="input-field">
        <label>Thanh Toán</label>
        <select v-model="formData.phuongThucThanhToan">
          <option value="CHUYEN_KHOAN">Chuyển khoản</option>
          <option value="VNPAY">VNPAY</option>
          <option value="TIEN_MAT">Tiền mặt</option>
        </select>
        <span v-if="errors.phuongThucThanhToan" class="error-msg">{{ errors.phuongThucThanhToan }}</span>
      </div>

      <div class="input-field">
        <label>Trạng Thái</label>
        <select v-model="formData.trangThai">
          <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
          <option value="DA_XAC_NHAN">Đã xác nhận</option>
          <option value="DA_NHAN_BAN">Đã nhận bàn</option>
          <option value="DA_HUY">Đã hủy</option>
          <option value="HOAN_THANH">Hoàn thành</option>
        </select>
        <span v-if="errors.trangThai" class="error-msg">{{ errors.trangThai }}</span>
      </div>
    </div>

    <div class="input-field full-width">
      <label>Ghi Chú</label>
      <textarea v-model="formData.ghiChu" rows="2"></textarea>
      <span v-if="errors.ghiChu" class="error-msg">{{ errors.ghiChu }}</span>
    </div>

    <div class="button-bar">
      <button class="btn-primary" @click.prevent="save()">
        {{ isEditing ? 'CẬP NHẬT THÔNG TIN' : 'THÊM MỚI ĐẶT BÀN' }}
      </button>
      
      <button v-if="isEditing" class="btn-secondary" @click.prevent="resetForm()">
        HỦY CHỌN
      </button>
    </div>
  </div>
</template>



<style scoped>
.form-wrapper {
  background: #0d0d0d;
  padding: 40px;
  max-width: 650px;
  margin: 40px auto;
  border: 1px solid #222;
  box-shadow: 0 20px 60px rgba(0,0,0,0.9);
  border-radius: 12px;
}

.form-title {
  color: #c5a059;
  font-size: 1.1rem;
  text-align: center;
  margin-bottom: 40px;
  letter-spacing: 5px;
  text-transform: uppercase;
  font-weight: 300;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px 20px;
}

.input-field { 
  display: flex; 
  flex-direction: column; 
  width: 100%; 
}

label {
  color: #888;
  font-size: 0.65rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 10px;
  transition: 0.3s;
}

/* INPUT & SELECT "GẠCH CHÂN" */
input, select, textarea {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-bottom: 1px solid #333;
  background: transparent;
  color: #fff;
  font-size: 0.95rem;
  transition: 0.4s;
  appearance: none; /* Bỏ icon mũi tên mặc định để trông sang hơn */
  -webkit-appearance: none;
}

/* Đảm bảo option hiển thị rõ ràng */
select option {
  background: #1a1a1a;
  color: #fff;
  padding: 10px;
}

input:focus, select:focus, textarea:focus {
  border-bottom: 1px solid #c5a059;
  outline: none;
}

.input-field:focus-within label { color: #c5a059; }

.full-width { grid-column: span 2; margin-top: 10px; }

.button-bar {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 50px;
}

.btn-primary, .btn-secondary {
  padding: 12px 40px;
  font-size: 0.7rem;
  letter-spacing: 2px;
  text-transform: uppercase;
  cursor: pointer;
  transition: 0.4s;
  border: 1px solid #c5a059;
}

.btn-primary { background: #c5a059; color: #000; font-weight: bold; }
.btn-primary:hover { background: #fff; border-color: #fff; }

.btn-secondary { background: transparent; color: #c5a059; }
.btn-secondary:hover { background: #333; border-color: #fff; color: #fff; }
.form-wrapper {
  position: relative; /* Rất quan trọng để nút absolute nằm trong form */
  /* ... các style cũ của bạn giữ nguyên ... */
}

.back-home-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  background: transparent;
  border: none;
  color: #666; /* Màu xám tối để hài hòa với theme */
  font-size: 0.7rem;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  transition: 0.3s;
}

.back-home-btn:hover {
  color: #c5a059; /* Đổi màu vàng gold khi hover */
}
.error-msg {
  color: red;
  font-size: 0.75rem;
  margin-top: 4px;
  display: block;
}
</style>