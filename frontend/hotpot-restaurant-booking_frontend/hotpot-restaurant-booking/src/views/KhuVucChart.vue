<script setup lang="ts">
import { computed } from "vue";
import { Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const props = defineProps<{
  data: any[]
}>();

const chartData = computed(() => ({
  labels: (props.data || []).map(i => i.khuVuc || ""),
  datasets: [
    {
      label: "Doanh thu (VNĐ)",
      data: (props.data || []).map(i => i.doanhThu ?? 0),
      backgroundColor: ["#3b82f6", "#22c55e", "#f59e0b"],
      borderRadius: 8
    }
  ]
}));

const options = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label(context: any) {
          return Number(context.raw).toLocaleString() + " đ";
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback(value: any) {
          return Number(value).toLocaleString();
        }
      }
    }
  }
};
</script>

<template>
  <div style="height:280px">
    <Bar :data="chartData" :options="options" />
  </div>
</template>
