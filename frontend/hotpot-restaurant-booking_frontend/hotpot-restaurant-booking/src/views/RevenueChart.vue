<script setup lang="ts">
import { Bar } from "vue-chartjs";
import { computed } from "vue";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

// ✅ CHỈ KHAI BÁO 1 LẦN
const props = defineProps<{ 
  data: any[],
  mode: string
}>();

// 🔥 đổi màu theo mode
const getColor = () => {
  if (props.mode === "ngay") return "#22c55e";   // xanh lá
  if (props.mode === "thang") return "#0ea5e9";  // xanh dương
  if (props.mode === "nam") return "#f59e0b";    // cam
  return "#0ea5e9";
};

const chartData = computed(() => {
  const currentData = Array.isArray(props.data) ? props.data : [];

  // 🔥 nếu chỉ có 1 cột → thêm padding cho đẹp
  if (currentData.length === 1) {
    return {
      labels: ["", currentData[0].thoiGian, ""],
      datasets: [
        {
          label:
            props.mode === "ngay"
              ? "Doanh thu theo ngày"
              : props.mode === "thang"
              ? "Doanh thu theo tháng"
              : "Doanh thu theo năm",

          data: [0, currentData[0].tongDoanhThu, 0],
          backgroundColor: getColor(),
          borderRadius: 8
        }
      ]
    };
  }

  return {
    labels: currentData.map(i => i?.thoiGian || ""),
    datasets: [
      {
        label:
          props.mode === "ngay"
            ? "Doanh thu theo ngày"
            : props.mode === "thang"
            ? "Doanh thu theo tháng"
            : "Doanh thu theo năm",

        data: currentData.map(i => i?.tongDoanhThu || 0),
        backgroundColor: getColor(),
        borderRadius: 8,
        barPercentage: 0.5,
        categoryPercentage: 0.6
      }
    ]
  };
});

const options = {
  responsive: true,
  maintainAspectRatio: false,
  animation: {
    duration: 800
  },
  plugins: {
    legend: { display: true }
  }
};
</script>

<template>
  <div style="height: 350px">
    <Bar :data="chartData" :options="options" />
  </div>
</template>
