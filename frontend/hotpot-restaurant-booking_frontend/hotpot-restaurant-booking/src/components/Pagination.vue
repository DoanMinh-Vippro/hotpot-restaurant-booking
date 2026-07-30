<script setup lang="ts">
defineProps<{
  pageNo: number
  totalPages: number
}>()

const emit = defineEmits(['change-page'])

const chuyenTrang = (trangMucTieu: number) => {
  emit('change-page', trangMucTieu)
}
</script>

<template>
  <div class="thanh-phan-trang">
    <button :disabled="pageNo === 0" @click="chuyenTrang(0)" class="nut-trang">«</button>
    <button :disabled="pageNo === 0" @click="chuyenTrang(pageNo - 1)" class="nut-trang">◀</button>
    
    <span class="nhan-trang">Trang <b>{{ pageNo + 1 }}</b> / {{ totalPages || 1 }}</span>

    <button :disabled="pageNo >= totalPages - 1 || totalPages === 0" @click="chuyenTrang(pageNo + 1)" class="nut-trang">▶</button>
    <button :disabled="pageNo >= totalPages - 1 || totalPages === 0" @click="chuyenTrang(totalPages - 1)" class="nut-trang">»</button>
  </div>
</template>

<style scoped>
.thanh-phan-trang {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  padding: 12px 20px;
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 16px;
  user-select: none;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.nut-trang {
  padding: 8px 14px;
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.nut-trang:disabled {
  background: #fffdf8;
  border-color: #efe0c1;
  color: #d1bba2;
  cursor: not-allowed;
}

.nut-trang:not(:disabled):hover {
  background: #d8a85c;
  color: #3d2814;
  border-color: #d8a85c;
  box-shadow: 0 4px 12px rgba(216, 168, 92, 0.25);
}

.nhan-trang {
  color: #8f6b46;
  font-size: 14px;
}

.nhan-trang b { 
  color: #8b5e34; 
  font-weight: 700;
}
</style>