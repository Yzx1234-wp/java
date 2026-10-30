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
      <button class="nav-item active">我的主页</button>
    </div>

    <span class="spacer"></span>
    <span class="clock">{{ clock }}</span>

    <div class="nav-user">
      <span class="avatar">{{ avatarChar }}</span>
      <span class="name">{{ nickname }}</span>
      <span class="points-chip" title="我的总积分">✦ {{ summary.totalPoints || 0 }}</span>
      <button class="btn-exit" @click="goLogout">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>
        </svg>
        退出
      </button>
    </div>
  </nav>

  <!-- ===== 我的主页主体（可滚动） ===== -->
  <main class="profile-shell">
    <!-- 首屏：修士名帖 -->
    <section class="profile-hero">
      <div class="hero-deco">我 的 主 页</div>
      <div class="id-card">
        <div class="avatar-ring">
          <span class="avatar-big">{{ avatarChar }}</span>
        </div>
        <div class="id-info">
          <h2>{{ nickname }}</h2>
          <p class="id-meta">
            <span>@{{ summary.username || '—' }}</span>
            <i>·</i>
            <span>入大荒于 {{ joinedAt }}</span>
          </p>
          <div class="id-chips">
            <span class="tier-chip" :style="tierChipStyle">{{ tierName }}境</span>
            <span class="stat-chip">✦ {{ summary.totalPoints || 0 }} 积分</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计条 -->
    <section class="stat-zone">
      <div class="stat-card">
        <b>{{ summary.totalPoints || 0 }}</b>
        <span>总积分</span>
      </div>
      <div class="stat-card jade">
        <b>{{ summary.questCount || 0 }}</b>
        <span>已接任务</span>
      </div>
      <div class="stat-card violet">
        <b>{{ summary.realmCount || 0 }}</b>
        <span>已报秘境</span>
      </div>
      <div class="stat-card">
        <b>{{ summary.badgeCount || 0 }}<i class="badge-total">/ {{ badges.length }}</i></b>
        <span>点亮成就</span>
      </div>
    </section>

    <div class="columns">
      <!-- 左列：修炼境界 + 成就徽章 -->
      <div class="col-left">
        <!-- 修炼境界 -->
        <section class="panel">
          <h3 class="panel-title">✦ 修炼境界</h3>
          <div class="tier-zone">
            <div class="tier-now">
              <span class="tier-name" :style="tierNameStyle">{{ tierName }}</span>
              <span class="tier-sub" v-if="tierNext">距「{{ tierNext.next }}境」还差 {{ remainPoints }} 积分</span>
              <span class="tier-sub" v-else>已至大荒之巅 · 独断万古</span>
            </div>
            <div class="tier-bar">
              <div class="tier-fill" :style="{ width: (tierProgress || 0) + '%' }"></div>
            </div>
            <div class="tier-scale">
              <span>{{ tierName }}境</span>
              <span v-if="tierNext">{{ tierNext.next }}境 · {{ tierNext.nextPoints }} 分</span>
              <span v-else>15000 分</span>
            </div>
          </div>
        </section>

        <!-- 成就徽章 -->
        <section class="panel">
          <h3 class="panel-title">✦ 成就徽章</h3>
          <div class="badge-grid">
            <div
              v-for="b in badges"
              :key="b.key"
              class="badge-item"
              :class="{ locked: !b.unlocked }"
              :title="b.desc"
            >
              <span class="badge-icon">{{ b.icon }}</span>
              <b>{{ b.name }}</b>
              <p>{{ b.unlocked ? b.desc : '尚未点亮' }}</p>
            </div>
          </div>
        </section>
      </div>

      <!-- 右列：页签（任务 / 秘境 / 动态） -->
      <div class="col-right">
        <section class="panel">
          <div class="tab-row">
            <button
              v-for="t in tabs"
              :key="t.key"
              class="tab-btn"
              :class="{ active: activeTab === t.key }"
              @click="activeTab = t.key"
            >{{ t.label }}<span class="cnt">{{ t.count }}</span></button>
          </div>

          <!-- 我的任务 -->
          <div v-if="activeTab === 'quests'" class="list-zone">
            <div v-for="q in quests" :key="'q' + q.questId" class="line-item" @click="goQuests">
              <span class="glyph" :style="glyphStyle(q.grade)">{{ q.glyph }}</span>
              <div class="line-main">
                <b>{{ q.name }}</b>
                <span class="line-sub">{{ q.grade }}级 · 接取于 {{ fmtTime(q.acceptedAt) }}</span>
              </div>
              <span class="line-points" :style="pointsStyle(q.grade)">+{{ q.points }}</span>
            </div>
            <div v-if="!quests.length && !loading" class="mini-empty">
              还未接取任何悬赏，<a @click.prevent="goQuests">去悬赏榜揭榜 →</a>
            </div>
          </div>

          <!-- 我的秘境 -->
          <div v-if="activeTab === 'realms'" class="list-zone">
            <div v-for="r in realms" :key="'r' + r.realmId" class="line-item" @click="goRealms">
              <span class="glyph" :style="glyphStyle(r.grade)">{{ r.glyph }}</span>
              <div class="line-main">
                <b>{{ r.name }}</b>
                <span class="line-sub">{{ r.grade }}级 · {{ r.openTime }}</span>
              </div>
              <span class="line-time">报名于 {{ fmtTime(r.signedAt) }}</span>
            </div>
            <div v-if="!realms.length && !loading" class="mini-empty">
              还未报名任何秘境，<a @click.prevent="goRealms">去秘境首页报名 →</a>
            </div>
          </div>

          <!-- 修炼动态 -->
          <div v-if="activeTab === 'acts'" class="list-zone">
            <div v-for="(a, i) in activities" :key="'a' + i" class="line-item act">
              <span class="glyph" :style="glyphStyle(a.grade)">{{ a.glyph }}</span>
              <div class="line-main">
                <b>
                  {{ a.type === 'quest' ? '接取悬赏' : '报名秘境' }} · {{ a.name }}
                </b>
                <span class="line-sub">{{ a.grade }}级 · {{ fmtTime(a.time) }}</span>
              </div>
              <span v-if="a.type === 'quest'" class="line-points" :style="pointsStyle(a.grade)">+{{ a.points }}</span>
              <span v-else class="act-tag">秘境</span>
            </div>
            <div v-if="!activities.length && !loading" class="mini-empty">
              暂无修炼动态，接取任务或报名秘境后这里会留下足迹
            </div>
          </div>
        </section>
      </div>
    </div>

    <div v-if="loadError" class="empty-box">
      <span class="big">⚠</span>
      {{ loadError }}
      <div class="retry-row">
        <button class="tab-btn active" @click="reload">重新加载</button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { summaryApi, myQuestsApi, myRealmsApi } from '../api/profile.js'

const router = useRouter()

/* ===== 用户会话（token 失效自动退回登录页） ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const clock = ref('')
let timer = null

/* ===== 后端数据 ===== */
const loading = ref(true)
const loadError = ref('')
const summary = ref({})
const badges = ref([])
const quests = ref([])
const realms = ref([])
const activeTab = ref('quests')

const tabs = computed(() => [
  { key: 'quests', label: '我的任务', count: quests.value.length },
  { key: 'realms', label: '我的秘境', count: realms.value.length },
  { key: 'acts', label: '修炼动态', count: (summary.value.activities || []).length }
])

/* ===== 境界相关 ===== */
const tierName = computed(() => (summary.value.tier && summary.value.tier.name) || '搬血')
const tierNext = computed(() => summary.value.tier || null)
const tierProgress = computed(() => (summary.value.tier && summary.value.tier.progress) || 0)
const remainPoints = computed(() => {
  const t = summary.value.tier
  if (!t || !t.nextPoints) return 0
  return Math.max(0, t.nextPoints - (summary.value.totalPoints || 0))
})
const joinedAt = computed(() => {
  const t = summary.value.createdAt
  return t ? String(t).slice(0, 10) : '太古之前'
})

/* 等级配色（与悬赏榜一致） */
const GRADE_COLORS = {
  仙帝: '#ffe08a',
  仙王: '#9fd8ff',
  真仙: '#ff9d7a',
  上古: '#8ce8b8'
}

/* ===== 数据加载 ===== */
async function loadSummary() {
  const res = await summaryApi()
  summary.value = res.data || {}
  badges.value = (res.data && res.data.badges) || []
  const me = res.data || {}
  nickname.value = me.nickname || me.username || nickname.value
  avatarChar.value = (nickname.value || '荒')[0]
}

async function loadLists() {
  const [qs, rs] = await Promise.all([myQuestsApi(), myRealmsApi()])
  quests.value = qs.data || []
  realms.value = rs.data || []
}

function reload() {
  loading.value = true
  loadError.value = ''
  Promise.all([loadSummary(), loadLists()])
    .catch((e) => { loadError.value = e.message || '加载失败，请确认后端服务已启动' })
    .finally(() => { loading.value = false })
}

/* ===== 展示辅助 ===== */
function glyphStyle(grade) {
  const c = GRADE_COLORS[grade] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 14px ${c}22` }
}

function pointsStyle(grade) {
  const c = GRADE_COLORS[grade] || '#b3a684'
  return { color: c, textShadow: `0 0 12px ${c}66` }
}

const tierChipStyle = computed(() => ({
  color: 'var(--gold)',
  border: '1px solid rgba(255,224,138,.45)',
  background: 'rgba(255,224,138,.1)',
  boxShadow: '0 0 16px rgba(255,224,138,.2)'
}))

const tierNameStyle = computed(() => ({
  textShadow: '0 0 20px rgba(255,224,138,.45)'
}))

function fmtTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

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

  reload()
})
onBeforeUnmount(() => clearInterval(timer))

const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goRealms = () => router.push('/realms')
const goQuests = () => router.push('/quests')
const goLogout = () => { logout(); router.push('/login') }
</script>

<style scoped>
/* ===== 主体壳层（与悬赏榜同款滚动布局） ===== */
.profile-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.profile-shell::-webkit-scrollbar{width:6px}
.profile-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* 导航栏积分徽章 */
.points-chip{
  display:inline-flex;align-items:center;gap:4px;
  padding:4px 12px;border-radius:999px;font-size:12px;font-weight:700;
  color:var(--gold);background:rgba(255,224,138,.1);border:1px solid rgba(255,224,138,.35);
  letter-spacing:1px;white-space:nowrap;
}

/* ===== 首屏名帖 ===== */
.profile-hero{
  max-width:1120px;margin:0 auto 22px;text-align:center;
  padding:40px 22px 26px;position:relative;
}
.hero-deco{
  position:absolute;top:2px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.id-card{
  display:inline-flex;align-items:center;gap:22px;text-align:left;
  padding:26px 40px 26px 26px;border-radius:20px;
  background:var(--panel);border:1px solid var(--panel-line);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
  box-shadow:var(--shadow);
}
.avatar-ring{
  width:86px;height:86px;flex:none;border-radius:50%;
  padding:3px;
  background:conic-gradient(from 120deg,var(--gold),rgba(140,232,184,.5),var(--gold));
}
.avatar-big{
  width:100%;height:100%;border-radius:50%;
  display:flex;align-items:center;justify-content:center;
  font-size:36px;font-weight:800;color:var(--gold);
  background:rgba(26,21,12,.92);
}
.id-info h2{
  font-size:24px;letter-spacing:4px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.id-meta{margin-top:6px;font-size:12px;color:var(--muted);letter-spacing:1px;display:flex;gap:8px;align-items:center}
.id-meta i{opacity:.5;font-style:normal}
.id-chips{display:flex;gap:10px;margin-top:12px}
.tier-chip,.stat-chip{
  display:inline-flex;align-items:center;gap:4px;
  padding:4px 14px;border-radius:999px;font-size:12px;font-weight:700;letter-spacing:2px;
}
.stat-chip{color:var(--cyan);border:1px solid rgba(140,232,184,.4);background:rgba(140,232,184,.08)}

/* ===== 统计条 ===== */
.stat-zone{
  max-width:1120px;margin:0 auto 20px;
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
.stat-card.violet b{background:linear-gradient(90deg,#fff,#c9a7ff);-webkit-background-clip:text;background-clip:text}
.badge-total{font-style:normal;font-size:13px;opacity:.55;margin-left:2px}
.stat-card span{display:block;margin-top:4px;font-size:12px;color:var(--muted);letter-spacing:2px}

/* ===== 双列布局 ===== */
.columns{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:360px 1fr;gap:18px;align-items:start;
}
.col-left,.col-right{display:flex;flex-direction:column;gap:18px;min-width:0}

/* 面板容器（width:100% 覆盖全局 .panel 的 420px 定宽，避免压到右列） */
.panel{
  width:100%;
  padding:20px 20px 18px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
}
.panel-title{
  font-size:14px;letter-spacing:3px;font-weight:700;color:var(--gold);
  margin-bottom:14px;
}

/* ===== 修炼境界 ===== */
.tier-now{display:flex;align-items:baseline;gap:12px;flex-wrap:wrap;margin-bottom:12px}
.tier-name{
  font-size:34px;font-weight:800;letter-spacing:6px;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.tier-sub{font-size:12px;color:var(--muted);letter-spacing:1px}
.tier-bar{
  height:12px;border-radius:999px;overflow:hidden;
  background:rgba(255,255,255,.06);border:1px solid rgba(255,214,130,.18);
}
.tier-fill{
  height:100%;border-radius:999px;
  background:linear-gradient(90deg,var(--gold),var(--gold-2));
  box-shadow:0 0 14px rgba(224,168,60,.5);
  transition:width .8s cubic-bezier(.22,1,.36,1);
}
.tier-scale{
  display:flex;justify-content:space-between;margin-top:8px;
  font-size:11px;color:var(--muted);letter-spacing:1px;
}

/* ===== 成就徽章 ===== */
.badge-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:12px}
.badge-item{
  padding:14px 12px;border-radius:14px;text-align:center;
  background:rgba(255,224,138,.06);border:1px solid rgba(255,224,138,.3);
  transition:.25s;
}
.badge-item:hover{transform:translateY(-2px);box-shadow:0 10px 24px rgba(255,224,138,.12)}
.badge-item .badge-icon{display:block;font-size:26px;margin-bottom:6px;filter:drop-shadow(0 0 8px rgba(255,224,138,.5))}
.badge-item b{display:block;font-size:13px;letter-spacing:2px;color:var(--gold)}
.badge-item p{margin-top:5px;font-size:11px;color:var(--muted);letter-spacing:.5px}
.badge-item.locked{background:rgba(255,255,255,.03);border-color:rgba(255,255,255,.08)}
.badge-item.locked .badge-icon{filter:grayscale(1) opacity(.4)}
.badge-item.locked b{color:var(--muted)}

/* ===== 页签行 ===== */
.tab-row{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:14px}
.tab-btn{
  padding:7px 16px;border-radius:999px;cursor:pointer;font-size:12.5px;
  color:var(--muted);background:rgba(255,255,255,.04);
  border:1px solid rgba(255,214,130,.16);
  transition:border-color .25s, box-shadow .25s, color .25s, background .25s;
}
.tab-btn .cnt{
  margin-left:6px;font-size:11px;opacity:.75;
  padding:1px 6px;border-radius:999px;background:rgba(255,255,255,.06);
}
.tab-btn:hover{color:var(--text);background:rgba(255,255,255,.07)}
.tab-btn.active{
  color:var(--gold);background:rgba(255,224,138,.1);
  border-color:rgba(255,224,138,.45);
  box-shadow:0 0 14px rgba(255,224,138,.15);
}

/* ===== 列表项 ===== */
.list-zone{display:flex;flex-direction:column;gap:10px;max-height:520px;overflow-y:auto;padding-right:4px;scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent}
.list-zone::-webkit-scrollbar{width:5px}
.list-zone::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.line-item{
  display:flex;align-items:center;gap:14px;
  padding:12px 14px;border-radius:14px;cursor:pointer;
  background:rgba(255,255,255,.03);border:1px solid rgba(255,214,130,.1);
  transition:border-color .25s, transform .25s;
  animation:lineIn .4s both;
}
@keyframes lineIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
.line-item:hover{border-color:rgba(255,224,138,.4);transform:translateX(3px)}
.line-item .glyph{
  width:44px;height:44px;flex:none;border-radius:12px;
  display:flex;align-items:center;justify-content:center;font-size:22px;user-select:none;
}
.line-main{flex:1;min-width:0}
.line-main b{
  display:block;font-size:14px;letter-spacing:1.5px;font-weight:700;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
}
.line-sub{display:block;margin-top:4px;font-size:11.5px;color:var(--muted);letter-spacing:.5px}
.line-points{font-size:17px;font-weight:800;white-space:nowrap;flex:none}
.line-time{font-size:11px;color:var(--muted);white-space:nowrap;flex:none;letter-spacing:.5px}
.act-tag{
  padding:3px 10px;border-radius:999px;font-size:11px;flex:none;
  color:#c9a7ff;border:1px solid rgba(201,167,255,.4);background:rgba(201,167,255,.08);
  letter-spacing:1px;
}
.mini-empty{padding:34px 10px;text-align:center;font-size:13px;color:var(--muted);letter-spacing:1px}
.mini-empty a{color:var(--gold);cursor:pointer;text-decoration:underline dotted}

/* ===== 错误 / 空状态 ===== */
.empty-box{
  max-width:1120px;margin:30px auto;padding:60px 20px;text-align:center;
  color:var(--muted);letter-spacing:2px;font-size:13px;
  background:var(--panel);border:1px solid var(--panel-line);border-radius:var(--radius);
}
.empty-box .big{display:block;font-size:34px;margin-bottom:12px;color:var(--gold)}
.retry-row{margin-top:14px}

/* ===== 响应式 ===== */
@media (max-width:900px){
  .columns{grid-template-columns:1fr}
}
@media (max-width:640px){
  .profile-shell{padding-top:88px}
  .id-card{flex-direction:column;text-align:center;padding:22px}
  .id-meta{justify-content:center}
  .id-chips{justify-content:center}
  .stat-zone{grid-template-columns:repeat(2,1fr)}
  .badge-grid{grid-template-columns:repeat(2,1fr)}
}
</style>
