<script setup lang="ts">
import MonApi, { type Mon } from '@/api/MonApi'
import ComBoApi, { type Combo } from '@/api/ComBoApi'
import { onMounted, ref, computed, watch, nextTick } from 'vue'
import GiamGiaApi from '@/api/GiamGiaApi'
import PopupThanhToan from './PopupThanhToan.vue'
import PopupTienMat from './PopupTienMat.vue'
import PopupSePayQR from './PopupSePayQR.vue'
import HoaDonApi from '@/api/HoaDonApi.ts'
import HoaDonChiTietApi from '@/api/HoaDonChiTietApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import BanApi from '@/api/BanApi'
import { useShiftStore } from '@/stores/ShiftStore'
import { useAuthStore } from '@/stores/AuthStore'
import printJS from 'print-js'
import DanhMucApi from '@/api/DanhMucApi.ts'
import MayInApi from '@/api/MayInApi'

// ================= PROPS =================
const props = defineProps<{
  ban: any
  datBan: any | null
}>()

// ================= EMIT =================
const emit = defineEmits(['quayLai', 'payment-complete'])
const shiftStore = useShiftStore()
const authStore = useAuthStore()

const SHIFT_CLOSED_MESSAGE = 'Ca làm việc đã đóng, không thể thực hiện thao tác gọi món mới'

const isShiftClosed = computed(() => !shiftStore.currentShift || shiftStore.currentShift.isOpen === false)

const hasActiveUnpaidInvoice = computed(() => {
  const invoice = hoaDonHienTai.value
  if (!invoice) return false

  return (
    Number(invoice.trangThaiThanhToan) === 0 &&
    Number(invoice.trangThaiHoaDon) === 0
  )
})

const isShiftClosedForUi = computed(() => isShiftClosed.value && !hasActiveUnpaidInvoice.value)

const blockShiftClosedAction = () => {
  if (isShiftClosedForUi.value) {
    alert(SHIFT_CLOSED_MESSAGE)
    return true
  }
  return false
}

const quayLai = () => {
  emit('quayLai')
}

const notifyPaymentComplete = () => {
  if (props.ban?.idBan) {
    emit('payment-complete', { idBan: props.ban.idBan, trangThai: 'TRONG' })
  }
}

const hoanTatThanhToan = async () => {
  try {
    await markReservationCompleted()
    emit('quayLai')
  } catch (error) {
    console.error('Không thể cập nhật trạng thái bàn sau thanh toán:', error)
  }
}

// ================= STATE =================
const isDataLoaded = ref(false)
const danhSachCombo = ref<any[]>([])
const danhSachMonAn = ref<any[]>([])
const danhMucDangChon = ref('combo')

const tabGioHang = ref('goi-mon')
const gioHang = ref<any[]>([])
const danhSachMonPhucVu = ref<any[]>([])
const monVuaGuiBep = ref<any[]>([])
const monTheoQuayMap = ref<Record<string, any[]>>({})

const getCurrentOperatorName = () =>
  authStore.accountName ||
  localStorage.getItem('tenDangNhap') ||
  authStore.customerInfo?.tenKhachHang ||
  authStore.tenKhachHang ||
  'Nhân viên'

const tenNhanVien = ref<string>(getCurrentOperatorName())

const getLocalDateTimeNow = () => {
  const now = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const danhSachGiamGia = ref<any[]>([])
const giamGiaDangChon = ref<number | null>(null)

const normalizeDiscountType = (value: any) => {
  if (value == null) return ''

  const normalized = String(value)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+/g, '')
    .replace(/%/g, 'PERCENT')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toUpperCase()

  if (normalized.includes('PERCENT') || normalized.includes('PHANTRAM')) return 'PHANTRAM'
  if (normalized.includes('TIEN') || normalized.includes('MAT') || normalized.includes('GIATRI') || normalized.includes('VALUE') || normalized.includes('VND') || normalized.includes('FIXED')) return 'TIEN'
  return normalized
}

const getDiscountCode = (discount: any) =>
  discount?.maGiamGia ?? discount?.ma ?? discount?.code ?? 'Mã giảm giá'

const getDiscountRawValue = (discount: any) =>
  discount?.giaTriGiam ??
  discount?.giaTri ??
  discount?.giatri ??
  discount?.discountValue ??
  discount?.discount_value ??
  discount?.value ??
  0

const getDiscountRawMax = (discount: any) =>
  discount?.giaTriGiamToiDa ??
  discount?.maxDiscount ??
  discount?.max_discount ??
  discount?.maximumDiscount ??
  0

const getDiscountPayload = (discount: any) => {
  const rawDiscountType =
    discount?.loaiGiam ??
    discount?.discountType ??
    discount?.discount_type ??
    discount?.type ??
    ''

  return {
    type: normalizeDiscountType(rawDiscountType),
    value: Number(getDiscountRawValue(discount) ?? 0),
    maxDiscount: Number(getDiscountRawMax(discount) ?? 0),
  }
}

const isPercentDiscountType = (type: string) =>
  type.includes('PHANTRAM') ||
  type.includes('PERCENT') ||
  type.includes('PERCENTAGE')

const isFixedDiscountType = (type: string) =>
  type.includes('TIEN') ||
  type.includes('MAT') ||
  type.includes('GIATRI') ||
  type.includes('VALUE') ||
  type.includes('VND') ||
  type.includes('FIXED')

const getDiscountLabel = (discount: any) => {
  const { type, value, maxDiscount } = getDiscountPayload(discount)
  const code = getDiscountCode(discount)
  const safeValue = Number.isFinite(value) ? value : 0
  const safeMax = Number.isFinite(maxDiscount) ? maxDiscount : 0

  if (isPercentDiscountType(type)) {
    const percentLabel = `${safeValue.toLocaleString('vi-VN')}%`
    const maxLabel = safeMax > 0 ? ` (tối đa ${safeMax.toLocaleString('vi-VN')}đ)` : ''
    return `${code} - ${percentLabel}${maxLabel}`
  }

  return `${code} - ${safeValue.toLocaleString('vi-VN')}đ`
}

const getDiscountDisplayText = (discount: any) => {
  const { type, value } = getDiscountPayload(discount)
  const code = getDiscountCode(discount)
  const safeValue = Number.isFinite(value) ? value : 0

  if (isPercentDiscountType(type)) {
    return `${code}: ${safeValue}%`
  }

  return `${code}: ${safeValue.toLocaleString('vi-VN')} đ`
}

const phuongThucThanhToan = ref(false)
const tienMatThanhToan = ref(false)
const hienThiSePayQR = ref(false)
const hoaDonHienTai = ref<any>(null)

// ================= UTILS =================
const itemName = (item: any) => item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'

const layTenQuay = (item: any): string => {
  const quay = item.quay || item.danhMuc?.quay || ''
  if (String(quay).toUpperCase() === 'BAR') {
    return 'Quầy Bar'
  }
  return 'Quầy Bếp'
}

// ================= LOAD DATA =================
const loadData = async () => {
  try {
    const [comboRes, monRes, danhMucRes] = await Promise.all([
      ComBoApi.hienThiComBo(),
      MonApi.hienThiMon(),
      DanhMucApi.getDanhSach()
    ])

    const dsDanhMuc = danhMucRes.data || []
    const dsMonRaw = monRes.data || []

    danhSachMonAn.value = dsMonRaw
      .filter((m: Mon) => m.trangThai === 0)
      .map((m: any) => {
        const dm = dsDanhMuc.find((d: any) => d.idDanhMuc === m.idDanhMuc)
        return {
          ...m,
          quay: m.quay || m.danhMuc?.quay || dm?.quay || 'BEP'
        }
      })

    danhSachCombo.value = (comboRes.data || []).filter((cb: Combo) => cb.trangThai === 1)
  } catch (error) {
    console.error('Lỗi load dữ liệu:', error)
  }
}

const loadGiamGia = async () => {
  const gg = await GiamGiaApi.getDanhSach()
  danhSachGiamGia.value = gg.data
}

// ================= GIỎ HÀNG =================
const themVaoGio = (item: any, loai: string) => {
  if (blockShiftClosedAction()) return

  if (item.trangThaiBan === 0) {
    alert(`${loai === 'MON' ? 'Món' : 'Combo'} "${item.tenMon || item.tenCombo}" này đã hết hàng!`)
    return
  }

  const quayCheBien = loai === 'COMBO' ? 'Quầy Bếp' : layTenQuay(item)

  const tonTai = gioHang.value.find(
    (x) =>
      x.loai === loai && (loai === 'MON' ? x.idMon === item.idMon : x.idCombo === item.idCombo),
  )
  if (tonTai) {
    tonTai.soLuong++
    tonTai.orderedBy = getCurrentOperatorName()
    tonTai.orderedAt = getLocalDateTimeNow()
  } else {
    gioHang.value.push({
      idMon: item.idMon ?? null,
      idCombo: item.idCombo ?? null,
      tenMon: item.tenMon ?? null,
      tenCombo: item.tenCombo ?? null,
      tenQuay: quayCheBien,
      gia: item.giaSauGiam ?? (loai === 'MON' ? item.gia : item.giaCombo) ?? 0,
      soLuong: 1,
      loai,
      comboItems: item.comboItems ?? [],
      orderedBy: getCurrentOperatorName(),
      orderedAt: getLocalDateTimeNow(),
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

// ================= XỬ LÝ LÊN MÓN (ĐÃ FIX KHÔNG BỊ TRÁO/GỌI LẠI MÓN ĐÃ LÊN) =================
const xacNhanTungMon = async (item: any) => {
  if (item.daLen < item.soLuong) {
    item.daLen += 1
    await capNhatDatabaseNoRebuild()
  }
}

const xacNhanTatCaMon = async () => {
  if (blockShiftClosedAction()) return

  danhSachMonPhucVu.value.forEach((item) => {
    item.daLen = item.soLuong
  })
  await capNhatDatabaseNoRebuild()
}

// ================= POPUP =================
const optionPay = async () => {
  if (blockShiftClosedAction()) return

  if (danhSachMonPhucVu.value.length === 0) {
    alert('Chưa có món nào được gửi vào bếp để thanh toán!')
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

const moPopupQR = async () => {
  if (blockShiftClosedAction()) return

  await xuLyHoaDon(0, 0)
  phuongThucThanhToan.value = false
  hienThiSePayQR.value = true
}

const handleChuyenKhoanThanhCong = async () => {
  if (blockShiftClosedAction()) return

  try {
    hienThiSePayQR.value = false
    await xuLyHoaDon(1, 1, 2)
    await markReservationCompleted()
    notifyPaymentComplete()
    emit('quayLai')
    alert(`Bàn ${props.ban?.tenBan} đã thanh toán chuyển khoản thành công tự động!`)

    hoaDonHienTai.value = null
    gioHang.value = []
    danhSachMonPhucVu.value = []
    giamGiaDangChon.value = null
  } catch (error) {
    alert('Có lỗi xảy ra khi cập nhật trạng thái hóa đơn!')
  }
}

// ================= COMPUTED CHI PHÍ =================
const tongTienTamTinhCotGiua = computed(() =>
  danhSachMonPhucVu.value.reduce((tong, item) => tong + item.gia * item.soLuong, 0),
)

const selectedDiscount = computed(() => {
  if (!giamGiaDangChon.value) return null
  return danhSachGiamGia.value.find((g) => Number(g.idGiamGia) === Number(giamGiaDangChon.value)) ?? null
})

const discountAmount = computed(() => {
  const subtotal = Number(tongTienTamTinhCotGiua.value || 0)
  const selected = selectedDiscount.value

  if (!selected || subtotal <= 0) return 0

  const { type, value, maxDiscount } = getDiscountPayload(selected)
  const cleanValue = Math.max(0, Number(value || 0))
  const cleanMax = Math.max(0, Number(maxDiscount || 0))

  if (isPercentDiscountType(type)) {
    const percent = Math.min(cleanValue, 100)
    const computedDiscount = Math.round(subtotal * (percent / 100))
    return cleanMax > 0 ? Math.min(computedDiscount, cleanMax) : computedDiscount
  }

  if (isFixedDiscountType(type)) {
    const fixedDiscount = cleanValue
    return Math.min(fixedDiscount, subtotal)
  }

  return 0
})

const tienGiamGia = computed(() => discountAmount.value)

const finalTotal = computed(() => {
  const subtotal = Number(tongTienTamTinhCotGiua.value || 0)
  const afterDiscount = Math.max(0, subtotal - discountAmount.value)
  return Math.max(0, afterDiscount - depositDaCoc.value)
})

const depositDaCoc = computed(() => {
  const deposit = Number(props.datBan?.soTienCoc ?? 0)
  return Number.isFinite(deposit) ? Math.max(0, deposit) : 0
})

const tongThanhToan = computed(() => finalTotal.value)

// ================= HÓA ĐƠN API =================
const checkHoaDonTam = async () => {
  if (isDataLoaded.value) return
  try {
    const reservationItems = props.datBan?.idDatBan ? buildReservationItems(props.datBan) : []

    if (reservationItems.length > 0) {
      gioHang.value = []
      danhSachMonPhucVu.value = reservationItems
      tabGioHang.value = 'mon-da-goi'
      hoaDonHienTai.value = null
      giamGiaDangChon.value = null
      isDataLoaded.value = true
      return
    }

    const res = await HoaDonApi.findByBanAndStatus(props.ban.idBan, 0)
    const hd = res.data
    if (!hd) {
      isDataLoaded.value = true
      return
    }
    hoaDonHienTai.value = hd

    danhSachMonPhucVu.value = (hd.chiTiet || []).map((item: any) => {
      const soLuong = Number(item.soLuong || 0)
      let daLen = item.daLen !== undefined && item.daLen !== null ? Number(item.daLen) : 0
      if (item.trangThaiMonAn === 'DA_LEN' || item.trangThaiMonAn === 'DA_PHUC_VU') {
        daLen = soLuong
      }

      return {
        idMon: item.idMon,
        idCombo: item.idCombo,
        tenMon: item.tenMon,
        tenCombo: item.tenCombo,
        tenQuay: item.tenQuay || 'Quầy Bếp',
        gia: Number(
          item.giaBanTaiThoiDiem ?? item.giaSauGiam ?? item.donGiaHienTai ?? item.giaCombo ?? 0,
        ),
        soLuong: soLuong,
        daLen: daLen,
        loai: item.idMon ? 'MON' : 'COMBO',
        comboItems: item.comboItems ?? [],
      }
    })

    giamGiaDangChon.value = hd.idGiamGia ?? null
    isDataLoaded.value = true
  } catch {
    console.log('Không có hóa đơn tạm')
    isDataLoaded.value = true
  }
}

const saveChiTietHoaDon = async (idHoaDon: number) => {
  for (const item of danhSachMonPhucVu.value) {
    const gia = item.gia ?? 0
    let trangThaiMonAn = 'DANG_LEN'
    if (item.daLen >= item.soLuong) {
      trangThaiMonAn = 'DA_LEN'
    }

    await HoaDonChiTietApi.add({
      maHoaDonChiTiet: `HDCT${Date.now()}${item.idMon || item.idCombo}`,
      idHoaDon,
      idMon: item.idMon,
      idCombo: item.idCombo,
      soLuong: item.soLuong,
      giaBanTaiThoiDiem: gia,
      tienGiamGiaMon: 0,
      thanhTien: gia * item.soLuong,
      trangThaiMonAn,
      daLen: item.daLen || 0,
      orderedBy: item.orderedBy || getCurrentOperatorName(),
      orderedAt: item.orderedAt || getLocalDateTimeNow(),
    } as any)
  }
}

const capNhatDatabaseNoRebuild = async () => {
  if (!hoaDonHienTai.value?.idHoaDon) return
  await HoaDonChiTietApi.deleteByHoaDon(hoaDonHienTai.value.idHoaDon)
  await saveChiTietHoaDon(hoaDonHienTai.value.idHoaDon)
}

const addHoaDon = async (payload: any) => {
  const res = await HoaDonApi.create(payload)
  if (res.data) {
    hoaDonHienTai.value = res.data
    await saveChiTietHoaDon(res.data.idHoaDon)
  }
}

const updateHoaDon = async (idHoaDon: number, payload: any) => {
  await HoaDonApi.update(idHoaDon, payload)
  await HoaDonChiTietApi.deleteByHoaDon(idHoaDon)
  await saveChiTietHoaDon(idHoaDon)
}

const buildChiTietPayload = () =>
  danhSachMonPhucVu.value.map((item: any, index: number) => ({
    maHoaDonChiTiet: `HDCT${Date.now()}${index + 1}${item.idMon || item.idCombo || ''}`,
    idMon: item.idMon ?? null,
    idCombo: item.idCombo ?? null,
    soLuong: Number(item.soLuong || 0),
    giaBanTaiThoiDiem: Number(item.gia || 0),
    tienGiamGiaMon: 0,
    thanhTien: Number((item.gia || 0) * (item.soLuong || 0)),
    orderedBy: item.orderedBy || getCurrentOperatorName(),
    orderedAt: item.orderedAt || getLocalDateTimeNow(),
  }))

const xuLyHoaDon = async (
  trangThaiHoaDon: number,
  trangThaiThanhToan: number,
  paymentMethodNumber: number | null = null,
) => {
  const currentOperator = getCurrentOperatorName()
  tenNhanVien.value = currentOperator

  const normalizedPaymentMethod = paymentMethodNumber ?? (trangThaiThanhToan === 1 ? 1 : null)
  const chiTietPayload = buildChiTietPayload()

  const payload = {
    maHoaDon: hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`,
    maGiaoDich: `TX${Date.now()}`,
    trangThaiHoaDon,
    trangThaiThanhToan,
    phuongThucThanhToan: normalizedPaymentMethod,
    tienTruocGiam: Number(tongTienTamTinhCotGiua.value || 0),
    tienGiamGia: Number(tienGiamGia.value || 0),
    tongTien: Number(tongThanhToan.value || 0),
    thoiGianXuat: getLocalDateTimeNow(),
    idBan: props.ban.idBan,
    idGiamGia: giamGiaDangChon.value,
    idDatBan: props.datBan?.idDatBan ?? null,
    idKhachHang: props.datBan?.idKhachHang ?? null,
    sdtKhachHang: props.datBan?.sdtKhachHang ?? null,
    tienCoc: props.datBan?.soTienCoc ?? null,
    tenNhanVien: currentOperator,
    chiTiet: chiTietPayload,
  }

  if (hoaDonHienTai.value) {
    await updateHoaDon(hoaDonHienTai.value.idHoaDon, payload)
  } else {
    await addHoaDon(payload)
  }
}


// // HÀM XỬ LÝ KHI BẤM NÚT LƯU
// const luuHoaDonTam = async () => {
//   if (blockShiftClosedAction()) return

//   try {
//     const isFirstTime = !hoaDonHienTai.value
//     await xuLyHoaDon(0, 0)
//     if (isFirstTime) {
//       alert('Tạo hóa đơn tạm thành công!')
//     } else {
//       alert('Cập nhật hóa đơn thành công!')
//     }
//   } catch (error) {
//     console.error('Lỗi khi lưu hóa đơn:', error)
//     alert('Lưu hóa đơn thất bại!')
//   }
// }

const normalizeReservationStatus = (value: any) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object' && 'name' in value) return String(value.name)
  return String(value)
}

const buildReservationItems = (db: any) => {
  const reservationItems: any[] = []
  const comboMap = new Map(danhSachCombo.value.map((item: any) => [Number(item.idCombo), item]))
  const monMap = new Map(danhSachMonAn.value.map((item: any) => [Number(item.idMon), item]))

  const pushItem = (item: any, loai: 'MON' | 'COMBO') => {
    if (!item) return

    const resolvedItem =
      loai === 'COMBO'
        ? (comboMap.get(Number(item.idCombo)) ?? null)
        : (monMap.get(Number(item.idMon)) ?? null)

    const gia = Number(
      resolvedItem?.giaSauGiam ??
        resolvedItem?.gia ??
        resolvedItem?.giaCombo ??
        item.giaSauGiam ??
        item.gia ??
        item.giaCombo ??
        0,
    )

    reservationItems.push({
      idMon: loai === 'MON' ? (Number(item.idMon) ?? null) : null,
      idCombo: loai === 'COMBO' ? (Number(item.idCombo) ?? null) : null,
      tenMon: item.tenMon ?? resolvedItem?.tenMon ?? null,
      tenCombo: item.tenCombo ?? resolvedItem?.tenCombo ?? null,
      tenQuay: loai === 'COMBO' ? 'Quầy Bếp' : layTenQuay(resolvedItem || item),
      gia,
      soLuong: Number(item.soLuong ?? 1),
      daLen: Number(item.soLuong ?? 1),
      loai,
      comboItems: resolvedItem?.comboItems ?? [],
    })
  }

  if (Array.isArray(db?.dsCombo)) {
    db.dsCombo.forEach((item: any) => pushItem(item, 'COMBO'))
  }

  if (Array.isArray(db?.dsMon)) {
    db.dsMon.forEach((item: any) => pushItem(item, 'MON'))
  }

  return reservationItems
}

const syncReservationToSeated = async () => {
  if (!props.datBan?.idDatBan) return
  const currentStatus = normalizeReservationStatus(props.datBan?.trangThai)
  if (currentStatus !== 'DA_XAC_NHAN' && currentStatus !== 'DA_NHAN_BAN') return

  try {
    if (props.ban?.idBan) {
      const currentBanStatus = normalizeReservationStatus(props.ban?.trangThai)
      const nextStatus =
        currentStatus === 'DA_NHAN_BAN'
          ? 'DANG_SU_DUNG'
          : currentBanStatus === 'DANG_SU_DUNG'
            ? 'DANG_SU_DUNG'
            : 'DA_DAT'

      const payload = {
        loaiBan: props.ban?.loaiBan ?? null,
        tenBan: props.ban?.tenBan ?? null,
        idKhuVuc: props.ban?.idKhuVuc ?? null,
        trangThai: nextStatus,
      }
      await BanApi.update(props.ban.idBan, payload)
    }
  } catch (error) {
    console.error(error)
  }
}

const buildReservationUpdatePayload = (datBan: any) => {
  if (!datBan) return null

  return {
    dsBan: Array.isArray(datBan.dsBan)
      ? datBan.dsBan.map((ban: any) => ban?.idBan).filter((id: any) => id != null)
      : [],
    dsCombo: Array.isArray(datBan.dsCombo)
      ? datBan.dsCombo.map((combo: any) => ({
          idCombo: combo.idCombo,
          soLuong: combo.soLuong,
        }))
      : [],
    dsMon: Array.isArray(datBan.dsMon)
      ? datBan.dsMon.map((mon: any) => ({
          idMon: mon.idMon,
          soLuong: mon.soLuong,
        }))
      : [],
    idKhachHang: datBan.idKhachHang ?? null,
    tenKhachHang: datBan.tenKhachHang ?? '',
    sdtKhachHang: datBan.sdtKhachHang ?? '',
    soNguoi: datBan.soNguoi ?? 1,
    thoiGianDenDuKien: datBan.thoiGianDenDuKien ?? null,
    soTienCoc: 0,
    trangThaiCoc: datBan?.soTienCoc > 0 ? 'DA_COC' : (datBan?.trangThaiCoc ?? 'CHUA_COC'),
    phuongThucThanhToan: datBan.phuongThucThanhToan ?? 'CHUA_THANH_TOAN',
    ghiChu: datBan.ghiChu ?? '',
    trangThai: 'HOAN_THANH',
  }
}

const markReservationCompleted = async () => {
  const reservationId = props.datBan?.idDatBan
  const banId = props.ban?.idBan

  if (reservationId && props.datBan) {
    try {
      const payload = buildReservationUpdatePayload(props.datBan)
      if (payload) {
        await DatBanQuanLyApi.update(reservationId, payload)
      }
    } catch (error) {
      console.warn('Không thể cập nhật đơn đặt bàn sau thanh toán:', error)
    }
  }

  if (banId) {
    try {
      const payload = {
        loaiBan: props.ban?.loaiBan ?? null,
        tenBan: props.ban?.tenBan ?? null,
        idKhuVuc: props.ban?.idKhuVuc ?? null,
        trangThai: 'TRONG',
      }
      await BanApi.update(banId, payload)
    } catch (error) {
      console.warn('Không thể cập nhật trạng thái bàn sau thanh toán:', error)
    }
  }
}

// Định nghĩa interface gọn gàng (đặt ở ngoài hoặc đầu file script)
interface CartItem {
  idMon?: number
  idCombo?: number
  loai: 'MON' | 'COMBO'
  soLuong: number
  tenQuay?: string
  [key: string]: any
}

// ================= ACTION XÁC NHẬN GỬI BẾP & GỌI API MAYIN =================
const luuTam = async () => {
  if (blockShiftClosedAction()) return

  const currentCart = gioHang.value as CartItem[]
  if (!currentCart.length) {
    alert('Vui lòng chọn món ăn trước khi nhấn gửi vào bếp!')
    return
  }

  try {
    monVuaGuiBep.value = [...currentCart]

    // 1. Cập nhật món phục vụ tại bàn
    currentCart.forEach((cartItem) => {
      const trungMon = danhSachMonPhucVu.value.find((p: any) =>
        p.loai === cartItem.loai &&
        (cartItem.loai === 'MON' ? p.idMon === cartItem.idMon : p.idCombo === cartItem.idCombo)
      )
      if (trungMon) {
        trungMon.soLuong += cartItem.soLuong
      } else {
        danhSachMonPhucVu.value.push({ ...cartItem, daLen: 0 })
      }
    })

    // 2. Nhóm món theo Quầy (Bếp / Bar)
    const grouped = currentCart.reduce<Record<string, CartItem[]>>((acc, item) => {
      const quay = item.tenQuay || 'Quầy Bếp'
      ;(acc[quay] ||= []).push(item) // Cú pháp ||= ngắn gọn hơn
      return acc
    }, {})

    monTheoQuayMap.value = grouped

    // 3. Lưu hóa đơn tạm vào DB trước
    await xuLyHoaDon(0, 0)
    await nextTick()

    // 4. GỬI PHIẾU BÁO BẰNG API SONG SONG (PROMISE.ALL) 🚀
    const now = new Date()
    const thoiGianFormatted = `${now.toLocaleTimeString('vi-VN')} ${now.toLocaleDateString('vi-VN')}`

    const printRequests = Object.entries(grouped).map(([quay, danhSachMon]) =>
      MayInApi.sendTicket({
        tenQuay: quay,
        maHoaDon: hoaDonHienTai.value?.maHoaDon ?? 'Mới',
        tenBan: props.ban?.tenBan ?? 'N/A',
        tenNhanVien: tenNhanVien?.value ?? 'Nhân viên',
        thoiGian: thoiGianFormatted,
        danhSachMon: danhSachMon.map((item) => ({
          tenMon: itemName(item),
          soLuong: item.soLuong,
        })),
      })
    )

    // Chờ tất cả quầy gửi request in xong cùng lúc
    await Promise.all(printRequests)

    // Reset giỏ và chuyển tab
    gioHang.value = []
    tabGioHang.value = 'mon-dang-len'
  } catch (error) {
    console.error('Lỗi khi gửi bếp / in phiếu:', error)
    alert('Gửi bếp thất bại!')
  }
}

const taoHoaDon = async () => {
  if (blockShiftClosedAction()) return

  try {
    await xuLyHoaDon(1, 1, 1)
    await markReservationCompleted()
    notifyPaymentComplete()
    emit('quayLai')

    if (shiftStore.currentShift?.isOpen) {
      shiftStore.syncBillFromPos({
        id: String(hoaDonHienTai.value?.idHoaDon || Date.now()),
        code: hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`,
        customer: props.datBan?.tenKhachHang || 'Khách hàng',
        total: Number(tongThanhToan.value || 0),
        gross: Number(tongTienTamTinhCotGiua.value || 0),
        discount: Number(tienGiamGia.value || 0),
        paymentMethod: 'cash',
        status: 'paid',
        createdAt: new Date().toLocaleTimeString('vi-VN', {
          hour: '2-digit',
          minute: '2-digit',
        }),
        createdAtTimestamp: Date.now(),
      })
    }

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
  () => giamGiaDangChon.value,
  (selectedId) => {
    if (selectedId == null) return
    if (tongTienTamTinhCotGiua.value <= 0) {
      alert('Vui lòng chọn món trước khi áp dụng mã giảm giá')
      giamGiaDangChon.value = null
    }
  },
)

watch(
  () => tongTienTamTinhCotGiua.value,
  (subtotal) => {
    if (subtotal <= 0 && giamGiaDangChon.value != null) {
      giamGiaDangChon.value = null
    }
  },
)

watch(
  () => props.datBan,
  async (db) => {
    if (!db || isDataLoaded.value) return

    const reservationItems = buildReservationItems(db)

    if (reservationItems.length > 0) {
      gioHang.value = []
      danhSachMonPhucVu.value = reservationItems
      tabGioHang.value = 'mon-da-goi'
    } else if (db.idCombo) {
      gioHang.value = [
        {
          idCombo: db.idCombo,
          tenCombo: db.tenCombo,
          tenQuay: 'Quầy Bếp',
          gia: db.giaCombo ?? 0,
          soLuong: 1,
          loai: 'COMBO',
          comboItems: db.comboItems ?? [],
        },
      ]
      danhSachMonPhucVu.value = []
    }

    await syncReservationToSeated()
  }
)

onMounted(async () => {
  await loadData()
  await loadGiamGia()
  await checkHoaDonTam()
})
</script>

<template>
  <div class="thanh-toan-container">
    <!-- CỘT DANH MỤC TRÁI -->
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

    <!-- CỘT DANH SÁCH MÓN GIỮA -->
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
            :style="isShiftClosedForUi ? { opacity: 0.55, cursor: 'not-allowed' } : null"
            @click="!isShiftClosedForUi && themVaoGio(combo, 'COMBO')"
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
            :style="isShiftClosedForUi ? { opacity: 0.55, cursor: 'not-allowed' } : null"
            @click="!isShiftClosedForUi && themVaoGio(mon, 'MON')"
          >
            {{ mon.tenMon }}
          </div>
        </template>
      </div>
    </div>

    <!-- CỘT GIỎ HÀNG PHẢI -->
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
        <!-- TAB: GỌI MÓN (ĐANG NHẶT VÀO GIỎ) -->
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

        <!-- TAB: MÓN ĐANG LÊN BẾP -->
        <div v-if="tabGioHang === 'mon-dang-len'" class="gio-hang-list">
          <div
            class="tab-action-header"
            v-if="danhSachMonPhucVu.some((i) => Number(i.soLuong) - Number(i.daLen || 0) > 0)"
          >
            <button class="btn-xac-nhan-all" :disabled="isShiftClosedForUi" @click="xacNhanTatCaMon">
              ✓ Xác nhận tất cả lên đồ
            </button>
          </div>
          <div v-if="!danhSachMonPhucVu.some((i) => Number(i.soLuong) - Number(i.daLen || 0) > 0)" class="empty-cart">
            Không có món đang chờ.
          </div>
          <div
            v-for="item in danhSachMonPhucVu.filter((i) => Number(i.soLuong) - Number(i.daLen || 0) > 0)"
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
                  Còn: {{ Number(item.soLuong) - Number(item.daLen || 0) }} / {{ item.soLuong }}
                </div>
                <div class="item-price">
                  Giá: {{ ((Number(item.soLuong) - Number(item.daLen || 0)) * item.gia).toLocaleString('vi-VN') }} đ
                </div>
              </div>
            </div>
            <button class="btn-check-item" :disabled="isShiftClosedForUi" @click="xacNhanTungMon(item)">✓ Lên</button>
          </div>
        </div>

        <!-- TAB: MÓN ĐÃ PHỤC VỤ XONG -->
        <div v-if="tabGioHang === 'mon-da-goi'" class="gio-hang-list">
          <div v-if="!danhSachMonPhucVu.some((i) => Number(i.daLen || 0) > 0)" class="empty-cart">
            Chưa có món nào được lên.
          </div>
          <div
            v-for="item in danhSachMonPhucVu.filter((i) => Number(i.daLen || 0) > 0)"
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
                  Thành tiền: {{ (Number(item.daLen) * item.gia).toLocaleString('vi-VN') }} đ
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- CỤM TÍNH TIỀN GIO-HANG-FOOTER -->
      <div class="gio-hang-footer">
        <hr />
        <button v-if="tabGioHang === 'goi-mon'" class="btn-luu-phu" :disabled="isShiftClosedForUi" @click="luuTam()">
          🔥 Xác nhận gửi vào bếp / Bar
        </button>

        <div class="tong-tien">
          Tạm tính: {{ tongTienTamTinhCotGiua.toLocaleString('vi-VN') }} đ
        </div>
        <div class="tong-tien">Tiền giảm giá: {{ tienGiamGia.toLocaleString('vi-VN') }} đ</div>
        <div v-if="depositDaCoc > 0" class="tong-tien">
          Tiền cọc đã trừ: {{ depositDaCoc.toLocaleString('vi-VN') }} đ
        </div>
        <div class="tong-tien main-total-center">
          Còn phải thanh toán: {{ tongThanhToan.toLocaleString('vi-VN') }} đ
        </div>
        <div>
          <select class="discount-input" v-model="giamGiaDangChon">
            <option :value="null">Chọn mã giảm giá</option>
            <option v-for="g in danhSachGiamGia" :key="g.idGiamGia" :value="g.idGiamGia">
              {{ getDiscountDisplayText(g) }}
            </option>
          </select>
        </div>
        <button class="btn-thanh-toan" :disabled="isShiftClosedForUi" @click="optionPay">Thanh toán</button>
      </div>
    </div>

    <!-- VÙNG IN KHUẤT PHIẾU BÁO CHẾ BIẾN K80 -->
    <div style="display: none">
      <div
        v-for="(items, tenQuay) in monTheoQuayMap"
        :key="tenQuay"
        :id="`vung-phieu-in-${String(tenQuay).replace(/\s+/g, '-')}`"
        class="phieu-in-bep"
      >
        <div class="phieu-header">
          <h2>PHIẾU BÁO CHẾ BIẾN</h2>
          <h3>[{{ tenQuay }}]</h3>
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
            <tr v-for="(item, idx) in items" :key="`print-${idx}`">
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
        <div class="phieu-footer">Vui lòng chế biến/pha chế theo thứ tự!</div>
      </div>
    </div>

    <!-- POPUPS THANH TOÁN -->
    <PopupThanhToan
      v-if="phuongThucThanhToan"
      :tongTien="tongThanhToan"
      @close="closePopup"
      @chonTienMat="popupTienMat"
      @chonChuyenKhoan="moPopupQR"
    />

    <PopupTienMat
      v-if="tienMatThanhToan"
      :tongTien="tongThanhToan"
      @close="closeTienMatPopup"
      @xacNhan="taoHoaDon"
    />

    <PopupSePayQR
      v-if="hienThiSePayQR"
      :show="hienThiSePayQR"
      :idHoaDon="hoaDonHienTai?.idHoaDon"
      :maHoaDon="hoaDonHienTai?.maHoaDon"
      :tongTien="tongThanhToan"
      :tenBan="props.ban?.tenBan"
      @close="hienThiSePayQR = false"
      @payment-success="handleChuyenKhoanThanhCong"
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
  height: calc(100vh - 100px);
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
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.danh-muc {
  width: 20%;
}

.danh-sach-mon {
  width: 50%;
}

.gio-hang {
  width: 30%;
}

.danh-muc > div:last-child {
  margin-top: auto;
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
  align-content: start;
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

.gio-hang-tab-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.gio-hang-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.gio-hang-footer {
  flex-shrink: 0;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 216, 107, 0.15);
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
.tong-tien {
  background: linear-gradient(135deg, rgba(255, 216, 107, 0.12), rgba(212, 175, 55, 0.05));
  border: 1px solid rgba(255, 216, 107, 0.15);
  border-radius: 12px;
  padding: 10px;
  color: #ffd86b;
  font-size: 14px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 8px;
}
.main-total-center {
  background: linear-gradient(135deg, #ffd86b, #d4af37) !important;
  color: #111 !important;
  font-size: 18px !important;
  font-weight: 800 !important;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.25);
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