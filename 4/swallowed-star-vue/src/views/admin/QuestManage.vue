<template>
  <div>
    <div class="adm-page-head">
      <h2>悬赏任务</h2>
      <p>共 {{ total }} 项悬赏 · 数据同步展示于前台「任务接取」</p>
      <span class="grow"></span>
      <button class="adm-btn gold" type="button" @click="openCreate">+ 新增悬赏</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 任务名 / 等级 / 发布方 / 简介…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <select v-model="query.grade" class="adm-ipt sel" @change="load()">
        <option value="">全部等级</option>
        <option v-for="g in GRADES" :key="g" :value="g">{{ g }}</option>
      </select>
      <select v-model="query.sortBy" class="adm-ipt sel" @change="load()">
        <option value="default">按积分排序</option>
        <option value="name">按名称排序</option>
      </select>
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
    </div>

    <div v-if="selected.length" class="adm-batch">
      <span>已选 {{ selected.length }} 项悬赏</span>
      <span class="grow"></span>
      <button class="adm-btn sm danger" type="button" @click="batchConfirm = true">批量删除</button>
      <button class="adm-btn sm" type="button" @click="clearSelection">取消选择</button>
    </div>

    <AdminTable
      v-model:page="page"
      :columns="columns"
      :rows="pageRows"
      :loading="loading"
      :selected="selected"
      selectable
      empty-text="没有找到悬赏任务，试试调整筛选条件或新增一项"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
      @update:selected="selected = $event"
    >
      <template #col-glyph="{ row }"><span class="adm-glyph">{{ row.glyph || '⚔' }}</span></template>
      <template #col-name="{ row }">
        <span class="adm-name" :title="row.name">{{ row.name }}<small>{{ row.poster || '—' }}</small></span>
      </template>
      <template #col-grade="{ row }">
        <span class="adm-tag" :style="gradeStyle(row.grade)">{{ row.grade }}</span>
      </template>
      <template #col-points="{ row }"><b style="color:var(--gold)">{{ row.points ?? 0 }}</b></template>
      <template #col-acceptedCount="{ row }">
        <span>{{ row.acceptedCount || 0 }} 人<small class="adm-muted" style="display:block">完成 {{ row.completedCount || 0 }}</small></span>
      </template>
      <template #col-hot="{ row }">
        <button class="adm-pill" :class="row.hot ? 'on' : 'off'" type="button" @click="toggleHot(row)">
          {{ row.hot ? '热门' : '普通' }}
        </button>
      </template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openEdit(row)">编辑</button>
        <button class="adm-op" type="button" @click="openRecords(row)">接取记录</button>
        <button class="adm-op del" type="button" @click="askDelete(row)">删除</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="showForm"
      :title="editingId ? `编辑悬赏 #${editingId}` : '新增悬赏'"
      :fields="fields"
      :initial="form"
      :saving="saving"
      @submit="save"
      @cancel="showForm = false"
    />

    <AdminRecordModal
      :open="records.open"
      :title="records.title"
      :columns="recordColumns"
      :rows="records.rows"
      :loading="records.loading"
      :busy-id="records.busyId"
      removable
      completable
      hint="修士接取后记录为 <b>待完成</b>，点「完成」才真正发放积分；已完成的记录可点「撤销完成」收回积分。"
      empty-text="当前还没有修士接取这项悬赏"
      @close="records.open = false"
      @complete="completeAccept"
      @revert="revertAccept"
      @remove="removeAccept"
    />

    <AdminConfirm
      :open="!!deleting"
      title="删除悬赏"
      :message="deleting ? `确定删除悬赏「<b>${deleting.name}</b>」（#${deleting.id}）吗？<br><span class='warn'>该任务下的接取记录会一并清除，修士积分将相应减少，且不可恢复。</span>` : ''"
      confirm-text="确认删除"
      danger
      :loading="saving"
      @confirm="doDelete"
      @cancel="deleting = null"
    />

    <AdminConfirm
      :open="batchConfirm"
      title="批量删除"
      :message="`确定删除已勾选的 <b>${selected.length}</b> 项悬赏吗？<br><span class='warn'>对应接取记录与积分会一并清除，此操作不可恢复。</span>`"
      confirm-text="全部删除"
      danger
      :loading="saving"
      @confirm="doBatchDelete"
      @cancel="batchConfirm = false"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import AdminTable from './components/AdminTable.vue'
import AdminFormModal from './components/AdminFormModal.vue'
import AdminConfirm from './components/AdminConfirm.vue'
import AdminRecordModal from './components/AdminRecordModal.vue'
import { useCrudPage } from './composables/useCrudPage.js'
import { questApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const GRADES = ['仙帝', '仙王', '真仙', '上古']
const PAGE_SIZE = 10
const GRADE_COLORS = { 仙帝: '#ffe08a', 仙王: '#8ce8b8', 真仙: '#7fc9ff', 上古: '#b3a684' }

const { show } = useToast()
const {
  loading, page, pageCount, pageRows, total, selected, query,
  load, reset, fail, clearSelection
} = useCrudPage({ list: questApi.list, pageSize: PAGE_SIZE, filters: ['keyword', 'grade', 'sortBy'] })
query.sortBy = 'default'

const columns = [
  { key: 'glyph', label: '图标', width: '52px' },
  { key: 'name', label: '任务' },
  { key: 'grade', label: '等级', width: '74px' },
  { key: 'points', label: '完成积分', width: '86px' },
  { key: 'acceptedCount', label: '接取 / 完成', width: '96px' },
  { key: 'hot', label: '热门', width: '74px' },
  { key: 'createdAt', label: '创建时间', width: '136px' }
]

const recordColumns = [
  { key: 'username', label: '接取账号' },
  { key: 'points', label: '积分', type: 'points', format: (r) => '+' + (r.points || 0) },
  { key: 'status', label: '状态', type: 'status' },
  { key: 'acceptedAt', label: '接取时间' },
  { key: 'completedAt', label: '完成时间', format: (r) => fmtTime(r.completedAt) },
  { key: 'completedBy', label: '确认人' }
]

const fields = [
  { key: 'name', label: '任务名称', type: 'text', required: true, maxlength: 50, placeholder: '如 猎杀堕落仙帝残魂' },
  { key: 'grade', label: '任务等级', type: 'select', required: true, options: GRADES.map(g => ({ value: g, label: g })) },
  { key: 'points', label: '完成积分', type: 'number', min: 0, max: 100000, step: 10, hint: '后台点「完成」后发放；参考：仙帝 1000 / 仙王 600 / 真仙 300 / 上古 100' },
  { key: 'glyph', label: '卡面图标', type: 'text', maxlength: 4, placeholder: '如 ⚔ 👑' },
  { key: 'poster', label: '发布方', type: 'text', maxlength: 60, placeholder: '如 上界 · 天庭悬赏' },
  { key: 'hot', label: '标记为热门任务', type: 'checkbox' },
  { key: 'desc', label: '任务简介', type: 'textarea', maxlength: 600, placeholder: '展示在任务卡片上的简介' }
]

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deleting = ref(null)
const batchConfirm = ref(false)
const form = reactive({})
const records = reactive({ open: false, id: null, name: '', title: '', rows: [], loading: false, busyId: null })

onMounted(load)

function openCreate() {
  editingId.value = null
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, { name: '', grade: '', points: 100, glyph: '', poster: '', desc: '', hot: false })
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: row.name || '',
    grade: row.grade || '',
    points: row.points === null || row.points === undefined ? 0 : Number(row.points),
    glyph: row.glyph || '',
    poster: row.poster || '',
    desc: row.desc || '',
    hot: !!row.hot
  })
  showForm.value = true
}

async function save(data) {
  const payload = {
    name: (data.name || '').trim(),
    grade: data.grade,
    points: data.points === '' || data.points === null ? 0 : Number(data.points),
    glyph: (data.glyph || '').trim(),
    poster: (data.poster || '').trim(),
    desc: (data.desc || '').trim(),
    hot: data.hot ? 1 : 0
  }
  saving.value = true
  try {
    if (editingId.value) {
      await questApi.update(editingId.value, payload)
      show('悬赏已更新', 'ok')
    } else {
      await questApi.create(payload)
      show('悬赏已新增', 'ok')
    }
    showForm.value = false
    load(true)
  } catch (e) {
    fail(e, '保存失败')
  } finally {
    saving.value = false
  }
}

function askDelete(row) {
  deleting.value = row
}

async function doDelete() {
  if (!deleting.value) return
  saving.value = true
  try {
    await questApi.remove(deleting.value.id)
    show('悬赏已删除', 'ok')
    deleting.value = null
    load(true)
  } catch (e) {
    fail(e, '删除失败')
  } finally {
    saving.value = false
  }
}

async function doBatchDelete() {
  saving.value = true
  try {
    const res = await questApi.batchRemove(selected.value)
    show(`已删除 ${res.data ?? selected.value.length} 项悬赏`, 'ok')
    batchConfirm.value = false
    load(true)
  } catch (e) {
    fail(e, '批量删除失败')
  } finally {
    saving.value = false
  }
}

async function toggleHot(row) {
  try {
    const res = await questApi.toggleHot(row.id)
    row.hot = res.data
    show(row.hot ? '已标记为热门' : '已取消热门', 'ok')
  } catch (e) {
    fail(e, '操作失败')
  }
}

async function openRecords(row) {
  records.open = true
  records.id = row.id
  records.name = row.name
  records.title = `「${row.name}」接取记录`
  records.rows = []
  await loadRecords()
}

async function loadRecords() {
  records.loading = true
  try {
    const res = await questApi.accepts(records.id)
    records.rows = res.data || []
  } catch (e) {
    fail(e, '接取记录加载失败')
  } finally {
    records.loading = false
  }
}

/* 点「完成」：后台确认后才为修士发放积分 */
async function completeAccept(row) {
  records.busyId = row.id
  try {
    const res = await questApi.completeAccept(row.id)
    const points = (res.data && res.data.points) || 0
    show(`已确认完成，为 ${row.username} 发放 ${points} 积分`, 'ok')
    await loadRecords()
    load(true)
  } catch (e) {
    fail(e, '确认完成失败')
  } finally {
    records.busyId = null
  }
}

/* 撤销完成：记录回到待完成，积分收回 */
async function revertAccept(row) {
  records.busyId = row.id
  try {
    const res = await questApi.revertAccept(row.id)
    const lost = (res.data && res.data.withdrawn) || 0
    show(`已撤销完成，收回 ${row.username} 的 ${lost} 积分`, 'ok')
    await loadRecords()
    load(true)
  } catch (e) {
    fail(e, '撤销完成失败')
  } finally {
    records.busyId = null
  }
}

async function removeAccept(row) {
  try {
    await questApi.removeAccept(row.id)
    show('已删除该接取记录', 'ok')
    await loadRecords()
    load(true)
  } catch (e) {
    fail(e, '删除失败')
  }
}

function fmtTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

function gradeStyle(grade) {
  const c = GRADE_COLORS[grade] || '#ffe08a'
  return { color: c, borderColor: c + '66', background: c + '18' }
}
</script>
