<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'

import type { ChiTietComBo, ChiTietComBoRequest } from '../api/ChiTietComBoApi'
import type { Mon } from '../api/MonApi'
import type { Combo } from '../api/ComBoApi'

import MonApi from '../api/MonApi'
import ComboApi from '../api/ComBoApi'

const emit = defineEmits(['submit'])

const danhSachMon = ref<Mon[]>([])
const danhSachCombo = ref<Combo[]>([])

const form = reactive({
  soLuong: 1,
  idMon: '' as number | '',
  idCombo: '' as number | '',
  moTa: '',
})

onMounted(async () => {
  try {
    const [monRes, comboRes] = await Promise.all([
      MonApi.hienThiMon(),
      ComboApi.hienThiComBo(),
    ])
    danhSachMon.value = Array.isArray(monRes.data) ? monRes.data : (monRes.data as any).content || []
    danhSachCombo.value = Array.isArray(comboRes.data) ? comboRes.data : (comboRes.data as any).content || []
  } catch (error) {
    console.error("Lỗi khi tải dữ liệu cấu hình:", error)
  }
})

const gui = () => {
  const mTa = form.moTa || ''

  // VALIDATE ĐẦU VÀO
  if (!form.idCombo) return alert("Vui lòng chọn Combo")
  if (!form.idMon) return alert("Vui lòng chọn Món ăn")
  
  if (!form.soLuong) return alert("Số lượng không được để trống")
  if (Number(form.soLuong) <= 0) return alert("Số lượng phải lớn hơn 0")

  if (mTa !== mTa.trim()) return alert("Mô tả không được chứa khoảng trắng ở đầu hoặc cuối")
  if (/\s{2,}/.test(mTa)) return alert("Mô tả không được chứa nhiều khoảng trắng liên tiếp")

  emit('submit', {
    soLuong: Number(form.soLuong),
    idMon: form.idMon as number,
    idCombo: form.idCombo as number,
    moTa: mTa.trim(),
  } as ChiTietComBoRequest)
}

defineExpose({
  fillForm(item?: ChiTietComBo) {
    if (!item) {
      form.soLuong = 1
      form.idMon = ''
      form.idCombo = ''
      form.moTa = ''
      return
    }

    form.soLuong = item.soLuong
    form.moTa = item.moTa

    // Dự phòng trường hợp API trả về id nằm phẳng hoặc lồng trong object quan hệ
    form.idMon = (item as any).idMon ?? (item as any).mon?.idMon ?? ''
    form.idCombo = (item as any).idCombo ?? (item as any).combo?.idCombo ?? ''
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <h2>Thông tin Chi Tiết Combo</h2>
      <p>Thêm mới hoặc cập nhật combo món ăn.</p>
    </div>

    <div class="luoi-bieu-mau">
      <label>
        Số lượng
        <input v-model.number="form.soLuong" type="number" min="1" placeholder="Nhập số lượng..." />
      </label>

      <label>
        Combo
        <select v-model="form.idCombo">
          <option value="">-- Chọn combo --</option>
          <option
            v-for="c in danhSachCombo"
            :key="c.idCombo"
            :value="c.idCombo"
          >
            {{ c.tenCombo }}
          </option>
        </select>
      </label>

      <label>
        Món ăn
        <select v-model="form.idMon">
          <option value="">-- Chọn món --</option>
          <option
            v-for="m in danhSachMon"
            :key="m.idMon"
            :value="m.idMon"
          >
            {{ m.tenMon }}
          </option>
        </select>
      </label>

      <label>
        Mô tả
        <input v-model="form.moTa" type="text" placeholder="Nhập mô tả" />
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
  border: 1px solid rgba(255,255,255,.08);
  background: rgba(255,255,255,.04);
  color: white;
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
  color: #ffffff;
}
</style>