<template>
  <div v-if="open" class="adm-mask" @click.self="$emit('cancel')">
    <div class="adm-modal sm">
      <div class="adm-modal-head">
        <h3>{{ title }}</h3>
        <span class="grow"></span>
        <button class="x" type="button" @click="$emit('cancel')">×</button>
      </div>
      <div class="adm-modal-body">
        <p class="adm-tip" v-html="message"></p>
      </div>
      <div class="adm-modal-foot">
        <button class="adm-btn" type="button" @click="$emit('cancel')">取 消</button>
        <button
          class="adm-btn"
          :class="danger ? 'danger' : 'gold'"
          type="button"
          :disabled="loading"
          @click="$emit('confirm')"
        >{{ loading ? '处理中…' : confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '操作确认' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '确 定' },
  danger: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})
defineEmits(['confirm', 'cancel'])
</script>
