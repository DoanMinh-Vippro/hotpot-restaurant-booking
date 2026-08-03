<script setup lang="ts">
import type { DanhMuc } from '../api/DanhMucApi'

defineProps<{ item?: DanhMuc }>()
</script>

<template>
  <div v-if="item" class="hop-xem-truoc">

    <h3>Chi tiết danh mục</h3>

    <p><strong>ID:</strong> {{ item.idDanhMuc }}</p>
    <p><strong>Loại:</strong> {{ item.loaiDanhMuc }}</p>
    <p>
      <strong>Quầy:</strong> 
      <span :class="['badge-quay', item.quay?.toLowerCase()]">
        {{ item.quay === 'BEP' ? 'Bếp' : item.quay === 'BAR' ? 'Bar' : (item.quay || 'Chưa xếp') }}
      </span>
    </p>
    <p><strong>Mô tả:</strong> {{ item.moTa }}</p>

  </div>
</template>

<style scoped>
.hop-xem-truoc {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: #fff8ea;
  border: 1px solid rgba(248,212,106,.15);
  color: #5f3d22;
}

h3 {
  color: #8b5e34;
  margin-bottom: 12px;
}

p {
  margin: 6px 0;
}

/* --- BADGE BỔ SUNG CHO QUẦY --- */
.badge-quay {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  display: inline-block;
  background: #efe0c1;
  color: #8b5e34;
  margin-left: 4px;
}
.badge-quay.bep {
  background: rgba(216, 168, 92, 0.2);
  color: #8b5e34;
}
.badge-quay.bar {
  background: rgba(248, 212, 106, 0.3);
  color: #5f3d22;
}
</style>