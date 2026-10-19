<template>
  <!-- ===== 顶部导航栏 ===== -->
  <nav class="topnav">
    <a class="logo" href="#" @click.prevent="scrollTop">
      <span class="mark">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="7" r="3.2"/><path d="M12 2v1.4M7.4 4.4l1 1M16.6 4.4l-1 1M5 20h14M6.5 20c0-4 2.4-7 5.5-7s5.5 3 5.5 7"/><path d="M12 17v3"/>
        </svg>
      </span>
      <b>完美世界</b>
    </a>

    <div class="nav-menu">
      <button
        v-for="n in navs"
        :key="n.key"
        class="nav-item"
        :class="{ active: activeNav === n.key }"
        @click="onNav(n)"
      >{{ n.label }}</button>
    </div>

    <span class="spacer"></span>
    <span class="clock">{{ clock }}</span>

    <div class="nav-user">
      <span class="avatar">{{ avatarChar }}</span>
      <span class="name">{{ nickname }}</span>
      <button class="btn-exit" @click="goLogout">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>
        </svg>
        退出
      </button>
    </div>
  </nav>

  <!-- ===== 首页主体（可滚动） ===== -->
  <main class="home-shell">
    <!-- 首屏：欢迎 + 搜索栏 -->
    <section class="home-hero">
      <h2>欢迎归来，{{ nickname }}</h2>
      <p class="sub">大荒功法库 · 收录 <b>真解</b>、<b>十凶宝术</b>、<b>至尊骨术</b> 等典籍 · 检索参悟，续你的道途</p>

      <div class="search-wrap">
        <span class="ico">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
        </span>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索 功法 / 宝术 / 剑诀 / 传承 / 修习者 …"
          maxlength="40"
          @keyup.enter="doSearch"
        >
        <button class="btn-search" @click="doSearch">检 索</button>
      </div>

      <div class="hot-row">
        <span class="tag">热门典籍</span>
        <button v-for="w in hotWords" :key="w" class="hot-chip" @click="pickHot(w)">{{ w }}</button>
      </div>
    </section>

    <!-- 功法库内容 -->
    <section class="result-zone">
      <!-- 统计条 -->
      <div class="art-stats">
        <div class="stat-card">
          <b>{{ total }}</b>
          <span>收录功法典籍</span>
        </div>
        <div class="stat-card">
          <b>{{ catCount('功法') }}</b>
          <span>修行根本功法</span>
        </div>
        <div class="stat-card jade">
          <b>{{ catCount('宝术') }}</b>
          <span>十凶 / 雷道宝术</span>
        </div>
        <div class="stat-card">
          <b>{{ catCount('至尊术') }}</b>
          <span>至尊骨术</span>
        </div>
      </div>

      <!-- 一级分类筛选 -->
      <div class="filter-row">
        <span class="label">类别</span>
        <button
          v-for="c in categories"
          :key="c.key"
          class="filter-chip"
          :class="{ active: activeCat === c.key }"
          @click="activeCat = c.key"
        >{{ c.label }}<span class="cnt">{{ c.count }}</span></button>

        <span class="filter-spacer"></span>
        <span class="label">排序</span>
        <button
          v-for="s in sorts"
          :key="s.key"
          class="filter-chip"
          :class="{ active: sortBy === s.key }"
          @click="sortBy = s.key"
        >{{ s.label }}</button>
      </div>

      <!-- 品阶分级筛选 -->
      <div class="filter-row">
        <span class="label">品阶</span>
        <button
          v-for="g in grades"
          :key="g.key"
          class="filter-chip"
          :class="{ active: activeGrade === g.key }"
          @click="activeGrade = g.key"
        >{{ g.label }}<span class="cnt">{{ g.count }}</span></button>
      </div>

      <!-- 列表头 -->
      <div class="result-head">
        <h3 v-if="searched">检索「<b>{{ searched }}</b>」 · 共 <b>{{ list.length }}</b> 部</h3>
        <h3 v-else>功法典籍总览 · 共 <b>{{ list.length }}</b> 部</h3>
        <span>{{ loading ? '正在检索功法库…' : '数据来源 · 后端功法库' }}</span>
      </div>

      <!-- 功法卡片 -->
      <div v-if="list.length" class="card-grid">
        <article
          v-for="(item, i) in list"
          :key="item.id || item.name"
          class="art-card"
          :style="{ animationDelay: (i * 0.04) + 's' }"
          @click="openCard(item)"
        >
          <span class="cat-badge" :style="badgeStyle(item)">{{ item.cat }}</span>

          <div class="art-top">
            <div class="glyph" :style="glyphStyle(item)">{{ item.glyph }}</div>
            <div class="art-title">
              <h4>{{ item.name }}</h4>
              <span class="tier">{{ item.tier }}</span>
            </div>
          </div>

          <p class="desc">{{ item.desc }}</p>

          <div class="art-meta">
            <span>传承 · <b>{{ item.from }}</b></span>
            <span>修习 · <b>{{ item.owner }}</b></span>
          </div>

          <div class="go">查看典籍 →</div>
        </article>
      </div>

      <div v-else-if="loading" class="empty-box">
        <span class="big">✧</span>
        正在从功法库中检索…
      </div>

      <div v-else-if="loadError" class="empty-box">
        <span class="big">⚠</span>
        {{ loadError }}
        <div class="retry-row">
          <button class="filter-chip active" @click="reload">重新加载</button>
        </div>
      </div>

      <div v-else class="empty-box">
        <span class="big">❋</span>
        未检索到「{{ searched }}」相关的功法典籍，试试「原始真解」「鲲鹏宝术」等关键词
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { homeApi, listApi } from '../api/gongfa.js'

const router = useRouter()
const { show } = useToast()

/* ===== 用户会话（沿用原有逻辑：token 失效自动退回登录页） ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const clock = ref('')
let timer = null

/* ===== 导航栏（其他页面暂不开发，点击提示建设中） ===== */
const navs = [
  { key: 'home', label: '功法首页' },
  { key: 'clan', label: '十凶宝术' },
  { key: 'sword', label: '剑诀秘藏' },
  { key: 'bone', label: '至尊骨术' },
  { key: 'train', label: '修行问道' }
]
const activeNav = ref('home')

function onNav(item) {
  if (item.key !== 'home') {
    show(`「${item.label}」模块建设中…`, 'ok')
    return
  }
  scrollTop()
}

function scrollTop() {
  const el = document.querySelector('.home-shell')
  if (el) el.scrollTo({ top: 0, behavior: 'smooth' })
}

/* ===== 分类配色（与后端 cat 字段对应） ===== */
const CAT_COLORS = {
  功法: '#ffe08a',
  宝术: '#ff9d7a',
  至尊术: '#c9a3ff',
  剑诀: '#9fd8ff',
  秘法: '#8ce8b8',
  身法: '#7ee08a'
}

/* ===== 排序方式 ===== */
const sorts = [
  { key: 'default', label: '默认' },
  { key: 'rank', label: '按品阶' },
  { key: 'name', label: '按名称' }
]

/* ===== 后端数据状态 ===== */
const ALL = '全部'
const loading = ref(true)
const loadError = ref('')
const total = ref(0)
const byCat = ref({})
const categories = ref([{ key: ALL, label: ALL, count: 0 }])
const grades = ref([{ key: ALL, label: ALL, count: 0 }])
const hotWords = ref([])
const list = ref([])

/* 检索条件 */
const keyword = ref('')
const searched = ref('')
const activeCat = ref(ALL)
const activeGrade = ref(ALL)
const sortBy = ref('default')

/* 首页加载完成后，筛选/排序变化才触发重新检索（避免首次双请求） */
let booted = false

function catCount(cat) {
  return byCat.value[cat] || 0
}

/* 首页聚合数据（统计 / 筛选项 / 热词 / 默认列表） */
async function loadHome() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await homeApi()
    const d = res.data || {}
    total.value = d.total || 0
    byCat.value = d.byCat || {}
    categories.value = (d.categories && d.categories.length) ? d.categories : [{ key: ALL, label: ALL, count: d.total || 0 }]
    grades.value = (d.grades && d.grades.length) ? d.grades : [{ key: ALL, label: ALL, count: d.total || 0 }]
    hotWords.value = d.hotWords || []
    list.value = d.list || []
  } catch (e) {
    loadError.value = e.message || '功法库数据加载失败，请确认后端服务已启动'
    list.value = []
  } finally {
    loading.value = false
  }
}

/* 检索（关键词 + 分类 + 品阶 + 排序 全部由后端完成） */
async function loadList() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listApi({
      keyword: searched.value,
      cat: activeCat.value === ALL ? undefined : activeCat.value,
      grade: activeGrade.value === ALL ? undefined : activeGrade.value,
      sortBy: sortBy.value
    })
    list.value = res.data || []
  } catch (e) {
    loadError.value = e.message || '检索失败，请确认后端服务已启动'
    list.value = []
  } finally {
    loading.value = false
  }
}

function reload() {
  loadHome()
}

function doSearch() {
  const k = keyword.value.trim()
  if (!k) {
    show('请输入关键词，当前展示全部功法典籍', 'ok')
    if (searched.value === '') return
    searched.value = ''
    return
  }
  if (k === searched.value) {
    loadList()
    return
  }
  searched.value = k
}

function pickHot(word) {
  keyword.value = word
  if (searched.value === word) {
    loadList()
  } else {
    searched.value = word
  }
  scrollToResult()
}

function scrollToResult() {
  const el = document.querySelector('.result-zone')
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function openCard(item) {
  show(`「${item.name}」典籍详情建设中`, 'ok')
}

function badgeStyle(item) {
  const c = CAT_COLORS[item.cat] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}14` }
}

function glyphStyle(item) {
  const c = CAT_COLORS[item.cat] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 16px ${c}22` }
}

/* 筛选 / 排序变化 → 重新向后台检索 */
watch([searched, activeCat, activeGrade, sortBy], () => {
  if (booted) loadList()
})

/* ===== 时钟 + 登录态校验 ===== */
function tick() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  clock.value = `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

onMounted(async () => {
  tick()
  timer = setInterval(tick, 1000)

  // 用 token 向后端校验登录态，失效则退回登录页（后端接口保持原样）
  const me = await fetchMe()
  if (!me) {
    logout()
    router.push('/login')
    return
  }
  nickname.value = me.nickname || me.username || nickname.value
  avatarChar.value = (nickname.value || '荒')[0]

  // 加载首页数据
  await loadHome()
  booted = true
})
onBeforeUnmount(() => clearInterval(timer))

const goLogout = () => { logout(); router.push('/login') }
</script>
