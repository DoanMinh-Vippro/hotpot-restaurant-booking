<script setup lang="ts">
import { Pie } from "vue-chartjs";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend
} from "chart.js";
import { computed } from "vue";

ChartJS.register(
  ArcElement,
  Tooltip,
  Legend
);

const props = defineProps<{
  data: any[]
}>();

const chartData = computed(() => ({
  labels: (props.data || []).map(i => {
    if (i.trangThai == 0) return "Chưa cọc";
    if (i.trangThai == 1) return "Đã cọc";
    return "Hoàn cọc";
  }),

  datasets: [
    {
      data: (props.data || []).map(i => i.soLuong),

      backgroundColor: [
        "#ef4444",
        "#22c55e",
        "#f59e0b"
      ]
    }
  ]
}));

const options = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom" as const
    }
  }
};
</script>

<template>
  <div style="height:320px">
    <Pie
      :data="chartData"
      :options="options"
    />
  </div>
</template>
