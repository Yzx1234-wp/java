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
      <button class="nav-item" @click="goGames">游戏乐园</button>
      <button class="nav-item" @click="goShop">商店兑换</button>
      <button class="nav-item" @click="goCultivation">修炼境界</button>
      <button class="nav-item active">好友</button>
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

  <!-- ===== 交友录主体（可滚动） ===== -->
  <main class="fr-shell">
    <section class="fr-hero">
      <div class="hero-deco">大 荒 交 友 录</div>
      <h2>大荒交友录</h2>
      <p class="sub">结识道友 · 私信论道 · 更可自定积分注，约战游戏乐园一较高下，<b>胜方独得注金</b></p>
    </section>

    <div class="fr-body">
      <!-- ===== 左栏：好友 + 添加 ===== -->
      <aside class="fr-side">
        <!-- 搜索添加好友 -->
        <div class="panel">
          <h3>✦ 寻访修士</h3>
          <div class="search-line">
            <input v-model="searchKw" type="text" placeholder="输入账号或昵称检索" maxlength="32" @keyup.enter="doSearch">
            <button class="btn-mini gold" @click="doSearch">寻访</button>
          </div>
          <div v-if="searchResults.length" class="search-list">
            <div v-for="u in searchResults" :key="u.username" class="search-item">
              <span class="s-avatar">{{ (u.nickname || u.username)[0] }}</span>
              <span class="s-info">
                <b>{{ u.nickname }}</b>
                <small>@{{ u.username }}</small>
              </span>
              <span v-if="u.isFriend" class="tag-ok">已是道友</span>
              <span v-else-if="u.requestStatus === 'PENDING'" class="tag-wait">{{ u.requestDir === 'OUT' ? '待对方通过' : '对方已申请' }}</span>
              <button v-else class="btn-mini" @click="addFriend(u)">+ 结交</button>
            </div>
          </div>
          <p v-else-if="searched" class="empty-tip">未寻访到相关修士</p>
        </div>

        <!-- 收到的申请 -->
        <div class="panel">
          <h3>✦ 收到的申请 <span v-if="incoming.length" class="badge">{{ incoming.length }}</span></h3>
          <div v-if="incoming.length" class="req-list">
            <div v-for="r in incoming" :key="r.id" class="req-item">
              <span class="s-avatar">{{ (r.userNickname || r.user)[0] }}</span>
              <span class="s-info"><b>{{ r.userNickname }}</b><small>@{{ r.user }}</small></span>
              <span class="btn-group">
                <button class="btn-mini gold" @click="acceptReq(r)">通过</button>
                <button class="btn-mini danger" @click="declineReq(r)">拒绝</button>
              </span>
            </div>
          </div>
          <p v-else class="empty-tip">暂无待处理的好友申请</p>
        </div>

        <!-- 好友列表 -->
        <div class="panel grow">
          <h3>✦ 我的道友 <span class="cnt">{{ friends.length }}</span></h3>
          <div v-if="friends.length" class="friend-list">
            <div
              v-for="f in friends"
              :key="f.username"
              class="friend-item"
              :class="{ active: chatFriend === f.username }"
              @click="openChat(f)"
            >
              <span class="s-avatar">{{ (f.nickname || f.username)[0] }}</span>
              <span class="s-info">
                <b>{{ f.nickname }}</b>
                <small>@{{ f.username }}</small>
              </span>
              <span v-if="unread[f.username]" class="unread-badge">{{ unread[f.username] }}</span>
              <span class="btn-group">
                <button class="btn-mini" title="发起对战" @click.stop="prepareBattle(f)">⚔</button>
                <button class="btn-mini danger" title="删除好友" @click.stop="delFriend(f)">✕</button>
              </span>
            </div>
          </div>
          <p v-else class="empty-tip">还未结交道友，先去寻访吧</p>
        </div>
      </aside>

      <!-- ===== 中栏：聊天 ===== -->
      <section class="panel chat-panel">
        <template v-if="chatFriend">
          <h3 class="chat-title">
            与 <b>{{ chatFriendNickname }}</b> 论道
            <small v-if="chatBattleOf" class="chat-battle-tag">⚔ 对战进行中 · 注金 {{ chatBattleOf.stake }}</small>
          </h3>
          <div ref="chatBox" class="chat-box">
            <div v-for="m in messages" :key="m.id" class="chat-msg" :class="{ mine: m.mine }">
              <span class="msg-bubble">{{ m.content }}</span>
              <small class="msg-time">{{ m.createdAt }}</small>
            </div>
            <p v-if="!messages.length" class="empty-tip">还没有消息，打个招呼吧</p>
          </div>
          <div class="chat-input">
            <input v-model="chatDraft" type="text" placeholder="输入消息…（回车发送）" maxlength="500" @keyup.enter="sendMsg">
            <button class="btn-mini gold" @click="sendMsg">发送</button>
          </div>
        </template>
        <div v-else class="chat-empty">
          <span class="big">❊</span>
          <p>在左侧选择一位道友，开始论道</p>
        </div>
      </section>

      <!-- ===== 右栏：对战 ===== -->
      <aside class="fr-side">
        <div class="panel">
          <h3>✦ 发起对战</h3>
          <template v-if="battleForm.friend">
            <p class="battle-foe">对手 · <b>{{ battleForm.friendNickname }}</b>（@{{ battleForm.friend }}）</p>
            <div class="form-row">
              <span class="label">项目</span>
              <button class="btn-mini" :class="{ gold: battleForm.game === 'CHESS' }" @click="battleForm.game = 'CHESS'">象棋</button>
              <button class="btn-mini" :class="{ gold: battleForm.game === 'GOMOKU' }" @click="battleForm.game = 'GOMOKU'">五子棋</button>
            </div>
            <div class="form-row">
              <span class="label">注金</span>
              <input v-model.number="battleForm.stake" type="number" min="1" class="stake-input" placeholder="自定义积分注">
            </div>
            <p class="form-tip">可用积分 {{ myPoints }} · 双方各押注金，<b>胜方净得注金</b>，负方扣除</p>
            <div class="btn-row">
              <button class="btn-mini gold" @click="createBattle">⚔ 发出战书</button>
              <button class="btn-mini" @click="battleForm.friend = null">取消</button>
            </div>
          </template>
          <p v-else class="empty-tip">点击好友列表中的 ⚔ 按钮发起对战</p>
        </div>

        <div class="panel grow">
          <h3>✦ 战书与战况</h3>
          <div v-if="battles.length" class="battle-list">
            <div v-for="b in battles" :key="b.id" class="battle-item">
              <div class="battle-head">
                <span class="game-tag" :class="b.game === 'CHESS' ? 'chess' : 'gomoku'">{{ b.game === 'CHESS' ? '象棋' : '五子棋' }}</span>
                <span class="stake-tag">注金 {{ b.stake }}</span>
                <span class="status-tag" :class="b.status.toLowerCase()">{{ statusLabel(b) }}</span>
              </div>
              <p class="battle-line">
                {{ b.challengerNickname }} (先手) vs {{ b.opponentNickname }}
                <template v-if="b.status === 'FINISHED'"> · <b :class="b.iWon ? 'win-txt' : 'lose-txt'">{{ b.iWon ? '你胜' : '你负' }}</b></template>
              </p>
              <div class="btn-row" v-if="b.status === 'REQUESTED'">
                <template v-if="b.myRole === 'opponent'">
                  <button class="btn-mini gold" @click="acceptBattle(b)">应战</button>
                  <button class="btn-mini danger" @click="declineBattle(b)">拒战</button>
                </template>
                <template v-else>
                  <button class="btn-mini danger" @click="declineBattle(b)">撤回战书</button>
                  <small class="wait-tip">等待对方应战…</small>
                </template>
              </div>
              <div class="btn-row" v-else-if="b.status === 'READY'">
                <button class="btn-mini gold" @click="enterBattle(b)">⚔ 进入对战</button>
                <small class="wait-tip">对战中 · 每 2 秒自动同步局面</small>
              </div>
            </div>
          </div>
          <p v-else class="empty-tip">还没有对战记录</p>
        </div>
      </aside>
    </div>

    <!-- ===== 对战竞技场弹层（Teleport 到 body：脱离 fr-shell 层叠上下文，否则 topnav 会压在弹层上） ===== -->
    <Teleport to="body">
      <transition name="fade">
        <div v-if="arenaBattle" class="arena-mask" @click.self="leaveArena">
          <div class="arena-panel">
            <div class="arena-head">
              <h3>
                ⚔ {{ arenaBattle.game === 'CHESS' ? '象棋' : '五子棋' }}对战 · 注金 <b>{{ arenaBattle.stake }}</b>
                <small>{{ arenaBattle.challengerNickname }} (先手) vs {{ arenaBattle.opponentNickname }}</small>
              </h3>
              <button class="btn-mini" @click="leaveArena">收起（对战仍在进行）</button>
            </div>
            <div class="arena-stage">
              <ChineseChess v-if="arenaBattle.game === 'CHESS'" :battle-ctx="arenaCtx" />
              <Gomoku v-else :battle-ctx="arenaCtx" />
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { pointsApi } from '../api/quest.js'
import ChineseChess from '../components/games/ChineseChess.vue'
import Gomoku from '../components/games/Gomoku.vue'
import {
  acceptBattleApi, acceptFriendRequestApi, battleDetailApi, battleListApi,
  chatHistoryApi, chatUnreadApi, createBattleApi, declineBattleApi,
  declineFriendRequestApi, friendListApi, friendRequestsApi, removeFriendApi,
  searchUsersApi, sendChatApi, sendFriendRequestApi
} from '../api/friend.js'

const router = useRouter()
const { show } = useToast()

/* ===== 用户会话 ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const myPoints = ref(0)
const clock = ref('')
let timer = null

/* ===== 好友 / 申请 / 搜索 ===== */
const friends = ref([])
const incoming = ref([])
const outgoing = ref([])
const searchKw = ref('')
const searched = ref('')
const searchResults = ref([])
const unread = ref({})

/* ===== 聊天 ===== */
const chatFriend = ref(null)
const chatFriendNickname = ref('')
const messages = ref([])
const chatDraft = ref('')
const chatBox = ref(null)
const chatMaxId = ref(0)
let chatTimer = null

/* ===== 对战 ===== */
const battles = ref([])
const battleForm = ref({ friend: null, friendNickname: '', game: 'CHESS', stake: 100 })
const arenaBattle = ref(null)     // 当前进入的对战（详情 VO）
const arenaCtx = computed(() => arenaBattle.value
  ? { id: arenaBattle.value.id, myRole: arenaBattle.value.myRole }
  : null)

const chatBattleOf = computed(() =>
  battles.value.find((b) => b.status === 'READY'
    && (b.challenger === chatFriend.value || b.opponent === chatFriend.value)))

/* ===== 数据加载 ===== */
async function loadPoints() {
  try {
    const res = await pointsApi()
    myPoints.value = (res.data && res.data.points) || 0
  } catch { myPoints.value = 0 }
}

async function loadFriends() {
  try {
    const res = await friendListApi()
    friends.value = res.data || []
  } catch { friends.value = [] }
}

async function loadRequests() {
  try {
    const res = await friendRequestsApi()
    incoming.value = (res.data && res.data.incoming) || []
    outgoing.value = (res.data && res.data.outgoing) || []
  } catch { incoming.value = []; outgoing.value = [] }
}

async function loadUnread() {
  try {
    const res = await chatUnreadApi()
    unread.value = res.data || {}
  } catch { unread.value = {} }
}

async function loadBattles() {
  try {
    const res = await battleListApi()
    battles.value = res.data || []
  } catch { battles.value = [] }
}

async function reloadAll() {
  await Promise.all([loadFriends(), loadRequests(), loadUnread(), loadBattles(), loadPoints()])
}

/* ===== 搜索与申请 ===== */
async function doSearch() {
  const k = searchKw.value.trim()
  if (!k) { show('请输入账号或昵称再寻访', 'ok'); return }
  searched.value = k
  try {
    const res = await searchUsersApi(k)
    searchResults.value = res.data || []
  } catch (e) { show(e.message, 'err') }
}

async function addFriend(u) {
  try {
    await sendFriendRequestApi(u.username)
    show(`已向 ${u.nickname} 递上交友名帖，静候回应`, 'ok')
    await doSearch()
    await loadRequests()
  } catch (e) { show(e.message, 'err') }
}

async function acceptReq(r) {
  try {
    await acceptFriendRequestApi(r.id)
    show(`已与 ${r.userNickname} 结为道友`, 'ok')
    await reloadAll()
  } catch (e) { show(e.message, 'err') }
}

async function declineReq(r) {
  try {
    await declineFriendRequestApi(r.id)
    show('已婉拒该申请', 'ok')
    await loadRequests()
  } catch (e) { show(e.message, 'err') }
}

async function delFriend(f) {
  if (!window.confirm(`确定与 ${f.nickname} 绝交吗？聊天记录将保留但无法继续私聊与对战。`)) return
  try {
    await removeFriendApi(f.username)
    if (chatFriend.value === f.username) closeChat()
    show('已绝交', 'ok')
    await reloadAll()
  } catch (e) { show(e.message, 'err') }
}

/* ===== 聊天 ===== */
async function openChat(f) {
  chatFriend.value = f.username
  chatFriendNickname.value = f.nickname || f.username
  messages.value = []
  chatMaxId.value = 0
  await pollChat(true)
  clearInterval(chatTimer)
  chatTimer = setInterval(() => pollChat(false), 2500)
}

function closeChat() {
  chatFriend.value = null
  messages.value = []
  clearInterval(chatTimer)
}

async function pollChat(reset) {
  if (!chatFriend.value) return
  try {
    const res = await chatHistoryApi(chatFriend.value, reset ? 0 : chatMaxId.value)
    const list = res.data || []
    if (reset) {
      messages.value = list
    } else if (list.length) {
      const known = new Set(messages.value.map((m) => m.id))
      for (const m of list) if (!known.has(m.id)) messages.value.push(m)
    }
    if (messages.value.length) {
      chatMaxId.value = Math.max(chatMaxId.value, ...messages.value.map((m) => m.id))
      await nextTick()
      if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
    }
    unread.value[chatFriend.value] = 0
    loadUnread()
  } catch { /* 轮询失败静默 */ }
}

async function sendMsg() {
  const content = chatDraft.value.trim()
  if (!content) return
  try {
    await sendChatApi(chatFriend.value, content)
    chatDraft.value = ''
    await pollChat(false)
  } catch (e) { show(e.message, 'err') }
}

/* ===== 对战 ===== */
function prepareBattle(f) {
  battleForm.value = { friend: f.username, friendNickname: f.nickname || f.username, game: 'CHESS', stake: 100 }
}

async function createBattle() {
  const f = battleForm.value
  const stake = Number(f.stake)
  if (!stake || stake <= 0 || !Number.isInteger(stake)) {
    show('积分注必须为正整数', 'err'); return
  }
  if (stake > myPoints.value) {
    show('你的可用积分不足', 'err'); return
  }
  try {
    await createBattleApi(f.friend, f.game, stake)
    show(`战书已送达 ${f.friendNickname}，静候应战`, 'ok')
    battleForm.value.friend = null
    await loadBattles()
    await loadPoints()
  } catch (e) { show(e.message, 'err') }
}

async function acceptBattle(b) {
  try {
    await acceptBattleApi(b.id)
    show(`已应战，与 ${b.counterpartNickname} 的对决开始！`, 'ok')
    await loadBattles()
    enterBattle(b)
  } catch (e) { show(e.message, 'err') }
}

async function declineBattle(b) {
  try {
    await declineBattleApi(b.id)
    show(b.myRole === 'challenger' ? '战书已撤回' : '已拒战', 'ok')
    await loadBattles()
  } catch (e) { show(e.message, 'err') }
}

async function enterBattle(b) {
  try {
    const res = await battleDetailApi(b.id)
    arenaBattle.value = res.data
  } catch (e) { show(e.message, 'err') }
}

function leaveArena() {
  arenaBattle.value = null
  loadBattles()
  loadPoints()
}

function statusLabel(b) {
  return { REQUESTED: '待应战', READY: '对战中', FINISHED: '已结算', CANCELLED: '已取消' }[b.status] || b.status
}

/* ===== 导航跳转 ===== */
const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goRealms = () => router.push('/realms')
const goQuests = () => router.push('/quests')
const goGames = () => router.push('/games')
const goShop = () => router.push('/shop')
const goCultivation = () => router.push('/cultivation')
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

  const me = await fetchMe()
  if (!me) {
    logout()
    router.push('/login')
    return
  }
  nickname.value = me.nickname || me.username || nickname.value
  avatarChar.value = (nickname.value || '荒')[0]

  await reloadAll()
  // 对战列表轮询：战书状态变化（对方应战/结算）实时可见
  setInterval(() => { loadBattles(); loadUnread(); loadPoints() }, 5000)
})
onBeforeUnmount(() => {
  clearInterval(timer)
  clearInterval(chatTimer)
})
</script>

<style scoped>
/* ===== 主体壳层（与其他页面同款滚动布局） ===== */
.fr-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.fr-shell::-webkit-scrollbar{width:6px}
.fr-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* ===== 首屏题头 ===== */
.fr-hero{
  max-width:1720px;margin:0 auto 22px;text-align:center;
  padding:34px 22px 20px;position:relative;
}
.hero-deco{
  position:absolute;top:6px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.fr-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.fr-hero .sub{color:var(--muted);margin-top:10px;font-size:13px;letter-spacing:2px}
.fr-hero .sub b{color:var(--gold)}

/* ===== 三栏主体 ===== */
.fr-body{
  max-width:1560px;margin:0 auto;
  display:grid;grid-template-columns:330px minmax(0,1fr) 340px;gap:18px;
  align-items:stretch;
}
.panel{
  padding:18px 16px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  display:flex;flex-direction:column;gap:10px;min-width:0;
}
.panel.grow{flex:1;min-height:220px}
.panel h3{font-size:14.5px;letter-spacing:2px;color:var(--gold);font-weight:700;display:flex;align-items:center;gap:8px}
.panel h3 .badge{
  padding:1px 8px;border-radius:999px;font-size:11px;
  color:#1a1305;background:linear-gradient(135deg,var(--gold),var(--gold-2));
}
.panel h3 .cnt{font-size:12px;color:var(--muted);font-weight:400}

/* ===== 搜索 ===== */
.search-line{display:flex;gap:8px}
.search-line input{
  flex:1;height:36px;padding:0 12px;border-radius:10px;min-width:0;
  border:1px solid var(--panel-line);outline:0;
  background:rgba(255,255,255,.04);color:var(--text);font-size:13px;
}
.search-line input:focus{border-color:rgba(255,224,138,.5)}
.btn-mini{
  padding:7px 12px;border-radius:10px;cursor:pointer;font-size:12px;
  letter-spacing:1px;font-weight:600;color:var(--gold);
  background:rgba(255,224,138,.08);border:1px solid rgba(255,224,138,.3);
  transition:.2s;white-space:nowrap;
}
.btn-mini:hover{background:rgba(255,224,138,.18)}
.btn-mini.gold{
  color:#1a1305;background:linear-gradient(135deg,var(--gold),var(--gold-2));
  border:0;box-shadow:0 6px 16px rgba(224,168,60,.3);
}
.btn-mini.danger{color:var(--danger);border-color:rgba(255,107,129,.4);background:rgba(255,107,129,.08)}
.btn-mini.danger:hover{background:rgba(255,107,129,.18)}
.btn-group{display:flex;gap:6px;margin-left:auto}

.search-list,.req-list,.friend-list,.battle-list{display:flex;flex-direction:column;gap:8px;overflow-y:auto;scrollbar-width:thin}
.search-item,.req-item,.friend-item{
  display:flex;align-items:center;gap:10px;
  padding:8px 10px;border-radius:12px;
  background:rgba(255,255,255,.03);border:1px solid rgba(255,214,130,.1);
}
.friend-item{cursor:pointer;transition:.2s}
.friend-item:hover{background:rgba(255,224,138,.08)}
.friend-item.active{border-color:rgba(255,224,138,.55);background:rgba(255,224,138,.1)}
.s-avatar{
  flex:none;width:34px;height:34px;border-radius:50%;
  display:flex;align-items:center;justify-content:center;
  font-size:15px;font-weight:800;color:var(--gold);
  background:rgba(255,224,138,.12);border:1px solid rgba(255,224,138,.35);
}
.s-info{display:flex;flex-direction:column;gap:1px;min-width:0;flex:1}
.s-info b{font-size:13px;color:var(--text);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.s-info small{font-size:11px;color:var(--muted)}
.tag-ok,.tag-wait{font-size:11px;color:var(--cyan);white-space:nowrap}
.tag-wait{color:var(--muted)}
.unread-badge{
  flex:none;min-width:20px;height:20px;padding:0 6px;border-radius:999px;
  display:flex;align-items:center;justify-content:center;
  font-size:11px;font-weight:700;color:#1a1305;
  background:linear-gradient(135deg,var(--danger),#ff9d7a);
}
.empty-tip{font-size:12px;color:var(--muted);letter-spacing:1px;padding:6px 2px}

/* ===== 聊天 ===== */
.chat-panel{min-height:420px}
.chat-title{display:flex;flex-direction:column;gap:4px}
.chat-title small{font-size:11.5px;color:var(--cyan);font-weight:400;letter-spacing:1px}
.chat-battle-tag{color:var(--cyan)}
.chat-box{
  flex:1;overflow-y:auto;display:flex;flex-direction:column;gap:10px;
  padding:12px;border-radius:14px;min-height:300px;
  background:rgba(255,255,255,.02);border:1px solid rgba(255,214,130,.08);
  scrollbar-width:thin;
}
.chat-msg{display:flex;flex-direction:column;align-items:flex-start;gap:2px;max-width:78%}
.chat-msg.mine{align-self:flex-end;align-items:flex-end}
.msg-bubble{
  padding:8px 13px;border-radius:14px;font-size:13px;line-height:1.6;
  color:var(--text);background:rgba(255,255,255,.06);
  border:1px solid rgba(255,214,130,.14);
  word-break:break-all;white-space:pre-wrap;
}
.chat-msg.mine .msg-bubble{
  color:#1a1305;background:linear-gradient(135deg,var(--gold),var(--gold-2));
  border:0;
}
.msg-time{font-size:10px;color:rgba(179,166,132,.6)}
.chat-input{display:flex;gap:8px}
.chat-input input{
  flex:1;height:38px;padding:0 12px;border-radius:10px;min-width:0;
  border:1px solid var(--panel-line);outline:0;
  background:rgba(255,255,255,.04);color:var(--text);font-size:13px;
}
.chat-input input:focus{border-color:rgba(255,224,138,.5)}
.chat-empty{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:10px;color:var(--muted)}
.chat-empty .big{font-size:34px;color:rgba(255,224,138,.4)}

/* ===== 对战表单 ===== */
.battle-foe{font-size:12.5px;color:var(--muted);letter-spacing:1px}
.battle-foe b{color:var(--gold)}
.form-row{display:flex;align-items:center;gap:8px}
.form-row .label{font-size:12px;color:var(--muted);letter-spacing:2px;flex:none}
.stake-input{
  flex:1;height:34px;padding:0 10px;border-radius:10px;min-width:0;
  border:1px solid var(--panel-line);outline:0;
  background:rgba(255,255,255,.04);color:var(--text);font-size:13px;
}
.stake-input:focus{border-color:rgba(255,224,138,.5)}
.form-tip{font-size:11.5px;color:var(--muted);letter-spacing:.5px;line-height:1.7}
.form-tip b{color:var(--gold)}
.btn-row{display:flex;align-items:center;gap:8px;flex-wrap:wrap}
.wait-tip{font-size:11px;color:var(--muted)}

/* ===== 对战列表 ===== */
.battle-item{
  display:flex;flex-direction:column;gap:8px;
  padding:10px 12px;border-radius:12px;
  background:rgba(255,255,255,.03);border:1px solid rgba(255,214,130,.1);
}
.battle-head{display:flex;align-items:center;gap:8px;flex-wrap:wrap}
.game-tag,.stake-tag,.status-tag{
  padding:2px 9px;border-radius:999px;font-size:11px;letter-spacing:1px;
}
.game-tag.chess{color:var(--violet);border:1px solid rgba(255,157,122,.4);background:rgba(255,157,122,.1)}
.game-tag.gomoku{color:#9fd8ff;border:1px solid rgba(159,216,255,.4);background:rgba(159,216,255,.1)}
.stake-tag{color:var(--gold);border:1px solid rgba(255,224,138,.4);background:rgba(255,224,138,.08)}
.status-tag{color:var(--muted);border:1px solid rgba(179,166,132,.35);background:rgba(179,166,132,.08)}
.status-tag.ready{color:var(--cyan);border-color:rgba(140,232,184,.4);background:rgba(140,232,184,.1)}
.status-tag.finished{color:var(--gold);border-color:rgba(255,224,138,.4);background:rgba(255,224,138,.1)}
.battle-line{font-size:12.5px;color:var(--muted);letter-spacing:.5px}
.win-txt{color:var(--ok)}
.lose-txt{color:var(--danger)}

/* ===== 竞技场弹层 ===== */
.arena-mask{
  position:fixed;inset:0;z-index:60;
  background:rgba(10,6,0,.72);backdrop-filter:blur(6px);-webkit-backdrop-filter:blur(6px);
  display:flex;align-items:center;justify-content:center;padding:20px;
}
.arena-panel{
  width:min(880px,100%);max-height:92vh;overflow-y:auto;
  padding:20px 20px 24px;border-radius:20px;
  background:rgba(26,21,12,.97);border:1px solid rgba(255,224,138,.35);
  box-shadow:0 24px 70px rgba(10,6,0,.7), 0 0 40px rgba(255,224,138,.12);
}
.arena-head{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:14px;flex-wrap:wrap}
.arena-head h3{
  font-size:17px;letter-spacing:2px;color:var(--gold);
  display:flex;flex-direction:column;gap:4px;
}
.arena-head h3 small{font-size:11.5px;color:var(--muted);font-weight:400;letter-spacing:1px}
.arena-head h3 b{color:var(--gold-2)}
.arena-stage{display:flex;justify-content:center}

.fade-enter-active,.fade-leave-active{transition:opacity .25s}
.fade-enter-from,.fade-leave-to{opacity:0}

@media (max-width:1200px){
  .fr-body{grid-template-columns:1fr 1fr}
  .chat-panel{grid-column:1 / -1;order:-1;min-height:340px}
}
@media (max-width:760px){
  .fr-body{grid-template-columns:1fr}
}
</style>
