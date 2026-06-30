<script setup lang="ts">
import TaiKhoanApi from '@/api/TaiKhoanApi';
import TaiKhoanForm from '@/components/TaiKhoanForm.vue';
import TaiKhoanTable from '@/components/TaiKhoanTable.vue';
import { onMounted,ref } from 'vue';
const tableList= ref([])
const selectedtable= ref(null)
const loadData= async ()=>{
    const res= await TaiKhoanApi.getAll();
    tableList.value= res.data;
};
const handleDetail = (table: any) => {
  console.log("DETAIL:", table)
  selectedtable.value = table
}
const handleDelete = async (id: number)=>{
    if(confirm("Bạn có chắc muốn xóa ?")){
        await TaiKhoanApi.delete(id);
        selectedtable.value = null;
        loadData();
    }
}
onMounted(loadData)
</script>
<template>
    <div>
        <TaiKhoanForm :formData="selectedtable" @refresh="loadData"></TaiKhoanForm>
        <TaiKhoanTable :tableList="tableList" @detail="handleDetail" @delete="handleDelete"></TaiKhoanTable>
    </div>
</template>
