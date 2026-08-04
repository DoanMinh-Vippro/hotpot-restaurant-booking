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
import { useModulePermission } from '@/utils/permissionGuard'
import printJS from 'print-js'
import DanhMucApi from '@/api/DanhMucApi.ts'

// ================= PROPS =================
const props = defineProps<{
  ban: any
  datBan: any | null
}>()

// ================= EMIT =================
const emit = defineEmits(['quayLai', 'payment-complete'])
const shiftStore = useShiftStore()
const authStore = useAuthStore()
const { ensureUse: ensurePosUse } = useModulePermission('pos')
const quayLai = () => {
  emit('quayLai')
}

const notifyPaymentComplete = (billId?: number | string | null) => {
  if (props.ban?.idBan) {
    emit('payment-complete', { idBan: props.ban.idBan, trangThai: 'TRONG', billId: billId ?? null })
  }
}

const clearPosOrderStorage = (tableId?: number | string | null) => {
  if (typeof window === 'undefined') return

  const keysToRemove = Object.keys(localStorage).filter((key) => {
    if (key === 'pos-order-context') return true
    return key.startsWith('pos-order-context-')
  })

  keysToRemove.forEach((key) => localStorage.removeItem(key))

  if (tableId != null) {
    localStorage.removeItem(`pos-order-context-${Number(tableId)}`)
  }
}

const normalizeTableStatus = (value: any) => {
  if (!value) return ''
  if (typeof value === 'string') return value.trim().toUpperCase()
  if (typeof value === 'object' && 'name' in value) return String(value.name).trim().toUpperCase()
  return String(value).trim().toUpperCase()
}

const resetPaymentState = () => {
  clearPosOrderStorage(props.ban?.idBan)
  hoaDonHienTai.value = null
  gioHang.value = []
  danhSachMonPhucVu.value = []
  monVuaGuiBep.value = []
  giamGiaDangChon.value = null
  phuongThucThanhToan.value = false
  tienMatThanhToan.value = false
  hienThiSePayQR.value = false
  isSubmittingPayment.value = false
  tabGioHang.value = 'goi-mon'
}

const finalizePaymentSuccess = async (billId?: number | string | null) => {
  await markReservationCompleted()
  resetPaymentState()
  notifyPaymentComplete(billId)
  await nextTick()
}

const hoanTatThanhToan = async () => {
  try {
    await finalizePaymentSuccess()
    emit('quayLai')
  } catch (error) {
    console.error('Không thể cập nhật trạng thái bàn sau thanh toán:', error)
  }
}

// ================= STATE =================
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

const hasActiveShift = computed(() => Boolean(shiftStore.currentShift?.isOpen))

const matchesInheritedPendingInvoice = (tableId?: number | string | null, billId?: number | string | null) => {
  const normalizedTableId = tableId != null ? Number(tableId) : null
  const normalizedBillId = billId != null ? String(billId) : null

  const pendingCandidates = [
    ...(shiftStore.currentShift?.handoverContext?.pendingTables || []),
    ...(shiftStore.currentShift?.bills || []),
    ...(shiftStore.history?.[0]?.handoverContext?.pendingTables || []),
  ]

  return pendingCandidates.some((item: any) => {
    const pendingTableId = item?.idBan != null ? Number(item.idBan) : null
    const pendingBillId = item?.billId != null ? String(item.billId) : null
    const billIdentifier = item?.id != null ? String(item.id) : null
    const matchesTable = normalizedTableId != null && pendingTableId != null && normalizedTableId === pendingTableId
    const matchesBill = normalizedBillId != null && (pendingBillId != null ? normalizedBillId === pendingBillId : false)
    const matchesShiftBill = normalizedBillId != null && billIdentifier != null && normalizedBillId === billIdentifier
    return matchesTable || matchesBill || matchesShiftBill
  })
}

const hasPendingHandoverAccess = computed(() => {
  const tableId = props.ban?.idBan ?? null
  const billId = hoaDonHienTai.value?.idHoaDon ?? null
  return matchesInheritedPendingInvoice(tableId, billId)
})

const ensureActiveShift = () => {
  if (hasActiveShift.value || hasPendingHandoverAccess.value) return true
  alert('Bạn chưa mở ca làm việc. Vui lòng mở ca trước khi gọi món, bán hàng hoặc thanh toán.')
  window.location.assign('/shift-management')
  return false
}

const tenNhanVien = ref<string>(getCurrentOperatorName())

const getLocalDateTimeNow = () => {
  const now = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const getCurrentLocalTimestamp = () => new Date().getTime()

const danhSachGiamGia = ref<any[]>([])
const giamGiaDangChon = ref<number | null>(null)

const phuongThucThanhToan = ref(false)
const tienMatThanhToan = ref(false)
const hienThiSePayQR = ref(false)
const hoaDonHienTai = ref<any>(null)
const isSubmittingPayment = ref(false)

// ================= UTILS =================
const itemName = (item: any) => item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'

const buildPaidShiftBill = (paymentMethod: 'cash' | 'transfer') => ({
  id: String(hoaDonHienTai.value?.idHoaDon || Date.now()),
  code: hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`,
  customer: props.datBan?.tenKhachHang || 'Khách hàng',
  total: Number(tongThanhToan.value || 0),
  gross: Number(tongTienTamTinhCotGiua.value || 0),
  discount: Number(tienGiamGia.value || 0),
  paymentMethod,
  status: 'paid' as const,
  createdAt: new Date().toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  }),
  createdAtTimestamp: Date.now(),
})

const syncPaidBillToShift = (bill: ReturnType<typeof buildPaidShiftBill>) => {
  if (shiftStore.currentShift?.isOpen) {
    shiftStore.syncBillFromPos(bill)
  }
}

// Map mã quầy từ Danh Mục ('BEP' | 'BAR') sang tên hiển thị
const layTenQuay = (item: any): string => {
  // Bắt trường `quay` từ item trực tiếp hoặc qua object `danhMuc`
  const quay = item.quay || item.danhMuc?.quay || ''

  if (String(quay).toUpperCase() === 'BAR') {
    return 'Quầy Bar'
  }
  
  return 'Quầy Bếp'
}

// ================= LOAD DATA =================
// const loadData = async () => {
//   const combo = await ComBoApi.hienThiComBo()
//   const mon = await MonApi.hienThiMon()
//   danhSachCombo.value = (combo.data || []).filter((cb: Combo) => cb.trangThai === 1)
//   danhSachMonAn.value = (mon.data || []).filter((m: Mon) => m.trangThai === 0)
// }

const loadData = async () => {
  try {
    // 1. Gọi song song cả 3 API cho nhanh
    const [comboRes, monRes, danhMucRes] = await Promise.all([
      ComBoApi.hienThiComBo(),
      MonApi.hienThiMon(),
      DanhMucApi.getDanhSach()
    ])

    const dsDanhMuc = danhMucRes.data || []
    const dsMonRaw = monRes.data || []

    // 2. Map dữ liệu quầy từ Danh Mục sang từng Món Ăn dựa vào idDanhMuc
    danhSachMonAn.value = dsMonRaw
      .filter((m: Mon) => m.trangThai === 0)
      .map((m: any) => {
        // Tìm danh mục tương ứng
        const dm = dsDanhMuc.find((d: any) => d.idDanhMuc === m.idDanhMuc)
        
        return {
          ...m,
          // Ưu tiên m.quay, nếu null thì lấy dm.quay, nếu vẫn ko có mới lấy 'BEP'
          quay: m.quay || m.danhMuc?.quay || dm?.quay || 'BEP'
        }
      })

    danhSachCombo.value = (comboRes.data || []).filter((cb: Combo) => cb.trangThai === 1)
  } catch (error) {
    console.error('Lỗi load dữ liệu:', error)
  }
}

const normalizeDiscountType = (value: unknown) => {
  const normalized = `${value ?? ''}`
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toUpperCase()

  if (['PHANTRAM', 'PERCENT', 'PHNTRAM', 'PHTNTRAM', 'PHTRAM'].includes(normalized)) return 'percent'
  if (normalized.includes('PH') && normalized.includes('TRAM')) return 'percent'
  if (['TIENMAT', 'TIEN', 'VND', 'FIXED', 'MONEY'].includes(normalized)) return 'fixed'
  if (normalized.includes('TIEN') || normalized.includes('MAT') || normalized.includes('GIATRI') || normalized.includes('VALUE')) return 'fixed'

  return 'unknown'
}

const parseNumericValue = (value: unknown) => {
  if (value == null || value === '') return 0
  if (typeof value === 'number') return Number.isFinite(value) ? value : 0

  const raw = `${value}`.trim()
  if (!raw) return 0

  const cleaned = raw.replace(/[^\d,.-]/g, '')
  if (!cleaned) return 0

  if (cleaned.includes(',') && cleaned.includes('.')) {
    const lastComma = cleaned.lastIndexOf(',')
    const lastDot = cleaned.lastIndexOf('.')
    const decimalSeparator = lastComma > lastDot ? ',' : '.'
    const thousandSeparator = decimalSeparator === ',' ? '.' : ','
    const withoutThousands = cleaned.replace(new RegExp(`\\${thousandSeparator}`, 'g'), '')
    const normalizedDecimal = withoutThousands.replace(decimalSeparator, '.')
    return Number(normalizedDecimal)
  }

  if (cleaned.includes(',')) {
    const parts = cleaned.split(',')
    const fractionalPart = parts[1]
    const integerPart = parts[0]
    if (parts.length > 2 || (fractionalPart != null && fractionalPart.length === 3 && integerPart != null && integerPart.length >= 1)) {
      return Number(parts.join(''))
    }
    return Number(cleaned.replace(/,/g, '.'))
  }

  if (cleaned.includes('.')) {
    const parts = cleaned.split('.')
    const fractionalPart = parts[1]
    const integerPart = parts[0]
    if (parts.length > 2 || (fractionalPart != null && fractionalPart.length === 3 && integerPart != null && integerPart.length >= 1)) {
      return Number(parts.join(''))
    }
    return Number(cleaned)
  }

  return Number(cleaned)
}

const formatDiscountValue = (item: any) => {
  const numeric = parseNumericValue(item?.giaTriGiam)
  const type = normalizeDiscountType(item?.loaiGiam)

  if (type === 'percent') {
    return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 2 })}%`
  }

  if (type === 'fixed') {
    return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 0 })} đ`
  }

  return `${numeric.toLocaleString('vi-VN', { maximumFractionDigits: 0 })}`
}

const parseConditionThreshold = (discount: any) => {
  const raw = `${discount?.dieuKienSuDung ?? ''}`.trim()
  if (!raw) return null

  const normalized = raw
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/[^a-zA-Z0-9\s.,-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  const match = normalized.match(/(-?\d+(?:[.,]\d+)?)(\s*(k|tr|trieu|nghin|nghìn))?/i)
  if (!match || !match[1]) return null

  const numericValue = Number(match[1].replace(/\./g, '').replace(',', '.'))
  if (!Number.isFinite(numericValue)) return null

  const suffix = `${match[2] || ''}`.toLowerCase()
  let multiplier = 1

  if (suffix.includes('k') || suffix.includes('nghin')) multiplier = 1000
  else if (suffix.includes('tr') || suffix.includes('trieu')) multiplier = 1000000

  return numericValue * multiplier
}

const isDiscountEligible = (discount: any, baseAmount: number) => {
  const threshold = parseConditionThreshold(discount)
  if (threshold == null) return true
  return baseAmount >= threshold
}

const getDateOnlyKey = (value: unknown) => {
  if (value == null || value === '') return ''

  const raw = `${value}`.trim()
  const match = raw.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})/)
  if (match) {
    const [, year, monthValue, dayValue] = match
    const month = monthValue ?? '1'
    const day = dayValue ?? '1'
    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`
  }

  const parsed = new Date(raw)
  if (Number.isNaN(parsed.getTime())) return ''

  const pad = (num: number) => String(num).padStart(2, '0')
  return `${parsed.getFullYear()}-${pad(parsed.getMonth() + 1)}-${pad(parsed.getDate())}`
}

const isDiscountActive = (item: any) => {
  if (Number(item?.trangThai ?? 1) !== 1) return false

  const endDate = getDateOnlyKey(item?.ngayKetThuc)
  if (!endDate) return true

  const today = new Date()
  const todayKey = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  return todayKey <= endDate
}

const loadGiamGia = async () => {
  try {
    const gg = await GiamGiaApi.getDanhSach()
    const rawDiscounts = Array.isArray(gg?.data) ? gg.data : []
    danhSachGiamGia.value = rawDiscounts.filter((item: any) => isDiscountActive(item))

    if (giamGiaDangChon.value && !danhSachGiamGia.value.some((item: any) => item.idGiamGia === giamGiaDangChon.value)) {
      giamGiaDangChon.value = null
    }
  } catch (error) {
    console.error('Lỗi tải mã giảm giá:', error)
    danhSachGiamGia.value = []
  }
}

// ================= GIỎ HÀNG =================
const themVaoGio = (item: any, loai: string) => {
  if (!ensureActiveShift()) return
  if (normalizeTableStatus(props.ban?.trangThai) === 'TRONG' && hoaDonHienTai.value) {
    resetPaymentState()
  }

  if (item.trangThaiBan === 0) {
    alert(`${loai === 'MON' ? 'Món' : 'Combo'} "${item.tenMon || item.tenCombo}" này đã hết hàng!`)
    return
  }

  // Xác định quầy: Combo mặc định Quầy Bếp, Món lẻ lấy theo Danh Mục
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
      tenQuay: quayCheBien, // Lưu thông tin quầy vào giỏ
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

// ================= XỬ LÝ LÊN MÓN =================
const xacNhanTungMon = async (item: any) => {
  if (item.daLen < item.soLuong) {
    item.daLen++
    await xuLyHoaDon(0, 0)
  }
}

const xacNhanTatCaMon = async () => {
  danhSachMonPhucVu.value.forEach((item) => {
    item.daLen = item.soLuong
  })
  await xuLyHoaDon(0, 0)
}

// ================= POPUP =================
const optionPay = async () => {
  if (!ensureActiveShift()) return
  if (isSubmittingPayment.value) return

  if (danhSachMonPhucVu.value.length === 0) {
    alert('Chưa có món nào được gửi vào bếp để thanh toán!')
    return
  }

  if (tongThanhToan.value <= 0) {
    alert('Không thể tạo hóa đơn khi tổng tiền bằng 0. Vui lòng kiểm tra giỏ hàng và số tiền thanh toán.')
    return
  }

  phuongThucThanhToan.value = true
}
const closePopup = () => {
  phuongThucThanhToan.value = false
  isSubmittingPayment.value = false
}
const popupTienMat = () => {
  if (isSubmittingPayment.value) return
  phuongThucThanhToan.value = false
  tienMatThanhToan.value = true
}
const closeTienMatPopup = () => {
  tienMatThanhToan.value = false
  isSubmittingPayment.value = false
}

const moPopupQR = async () => {
  if (isSubmittingPayment.value) return

  if (tongThanhToan.value <= 0) {
    alert('Không thể tạo hóa đơn khi tổng tiền bằng 0. Vui lòng kiểm tra giỏ hàng và số tiền thanh toán.')
    return
  }

  isSubmittingPayment.value = true
  try {
    await xuLyHoaDon(0, 0)
    phuongThucThanhToan.value = false
    hienThiSePayQR.value = true
  } catch (error) {
    console.error('Không thể tạo hóa đơn chuyển khoản:', error)
    alert('Không thể tạo hóa đơn chuyển khoản. Vui lòng thử lại.')
    isSubmittingPayment.value = false
  }
}

const handleChuyenKhoanThanhCong = async () => {
  if (isSubmittingPayment.value) return
  isSubmittingPayment.value = true

  try {
    hienThiSePayQR.value = false
    await xuLyHoaDon(1, 1, 2)
    const paidBill = buildPaidShiftBill('transfer')
    syncPaidBillToShift(paidBill)
    await finalizePaymentSuccess(paidBill.id)

    emit('quayLai')
    alert(`Bàn ${props.ban?.tenBan} đã thanh toán chuyển khoản thành công tự động!`)
  } catch (error) {
    isSubmittingPayment.value = false
    alert('Có lỗi xảy ra khi cập nhật trạng thái hóa đơn!')
  }
}

// ================= COMPUTED CHI PHÍ =================
const tongTienTamTinhCotGiua = computed(() =>
  danhSachMonPhucVu.value.reduce((tong, item) => tong + item.gia * item.soLuong, 0),
)

const tienGiamGia = computed(() => {
  const base = tongTienTamTinhCotGiua.value
  if (!giamGiaDangChon.value) return 0

  const giamGia = danhSachGiamGia.value.find((g: any) => g.idGiamGia === giamGiaDangChon.value)
  if (!giamGia || !isDiscountEligible(giamGia, base)) return 0

  const value = parseNumericValue(giamGia.giaTriGiam)
  const type = normalizeDiscountType(giamGia.loaiGiam)

  if (type === 'fixed') return Math.min(value, base)
  if (type === 'percent') {
    const percentDiscount = (base * value) / 100
    const maxDiscount = parseNumericValue(giamGia.giaTriGiamToiDa)
    const cappedDiscount = maxDiscount > 0 ? Math.min(percentDiscount, maxDiscount) : percentDiscount
    return Math.min(cappedDiscount, base)
  }
  return 0
})

const depositDaCoc = computed(() => {
  const deposit = Number(props.datBan?.soTienCoc ?? 0)
  return Number.isFinite(deposit) ? Math.max(0, deposit) : 0
})

const tongThanhToan = computed(() => {
  const base = tongTienTamTinhCotGiua.value - tienGiamGia.value
  return Math.max(0, base - depositDaCoc.value)
})

watch([tongTienTamTinhCotGiua, () => danhSachGiamGia.value], () => {
  if (!giamGiaDangChon.value) return

  const selected = danhSachGiamGia.value.find((item: any) => item.idGiamGia === giamGiaDangChon.value)
  if (!selected || !isDiscountEligible(selected, tongTienTamTinhCotGiua.value)) {
    giamGiaDangChon.value = null
  }
})

watch(giamGiaDangChon, (newValue, oldValue) => {
  if (!newValue || newValue === oldValue) return

  const selected = danhSachGiamGia.value.find((item: any) => item.idGiamGia === newValue)
  if (!selected) {
    giamGiaDangChon.value = null
    return
  }

  if (!isDiscountEligible(selected, tongTienTamTinhCotGiua.value)) {
    window.alert('Đơn hàng chưa đạt điều kiện để sử dụng mã giảm giá này')
    giamGiaDangChon.value = null
  }
})

// ================= HÓA ĐƠN API =================
const checkHoaDonTam = async () => {
  try {
    const reservationStatus = normalizeReservationStatus(props.datBan?.trangThai)
    const reservationItems = props.datBan?.idDatBan ? buildReservationItems(props.datBan) : []

    if (normalizeTableStatus(props.ban?.trangThai) === 'TRONG') {
      resetPaymentState()
      return
    }

    if (props.datBan?.idDatBan && !['DA_XAC_NHAN', 'DA_NHAN_BAN'].includes(reservationStatus)) {
      resetPaymentState()
      return
    }

    if (reservationItems.length > 0) {
      gioHang.value = []
      danhSachMonPhucVu.value = reservationItems
      tabGioHang.value = 'mon-da-goi'
      hoaDonHienTai.value = null
      giamGiaDangChon.value = null
      return
    }

    const res = await HoaDonApi.findByBanAndStatus(props.ban.idBan, 0)
    const hd = res.data
    if (!hd) return
    hoaDonHienTai.value = hd

    danhSachMonPhucVu.value = (hd.chiTiet || []).map((item: any) => ({
      idMon: item.idMon,
      idCombo: item.idCombo,
      tenMon: item.tenMon,
      tenCombo: item.tenCombo,
      tenQuay: item.tenQuay || 'Quầy Bếp',
      gia: Number(
        item.giaBanTaiThoiDiem ?? item.giaSauGiam ?? item.donGiaHienTai ?? item.giaCombo ?? 0,
      ),
      soLuong: item.soLuong,
      daLen: item.trangThaiMonAn === 'DA_LEN' ? item.soLuong : item.daLen || 0,
      loai: item.idMon ? 'MON' : 'COMBO',
      comboItems: item.comboItems ?? [],
    }))
    giamGiaDangChon.value = hd.idGiamGia ?? null
  } catch {
    console.log('Không có hóa đơn tạm')
  }
}

const saveChiTietHoaDon = async (idHoaDon: number) => {
  for (const item of danhSachMonPhucVu.value) {
    const gia = item.gia ?? 0
    let trangThaiMonAn = 'DANG_LEN'
    if (item.daLen === item.soLuong) {
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
      daLen: item.daLen,
      orderedBy: item.orderedBy || getCurrentOperatorName(),
      orderedAt: item.orderedAt || getLocalDateTimeNow(),
    } as any)
  }
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

const buildInvoiceItemsPayload = () =>
  danhSachMonPhucVu.value
    .filter((item: any) => Number(item.soLuong ?? 0) > 0)
    .map((item: any) => ({
      maHoaDonChiTiet: `HDCT${Date.now()}${item.idMon || item.idCombo}`,
      idMon: item.idMon ?? null,
      idCombo: item.idCombo ?? null,
      soLuong: Number(item.soLuong ?? 0),
      giaBanTaiThoiDiem: Number(item.gia ?? 0),
      tienGiamGiaMon: 0,
      thanhTien: Number(item.gia ?? 0) * Number(item.soLuong ?? 0),
    }))

const xuLyHoaDon = async (
  trangThaiHoaDon: number,
  trangThaiThanhToan: number,
  phuongThucThanhToanParam?: number | string,
) => {
  const currentOperator = getCurrentOperatorName()
  tenNhanVien.value = currentOperator

  const selectedPaymentMethod =
    phuongThucThanhToanParam !== undefined
      ? Number(phuongThucThanhToanParam)
      : trangThaiThanhToan === 1
        ? 1
        : null

  const payload = {
    maHoaDon: hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`,
    trangThaiHoaDon,
    trangThaiThanhToan,
    phuongThucThanhToan: selectedPaymentMethod,
    tienTruocGiam: tongTienTamTinhCotGiua.value,
    tienGiamGia: tienGiamGia.value,
    tongTien: tongThanhToan.value,
    thoiGianXuat: getLocalDateTimeNow(),
    idBan: props.ban.idBan,
    idGiamGia: giamGiaDangChon.value,
    idDatBan: props.datBan?.idDatBan ?? null,
    idKhachHang: props.datBan?.idKhachHang ?? null,
    sdtKhachHang: props.datBan?.sdtKhachHang ?? null,
    tienCoc: props.datBan?.soTienCoc ?? null,
    tenNhanVien: currentOperator,
    chiTiet: buildInvoiceItemsPayload(),
  }
  if (hoaDonHienTai.value) {
    await updateHoaDon(hoaDonHienTai.value.idHoaDon, payload)
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

  clearPosOrderStorage(banId)

  const updateReservationToCompleted = async (reservation: any) => {
    if (!reservation?.idDatBan) return
  if (reservationId && props.datBan) {
    try {
      const payload = buildReservationUpdatePayload(reservation)
      if (payload) {
        await DatBanQuanLyApi.update(reservation.idDatBan, payload)
      }
    } catch (error) {
      console.warn('Không thể cập nhật đơn đặt bàn sau thanh toán:', error)
    }
  }}

  // Nếu có reservation hiện tại thì cập nhật trực tiếp
  if (reservationId && props.datBan) {
    await updateReservationToCompleted(props.datBan)
  } else if (banId) {
    // Nếu không có reservation trực tiếp, tìm reservation hoạt động theo bàn và đóng nó
    try {
      const reservationRes = await DatBanQuanLyApi.getAll()
      const reservationList = Array.isArray(reservationRes?.data) ? reservationRes.data : []
      const matchedReservation = reservationList.find((reservation: any) =>
        Array.isArray(reservation?.dsBan) &&
        reservation.dsBan.some((ban: any) => Number(ban?.idBan) === Number(banId)) &&
        ['DA_NHAN_BAN', 'DA_XAC_NHAN'].includes(String(reservation?.trangThai || ''))
      )
      if (matchedReservation) {
        await updateReservationToCompleted(matchedReservation)
      }
    } catch (error) {
      console.warn('Không thể tìm reservation theo bàn để cập nhật trạng thái:', error)
    }
  }

  // Cập nhật trạng thái bàn về TRỐNG (quan trọng: luôn thực hiện)
  if (banId) {
    try {
      const payload = {
        loaiBan: props.ban?.loaiBan ?? null,
        tenBan: props.ban?.tenBan ?? null,
        idKhuVuc: props.ban?.idKhuVuc ?? null,
        trangThai: 'TRONG',
      }
      await BanApi.update(banId, payload)
      console.log(`Đã cập nhật bàn ${props.ban?.tenBan} sang trạng thái TRỐNG`)
    } catch (error) {
      console.warn('Không thể cập nhật trạng thái bàn sau thanh toán:', error)
    }
  }
}

// ================= ACTION XÁC NHẬN GỬI BẾP & IN K80 (PHÂN LOẠI THEO QUẦY) =================

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const luuTam = async () => {
  if (!ensureActiveShift()) return

  if (gioHang.value.length === 0) {
    alert('Vui lòng chọn món ăn trước khi nhấn gửi vào bếp!')
    return
  }
  try {
    monVuaGuiBep.value = [...gioHang.value]

    // Đồng bộ vào danh sách món phục vụ tại bàn
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

    // Gom nhóm món theo tên quầy ('Quầy Bếp', 'Quầy Bar',...)
    const grouped = gioHang.value.reduce((acc: Record<string, any[]>, item) => {
      const quay = item.tenQuay || 'Quầy Bếp'
      if (!acc[quay]) {
        acc[quay] = []
      }
      acc[quay].push(item)
      return acc
    }, {})

    monTheoQuayMap.value = grouped

    gioHang.value = []
    await xuLyHoaDon(0, 0)
    await nextTick()

    // Lần lượt gửi lệnh in phách K80 cho từng quầy
    const danhSachQuay = Object.keys(grouped)

    for (const quay of danhSachQuay) {
      const elementId = `vung-phieu-in-${quay.replace(/\s+/g, '-')}`

      printJS({
        printable: elementId,
        type: 'html',
        header: undefined,
        targetStyles: ['*'],
        style: `
          @page { size: 80mm auto; margin: 0; }
          body { margin: 0; padding: 0; }
          .phieu-in-bep { 
            font-family: 'Courier New', Courier, monospace; 
            color: #000000; width: 72mm; margin: 0 auto; padding: 6mm 0; box-sizing: border-box;
          }
          .phieu-header { text-align: center; }
          .phieu-header h2 { margin: 0 0 6px 0; font-size: 18px; font-weight: bold; letter-spacing: 0.5px; }
          .phieu-header h3 { margin: 0 0 10px 0; font-size: 16px; text-transform: uppercase; }
          .phieu-header p { margin: 3px 0; font-size: 13px; text-align: left; }
          .dash-line { border: none; border-top: 1px dashed #000000 !important; margin: 10px 0; height: 0; width: 100%; }
          .phieu-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          .phieu-table th { border-bottom: 2px solid #000000 !important; font-size: 14px; font-weight: bold; padding-bottom: 6px; }
          .phieu-table td { padding: 8px 0; font-size: 15px; vertical-align: top; }
          .print-item-name { font-weight: bold; }
          .print-combo-sub { font-size: 12px; font-style: italic; padding-left: 10px; margin-top: 2px; }
          .phieu-footer { text-align: center; margin-top: 18px; font-size: 13px; font-style: italic; }
        `,
      })
      await delay(500)
    }

    tabGioHang.value = 'mon-dang-len'
  } catch (error) {
    console.error(error)
    alert('Gửi bếp thất bại')
  }
}

const taoHoaDon = async () => {
  if (!ensurePosUse()) return
  if (!ensureActiveShift()) return
  if (isSubmittingPayment.value) return
  if (danhSachMonPhucVu.value.length === 0 || tongThanhToan.value <= 0) {
    alert('Không thể tạo hóa đơn khi giỏ hàng trống hoặc tổng tiền bằng 0.')
    return
  }

  isSubmittingPayment.value = true

  try {
    await xuLyHoaDon(1, 1, 1)
    const paidBill = buildPaidShiftBill('cash')
    syncPaidBillToShift(paidBill)
    await finalizePaymentSuccess(paidBill.id)
    emit('quayLai')

    alert('Thanh toán thành công!')
  } catch {
    isSubmittingPayment.value = false
    alert('Thanh toán thất bại')
  }
}

watch(
  () => props.datBan,
  async (db) => {
    if (!db) {
      resetPaymentState()
      return
    }

    const reservationStatus = normalizeReservationStatus(db?.trangThai)
    if (!['DA_XAC_NHAN', 'DA_NHAN_BAN'].includes(reservationStatus)) {
      resetPaymentState()
      return
    }

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

        <!-- TAB: MÓN ĐÃ PHỤC VỤ XONG -->
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

      <!-- CỤM TÍNH TIỀN GIO-HANG-FOOTER -->
      <div class="gio-hang-footer">
        <hr />
        <button v-if="tabGioHang === 'goi-mon'" class="btn-luu-phu" @click="luuTam()">
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
            <option
              v-for="g in danhSachGiamGia"
              :key="g.idGiamGia"
              :value="g.idGiamGia"
              :disabled="!isDiscountEligible(g, tongTienTamTinhCotGiua)"
            >
              {{ g.maGiamGia }} - {{ formatDiscountValue(g) }}{{ !isDiscountEligible(g, tongTienTamTinhCotGiua) ? ' (không đủ điều kiện)' : '' }}
            </option>
          </select>
        </div>
        <button class="btn-thanh-toan" :disabled="isSubmittingPayment" @click="optionPay">
          {{ isSubmittingPayment ? 'Đang xử lý...' : 'Thanh toán' }}
        </button>
      </div>
    </div>

    <!-- VÙNG IN KHUẤT PHIẾU BÁO CHẾ BIẾN K80 (TỰ ĐỘNG LÊN ĐỒ THEO QUẦY BẾP / QUẦY BAR) -->
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
      :is-processing="isSubmittingPayment"
      @close="closePopup"
      @chonTienMat="popupTienMat"
      @chonChuyenKhoan="moPopupQR"
    />

    <PopupTienMat
      v-if="tienMatThanhToan"
      :tongTien="tongThanhToan"
      :is-processing="isSubmittingPayment"
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

/* --- CỘT GIỮA: DANH SÁCH MÓN --- */
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

/* --- CỘT PHẢI: GIỎ HÀNG --- */
.gio-hang-tabs {
  display: flex;
  background: #242424;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 12px;
  border: 1px solid rgba(212, 175, 55, 0.15);
  flex-shrink: 0;
}

.gio-hang-list-wrapper {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
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
