<script setup lang="ts">
export type OrderCategory = 'MON' | 'COMBO' | 'DO_UONG'

defineProps<{
  modelValue: OrderCategory
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: OrderCategory): void
}>()

const tabs = [
  {
    key: 'MON',
    icon: '🍲',
    label: 'Món ăn',
  },
  {
    key: 'COMBO',
    icon: '🎁',
    label: 'Combo',
  },
  {
    key: 'DO_UONG',
    icon: '🥤',
    label: 'Đồ uống',
  },
] as const

function changeTab(tab: OrderCategory) {
  emit('update:modelValue', tab)
}
</script>

<template>
  <div class="tabs">
    <div
      v-for="tab in tabs"
      :key="tab.key"
      class="tab"
      :class="{ active: modelValue === tab.key }"
      @click="changeTab(tab.key)"
    >
      <div class="icon">
        {{ tab.icon }}
      </div>

      <div class="label">
        {{ tab.label }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 14px;

  padding: 18px;

  background: white;

  border-bottom: 1px solid #ece2d4;
}

.tab {
  flex: 1;
  min-width: 0;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  gap: 6px;

  padding: 12px;

  border-radius: 18px;

  background: #f7f3eb;

  cursor: pointer;

  user-select: none;

  transition:
    background 0.25s,
    color 0.25s,
    transform 0.2s,
    box-shadow 0.25s;
}

.tab:hover {
  background: #efe5d8;
}

.tab:active {
  transform: scale(0.98);
}

.tab.active {
  background: #b7793f;
  color: white;

  box-shadow: 0 8px 18px rgba(183, 121, 63, 0.28);
}

.icon {
  font-size: 24px;
  line-height: 1;
}

.label {
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  line-height: 1.25;
}

/* ================= TABLET ================= */

@media (max-width: 992px) {
  .tabs {
    gap: 10px;
    padding: 14px;
  }

  .tab {
    padding: 10px;
    border-radius: 14px;
  }

  .icon {
    font-size: 22px;
  }

  .label {
    font-size: 12px;
  }
}

/* ================= MOBILE ================= */

@media (max-width: 768px) {
  .tabs {
    gap: 8px;
    padding: 10px;

    display: flex;
  }

  .tab {
    flex: 1;

    min-height: 70px;

    padding: 8px;

    border-radius: 12px;

    touch-action: manipulation;
  }

  .tab.active {
    box-shadow: 0 5px 12px rgba(183, 121, 63, 0.22);
  }

  .icon {
    font-size: 20px;
  }

  .label {
    font-size: 11px;
  }
}

/* ================= SMALL MOBILE ================= */

@media (max-width: 480px) {
  .tabs {
    gap: 6px;
    padding: 8px;
  }

  .tab {
    min-height: 62px;

    padding: 6px;

    border-radius: 10px;
  }

  .icon {
    font-size: 18px;
  }

  .label {
    font-size: 10px;
  }
}
</style>
