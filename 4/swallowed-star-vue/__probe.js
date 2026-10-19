// 临时验收探针：挂载真实 Home.vue，通过 Vite 代理连接真实后端接口。
// 用法：http://localhost:5173/__probe.html?q=雷&cat=宝术&grade=绝顶&sort=name
import { createApp, h } from 'vue'
import { createRouter, createWebHistory, RouterView } from 'vue-router'
import './src/style.css'
import Home from './src/views/Home.vue'

const log = (m) => { const p = document.createElement('div'); p.className = 'probe-log'; p.textContent = m; document.body.appendChild(p) }

// 先取 URL 参数：后面 router.push('/home') 会清掉 location.search
const params = new URLSearchParams(location.search)

// 1) 真实调用后端登录接口拿到真 token
const loginRes = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: '123456' })
})
const loginJson = await loginRes.json()
log('login code=' + loginJson.code)
localStorage.setItem('ss_token', loginJson.data.token)
localStorage.setItem('ss_session', JSON.stringify({ username: loginJson.data.username, nickname: loginJson.data.nickname }))

// 2) 挂载真实 Home.vue（运行时版 Vue 不支持 template 字符串，这里用渲染函数）
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/home', component: Home },
    { path: '/login', component: { render: () => h('div', { id: 'probe-login' }, 'LOGIN_PAGE') } },
    { path: '/:pathMatch(.*)*', component: { render: () => h('div', 'NOT_FOUND') } }
  ]
})
const app = createApp({ render: () => h(RouterView) })
app.use(router)
await router.push('/home')
await router.isReady()
app.mount('#app')

const wait = (ms) => new Promise((r) => setTimeout(r, ms))
await wait(1500) // 等首次 /api/gongfa/home 返回并渲染

// 3) 可选：模拟用户操作（关键词搜索 / 分类筛选 / 分级筛选 / 排序）
const clickChip = (label) => {
  const chips = [...document.querySelectorAll('.filter-chip')]
  const hit = chips.find((c) => c.textContent.trim().startsWith(label))
  if (hit) { hit.click(); log('clicked chip: ' + label) } else { log('chip NOT FOUND: ' + label) }
}

const q = params.get('q')
if (q) {
  const input = document.querySelector('.search-wrap input')
  input.value = q
  input.dispatchEvent(new Event('input', { bubbles: true }))
  document.querySelector('.btn-search').click()
  log('searched: ' + q)
  await wait(1500)
}
if (params.get('cat')) { clickChip(params.get('cat')); await wait(1500) }
if (params.get('grade')) { clickChip(params.get('grade')); await wait(1500) }
if (params.get('sort')) { clickChip(params.get('sort')); await wait(1500) }

log('probe done')
