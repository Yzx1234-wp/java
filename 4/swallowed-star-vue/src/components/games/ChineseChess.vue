<template>
  <div class="cc-wrap">
    <!-- 顶部状态条 -->
    <div class="cc-stats">
      <div class="stat"><span>模式</span><b>{{ battleCtx ? '好友对战' : (mode === 'ai' ? '人机对战' : '双人对弈') }}</b></div>
      <div class="stat jade"><span>回合</span><b>{{ turn === 'r' ? '红方' : '黑方' }}</b></div>
      <div class="stat violet"><span>步数</span><b>{{ moveCount }}</b></div>
      <template v-if="!battleCtx">
        <button class="btn-cc" @click="toggleMode">{{ mode === 'ai' ? '切双人对弈' : '切人机对战' }}</button>
        <button class="btn-cc" @click="undo">悔棋</button>
        <button class="btn-cc gold" @click="newGame">新对局</button>
      </template>
      <template v-else>
        <div class="stat"><span>对手</span><b class="opp">{{ battleInfo?.counterpartNickname || '—' }}</b></div>
        <div class="stat"><span>注金</span><b class="opp">{{ battleInfo?.stake ?? '—' }}</b></div>
        <button class="btn-cc" :disabled="battleFinished" @click="resign">认 输</button>
        <button class="btn-cc gold" @click="refreshBattle">同步局面</button>
      </template>
    </div>

    <p class="cc-status" :class="{ alarm: alarm }">{{ statusText }}</p>

    <!-- 棋盘 -->
    <div class="cc-board-outer">
      <div class="cc-board" :style="boardSize">
        <svg class="cc-grid" :width="PAD * 2 + CELL * 8" :height="PAD * 2 + CELL * 9">
          <!-- 横线 10 条 -->
          <line v-for="r in 10" :key="'h' + r"
            :x1="PAD" :y1="PAD + (r - 1) * CELL"
            :x2="PAD + 8 * CELL" :y2="PAD + (r - 1) * CELL"
            stroke="rgba(255,214,130,.35)" stroke-width="1.2" />
          <!-- 竖线：两侧贯通，中间断于楚河 -->
          <line v-for="c in 9" :key="'v' + c"
            :x1="PAD + (c - 1) * CELL" :y1="PAD"
            :x2="PAD + (c - 1) * CELL"
            :y2="c === 1 || c === 9 ? PAD + 9 * CELL : PAD + 4 * CELL"
            stroke="rgba(255,214,130,.35)" stroke-width="1.2" />
          <line v-for="c in 7" :key="'v2' + c"
            :x1="PAD + c * CELL" :y1="PAD + 5 * CELL"
            :x2="PAD + c * CELL" :y2="PAD + 9 * CELL"
            stroke="rgba(255,214,130,.35)" stroke-width="1.2" />
          <!-- 九宫斜线 -->
          <line :x1="PAD + 3 * CELL" :y1="PAD" :x2="PAD + 5 * CELL" :y2="PAD + 2 * CELL" stroke="rgba(255,214,130,.28)" stroke-width="1" />
          <line :x1="PAD + 5 * CELL" :y1="PAD" :x2="PAD + 3 * CELL" :y2="PAD + 2 * CELL" stroke="rgba(255,214,130,.28)" stroke-width="1" />
          <line :x1="PAD + 3 * CELL" :y1="PAD + 7 * CELL" :x2="PAD + 5 * CELL" :y2="PAD + 9 * CELL" stroke="rgba(255,214,130,.28)" stroke-width="1" />
          <line :x1="PAD + 5 * CELL" :y1="PAD + 7 * CELL" :x2="PAD + 3 * CELL" :y2="PAD + 9 * CELL" stroke="rgba(255,214,130,.28)" stroke-width="1" />
          <!-- 楚河汉界 -->
          <text :x="PAD + CELL * 1.6" :y="PAD + CELL * 4.62" class="river">楚 河</text>
          <text :x="PAD + CELL * 6.1" :y="PAD + CELL * 4.62" class="river">汉 界</text>
        </svg>

        <!-- 棋子 + 点击层 -->
        <div
          v-for="(p, i) in flat"
          :key="'p' + i"
          class="cc-slot"
          :style="slotStyle(i)"
          @click="onSlot(i)"
        >
          <!-- 落点提示 -->
          <span v-if="targetSet.has(i)" class="target-dot" :class="{ cap: !!p }"></span>
          <!-- 棋子 -->
          <div
            v-if="p"
            class="piece"
            :class="[p.s, {
              sel: selected === i,
              last: lastMove.includes(i),
              dead: checkedKing === i
            }]"
          >
            <span>{{ pieceChar(p) }}</span>
          </div>
        </div>

        <!-- AI 思考遮罩 / 对局结算横幅 -->
        <transition name="fade">
          <div v-if="thinking" class="thinking">
            <span class="spin"></span> 黑方思忖中…
          </div>
        </transition>
        <transition name="fade">
          <div v-if="battleCtx && battleFinished" class="battle-result" :class="{ won: battleWon }">
            {{ battleWon ? '⚔ 对局告捷 · 注金已入账' : '⚔ 对局失利 · 注金已扣除' }}
          </div>
        </transition>
      </div>

      <!-- 被吃棋子 -->
      <div class="cc-captured">
        <div class="cap-col">
          <span class="cap-label">红方所获</span>
          <div class="cap-list">
            <span v-for="(p, i) in capturedByRed" :key="'cr' + i" class="cap-piece black">{{ pieceChar(p) }}</span>
          </div>
        </div>
        <div class="cap-col">
          <span class="cap-label">黑方所获</span>
          <div class="cap-list">
            <span v-for="(p, i) in capturedByBlack" :key="'cb' + i" class="cap-piece red">{{ pieceChar(p) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { battleDetailApi, battleFinishApi, battleMoveApi } from '../../api/friend.js'

const CELL = 50
const PAD = 32
const boardSize = {
  width: PAD * 2 + CELL * 8 + 'px',
  height: PAD * 2 + CELL * 9 + 'px'
}

const CHAR = {
  r: { K: '帅', A: '仕', E: '相', N: '马', R: '车', C: '炮', P: '兵' },
  b: { K: '将', A: '士', E: '象', N: '马', R: '车', C: '炮', P: '卒' }
}
const VAL = { K: 10000, R: 900, C: 450, N: 400, A: 200, E: 200, P: 100 }

const mode = ref('ai')
const turn = ref('r')
const moveCount = ref(0)
const board = ref([])
const selected = ref(null)      // 扁平索引
const targets = ref([])         // 合法走法 [{r1,c1,r2,c2}]
const lastMove = ref([])        // 上一步起止（扁平索引）
const checkedKing = ref(-1)     // 被将军一方老将位置
const gameOver = ref(false)
const thinking = ref(false)
const alarm = ref(false)
const capturedByRed = ref([])
const capturedByBlack = ref([])
const history = ref([])         // 快照栈

const idx = (r, c) => r * 9 + c
const rc = (i) => [Math.floor(i / 9), i % 9]

const flat = computed(() => {
  const out = []
  for (let r = 0; r < 10; r++) for (let c = 0; c < 9; c++) out.push(board.value[r] ? board.value[r][c] : null)
  return out
})
const targetSet = computed(() => {
  const s = new Set()
  for (const m of targets.value) s.add(idx(m.r2, m.c2))
  return s
})

const statusText = computed(() => {
  if (battleCtx && battleFinished.value) {
    return battleWon.value ? '对局已结算 · 你赢了这场对战！' : '对局已结算 · 对方赢了这场对战'
  }
  if (gameOver.value) {
    if (battleCtx) return '对局结束 · 正在上报结算…'
    return winner.value === 'r' ? '红方胜 · 黑方道陨！点「新对局」再战' : '黑方胜 · 红方道陨！点「新对局」再战'
  }
  if (thinking.value) return '黑方正在思忖…'
  if (props.battleCtx) {
    const mine = myTurn.value
    const side = turn.value === 'r' ? '红方行棋' : '黑方行棋'
    if (!mine) return '等待对方落子…'
    return alarm.value ? `将军！轮到你走棋（${side}）` : `轮到你走棋（${side}）`
  }
  const side = turn.value === 'r' ? '红方行棋' : '黑方行棋'
  return alarm.value ? `将军！${side}，速解杀局` : side
})
const winner = ref('')

/* ===== 好友对战模式 ===== */
const props = defineProps({
  // 对战上下文：{ id: 对战编号, myRole: 'challenger'|'opponent' }；为空即本地模式
  battleCtx: { type: Object, default: null }
})
const battleInfo = ref(null)      // 后端对战详情
const appliedMoves = ref(0)       // 已同步到本地的招法数
const battleFinished = computed(() => props.battleCtx && battleInfo.value?.status === 'FINISHED')
const battleWon = computed(() => !!battleInfo.value?.iWon)
const myTurn = computed(() => {
  if (!props.battleCtx) return true
  // 挑战者执红先行；招法偶数条 → 红方回合
  const redTurn = moveCount.value % 2 === 0
  return props.battleCtx.myRole === 'challenger' ? redTurn : !redTurn
})

async function refreshBattle() {
  if (!props.battleCtx) return
  try {
    const res = await battleDetailApi(props.battleCtx.id)
    const b = res.data || {}
    battleInfo.value = b
    const moves = b.moves || []
    // 补放尚未同步的招法
    for (let i = appliedMoves.value; i < moves.length; i++) {
      const parts = String(moves[i]).split(',').map(Number)
      if (parts.length === 4 && !Number.isNaN(parts[0])) {
        commit({ r1: parts[0], c1: parts[1], r2: parts[2], c2: parts[3] })
      }
    }
    appliedMoves.value = Math.max(appliedMoves.value, moves.length)
    if (b.status === 'FINISHED') gameOver.value = true
  } catch { /* 轮询失败静默，下轮重试 */ }
}

let battleTimer = null
onMounted(() => {
  if (props.battleCtx) {
    refreshBattle()
    battleTimer = setInterval(refreshBattle, 2000)
  }
})
onBeforeUnmount(() => { if (battleTimer) clearInterval(battleTimer) })

async function postMove(m) {
  if (!props.battleCtx) return
  try {
    await battleMoveApi(props.battleCtx.id, `${m.r1},${m.c1},${m.r2},${m.c2}`)
    // commit 已判定绝杀/困毙（对方无合法走法）→ 主动上报获胜结算
    if (gameOver.value) {
      const res = await battleFinishApi(props.battleCtx.id, 'WIN')
      battleInfo.value = res.data || battleInfo.value
    }
  } catch (e) {
    console.warn('招法上报失败', e)
  }
}

async function resign() {
  if (!props.battleCtx || battleFinished.value) return
  try {
    const res = await battleFinishApi(props.battleCtx.id, 'LOSE')
    battleInfo.value = res.data || battleInfo.value
    gameOver.value = true
  } catch { /* 忽略 */ }
}

const pieceChar = (p) => CHAR[p.s][p.t]

/* ===== 初始局面 ===== */
function initialBoard() {
  const b = Array.from({ length: 10 }, () => Array(9).fill(null))
  const back = ['R', 'N', 'E', 'A', 'K', 'A', 'E', 'N', 'R']
  back.forEach((t, c) => { b[0][c] = { t, s: 'b' } })
  b[2][1] = { t: 'C', s: 'b' }; b[2][7] = { t: 'C', s: 'b' }
  for (const c of [0, 2, 4, 6, 8]) b[3][c] = { t: 'P', s: 'b' }
  for (const c of [0, 2, 4, 6, 8]) b[6][c] = { t: 'P', s: 'r' }
  b[7][1] = { t: 'C', s: 'r' }; b[7][7] = { t: 'C', s: 'r' }
  back.forEach((t, c) => { b[9][c] = { t, s: 'r' } })
  return b
}

function newGame() {
  board.value = initialBoard()
  turn.value = 'r'
  moveCount.value = 0
  selected.value = null
  targets.value = []
  lastMove.value = []
  checkedKing.value = -1
  gameOver.value = false
  winner.value = ''
  alarm.value = false
  thinking.value = false
  capturedByRed.value = []
  capturedByBlack.value = []
  history.value = []
}
newGame()

function toggleMode() {
  mode.value = mode.value === 'ai' ? 'pvp' : 'ai'
  newGame()
}

/* ===== 走法生成 ===== */
const inBoard = (r, c) => r >= 0 && r < 10 && c >= 0 && c < 9
const inPalace = (r, c, s) => c >= 3 && c <= 5 && (s === 'r' ? r >= 7 : r <= 2)

function genPseudo(b, side, r, c) {
  const p = b[r][c]
  if (!p || p.s !== side) return []
  const out = []
  const push = (nr, nc) => {
    if (!inBoard(nr, nc)) return
    const q = b[nr][nc]
    if (!q || q.s !== side) out.push({ r1: r, c1: c, r2: nr, c2: nc })
  }

  if (p.t === 'K' || p.t === 'A') {
    const dirs = p.t === 'K'
      ? [[1, 0], [-1, 0], [0, 1], [0, -1]]
      : [[1, 1], [1, -1], [-1, 1], [-1, -1]]
    for (const [dr, dc] of dirs) {
      const nr = r + dr, nc = c + dc
      if (inPalace(nr, nc, side)) push(nr, nc)
    }
  } else if (p.t === 'E') {
    for (const [dr, dc] of [[2, 2], [2, -2], [-2, 2], [-2, -2]]) {
      const nr = r + dr, nc = c + dc
      // 象不过河：红 r>=5，黑 r<=4
      if (!inBoard(nr, nc)) continue
      if (side === 'r' && nr < 5) continue
      if (side === 'b' && nr > 4) continue
      // 象眼
      if (b[r + dr / 2][c + dc / 2]) continue
      push(nr, nc)
    }
  } else if (p.t === 'N') {
    for (const [dr, dc, lr, lc] of [
      [2, 1, 1, 0], [2, -1, 1, 0], [-2, 1, -1, 0], [-2, -1, -1, 0],
      [1, 2, 0, 1], [-1, 2, 0, 1], [1, -2, 0, -1], [-1, -2, 0, -1]
    ]) {
      const nr = r + dr, nc = c + dc
      if (!inBoard(nr, nc)) continue
      if (b[r + lr][c + lc]) continue // 马腿
      push(nr, nc)
    }
  } else if (p.t === 'R') {
    for (const [dr, dc] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      let nr = r + dr, nc = c + dc
      while (inBoard(nr, nc)) {
        const q = b[nr][nc]
        if (!q) out.push({ r1: r, c1: c, r2: nr, c2: nc })
        else {
          if (q.s !== side) out.push({ r1: r, c1: c, r2: nr, c2: nc })
          break
        }
        nr += dr; nc += dc
      }
    }
  } else if (p.t === 'C') {
    for (const [dr, dc] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      let nr = r + dr, nc = c + dc
      let screened = false
      while (inBoard(nr, nc)) {
        const q = b[nr][nc]
        if (!screened) {
          if (!q) out.push({ r1: r, c1: c, r2: nr, c2: nc })
          else screened = true
        } else if (q) {
          if (q.s !== side) out.push({ r1: r, c1: c, r2: nr, c2: nc })
          break
        }
        nr += dr; nc += dc
      }
    }
  } else if (p.t === 'P') {
    const fwd = side === 'r' ? -1 : 1
    push(r + fwd, c)
    const crossed = side === 'r' ? r <= 4 : r >= 5
    if (crossed) {
      push(r, c - 1)
      push(r, c + 1)
    }
  }
  return out
}

function findKing(b, side) {
  for (let r = 0; r < 10; r++)
    for (let c = 0; c < 9; c++) {
      const p = b[r][c]
      if (p && p.t === 'K' && p.s === side) return [r, c]
    }
  return null
}

/* side 的老将是否被攻击（含将帅对脸） */
function isKingAttacked(b, side) {
  const kp = findKing(b, side)
  if (!kp) return true
  const [kr, kc] = kp
  const foe = side === 'r' ? 'b' : 'r'

  // 四向射线：车 / 炮 / 对脸
  for (const [dr, dc] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    let nr = kr + dr, nc = kc + dc
    let screen = false
    while (inBoard(nr, nc)) {
      const q = b[nr][nc]
      if (q) {
        if (!screen) {
          if (q.s === foe && (q.t === 'R' || q.t === 'K')) return true
          screen = true
        } else if (q.s === foe && q.t === 'C') return true
        else if (q) break
      }
      nr += dr; nc += dc
    }
  }
  // 马（注意蹩腿以马自身位置计）
  for (const [dr, dc] of [[1, 2], [2, 1], [-1, 2], [-2, 1], [1, -2], [2, -1], [-1, -2], [-2, -1]]) {
    const nr = kr + dr, nc = kc + dc
    if (!inBoard(nr, nc)) continue
    const q = b[nr][nc]
    if (!q || q.s !== foe || q.t !== 'N') continue
    // 马腿：马向老将方向一格（2 分量方向）
    let lr, lc
    if (Math.abs(dr) === 2) { lr = nr - dr / 2; lc = nc }
    else { lr = nr; lc = nc - dc / 2 }
    if (!b[lr][lc]) return true
  }
  // 兵卒
  const fwdOf = (s) => (s === 'r' ? -1 : 1)
  // 敌兵向前一步可击中老将 → 敌兵位于老将身后一格
  {
    const pr = kr - fwdOf(foe)
    if (inBoard(pr, kc)) {
      const q = b[pr][kc]
      if (q && q.s === foe && q.t === 'P') return true
    }
    // 敌兵横击：需已过河
    for (const dc of [-1, 1]) {
      if (!inBoard(kr, kc + dc)) continue
      const q = b[kr][kc + dc]
      if (q && q.s === foe && q.t === 'P') {
        const crossed = foe === 'r' ? kr <= 4 : kr >= 5
        if (crossed) return true
      }
    }
  }
  return false
}

function genLegal(b, side) {
  const out = []
  for (let r = 0; r < 10; r++)
    for (let c = 0; c < 9; c++) {
      for (const m of genPseudo(b, side, r, c)) {
        const piece = b[m.r1][m.c1]
        const cap = b[m.r2][m.c2]
        b[m.r2][m.c2] = piece
        b[m.r1][m.c1] = null
        const bad = isKingAttacked(b, side)
        b[m.r1][m.c1] = piece
        b[m.r2][m.c2] = cap
        if (!bad) out.push(m)
      }
    }
  return out
}

/* ===== 评估（红方为正） ===== */
function evaluate(b) {
  let s = 0
  for (let r = 0; r < 10; r++)
    for (let c = 0; c < 9; c++) {
      const p = b[r][c]
      if (!p) continue
      let v = VAL[p.t]
      if (p.t === 'P') {
        const crossed = p.s === 'r' ? r <= 4 : r >= 5
        if (crossed) v += 80
      }
      s += p.s === 'r' ? v : -v
    }
  return s
}

/* ===== AI（黑方，2 层 α-β 搜索） ===== */
function aiMove() {
  const b = board.value.map((row) => row.slice())
  const moves = genLegal(b, 'b')
  if (!moves.length) {
    finish('r')
    return
  }
  moves.sort((x, y) => (b[y.r2][y.c2] ? VAL[b[y.r2][y.c2].t] : 0) - (b[x.r2][x.c2] ? VAL[b[x.r2][x.c2].t] : 0))

  let best = moves[0]
  let bestV = -Infinity
  let alpha = -Infinity
  for (const m of moves) {
    const piece = b[m.r1][m.c1]
    const cap = b[m.r2][m.c2]
    b[m.r2][m.c2] = piece
    b[m.r1][m.c1] = null
    const v = -search(b, 'r', 1, -Infinity, -alpha)
    b[m.r1][m.c1] = piece
    b[m.r2][m.c2] = cap
    if (v > bestV) {
      bestV = v
      best = m
      alpha = v
    }
  }
  commit(best)
}

function search(b, side, depth, alpha, beta) {
  if (depth === 0) {
    const e = evaluate(b)
    return side === 'r' ? e : -e
  }
  const moves = genLegal(b, side)
  if (!moves.length) return -100000
  moves.sort((x, y) => (b[y.r2][y.c2] ? VAL[b[y.r2][y.c2].t] : 0) - (b[x.r2][x.c2] ? VAL[b[x.r2][x.c2].t] : 0))
  let bestV = -Infinity
  for (const m of moves) {
    const piece = b[m.r1][m.c1]
    const cap = b[m.r2][m.c2]
    b[m.r2][m.c2] = piece
    b[m.r1][m.c1] = null
    const v = -search(b, side === 'r' ? 'b' : 'r', depth - 1, -beta, -alpha)
    b[m.r1][m.c1] = piece
    b[m.r2][m.c2] = cap
    if (v > bestV) bestV = v
    if (bestV > alpha) alpha = bestV
    if (alpha >= beta) break
  }
  return bestV
}

/* ===== 落子 ===== */
function commit(m) {
  history.value.push({
    board: board.value.map((row) => row.slice()),
    capR: capturedByRed.value.slice(),
    capB: capturedByBlack.value.slice(),
    last: lastMove.value.slice(),
    count: moveCount.value
  })
  const b = board.value.map((row) => row.slice())
  const piece = b[m.r1][m.c1]
  const cap = b[m.r2][m.c2]
  if (cap) {
    if (cap.s === 'b') capturedByRed.value.push(cap)
    else capturedByBlack.value.push(cap)
  }
  b[m.r2][m.c2] = piece
  b[m.r1][m.c1] = null
  board.value = b
  lastMove.value = [idx(m.r1, m.c1), idx(m.r2, m.c2)]
  moveCount.value++
  selected.value = null
  targets.value = []

  const opp = turn.value === 'r' ? 'b' : 'r'
  turn.value = opp

  // 局势判定
  const oppMoves = genLegal(b, opp)
  const inCheck = isKingAttacked(b, opp)
  const kp = findKing(b, opp)
  checkedKing.value = kp ? idx(kp[0], kp[1]) : -1
  if (!oppMoves.length) {
    finish(turn.value === 'r' ? 'r' : 'b')
  } else {
    alarm.value = inCheck
  }
}

function finish(winSide) {
  gameOver.value = true
  winner.value = winSide
  alarm.value = false
  checkedKing.value = -1
  thinking.value = false
}

function onSlot(i) {
  if (gameOver.value || thinking.value) return
  if (props.battleCtx) {
    // 好友对战：结算后锁定；只允许在轮到自己时走己方棋子
    if (battleFinished.value) return
    if (!myTurn.value) return
    const mySide = props.battleCtx.myRole === 'challenger' ? 'r' : 'b'
    if (turn.value !== mySide) return
  } else if (mode.value === 'ai' && turn.value === 'b') return
  const [r, c] = rc(i)
  const p = board.value[r][c]

  // 已选中且点击合法落点 → 走棋
  if (selected.value !== null) {
    const m = targets.value.find((t) => t.r2 === r && t.c2 === c)
    if (m) {
      commit(m)
      if (props.battleCtx) {
        postMove(m)
      } else if (mode.value === 'ai' && turn.value === 'b' && !gameOver.value) {
        thinking.value = true
        setTimeout(() => {
          thinking.value = false
          aiMove()
        }, 380)
      }
      return
    }
  }

  // 选中己方棋子
  if (p && p.s === turn.value) {
    selected.value = i
    targets.value = genLegal(board.value, turn.value).filter((t) => t.r1 === r && t.c1 === c)
  } else {
    selected.value = null
    targets.value = []
  }
}

function undo() {
  if (!history.value.length || thinking.value) return
  // 人机模式悔两步回到红方；双人模式悔一步
  const steps = mode.value === 'ai' && turn.value === 'r' ? 2 : 1
  let snap = null
  for (let k = 0; k < steps && history.value.length; k++) snap = history.value.pop()
  if (snap) {
    board.value = snap.board
    capturedByRed.value = snap.capR
    capturedByBlack.value = snap.capB
    lastMove.value = snap.last
    moveCount.value = snap.count
  }
  gameOver.value = false
  winner.value = ''
  alarm.value = false
  checkedKing.value = -1
  selected.value = null
  targets.value = []
  turn.value = mode.value === 'ai' ? 'r' : (turn.value === 'r' ? 'b' : 'r')
}

/* ===== 渲染辅助 ===== */
function slotStyle(i) {
  const [r, c] = rc(i)
  return {
    left: PAD + c * CELL - 23 + 'px',
    top: PAD + r * CELL - 23 + 'px'
  }
}
</script>

<style scoped>
.cc-wrap{display:flex;flex-direction:column;align-items:center;gap:14px}
.cc-stats{display:flex;align-items:center;gap:10px;flex-wrap:wrap;justify-content:center}
.stat{
  display:flex;flex-direction:column;align-items:center;gap:2px;
  padding:8px 18px;border-radius:14px;min-width:88px;
  background:var(--panel);border:1px solid var(--panel-line);
}
.stat span{font-size:11px;color:var(--muted);letter-spacing:2px}
.stat b{font-size:17px;color:var(--gold)}
.stat.jade b{color:var(--cyan)}
.stat.violet b{color:var(--violet)}
.stat .opp{font-size:14px}
.btn-cc:disabled{opacity:.45;cursor:not-allowed}
.battle-result{
  position:absolute;inset:0;display:flex;align-items:center;justify-content:center;
  background:rgba(10,6,0,.62);backdrop-filter:blur(2px);
  border-radius:20px;color:var(--danger);font-size:19px;font-weight:800;letter-spacing:5px;
  pointer-events:none;
}
.battle-result.won{color:var(--ok);text-shadow:0 0 18px rgba(126,224,138,.6)}
.btn-cc{
  padding:8px 14px;border-radius:12px;cursor:pointer;font-size:12.5px;
  letter-spacing:1px;font-weight:600;color:var(--gold);
  background:rgba(255,224,138,.08);border:1px solid rgba(255,224,138,.3);
  transition:.2s;
}
.btn-cc:hover{background:rgba(255,224,138,.18)}
.btn-cc.gold{
  color:#1a1305;background:linear-gradient(135deg,var(--gold),var(--gold-2));
  border:0;box-shadow:0 8px 22px rgba(224,168,60,.3);
}
.btn-cc.gold:hover{transform:translateY(-1px)}
.cc-status{font-size:14px;letter-spacing:2px;color:var(--gold);min-height:20px}
.cc-status.alarm{color:#ff6b81;animation:ccblink 1s infinite}
@keyframes ccblink{0%,100%{opacity:1}50%{opacity:.45}}

.cc-board-outer{display:flex;flex-direction:column;align-items:center;gap:14px}
.cc-board{
  position:relative;
  border-radius:20px;
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  user-select:none;
}
.cc-grid{position:absolute;inset:0;pointer-events:none}
.cc-grid .river{
  fill:rgba(255,214,130,.4);font-size:15px;letter-spacing:6px;font-weight:700;
}
.cc-slot{position:absolute;width:46px;height:46px}
.cc-slot .target-dot{
  position:absolute;left:50%;top:50%;width:12px;height:12px;
  transform:translate(-50%,-50%);
  border-radius:50%;background:rgba(140,232,184,.85);
  box-shadow:0 0 10px rgba(140,232,184,.6);
  pointer-events:none;
}
.cc-slot .target-dot.cap{
  width:40px;height:40px;background:transparent;
  border:2px dashed rgba(255,107,129,.85);
  box-shadow:0 0 12px rgba(255,107,129,.5);
}
.piece{
  position:absolute;inset:0;border-radius:50%;cursor:pointer;
  display:flex;align-items:center;justify-content:center;
  font-size:24px;font-weight:800;
  transition:transform .15s, box-shadow .15s;
  animation:pieceIn .22s both;
}
@keyframes pieceIn{from{transform:scale(.7)}to{transform:scale(1)}}
.piece:hover{transform:scale(1.08)}
.piece.r{
  color:#ff8a9a;
  background:radial-gradient(circle at 34% 28%, rgba(255,157,122,.28), rgba(255,107,129,.1) 70%);
  border:2px solid rgba(255,107,129,.75);
  box-shadow:inset 0 0 10px rgba(255,107,129,.18), 0 0 12px rgba(255,107,129,.2);
}
.piece.b{
  color:#ffe08a;
  background:radial-gradient(circle at 34% 28%, rgba(255,224,138,.22), rgba(255,214,130,.08) 70%);
  border:2px solid rgba(255,214,130,.7);
  box-shadow:inset 0 0 10px rgba(255,224,138,.15), 0 0 12px rgba(255,224,138,.18);
}
.piece.sel{
  transform:scale(1.14);
  box-shadow:0 0 0 3px var(--gold), 0 0 22px rgba(255,224,138,.55) !important;
}
.piece.last{outline:1px dashed rgba(255,224,138,.5);outline-offset:-1px}
.piece.dead{animation:ccshake .4s infinite}
@keyframes ccshake{0%,100%{transform:translateX(0)}25%{transform:translateX(-2px)}75%{transform:translateX(2px)}}

.thinking{
  position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:10px;
  background:rgba(10,6,0,.5);backdrop-filter:blur(2px);
  border-radius:20px;color:var(--gold);font-size:14px;letter-spacing:3px;
}
.thinking .spin{
  width:16px;height:16px;border-radius:50%;
  border:2px solid rgba(255,224,138,.3);border-top-color:var(--gold);
  animation:spin .8s linear infinite;
}
@keyframes spin{to{transform:rotate(360deg)}}

.cc-captured{display:flex;gap:18px;flex-wrap:wrap;justify-content:center}
.cap-col{display:flex;flex-direction:column;gap:6px;align-items:center}
.cap-label{font-size:11px;color:var(--muted);letter-spacing:2px}
.cap-list{display:flex;gap:5px;flex-wrap:wrap;justify-content:center;min-height:26px;max-width:420px}
.cap-piece{
  width:26px;height:26px;border-radius:50%;
  display:flex;align-items:center;justify-content:center;
  font-size:13px;font-weight:700;
}
.cap-piece.red{color:#ff8a9a;border:1px solid rgba(255,107,129,.55);background:rgba(255,107,129,.1)}
.cap-piece.black{color:#ffe08a;border:1px solid rgba(255,214,130,.5);background:rgba(255,224,138,.08)}

.fade-enter-active,.fade-leave-active{transition:opacity .2s}
.fade-enter-from,.fade-leave-to{opacity:0}
</style>
