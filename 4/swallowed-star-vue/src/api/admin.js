// 后台管理接口层（对接 Spring Boot 后端 /api/admin/*）
// 与前台会话完全独立：后台令牌存于 localStorage 的 ss_admin_token
const BASE = '/api'

const TOKEN_KEY = 'ss_admin_token'
const SESSION_KEY = 'ss_admin_session'

export function getAdminToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function getAdminSession() {
  try { return JSON.parse(localStorage.getItem(SESSION_KEY)) } catch { return null }
}

/** 是否属于「登录态失效 / 无权限」，页面据此跳回登录页 */
export function isAuthError(e) {
  return !!e && (e.code === 401 || e.code === 403)
}

async function request(method, path, body) {
  const token = getAdminToken()
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = 'Bearer ' + token

  let res
  try {
    res = await fetch(BASE + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch {
    const err = new Error('无法连接后端服务，请确认后端已启动（8081 端口）')
    err.code = 0
    throw err
  }

  let data = {}
  try { data = await res.json() } catch { /* 空响应 */ }

  if (!res.ok || (data && data.code !== undefined && data.code !== 0)) {
    const err = new Error((data && data.message) || `请求失败（HTTP ${res.status}）`)
    err.code = (data && data.code) || res.status
    throw err
  }
  return data
}

// ---------- 登录 / 登出 ----------

/**
 * 后台登录：先走 /auth/login 取令牌，再用 /admin/me 校验后台权限；
 * 普通账号即使密码正确也会被拒绝并清理本地令牌。
 */
export async function adminLogin(username, password) {
  const res = await request('POST', '/auth/login', { username, password })
  if (!res || !res.data || !res.data.token) {
    throw new Error('账号或密码不正确')
  }
  localStorage.setItem(TOKEN_KEY, res.data.token)
  try {
    const me = await request('GET', '/admin/me')
    const info = me.data || {}
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      username: info.username || res.data.username,
      nickname: info.nickname || res.data.nickname || res.data.username,
      role: info.role || 'admin'
    }))
    return true
  } catch (e) {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(SESSION_KEY)
    throw e
  }
}

/** 校验本地令牌是否仍然有效，并刷新会话信息 */
export async function fetchAdminMe() {
  const res = await request('GET', '/admin/me')
  if (res && res.data) {
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      username: res.data.username,
      nickname: res.data.nickname || res.data.username,
      role: res.data.role || 'admin'
    }))
  }
  return res.data
}

export function adminLogout() {
  try { request('POST', '/auth/logout') } catch { /* 忽略 */ }
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(SESSION_KEY)
}

// ---------- 概览 ----------

export function adminOverviewApi() {
  return request('GET', '/admin/overview')
}

// ---------- 小工具 ----------

function qs(query = {}) {
  const sp = new URLSearchParams()
  Object.entries(query).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== '') sp.append(k, v)
  })
  const s = sp.toString()
  return s ? '?' + s : ''
}

/** 为某个资源生成一套标准 CRUD 接口 */
function crud(base, extra = {}) {
  return {
    list: (query) => request('GET', base + '/list' + qs(query)),
    create: (body) => request('POST', base, body),
    update: (id, body) => request('PUT', base + '/' + id, body),
    remove: (id) => request('DELETE', base + '/' + id),
    batchRemove: (ids) => request('POST', base + '/batch-delete', { ids }),
    toggleHot: (id) => request('POST', base + '/' + id + '/toggle-hot'),
    ...extra
  }
}

// ---------- 功法典籍（JSON 字段：from / rank / desc / hot） ----------

export const gongfaApi = crud('/admin/gongfa', {
  categories: () => request('GET', '/admin/gongfa/categories')
})

// ---------- 大荒人物志（简介字段是 description，注意与功法不同） ----------

export const characterApi = crud('/admin/characters', {
  factions: () => request('GET', '/admin/characters/factions')
})

// ---------- 大荒秘境（含报名记录） ----------

export const realmApi = crud('/admin/realms', {
  signups: (id) => request('GET', `/admin/realms/${id}/signups`),
  removeSignup: (signupId) => request('DELETE', `/admin/realms/signups/${signupId}`)
})

// ---------- 大荒悬赏（含接取记录） ----------

export const questApi = crud('/admin/quests', {
  accepts: (id) => request('GET', `/admin/quests/${id}/accepts`),
  removeAccept: (acceptId) => request('DELETE', `/admin/quests/accepts/${acceptId}`)
})

// ---------- 修士账号 ----------

export const userApi = {
  list: (query) => request('GET', '/admin/users/list' + qs(query)),
  rename: (id, nickname) => request('PUT', '/admin/users/' + id, { nickname }),
  resetPassword: (id, password) => request('PUT', `/admin/users/${id}/password`, { password }),
  remove: (id) => request('DELETE', '/admin/users/' + id)
}
