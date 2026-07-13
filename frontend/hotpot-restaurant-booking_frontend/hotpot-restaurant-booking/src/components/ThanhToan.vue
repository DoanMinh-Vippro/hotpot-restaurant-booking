<script setup lang="ts">
import MonApi, { type Mon } from '@/api/MonApi'
import ComBoApi, { type Combo } from '@/api/ComBoApi'
import { onMounted, ref, computed, watch, nextTick } from 'vue'
import GiamGiaApi from '@/api/GiamGiaApi'
import PopupThanhToan from './PopupThanhToan.vue'
import PopupTienMat from './PopupTienMat.vue'
import HoaDonApi from '@/api/HoaDonApi.ts'
import HoaDonChiTietApi from '@/api/HoaDonChiTietApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import BanApi from '@/api/BanApi'
import printJS from 'print-js'

// ================= PROPS =================
const props = defineProps<{
  ban: any
  datBan: any | null
}>()

// ================= EMIT =================
const emit = defineEmits(['quayLai'])
const quayLai = () => {
  emit('quayLai')
}

// ================= STATE =================
const danhSachCombo = ref<any[]>([])
const danhSachMonAn = ref<any[]>([])
const danhMucDangChon = ref('combo')

const tabGioHang = ref('goi-mon')
const gioHang = ref<any[]>([])
const danhSachMonPhucVu = ref<any[]>([])
const monVuaGuiBep = ref<any[]>([])

// Tự động lấy tên nhân viên dựa theo tài khoản đăng nhập
const tenNhanVien = ref<string>(
  localStorage.getItem('username') ||
    JSON.parse(localStorage.getItem('user') || '{}').hoTen ||
    'Nhân viên',
)

const danhSachGiamGia = ref<any[]>([])
const giamGiaDangChon = ref<number | null>(null)

const phuongThucThanhToan = ref(false)
const tienMatThanhToan = ref(false)
const hoaDonHienTai = ref<any>(null)

// ================= UTILS =================
const itemName = (item: any) => item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'

// ================= LOAD DATA =================
const loadData = async () => {
  const combo = await ComBoApi.hienThiComBo()
  const mon = await MonApi.hienThiMon()
  danhSachCombo.value = (combo.data || []).filter((cb: Combo) => cb.trangThai === 1)
  danhSachMonAn.value = (mon.data || []).filter((m: Mon) => m.trangThai === 0)
}

const loadGiamGia = async () => {
  const gg = await GiamGiaApi.getDanhSach()
  danhSachGiamGia.value = gg.data
}

// ================= GIỎ HÀNG =================
const themVaoGio = (item: any, loai: string) => {
  if (item.trangThaiBan === 0) {
    alert(`${loai === 'MON' ? 'Món' : 'Combo'} "${item.tenMon || item.tenCombo}" này đã hết hàng!`)
    return
  }
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
      comboItems: item.comboItems ?? [],
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

// ================= XỬ LÝ LÊN MÓN =================
const xacNhanTungMon = (item: any) => {
  if (item.daLen < item.soLuong) item.daLen++
}
const xacNhanTatCaMon = () => {
  danhSachMonPhucVu.value.forEach((item) => {
    item.daLen = item.soLuong
  })
}

// ================= POPUP =================
const optionPay = async () => {
  if (danhSachMonPhucVu.value.filter((i) => i.daLen > 0).length === 0) {
    alert('Chưa có món nào được xác nhận đã lên bàn để thanh toán!')
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
const tongTien = computed(() =>
  gioHang.value.reduce((tong, item) => tong + item.gia * item.soLuong, 0),
)
const tongTienMonDangGoi = computed(() =>
  danhSachMonPhucVu.value
    .filter((item) => item.daLen < item.soLuong)
    .reduce((tong, item) => tong + item.gia * (item.soLuong - item.daLen), 0),
)
const tongTienMonDaGoi = computed(() =>
  danhSachMonPhucVu.value
    .filter((item) => item.daLen > 0)
    .reduce((tong, item) => tong + item.gia * item.daLen, 0),
)
const tongTienTamTinhCotGiua = computed(() => tongTienMonDaGoi.value)

const tienGiamGia = computed(() => {
  const base = tongTienTamTinhCotGiua.value
  if (!giamGiaDangChon.value) return 0
  const giamGia = danhSachGiamGia.value.find((g) => g.idGiamGia === giamGiaDangChon.value)
  if (!giamGia) return 0
  if (giamGia.loaiGiam === 'TIENMAT') return Math.min(giamGia.giaTriGiam, base)
  if (giamGia.loaiGiam === 'PHANTRAM') return (base * giamGia.giaTriGiam) / 100
  return 0
})
const tongThanhToan = computed(() => Math.max(0, tongTienTamTinhCotGiua.value - tienGiamGia.value))

// ================= HÓA ĐƠN API =================
const checkHoaDonTam = async () => {
  try {
    const res = await HoaDonApi.findByBanAndStatus(props.ban.idBan, 0)
    const hd = res.data
    if (!hd) return
    hoaDonHienTai.value = hd
    danhSachMonPhucVu.value = (hd.chiTiet || []).map((item: any) => ({
      idMon: item.idMon,
      idCombo: item.idCombo,
      tenMon: item.tenMon,
      tenCombo: item.tenCombo,
      gia: item.donGiaHienTai ?? item.giaCombo ?? item.giaBanTaiThoiDiem ?? 0,
      soLuong: item.soLuong,
      daLen: item.trangThaiMonAn === 'DA_LEN' ? item.soLuong : 0,
      loai: item.idMon ? 'MON' : 'COMBO',
      comboItems: item.comboItems ?? [],
    }))
    giamGiaDangChon.value = hd.idGiamGia ?? null
  } catch {
    console.log('Không có hóa đơn tạm')
  }
}

const addHoaDon = async (payload: any) => {
  const res = await HoaDonApi.create(payload)
  const idHoaDon = res.data.idHoaDon
  for (const item of danhSachMonPhucVu.value) {
    const gia = item.gia ?? 0
    await HoaDonChiTietApi.add({
      maHoaDonChiTiet: `HDCT${Date.now()}${item.idMon || item.idCombo}`,
      idHoaDon,
      idMon: item.idMon,
      idCombo: item.idCombo,
      soLuong: item.soLuong,
      giaBanTaiThoiDiem: gia,
      tienGiamGiaMon: 0,
      thanhTien: gia * item.soLuong,
      trangThaiMonAn: item.daLen === item.soLuong ? 'DA_LEN' : 'DANG_LEN',
    } as any)
  }
  hoaDonHienTai.value = res.data
}

const updateHoaDon = async (idHoaDon: number, payload: any) => {
  await HoaDonApi.update(idHoaDon, payload)
  await HoaDonChiTietApi.deleteByHoaDon(idHoaDon)
  for (const item of danhSachMonPhucVu.value) {
    const gia = item.gia ?? 0
    await HoaDonChiTietApi.add({
      maHoaDonChiTiet: `HDCT${Date.now()}${item.idMon || item.idCombo}`,
      idHoaDon,
      idMon: item.idMon,
      idCombo: item.idCombo,
      soLuong: item.soLuong,
      giaBanTaiThoiDiem: gia,
      tienGiamGiaMon: 0,
      thanhTien: gia * item.soLuong,
      trangThaiMonAn: item.daLen === item.soLuong ? 'DA_LEN' : 'DANG_LEN',
    } as any)
  }
}

const xuLyHoaDon = async (trangThaiHoaDon: number, trangThaiThanhToan: number) => {
  const payload = {
    maHoaDon: hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`,
    trangThaiHoaDon,
    trangThaiThanhToan,
    phuongThucThanhToan: trangThaiThanhToan === 1 ? 1 : null,
    tienTruocGiam: tongTienTamTinhCotGiua.value,
    tienGiamGia: tienGiamGia.value,
    tongTien: tongThanhToan.value,
    thoiGianXuat: new Date().toISOString(),
    idBan: props.ban.idBan,
    idGiamGia: giamGiaDangChon.value,
    idDatBan: props.datBan?.idDatBan ?? null,
    idKhachHang: props.datBan?.idKhachHang ?? null,
    sdtKhachHang: props.datBan?.sdtKhachHang ?? null,
    tienCoc: props.datBan?.soTienCoc ?? null,
    tenNhanVien: tenNhanVien.value,
  }
  if (hoaDonHienTai.value) {
    await updateHoaDon(hoaDonHienTai.value.idHoaDon, payload)
    await checkHoaDonTam()
  } else {
    await addHoaDon(payload)
  }
}

const normalizeReservationStatus = (value: any) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object' && 'name' in value) return String(value.name)
  return String(value)
}

const syncReservationToSeated = async () => {
  if (!props.datBan?.idDatBan) return
  const currentStatus = normalizeReservationStatus(props.datBan?.trangThai)
  if (currentStatus !== 'DA_XAC_NHAN' && currentStatus !== 'DA_NHAN_BAN') return
  try {
    if (props.ban?.idBan) {
      await BanApi.update(props.ban.idBan, {
        trangThai: currentStatus === 'DA_NHAN_BAN' ? 'DANG_SU_DUNG' : 'DA_DAT',
      })
    }
  } catch (error) {
    console.error(error)
  }
}

const markReservationCompleted = async () => {
  if (!props.datBan?.idDatBan) return
  try {
    await DatBanQuanLyApi.update(props.datBan.idDatBan, {
      ...props.datBan,
      trangThai: 'HOAN_THANH',
    })
    if (props.ban?.idBan) {
      await BanApi.update(props.ban.idBan, { trangThai: 'TRONG' })
    }
  } catch (error) {
    console.error(error)
  }
}

// ================= ACTION XÁC NHẬN GỬI BẾP & IN K80 =================
const luuTam = async () => {
  if (gioHang.value.length === 0) {
    alert('Vui lòng chọn món ăn trước khi nhấn gửi vào bếp!')
    return
  }
  try {
    monVuaGuiBep.value = [...gioHang.value]

    gioHang.value.forEach((cartItem) => {
      const trungMon = danhSachMonPhucVu.value.find(
        (p) =>
          p.loai === cartItem.loai &&
          (cartItem.loai === 'MON' ? p.idMon === cartItem.idMon : p.idCombo === cartItem.idCombo),
      )
      if (trungMon) {
        trungMon.soLuong += cartItem.soLuong
      } else {
        danhSachMonPhucVu.value.push({ ...cartItem, daLen: 0 })
      }
    })

    gioHang.value = []
    await xuLyHoaDon(0, 0)

    await nextTick()

    // GỌI THƯ VIỆN IN CHUẨN ĐỊNH DẠNG HOÁ ĐƠN NHIỆT K80 CĂN GIỮA
    printJS({
      printable: 'vung-phieu-in-bep',
      type: 'html',
      header: undefined, // Fix lỗi TypeScript Configuration overload
      targetStyles: ['*'],
      style: `
        @page {
          size: 80mm auto;
          margin: 0;
        }
        body {
          margin: 0;
          padding: 0;
        }
        #vung-phieu-in-bep { 
          font-family: 'Courier New', Courier, monospace; 
          color: #000000; 
          width: 72mm;        /* Độ rộng hiển thị ruột hóa đơn */
          margin: 0 auto;     /* Thần chú căn giữa trang giấy */
          padding: 6mm 0;     /* Khoảng cách đệm trên dưới đầu trang */
          box-sizing: border-box;
        }
        .phieu-header { text-align: center; }
        .phieu-header h2 { margin: 0 0 10px 0; font-size: 19px; font-weight: bold; letter-spacing: 0.5px; }
        .phieu-header p { margin: 5px 0; font-size: 13px; text-align: left; }
        
        .dash-line { 
          border: none; 
          border-top: 1px dashed #000000 !important; 
          margin: 12px 0; 
          height: 0;
          width: 100%;
        }
        
        .phieu-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        .phieu-table th { 
          border-bottom: 2px solid #000000 !important; 
          font-size: 14px; 
          font-weight: bold;
          padding-bottom: 6px; 
        }
        .phieu-table td { padding: 8px 0; font-size: 15px; vertical-align: top; }
        
        .print-item-name { font-weight: bold; }
        .print-combo-sub { 
          font-size: 12px; 
          font-style: italic; 
          padding-left: 10px; 
          margin-top: 2px;
        }
        .phieu-footer { text-align: center; margin-top: 18px; font-size: 13px; font-style: italic; }
      `,
    })

    tabGioHang.value = 'mon-dang-len'
  } catch {
    alert('Gửi bếp thất bại')
  }
}

const taoHoaDon = async () => {
  try {
    await xuLyHoaDon(1, 1)
    await markReservationCompleted()
    alert('Thanh toán thành công!')
    hoaDonHienTai.value = null
    gioHang.value = []
    danhSachMonPhucVu.value = []
    giamGiaDangChon.value = null
    tienMatThanhToan.value = false
  } catch {
    alert('Thanh toán thất bại')
  }
}

watch(
  () => props.datBan,
  async (db) => {
    if (!db) return
    gioHang.value = []
    if (db.idCombo) {
      gioHang.value.push({
        idCombo: db.idCombo,
        tenCombo: db.tenCombo,
        gia: db.giaCombo ?? 0,
        soLuong: 1,
        loai: 'COMBO',
        comboItems: db.comboItems ?? [],
      })
    }
    await syncReservationToSeated()
  },
  { immediate: true },
)

onMounted(() => {
  loadData()
  loadGiamGia()
  checkHoaDonTam()
})
</script>

<template>
  <div class="thanh-toan-container">
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
      <div><button class="btn-quay-lai" @click="quayLai">Quay Lại</button></div>
    </div>

    <div class="danh-sach-mon">
      <div class="title">
        {{ danhMucDangChon === 'combo' ? 'Danh sách Combo' : 'Danh sách Món ăn' }}
      </div>
      <div class="food-grid">
        <template v-if="danhMucDangChon === 'combo'">
          <div
            v-for="combo in danhSachCombo"
            :key="combo.idCombo"
            class="food-card"
            :class="combo.trangThaiBan === 1 ? 'con-hang' : 'het-hang'"
            @click="themVaoGio(combo, 'COMBO')"
          >
            {{ combo.tenCombo }}
          </div>
        </template>
        <template v-else>
          <div
            v-for="mon in danhSachMonAn"
            :key="mon.idMon"
            class="food-card"
            :class="mon.trangThaiBan === 1 ? 'con-hang' : 'het-hang'"
            @click="themVaoGio(mon, 'MON')"
          >
            {{ mon.tenMon }}
          </div>
        </template>
      </div>

      <div class="gio-hang-footer">
        <hr />
        <div class="tong-tien">
          Tổng tiền tạm tính : {{ tongTienTamTinhCotGiua.toLocaleString('vi-VN') }} đ
        </div>
        <div class="tong-tien">Tiền giảm giá: {{ tienGiamGia.toLocaleString('vi-VN') }} đ</div>
        <div class="tong-tien main-total-center">
          Tổng tiền thanh toán: {{ tongThanhToan.toLocaleString('vi-VN') }} đ
        </div>
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

    <div class="gio-hang">
      <div class="title">Giỏ hàng: {{ props.ban.tenBan }}</div>
      <div v-if="props.datBan" class="reservation-status-pill">
        {{
          normalizeReservationStatus(props.datBan?.trangThai) === 'DA_XAC_NHAN'
            ? 'Đã cọc'
            : 'Đã nhận bàn'
        }}
      </div>

      <div class="gio-hang-tabs">
        <div
          class="tab-item"
          :class="{ active: tabGioHang === 'goi-mon' }"
          @click="tabGioHang = 'goi-mon'"
        >
          Gọi món
        </div>
        <div
          class="tab-item"
          :class="{ active: tabGioHang === 'mon-dang-len' }"
          @click="tabGioHang = 'mon-dang-len'"
        >
          Món đang lên
        </div>
        <div
          class="tab-item"
          :class="{ active: tabGioHang === 'mon-da-goi' }"
          @click="tabGioHang = 'mon-da-goi'"
        >
          Món đã gọi
        </div>
      </div>

      <div class="gio-hang-tab-content">
        <div v-if="tabGioHang === 'goi-mon'" class="gio-hang-list">
          <div v-if="gioHang.length === 0" class="empty-cart">Chưa chọn món ăn nào.</div>
          <div
            v-for="item in gioHang"
            :key="`cart-${item.loai}-${item.idMon ?? item.idCombo}`"
            class="cart-item"
          >
            <button class="btn-minus" @click="giamSoLuong(item)">-</button>
            <div class="item-info">
              <div class="item-name">{{ itemName(item) }}</div>
              <div v-if="item.comboItems?.length" class="mon-combo">
                Gồm: {{ item.comboItems.join(', ') }}
              </div>
              <div class="item-bottom">
                <div class="item-qty">x{{ item.soLuong }}</div>
                <div class="item-price">
                  <b>{{ ((item.gia ?? 0) * item.soLuong).toLocaleString('vi-VN') }} đ</b>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="tabGioHang === 'mon-dang-len'" class="gio-hang-list">
          <div
            class="tab-action-header"
            v-if="danhSachMonPhucVu.some((i) => i.soLuong - i.daLen > 0)"
          >
            <button class="btn-xac-nhan-all" @click="xacNhanTatCaMon">
              ✓ Xác nhận tất cả lên đồ
            </button>
          </div>
          <div v-if="!danhSachMonPhucVu.some((i) => i.soLuong - i.daLen > 0)" class="empty-cart">
            Không có món đang chờ.
          </div>
          <div
            v-for="item in danhSachMonPhucVu.filter((i) => i.soLuong - i.daLen > 0)"
            :key="`pending-${item.loai}-${item.idMon ?? item.idCombo}`"
            class="cart-item pending-item"
          >
            <div class="item-info">
              <div class="item-name">{{ itemName(item) }}</div>
              <div v-if="item.comboItems?.length" class="mon-combo">
                Gồm: {{ item.comboItems.join(', ') }}
              </div>
              <div class="item-bottom">
                <div class="item-qty">
                  Còn: {{ item.soLuong - item.daLen }} / {{ item.soLuong }}
                </div>
                <div class="item-price">
                  Giá: {{ ((item.soLuong - item.daLen) * item.gia).toLocaleString('vi-VN') }} đ
                </div>
              </div>
            </div>
            <button class="btn-check-item" @click="xacNhanTungMon(item)">✓ Lên</button>
          </div>
        </div>

        <div v-if="tabGioHang === 'mon-da-goi'" class="gio-hang-list">
          <div v-if="!danhSachMonPhucVu.some((i) => i.daLen > 0)" class="empty-cart">
            Chưa có món nào được lên.
          </div>
          <div
            v-for="item in danhSachMonPhucVu.filter((i) => i.daLen > 0)"
            :key="`done-${item.loai}-${item.idMon ?? item.idCombo}`"
            class="cart-item done-item"
          >
            <div class="item-info">
              <div class="item-name">🎉 {{ itemName(item) }}</div>
              <div v-if="item.comboItems?.length" class="mon-combo">
                Gồm: {{ item.comboItems.join(', ') }}
              </div>
              <div class="item-bottom">
                <div class="item-qty">Đã phục vụ: x{{ item.daLen }}</div>
                <div class="item-price">
                  Thành tiền: {{ (item.daLen * item.gia).toLocaleString('vi-VN') }} đ
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-sidebar-footer">
        <hr />
        <button v-if="tabGioHang === 'goi-mon'" class="btn-luu-phu" @click="luuTam()">
          🔥 Xác nhận gửi vào bếp
        </button>
        <div class="tong-tien-dong">
          <span>Tiền món đang gọi:</span>
          <b>{{ tongTienMonDangGoi.toLocaleString('vi-VN') }} đ</b>
        </div>
        <div class="tong-tien-dong main-total-right">
          <span>Tiền món đã lên bàn:</span>
          <b>{{ tongTienMonDaGoi.toLocaleString('vi-VN') }} đ</b>
        </div>
      </div>
    </div>

    <div style="display: none">
      <div id="vung-phieu-in-bep">
        <div class="phieu-header">
          <h2>PHIẾU GỌI MÓN (BẾP)</h2>
          <p>Mã HD: {{ hoaDonHienTai?.maHoaDon || 'Mới' }}</p>
          <p>Bàn: {{ props.ban?.tenBan || 'N/A' }}</p>
          <p>Nhân viên phục vụ: {{ tenNhanVien }}</p>
          <p>
            Giờ đặt: {{ new Date().toLocaleTimeString('vi-VN') }}
            {{ new Date().toLocaleDateString('vi-VN') }}
          </p>
        </div>

        <div class="dash-line"></div>

        <table class="phieu-table">
          <thead>
            <tr>
              <th align="left">Tên món / Combo</th>
              <th align="right" style="width: 50px">SL</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, idx) in monVuaGuiBep" :key="`print-${idx}`">
              <td>
                <span class="print-item-name">{{ itemName(item) }}</span>
                <div v-if="item.comboItems?.length" class="print-combo-sub">
                  ↳ Gồm: {{ item.comboItems.join(', ') }}
                </div>
              </td>
              <td align="right">
                <b>x{{ item.soLuong }}</b>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="dash-line"></div>

        <div class="phieu-footer">Vui lòng chế biến món theo thứ tự!</div>
      </div>
    </div>

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
  background: rgba(255, 248, 234, 0.96);
}
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
.menu-item {
  padding: 14px;
  margin-bottom: 12px;
  border-radius: 10px;
  background: #333;
  color: #f5f5f5;
  cursor: pointer;
  transition: 0.25s;
}
.menu-item:hover,
.menu-item.active {
  background: #3f3f3f;
  color: #ffd86b;
  border-left: 4px solid #ffd86b;
  transform: translateX(4px);
}
.danh-sach-mon {
  width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.food-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 6px;
  padding-bottom: 15px;
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
  padding: 8px;
}
.food-card.con-hang {
  background: linear-gradient(145deg, #2e7d32, #1b5e20);
  box-shadow: 0 4px 10px rgba(46, 125, 50, 0.3);
}
.food-card.con-hang:hover {
  background: linear-gradient(145deg, #388e3c, #2e7d32);
  border-color: #ffd86b;
  color: #ffd86b;
  transform: translateY(-3px);
}
.food-card.het-hang {
  background: linear-gradient(145deg, #fbc02d, #f9a825);
  color: #333;
  font-weight: 600;
  cursor: not-allowed;
}
.gio-hang-footer {
  flex-shrink: 0;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 216, 107, 0.15);
}
.main-total-center {
  background: linear-gradient(135deg, #ffd86b, #d4af37) !important;
  color: #111 !important;
  font-size: 20px !important;
  font-weight: 800 !important;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.25);
}
.gio-hang {
  width: 30%;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.gio-hang-tabs {
  display: flex;
  background: #242424;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 12px;
  border: 1px solid rgba(212, 175, 55, 0.15);
  flex-shrink: 0;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 4px;
  font-size: 13px;
  font-weight: 600;
  color: #b5b5b5;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
}
.tab-item.active {
  background: linear-gradient(135deg, #ffd86b, #d4af37);
  color: #111;
  font-weight: 700;
}
.gio-hang-tab-content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.gio-hang-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}
.right-sidebar-footer {
  flex-shrink: 0;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 216, 107, 0.1);
}
.tong-tien {
  background: linear-gradient(135deg, rgba(255, 216, 107, 0.12), rgba(212, 175, 55, 0.05));
  border: 1px solid rgba(255, 216, 107, 0.15);
  border-radius: 12px;
  padding: 12px;
  color: #ffd86b;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 10px;
}
.tong-tien-dong {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #262626;
  border-radius: 8px;
  color: #b5b5b5;
  font-size: 13px;
  margin-bottom: 8px;
  border: 1px solid rgba(255, 216, 107, 0.05);
}
.tong-tien-dong b {
  color: #ffd86b;
}
.main-total-right b {
  color: #fff;
  font-size: 15px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(145deg, #353535, #2b2b2b);
  color: white;
  border-radius: 12px;
  margin-bottom: 10px;
  border-left: 4px solid #ffd86b;
}
.pending-item {
  border-left-color: #f57c00;
}
.done-item {
  border-left-color: #2e7d32;
  background: linear-gradient(145deg, #242b24, #1c221c);
}
.item-info {
  flex: 1;
}
.item-name {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 14px;
}
.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.item-qty {
  font-size: 14px;
  font-weight: 700;
  color: #ffd86b;
}
.item-price {
  font-size: 13px;
  opacity: 0.9;
}
.mon-combo {
  font-size: 12px;
  color: #ffd86b;
  opacity: 0.8;
  margin-bottom: 6px;
  font-style: italic;
}
.btn-minus {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 216, 107, 0.25);
  border-radius: 8px;
  background: #242424;
  color: #ffd86b;
  font-size: 16px;
  cursor: pointer;
}
.btn-minus:hover {
  background: #ffd86b;
  color: #111;
}
.btn-check-item {
  padding: 6px 12px;
  background: #ffd86b;
  border: none;
  color: #111;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.tab-action-header {
  margin-bottom: 10px;
}
.btn-xac-nhan-all {
  width: 100%;
  padding: 10px;
  background: #1b5e20;
  border: 1px solid #2e7d32;
  color: #fff;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
}
.btn-luu-phu {
  width: 100%;
  padding: 12px;
  background: #e65100;
  border: none;
  color: white;
  font-weight: 700;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
}
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
}
.btn-thanh-toan:hover {
  box-shadow: 0 0 16px rgba(255, 216, 107, 0.35);
}
.discount-input {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid rgba(212, 175, 55, 0.25);
  background: #2f2f2f;
  color: white;
  margin-bottom: 10px;
}
.title {
  color: #ffd86b;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 15px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.25);
  padding-bottom: 8px;
  flex-shrink: 0;
}
.empty-cart {
  color: #666;
  text-align: center;
  margin-top: 30px;
  font-style: italic;
  font-size: 13px;
}
.gio-hang-list::-webkit-scrollbar,
.food-grid::-webkit-scrollbar {
  width: 6px;
}
.gio-hang-list::-webkit-scrollbar-thumb,
.food-grid::-webkit-scrollbar-thumb {
  background: rgba(255, 216, 107, 0.3);
  border-radius: 10px;
}
</style>
