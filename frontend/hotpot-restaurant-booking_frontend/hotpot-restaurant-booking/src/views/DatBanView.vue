<script setup lang="ts">
import DatBanApi from '@/api/DatBanApi';
import DatBanForm from '@/components/DatBanForm.vue';
import DatBanTable from '@/components/DatBanTable.vue';
import { onMounted, ref } from 'vue';

const list = ref([]) // Truyền data xuống DatBanTable
const datBanForm = ref(null) // Biến dùng với form

const loadData = async () =>{
    const response = await DatBanApi.getAll();
    list.value = response.data 
}

const detail = (datban: any) =>{
    datBanForm.value = datban
}

const deleteById = async (id: number) =>{
    if(confirm('Bạn có chắc chắn muốn xóa?')){
        await DatBanApi.delete(id)
        loadData()
    }
}

onMounted(loadData)
</script>

<template>
    <div class="dat-ban-layout">
        <DatBanForm :datBanForm="datBanForm" @refresh="loadData" />        

        <DatBanTable :list="list" @detail="detail" @delete="deleteById" />
    </div>
</template>

<style scoped>
.dat-ban-layout {
  min-height: 100vh;
  padding: 30px;
  background: #0f0f0f;
  display: grid;
  grid-template-columns: 460px 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1200px) {
  .dat-ban-layout {
    grid-template-columns: 1fr;
  }
}
</style>