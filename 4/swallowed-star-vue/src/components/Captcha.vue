<template>
  <canvas ref="cv" class="captcha-canvas" title="点击刷新验证码" @click="refresh"></canvas>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789' // 剔除易混淆字符
const COLORS = ['#ffe08a', '#9ef0c3', '#ffd08a', '#a8e6ff', '#ffb39b']
const W = 140, H = 46
const PAD = 10          // 画布左右留白，保证字符与光晕不贴边
const FONT = '"Segoe UI", system-ui, sans-serif'

const rnd = (a, b) => a + Math.random() * (b - a)
const rndInt = (a, b) => Math.floor(rnd(a, b + 1))
const pick = (s) => s[rndInt(0, s.length - 1)]

const cv = ref(null)
const code = ref('')

function draw(codeStr) {
  const canvas = cv.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = W * dpr
  canvas.height = H * dpr
  canvas.style.width = W + 'px'
  canvas.style.height = H + 'px'
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  ctx.clearRect(0, 0, W, H)

  const g = ctx.createLinearGradient(0, 0, W, H)
  g.addColorStop(0, '#16241a')
  g.addColorStop(1, '#33220e')
  ctx.fillStyle = g
  ctx.fillRect(0, 0, W, H)

  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = 'rgba(255,255,255,' + rnd(0.12, 0.7) + ')'
    ctx.fillRect(rnd(0, W), rnd(0, H), 1.3, 1.3)
  }
  for (let i = 0; i < 5; i++) {
    ctx.strokeStyle = 'rgba(' + rndInt(120, 255) + ',' + rndInt(150, 232) + ',' + rndInt(60, 160) + ',' + rnd(0.12, 0.4) + ')'
    ctx.lineWidth = rnd(0.6, 1.6)
    ctx.beginPath()
    ctx.moveTo(rnd(0, W), rnd(0, H))
    ctx.bezierCurveTo(rnd(0, W), rnd(0, H), rnd(0, W), rnd(0, H), rnd(0, W), rnd(0, H))
    ctx.stroke()
  }

  /* ===== 字符绘制：每个字符独占一个格子，字号按实测字宽自适应，绝不越界 ===== */
  const n = codeStr.length || 1
  const cell = (W - PAD * 2) / n        // 单格宽度
  const maxW = cell * 0.82              // 字符允许的最大宽度
  const maxFont = Math.min(H * 0.5, maxW / 0.66)
  const limitW = cell / 2 - 0.5         // 旋转外接框允许的半宽

  for (let i = 0; i < n; i++) {
    const ch = codeStr[i]

    // 逐级缩小字号，直到实测字宽落进格子
    let size = Math.floor(maxFont)
    ctx.font = 'bold ' + size + 'px ' + FONT
    let cw = ctx.measureText(ch).width
    while (cw > maxW && size > 10) {
      size -= 1
      ctx.font = 'bold ' + size + 'px ' + FONT
      cw = ctx.measureText(ch).width
    }

    // 旋转外接半宽超限时收拢旋转角，避免压到相邻格子或边框
    let rot = rnd(-0.2, 0.2)
    const halfRotW = Math.abs(cw / 2 * Math.cos(rot)) + Math.abs(size / 2 * Math.sin(rot))
    if (halfRotW > limitW) {
      rot = Math.sign(rot || 1) * Math.abs(rot) * (limitW / halfRotW) * 0.9
    }

    const cx = PAD + cell * (i + 0.5)                  // 格内居中
    const cy = H / 2 + rnd(-1.5, 1.5)

    ctx.save()
    ctx.translate(cx, cy)
    ctx.rotate(rot)
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    const color = pick(COLORS)
    ctx.fillStyle = color
    ctx.shadowColor = color
    ctx.shadowBlur = 6
    ctx.fillText(ch, 0, 0)
    ctx.restore()
  }

  for (let i = 0; i < 46; i++) {
    ctx.fillStyle = 'rgba(255,255,255,' + rnd(0.05, 0.22) + ')'
    ctx.fillRect(rnd(0, W), rnd(0, H), 1, 1)
  }
}

function refresh() {
  let c = ''
  for (let i = 0; i < 4; i++) c += pick(CHARS)
  code.value = c.toLowerCase()
  draw(c)
}

function verify(v) {
  return String(v || '').trim().toLowerCase() === code.value
}

onMounted(refresh)
defineExpose({ refresh, verify })
</script>
