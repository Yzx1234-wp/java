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
      <button class="nav-item" @click="goQuests">任务接取</button>
      <button class="nav-item" @click="goShop">商店兑换</button>
      <button class="nav-item active">修炼境界</button>
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

  <!-- ===== 问道台主体（可滚动） ===== -->
  <main class="cult-shell">
    <!-- 首屏题头 -->
    <section class="cult-hero">
      <div class="hero-deco">修 炼 境 界</div>
      <h2>问道台 · 修炼境界</h2>
      <p class="sub">
        境界不看积分，只看修为 · 参悟典籍与打坐吐纳积蓄修为 · 修为圆满后亲身渡劫，成则破境、败则道基受损
      </p>
    </section>

    <!-- 境界主盘 -->
    <section class="realm-stage" :class="{ flash: justBroke }">
      <div class="stage-ring"></div>

      <div class="stage-main">
        <span class="stage-label"><i></i>当前境界 · 第 {{ (o.tierIndex || 0) + 1 }} 境<i></i></span>
        <h3 class="stage-name">
          {{ o.tierName || '搬血' }}<em>境</em>
        </h3>
        <p class="stage-tip">{{ o.tierTip || '淬炼气血，壮筋膜、强肉身' }}</p>

        <!-- 修为进度 -->
        <div class="exp-block">
          <div class="exp-bar">
            <div class="exp-fill" :style="{ width: barPct + '%' }"></div>
            <span v-if="o.canBreak" class="exp-glow"></span>
          </div>
          <div class="exp-meta">
            <span>本境修为 <b>{{ o.exp || 0 }}</b><i v-if="!o.top"> / {{ o.need }}</i></span>
            <span v-if="o.top" class="to-top">已至大荒之巅 · 独断万古</span>
            <span v-else-if="(o.remain || 0) > 0">距「{{ o.next }}境」尚需 <b>{{ o.remain }}</b> 修为</span>
            <span v-else class="to-top">修为圆满 · 渡劫可冲击「{{ o.next }}境」</span>
          </div>
        </div>

        <!-- 渡劫按钮 -->
        <div class="break-row">
          <button
            class="btn-break"
            :class="{ ready: o.canBreak && !o.top }"
            :disabled="!o.canBreak || o.top || breaking"
            @click="onBreak"
          >
            {{ breakText }}
          </button>
          <span class="rate-line">
            渡劫成功率 <b>{{ o.successRate || 60 }}%</b>
            <i>（基础 60% + 每参悟一部 +2%，上限 92%）</i>
          </span>
        </div>
      </div>

      <!-- 右侧统计 -->
      <aside class="stage-side">
        <div class="mini-stat">
          <b>{{ o.comprehendCount || 0 }}<i>/ {{ o.comprehendTotal || 0 }}</i></b>
          <span>参悟典籍</span>
        </div>
        <div class="mini-stat jade">
          <b>{{ o.breakthroughCount || 0 }}</b>
          <span>成功破境</span>
        </div>
        <div class="mini-stat rose">
          <b>{{ o.failCount || 0 }}</b>
          <span>渡劫失败</span>
        </div>
        <div class="mini-stat">
          <b>{{ o.totalExp || 0 }}</b>
          <span>累计修为</span>
        </div>
      </aside>
    </section>

    <!-- 修炼两法 -->
    <section class="method-zone">
      <article class="method-card">
        <div class="method-head">
          <span class="method-ico">📖</span>
          <div>
            <h3>参悟典籍</h3>
            <p>研读功法库典籍，按品阶得修为。每部典籍一生只可参悟一次，是修为最丰的来源。</p>
          </div>
        </div>
        <div class="method-foot">
          <span>绝世 +200 · 绝顶 +150 · 一流 +110 · 上乘 +80 · 中乘 +55</span>
          <button class="btn-ghost" @click="goHome">去功法库 →</button>
        </div>
      </article>

      <article class="method-card">
        <div class="method-head">
          <span class="method-ico">🧘</span>
          <div>
            <h3>打坐吐纳</h3>
            <p>调息周天、吐纳灵气，每次可得 <b>{{ o.meditateGain || 24 }}</b> 修为，境界越高一次所得越多。</p>
          </div>
        </div>
        <div class="method-foot">
          <span>冷却 30 秒 · 可持续积累</span>
          <button class="btn-ghost jade" :disabled="meditateLeft > 0 || meditating" @click="onMeditate">
            {{ meditating ? '吐纳中…' : (meditateLeft > 0 ? `冷却 ${meditateLeft}s` : '打坐吐纳') }}
          </button>
        </div>
      </article>
    </section>

    <!-- 参悟功法 -->
    <section class="comprehend-zone">
      <div class="zone-head">
        <h3>✦ 可参悟典籍</h3>
        <span>参悟过的典籍不可重复参悟，但每一部都会提高渡劫成功率</span>
      </div>

      <div class="gf-filter">
        <button
          v-for="g in gradeChips"
          :key="g.key"
          class="filter-chip"
          :class="{ active: activeGrade === g.key }"
          @click="activeGrade = g.key"
        >{{ g.label }}<span class="cnt">{{ g.count }}</span></button>

        <span class="filter-spacer"></span>
        <button class="filter-chip" :class="{ active: onlyTodo }" @click="onlyTodo = !onlyTodo">
          只看未参悟
        </button>
      </div>

      <div v-if="filteredGongfa.length" class="gf-grid">
        <article
          v-for="(g, i) in filteredGongfa"
          :key="g.id"
          class="gf-card"
          :class="{ done: g.comprehended }"
          :style="{ animationDelay: (i % 12) * 0.04 + 's' }"
        >
          <span class="gf-grade" :style="badgeStyle(g)">{{ g.grade }}</span>
          <div class="gf-top">
            <div class="glyph" :style="glyphStyle(g)">{{ g.glyph }}</div>
            <div class="gf-title">
              <h4>{{ g.name }}</h4>
              <span class="gf-cat">{{ g.cat }} · 品阶 {{ g.rank }}</span>
            </div>
          </div>
          <div class="gf-foot">
            <span class="gf-gain" :style="badgeStyle(g)">+{{ g.gain }} 修为</span>
            <button
              class="btn-comprehend"
              :class="{ done: g.comprehended }"
              :disabled="g.comprehended || comprehendingId === g.id"
              @click="onComprehend(g)"
            >{{ g.comprehended ? '已参悟' : (comprehendingId === g.id ? '参悟中…' : '参 悟') }}</button>
          </div>
        </article>
      </div>
      <div v-else class="empty-box">
        <span class="big">❋</span>
        {{ onlyTodo ? '该品阶的典籍已全部参悟，换一品阶看看' : '暂无该品阶典籍' }}
      </div>
    </section>

    <!-- 十八境阶梯 -->
    <section class="ladder-zone">
      <div class="zone-head">
        <h3>✦ 十八境阶梯</h3>
        <span>金色为已达成 · 高亮为当前境界 · 灰暗为未至（数字为冲击下一境所需修为）</span>
      </div>
      <div class="ladder">
        <div
          v-for="s in o.ladder || []"
          :key="s.index"
          class="ladder-step"
          :class="s.state"
          :title="s.tip"
        >
          <span class="step-no">{{ s.index + 1 }}</span>
          <b>{{ s.name }}</b>
          <span class="step-need">{{ s.need ? s.need + ' 修为' : '巅峰' }}</span>
        </div>
      </div>
    </section>

    <!-- 修炼记录 -->
    <section class="log-zone">
      <div class="zone-head">
        <h3>✦ 修炼记录</h3>
        <span>参悟、吐纳、破境与渡劫失败，皆在此留痕</span>
      </div>
      <div v-if="logs.length" class="log-list">
        <div v-for="l in logs" :key="l.id" class="log-item" :class="logClass(l.type)">
          <span class="log-ico">{{ logIcon(l.type) }}</span>
          <div class="log-main">
            <b>{{ l.title }}</b>
            <span class="log-sub">{{ l.detail }}</span>
          </div>
          <span v-if="l.expDelta" class="log-delta" :class="{ minus: l.expDelta < 0 }">
            {{ l.expDelta > 0 ? '+' : '' }}{{ l.expDelta }}
          </span>
          <span class="log-time">{{ fmtTime(l.createdAt) }}</span>
        </div>
      </div>
      <div v-else class="empty-box">
        <span class="big">✧</span>
        尚无修炼痕迹，去参悟一部典籍或打坐吐纳吧
      </div>
    </section>

    <div v-if="loadError" class="empty-box">
      <span class="big">⚠</span>
      {{ loadError }}
      <div class="retry-row">
        <button class="filter-chip active" @click="reload">重新加载</button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { overviewApi, comprehendApi, meditateApi, breakthroughApi, logsApi } from '../api/cultivation.js'
import { pointsApi } from '../api/quest.js'

const router = useRouter()
const { show } = useToast()

/* ===== 用户会话（token 失效自动退回登录页） ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const clock = ref('')
const myPoints = ref(0)
let timer = null

/* ===== 数据状态 ===== */
const o = ref({ ladder: [], gongfa: [] })
const logs = ref([])
const loading = ref(true)
const loadError = ref('')
const comprehendingId = ref(null)
const meditating = ref(false)
const breaking = ref(false)
const justBroke = ref(false)
const meditateLeft = ref(0)

/* 参悟列表筛选 */
const ALL = '全部'
const activeGrade = ref(ALL)
const onlyTodo = ref(false)

const GRADE_COLORS = {
  绝世: '#ffe08a',
  绝顶: '#9fd8ff',
  一流: '#ff9d7a',
  上乘: '#8ce8b8',
  中乘: '#b3a684'
}

/* ===== 派生数据 ===== */
const barPct = computed(() => Math.min(100, Number(o.value.progress) || 0))

const breakText = computed(() => {
  if (o.value.top) return '已至仙帝 · 独断万古'
  if (breaking.value) return '渡劫中…'
  if (!o.value.canBreak) return `修为未满 · 尚需 ${o.value.remain || 0}`
  return `冲 击「${o.value.next}境」`
})

const gradeChips = computed(() => {
  const list = o.value.gongfa || []
  const cnt = {}
  list.forEach(g => { cnt[g.grade] = (cnt[g.grade] || 0) + 1 })
  const fixed = ['绝世', '绝顶', '一流', '上乘', '中乘']
  const chips = [{ key: ALL, label: ALL, count: list.length }]
  fixed.forEach(k => {
    if (cnt[k]) chips.push({ key: k, label: k, count: cnt[k] })
  })
  return chips
})

const filteredGongfa = computed(() => {
  let list = o.value.gongfa || []
  if (activeGrade.value !== ALL) list = list.filter(g => g.grade === activeGrade.value)
  if (onlyTodo.value) list = list.filter(g => !g.comprehended)
  return list
})

/* ===== 加载 ===== */
async function loadOverview(silent = false) {
  if (!silent) loading.value = true
  loadError.value = ''
  try {
    const res = await overviewApi()
    o.value = res.data || { ladder: [], gongfa: [] }
    meditateLeft.value = Math.max(0, Number(o.value.meditateCooldown) || 0)
  } catch (e) {
    loadError.value = e.message || '修炼数据加载失败，请确认后端服务已启动'
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  try {
    const res = await logsApi(20)
    logs.value = res.data || []
  } catch { logs.value = [] }
}

function reload() {
  loadOverview()
  loadLogs()
}

/* ===== 修炼操作 ===== */
async function onComprehend(g) {
  if (g.comprehended) return
  comprehendingId.value = g.id
  try {
    const res = await comprehendApi(g.id)
    const d = res.data || {}
    g.comprehended = true
    patchProgress(d)
    show(`参悟《${g.name}》 · 修为 +${d.gain || g.gain}`, 'ok')
    loadOverview(true)
    loadLogs()
  } catch (e) {
    show(e.message || '参悟失败，请稍后再试', 'err')
  } finally {
    comprehendingId.value = null
  }
}

async function onMeditate() {
  if (meditateLeft.value > 0) return
  meditating.value = true
  try {
    const res = await meditateApi()
    const d = res.data || {}
    patchProgress(d)
    meditateLeft.value = Math.max(0, Number(d.meditateCooldown) || 30)
    show(`调息周天 · 修为 +${d.gain || 0}`, 'ok')
    loadLogs()
  } catch (e) {
    show(e.message || '吐纳失败，请稍后再试', 'err')
  } finally {
    meditating.value = false
  }
}

async function onBreak() {
  if (!o.value.canBreak || o.value.top) return
  breaking.value = true
  try {
    const res = await breakthroughApi()
    const d = res.data || {}
    patchProgress(d)
    if (d.success) {
      justBroke.value = true
      setTimeout(() => { justBroke.value = false }, 1600)
      show(d.message || `成功晋升「${d.to}境」`, 'ok')
    } else {
      show(d.message || '渡劫失败，修为折损', 'err')
    }
    loadOverview(true)
    loadLogs()
  } catch (e) {
    show(e.message || '冲击境界失败，请稍后再试', 'err')
  } finally {
    breaking.value = false
  }
}

/* 用接口返回的最新进度就地刷新面板（避免整页闪烁） */
function patchProgress(d) {
  if (d.tierIndex !== undefined) o.value.tierIndex = d.tierIndex
  if (d.tierName) o.value.tierName = d.tierName
  if (d.exp !== undefined) o.value.exp = d.exp
  if (d.need !== undefined) o.value.need = d.need
  if (d.progress !== undefined) o.value.progress = d.progress
  if (d.remain !== undefined) o.value.remain = d.remain
  if (d.canBreak !== undefined) o.value.canBreak = d.canBreak
  if (d.top !== undefined) o.value.top = d.top
  if (d.next !== undefined) o.value.next = d.next
  if (d.successRate !== undefined) o.value.successRate = d.successRate
  if (d.comprehendCount !== undefined) o.value.comprehendCount = d.comprehendCount
  if (d.totalExp !== undefined) o.value.totalExp = d.totalExp
  if (d.breakthroughCount !== undefined) o.value.breakthroughCount = d.breakthroughCount
  if (d.failCount !== undefined) o.value.failCount = d.failCount
  if (d.meditateGain !== undefined) o.value.meditateGain = d.meditateGain
}

/* ===== 展示辅助 ===== */
function badgeStyle(g) {
  const c = GRADE_COLORS[g.grade] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}14` }
}

function glyphStyle(g) {
  const c = GRADE_COLORS[g.grade] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 16px ${c}22` }
}

function logIcon(type) {
  if (type === 'COMPREHEND') return '📖'
  if (type === 'MEDITATE') return '🧘'
  if (type === 'BREAKTHROUGH') return '⚡'
  if (type === 'ADMIN') return '⚖️'
  return '💢'
}

function logClass(type) {
  return {
    comprehend: type === 'COMPREHEND',
    meditate: type === 'MEDITATE',
    up: type === 'BREAKTHROUGH',
    down: type === 'FAIL'
  }
}

function fmtTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(5, 16)
}

/* ===== 时钟 / 冷却倒计时 / 登录态 ===== */
function tick() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  clock.value = `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  if (meditateLeft.value > 0) meditateLeft.value -= 1
}

onMounted(async () => {
  tick()
  timer = setInterval(tick, 1000)

  const me = await fetchMe()
  if (!me) {
    logout()
    router.push('/login')
    return
  }
  nickname.value = me.nickname || me.username || nickname.value
  avatarChar.value = (nickname.value || '荒')[0]

  try {
    const res = await pointsApi()
    myPoints.value = (res.data && res.data.points) || 0
  } catch { myPoints.value = 0 }

  await Promise.all([loadOverview(), loadLogs()])
})
onBeforeUnmount(() => clearInterval(timer))

const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goRealms = () => router.push('/realms')
const goQuests = () => router.push('/quests')
const goShop = () => router.push('/shop')
const goProfile = () => router.push('/profile')
const goLogout = () => { logout(); router.push('/login') }
</script>

<style scoped>
/* ===== 主体壳层 ===== */
.cult-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.cult-shell::-webkit-scrollbar{width:6px}
.cult-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* ===== 题头 ===== */
.cult-hero{
  max-width:1120px;margin:0 auto 22px;text-align:center;
  padding:34px 22px 6px;position:relative;
}
.hero-deco{
  position:absolute;top:0;left:50%;transform:translateX(-50%);
  font-size:62px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.cult-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.cult-hero .sub{color:var(--muted);margin-top:10px;font-size:13px;letter-spacing:1.5px;line-height:1.9}

/* ===== 境界主盘 ===== */
.realm-stage{
  position:relative;overflow:hidden;
  max-width:1120px;margin:0 auto 18px;
  display:grid;grid-template-columns:minmax(0,1fr) 240px;gap:26px;
  padding:30px 32px;border-radius:22px;
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(16px) saturate(140%);
  -webkit-backdrop-filter:blur(16px) saturate(140%);
  transition:box-shadow .4s, border-color .4s;
}
.realm-stage::before{
  content:'';position:absolute;inset:0 0 auto 0;height:2px;
  background:linear-gradient(90deg,transparent,rgba(255,224,138,.6),rgba(140,232,184,.35),transparent);
}
.stage-ring{
  position:absolute;right:-90px;top:-90px;width:280px;height:280px;border-radius:50%;
  background:radial-gradient(circle,rgba(255,224,138,.14),transparent 68%);
  pointer-events:none;
}
.realm-stage.flash{
  border-color:rgba(255,224,138,.7);
  box-shadow:0 0 0 1px rgba(255,224,138,.3), 0 0 60px rgba(255,224,138,.35), var(--shadow);
  animation:breakFlash .8s ease-out 2;
}
@keyframes breakFlash{
  0%{filter:brightness(1)}
  40%{filter:brightness(1.25)}
  100%{filter:brightness(1)}
}

.stage-label{
  display:inline-flex;align-items:center;gap:8px;
  font-size:12px;letter-spacing:3px;color:var(--muted);
}
.stage-label i{width:16px;height:1px;background:rgba(255,224,138,.5);display:inline-block}
.stage-name{
  margin-top:10px;font-size:44px;font-weight:800;letter-spacing:6px;line-height:1.1;
  background:linear-gradient(90deg,#fff 10%,var(--gold) 60%,#ffb347);
  -webkit-background-clip:text;background-clip:text;color:transparent;
  text-shadow:0 0 40px rgba(255,224,138,.25);
}
.stage-name em{
  font-style:normal;font-size:18px;letter-spacing:2px;margin-left:8px;
  -webkit-text-fill-color:rgba(255,224,138,.75);
}
.stage-tip{margin-top:8px;font-size:12.5px;color:var(--muted);letter-spacing:1px;line-height:1.8}

/* 修为进度 */
.exp-block{margin:20px 0 18px}
.exp-bar{
  position:relative;height:12px;border-radius:999px;overflow:hidden;
  background:rgba(255,255,255,.06);border:1px solid rgba(255,214,130,.16);
}
.exp-fill{
  height:100%;border-radius:999px;
  background:linear-gradient(90deg,rgba(255,224,138,.75),var(--gold),#ffd97a);
  box-shadow:0 0 18px rgba(255,224,138,.5);
  transition:width .5s ease;
}
.exp-glow{
  position:absolute;inset:0;
  background:linear-gradient(90deg,transparent,rgba(255,255,255,.28),transparent);
  animation:sweep 1.8s linear infinite;
}
@keyframes sweep{from{transform:translateX(-100%)}to{transform:translateX(100%)}}
.exp-meta{
  display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;
  margin-top:8px;font-size:12px;color:var(--muted);letter-spacing:1px;
}
.exp-meta b{color:var(--gold);font-weight:700}
.exp-meta i{font-style:normal;opacity:.6}
.to-top{color:var(--gold);font-weight:700}

/* 渡劫按钮 */
.break-row{display:flex;align-items:center;gap:16px;flex-wrap:wrap}
.btn-break{
  padding:13px 34px;border:1px solid rgba(255,214,130,.25);border-radius:12px;cursor:not-allowed;
  font-size:14px;font-weight:800;letter-spacing:3px;font-family:inherit;
  color:rgba(179,166,132,.8);background:rgba(255,255,255,.04);
  transition:transform .2s, box-shadow .25s, filter .2s;
}
.btn-break.ready{
  cursor:pointer;color:#1a1305;
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  border-color:transparent;
  box-shadow:0 10px 30px rgba(224,168,60,.4), 0 0 26px rgba(255,224,138,.3);
  animation:pulse 2.4s ease-in-out infinite;
}
.btn-break.ready:hover{transform:translateY(-2px);box-shadow:0 16px 40px rgba(224,168,60,.5)}
.btn-break:disabled{filter:none}
@keyframes pulse{
  0%,100%{box-shadow:0 10px 30px rgba(224,168,60,.4), 0 0 22px rgba(255,224,138,.25)}
  50%{box-shadow:0 10px 34px rgba(224,168,60,.5), 0 0 40px rgba(255,224,138,.5)}
}
.rate-line{font-size:12px;color:var(--muted);letter-spacing:1px}
.rate-line b{color:var(--gold);font-size:13px}
.rate-line i{font-style:normal;opacity:.65}

/* 右侧统计 */
.stage-side{
  display:grid;grid-template-columns:1fr 1fr;gap:10px;align-content:center;
  border-left:1px solid var(--panel-line);padding-left:22px;
}
.mini-stat{
  padding:12px 10px;border-radius:12px;text-align:center;
  background:rgba(255,255,255,.03);border:1px solid rgba(255,214,130,.12);
}
.mini-stat b{
  display:block;font-size:20px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.mini-stat b i{font-style:normal;font-size:11px;-webkit-text-fill-color:rgba(179,166,132,.85);letter-spacing:0}
.mini-stat span{display:block;margin-top:4px;font-size:11px;color:var(--muted);letter-spacing:1.5px}
.mini-stat.jade b{background:linear-gradient(90deg,#fff,var(--cyan));-webkit-background-clip:text;background-clip:text}
.mini-stat.rose b{background:linear-gradient(90deg,#fff,#ff9d7a);-webkit-background-clip:text;background-clip:text}

/* ===== 修炼两法 ===== */
.method-zone{
  max-width:1120px;margin:0 auto 22px;
  display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:16px;
}
.method-card{
  padding:20px 22px;border-radius:16px;
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
}
.method-head{display:flex;gap:14px}
.method-ico{
  width:44px;height:44px;flex:none;border-radius:12px;display:grid;place-items:center;
  font-size:20px;background:rgba(255,224,138,.1);border:1px solid rgba(255,224,138,.25);
}
.method-head h3{font-size:15px;letter-spacing:3px;color:var(--gold);font-weight:700}
.method-head p{margin-top:6px;font-size:12.4px;line-height:1.85;color:rgba(253,246,227,.75)}
.method-head p b{color:var(--gold)}
.method-foot{
  display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;
  margin-top:16px;padding-top:14px;border-top:1px dashed rgba(255,214,130,.16);
}
.method-foot span{font-size:11.5px;color:var(--muted);letter-spacing:.5px}
.btn-ghost{
  padding:7px 16px;border-radius:10px;cursor:pointer;font-family:inherit;
  font-size:12px;letter-spacing:1px;font-weight:700;color:var(--gold);
  background:rgba(255,224,138,.08);border:1px solid rgba(255,224,138,.4);
  transition:.2s;
}
.btn-ghost:hover{background:rgba(255,224,138,.18);box-shadow:0 0 14px rgba(255,224,138,.25)}
.btn-ghost.jade{color:var(--cyan);background:rgba(140,232,184,.08);border-color:rgba(140,232,184,.4)}
.btn-ghost.jade:hover{background:rgba(140,232,184,.18);box-shadow:0 0 14px rgba(140,232,184,.25)}
.btn-ghost:disabled{cursor:not-allowed;opacity:.5;box-shadow:none}

/* ===== 区块头 ===== */
.zone-head{
  max-width:1120px;margin:26px auto 14px;
  display:flex;align-items:baseline;gap:14px;flex-wrap:wrap;
  padding-bottom:10px;border-bottom:1px solid var(--panel-line);
}
.zone-head h3{font-size:15px;letter-spacing:2px;color:var(--text);font-weight:700}
.zone-head span{font-size:12px;color:var(--muted);letter-spacing:1px}

/* ===== 参悟筛选 ===== */
.gf-filter{max-width:1120px;margin:0 auto 14px;display:flex;align-items:center;gap:8px;flex-wrap:wrap}
.filter-spacer{flex:1}
.filter-chip{
  padding:6px 14px;border-radius:999px;cursor:pointer;font-size:12.5px;font-family:inherit;
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

/* ===== 功法卡 ===== */
.gf-grid{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:14px;
}
.gf-card{
  position:relative;padding:18px 18px 14px;border-radius:16px;
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  animation:cardIn .45s both;
  transition:transform .22s, border-color .22s, box-shadow .22s;
}
.gf-card:hover{transform:translateY(-3px);border-color:rgba(255,224,138,.4);box-shadow:0 22px 50px rgba(10,6,0,.55),0 0 26px rgba(255,224,138,.1)}
.gf-card.done{opacity:.62}
.gf-card.done:hover{transform:none;border-color:var(--panel-line);box-shadow:var(--shadow)}
@keyframes cardIn{from{opacity:0;transform:translateY(12px)}to{opacity:1;transform:none}}
.gf-grade{
  position:absolute;top:12px;right:12px;
  font-size:11px;padding:2px 9px;border-radius:999px;letter-spacing:1px;
}
.gf-top{display:flex;align-items:center;gap:12px}
.glyph{
  width:46px;height:46px;flex:none;border-radius:12px;
  display:flex;align-items:center;justify-content:center;font-size:22px;user-select:none;
}
.gf-title{flex:1;min-width:0;padding-right:44px}
.gf-title h4{
  font-size:15px;letter-spacing:1.5px;font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.gf-cat{display:block;margin-top:4px;font-size:11.5px;color:var(--muted);letter-spacing:.5px}
.gf-foot{display:flex;align-items:center;justify-content:space-between;gap:10px;margin-top:14px}
.gf-gain{font-size:12.5px;font-weight:700;padding:3px 10px;border-radius:999px;letter-spacing:.5px}
.btn-comprehend{
  padding:7px 16px;border:0;border-radius:9px;cursor:pointer;font-family:inherit;
  font-size:12px;font-weight:700;letter-spacing:2px;color:#1a1305;
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  box-shadow:0 8px 20px rgba(224,168,60,.3);
  transition:transform .2s, box-shadow .2s;
}
.btn-comprehend:hover{transform:translateY(-1px);box-shadow:0 12px 26px rgba(224,168,60,.42)}
.btn-comprehend:disabled{cursor:not-allowed;filter:saturate(.25) brightness(.85);transform:none;box-shadow:none}
.btn-comprehend.done{
  color:var(--cyan);background:rgba(140,232,184,.08);border:1px solid rgba(140,232,184,.35);
}

/* ===== 十八境阶梯 ===== */
.ladder{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:repeat(auto-fit,minmax(108px,1fr));gap:8px;
}
.ladder-step{
  padding:10px 8px;border-radius:12px;text-align:center;
  background:rgba(255,255,255,.02);border:1px solid var(--panel-line);
  transition:.25s;
}
.ladder-step .step-no{
  display:block;font-size:10px;color:rgba(179,166,132,.55);letter-spacing:1px;
}
.ladder-step b{
  display:block;margin:3px 0 2px;font-size:13px;letter-spacing:1px;font-weight:700;
  color:rgba(179,166,132,.6);
}
.ladder-step .step-need{font-size:10.5px;color:rgba(179,166,132,.45);letter-spacing:.5px}
.ladder-step.done{
  background:rgba(255,224,138,.07);border-color:rgba(255,224,138,.28);
}
.ladder-step.done b{color:var(--gold)}
.ladder-step.done .step-need{color:rgba(255,224,138,.65)}
.ladder-step.current{
  background:linear-gradient(160deg,rgba(255,224,138,.18),rgba(140,232,184,.08));
  border-color:rgba(255,224,138,.65);
  box-shadow:0 0 22px rgba(255,224,138,.22);
  transform:translateY(-2px);
}
.ladder-step.current b{color:#fff8e1;font-size:14px}
.ladder-step.current .step-need{color:rgba(255,224,138,.85)}
.ladder-step.locked{opacity:.55}

/* ===== 修炼记录 ===== */
.log-list{max-width:1120px;margin:0 auto;display:flex;flex-direction:column;gap:8px}
.log-item{
  display:flex;align-items:center;gap:12px;
  padding:12px 16px;border-radius:12px;
  background:var(--panel);border:1px solid var(--panel-line);
  transition:border-color .2s;
}
.log-item:hover{border-color:rgba(255,214,130,.28)}
.log-ico{
  width:34px;height:34px;flex:none;border-radius:10px;display:grid;place-items:center;
  font-size:15px;background:rgba(255,255,255,.04);border:1px solid rgba(255,214,130,.14);
}
.log-main{flex:1;min-width:0}
.log-main b{display:block;font-size:13.5px;letter-spacing:1px;color:var(--text);font-weight:600}
.log-sub{display:block;margin-top:3px;font-size:11.5px;color:var(--muted);letter-spacing:.5px}
.log-delta{font-size:13px;font-weight:800;color:var(--gold);letter-spacing:.5px;flex:none}
.log-delta.minus{color:#ff9d7a}
.log-time{font-size:11px;color:rgba(179,166,132,.6);letter-spacing:.5px;flex:none;font-variant-numeric:tabular-nums}
.log-item.up{border-color:rgba(255,224,138,.4);background:linear-gradient(90deg,rgba(255,224,138,.08),var(--panel))}
.log-item.down{border-color:rgba(255,157,122,.28)}

/* ===== 空状态 ===== */
.empty-box{
  max-width:1120px;margin:22px auto;padding:44px 20px;text-align:center;
  color:var(--muted);letter-spacing:2px;font-size:13px;
  background:var(--panel);border:1px solid var(--panel-line);border-radius:var(--radius);
}
.empty-box .big{display:block;font-size:32px;margin-bottom:10px;color:var(--gold)}
.retry-row{margin-top:14px}

/* ===== 响应式 ===== */
@media (max-width:1024px){
  .realm-stage{grid-template-columns:1fr;padding:26px 22px}
  .stage-side{
    border-left:0;border-top:1px solid var(--panel-line);
    padding-left:0;padding-top:18px;grid-template-columns:repeat(4,1fr);
  }
}
@media (max-width:640px){
  .cult-shell{padding-top:88px}
  .cult-hero h2{font-size:23px;letter-spacing:5px}
  .hero-deco{font-size:40px;letter-spacing:9px}
  .stage-name{font-size:34px;letter-spacing:4px}
  .stage-side{grid-template-columns:repeat(2,1fr)}
  .btn-break{width:100%}
}
</style>
