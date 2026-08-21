<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import HoaDonApi from '@/api/HoaDonApi'

interface SplitItem {
  idHoaDonChiTiet: number
  idMon?: number | null
  idCombo?: number | null
  tenMon?: string | null
  tenCombo?: string | null
  soLuong: number
  gia: number
}

const props = defineProps<{
  visible: boolean
  idHoaDon: number
  items: SplitItem[]
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'success', invoice: any): void
}>()

const leftItems = ref<SplitItem[]>([])
const rightItems = ref<SplitItem[]>([])
const quantities = ref<Record<number, number>>({})
const isSaving = ref(false)

const cloneItems = (items: SplitItem[]) => items.map((item) => ({ ...item }))

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      leftItems.value = cloneItems(props.items)
      rightItems.value = []
      quantities.value = {}
      isSaving.value = false
    }
  },
  { immediate: true },
)

const itemName = (item: SplitItem) => item.tenMon || item.tenCombo || 'Món chưa đặt tên'
const itemKey = (item: SplitItem) => item.idHoaDonChiTiet
const itemTotal = (item: SplitItem) => Number(item.gia || 0) * Number(item.soLuong || 0)
const total = (items: SplitItem[]) => items.reduce((sum, item) => sum + itemTotal(item), 0)
const leftTotal = computed(() => total(leftItems.value))
const rightTotal = computed(() => total(rightItems.value))
const canConfirm = computed(() => leftItems.value.length > 0 && rightItems.value.length > 0)

const moveRight = (item: SplitItem) => {
  const quantity = Math.min(
    Math.max(1, Number(quantities.value[itemKey(item)] || 1)),
    item.soLuong,
  )
  const left = leftItems.value.find((candidate) => itemKey(candidate) === itemKey(item))
  if (!left) return

  left.soLuong -= quantity
  const existing = rightItems.value.find((candidate) => itemKey(candidate) === itemKey(item))
  if (existing) existing.soLuong += quantity
  else rightItems.value.push({ ...item, soLuong: quantity })
  if (left.soLuong <= 0) leftItems.value = leftItems.value.filter((candidate) => itemKey(candidate) !== itemKey(item))
  quantities.value[itemKey(item)] = 1
}

const moveLeft = (item: SplitItem) => {
  const existing = leftItems.value.find((candidate) => itemKey(candidate) === itemKey(item))
  if (existing) existing.soLuong += item.soLuong
  else leftItems.value.push({ ...item })
  rightItems.value = rightItems.value.filter((candidate) => itemKey(candidate) !== itemKey(item))
}

const formatMoney = (value: number) => value.toLocaleString('vi-VN')

const confirmSplit = async () => {
  if (!canConfirm.value || isSaving.value) return
  isSaving.value = true
  try {
    const selected = rightItems.value.map((item) => ({
      idHoaDonChiTiet: item.idHoaDonChiTiet,
      soLuong: item.soLuong,
    }))
    const response = await HoaDonApi.split(props.idHoaDon, selected)
    emit('success', response.data)
  } catch (error: any) {
    alert(error?.response?.data?.message || 'Tách hóa đơn thất bại.')
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div v-if="visible" class="split-overlay" @click.self="emit('close')">
    <section class="split-modal" role="dialog" aria-modal="true" aria-labelledby="split-title">
      <header class="split-header">
        <div>
          <p class="eyebrow">Bàn đang sử dụng</p>
          <h2 id="split-title">Tách hóa đơn</h2>
        </div>
        <button class="icon-close" type="button" aria-label="Đóng" @click="emit('close')">×</button>
      </header>

      <div class="split-columns">
        <section class="bill-column">
          <div class="bill-heading"><strong>Hóa đơn gốc (HD1)</strong><span>{{ leftItems.length }} món</span></div>
          <div class="bill-list">
            <div v-for="item in leftItems" :key="`left-${item.idHoaDonChiTiet}`" class="split-item">
              <div class="item-copy"><strong>{{ itemName(item) }}</strong><small>x{{ item.soLuong }} · {{ formatMoney(itemTotal(item)) }} đ</small></div>
              <div class="move-control">
                <input v-model.number="quantities[item.idHoaDonChiTiet]" min="1" :max="item.soLuong" type="number" aria-label="Số lượng chuyển" />
                <button type="button" title="Chuyển sang HD2" @click="moveRight(item)">›</button>
              </div>
            </div>
            <p v-if="leftItems.length === 0" class="empty-state">HD1 không còn món.</p>
          </div>
          <footer class="bill-total"><span>Tổng HD1</span><strong>{{ formatMoney(leftTotal) }} đ</strong></footer>
        </section>

        <section class="bill-column target-column">
          <div class="bill-heading"><strong>Hóa đơn mới (HD2)</strong><span>{{ rightItems.length }} món</span></div>
          <div class="bill-list">
            <div v-for="item in rightItems" :key="`right-${item.idHoaDonChiTiet}`" class="split-item">
              <div class="item-copy"><strong>{{ itemName(item) }}</strong><small>x{{ item.soLuong }} · {{ formatMoney(itemTotal(item)) }} đ</small></div>
              <button class="back-button" type="button" title="Chuyển về HD1" @click="moveLeft(item)">‹</button>
            </div>
            <p v-if="rightItems.length === 0" class="empty-state">Chọn món từ HD1 để chuyển sang đây.</p>
          </div>
          <footer class="bill-total"><span>Tổng HD2</span><strong>{{ formatMoney(rightTotal) }} đ</strong></footer>
        </section>
      </div>

      <footer class="modal-actions">
        <button class="cancel-button" type="button" :disabled="isSaving" @click="emit('close')">Hủy</button>
        <button class="confirm-button" type="button" :disabled="!canConfirm || isSaving" @click="confirmSplit">{{ isSaving ? 'Đang xử lý...' : 'Xác nhận tách' }}</button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.split-overlay { position: fixed; inset: 0; z-index: 1100; display: grid; place-items: center; padding: 20px; background: rgba(31, 28, 24, .55); }
.split-modal { width: min(900px, 100%); max-height: 90vh; overflow: auto; border-radius: 16px; background: #fffdf8; color: #302b25; box-shadow: 0 24px 70px rgba(0, 0, 0, .25); }
.split-header, .modal-actions { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px; border-bottom: 1px solid #eadfce; }
.modal-actions { justify-content: flex-end; gap: 10px; border-top: 1px solid #eadfce; border-bottom: 0; }
.eyebrow { margin: 0 0 4px; color: #a26d36; font-size: 11px; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }
h2 { margin: 0; font-size: 24px; }
.icon-close { border: 0; background: transparent; color: #766c61; font-size: 28px; cursor: pointer; }
.split-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; padding: 20px 24px; }
.bill-column { min-width: 0; border: 1px solid #eadfce; border-radius: 10px; background: #fff; }
.target-column { background: #fffaf1; }
.bill-heading, .bill-total { display: flex; align-items: center; justify-content: space-between; padding: 13px 15px; }
.bill-heading { border-bottom: 1px solid #eee5d8; }
.bill-heading span { color: #887c70; font-size: 12px; }
.bill-list { min-height: 190px; max-height: 330px; overflow: auto; padding: 8px; }
.split-item { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 11px 7px; border-bottom: 1px solid #f1eade; }
.item-copy { min-width: 0; display: grid; gap: 3px; }
.item-copy strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-copy small { color: #887c70; }
.move-control { display: flex; align-items: center; gap: 5px; }
.move-control input { width: 48px; padding: 6px 4px; border: 1px solid #d9cbb8; border-radius: 5px; text-align: center; }
.move-control button, .back-button { width: 30px; height: 30px; border: 0; border-radius: 5px; background: #b36f35; color: #fff; font-size: 24px; line-height: 1; cursor: pointer; }
.back-button { background: #796b5d; }
.bill-total { border-top: 1px solid #eadfce; color: #665b50; }
.bill-total strong { color: #9b5d2c; }
.empty-state { margin: 0; padding: 34px 12px; color: #9b9187; text-align: center; font-size: 13px; }
.modal-actions button { padding: 10px 16px; border: 0; border-radius: 6px; font-weight: 700; cursor: pointer; }
.modal-actions button:disabled { cursor: not-allowed; opacity: .5; }
.cancel-button { background: #eee7dc; color: #5d5349; }
.confirm-button { background: #a85f2b; color: white; }
@media (max-width: 680px) { .split-columns { grid-template-columns: 1fr; } .split-modal { max-height: 95vh; } }
</style>
