<script setup lang="ts">
import { reactive, watch } from 'vue'

const props = defineProps<{
  keyword: string
  trangThai: string
  tuNgay: string
  denNgay: string
}>()

const emit = defineEmits(['search', 'reset', 'today'])

const filter = reactive({
  keyword: props.keyword,
  trangThai: props.trangThai,
  tuNgay: props.tuNgay,
  denNgay: props.denNgay,
})

watch(
  () => props,
  () => {
    filter.keyword = props.keyword
    filter.trangThai = props.trangThai
    filter.tuNgay = props.tuNgay
    filter.denNgay = props.denNgay
  },
  {
    deep: true,
  },
)

const search = () => {
  emit('search', {
    ...filter,
  })
}

const reset = () => {
  filter.keyword = ''
  filter.trangThai = ''
  filter.tuNgay = ''
  filter.denNgay = ''
  emit('reset')
}
const today = () => {
  emit('today')
}
</script>

<template>
  <div class="toolbar">
    <div class="toolbar-header">
      <h3>Bộ lọc</h3>
    </div>

    <div class="toolbar-grid">
      <div class="field">
        <label>Từ khóa</label>

        <input v-model="filter.keyword" type="text" placeholder="Tên khách, SĐT..." />
      </div>

      <div class="field">
        <label>Trạng thái</label>

        <select v-model="filter.trangThai">
          <option value="">Tất cả</option>
          <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
          <option value="DA_XAC_NHAN">Đã xác nhận</option>
          <!-- <option value="DA_NHAN_BAN">Đã nhận bàn</option> -->
          <!-- <option value="HOAN_THANH">Hoàn thành</option> -->
          <option value="DA_HUY">Đã hủy</option>
        </select>
      </div>

      <div class="field">
        <label>Từ ngày</label>

        <input v-model="filter.tuNgay" type="date" />
      </div>

      <div class="field">
        <label>Đến ngày</label>

        <input v-model="filter.denNgay" type="date" />
      </div>
    </div>

    <div class="toolbar-footer">
      <button class="btn today" @click="today">Đơn hôm nay</button>

      <button class="btn reset" @click="reset">Đặt lại</button>

      <button class="btn search" @click="search">Tìm kiếm</button>
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  background: #f8f5ef;

  border: 1px solid #e8dcc8;

  border-radius: 18px;

  padding: 24px;

  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.toolbar-header {
  margin-bottom: 20px;
}

.toolbar-header h3 {
  margin: 0;

  color: #4d3d29;
}

.toolbar-grid {
  display: grid;

  grid-template-columns: repeat(2, 1fr);

  gap: 20px;
}

.today {
  background: #4caf50;
  color: white;
}

.today:hover {
  background: #43a047;
}

.field {
  display: flex;

  flex-direction: column;
}

.field label {
  margin-bottom: 8px;

  font-size: 13px;

  font-weight: 600;

  color: #8a775d;
}

.field input,
.field select {
  padding: 13px 14px;

  border-radius: 10px;

  border: 1px solid #d9ccb3;

  background: white;

  font-size: 15px;

  transition: 0.25s;

  box-sizing: border-box;
}

.field input:focus,
.field select:focus {
  outline: none;

  border-color: #c5a05a;

  box-shadow: 0 0 0 3px rgba(197, 160, 90, 0.18);
}

.toolbar-footer {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  margin-top: 24px;
}

.btn {
  padding: 11px 22px;

  border: none;

  border-radius: 10px;

  cursor: pointer;

  font-weight: 600;

  transition: 0.25s;
}

.reset {
  background: #ddd;
}

.reset:hover {
  background: #c7c7c7;
}

.search {
  background: #c6a15b;

  color: white;
}

.search:hover {
  background: #b58e45;
}

@media (max-width: 768px) {
  .toolbar-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-footer {
    flex-wrap: wrap;
  }

  .btn {
    flex: 1;
  }
}
</style>
