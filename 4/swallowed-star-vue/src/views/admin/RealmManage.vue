<template>
  <div>
    <div class="adm-page-head">
      <h2>秘境管理</h2>
      <p>共 {{ total }} 处秘境 · 数据同步展示于前台「秘境首页」</p>
      <span class="grow"></span>
      <button class="adm-btn gold" type="button" @click="openCreate">+ 新增秘境</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 秘境名 / 等级 / 所在地 / 开启时间…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <select v-model="query.grade" class="adm-ipt sel" @change="load()">
        <option value="">全部等级</option>
        <option v-for="g in GRADES" :key="g" :value="g">{{ g }}</option>
      </select>
      <select v-model="query.sortBy" class="adm-ipt sel" @change="load()">
        <option value="default">按评级排序</option>
        <option value="name">按名称排序</option>
      </select>
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
    </div>

    <div v-if="selected.length" class="adm-batch">
      <span>已选 {{ selected.length }} 处秘境</span>
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
      empty-text="没有找到秘境，试试调整筛选条件或新增一处"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
      @update:selected="selected = $event"
    >
      <template #col-glyph="{ row }"><span class="adm-glyph">{{ row.glyph || '🌌' }}</span></template>
      <template #col-name="{ row }">
        <span class="adm-name" :title="row.name">{{ row.name }}<small>{{ row.location || '—' }}</small></span>
      </template>
      <template #col-grade="{ row }">
        <span class="adm-tag" :style="gradeStyle(row.grade)">{{ row.grade }}</span>
      </template>
      <template #col-openTime="{ row }">
        <span>{{ row.openTime || '—' }}<small class="adm-muted" style="display:block">时长 {{ row.duration || '—' }}</small></span>
      </template>
      <template #col-quota="{ row }">
        <span :style="{ color: quotaColor(row) }">{{ quotaText(row) }}</span>
      </template>
      <template #col-hot="{ row }">
        <button class="adm-pill" :class="row.hot ? 'on' : 'off'" type="button" @click="toggleHot(row)">
          {{ row.hot ? '热门' : '普通' }}
        </button>
      </template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openEdit(row)">编辑</button>
        <button class="adm-op" type="button" @click="openRecords(row)">报名记录</button>
        <button class="adm-op del" type="button" @click="askDelete(row)">删除</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="showForm"
      :title="editingId ? `编辑秘境 #${editingId}` : '新增秘境'"
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
      removable
      empty-text="当前还没有修士报名这处秘境"
      @close="records.open = false"
      @remove="removeSignup"
    />

    <AdminConfirm
      :open="!!deleting"
      title="删除秘境"
      :message="deleting ? `确定删除秘境「<b>${deleting.name}</b>」（#${deleting.id}）吗？<br><span class='warn'>该秘境下的报名记录会一并清除，且不可恢复。</span>` : ''"
      confirm-text="确认删除"
      danger
      :loading="saving"
      @confirm="doDelete"
      @cancel="deleting = null"
    />

    <AdminConfirm
      :open="batchConfirm"
      title="批量删除"
      :message="`确定删除已勾选的 <b>${selected.length}</b> 处秘境吗？<br><span class='warn'>对应报名记录会一并清除，此操作不可恢复。</span>`"
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
import { realmApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const GRADES = ['仙帝', '仙王', '真仙', '上古']
const PAGE_SIZE = 10
const GRADE_COLORS = { 仙帝: '#ffe08a', 仙王: '#8ce8b8', 真仙: '#7fc9ff', 上古: '#b3a684' }

const { show } = useToast()
const {
  loading, page, pageCount, pageRows, total, selected, query,
  load, reset, fail, clearSelection
} = useCrudPage({ list: realmApi.list, pageSize: PAGE_SIZE, filters: ['keyword', 'grade', 'sortBy'] })
query.sortBy = 'default'

const columns = [
  { key: 'glyph', label: '图标', width: '52px' },
  { key: 'name', label: '秘境' },
  { key: 'grade', label: '等级', width: '74px' },
  { key: 'openTime', label: '开启时间' },
  { key: 'quota', label: '名额', width: '110px' },
  { key: 'rank', label: '评级', width: '58px' },
  { key: 'hot', label: '热门', width: '74px' },
  { key: 'createdAt', label: '创建时间', width: '136px' }
]

const recordColumns = [
  { key: 'username', label: '报名账号' },
  { key: 'signedAt', label: '报名时间' }
]

const fields = [
  { key: 'name', label: '秘境名称', type: 'text', required: true, maxlength: 50, placeholder: '如 虚神界' },
  { key: 'grade', label: '秘境等级', type: 'select', required: true, options: GRADES.map(g => ({ value: g, label: g })) },
  { key: 'glyph', label: '卡面图标', type: 'text', maxlength: 4, placeholder: '如 🌌 ⚰' },
  { key: 'rank', label: '评级评分', type: 'number', min: 0, max: 10, step: 0.1, hint: '0-10 之间，越大越靠前' },
  { key: 'location', label: '所在地', type: 'text', maxlength: 60, placeholder: '如 九天之上 · 天幕裂隙' },
  { key: 'quota', label: '每期名额', type: 'number', min: 0, step: 1, hint: '填 0 表示不限名额' },
  { key: 'openTime', label: '开启时间', type: 'text', maxlength: 100, placeholder: '如 每逢甲子 · 十月圆满之夜' },
  { key: 'duration', label: '开启时长', type: 'text', maxlength: 20, placeholder: '如 三十日' },
  { key: 'desc', label: '秘境介绍', type: 'textarea', maxlength: 600, placeholder: '展示在秘境卡片上的介绍' },
  { key: 'hot', label: '标记为热门秘境', type: 'checkbox' }
]

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deleting = ref(null)
const batchConfirm = ref(false)
const form = reactive({})
const records = reactive({ open: false, id: null, title: '', rows: [], loading: false })

onMounted(load)

function quotaText(row) {
  const signed = row.signedCount || 0
  const quota = row.quota || 0
  return quota > 0 ? `${signed} / ${quota}` : `${signed} / 不限`
}

function quotaColor(row) {
  const quota = row.quota || 0
  const signed = row.signedCount || 0
  if (quota > 0 && signed >= quota) return 'var(--danger)'
  return 'var(--text)'
}

function openCreate() {
  editingId.value = null
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: '', grade: '', glyph: '', rank: 5, location: '', quota: 0,
    openTime: '', duration: '', desc: '', hot: false
  })
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: row.name || '',
    grade: row.grade || '',
    glyph: row.glyph || '',
    rank: row.rank === null || row.rank === undefined ? 0 : Number(row.rank),
    location: row.location || '',
    quota: row.quota === null || row.quota === undefined ? 0 : Number(row.quota),
    openTime: row.openTime || '',
    duration: row.duration || '',
    desc: row.desc || '',
    hot: !!row.hot
  })
  showForm.value = true
}

async function save(data) {
  const payload = {
    name: (data.name || '').trim(),
    grade: data.grade,
    glyph: (data.glyph || '').trim(),
    rank: data.rank === '' || data.rank === null ? 0 : Number(data.rank),
    location: (data.location || '').trim(),
    quota: data.quota === '' || data.quota === null ? 0 : Number(data.quota),
    openTime: (data.openTime || '').trim(),
    duration: (data.duration || '').trim(),
    desc: (data.desc || '').trim(),
    hot: data.hot ? 1 : 0
  }
  saving.value = true
  try {
    if (editingId.value) {
      await realmApi.update(editingId.value, payload)
      show('秘境已更新', 'ok')
    } else {
      await realmApi.create(payload)
      show('秘境已新增', 'ok')
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
    await realmApi.remove(deleting.value.id)
    show('秘境已删除', 'ok')
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
    const res = await realmApi.batchRemove(selected.value)
    show(`已删除 ${res.data ?? selected.value.length} 处秘境`, 'ok')
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
    const res = await realmApi.toggleHot(row.id)
    row.hot = res.data
    show(row.hot ? '已标记为热门' : '已取消热门', 'ok')
  } catch (e) {
    fail(e, '操作失败')
  }
}

async function openRecords(row) {
  records.open = true
  records.id = row.id
  records.title = `「${row.name}」报名记录`
  records.rows = []
  records.loading = true
  try {
    const res = await realmApi.signups(row.id)
    records.rows = res.data || []
  } catch (e) {
    fail(e, '报名记录加载失败')
  } finally {
    records.loading = false
  }
}

async function removeSignup(row) {
  try {
    await realmApi.removeSignup(row.id)
    show('已撤销该报名记录', 'ok')
    records.rows = records.rows.filter(r => r.id !== row.id)
    load(true)
  } catch (e) {
    fail(e, '撤销失败')
  }
}

function gradeStyle(grade) {
  const c = GRADE_COLORS[grade] || '#ffe08a'
  return { color: c, borderColor: c + '66', background: c + '18' }
}
</script>
