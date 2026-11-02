<template>
  <div class="adm-card">
    <div class="adm-table-wrap">
      <table class="adm-table">
        <thead>
          <tr>
            <th v-if="selectable" style="width:40px">
              <input
                class="adm-check"
                type="checkbox"
                :checked="allChecked"
                :indeterminate.prop="someChecked && !allChecked"
                @change="toggleAll($event.target.checked)"
              >
            </th>
            <th v-for="c in columns" :key="c.key" :style="c.width ? { width: c.width } : null">{{ c.label }}</th>
            <th v-if="hasActions" style="width:1%">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td :colspan="colCount" class="adm-empty">正在加载数据…</td>
          </tr>
          <tr v-else-if="!rows.length">
            <td :colspan="colCount" class="adm-empty">{{ emptyText }}</td>
          </tr>
          <template v-else>
            <tr v-for="(row, i) in rows" :key="row[rowKey] ?? i" :class="{ sel: isSelected(row) }">
              <td v-if="selectable">
                <input
                  class="adm-check"
                  type="checkbox"
                  :checked="isSelected(row)"
                  @change="toggleRow(row, $event.target.checked)"
                >
              </td>
              <td v-for="c in columns" :key="c.key">
                <slot :name="'col-' + c.key" :row="row" :index="i">
                  {{ format(row[c.key]) }}
                </slot>
              </td>
              <td v-if="hasActions">
                <div class="adm-ops">
                  <slot name="actions" :row="row"></slot>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <div v-if="total > pageSize" class="adm-pager">
      <button class="adm-btn sm" type="button" :disabled="page <= 1" @click="$emit('update:page', page - 1)">上一页</button>
      <span class="info">第 {{ page }} / {{ pageCount }} 页 · 共 {{ total }} 条</span>
      <button class="adm-btn sm" type="button" :disabled="page >= pageCount" @click="$emit('update:page', page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { computed, useSlots } from 'vue'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无数据' },
  rowKey: { type: String, default: 'id' },
  selectable: { type: Boolean, default: false },
  selected: { type: Array, default: () => [] },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageCount: { type: Number, default: 1 }
})

const emit = defineEmits(['update:page', 'update:selected'])
const slots = useSlots()

const hasActions = computed(() => !!slots.actions)
const colCount = computed(() => props.columns.length + (props.selectable ? 1 : 0) + (hasActions.value ? 1 : 0))
const allChecked = computed(() => props.rows.length > 0 && props.rows.every(isSelected))
const someChecked = computed(() => props.rows.some(isSelected))

function isSelected(row) {
  return props.selected.includes(row[props.rowKey])
}

function toggleRow(row, checked) {
  const id = row[props.rowKey]
  const next = props.selected.filter(x => x !== id)
  if (checked) next.push(id)
  emit('update:selected', next)
}

function toggleAll(checked) {
  const ids = props.rows.map(r => r[props.rowKey])
  if (!checked) {
    emit('update:selected', props.selected.filter(x => !ids.includes(x)))
    return
  }
  const merged = new Set([...props.selected, ...ids])
  emit('update:selected', [...merged])
}

function format(v) {
  return v === null || v === undefined || v === '' ? '—' : v
}
</script>
