import { defineStore } from 'pinia'

export interface ShiftInvoice {
  id: string
  code: string
  customer: string
  total: number
  gross: number
  discount: number
  paymentMethod: 'cash' | 'transfer'
  status: 'paid' | 'unpaid'
  createdAt: string
  createdAtTimestamp?: number
}

export interface ShiftExpense {
  id: number
  type: 'income' | 'expense'
  amount: number
  reason: string
  paymentMethod: 'cash' | 'transfer'
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
}

export interface ShiftHistoryEntry extends ShiftSession {
  closedAt: string
  summary: {
    gross: number
    discount: number
    revenue: number
    cashSales: number
    transferSales: number
    totalIncome: number
    cashIncome: number
    transferIncome: number
    totalExpense: number
    cashExpense: number
    transferExpense: number
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

const buildShiftSummary = (shift: ShiftSession) => {
  const bills = shift.bills || []
  const expenses = shift.expenses || []

  const gross = bills
    .filter((bill) => bill.status === 'paid')
    .reduce((sum, bill) => sum + bill.gross, 0)

  const discount = bills
    .filter((bill) => bill.status === 'paid')
    .reduce((sum, bill) => sum + bill.discount, 0)

  const revenue = bills
    .filter((bill) => bill.status === 'paid')
    .reduce((sum, bill) => sum + bill.total, 0)

  const cashSales = bills
    .filter((bill) => bill.status === 'paid' && bill.paymentMethod === 'cash')
    .reduce((sum, bill) => sum + bill.total, 0)

  const transferSales = bills
    .filter((bill) => bill.status === 'paid' && bill.paymentMethod === 'transfer')
    .reduce((sum, bill) => sum + bill.total, 0)

  const cashIncome = expenses
    .filter((item) => item.type === 'income' && item.paymentMethod === 'cash')
    .reduce((sum, item) => sum + item.amount, 0)

  const transferIncome = expenses
    .filter((item) => item.type === 'income' && item.paymentMethod === 'transfer')
    .reduce((sum, item) => sum + item.amount, 0)

  const cashExpense = expenses
    .filter((item) => item.type === 'expense' && item.paymentMethod === 'cash')
    .reduce((sum, item) => sum + item.amount, 0)

  const transferExpense = expenses
    .filter((item) => item.type === 'expense' && item.paymentMethod === 'transfer')
    .reduce((sum, item) => sum + item.amount, 0)

  const totalIncome = cashIncome + transferIncome
  const totalExpense = cashExpense + transferExpense
  const endingCash = (shift.openingCash || 0) + cashSales + cashIncome - cashExpense

  return {
    gross,
    discount,
    revenue,
    cashSales,
    transferSales,
    totalIncome,
    cashIncome,
    transferIncome,
    totalExpense,
    cashExpense,
    transferExpense,
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
      (state.currentShift?.bills || []).reduce((sum, bill) => sum + (bill.status === 'paid' ? bill.total : 0), 0),
    cashSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => bill.status === 'paid' && bill.paymentMethod === 'cash')
        .reduce((sum, bill) => sum + bill.total, 0),
    transferSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => bill.status === 'paid' && bill.paymentMethod === 'transfer')
        .reduce((sum, bill) => sum + bill.total, 0),
    grossSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => bill.status === 'paid')
        .reduce((sum, bill) => sum + bill.gross, 0),
    discountSales: (state) =>
      (state.currentShift?.bills || [])
        .filter((bill) => bill.status === 'paid')
        .reduce((sum, bill) => sum + bill.discount, 0),
    transferIncome: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'income' && item.paymentMethod === 'transfer')
        .reduce((sum, item) => sum + item.amount, 0),
    transferExpense: (state) =>
      (state.currentShift?.expenses || [])
        .filter((item) => item.type === 'expense' && item.paymentMethod === 'transfer')
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
        .filter((bill) => bill.status === 'paid' && bill.paymentMethod === 'cash')
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
      (state.currentShift?.bills || []).some((bill) => bill.status === 'unpaid'),
  },

  actions: {
    openShift(payload: { openingCash: number; employeeName: string }) {
      if (this.currentShift?.isOpen) return

      const startTime = new Date().toISOString()

      this.currentShift = {
        shiftId: `SHIFT-${Date.now()}`,
        startTime,
        openedAt: startTime,
        endTime: null,
        employeeName: payload.employeeName || 'Nhân viên',
        openingCash: payload.openingCash,
        bills: [],
        expenses: [],
        isOpen: true,
      }

      persistCurrentShift(this.currentShift)
    },

    syncBillFromPos(bill: ShiftInvoice) {
      if (!this.currentShift?.isOpen) return

      const shiftStartTime = new Date(this.currentShift.startTime || this.currentShift.openedAt).getTime()
      const billTime = bill.createdAtTimestamp ?? Date.now()

      if (billTime < shiftStartTime) return

      const duplicate = this.currentShift.bills.some(
        (existing) => existing.id === bill.id || existing.code === bill.code,
      )

      if (duplicate) return

      this.currentShift.bills.unshift(bill)
      persistCurrentShift(this.currentShift)
    },

    addCashTransaction(payload: { type: 'income' | 'expense'; amount: number; reason: string; paymentMethod?: 'cash' | 'transfer' }) {
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

    updateExpenseTransaction(payload: { id: number; type: 'income' | 'expense'; amount: number; reason: string; paymentMethod?: 'cash' | 'transfer' }) {
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

    closeShift() {
      if (!this.currentShift?.isOpen) return

      const currentShift = this.currentShift
      const endTime = new Date().toISOString()
      const historyEntry: ShiftHistoryEntry = {
        ...currentShift,
        isOpen: false,
        endTime,
        closedAt: endTime,
        summary: buildShiftSummary(currentShift),
      }

      this.history.unshift(historyEntry)
      persistHistory(this.history)
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
