<script setup lang="ts">
import DatBanQuanLyApi from '@/api/DatBanQuanLy';
import DatBanQuanLyForm from '@/components/DatBanQuanLyForm.vue';
import DatBanQuanLyTable from '@/components/DatBanQuanLyTable.vue';
import { onMounted, ref } from 'vue';

    const list = ref([]) //bien truyen xuong table
    const form = ref(null)
    const loadData = async () =>{
        const response = await DatBanQuanLyApi.getAll()
        list.value = response.data
    }
    const detail = (datBanQuanLy: any)=>{
        form.value = datBanQuanLy
    }

    const deleteById = async (id: number) =>{
        if(confirm('ban co chac chan muon xoa?')){
        await DatBanQuanLyApi.delete(id)
        loadData()
        }
    }

    onMounted(loadData)
</script>

<template>
    <DatBanQuanLyForm :datBanQuanLy="form"
    @refresh="loadData"></DatBanQuanLyForm>
    <DatBanQuanLyTable :list="list"
    @detail="detail"
    @delete="deleteById"></DatBanQuanLyTable>
</template>