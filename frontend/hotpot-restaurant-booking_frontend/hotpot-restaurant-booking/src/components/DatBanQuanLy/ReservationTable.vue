<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  list: any[]
}>()

const emit = defineEmits(['detail', 'huy'])

const getTrangThaiText = (trangThai: string) => {
  switch (trangThai) {
    case 'CHO_XAC_NHAN':
      return 'Chờ xác nhận'

    case 'DA_XAC_NHAN':
      return 'Đã xác nhận'

    case 'DA_NHAN_BAN':
      return 'Đã nhận bàn'

    case 'HOAN_THANH':
      return 'Hoàn thành'

    case 'DA_HUY':
      return 'Đã huỷ'

    default:
      return trangThai
  }
}

const getTrangThaiClass = (trangThai: string) => {
  switch (trangThai) {
    case 'CHO_XAC_NHAN':
      return 'waiting'

    case 'DA_XAC_NHAN':
      return 'confirmed'

    case 'DA_NHAN_BAN':
      return 'using'

    case 'HOAN_THANH':
      return 'done'

    case 'DA_HUY':
      return 'cancel'

    default:
      return ''
  }
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('vi-VN').format(value || 0)
}

const rows = computed(() => props.list ?? [])

// format thời gian
const formatDateTime = (value: string) => {
  if (!value) return '-'

  const date = new Date(value)

  if (isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}
</script>

<template>
  <div class="reservation-table">
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Khách hàng</th>
          <th>SĐT</th>
          <th>Người</th>
          <th>Giờ đến</th>
          <th>Tiền cọc</th>
          <th>Trạng thái</th>
          <th class="action-col">Thao tác</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="item in rows" :key="item.idDatBan">
          <td>#{{ item.idDatBan }}</td>

          <td>
            {{ item.tenKhachHang || 'Khách lẻ' }}
          </td>

          <td>
            {{ item.sdtKhachHang }}
          </td>

          <td>
            {{ item.soNguoi }}
          </td>

          <td>
            {{ formatDateTime(item.thoiGianDenDuKien) }}
          </td>

          <td>{{ formatCurrency(item.soTienCoc) }} đ</td>

          <td>
            <span class="status" :class="getTrangThaiClass(item.trangThai)">
              {{ getTrangThaiText(item.trangThai) }}
            </span>
          </td>

          <td>
            <div class="actions">
              <button class="btn detail" @click="emit('detail', item)">Chi tiết</button>

              <button
                v-if="item.trangThai === 'CHO_XAC_NHAN' || item.trangThai === 'DA_XAC_NHAN'"
                class="btn danger"
                @click="emit('huy', item)"
              >
                Huỷ
              </button>
            </div>
          </td>
        </tr>

        <tr v-if="rows.length === 0">
          <td colspan="8" class="empty">Không có dữ liệu</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.reservation-table {
  background: #fffdf8;
  border: 1px solid #eadfcb;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.05);
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: linear-gradient(90deg, #efe2c8, #f8f2e7);
}

th {
  padding: 16px;
  text-align: center;
  color: #5c4631;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.action-col {
  width: 150px;
}

th:last-child,
td:last-child {
  width: 150px;
  white-space: nowrap;
}

td {
  padding: 15px;
  text-align: center;
  color: #3b3228;
  border-top: 1px solid #f0e7d9;
  font-size: 14px;
}

tbody tr {
  transition: 0.25s;
}

tbody tr:hover {
  background: #faf4eb;
}

.status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.waiting {
  background: #fff4d8;
  color: #c27a00;
}

.confirmed {
  background: #dff5e5;
  color: #0d8a49;
}

.using {
  background: #dbeafe;
  color: #2563eb;
}

.done {
  background: #ececec;
  color: #666;
}

.cancel {
  background: #fde4e4;
  color: #d93d3d;
}

.actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn {
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  min-width: 68px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: 0.25s;
}

.btn:hover {
  transform: translateY(-2px);
}

.detail {
  background: #d7b56d;
  color: white;
}

.detail:hover {
  background: #bf9a4f;
}

.danger {
  background: #d9534f;
  color: white;
}

.danger:hover {
  background: #bf3c38;
}

.empty {
  padding: 40px;
  color: #888;
  font-style: italic;
}

@media (max-width: 1200px) {
  .reservation-table {
    overflow-x: auto;
  }

  table {
    min-width: 1100px;
  }
}
</style>
