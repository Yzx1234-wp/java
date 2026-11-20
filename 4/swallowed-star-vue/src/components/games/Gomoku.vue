<template>
  <div class="gk-wrap">
    <!-- 顶部状态条 -->
    <div class="gk-stats">
      <div class="stat"><span>模式</span><b>{{ battleCtx ? '好友对战' : (mode === 'ai' ? '人机对战' : '双人对弈') }}</b></div>
      <div class="stat jade"><span>回合</span><b>{{ turn === 1 ? '黑方' : '白方' }}</b></div>
      <div class="stat violet"><span>手数</span><b>{{ moveCount }}</b></div>
      <template v-if="!battleCtx">
        <button class="btn-gk" @click="toggleMode">{{ mode === 'ai' ? '切双人对弈' : '切人机对战' }}</button>
        <button class="btn-gk" @click="undo">悔棋</button>
        <button class="btn-gk gold" @click="newGame">新对局</button>
      </template>
      <template v-else>
        <div class="stat"><span>对手</span><b class="opp">{{ battleInfo?.counterpartNickname || '—' }}</b></div>
        <div class="stat"><span>注金</span><b class="opp">{{ battleInfo?.stake ?? '—' }}</b></div>
        <button class="btn-gk" :disabled="battleFinished" @click="resign">认 输</button>
        <button class="btn-gk gold" @click="refreshBattle">同步局面</button>
      </template>
    </div>

    <p class="gk-status" :class="{ alarm: !!winLine }">{{ statusText }}</p>

    <!-- 棋盘 -->
    <div class="gk-board-outer">
      <div class="gk-board" :style="boardSize">
        <svg class="gk-grid" :width="PAD * 2 + CELL * 14" :height="PAD * 2 + CELL * 14">
          <line v-for="i in 15" :key="'h' + i"
            :x1="PAD" :y1="PAD + (i - 1) * CELL"
            :x2="PAD + 14 * CELL" :y2="PAD + (i - 1) * CELL"
            stroke="rgba(255,214,130,.32)" stroke-width="1" />
          <line v-for="i in 15" :key="'v' + i"
            :x1="PAD + (i - 1) * CELL" :y1="PAD"
            :x2="PAD + (i - 1) * CELL" :y2="PAD + 14 * CELL"
            stroke="rgba(255,214,130,.32)" stroke-width="1" />
          <!-- 边框加粗 -->
          <rect :x="PAD" :y="PAD" :width="14 * CELL" :height="14 * CELL"
            fill="none" stroke="rgba(255,214,130,.5)" stroke-width="1.6" />
          <!-- 星位 + 天元 -->
          <circle v-for="(s, i) in STAR_POINTS" :key="'s' + i"
            :cx="PAD + s[1] * CELL" :cy="PAD + s[0] * CELL" r="3.5"
            fill="rgba(255,214,130,.55)" />
        </svg>

        <!-- 落子点击层 -->
        <div
          v-for="(p, i) in flat"
          :key="'c' + i"
          class="gk-slot"
          :style="slotStyle(i)"
          :class="{ win: winSet.has(i) }"
          @click="onSlot(i)"
        >
          <!-- 落子预览 -->
          <span v-if="!p && !gameOver && hoverable(i)" class="ghost" :class="turn === 1 ? 'ghost-b' : 'ghost-w'"></span>
          <!-- 棋子 -->
          <div v-if="p" class="stone" :class="[p === 1 ? 'black' : 'white', { last: i === lastIdx, win: winSet.has(i) }]">
            <span v-if="i === lastIdx" class="mark"></span>
          </div>
        </div>

        <!-- 胜利横幅 -->
        <transition name="fade">
          <div v-if="winLine" class="win-banner">
            {{ winLine.side === 1 ? '黑' : '白' }}方五子成阵 · 胜！
          </div>
        </transition>
        <!-- 对局结算横幅 -->
        <transition name="fade">
          <div v-if="battleCtx && battleFinished && !winLine" class="win-banner" :class="{ won: battleWon }">
            {{ battleWon ? '⚔ 对局告捷 · 注金已入账' : '⚔ 对局失利 · 注金已扣除' }}
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { battleDetailApi, battleFinishApi, battleMoveApi } from '../../api/friend.js'

const N = 15
const CELL = 32
const PAD = 26
const boardSize = {
  width: PAD * 2 + CELL * 14 + 'px',
  height: PAD * 2 + CELL * 14 + 'px'
}
const STAR_POINTS = [[3, 3], [3, 11], [11, 3], [11, 11], [7, 7]]

const mode = ref('ai')
const turn = ref(1)              // 1 黑 / 2 白
const board = ref([])
const moveCount = ref(0)
const lastIdx = ref(-1)
const winLine = ref(null)        // { side, cells: [idx...] }
const gameOver = ref(false)
const history = ref([])

const idx = (r, c) => r * N + c
const rc = (i) => [Math.floor(i / N), i % N]

const flat = computed(() => {
  const out = []
  for (let r = 0; r < N; r++)
    for (let c = 0; c < N; c++) out.push(board.value[r] ? board.value[r][c] : 0)
  return out
})
const winSet = computed(() => new Set(winLine.value ? winLine.value.cells : []))

const statusText = computed(() => {
  if (props.battleCtx && battleFinished.value) {
    return battleWon.value ? '对局已结算 · 你赢了这场对战！' : '对局已结算 · 对方赢了这场对战'
  }
  if (gameOver.value) {
    if (!winLine.value) return '棋盘已满 · 握手言和'
    return (winLine.value.side === 1 ? '黑方胜 · 五子成阵！' : '白方胜 · 五子成阵！')
  }
  if (props.battleCtx) {
    if (!myTurn.value) return '等待对方落子…'
    return `轮到你落子（${turn.value === 1 ? '黑方' : '白方'}）`
  }
  return (turn.value === 1 ? '黑方行棋' : '白方行棋') + (mode.value === 'ai' && turn.value === 2 ? ' · AI 思忖中…' : '')
})

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
  // 挑战者执黑先行；招法偶数条 → 黑方回合
  const blackTurn = moveCount.value % 2 === 0
  return props.battleCtx.myRole === 'challenger' ? blackTurn : !blackTurn
})

async function refreshBattle() {
  if (!props.battleCtx) return
  try {
    const res = await battleDetailApi(props.battleCtx.id)
    const b = res.data || {}
    battleInfo.value = b
    const moves = b.moves || []
    for (let i = appliedMoves.value; i < moves.length; i++) {
      const parts = String(moves[i]).split(',').map(Number)
      if (parts.length === 2 && !Number.isNaN(parts[0])) {
        place(parts[0], parts[1])
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

async function postMove(r, c) {
  if (!props.battleCtx) return
  try {
    await battleMoveApi(props.battleCtx.id, `${r},${c}`)
    // place 已判定五连 → 主动上报获胜结算
    if (gameOver.value) {
      const res = await battleFinishApi(props.battleCtx.id, 'WIN')
      battleInfo.value = res.data || battleInfo.value
    }
  } catch (e) {
    console.warn('落子上报失败', e)
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

const hoverable = (i) => {
  if (props.battleCtx) return myTurn.value
  if (mode.value === 'ai' && turn.value === 2) return false
  return true
}

function newGame() {
  board.value = Array.from({ length: N }, () => Array(N).fill(0))
  turn.value = 1
  moveCount.value = 0
  lastIdx.value = -1
  winLine.value = null
  gameOver.value = false
  history.value = []
}
newGame()

function toggleMode() {
  mode.value = mode.value === 'ai' ? 'pvp' : 'ai'
  newGame()
}

/* ===== 胜负判定：从 (r,c) 四向数连子，返回五连坐标 ===== */
function findWin(b, r, c, side) {
  for (const [dr, dc] of [[0, 1], [1, 0], [1, 1], [1, -1]]) {
    const cells = [idx(r, c)]
    for (const sign of [1, -1]) {
      let nr = r + dr * sign, nc = c + dc * sign
      while (nr >= 0 && nr < N && nc >= 0 && nc < N && b[nr][nc] === side) {
        cells.push(idx(nr, nc))
        nr += dr * sign
        nc += dc * sign
      }
    }
    if (cells.length >= 5) return cells
  }
  return null
}

/* ===== 单方向打分：假定 side 在 (r,c) 落子后的连子形态 ===== */
function dirScore(b, r, c, side, dr, dc) {
  let count = 1
  let block = 0
  for (const sign of [1, -1]) {
    let nr = r + dr * sign, nc = c + dc * sign
    while (nr >= 0 && nr < N && nc >= 0 && nc < N && b[nr][nc] === side) {
      count++
      nr += dr * sign
      nc += dc * sign
    }
    // 连子尽头：空格为活，边界/对方为堵
    if (nr < 0 || nr >= N || nc < 0 || nc >= N) block++
    else if (b[nr][nc] !== 0 && b[nr][nc] !== side) block++
  }
  if (block === 2 && count < 5) return 0
  if (count >= 5) return 100000
  if (count === 4) return block === 0 ? 10000 : 1200
  if (count === 3) return block === 0 ? 1500 : 150
  if (count === 2) return block === 0 ? 120 : 15
  return block === 0 ? 15 : 3
}

function cellScore(b, r, c, side) {
  let s = 0
  for (const [dr, dc] of [[0, 1], [1, 0], [1, 1], [1, -1]]) {
    s += dirScore(b, r, c, side, dr, dc)
  }
  return s
}

/* 候选点：已有棋子周围 2 格内的空位 */
function candidates(b) {
  const set = new Set()
  let hasStone = false
  for (let r = 0; r < N; r++)
    for (let c = 0; c < N; c++) {
      if (!b[r][c]) continue
      hasStone = true
      for (let dr = -2; dr <= 2; dr++)
        for (let dc = -2; dc <= 2; dc++) {
          const nr = r + dr, nc = c + dc
          if (nr >= 0 && nr < N && nc >= 0 && nc < N && !b[nr][nc]) set.add(idx(nr, nc))
        }
    }
  if (!hasStone) return [idx(7, 7)]
  return [...set]
}

/* AI（白方）：进攻 + 防守加权择优 */
function aiMove() {
  const b = board.value.map((row) => row.slice())
  const cands = candidates(b)
  let best = cands[0]
  let bestV = -1
  for (const i of cands) {
    const [r, c] = rc(i)
    // 进攻分（白落子）+ 防守分（黑若抢先落子）×0.9
    const v = cellScore(b, r, c, 2) + cellScore(b, r, c, 1) * 0.9
    const jitter = Math.random() * 2
    if (v + jitter > bestV) {
      bestV = v + jitter
      best = i
    }
  }
  const [r, c] = rc(best)
  place(r, c)
}

/* ===== 落子 ===== */
function place(r, c) {
  history.value.push({
    board: board.value.map((row) => row.slice()),
    turn: turn.value,
    moveCount: moveCount.value,
    lastIdx: lastIdx.value
  })
  const b = board.value.map((row) => row.slice())
  b[r][c] = turn.value
  board.value = b
  lastIdx.value = idx(r, c)
  moveCount.value++

  const win = findWin(b, r, c, turn.value)
  if (win) {
    winLine.value = { side: turn.value, cells: win }
    gameOver.value = true
    return
  }
  if (moveCount.value >= N * N) {
    gameOver.value = true
    return
  }
  turn.value = turn.value === 1 ? 2 : 1
}

function onSlot(i) {
  if (gameOver.value) return
  if (props.battleCtx) {
    // 好友对战：只允许在自己回合落子
    if (battleFinished.value) return
    if (!myTurn.value) return
  } else if (mode.value === 'ai' && turn.value === 2) return
  const [r, c] = rc(i)
  if (board.value[r][c]) return
  place(r, c)
  if (props.battleCtx) {
    postMove(r, c)
    return
  }
  // 人机模式：白方 AI 回应
  if (mode.value === 'ai' && turn.value === 2 && !gameOver.value) {
    setTimeout(() => aiMove(), 260)
  }
}

function undo() {
  if (!history.value.length) return
  // 人机模式悔两步回到黑方；双人模式悔一步
  const steps = mode.value === 'ai' && turn.value === 1 ? 2 : 1
  let snap = null
  for (let k = 0; k < steps && history.value.length; k++) snap = history.value.pop()
  if (snap) {
    board.value = snap.board
    moveCount.value = snap.moveCount
    lastIdx.value = snap.lastIdx
    turn.value = snap.turn
  }
  gameOver.value = false
  winLine.value = null
}

/* ===== 渲染辅助 ===== */
function slotStyle(i) {
  const [r, c] = rc(i)
  return {
    left: PAD + c * CELL - 15 + 'px',
    top: PAD + r * CELL - 15 + 'px'
  }
}
</script>

<style scoped>
.gk-wrap{display:flex;flex-direction:column;align-items:center;gap:14px}
.gk-stats{display:flex;align-items:center;gap:10px;flex-wrap:wrap;justify-content:center}
.stat{
  display:flex;flex-direction:column;align-items:center;gap:2px;
  padding:8px 18px;border-radius:14px;min-width:88px;
  background:var(--panel);border:1px solid var(--panel-line);
}
.stat span{font-size:11px;color:var(--muted);letter-spacing:2px}
.stat b{font-size:17px;color:var(--gold)}
.stat.jade b{color:var(--cyan)}
.stat.violet b{color:var(--violet)}
.btn-gk{
  padding:8px 14px;border-radius:12px;cursor:pointer;font-size:12.5px;
  letter-spacing:1px;font-weight:600;color:var(--gold);
  background:rgba(255,224,138,.08);border:1px solid rgba(255,224,138,.3);
  transition:.2s;
}
.btn-gk:hover{background:rgba(255,224,138,.18)}
.btn-gk.gold{
  color:#1a1305;background:linear-gradient(135deg,var(--gold),var(--gold-2));
  border:0;box-shadow:0 8px 22px rgba(224,168,60,.3);
}
.btn-gk.gold:hover{transform:translateY(-1px)}
.gk-status{font-size:14px;letter-spacing:2px;color:var(--gold);min-height:20px}
.gk-status.alarm{color:var(--cyan)}
.stat .opp{font-size:14px}
.btn-gk:disabled{opacity:.45;cursor:not-allowed}
.win-banner.won{color:var(--ok);text-shadow:0 0 18px rgba(126,224,138,.6)}

.gk-board-outer{display:flex;justify-content:center}
.gk-board{
  position:relative;
  border-radius:20px;
  background:var(--panel);border:1px solid var(--panel-line);
  box-shadow:var(--shadow);
  user-select:none;
}
.gk-grid{position:absolute;inset:0;pointer-events:none}
.gk-slot{position:absolute;width:30px;height:30px;cursor:pointer}

.ghost{
  position:absolute;left:50%;top:50%;width:20px;height:20px;
  transform:translate(-50%,-50%);border-radius:50%;
  opacity:0;transition:opacity .15s;pointer-events:none;
}
.gk-slot:hover .ghost{opacity:.35}
.ghost-b{background:#1c1509;border:1px solid rgba(255,214,130,.5)}
.ghost-w{background:#fdf6e3;border:1px solid rgba(179,166,132,.5)}

.stone{
  position:absolute;inset:1px;border-radius:50%;
  animation:stoneIn .2s both;
}
@keyframes stoneIn{from{transform:scale(.5);opacity:.4}to{transform:scale(1);opacity:1}}
.stone.black{
  background:radial-gradient(circle at 34% 30%, #4a3f22, #17120a 70%);
  border:1.5px solid rgba(255,214,130,.75);
  box-shadow:0 2px 8px rgba(10,6,0,.6), inset 0 0 6px rgba(255,224,138,.15);
}
.stone.white{
  background:radial-gradient(circle at 34% 30%, #fffdf5, #d9d0b8 75%);
  border:1.5px solid rgba(179,166,132,.6);
  box-shadow:0 2px 8px rgba(10,6,0,.5);
}
.stone.last .mark{
  position:absolute;left:50%;top:50%;width:8px;height:8px;
  transform:translate(-50%,-50%);border-radius:50%;
  background:rgba(255,107,129,.9);
  box-shadow:0 0 8px rgba(255,107,129,.7);
}
.stone.win{box-shadow:0 0 14px rgba(140,232,184,.9) !important;animation:winPulse 1s infinite}
@keyframes winPulse{0%,100%{filter:brightness(1)}50%{filter:brightness(1.5)}}
.gk-slot.win{background:rgba(140,232,184,.12);border-radius:8px}

.win-banner{
  position:absolute;inset:0;display:flex;align-items:center;justify-content:center;
  background:rgba(10,6,0,.55);backdrop-filter:blur(2px);
  border-radius:20px;color:var(--cyan);font-size:20px;font-weight:800;letter-spacing:6px;
  text-shadow:0 0 18px rgba(140,232,184,.6);
  pointer-events:none;
}

.fade-enter-active,.fade-leave-active{transition:opacity .25s}
.fade-enter-from,.fade-leave-to{opacity:0}
</style>
