<script setup lang="ts">
const props = defineProps<{
  chiTiet: any
}>()

const formatMoney = (v: number) => (v ?? 0).toLocaleString('vi-VN') + ' đ'
</script>

<template>
  <div class="detail">
    <h3>Hóa đơn hiện tại</h3>

    <div class="section">
      <h4>Món ăn</h4>

      <div v-for="item in chiTiet.dsMon" :key="item.idHoaDonChiTiet" class="row">
        <div>
          <div class="name">{{ item.tenMon }}</div>
          <div class="qty">x{{ item.soLuong }}</div>
        </div>

        <div class="price">
          {{ formatMoney(item.thanhTien) }}
        </div>
      </div>

      <div v-if="!chiTiet.dsMon.length" class="empty">Chưa có món</div>
    </div>

    <div class="section">
      <h4>Combo</h4>

      <div v-for="item in chiTiet.dsCombo" :key="item.idHoaDonChiTiet" class="row">
        <div>
          <div class="name">{{ item.tenCombo }}</div>
          <div class="qty">x{{ item.soLuong }}</div>
        </div>

        <div class="price">
          {{ formatMoney(item.thanhTien) }}
        </div>
      </div>

      <div v-if="!chiTiet.dsCombo.length" class="empty">Chưa có combo</div>
    </div>

    <div class="total">
      Tổng:
      {{ formatMoney(chiTiet.tongTien) }}
    </div>
  </div>
</template>

<style scoped>
.detail {
  height: 100%;
  padding: 18px;
  overflow: auto;
  background: #fffdf9;
}

.section {
  margin-top: 20px;
}

.row {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  padding: 10px;
  border-radius: 10px;
  background: #f7f3eb;
}

.name {
  font-weight: 600;
}

.qty {
  color: #777;
  font-size: 13px;
}

.price {
  font-weight: 700;
  color: #b7793f;
}

.total {
  margin-top: 25px;
  padding-top: 15px;
  border-top: 1px solid #ddd;
  font-size: 18px;
  font-weight: bold;
}

.empty {
  margin-top: 10px;
  color: #999;
}
</style>
