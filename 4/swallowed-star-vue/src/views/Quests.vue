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
      <button class="nav-item" @click="goRealms">秘境首页</button>
      <button class="nav-item active">任务接取</button>
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

  <!-- ===== 任务大厅主体（可滚动） ===== -->
  <main class="quest-shell">
    <!-- 首屏：悬赏榜题头 + 搜索栏 -->
    <section class="quest-hero">
      <div class="hero-deco">大 荒 悬 赏 榜</div>
      <h2>大荒悬赏榜</h2>
      <p class="sub">接取悬赏即得积分 · 任务分四等，等级越高积分越丰 · 榜上现有 <b>{{ total }}</b> 项任务</p>

      <div class="search-wrap">
        <span class="ico">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
        </span>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索 任务 / 发布方 / 简介 …"
          maxlength="40"
          @keyup.enter="doSearch"
        >
        <button class="btn-search" @click="doSearch">揭 榜</button>
      </div>

      <div class="hot-row">
        <span class="tag">热门悬赏</span>
        <button v-for="w in hotWords" :key="w" class="hot-chip" @click="pickHot(w)">{{ w }}</button>
      </div>
    </section>

    <!-- 统计条 -->
    <section class="stat-zone">
      <div class="stat-card">
        <b>{{ myPoints }}</b>
        <span>我的总积分</span>
      </div>
      <div class="stat-card jade">
        <b>{{ myCount }}</b>
        <span>已接任务</span>
      </div>
      <div class="stat-card">
        <b>{{ total }}</b>
        <span>悬赏任务总数</span>
      </div>
      <div class="stat-card">
        <b>{{ topCount }}</b>
        <span>仙帝级悬赏</span>
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
      <h3 v-if="searched">检索「<b>{{ searched }}</b>」 · 得 <b>{{ list.length }}</b> 项悬赏</h3>
      <h3 v-else>悬赏总览 · 共 <b>{{ list.length }}</b> 项</h3>
      <span>{{ loading ? '正在翻阅悬赏榜…' : '数据来源 · 后端悬赏榜' }}</span>
    </div>

    <!-- 任务卡片 -->
    <div v-if="list.length" class="quest-grid">
      <article
        v-for="(item, i) in list"
        :key="item.id || item.name"
        class="quest-card"
        :class="{ done: item.accepted }"
        :style="{ animationDelay: (i * 0.05) + 's' }"
        @click="openCard(item)"
      >
        <span class="grade-badge" :style="badgeStyle(item)">{{ item.grade }}级</span>

        <div class="quest-top">
          <div class="glyph" :style="glyphStyle(item)">{{ item.glyph }}</div>
          <div class="quest-title">
            <h4>{{ item.name }}</h4>
            <span class="poster">发布 · {{ item.poster }}</span>
          </div>
          <div class="points" :style="pointsStyle(item)">+{{ item.points }}<i>积分</i></div>
        </div>

        <p class="desc">{{ item.desc }}</p>

        <div class="quest-meta">
          <span>已接 · <b>{{ item.acceptedCount || 0 }} 人</b></span>
          <span v-if="item.accepted" class="ok-tag">✓ 我已接取</span>
        </div>

        <div class="card-foot">
          <div class="go">查看任务详情 →</div>
          <button
            class="btn-accept"
            :class="{ done: item.accepted }"
            :disabled="acceptingId === item.id"
            @click.stop="onAccept(item)"
          >{{ acceptText(item) }}</button>
        </div>
      </article>
    </div>

    <div v-else-if="loading" class="empty-box">
      <span class="big">✧</span>
      正在翻阅大荒悬赏榜…
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
      未检索到「{{ searched }}」相关悬赏，试试「护送」「凶兽」等关键词
    </div>

    <!-- ===== 任务详情弹层 ===== -->
    <transition name="fade">
      <div v-if="detail" class="detail-mask" @click.self="detail = null">
        <div class="detail-panel" :style="detailBorderStyle(detail)">
          <button class="detail-close" @click="detail = null">✕</button>

          <div class="detail-head">
            <div class="glyph big" :style="glyphStyle(detail)">{{ detail.glyph }}</div>
            <div>
              <h3>{{ detail.name }}<span class="grade-badge inline" :style="badgeStyle(detail)">{{ detail.grade }}级</span></h3>
              <p class="detail-loc">发布 · {{ detail.poster }}</p>
            </div>
            <div class="points big" :style="pointsStyle(detail)">+{{ detail.points }}<i>积分</i></div>
          </div>

          <div class="detail-rows">
            <div class="row"><span>任务等级</span><b>{{ detail.grade }}级（{{ gradeTip(detail.grade) }}）</b></div>
            <div class="row"><span>接取奖励</span><b>+{{ detail.points }} 积分</b></div>
            <div class="row"><span>接取进度</span><b>{{ detail.acceptedCount || 0 }} 人已接{{ detail.accepted ? '（含我）' : '' }}</b></div>
          </div>

          <p class="detail-desc">{{ detail.desc }}</p>

          <button
            class="btn-accept full"
            :class="{ done: detail.accepted }"
            :disabled="acceptingId === detail.id"
            @click="onAccept(detail)"
          >{{ acceptText(detail) }}</button>

          <div class="detail-foot">—— 《大荒悬赏榜》 ——</div>
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
import { listApi, gradesApi, hotApi, acceptApi, pointsApi } from '../api/quest.js'

const router = useRouter()
const { show } = useToast()

/* ===== 用户会话（token 失效自动退回登录页，与首页一致） ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const clock = ref('')
let timer = null

/* ===== 等级配色（与后端 grade 字段对应） ===== */
const GRADE_COLORS = {
  仙帝: '#ffe08a',
  仙王: '#9fd8ff',
  真仙: '#ff9d7a',
  上古: '#8ce8b8'
}
const GRADE_TIPS = {
  仙帝: '积分最丰 · 九死一生',
  仙王: '高额悬赏 · 凶险非常',
  真仙: '报酬可观 · 需真本领',
  上古: '轻松历练 · 积分虽少胜在稳'
}

/* ===== 排序方式 ===== */
const sorts = [
  { key: 'default', label: '默认' },
  { key: 'points', label: '按积分' },
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
const acceptingId = ref(null)
const myPoints = ref(0)

/* 检索条件 */
const keyword = ref('')
const searched = ref('')
const activeGrade = ref(ALL)
const sortBy = ref('default')

/* 首次加载完成后，筛选/排序变化才触发重新检索（避免首次双请求） */
let booted = false

/* 统计：已接任务数 / 仙帝级数量 */
const myCount = computed(() => list.value.filter(q => q.accepted).length)
const topCount = computed(() => list.value.filter(q => q.grade === '仙帝').length)

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

/* 我的总积分 */
async function loadPoints() {
  try {
    const res = await pointsApi()
    myPoints.value = (res.data && res.data.points) || 0
  } catch (e) {
    myPoints.value = 0
  }
}

/* 任务检索（关键词 + 等级 + 排序全部由后端完成，并带回接取状态） */
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
  loadPoints()
}

function doSearch() {
  const k = keyword.value.trim()
  if (!k) {
    show('请输入关键词，当前展示全部悬赏', 'ok')
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

/* ===== 接取任务（接取即发积分） ===== */
function acceptText(item) {
  if (acceptingId.value === item.id) return '处理中…'
  if (item.accepted) return '✓ 已接取'
  return '接取 +' + item.points
}

async function onAccept(item) {
  if (item.accepted) {
    show('该任务已在你的接取记录中', 'ok')
    return
  }
  acceptingId.value = item.id
  try {
    const res = await acceptApi(item.id)
    const gained = (res.data && res.data.gained) || item.points
    // 弹层与卡片共用同一份数据引用，直接改状态即可同步
    item.accepted = true
    item.acceptedCount = (item.acceptedCount || 0) + 1
    await loadPoints()
    show(`接取成功！「${item.name}」 +${gained} 积分`, 'ok')
  } catch (e) {
    show(e.message || '接取失败，请稍后再试', 'err')
  } finally {
    acceptingId.value = null
  }
}

/* ===== 展示辅助 ===== */
function gradeTip(grade) {
  return GRADE_TIPS[grade] || ''
}

function badgeStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}14` }
}

function glyphStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 16px ${c}22` }
}

function pointsStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, textShadow: `0 0 12px ${c}66` }
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

  await Promise.all([loadFilters(), loadList(), loadPoints()])
  booted = true
})
onBeforeUnmount(() => clearInterval(timer))

const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goRealms = () => router.push('/realms')
const goProfile = () => router.push('/profile')
const goLogout = () => { logout(); router.push('/login') }
</script>

<style scoped>
/* ===== 主体壳层（与首页 .home-shell 同款滚动布局） ===== */
.quest-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.quest-shell::-webkit-scrollbar{width:6px}
.quest-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* 导航栏积分徽章 */
.points-chip{
  display:inline-flex;align-items:center;gap:4px;
  padding:4px 12px;border-radius:999px;font-size:12px;font-weight:700;
  color:var(--gold);background:rgba(255,224,138,.1);border:1px solid rgba(255,224,138,.35);
  letter-spacing:1px;white-space:nowrap;
}

/* ===== 首屏题头 ===== */
.quest-hero{
  max-width:1120px;margin:0 auto 26px;text-align:center;
  padding:42px 22px 30px;
  position:relative;
}
.hero-deco{
  position:absolute;top:6px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.quest-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.quest-hero .sub{color:var(--muted);margin:10px 0 22px;font-size:13px;letter-spacing:2px}
.quest-hero .sub b{color:var(--gold)}

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

/* ===== 任务卡片 ===== */
.quest-grid{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:repeat(auto-fill,minmax(330px,1fr));gap:18px;
}
.quest-card{
  position:relative;overflow:hidden;cursor:pointer;
  padding:20px 20px 16px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
  animation:cardIn .5s both;
  transition:transform .25s, border-color .25s, box-shadow .25s;
}
.quest-card::before{
  content:'';position:absolute;inset:0 0 auto 0;height:2px;
  background:linear-gradient(90deg,transparent,rgba(255,224,138,.55),rgba(140,232,184,.3),transparent);
  opacity:.6;transition:opacity .25s;
}
.quest-card:hover{
  transform:translateY(-4px);
  border-color:rgba(255,224,138,.45);
  box-shadow:0 26px 60px rgba(10,6,0,.6), 0 0 30px rgba(255,224,138,.12);
}
.quest-card:hover::before{opacity:1}
.quest-card.done{border-color:rgba(140,232,184,.35)}
.quest-card.done::before{
  background:linear-gradient(90deg,transparent,rgba(140,232,184,.6),transparent);
}
@keyframes cardIn{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:none}}

.grade-badge{
  position:absolute;top:14px;right:14px;
  font-size:11px;padding:3px 10px;border-radius:999px;letter-spacing:2px;
}
.grade-badge.inline{position:static;margin-left:10px;vertical-align:2px}

.quest-top{display:flex;align-items:center;gap:14px;margin-bottom:12px}
.glyph{
  width:54px;height:54px;flex:none;border-radius:14px;
  display:flex;align-items:center;justify-content:center;
  font-size:26px;user-select:none;
}
.glyph.big{width:72px;height:72px;font-size:34px;border-radius:18px}
.quest-title{flex:1;min-width:0}
.quest-title h4{
  font-size:19px;letter-spacing:3px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.quest-title .poster{display:block;margin-top:4px;font-size:12px;color:var(--muted);letter-spacing:1px}

/* 积分高亮 */
.points{font-size:20px;font-weight:800;white-space:nowrap;flex:none}
.points i{font-style:normal;font-size:11px;font-weight:600;margin-left:3px;opacity:.8;letter-spacing:1px}
.points.big{font-size:26px}

.desc{
  font-size:12.8px;line-height:1.85;color:rgba(253,246,227,.78);
  min-height:72px;letter-spacing:.5px;
  display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;
}
.quest-meta{display:flex;gap:14px;align-items:center;margin:10px 0 8px;font-size:12px;color:var(--muted);letter-spacing:1px}
.quest-meta b{color:var(--text);font-weight:600}
.ok-tag{color:var(--cyan);font-weight:600}

.card-foot{display:flex;align-items:center;justify-content:space-between;gap:10px}
.go{font-size:12px;color:var(--muted);letter-spacing:2px;transition:color .25s}
.quest-card:hover .go{color:var(--gold)}
.btn-accept{
  padding:8px 18px;border:0;border-radius:10px;cursor:pointer;
  font-size:12.5px;font-weight:700;letter-spacing:1px;color:#1a1305;
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  box-shadow:0 8px 22px rgba(224,168,60,.35);
  transition:transform .2s, box-shadow .2s, filter .2s;
}
.btn-accept:hover{transform:translateY(-1px);box-shadow:0 12px 28px rgba(224,168,60,.45)}
.btn-accept:disabled{filter:saturate(.3) brightness(.75);cursor:not-allowed;transform:none}
.btn-accept.done{
  color:var(--cyan);background:rgba(140,232,184,.08);border:1px solid rgba(140,232,184,.4);
  box-shadow:none;
}
.btn-accept.full{width:100%;margin-top:14px;padding:12px;font-size:14px;letter-spacing:4px}

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
.detail-head .points{margin-left:auto}
.detail-loc{margin-top:5px;font-size:12px;color:var(--muted);letter-spacing:1px}
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
  .quest-shell{padding-top:88px}
  .quest-hero h2{font-size:24px;letter-spacing:5px}
  .hero-deco{font-size:44px;letter-spacing:10px}
  .stat-zone{grid-template-columns:repeat(2,1fr)}
}
</style>
