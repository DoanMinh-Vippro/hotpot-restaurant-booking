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

const normalizeLoaiGiamValue = (value: string | null | undefined) => {
  const normalized = `${value ?? ''}`
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toUpperCase()

  if (['PHANTRAM', 'PERCENT', 'PHNTRAM', 'PHTNTRAM', 'PHTRAM'].includes(normalized)) return 'PHẦN TRĂM'
  if (normalized.includes('PH') && normalized.includes('TRAM')) return 'PHẦN TRĂM'
  if (['GIATRI', 'TIEN', 'TIENMAT', 'VND', 'FIXED', 'MONEY'].includes(normalized)) return 'GIÁ TRỊ'
  if (normalized.includes('TIEN') || normalized.includes('MAT') || normalized.includes('GIATRI') || normalized.includes('VALUE')) return 'GIÁ TRỊ'

  return 'PHẦN TRĂM'
}

const nhan_gui = computed(() => (props.che_do_bieu_mau === 'create' ? 'Tạo mới' : 'Cập nhật'))
const la_phan_tram = computed(() => normalizeLoaiGiamValue(bieu_mau.loaiGiam) === 'PHẦN TRĂM')
const nhan_gia_tri_giam = computed(() => (la_phan_tram.value ? 'Giá trị giảm (%)' : 'Giá trị giảm (đ)'))
const noi_dung_gia_tri_giam = computed(() => (la_phan_tram.value ? 'Khoảng từ 1% đến 100%' : 'Giá trị theo đơn vị VNĐ (đ)'))
const placeholder_gia_tri_giam = computed(() => (la_phan_tram.value ? 'Nhập % từ 1 - 100' : 'Nhập số tiền giảm (đ)'))

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
    loi_val.giaTriGiam = la_phan_tram.value ? 'Giá trị phần trăm phải lớn hơn 0' : 'Giá trị giảm phải lớn hơn 0'
    valid = false
  } else if (la_phan_tram.value && (giaTriGiam < 1 || giaTriGiam > 100)) {
    loi_val.giaTriGiam = 'Giá trị phần trăm phải từ 1 đến 100'
    valid = false
  }

  if (la_phan_tram.value) {
    if (!bieu_mau.giaTriGiamToiDa) {
      loi_val.giaTriGiamToiDa = 'Giá trị giảm tối đa không được để trống'
      valid = false
    } else if (Number.isNaN(giaTriToiDa) || giaTriToiDa <= 0) {
      loi_val.giaTriGiamToiDa = 'Giá trị giảm tối đa phải lớn hơn 0'
      valid = false
    } else if (!Number.isNaN(giaTriGiam) && !Number.isNaN(giaTriToiDa) && giaTriToiDa < giaTriGiam) {
      loi_val.giaTriGiamToiDa = 'Giá trị giảm tối đa phải lớn hơn hoặc bằng giá trị giảm'
      valid = false
    }
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
    giaTriGiamToiDa: la_phan_tram.value ? Number(bieu_mau.giaTriGiamToiDa || 0) : Number(bieu_mau.giaTriGiam || 0),
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
      bieu_mau.loaiGiam = normalizeLoaiGiamValue(discount.loaiGiam)
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
        {{ nhan_gia_tri_giam }}
        <input
          v-model="bieu_mau.giaTriGiam"
          type="number"
          :min="la_phan_tram ? 1 : 0"
          :max="la_phan_tram ? 100 : undefined"
          step="0.01"
          :placeholder="placeholder_gia_tri_giam"
        />
        <span class="ghi-chu-truong">{{ noi_dung_gia_tri_giam }}</span>
        <span class="loi-truong" v-if="loi_val.giaTriGiam">{{ loi_val.giaTriGiam }}</span>
      </label>

      <label v-if="la_phan_tram">
        Giá trị giảm tối đa (VNĐ)
        <input v-model="bieu_mau.giaTriGiamToiDa" type="number" min="0" step="0.01" placeholder="Nhập số tiền tối đa" />
        <span class="ghi-chu-truong">Mức giảm tối đa chỉ áp dụng cho mã giảm giá theo phần trăm.</span>
        <span class="loi-truong" v-if="loi_val.giaTriGiamToiDa">{{ loi_val.giaTriGiamToiDa }}</span>
      </label>

      <label>
        Loại giảm
        <select v-model="bieu_mau.loaiGiam">
          <option value="PHẦN TRĂM">Phần trăm</option>
          <option value="GIÁ TRỊ">Tiền mặt</option>
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
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  box-shadow: 0 16px 40px rgba(103, 72, 32, 0.08);
  border-radius: 24px;
  padding: 24px;
  grid-column: 2;
  color: #5f3d22;
}

.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.tieu-de-panel h2 {
  margin: 0 0 8px;
  font-size: 1.1rem;
  color: #8b5e34;
}

.tieu-de-panel p {
  margin: 0;
  color: #8f6b46;
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
  color: #6b4728;
  font-weight: 600;
}

label > :not(.loi-truong) {
  margin-top: 6px;
}

input[type='text'],
input[type='date'],
input[type='number'],
select {
  border: 1px solid #e6d2aa;
  background: #fffdf8;
  color: #5f3d22;
  border-radius: 14px;
  padding: 12px 14px;
}

input::placeholder {
  color: #b18c62;
}

select {
  cursor: pointer;
}

.ghi-chu-truong {
  margin-top: 4px;
  color: #8f6b46;
  font-size: 0.78rem;
}

.loi-truong {
  margin-top: 4px;
  color: #c94f3a;
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
  border-radius: 14px;
  padding: 12px 20px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nut-chinh {
  background: #d8a85c;
  color: #3d2814;
}

.nut-chinh:hover:not(:disabled) {
  background: #c99646;
}

.nut-chinh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.nut-phu {
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
}

.nut-phu:hover:not(:disabled) {
  background: #f2dfb0;
}

.nut-phu:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.thong_bao-thanh-cong {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(76, 175, 80, 0.13);
  color: #2f7a3c;
  margin: 12px 0 0;
  font-size: 0.9rem;
}

.thong_bao-loi {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 107, 107, 0.13);
  color: #b84f3f;
  margin: 12px 0 0;
  font-size: 0.9rem;
}
</style>
