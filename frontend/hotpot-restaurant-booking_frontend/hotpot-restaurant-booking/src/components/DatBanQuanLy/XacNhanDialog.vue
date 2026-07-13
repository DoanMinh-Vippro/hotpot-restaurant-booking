<script setup lang="ts">
const props = defineProps<{
  visible: boolean
  title?: string
  message?: string
}>()

const emit = defineEmits(['confirm', 'close'])

const close = () => {
  emit('close')
}

const confirmAction = () => {
  emit('confirm')
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="overlay" @click.self="close">
      <div class="dialog">
        <div class="dialog-header">
          <h2>
            {{ title || 'Xác nhận' }}
          </h2>
        </div>

        <div class="content">
          <p>
            {{ message }}
          </p>
        </div>

        <div class="dialog-footer">
          <button class="btn cancel" @click="close">Huỷ</button>

          <button class="btn confirm" @click="confirmAction">Đồng ý</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;

  inset: 0;

  background: rgba(0, 0, 0, 0.45);

  display: flex;

  justify-content: center;

  align-items: center;

  z-index: 999;
}

.dialog {
  width: 420px;

  background: #f8f5ef;

  border-radius: 18px;

  overflow: hidden;

  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.18);

  border: 1px solid #e6d8bb;
}

.dialog-header {
  padding: 24px;

  border-bottom: 1px solid #e8dcc7;
}

.dialog-header h2 {
  margin: 0;

  color: #3d3226;
}

.content {
  padding: 28px;

  font-size: 16px;

  line-height: 1.6;

  color: #54483a;
}

.dialog-footer {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  padding: 20px 24px;

  border-top: 1px solid #e8dcc7;
}

.btn {
  padding: 11px 22px;

  border: none;

  border-radius: 10px;

  cursor: pointer;

  font-weight: 600;

  transition: 0.25s;
}

.cancel {
  background: #ddd;
}

.cancel:hover {
  background: #c8c8c8;
}

.confirm {
  background: #c7a15b;

  color: white;
}

.confirm:hover {
  background: #b88e42;
}
</style>
