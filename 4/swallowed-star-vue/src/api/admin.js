// 后台管理接口层（对接 Spring Boot 后端 /api/admin/*）
// 与前台会话相互独立：后台令牌存于 localStorage 的 ss_admin_token
const BASE = '/api'

const ADMIN_TOKEN_KEY = 'ss_admin_token'
const ADMIN_SESSION_KEY = 'ss_admin_session'

export function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY)
}

export function getAdminSession() {
  try { return JSON.parse(localStorage.getItem(ADMIN_SESSION_KEY)) } catch { return null }
}

async function request(method, path, body) {
  const token = getAdminToken()
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = 'Bearer ' + token

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })

  let data = {}
  try { data = await res.json() } catch { /* 忽略空响应 */ }

  if (!res.ok || (data && data.code !== undefined && data.code !== 0)) {
    const err = new Error((data && data.message) || '请求失败，请确认后端服务已启动')
    err.code = (data && data.code) || res.status
    throw err
  }
  return data
}

// ---------- 登录 / 登出（复用 /api/auth/login 签发令牌，但独立存放） ----------

export async function adminLogin(username, password) {
  const res = await request('POST', '/auth/login', { username, password })
  if (!res || !res.data) return false
  const { token, username: un, nickname } = res.data
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
  localStorage.setItem(ADMIN_SESSION_KEY, JSON.stringify({ username: un, nickname: nickname || un }))
  return true
}

export function adminLogout() {
  try { request('POST', '/auth/logout') } catch { /* 忽略网络错误 */ }
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  localStorage.removeItem(ADMIN_SESSION_KEY)
}

// ---------- 功法典籍管理（JSON 字段名与首页卡片一致：from / rank / desc / hot） ----------

/** 列表（后端全量返回，前端做分页展示） */
export function adminListApi(query = {}) {
  const sp = new URLSearchParams()
  Object.entries(query).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== '') sp.append(k, v)
  })
  const s = sp.toString()
  return request('GET', '/admin/gongfa/list' + (s ? '?' + s : ''))
}

/** 新增功法典籍 */
export function adminCreateApi(gongfa) {
  return request('POST', '/admin/gongfa', gongfa)
}

/** 修改功法典籍 */
export function adminUpdateApi(id, gongfa) {
  return request('PUT', '/admin/gongfa/' + id, gongfa)
}

/** 删除功法典籍 */
export function adminDeleteApi(id) {
  return request('DELETE', '/admin/gongfa/' + id)
}
