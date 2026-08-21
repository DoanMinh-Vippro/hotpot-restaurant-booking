<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { useShiftStore, type ShiftHistoryEntry, type ShiftInvoice, type ShiftHandoverTable, type ShiftHandoverContext, type ShiftPaymentMethod, normalizeShiftPaymentMethod, isValidShiftInvoice } from '@/stores/ShiftStore'
import HoaDonApi, { type HoaDon, type HoaDonChiTiet } from '@/api/HoaDonApi'
import BanApi from '@/api/BanApi'
import HoaDonDetail from '@/components/HoaDonDetail.vue'
import { printInvoiceReceipt } from '@/utils/printInvoice'

const authStore = useAuthStore()
const shiftStore = useShiftStore()
const router = useRouter()
const activeTab = ref<'history' | 'bills' | 'transactions' | 'report'>('history')
const showOpenModal = ref(false)
const showCloseModal = ref(false)
const showPrintPreviewModal = ref(false)
const isSubmittingCloseShift = ref(false)
const closeMode = ref<'normal' | 'handover'>('handover')
const handoverNote = ref('')
const pendingTables = ref<ShiftHandoverTable[]>([])
const closeShiftError = ref<string | null>(null)
const openingCashInput = ref('1500000')
const transactionForm = ref<{ type: 'income' | 'expense'; paymentMethod: ShiftPaymentMethod; amount: string; reason: string }>({
  type: 'income',
  paymentMethod: 'cash',
  amount: '',
  reason: '',
})
const refreshingBills = ref(false)
const showInvoiceDetailModal = ref(false)
const selectedInvoice = ref<HoaDon | null>(null)
const invoiceDetailItems = ref<HoaDonChiTiet[]>([])
const detailLoading = ref(false)
const showHistoryDetailModal = ref(false)
const selectedHistoryEntry = ref<ShiftHistoryEntry | null>(null)
const editingTransactionId = ref<number | null>(null)

const moneyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
})

const shiftSession = computed(() => shiftStore.currentShift)
const invoiceRevenue = computed(() => shiftStore.invoiceRevenue)
const cashSales = computed(() => shiftStore.cashSales)
const transferSales = computed(() => shiftStore.transferSales)
const electronicSales = computed(() => shiftStore.electronicSales)
const otherSales = computed(() => shiftStore.otherSales)
const grossSales = computed(() => shiftStore.grossSales)
const discountSales = computed(() => shiftStore.discountSales)
const cashIncome = computed(() => shiftStore.cashIncome)
const transferIncome = computed(() => shiftStore.transferIncome)
const electronicIncome = computed(() => shiftStore.electronicIncome)
const otherIncome = computed(() => shiftStore.otherIncome)
const cashExpense = computed(() => shiftStore.cashExpense)
const transferExpense = computed(() => shiftStore.transferExpense)
const electronicExpense = computed(() => shiftStore.electronicExpense)
const otherExpense = computed(() => shiftStore.otherExpense)
const netRevenue = computed(() => cashSales.value + transferSales.value + electronicSales.value + otherSales.value)
const endingCash = computed(() => shiftStore.endingCash)
const hasUnpaidBills = computed(() => shiftStore.hasUnpaidBills)
const hasPendingClosure = computed(() => pendingTables.value.length > 0)
const incomeTransactions = computed(() => (shiftSession.value?.expenses || []).filter((item) => item.type === 'income'))
const expenseTransactions = computed(() => (shiftSession.value?.expenses || []).filter((item) => item.type === 'expense'))
const cashInShift = computed(() => cashSales.value + cashIncome.value - cashExpense.value)
const transferInShift = computed(() => transferSales.value + transferIncome.value - transferExpense.value)
const totalShiftFunds = computed(() => {
  const openingCash = Number(shiftSession.value?.openingCash || 0)
  return openingCash + netRevenue.value + totalIncome.value - totalExpense.value
})
const totalIncome = computed(() => cashIncome.value + transferIncome.value + electronicIncome.value + otherIncome.value)
const totalExpense = computed(() => cashExpense.value + transferExpense.value + electronicExpense.value + otherExpense.value)
const visibleShiftBills = computed(() => (shiftSession.value?.bills || []).filter(isValidShiftInvoice))
const selectedHistoryBills = computed(() => (selectedHistoryEntry.value?.bills || []).filter(isValidShiftInvoice))
const billCount = computed(() => visibleShiftBills.value.length)
const shiftId = computed(() => shiftSession.value?.shiftId || '---')
const openedAt = computed(() => shiftSession.value?.openedAt ? formatDate(shiftSession.value.openedAt) : '---')
const historyEntries = computed(() => {
  const entries = [...(shiftStore.history || [])]
  return entries.sort((a, b) => {
    const timeA = new Date(b.closedAt || b.endTime || b.openedAt).getTime()
    const timeB = new Date(a.closedAt || a.endTime || a.openedAt).getTime()
    return timeA - timeB
  })
})
const getEntryCashInShift = (entry?: ShiftHistoryEntry | null) =>
  Number(entry?.summary?.cashSales || 0) + Number(entry?.summary?.cashIncome || 0) - Number(entry?.summary?.cashExpense || 0)

const formatPaymentBreakdown = (label: string, amount: number) => `${label}: ${moneyFormatter.format(amount)}`
const employeeName = computed(() => {
  const fromShift = shiftSession.value?.employeeName?.trim()
  const fromAuth = authStore.accountName?.trim()
  const fromLocal = localStorage.getItem('tenDangNhap')?.trim()

  return fromShift || fromAuth || fromLocal || 'Nhân viên'
})

const openInvoiceDetail = async (billId: string) => {
  const invoiceId = Number(billId)
  if (!invoiceId) return

  showInvoiceDetailModal.value = true
  detailLoading.value = true

  try {
    const [invoiceRes, detailRes] = await Promise.all([
      HoaDonApi.getById(invoiceId),
      HoaDonApi.getChiTiet(invoiceId),
    ])

    selectedInvoice.value = invoiceRes.data
    invoiceDetailItems.value = Array.isArray(detailRes.data) ? detailRes.data : []
  } catch {
    alert('Không thể tải chi tiết hóa đơn trong ca.')
  } finally {
    detailLoading.value = false
  }
}

const closeInvoiceDetailModal = () => {
  showInvoiceDetailModal.value = false
  selectedInvoice.value = null
  invoiceDetailItems.value = []
}

const openHistoryDetail = (entry: ShiftHistoryEntry) => {
  selectedHistoryEntry.value = entry
  showHistoryDetailModal.value = true
}

const closeHistoryDetailModal = () => {
  showHistoryDetailModal.value = false
  selectedHistoryEntry.value = null
}

const printSelectedInvoice = async (billId: string) => {
  const invoiceId = Number(billId)
  if (!invoiceId) return

  try {
    const [invoiceRes, detailRes] = await Promise.all([
      HoaDonApi.getById(invoiceId),
      HoaDonApi.getChiTiet(invoiceId),
    ])

    printInvoiceReceipt(invoiceRes.data, Array.isArray(detailRes.data) ? detailRes.data : [])
  } catch {
    alert('Không thể in hóa đơn này.')
  }
}

const getShiftSummary = () => ({
  revenue: invoiceRevenue.value,
  totalIncome: cashIncome.value + transferIncome.value + electronicIncome.value + otherIncome.value,
  totalExpense: cashExpense.value + transferExpense.value + electronicExpense.value + otherExpense.value,
  endingCash: endingCash.value,
})

const hasValidInvoiceCode = (invoice: HoaDon) => {
  const code = String(invoice.maHoaDon ?? '').trim()
  return code.length > 0
}

const isMeaningfulPendingTable = (item: ShiftHandoverTable) => {
  const amount = Number(item.total || 0)
  if (!Number.isFinite(amount) || amount <= 0) return false
  if (item.billId && String(item.billId).trim() !== '' && !String(item.code || '').trim()) return false
  return true
}

const closeShiftReport = computed(() => ({
  grossSales: grossSales.value,
  discountSales: discountSales.value,
  netRevenue: netRevenue.value,
  cashSales: cashSales.value,
  transferSales: transferSales.value,
  electronicSales: electronicSales.value,
  otherSales: otherSales.value,
  cashIncome: cashIncome.value,
  transferIncome: transferIncome.value,
  electronicIncome: electronicIncome.value,
  otherIncome: otherIncome.value,
  totalIncome: totalIncome.value,
  cashExpense: cashExpense.value,
  transferExpense: transferExpense.value,
  electronicExpense: electronicExpense.value,
  otherExpense: otherExpense.value,
  totalExpense: totalExpense.value,
  endingCash: endingCash.value,
  cashInShift: cashInShift.value,
  transferInShift: transferInShift.value,
  totalShiftFunds: totalShiftFunds.value,
}))

const resolvePendingTables = async () => {
  try {
    const [tableRes, invoiceRes] = await Promise.all([BanApi.getAll(), HoaDonApi.getDanhSach()])
    const tables = Array.isArray(tableRes.data) ? tableRes.data : []
    const invoices = Array.isArray(invoiceRes.data) ? invoiceRes.data : []

    const pendingMap = new Map<string, ShiftHandoverTable>()

    invoices
      .filter((invoice: HoaDon) => Number(invoice.trangThaiThanhToan) === 0)
      .filter((invoice: HoaDon) => hasValidInvoiceCode(invoice))
      .filter((invoice: HoaDon) => Number(invoice.tongTien || 0) > 0)
      .forEach((invoice: HoaDon) => {
        const key = `invoice-${invoice.idHoaDon}`
        pendingMap.set(key, {
          idBan: invoice.idBan ?? null,
          code: invoice.maHoaDon || `HD-${invoice.idHoaDon}`,
          name: invoice.tenKhachHang || 'Khách lẻ',
          total: Number(invoice.tongTien || 0),
          status: 'unpaid-bill',
          billId: String(invoice.idHoaDon),
        })
      })

    tables.forEach((table: any) => {
      const rawStatus = String(table?.trangThai ?? table?.status ?? '').trim().toUpperCase()
      const isInService =
        rawStatus === 'DANG_SU_DUNG' ||
        rawStatus === 'DANG_PHUC_VU' ||
        rawStatus === 'IN_SERVICE' ||
        rawStatus === 'ĐANG_PHỤC_VỤ' ||
        rawStatus === 'ĐANG_SỬ_DỤNG'

      if (!isInService) return

      const tableTotal = Number(table?.tongTien ?? table?.total ?? 0)
      if (!Number.isFinite(tableTotal) || tableTotal <= 0) return

      const key = `table-${table?.idBan ?? ''}`
      if (!pendingMap.has(key)) {
        pendingMap.set(key, {
          idBan: table?.idBan ?? null,
          code: `Bàn ${table?.tenBan || table?.idBan || '-'}`,
          name: table?.tenBan || 'Bàn đang phục vụ',
          total: tableTotal,
          status: 'in-service',
          billId: null,
        })
      }
    })

    const pending = Array.from(pendingMap.values()).filter(isMeaningfulPendingTable)
    pendingTables.value = pending
    return pending
  } catch {
    pendingTables.value = []
    return []
  }
}

const toTimestamp = (value: string | number[] | null | undefined) => {
  if (!value) return null
  if (typeof value === 'number') return value
  if (Array.isArray(value)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value
    return new Date(year, month - 1, day, hour, minute, second).getTime()
  }

  const normalized = value.replace(' ', 'T')
  const parsed = new Date(normalized)
  return Number.isNaN(parsed.getTime()) ? null : parsed.getTime()
}

const getInvoiceTimestamp = (invoice: HoaDon) => {
  const candidates = [
    invoice.thoiGianXuat,
    (invoice as HoaDon & { createdAt?: string | number[] | null }).createdAt,
    (invoice as HoaDon & { time?: string | number[] | null }).time,
    (invoice as HoaDon & { thoiGian?: string | number[] | null }).thoiGian,
  ]

  for (const candidate of candidates) {
    const parsed = toTimestamp(candidate as string | number[] | null | undefined)
    if (parsed !== null) return parsed
  }

  return null
}

const normalizeShiftBill = (invoice: HoaDon): ShiftInvoice => {
  const gross = Number(invoice.tienTruocGiam ?? (Number(invoice.tongTien || 0) + Number(invoice.tienGiamGia || 0)))
  const discount = Number(invoice.tienGiamGia || 0)
  const paymentMethod = normalizeShiftPaymentMethod(invoice.phuongThucThanhToan)
  const invoiceTime = getInvoiceTimestamp(invoice)

  return {
    id: String(invoice.idHoaDon),
    code: invoice.maHoaDon || `HD-${invoice.idHoaDon}`,
    customer: invoice.tenKhachHang || 'Khách hàng',
    total: Number(invoice.tongTien || 0),
    gross,
    discount,
    paymentMethod,
    status: Number(invoice.trangThaiThanhToan) === 1 ? 'paid' : 'unpaid',
    createdAt: invoiceTime ? new Date(invoiceTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : '---',
    createdAtTimestamp: invoiceTime ?? undefined,
  }
}

const isValidServerShiftInvoice = (invoice: HoaDon) => {
  return Number(invoice.tongTien || 0) > 0 && Number(invoice.trangThaiHoaDon) !== 0
}

const getEffectiveShiftAllocationStart = () => {
  const currentShiftStart = new Date(shiftStore.currentShift?.startTime || shiftStore.currentShift?.openedAt || Date.now()).getTime()
  const historyTimeline = [...(shiftStore.history || [])]
    .map((entry) => {
      const closedAt = toTimestamp(entry.closedAt || entry.endTime)
      return closedAt !== null ? closedAt : null
    })
    .filter((value): value is number => value !== null)
    .sort((a, b) => b - a)

  const latestClosedBoundary = historyTimeline[0] ?? currentShiftStart
  return Number.isFinite(latestClosedBoundary) ? latestClosedBoundary : currentShiftStart
}

const getInvoicePaymentTimestamp = (invoice: HoaDon) => {
  const parsed = toTimestamp(invoice.thoiGianXuat)
  if (parsed !== null) return parsed
  return getInvoiceTimestamp(invoice)
}

const lastHandoverContext = computed(() => shiftStore.history[0]?.handoverContext ?? null)

const loadShiftBillsFromServer = async () => {
  if (!shiftStore.currentShift?.startTime || !shiftStore.currentShift.isOpen) return

  refreshingBills.value = true
  try {
    const shiftStartTime = getEffectiveShiftAllocationStart()
    const shiftEndTime = Date.now()

    if (!Number.isFinite(shiftStartTime)) {
      return
    }

    const res = await HoaDonApi.getDanhSach()
    const invoices = Array.isArray(res.data) ? res.data : []

    // Collect idBan of all handed-over pending tables so their unpaid invoices
    // are always included in this shift regardless of creation timestamp.
    const handoverBanIds = new Set<number>(
      (shiftStore.currentShift.handoverContext?.pendingTables || [])
        .filter((t) => t.idBan != null)
        .map((t) => Number(t.idBan)),
    )
    const handoverBillIds = new Set<string>(
      (shiftStore.currentShift.handoverContext?.pendingTables || [])
        .filter((t) => t.billId && t.billId !== 'null' && t.billId !== '')
        .map((t) => String(t.billId)),
    )

    const serverBills: ShiftInvoice[] = invoices
      .filter((invoice: HoaDon) => isValidServerShiftInvoice(invoice))
      .filter((invoice: HoaDon) => {
        const createdTime = getInvoiceTimestamp(invoice)
        const paymentTime = getInvoicePaymentTimestamp(invoice)
        const isPaid = Number(invoice.trangThaiThanhToan) === 1
        const isHandoverBan = invoice.idBan != null && handoverBanIds.has(Number(invoice.idBan))
        const isHandoverBill = handoverBillIds.has(String(invoice.idHoaDon))

        if (isPaid) {
          const paidInShift = paymentTime !== null && paymentTime >= shiftStartTime && paymentTime <= shiftEndTime
          const createdInShift = createdTime !== null && createdTime >= shiftStartTime && createdTime <= shiftEndTime
          // Handed-over table paid in this shift → count it
          return paidInShift || createdInShift || (isHandoverBan && paidInShift)
        }

        // Always include unpaid invoices for handed-over tables/bills
        if (isHandoverBan || isHandoverBill) return true

        return createdTime !== null && createdTime <= shiftEndTime
      })
      .map((invoice: HoaDon) => normalizeShiftBill(invoice))

    // Merge: update existing entries, append new ones
    const mergedMap = new Map<string, ShiftInvoice>()
    ;(shiftStore.currentShift?.bills || []).forEach((b) => mergedMap.set(b.id, b))
    serverBills.forEach((bill) => {
      // Prefer server data (more up-to-date) over local store stubs
      mergedMap.set(bill.id, { ...(mergedMap.get(bill.id) || {}), ...bill })
    })

    const uniqueBills = Array.from(mergedMap.values()).filter(isValidShiftInvoice)
    uniqueBills.sort((a, b) => (b.createdAtTimestamp ?? 0) - (a.createdAtTimestamp ?? 0))

    if (!shiftStore.currentShift) return
    shiftStore.currentShift.bills = uniqueBills
    localStorage.setItem('restaurant_shift_session', JSON.stringify(shiftStore.currentShift))
  } finally {
    refreshingBills.value = false
  }
}

const openShift = () => {
  const openingCash = Number(openingCashInput.value)

  if (!openingCashInput.value || Number.isNaN(openingCash) || openingCash < 0) {
    alert('Vui lòng nhập số tiền mặt đầu ca hợp lệ.')
    return
  }

  const resolvedEmployeeName =
    authStore.accountName?.trim() ||
    localStorage.getItem('tenDangNhap')?.trim() ||
    authStore.customerInfo.tenKhachHang ||
    authStore.tenKhachHang ||
    'Nhân viên'

  const handoverContext = shiftStore.history[0]?.handoverContext ?? null

  shiftStore.openShift({
    openingCash,
    employeeName: resolvedEmployeeName,
    handoverContext,
  })

  const newShiftId = shiftStore.currentShift?.shiftId
  if (newShiftId && handoverContext?.pendingTables?.length) {
    void Promise.all(
      handoverContext.pendingTables
        .filter((item) => item.billId)
        .map(async (item) => {
          try {
            await HoaDonApi.update(Number(item.billId), { shiftId: newShiftId })
          } catch (error) {
            console.warn('Không thể cập nhật shift cho hóa đơn bàn giao:', error)
          }
        }),
    )

    void Promise.all(
      handoverContext.pendingTables
        .filter((item) => item.idBan != null)
        .map(async (item) => {
          try {
            await BanApi.update(Number(item.idBan), { trangThai: 'DANG_SU_DUNG' })
          } catch (error) {
            console.warn('Không thể cập nhật trạng thái bàn bàn giao:', error)
          }
        }),
    )
  }

  showOpenModal.value = false
  openingCashInput.value = ''

  void loadShiftBillsFromServer()
}

const resetTransactionForm = () => {
  editingTransactionId.value = null
  transactionForm.value = {
    type: 'income',
    paymentMethod: 'cash',
    amount: '',
    reason: '',
  }
}

const startEditTransaction = (item: { id: number; type: 'income' | 'expense'; amount: number; reason: string; paymentMethod: ShiftPaymentMethod }) => {
  editingTransactionId.value = item.id
  transactionForm.value = {
    type: item.type,
    paymentMethod: item.type === 'expense'
      ? ['cash', 'transfer'].includes(item.paymentMethod)
        ? item.paymentMethod
        : 'cash'
      : ['cash', 'transfer', 'electronic', 'other'].includes(item.paymentMethod)
        ? item.paymentMethod
        : 'cash',
    amount: String(item.amount),
    reason: item.reason,
  }
}

const cancelEditTransaction = () => {
  resetTransactionForm()
}

const saveTransaction = () => {
  const amount = Number(transactionForm.value.amount)
  const paymentMethod: ShiftPaymentMethod = transactionForm.value.type === 'expense'
    ? ['cash', 'transfer'].includes(transactionForm.value.paymentMethod)
      ? transactionForm.value.paymentMethod
      : 'cash'
    : ['cash', 'transfer', 'electronic', 'other'].includes(transactionForm.value.paymentMethod)
      ? transactionForm.value.paymentMethod
      : 'cash'

  if (!transactionForm.value.reason.trim() || !transactionForm.value.amount || Number.isNaN(amount) || amount <= 0) {
    alert('Vui lòng nhập số tiền và lý do hợp lệ.')
    return
  }

  if (transactionForm.value.type === 'expense' && amount > Number(shiftStore.invoiceRevenue || 0)) {
    alert('Số tiền chi không được vượt quá tổng doanh thu hiện tại!')
    return
  }

  if (editingTransactionId.value !== null) {
    shiftStore.updateExpenseTransaction({
      id: editingTransactionId.value,
      type: transactionForm.value.type,
      amount,
      reason: transactionForm.value.reason.trim(),
      paymentMethod,
    })
  } else {
    shiftStore.addCashTransaction({
      type: transactionForm.value.type,
      amount,
      reason: transactionForm.value.reason.trim(),
      paymentMethod,
    })
  }

  resetTransactionForm()
}

const openPrintPreview = () => {
  showPrintPreviewModal.value = true
}

const confirmPrintReport = () => {
  showPrintPreviewModal.value = false
  window.print()
}

const removeTransaction = (id: number) => {
  if (!window.confirm('Bạn có chắc muốn xóa khoản thu/chi này khỏi ca hiện tại?')) return

  shiftStore.deleteExpenseTransaction(id)
  if (editingTransactionId.value === id) {
    resetTransactionForm()
  }
}

const startCloseShift = async () => {
  closeShiftError.value = null
  const pending = await resolvePendingTables()
  closeMode.value = pending.length > 0 ? 'handover' : 'normal'
  handoverNote.value = pending.length > 0 ? `Bàn giao ca cho ca sau: ${pending.length} bàn/đơn còn treo.` : ''
  showCloseModal.value = true

  if (pending.length > 0) {
    closeShiftError.value = `Vẫn còn ${pending.length} bàn/đơn đang hoạt động hoặc chưa thanh toán. Vui lòng chọn phương án phù hợp.`
  }
}

const goToSalesScreen = (item?: ShiftHandoverTable) => {
  showCloseModal.value = false
  closeShiftError.value = null

  const target = item || pendingTables.value[0]
  const targetQuery: Record<string, string> = {}

  if (target?.idBan != null) {
    targetQuery.pendingTableId = String(target.idBan)
  }
  if (target?.billId) {
    targetQuery.pendingBillId = String(target.billId)
  }
  if (target?.name) {
    targetQuery.pendingTableName = target.name
  }

  void router.push({ name: 'ban-hang', query: Object.keys(targetQuery).length > 0 ? targetQuery : undefined })
}

const confirmCloseShift = async (keepLoggedIn: boolean) => {
  if (isSubmittingCloseShift.value) return
  isSubmittingCloseShift.value = true

  try {
    const pending = await resolvePendingTables()
    const mode = closeMode.value

    if (mode === 'normal' && pending.length > 0) {
      closeShiftError.value = `Không thể đóng ca ở chế độ thanh toán dứt điểm khi còn ${pending.length} bàn/đơn đang hoạt động hoặc chưa thanh toán. Vui lòng bấm 'Đi tới Bán hàng' để xử lý.`
      return
    }

    const handoverContext: ShiftHandoverContext | null =
      mode === 'handover'
        ? {
            sourceShiftId: shiftSession.value?.shiftId || 'SHIFT-UNKNOWN',
            handoverAt: new Date().toISOString(),
            pendingTables: pending,
            totalPending: pending.reduce((sum, item) => sum + item.total, 0),
            note: handoverNote.value.trim() || undefined,
          }
        : null

    shiftStore.closeShift({
      mode,
      handoverContext,
    })

    showCloseModal.value = false

    if (!keepLoggedIn) {
      authStore.logout()
      router.replace('/auth')
      return
    }

    router.push({ name: 'dat-ban-quan-ly' })
  } finally {
    isSubmittingCloseShift.value = false
  }
}

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })

const formatShiftRange = (start: string, end?: string | null) => {
  if (!start) return '---'
  const endText = end ? ` - ${formatDate(end)}` : ''
  return `${formatDate(start)}${endText}`
}

watch(
  () => [shiftStore.currentShift?.startTime, shiftStore.currentShift?.openedAt, shiftStore.currentShift?.isOpen],
  async () => {
    await loadShiftBillsFromServer()
  },
)

watch(
  () => transactionForm.value.type,
  (type) => {
    if (type === 'expense') {
      if (!['cash', 'transfer'].includes(transactionForm.value.paymentMethod)) {
        transactionForm.value.paymentMethod = 'cash'
      }
    } else if (!['cash', 'transfer', 'electronic', 'other'].includes(transactionForm.value.paymentMethod)) {
      transactionForm.value.paymentMethod = 'cash'
    }
  },
)

onMounted(() => {
  shiftStore.restoreFromStorage()
  void loadShiftBillsFromServer()
})
</script>

<template>
  <div class="shift-page">
    <div class="shift-header">
      <div>
        <p class="eyebrow">Quản trị nhà hàng</p>
        <h1>Quản lý ca</h1>
      </div>
      <button v-if="shiftSession?.isOpen" class="btn-danger" @click="startCloseShift">Đóng ca</button>
    </div>

    <div v-if="!shiftSession?.isOpen && activeTab !== 'history'" class="empty-state">
      <div class="empty-card">
        <div class="empty-icon">🕒</div>
        <h2>Chưa mở ca</h2>
        <p>Chưa có ca làm việc nào được tạo. Hãy mở ca để bắt đầu giao dịch trong ngày.</p>
        <button class="btn-primary" @click="showOpenModal = true">Mở ca</button>
      </div>
    </div>

    <div v-else class="shift-dashboard">
      <section v-if="shiftSession?.isOpen" class="overview-grid">
        <div class="stat-card">
          <span>Thời gian mở</span>
          <strong>{{ formatDate(shiftSession.openedAt) }}</strong>
        </div>
        <div class="stat-card">
          <span>Nhân viên</span>
          <strong>{{ employeeName }}</strong>
        </div>
        <div class="stat-card">
          <span>Số dư đầu ca</span>
          <strong>{{ moneyFormatter.format(shiftSession.openingCash) }}</strong>
        </div>
      </section>

      <section class="tabs-panel">
        <div class="tab-switcher">
          <button :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">Lịch sử ca</button>
          <button :class="{ active: activeTab === 'bills' }" @click="activeTab = 'bills'">Hóa đơn trong ca</button>
          <button :class="{ active: activeTab === 'transactions' }" @click="activeTab = 'transactions'">Thu/Chi phát sinh</button>
          <button :class="{ active: activeTab === 'report' }" @click="activeTab = 'report'">Báo cáo chốt ca</button>
        </div>

        <div v-if="activeTab === 'history'" class="tab-content">
          <div class="table-card">
            <table>
              <thead>
                <tr>
                  <th>Mã ca</th>
                  <th>Nhân viên mở ca</th>
                  <th>Thời gian mở → đóng</th>
                  <th>Số dư đầu ca</th>
                  <th>Doanh thu Gross</th>
                  <th>Giảm giá</th>
                  <th>Doanh thu NET</th>
                  <th>Tiền mặt / Chuyển khoản</th>
                  <th>Tiền mặt cuối ca</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="entry in historyEntries" :key="entry.shiftId">
                  <td>{{ entry.shiftId }}</td>
                  <td>{{ entry.employeeName }}</td>
                  <td>{{ formatShiftRange(entry.startTime, entry.endTime || entry.closedAt) }}</td>
                  <td>{{ moneyFormatter.format(entry.openingCash) }}</td>
                  <td>{{ moneyFormatter.format(entry.summary?.gross ?? 0) }}</td>
                  <td>{{ moneyFormatter.format(entry.summary?.discount ?? 0) }}</td>
                  <td>{{ moneyFormatter.format(entry.summary?.revenue ?? 0) }}</td>
                  <td>
                    <div class="inline-stack">
                      <span>{{ moneyFormatter.format(entry.summary?.cashSales ?? 0) }}</span>
                      <span class="subtle-label">/</span>
                      <span>{{ moneyFormatter.format((entry.summary?.transferSales ?? 0) + (entry.summary?.electronicSales ?? 0) + (entry.summary?.otherSales ?? 0)) }}</span>
                    </div>
                  </td>
                  <td>{{ moneyFormatter.format(entry.summary?.endingCash ?? 0) }}</td>
                  <td>
                    <button class="btn-link" @click="openHistoryDetail(entry)">Xem chi tiết</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else-if="activeTab === 'bills'" class="tab-content">
          <div class="table-card">
            <table>
              <thead>
                <tr>
                  <th>Mã hóa đơn</th>
                  <th>Khách hàng</th>
                  <th>Thời gian</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="bill in visibleShiftBills" :key="bill.id">
                  <td>{{ bill.code }}</td>
                  <td>{{ bill.customer }}</td>
                  <td>{{ bill.createdAt }}</td>
                  <td>{{ moneyFormatter.format(bill.total) }}</td>
                  <td>
                    <span :class="['status-badge', bill.status]">{{ bill.status === 'paid' ? 'Đã thanh toán' : 'Chưa thanh toán' }}</span>
                  </td>
                  <td>
                    <button class="btn-link" @click="openInvoiceDetail(bill.id)">Xem chi tiết</button>
                    <button class="btn-link" @click="printSelectedInvoice(bill.id)">In lại</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else-if="activeTab === 'transactions'" class="tab-content transaction-layout">
          <div class="form-card">
            <h3>{{ editingTransactionId !== null ? 'Cập nhật khoản thu/chi phát sinh' : 'Thêm khoản thu/chi phát sinh' }}</h3>
            <div class="form-grid">
              <select v-model="transactionForm.type">
                <option value="income">Thu</option>
                <option value="expense">Chi</option>
              </select>
              <select v-model="transactionForm.paymentMethod">
                <option value="cash">Tiền mặt</option>
                <option value="transfer">Tài khoản / Chuyển khoản</option>
                <option v-if="transactionForm.type === 'income'" value="electronic">Thanh toán điện tử</option>
                <option v-if="transactionForm.type === 'income'" value="other">Khác</option>
              </select>
              <input v-model="transactionForm.amount" type="number" min="0" placeholder="Nhập số tiền" />
              <input v-model="transactionForm.reason" type="text" placeholder="Nhập lý do" />
            </div>
            <div class="modal-actions split">
              <button class="btn-primary" @click="saveTransaction">{{ editingTransactionId !== null ? 'Cập nhật' : 'Lưu giao dịch' }}</button>
              <button v-if="editingTransactionId !== null" class="btn-secondary" @click="cancelEditTransaction">Hủy</button>
            </div>
          </div>

          <div class="table-card">
            <table>
              <thead>
                <tr>
                  <th>Loại</th>
                  <th>Số tiền</th>
                  <th>Lý do</th>
                  <th>Thời gian</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in shiftSession?.expenses || []" :key="item.id">
                  <td>
                    <span :class="['status-badge', item.type]">{{ item.type === 'income' ? 'Thu' : 'Chi' }}</span>
                  </td>
                  <td>{{ moneyFormatter.format(item.amount) }}</td>
                  <td>{{ item.reason }} <span class="payment-method-tag">{{ item.paymentMethod === 'cash' ? 'Tiền mặt' : item.paymentMethod === 'transfer' ? 'Tài khoản / Chuyển khoản' : item.paymentMethod === 'electronic' ? 'Thanh toán điện tử' : 'Khác' }}</span></td>
                  <td>{{ item.createdAt }}</td>
                  <td>
                    <div class="transaction-actions">
                      <button class="action-btn" @click="startEditTransaction(item)" title="Sửa">
                        ✎
                      </button>
                      <button class="action-btn danger" @click="removeTransaction(item.id)" title="Xóa">
                        🗑
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else-if="activeTab === 'report'" class="tab-content report-layout">
          <div class="report-stack">
            <section class="report-section">
              <div class="section-title">THÔNG TIN CHUNG</div>
              <div class="section-body">
                <div class="metric-row">
                  <span>Mã ca</span>
                  <strong>{{ shiftId }}</strong>
                </div>
                <div class="metric-row">
                  <span>Giờ mở ca</span>
                  <strong>{{ openedAt }}</strong>
                </div>
                <div class="metric-row">
                  <span>Nhân viên</span>
                  <strong>{{ employeeName }}</strong>
                </div>
              </div>
            </section>

            <section class="report-section">
              <div class="section-title">KHỐI BÁN HÀNG (SALES)</div>
              <div class="section-body">
                <div class="metric-row">
                  <span>Số hóa đơn</span>
                  <strong>{{ billCount }}</strong>
                </div>
                <div class="metric-row">
                  <span>Doanh thu (Gross)</span>
                  <strong>{{ moneyFormatter.format(grossSales) }}</strong>
                </div>
                <div class="metric-row">
                  <span>Tổng giảm giá</span>
                  <strong>{{ moneyFormatter.format(discountSales) }}</strong>
                </div>
                <div class="metric-row net-row">
                  <span>Doanh thu (NET)</span>
                  <strong>{{ moneyFormatter.format(netRevenue) }}</strong>
                </div>
                <div class="payment-split">
                  <div class="payment-pill">
                    <span>+ Tiền mặt</span>
                    <strong>{{ moneyFormatter.format(cashSales) }}</strong>
                  </div>
                  <div class="payment-pill">
                    <span>+ Chuyển khoản</span>
                    <strong>{{ moneyFormatter.format(transferSales) }}</strong>
                  </div>
                  <div class="payment-pill">
                    <span>+ Thanh toán điện tử</span>
                    <strong>{{ moneyFormatter.format(electronicSales) }}</strong>
                  </div>
                  <div class="payment-pill">
                    <span>+ Khác</span>
                    <strong>{{ moneyFormatter.format(otherSales) }}</strong>
                  </div>
                </div>
              </div>
            </section>

            <section class="report-section">
              <div class="section-title">KHỐI THU - CHI KHÁC (INCOME / EXPENSE)</div>
              <div class="income-expense-grid">
                <div class="mini-card">
                  <div class="mini-title">THU</div>
                  <div class="mini-meta">
                    <span>Số lượng</span>
                    <strong>{{ incomeTransactions.length }}</strong>
                  </div>
                  <div class="mini-meta">
                    <span>Tiền mặt</span>
                    <strong>{{ moneyFormatter.format(cashIncome) }}</strong>
                  </div>
                  <div class="mini-meta">
                    <span>Chuyển khoản</span>
                    <strong>{{ moneyFormatter.format(transferIncome) }}</strong>
                  </div>
                  <ul class="mini-list">
                    <li v-for="item in incomeTransactions" :key="item.id">
                      <span>{{ item.reason }}</span>
                      <strong>{{ moneyFormatter.format(item.amount) }}</strong>
                    </li>
                  </ul>
                </div>
                <div class="mini-card">
                  <div class="mini-title">CHI</div>
                  <div class="mini-meta">
                    <span>Số lượng</span>
                    <strong>{{ expenseTransactions.length }}</strong>
                  </div>
                  <div class="mini-meta">
                    <span>Tiền mặt</span>
                    <strong>{{ moneyFormatter.format(cashExpense) }}</strong>
                  </div>
                  <div class="mini-meta">
                    <span>Chuyển khoản</span>
                    <strong>{{ moneyFormatter.format(0) }}</strong>
                  </div>
                  <ul class="mini-list">
                    <li v-for="item in expenseTransactions" :key="item.id">
                      <span>{{ item.reason }}</span>
                      <strong>{{ moneyFormatter.format(item.amount) }}</strong>
                    </li>
                  </ul>
                </div>
              </div>
            </section>

            <section class="report-section handover-section">
              <div class="section-title">KHỐI BÀN GIAO CA (HANDOVER)</div>
              <div class="section-body">
                <div class="metric-row">
                  <span>Tiền mặt đầu ca</span>
                  <strong>{{ moneyFormatter.format(shiftSession?.openingCash || 0) }}</strong>
                </div>
                <div class="metric-row">
                  <span>Tiền mặt trong ca</span>
                  <strong>{{ moneyFormatter.format(cashInShift) }}</strong>
                </div>
                <div class="metric-row">
                  <span>Tổng tiền chuyển khoản</span>
                  <strong>{{ moneyFormatter.format(transferInShift) }}</strong>
                </div>
                <div class="metric-row">
                  <span>Tổng tiền chung</span>
                  <strong>{{ moneyFormatter.format(totalShiftFunds) }}</strong>
                </div>
                <div class="metric-row">
                  <span>Tiền mặt thực tế trong két</span>
                  <strong>{{ moneyFormatter.format(endingCash) }}</strong>
                </div>
                <div class="metric-row handover-row">
                  <span>Tiền mặt cuối ca (cash drawer)</span>
                  <strong>{{ moneyFormatter.format(endingCash) }}</strong>
                </div>
                <button class="btn-report-preview" type="button" @click="openPrintPreview">In báo cáo</button>
              </div>
            </section>
          </div>
        </div>
      </section>
    </div>

    <div v-if="showOpenModal" class="modal-overlay" @click.self="showOpenModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>Mở ca làm việc</h3>
          <button class="close-btn" @click="showOpenModal = false">×</button>
        </div>
        <div v-if="lastHandoverContext" class="handover-alert" style="margin-bottom: 12px;">
          📌 Ca trước đã bàn giao: {{ lastHandoverContext.pendingTables?.length || 0 }} bàn/đơn còn treo.
        </div>
        <label class="field-label">Tiền mặt đầu ca</label>
        <input v-model="openingCashInput" type="number" min="0" placeholder="Nhập số tiền mặt đầu ca" />
        <div class="modal-actions">
          <button class="btn-secondary" @click="showOpenModal = false">Hủy</button>
          <button class="btn-primary" @click="openShift">Xác nhận mở ca</button>
        </div>
      </div>
    </div>

    <div v-if="showInvoiceDetailModal" class="modal-overlay" @click.self="closeInvoiceDetailModal">
      <div class="modal-card wide detail-modal-card">
        <div class="modal-header">
          <h3>Chi tiết hóa đơn</h3>
          <button class="close-btn" @click="closeInvoiceDetailModal">×</button>
        </div>

        <HoaDonDetail :selected-hoa-don="selectedInvoice || undefined" :chi-tiets="invoiceDetailItems" :loading="detailLoading" />
      </div>
    </div>

    <div v-if="showHistoryDetailModal" class="modal-overlay" @click.self="closeHistoryDetailModal">
      <div class="modal-card wide detail-modal-card">
        <div class="modal-header">
          <h3>Chi tiết ca {{ selectedHistoryEntry?.shiftId }}</h3>
          <button class="close-btn" @click="closeHistoryDetailModal">×</button>
        </div>

        <div v-if="selectedHistoryEntry" class="history-detail-card">
          <section class="report-section">
            <div class="section-title">THÔNG TIN CHUNG</div>
            <div class="section-body">
              <div class="metric-row">
                <span>Mã ca</span>
                <strong>{{ selectedHistoryEntry.shiftId }}</strong>
              </div>
              <div class="metric-row">
                <span>Nhân viên mở ca</span>
                <strong>{{ selectedHistoryEntry.employeeName }}</strong>
              </div>
              <div class="metric-row">
                <span>Thời gian mở - đóng</span>
                <strong>{{ formatShiftRange(selectedHistoryEntry.startTime, selectedHistoryEntry.endTime || selectedHistoryEntry.closedAt) }}</strong>
              </div>
              <div class="metric-row">
                <span>Số dư đầu ca</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.openingCash || 0) }}</strong>
              </div>
            </div>
          </section>

          <section class="report-section">
            <div class="section-title">KHỐI DOANH THU CHI TIẾT</div>
            <div class="section-body">
              <div class="metric-row">
                <span>Doanh thu Gross</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.gross || 0) }}</strong>
              </div>
              <div class="metric-row">
                <span>Tổng giảm giá</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.discount || 0) }}</strong>
              </div>
              <div class="metric-row net-row">
                <span>Doanh thu NET</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.revenue || 0) }}</strong>
              </div>
              <div class="payment-split">
                <div class="payment-pill">
                  <span>+ Tiền mặt trong ca</span>
                  <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.cashSales || 0) }}</strong>
                </div>
                <div class="payment-pill">
                  <span>+ Chuyển khoản trong ca</span>
                  <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.transferSales || 0) }}</strong>
                </div>
              </div>
            </div>
          </section>

          <section class="report-section">
            <div class="section-title">KHỐI THU - CHI KHÁC (INCOME / EXPENSE)</div>
            <div class="income-expense-grid">
              <div class="mini-card">
                <div class="mini-title">THU</div>
                <div class="mini-meta">
                  <span>Số lượng</span>
                  <strong>{{ (selectedHistoryEntry.expenses || []).filter((item) => item.type === 'income').length }}</strong>
                </div>
                <div class="mini-meta">
                  <span>Tiền mặt</span>
                  <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.cashIncome || 0) }}</strong>
                </div>
                <div class="mini-meta">
                  <span>Chuyển khoản</span>
                  <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.transferIncome || 0) }}</strong>
                </div>
                <ul class="mini-list">
                  <li v-for="item in (selectedHistoryEntry.expenses || []).filter((entry) => entry.type === 'income')" :key="item.id">
                    <span>{{ item.reason }}</span>
                    <strong>{{ moneyFormatter.format(item.amount) }}</strong>
                  </li>
                </ul>
              </div>
              <div class="mini-card">
                <div class="mini-title">CHI</div>
                <div class="mini-meta">
                  <span>Số lượng</span>
                  <strong>{{ (selectedHistoryEntry.expenses || []).filter((item) => item.type === 'expense').length }}</strong>
                </div>
                <div class="mini-meta">
                  <span>Tiền mặt</span>
                  <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.cashExpense || 0) }}</strong>
                </div>
                <div class="mini-meta">
                  <span>Chuyển khoản</span>
                  <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.transferExpense || 0) }}</strong>
                </div>
                <ul class="mini-list">
                  <li v-for="item in (selectedHistoryEntry.expenses || []).filter((entry) => entry.type === 'expense')" :key="item.id">
                    <span>{{ item.reason }}</span>
                    <strong>{{ moneyFormatter.format(item.amount) }}</strong>
                  </li>
                </ul>
              </div>
            </div>
          </section>

          <section class="report-section handover-section">
            <div class="section-title">KHỐI BÀN GIAO CA (HANDOVER)</div>
            <div class="section-body">
              <div class="metric-row">
                <span>Số dư đầu ca</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.openingCash || 0) }}</strong>
              </div>
              <div class="metric-row">
                <span>Tiền mặt trong ca</span>
                <strong>{{ moneyFormatter.format(getEntryCashInShift(selectedHistoryEntry)) }}</strong>
              </div>
              <div class="metric-row handover-row">
                <span>Tiền mặt cuối ca</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.summary?.endingCash || 0) }}</strong>
              </div>
            </div>
          </section>

          <section v-if="selectedHistoryEntry.handoverContext" class="report-section">
            <div class="section-title">BIÊN BẢN BÀN GIAO CA</div>
            <div class="section-body">
              <div class="metric-row">
                <span>Mã ca nguồn</span>
                <strong>{{ selectedHistoryEntry.handoverContext.sourceShiftId }}</strong>
              </div>
              <div class="metric-row">
                <span>Thời gian bàn giao</span>
                <strong>{{ formatDate(selectedHistoryEntry.handoverContext.handoverAt) }}</strong>
              </div>
              <div class="metric-row">
                <span>Tổng tiền bàn giao</span>
                <strong>{{ moneyFormatter.format(selectedHistoryEntry.handoverContext.totalPending || 0) }}</strong>
              </div>
              <div v-if="selectedHistoryEntry.handoverContext.note" class="metric-row">
                <span>Ghi chú</span>
                <strong>{{ selectedHistoryEntry.handoverContext.note }}</strong>
              </div>
              <div v-if="selectedHistoryEntry.handoverContext.pendingTables?.length" class="pending-table-list">
                <div class="section-title">Danh sách hóa đơn còn treo</div>
                <ul>
                  <li v-for="item in selectedHistoryEntry.handoverContext.pendingTables" :key="`${item.idBan}-${item.billId}`">
                    <span>{{ item.code }} · {{ item.name || 'Khách lẻ' }}</span>
                    <strong>{{ moneyFormatter.format(item.total) }}</strong>
                  </li>
                </ul>
              </div>
            </div>
          </section>

          <section class="report-section">
            <div class="section-title">DANH SÁCH HÓA ĐƠN ĐÃ GÁN VÀO CA</div>
            <div class="table-card nested-table-card">
              <table>
                <thead>
                  <tr>
                    <th>Mã hóa đơn</th>
                    <th>Khách hàng</th>
                    <th>Thời gian</th>
                    <th>Tổng tiền</th>
                    <th>Phương thức</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="bill in selectedHistoryBills" :key="bill.id">
                    <td>{{ bill.code }}</td>
                    <td>{{ bill.customer }}</td>
                    <td>{{ bill.createdAt }}</td>
                    <td>{{ moneyFormatter.format(bill.total) }}</td>
                    <td>{{ bill.paymentMethod === 'cash' ? 'Tiền mặt' : 'Chuyển khoản' }}</td>
                    <td>
                      <span :class="['status-badge', bill.status]">{{ bill.status === 'paid' ? 'Đã thanh toán' : 'Chưa thanh toán' }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </div>
    </div>

    <div v-if="showCloseModal" class="modal-overlay" @click.self="showCloseModal = false">
      <div class="modal-card wide">
        <div class="modal-header">
          <h3>Xác nhận chốt ca</h3>
          <button class="close-btn" @click="showCloseModal = false">×</button>
        </div>

        <div class="summary-grid">
          <div>
            <span>Doanh thu (NET)</span>
            <strong>{{ moneyFormatter.format(netRevenue) }}</strong>
          </div>
          <div>
            <span>Tổng tiền mặt</span>
            <strong>{{ moneyFormatter.format(endingCash) }}</strong>
          </div>
          <div>
            <span>Tổng tiền chuyển khoản</span>
            <strong>{{ moneyFormatter.format(transferInShift) }}</strong>
          </div>
          <div>
            <span>Tổng tiền chung</span>
            <strong>{{ moneyFormatter.format(totalShiftFunds) }}</strong>
          </div>
        </div>

        <div v-if="closeShiftError" class="handover-alert">{{ closeShiftError }}</div>

        <div class="report-review-panel">
          <div class="section-title">BÁO CÁO CHỐT CA (kiểm tra lại trước khi xác nhận)</div>
          <div class="review-report-grid">
            <div class="review-item">
              <span>Doanh thu gross</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.grossSales) }}</strong>
            </div>
            <div class="review-item">
              <span>Giảm giá</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.discountSales) }}</strong>
            </div>
            <div class="review-item emphasis">
              <span>Doanh thu NET</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.netRevenue) }}</strong>
            </div>
            <div class="review-item">
              <span>Tiền mặt</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.cashSales) }}</strong>
            </div>
            <div class="review-item">
              <span>Chuyển khoản</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.transferSales + closeShiftReport.electronicSales + closeShiftReport.otherSales) }}</strong>
            </div>
            <div class="review-item">
              <span>Thu phát sinh</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.totalIncome) }}</strong>
            </div>
            <div class="review-item">
              <span>Chi phát sinh</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.totalExpense) }}</strong>
            </div>
            <div class="review-item">
              <span>Tiền mặt thực tế</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.endingCash) }}</strong>
            </div>
            <div class="review-item">
              <span>Tổng tiền chung</span>
              <strong>{{ moneyFormatter.format(closeShiftReport.totalShiftFunds) }}</strong>
            </div>
          </div>
        </div>

        <div class="handover-options">
          <p class="handover-hint">Chọn một trong hai cách đóng ca:</p>
          <div class="mode-choice-row">
            <label class="mode-option">
              <input v-model="closeMode" type="radio" value="normal" />
              <span>Thanh toán dứt điểm trước</span>
            </label>
            <label class="mode-option">
              <input v-model="closeMode" type="radio" value="handover" />
              <span>Bàn giao sang ca sau</span>
            </label>
          </div>
          <div v-if="closeMode === 'normal' && pendingTables.length > 0" class="handover-alert">
            Hãy chuyển sang màn hình Bán hàng để thanh toán nốt các bàn treo trước khi đóng ca.
          </div>
          <textarea v-if="pendingTables.length > 0 && closeMode === 'handover'" v-model="handoverNote" rows="3" placeholder="Ghi chú bàn giao ca tiếp theo..."></textarea>
        </div>

        <div v-if="pendingTables.length > 0" class="pending-table-list">
          <div class="section-title">DANH SÁCH HÓA ĐƠN CÒN TREO (Click để xử lý)</div>
          <ul>
            <li
              v-for="item in pendingTables"
              :key="`${item.idBan}-${item.billId}`"
              class="pending-table-item-clickable"
              @click="goToSalesScreen(item)"
              title="Click để tới màn hình Bán hàng xử lý hóa đơn này"
              style="cursor: pointer;"
            >
              <span>{{ item.code }} · {{ item.name || 'Khách lẻ' }}</span>
              <div class="pending-item-action">
                <strong>{{ moneyFormatter.format(item.total) }}</strong>
                <span class="action-link-text">Xử lý ngay →</span>
              </div>
            </li>
          </ul>
        </div>

        <button class="btn-report-preview compact" type="button" @click="openPrintPreview">In báo cáo</button>

        <div class="modal-actions split">
          <template v-if="closeMode === 'normal'">
            <button
              v-if="pendingTables.length > 0"
              class="btn-primary"
              :disabled="isSubmittingCloseShift"
              @click="goToSalesScreen()"
            >
              Đi tới Bán hàng
            </button>
            <button
              v-else
              class="btn-primary"
              :disabled="isSubmittingCloseShift"
              @click="confirmCloseShift(true)"
            >
              {{ isSubmittingCloseShift ? 'Đang xử lý...' : 'Đóng ca (thanh toán xong)' }}
            </button>
          </template>

          <template v-else-if="closeMode === 'handover'">
            <button
              class="btn-primary"
              :disabled="isSubmittingCloseShift"
              @click="confirmCloseShift(true)"
            >
              {{ isSubmittingCloseShift ? 'Đang xử lý...' : 'Đóng ca bàn giao' }}
            </button>
            <button
              class="btn-danger"
              :disabled="isSubmittingCloseShift"
              @click="confirmCloseShift(false)"
            >
              {{ isSubmittingCloseShift ? 'Đang xử lý...' : 'Đóng ca và đăng xuất' }}
            </button>
          </template>
        </div>
      </div>
    </div>

    <div v-if="showPrintPreviewModal" class="modal-overlay" @click.self="showPrintPreviewModal = false">
      <div class="modal-card wide print-preview-card">
        <div class="modal-header">
          <h3>Xem trước báo cáo chốt ca</h3>
          <button class="close-btn" @click="showPrintPreviewModal = false">×</button>
        </div>

        <div class="preview-grid">
          <section class="report-section">
            <div class="section-title">TỔNG QUAN CA</div>
            <div class="section-body">
              <div class="metric-row">
                <span>Mã ca</span>
                <strong>{{ shiftId }}</strong>
              </div>
              <div class="metric-row">
                <span>Thời gian mở ca</span>
                <strong>{{ openedAt }}</strong>
              </div>
              <div class="metric-row">
                <span>Nhân viên</span>
                <strong>{{ employeeName }}</strong>
              </div>
              <div class="metric-row">
                <span>Số dư đầu ca</span>
                <strong>{{ moneyFormatter.format(shiftSession?.openingCash || 0) }}</strong>
              </div>
            </div>
          </section>

          <section class="report-section">
            <div class="section-title">DÒNG TIỀN</div>
            <div class="section-body">
              <div class="metric-row">
                <span>Doanh thu tiền mặt</span>
                <strong>{{ moneyFormatter.format(cashSales) }}</strong>
              </div>
              <div class="metric-row">
                <span>Doanh thu chuyển khoản</span>
                <strong>{{ moneyFormatter.format(transferSales) }}</strong>
              </div>
              <div class="metric-row">
                <span>Thu phát sinh</span>
                <strong>{{ moneyFormatter.format(totalIncome) }}</strong>
              </div>
              <div class="metric-row">
                <span>Chi phát sinh</span>
                <strong>{{ moneyFormatter.format(totalExpense) }}</strong>
              </div>
              <div class="metric-row">
                <span>Tiền mặt thực tế trong két</span>
                <strong>{{ moneyFormatter.format(endingCash) }}</strong>
              </div>
              <div class="metric-row handover-row">
                <span>Tổng tiền chung</span>
                <strong>{{ moneyFormatter.format(totalShiftFunds) }}</strong>
              </div>
            </div>
          </section>
        </div>

        <div class="modal-actions split">
          <button class="btn-secondary" type="button" @click="showPrintPreviewModal = false">Hủy/Quay lại</button>
          <button class="btn-primary" type="button" @click="confirmPrintReport">Xác nhận in / In báo cáo</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.shift-page {
  padding: 24px 24px 56px;
  color: #5d3a1f;
  max-height: calc(100vh - 110px);
  overflow-y: auto;
  overflow-x: hidden;
  scroll-behavior: smooth;
}

.shift-dashboard {
  display: grid;
  gap: 18px;
  align-content: start;
}

.shift-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 4px;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #9a6a38;
}

h1,
h2,
h3,
p {
  margin: 0;
}

.empty-state {
  min-height: 420px;
  display: grid;
  place-items: center;
}

.empty-card,
.table-card,
.form-card,
.report-card,
.formula-box,
.stat-card,
.modal-card {
  background: rgba(255, 248, 234, 0.95);
  border: 1px solid #e7cfaa;
  border-radius: 16px;
  box-shadow: 0 12px 24px rgba(116, 80, 35, 0.08);
}

.empty-card {
  max-width: 520px;
  text-align: center;
  padding: 32px;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 12px;
}

.btn-primary,
.btn-secondary,
.btn-danger,
.btn-link,
.tab-switcher button {
  border: none;
  cursor: pointer;
  font-weight: 700;
}

.btn-primary {
  background: linear-gradient(135deg, #a76b2c, #c58f4c);
  color: #fffaf3;
  border-radius: 10px;
  padding: 10px 16px;
}

.btn-secondary {
  background: #efe1c6;
  color: #633f1d;
  border-radius: 10px;
  padding: 10px 16px;
}

.btn-danger {
  background: #8f3b2f;
  color: #fff;
  border-radius: 10px;
  padding: 10px 16px;
}

.btn-link {
  background: transparent;
  color: #8a5724;
  padding: 0 8px 0 0;
}

.btn-report-preview {
  width: fit-content;
  border: 1px solid #b57b3e;
  background: #fff8ec;
  color: #7b4a22;
  border-radius: 8px;
  padding: 7px 12px;
  font-size: 0.86rem;
  font-weight: 800;
  cursor: pointer;
}

.btn-report-preview.compact {
  margin-top: 14px;
  padding: 6px 10px;
  font-size: 0.82rem;
}

.report-review-panel {
  margin-top: 18px;
  margin-bottom: 18px;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #edd7af;
  background: rgba(255, 249, 240, 0.85);
}

.review-report-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.review-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #f0dfbe;
  color: #68421e;
}

.review-item.emphasis {
  background: #fff5e7;
  border-color: #d69d58;
}

.review-item span {
  font-size: 0.82rem;
}

.review-item strong {
  font-size: 0.82rem;
}

.shift-dashboard {
  display: grid;
  gap: 20px;
}

.overview-grid,
.summary-grid,
.report-grid {
  display: grid;
  gap: 14px;
}

.overview-grid {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.summary-grid,
.report-grid {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.stat-card,
.report-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-card span,
.report-card span,
.summary-grid span {
  font-size: 0.85rem;
  color: #92663d;
}

.stat-card strong,
.report-card strong,
.summary-grid strong {
  font-size: 1.1rem;
}

.tabs-panel {
  display: grid;
  gap: 16px;
}

.tab-switcher {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-switcher button {
  padding: 10px 14px;
  border-radius: 999px;
  background: #eee0c0;
  color: #6b4422;
}

.tab-switcher button.active {
  background: #a76b2c;
  color: #fffaf3;
}

.tab-content {
  display: grid;
  gap: 16px;
}

.transaction-layout {
  grid-template-columns: 320px 1fr;
}

.table-card,
.form-card,
.formula-box {
  padding: 16px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

.transaction-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  border: 1px solid #d9b27c;
  background: #fff7ea;
  color: #7b4b1f;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.95rem;
}

.action-btn.danger {
  border-color: #b87162;
  color: #8c3d2f;
}

th,
td {
  border-bottom: 1px solid #ebd4ab;
  text-align: left;
  padding: 10px 8px;
  font-size: 0.92rem;
}

th {
  color: #8a5724;
}

.form-grid {
  display: grid;
  gap: 10px;
  margin: 12px 0 16px;
}

input,
select {
  width: 100%;
  border: 1px solid #d8b180;
  border-radius: 10px;
  background: #fffdf7;
  color: #5d3a1f;
  padding: 10px 12px;
  box-sizing: border-box;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}

.status-badge.paid,
.status-badge.income {
  background: #dff2e2;
  color: #24643a;
}

.status-badge.unpaid,
.status-badge.expense {
  background: #f9e2de;
  color: #943d2c;
}

.report-card.highlight {
  background: linear-gradient(135deg, #8f5a29, #b57b3e);
  color: #fffaf3;
}

.report-card.highlight span {
  color: #f7e6cf;
}

.formula-box {
  display: grid;
  gap: 10px;
  background: #fff7ea;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(58, 35, 19, 0.45);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 1000;
  align-items: flex-start;
}

.modal-card {
  width: min(460px, 100%);
  max-width: 100%;
  padding: 18px;
  box-sizing: border-box;
}

.modal-card.wide {
  width: min(1000px, 94vw);
  max-width: 1000px;
}

.print-preview-card {
  max-height: 86vh;
  overflow-y: auto;
}

.preview-grid {
  display: grid;
  gap: 12px;
}

.detail-modal-card {
  max-height: min(88vh, 920px);
  overflow-y: auto;
  overflow-x: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.close-btn {
  background: transparent;
  border: none;
  color: #7b4a22;
  font-size: 1.6rem;
  cursor: pointer;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 700;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.modal-actions.split {
  justify-content: space-between;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  flex-wrap: nowrap;
  min-width: 0;
}

.report-layout {
  display: grid;
  align-content: start;
}

.report-stack {
  display: grid;
  gap: 14px;
  width: min(100%, 1100px);
  margin: 0 auto;
  align-content: start;
}

.report-section {
  background: rgba(255, 248, 234, 0.95);
  border: 1px solid #e8d0a6;
  border-radius: 18px;
  box-shadow: 0 10px 20px rgba(116, 80, 35, 0.08);
  overflow: hidden;
}

.section-title {
  background: linear-gradient(135deg, #8f5a29, #b57b3e);
  color: #fffaf3;
  font-size: 0.86rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  padding: 12px 16px;
  text-transform: uppercase;
}

.section-body {
  padding: 16px;
  display: grid;
  gap: 10px;
  min-width: 0;
}

.metric-row,
.mini-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed #ebd4ab;
  min-height: 42px;
}

.metric-row:last-child,
.mini-meta:last-child {
  border-bottom: none;
}

.net-row {
  font-size: 1rem;
  font-weight: 800;
  color: #5d3a1f;
}

.payment-split {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.payment-pill {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #fff9ee;
  border: 1px solid #ead4ad;
  border-radius: 12px;
  padding: 10px 12px;
}

.income-expense-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.mini-card {
  background: #fffdf8;
  border: 1px solid #ead4ad;
  border-radius: 14px;
  padding: 14px;
  display: grid;
  gap: 8px;
}

.mini-title {
  font-weight: 800;
  color: #8a5724;
}

.mini-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 6px;
}

.mini-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 0.88rem;
  color: #6a4424;
  flex-wrap: wrap;
}

.handover-row {
  border: 2px solid #8a5724;
  border-radius: 12px;
  padding: 12px;
  background: #fff6e8;
  font-weight: 800;
}

.payment-method-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  margin-left: 6px;
  font-size: 0.72rem;
  background: #f3e4c8;
  color: #7d4c1f;
}

@media (max-width: 900px) {
  .transaction-layout {
    grid-template-columns: 1fr;
  }

  .shift-header {
    flex-direction: column;
    align-items: stretch;
  }

  .report-stack {
    width: 100%;
  }
}

.handover-alert {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff2cc;
  color: #8a4b00;
  border: 1px solid #e8c06b;
}

.handover-options {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.handover-hint {
  color: #8b5b2c;
  font-size: 0.92rem;
}

.mode-choice-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pending-table-list {
  margin-top: 10px;
  display: grid;
  gap: 8px;
}

.pending-table-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 8px;
}

.pending-table-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: 1px solid #ead4ad;
  border-radius: 10px;
  background: #fff9eb;
  transition: all 0.2s ease;
}

.pending-table-item-clickable:hover {
  background: #fdeccb;
  border-color: #c58f4c;
  transform: translateY(-1px);
}

.pending-item-action {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-link-text {
  font-size: 0.8rem;
  font-weight: 700;
  color: #a76b2c;
  background: #f3e3ca;
  padding: 3px 8px;
  border-radius: 6px;
}

textarea {
  width: 100%;
  min-height: 90px;
  border: 1px solid #d8b180;
  border-radius: 10px;
  background: #fffdf7;
  color: #5d3a1f;
  padding: 10px 12px;
  box-sizing: border-box;
  resize: vertical;
}

.history-detail-card {
  display: grid;
  gap: 8px;
}
</style>
