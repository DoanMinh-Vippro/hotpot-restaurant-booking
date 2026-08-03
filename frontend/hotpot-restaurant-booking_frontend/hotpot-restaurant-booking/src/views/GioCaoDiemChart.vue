<script setup lang="ts">
import { computed } from "vue";
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

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
  Filler
);

const props = defineProps<{
  data: any[]
}>();

const chartData = computed(() => ({
  labels: (props.data || []).map(i => i.thoiGian || ""),
  datasets: [
    {
      label: "Doanh thu (VNĐ)",
      data: (props.data || []).map(i => i.doanhThu ?? 0),
      borderColor: "#6366f1",
      backgroundColor: "rgba(99, 102, 241, 0.1)",
      fill: true,
      tension: 0.4,
      pointRadius: 5,
      pointBackgroundColor: "#6366f1"
    }
  ]
}));

const options = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: true },
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
    <Line :data="chartData" :options="options" />
  </div>
</template>
