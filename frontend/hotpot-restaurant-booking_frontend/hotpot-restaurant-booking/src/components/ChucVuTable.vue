<script setup lang="ts">
defineProps<{
  tableList: any[]
}>();
const emit =defineEmits(['detail', 'delete'])
</script>
<template>
    <table class="table-container">
        <thead>
            <tr>
                <th>Mã chức vụ</th>
                <th>Tên chức vụ</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="nv in tableList" :key="nv.id">
                <td>{{ nv.maChucVu }}</td>
                <td>{{ nv.tenChucVu }}</td>
                <td>
                    <button class="btn-edit" @click="emit('detail', nv)">Chi Tiết</button>
          <button class="btn-delete" @click="emit('delete', nv.id)">Xóa</button>
                </td>
            </tr>
        </tbody>
    </table>
</template>
<style scoped>
.table-container {
  width: 100%;
  margin-top: 24px;

  border-collapse: separate;
  border-spacing: 0;

  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);

  border-radius: 14px;
  overflow: hidden;

  border: 1px solid rgba(255, 255, 255, 0.08);

  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  color: #e5e7eb;
  font-family: system-ui, sans-serif;
}

/* HEADER */
th {
  padding: 14px;
  text-align: center;

  font-size: 0.85rem;
  letter-spacing: 0.8px;
  text-transform: uppercase;

  background: linear-gradient(90deg, #1e3a8a, #2563eb);
  color: white;
}

/* BODY CELL */
td {
  padding: 12px;
  text-align: center;

  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  font-size: 0.92rem;
}

/* hover row */
tr:hover {
  background: rgba(59, 130, 246, 0.12);
  transition: 0.2s ease;
}

/* action cell spacing */
td:last-child {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* BUTTON BASE */
button {
  padding: 6px 10px;
  border-radius: 8px;
  border: none;

  font-size: 0.75rem;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.2s ease;
}

/* DETAIL */
.btn-edit {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
}

.btn-edit:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

/* DELETE */
.btn-delete {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
}

.btn-delete:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

/* table rounded fix */
thead tr th:first-child {
  border-top-left-radius: 14px;
}

thead tr th:last-child {
  border-top-right-radius: 14px;
}
</style>