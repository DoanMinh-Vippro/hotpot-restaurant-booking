<script setup lang="ts">
import BanApi from '@/api/BanApi';
import BanForm from '@/components/BanForm.vue';
import BanTable from '@/components/BanTable.vue';
import { onMounted, ref } from 'vue';

const tableList = ref([])
const selectedTable = ref(null)
const searchQuery = ref('')
const sortActive = ref(false)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const response = await BanApi.getAll()
    tableList.value = response.data
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    loadData()
    return
  }

  loading.value = true
  try {
    const response = await BanApi.search(searchQuery.value.trim())
    tableList.value = response.data
    sortActive.value = false
  } finally {
    loading.value = false
  }
}

const handleSort = async () => {
  loading.value = true
  try {
    const response = await BanApi.sort()
    tableList.value = response.data
    sortActive.value = true
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchQuery.value = ''
  sortActive.value = false
  loadData()
}

const handleDetail = (table: any) => {
  selectedTable.value = table
  console.log("Đang xem chi tiết bàn:", selectedTable.value)
}

const handleDelete = async (id: number) => {
  if (confirm("Bạn có chắc muốn xóa bàn này?")) {
    await BanApi.delete(id)
    loadData()
  }
}

onMounted(loadData)
</script>

<template>
  <div>
    <div class="ban-controls">
      <div class="search-sort-container">
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="Tìm kiếm theo loại bàn, khu vực..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button @click="handleSearch" class="btn-search">Tìm kiếm</button>
        <button @click="handleSort" :class="['btn-sort', { active: sortActive }]">Sắp xếp</button>
        <button @click="handleReset" class="btn-reset">Làm mới</button>
      </div>
    </div>

    <BanForm :formData="selectedTable" @refresh="loadData"></BanForm>
    <BanTable 
      :tableList="tableList"
      :loading="loading"
      @detail="handleDetail"
      @delete="handleDelete"
    ></BanTable>
  </div>
</template>

<style scoped>
.ban-controls {
  padding: 20px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-sort-container {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  padding: 10px 15px;
  border: 1px solid #c5a059;
  border-radius: 6px;
  background: #0f0f0f;
  color: #fff;
  font-size: 0.95rem;
}

.search-input::placeholder {
  color: #999;
}

.search-input:focus {
  outline: none;
  border-color: #d4af37;
  box-shadow: 0 0 8px rgba(197, 160, 89, 0.3);
}

button {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn-search {
  background: #c5a059;
  color: #000;
}

.btn-search:hover {
  background: #d4af37;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(197, 160, 89, 0.4);
}

.btn-sort {
  background: #444;
  color: #fff;
}

.btn-sort:hover {
  background: #555;
}

.btn-sort.active {
  background: #27ae60;
  color: #fff;
}

.btn-reset {
  background: #7f8c8d;
  color: #fff;
}

.btn-reset:hover {
  background: #95a5a6;
}

@media (max-width: 768px) {
  .search-sort-container {
    flex-direction: column;
  }

  .search-input {
    min-width: 100%;
  }

  button {
    width: 100%;
  }
}
</style>