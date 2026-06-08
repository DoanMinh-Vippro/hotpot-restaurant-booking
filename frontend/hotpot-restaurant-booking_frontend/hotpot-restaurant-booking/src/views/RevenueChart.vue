<script setup lang="ts">
import { Line } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
  Filler
} from "chart.js";
import { computed, watch, ref } from "vue";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend, Filler);

const props = defineProps<{ data: any[] }>();

// ✅ dùng ref để force update
const chartRef = ref();

const chartData = computed(() => ({
  labels: props.data.map(i => i.thoiGian),
  datasets: [
    {
      label: "Doanh thu",
      data: props.data.map(i => i.doanhThu),
      borderColor: "#0ea5e9",
      backgroundColor: "rgba(14,165,233,0.2)",
      fill: true,
      tension: 0.4
    }
  ]
}));


watch(() => props.data, () => {
  if (chartRef.value) {
    chartRef.value.chart.update();
  }
}, { deep: true });

const options = {
  responsive: true,
  plugins: {
    legend: { display: true }
  }
};
</script>
<template>
  <Line ref="chartRef" :data="chartData" :options="options" />
</template>