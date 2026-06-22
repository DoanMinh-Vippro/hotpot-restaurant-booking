<script setup lang="ts">
import NhanVienApi from '@/api/NhanVienApi';
import NhanVienForm from '@/components/NhanVienForm.vue';
import NhanVienTable from '@/components/NhanVienTable.vue';
import { onMounted, ref } from 'vue';

const tableList = ref<any[]>([])
const selected = ref<any | null>(null)

// ✅ util dùng chung
const toBoolean = (val: any) => {
  if (val === true || val === 1 || val === "1") return true;
  if (val === false || val === 0 || val === "0") return false;

  // xử lý string "true"/"false"
  if (typeof val === "string") {
    return val.toLowerCase() === "true";
  }

  return false;
};
const loadData = async () => {
    const res = await NhanVienApi.getAll();

  tableList.value = res.data.map((nv: any) => ({
    ...nv,
    gioiTinh: toBoolean(nv.gioiTinh),
    trangThai: toBoolean(nv.trangThai),
  }));

  selected.value = null;

   // reset form
};

const handleDetail = (nv: any) => {
  selected.value = { ...nv };
};

const handleDelete = async (id: number) => {
  if (confirm("Bạn có chắc muốn xóa ?")) {
    await NhanVienApi.delete(id);
    loadData();
  }
};

onMounted(loadData);
</script>

<template>
  <div>
    <!-- 🔥 KEY giúp re-render form -->
    <NhanVienForm
      :key="JSON.stringify(selected)"
      :selectedNhanVien="selected"
      @refresh="loadData"
    />

    <NhanVienTable
      :tableList="tableList"
      @detail="handleDetail"
      @delete="handleDelete"
    />
  </div>
</template>
