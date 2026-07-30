<script setup lang="ts">
export type OrderCategory = 'MON' | 'COMBO' | 'DO_UONG'

defineProps<{
  modelValue: OrderCategory
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: OrderCategory): void
}>()

const changeTab = (tab: OrderCategory) => {
  emit('update:modelValue', tab)
}
</script>

<template>
  <div class="tabs">
    <button :class="{ active: modelValue === 'MON' }" @click="changeTab('MON')">🍲 MÓN ĂN</button>

    <button :class="{ active: modelValue === 'COMBO' }" @click="changeTab('COMBO')">
      🎁 COMBO
    </button>

    <button :class="{ active: modelValue === 'DO_UONG' }" @click="changeTab('DO_UONG')">
      🥤 ĐỒ UỐNG
    </button>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 14px;
  padding: 18px 22px;

  background: #fffdf9;

  border-bottom: 1px solid #eadfce;
}

.tabs button {
  padding: 10px 22px;

  border: none;
  border-radius: 14px;

  background: #efe6da;

  color: #705842;

  font-size: 14px;
  font-weight: 700;

  cursor: pointer;

  transition: all 0.25s ease;
}

.tabs button:hover {
  background: #e3d3bf;
}

.tabs button.active {
  background: #b7793f;
  color: white;

  box-shadow: 0 6px 18px rgba(183, 121, 63, 0.25);
}
</style>
