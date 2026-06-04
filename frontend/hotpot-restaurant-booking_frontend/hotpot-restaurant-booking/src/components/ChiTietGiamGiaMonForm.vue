<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'

import type {
  ChiTietGiamGiaMon,
  ChiTietGiamGiaMonRequest,
} from '../api/ChiTietGiamGiaMonApi'

import type { Mon } from '../api/MonApi'
import type { DotGiamGia } from '../api/DotGiamGiaApi'

import MonApi from '../api/MonApi'
import DotGiamGiaApi from '../api/DotGiamGiaApi'

const emit = defineEmits(['submit'])

const danhSachMon = ref<Mon[]>([])
const danhSachDGG = ref<DotGiamGia[]>([])

const form = reactive({
  mucGiam: '',
  idMon: '' as number | '',
  idDotGiamGia: '' as number | '',
})

onMounted(async () => {
  try {
    const [monRes, dggRes] = await Promise.all([
      MonApi.hienThiMon(),
      DotGiamGiaApi.getDanhSach(),
    ])
    danhSachMon.value = Array.isArray(monRes.data) ? monRes.data : (monRes.data as any).content || []
    danhSachDGG.value = Array.isArray(dggRes.data) ? dggRes.data : (dggRes.data as any).content || []
  } catch (error) {
    console.error("Lỗi khi tải cấu hình danh mục lựa chọn:", error)
  }
})

const gui = () => {
  if (!form.idMon) return alert("Vui lòng chọn Món ăn áp dụng giảm giá")
  if (!form.idDotGiamGia) return alert("Vui lòng chọn Chương trình giảm giá")
  
  if (form.mucGiam === '' || form.mucGiam === null) return alert("Mức giảm không được để trống")
  
  const giáTrịMứcGiảm = Number(form.mucGiam)
  if (giáTrịMứcGiảm <= 0) return alert("Mức giảm phải lớn hơn 0")

  emit('submit', {
    mucGiam: giáTrịMứcGiảm,
    idMon: form.idMon as number,
    idDotGiamGia: form.idDotGiamGia as number,
  })
}

defineExpose({
  // Sử dụng kiểu dữ liệu 'any' để tránh lỗi biên dịch nghiêm ngặt TypeScript 2339
  fillForm(item?: any) {
    if (!item) {
      form.mucGiam = ''
      form.idMon = ''
      form.idDotGiamGia = ''
      return
    }

    // Nếu object chỉ chứa idDotGiamGia (được dựng giả lập từ URL), ta thiết lập Form thêm mới
    if (item.idDotGiamGia && (item.idChiTietGiamGiaMon === undefined || item.idChiTietGiamGiaMon === null)) {
      form.mucGiam = ''
      form.idMon = ''
      form.idDotGiamGia = item.idDotGiamGia
      return
    }

    // Thiết lập điền toàn bộ dữ liệu khi bấm nút "Sửa" bản ghi có sẵn từ DB
    form.mucGiam = item.mucGiam !== undefined && item.mucGiam !== null ? item.mucGiam.toString() : ''
    form.idMon = item.idMon ?? item.mon?.idMon ?? ''
    form.idDotGiamGia = item.idDotGiamGia ?? item.dotGiamGia?.idDotGiamGia ?? ''
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Chi tiết giảm giá món</h2>
      <p>Thêm hoặc cập nhật chương trình giảm giá.</p>
    </div>

    <div class="luoi-bieu-mau">
      <label>
        Mức giảm (%)
        <input v-model="form.mucGiam" type="number" min="1" placeholder="Nhập phần trăm giảm..." />
      </label>

      <label>
        Món ăn
        <select v-model="form.idMon">
          <option value="">-- Chọn món --</option>
          <option v-for="m in danhSachMon" :key="m.idMon" :value="m.idMon">
            {{ m.tenMon }}
          </option>
        </select>
      </label>

      <label>
        Chương trình giảm giá
        <select v-model="form.idDotGiamGia">
          <option value="">-- Chọn chương trình --</option>
          <option
            v-for="d in danhSachDGG"
            :key="d.idDotGiamGia"
            :value="d.idDotGiamGia"
          >
            {{ d.tenChuongTrinh }}
          </option>
        </select>
      </label>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="gui">
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
}

.tieu-de-panel h2 {
  color: #f8d46a;
  margin-bottom: 10px;
}

.tieu-de-panel p {
  color: #c7c7c7;
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

input,
select {
  margin-top: 6px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: white;
  outline: none;
}
input:focus,
select:focus {
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
  border: none;
  font-weight: 600;
  cursor: pointer;
}

select option {
  background: #151515;
  color: #fff;
}
</style>