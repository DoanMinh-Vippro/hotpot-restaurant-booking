<script setup lang="ts">
import ChucVuApi from '@/api/ChucVuApi';
import ChucVuForm from '@/components/ChucVuForm.vue';
import ChucVuTable from '@/components/ChucVuTable.vue';
import { onMounted,ref } from 'vue';
const tableList= ref([])
const selectedtable= ref(null)
const loadData= async ()=>{
    const res= await ChucVuApi.getAll();
    tableList.value= res.data;
};
const handleDetail = (table: any) => {
  console.log("DETAIL:", table)
  selectedtable.value = table
}
const handleDelete = async (id: number)=>{
    if(confirm("Bạn có chắc muốn xóa ?")){
        await ChucVuApi.delete(id);
        selectedtable.value = null;
        loadData();
    }
}
onMounted(loadData)
</script>
<template>
    <div>
        <ChucVuForm :formData="selectedtable" @refresh="loadData"></ChucVuForm>
        <ChucVuTable :tableList="tableList" @detail="handleDetail" @delete="handleDelete"></ChucVuTable>
    </div>
</template>
