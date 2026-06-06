<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ComBoApi from '@/api/ComBoApi'
import type { Combo } from '@/api/ComBoApi'

const danhSachCombo = ref<Combo[]>([])
const loading = ref(false)

const loadComboGoiY = async () => {
  loading.value = true
  try {
    const res = await ComBoApi.hienThiComBo()
    // Lọc chỉ hiển thị những combo đang ở trạng thái "Còn bán" (trangThai === 1)
    danhSachCombo.value = (res.data || []).filter((cb: Combo) => cb.trangThai === 1)
  } catch (error) {
    console.error('Không thể tải danh sách combo gợi ý:', error)
  } finally {
    loading.value = false
  }
}

onMounted(loadComboGoiY)
</script>

<template>
  <div class="combo-select-box">
    <div class="combo-header">
      <span>🍱 Gói Combo Ưu Đãi Bán Chạy</span>
    </div>

    <div v-if="loading" class="loading-text">Đang tải danh sách combo...</div>

    <div v-else class="luoi-combo-mini">
      <div v-for="cb in danhSachCombo" :key="cb.idCombo" class="card-combo-mini">
        <div class="khung-anh">
          <img 
            v-if="cb.hinhAnh" 
            :src="`http://localhost:8080/uploads/${cb.hinhAnh}`" 
            alt="Combo"
          />
          <div v-else class="no-img">No Name</div>
        </div>
        <div class="chi-tiet">
          <h4 class="ten" :title="cb.tenCombo">{{ cb.tenCombo }}</h4>
          <span class="gia">{{ Number(cb.giaCombo).toLocaleString('vi-VN') }} đ</span>
        </div>
      </div>
    </div>

    <div v-if="!loading && danhSachCombo.length === 0" class="trong-text">
      Không có combo nào đang mở bán.
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

.loading-text, .trong-text {
  text-align: center;
  font-size: 12px;
  color: #888;
  padding: 10px 0;
}
</style>