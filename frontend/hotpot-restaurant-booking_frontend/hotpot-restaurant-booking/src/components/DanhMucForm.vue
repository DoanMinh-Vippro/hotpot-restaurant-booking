<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { DanhMuc, DanhMucRequest } from '../api/DanhMucApi'

const emit = defineEmits(['submit'])

const isEditMode = ref(false)

const form = reactive<DanhMucRequest>({
  loaiDanhMuc: '',
  moTa: ''
})

const gui = () => {
  const loai = form.loaiDanhMuc || ''
  const mTa = form.moTa || ''

  // 1. VALIDATE LOẠI DANH MỤC
  if (!loai.trim()) return alert("Loại danh mục không được để trống")
  if (loai.length < 3 || loai.length > 50) return alert("Loại danh mục phải từ 3 đến 50 ký tự")
  if (loai !== loai.trim()) return alert("Loại danh mục không được chứa khoảng trắng ở đầu hoặc cuối")
  if (/\s{2,}/.test(loai)) return alert("Loại danh mục không được chứa nhiều khoảng trắng liên tiếp")

  // 2. VALIDATE MÔ TẢ
  if (!mTa.trim()) return alert("Mô tả danh mục không được để trống")
  if (mTa !== mTa.trim()) return alert("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối")
  if (/\s{2,}/.test(mTa)) return alert("Mô tả không được chứa nhiều khoảng trắng liên tiếp")

  emit('submit', {
    loaiDanhMuc: loai.trim(),
    moTa: mTa.trim()
  })
}

defineExpose({
  fillForm(item?: DanhMuc) {
    if (!item) {
      isEditMode.value = false
      form.loaiDanhMuc = ''
      form.moTa = ''
      return
    }

    isEditMode.value = true
    form.loaiDanhMuc = item.loaiDanhMuc
    form.moTa = item.moTa
  }
})
</script>

<template>
  <section class="bieu-mau-panel">

    <div class="tieu-de-panel">
      <h2>Thông tin danh mục</h2>
      <p>{{ isEditMode ? 'Cập nhật danh mục hệ thống' : 'Thêm mới danh mục hệ thống' }}</p>
    </div>

    <div class="luoi-bieu-mau">

      <label>
        Loại danh mục
        <input v-model="form.loaiDanhMuc" placeholder="Nhập loại danh mục (ví dụ: Món lẩu, Đồ uống)..." />
      </label>

      <label>
        Mô tả
        <input v-model="form.moTa" placeholder="Nhập mô tả chi tiết danh mục..." />
      </label>

    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" @click="gui">
        Lưu
      </button>
    </div>

  </section>
</template>

<style scoped>
.bieu-mau-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 28px;
  padding: 26px;
  color: white;
}

.tieu-de-panel h2 {
  color: #f8d46a;
  margin-bottom: 4px;
}

.tieu-de-panel p {
  color: #c7c7c7;
  font-size: 14px;
  margin-bottom: 12px;
}

.luoi-bieu-mau {
  display: grid;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  color: #d8d8d8;
}

input {
  margin-top: 6px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,.08);
  background: rgba(255,255,255,.04);
  color: white;
  outline: none;
}

input:focus {
  border-color: #f8d46a;
}

.nhom-nut {
  margin-top: 18px;
}

.nut-chinh {
  width: 100%;
  padding: 12px;
  border-radius: 16px;
  background: #f8d46a;
  color: #1a1410;
  font-weight: 600;
  cursor: pointer;
}
</style>