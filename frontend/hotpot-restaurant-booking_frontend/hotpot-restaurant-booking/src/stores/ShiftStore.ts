import { defineStore } from 'pinia'

export type ShiftPaymentMethod = 'cash' | 'transfer' | 'electronic' | 'other'

export interface ShiftInvoice {
  id: string
  code: string
  customer: string
  total: number
  gross: number
  discount: number
  paymentMethod: ShiftPaymentMethod
  status: 'paid' | 'unpaid'
  createdAt: string
  createdAtTimestamp?: number
  sourceShiftId?: string | null
  targetShiftId?: string | null
}

export interface ShiftHandoverTable {
  idBan: number | string | null
  code: string
  name: string
  total: number
  status: 'in-service' | 'unpaid-bill'
  billId?: string | null
  sourceShiftId?: string | null
  targetShiftId?: string | null
}

export interface ShiftHandoverContext {
  sourceShiftId: string
  handoverAt: string
  pendingTables: ShiftHandoverTable[]
  totalPending: number
  note?: string
  targetShiftId?: string | null
}

export interface ShiftExpense {
  id: number
  type: 'income' | 'expense'
  amount: number
  reason: string
  paymentMethod: ShiftPaymentMethod
  createdAt: string
}

export interface ShiftSession {
  shiftId: string
  startTime: string
  openedAt: string
  endTime?: string | null
  employeeName: string
  openingCash: number
  bills: ShiftInvoice[]
  expenses: ShiftExpense[]
  isOpen: boolean
  handoverContext?: ShiftHandoverContext | null
}

export interface ShiftHistoryEntry extends ShiftSession {
  closedAt: string
  summary: {
    gross: number
    discount: number
    revenue: number
    cashSales: number
    transferSales: number
    electronicSales: number
    otherSales: number
    totalIncome: number
    cashIncome: number
    transferIncome: number
    electronicIncome: number
    otherIncome: number
    totalExpense: number
    cashExpense: number
    transferExpense: number
    electronicExpense: number
    otherExpense: number
    endingCash: number
  }
}

const SHIFT_STORAGE_KEY = 'restaurant_shift_session'
const SHIFT_HISTORY_STORAGE_KEY = 'restaurant_shift_history'

const readJson = <T>(key: string, fallback: T): T => {
  try {
    const saved = localStorage.getItem(key)
    if (!saved) return fallback
    return JSON.parse(saved) as T
  } catch {
    localStorage.removeItem(key)
    return fallback
  }
}

const persistCurrentShift = (shift: ShiftSession | null) => {
  if (!shift) {
    localStorage.removeItem(SHIFT_STORAGE_KEY)
    return
  }

  const normalizedShift = {
    ...shift,
    startTime: shift.startTime || shift.openedAt,
    openedAt: shift.openedAt || shift.startTime,
    endTime: shift.endTime || null,
  }

  localStorage.setItem(SHIFT_STORAGE_KEY, JSON.stringify(normalizedShift))
}

const persistHistory = (history: ShiftHistoryEntry[]) => {
  localStorage.setItem(SHIFT_HISTORY_STORAGE_KEY, JSON.stringify(history))
}

export const normalizeShiftPaymentMethod = (value: unknown): ShiftPaymentMethod => {
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (['cash', 'tien mat', 'tiền mặt', 'tien-mat', '1'].includes(normalized)) return 'cash'
    if (['transfer', 'chuyen khoan', 'chuyển khoản', 'bank transfer', 'transfer-bank', '2', 'chuyen_khoan', 'sepay', 'qr', 'banking'].includes(normalized)) return 'transfer'
    if (['electronic', 'vnpay', 'momo', 'zalopay', 'payoo', '3'].includes(normalized)) return 'electronic'
    return 'other'
  }

  if (typeof value === 'number') {
    if (value === 1) return 'cash'
    if (value === 2) return 'transfer'
    if (value === 3) return 'electronic'
    return 'other'
  }

  return 'other'
}

export const isShiftClosed = (shift?: ShiftSession | null) => !shift || shift.isOpen === false

export const getShiftDisplayStatus = (shift?: ShiftSession | null) => {
  if (!shift) return 'Closed'
  return shift.isOpen === false ? 'Closed' : 'Open'
}

export const isValidShiftInvoice = (bill: unknown) => {
  const invoice = (bill || {}) as Partial<ShiftInvoice> & { trangThaiHoaDon?: unknown; invoiceStatus?: unknown }
  const total = Number(invoice.total ?? 0)
  const status = String(invoice.status ?? '').trim().toUpperCase()
  const invoiceStatus = invoice.trangThaiHoaDon ?? invoice.invoiceStatus

  return total > 0 && status !== 'DRAFT' && invoiceStatus !== 0 && invoiceStatus !== '0'
}

const buildShiftSummary = (shift: ShiftSession) => {
  const bills = (shift.bills || []).filter(isValidShiftInvoice)
  const expenses = shift.expenses || []
  const paidBills = bills.filter((bill) => bill.status === 'paid')

  const gross = paidBills.reduce((sum, bill) => sum + bill.gross, 0)
  const discount = paidBills.reduce((sum, bill) => sum + bill.discount, 0)
  const revenue = paidBills.reduce((sum, bill) => sum + bill.total, 0)

  const cashSales = paidBills.filter((bill) => bill.paymentMethod === 'cash').reduce((sum, bill) => sum + bill.total, 0)
  const transferSales = paidBills.filter((bill) => bill.paymentMethod === 'transfer').reduce((sum, bill) => sum + bill.total, 0)
  const electronicSales = paidBills.filter((bill) => bill.paymentMethod === 'electronic').reduce((sum, bill) => sum + bill.total, 0)
  const otherSales = paidBills.filter((bill) => bill.paymentMethod === 'other').reduce((sum, bill) => sum + bill.total, 0)

  const cashIncome = expenses.filter((item) => item.type === 'income' && item.paymentMethod === 'cash').reduce((sum, item) => sum + item.amount, 0)
  const transferIncome = expenses.filter((item) => item.type === 'income' && item.paymentMethod === 'transfer').reduce((sum, item) => sum + item.amount, 0)
  const electronicIncome = expenses.filter((item) => item.type === 'income' && item.paymentMethod === 'electronic').reduce((sum, item) => sum + item.amount, 0)
  const otherIncome = expenses.filter((item) => item.type === 'income' && item.paymentMethod === 'other').reduce((sum, item) => sum + item.amount, 0)

  const cashExpense = expenses.filter((item) => item.type === 'expense' && item.paymentMethod === 'cash').reduce((sum, item) => sum + item.amount, 0)
  const transferExpense = expenses.filter((item) => item.type === 'expense' && item.paymentMethod === 'transfer').reduce((sum, item) => sum + item.amount, 0)
  const electronicExpense = expenses.filter((item) => item.type === 'expense' && item.paymentMethod === 'electronic').reduce((sum, item) => sum + item.amount, 0)
  const otherExpense = expenses.filter((item) => item.type === 'expense' && item.paymentMethod === 'other').reduce((sum, item) => sum + item.amount, 0)

  const totalIncome = cashIncome + transferIncome + electronicIncome + otherIncome
  const totalExpense = cashExpense + transferExpense + electronicExpense + otherExpense
  const endingCash = (shift.openingCash || 0) + cashSales + cashIncome - cashExpense

  return {
    gross,
    discount,
    revenue,
    cashSales,
    transferSales,
    electronicSales,
    otherSales,
    totalIncome,
    cashIncome,
    transferIncome,
    electronicIncome,
    otherIncome,
    totalExpense,
    cashExpense,
    transferExpense,
    electronicExpense,
    otherExpense,
    endingCash,
  }
}

export const useShiftStore = defineStore('shift', {
  state: () => ({
    currentShift: readJson<ShiftSession | null>(SHIFT_STORAGE_KEY, null),
    history: readJson<ShiftHistoryEntry[]>(SHIFT_HISTORY_STORAGE_KEY, []),
  }),

  getters: {
    invoiceRevenue: (state) =>
      (state.currentShift?.bills || [])
        .filter(isValidShiftInvoice)
        .reduce((sum, bill) => sum + (bill.status === 'paid' ? bill.total : 0), 0),
    cashSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid' && bill.paymentMethod === 'cash')
        .reduce((sum, bill) => sum + bill.total, 0),
    transferSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid' && bill.paymentMethod === 'transfer')
        .reduce((sum, bill) => sum + bill.total, 0),
    grossSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid')
        .reduce((sum, bill) => sum + bill.gross, 0),
    discountSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid')
        .reduce((sum, bill) => sum + bill.discount, 0),
    transferIncome: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'income' && item.paymentMethod === 'transfer')
        .reduce((sum, item) => sum + item.amount, 0),
    transferExpense: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'expense' && item.paymentMethod === 'transfer')
        .reduce((sum, item) => sum + item.amount, 0),
    electronicSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid' && bill.paymentMethod === 'electronic')
        .reduce((sum, bill) => sum + bill.total, 0),
    otherSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid' && bill.paymentMethod === 'other')
        .reduce((sum, bill) => sum + bill.total, 0),
    electronicIncome: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'income' && item.paymentMethod === 'electronic')
        .reduce((sum, item) => sum + item.amount, 0),
    electronicExpense: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'expense' && item.paymentMethod === 'electronic')
        .reduce((sum, item) => sum + item.amount, 0),
    otherIncome: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'income' && item.paymentMethod === 'other')
        .reduce((sum, item) => sum + item.amount, 0),
    otherExpense: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'expense' && item.paymentMethod === 'other')
        .reduce((sum, item) => sum + item.amount, 0),
    cashIncome: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'income' && item.paymentMethod === 'cash')
        .reduce((sum, item) => sum + item.amount, 0),
    cashExpense: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'expense' && item.paymentMethod === 'cash')
        .reduce((sum, item) => sum + item.amount, 0),
    endingCash: (state) => {
      const openingCash = state.currentShift?.openingCash || 0
      const cashSales = (state.currentShift?.bills || [])
        .filter((bill) => isValidShiftInvoice(bill) && bill.status === 'paid' && bill.paymentMethod === 'cash')
        .reduce((sum, bill) => sum + bill.total, 0)
      const cashIncome = (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'income' && item.paymentMethod === 'cash')
        .reduce((sum, item) => sum + item.amount, 0)
      const cashExpense = (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'expense' && item.paymentMethod === 'cash')
        .reduce((sum, item) => sum + item.amount, 0)

      return openingCash + cashSales + cashIncome - cashExpense
    },
    hasUnpaidBills: (state) =>
      (state.currentShift?.bills || []).some((bill) => isValidShiftInvoice(bill) && bill.status === 'unpaid'),
  },

  actions: {
    openShift(payload: { openingCash: number; employeeName: string; handoverContext?: ShiftHandoverContext | null }) {
      if (this.currentShift?.isOpen) return

      const startTime = new Date().toISOString()
      const latestHistoryEntry = this.history[0]
      const resolvedHandoverContext = payload.handoverContext ?? latestHistoryEntry?.handoverContext ?? null

      const newShiftId = `SHIFT-${Date.now()}`
      const nextHandoverContext = resolvedHandoverContext
        ? {
            ...resolvedHandoverContext,
            targetShiftId: newShiftId,
            pendingTables: (resolvedHandoverContext.pendingTables || []).map((item) => ({
              ...item,
              sourceShiftId: item.sourceShiftId || resolvedHandoverContext.sourceShiftId || null,
              targetShiftId: newShiftId,
            })),
          }
        : null

      // Seed only handed-over bills that have a real billId (actual DB invoice ID).
      // Tables without a known billId will be loaded by loadShiftBillsFromServer via HoaDonApi.
      const handoverBills: ShiftInvoice[] = (nextHandoverContext?.pendingTables || [])
        .filter((item) => item.billId && item.billId !== 'null' && item.billId !== '')
        .map((item) => ({
          id: String(item.billId!),
          code: item.code || `HD-${item.billId}`,
          customer: item.name || 'Khách hàng',
          total: Number(item.total || 0),
          gross: Number(item.total || 0),
          discount: 0,
          paymentMethod: 'cash' as ShiftPaymentMethod,
          status: 'unpaid' as const,
          createdAt: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
          createdAtTimestamp: Date.now(),
          sourceShiftId: item.sourceShiftId || resolvedHandoverContext?.sourceShiftId || null,
          targetShiftId: newShiftId,
        }))

      this.currentShift = {
        shiftId: newShiftId,
        startTime,
        openedAt: startTime,
        endTime: null,
        employeeName: payload.employeeName || 'Nhân viên',
        openingCash: payload.openingCash,
        bills: handoverBills,
        expenses: [],
        isOpen: true,
        handoverContext: nextHandoverContext,
      }

      persistCurrentShift(this.currentShift)
    },

    syncBillFromPos(bill: ShiftInvoice) {
      if (!this.currentShift?.isOpen) return
      if (!isValidShiftInvoice(bill)) return

      const existingIndex = this.currentShift.bills.findIndex(
        (existing) => existing.id === bill.id || (bill.code && existing.code === bill.code),
      )

      if (existingIndex >= 0) {
        // Update existing bill (e.g. handed-over bill now being paid in new shift)
        this.currentShift.bills[existingIndex] = {
          ...this.currentShift.bills[existingIndex],
          ...bill,
        }
      } else {
        this.currentShift.bills.unshift(bill)
      }

      persistCurrentShift(this.currentShift)
    },

    clearSettledTableReferences(payload: { tableId?: number | string | null; billId?: number | string | null }) {
      const tableId = payload.tableId != null ? Number(payload.tableId) : null
      const billId = payload.billId != null ? String(payload.billId) : null

      const shouldKeepPendingTable = (item: ShiftHandoverTable) => {
        const itemTableId = item.idBan != null ? Number(item.idBan) : null
        const itemBillId = item.billId != null ? String(item.billId) : null
        const matchesTable = tableId != null && itemTableId != null && itemTableId === tableId
        const matchesBill = billId != null && itemBillId != null && itemBillId === billId
        return !matchesTable && !matchesBill
      }

      if (this.currentShift?.handoverContext) {
        const pendingTables = (this.currentShift.handoverContext.pendingTables || []).filter(shouldKeepPendingTable)
        this.currentShift.handoverContext = {
          ...this.currentShift.handoverContext,
          pendingTables,
          totalPending: pendingTables.reduce((sum, item) => sum + Number(item.total || 0), 0),
        }
        persistCurrentShift(this.currentShift)
      }

      this.history = (this.history || []).map((entry) => {
        if (!entry.handoverContext) return entry
        const pendingTables = (entry.handoverContext.pendingTables || []).filter(shouldKeepPendingTable)
        return {
          ...entry,
          handoverContext: {
            ...entry.handoverContext,
            pendingTables,
            totalPending: pendingTables.reduce((sum, item) => sum + Number(item.total || 0), 0),
          },
        }
      })
      persistHistory(this.history)
    },

    addCashTransaction(payload: { type: 'income' | 'expense'; amount: number; reason: string; paymentMethod?: ShiftPaymentMethod }) {
      if (!this.currentShift?.isOpen) return

      this.currentShift.expenses.unshift({
        id: Date.now(),
        type: payload.type,
        amount: payload.amount,
        reason: payload.reason,
        paymentMethod: payload.paymentMethod || 'cash',
        createdAt: new Date().toLocaleTimeString('vi-VN', {
          hour: '2-digit',
          minute: '2-digit',
        }),
      })

      persistCurrentShift(this.currentShift)
    },

    updateExpenseTransaction(payload: { id: number; type: 'income' | 'expense'; amount: number; reason: string; paymentMethod?: ShiftPaymentMethod }) {
      if (!this.currentShift?.isOpen) return

      const index = this.currentShift.expenses.findIndex((item) => item.id === payload.id)
      if (index === -1) return

      const existing = this.currentShift.expenses[index]
      if (!existing) return

      this.currentShift.expenses[index] = {
        id: existing.id,
        createdAt: existing.createdAt,
        type: payload.type,
        amount: payload.amount,
        reason: payload.reason,
        paymentMethod: payload.paymentMethod || 'cash',
      }

      persistCurrentShift(this.currentShift)
    },

    deleteExpenseTransaction(id: number) {
      if (!this.currentShift?.isOpen) return

      const nextExpenses = this.currentShift.expenses.filter((item) => item.id !== id)
      this.currentShift.expenses = nextExpenses
      persistCurrentShift(this.currentShift)
    },

    closeShift(options?: { mode?: 'normal' | 'handover'; handoverContext?: ShiftHandoverContext | null }) {
      if (!this.currentShift?.isOpen) return

      const currentShift = this.currentShift
      const endTime = new Date().toISOString()
      const historyEntry: ShiftHistoryEntry = {
        ...currentShift,
        isOpen: false,
        endTime,
        closedAt: endTime,
        summary: buildShiftSummary(currentShift),
        handoverContext: options?.mode === 'handover' ? options.handoverContext ?? null : null,
      }

      const alreadyInHistory = this.history.some((entry) => entry.shiftId === currentShift.shiftId)
      if (!alreadyInHistory) {
        this.history.unshift(historyEntry)
        persistHistory(this.history)
      }

      this.currentShift = null
      persistCurrentShift(null)
    },

    restoreFromStorage() {
      const storedShift = readJson<ShiftSession | null>(SHIFT_STORAGE_KEY, null)
      this.currentShift = storedShift
        ? {
            ...storedShift,
            startTime: storedShift.startTime || storedShift.openedAt,
            openedAt: storedShift.openedAt || storedShift.startTime,
            endTime: storedShift.endTime || null,
          }
        : null
      this.history = readJson<ShiftHistoryEntry[]>(SHIFT_HISTORY_STORAGE_KEY, [])
    },
  },
})
