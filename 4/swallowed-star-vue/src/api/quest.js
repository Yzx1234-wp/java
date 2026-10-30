// 「大荒悬赏 · 任务接取」接口层（对接 Spring Boot 后端 /api/quests）
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

// 统一带上登录 token（任务浏览公开，接取需登录）
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
 * 任务检索（分级搜索；带 token 时返回是否已接取标记）
 * @param {{keyword?:string, grade?:string, sortBy?:string}} query
 */
export function listApi(query = {}) {
  return get('/quests/list', query)
}

/** 等级筛选项（含数量） */
export function gradesApi() {
  return get('/quests/grades')
}

/** 热门任务名称 */
export function hotApi() {
  return get('/quests/hot')
}

/** 任务详情 */
export function detailApi(id) {
  return get('/quests/' + id)
}

/** 我的总积分 */
export function pointsApi() {
  return get('/quests/points')
}

/** 我的接取列表 */
export function myApi() {
  return get('/quests/my')
}

/** 接取任务（接取即发积分） */
export function acceptApi(questId) {
  return request('/quests/accept', {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ questId })
  })
}

/** 取消接取（删除接取记录并撤销接取时发放的积分） */
export function cancelApi(questId) {
  return request('/quests/accept/' + questId, {
    method: 'DELETE',
    headers: authHeaders()
  })
}
