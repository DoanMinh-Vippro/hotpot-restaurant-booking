<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ComBoApi from '@/api/ComBoApi'
import type { Combo } from '@/api/ComBoApi'

// v-model từ DatBanForm
const props = defineProps<{ modelValue: number | null }>()
const emit = defineEmits(['update:modelValue', 'selectedCombo'])

const danhSachCombo = ref<Combo[]>([])
const loading = ref(false)

const isComboAvailable = (combo: Combo | any) => {
  const status = combo?.trangThai

  if (status === null || status === undefined || status === '') return true

  if (typeof status === 'number') return status !== 0

  if (typeof status === 'boolean') return status

  if (typeof status === 'string') {
    const normalized = status.trim().toLowerCase()
    return !['0', 'false', 'inactive', 'khong-hoat-dong', 'da-xoa', 'disabled'].includes(normalized)
  }

  return true
}

// load data
const loadComboGoiY = async () => {
  loading.value = true
  try {
    const res = await ComBoApi.hienThiComBo()

    danhSachCombo.value = (res.data || []).filter(isComboAvailable)
  } catch (error) {
    console.error('Không thể tải danh sách combo gợi ý:', error)
  } finally {
    loading.value = false
  }
}

// chọn combo (sync chuẩn v-model, không check stale state nữa)
const selectCombo = (id: number | null) => {
  emit('update:modelValue', id)

  if (id == null) {
    emit('selectedCombo', null)
    return
  }

  const combo = danhSachCombo.value.find((c) => c.idCombo === id)

  emit('selectedCombo', combo)
}

onMounted(loadComboGoiY)
</script>

<template>
  <div class="combo-select-box">
    <div class="combo-header">
      <span>🍱 Gói Combo Ưu Đãi (Chọn 1)</span>

      <button v-if="modelValue !== null" @click="selectCombo(null)">Bỏ chọn</button>
    </div>

    <div v-if="loading" class="loading-text">Đang tải...</div>

    <div v-else class="luoi-combo-mini">
      <div
        v-for="cb in danhSachCombo"
        :key="cb.idCombo"
        class="card-combo-mini"
        :class="{ active: modelValue === cb.idCombo }"
        @click="selectCombo(cb.idCombo)"
      >
        <div class="khung-anh">
          <img v-if="cb.hinhAnh" :src="`http://localhost:8080/uploads/${cb.hinhAnh}`" />
          <div v-else class="no-img">No Image</div>
        </div>

        <div class="chi-tiet">
          <h4 class="ten">{{ cb.tenCombo }}</h4>
          <span class="gia"> {{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.combo-select-box {
  background: #222222;
  border: 1px solid #333;
  border-radius: 8px;
  padding: 14px;
  margin-top: 5px;
}

.combo-header {
  margin-bottom: 12px;
  border-left: 3px solid #c5a059;
  padding-left: 8px;
}

.combo-header span {
  color: #c5a059;
  font-size: 0.85rem;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.luoi-combo-mini {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 10px;
  max-height: 190px;
  overflow-y: auto;
  padding-right: 4px;
}

/* Tối ưu thanh cuộn nhỏ gọn */
.luoi-combo-mini::-webkit-scrollbar {
  width: 4px;
}
.luoi-combo-mini::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 4px;
}

.card-combo-mini {
  background: #1a1a1a;
  border: 1px solid #2d2d2d;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.khung-anh {
  width: 100%;
  height: 75px;
  background: #111;
}

.khung-anh img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-img {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #555;
}

.chi-tiet {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ten {
  margin: 0;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gia {
  color: #c5a059;
  font-size: 12px;
  font-weight: bold;
}

.loading-text,
.trong-text {
  text-align: center;
  font-size: 12px;
  color: #888;
  padding: 10px 0;
}
.card-combo-mini {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.card-combo-mini:hover {
  border-color: #ccc;
}

/* Class active này được thêm vào khi modelValue trùng với idCombo */
.card-combo-mini.active {
  border: 2px solid #ff4500; /* Màu cam nổi bật */
  background-color: #fff5f0;
  transform: scale(1.02);
}
</style>
