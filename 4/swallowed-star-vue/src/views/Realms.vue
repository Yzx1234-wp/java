<template>
  <!-- ===== 顶部导航栏（复用全局 topnav 样式） ===== -->
  <nav class="topnav">
    <a class="logo" href="#" @click.prevent="goHome">
      <span class="mark">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="7" r="3.2"/><path d="M12 2v1.4M7.4 4.4l1 1M16.6 4.4l-1 1M5 20h14M6.5 20c0-4 2.4-7 5.5-7s5.5 3 5.5 7"/><path d="M12 17v3"/>
        </svg>
      </span>
      <b>完美世界</b>
    </a>

    <div class="nav-menu">
      <button class="nav-item" @click="goHome">功法首页</button>
      <button class="nav-item" @click="goCharacters">人物介绍</button>
      <button class="nav-item active">秘境首页</button>
      <button class="nav-item" @click="goQuests">任务接取</button>
      <button class="nav-item" @click="goProfile">我的主页</button>
    </div>

    <span class="spacer"></span>
    <span class="clock">{{ clock }}</span>

    <div class="nav-user">
      <span class="avatar">{{ avatarChar }}</span>
      <span class="name">{{ nickname }}</span>
      <span class="points-chip" title="查看我的主页" @click="router.push('/profile')">✦ {{ myPoints }}</span>
      <button class="btn-exit" @click="goLogout">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>
        </svg>
        退出
      </button>
    </div>
  </nav>

  <!-- ===== 秘境主体（可滚动） ===== -->
  <main class="realm-shell">
    <!-- 首屏：秘境录题头 + 搜索栏 -->
    <section class="realm-hero">
      <div class="hero-deco">大 荒 秘 境 录</div>
      <h2>大荒秘境录</h2>
      <p class="sub">仙古遗迹 · 十凶凶地 · 帝路机缘，凡开启有时者皆录于此 · 共 <b>{{ total }}</b> 处秘境</p>

      <div class="search-wrap">
        <span class="ico">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
        </span>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索 秘境 / 所在地 / 开启时间 / 介绍 …"
          maxlength="40"
          @keyup.enter="doSearch"
        >
        <button class="btn-search" @click="doSearch">探 寻</button>
      </div>

      <div class="hot-row">
        <span class="tag">热门秘境</span>
        <button v-for="w in hotWords" :key="w" class="hot-chip" @click="pickHot(w)">{{ w }}</button>
      </div>
    </section>

    <!-- 统计条 -->
    <section class="stat-zone">
      <div class="stat-card">
        <b>{{ total }}</b>
        <span>收录秘境</span>
      </div>
      <div class="stat-card jade">
        <b>{{ totalSigned }}</b>
        <span>累计报名人次</span>
      </div>
      <div class="stat-card">
        <b>{{ myCount }}</b>
        <span>我的报名</span>
      </div>
      <div class="stat-card">
        <b>{{ openNowCount }}</b>
        <span>已报满待下期</span>
      </div>
    </section>

    <!-- 筛选区 -->
    <section class="filter-zone">
      <div class="filter-row">
        <span class="label">等级</span>
        <button
          v-for="g in grades"
          :key="g.key"
          class="filter-chip"
          :class="{ active: activeGrade === g.key }"
          @click="activeGrade = g.key"
        >{{ g.label }}<span class="cnt">{{ g.count }}</span></button>

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
    </section>

    <!-- 列表头 -->
    <div class="result-head">
      <h3 v-if="searched">探寻「<b>{{ searched }}</b>」 · 得 <b>{{ list.length }}</b> 处秘境</h3>
      <h3 v-else>秘境总览 · 共 <b>{{ list.length }}</b> 处</h3>
      <span>{{ loading ? '正在探寻秘境之门…' : '数据来源 · 后端秘境录' }}</span>
    </div>

    <!-- 秘境卡片 -->
    <div v-if="list.length" class="realm-grid">
      <article
        v-for="(item, i) in list"
        :key="item.id || item.name"
        class="realm-card"
        :style="{ animationDelay: (i * 0.05) + 's' }"
        @click="openCard(item)"
      >
        <span class="grade-badge" :style="badgeStyle(item)">{{ item.grade }}级</span>

        <div class="realm-top">
          <div class="glyph" :style="glyphStyle(item)">{{ item.glyph }}</div>
          <div class="realm-title">
            <h4>{{ item.name }}</h4>
            <span class="loc">📍 {{ item.location }}</span>
          </div>
        </div>

        <div class="open-time" :style="openStyle(item)">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
            <circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/>
          </svg>
          开启 · {{ item.openTime }}
        </div>

        <p class="desc">{{ item.desc }}</p>

        <div class="realm-meta">
          <span>时长 · <b>{{ item.duration || '不限' }}</b></span>
          <span>名额 · <b>{{ quotaText(item) }}</b></span>
        </div>

        <div class="quota-row">
          <div class="quota-bar">
            <span :style="barStyle(item)"></span>
          </div>
          <span class="quota-num">{{ item.signedCount || 0 }}<i>/</i>{{ quotaText(item) }} 已入</span>
        </div>

        <div class="card-foot">
          <div class="go">查看秘境详情 →</div>
          <button
            class="btn-sign"
            :class="{ signed: item.signed }"
            :disabled="signingId === item.id"
            @click.stop="onSignup(item)"
          >{{ signupText(item) }}</button>
        </div>
      </article>
    </div>

    <div v-else-if="loading" class="empty-box">
      <span class="big">✧</span>
      正在探寻大荒秘境…
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
      未探寻到「{{ searched }}」相关秘境，试试「虚神界」「神灵之墓」等关键词
    </div>

    <!-- ===== 秘境详情弹层 ===== -->
    <transition name="fade">
      <div v-if="detail" class="detail-mask" @click.self="detail = null">
        <div class="detail-panel" :style="detailBorderStyle(detail)">
          <button class="detail-close" @click="detail = null">✕</button>

          <div class="detail-head">
            <div class="glyph big" :style="glyphStyle(detail)">{{ detail.glyph }}</div>
            <div>
              <h3>{{ detail.name }}<span class="grade-badge inline" :style="badgeStyle(detail)">{{ detail.grade }}级</span></h3>
              <p class="detail-loc">📍 {{ detail.location }}</p>
            </div>
          </div>

          <div class="detail-open" :style="openStyle(detail)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
              <circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/>
            </svg>
            开启时间 · {{ detail.openTime }}
          </div>

          <div class="detail-rows">
            <div class="row"><span>开启时长</span><b>{{ detail.duration || '不限' }}</b></div>
            <div class="row"><span>每期名额</span><b>{{ quotaText(detail) }}</b></div>
            <div class="row"><span>秘境评级</span><b>{{ detail.rank }} / 10</b></div>
            <div class="row"><span>已报名</span><b>{{ detail.signedCount || 0 }} 人{{ detail.signed ? '（含我）' : '' }}</b></div>
          </div>

          <p class="detail-desc">{{ detail.desc }}</p>

          <button
            class="btn-sign full"
            :class="{ signed: detail.signed }"
            :disabled="signingId === detail.id"
            @click="onSignup(detail)"
          >{{ signupText(detail) }}</button>

          <div class="detail-foot">—— 《大荒秘境录》 ——</div>
        </div>
      </div>
    </transition>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { listApi, gradesApi, hotApi, signupApi, cancelApi } from '../api/realm.js'
import { pointsApi } from '../api/quest.js'

const router = useRouter()
const { show } = useToast()

/* ===== 用户会话（token 失效自动退回登录页，与首页一致） ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const myPoints = ref(0)
const clock = ref('')
let timer = null

/* ===== 等级配色（与后端 grade 字段对应） ===== */
const GRADE_COLORS = {
  仙帝: '#ffe08a',
  仙王: '#9fd8ff',
  真仙: '#ff9d7a',
  上古: '#8ce8b8'
}

/* ===== 排序方式 ===== */
const sorts = [
  { key: 'default', label: '默认' },
  { key: 'rank', label: '按评级' },
  { key: 'name', label: '按名称' }
]

/* ===== 后端数据状态 ===== */
const ALL = '全部'
const loading = ref(true)
const loadError = ref('')
const total = ref(0)
const grades = ref([{ key: ALL, label: ALL, count: 0 }])
const hotWords = ref([])
const list = ref([])
const detail = ref(null)
const signingId = ref(null)

/* 检索条件 */
const keyword = ref('')
const searched = ref('')
const activeGrade = ref(ALL)
const sortBy = ref('default')

/* 首次加载完成后，筛选/排序变化才触发重新检索（避免首次双请求） */
let booted = false

/* 统计：报名总人次 / 我的报名 / 已满秘境数 */
const totalSigned = computed(() => list.value.reduce((s, r) => s + (r.signedCount || 0), 0))
const myCount = computed(() => list.value.filter(r => r.signed).length)
const openNowCount = computed(() => list.value.filter(r => r.quota > 0 && (r.signedCount || 0) >= r.quota).length)

/* 筛选项加载（等级 + 热词） */
async function loadFilters() {
  try {
    const [g, h] = await Promise.all([gradesApi(), hotApi()])
    const gd = g.data || []
    grades.value = gd.length ? gd : [{ key: ALL, label: ALL, count: 0 }]
    hotWords.value = h.data || []
    if (gd.length) total.value = gd[0].count
  } catch (e) {
    // 筛选项失败不致命，列表接口会兜底报错
    console.warn('筛选项加载失败', e)
  }
}

/* 秘境检索（关键词 + 等级 + 排序全部由后端完成，并带回报名状态） */
async function loadList() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listApi({
      keyword: searched.value,
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
  loadFilters()
  loadList()
}

function doSearch() {
  const k = keyword.value.trim()
  if (!k) {
    show('请输入关键词，当前展示全部秘境', 'ok')
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
  const el = document.querySelector('.result-head')
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function openCard(item) {
  detail.value = item
}

/* ===== 报名 / 取消报名 ===== */
function signupText(item) {
  if (signingId.value === item.id) return '处理中…'
  if (item.signed) return '✓ 已报名 · 取消'
  if (item.quota > 0 && (item.signedCount || 0) >= item.quota) return '名额已满'
  return '秘境报名'
}

async function onSignup(item) {
  signingId.value = item.id
  try {
    if (item.signed) {
      await cancelApi(item.id)
      show(`已取消「${item.name}」报名`, 'ok')
    } else {
      await signupApi(item.id)
      show(`报名成功，静候「${item.name}」开启`, 'ok')
    }
    // 弹层与卡片共用同一份数据引用，直接改状态即可同步
    item.signed = !item.signed
    item.signedCount = Math.max(0, (item.signedCount || 0) + (item.signed ? 1 : -1))
  } catch (e) {
    show(e.message || '报名失败，请稍后再试', 'err')
  } finally {
    signingId.value = null
  }
}

/* ===== 展示辅助 ===== */
function quotaText(item) {
  return item.quota > 0 ? item.quota : '不限'
}

function badgeStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}14` }
}

function glyphStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 16px ${c}22` }
}

function openStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, borderColor: `${c}44`, background: `${c}0d` }
}

function barStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  const pct = item.quota > 0 ? Math.min(100, Math.round((item.signedCount || 0) / item.quota * 100)) : 0
  return { width: pct + '%', background: `linear-gradient(90deg, ${c}88, ${c})`, boxShadow: `0 0 10px ${c}66` }
}

function detailBorderStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { borderColor: `${c}55`, boxShadow: `0 24px 70px rgba(10,6,0,.6), 0 0 40px ${c}22` }
}

/* 筛选 / 排序变化 → 重新向后台检索 */
watch([searched, activeGrade, sortBy], () => {
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

  // 用 token 向后端校验登录态，失效则退回登录页
  const me = await fetchMe()
  if (!me) {
    logout()
    router.push('/login')
    return
  }
  nickname.value = me.nickname || me.username || nickname.value
  avatarChar.value = (nickname.value || '荒')[0]

  // 导航栏总积分（失败不致命）
  try {
    const res = await pointsApi()
    myPoints.value = (res.data && res.data.points) || 0
  } catch { myPoints.value = 0 }

  await Promise.all([loadFilters(), loadList()])
  booted = true
})
onBeforeUnmount(() => clearInterval(timer))

const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goQuests = () => router.push('/quests')
const goProfile = () => router.push('/profile')
const goLogout = () => { logout(); router.push('/login') }
</script>

<style scoped>
/* ===== 主体壳层（与首页 .home-shell 同款滚动布局） ===== */
.realm-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.realm-shell::-webkit-scrollbar{width:6px}
.realm-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* ===== 首屏题头 ===== */
.realm-hero{
  max-width:1120px;margin:0 auto 26px;text-align:center;
  padding:42px 22px 30px;
  position:relative;
}
.hero-deco{
  position:absolute;top:6px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.realm-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.realm-hero .sub{color:var(--muted);margin:10px 0 22px;font-size:13px;letter-spacing:2px}
.realm-hero .sub b{color:var(--gold)}

/* 搜索栏（与登录页输入框同风格） */
.search-wrap{
  display:flex;align-items:center;gap:10px;max-width:640px;margin:0 auto;
  padding:6px 8px 6px 16px;border-radius:14px;
  background:var(--panel);border:1px solid var(--panel-line);
  transition:border-color .25s, box-shadow .25s;
}
.search-wrap:focus-within{border-color:rgba(255,224,138,.5);box-shadow:0 0 0 4px rgba(255,224,138,.08)}
.search-wrap .ico{color:var(--muted);display:flex}
.search-wrap input{
  flex:1;height:42px;border:0;outline:0;background:transparent;
  color:var(--text);font-size:14px;letter-spacing:1px;min-width:0;
}
.search-wrap input::placeholder{color:rgba(179,166,132,.65)}
.btn-search{
  height:42px;padding:0 24px;border:0;border-radius:10px;cursor:pointer;
  font-weight:700;letter-spacing:4px;font-size:14px;color:#1a1305;
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  box-shadow:0 8px 22px rgba(224,168,60,.35);
  transition:transform .2s, box-shadow .2s;
}
.btn-search:hover{transform:translateY(-1px);box-shadow:0 12px 28px rgba(224,168,60,.45)}

.hot-row{display:flex;align-items:center;justify-content:center;gap:10px;margin-top:18px;flex-wrap:wrap}
.hot-row .tag{font-size:12px;color:var(--muted);letter-spacing:2px}
.hot-chip{
  padding:5px 14px;border-radius:999px;cursor:pointer;font-size:12px;
  color:var(--gold);background:rgba(255,224,138,.08);border:1px solid rgba(255,224,138,.25);
  transition:.2s;
}
.hot-chip:hover{background:rgba(255,224,138,.18);transform:translateY(-1px)}

/* ===== 统计条 ===== */
.stat-zone{
  max-width:1120px;margin:0 auto 16px;
  display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:14px;
}
.stat-card{
  padding:16px 18px;border-radius:14px;text-align:center;
  background:var(--panel);border:1px solid var(--panel-line);
  backdrop-filter:blur(12px);-webkit-backdrop-filter:blur(12px);
}
.stat-card b{
  display:block;font-size:24px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.stat-card.jade b{background:linear-gradient(90deg,#fff,var(--cyan));-webkit-background-clip:text;background-clip:text}
.stat-card span{display:block;margin-top:4px;font-size:12px;color:var(--muted);letter-spacing:2px}

/* ===== 筛选区 ===== */
.filter-zone{max-width:1120px;margin:0 auto 8px}
.filter-row{display:flex;align-items:center;gap:8px;flex-wrap:wrap}
.filter-row .label{font-size:12px;color:var(--muted);letter-spacing:2px;margin-right:2px}
.filter-spacer{flex:1}
.filter-chip{
  padding:6px 14px;border-radius:999px;cursor:pointer;font-size:12.5px;
  color:var(--muted);background:rgba(255,255,255,.04);
  border:1px solid rgba(255,214,130,.16);
  transition:border-color .25s, box-shadow .25s, color .25s, background .25s;
}
.filter-chip .cnt{
  margin-left:6px;font-size:11px;opacity:.75;
  padding:1px 6px;border-radius:999px;background:rgba(255,255,255,.06);
}
.filter-chip:hover{color:var(--text);background:rgba(255,255,255,.07)}
.filter-chip.active{
  color:var(--gold);background:rgba(255,224,138,.1);
  border-color:rgba(255,224,138,.45);
  box-shadow:0 0 14px rgba(255,224,138,.15);
}

/* ===== 列表头 ===== */
.result-head{
  max-width:1120px;margin:20px auto 14px;
  display:flex;align-items:baseline;justify-content:space-between;gap:12px;flex-wrap:wrap;
  padding-bottom:10px;border-bottom:1px solid var(--panel-line);
}
.result-head h3{font-size:15px;letter-spacing:2px;font-weight:600}
.result-head h3 b{color:var(--gold)}
.result-head span{font-size:12px;color:var(--muted);letter-spacing:1px}

/* ===== 秘境卡片 ===== */
.realm-grid{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:repeat(auto-fill,minmax(330px,1fr));gap:18px;
}
.realm-card{
  position:relative;overflow:hidden;cursor:pointer;
  padding:20px 20px 16px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
  animation:cardIn .5s both;
  transition:transform .25s, border-color .25s, box-shadow .25s;
}
.realm-card::before{
  content:'';position:absolute;inset:0 0 auto 0;height:2px;
  background:linear-gradient(90deg,transparent,rgba(255,224,138,.55),rgba(140,232,184,.3),transparent);
  opacity:.6;transition:opacity .25s;
}
.realm-card:hover{
  transform:translateY(-4px);
  border-color:rgba(255,224,138,.45);
  box-shadow:0 26px 60px rgba(10,6,0,.6), 0 0 30px rgba(255,224,138,.12);
}
.realm-card:hover::before{opacity:1}
@keyframes cardIn{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:none}}

.grade-badge{
  position:absolute;top:14px;right:14px;
  font-size:11px;padding:3px 10px;border-radius:999px;letter-spacing:2px;
}
.grade-badge.inline{position:static;margin-left:10px;vertical-align:2px}

.realm-top{display:flex;align-items:center;gap:14px;margin-bottom:12px}
.glyph{
  width:54px;height:54px;flex:none;border-radius:14px;
  display:flex;align-items:center;justify-content:center;
  font-size:26px;user-select:none;
}
.glyph.big{width:72px;height:72px;font-size:34px;border-radius:18px}
.realm-title h4{
  font-size:19px;letter-spacing:3px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.realm-title .loc{display:block;margin-top:4px;font-size:12px;color:var(--muted);letter-spacing:1px}

/* 开启时间横幅 */
.open-time{
  display:flex;align-items:center;gap:8px;
  margin:0 0 10px;padding:8px 12px;border-radius:10px;
  border:1px solid;font-size:12.5px;letter-spacing:1px;font-weight:600;
}
.desc{
  font-size:12.8px;line-height:1.85;color:rgba(253,246,227,.78);
  min-height:72px;letter-spacing:.5px;
  display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;
}
.realm-meta{display:flex;gap:16px;margin:10px 0 8px;font-size:12px;color:var(--muted);letter-spacing:1px}
.realm-meta b{color:var(--text);font-weight:600}

/* 名额进度 */
.quota-row{display:flex;align-items:center;gap:10px;margin-bottom:12px}
.quota-bar{
  flex:1;height:5px;border-radius:3px;background:rgba(255,255,255,.1);overflow:hidden;
}
.quota-bar span{display:block;height:100%;border-radius:3px;transition:width .4s}
.quota-num{font-size:11.5px;color:var(--muted);white-space:nowrap}
.quota-num i{font-style:normal;margin:0 3px;opacity:.6}

.card-foot{display:flex;align-items:center;justify-content:space-between;gap:10px}
.go{font-size:12px;color:var(--muted);letter-spacing:2px;transition:color .25s}
.realm-card:hover .go{color:var(--gold)}
.btn-sign{
  padding:8px 18px;border:0;border-radius:10px;cursor:pointer;
  font-size:12.5px;font-weight:700;letter-spacing:2px;color:#1a1305;
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  box-shadow:0 8px 22px rgba(224,168,60,.35);
  transition:transform .2s, box-shadow .2s, filter .2s;
}
.btn-sign:hover{transform:translateY(-1px);box-shadow:0 12px 28px rgba(224,168,60,.45)}
.btn-sign:disabled{filter:saturate(.3) brightness(.75);cursor:not-allowed;transform:none}
.btn-sign.signed{
  color:var(--cyan);background:rgba(140,232,184,.08);border:1px solid rgba(140,232,184,.4);
  box-shadow:none;
}
.btn-sign.full{width:100%;margin-top:14px;padding:12px;font-size:14px;letter-spacing:4px}

/* ===== 空状态 ===== */
.empty-box{
  max-width:1120px;margin:30px auto;padding:60px 20px;text-align:center;
  color:var(--muted);letter-spacing:2px;font-size:13px;
  background:var(--panel);border:1px solid var(--panel-line);border-radius:var(--radius);
}
.empty-box .big{display:block;font-size:34px;margin-bottom:12px;color:var(--gold)}
.retry-row{margin-top:14px}

/* ===== 详情弹层 ===== */
.detail-mask{
  position:fixed;inset:0;z-index:30;
  background:rgba(10,6,0,.66);backdrop-filter:blur(6px);-webkit-backdrop-filter:blur(6px);
  display:flex;align-items:center;justify-content:center;padding:20px;
}
.detail-panel{
  position:relative;width:min(520px,100%);max-height:86vh;overflow-y:auto;
  padding:28px 26px 22px;border-radius:20px;
  background:rgba(26,21,12,.95);border:1px solid;
  animation:panelIn .3s both;
}
@keyframes panelIn{from{opacity:0;transform:translateY(18px) scale(.97)}to{opacity:1;transform:none}}
.detail-close{
  position:absolute;top:12px;right:12px;width:30px;height:30px;
  border:0;border-radius:8px;cursor:pointer;font-size:14px;
  color:var(--muted);background:rgba(255,255,255,.05);transition:.2s;
}
.detail-close:hover{color:var(--gold);background:rgba(255,224,138,.12)}
.detail-head{display:flex;align-items:center;gap:16px;margin-bottom:14px}
.detail-head h3{
  font-size:21px;letter-spacing:3px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.detail-loc{margin-top:5px;font-size:12px;color:var(--muted);letter-spacing:1px}
.detail-open{
  display:flex;align-items:center;gap:8px;
  margin-bottom:14px;padding:10px 14px;border-radius:10px;
  border:1px solid;font-size:13px;letter-spacing:1px;font-weight:600;
}
.detail-rows{display:flex;flex-direction:column;gap:8px;margin-bottom:14px}
.detail-rows .row{
  display:flex;justify-content:space-between;gap:12px;
  padding:9px 12px;border-radius:10px;
  background:rgba(255,255,255,.03);border:1px solid rgba(255,214,130,.1);
  font-size:13px;
}
.detail-rows .row span{color:var(--muted);letter-spacing:1px}
.detail-rows .row b{color:var(--gold);font-weight:600}
.detail-desc{font-size:13.2px;line-height:1.9;color:rgba(253,246,227,.82);letter-spacing:.5px;margin-bottom:6px}
.detail-foot{margin-top:16px;text-align:center;font-size:11px;color:rgba(179,166,132,.6);letter-spacing:3px}

/* 弹层过渡 */
.fade-enter-active,.fade-leave-active{transition:opacity .25s}
.fade-enter-from,.fade-leave-to{opacity:0}

/* ===== 响应式 ===== */
@media (max-width:640px){
  .realm-shell{padding-top:88px}
  .realm-hero h2{font-size:24px;letter-spacing:5px}
  .hero-deco{font-size:44px;letter-spacing:10px}
  .stat-zone{grid-template-columns:repeat(2,1fr)}
}
</style>
