<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import NhanVienForm from '@/components/NhanVienForm.vue';
import NhanVienTable from '@/components/NhanVienTable.vue';
import { onMounted,ref } from 'vue';
const tableList= ref([])

const selectedtable= ref(null)

const selected= ref(null)
const loadData= async ()=>{
    const res= await NhanVienApi.getAll();
    tableList.value= res.data;
};
const handleDetail = (nv: any) => {
  console.log("DETAIL:", nv)
  selectedtable.value = nv
   selected.value = {
    ...nv,
    gioiTinh: nv.gioiTinh? 0:1,
    trangThai: nv.trangThai ? 0 : 1,
};
};
const handleDelete = async (id: number)=>{
    if(confirm("Bạn có chắc muốn xóa ?")){
        await NhanVienApi.delete(id);

        selectedtable.value = null;

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
