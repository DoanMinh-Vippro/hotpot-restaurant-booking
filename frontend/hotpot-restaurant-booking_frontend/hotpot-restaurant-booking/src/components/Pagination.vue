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
  <div class="thanh-phan-trang" >
    <button :disabled="pageNo === 0" @click="chuyenTrang(0)" class="nut-trang">«</button>
    <button :disabled="pageNo === 0" @click="chuyenTrang(pageNo - 1)" class="nut-trang">◀</button>
    
    <span class="nhan-trang">Trang <b>{{ pageNo + 1 }}</b> / {{ totalPages }}</span>

    <button :disabled="pageNo >= totalPages - 1" @click="chuyenTrang(pageNo + 1)" class="nut-trang">▶</button>
    <button :disabled="pageNo >= totalPages - 1" @click="chuyenTrang(totalPages - 1)" class="nut-trang">»</button>
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
  background: rgba(15, 15, 15, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  user-select: none;
}
.nut-trang {
  padding: 8px 14px;
  background: rgba(248, 212, 106, 0.08);
  color: #f8d46a;
  border: 1px solid rgba(248, 212, 106, 0.15);
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}
.nut-trang:disabled {
  background: rgba(255, 255, 255, 0.01);
  border-color: rgba(255, 255, 255, 0.03);
  color: #555;
  cursor: not-allowed;
}
.nut-trang:not(:disabled):hover {
  background: #f8d46a;
  color: #1a1410;
  box-shadow: 0 0 12px rgba(248, 212, 106, 0.2);
}
.nhan-trang {
  color: #c7c7c7;
  font-size: 13px;
}
.nhan-trang b { color: #f8d46a; }
</style>