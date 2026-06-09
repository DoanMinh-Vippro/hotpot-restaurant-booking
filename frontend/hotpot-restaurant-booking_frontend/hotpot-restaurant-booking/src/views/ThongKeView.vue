<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import ThongKeApi from "@/api/ThongKeApi";
import RevenueChart from "./RevenueChart.vue";

const dashboard = ref<any>({});
const topMon = ref<any[]>([]);
const topNhanVien = ref<any[]>([]);

const ngay = ref<any[]>([]);
const thang = ref<any[]>([]);
const nam = ref<any[]>([]);

// ✅ FIX: thêm mode
const mode = ref<"ngay" | "thang" | "nam">("thang");

const modes = ["ngay", "thang", "nam"] as const;

const load = async () => {
  try {
    const [db, mon, nv, dNgay, dThang, dNam] = await Promise.all([
      ThongKeApi.dashboard(),
      ThongKeApi.topMon(),
      ThongKeApi.topNhanVien(),
      ThongKeApi.theoNgay("2026-01-01", "2026-12-31"),
      ThongKeApi.theoThang(),
      ThongKeApi.theoNam()
    ]);

    dashboard.value = db.data;
    topMon.value = mon.data;
    topNhanVien.value = nv.data;
    ngay.value = dNgay.data;
    thang.value = dThang.data;
    nam.value = dNam.data;
  } catch (err) {
    console.error("LOAD ERROR:", err);
  }
};

const chartData = computed(() => {
  let data: any[] = [];

  if (mode.value === "ngay") data = ngay.value;
  else if (mode.value === "nam") data = nam.value;
  else data = thang.value;

  return data.map(i => ({
    ...i,
    thoiGian:
      mode.value === "ngay"
        ? (i?.thoiGian ? i.thoiGian.slice(5) : "")
        : i?.thoiGian || ""
  }));
});

onMounted(load);
</script>
<template>
  <div class="dashboard">

    <!-- HEADER -->
    <div class="header">
      <h2>🍲 POS Restaurant Dashboard</h2>
      <p>Realtime Business Overview</p>
    </div>

    <!-- KPI -->
    <div class="kpi-grid">
      <div class="kpi card-green">
        <h4>💰 Doanh thu</h4>
        <p>{{ dashboard.tongDoanhThu || 0 }}</p>
      </div>

      <div class="kpi card-blue">
        <h4>🧾 Hóa đơn</h4>
        <p>{{ dashboard.tongHoaDon || 0 }}</p>
      </div>

      <div class="kpi card-orange">
        <h4>👥 Khách hàng</h4>
        <p>{{ dashboard.tongKhachHang || 0 }}</p>
      </div>
    </div>

    <!-- FILTER -->
    <div class="filter">
      <button 
        v-for="m in modes" 
        :key="m"
        @click="mode = m"
        :class="{active: mode === m}"
      >
        {{ m }}
      </button>
    </div>

    <!-- CHART -->
    <div class="chart-box">
      <RevenueChart :key="mode" :data="chartData" />
    </div>

    <!-- GRID -->
    <div class="grid">

      <div class="box">
        <h3>🔥 Top món bán chạy</h3>
        <div v-for="m in topMon" :key="m.tenMon" class="row">
          <span>{{ m.tenMon }}</span>
          <b>{{ m.soLuongBan }}</b>
        </div>
      </div>

      <div class="box">
        <h3>👨‍🍳 Top nhân viên</h3>
        <div v-for="nv in topNhanVien" :key="nv.tenNhanVien" class="row">
          <span>{{ nv.tenNhanVien }}</span>
          <b>{{ nv.tongDoanhThu }}</b>
        </div>
      </div>

    </div>

  </div>
</template>
<style scoped>
.dashboard {
  padding: 24px;
  font-family: Inter;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
  min-height: 100vh;
}

/* HEADER */
.header h2 {
  font-size: 24px;
  font-weight: 700;
}
.header p {
  color: #64748b;
}

/* KPI */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* FIX */
  gap: 16px;
  margin: 20px 0;
}

.kpi {
  background: white;
  padding: 18px;
  border-radius: 16px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.05);
}

.kpi h4 {
  color: #64748b;
  font-size: 13px;
}
.kpi p {
  font-size: 22px;
  font-weight: bold;
}

.card-green { border-left: 4px solid #22c55e; }
.card-blue { border-left: 4px solid #3b82f6; }
.card-orange { border-left: 4px solid #f59e0b; }

/* FILTER */
.filter {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.filter button {
  padding: 8px 14px;
  border-radius: 10px;
  border: none;
  background: #e2e8f0;
  cursor: pointer;
}

.filter button.active {
  background: #0ea5e9;
  color: white;
}

/* CHART */
.chart-box {
  background: white;
  padding: 16px;
  border-radius: 16px;
  margin-bottom: 20px;
}

/* GRID */
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.box {
  background: white;
  border-radius: 16px;
  padding: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

</style>