<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { useShiftStore, type ShiftHistoryEntry, type ShiftInvoice } from '@/stores/ShiftStore'
import HoaDonApi, { type HoaDon, type HoaDonChiTiet } from '@/api/HoaDonApi'
import HoaDonDetail from '@/components/HoaDonDetail.vue'
import { printInvoiceReceipt } from '@/utils/printInvoice'

const authStore = useAuthStore()
const shiftStore = useShiftStore()
const router = useRouter()
const activeTab = ref<'history' | 'bills' | 'transactions' | 'report'>('history')
const showOpenModal = ref(false)
const showCloseModal = ref(false)
const openingCashInput = ref('1500000')
const transactionForm = ref({
  type: 'income' as 'income' | 'expense',
  paymentMethod: 'cash' as 'cash' | 'transfer',
  amount: '',
  reason: '',
})
const printSummary = ref(true)
const refreshingBills = ref(false)
const showInvoiceDetailModal = ref(false)
const selectedInvoice = ref<HoaDon | null>(null)
const invoiceDetailItems = ref<HoaDonChiTiet[]>([])
const detailLoading = ref(false)
const showHistoryDetailModal = ref(false)
const selectedHistoryEntry = ref<ShiftHistoryEntry | null>(null)

const moneyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
})

const shiftSession = computed(() => shiftStore.currentShift)
const invoiceRevenue = computed(() => shiftStore.invoiceRevenue)
const cashSales = computed(() => shiftStore.cashSales)
const transferSales = computed(() => shiftStore.transferSales)
const grossSales = computed(() => shiftStore.grossSales)
const discountSales = computed(() => shiftStore.discountSales)
const cashIncome = computed(() => shiftStore.cashIncome)
const transferIncome = computed(() => shiftStore.transferIncome)
const cashExpense = computed(() => shiftStore.cashExpense)
const transferExpense = computed(() => shiftStore.transferExpense)
const endingCash = computed(() => shiftStore.endingCash)
const hasUnpaidBills = computed(() => shiftStore.hasUnpaidBills)
const incomeTransactions = computed(() => (shiftSession.value?.expenses || []).filter((item) => item.type === 'income'))
const expenseTransactions = computed(() => (shiftSession.value?.expenses || []).filter((item) => item.type === 'expense'))
const cashInShift = computed(() => cashSales.value + cashIncome.value - cashExpense.value)
const billCount = computed(() => (shiftSession.value?.bills || []).length)
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
  totalIncome: cashIncome.value,
  totalExpense: cashExpense.value,
  endingCash: endingCash.value,
})

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
  const paymentMethod = Number(invoice.phuongThucThanhToan) === 2 ? 'transfer' : 'cash'
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

const loadShiftBillsFromServer = async () => {
  if (!shiftStore.currentShift?.startTime || !shiftStore.currentShift.isOpen) return

  refreshingBills.value = true
  try {
    const shiftStartTime = new Date(shiftStore.currentShift.startTime || shiftStore.currentShift.openedAt).getTime()
    const latestClosedShift = [...(shiftStore.history || [])]
      .filter((entry) => entry.endTime || entry.closedAt)
      .sort((a, b) => {
        const timeA = new Date(b.closedAt || b.endTime || b.openedAt).getTime()
        const timeB = new Date(a.closedAt || a.endTime || a.openedAt).getTime()
        return timeA - timeB
      })[0]

    const previousClosedAt = latestClosedShift
      ? new Date(latestClosedShift.closedAt || latestClosedShift.endTime || latestClosedShift.openedAt).getTime()
      : null

    const effectiveLowerBound = previousClosedAt && !Number.isNaN(previousClosedAt)
      ? previousClosedAt
      : shiftStartTime

    const res = await HoaDonApi.getDanhSach()
    const invoices = Array.isArray(res.data) ? res.data : []

    const serverBills: ShiftInvoice[] = invoices
      .filter((invoice: HoaDon) => {
        const invoiceTime = getInvoiceTimestamp(invoice)
        return invoiceTime !== null && invoiceTime >= effectiveLowerBound
      })
      .map((invoice: HoaDon) => normalizeShiftBill(invoice))

    const mergedBills = [...(shiftStore.currentShift?.bills || [])]

    serverBills.forEach((bill) => {
      const exists = mergedBills.some((existing) => existing.id === bill.id || existing.code === bill.code)
      if (!exists) {
        mergedBills.push(bill)
      }
    })

    const uniqueBills = mergedBills.filter((bill, index, list) => {
      return list.findIndex((candidate) => candidate.id === bill.id || candidate.code === bill.code) === index
    })

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

  shiftStore.openShift({
    openingCash,
    employeeName: resolvedEmployeeName,
  })

  showOpenModal.value = false
  openingCashInput.value = ''

  void loadShiftBillsFromServer()
}

const addTransaction = () => {
  const amount = Number(transactionForm.value.amount)

  if (!transactionForm.value.reason.trim() || !transactionForm.value.amount || Number.isNaN(amount) || amount <= 0) {
    alert('Vui lòng nhập số tiền và lý do hợp lệ.')
    return
  }

  shiftStore.addCashTransaction({
    type: transactionForm.value.type,
    amount,
    reason: transactionForm.value.reason.trim(),
    paymentMethod: transactionForm.value.paymentMethod,
  })

  transactionForm.value = {
    type: 'income',
    paymentMethod: 'cash',
    amount: '',
    reason: '',
  }
}

const startCloseShift = () => {
  if (hasUnpaidBills.value) {
    alert('Có hóa đơn chưa thanh toán trong ca. Vui lòng thanh toán trước khi đóng ca.')
    return
  }

  showCloseModal.value = true
}

const confirmCloseShift = (keepLoggedIn: boolean) => {
  const summary = getShiftSummary()
  console.log('Closing shift summary:', summary, 'print', printSummary.value)

  shiftStore.closeShift()
  showCloseModal.value = false

  if (!keepLoggedIn) {
    authStore.logout()
    router.replace('/auth')
    return
  }

  router.push({ name: 'dat-ban-quan-ly' })
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
                  <th>Tên nhân viên mở</th>
                  <th>Thời gian mở - đóng</th>
                  <th>Số dư đầu ca</th>
                  <th>Doanh thu NET</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="entry in historyEntries" :key="entry.shiftId">
                  <td>{{ entry.shiftId }}</td>
                  <td>{{ entry.employeeName }}</td>
                  <td>{{ formatShiftRange(entry.startTime, entry.endTime || entry.closedAt) }}</td>
                  <td>{{ moneyFormatter.format(entry.openingCash) }}</td>
                  <td>{{ moneyFormatter.format(entry.summary?.revenue ?? 0) }}</td>
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
                <tr v-for="bill in shiftSession?.bills || []" :key="bill.id">
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
            <h3>Thêm khoản thu/chi phát sinh</h3>
            <div class="form-grid">
              <select v-model="transactionForm.type">
                <option value="income">Thu</option>
                <option value="expense">Chi</option>
              </select>
              <select v-model="transactionForm.paymentMethod">
                <option value="cash">Tiền mặt</option>
                <option value="transfer">Chuyển khoản</option>
              </select>
              <input v-model="transactionForm.amount" type="number" min="0" placeholder="Nhập số tiền" />
              <input v-model="transactionForm.reason" type="text" placeholder="Nhập lý do" />
            </div>
            <button class="btn-primary" @click="addTransaction">Lưu giao dịch</button>
          </div>

          <div class="table-card">
            <table>
              <thead>
                <tr>
                  <th>Loại</th>
                  <th>Số tiền</th>
                  <th>Lý do</th>
                  <th>Thời gian</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in shiftSession?.expenses || []" :key="item.id">
                  <td>
                    <span :class="['status-badge', item.type]">{{ item.type === 'income' ? 'Thu' : 'Chi' }}</span>
                  </td>
                  <td>{{ moneyFormatter.format(item.amount) }}</td>
                  <td>{{ item.reason }} <span class="payment-method-tag">{{ item.paymentMethod === 'cash' ? 'Tiền mặt' : 'Chuyển khoản' }}</span></td>
                  <td>{{ item.createdAt }}</td>
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
                  <strong>{{ moneyFormatter.format(invoiceRevenue) }}</strong>
                </div>
                <div class="payment-split">
                  <div class="payment-pill">
                    <span>+ Tiền mặt (1)</span>
                    <strong>{{ moneyFormatter.format(cashSales) }}</strong>
                  </div>
                  <div class="payment-pill">
                    <span>+ Chuyển khoản</span>
                    <strong>{{ moneyFormatter.format(transferSales) }}</strong>
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
                    <span>Tiền mặt (2)</span>
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
                    <span>Tiền mặt (3)</span>
                    <strong>{{ moneyFormatter.format(cashExpense) }}</strong>
                  </div>
                  <div class="mini-meta">
                    <span>Chuyển khoản</span>
                    <strong>{{ moneyFormatter.format(transferExpense) }}</strong>
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
                <div class="metric-row handover-row">
                  <span>Tiền mặt cuối ca</span>
                  <strong>{{ moneyFormatter.format(endingCash) }}</strong>
                </div>
                <label class="checkbox-row report-checkbox">
                  <input v-model="printSummary" type="checkbox" />
                  <span>In báo cáo khi chốt ca</span>
                </label>
              </div>
            </section>
          </div>
        </div>
      </section>
    </div>

    <div v-if="showOpenModal" class="modal-overlay" @click.self="showOpenModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>Mở ca</h3>
          <button class="close-btn" @click="showOpenModal = false">×</button>
        </div>
        <label class="field-label">Tiền mặt đầu ca</label>
        <input v-model="openingCashInput" type="number" min="0" placeholder="Nhập số tiền mặt đầu ca" />
        <div class="modal-actions">
          <button class="btn-secondary" @click="showOpenModal = false">Hủy</button>
          <button class="btn-primary" @click="openShift">Xác nhận</button>
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

        <div class="history-detail-card">
          <div class="metric-row">
            <span>Nhân viên mở ca</span>
            <strong>{{ selectedHistoryEntry?.employeeName }}</strong>
          </div>
          <div class="metric-row">
            <span>Thời gian mở</span>
            <strong>{{ selectedHistoryEntry?.startTime ? formatDate(selectedHistoryEntry.startTime) : '---' }}</strong>
          </div>
          <div class="metric-row">
            <span>Thời gian đóng</span>
            <strong>{{ selectedHistoryEntry?.endTime || selectedHistoryEntry?.closedAt ? formatDate(selectedHistoryEntry?.endTime || selectedHistoryEntry?.closedAt || selectedHistoryEntry.startTime) : '---' }}</strong>
          </div>
          <div class="metric-row">
            <span>Số dư đầu ca</span>
            <strong>{{ moneyFormatter.format(selectedHistoryEntry?.openingCash || 0) }}</strong>
          </div>
          <div class="metric-row">
            <span>Doanh thu NET</span>
            <strong>{{ moneyFormatter.format(selectedHistoryEntry?.summary?.revenue || 0) }}</strong>
          </div>
          <div class="metric-row">
            <span>Tổng thu</span>
            <strong>{{ moneyFormatter.format(selectedHistoryEntry?.summary?.totalIncome || 0) }}</strong>
          </div>
          <div class="metric-row">
            <span>Tổng chi</span>
            <strong>{{ moneyFormatter.format(selectedHistoryEntry?.summary?.totalExpense || 0) }}</strong>
          </div>
          <div class="metric-row handover-row">
            <span>Tiền mặt cuối ca</span>
            <strong>{{ moneyFormatter.format(selectedHistoryEntry?.summary?.endingCash || 0) }}</strong>
          </div>
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
            <span>Doanh thu</span>
            <strong>{{ moneyFormatter.format(invoiceRevenue) }}</strong>
          </div>
          <div>
            <span>Tổng thu</span>
            <strong>{{ moneyFormatter.format(cashIncome) }}</strong>
          </div>
          <div>
            <span>Tổng chi</span>
            <strong>{{ moneyFormatter.format(cashExpense) }}</strong>
          </div>
          <div>
            <span>Tiền mặt cuối ca</span>
            <strong>{{ moneyFormatter.format(endingCash) }}</strong>
          </div>
        </div>

        <label class="checkbox-row">
          <input v-model="printSummary" type="checkbox" />
          <span>In báo cáo chốt ca</span>
        </label>

        <div class="modal-actions split">
          <button class="btn-secondary" @click="confirmCloseShift(true)">Đóng ca</button>
          <button class="btn-danger" @click="confirmCloseShift(false)">Đóng ca và đăng xuất</button>
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
}

.modal-card {
  width: min(460px, 100%);
  padding: 18px;
}

.modal-card.wide {
  width: min(640px, 100%);
}

.detail-modal-card {
  max-height: min(88vh, 920px);
  overflow: auto;
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

.report-checkbox {
  margin-top: 8px;
  padding: 10px 12px;
  width: fit-content;
  border: 1px solid #d7ad72;
  border-radius: 10px;
  background: #f7eedc;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.report-checkbox input {
  margin: 0;
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

.history-detail-card {
  display: grid;
  gap: 8px;
}
</style>
