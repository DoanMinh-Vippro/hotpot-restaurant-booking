<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MonApi from '@/api/MonApi'
import type { Mon } from '@/api/MonApi'

interface MonDatBan {
  idMon: number
  tenMon: string
  giaSauGiam: number
  soLuong: number
}

const props = defineProps<{
  modelValue: MonDatBan[]
}>()

const emit = defineEmits(['update:modelValue', 'selectedMon'])

const danhSachMon = ref<Mon[]>([])
const loading = ref(false)

const loadMon = async () => {
  loading.value = true

  try {
    const res = await MonApi.hienThiMon()
    danhSachMon.value = (res.data || []).filter((mon: Mon) => mon.trangThai === 0)
  } catch (error) {
    console.error('Không thể tải danh sách món:', error)
  } finally {
    loading.value = false
  }
}

const getSelectedItems = () => props.modelValue || []

const selectMon = (mon: Mon) => {
  if (mon.trangThaiBan === 0) {
    alert(`Món "${mon.tenMon}" hiện đã hết hàng!`)
    return
  }

  const dsMon = [...getSelectedItems()]
  const index = dsMon.findIndex((item) => item.idMon === mon.idMon)

  if (index >= 0) {
    const current = dsMon[index]
    if (current) current.soLuong++
  } else {
    dsMon.push({
      idMon: mon.idMon,
      tenMon: mon.tenMon,
      giaSauGiam: Number(mon.giaSauGiam),
      soLuong: 1,
    })
  }

  emit('update:modelValue', dsMon)
  emit('selectedMon', dsMon)
}

const giamSoLuong = (idMon: number) => {
  const dsMon = [...getSelectedItems()]
  const index = dsMon.findIndex((item) => item.idMon === idMon)

  if (index >= 0) {
    const current = dsMon[index]
    if (current) {
      if (current.soLuong > 1) {
        current.soLuong--
      } else {
        dsMon.splice(index, 1)
      }
    }
  }

  emit('update:modelValue', dsMon)
  emit('selectedMon', dsMon)
}

const xoaTatCa = () => {
  emit('update:modelValue', [])
  emit('selectedMon', [])
}

onMounted(() => {
  loadMon()
})
</script>

<template>
  <div class="combo-select-box">
    <div class="combo-header">
      <span>🍲 Món đặt trước</span>
      <button v-if="getSelectedItems().length > 0" @click="xoaTatCa">Bỏ chọn</button>
    </div>

    <div v-if="loading" class="loading-text">Đang tải...</div>

    <div v-else class="luoi-combo-mini">
      <div
        v-for="mon in danhSachMon"
        :key="mon.idMon"
        class="card-combo-mini"
        :class="{ active: getSelectedItems().some((item) => item.idMon === mon.idMon) }"
        @click="selectMon(mon)"
      >
        <div class="khung-anh">
          <img v-if="mon.hinhAnh" :src="mon.hinhAnh" />
          <div v-else class="no-img">No Image</div>
        </div>

        <div class="chi-tiet">
          <h4 class="ten" :title="mon.tenMon">{{ mon.tenMon }}</h4>
          <span class="gia">{{ Number(mon.giaSauGiam).toLocaleString('vi-VN') }} đ</span>

          <div v-if="getSelectedItems().some((item) => item.idMon === mon.idMon)">
            <span>
              SL: {{ getSelectedItems().find((item) => item.idMon === mon.idMon)?.soLuong }}
            </span>
            <button @click.stop="giamSoLuong(mon.idMon)">-</button>
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

/* ================= DANH SÁCH MÓN SỔ DỌC (2-3 MÓN/HÀNG) ================= */

.luoi-combo-mini {
  display: grid;
  /* 3 món trên 1 hàng ngang */
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;

  /* Giới hạn chiều cao & Cuộn dọc */
  max-height: 320px;
  overflow-y: auto;
  padding-right: 6px;
}

/* Responsive: 2 món trên 1 hàng với màn nhỏ */
@media (max-width: 480px) {
  .luoi-combo-mini {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Thanh cuộn dọc */
.luoi-combo-mini::-webkit-scrollbar {
  width: 6px;
}

.luoi-combo-mini::-webkit-scrollbar-thumb {
  background: #c5a059;
  border-radius: 10px;
}

.luoi-combo-mini::-webkit-scrollbar-track {
  background: #111;
}

/* Card */
.card-combo-mini {
  width: 100%;
  background: #1a1a1a;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;

  display: flex;
  flex-direction: column;

  cursor: pointer;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.card-combo-mini:hover {
  border-color: #c5a059;
  transform: translateY(-2px);
}

.card-combo-mini.active {
  border-color: #ff8c00;
  background: #292019;
}

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
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 11px;
  color: #555;
}

.chi-tiet {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.ten {
  margin: 0;
  color: #fff;
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

.chi-tiet > div {
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-top: 5px;
  padding-top: 5px;
  border-top: 1px solid #333;

  color: #ddd;
  font-size: 12px;
}

.chi-tiet button {
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;

  background: #c5a059;
  color: #111;

  font-weight: bold;
  cursor: pointer;

  display: flex;
  justify-content: center;
  align-items: center;

  transition: 0.2s;
}

.chi-tiet button:hover {
  background: #fff;
  transform: scale(1.1);
}

.loading-text {
  text-align: center;
  font-size: 12px;
  color: #888;
  padding: 10px 0;
}
</style>