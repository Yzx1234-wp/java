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
      <button class="nav-item active">游戏乐园</button>
      <button class="nav-item" @click="goShop">商店兑换</button>
      <button class="nav-item" @click="goCultivation">修炼境界</button>
      <button class="nav-item" @click="goFriends">好友</button>
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

  <!-- ===== 游戏乐园主体（可滚动） ===== -->
  <main class="game-shell">
    <!-- 首屏题头 -->
    <section class="game-hero">
      <div class="hero-deco">大 荒 游 园</div>
      <h2>大荒游园 · 修士试炼场</h2>
      <p class="sub">论道之余，亦可怡情 · 大荒棋局 · 五子连珠，两大棋弈任你挑选</p>
    </section>

    <!-- 游戏选择卡片 -->
    <section class="game-tabs">
      <button
        v-for="g in games"
        :key="g.key"
        class="game-tab"
        :class="{ active: activeGame === g.key }"
        :style="activeGame === g.key ? tabActiveStyle(g) : {}"
        @click="activeGame = g.key"
      >
        <span class="tab-glyph" :style="{ color: g.color, textShadow: `0 0 14px ${g.color}66` }">{{ g.glyph }}</span>
        <span class="tab-name">{{ g.name }}</span>
        <span class="tab-desc">{{ g.desc }}</span>
      </button>
    </section>

    <!-- 当前游戏 -->
    <section class="game-stage">
      <transition name="fade" mode="out-in">
        <component :is="activeComp" :key="activeGame" />
      </transition>
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { pointsApi } from '../api/quest.js'
import ChineseChess from '../components/games/ChineseChess.vue'
import Gomoku from '../components/games/Gomoku.vue'

const router = useRouter()

/* ===== 用户会话（token 失效自动退回登录页，与其他页面一致） ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const myPoints = ref(0)
const clock = ref('')
let timer = null

/* ===== 游戏目录 ===== */
const games = [
  { key: 'chess', name: '大荒棋局', glyph: '帥', color: '#ff9d7a', desc: '楚河汉界 · 一局定道' },
  { key: 'gomoku', name: '五子连珠', glyph: '⚫', color: '#9fd8ff', desc: '黑白对弈 · 五子成阵' }
]
const activeGame = ref('chess')

const activeComp = computed(() => {
  return { chess: ChineseChess, gomoku: Gomoku }[activeGame.value]
})

function tabActiveStyle(g) {
  return {
    borderColor: `${g.color}88`,
    boxShadow: `0 0 22px ${g.color}2e`,
    background: `${g.color}0d`
  }
}

/* ===== 导航跳转 ===== */
const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goRealms = () => router.push('/realms')
const goQuests = () => router.push('/quests')
const goShop = () => router.push('/shop')
const goCultivation = () => router.push('/cultivation')
const goFriends = () => router.push('/friends')
const goProfile = () => router.push('/profile')
const goLogout = () => { logout(); router.push('/login') }

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
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
/* ===== 主体壳层（与其他页面同款滚动布局） ===== */
.game-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.game-shell::-webkit-scrollbar{width:6px}
.game-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* ===== 首屏题头 ===== */
.game-hero{
  max-width:1720px;margin:0 auto 24px;text-align:center;
  padding:38px 22px 24px;
  position:relative;
}
.hero-deco{
  position:absolute;top:6px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.game-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.game-hero .sub{color:var(--muted);margin-top:10px;font-size:13px;letter-spacing:2px}
.game-hero .sub b{color:var(--gold)}

/* ===== 游戏选择卡片 ===== */
.game-tabs{
  display:flex;gap:16px;justify-content:center;flex-wrap:wrap;
  max-width:640px;margin:0 auto 20px;
}
.game-tab{
  flex:1;min-width:220px;
  display:flex;flex-direction:column;align-items:center;gap:6px;
  padding:20px 16px;border-radius:18px;cursor:pointer;
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  transition:.25s;
}
.game-tab:hover{transform:translateY(-3px)}
.tab-glyph{font-size:30px;line-height:1}
.tab-name{font-size:16px;font-weight:800;letter-spacing:3px;color:var(--text)}
.tab-desc{font-size:11.5px;color:var(--muted);letter-spacing:1px}

/* ===== 游戏舞台 ===== */
.game-stage{
  max-width:900px;margin:0 auto;
  padding:26px 20px 30px;
  border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  display:flex;align-items:flex-start;justify-content:center;
}

.fade-enter-active,.fade-leave-active{transition:opacity .2s}
.fade-enter-from,.fade-leave-to{opacity:0}
</style>
