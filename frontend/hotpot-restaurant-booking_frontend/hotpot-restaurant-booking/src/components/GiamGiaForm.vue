<script setup lang="ts">
import { computed, reactive } from 'vue'
import type { GiamGia } from '../api/GiamGiaApi'

const props = defineProps<{
  che_do_bieu_mau: 'create' | 'edit'
  dang_gui: boolean
  loi_may_chu: string
  thong_bao_thanh_cong: string
}>()

const emit = defineEmits<{
  'submit': [payload: Record<string, unknown>]
  'reset': []
}>()

const bieu_mau = reactive({
  maGiamGia: '',
  ngayKetThuc: '',
  dieuKienSuDung: '',
  giaTriGiamToiDa: '',
  giaTriGiam: '',
  loaiGiam: 'PHẦN TRĂM',
  soLuongMaGiamGia: 1,
  trangThai: 1,
})

const loi_val = reactive<Record<string, string>>({})

const nhan_gui = computed(() => (props.che_do_bieu_mau === 'create' ? 'Tạo mới' : 'Cập nhật'))

const kiem_tra_bieu_mau = () => {
  Object.keys(loi_val).forEach((key) => delete loi_val[key])
  let valid = true
  const ma = bieu_mau.maGiamGia.trim()
  const dieuKien = bieu_mau.dieuKienSuDung.trim()
  const giaTriGiam = Number(bieu_mau.giaTriGiam)
  const giaTriToiDa = Number(bieu_mau.giaTriGiamToiDa)

  if (!ma) {
    loi_val.maGiamGia = 'Mã giảm giá không được để trống'
    valid = false
  } else if (ma.length < 3 || ma.length > 40) {
    loi_val.maGiamGia = 'Mã giảm giá phải từ 3 đến 40 ký tự'
    valid = false
  }

  if (!dieuKien) {
    loi_val.dieuKienSuDung = 'Điều kiện sử dụng không được để trống'
    valid = false
  }

  if (!bieu_mau.ngayKetThuc) {
    loi_val.ngayKetThuc = 'Ngày kết thúc không được để trống'
    valid = false
  }

  if (!bieu_mau.giaTriGiam) {
    loi_val.giaTriGiam = 'Giá trị giảm không được để trống'
    valid = false
  } else if (Number.isNaN(giaTriGiam) || giaTriGiam <= 0) {
    loi_val.giaTriGiam = 'Giá trị giảm phải lớn hơn 0'
    valid = false
  }

  if (!bieu_mau.giaTriGiamToiDa) {
    loi_val.giaTriGiamToiDa = 'Giá trị giảm tối đa không được để trống'
    valid = false
  } else if (Number.isNaN(giaTriToiDa) || giaTriToiDa <= 0) {
    loi_val.giaTriGiamToiDa = 'Giá trị giảm tối đa phải lớn hơn 0'
    valid = false
  }

  if (!Number.isNaN(giaTriGiam) && !Number.isNaN(giaTriToiDa) && giaTriToiDa < giaTriGiam) {
    loi_val.giaTriGiamToiDa = 'Giá trị giảm tối đa phải lớn hơn hoặc bằng giá trị giảm'
    valid = false
  }

  if (!bieu_mau.soLuongMaGiamGia || bieu_mau.soLuongMaGiamGia <= 0) {
    loi_val.soLuongMaGiamGia = 'Số lượng mã giảm giá phải lớn hơn 0'
    valid = false
  }

  return valid
}

const xu_ly_gui = () => {
  if (!kiem_tra_bieu_mau()) return

  const payload = {
    maGiamGia: bieu_mau.maGiamGia.trim(),
    ngayKetThuc: bieu_mau.ngayKetThuc,
    dieuKienSuDung: bieu_mau.dieuKienSuDung.trim(),
    giaTriGiamToiDa: Number(bieu_mau.giaTriGiamToiDa),
    giaTriGiam: Number(bieu_mau.giaTriGiam),
    loaiGiam: bieu_mau.loaiGiam.trim(),
    soLuongMaGiamGia: bieu_mau.soLuongMaGiamGia,
    trangThai: bieu_mau.trangThai,
  }

  emit('submit', payload)
}

const xu_ly_huy = () => {
  Object.keys(loi_val).forEach((key) => delete loi_val[key])
  bieu_mau.maGiamGia = ''
  bieu_mau.ngayKetThuc = ''
  bieu_mau.dieuKienSuDung = ''
  bieu_mau.giaTriGiamToiDa = ''
  bieu_mau.giaTriGiam = ''
  bieu_mau.loaiGiam = 'PHẦN TRĂM'
  bieu_mau.soLuongMaGiamGia = 1
  bieu_mau.trangThai = 1
  emit('reset')
}

defineExpose({
  bieu_mau,
  chuan_bi_bieu_mau: (discount?: GiamGia) => {
    if (discount) {
      bieu_mau.maGiamGia = discount.maGiamGia
      bieu_mau.ngayKetThuc = discount.ngayKetThuc ?? ''
      bieu_mau.dieuKienSuDung = discount.dieuKienSuDung ?? ''
      bieu_mau.giaTriGiamToiDa = discount.giaTriGiamToiDa?.toString() ?? ''
      bieu_mau.giaTriGiam = discount.giaTriGiam?.toString() ?? ''
      bieu_mau.loaiGiam = discount.loaiGiam ?? 'PHẦN TRĂM'
      bieu_mau.soLuongMaGiamGia = discount.soLuongMaGiamGia ?? 1
      bieu_mau.trangThai = discount.trangThai ?? 1
    } else {
      xu_ly_huy()
    }
    Object.keys(loi_val).forEach((key) => delete loi_val[key])
  },
})
</script>

<template>
  <section class="bieu-mau-panel">
    <div class="tieu-de-panel">
      <div>
        <h2>{{ che_do_bieu_mau === 'create' ? 'Tạo mã giảm giá mới' : 'Cập nhật mã giảm giá' }}</h2>
        <p>{{ che_do_bieu_mau === 'create' ? 'Nhập đầy đủ thông tin để tạo mã.' : 'Cập nhật thông tin mã giảm giá đã chọn.' }}</p>
      </div>
    </div>

    <div class="luoi-bieu-mau">
      <label>
        Mã giảm giá
        <input v-model="bieu_mau.maGiamGia" type="text" />
        <span class="loi-truong" v-if="loi_val.maGiamGia">{{ loi_val.maGiamGia }}</span>
      </label>

      <label>
        Điều kiện sử dụng
        <input v-model="bieu_mau.dieuKienSuDung" type="text" />
        <span class="loi-truong" v-if="loi_val.dieuKienSuDung">{{ loi_val.dieuKienSuDung }}</span>
      </label>

      <label>
        Ngày kết thúc
        <input v-model="bieu_mau.ngayKetThuc" type="date" />
        <span class="loi-truong" v-if="loi_val.ngayKetThuc">{{ loi_val.ngayKetThuc }}</span>
      </label>

      <label>
        Giá trị giảm
        <input v-model="bieu_mau.giaTriGiam" type="number" min="0" step="0.01" />
        <span class="loi-truong" v-if="loi_val.giaTriGiam">{{ loi_val.giaTriGiam }}</span>
      </label>

      <label>
        Giá trị giảm tối đa
        <input v-model="bieu_mau.giaTriGiamToiDa" type="number" min="0" step="0.01" />
        <span class="loi-truong" v-if="loi_val.giaTriGiamToiDa">{{ loi_val.giaTriGiamToiDa }}</span>
      </label>

      <label>
        Loại giảm
        <select v-model="bieu_mau.loaiGiam">
          <option value="PHẦN TRĂM">PHẦN TRĂM</option>
          <option value="GIÁ TRỊ">GIÁ TRỊ</option>
        </select>
      </label>

      <label>
        Số lượng mã
        <input v-model.number="bieu_mau.soLuongMaGiamGia" type="number" min="1" />
        <span class="loi-truong" v-if="loi_val.soLuongMaGiamGia">{{ loi_val.soLuongMaGiamGia }}</span>
      </label>

      <label>
        Trạng thái
        <select v-model.number="bieu_mau.trangThai">
          <option :value="1">Hoạt động</option>
          <option :value="0">Ngưng</option>
        </select>
      </label>
    </div>

    <div class="nhom-nut">
      <button class="nut-chinh" type="button" @click="xu_ly_gui" :disabled="dang_gui">{{ nhan_gui }}</button>
      <button class="nut-phu" type="button" @click="xu_ly_huy" :disabled="dang_gui">Hủy</button>
    </div>

    <p v-if="thong_bao_thanh_cong" class="thong_bao-thanh-cong">{{ thong_bao_thanh_cong }}</p>
    <p v-if="loi_may_chu" class="thong_bao-loi">{{ loi_may_chu }}</p>
  </section>
</template>

<style scoped>
.bieu-mau-panel {
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  border-radius: 28px;
  padding: 26px;
  grid-column: 2;
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.tieu-de-panel h2 {
  margin: 0 0 10px;
  font-size: 1.15rem;
}

.tieu-de-panel p {
  margin: 0;
  color: #c7c7c7;
  max-width: 420px;
}

.luoi-bieu-mau {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-bottom: 18px;
}

label {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
  color: #d8d8d8;
  font-weight: 500;
}

label > :not(.loi-truong) {
  margin-top: 6px;
}

input[type='text'],
input[type='date'],
input[type='number'],
select {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  color: #f5f5f5;
  border-radius: 16px;
  padding: 14px 16px;
}

input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

select {
  cursor: pointer;
}

.loi-truong {
  margin-top: 4px;
  color: #ff6b6b;
  font-size: 0.8rem;
}

.nhom-nut {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.nut-chinh,
.nut-phu {
  flex: 1;
  border: none;
  border-radius: 16px;
  padding: 12px 24px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nut-chinh {
  background: #f8d46a;
  color: #1a1410;
}

.nut-chinh:hover:not(:disabled) {
  background: #f5cd4a;
}

.nut-chinh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.nut-phu {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.nut-phu:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
}

.nut-phu:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.thong_bao-thanh-cong {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(76, 175, 80, 0.15);
  color: #81c784;
  margin: 12px 0 0;
  font-size: 0.9rem;
}

.thong_bao-loi {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
  margin: 12px 0 0;
  font-size: 0.9rem;
}
</style>
