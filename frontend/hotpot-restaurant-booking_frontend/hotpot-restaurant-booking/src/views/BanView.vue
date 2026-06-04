<script setup lang="ts">
import BanApi from '@/api/BanApi';
import BanForm from '@/components/BanForm.vue';
import BanTable from '@/components/BanTable.vue';
import { onMounted, ref } from 'vue';

    const tableList = ref([]) // biến list để lưu danh sách truyền xuống table

    const selectedTable = ref(null); // biến để dùng với form
    
    const loadData = async () => {
    
        const response = await BanApi.getAll(); // tạo biến gọi api 
            tableList.value = response.data; // gán dữ liệu đã lấy được từ backend vào biến danh sách 
    }
// 3. Hàm xử lý khi bấm nút "Chi tiết"
const handleDetail = (table: any) => {
  // Gán dữ liệu của bàn vừa bấm vào biến selectedTable
  // Biến này sẽ được truyền xuống BanForm qua props
  selectedTable.value = table;
  console.log("Đang xem chi tiết bàn:", selectedTable.value);
};

// 4. Hàm xử lý Xóa
const handleDelete = async (id: number) => {
  if (confirm("Bạn có chắc muốn xóa bàn này?")) {
    await BanApi.delete(id);
    loadData(); // Xóa xong load lại bảng
  }}


onMounted(loadData)

</script>

<template>
    <div>
        <BanForm :formData="selectedTable"
        @refresh="loadData"></BanForm>
        <BanTable :tableList="tableList"
        @detail="handleDetail"
        @delete="handleDelete"></BanTable>
    </div>
</template>