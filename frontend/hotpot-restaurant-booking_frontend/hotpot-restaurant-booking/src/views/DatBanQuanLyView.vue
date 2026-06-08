<script setup lang="ts">
import BanApi from '@/api/BanApi';
import DatBanQuanLyApi from '@/api/DatBanQuanLy';
import DatBanQuanLyForm from '@/components/DatBanQuanLyForm.vue';
import DatBanQuanLyTable from '@/components/DatBanQuanLyTable.vue';
import { onMounted, ref } from 'vue';

    const list = ref([]) //bien truyen xuong table
    const form = ref<any>({}); // Khai báo là ref chứa object
    const listBan = ref([]) //biến lấy danh sách bàn để load vào select

    const loadData = async () =>{
        const response = await DatBanQuanLyApi.getAll()
        const resBan = await BanApi.getAll()
        list.value = response.data
        listBan.value = resBan.data
    }
    const detail = (datBanQuanLy: any) => {
    // Dùng spread operator để tạo bản sao mới tinh
    form.value = { ...datBanQuanLy }; 
    
    // Ép kiểu chắc chắn là số để dropdown khớp giá trị
    form.value.idBan = Number(datBanQuanLy.idBan);
    
    console.log("Form sau khi clone:", form.value);
};

    const deleteById = async (id: number) =>{
        if(confirm('ban co chac chan muon xoa?')){
        await DatBanQuanLyApi.delete(id)
        loadData()
        }
    }

    onMounted(loadData)
</script>

<template>
    <DatBanQuanLyForm 
    :datBanQuanLy="form"
    :listBan="listBan"
    @refresh="loadData"></DatBanQuanLyForm>
    <DatBanQuanLyTable :list="list"
    @detail="detail"
    @delete="deleteById"></DatBanQuanLyTable>
</template>