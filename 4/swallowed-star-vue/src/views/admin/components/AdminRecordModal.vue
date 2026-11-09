<template>
  <div v-if="open" class="adm-mask" @click.self="$emit('close')">
    <div class="adm-modal lg">
      <div class="adm-modal-head">
        <h3>{{ title }}</h3>
        <span class="grow"></span>
        <button class="x" type="button" @click="$emit('close')">×</button>
      </div>
      <div class="adm-modal-body">
        <p v-if="hint" class="adm-tip" v-html="hint"></p>
        <table v-if="loading || rows.length" class="adm-rec-table">
          <thead>
            <tr>
              <th v-for="c in columns" :key="c.key">{{ c.label }}</th>
              <th v-if="hasActions" style="width:1%">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td :colspan="columns.length + (hasActions ? 1 : 0)" class="adm-rec-empty">加载中…</td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td v-for="c in columns" :key="c.key">
                <span
                  v-if="c.type === 'status'"
                  class="adm-pill"
                  :class="isDone(row[c.key]) ? 'on' : 'off'"
                >{{ statusText(row[c.key]) }}</span>
                <span v-else-if="c.type === 'points'" :style="pointStyle(row)">{{ cell(c, row) }}</span>
                <template v-else>{{ cell(c, row) }}</template>
              </td>
              <td v-if="hasActions">
                <div class="adm-ops">
                  <button
                    v-if="completable && !isDone(row.status)"
                    class="adm-op gold"
                    type="button"
                    :disabled="busyId === row.id"
                    :title="`确认完成后为 ${row.username} 发放 ${row.points || 0} 积分`"
                    @click="$emit('complete', row)"
                  >完成 +{{ row.points || 0 }}</button>
                  <button
                    v-if="completable && isDone(row.status)"
                    class="adm-op"
                    type="button"
                    :disabled="busyId === row.id"
                    title="撤销完成后该记录回到待完成，积分同时收回"
                    @click="$emit('revert', row)"
                  >撤销完成</button>
                  <button
                    v-if="removable"
                    class="adm-op del"
                    type="button"
                    title="直接删除这条记录"
                    @click="$emit('remove', row)"
                  >删除记录</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="adm-rec-empty">{{ emptyText }}</p>
      </div>
      <div class="adm-modal-foot">
        <span v-if="completable && rows.length" class="adm-muted" style="margin-right:auto">
          共 {{ rows.length }} 条 · 已完成 {{ doneCount }} · 待完成 {{ rows.length - doneCount }}
        </span>
        <button class="adm-btn" type="button" @click="$emit('close')">关 闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '明细' },
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  removable: { type: Boolean, default: false },
  /** 是否启用「完成 / 撤销完成」（审核发积分） */
  completable: { type: Boolean, default: false },
  /** 弹窗顶部提示（支持简单 HTML） */
  hint: { type: String, default: '' },
  /** 正在处理中的记录 id（按钮置灰防重复点） */
  busyId: { type: [Number, String], default: null },
  emptyText: { type: String, default: '暂无记录' }
})
defineEmits(['close', 'remove', 'complete', 'revert'])

const hasActions = computed(() => props.removable || props.completable)
const doneCount = computed(() => props.rows.filter(r => r.status === 'COMPLETED').length)

function isDone(status) {
  return status === 'COMPLETED'
}

function statusText(status) {
  return isDone(status) ? '已完成' : '待完成'
}

function cell(column, row) {
  if (typeof column.format === 'function') return column.format(row)
  const v = row[column.key]
  return v === null || v === undefined || v === '' ? '—' : v
}

function pointStyle(row) {
  return isDone(row.status)
    ? { color: 'var(--gold)', fontWeight: 700 }
    : { color: 'var(--muted)' }
}
</script>
