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
      <button class="nav-item active">人物介绍</button>
      <button class="nav-item" @click="goRealms">秘境首页</button>
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

  <!-- ===== 人物志主体（可滚动） ===== -->
  <main class="char-shell">
    <!-- 首屏：人物志题头 + 搜索栏 -->
    <section class="char-hero">
      <div class="hero-deco">大 荒 人 物 志</div>
      <h2>大荒人物志</h2>
      <p class="sub">仙古至今，凡在青史留名者，皆录于此 · 共 <b>{{ total }}</b> 位人物</p>

      <div class="search-wrap">
        <span class="ico">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
        </span>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索 人物 / 称号 / 境界 / 势力 / 语录 …"
          maxlength="40"
          @keyup.enter="doSearch"
        >
        <button class="btn-search" @click="doSearch">查 询</button>
      </div>

      <div class="hot-row">
        <span class="tag">热门人物</span>
        <button v-for="w in hotWords" :key="w" class="hot-chip" @click="pickHot(w)">{{ w }}</button>
      </div>
    </section>

    <!-- 筛选区 -->
    <section class="filter-zone">
      <div class="filter-row">
        <span class="label">势力</span>
        <button
          v-for="f in factions"
          :key="f.key"
          class="filter-chip"
          :class="{ active: activeFaction === f.key }"
          @click="activeFaction = f.key"
        >{{ f.label }}<span class="cnt">{{ f.count }}</span></button>

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

      <div class="filter-row">
        <span class="label">定位</span>
        <button
          v-for="g in grades"
          :key="g.key"
          class="filter-chip"
          :class="{ active: activeGrade === g.key }"
          @click="activeGrade = g.key"
        >{{ g.label }}<span class="cnt">{{ g.count }}</span></button>
      </div>
    </section>

    <!-- ===== 人物实力排行榜（天骄榜） ===== -->
    <section class="rank-zone">
      <div class="rank-head">
        <h3>天 骄 榜</h3>
        <span>以实力评分论座次 · 点击榜上人物可查看人物志</span>
      </div>

      <div v-if="ranking.length" class="rank-board">
        <div
          v-for="(item, i) in ranking"
          :key="'rk' + (item.id || item.name)"
          class="rank-row"
          :class="{ top3: i < 3 }"
          @click="openCard(item)"
        >
          <span class="rk-no" :class="'no-' + (i + 1)">{{ i + 1 }}</span>
          <span class="rk-glyph" :style="glyphStyle(item)">{{ item.glyph }}</span>
          <span class="rk-name">
            <b>{{ item.name }}</b>
            <i>{{ item.title }}</i>
          </span>
          <span class="rk-realm">{{ item.realm }}</span>
          <span class="rk-bar-wrap" title="实力评分">
            <span class="rk-bar" :style="barStyle(item)"></span>
          </span>
          <span class="rk-score">{{ (item.rank || 0).toFixed(1) }}</span>
        </div>
      </div>

      <div v-else class="rank-empty">榜单暂无数据，请确认后端服务已启动</div>
    </section>

    <!-- 列表头 -->
    <div class="result-head">
      <h3 v-if="searched">检索「<b>{{ searched }}</b>」 · 共 <b>{{ list.length }}</b> 位</h3>
      <h3 v-else>人物总览 · 共 <b>{{ list.length }}</b> 位</h3>
      <span>{{ loading ? '正在翻阅人物志…' : '数据来源 · 后端人物志' }}</span>
    </div>

    <!-- 人物卡片 -->
    <div v-if="list.length" class="char-grid">
      <article
        v-for="(item, i) in list"
        :key="item.id || item.name"
        class="char-card"
        :style="{ animationDelay: (i * 0.05) + 's' }"
        @click="openCard(item)"
      >
        <span class="grade-badge" :style="badgeStyle(item)">{{ item.grade }}</span>

        <div class="char-top">
          <div class="glyph" :style="glyphStyle(item)">{{ item.glyph }}</div>
          <div class="char-title">
            <h4>{{ item.name }}</h4>
            <span class="title">{{ item.title }}</span>
          </div>
        </div>

        <blockquote class="quote">「{{ item.quote }}」</blockquote>

        <p class="desc">{{ item.description }}</p>

        <div class="char-meta">
          <span>境界 · <b>{{ item.realm }}</b></span>
          <span>势力 · <b>{{ item.faction }}</b></span>
        </div>

        <div class="go">查看人物志 →</div>
      </article>
    </div>

    <div v-else-if="loading" class="empty-box">
      <span class="big">✧</span>
      正在翻阅大荒人物志…
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
      未检索到「{{ searched }}」相关人物，试试「石昊」「柳神」等关键词
    </div>

    <!-- ===== 人物详情弹层 ===== -->
    <transition name="fade">
      <div v-if="detail" class="detail-mask" @click.self="detail = null">
        <div class="detail-panel" :style="detailBorderStyle(detail)">
          <button class="detail-close" @click="detail = null">✕</button>

          <div class="detail-head">
            <div class="glyph big" :style="glyphStyle(detail)">{{ detail.glyph }}</div>
            <div>
              <h3>{{ detail.name }}<span class="grade-badge inline" :style="badgeStyle(detail)">{{ detail.grade }}</span></h3>
              <p class="detail-title">{{ detail.title }}</p>
            </div>
          </div>

          <blockquote class="detail-quote">「{{ detail.quote }}」</blockquote>

          <div class="detail-rows">
            <div class="row"><span>当前境界</span><b>{{ detail.realm }}</b></div>
            <div class="row"><span>出身势力</span><b>{{ detail.faction }}</b></div>
            <div class="row"><span>实力评分</span><b>{{ detail.rank }} / 10</b></div>
          </div>

          <p class="detail-desc">{{ detail.description }}</p>

          <div class="detail-foot">—— 《大荒人物志》 ——</div>
        </div>
      </div>
    </transition>
  </main>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { listApi, factionsApi, gradesApi, hotApi, rankApi } from '../api/character.js'
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

/* ===== 定位配色（与后端 grade 字段对应） ===== */
const GRADE_COLORS = {
  仙帝: '#ffe08a',
  准仙帝: '#c9a3ff',
  仙王: '#9fd8ff',
  真仙: '#ff9d7a',
  尊者: '#8ce8b8'
}

/* ===== 排序方式 ===== */
const sorts = [
  { key: 'default', label: '默认' },
  { key: 'rank', label: '按实力' },
  { key: 'name', label: '按姓名' }
]

/* ===== 后端数据状态 ===== */
const ALL = '全部'
const loading = ref(true)
const loadError = ref('')
const total = ref(0)
const factions = ref([{ key: ALL, label: ALL, count: 0 }])
const grades = ref([{ key: ALL, label: ALL, count: 0 }])
const hotWords = ref([])
const list = ref([])
const detail = ref(null)
const ranking = ref([])

/* 检索条件 */
const keyword = ref('')
const searched = ref('')
const activeFaction = ref(ALL)
const activeGrade = ref(ALL)
const sortBy = ref('default')

/* 首次加载完成后，筛选/排序变化才触发重新检索（避免首次双请求） */
let booted = false

/* 筛选项加载（势力 + 定位 + 热词，共 3 次小请求） */
async function loadFilters() {
  try {
    const [f, g, h] = await Promise.all([factionsApi(), gradesApi(), hotApi()])
    const fd = f.data || []
    factions.value = fd.length ? fd : [{ key: ALL, label: ALL, count: 0 }]
    const gd = g.data || []
    grades.value = gd.length ? gd : [{ key: ALL, label: ALL, count: 0 }]
    hotWords.value = h.data || []
    if (fd.length) total.value = fd[0].count
  } catch (e) {
    // 筛选项失败不致命，列表接口会兜底报错
    console.warn('筛选项加载失败', e)
  }
}

/* 人物检索（关键词 + 势力 + 定位 + 排序 全部由后端完成） */
async function loadList() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listApi({
      keyword: searched.value,
      faction: activeFaction.value === ALL ? undefined : activeFaction.value,
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

/* 天骄榜：实力前 10（失败不致命，榜单区域自行兜底） */
async function loadRanking() {
  try {
    const res = await rankApi(10)
    ranking.value = res.data || []
  } catch (e) {
    console.warn('天骄榜加载失败', e)
    ranking.value = []
  }
}

function reload() {
  loadFilters()
  loadList()
  loadRanking()
}

function doSearch() {
  const k = keyword.value.trim()
  if (!k) {
    show('请输入关键词，当前展示全部人物', 'ok')
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

function badgeStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}14` }
}

function glyphStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 16px ${c}22` }
}

function detailBorderStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { borderColor: `${c}55`, boxShadow: `0 24px 70px rgba(10,6,0,.6), 0 0 40px ${c}22` }
}

/* 天骄榜实力条：宽度按评分比例，颜色随定位分级 */
function barStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  const pct = Math.max(0, Math.min(100, ((item.rank || 0) / 10) * 100))
  return { width: pct + '%', background: `linear-gradient(90deg, ${c}55, ${c})` }
}

/* 筛选 / 排序变化 → 重新向后台检索 */
watch([searched, activeFaction, activeGrade, sortBy], () => {
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

  await Promise.all([loadFilters(), loadList(), loadRanking()])
  booted = true
})
onBeforeUnmount(() => clearInterval(timer))

const goHome = () => router.push('/home')
const goRealms = () => router.push('/realms')
const goQuests = () => router.push('/quests')
const goProfile = () => router.push('/profile')
const goLogout = () => { logout(); router.push('/login') }
</script>

<style scoped>
/* ===== 主体壳层（与首页 .home-shell 同款滚动布局） ===== */
.char-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.char-shell::-webkit-scrollbar{width:6px}
.char-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* ===== 首屏题头 ===== */
.char-hero{
  max-width:1120px;margin:0 auto 26px;text-align:center;
  padding:42px 22px 30px;
  position:relative;
}
.hero-deco{
  position:absolute;top:6px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.char-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.char-hero .sub{color:var(--muted);margin:10px 0 22px;font-size:13px;letter-spacing:2px}
.char-hero .sub b{color:var(--gold)}

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

/* ===== 筛选区 ===== */
.filter-zone{max-width:1120px;margin:0 auto 8px;display:flex;flex-direction:column;gap:10px}
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

/* ===== 人物卡片 ===== */
.char-grid{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:repeat(auto-fill,minmax(330px,1fr));gap:18px;
}
.char-card{
  position:relative;overflow:hidden;cursor:pointer;
  padding:20px 20px 16px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
  animation:cardIn .5s both;
  transition:transform .25s, border-color .25s, box-shadow .25s;
}
.char-card::before{
  content:'';position:absolute;inset:0 0 auto 0;height:2px;
  background:linear-gradient(90deg,transparent,rgba(255,224,138,.55),rgba(140,232,184,.3),transparent);
  opacity:.6;transition:opacity .25s;
}
.char-card:hover{
  transform:translateY(-4px);
  border-color:rgba(255,224,138,.45);
  box-shadow:0 26px 60px rgba(10,6,0,.6), 0 0 30px rgba(255,224,138,.12);
}
.char-card:hover::before{opacity:1}
@keyframes cardIn{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:none}}

.grade-badge{
  position:absolute;top:14px;right:14px;
  font-size:11px;padding:3px 10px;border-radius:999px;letter-spacing:2px;
}
.grade-badge.inline{position:static;margin-left:10px;vertical-align:2px}

.char-top{display:flex;align-items:center;gap:14px;margin-bottom:12px}
.glyph{
  width:54px;height:54px;flex:none;border-radius:14px;
  display:flex;align-items:center;justify-content:center;
  font-size:26px;user-select:none;
}
.glyph.big{width:72px;height:72px;font-size:34px;border-radius:18px}
.char-title h4{
  font-size:19px;letter-spacing:3px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.char-title .title{display:block;margin-top:4px;font-size:12px;color:var(--muted);letter-spacing:1px}

.quote{
  margin:0 0 10px;padding:8px 12px;
  border-left:2px solid rgba(255,224,138,.45);
  background:rgba(255,224,138,.05);
  border-radius:0 10px 10px 0;
  font-size:12.5px;color:var(--gold);letter-spacing:1px;
  font-style:italic;
}
.desc{
  font-size:12.8px;line-height:1.85;color:rgba(253,246,227,.78);
  min-height:72px;letter-spacing:.5px;
  display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;
}
.char-meta{
  display:flex;gap:14px;flex-wrap:wrap;margin:12px 0 10px;
  padding-top:10px;border-top:1px dashed rgba(255,214,130,.16);
}
.char-meta span{font-size:11.5px;color:var(--muted);letter-spacing:1px}
.char-meta b{color:var(--text);font-weight:600}
.go{
  font-size:12px;color:var(--gold);letter-spacing:2px;text-align:right;
  opacity:.75;transition:opacity .2s, transform .2s;
}
.char-card:hover .go{opacity:1;transform:translateX(3px)}

/* ===== 人物实力排行榜（天骄榜） ===== */
.rank-zone{
  max-width:1120px;margin:6px auto 22px;
  padding:18px 20px 16px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
}
.rank-head{display:flex;align-items:baseline;gap:14px;flex-wrap:wrap;margin-bottom:12px}
.rank-head h3{
  font-size:18px;letter-spacing:6px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.rank-head span{font-size:12px;color:var(--muted);letter-spacing:1px}
.rank-board{display:grid;grid-template-columns:1fr 1fr;gap:6px 18px}
.rank-row{
  display:flex;align-items:center;gap:10px;
  padding:8px 10px;border-radius:12px;cursor:pointer;
  border:1px solid transparent;
  transition:background .2s, border-color .2s, transform .2s;
}
.rank-row:hover{
  background:rgba(255,224,138,.06);border-color:rgba(255,224,138,.25);
  transform:translateX(3px);
}
.rank-row.top3{background:rgba(255,224,138,.04)}
.rk-no{
  width:30px;height:30px;flex:none;border-radius:9px;
  display:flex;align-items:center;justify-content:center;
  font-size:14px;font-weight:800;font-style:normal;
  color:var(--muted);background:rgba(255,255,255,.05);
  border:1px solid rgba(255,214,130,.16);
}
.rk-no.no-1{color:#1a1305;background:linear-gradient(135deg,#ffe08a,#e0a83c);border-color:transparent;box-shadow:0 0 14px rgba(255,224,138,.45)}
.rk-no.no-2{color:#fff;background:linear-gradient(135deg,#cfd8e3,#8fa1b8);border-color:transparent}
.rk-no.no-3{color:#4a2c08;background:linear-gradient(135deg,#f0b98a,#b9743c);border-color:transparent}
.rk-glyph{
  width:38px;height:38px;flex:none;border-radius:11px;
  display:flex;align-items:center;justify-content:center;font-size:19px;
}
.rk-name{width:170px;flex:none;min-width:0}
.rk-name b{
  display:block;font-size:14.5px;letter-spacing:2px;color:var(--text);
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
}
.rk-name i{
  display:block;margin-top:2px;font-size:11px;font-style:normal;color:var(--muted);
  letter-spacing:.5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
}
.rk-realm{
  width:150px;flex:none;font-size:11.5px;color:var(--muted);letter-spacing:.5px;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
}
.rk-bar-wrap{
  flex:1;height:6px;border-radius:999px;overflow:hidden;
  background:rgba(255,255,255,.07);
}
.rk-bar{display:block;height:100%;border-radius:999px;transition:width .6s ease}
.rk-score{
  width:44px;flex:none;text-align:right;font-size:13px;font-weight:700;
  color:var(--gold);letter-spacing:1px;
}
.rank-empty{
  padding:18px 0;text-align:center;font-size:12.5px;color:var(--muted);letter-spacing:1px;
}

/* ===== 空态 / 错误 ===== */
.empty-box{
  max-width:520px;margin:40px auto;padding:36px 20px;text-align:center;
  border-radius:var(--radius);background:var(--panel);border:1px solid var(--panel-line);
  color:var(--muted);font-size:13px;letter-spacing:1px;
}
.empty-box .big{display:block;font-size:30px;margin-bottom:10px;color:var(--gold)}
.retry-row{margin-top:14px}

/* ===== 详情弹层 ===== */
.detail-mask{
  position:fixed;inset:0;z-index:20;
  display:flex;align-items:center;justify-content:center;padding:24px;
  background:rgba(10,6,0,.66);backdrop-filter:blur(6px);-webkit-backdrop-filter:blur(6px);
}
.detail-panel{
  position:relative;width:min(560px,100%);max-height:82vh;overflow-y:auto;
  padding:30px 30px 22px;border-radius:20px;
  background:rgba(26,21,12,.94);
  border:1px solid var(--panel-line);
  animation:cardIn .35s both;
}
.detail-close{
  position:absolute;top:14px;right:14px;width:30px;height:30px;
  border-radius:50%;border:1px solid var(--panel-line);cursor:pointer;
  background:rgba(255,255,255,.04);color:var(--muted);font-size:13px;
  transition:.2s;
}
.detail-close:hover{color:var(--gold);border-color:rgba(255,224,138,.45)}
.detail-head{display:flex;align-items:center;gap:16px;margin-bottom:16px}
.detail-head h3{font-size:22px;letter-spacing:3px;color:#fff}
.detail-title{margin-top:6px;font-size:12.5px;color:var(--muted);letter-spacing:1px}
.detail-quote{
  margin:0 0 16px;padding:12px 16px;
  border-left:2px solid rgba(255,224,138,.5);
  background:rgba(255,224,138,.06);border-radius:0 12px 12px 0;
  color:var(--gold);font-size:14px;font-style:italic;letter-spacing:1px;line-height:1.8;
}
.detail-rows{
  display:flex;flex-direction:column;gap:8px;margin-bottom:16px;
  padding:14px 16px;border-radius:12px;
  background:rgba(255,255,255,.03);border:1px dashed rgba(255,214,130,.18);
}
.detail-rows .row{display:flex;justify-content:space-between;font-size:13px}
.detail-rows .row span{color:var(--muted);letter-spacing:2px}
.detail-rows .row b{color:var(--text);font-weight:600;letter-spacing:1px}
.detail-desc{font-size:13.5px;line-height:2;color:rgba(253,246,227,.85);letter-spacing:.5px}
.detail-foot{margin-top:18px;text-align:center;font-size:11px;color:var(--muted);letter-spacing:4px}

.fade-enter-active,.fade-leave-active{transition:opacity .25s}
.fade-enter-from,.fade-leave-to{opacity:0}

/* ===== 响应式 ===== */
@media (max-width:640px){
  .char-shell{padding:88px 12px 36px}
  .char-hero h2{font-size:24px;letter-spacing:5px}
  .hero-deco{font-size:44px;letter-spacing:10px}
  .char-grid{grid-template-columns:1fr}
  .filter-spacer{display:none}
  .rank-board{grid-template-columns:1fr}
  .rk-realm{display:none}
}
</style>
