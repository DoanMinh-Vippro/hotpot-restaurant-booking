<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import { computed, ref, watch } from 'vue';
const emit = defineEmits(['refresh', 'close', 'saved'])
const props = defineProps<{
  selectedNhanVien?: any
  modelValue?: any
  accounts?: any[]
  roles?: any[]
  mode?: 'create' | 'edit'
}>()

interface NhanVienFormData {
  id: number | null
  tenNhanVien: string
  gioiTinh: boolean | null
  soDienThoai: string
  email: string
  diaChi: string
  trangThai: boolean
  idChucVu: number | null
  idTaiKhoan: number | null
}

const formData = ref<NhanVienFormData>({
  id: null,
  tenNhanVien: '',
  gioiTinh: null,
  soDienThoai: '',
  email: '',
  diaChi: '',
  trangThai: true,
  idChucVu: null,
  idTaiKhoan: null,
})

const resetForm = () => {
  formData.value = {
    id: null,
    tenNhanVien: '',
    gioiTinh: null,
    soDienThoai: '',
    email: '',
    diaChi: '',
    trangThai: true,
    idChucVu: null,
    idTaiKhoan: null,
  }
}

const selectedNhanVien = computed(() => props.modelValue ?? props.selectedNhanVien)

const mode = computed(() => {
  if (props.mode) return props.mode
  return selectedNhanVien.value?.id ? 'edit' : 'create'
})

const add = async () => {
  try {
    if (mode.value === 'edit') {
      alert('Chuyển sang chế độ tạo mới để thêm nhân viên khác.')
      return
    }
    await NhanVienApi.add(formData.value)
    alert('Thêm nhân viên thành công!')
    emit('refresh')
    emit('saved')
    resetForm()
  } catch (error) {
    console.error('thêm thất bại:', error)
  }
}

const update = async () => {
  try {
    if (!formData.value.id) {
      alert('Chưa chọn nhân viên!')
      return
    }

    await NhanVienApi.update(formData.value.id, formData.value)
    alert('Cập nhật nhân viên thành công!')
    emit('refresh')
    emit('saved')
  } catch (error: any) {
    console.log('UPDATE ERROR:', error)
    console.log('SERVER:', error?.response?.data)
  }
}

const onCancel = () => {
  resetForm()
  emit('close')
}

const toBoolean = (val: any) => {
  if (val === true || val === 1 || val === '1') return true
  if (val === false || val === 0 || val === '0') return false

  if (typeof val === 'string') {
    return val.toLowerCase() === 'true'
  }

  return false
}

const roleOptions = computed(() => props.roles || [])
const accountOptions = computed(() => props.accounts || [])

watch(
  selectedNhanVien,
  (nv) => {
    if (!nv) {
      resetForm()
      return
    }

    formData.value = {
      id: nv.id ?? null,
      tenNhanVien: nv.tenNhanVien ?? '',
      gioiTinh: toBoolean(nv.gioiTinh),
      soDienThoai: nv.soDienThoai ?? '',
      email: nv.email ?? '',
      diaChi: nv.diaChi ?? '',
      trangThai: toBoolean(nv.trangThai) ?? true,
      idChucVu: nv.idChucVu ?? null,
      idTaiKhoan: nv.idTaiKhoan ?? null,
    }
  },
  { immediate: true },
)
</script>
<template>
    <div class="form-container">
        <div class="form-grid">
          <div class="field-card">
            <div class="field-header">
              <h3>Thông tin nhân viên</h3>
              <p>Nhập đầy đủ tên, giới tính và chức vụ để thêm hoặc sửa hồ sơ.</p>
            </div>

            <div class="form-field">
              <label>Tên nhân viên</label>
              <input type="text" v-model="formData.tenNhanVien" placeholder="Nhập họ và tên" />
            </div>

            <div class="form-row">
              <div class="form-field half">
                <label>Giới tính</label>
                <select v-model="formData.gioiTinh">
                  <option :value="null">Chọn</option>
                  <option :value="true">Nam</option>
                  <option :value="false">Nữ</option>
                </select>
              </div>
              <div class="form-field half">
                <label>Chức vụ</label>
                <select v-model.number="formData.idChucVu">
                  <option :value="null">Chọn chức vụ</option>
                  <option v-for="role in roleOptions" :key="role.id" :value="role.id">{{ role.tenChucVu }}</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-field half">
                <label>Số điện thoại</label>
                <input type="text" v-model="formData.soDienThoai" placeholder="Nhập số điện thoại" />
              </div>
              <div class="form-field half">
                <label>Email</label>
                <input type="email" v-model="formData.email" placeholder="Nhập email" />
              </div>
            </div>

            <div class="form-field">
              <label>Địa chỉ</label>
              <input type="text" v-model="formData.diaChi" placeholder="Nhập địa chỉ" />
            </div>

            <div class="form-row">
              <div class="form-field half">
                <label>Trạng thái</label>
                <select v-model="formData.trangThai">
                  <option :value="true">Hoạt động</option>
                  <option :value="false">Ngừng</option>
                </select>
              </div>
              <div class="form-field half">
                <label>Tài khoản liên kết</label>
                <select v-model.number="formData.idTaiKhoan">
                  <option :value="null">Không chọn tài khoản</option>
                  <option v-for="account in accountOptions" :key="account.id" :value="account.id">{{ account.tenDangNhap }}</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="button-row">
          <button v-if="mode === 'create'" class="btn-primary" @click.prevent="add()">Thêm nhân viên</button>
          <button v-if="mode === 'edit'" class="btn-secondary" @click.prevent="update()">Cập nhật nhân viên</button>
          <button class="btn-tertiary" @click.prevent="onCancel()">Hủy</button>
        </div>
    </div>
</template>
<style scoped>
.form-container {
  background: linear-gradient(145deg, #141414, #0f0f0f);
  padding: 24px;
  border-radius: 14px;
  border: 1px solid #2a2a2a;
  max-width: 520px;
  width: min(100%, 520px);
  margin: 20px auto;
  color: #fff;
  font-family: "Segoe UI", sans-serif;
  box-shadow: 0 10px 30px rgba(0,0,0,0.6);
}

/* FIELD */
.form-field {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.form-container .button-row {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 10px;
}

button.btn-primary,
button.btn-secondary,
button.btn-tertiary {
  padding: 14px 22px;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  font-weight: 700;
  text-transform: uppercase;
  transition: transform 0.2s ease, filter 0.2s ease, background-color 0.2s ease;
}

button.btn-primary {
  background: linear-gradient(135deg, #5f8dff, #2c6bff);
  color: #fff;
}

button.btn-primary:hover {
  transform: translateY(-2px);
  filter: brightness(1.05);
}

button.btn-secondary {
  background: linear-gradient(135deg, #34d399, #10b981);
  color: #fff;
}

button.btn-secondary:hover {
  transform: translateY(-2px);
  filter: brightness(1.05);
}

button.btn-tertiary {
  background: rgba(255, 255, 255, 0.08);
  color: #d1d5db;
  border: 1px solid rgba(255, 255, 255, 0.12);
}

button.btn-tertiary:hover {
  background: rgba(255, 255, 255, 0.14);
}

.form-grid {
  display: grid;
  gap: 20px;
}

.field-card {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.18);
}

.field-header {
  margin-bottom: 22px;
}

.field-header h3 {
  margin: 0 0 8px;
  font-size: 1.15rem;
  letter-spacing: 0.02em;
  color: #f8fafc;
}

.field-header p {
  margin: 0;
  color: #cbd5e1;
  font-size: 0.92rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-field.half {
  width: 100%;
}

.form-field input,
.form-field select {
  width: 100%;
}

label {
  margin-bottom: 8px;
  font-size: 0.88rem;
  color: #f3d57f;
  font-weight: 600;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

input,
select {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: #111111;
  color: #f7f7f7;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

input:focus,
select:focus {
  border-color: #f1c56c;
  box-shadow: 0 0 0 2px rgba(241, 197, 108, 0.2);
}
</style>
