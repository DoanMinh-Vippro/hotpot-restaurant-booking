<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import NhanVienForm from '@/components/NhanVienForm.vue';
import NhanVienTable from '@/components/NhanVienTable.vue';
import { onMounted,ref } from 'vue';
const tableList= ref([])
const selected= ref(null)
const loadData= async ()=>{
    const res= await NhanVienApi.getAll();
    tableList.value= res.data;
};
const handleDetail = (table: any) => {
  console.log("DETAIL:", table)
  selected.value = {
    ...table,
    // Nếu bị đảo thì đảo ngược lại
    trangThai: table.trangThai ? 0 : 1  // Đảo ngược logic
  };
}
const handleDelete = async (id: number)=>{
    if(confirm("Bạn có chắc muốn xóa ?")){
        await NhanVienApi.delete(id);
        selected.value = null;
        loadData();
    }
}
onMounted(loadData)
</script>
<template>
    <div>
        <NhanVienForm :formData="selected" @refresh="loadData"></NhanVienForm>
        <NhanVienTable :tableList="tableList" @detail="handleDetail" @delete="handleDelete"></NhanVienTable>
    </div>
</template>