<template>
  <div>
    <div class="adm-page-head">
      <h2>修炼境界</h2>
      <p>共 {{ total }} 位修士 · 已入门 {{ profiledCount }} 位 · 可调整境界（演示纠错）或重置修炼档案</p>
      <span class="grow"></span>
      <button class="adm-btn" type="button" :disabled="loading" @click="load()">刷新列表</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 账号 / 昵称…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
      <span class="grow"></span>
      <span class="adm-muted">境界不由积分决定 · 调整会写入该修士的修炼记录</span>
    </div>

    <AdminTable
      v-model:page="page"
      :columns="columns"
      :rows="pageRows"
      :loading="loading"
      empty-text="没有找到修士"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
    >
      <template #col-username="{ row }">
        <span class="adm-name" :title="row.username">{{ row.username }}</span>
      </template>
      <template #col-realm="{ row }">
        <span class="adm-tag" :style="realmTagStyle(row)">{{ row.profiled ? row.realmName + '境' : '未入门' }}</span>
      </template>
      <template #col-exp="{ row }">
        <span v-if="row.profiled"><b style="color:var(--gold)">{{ row.exp }}</b> / {{ row.need || '—' }}</span>
        <span v-else class="adm-muted">—</span>
      </template>
      <template #col-comprehendCount="{ row }">
        <span>{{ row.comprehendCount }}<i class="dim">/{{ comprehendTotal }}</i></span>
      </template>
      <template #col-totalExp="{ row }">
        <span v-if="row.profiled">{{ row.totalExp }}</span>
        <span v-else class="adm-muted">—</span>
      </template>
      <template #col-updatedAt="{ row }"><span class="adm-muted">{{ row.updatedAt || '—' }}</span></template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openAdjust(row)">调整境界</button>
        <button
          class="adm-op del"
          type="button"
          :disabled="!row.profiled"
          :title="row.profiled ? '' : '尚未入门，无需重置'"
          @click="askReset(row)"
        >重置修炼</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="adjust.open"
      :title="`调整境界 · ${adjust.username}`"
      :fields="adjustFields"
      :initial="adjust.form"
      :saving="saving"
      @submit="doAdjust"
      @cancel="adjust.open = false"
    />

    <AdminConfirm
      :open="!!resetting"
      title="重置修炼档案"
      :message="resetting ? `确定重置修士「<b>${resetting.username}</b>」的修炼档案吗？<br><span class='warn'>其境界回到「未入门」，${resetting.comprehendCount} 条参悟记录与全部修炼记录将被清除，且不可恢复（积分不受影响）。</span>` : ''"
      confirm-text="确认重置"
      danger
      :loading="saving"
      @confirm="doReset"
      @cancel="resetting = null"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AdminTable from './components/AdminTable.vue'
import AdminFormModal from './components/AdminFormModal.vue'
import AdminConfirm from './components/AdminConfirm.vue'
import { useCrudPage } from './composables/useCrudPage.js'
import { cultivationApi, adminOverviewApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const PAGE_SIZE = 10

// 十八境阶梯（与后端 CultivationLadder 一致）
const LADDER = [
  '搬血', '洞天', '化灵', '铭纹', '列阵', '尊者', '神火', '真一', '圣祭',
  '天神', '虚道', '斩我', '遁一', '至尊', '真仙', '仙王', '准仙帝', '仙帝'
]

const { show } = useToast()
const {
  rows, loading, page, pageCount, pageRows, total, query,
  load, reset, fail
} = useCrudPage({ list: cultivationApi.list, pageSize: PAGE_SIZE, filters: ['keyword'] })

const columns = [
  { key: 'username', label: '账号' },
  { key: 'nickname', label: '昵称' },
  { key: 'realm', label: '当前境界', width: '96px' },
  { key: 'exp', label: '本境修为', width: '104px' },
  { key: 'comprehendCount', label: '参悟典籍', width: '88px' },
  { key: 'breakthroughCount', label: '破境', width: '66px' },
  { key: 'failCount', label: '渡劫失败', width: '88px' },
  { key: 'totalExp', label: '累计修为', width: '92px' },
  { key: 'updatedAt', label: '最近变动', width: '150px' }
]

const comprehendTotal = ref(29)
const saving = ref(false)
const resetting = ref(null)

const profiledCount = computed(() => rows.value.filter(r => r.profiled).length)

const adjustFields = [
  {
    key: 'realmIndex',
    label: '境界',
    type: 'select',
    required: true,
    placeholder: '请选择境界',
    options: LADDER.map((name, i) => ({ value: i, label: `${name}境（第 ${i + 1} 境）` }))
  },
  {
    key: 'exp',
    label: '本境修为',
    type: 'number',
    required: true,
    min: 0,
    max: 999999,
    placeholder: '0 - 999999',
    hint: '调整会写入该修士的修炼记录「天道法则变动」'
  }
]

const adjust = reactive({ open: false, username: '', form: {} })

onMounted(async () => {
  load()
  // 参悟分母取功法典籍总数（后台概览里有现成计数）
  try {
    const res = await adminOverviewApi()
    comprehendTotal.value = (res.data && res.data.gongfaCount) || 29
  } catch { /* 概览失败不致命 */ }
})

function openAdjust(row) {
  adjust.open = true
  adjust.username = row.username
  adjust.form = { realmIndex: row.profiled ? row.realmIndex : 0, exp: row.profiled ? row.exp : 0 }
}

function realmTagStyle(row) {
  if (!row.profiled) {
    return { color: '#b3a684', borderColor: 'rgba(179,166,132,.4)', background: 'rgba(179,166,132,.1)' }
  }
  if (row.realmIndex >= 14) {
    return { color: '#ffe08a', borderColor: 'rgba(255,224,138,.45)', background: 'rgba(255,224,138,.12)' }
  }
  return { color: '#8ce8b8', borderColor: 'rgba(140,232,184,.35)', background: 'rgba(140,232,184,.1)' }
}

async function doAdjust(data) {
  const realmIndex = Number(data.realmIndex)
  const exp = Number(data.exp)
  if (Number.isNaN(realmIndex) || realmIndex < 0 || realmIndex >= LADDER.length) {
    show('请选择有效境界', 'err')
    return
  }
  if (!Number.isInteger(exp) || exp < 0 || exp > 999999) {
    show('修为需为 0-999999 的整数', 'err')
    return
  }
  saving.value = true
  try {
    await cultivationApi.adjust(adjust.username, realmIndex, exp)
    show(`已调整「${adjust.username}」为 ${LADDER[realmIndex]}境 · 修为 ${exp}`, 'ok')
    adjust.open = false
    load(true)
  } catch (e) {
    fail(e, '调整失败')
  } finally {
    saving.value = false
  }
}

function askReset(row) {
  if (!row.profiled) {
    show('该修士尚未入门，无需重置', 'err')
    return
  }
  resetting.value = row
}

async function doReset() {
  if (!resetting.value) return
  saving.value = true
  try {
    await cultivationApi.reset(resetting.value.username)
    show(`「${resetting.value.username}」修炼档案已重置`, 'ok')
    resetting.value = null
    load(true)
  } catch (e) {
    fail(e, '重置失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.dim{font-style:normal;font-size:11px;opacity:.55;margin-left:2px}
</style>
