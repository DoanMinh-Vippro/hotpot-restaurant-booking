<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

type Discount = {
  idGiamGia: number
  maGiamGia: string
  ngayTao: string | null
  ngayKetThuc: string | null
  dieuKienSuDung: string | null
  giaTriGiamToiDa: number | null
  giaTriGiam: number | null
  loaiGiam: string | null
  soLuongMaGiamGia: number | null
  soLuongDung: number | null
  trangThai: number | null
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

const discounts = ref<Discount[]>([])
const loading = ref(false)
const submitting = ref(false)
const serverError = ref('')
const serverMessage = ref('')
const searchQuery = ref('')
const selectedId = ref<number | null>(null)
const formMode = ref<'create' | 'edit'>('create')

const form = reactive({
  maGiamGia: '',
  ngayKetThuc: '',
  dieuKienSuDung: '',
  giaTriGiamToiDa: '',
  giaTriGiam: '',
  loaiGiam: 'PHẦN TRĂM',
  soLuongMaGiamGia: 1,
  trangThai: 1,
})

const errors = reactive<Record<string, string>>({})

const selectedDiscount = computed(() => discounts.value.find((item) => item.idGiamGia === selectedId.value))

const formatCurrency = (value: number | string | null) => {
  const numeric = Number(value ?? 0)
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(numeric)
}

const formatDate = (value: string | null) => {
  if (!value) return 'Chưa có'
  return new Date(value).toLocaleDateString('vi-VN')
}

const loadDiscounts = async () => {
  loading.value = true
  serverError.value = ''
  try {
    const query = searchQuery.value.trim() ? `?keyword=${encodeURIComponent(searchQuery.value.trim())}` : ''
    const response = await fetch(`${apiBaseUrl}/api/giam-gia${query}`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    discounts.value = await response.json()
    if (!discounts.value.length) {
      selectedId.value = null
    } else if (selectedId.value == null) {
      selectedId.value = discounts.value[0]?.idGiamGia ?? null
    }
  } catch (error) {
    serverError.value = 'Không tải được dữ liệu giảm giá. Kiểm tra backend hoặc cấu hình API.'
    console.error(error)
  } finally {
    loading.value = false
  }
}

const selectDiscount = (id: number) => {
  selectedId.value = id
}

const prepareForm = (discount?: Discount) => {
  if (discount) {
    formMode.value = 'edit'
    selectedId.value = discount.idGiamGia
    form.maGiamGia = discount.maGiamGia
    form.ngayKetThuc = discount.ngayKetThuc ?? ''
    form.dieuKienSuDung = discount.dieuKienSuDung ?? ''
    form.giaTriGiamToiDa = discount.giaTriGiamToiDa?.toString() ?? ''
    form.giaTriGiam = discount.giaTriGiam?.toString() ?? ''
    form.loaiGiam = discount.loaiGiam ?? 'PHẦN TRĂM'
    form.soLuongMaGiamGia = discount.soLuongMaGiamGia ?? 1
    form.trangThai = discount.trangThai ?? 1
  } else {
    formMode.value = 'create'
    selectedId.value = null
    form.maGiamGia = ''
    form.ngayKetThuc = ''
    form.dieuKienSuDung = ''
    form.giaTriGiamToiDa = ''
    form.giaTriGiam = ''
    form.loaiGiam = 'PHẦN TRĂM'
    form.soLuongMaGiamGia = 1
    form.trangThai = 1
  }
  serverError.value = ''
  serverMessage.value = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}

const validateForm = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  let valid = true
  const ma = form.maGiamGia.trim()
  const dieuKien = form.dieuKienSuDung.trim()
  const giaTriGiam = Number(form.giaTriGiam)
  const giaTriToiDa = Number(form.giaTriGiamToiDa)

  if (!ma) {
    errors.maGiamGia = 'Mã giảm giá không được để trống'
    valid = false
  } else if (ma.length < 3 || ma.length > 40) {
    errors.maGiamGia = 'Mã giảm giá phải từ 3 đến 40 ký tự'
    valid = false
  }

  if (!dieuKien) {
    errors.dieuKienSuDung = 'Điều kiện sử dụng không được để trống'
    valid = false
  }

  if (!form.ngayKetThuc) {
    errors.ngayKetThuc = 'Ngày kết thúc không được để trống'
    valid = false
  }

  if (!form.giaTriGiam) {
    errors.giaTriGiam = 'Giá trị giảm không được để trống'
    valid = false
  } else if (Number.isNaN(giaTriGiam) || giaTriGiam <= 0) {
    errors.giaTriGiam = 'Giá trị giảm phải lớn hơn 0'
    valid = false
  }

  if (!form.giaTriGiamToiDa) {
    errors.giaTriGiamToiDa = 'Giá trị giảm tối đa không được để trống'
    valid = false
  } else if (Number.isNaN(giaTriToiDa) || giaTriToiDa <= 0) {
    errors.giaTriGiamToiDa = 'Giá trị giảm tối đa phải lớn hơn 0'
    valid = false
  }

  if (!Number.isNaN(giaTriGiam) && !Number.isNaN(giaTriToiDa) && giaTriToiDa < giaTriGiam) {
    errors.giaTriGiamToiDa = 'Giá trị giảm tối đa phải lớn hơn hoặc bằng giá trị giảm'
    valid = false
  }

  if (!form.soLuongMaGiamGia || form.soLuongMaGiamGia <= 0) {
    errors.soLuongMaGiamGia = 'Số lượng mã giảm giá phải lớn hơn 0'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validateForm()) return
  submitting.value = true
  serverError.value = ''
  serverMessage.value = ''

  const payload = {
    maGiamGia: form.maGiamGia.trim(),
    ngayKetThuc: form.ngayKetThuc,
    dieuKienSuDung: form.dieuKienSuDung.trim(),
    giaTriGiamToiDa: Number(form.giaTriGiamToiDa),
    giaTriGiam: Number(form.giaTriGiam),
    loaiGiam: form.loaiGiam.trim(),
    soLuongMaGiamGia: form.soLuongMaGiamGia,
    trangThai: form.trangThai,
  }

  try {
    const url = selectedId.value && formMode.value === 'edit'
      ? `${apiBaseUrl}/api/giam-gia/${selectedId.value}`
      : `${apiBaseUrl}/api/giam-gia`
    const method = formMode.value === 'edit' ? 'PUT' : 'POST'
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
      const errorBody = await response.json().catch(() => null)
      serverError.value = errorBody?.message || 'Lỗi khi gửi dữ liệu giảm giá'
      return
    }

    serverMessage.value = formMode.value === 'edit'
      ? 'Cập nhật giảm giá thành công'
      : 'Tạo giảm giá thành công'

    await loadDiscounts()
    prepareForm()
  } catch (error) {
    serverError.value = 'Lỗi kết nối với server giảm giá'
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleEdit = (discount: Discount) => {
  prepareForm(discount)
}

const handleDelete = async (id: number) => {
  const confirmed = window.confirm('Bạn có chắc muốn xóa mã giảm giá này?')
  if (!confirmed) return
  loading.value = true
  serverError.value = ''
  serverMessage.value = ''

  try {
    const response = await fetch(`${apiBaseUrl}/api/giam-gia/${id}`, {
      method: 'DELETE',
    })
    if (!response.ok) {
      const payload = await response.json().catch(() => null)
      serverError.value = payload?.message || 'Xóa mã giảm giá thất bại'
      return
    }

    serverMessage.value = 'Xóa mã giảm giá thành công'
    await loadDiscounts()
    prepareForm()
  } catch (error) {
    serverError.value = 'Không thể xóa mã giảm giá'
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  prepareForm()
}

const submitLabel = computed(() => formMode.value === 'create' ? 'Tạo mới' : 'Cập nhật')

onMounted(loadDiscounts)
</script>

<template>
  <main class="discount-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Quản lý giảm giá</p>
        <h1>CRUD Giảm giá</h1>
      </div>
      <button class="primary-button" type="button" @click="loadDiscounts">Tải lại</button>
    </header>

    <section class="discount-grid">
      <section class="panel list-panel">
        <div class="panel-title">
          <div>
            <h2>Danh sách mã</h2>
            <p>Nhấp vào mã để sửa hoặc xóa.</p>
          </div>
          <button class="secondary-button" type="button" @click="resetForm">Thêm mới</button>
        </div>

        <div class="panel-toolbar">
          <input
            class="search-input"
            type="search"
            v-model="searchQuery"
            @input="loadDiscounts"
            placeholder="Tìm mã hoặc điều kiện sử dụng..."
          />
          <span class="status-chip">
            {{ loading ? 'Đang tải dữ liệu...' : discounts.length + ' mã giảm giá' }}
          </span>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Mã</th>
                <th>Loại giảm</th>
                <th>Giá trị</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="discount in discounts" :key="discount.idGiamGia" :class="{ active: discount.idGiamGia === selectedId }">
                <td>{{ discount.maGiamGia }}</td>
                <td>{{ discount.loaiGiam }}</td>
                <td>{{ formatCurrency(discount.giaTriGiam) }}</td>
                <td>{{ discount.trangThai === 1 ? 'Hoạt động' : 'Ngưng' }}</td>
                <td class="actions">
                  <button type="button" class="text-button" @click="selectDiscount(discount.idGiamGia); handleEdit(discount)">Sửa</button>
                  <button type="button" class="danger-button" @click="handleDelete(discount.idGiamGia)">Xóa</button>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-if="!loading && discounts.length === 0" class="empty-state">Không có mã giảm giá nào.</p>
        </div>
      </section>

      <section class="panel form-panel">
        <div class="panel-title">
          <div>
            <h2>{{ formMode === 'create' ? 'Tạo mã giảm giá mới' : 'Cập nhật mã giảm giá' }}</h2>
            <p>{{ formMode === 'create' ? 'Nhập đầy đủ thông tin để tạo mã.' : 'Cập nhật thông tin mã giảm giá đã chọn.' }}</p>
          </div>
        </div>

        <div class="form-grid">
          <label>
            Mã giảm giá
            <input v-model="form.maGiamGia" type="text" />
            <span class="field-error" v-if="errors.maGiamGia">{{ errors.maGiamGia }}</span>
          </label>

          <label>
            Điều kiện sử dụng
            <input v-model="form.dieuKienSuDung" type="text" />
            <span class="field-error" v-if="errors.dieuKienSuDung">{{ errors.dieuKienSuDung }}</span>
          </label>

          <label>
            Ngày kết thúc
            <input v-model="form.ngayKetThuc" type="date" />
            <span class="field-error" v-if="errors.ngayKetThuc">{{ errors.ngayKetThuc }}</span>
          </label>

          <label>
            Giá trị giảm
            <input v-model="form.giaTriGiam" type="number" min="0" step="0.01" />
            <span class="field-error" v-if="errors.giaTriGiam">{{ errors.giaTriGiam }}</span>
          </label>

          <label>
            Giá trị giảm tối đa
            <input v-model="form.giaTriGiamToiDa" type="number" min="0" step="0.01" />
            <span class="field-error" v-if="errors.giaTriGiamToiDa">{{ errors.giaTriGiamToiDa }}</span>
          </label>

          <label>
            Loại giảm
            <select v-model="form.loaiGiam">
              <option value="PHẦN TRĂM">PHẦN TRĂM</option>
              <option value="GIÁ TRỊ">GIÁ TRỊ</option>
            </select>
          </label>

          <label>
            Số lượng mã
            <input v-model.number="form.soLuongMaGiamGia" type="number" min="1" />
            <span class="field-error" v-if="errors.soLuongMaGiamGia">{{ errors.soLuongMaGiamGia }}</span>
          </label>

          <label>
            Trạng thái
            <select v-model.number="form.trangThai">
              <option :value="1">Hoạt động</option>
              <option :value="0">Ngưng</option>
            </select>
          </label>
        </div>

        <div class="button-group">
          <button class="primary-button" type="button" @click="handleSubmit" :disabled="submitting">{{ submitLabel }}</button>
          <button class="secondary-button" type="button" @click="resetForm" :disabled="submitting">Hủy</button>
        </div>

        <p v-if="serverMessage" class="success-message">{{ serverMessage }}</p>
        <p v-if="serverError" class="error-message">{{ serverError }}</p>

        <div v-if="selectedDiscount" class="preview-box">
          <h3>Thông tin mã đang chọn</h3>
          <p><strong>Mã:</strong> {{ selectedDiscount?.maGiamGia }}</p>
          <p><strong>Ngày tạo:</strong> {{ formatDate(selectedDiscount?.ngayTao ?? null) }}</p>
          <p><strong>Ngày kết thúc:</strong> {{ formatDate(selectedDiscount?.ngayKetThuc ?? null) }}</p>
          <p><strong>Số đã dùng:</strong> {{ selectedDiscount?.soLuongDung ?? 0 }}</p>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.discount-page {
  padding: 120px 60px 40px;
  min-height: calc(100vh - 120px);
  color: #f1f1f1;
  background: radial-gradient(circle at top, rgba(197, 160, 89, 0.12), transparent 35%),
              linear-gradient(180deg, #070707 0%, #0f0f0f 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  margin-bottom: 28px;
}

.page-header h1 {
  margin: 0;
  font-size: clamp(2rem, 2.4vw, 2.6rem);
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #f8d46a;
  margin-bottom: 8px;
  font-size: 0.8rem;
}

.discount-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 24px;
}

.panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  border-radius: 28px;
  padding: 26px;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.discount-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1.2fr) minmax(360px, 0.9fr);
  gap: 24px;
  align-items: start;
}

.panel-title h2 {
  margin: 0 0 10px;
  font-size: 1.15rem;
}

.panel-title p {
  margin: 0;
  color: #c7c7c7;
  max-width: 420px;
}

.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 170px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  color: #d8d8d8;
  font-size: 0.9rem;
}

.search-input,
input[type='text'],
input[type='date'],
input[type='number'],
select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #f5f5f5;
  border-radius: 16px;
  padding: 14px 16px;
  font-size: 0.95rem;
  transition: border 0.2s ease, transform 0.2s ease;
}

.search-input:focus,
input[type='text']:focus,
input[type='date']:focus,
input[type='number']:focus,
select:focus {
  outline: none;
  border-color: #c5a059;
  box-shadow: 0 0 0 4px rgba(197, 160, 89, 0.12);
  transform: translateY(-1px);
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 100%;
}

thead {
  background: rgba(255, 255, 255, 0.04);
}

th,
td {
  text-align: left;
  padding: 16px 14px;
  font-size: 0.92rem;
  color: #efefef;
}

thead th {
  color: #b9b9b9;
  font-weight: 600;
}

tbody tr {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  transition: background 0.25s ease;
}

tbody tr:hover {
  background: rgba(255, 255, 255, 0.04);
}

tbody tr.active {
  background: rgba(197, 160, 89, 0.12);
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.text-button,
.primary-button,
.secondary-button,
.danger-button {
  border: 0;
  border-radius: 14px;
  padding: 12px 18px;
  cursor: pointer;
  transition: transform 0.2s ease, filter 0.2s ease, background 0.2s ease;
  font-weight: 600;
}

.primary-button {
  background: #f3cc6e;
  color: #111;
}

.primary-button:hover {
  filter: brightness(1.05);
}

.secondary-button {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 200, 110, 0.26);
  color: #f5f5f5;
}

.danger-button {
  background: #b83939;
  color: #fff;
}

.text-button {
  background: transparent;
  color: #f3cc6e;
}

.text-button:hover,
.secondary-button:hover,
.danger-button:hover {
  transform: translateY(-1px);
}

.button-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.9rem;
  color: #e4e4e4;
}

.field-error {
  color: #ff8e8e;
  font-size: 0.82rem;
}

.success-message,
.error-message {
  margin-top: 18px;
  border-radius: 16px;
  padding: 16px 18px;
  font-size: 0.95rem;
}

.success-message {
  background: rgba(152, 200, 120, 0.16);
  color: #def5c9;
}

.error-message {
  background: rgba(220, 90, 90, 0.16);
  color: #ffbebe;
}

.preview-box {
  margin-top: 22px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.07);
  border-radius: 20px;
  padding: 22px;
}

.preview-box h3 {
  margin-top: 0;
}

.empty-state {
  margin-top: 14px;
  color: #999;
}

@media (max-width: 1024px) {
  .discount-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .page-header,
  .panel-title,
  .button-group {
    flex-direction: column;
    align-items: stretch;
  }

  .panel-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .panel-toolbar .status-chip {
    width: 100%;
    justify-content: flex-start;
    margin-top: 12px;
  }
}
</style>
