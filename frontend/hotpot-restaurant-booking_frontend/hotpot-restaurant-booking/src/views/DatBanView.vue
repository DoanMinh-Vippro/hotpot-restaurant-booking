<script setup lang="ts">
import DatBanApi from '@/api/DatBanApi';
import DatBanForm from '@/components/DatBanForm.vue';
import DatBanTable from '@/components/DatBanTable.vue';
import { onMounted, ref } from 'vue';

const list = ref([]) //truyen data xuong DatBanTable
const datBanForm = ref(null) //bien dung voi form

const loadData = async () =>{
    const response = await DatBanApi.getAll();
    list.value = response.data //gan data tu DatBanView lay duoc vao bien list truyen xuong DatBanTable
}

const detail = (datban: any) =>{
    // Gán dữ liệu của bàn vừa bấm vào biến form
  // Biến này sẽ được truyền xuống BanForm qua props
    datBanForm.value = datban
}

const deleteById = async (id: number) =>{
    if(confirm('ban co chac chan muon xoa')){
        await DatBanApi.delete(id)
        loadData()
    }
}

onMounted(loadData)
</script>

<template>
    <div>
        <DatBanForm :datBanForm="datBanForm"
        @refresh="loadData"></DatBanForm>        

        <DatBanTable :list="list"
        @detail="detail"
        @delete="deleteById"></DatBanTable>
    </div>
</template>