// 「大荒宝库 · 商店兑换」接口层（对接 Spring Boot 后端 /api/shop）
// 开发环境通过 Vite 代理把 /api 转发到后端 8081
const BASE = '/api'

// 过滤空值参数
function buildQuery(params) {
  if (!params) return ''
  const sp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== '') sp.append(k, v)
  })
  const s = sp.toString()
  return s ? '?' + s : ''
}

// 统一带上登录 token（商品浏览公开，兑换 / 钱包 / 记录需登录）
function authHeaders(extra = {}) {
  const headers = { ...extra }
  const token = localStorage.getItem('ss_token')
  if (token) headers['Authorization'] = 'Bearer ' + token
  return headers
}

async function request(path, options = {}) {
  const res = await fetch(BASE + path, options)

  let data = {}
  try { data = await res.json() } catch { /* 忽略空响应 */ }

  if (!res.ok || (data && data.code !== undefined && data.code !== 0)) {
    const err = new Error((data && data.message) || '请求失败，请确认后端服务已启动')
    err.code = (data && data.code) || res.status
    throw err
  }
  return data
}

function get(path, params) {
  return request(path + buildQuery(params), { method: 'GET', headers: authHeaders() })
}

/**
 * 商品检索
 * @param {{keyword?:string, category?:string, sortBy?:string}} query
 */
export function listApi(query = {}) {
  return get('/shop/list', query)
}

/** 分类筛选项（含数量） */
export function categoriesApi() {
  return get('/shop/categories')
}

/** 我的积分钱包（累计获得 / 已消耗 / 可用余额） */
export function walletApi() {
  return get('/shop/wallet')
}

/** 我的兑换记录 */
export function ordersApi() {
  return get('/shop/orders')
}

/** 兑换商品（扣积分、扣库存、写兑换记录） */
export function redeemApi(itemId) {
  return request('/shop/redeem', {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ itemId })
  })
}
