<script setup lang="ts">
import { ref } from 'vue'
import type { DanhMuc } from '../api/DanhMucApi'

defineProps<{
  danhSach: DanhMuc[]
  selectedId: number | null
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'search', 'reset'])

const searchLoaiDanhMuc = ref('')

const kíchHoạtTìmKiem = () => {
  emit('search', searchLoaiDanhMuc.value)
}

const kíchHoạtLamMoi = () => {
  searchLoaiDanhMuc.value = ''
  emit('reset')
}

const xoa = (id: number) => {
  if (confirm('Ngưng danh mục này?')) {
    emit('delete', id)
  }
}
</script>

<template>
  <div class="khu-vuc-danh-sach">
    <div class="bo-loc-panel">
      <input
        v-model="searchLoaiDanhMuc"
        type="text"
        placeholder="🔍 Nhập loại danh mục cần tìm..."
        @keyup.enter="kíchHoạtTìmKiem"
      />
      <button class="nut-tim" @click="kíchHoạtTìmKiem">Tìm kiếm</button>
      <button class="nut-lam-moi" @click="kíchHoạtLamMoi">Làm mới</button>
    </div>

    <section class="danh-sach-panel">
      <div class="tieu-de-panel">
        <div>
          <h2>Danh sách danh mục</h2>
          <p>Quản lý danh mục hệ thống</p>
        </div>
        <button class="nut-phu" @click="$emit('add')">Thêm danh mục</button>
      </div>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Loại danh mục</th>
            <th>Mô tả</th>
            <!-- BỔ SUNG CỘT QUẦY -->
            <th>Quầy</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in danhSach"
            :key="item.idDanhMuc"
            :class="{ active: item.idDanhMuc === selectedId }"
          >
            <td>{{ item.idDanhMuc }}</td>
            <td>{{ item.loaiDanhMuc }}</td>
            <td>{{ item.moTa }}</td>
            <!-- BỔ SUNG DỮ LIỆU CỘT QUẦY -->
            <td>
              <span :class="['badge-quay', item.quay?.toLowerCase()]">
                {{ item.quay === 'BEP' ? 'Bếp' : item.quay === 'BAR' ? 'Bar' : item.quay }}
              </span>
            </td>
            <td class="hanh-dong">
              <button class="nut-sua" @click="$emit('edit', item)">Sửa</button>
              <button class="nut-xoa" @click="xoa(item.idDanhMuc)">Xoá</button>
            </td>
          </tr>
          <tr v-if="danhSach.length === 0">
            <!-- TĂNG COLSPAN LÊN 5 CHO KHỚP CỘT -->
            <td colspan="5" style="text-align: center; color: #a0a0a0; padding: 20px">
              Không tìm thấy danh mục phù hợp.
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.khu-vuc-danh-sach {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.bo-loc-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.bo-loc-panel input {
  background: #fffdf8;
  border: 1px solid #e6d2aa;
  color: #5f3d22;
  padding: 10px 14px;
  border-radius: 12px;
  outline: none;
  flex: 1;
}
.bo-loc-panel input:focus {
  border-color: #f8d46a;
}
.nut-tim,
.nut-lam-moi {
  padding: 10px 18px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.nut-tim {
  background: #d8a85c;
  color: #3d2814;
}
.nut-lam-moi {
  background: #fff3d3;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
}
.danh-sach-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 28px;
  padding: 26px;
}
.tieu-de-panel {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.tieu-de-panel h2 {
  color: #8b5e34;
}
.tieu-de-panel p {
  color: #8f6b46;
}
table {
  width: 100%;
  border-collapse: collapse;
  color: #5f3d22;
}
th {
  text-align: left;
  padding: 12px;
  color: #8b5e34;
}
td {
  padding: 14px;
  border-bottom: 1px solid #efe0c1;
}
tr.active {
  background: rgba(248, 212, 106, 0.08);
}
.hanh-dong {
  display: flex;
  gap: 8px;
}
.nut-sua {
  background: rgba(248, 212, 106, 0.15);
  color: #f8d46a;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.nut-xoa {
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.nut-phu {
  border: none;
  padding: 10px 16px;
  border-radius: 12px;
  background: #f8d46a;
  color: #1a1410;
  font-weight: 600;
  cursor: pointer;
}

/* --- THÊM STYLE BADGE CHO QUẦY --- */
.badge-quay {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  display: inline-block;
  background: #efe0c1;
  color: #8b5e34;
}
.badge-quay.bep {
  background: rgba(216, 168, 92, 0.2);
  color: #8b5e34;
}
.badge-quay.bar {
  background: rgba(248, 212, 106, 0.3);
  color: #5f3d22;
}
</style>