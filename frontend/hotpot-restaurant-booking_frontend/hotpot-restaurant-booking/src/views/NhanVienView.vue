<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import NhanVienForm from '@/components/NhanVienForm.vue';
import NhanVienTable from '@/components/NhanVienTable.vue';
import { onMounted,ref } from 'vue';
const tableList= ref([])
const toBoolean = (val: any) => {
  if (val === true || val === 1 || val === "1") return true;
  if (val === false || val === 0 || val === "0") return false;

  if (typeof val === "string") {
    return val.toLowerCase() === "true";
  }

  return false;
};
const selectedtable= ref(null)

const selected= ref(null)
const loadData= async ()=>{
     const res = await NhanVienApi.getAll();

  tableList.value = res.data.map((nv: any) => ({
    ...nv,
    gioiTinh: toBoolean(nv.gioiTinh),
    trangThai: toBoolean(nv.trangThai),
  }));

  selected.value = null;
};
const handleDetail = (nv: any) => {
  console.log("DETAIL:", nv)
  selectedtable.value = nv
    selected.value = { ...nv }
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
        <NhanVienForm :selectedNhanVien="selected" @refresh="loadData"></NhanVienForm>
        <NhanVienTable :tableList="tableList" @detail="handleDetail" @delete="handleDelete"></NhanVienTable>
    </div>
</template>
