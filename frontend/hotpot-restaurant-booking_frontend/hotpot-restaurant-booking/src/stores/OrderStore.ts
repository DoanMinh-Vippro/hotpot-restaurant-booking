import { defineStore } from 'pinia'

export interface PendingOrderItem {
  idMon?: number | null
  idCombo?: number | null

  tenMon?: string | null
  tenCombo?: string | null

  tenQuay: string

  gia: number

  soLuong: number

  loai: 'MON' | 'COMBO'

  comboItems?: string[]

  daLen: number

  orderedBy?: string

  orderedAt?: string
}

export const useOrderStore = defineStore('order-store', {
  state: () => ({
    orders: {} as Record<number, PendingOrderItem[]>,
  }),

  getters: {
    getByBan: (state) => {
      return (idBan: number) => state.orders[idBan] || []
    },
  },

  actions: {
    setOrder(idBan: number, items: PendingOrderItem[]) {
      this.orders[idBan] = structuredClone(items)
    },

    clearOrder(idBan: number) {
      delete this.orders[idBan]
    },

    addItem(idBan: number, item: PendingOrderItem) {
      if (!this.orders[idBan]) {
        this.orders[idBan] = []
      }

      const exist = this.orders[idBan].find((i) => {
        if (item.loai === 'MON') {
          return i.idMon === item.idMon
        }

        return i.idCombo === item.idCombo
      })

      if (exist) {
        exist.soLuong += item.soLuong
      } else {
        this.orders[idBan].push(structuredClone(item))
      }
    },

    updateItems(idBan: number, items: PendingOrderItem[]) {
      this.orders[idBan] = structuredClone(items)
    },
  },
})
