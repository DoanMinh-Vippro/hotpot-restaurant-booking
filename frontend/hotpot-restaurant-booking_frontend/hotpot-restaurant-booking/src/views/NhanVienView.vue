<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import NhanVienForm from '@/components/NhanVienForm.vue';
import NhanVienTable from '@/components/NhanVienTable.vue';
import { onMounted,ref } from 'vue';
const tableList= ref([])
<<<<<<< HEAD
const selectedtable= ref(null)
=======
const selected= ref(null)
>>>>>>> feature/my-update
const loadData= async ()=>{
    const res= await NhanVienApi.getAll();
    tableList.value= res.data;
};
const handleDetail = (table: any) => {
  console.log("DETAIL:", table)
<<<<<<< HEAD
  selectedtable.value = table
=======
  selected.value = {
    ...table,
    // Nếu bị đảo thì đảo ngược lại
    trangThai: table.trangThai ? 0 : 1  // Đảo ngược logic
  };
>>>>>>> feature/my-update
}
const handleDelete = async (id: number)=>{
    if(confirm("Bạn có chắc muốn xóa ?")){
        await NhanVienApi.delete(id);
<<<<<<< HEAD
        selectedtable.value = null;
=======
        selected.value = null;
>>>>>>> feature/my-update
        loadData();
    }
}
onMounted(loadData)
</script>
<template>
    <div>
<<<<<<< HEAD
        <NhanVienForm :formData="selectedtable" @refresh="loadData"></NhanVienForm>
=======
        <NhanVienForm :formData="selected" @refresh="loadData"></NhanVienForm>
>>>>>>> feature/my-update
        <NhanVienTable :tableList="tableList" @detail="handleDetail" @delete="handleDelete"></NhanVienTable>
    </div>
</template>