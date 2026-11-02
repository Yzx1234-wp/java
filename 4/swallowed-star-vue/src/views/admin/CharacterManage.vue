<template>
  <div>
    <div class="adm-page-head">
      <h2>人物管理</h2>
      <p>共 {{ total }} 位人物 · 数据同步展示于前台「人物介绍」</p>
      <span class="grow"></span>
      <button class="adm-btn gold" type="button" @click="openCreate">+ 新增人物</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 姓名 / 称号 / 境界 / 势力 / 语录…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <select v-model="query.faction" class="adm-ipt sel" style="width:150px" @change="load()">
        <option value="">全部势力</option>
        <option v-for="f in factions" :key="f.key" :value="f.key">{{ f.label }}</option>
      </select>
      <select v-model="query.grade" class="adm-ipt sel" @change="load()">
        <option value="">全部分级</option>
        <option v-for="g in GRADES" :key="g" :value="g">{{ g }}</option>
      </select>
      <select v-model="query.sortBy" class="adm-ipt sel" @change="load()">
        <option value="default">按实力排序</option>
        <option value="name">按姓名排序</option>
      </select>
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
    </div>

    <div v-if="selected.length" class="adm-batch">
      <span>已选 {{ selected.length }} 位人物</span>
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
      empty-text="没有找到人物，试试调整筛选条件或新增一位"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
      @update:selected="selected = $event"
    >
      <template #col-glyph="{ row }">
        <img v-if="row.avatar" class="adm-avatar" :src="row.avatar" :alt="row.name" :title="row.avatar">
        <span v-else class="adm-glyph">{{ row.glyph || '✦' }}</span>
      </template>
      <template #col-name="{ row }">
        <span class="adm-name" :title="row.name">{{ row.name }}<small>{{ row.title || '—' }}</small></span>
      </template>
      <template #col-grade="{ row }">
        <span class="adm-tag" :style="gradeStyle(row.grade)">{{ row.grade }}</span>
      </template>
      <template #col-hot="{ row }">
        <button class="adm-pill" :class="row.hot ? 'on' : 'off'" type="button" @click="toggleHot(row)">
          {{ row.hot ? '热门' : '普通' }}
        </button>
      </template>
      <template #col-description="{ row }">
        <span class="adm-muted adm-clip" :title="row.description">{{ row.description || '—' }}</span>
      </template>
      <template #col-createdAt="{ row }"><span class="adm-muted">{{ row.createdAt || '—' }}</span></template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openEdit(row)">编辑</button>
        <button class="adm-op del" type="button" @click="askDelete(row)">删除</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="showForm"
      :title="editingId ? `编辑人物 #${editingId}` : '新增人物'"
      :fields="fields"
      :initial="form"
      :saving="saving"
      @submit="save"
      @cancel="showForm = false"
    />

    <AdminConfirm
      :open="!!deleting"
      title="删除人物"
      :message="deleting ? `确定删除人物「<b>${deleting.name}</b>」（#${deleting.id}）吗？<br><span class='warn'>删除后前台人物介绍页将不再展示，且不可恢复。</span>` : ''"
      confirm-text="确认删除"
      danger
      :loading="saving"
      @confirm="doDelete"
      @cancel="deleting = null"
    />

    <AdminConfirm
      :open="batchConfirm"
      title="批量删除"
      :message="`确定删除已勾选的 <b>${selected.length}</b> 位人物吗？<br><span class='warn'>此操作不可恢复。</span>`"
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
import { useCrudPage } from './composables/useCrudPage.js'
import { characterApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const GRADES = ['仙帝', '准仙帝', '仙王', '真仙', '尊者']
const PAGE_SIZE = 10
const GRADE_COLORS = {
  仙帝: '#ffe08a', 准仙帝: '#ff9d7a', 仙王: '#8ce8b8', 真仙: '#7fc9ff', 尊者: '#b3a684'
}

const { show } = useToast()
const {
  loading, page, pageCount, pageRows, total, selected, query,
  load, reset, fail, clearSelection
} = useCrudPage({ list: characterApi.list, pageSize: PAGE_SIZE, filters: ['keyword', 'faction', 'grade', 'sortBy'] })
query.sortBy = 'default'

const columns = [
  { key: 'glyph', label: '图标', width: '52px' },
  { key: 'name', label: '姓名' },
  { key: 'realm', label: '当前境界' },
  { key: 'faction', label: '出身势力' },
  { key: 'grade', label: '分级', width: '82px' },
  { key: 'rank', label: '实力', width: '58px' },
  { key: 'description', label: '简介', width: '200px' },
  { key: 'hot', label: '热门', width: '74px' },
  { key: 'createdAt', label: '创建时间', width: '136px' }
]

const factions = ref([])
const fields = ref([])
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deleting = ref(null)
const batchConfirm = ref(false)
const form = reactive({})

onMounted(async () => {
  await loadFactions()
  buildFields()
  load()
})

async function loadFactions() {
  try {
    const res = await characterApi.factions()
    factions.value = (res.data || []).filter(f => f.key !== '全部')
  } catch { /* 势力列表失败不阻塞主流程 */ }
}

function buildFields() {
  fields.value = [
    { key: 'name', label: '人物姓名', type: 'text', required: true, maxlength: 50, placeholder: '如 石昊' },
    { key: 'title', label: '称号', type: 'text', maxlength: 60, placeholder: '如 荒天帝' },
    { key: 'grade', label: '定位分级', type: 'select', required: true, options: GRADES.map(g => ({ value: g, label: g })) },
    { key: 'rank', label: '实力评分', type: 'number', min: 0, max: 10, step: 0.1, hint: '0-10 之间，越大越靠前' },
    { key: 'glyph', label: '卡面图标', type: 'text', maxlength: 4, placeholder: '如 🌞 🌿' },
    { key: 'avatar', label: '头像图片路径', type: 'text', maxlength: 255, span: 2, placeholder: '如 /avatars/shihao.jpg（留空则用卡面图标；图片可放前端 public/avatars/ 下）' },
    { key: 'realm', label: '当前境界', type: 'text', maxlength: 60, placeholder: '如 仙帝境' },
    { key: 'faction', label: '出身 / 势力', type: 'text', maxlength: 60, placeholder: '如 石族天庭（可直接填新势力）' },
    { key: 'hot', label: '标记为热门人物', type: 'checkbox' },
    { key: 'quote', label: '经典语录', type: 'text', maxlength: 200, span: 2, placeholder: '如 我为至尊，谁与争锋！' },
    { key: 'description', label: '人物简介', type: 'textarea', maxlength: 600, placeholder: '展示在人物介绍页的简介' }
  ]
}

function openCreate() {
  editingId.value = null
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: '', title: '', grade: '', rank: 5, glyph: '', avatar: '', realm: '', faction: '', quote: '', description: '', hot: false
  })
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: row.name || '',
    title: row.title || '',
    grade: row.grade || '',
    rank: row.rank === null || row.rank === undefined ? 0 : Number(row.rank),
    glyph: row.glyph || '',
    avatar: row.avatar || '',
    realm: row.realm || '',
    faction: row.faction || '',
    quote: row.quote || '',
    description: row.description || '',
    hot: !!row.hot
  })
  showForm.value = true
}

async function save(data) {
  const payload = {
    name: (data.name || '').trim(),
    title: (data.title || '').trim(),
    grade: data.grade,
    rank: data.rank === '' || data.rank === null ? 0 : Number(data.rank),
    glyph: (data.glyph || '').trim(),
    avatar: (data.avatar || '').trim(),
    realm: (data.realm || '').trim(),
    faction: (data.faction || '').trim(),
    quote: (data.quote || '').trim(),
    description: (data.description || '').trim(),
    hot: data.hot ? 1 : 0
  }
  saving.value = true
  try {
    if (editingId.value) {
      await characterApi.update(editingId.value, payload)
      show('人物已更新', 'ok')
    } else {
      await characterApi.create(payload)
      show('人物已新增', 'ok')
    }
    showForm.value = false
    load(true)
    loadFactions()
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
    await characterApi.remove(deleting.value.id)
    show('人物已删除', 'ok')
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
    const res = await characterApi.batchRemove(selected.value)
    show(`已删除 ${res.data ?? selected.value.length} 位人物`, 'ok')
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
    const res = await characterApi.toggleHot(row.id)
    row.hot = res.data
    show(row.hot ? '已标记为热门' : '已取消热门', 'ok')
  } catch (e) {
    fail(e, '操作失败')
  }
}

function gradeStyle(grade) {
  const c = GRADE_COLORS[grade] || '#ffe08a'
  return { color: c, borderColor: c + '66', background: c + '18' }
}
</script>
