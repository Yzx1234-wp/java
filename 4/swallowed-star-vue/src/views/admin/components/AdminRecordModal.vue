<template>
  <div v-if="open" class="adm-mask" @click.self="$emit('close')">
    <div class="adm-modal md">
      <div class="adm-modal-head">
        <h3>{{ title }}</h3>
        <span class="grow"></span>
        <button class="x" type="button" @click="$emit('close')">×</button>
      </div>
      <div class="adm-modal-body">
        <table v-if="loading || rows.length" class="adm-rec-table">
          <thead>
            <tr>
              <th v-for="c in columns" :key="c.key">{{ c.label }}</th>
              <th v-if="removable" style="width:1%">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td :colspan="columns.length + (removable ? 1 : 0)" class="adm-rec-empty">加载中…</td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td v-for="c in columns" :key="c.key">{{ row[c.key] ?? '—' }}</td>
              <td v-if="removable">
                <button class="adm-op del" type="button" @click="$emit('remove', row)">撤销</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="adm-rec-empty">{{ emptyText }}</p>
      </div>
      <div class="adm-modal-foot">
        <button class="adm-btn" type="button" @click="$emit('close')">关 闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '明细' },
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  removable: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无记录' }
})
defineEmits(['close', 'remove'])
</script>
