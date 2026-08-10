<!-- src/views/BanHang.vue -->
<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import BanApi from '@/api/BanApi'
import { getAllKhuVuc } from '@/api/khuvuc'
import PopupListDatBan from '@/components/PopupListDatBan.vue'
import DatBanQLTab from '@/components/DatBanQLTab.vue'
import DatBanPopupCheck from '@/components/DatBanPopupCheck.vue'
import ThanhToan from '@/components/ThanhToan.vue'
import HoaDonApi from '@/api/HoaDonApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import AllBillsView from '@/components/AllBillsView.vue'
import { useShiftStore } from '@/stores/ShiftStore'

interface CheckedInReservation {
  idDatBan: number
  idBan?: number
  tenKhachHang?: string
  sdtKhachHang?: string
  soNguoi?: number
  trangThai?: string
  thoiGianDenDuKien?: string
  ngayDat?: string
  gioDat?: string
  soTienCoc?: number
  dsBan?: Array<{ idBan: number; tenBan: string }>
}

// ======================== STATE ========================
const route = useRoute()
const shiftStore = useShiftStore()
const manHinhHienTai = ref('danhSachBan')
const showPopup = ref(false)
const showPopupDaXacNhan = ref(false)
const showAllBills = ref(false)
const soLuongHoaDon = ref(0)
const checkedInReservations = ref<CheckedInReservation[]>([])

const danhSachBan = ref<any[]>([])
const danhSachKhuVuc = ref<any[]>([])
const idKhuVucDangChon = ref<number | null>(null)
const banDangChon = ref<any | null>(null)
const datBanDangChon = ref<any | null>(null)

// Tab cho danh sách đặt bàn
const activeTabDatBan = ref('daXepBan') // 'daXepBan' | 'chuaXepBan'

// ======================== COMPUTED ========================
const danhSachDaXepBan = computed(() => {
  return danhSachBan.value.filter(
    (ban) => ban.trangThai === 'DA_DAT' || ban.trangThai === 'DANG_SU_DUNG',
  )
})

const danhSachChuaXepBan = computed(() => {
  return danhSachBan.value.filter((ban) => ban.trangThai === 'TRONG')
})

// ======================== HELPER FUNCTIONS ========================
const formatDateTime = (value: any) => {
  if (!value) return null
  if (Array.isArray(value)) {
    const [y, m, d, h = 0, min = 0] = value
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${pad(h)}:${pad(min)} - ${pad(d)}/${pad(m)}/${y}`
  }
  const date = new Date(value)
  if (isNaN(date.getTime())) return String(value)

  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())} - ${pad(date.getDate())}/${pad(
    date.getMonth() + 1,
  )}/${date.getFullYear()}`
}

const normalizeStatus = (value: any) => {
  if (!value) return ''
  if (typeof value === 'string') return value.trim().toUpperCase()
  if (typeof value === 'object' && 'name' in value) return String(value.name).trim().toUpperCase()
  return String(value).trim().toUpperCase()
}

const isAvailableTable = (ban: any) => normalizeStatus(ban?.trangThai) === 'TRONG'

// ======================== METHODS ========================
const loadSoLuongHoaDon = async () => {
  try {
    const res = await HoaDonApi.getActiveCount();
    soLuongHoaDon.value = typeof res.data === 'number' ? res.data : 0;
  } catch (error) {
    console.error('Không thể tải số lượng hóa đơn:', error);
    soLuongHoaDon.value = 0;
  }
};

const canContinueServingClosedShift = (ban: any | null) => {
  if (!ban?.idBan) return false

  const activeInvoice = ban?.hoaDonInfo
  const hasUnpaidInvoice =
    activeInvoice &&
    Number(activeInvoice.trangThaiThanhToan) === 0 &&
    Number(activeInvoice.trangThaiHoaDon) === 0

  return Boolean(hasUnpaidInvoice)
}

const chuyenManHinhThanhToan = async () => {
  const isShiftClosed = !shiftStore.currentShift || shiftStore.currentShift.isOpen === false
  const canContinue = canContinueServingClosedShift(banDangChon.value)

  if (isShiftClosed && !canContinue) {
    alert('Ca làm việc đã đóng, không thể thực hiện thao tác gọi món mới')
    return
  }

  showPopup.value = false
  if (banDangChon.value) {
    await markBanDangSuDung(banDangChon.value)
  }
  manHinhHienTai.value = 'thanhToan'
}

const loadBan = async () => {
  const [banRes, reservationRes, invoiceRes] = await Promise.all([
    BanApi.getAll(),
    DatBanQuanLyApi.getAll(),
    HoaDonApi.getDanhSach().catch(() => ({ data: [] })),
  ])

  const rawBan = Array.isArray(banRes?.data) ? banRes.data : []
  const reservations = Array.isArray(reservationRes?.data) ? reservationRes.data : []
  const invoices = Array.isArray(invoiceRes?.data) ? invoiceRes.data : []

  const rawBanStatusById = new Map<number, string>(
    rawBan.map((ban: any) => [Number(ban.idBan), normalizeStatus(ban.trangThai)]),
  )
  const unpaidInvoices = invoices.filter(
    (inv: any) => Number(inv.trangThaiThanhToan) === 0 && inv.idBan != null,
  )
  const unpaidBillIds = new Set<string>(unpaidInvoices.map((inv: any) => String(inv.idHoaDon)))
  const unpaidBanIds = new Set<number>()
  unpaidInvoices.forEach((inv: any) => unpaidBanIds.add(Number(inv.idBan)))

  const handoverPending = shiftStore.currentShift?.handoverContext?.pendingTables || []
  handoverPending.forEach((item: any) => {
    const itemBanId = item.idBan != null ? Number(item.idBan) : null
    if (itemBanId == null) return

    const itemBillId = item.billId != null ? String(item.billId) : null
    const stillHasUnpaidBill = itemBillId != null && unpaidBillIds.has(itemBillId)
    const tableStillInUse = rawBanStatusById.get(itemBanId) !== 'TRONG'

    if (stillHasUnpaidBill || tableStillInUse) {
      unpaidBanIds.add(itemBanId)
    } else {
      shiftStore.clearSettledTableReferences({ tableId: itemBanId, billId: itemBillId })
    }
  })

  const normalizeReservationStatus = (value: any) => {
    if (!value) return ''
    if (typeof value === 'string') return value
    if (typeof value === 'object' && 'name' in value) return String(value.name)
    return String(value)
  }

  const normalizeBanStatus = (value: any) => {
    if (!value) return ''
    if (typeof value === 'string') return value
    if (typeof value === 'object' && 'name' in value) return String(value.name)
    return String(value)
  }

  const getReservationInfoForBan = (banId: number, reservations: any[]) => {
    const banReservations = reservations.filter(
      (reservation: any) =>
        Array.isArray(reservation?.dsBan) &&
        reservation.dsBan.some((ban: any) => Number(ban?.idBan) === banId),
    )

    if (!banReservations.length) {
      return null
    }

    const activeReservation = banReservations.find((reservation: any) =>
      ['DA_NHAN_BAN', 'DA_XAC_NHAN'].includes(normalizeReservationStatus(reservation?.trangThai)),
    )

    return activeReservation || null
  }

  danhSachBan.value = rawBan.map((ban: any) => {
    const banId = Number(ban.idBan)
    const activeReservation = getReservationInfoForBan(banId, reservations)
    const reservationStatus = activeReservation
      ? normalizeReservationStatus(activeReservation.trangThai)
      : null
    const activeInvoice = unpaidInvoices.find((inv: any) => Number(inv.idBan) === banId)

    const banStatus = normalizeBanStatus(ban.trangThai)
    let nextStatus = banStatus

    if (unpaidBanIds.has(banId)) {
      nextStatus = 'DANG_SU_DUNG'
    } else if (reservationStatus === 'DA_NHAN_BAN') {
      nextStatus = 'DANG_SU_DUNG'
    } else if (reservationStatus === 'DA_XAC_NHAN' && banStatus === 'TRONG') {
      nextStatus = 'DA_DAT'
    } else if (
      !['TRONG', 'DA_DAT', 'DANG_SU_DUNG'].includes(banStatus) &&
      reservationStatus === 'DA_XAC_NHAN'
    ) {
      nextStatus = 'DA_DAT'
    }

    return {
      ...ban,
      trangThai: nextStatus,
      datBanInfo: activeReservation,
      hoaDonInfo: activeInvoice,
    }
  })
}

const loadKhuVuc = async () => {
  const re = await getAllKhuVuc()
  danhSachKhuVuc.value = re.data
}

const handleChangeTab = (idKhuVuc: number) => {
  idKhuVucDangChon.value = idKhuVuc
}

const handleSelectBan = (ban: any) => {
  if (isAvailableTable(ban)) {
    clearPosOrderCache(ban.idBan)
    datBanDangChon.value = null
    banDangChon.value = { ...ban, current_order_id: null, invoice_id: null, idHoaDon: null }
    return
  }

  banDangChon.value = ban
}

const moPopupDatBan = async (ban: any) => {
  handleSelectBan(ban)

  if (isAvailableTable(ban)) {
    showPopup.value = true
    return
  }

  // Nếu bàn đang dùng thì không hiện popup đặt bàn, đi thẳng sang màn thanh toán.
  if (normalizeStatus(ban.trangThai) === 'DANG_SU_DUNG') {
    datBanDangChon.value = null
    manHinhHienTai.value = 'thanhToan'
    return
  }

  try {
    const res = await HoaDonApi.findByBanAndStatus(ban.idBan, 0)
    if (res.data) {
      datBanDangChon.value = null
      manHinhHienTai.value = 'thanhToan'
      return
    }
  } catch (error) {
    // Không có hóa đơn -> hiện popup
  }

  showPopup.value = true
}

const dongPopup = () => {
  showPopup.value = false
}

const markBanDangSuDung = async (ban: any) => {
  if (!ban?.idBan) return

  try {
    await BanApi.update(ban.idBan, { trangThai: 'DANG_SU_DUNG' })
    const targetIndex = danhSachBan.value.findIndex(
      (item: any) => Number(item.idBan) === Number(ban.idBan),
    )
    if (targetIndex >= 0) {
      danhSachBan.value[targetIndex] = {
        ...danhSachBan.value[targetIndex],
        trangThai: 'DANG_SU_DUNG',
      }
    }
    banDangChon.value = { ...ban, trangThai: 'DANG_SU_DUNG' }
    await loadBan()
  } catch (error) {
    console.warn('Không thể cập nhật trạng thái bàn:', error)
  }
}

const moDanhSachDatBan = async () => {
  showPopup.value = false

  if (!banDangChon?.value?.idBan) {
    showPopupDaXacNhan.value = true
    return
  }

  try {
    const reservationRes = await DatBanQuanLyApi.getAll()
    const reservations = Array.isArray(reservationRes?.data) ? reservationRes.data : []
    const matchedReservation = reservations.find(
      (reservation: any) =>
        Array.isArray(reservation?.dsBan) &&
        reservation.dsBan.some(
          (ban: any) => Number(ban?.idBan) === Number(banDangChon.value.idBan),
        ) &&
        ['DA_NHAN_BAN', 'DA_XAC_NHAN'].includes(String(reservation?.trangThai || '')),
    )

    if (matchedReservation) {
      datBanDangChon.value = matchedReservation
      showPopupDaXacNhan.value = false
      manHinhHienTai.value = 'thanhToan'
      return
    }
  } catch (error) {
    console.warn('Không thể lấy đơn đặt bàn tương ứng:', error)
  }

  showPopupDaXacNhan.value = true
}

const chonDatBan = (datBan: any) => {
  datBanDangChon.value = datBan
  showPopupDaXacNhan.value = false
  manHinhHienTai.value = 'thanhToan'
}

const quayVeDanhSachBan = async () => {
  manHinhHienTai.value = 'danhSachBan'
  datBanDangChon.value = null
  banDangChon.value = null
  await loadBan()
  await loadSoLuongHoaDon()
}

const layNgayHienTai = () => {
  const now = new Date()
  const ngay = String(now.getDate()).padStart(2, '0')
  const thang = String(now.getMonth() + 1).padStart(2, '0')
  const nam = now.getFullYear()
  return `${ngay}/${thang}/${nam}`
}

const layThuTrongTuan = () => {
  const thu = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7']
  return thu[new Date().getDay()]
}

const handleViewBill = (hoaDon: any) => {
  showAllBills.value = false
  datBanDangChon.value = null
  banDangChon.value = danhSachBan.value.find((b) => b.idBan === hoaDon.idBan)
  manHinhHienTai.value = 'thanhToan'
}

const openPendingTarget = async () => {
  const pendingTableId = route.query.pendingTableId ? String(route.query.pendingTableId) : ''
  const pendingBillId = route.query.pendingBillId ? String(route.query.pendingBillId) : ''
  const pendingTableName = route.query.pendingTableName ? String(route.query.pendingTableName) : ''
  const pendingDatBanId = route.query.pendingDatBanId ? String(route.query.pendingDatBanId) : ''

  const loadReservation = async (datBanId: number) => {
    try {
      const reservationRes = await DatBanQuanLyApi.findById(datBanId)
      if (reservationRes?.data) {
        datBanDangChon.value = reservationRes.data
        const tableFromReservation = reservationRes.data?.dsBan?.[0]
        if (tableFromReservation?.idBan != null) {
          const matchedBan = danhSachBan.value.find(
            (item: any) => Number(item.idBan) === Number(tableFromReservation.idBan),
          )
          banDangChon.value = matchedBan
            ? { ...matchedBan }
            : { idBan: tableFromReservation.idBan, tenBan: tableFromReservation.tenBan, trangThai: 'DANG_SU_DUNG' }
        }
        manHinhHienTai.value = 'thanhToan'
        showPopup.value = false
        return true
      }
    } catch (err) {
      console.warn('Không thể tải đơn đặt bàn từ query:', err)
    }
    return false
  }

  if (!pendingTableId && !pendingBillId && !pendingDatBanId) return

  if (pendingDatBanId) {
    const loaded = await loadReservation(Number(pendingDatBanId))
    if (loaded) return
  }

  if (pendingBillId) {
    try {
      const billRes = await HoaDonApi.getById(Number(pendingBillId))
      const invoice = billRes?.data
      if (!invoice) return

      if (invoice.idDatBan) {
        const loaded = await loadReservation(invoice.idDatBan)
        if (loaded) return
      }

      if (
        Number(invoice.trangThaiThanhToan) === 1 ||
        Number(invoice.trangThaiHoaDon) === 1
      ) {
        clearPosOrderCache(invoice?.idBan ?? null)
        shiftStore.clearSettledTableReferences({ tableId: invoice?.idBan ?? null, billId: pendingBillId })
        return
      }

      if (invoice?.idBan) {
        const matchedBan = danhSachBan.value.find(
          (item: any) => Number(item.idBan) === Number(invoice.idBan),
        )
        if (matchedBan && isAvailableTable(matchedBan)) {
          clearPosOrderCache(invoice.idBan)
          shiftStore.clearSettledTableReferences({ tableId: invoice.idBan, billId: pendingBillId })
          return
        }
        banDangChon.value = matchedBan
          ? {
              ...matchedBan,
              tenBan: matchedBan.tenBan || pendingTableName || `Bàn ${invoice.idBan}`,
            }
          : { idBan: Number(invoice.idBan), tenBan: pendingTableName || `Bàn ${invoice.idBan}` }
        manHinhHienTai.value = 'thanhToan'
        showPopup.value = false
        return
      }
    } catch (error) {
      console.warn('Không thể mở hóa đơn treo từ link chốt ca:', error)
    }
  }

  if (pendingTableId) {
    const matchedBan = danhSachBan.value.find(
      (item: any) => Number(item.idBan) === Number(pendingTableId),
    )

    if (matchedBan && matchedBan.datBanInfo) {
      datBanDangChon.value = matchedBan.datBanInfo
    }

    if (matchedBan && isAvailableTable(matchedBan)) {
      clearPosOrderCache(pendingTableId)
      shiftStore.clearSettledTableReferences({
        tableId: pendingTableId,
        billId: pendingBillId || null,
      })
      return
    }

    banDangChon.value = matchedBan
      ? {
          ...matchedBan,
          tenBan: matchedBan.tenBan || pendingTableName || `Bàn ${pendingTableId}`,
        }
      : { idBan: Number(pendingTableId), tenBan: pendingTableName || `Bàn ${pendingTableId}`, trangThai: 'DANG_SU_DUNG' }
    manHinhHienTai.value = 'thanhToan'
    showPopup.value = false
    return
  }
}

const clearPosOrderCache = (tableId?: number | string | null) => {
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

const handlePaymentComplete = async (payload: {
  idBan: number
  trangThai: string
  billId?: number | string | null
}) => {
  try {
    const normalizedStatus = payload?.trangThai || 'TRONG';
    clearPosOrderCache(payload?.idBan);
    shiftStore.clearSettledTableReferences({ tableId: payload?.idBan, billId: payload?.billId });

    const targetIndex = danhSachBan.value.findIndex(
      (item: any) => Number(item.idBan) === Number(payload.idBan),
    );
    if (targetIndex >= 0) {
      danhSachBan.value[targetIndex] = {
        ...danhSachBan.value[targetIndex],
        trangThai: normalizedStatus,
        current_order_id: null,
        invoice_id: null,
        idHoaDon: null,
      };
    }

    if (banDangChon.value && Number(banDangChon.value.idBan) === Number(payload.idBan)) {
      banDangChon.value = null;
    }

    manHinhHienTai.value = 'danhSachBan';
    datBanDangChon.value = null;

    // 1. Tách Promise.all: Ưu tiên sync danh sách bàn trước
    await loadBan();

    // 2. Load số lượng hóa đơn sau
    await loadSoLuongHoaDon();

  } catch (err) {
    console.error('Lỗi trong quá trình hoàn tất thanh toán:', err);
  }
};

// ======================== HOOKS ========================
watch(
  () => route.query,
  async () => {
    await openPendingTarget()
  },
  { deep: true },
)

let messageListener: any = null

onMounted(async () => {
  await loadBan()
  await loadKhuVuc()
  await loadSoLuongHoaDon()
  await openPendingTarget()

  // Periodically refresh invoice count
  setInterval(loadSoLuongHoaDon, 30000)

  // Listen for cross-window payment notifications (from payment return page)
  messageListener = (event: MessageEvent) => {
    try {
      const payload = event.data || {}
      if (payload && payload.type === 'payment-complete' && payload.idBan) {
        // Call the same handler used by ThanhToan @payment-complete
        void handlePaymentComplete({
          idBan: payload.idBan,
          trangThai: payload.trangThai || 'TRONG',
          billId: payload.billId || null,
        })
      }
    } catch (e) {
      console.warn('Invalid payment message received', e)
    }
  }

  window.addEventListener('message', messageListener)
})

onUnmounted(() => {
  if (messageListener) window.removeEventListener('message', messageListener)
})
</script>

<template>
  <div class="ban-hang-view">
    <div class="page-actions">
      <button class="btn-bills" type="button" @click="showAllBills = true">
        <span class="btn-icon">📋</span>
        Tất cả hóa đơn
        <span class="badge" v-if="soLuongHoaDon > 0">{{ soLuongHoaDon }}</span>
      </button>
    </div>

    <template v-if="manHinhHienTai === 'danhSachBan'">
      <!-- HEADER: TIÊU ĐỀ + NGÀY THÁNG -->
      <div class="header-section">
        <div class="header-left">
          <h1 class="page-title">🍽️ Quản lý bàn</h1>
          <div class="date-display">
            <span class="date-day">{{ layThuTrongTuan() }}</span>
            <span class="date-number">{{ layNgayHienTai() }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="stats-badge">
            <span class="stat-item">
              <span class="stat-dot busy"></span>
              Đã đặt: {{ danhSachDaXepBan.length }}
            </span>
            <span class="stat-item">
              <span class="stat-dot free"></span>
              Trống: {{ danhSachChuaXepBan.length }}
            </span>
          </div>
        </div>
      </div>

      <!-- TAB KHU VỰC -->
      <DatBanQLTab :listKhuVuc="danhSachKhuVuc" @change="handleChangeTab">
        <template #default="{ idKhuVuc }">
          <div class="ban-list-container">
            <div class="ban-grid">
              <div
                v-for="ban in danhSachBan.filter((b) => b.idKhuVuc === idKhuVuc)"
                :key="ban.idBan"
                class="ban-item-wrapper"
              >
                <!-- CARD BÀN -->
                <div
                  class="ban-item"
                  :class="{
                    trong: ban.trangThai === 'TRONG',
                    'da-dat': ban.trangThai === 'DA_DAT',
                    'dang-su-dung': ban.trangThai === 'DANG_SU_DUNG',
                  }"
                  @click="handleSelectBan(ban)"
                  @dblclick="moPopupDatBan(ban)"
                >
                  <div class="ban-icon">
                    <svg
                      v-if="ban.trangThai === 'TRONG'"
                      width="32"
                      height="32"
                      viewBox="0 0 24 24"
                      fill="none"
                    >
                      <rect
                        x="4"
                        y="4"
                        width="16"
                        height="16"
                        rx="3"
                        stroke="#4CAF50"
                        stroke-width="2"
                      />
                      <circle cx="12" cy="12" r="2" fill="#4CAF50" />
                    </svg>
                    <svg
                      v-else-if="ban.trangThai === 'DA_DAT'"
                      width="32"
                      height="32"
                      viewBox="0 0 24 24"
                      fill="none"
                    >
                      <rect
                        x="4"
                        y="4"
                        width="16"
                        height="16"
                        rx="3"
                        stroke="#FF9800"
                        stroke-width="2"
                      />
                      <circle cx="12" cy="12" r="2" fill="#FF9800" />
                      <text x="12" y="20" text-anchor="middle" font-size="8" fill="#FF9800">
                        ⏳
                      </text>
                    </svg>
                    <svg v-else width="32" height="32" viewBox="0 0 24 24" fill="none">
                      <rect
                        x="4"
                        y="4"
                        width="16"
                        height="16"
                        rx="3"
                        stroke="#F44336"
                        stroke-width="2"
                      />
                      <circle cx="12" cy="12" r="2" fill="#F44336" />
                      <text x="12" y="20" text-anchor="middle" font-size="8" fill="#F44336">●</text>
                    </svg>
                  </div>
                  <div class="ban-info">
                    <span class="ban-name">{{ ban.tenBan }}</span>
                    <span class="ban-capacity">👥 {{ ban.soLuongNguoi || ban.soNguoi || 4 }}</span>
                  </div>
                  <div class="ban-status">
                    <span class="status-badge" :class="ban.trangThai.toLowerCase()">
                      {{
                        ban.trangThai === 'TRONG'
                          ? 'Trống'
                          : ban.trangThai === 'DA_DAT'
                            ? 'Đã đặt'
                            : 'Đang dùng'
                      }}
                    </span>
                  </div>
                </div>

                <!-- TOOLTIP HIỂN THỊ THÔNG TIN KHI HOVER -->
                <div class="ban-tooltip">
                  <div class="tooltip-header">
                    <strong>Thông tin {{ ban.tenBan }}</strong>
                  </div>
                  <div class="tooltip-body">
                    <div class="tooltip-row">
                      <span class="label">Mã hóa đơn:</span>
                      <span class="value highlight">
                        {{ ban.hoaDonInfo?.maHoaDon || ban.datBanInfo?.maHoaDon || 'Chưa tạo' }}
                      </span>
                    </div>
                    <div class="tooltip-row">
                      <span class="label">Khách hàng:</span>
                      <span class="value">
                        {{
                          ban.datBanInfo?.tenKhachHang || ban.hoaDonInfo?.tenKhachHang || 'Khách lẻ'
                        }}
                      </span>
                    </div>
                    <div class="tooltip-row">
                      <span class="label">Số điện thoại:</span>
                      <span class="value">
                        {{ ban.datBanInfo?.sdtKhachHang || ban.hoaDonInfo?.sdtKhachHang || 'N/A' }}
                      </span>
                    </div>
                    <div class="tooltip-row">
                      <span class="label">Thời gian đến:</span>
                      <span class="value">
                        {{
                          formatDateTime(ban.datBanInfo?.thoiGianDenDuKien) || 'Khách vào trực tiếp'
                        }}
                      </span>
                    </div>
                    <div class="tooltip-row">
                      <span class="label">Số khách:</span>
                      <span class="value">
                        {{ ban.datBanInfo?.soNguoi ? `${ban.datBanInfo.soNguoi} người` : 'N/A' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </DatBanQLTab>

      <!-- PHẦN TAB ĐƠN ĐẶT BÀN -->
      <div class="dat-ban-tabs-section">
        <div class="tabs-header">
          <button
            class="tab-btn"
            :class="{ active: activeTabDatBan === 'daXepBan' }"
            @click="activeTabDatBan = 'daXepBan'"
          >
            📋 Đã sắp bàn
            <span class="tab-count">{{ danhSachDaXepBan.length }}</span>
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTabDatBan === 'chuaXepBan' }"
            @click="activeTabDatBan = 'chuaXepBan'"
          >
            📌 Chưa sắp bàn
            <span class="tab-count">{{ danhSachChuaXepBan.length }}</span>
          </button>
        </div>

        <div class="tabs-content">
          <div v-show="activeTabDatBan === 'daXepBan'" class="tab-panel">
            <div v-if="danhSachDaXepBan.length === 0" class="empty-state">
              <span class="empty-icon">✅</span>
              <p>Chưa có bàn nào được sắp xếp</p>
            </div>
            <div v-else class="ban-list-mini">
              <div v-for="ban in danhSachDaXepBan" :key="ban.idBan" class="ban-mini-item">
                <span class="ban-mini-name">{{ ban.tenBan }}</span>
                <span class="ban-mini-status" :class="ban.trangThai.toLowerCase()">
                  {{ ban.trangThai === 'DA_DAT' ? 'Đã đặt' : 'Đang dùng' }}
                </span>
                <span class="ban-mini-time">⏰ {{ new Date().toLocaleTimeString() }}</span>
              </div>
            </div>
          </div>

          <div v-show="activeTabDatBan === 'chuaXepBan'" class="tab-panel">
            <div v-if="danhSachChuaXepBan.length === 0" class="empty-state">
              <span class="empty-icon">🎉</span>
              <p>Tất cả bàn đều đã được sắp xếp</p>
            </div>
            <div v-else class="ban-list-mini">
              <div v-for="ban in danhSachChuaXepBan" :key="ban.idBan" class="ban-mini-item">
                <span class="ban-mini-name">{{ ban.tenBan }}</span>
                <span class="ban-mini-status trong">Trống</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- POPUP KIỂM TRA ĐẶT BÀN -->
      <DatBanPopupCheck
        v-if="showPopup"
        :ban="banDangChon"
        @close="dongPopup"
        @coDonDatBan="moDanhSachDatBan"
        @khongCoDonDatBan="chuyenManHinhThanhToan"
      />

      <PopupListDatBan
        v-if="showPopupDaXacNhan"
        @close="showPopupDaXacNhan = false"
        @chonDatBan="chonDatBan"
      />
    </template>

    <ThanhToan
      v-if="manHinhHienTai === 'thanhToan'"
      :ban="banDangChon"
      :datBan="datBanDangChon"
      @quayLai="quayVeDanhSachBan"
      @payment-complete="handlePaymentComplete"
    />

    <!-- MODAL TẤT CẢ HÓA ĐƠN -->
    <div v-if="showAllBills" class="modal-overlay" @click.self="showAllBills = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>📊 Tất cả hóa đơn đang hoạt động</h3>
          <button class="btn-close" @click="showAllBills = false">✕</button>
        </div>
        <AllBillsView @viewBill="handleViewBill" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== BASE ========== */
.ban-hang-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5efe6 0%, #e8ddd0 100%);
  padding: 20px;
  box-sizing: border-box;
  color: #4a3520;
  font-family:
    'Segoe UI',
    system-ui,
    -apple-system,
    sans-serif;
}

/* ========== HEADER ========== */
.page-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #8b6b4a, #5f3d22);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.date-display {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(139, 107, 74, 0.1);
  padding: 6px 16px;
  border-radius: 30px;
}

.date-day {
  font-weight: 500;
  color: #8b6b4a;
}

.date-number {
  font-weight: 600;
  color: #5f3d22;
  background: white;
  padding: 2px 12px;
  border-radius: 20px;
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-badge {
  display: flex;
  gap: 16px;
  background: white;
  padding: 8px 20px;
  border-radius: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #4a3520;
}

.stat-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.stat-dot.busy {
  background: #ff9800;
}

.stat-dot.free {
  background: #4caf50;
}

/* ========== BUTTON BILLS ========== */
.btn-bills {
  padding: 8px 16px;
  border: none;
  border-radius: 12px;
  background: white;
  color: #4a3520;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.btn-bills:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(139, 107, 74, 0.2);
  background: #faf6f0;
}

.btn-icon {
  font-size: 16px;
}

.badge {
  background: #8b6b4a;
  color: white;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
}

/* ========== MODAL ========== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.2s ease;
}

.modal-content {
  background: white;
  border-radius: 16px;
  max-width: 1000px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #faf6f0;
  border-radius: 16px 16px 0 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #4a3520;
}

.btn-close {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a3520;
}

.btn-close:hover {
  background: rgba(0, 0, 0, 0.1);
  transform: rotate(90deg);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* ========== BAN GRID & TOOLTIP ========== */
.ban-list-container {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}

.ban-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.ban-item-wrapper {
  position: relative;
}

.ban-item {
  background: white;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 2px solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.ban-item-wrapper:hover .ban-item {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.ban-item.trong {
  border-color: #4caf50;
  background: linear-gradient(135deg, #fafffe, #f0faf0);
}

.ban-item.trong:hover {
  border-color: #388e3c;
  background: linear-gradient(135deg, #f0faf0, #e0f0e0);
}

.ban-item.da-dat {
  border-color: #ff9800;
  background: linear-gradient(135deg, #fffbf0, #fef5e6);
}

.ban-item.da-dat:hover {
  border-color: #f57c00;
  background: linear-gradient(135deg, #fef5e6, #fde8cc);
}

.ban-item.dang-su-dung {
  border-color: #f44336;
  background: linear-gradient(135deg, #fff5f5, #fde8e8);
}

.ban-item.dang-su-dung:hover {
  border-color: #d32f2f;
  background: linear-gradient(135deg, #fde8e8, #fcd0d0);
}

.ban-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ban-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.ban-name {
  font-weight: 600;
  font-size: 15px;
  color: #4a3520;
}

.ban-capacity {
  font-size: 12px;
  color: #8a7a6a;
}

.ban-status {
  margin-top: 4px;
}

.status-badge {
  font-size: 11px;
  padding: 3px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.status-badge.trong {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge.da-dat {
  background: #fff3e0;
  color: #e65100;
}

.status-badge.dang-su-dung {
  background: #ffebee;
  color: #c62828;
}

/* ================= TOOLTIP DESIGN ================= */
.ban-tooltip {
  visibility: hidden;
  opacity: 0;
  width: 240px;
  background-color: #2d2319;
  color: #fceee0;
  text-align: left;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #c8a374;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  position: absolute;
  z-index: 999;
  bottom: 105%;
  left: 50%;
  transform: translateX(-50%);
  transition:
    opacity 0.25s ease,
    visibility 0.25s ease;
  pointer-events: none;
}

.ban-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  margin-left: -6px;
  border-width: 6px;
  border-style: solid;
  border-color: #2d2319 transparent transparent transparent;
}

.ban-item-wrapper:hover .ban-tooltip {
  visibility: visible;
  opacity: 1;
}

.tooltip-header {
  font-size: 13px;
  color: #ffc875;
  border-bottom: 1px solid rgba(200, 163, 116, 0.3);
  padding-bottom: 6px;
  margin-bottom: 8px;
}

.tooltip-body {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.tooltip-row .label {
  color: #bfa893;
}

.tooltip-row .value {
  color: #ffffff;
  font-weight: 600;
  text-align: right;
}

.tooltip-row .value.highlight {
  color: #ffc875;
}

/* ========== TAB ĐƠN ĐẶT BÀN ========== */
.dat-ban-tabs-section {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}

.tabs-header {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid rgba(0, 0, 0, 0.06);
  padding-bottom: 12px;
}

.tab-btn {
  padding: 10px 24px;
  border: none;
  background: transparent;
  border-radius: 12px 12px 0 0;
  font-weight: 600;
  font-size: 14px;
  color: #8a7a6a;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
}

.tab-btn:hover {
  background: rgba(139, 107, 74, 0.05);
  color: #5f3d22;
}

.tab-btn.active {
  color: #5f3d22;
  background: white;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #8b6b4a, #5f3d22);
  border-radius: 4px;
}

.tab-count {
  background: rgba(139, 107, 74, 0.12);
  padding: 1px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.tab-btn.active .tab-count {
  background: rgba(139, 107, 74, 0.2);
}

.tabs-content {
  padding-top: 16px;
  min-height: 120px;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 0;
  color: #a09080;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* ========== BAN MINI LIST ========== */
.ban-list-mini {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ban-mini-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: white;
  border-radius: 12px;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.ban-mini-item:hover {
  background: #faf6f0;
  transform: translateX(4px);
}

.ban-mini-name {
  font-weight: 500;
  color: #4a3520;
}

.ban-mini-status {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 12px;
  border-radius: 20px;
}

.ban-mini-status.trong {
  background: #e8f5e9;
  color: #2e7d32;
}

.ban-mini-status.da-dat {
  background: #fff3e0;
  color: #e65100;
}

.ban-mini-status.dang-su-dung {
  background: #ffebee;
  color: #c62828;
}

.ban-mini-time {
  font-size: 12px;
  color: #a09080;
}

/* ========== RESPONSIVE ========== */
@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .header-left {
    flex-wrap: wrap;
    gap: 12px;
  }

  .header-right {
    flex-wrap: wrap;
    width: 100%;
  }

  .btn-bills {
    flex: 1;
  }

  .page-title {
    font-size: 20px;
  }

  .ban-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }

  .stats-badge {
    padding: 6px 16px;
    font-size: 13px;
    flex-wrap: wrap;
  }

  .tabs-header {
    flex-direction: column;
    gap: 4px;
  }

  .tab-btn {
    justify-content: center;
    border-radius: 10px;
    padding: 8px 16px;
  }

  .tab-btn.active::after {
    display: none;
  }

  .tab-btn.active {
    background: rgba(139, 107, 74, 0.08);
    box-shadow: none;
  }

  .ban-mini-item {
    flex-wrap: wrap;
    gap: 8px;
  }

  .modal-content {
    max-height: 95vh;
    border-radius: 12px;
  }
}

@media (max-width: 480px) {
  .ban-hang-view {
    padding: 12px;
  }

  .ban-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 8px;
  }

  .ban-item {
    padding: 12px;
  }

  .ban-name {
    font-size: 13px;
  }
}
</style>
