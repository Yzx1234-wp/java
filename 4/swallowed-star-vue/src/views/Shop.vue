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
      <button class="nav-item active">商店兑换</button>
      <button class="nav-item" @click="goCultivation">修炼境界</button>
      <button class="nav-item" @click="goProfile">我的主页</button>
    </div>

    <span class="spacer"></span>
    <span class="clock">{{ clock }}</span>

    <div class="nav-user">
      <span class="avatar">{{ avatarChar }}</span>
      <span class="name">{{ nickname }}</span>
      <span class="points-chip" title="可用积分 · 点此查看我的主页" @click="router.push('/profile')">✦ {{ wallet.available }}</span>
      <button class="btn-exit" @click="goLogout">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>
        </svg>
        退出
      </button>
    </div>
  </nav>

  <!-- ===== 宝库主体（可滚动） ===== -->
  <main class="shop-shell">
    <!-- 首屏：宝库题头 + 搜索栏 -->
    <section class="shop-hero">
      <div class="hero-deco">大 荒 宝 库</div>
      <h2>大荒宝库</h2>
      <p class="sub">丹药续命 · 法宝护身 · 武器争锋 · 高级功法传世 · 积分兑换，不问出身 · 库中现有 <b>{{ total }}</b> 件宝物</p>

      <div class="search-wrap">
        <span class="ico">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
        </span>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索 丹药 / 法宝 / 武器 / 功法 …"
          maxlength="40"
          @keyup.enter="doSearch"
        >
        <button class="btn-search" @click="doSearch">寻 宝</button>
      </div>

      <div v-if="hotNames.length" class="hot-row">
        <span class="tag">热门宝物</span>
        <button v-for="w in hotNames" :key="w" class="hot-chip" @click="pickHot(w)">{{ w }}</button>
      </div>
    </section>

    <!-- 积分钱包 -->
    <section class="wallet-zone">
      <div class="wallet-card">
        <span class="w-label">可用积分</span>
        <b>{{ wallet.available }}</b>
        <span class="w-sub">累计获得 {{ wallet.earned }} · 已消耗 {{ wallet.spent }}</span>
      </div>
      <div class="stat-zone">
        <div class="stat-card jade">
          <b>{{ wallet.orderCount }}</b>
          <span>我的兑换次数</span>
        </div>
        <div class="stat-card">
          <b>{{ wallet.earned }}</b>
          <span>历史累计积分</span>
        </div>
        <div class="stat-card">
          <b>{{ wallet.spent }}</b>
          <span>宝库已花积分</span>
        </div>
      </div>
    </section>

    <!-- 筛选区 -->
    <section class="filter-zone">
      <div class="filter-row">
        <span class="label">品类</span>
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
    </section>

    <!-- 列表头 -->
    <div class="result-head">
      <h3 v-if="searched">检索「<b>{{ searched }}</b>」 · 得 <b>{{ list.length }}</b> 件宝物</h3>
      <h3 v-else>宝库总览 · 共 <b>{{ list.length }}</b> 件</h3>
      <span>{{ loading ? '正在开库清点…' : '数据来源 · 后端宝库' }}</span>
    </div>

    <!-- 商品卡片 -->
    <div v-if="list.length" class="shop-grid">
      <article
        v-for="(item, i) in list"
        :key="item.id || item.name"
        class="shop-card"
        :class="{ soldout: item.soldOut }"
        :style="{ animationDelay: (i * 0.04) + 's' }"
        @click="openItem(item)"
      >
        <span class="grade-badge" :style="badgeStyle(item)">{{ item.grade }}</span>

        <div class="shop-top">
          <div class="glyph" :style="glyphStyle(item)">{{ item.glyph }}</div>
          <div class="shop-title">
            <h4>{{ item.name }}</h4>
            <span class="cat" :style="catStyle(item)">{{ item.category }}</span>
          </div>
        </div>

        <p class="desc">{{ item.desc }}</p>

        <div class="shop-meta">
          <span v-if="item.stock > 0" :class="{ warn: item.remain <= 1 }">限量 · 余 <b>{{ item.remain }}</b> 件</span>
          <span v-else>不限量</span>
          <span v-if="item.owned > 0" class="own-tag">已兑换 {{ item.owned }} 件</span>
        </div>

        <div class="card-foot">
          <div class="price" :style="priceStyle(item)">✦ {{ item.price }}<i>积分</i></div>
          <button
            class="btn-redeem"
            :class="{ disabled: item.soldOut || !item.affordable }"
            :disabled="item.soldOut || !item.affordable"
            @click.stop="openItem(item)"
          >{{ actionText(item) }}</button>
        </div>
      </article>
    </div>

    <div v-else-if="loading" class="empty-box">
      <span class="big">✧</span>
      正在开库清点…
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
      未检索到「{{ searched }}」相关宝物，试试「丹药」「飞仙石」「功法」等关键词
    </div>

    <!-- 我的兑换记录 -->
    <section class="order-zone">
      <div class="order-head">
        <h3>✦ 我的兑换记录</h3>
        <span v-if="orders.length">共 {{ orders.length }} 笔 · 累计花费 {{ orders.reduce((s, o) => s + (o.price || 0), 0) }} 积分</span>
        <span v-else>尚无兑换记录</span>
      </div>

      <div v-if="orders.length" class="order-list">
        <div v-for="o in orders" :key="o.id" class="order-item">
          <span class="o-icon">{{ CAT_ICONS[o.category] || '🎁' }}</span>
          <div class="o-main">
            <b>{{ o.itemName }}</b>
            <span class="o-sub">{{ o.category }} · {{ o.createdAt }}</span>
          </div>
          <span class="o-price">-{{ o.price }} 积分</span>
        </div>
      </div>
      <p v-else class="order-empty">去上方宝库挑一件，兑换后会在此留下记录。</p>
    </section>

    <!-- ===== 兑换确认弹层 ===== -->
    <transition name="fade">
      <div v-if="confirmItem" class="detail-mask" @click.self="confirmItem = null">
        <div class="detail-panel" :style="detailBorderStyle(confirmItem)">
          <button class="detail-close" @click="confirmItem = null">✕</button>

          <div class="detail-head">
            <div class="glyph big" :style="glyphStyle(confirmItem)">{{ confirmItem.glyph }}</div>
            <div>
              <h3>{{ confirmItem.name }}<span class="grade-badge inline" :style="badgeStyle(confirmItem)">{{ confirmItem.grade }}</span></h3>
              <p class="detail-loc">{{ confirmItem.category }} · {{ catTip(confirmItem.category) }}</p>
            </div>
            <div class="price big" :style="priceStyle(confirmItem)">✦ {{ confirmItem.price }}<i>积分</i></div>
          </div>

          <div class="detail-rows">
            <div class="row"><span>所需积分</span><b>{{ confirmItem.price }} 积分</b></div>
            <div class="row"><span>我的可用积分</span><b>{{ wallet.available }} 积分</b></div>
            <div class="row"><span>兑换后余额</span><b>{{ wallet.available - confirmItem.price }} 积分</b></div>
            <div class="row">
              <span>库存</span>
              <b v-if="confirmItem.stock > 0">限量 {{ confirmItem.stock }} 件 · 余 {{ confirmItem.remain }} 件</b>
              <b v-else>不限量</b>
            </div>
            <div v-if="confirmItem.owned > 0" class="row"><span>我已拥有</span><b>{{ confirmItem.owned }} 件</b></div>
          </div>

          <p class="detail-desc">{{ confirmItem.desc }}</p>

          <button
            class="btn-redeem full"
            :class="{ disabled: confirmItem.soldOut || !confirmItem.affordable }"
            :disabled="redeeming || confirmItem.soldOut || !confirmItem.affordable"
            @click="doRedeem(confirmItem)"
          >{{ redeeming ? '兑换中…' : actionText(confirmItem, true) }}</button>

          <div class="detail-foot">—— 《大荒宝库》 ——</div>
        </div>
      </div>
    </transition>
  </main>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSession, fetchMe, logout } from '../composables/useAuth.js'
import { useToast } from '../composables/useToast.js'
import { listApi, categoriesApi, walletApi, ordersApi, redeemApi } from '../api/shop.js'

const router = useRouter()
const { show } = useToast()

/* ===== 用户会话 ===== */
const session = getSession()
const nickname = ref((session && (session.nickname || session.username)) || '无名修士')
const avatarChar = ref((nickname.value || '荒')[0])
const clock = ref('')
let timer = null

/* ===== 配色字典 ===== */
const CAT_COLORS = {
  丹药: '#8ce8b8',
  法宝: '#c9a3ff',
  武器: '#ff9d7a',
  高级功法: '#ffe08a'
}
const CAT_ICONS = {
  丹药: '🧪',
  法宝: '💎',
  武器: '⚔️',
  高级功法: '📜'
}
const CAT_TIPS = {
  丹药: '疗伤续命 · 辅助修行',
  法宝: '护道之器 · 妙用无穷',
  武器: '争锋杀伐 · 一寸长一寸强',
  高级功法: '传世绝学 · 可于功法首页细读'
}
const GRADE_COLORS = {
  绝世: '#ffe08a',
  绝顶: '#ff9d7a',
  一流: '#9fd8ff',
  上乘: '#8ce8b8',
  中乘: '#b3a684'
}

/* ===== 排序方式 ===== */
const sorts = [
  { key: 'default', label: '默认' },
  { key: 'priceAsc', label: '积分由低到高' },
  { key: 'priceDesc', label: '积分由高到低' }
]

/* ===== 状态 ===== */
const ALL = '全部'
const loading = ref(true)
const loadError = ref('')
const total = ref(0)
const categories = ref([{ key: ALL, label: ALL, count: 0 }])
const hotNames = ref([])
const list = ref([])
const wallet = ref({ available: 0, earned: 0, spent: 0, orderCount: 0 })
const orders = ref([])
const confirmItem = ref(null)
const redeeming = ref(false)

const keyword = ref('')
const searched = ref('')
const activeCat = ref(ALL)
const sortBy = ref('default')

let booted = false
let hotLocked = false

/* 弹层按钮文案 */
const actionText = (item, inModal = false) => {
  if (item.soldOut) return '已被兑完'
  if (!item.affordable) return '积分不足'
  return inModal ? '确认兑换' : '兑 换'
}

/* ===== 数据加载 ===== */
async function loadCategories() {
  try {
    const res = await categoriesApi()
    const cd = res.data || []
    categories.value = cd.length ? cd : [{ key: ALL, label: ALL, count: 0 }]
    if (cd.length) total.value = cd[0].count
  } catch (e) {
    console.warn('宝库分类加载失败', e)
  }
}

async function loadList() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await listApi({
      keyword: searched.value,
      category: activeCat.value === ALL ? undefined : activeCat.value,
      sortBy: sortBy.value
    })
    list.value = res.data || []
    // 首次无筛选的列表用来生成「热门宝物」快捷入口
    if (!hotLocked) {
      hotNames.value = list.value.filter(i => i.hot).slice(0, 6).map(i => i.name)
      hotLocked = true
    }
  } catch (e) {
    loadError.value = e.message || '宝库检索失败，请确认后端服务已启动'
    list.value = []
  } finally {
    loading.value = false
  }
}

async function loadWallet() {
  try {
    const res = await walletApi()
    wallet.value = res.data || wallet.value
  } catch (e) {
    console.warn('积分钱包加载失败', e)
  }
}

async function loadOrders() {
  try {
    const res = await ordersApi()
    orders.value = res.data || []
  } catch (e) {
    console.warn('兑换记录加载失败', e)
  }
}

function reload() {
  loadCategories()
  loadList()
  loadWallet()
  loadOrders()
}

/* ===== 交互 ===== */
function doSearch() {
  const k = keyword.value.trim()
  if (!k) {
    show('请输入关键词，当前展示全部宝物', 'ok')
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

function openItem(item) {
  confirmItem.value = item
}

/* 兑换：扣积分 + 扣库存 + 写记录，成功后同步本地数据 */
async function doRedeem(item) {
  if (redeeming.value || item.soldOut || !item.affordable) return
  redeeming.value = true
  try {
    const res = await redeemApi(item.id)
    const d = res.data || {}
    wallet.value = {
      ...wallet.value,
      earned: d.earned !== undefined ? d.earned : wallet.value.earned,
      spent: d.spent !== undefined ? d.spent : wallet.value.spent,
      available: d.available !== undefined ? d.available : wallet.value.available,
      orderCount: (wallet.value.orderCount || 0) + 1
    }
    item.owned = d.owned !== undefined ? d.owned : (item.owned || 0) + 1
    if (d.remain !== undefined) item.remain = d.remain
    if (d.soldOut !== undefined) item.soldOut = d.soldOut
    confirmItem.value = null
    show(`兑换成功！「${item.name}」已入囊中 · 花费 ${item.price} 积分`, 'ok')
    await Promise.all([loadOrders(), loadList()])
  } catch (e) {
    show(e.message || '兑换失败，请稍后再试', 'err')
  } finally {
    redeeming.value = false
  }
}

/* ===== 展示辅助 ===== */
const catTip = (c) => CAT_TIPS[c] || ''

function badgeStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}14` }
}

function glyphStyle(item) {
  const c = GRADE_COLORS[item.grade] || '#b3a684'
  return { color: c, background: `${c}18`, border: `1px solid ${c}33`, boxShadow: `0 0 18px ${c}22` }
}

function catStyle(item) {
  const c = CAT_COLORS[item.category] || '#b3a684'
  return { color: c, border: `1px solid ${c}44`, background: `${c}12` }
}

function priceStyle(item) {
  const c = CAT_COLORS[item.category] || '#b3a684'
  return { color: c, textShadow: `0 0 12px ${c}55` }
}

function detailBorderStyle(item) {
  const c = CAT_COLORS[item.category] || '#b3a684'
  return { borderColor: `${c}55`, boxShadow: `0 24px 70px rgba(10,6,0,.6), 0 0 40px ${c}22` }
}

/* 筛选变化 → 重新向后台检索 */
watch([searched, activeCat, sortBy], () => {
  if (booted) loadList()
})

/* ===== 时钟 + 登录态 ===== */
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

  await Promise.all([loadCategories(), loadList(), loadWallet(), loadOrders()])
  booted = true
})
onBeforeUnmount(() => clearInterval(timer))

const goHome = () => router.push('/home')
const goCharacters = () => router.push('/characters')
const goRealms = () => router.push('/realms')
const goQuests = () => router.push('/quests')
const goCultivation = () => router.push('/cultivation')
const goProfile = () => router.push('/profile')
const goLogout = () => { logout(); router.push('/login') }
</script>

<style scoped>
/* ===== 主体壳层 ===== */
.shop-shell{
  position:fixed;inset:0;z-index:2;
  overflow-y:auto;overflow-x:hidden;
  padding:100px 18px 46px;
  scrollbar-width:thin;scrollbar-color:rgba(255,214,130,.25) transparent;
}
.shop-shell::-webkit-scrollbar{width:6px}
.shop-shell::-webkit-scrollbar-thumb{background:rgba(255,214,130,.25);border-radius:3px}
.topnav .spacer{flex:1}

/* ===== 首屏题头 ===== */
.shop-hero{max-width:1120px;margin:0 auto 22px;text-align:center;padding:42px 22px 26px;position:relative}
.hero-deco{
  position:absolute;top:6px;left:50%;transform:translateX(-50%);
  font-size:64px;font-weight:800;letter-spacing:18px;white-space:nowrap;
  color:rgba(255,224,138,.06);pointer-events:none;user-select:none;
}
.shop-hero h2{
  font-size:30px;letter-spacing:8px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.shop-hero .sub{color:var(--muted);margin:10px 0 22px;font-size:13px;letter-spacing:2px}
.shop-hero .sub b{color:var(--gold)}

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

/* ===== 积分钱包 ===== */
.wallet-zone{
  max-width:1120px;margin:0 auto 16px;
  display:grid;grid-template-columns:minmax(240px,1.1fr) 2fr;gap:14px;
}
.wallet-card{
  padding:16px 20px;border-radius:16px;
  background:linear-gradient(135deg,rgba(255,224,138,.14),rgba(255,157,122,.06));
  border:1px solid rgba(255,224,138,.35);
  box-shadow:0 16px 40px rgba(10,6,0,.35), inset 0 0 40px rgba(255,224,138,.05);
}
.wallet-card .w-label{display:block;font-size:12px;color:var(--muted);letter-spacing:3px}
.wallet-card b{
  display:block;margin:2px 0 4px;font-size:32px;font-weight:800;line-height:1.15;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
  font-variant-numeric:tabular-nums;
}
.wallet-card .w-sub{font-size:11.5px;color:rgba(179,166,132,.9);letter-spacing:1px}
.stat-zone{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}
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

/* ===== 商品卡片 ===== */
.shop-grid{
  max-width:1120px;margin:0 auto;
  display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:18px;
}
.shop-card{
  position:relative;overflow:hidden;cursor:pointer;
  padding:20px 20px 16px;border-radius:var(--radius);
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  backdrop-filter:blur(14px) saturate(140%);
  -webkit-backdrop-filter:blur(14px) saturate(140%);
  animation:cardIn .5s both;
  transition:transform .25s, border-color .25s, box-shadow .25s;
}
.shop-card::before{
  content:'';position:absolute;inset:0 0 auto 0;height:2px;
  background:linear-gradient(90deg,transparent,rgba(255,224,138,.55),rgba(201,163,255,.3),transparent);
  opacity:.6;transition:opacity .25s;
}
.shop-card:hover{
  transform:translateY(-4px);
  border-color:rgba(255,224,138,.45);
  box-shadow:0 26px 60px rgba(10,6,0,.6), 0 0 30px rgba(255,224,138,.12);
}
.shop-card:hover::before{opacity:1}
.shop-card.soldout{opacity:.62}
.shop-card.soldout::before{background:linear-gradient(90deg,transparent,rgba(255,157,122,.5),transparent)}
@keyframes cardIn{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:none}}

.grade-badge{
  position:absolute;top:14px;right:14px;
  font-size:11px;padding:3px 10px;border-radius:999px;letter-spacing:2px;
}
.grade-badge.inline{position:static;margin-left:10px;vertical-align:2px}

.shop-top{display:flex;align-items:center;gap:14px;margin-bottom:12px}
.glyph{
  width:54px;height:54px;flex:none;border-radius:14px;
  display:flex;align-items:center;justify-content:center;
  font-size:26px;user-select:none;
}
.glyph.big{width:72px;height:72px;font-size:34px;border-radius:18px}
.shop-title{flex:1;min-width:0}
.shop-title h4{
  font-size:19px;letter-spacing:3px;font-weight:800;
  background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;color:transparent;
}
.shop-title .cat{
  display:inline-block;margin-top:6px;padding:2px 9px;border-radius:999px;
  font-size:11px;letter-spacing:1px;
}

.desc{
  font-size:12.8px;line-height:1.85;color:rgba(253,246,227,.78);
  min-height:72px;letter-spacing:.5px;
  display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;
}
.shop-meta{display:flex;gap:14px;align-items:center;margin:10px 0 8px;font-size:12px;color:var(--muted);letter-spacing:1px}
.shop-meta b{color:var(--text);font-weight:600}
.shop-meta .warn{color:var(--gold)}
.own-tag{color:var(--cyan)}

.card-foot{display:flex;align-items:center;justify-content:space-between;gap:10px}
.price{font-size:20px;font-weight:800;white-space:nowrap}
.price i{font-style:normal;font-size:11px;font-weight:600;margin-left:3px;opacity:.8;letter-spacing:1px}
.price.big{font-size:26px}
.btn-redeem{
  padding:8px 18px;border:0;border-radius:10px;cursor:pointer;
  font-size:12.5px;font-weight:700;letter-spacing:1px;color:#1a1305;
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  box-shadow:0 8px 22px rgba(224,168,60,.35);
  transition:transform .2s, box-shadow .2s, filter .2s;
  font-family:inherit;
}
.btn-redeem:hover{transform:translateY(-1px);box-shadow:0 12px 28px rgba(224,168,60,.45)}
.btn-redeem.disabled{
  color:var(--muted);background:rgba(255,255,255,.06);border:1px solid var(--panel-line);
  box-shadow:none;cursor:not-allowed;transform:none;
}
.btn-redeem.full{width:100%;margin-top:14px;padding:12px;font-size:14px;letter-spacing:4px}

/* ===== 空状态 ===== */
.empty-box{
  max-width:1120px;margin:30px auto;padding:60px 20px;text-align:center;
  color:var(--muted);letter-spacing:2px;font-size:13px;
  background:var(--panel);border:1px solid var(--panel-line);border-radius:var(--radius);
}
.empty-box .big{display:block;font-size:34px;margin-bottom:12px;color:var(--gold)}
.retry-row{margin-top:14px}

/* ===== 兑换记录 ===== */
.order-zone{max-width:1120px;margin:34px auto 0}
.order-head{
  display:flex;align-items:baseline;justify-content:space-between;gap:12px;flex-wrap:wrap;
  padding-bottom:10px;border-bottom:1px solid var(--panel-line);margin-bottom:14px;
}
.order-head h3{font-size:15px;letter-spacing:2px;font-weight:600;color:var(--gold)}
.order-head span{font-size:12px;color:var(--muted);letter-spacing:1px}
.order-list{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:10px}
.order-item{
  display:flex;align-items:center;gap:12px;
  padding:11px 14px;border-radius:12px;
  background:var(--panel);border:1px solid var(--panel-line);
}
.o-icon{font-size:20px;width:34px;height:34px;display:grid;place-items:center;border-radius:10px;background:rgba(255,255,255,.04)}
.o-main{flex:1;min-width:0}
.o-main b{display:block;font-size:13.5px;letter-spacing:1px}
.o-sub{display:block;margin-top:3px;font-size:11px;color:var(--muted)}
.o-price{font-size:12.5px;font-weight:700;color:#ffb49a;white-space:nowrap}
.order-empty{
  padding:26px;text-align:center;font-size:12.5px;letter-spacing:2px;color:var(--muted);
  background:var(--panel);border:1px dashed var(--panel-line);border-radius:14px;
}

/* ===== 兑换确认弹层 ===== */
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
.detail-head .price{margin-left:auto}
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

.fade-enter-active,.fade-leave-active{transition:opacity .25s}
.fade-enter-from,.fade-leave-to{opacity:0}

/* ===== 响应式 ===== */
@media (max-width:980px){
  .wallet-zone{grid-template-columns:1fr}
}
@media (max-width:640px){
  .shop-shell{padding-top:88px}
  .shop-hero h2{font-size:24px;letter-spacing:5px}
  .hero-deco{font-size:44px;letter-spacing:10px}
  .stat-zone{grid-template-columns:1fr}
}
</style>
