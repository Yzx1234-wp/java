<template>
  <div>
    <div class="adm-page-head">
      <h2>兑换商店</h2>
      <p>共 {{ total }} 件商品 · 全站已兑换 {{ orderTotal }} 件 · 数据同步展示于前台「商店兑换」</p>
      <span class="grow"></span>
      <button class="adm-btn" type="button" @click="openOrders">兑换记录</button>
      <button class="adm-btn gold" type="button" @click="openCreate">+ 新增商品</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 商品名 / 分类 / 品阶 / 简介…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <select v-model="query.category" class="adm-ipt sel" @change="load()">
        <option value="">全部分类</option>
        <option v-for="c in CATEGORIES" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="query.sortBy" class="adm-ipt sel" @change="load()">
        <option value="default">默认排序</option>
        <option value="priceAsc">积分由低到高</option>
        <option value="priceDesc">积分由高到低</option>
      </select>
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
    </div>

    <div v-if="selected.length" class="adm-batch">
      <span>已选 {{ selected.length }} 件商品</span>
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
      empty-text="没有找到商品，试试调整筛选条件或新增一件"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
      @update:selected="selected = $event"
    >
      <template #col-glyph="{ row }"><span class="adm-glyph">{{ row.glyph || '✦' }}</span></template>
      <template #col-name="{ row }">
        <span class="adm-name" :title="row.name">{{ row.name }}<small>{{ row.grade || '—' }}</small></span>
      </template>
      <template #col-category="{ row }">
        <span class="adm-tag" :style="catStyle(row.category)">{{ row.category }}</span>
      </template>
      <template #col-price="{ row }"><b style="color:var(--gold)">{{ row.price ?? 0 }}</b></template>
      <template #col-stockInfo="{ row }">
        <span>
          {{ row.sold || 0 }} 件已兑
          <small class="adm-muted" style="display:block">
            {{ !row.stock || row.stock <= 0 ? '不限量' : `剩 ${Math.max(0, row.stock - (row.sold || 0))} 件` }}
          </small>
        </span>
      </template>
      <template #col-hot="{ row }">
        <button class="adm-pill" :class="row.hot ? 'on' : 'off'" type="button" @click="toggleHot(row)">
          {{ row.hot ? '热门' : '普通' }}
        </button>
      </template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openEdit(row)">编辑</button>
        <button class="adm-op del" type="button" @click="askDelete(row)">删除</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="showForm"
      :title="editingId ? `编辑商品 #${editingId}` : '新增商品'"
      :fields="fields"
      :initial="form"
      :saving="saving"
      @submit="save"
      @cancel="showForm = false"
    />

    <AdminRecordModal
      :open="orders.open"
      title="兑换记录（全站最近 200 条）"
      :columns="orderColumns"
      :rows="orders.rows"
      :loading="orders.loading"
      :busy-id="orders.busyId"
      removable
      hint="兑换记录同时是<b>积分消耗账本</b>：删除记录会收回该笔消耗，修士可用积分相应回升，商品已售件数也会同步减一。"
      empty-text="还没有修士兑换过商品"
      @close="orders.open = false"
      @remove="removeOrder"
    />

    <AdminConfirm
      :open="!!deleting"
      title="删除商品"
      :message="deleting ? `确定删除商品「<b>${deleting.name}</b>」（#${deleting.id}）吗？<br><span class='warn'>历史兑换记录会保留（积分账本不受影响），此操作不可恢复。</span>` : ''"
      confirm-text="确认删除"
      danger
      :loading="saving"
      @confirm="doDelete"
      @cancel="deleting = null"
    />

    <AdminConfirm
      :open="batchConfirm"
      title="批量删除"
      :message="`确定删除已勾选的 <b>${selected.length}</b> 件商品吗？<br><span class='warn'>历史兑换记录会保留（积分账本不受影响），此操作不可恢复。</span>`"
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
import { shopApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const CATEGORIES = ['丹药', '法宝', '武器', '高级功法']
const GRADES = ['绝世', '绝顶', '一流', '上乘', '中乘']
const PAGE_SIZE = 10
const CAT_COLORS = { 丹药: '#8ce8b8', 法宝: '#c9a3ff', 武器: '#ff9d7a', 高级功法: '#ffe08a' }

const { show } = useToast()
const {
  loading, page, pageCount, pageRows, total, selected, query,
  load, reset, fail, clearSelection
} = useCrudPage({ list: shopApi.list, pageSize: PAGE_SIZE, filters: ['keyword', 'category', 'sortBy'] })
query.sortBy = 'default'

/* 全站兑换件数（跟列表一起刷新） */
const orderTotal = ref(0)

const columns = [
  { key: 'glyph', label: '图标', width: '52px' },
  { key: 'name', label: '商品' },
  { key: 'category', label: '分类', width: '86px' },
  { key: 'price', label: '兑换积分', width: '86px' },
  { key: 'stockInfo', label: '已兑 / 库存', width: '100px' },
  { key: 'hot', label: '热门', width: '74px' },
  { key: 'createdAt', label: '创建时间', width: '136px' }
]

const orderColumns = [
  { key: 'username', label: '兑换账号' },
  { key: 'itemName', label: '商品' },
  { key: 'category', label: '分类', width: '86px' },
  { key: 'price', label: '消耗积分', type: 'points', format: (r) => '-' + (r.price || 0) },
  { key: 'createdAt', label: '兑换时间' }
]

const fields = [
  { key: 'name', label: '商品名称', type: 'text', required: true, maxlength: 50, placeholder: '如 回魂丹' },
  { key: 'category', label: '分类', type: 'select', required: true, options: CATEGORIES.map(c => ({ value: c, label: c })) },
  { key: 'grade', label: '品阶', type: 'select', required: true, options: GRADES.map(g => ({ value: g, label: g })) },
  { key: 'price', label: '兑换积分', type: 'number', min: 0, max: 100000, step: 10, hint: '修士用任务 / 秘境获得的积分兑换' },
  { key: 'stock', label: '库存件数', type: 'number', min: 0, max: 1000000, hint: '填 0 表示不限量供应' },
  { key: 'glyph', label: '卡面图标', type: 'text', maxlength: 4, placeholder: '如 💊 ⚔ 🗡' },
  { key: 'sortNo', label: '排序号', type: 'number', min: 0, max: 9999, hint: '越小越靠前（默认排序时生效）' },
  { key: 'hot', label: '标记为热门商品', type: 'checkbox' },
  { key: 'desc', label: '商品简介', type: 'textarea', maxlength: 300, placeholder: '展示在商品卡片上的简介' }
]

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const deleting = ref(null)
const batchConfirm = ref(false)
const form = reactive({})
const orders = reactive({ open: false, rows: [], loading: false, busyId: null })

onMounted(load)

function openCreate() {
  editingId.value = null
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, { name: '', category: '', grade: '', price: 100, stock: 0, glyph: '', sortNo: 0, desc: '', hot: false })
  showForm.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, {
    name: row.name || '',
    category: row.category || '',
    grade: row.grade || '',
    price: row.price === null || row.price === undefined ? 0 : Number(row.price),
    stock: row.stock === null || row.stock === undefined ? 0 : Number(row.stock),
    glyph: row.glyph || '',
    sortNo: row.sortNo === null || row.sortNo === undefined ? 0 : Number(row.sortNo),
    desc: row.desc || '',
    hot: !!row.hot
  })
  showForm.value = true
}

async function save(data) {
  const payload = {
    name: (data.name || '').trim(),
    category: data.category,
    grade: data.grade,
    price: data.price === '' || data.price === null ? 0 : Number(data.price),
    stock: data.stock === '' || data.stock === null ? 0 : Number(data.stock),
    glyph: (data.glyph || '').trim(),
    sortNo: data.sortNo === '' || data.sortNo === null ? 0 : Number(data.sortNo),
    desc: (data.desc || '').trim(),
    hot: data.hot ? 1 : 0
  }
  saving.value = true
  try {
    if (editingId.value) {
      await shopApi.update(editingId.value, payload)
      show('商品已更新', 'ok')
    } else {
      await shopApi.create(payload)
      show('商品已新增', 'ok')
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
    await shopApi.remove(deleting.value.id)
    show('商品已删除', 'ok')
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
    const res = await shopApi.batchRemove(selected.value)
    show(`已删除 ${res.data ?? selected.value.length} 件商品`, 'ok')
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
    const res = await shopApi.toggleHot(row.id)
    row.hot = res.data
    show(row.hot ? '已标记为热门' : '已取消热门', 'ok')
  } catch (e) {
    fail(e, '操作失败')
  }
}

/* ===== 兑换记录 ===== */

async function openOrders() {
  orders.open = true
  orders.rows = []
  await loadOrders()
}

async function loadOrders() {
  orders.loading = true
  try {
    const res = await shopApi.orders()
    orders.rows = res.data || []
    orderTotal.value = orders.rows.length
  } catch (e) {
    fail(e, '兑换记录加载失败')
  } finally {
    orders.loading = false
  }
}

/* 删除兑换记录：收回该笔消耗，修士积分回升、商品已售件数同步减一 */
async function removeOrder(row) {
  orders.busyId = row.id
  try {
    const res = await shopApi.removeOrder(row.id)
    show(`已删除该记录，收回 ${row.username} 的 ${res.data ?? row.price ?? 0} 积分消耗`, 'ok')
    await loadOrders()
    load(true)
  } catch (e) {
    fail(e, '删除失败')
  } finally {
    orders.busyId = null
  }
}

function catStyle(cat) {
  const c = CAT_COLORS[cat] || '#ffe08a'
  return { color: c, borderColor: c + '66', background: c + '18' }
}
</script>
