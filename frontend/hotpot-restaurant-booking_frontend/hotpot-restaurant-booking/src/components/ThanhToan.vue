<script setup lang="ts">
import MonApi from '@/api/MonApi'
import ComBoApi from '@/api/ComBoApi'
import { onMounted, ref, computed, watch } from 'vue'
import GiamGiaApi from '@/api/GiamGiaApi'
import PopupThanhToan from './PopupThanhToan.vue'
import PopupTienMat from './PopupTienMat.vue'
import HoaDonApi from '@/api/HoaDonApi.ts'
import HoaDonChiTietApi from '@/api/HoaDonChiTietApi'

// ================= PROPS =================
const props = defineProps<{
  ban: any
  datBan: any | null
}>()

// ================= EMIT (BẮT BUỘC GIỮ) =================
const emit = defineEmits(['quayLai'])

const quayLai = () => {
  emit('quayLai')
}
// ================= STATE =================
const danhSachCombo = ref<any[]>([])
const danhSachMonAn = ref<any[]>([])
const danhMucDangChon = ref('combo')

const gioHang = ref<any[]>([])
const danhSachGiamGia = ref<any[]>([])
const giamGiaDangChon = ref<number | null>(null)

const phuongThucThanhToan = ref(false)
const tienMatThanhToan = ref(false)

const hoaDonHienTai = ref<any>(null)

// ================= LOAD DATA =================
const loadData = async () => {
  const combo = await ComBoApi.hienThiComBo()
  const mon = await MonApi.hienThiMon()

  danhSachCombo.value = combo.data
  danhSachMonAn.value = mon.data
}

const loadGiamGia = async () => {
  const gg = await GiamGiaApi.getDanhSach()
  danhSachGiamGia.value = gg.data
}

// ================= GIỎ HÀNG =================
const themVaoGio = (item: any, loai: string) => {
  const tonTai = gioHang.value.find(
    (x) =>
      x.loai === loai && (loai === 'MON' ? x.idMon === item.idMon : x.idCombo === item.idCombo),
  )

  if (tonTai) {
    tonTai.soLuong++
  } else {
    gioHang.value.push({
      idMon: item.idMon ?? null,
      idCombo: item.idCombo ?? null,

      tenMon: item.tenMon ?? null,
      tenCombo: item.tenCombo ?? null,

      gia: loai === 'MON' ? item.giaSauGiam : (item.giaCombo ?? 0),

      soLuong: 1,
      loai,
    })
  }
}

const giamSoLuong = (item: any) => {
  const index = gioHang.value.findIndex(
    (x) =>
      x.loai === item.loai &&
      (item.loai === 'COMBO' ? x.idCombo === item.idCombo : x.idMon === item.idMon),
  )

  if (index === -1) return

  if (gioHang.value[index].soLuong > 1) {
    gioHang.value[index].soLuong--
  } else {
    gioHang.value.splice(index, 1)
  }
}

// ================= POPUP =================
const optionPay = async () => {
  if (gioHang.value.length === 0) {
    alert('Giỏ hàng đang trống')
    return
  }
  phuongThucThanhToan.value = true
}

const closePopup = () => {
  phuongThucThanhToan.value = false
}

const popupTienMat = () => {
  phuongThucThanhToan.value = false
  tienMatThanhToan.value = true
}

const closeTienMatPopup = () => {
  tienMatThanhToan.value = false
}

// ================= COMPUTED =================
const tongTien = computed(() => {
  return gioHang.value.reduce((tong, item) => tong + item.gia * item.soLuong, 0)
})

const tienGiamGia = computed(() => {
  const base = tongTien.value

  if (!giamGiaDangChon.value) return 0

  const giamGia = danhSachGiamGia.value.find((g) => g.idGiamGia === giamGiaDangChon.value)

  if (!giamGia) return 0

  if (giamGia.loaiGiam === 'TIENMAT') {
    return Math.min(giamGia.giaTriGiam, base)
  }

  if (giamGia.loaiGiam === 'PHANTRAM') {
    return (base * giamGia.giaTriGiam) / 100
  }

  return 0
})

const tongThanhToan = computed(() => {
  return Math.max(0, tongTien.value - tienGiamGia.value)
})

// ================= HÓA ĐƠN =================
const checkHoaDonTam = async () => {
  try {
    const res = await HoaDonApi.findByBanAndStatus(props.ban.idBan, 0)

    const hd = res.data
    if (!hd) return

    hoaDonHienTai.value = hd

    gioHang.value = (hd.chiTiet || []).map((item: any) => ({
      idMon: item.idMon,
      idCombo: item.idCombo,

      tenMon: item.tenMon,
      tenCombo: item.tenCombo,

      gia: item.donGiaHienTai ?? item.giaCombo ?? item.giaBanTaiThoiDiem ?? 0,

      soLuong: item.soLuong,
      loai: item.idMon ? 'MON' : 'COMBO',
    }))

    giamGiaDangChon.value = hd.idGiamGia ?? null
  } catch (e) {
    console.log('Không có hóa đơn tạm')
  }
  console.log('LOAD HOA DON TAM')
}

const addHoaDon = async (payload: any) => {
  const res = await HoaDonApi.create(payload)
  const idHoaDon = res.data.idHoaDon

  for (const item of gioHang.value) {
    const gia = item.gia ?? item.donGiaHienTai ?? item.giaCombo ?? 0

    await HoaDonChiTietApi.add({
      maHoaDonChiTiet: `HDCT${Date.now()}${item.idMon || item.idCombo}`,
      idHoaDon,

      idMon: item.idMon,
      idCombo: item.idCombo,

      soLuong: item.soLuong,
      giaBanTaiThoiDiem: gia,
      tienGiamGiaMon: 0,
      thanhTien: gia * item.soLuong,
    })
  }

  hoaDonHienTai.value = res.data
}

const updateHoaDon = async (idHoaDon: number, payload: any) => {
  await HoaDonApi.update(idHoaDon, payload)

  await HoaDonChiTietApi.deleteByHoaDon(idHoaDon)

  for (const item of gioHang.value) {
    const gia = item.gia ?? item.donGiaHienTai ?? item.giaCombo ?? 0

    await HoaDonChiTietApi.add({
      maHoaDonChiTiet: `HDCT${Date.now()}${item.idMon || item.idCombo}`,
      idHoaDon,

      idMon: item.idMon,
      idCombo: item.idCombo,

      soLuong: item.soLuong,
      giaBanTaiThoiDiem: gia,
      tienGiamGiaMon: 0,
      thanhTien: gia * item.soLuong,
    })
  }
}

const xuLyHoaDon = async (trangThaiHoaDon: number, trangThaiThanhToan: number) => {
  const payload = {
    maHoaDon: hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`,
    trangThaiHoaDon,
    trangThaiThanhToan,
    phuongThucThanhToan: trangThaiThanhToan === 1 ? 1 : null,
    tienTruocGiam: tongTien.value,
    tienGiamGia: tienGiamGia.value,
    tongTien: tongThanhToan.value,
    thoiGianXuat: new Date().toISOString(),
    idBan: props.ban.idBan,
    idGiamGia: giamGiaDangChon.value,
    idDatBan: props.datBan?.idDatBan ?? null,
    idKhachHang: props.datBan?.idKhachHang ?? null,
    sdtKhachHang: props.datBan?.sdtKhachHang ?? null,
    tienCoc: props.datBan?.soTienCoc ?? null,
  }

  if (hoaDonHienTai.value) {
    await updateHoaDon(hoaDonHienTai.value.idHoaDon, payload)
    await checkHoaDonTam()
  } else {
    await addHoaDon(payload)
  }
}

// ================= ACTION =================
const luuTam = async () => {
  try {
    await xuLyHoaDon(0, 0)
    alert('Lưu thành công')
  } catch {
    alert('Lưu thất bại')
  }
}

const taoHoaDon = async () => {
  try {
    await xuLyHoaDon(1, 1)

    alert('Thanh toán thành công')

    hoaDonHienTai.value = null
    gioHang.value = []
    giamGiaDangChon.value = null
    tienMatThanhToan.value = false
  } catch (e) {
    alert('Thanh toán thất bại')
  }
}
//=========================================

watch(
  () => props.datBan,
  (db) => {
    if (!db) return

    console.log('datBan:', db)

    gioHang.value = []

    // ✔ check đúng field idCombo
    if (db.idCombo) {
      gioHang.value.push({
        idCombo: db.idCombo,
        tenCombo: db.tenCombo, // nếu backend có trả
        gia: db.giaCombo ?? 0,
        soLuong: 1,
        loai: 'COMBO',
      })

      console.log('✔ Fill combo:', db.idCombo)
    } else {
      console.log('❌ Không có combo trong datBan')
    }
  },
  { immediate: true },
)

// ================= INIT =================
onMounted(() => {
  loadData()
  loadGiamGia()
  checkHoaDonTam()
})
</script>

<template>
  <div class="thanh-toan-container">
    <!-- DANH MỤC -->
    <div class="danh-muc">
      <div class="title">Danh mục</div>

      <div
        class="menu-item"
        :class="{ active: danhMucDangChon === 'combo' }"
        @click="danhMucDangChon = 'combo'"
      >
        Combo
      </div>

      <div
        class="menu-item"
        :class="{ active: danhMucDangChon === 'mon' }"
        @click="danhMucDangChon = 'mon'"
      >
        Món ăn
      </div>

      <div>
        <button class="btn-luu" @click="luuTam()">Lưu</button>
        <button class="btn-quay-lai" @click="quayLai">Quay Lại</button>
      </div>
    </div>

    <!-- DANH SÁCH MÓN -->
    <div class="danh-sach-mon">
      <div class="title">
        {{ danhMucDangChon === 'combo' ? 'Danh sách Combo' : 'Danh sách Món ăn' }}
      </div>

      <div class="food-grid">
        <!-- COMBO -->
        <template v-if="danhMucDangChon === 'combo'">
          <div
            v-for="combo in danhSachCombo"
            :key="combo.idCombo"
            class="food-card"
            @click="themVaoGio(combo, 'COMBO')"
          >
            {{ combo.tenCombo }}
          </div>
        </template>

        <!-- MÓN -->
        <template v-else>
          <div
            v-for="mon in danhSachMonAn"
            :key="mon.idMon"
            class="food-card"
            @click="themVaoGio(mon, 'MON')"
          >
            {{ mon.tenMon }}
          </div>
        </template>
      </div>
    </div>

    <!-- GIỎ HÀNG -->
    <div class="gio-hang">
      <div class="title">Giỏ hàng: {{ props.ban.tenBan }}</div>

      <div class="gio-hang-list">
        <div
          v-for="item in gioHang"
          :key="`${item.loai}-${item.idMon ?? item.idCombo ?? Math.random()}`"
          class="cart-item"
        >
          <button class="btn-minus" @click="giamSoLuong(item)">-</button>

          <div class="item-info">
            <div class="item-name">
              {{ item.tenMon || item.tenCombo }}
            </div>

            <div class="item-bottom">
              <div class="item-qty">x{{ item.soLuong }}</div>

              <div class="item-price">
                {{ item.soLuong }} × {{ Number(item.gia ?? 0).toLocaleString('vi-VN') }} đ
                <b> = {{ ((item.gia ?? 0) * (item.soLuong ?? 1)).toLocaleString('vi-VN') }} đ </b>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="gio-hang-footer">
        <hr />

        <div class="tong-tien">Tổng tiền tạm tính: {{ tongTien.toLocaleString('vi-VN') }} đ</div>

        <div class="tong-tien">Tiền giảm giá: {{ tienGiamGia.toLocaleString('vi-VN') }} đ</div>

        <div class="tong-tien">
          Tổng tiền thanh toán: {{ tongThanhToan.toLocaleString('vi-VN') }} đ
        </div>

        <!-- DISCOUNT -->
        <div>
          <select class="discount-input" v-model="giamGiaDangChon">
            <option :value="null">Chọn mã giảm giá</option>

            <option v-for="g in danhSachGiamGia" :key="g.idGiamGia" :value="g.idGiamGia">
              {{ g.maGiamGia }} - {{ g.giaTriGiam }}{{ g.loaiGiam === 'PHANTRAM' ? '%' : 'Đ' }}
            </option>
          </select>
        </div>

        <button class="btn-thanh-toan" @click="optionPay">Thanh toán</button>
      </div>
    </div>

    <!-- POPUP -->
    <PopupThanhToan
      v-if="phuongThucThanhToan"
      :tongTien="tongThanhToan"
      @close="closePopup"
      @chonTienMat="popupTienMat"
    />

    <PopupTienMat
      v-if="tienMatThanhToan"
      :tongTien="tongThanhToan"
      @close="closeTienMatPopup"
      @xacNhan="taoHoaDon"
    />
  </div>
</template>

<style scoped>
/* =====================================================
   LAYOUT CHÍNH
===================================================== */
* {
  box-sizing: border-box;
}

.thanh-toan-container {
  display: flex;
  gap: 20px;

  width: 100%;
  height: 100%;

  padding: 20px;

  overflow: hidden;

  background: linear-gradient(135deg, #1b1b1b, #242424);
}

/* =====================================================
   CARD CHUNG
===================================================== */

.danh-muc,
.danh-sach-mon,
.gio-hang {
  background: linear-gradient(180deg, #2a2a2a, #1d1d1d);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(212, 175, 55, 0.25);

  box-shadow:
    0 8px 25px rgba(0, 0, 0, 0.4),
    0 0 12px rgba(212, 175, 55, 0.08);

  min-height: 0;

  overflow: hidden;
}

/* =====================================================
   CỘT TRÁI
===================================================== */

.danh-muc {
  width: 20%;

  display: flex;

  flex-direction: column;
}

.danh-muc > div:last-child {
  margin-top: auto;
}

.btn-quay-lai {
  width: 100%;

  padding: 14px;

  border: 1px solid rgba(255, 216, 107, 0.25);

  border-radius: 12px;

  background: linear-gradient(145deg, #303030, #252525);

  color: #ffd86b;

  font-size: 15px;

  font-weight: 600;

  cursor: pointer;

  transition: all 0.25s ease;
}

.btn-quay-lai:hover {
  background: linear-gradient(145deg, #3a3a3a, #2f2f2f);

  border-color: #ffd86b;

  transform: translateY(-2px);

  box-shadow: 0 0 12px rgba(255, 216, 107, 0.2);
}

/* =====================================================
   CỘT GIỮA
===================================================== */

.danh-sach-mon {
  width: 50%;

  min-height: 0;

  overflow-y: auto;
  overflow-x: hidden;
}

/* =====================================================
   CỘT PHẢI
===================================================== */

.gio-hang {
  width: 30%;

  display: flex;
  flex-direction: column;

  min-height: 0;

  overflow: hidden;
}

/* =====================================================
   TITLE
===================================================== */

.title {
  color: #ffd86b;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 18px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.25);
  padding-bottom: 10px;

  flex-shrink: 0;
}

/* =====================================================
   DANH MỤC
===================================================== */

.menu-item {
  padding: 14px;
  margin-bottom: 12px;
  border-radius: 10px;
  background: #333;
  color: #f5f5f5;
  cursor: pointer;
  transition: 0.25s;
}

.menu-item:hover {
  background: #3f3f3f;
  color: #ffd86b;
  transform: translateX(4px);
  border-left: 4px solid #ffd86b;
}

.menu-item.active {
  background: #3f3f3f;
  color: #ffd86b;
  border-left: 4px solid #ffd86b;
}

/* =====================================================
   GRID MÓN ĂN
===================================================== */

.food-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.food-card {
  height: 120px;
  background: linear-gradient(145deg, #363636, #292929);
  border-radius: 12px;
  color: #f5f5f5;

  display: flex;
  justify-content: center;
  align-items: center;

  text-align: center;

  cursor: pointer;

  transition: 0.3s;

  border: 1px solid transparent;
}

.food-card:hover {
  border-color: #ffd86b;
  color: #ffd86b;
  transform: translateY(-3px);
  box-shadow: 0 0 12px rgba(255, 216, 107, 0.2);
}

/* =====================================================
   DANH SÁCH GIỎ HÀNG CUỘN
===================================================== */

.gio-hang-list {
  flex: 1;

  min-height: 0;

  overflow-y: auto;
  overflow-x: hidden;

  padding-right: 4px;
}

/* =====================================================
   FOOTER GIỎ HÀNG
===================================================== */

.gio-hang-footer {
  flex-shrink: 0;

  margin-top: auto;

  padding-top: 12px;

  border-top: 1px solid rgba(255, 216, 107, 0.15);

  background: linear-gradient(180deg, #2a2a2a, #1d1d1d);
}

/* =====================================================
   ITEM GIỎ HÀNG
===================================================== */

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;

  padding: 14px;

  background: linear-gradient(145deg, #353535, #2b2b2b);

  color: white;

  border-radius: 12px;

  margin-bottom: 12px;

  border-left: 4px solid #ffd86b;

  transition: all 0.25s ease;
}

.cart-item:hover {
  transform: translateX(3px);
  box-shadow: 0 0 12px rgba(255, 216, 107, 0.15);
}

.item-info {
  flex: 1;
}

.item-info div:first-child {
  font-weight: 600;
  margin-bottom: 4px;
}

.item-info div:nth-child(2) {
  color: #ffd86b;
  margin-bottom: 4px;
}

.item-info div:last-child {
  font-size: 18px;
  opacity: 0.9;
}

/* =====================================================
   NÚT GIẢM
===================================================== */

.btn-minus {
  width: 34px;
  height: 34px;

  border: 1px solid rgba(255, 216, 107, 0.25);

  border-radius: 10px;

  background: #242424;

  color: #ffd86b;

  font-size: 18px;

  font-weight: 700;

  cursor: pointer;

  transition: all 0.25s ease;
}

.btn-minus:hover {
  background: #ffd86b;
  color: #111;

  transform: scale(1.08);

  box-shadow: 0 0 12px rgba(255, 216, 107, 0.25);
}

/* =====================================================
   HR
===================================================== */

hr {
  border: none;
  height: 1px;
  background: rgba(255, 216, 107, 0.2);
  margin: 0 0 18px 0;
}

/* =====================================================
   TỔNG TIỀN
===================================================== */

.tong-tien {
  background: linear-gradient(135deg, rgba(255, 216, 107, 0.12), rgba(212, 175, 55, 0.05));

  border: 1px solid rgba(255, 216, 107, 0.15);

  border-radius: 12px;

  padding: 14px;

  color: #ffd86b;

  font-size: 20px;

  font-weight: 700;

  text-align: center;

  margin-bottom: 16px;
}

/* =====================================================
   INPUT MÃ GIẢM GIÁ
===================================================== */

.discount-input {
  width: 100%;

  padding: 12px;

  border-radius: 10px;

  border: 1px solid rgba(212, 175, 55, 0.25);

  background: #2f2f2f;

  color: white;

  outline: none;

  margin-bottom: 12px;
}

.discount-input:focus {
  border-color: #ffd86b;
}

/* =====================================================
   NÚT THANH TOÁN
===================================================== */

.btn-thanh-toan {
  width: 100%;

  padding: 14px;

  border: none;

  border-radius: 10px;

  background: linear-gradient(135deg, #ffd86b, #d4af37);

  color: #111;

  font-size: 16px;

  font-weight: 700;

  cursor: pointer;

  transition: 0.3s;
}

.btn-thanh-toan:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 16px rgba(255, 216, 107, 0.35);
}

/* =====================================================
   SCROLLBAR
===================================================== */

.gio-hang-list::-webkit-scrollbar,
.danh-sach-mon::-webkit-scrollbar {
  width: 6px;
}

.gio-hang-list::-webkit-scrollbar-track,
.danh-sach-mon::-webkit-scrollbar-track {
  background: transparent;
}

.gio-hang-list::-webkit-scrollbar-thumb,
.danh-sach-mon::-webkit-scrollbar-thumb {
  background: rgba(255, 216, 107, 0.35);
  border-radius: 20px;
}

.gio-hang-list::-webkit-scrollbar-thumb:hover,
.danh-sach-mon::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 216, 107, 0.7);
}
.btn-luu {
  width: 100%;
  padding: 14px;
  margin-bottom: 12px; /* Tạo khoảng cách với nút phía trên */

  border: 1px solid rgba(255, 216, 107, 0.25);
  border-radius: 12px;

  background: linear-gradient(145deg, #303030, #252525);
  color: #ffd86b;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-luu:hover {
  background: linear-gradient(145deg, #3a3a3a, #2f2f2f);
  border-color: #ffd86b;
  transform: translateY(-2px);
  box-shadow: 0 0 12px rgba(255, 216, 107, 0.2);
}
.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 6px;
}

/* tên món */
.item-name {
  font-weight: 600;
  margin-bottom: 6px;
}

/* số lượng to hơn, nhưng không phá layout */
.item-qty {
  font-size: 18px;
  font-weight: 800;
  color: #ffd86b;
}
</style>
