<script setup lang="ts">
import MonApi, { type Mon } from '@/api/MonApi'
import ComBoApi, { type Combo } from '@/api/ComBoApi'
import { onMounted, ref, computed, watch, nextTick } from 'vue'
import GiamGiaApi from '@/api/GiamGiaApi'
import PopupThanhToan from './PopupThanhToan.vue'
import PopupTienMat from './PopupTienMat.vue'
import PopupKetHopThanhToan from './PopupKetHopThanhToan.vue'
import PopupSePayQR from './PopupSePayQR.vue'
import HoaDonApi from '@/api/HoaDonApi.ts'
import HoaDonChiTietApi from '@/api/HoaDonChiTietApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import BanApi from '@/api/BanApi'
import { useShiftStore } from '@/stores/ShiftStore'
import { useAuthStore } from '@/stores/AuthStore'
import { useOrderStore, type PendingOrderItem } from '@/stores/OrderStore'
import DanhMucApi from '@/api/DanhMucApi.ts'
import MayInApi from '@/api/MayInApi'
import SplitBillModal from './SplitBillModal.vue'

// ================= PROPS =================
const props = defineProps<{
  ban: any
  datBan: any | null
}>()

// ================= EMIT =================
const emit = defineEmits(['quayLai', 'payment-complete'])
const shiftStore = useShiftStore()
const authStore = useAuthStore()
const orderStore = useOrderStore()

const SHIFT_CLOSED_MESSAGE = 'Ca làm việc đã đóng, không thể thực hiện thao tác gọi món mới'

const isShiftClosed = computed(
  () => !shiftStore.currentShift || shiftStore.currentShift.isOpen === false,
)

const hasActiveUnpaidInvoice = computed(() => {
  const invoice = hoaDonHienTai.value
  if (!invoice) return false

  return Number(invoice.trangThaiThanhToan) === 0 && Number(invoice.trangThaiHoaDon) === 0
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

  const raw = String(value).trim()
  const normalized = String(value)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+/g, '')
    .replace(/%/g, 'PERCENT')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toUpperCase()

  if (
    raw.includes('%') ||
    normalized.includes('PERCENT') ||
    normalized.includes('PHANTRAM') ||
    (normalized.includes('PHAN') && normalized.includes('TRAM')) ||
    (normalized.includes('PH') && normalized.includes('TRAM'))
  )
    return 'PHANTRAM'
  if (
    normalized.includes('TIEN') ||
    normalized.includes('MAT') ||
    normalized.includes('GIATRI') ||
    normalized.includes('GIATR') ||
    normalized.includes('VALUE') ||
    normalized.includes('VND') ||
    normalized.includes('DONG') ||
    normalized.includes('FIXED') ||
    normalized.includes('CODINH') ||
    normalized.includes('CASH') ||
    normalized.includes('MONEY')
  )
    return 'TIEN'
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
    discount?.loaiGiamGia ??
    discount?.loai_giam ??
    discount?.loai_giam_gia ??
    discount?.hinhThucGiam ??
    discount?.hinh_thuc_giam ??
    discount?.discountType ??
    discount?.discount_type ??
    discount?.type ??
    ''

  const value = Number(getDiscountRawValue(discount) ?? 0)
  const maxDiscount = Number(getDiscountRawMax(discount) ?? 0)
  let type = normalizeDiscountType(rawDiscountType)

  if (!isPercentDiscountType(type) && !isFixedDiscountType(type)) {
    if (value > 0 && value <= 100 && maxDiscount > value) {
      type = 'PHANTRAM'
    } else if (value > 0) {
      type = 'TIEN'
    }
  }

  return {
    type,
    value,
    maxDiscount,
  }
}

const isPercentDiscountType = (type: string) =>
  type.includes('PHANTRAM') || type.includes('PERCENT') || type.includes('PERCENTAGE')

const isFixedDiscountType = (type: string) =>
  type.includes('TIEN') ||
  type.includes('MAT') ||
  type.includes('GIATRI') ||
  type.includes('GIATR') ||
  type.includes('VALUE') ||
  type.includes('VND') ||
  type.includes('DONG') ||
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

const parseMinimumOrderValue = (discount: any): number | null => {
  if (!discount) return null

  const rawCondition =
    discount?.dieuKienSuDung ??
    discount?.dieuKien ??
    discount?.minimumOrder ??
    discount?.minOrder ??
    discount?.condition ??
    ''

  if (rawCondition === null || rawCondition === undefined || rawCondition === '') {
    return null
  }

  const str = String(rawCondition).trim()
  if (!str) return null

  const kMatch = str.match(/(\d+(?:[\.,]\d+)?)\s*k/i)
  if (kMatch?.[1]) {
    const num = parseFloat(kMatch[1].replace(',', '.'))
    if (!isNaN(num)) return num * 1000
  }

  if (/^\d+$/.test(str)) {
    return Number(str)
  }

  const digitsOnly = str.replace(/[^0-9]/g, '')
  if (digitsOnly) {
    const numeric = Number(digitsOnly)
    if (Number.isFinite(numeric) && numeric > 0) {
      return numeric
    }
  }

  return null
}

const isDiscountActiveAndValid = (g: any): boolean => {
  if (!g) return false

  const trangThaiVal = g.trangThai
  const isHoatDong =
    trangThaiVal === 1 ||
    String(trangThaiVal) === '1' ||
    String(trangThaiVal).toUpperCase() === 'HOAT_DONG'
  if (!isHoatDong) return false

  const remainingQty = Number(g.soLuongMaGiamGia ?? g.soLuong ?? 0)
  if (remainingQty <= 0) return false

  if (g.ngayKetThuc) {
    const now = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
    const endDateStr = String(g.ngayKetThuc).split('T')[0] ?? ''
    if (endDateStr < todayStr) {
      return false
    }
  }

  return true
}

const danhSachGiamGiaKhaDung = computed(() => {
  return danhSachGiamGia.value.filter((g) => {
    if (giamGiaDangChon.value != null && Number(g.idGiamGia) === Number(giamGiaDangChon.value)) {
      return true
    }
    return isDiscountActiveAndValid(g)
  })
})

const isDiscountEligibleForSubtotal = (discount: any, subtotal: number) => {
  const requiredValue = parseMinimumOrderValue(discount)
  if (requiredValue == null) return true
  return Number(subtotal || 0) >= requiredValue
}

const getDiscountEligibilityMessage = (discount: any, subtotal: number) => {
  const requiredValue = parseMinimumOrderValue(discount)
  if (requiredValue == null) return ''

  return `Đơn hàng chưa đạt giá trị tối thiểu ${requiredValue.toLocaleString('vi-VN')}đ để sử dụng mã ${getDiscountCode(discount)}.`
}

const phuongThucThanhToan = ref(false)
const tienMatThanhToan = ref(false)
const ketHopThanhToan = ref(false)
const hienThiSePayQR = ref(false)
const hoaDonHienTai = ref<any>(null)
const hienThiTachHoaDon = ref(false)
const danhSachHoaDonBan = ref<any[]>([])

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
const searchQuery = ref('')

// Computed lọc danh sách Combo theo từ khóa
const danhSachComboFilter = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return danhSachCombo.value
  return danhSachCombo.value.filter((cb: any) =>
    (cb.tenCombo || '').toLowerCase().includes(query)
  )
})

// Computed lọc danh sách Món ăn theo từ khóa
const danhSachMonAnFilter = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return danhSachMonAn.value
  return danhSachMonAn.value.filter((mon: any) =>
    (mon.tenMon || '').toLowerCase().includes(query)
  )
})


const loadData = async () => {
  try {
    const [comboRes, monRes, danhMucRes] = await Promise.all([
      ComBoApi.hienThiComBo(),
      MonApi.hienThiMon(),
      DanhMucApi.getDanhSach(),
    ])

    const dsDanhMuc = danhMucRes.data || []
    const dsMonRaw = monRes.data || []

    danhSachMonAn.value = dsMonRaw
      .filter((m: Mon) => m.trangThai === 0)
      .map((m: any) => {
        const dm = dsDanhMuc.find((d: any) => d.idDanhMuc === m.idDanhMuc)
        return {
          ...m,
          quay: m.quay || m.danhMuc?.quay || dm?.quay || 'BEP',
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
const tangSoLuong = (item: any) => {
  const index = gioHang.value.findIndex(
    (x) =>
      x.loai === item.loai &&
      (item.loai === 'COMBO' ? x.idCombo === item.idCombo : x.idMon === item.idMon),
  )
  if (index === -1) return
  gioHang.value[index].soLuong++
}

const updateQuantity = (item: any, value: number) => {
  const index = gioHang.value.findIndex(
    (x) =>
      x.loai === item.loai &&
      (item.loai === 'COMBO' ? x.idCombo === item.idCombo : x.idMon === item.idMon),
  )
  if (index === -1) return
  const newQty = Number(value || 0)
  if (newQty <= 0) {
    gioHang.value.splice(index, 1)
  } else {
    gioHang.value[index].soLuong = newQty
  }
}

const showPaymentReview = ref(false)
const itemsToReview = computed(() =>
  danhSachMonPhucVu.value.filter((item: any) => Number(item.soLuong) > 0),
)

const proceedToPayment = () => {
  showPaymentReview.value = false
  phuongThucThanhToan.value = true
}

const closePaymentReview = () => {
  showPaymentReview.value = false
}

const moPopupTachHoaDon = () => {
  if (blockShiftClosedAction()) return
  if (!hoaDonHienTai.value?.idHoaDon || danhSachMonPhucVu.value.length === 0) {
    alert('Bàn chưa có hóa đơn và món để tách.')
    return
  }
  hienThiTachHoaDon.value = true
}

const mapInvoiceItems = (invoice: any): any[] => (invoice?.chiTiet || []).map((item: any) => ({
  idHoaDonChiTiet: item.idHoaDonChiTiet,
  idMon: item.idMon,
  idCombo: item.idCombo,
  tenMon: item.tenMon,
  tenCombo: item.tenCombo,
  tenQuay: item.tenQuay || 'Quầy Bếp',
  gia: Number(item.giaBanTaiThoiDiem ?? item.donGiaHienTai ?? item.giaCombo ?? 0),
  soLuong: Number(item.soLuong || 0),
  daLen: Number(item.daLen || 0),
  loai: item.idMon ? 'MON' : 'COMBO',
  comboItems: item.comboItems ?? [],
  orderedBy: item.orderedBy,
  orderedAt: item.orderedAt,
}))

const syncInvoiceStore = () => {
  if (!props.ban?.idBan) return
  const invoiceOrders = Object.fromEntries(
    danhSachHoaDonBan.value.map((invoice) => [invoice.idHoaDon, mapInvoiceItems(invoice)]),
  ) as Record<number, PendingOrderItem[]>
  orderStore.setInvoiceOrders(props.ban.idBan, invoiceOrders, hoaDonHienTai.value?.idHoaDon || null)
}

const chonHoaDon = (invoice: any) => {
  if (!invoice?.idHoaDon) return
  hoaDonHienTai.value = invoice
  danhSachMonPhucVu.value = mapInvoiceItems(invoice)
  gioHang.value = []
  giamGiaDangChon.value = invoice.idGiamGia ?? null
  tabGioHang.value = 'goi-mon'
  orderStore.setActiveInvoice(props.ban.idBan, invoice.idHoaDon)
}

const taiLaiHoaDonBan = async (activeInvoiceId?: number) => {
  const response = await HoaDonApi.getDanhSach()
  danhSachHoaDonBan.value = (response.data || []).filter(
    (invoice: any) => Number(invoice.idBan) === Number(props.ban.idBan)
      && Number(invoice.trangThaiThanhToan) === 0
      && Number(invoice.trangThaiHoaDon) === 0,
  )
  syncInvoiceStore()
  const activeInvoice = danhSachHoaDonBan.value.find((invoice) => Number(invoice.idHoaDon) === Number(activeInvoiceId))
    || danhSachHoaDonBan.value[0]
  if (activeInvoice) chonHoaDon(activeInvoice)
}

const xuLyTachHoaDonThanhCong = async (invoice: any) => {
  hienThiTachHoaDon.value = false
  await taiLaiHoaDonBan(invoice?.idHoaDon)
  alert('Tách hóa đơn thành công.')
}

// ================= XỬ LÝ LÊN MÓN =================
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
  showPaymentReview.value = true
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

const openMixedPaymentPopup = () => {
  phuongThucThanhToan.value = false
  ketHopThanhToan.value = true
}

const closeMixedPaymentPopup = () => {
  ketHopThanhToan.value = false
}

const moPopupQR = async () => {
  if (blockShiftClosedAction()) return

  await xuLyHoaDon(0, 0)
  phuongThucThanhToan.value = false
  hienThiSePayQR.value = true
}

const mixedPaymentContext = ref<{ tienChuyenKhoan: number; tienTienMat: number } | null>(null)
const mixedPaymentCode = ref('')

const handleChuyenKhoanThanhCong = async () => {
  if (blockShiftClosedAction()) return

  try {
    hienThiSePayQR.value = false
    await xuLyHoaDon(1, 1, 2, { tienChuyenKhoan: Number(tongThanhToan.value || 0), tienTienMat: 0 })
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

const handleMixedPaymentConfirm = async (payload: { tienChuyenKhoan: number; tienTienMat: number }) => {
  ketHopThanhToan.value = false

  const totalChuyenKhoan = Number(payload.tienChuyenKhoan || 0)
  const totalTienMat = Number(payload.tienTienMat || 0)

  if (totalChuyenKhoan === 0) {
    await taoHoaDonKetHop({ tienChuyenKhoan: 0, tienTienMat: totalTienMat })
    return
  }

  mixedPaymentContext.value = { tienChuyenKhoan: totalChuyenKhoan, tienTienMat: totalTienMat }
  mixedPaymentCode.value = hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`
  hienThiSePayQR.value = true
}

const taoHoaDonKetHop = async ({ tienChuyenKhoan, tienTienMat }: { tienChuyenKhoan: number; tienTienMat: number }) => {
  if (blockShiftClosedAction()) return

  const totalChuyenKhoan = Number(tienChuyenKhoan || 0)
  const totalTienMat = Number(tienTienMat || 0)
  const totalSplit = totalChuyenKhoan + totalTienMat

  if (totalSplit !== Number(tongThanhToan.value || 0)) {
    alert('Tổng số tiền chuyển khoản + tiền mặt phải bằng tổng thanh toán.')
    return
  }

  try {
    const finalCode = mixedPaymentCode.value || hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`
    await xuLyHoaDon(1, 1, 4, { tienChuyenKhoan: totalChuyenKhoan, tienTienMat: totalTienMat }, finalCode)
    await markReservationCompleted()
    notifyPaymentComplete()
    emit('quayLai')

    if (shiftStore.currentShift?.isOpen) {
      shiftStore.syncBillFromPos({
        id: String(hoaDonHienTai.value?.idHoaDon || Date.now()),
        code: finalCode,
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

    alert('Thanh toán kết hợp thành công!')
    hoaDonHienTai.value = null
    gioHang.value = []
    danhSachMonPhucVu.value = []
    giamGiaDangChon.value = null
    mixedPaymentContext.value = null
    mixedPaymentCode.value = ''
    ketHopThanhToan.value = false
    hienThiSePayQR.value = false
  } catch {
    alert('Thanh toán kết hợp thất bại')
  }
}

// ================= COMPUTED CHI PHÍ =================
const tongTienTamTinhCotGiua = computed(() =>
  danhSachMonPhucVu.value.reduce((tong, item) => tong + item.gia * item.soLuong, 0),
)

const selectedDiscount = computed(() => {
  if (!giamGiaDangChon.value) return null

  const selected =
    danhSachGiamGia.value.find((g) => Number(g.idGiamGia) === Number(giamGiaDangChon.value)) ?? null

  if (!selected) return null

  const subtotal = Number(tongTienTamTinhCotGiua.value || 0)
  return isDiscountEligibleForSubtotal(selected, subtotal) ? selected : null
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
    const fixedDiscount = Math.min(cleanValue, subtotal)
    return fixedDiscount
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
const initializeReservationItems = (db: any) => {
  if (!db?.idDatBan) return false

  const reservationItems = buildReservationItems(db)
  if (!reservationItems.length) {
    return false
  }

  gioHang.value = []
  danhSachMonPhucVu.value = reservationItems
  tabGioHang.value = 'mon-da-goi'
  hoaDonHienTai.value = null
  giamGiaDangChon.value = null
  return true
}

const checkHoaDonTam = async () => {
  await loadGiamGia()
  if (isDataLoaded.value) return
  try {
    const allInvoices = await HoaDonApi.getDanhSach()
    danhSachHoaDonBan.value = (allInvoices.data || []).filter(
      (invoice: any) => Number(invoice.idBan) === Number(props.ban.idBan)
        && Number(invoice.trangThaiThanhToan) === 0
        && Number(invoice.trangThaiHoaDon) === 0,
    )
    const hd = danhSachHoaDonBan.value[0]

    if (hd) {
      syncInvoiceStore()
      chonHoaDon(hd)
      isDataLoaded.value = true
      return
    }

    if (initializeReservationItems(props.datBan)) {
      isDataLoaded.value = true
      return
    }

    isDataLoaded.value = true
  } catch {
    console.log('Không có hóa đơn tạm')
    if (initializeReservationItems(props.datBan)) {
      isDataLoaded.value = true
      return
    }
    isDataLoaded.value = true
  }
}

const saveChiTietHoaDon = async (idHoaDon: number) => {
  for (const [index, item] of danhSachMonPhucVu.value.entries()) {
    const gia = Number(item.gia ?? 0)
    const soLuong = Number(item.soLuong || 0)
    const daLen = Number(item.daLen || 0)

    const trangThaiMonAn =
      daLen >= soLuong && soLuong > 0
        ? 'DA_LEN'
        : 'DANG_LEN'

    await HoaDonChiTietApi.add({
      maHoaDonChiTiet: `HDCT${idHoaDon}${String(index + 1).padStart(2, '0')}`,
      idHoaDon,

      idMon: item.idMon ?? null,
      idCombo: item.idCombo ?? null,

      soLuong,

      giaBanTaiThoiDiem: gia,

      tienGiamGiaMon: 0,

      thanhTien: gia * soLuong,

      trangThaiMonAn,
      daLen,

      orderedBy: item.orderedBy || getCurrentOperatorName(),
      orderedAt: item.orderedAt || getLocalDateTimeNow(),
    } as any)
  }
}

const capNhatDatabaseNoRebuild = async () => {
  if (!hoaDonHienTai.value?.idHoaDon) return

  await HoaDonChiTietApi.deleteByHoaDon(
    hoaDonHienTai.value.idHoaDon
  )

  await saveChiTietHoaDon(
    hoaDonHienTai.value.idHoaDon
  )
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

const buildChiTietPayload = () => {
  const itemMap = new Map<string, any>()

  // 1. Lấy tất cả món đã gửi bếp / đã lên bàn
  danhSachMonPhucVu.value.forEach((item: any) => {
    const key = item.idMon ? `MON_${item.idMon}` : `COMBO_${item.idCombo}`
    itemMap.set(key, {
      ...item,
      soLuong: Number(item.soLuong || 0),
      daLen: Number(item.daLen || 0),
    })
  })

  // 2. Gom thêm các món mới đang nằm trong Giỏ hàng (chưa gửi bếp)
  gioHang.value.forEach((item: any) => {
    const key = item.idMon ? `MON_${item.idMon}` : `COMBO_${item.idCombo}`
    const qty = Number(item.soLuong || 0)

    if (itemMap.has(key)) {
      const existing = itemMap.get(key)
      existing.soLuong += qty
    } else {
      itemMap.set(key, {
        ...item,
        soLuong: qty,
        daLen: 0,
      })
    }
  })

  // 3. Tạo payload gửi xuống API
  return Array.from(itemMap.values()).map((item: any, index: number) => {
    const soLuong = Number(item.soLuong || 0)
    const daLen = Number(item.daLen || 0)

    const trangThaiMonAn =
      daLen >= soLuong && soLuong > 0
        ? 'DA_LEN'
        : 'DANG_LEN'

    return {
      maHoaDonChiTiet: `HDCT${Date.now()}${index + 1}${item.idMon || item.idCombo || ''}`,
      idMon: item.idMon ?? null,
      idCombo: item.idCombo ?? null,
      soLuong,
      daLen,
      trangThaiMonAn,
      giaBanTaiThoiDiem: Number(item.gia || 0),
      tienGiamGiaMon: 0,
      thanhTien: Number(item.gia || 0) * soLuong,
      orderedBy: item.orderedBy || getCurrentOperatorName(),
      orderedAt: item.orderedAt || getLocalDateTimeNow(),
    }
  })
}

const xuLyHoaDon = async (
  trangThaiHoaDon: number,
  trangThaiThanhToan: number,
  paymentMethodNumber: number | null = null,
  splitPayment: { tienChuyenKhoan?: number; tienTienMat?: number } = {},
  orderCodeOverride?: string,
) => {
  const currentOperator = getCurrentOperatorName()
  tenNhanVien.value = currentOperator

  const normalizedPaymentMethod = paymentMethodNumber ?? (trangThaiThanhToan === 1 ? 1 : null)
  const chiTietPayload = buildChiTietPayload()
  const finalOrderCode = orderCodeOverride || hoaDonHienTai.value?.maHoaDon || `HD${Date.now()}`

  const payload = {
    maHoaDon: finalOrderCode,
    maGiaoDich: `TX${Date.now()}`,
    trangThaiHoaDon,
    trangThaiThanhToan,
    phuongThucThanhToan: normalizedPaymentMethod,
    soTienChuyenKhoan: Number(splitPayment.tienChuyenKhoan ?? 0),
    soTienTienMat: Number(splitPayment.tienTienMat ?? 0),
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


// HÀM XỬ LÝ KHI BẤM NÚT LƯU
const luuHoaDonTam = async () => {
  if (blockShiftClosedAction()) return

  try {
    const isFirstTime = !hoaDonHienTai.value
    // Gọi API lưu/cập nhật hóa đơn với số tiền thanh toán = 0, tiền khách đưa = 0
    await xuLyHoaDon(0, 0)
    
    if (isFirstTime) {
      alert('Tạo hóa đơn tạm thành công!')
    } else {
      alert('Cập nhật hóa đơn thành công!')
    }
  } catch (error) {
    console.error('Lỗi khi lưu hóa đơn:', error)
    alert('Lưu hóa đơn thất bại!')
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

  if (reservationId) {
    try {
      await DatBanQuanLyApi.hoanThanh(reservationId)
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
      const trungMon = danhSachMonPhucVu.value.find(
        (p: any) =>
          p.loai === cartItem.loai &&
          (cartItem.loai === 'MON' ? p.idMon === cartItem.idMon : p.idCombo === cartItem.idCombo),
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
      }),
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
      return
    }

    const selected =
      danhSachGiamGia.value.find((g) => Number(g.idGiamGia) === Number(selectedId)) ?? null

    if (!selected) {
      giamGiaDangChon.value = null
      return
    }

    const subtotal = Number(tongTienTamTinhCotGiua.value || 0)
    if (!isDiscountEligibleForSubtotal(selected, subtotal)) {
      const message = getDiscountEligibilityMessage(selected, subtotal)
      alert(message)
      giamGiaDangChon.value = null
    }
  },
  { immediate: true },
)

watch(
  () => tongTienTamTinhCotGiua.value,
  (subtotal) => {
    if (subtotal <= 0 && giamGiaDangChon.value != null) {
      giamGiaDangChon.value = null
      return
    }

    if (giamGiaDangChon.value != null) {
      const selected =
        danhSachGiamGia.value.find((g) => Number(g.idGiamGia) === Number(giamGiaDangChon.value)) ?? null

      if (selected && !isDiscountEligibleForSubtotal(selected, Number(subtotal || 0))) {
        alert(getDiscountEligibilityMessage(selected, Number(subtotal || 0)))
        giamGiaDangChon.value = null
      }
    }
  },
)

watch(
  () => props.datBan,
  async (db) => {
    if (!db || !isDataLoaded.value) return

    const reservationItems = buildReservationItems(db)

    if (reservationItems.length > 0) {
      gioHang.value = []
      danhSachMonPhucVu.value = reservationItems
      tabGioHang.value = 'goi-mon'
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

watch(
  () => props.ban?.idBan,
  async (newBanId) => {
    if (newBanId) {
      await loadGiamGia()
    }
  },
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
     <div class="action-bottom-group">
  <button 
    class="btn-luu-tam" 
    :disabled="isShiftClosedForUi" 
    @click="luuHoaDonTam"
  >
    💾 Lưu
  </button>
  
  <button class="btn-quay-lai" @click="quayLai">Quay Lại</button>
</div>
    </div>

    <!-- CỘT DANH SÁCH MÓN GIỮA -->
    <div class="danh-sach-mon">
      <div class="header-danh-sach">
    <div class="title">
      {{ danhMucDangChon === 'combo' ? 'Danh sách Combo' : 'Danh sách Món ăn' }}
    </div>
    <div class="search-box">
      <input
        v-model="searchQuery"
        type="text"
        :placeholder="danhMucDangChon === 'combo' ? 'Tìm combo...' : 'Tìm món ăn...'"
        class="search-input"
      />
      <button v-if="searchQuery" class="btn-clear-search" @click="searchQuery = ''">✕</button>
    </div>
  </div>

  <div class="food-grid">
    <template v-if="danhMucDangChon === 'combo'">
      <div
        v-for="combo in danhSachComboFilter"
        :key="combo.idCombo"
        class="food-card"
        :class="combo.trangThaiBan === 1 ? 'con-hang' : 'het-hang'"
        :style="isShiftClosedForUi ? { opacity: 0.55, cursor: 'not-allowed' } : null"
        @click="!isShiftClosedForUi && themVaoGio(combo, 'COMBO')"
      >
        {{ combo.tenCombo }}
      </div>
      <div v-if="danhSachComboFilter.length === 0" class="empty-search">
        Không tìm thấy combo nào phù hợp
      </div>
    </template>
    <template v-else>
      <div
        v-for="mon in danhSachMonAnFilter"
        :key="mon.idMon"
        class="food-card"
        :class="mon.trangThaiBan === 1 ? 'con-hang' : 'het-hang'"
        :style="isShiftClosedForUi ? { opacity: 0.55, cursor: 'not-allowed' } : null"
        @click="!isShiftClosedForUi && themVaoGio(mon, 'MON')"
      >
        {{ mon.tenMon }}
      </div>
      <div v-if="danhSachMonAnFilter.length === 0" class="empty-search">
        Không tìm thấy món ăn nào phù hợp
      </div>
    </template>
  </div>
    </div>

    <!-- CỘT GIỎ HÀNG PHẢI -->
    <div class="gio-hang">
      <div class="title">Giỏ hàng bàn {{ props.ban.tenBan }} - MãHD: {{ hoaDonHienTai?.maHoaDon }}</div>
      <div v-if="danhSachHoaDonBan.length > 1" class="invoice-switcher">
        <button
          v-for="invoice in danhSachHoaDonBan"
          :key="invoice.idHoaDon"
          type="button"
          :class="{ active: Number(invoice.idHoaDon) === Number(hoaDonHienTai?.idHoaDon) }"
          @click="chonHoaDon(invoice)"
        >
          {{ invoice.maHoaDon || `HD${invoice.idHoaDon}` }}
        </button>
      </div>
      <div v-if="props.datBan" class="reservation-status-pill">
        {{
          normalizeReservationStatus(props.datBan?.trangThai) === 'DA_XAC_NHAN'
            ? ''
            : ''
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
            <div class="qty-control">
              <button class="btn-minus" @click="giamSoLuong(item)">-</button>
              <span class="qty-display">{{ item.soLuong }}</span>
              <button class="btn-plus" @click="tangSoLuong(item)">+</button>
            </div>
            <div class="item-info">
              <div class="item-name">{{ itemName(item) }}</div>
              <div v-if="item.comboItems?.length" class="mon-combo">
                Gồm: {{ item.comboItems.join(', ') }}
              </div>
              <div class="item-bottom">
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
            <button
              class="btn-xac-nhan-all"
              :disabled="isShiftClosedForUi"
              @click="xacNhanTatCaMon"
            >
              ✓ Xác nhận tất cả lên đồ
            </button>
          </div>
          <div
            v-if="!danhSachMonPhucVu.some((i) => Number(i.soLuong) - Number(i.daLen || 0) > 0)"
            class="empty-cart"
          >
            Không có món đang chờ.
          </div>
          <div
            v-for="item in danhSachMonPhucVu.filter(
              (i) => Number(i.soLuong) - Number(i.daLen || 0) > 0,
            )"
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
                  Giá:
                  {{
                    ((Number(item.soLuong) - Number(item.daLen || 0)) * item.gia).toLocaleString(
                      'vi-VN',
                    )
                  }}
                  đ
                </div>
              </div>
            </div>
            <button
              class="btn-check-item"
              :disabled="isShiftClosedForUi"
              @click="xacNhanTungMon(item)"
            >
              ✓ Lên
            </button>
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
        <button
          v-if="tabGioHang === 'goi-mon'"
          class="btn-luu-phu"
          :disabled="isShiftClosedForUi"
          @click="luuTam()"
        >
          🔥 Xác nhận gửi vào bếp / Bar
        </button>

        <div class="tong-tien">
          Tạm tính: {{ tongTienTamTinhCotGiua.toLocaleString('vi-VN') }} đ
        </div>
        <div class="tong-tien">Tiền giảm giá: {{ tienGiamGia.toLocaleString('vi-VN') }} đ</div>
        <div v-if="depositDaCoc > 0" class="tong-tien">
          Tiền cọc đã trừ: {{ depositDaCoc.toLocaleString('vi-VN') }} đ
        </div>
        <button
          v-if="hoaDonHienTai?.idHoaDon"
          class="btn-tach-hoa-don"
          :disabled="isShiftClosedForUi"
          @click="moPopupTachHoaDon"
        >
          Tách hóa đơn
        </button>
        <div>
          <select class="discount-input" v-model="giamGiaDangChon" @focus="loadGiamGia" @click="loadGiamGia">
            <option :value="null">Chọn mã giảm giá</option>
            <option
              v-for="g in danhSachGiamGiaKhaDung"
              :key="g.idGiamGia"
              :value="g.idGiamGia"
              :disabled="!isDiscountEligibleForSubtotal(g, Number(tongTienTamTinhCotGiua || 0))"
              :title="
                isDiscountEligibleForSubtotal(g, Number(tongTienTamTinhCotGiua || 0))
                  ? getDiscountDisplayText(g)
                  : getDiscountEligibilityMessage(g, Number(tongTienTamTinhCotGiua || 0))
              "
            >
              {{ getDiscountDisplayText(g) }}
              <template v-if="!isDiscountEligibleForSubtotal(g, Number(tongTienTamTinhCotGiua || 0))">
                (Chưa đủ điều kiện)
              </template>
            </option>
          </select>
        </div>
        <button class="btn-thanh-toan pay-button" :disabled="isShiftClosedForUi" @click="optionPay">
          Thanh toán • {{ tongThanhToan.toLocaleString('vi-VN') }} đ
        </button>
      </div>
    </div>

    <div v-if="showPaymentReview" class="payment-review-overlay">
      <div class="payment-review-dialog">
        <div class="review-header">
          <div class="review-title">Xác nhận thanh toán</div>
          <div class="review-subtitle">
            Vui lòng kiểm tra lại các món chuẩn bị thanh toán trước khi tiếp tục.
          </div>
        </div>
        <div v-if="itemsToReview.length === 0" class="empty-cart">
          Không có món nào để thanh toán.
        </div>
        <div v-else class="payment-review-list">
          <div
            v-for="item in itemsToReview"
            :key="`review-${item.loai}-${item.idMon ?? item.idCombo}`"
            class="review-item"
          >
            <div class="review-name">{{ itemName(item) }}</div>
            <div class="review-detail">
              x{{ item.soLuong }} · {{ ((item.gia ?? 0) * item.soLuong).toLocaleString('vi-VN') }} đ
            </div>
          </div>
          <div class="review-total">
            Tổng thanh toán: {{ tongThanhToan.toLocaleString('vi-VN') }} đ
          </div>
        </div>
        <div class="review-actions">
          <button class="btn-primary" @click="proceedToPayment">Tiếp tục thanh toán</button>
          <button class="btn-secondary" @click="closePaymentReview">Hủy</button>
        </div>
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
      @chonKetHop="openMixedPaymentPopup"
    />

    <PopupTienMat
      v-if="tienMatThanhToan"
      :tongTien="tongThanhToan"
      @close="closeTienMatPopup"
      @xacNhan="taoHoaDon"
    />

    <PopupKetHopThanhToan
      v-if="ketHopThanhToan"
      :tongTien="tongThanhToan"
      @close="closeMixedPaymentPopup"
      @xacNhan="handleMixedPaymentConfirm"
    />

    <PopupSePayQR
      v-if="hienThiSePayQR"
      :show="hienThiSePayQR"
      :idHoaDon="hoaDonHienTai?.idHoaDon || 0"
      :maHoaDon="mixedPaymentCode || hoaDonHienTai?.maHoaDon || `HD${Date.now()}`"
      :tongTien="mixedPaymentContext?.tienChuyenKhoan ?? tongThanhToan"
      :amount="mixedPaymentContext?.tienChuyenKhoan ?? tongThanhToan"
      :description="mixedPaymentCode || hoaDonHienTai?.maHoaDon || `HD${Date.now()}`"
      :tenBan="props.ban?.tenBan"
      @close="hienThiSePayQR = false"
      @payment-success="mixedPaymentContext ? taoHoaDonKetHop(mixedPaymentContext) : handleChuyenKhoanThanhCong()"
    />

    <SplitBillModal
      v-if="hienThiTachHoaDon"
      :visible="hienThiTachHoaDon"
      :idHoaDon="hoaDonHienTai?.idHoaDon || 0"
      :items="danhSachMonPhucVu"
      @close="hienThiTachHoaDon = false"
      @success="xuLyTachHoaDonThanhCong"
    />
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

/* =========================================================
   LAYOUT CHÍNH
   ========================================================= */

.thanh-toan-container {
  display: grid;
  grid-template-columns:
    minmax(220px, 280px)
    minmax(360px, 1fr)
    minmax(320px, 380px);

  gap: 20px;
  width: 100%;
  height: 100vh;
  min-height: 0;

  padding: 24px;
  overflow: hidden;

  background: linear-gradient(180deg, #1b120a, #2c1f16);
}

.danh-muc,
.danh-sach-mon,
.gio-hang {
  background: linear-gradient(180deg, #2d241d, #1f1914);

  border-radius: 24px;
  padding: 20px;

  border: 1px solid rgba(255, 210, 130, 0.15);

  box-shadow:
    0 12px 30px rgba(0, 0, 0, 0.35),
    0 0 18px rgba(255, 182, 102, 0.06);

  min-height: 280px;

  display: flex;
  flex-direction: column;

  overflow: hidden;
}

.danh-muc,
.danh-sach-mon {
  min-height: 0;
}

.danh-muc {
  display: flex;
  flex-direction: column;
  min-height: 0;
}
/* =========================================================
   GIỎ HÀNG
   ========================================================= */

.gio-hang {
  min-height: 0;
  height: 100%;

  position: relative;

  display: flex;
  flex-direction: column;


  gap: 0;

  padding: 22px;

  border: 1px solid rgba(255, 255, 255, 0.14);

  background:
    radial-gradient(
      circle at top right,
      rgba(255, 255, 255, 0.14),
      transparent 30%
    ),
    radial-gradient(
      circle at bottom left,
      rgba(138, 43, 226, 0.16),
      transparent 18%
    ),
    linear-gradient(180deg, #20162f 0%, #120812 100%);

  box-shadow:
    0 18px 46px rgba(92, 35, 148, 0.22),
    inset 0 0 40px rgba(255, 255, 255, 0.05);

  border-radius: 28px;

  overflow: hidden;
}

/* Hiệu ứng nền */
.gio-hang::before {
  content: '';

  position: absolute;
  inset: 0;

  pointer-events: none;

  background-image:
    radial-gradient(
      circle at 20% 20%,
      rgba(255, 255, 255, 0.18),
      transparent 10%
    ),
    radial-gradient(
      circle at 80% 10%,
      rgba(174, 112, 255, 0.22),
      transparent 8%
    ),
    radial-gradient(
      circle at 50% 80%,
      rgba(230, 150, 255, 0.12),
      transparent 12%
    );

  opacity: 0.9;
  mix-blend-mode: screen;

  z-index: 0;
}


.gio-hang > * {
  position: relative;
  z-index: 1;
}

/* =========================================================
   TIÊU ĐỀ GIỎ HÀNG
   ========================================================= */

.gio-hang > .title {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.invoice-switcher {
  display: flex;
  gap: 8px;
  margin: -2px 0 12px;
  overflow-x: auto;
}

.invoice-switcher button {
  flex: 0 0 auto;
  padding: 7px 12px;
  border: 1px solid #dbcdbb;
  border-radius: 6px;
  background: #fffaf2;
  color: #756044;
  cursor: pointer;
  font-weight: 600;
}

.invoice-switcher button.active {
  border-color: #a85f2b;
  background: #a85f2b;
  color: #fff;
}

/* =========================================================
   TAB GIỎ HÀNG
   ========================================================= */

.gio-hang-tabs {
  display: flex;

  flex-shrink: 0;

  flex-wrap: nowrap;

  gap: 10px;

  background: rgba(255, 255, 255, 0.08);

  border-radius: 16px;

  padding: 8px;

  margin-bottom: 12px;

  border: 1px solid rgba(179, 117, 255, 0.16);

  overflow-x: auto;

  scroll-behavior: smooth;
}

.gio-hang-tabs .tab-item {
  flex: 1 1 0;

  min-width: 0;

  white-space: nowrap;
}

/* =========================================================
   KHUNG DANH SÁCH MÓN
   ========================================================= */



.gio-hang-tab-content {
  flex: 1 1 auto;

  min-height: 0;

  width: 100%;

  display: flex;
  flex-direction: column;

  overflow: hidden;

  border: 1px solid rgba(179, 117, 255, 0.25);

  border-radius: 14px;

  background: rgba(20, 10, 30, 0.35);


}


.gio-hang-list {
  flex: 1 1 auto;

  min-height: 0;

  width: 100%;

  overflow-y: auto;
  overflow-x: hidden;

  padding: 8px 6px 10px 8px;

  box-sizing: border-box;
}

/*
 * Thanh cuộn danh sách món
 */
.gio-hang-list::-webkit-scrollbar {
  width: 6px;
}

.gio-hang-list::-webkit-scrollbar-track {
  background: transparent;
}

.gio-hang-list::-webkit-scrollbar-thumb {
  background: rgba(255, 216, 107, 0.3);

  border-radius: 10px;
}

.gio-hang-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 216, 107, 0.5);
}

/* =========================================================
   ACTION BOTTOM
   ========================================================= */

.action-bottom-group {
  flex-shrink: 0;

  margin-top: auto;

  padding-top: 12px;
}


.gio-hang-footer {
  position: static !important;

  flex: 0 0 auto;

  width: 100%;

  z-index: 50;

  display: flex;
  flex-direction: column;

  gap: 8px;

  margin: 0 !important;

  padding: 12px 0 0;

  background: linear-gradient(
    180deg,
    rgba(18, 8, 18, 0.15) 0%,
    rgba(18, 8, 18, 0.92) 25%,
    #120812 100%
  );
}

/* Đường kẻ đầu footer */
.gio-hang-footer > hr {
  width: 100%;

  margin: 0 0 4px;

  border: 0;

  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

/* =========================================================
   DANH SÁCH MÓN GIỮA
   ========================================================= */

.food-grid {
  display: grid;

  grid-template-columns: repeat(3, minmax(0, 1fr));

  gap: 14px;

  flex: 1;

  min-height: 0;

  overflow-y: auto;
  overflow-x: hidden;

  padding-right: 6px;
  padding-bottom: 15px;

  align-content: start;
}

.food-grid::-webkit-scrollbar {
  width: 6px;
}

.food-grid::-webkit-scrollbar-track {
  background: transparent;
}

.food-grid::-webkit-scrollbar-thumb {
  background: rgba(255, 216, 107, 0.3);

  border-radius: 10px;
}

/* =========================================================
   RESPONSIVE
   ========================================================= */

@media (max-width: 1380px) {
  .thanh-toan-container {
    grid-template-columns:
      minmax(220px, 1fr)
      minmax(320px, 1fr);
  }
}

@media (max-width: 980px) {
  .thanh-toan-container {
    grid-template-columns: 1fr;

    height: auto;
    min-height: 100vh;

    overflow-y: auto;
  }

  .food-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .gio-hang {
    height: 700px;
    min-height: 700px;
  }
}

@media (max-width: 640px) {
  .thanh-toan-container {
    grid-template-columns: 1fr;

    height: auto;
    min-height: 100vh;

    padding: 12px;

    overflow-y: auto;
  }

  .food-grid {
    grid-template-columns: 1fr;
  }

  .gio-hang {
    height: 700px;
    min-height: 700px;

    padding: 16px;
  }

  .gio-hang > h2,
  .gio-hang > .gio-hang-header {
    flex-shrink: 0;
  }

  .gio-hang-tabs .tab-item {
    flex: 1 1 0;

    min-width: 0;

    white-space: nowrap;
  }

  .gio-hang-footer .discount-input {
    margin-bottom: 0;
  }

  .gio-hang-footer .tong-tien {
    text-align: left;
  }

  .gio-hang-footer .btn-thanh-toan {
    margin-top: 8px;
  }
}

/* =========================================================
   NÚT QUAY LẠI
   ========================================================= */

.btn-quay-lai {
  width: 100%;

  margin-top: auto;

  flex-shrink: 0;

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

/* =========================================================
   NÚT LƯU
   ========================================================= */
/* Style cho nút Lưu đồng bộ kích thước và giao diện với nút Quay Lại */
.btn-luu-tam {
  width: 100%;
  padding: 14px 16px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  /* Màu nền tone xanh lá đậm hợp tông với các nút hệ thống */
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  color: #ffffff;
  border: 1px solid rgba(76, 175, 80, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-luu-tam:hover {
  background: linear-gradient(135deg, #388e3c 0%, #2e7d32 100%);
  border-color: #81c784;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(46, 125, 50, 0.35);
}

.btn-luu-tam:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.btn-luu-tam:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Giúp 2 nút xếp chồng cách nhau đẹp mắt */
.action-bottom-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  margin-top: auto;
}
/* =========================================================
   DANH MỤC
   ========================================================= */

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

/* =========================================================
   FOOD CARD
   ========================================================= */

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

/* =========================================================
   TAB
   ========================================================= */

.tab-item {
  flex: 1;

  text-align: center;

  padding: 12px 10px;

  font-size: 13px;

  font-weight: 600;

  color: #d8c4ff;

  cursor: pointer;

  border-radius: 14px;

  transition: all 0.25s ease;
}

.tab-item.active {
  background:
    linear-gradient(
      135deg,
      rgba(165, 92, 255, 0.24),
      rgba(87, 23, 255, 0.32)
    );

  color: #ffffff;

  font-weight: 700;

  box-shadow: 0 0 20px rgba(127, 76, 255, 0.28);
}

.tab-item:hover {
  color: #ffffff;

  background: rgba(255, 255, 255, 0.04);
}

/* =========================================================
   TỔNG TIỀN
   ========================================================= */

.tong-tien {
  background:
    linear-gradient(
      135deg,
      rgba(138, 92, 255, 0.16),
      rgba(77, 29, 156, 0.08)
    );

  border: 1px solid rgba(171, 130, 255, 0.24);

  border-radius: 14px;

  padding: 12px;

  color: #e7d8ff;

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

/* =========================================================
   CART ITEM
   ========================================================= */

.cart-item {
  display: grid;

  grid-template-columns: 74px minmax(0, 1fr);

  gap: 22px;

  padding: 20px;

  background:
    linear-gradient(
      145deg,
      rgba(35, 10, 50, 0.96),
      rgba(15, 6, 30, 0.98)
    );

  color: #f9f4ff;

  border-radius: 22px;

  margin-bottom: 16px;

  border-left: 4px solid rgba(190, 160, 255, 0.85);

  align-items: center;

  border: 1px solid rgba(193, 146, 255, 0.18);

  box-shadow: 0 14px 34px rgba(102, 16, 180, 0.22);

  position: relative;

  overflow: hidden;
}

.cart-item::before {
  content: '';

  position: absolute;

  inset: 0;

  background:
    radial-gradient(
      circle at top right,
      rgba(255, 255, 255, 0.08),
      transparent 28%
    ),
    radial-gradient(
      circle at bottom left,
      rgba(142, 96, 255, 0.08),
      transparent 24%
    );

  pointer-events: none;
}

.cart-item:hover {
  transform: translateY(-1px);
}

.cart-item .qty-control {
  grid-row: 1 / span 2;
}

.cart-item .item-info {
  display: flex;

  flex-direction: column;

  gap: 8px;
}

.cart-item .item-bottom {
  display: flex;

  justify-content: space-between;

  gap: 18px;

  flex-wrap: wrap;
}

.pending-item {
  border-left-color: #f39c12;

  background:
    linear-gradient(
      145deg,
      rgba(82, 34, 105, 0.96),
      rgba(37, 17, 42, 0.95)
    );
}

.done-item {
  border-left-color: #5dcb80;

  background:
    linear-gradient(
      145deg,
      rgba(25, 34, 38, 0.92),
      rgba(8, 15, 28, 0.96)
    );
}

/* =========================================================
   ITEM INFO
   ========================================================= */

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

/* =========================================================
   QUANTITY
   ========================================================= */

.qty-control {
  display: inline-flex;

  align-items: center;

  gap: 8px;

  margin-right: 12px;
}

.qty-input {
  width: 58px;

  min-width: 58px;

  padding: 4px 8px;

  border: 1px solid rgba(255, 216, 107, 0.35);

  border-radius: 8px;

  background: #2c2c2c;

  color: #ffd86b;

  text-align: center;

  font-weight: 700;
}

.qty-input:focus {
  outline: none;

  border-color: #ffd86b;

  box-shadow: 0 0 0 3px rgba(255, 216, 107, 0.15);
}

.btn-minus,
.btn-plus {
  width: 32px;

  height: 32px;

  border: 1px solid rgba(255, 216, 107, 0.25);

  border-radius: 8px;

  background: #242424;

  color: #ffd86b;

  font-size: 16px;

  cursor: pointer;
}

.btn-minus:hover,
.btn-plus:hover {
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

/* =========================================================
   TAB ACTION
   ========================================================= */

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

/* =========================================================
   THANH TOÁN
   ========================================================= */

.btn-thanh-toan {
  width: 100%;

  padding: 16px 20px;

  border: none;

  border-radius: 18px;

  background:
    linear-gradient(
      135deg,
      #b448ff,
      #6d32ff 45%,
      #f69cff 90%
    );

  color: #fff;

  font-size: 16px;

  font-weight: 800;

  cursor: pointer;

  display: flex;

  justify-content: space-between;

  align-items: center;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;

  box-shadow:
    0 20px 40px rgba(143, 95, 255, 0.35);

  animation: pulseGlow 3.8s ease-in-out infinite;
}

.btn-thanh-toan:hover {
  transform: translateY(-3px);

  box-shadow:
    0 24px 48px rgba(168, 108, 255, 0.5);

  background:
    linear-gradient(
      135deg,
      #c86eff,
      #7f49ff 45%,
      #ffb8ff 90%
    );
}

.btn-thanh-toan.pay-button {
  gap: 14px;
}

.discount-input {
  width: 100%;

  padding: 14px 16px;

  border-radius: 14px;

  border: 1px solid rgba(142, 83, 255, 0.28);

  background: rgba(25, 12, 44, 0.92);

  color: #f4e8ff;

  margin-bottom: 10px;

  box-shadow:
    inset 0 1px 2px rgba(255, 255, 255, 0.06);
}

.discount-input option {
  background: #160b25;

  color: #f4e8ff;
}

/* =========================================================
   TITLE
   ========================================================= */

.title {
  color: #f9eeff;

  font-size: 20px;

  font-weight: 900;

  margin-bottom: 18px;

  position: relative;

  padding-bottom: 10px;

  flex-shrink: 0;

  text-shadow:
    0 0 12px rgba(158, 114, 255, 0.25);
}

.title::after {
  content: '';

  position: absolute;

  left: 0;

  bottom: 0;

  width: 60px;

  height: 5px;

  border-radius: 999px;

  background:
    linear-gradient(
      90deg,
      rgba(214, 155, 255, 0.95),
      rgba(130, 79, 255, 0.95)
    );

  box-shadow:
    0 0 18px rgba(214, 155, 255, 0.35);
}

/* =========================================================
   EMPTY CART
   ========================================================= */

.empty-cart {
  color: #d5c1ff;

  text-align: center;

  margin-top: 32px;

  font-style: italic;

  font-size: 13px;

  background: rgba(86, 25, 115, 0.16);

  border: 1px dashed rgba(213, 193, 255, 0.45);

  padding: 20px;

  border-radius: 18px;

  box-shadow:
    inset 0 0 20px rgba(255, 254, 255, 0.08);
}

/* =========================================================
   HEADER DANH SÁCH MÓN
   ========================================================= */

.header-danh-sach {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 15px;

  border-bottom: 1px solid rgba(212, 175, 55, 0.25);

  padding-bottom: 8px;

  flex-shrink: 0;
}

.header-danh-sach .title {
  margin-bottom: 0;

  border-bottom: none;

  padding-bottom: 0;
}

/* =========================================================
   SEARCH
   ========================================================= */

.search-box {
  position: relative;

  display: flex;

  align-items: center;

  width: 220px;
}

.search-input {
  width: 100%;

  padding: 8px 30px 8px 12px;

  border-radius: 8px;

  border: 1px solid rgba(212, 175, 55, 0.3);

  background: #2a2a2a;

  color: #fff;

  font-size: 13px;

  outline: none;

  transition: all 0.25s ease;
}

.search-input:focus {
  border-color: #ffd86b;

  box-shadow:
    0 0 8px rgba(255, 216, 107, 0.2);
}

.btn-clear-search {
  position: absolute;

  right: 8px;

  background: transparent;

  border: none;

  color: #aaa;

  cursor: pointer;

  font-size: 12px;

  padding: 2px;
}

.btn-clear-search:hover {
  color: #ffd86b;
}

.empty-search {
  grid-column: span 3;

  color: #888;

  text-align: center;

  padding: 20px;

  font-style: italic;

  font-size: 14px;
}

/* =========================================================
   PAYMENT REVIEW
   ========================================================= */

.payment-review-overlay {
  position: fixed;

  inset: 0;

  z-index: 2000;

  background:
    radial-gradient(
      circle at top left,
      rgba(255, 255, 255, 0.08),
      transparent 20%
    ),
    rgba(10, 10, 10, 0.82);

  display: flex;

  align-items: center;

  justify-content: center;

  padding: 24px;
}

.payment-review-dialog {
  width: min(560px, 100%);

  background:
    linear-gradient(
      180deg,
      #fff8eb,
      #fff1d3
    );

  border-radius: 26px;

  border: 1px solid rgba(212, 175, 55, 0.24);

  box-shadow:
    0 26px 68px rgba(0, 0, 0, 0.22);

  padding: 24px;

  color: #4e3511;
}

.review-header {
  margin-bottom: 18px;

  padding: 14px 16px;

  background:
    linear-gradient(
      135deg,
      rgba(255, 234, 187, 0.97),
      rgba(255, 239, 213, 0.95)
    );

  border-radius: 18px;

  border: 1px solid rgba(255, 210, 115, 0.35);
}

.review-title {
  font-size: 22px;

  font-weight: 800;

  color: #7b4d14;

  margin-bottom: 8px;
}

.review-subtitle {
  font-size: 14px;

  line-height: 1.6;

  color: #735623;
}

.payment-review-list {
  display: flex;

  flex-direction: column;

  gap: 12px;

  margin-bottom: 20px;
}

.review-item {
  display: flex;

  justify-content: space-between;

  gap: 12px;

  padding: 14px 18px;

  border-radius: 16px;

  background: rgba(255, 244, 224, 0.95);

  border: 1px solid rgba(255, 210, 114, 0.28);
}

.review-name {
  font-weight: 700;

  color: #563812;
}

.review-detail {
  color: #7a5728;

  font-size: 13px;

  white-space: nowrap;
}

.review-total {
  padding: 16px;

  border-radius: 16px;

  background: #fff3d2;

  border: 1px solid rgba(255, 200, 88, 0.32);

  font-weight: 800;

  color: #6d4a1d;

  text-align: right;
}

.review-actions {
  display: flex;

  gap: 12px;

  justify-content: flex-end;

  flex-wrap: wrap;
}

.review-actions button {
  padding: 12px 18px;

  border-radius: 12px;

  border: none;

  cursor: pointer;

  font-weight: 700;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.review-actions button:hover {
  transform: translateY(-1px);

  box-shadow:
    0 10px 24px rgba(0, 0, 0, 0.14);
}

.btn-primary {
  background:
    linear-gradient(
      135deg,
      #f6c24b,
      #d49b13
    );

  color: #111;
}

.btn-secondary {
  background: #fff7e7;

  color: #7b4f19;

  border: 1px solid rgba(212, 175, 55, 0.25);
}
</style>
