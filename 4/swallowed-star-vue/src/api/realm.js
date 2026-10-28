// 「大荒秘境录」秘境首页接口层（对接 Spring Boot 后端 /api/realms）
// 开发环境通过 Vite 代理把 /api 转发到 http://localhost:8081
const BASE = '/api'

// 过滤掉空值参数，避免发出 keyword=&grade= 这类无意义查询
function buildQuery(params) {
  if (!params) return ''
  const sp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== '') sp.append(k, v)
  })
  const s = sp.toString()
  return s ? '?' + s : ''
}

// 统一带上登录 token（秘境浏览公开，报名需登录）
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

  // 后端统一返回 { code, message, data }，code !== 0 视为业务失败
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
 * 秘境检索（分级搜索；带 token 时返回是否已报名标记）
 * @param {{keyword?:string, grade?:string, sortBy?:string}} query
 */
export function listApi(query = {}) {
  return get('/realms/list', query)
}

/** 等级筛选项（含数量） */
export function gradesApi() {
  return get('/realms/grades')
}

/** 热门秘境名称 */
export function hotApi() {
  return get('/realms/hot')
}

/** 秘境详情 */
export function detailApi(id) {
  return get('/realms/' + id)
}

/** 我的报名列表 */
export function myApi() {
  return get('/realms/my')
}

/** 报名秘境 */
export function signupApi(realmId) {
  return request('/realms/signup', {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ realmId })
  })
}

/** 取消报名 */
export function cancelApi(realmId) {
  return request('/realms/signup/' + realmId, { method: 'DELETE', headers: authHeaders() })
}
