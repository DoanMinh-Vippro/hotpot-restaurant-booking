<script setup lang="ts">
import { onMounted, ref, onBeforeUnmount } from 'vue'
import ComBoApi from '@/api/ComBoApi'
import type { Combo } from '@/api/ComBoApi'

interface ComboDatBan {
  idCombo: number
  tenCombo: string
  giaSauGiam: number
  soLuong: number
}

// v-model có thể là mảng combo hoặc id combo đơn giản tùy nơi dùng
const props = defineProps<{
  modelValue: ComboDatBan[] | number | null
}>()

const emit = defineEmits(['update:modelValue', 'selectedCombo'])

const danhSachCombo = ref<Combo[]>([])
const loading = ref(false)
const gridRef = ref<HTMLElement | null>(null)
let wheelHandler: ((e: WheelEvent) => void) | null = null

// load danh sách combo
const loadComboGoiY = async () => {
  loading.value = true

  try {
    const res = await ComBoApi.hienThiComBo()
    // Chỉ lấy combo đang kinh doanh (trangThai === 1)
    danhSachCombo.value = (res.data || []).filter((cb: Combo) => cb.trangThai === 1)
  } catch (error) {
    console.error('Không thể tải danh sách combo:', error)
  } finally {
    loading.value = false
  }
}

// chọn hoặc bỏ chọn combo
const getSelectedItems = () => {
  if (Array.isArray(props.modelValue)) return props.modelValue
  return []
}

const selectCombo = (combo: Combo) => {
  if (combo.trangThaiBan === 0) {
    alert(`Combo "${combo.tenCombo}" hiện đã hết hàng, vui lòng chọn combo khác!`)
    return
  }

  const dsCombo = [...getSelectedItems()]

  const index = dsCombo.findIndex((item) => item.idCombo === combo.idCombo)

  // Nếu đã có thì tăng số lượng
  if (index >= 0) {
    const currentItem = dsCombo[index]
    if (currentItem) {
      currentItem.soLuong++
    }
  } else {
    // Nếu chưa có thì thêm mới
    dsCombo.push({
      idCombo: combo.idCombo,
      tenCombo: combo.tenCombo,
      giaSauGiam: Number(combo.giaSauGiam),
      soLuong: 1,
    })
  }

  emit('update:modelValue', dsCombo)

  emit('selectedCombo', dsCombo)
}

// giảm số lượng combo
const giamSoLuong = (idCombo: number) => {
  const dsCombo = [...getSelectedItems()]

  const index = dsCombo.findIndex((item) => item.idCombo === idCombo)

  if (index >= 0) {
    const currentItem = dsCombo[index]
    if (currentItem) {
      if (currentItem.soLuong > 1) {
        currentItem.soLuong--
      } else {
        dsCombo.splice(index, 1)
      }
    }
  }

  emit('update:modelValue', dsCombo)

  emit('selectedCombo', dsCombo)
}

// xóa toàn bộ combo
const xoaTatCa = () => {
  emit('update:modelValue', [])

  emit('selectedCombo', [])
}
onMounted(async () => {
  await loadComboGoiY()

  if (!gridRef.value) return

  wheelHandler = (e: WheelEvent) => {
    e.preventDefault()
    gridRef.value!.scrollLeft += e.deltaY
  }

  gridRef.value.addEventListener('wheel', wheelHandler, {
    passive: false,
  })
})

onBeforeUnmount(() => {
  if (gridRef.value && wheelHandler) {
    gridRef.value.removeEventListener('wheel', wheelHandler)
  }
})
</script>

<template>
  <div class="combo-select-box">
    <div class="combo-header">
      <span> 🍱 Gói Combo Ưu Đãi </span>

      <button v-if="getSelectedItems().length > 0" @click="xoaTatCa">Bỏ chọn</button>
    </div>

    <div v-if="loading" class="loading-text">Đang tải...</div>

    <div v-else ref="gridRef" class="luoi-combo-mini">
      <div
        v-for="cb in danhSachCombo"
        :key="cb.idCombo"
        class="card-combo-mini"
        @click="selectCombo(cb)"
      >
        <div class="khung-anh">
          <img v-if="cb.hinhAnh" :src="cb.hinhAnh" />

          <div v-else class="no-img">No Image</div>
        </div>

        <div class="chi-tiet">
          <h4 class="ten">
            {{ cb.tenCombo }}
          </h4>

          <span class="gia"> {{ Number(cb.giaSauGiam).toLocaleString('vi-VN') }} đ </span>

          <div v-if="getSelectedItems().some((item) => item.idCombo === cb.idCombo)">
            <span>
              SL:
              {{ getSelectedItems().find((item) => item.idCombo === cb.idCombo)?.soLuong }}
            </span>

            <button @click.stop="giamSoLuong(cb.idCombo)">-</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.combo-select-box {
  background: #222;
  border: 1px solid #333;
  border-radius: 10px;
  padding: 14px;
  margin-top: 5px;
}

/* Header */
.combo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

/* nút bỏ chọn */
.combo-header button {
  background: transparent;
  color: #ff6b6b;
  border: 1px solid #ff6b6b;
  border-radius: 5px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
  transition: 0.2s;
}

.combo-header button:hover {
  background: #ff6b6b;
  color: white;
}

/* ================= DANH SÁCH COMBO ================= */

.luoi-combo-mini {
  display: flex;
  gap: 12px;

  overflow-x: auto;
  overflow-y: hidden;

  padding-bottom: 6px;
  scroll-behavior: smooth;
}

.luoi-combo-mini::-webkit-scrollbar {
  height: 6px;
}

.luoi-combo-mini::-webkit-scrollbar-thumb {
  background: #555;
  border-radius: 20px;
}

.luoi-combo-mini::-webkit-scrollbar-track {
  background: transparent;
}

/* ================= CARD ================= */

.card-combo-mini {
  flex: 0 0 145px;
  flex-shrink: 0;
  width: 145px;

  background: #1a1a1a;
  border: 2px solid transparent;
  border-radius: 8px;

  overflow: hidden;
  display: flex;
  flex-direction: column;

  cursor: pointer;
  transition: all 0.25s ease;
}

.card-combo-mini:hover {
  border-color: #c5a059;
  transform: translateY(-2px);
}

/* Khi được chọn */
.card-combo-mini.active {
  border-color: #ff8c00;
  background: #292019;
}

/* Ảnh */
.khung-anh {
  width: 100%;
  height: 80px;
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
  justify-content: center;
  align-items: center;

  font-size: 11px;
  color: #555;
}

/* Nội dung */
.chi-tiet {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.ten {
  margin: 0;
  color: white;
  font-size: 12px;
  font-weight: 600;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gia {
  color: #c5a059;
  font-size: 12px;
  font-weight: bold;
}

/* Khu vực số lượng */
.chi-tiet > div {
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-top: 5px;
  padding-top: 5px;

  border-top: 1px solid #333;

  color: #ddd;
  font-size: 16px;
}

/* Nút giảm số lượng */
.chi-tiet button {
  width: 25px;
  height: 25px;

  border-radius: 50%;
  border: none;

  background: #c5a059;
  color: #111;

  font-weight: bold;
  cursor: pointer;

  display: flex;
  align-items: center;
  justify-content: center;

  transition: 0.2s;
}

.chi-tiet button:hover {
  background: #fff;
  transform: scale(1.1);
}

/* Loading */
.loading-text,
.trong-text {
  text-align: center;
  font-size: 12px;
  color: #888;
  padding: 10px 0;
}
</style>
