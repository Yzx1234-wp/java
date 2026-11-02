<template>
  <div>
    <div class="adm-page-head">
      <h2>功法管理</h2>
      <p>共 {{ total }} 部典籍 · 数据同步展示于前台「大荒功法库」</p>
      <span class="grow"></span>
      <button class="adm-btn gold" type="button" @click="openCreate">+ 新增典籍</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 名称 / 分类 / 传承 / 修习者 / 简介…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <select v-model="query.cat" class="adm-ipt sel" @change="load()">
        <option value="">全部分类</option>
        <option v-for="c in cats" :key="c.code" :value="c.code">{{ c.name }}</option>
      </select>
      <select v-model="query.grade" class="adm-ipt sel" @change="load()">
        <option value="">全部品阶</option>
        <option v-for="g in GRADES" :key="g" :value="g">{{ g }}</option>
      </select>
      <select v-model="query.sortBy" class="adm-ipt sel" @change="load()">
        <option value="default">按品阶排序</option>
        <option value="name">按名称排序</option>
      </select>
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
    </div>

    <div v-if="selected.length" class="adm-batch">
      <span>已选 {{ selected.length }} 部典籍</span>
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
      empty-text="没有找到典籍，试试调整筛选条件或新增一部"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
      @update:selected="selected = $event"
    >
      <template #col-glyph="{ row }"><span class="adm-glyph">{{ row.glyph || '✦' }}</span></template>
      <template #col-name="{ row }">
        <span class="adm-name" :title="row.name">{{ row.name }}<small>{{ row.tier || '—' }}</small></span>
      </template>
      <template #col-cat="{ row }">
        <span class="adm-tag" :style="catStyle(row.cat)">{{ row.cat }}</span>
      </template>
      <template #col-hot="{ row }">
        <button class="adm-pill" :class="row.hot ? 'on' : 'off'" type="button" @click="toggleHot(row)">
          {{ row.hot ? '热门' : '普通' }}
        </button>
      </template>
      <template #col-createdAt="{ row }"><span class="adm-muted">{{ row.createdAt || '—' }}</span></template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openEdit(row)">编辑</button>
        <button class="adm-op del" type="button" @click="askDelete(row)">删除</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="showForm"
      :title="editingId ? `编辑典籍 #${editingId}` : '新增典籍'"
      :fields="fields"
      :initial="form"
      :saving="saving"
      @submit="save"
      @cancel="showForm = false"
    />

    <AdminConfirm
      :open="!!deleting"
      title="删除典籍"
      :message="deleting ? `确定删除典籍「<b>${deleting.name}</b>」（#${deleting.id}）吗？<br><span class='warn'>删除后前台将不再展示，且不可恢复。</span>` : ''"
      confirm-text="确认删除"
      danger
      :loading="saving"
      @confirm="doDelete"
      @cancel="deleting = null"
    />

    <AdminConfirm
      :open="batchConfirm"
      title="批量删除"
      :message="`确定删除已勾选的 <b>${selected.length}</b> 部典籍吗？<br><span class='warn'>此操作不可恢复。</span>`"
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
import { gongfaApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const GRADES = ['绝世', '绝顶', '一流', '上乘', '中乘']
const PAGE_SIZE = 10
const CAT_COLORS = {
  功法: '#ffe08a', 宝术: '#8ce8b8', 至尊术: '#ff9d7a',
  剑诀: '#b8a6ff', 秘法: '#7fc9ff', 身法: '#a8e06a'
}

const { show } = useToast()
const {
  loading, page, pageCount, pageRows, total, selected, query,
  load, reset, fail, clearSelection
} = useCrudPage({ list: gongfaApi.list, pageSize: PAGE_SIZE, filters: ['keyword', 'cat', 'grade', 'sortBy'] })
query.sortBy = 'default'

const columns = [
  { key: 'glyph', label: '图标', width: '52px' },
  { key: 'name', label: '名称' },
  { key: 'cat', label: '分类' },
  { key: 'grade', label: '品阶', width: '72px' },
  { key: 'rank', label: '评分', width: '62px' },
  { key: 'from', label: '传承来源' },
  { key: 'owner', label: '修习者' },
  { key: 'hot', label: '热门', width: '74px' },
  { key: 'createdAt', label: '创建时间', width: '136px' }
]

const cats = ref([])
const fields = ref([])

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deleting = ref(null)
const batchConfirm = ref(false)
const form = reactive({})

onMounted(async () => {
  await loadCategories()
  buildFields()
  load()
})

async function loadCategories() {
  try {
    const res = await gongfaApi.categories()
    cats.value = (res.data || []).map(c => ({ code: c.code, name: c.name || c.code }))
  } catch (e) {
    fail(e, '分类字典加载失败')
  }
}

function buildFields() {
  fields.value = [
    { key: 'name', label: '功法名称', type: 'text', required: true, maxlength: 50, placeholder: '如 原始真解' },
    { key: 'cat', label: '分类', type: 'select', required: true, options: cats.value.map(c => ({ value: c.code, label: c.name })) },
    { key: 'grade', label: '品阶分级', type: 'select', required: true, options: GRADES.map(g => ({ value: g, label: g })) },
    { key: 'rank', label: '品阶评分', type: 'number', min: 0, max: 10, step: 0.1, hint: '0-10 之间，越大越靠前（与前台排序一致）' },
    { key: 'glyph', label: '卡面图标', type: 'text', maxlength: 4, placeholder: '如 ✦ ⚔ 🐉' },
    { key: 'tier', label: '品阶 / 流派简述', type: 'text', maxlength: 60, placeholder: '如 万法之基 · 神引篇' },
    { key: 'from', label: '传承来源', type: 'text', maxlength: 40, placeholder: '如 大荒藏书阁' },
    { key: 'owner', label: '修习者', type: 'text', maxlength: 40, placeholder: '如 石昊' },
    { key: 'desc', label: '功法简介', type: 'textarea', maxlength: 300, placeholder: '展示在首页卡片上的简介' },
    { key: 'hot', label: '标记为热门典籍（首页「热门典籍」展示）', type: 'checkbox' }
  ]
}

function openCreate() {
  editingId.value = null
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, { name: '', cat: '', grade: '', rank: 5, glyph: '', tier: '', from: '', owner: '', desc: '', hot: false })
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: row.name || '',
    cat: row.cat || '',
    grade: row.grade || '',
    rank: row.rank === null || row.rank === undefined ? 0 : Number(row.rank),
    glyph: row.glyph || '',
    tier: row.tier || '',
    from: row.from || '',
    owner: row.owner || '',
    desc: row.desc || '',
    hot: !!row.hot
  })
  showForm.value = true
}

async function save(data) {
  const payload = {
    name: (data.name || '').trim(),
    cat: data.cat,
    grade: data.grade,
    rank: data.rank === '' || data.rank === null ? 0 : Number(data.rank),
    glyph: (data.glyph || '').trim(),
    tier: (data.tier || '').trim(),
    from: (data.from || '').trim(),
    owner: (data.owner || '').trim(),
    desc: (data.desc || '').trim(),
    hot: data.hot ? 1 : 0
  }
  saving.value = true
  try {
    if (editingId.value) {
      await gongfaApi.update(editingId.value, payload)
      show('典籍已更新，前台刷新后生效', 'ok')
    } else {
      await gongfaApi.create(payload)
      show('典籍已新增，前台刷新后生效', 'ok')
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
    await gongfaApi.remove(deleting.value.id)
    show('典籍已删除', 'ok')
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
    const res = await gongfaApi.batchRemove(selected.value)
    show(`已删除 ${res.data ?? selected.value.length} 部典籍`, 'ok')
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
    const res = await gongfaApi.toggleHot(row.id)
    row.hot = res.data
    show(row.hot ? '已标记为热门' : '已取消热门', 'ok')
  } catch (e) {
    fail(e, '操作失败')
  }
}

function catStyle(cat) {
  const c = CAT_COLORS[cat] || '#ffe08a'
  return { color: c, borderColor: c + '66', background: c + '18' }
}
</script>
