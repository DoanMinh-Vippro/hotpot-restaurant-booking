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

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
);

const props = defineProps<{
  data: any[],
  mode: string,
  label?: string,
  valueField?: string
}>();

const getColor = () => {
  switch (props.mode) {
    case "ngay":
      return "#22c55e";
    case "thang":
      return "#0ea5e9";
    case "nam":
      return "#f59e0b";
    default:
      return "#6366f1";
  }
};

const field = computed(() => props.valueField ?? "tongDoanhThu");

const chartData = computed(() => {

  const arr = props.data ?? [];

  return {
    labels: arr.map(i => i.thoiGian),
    datasets: [
      {
        label: props.label ?? "Doanh thu",
        data: arr.map(i => i[field.value] ?? 0),
        backgroundColor: getColor(),
        borderRadius: 8,
        barPercentage: 0.55,
        categoryPercentage: 0.65
      }
    ]
  };
});

const options = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      display: true
    },

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
  <div style="height:350px">
    <Bar
      :data="chartData"
      :options="options"
    />
  </div>
</template>
