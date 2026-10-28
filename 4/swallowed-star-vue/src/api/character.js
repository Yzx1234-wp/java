// 「大荒人物志」人物介绍接口层（对接 Spring Boot 后端 /api/characters）
// 开发环境通过 Vite 代理把 /api 转发到 http://localhost:8081
const BASE = '/api'

// 过滤掉空值参数，避免发出 keyword=&faction= 这类无意义查询
function buildQuery(params) {
  if (!params) return ''
  const sp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== '') sp.append(k, v)
  })
  const s = sp.toString()
  return s ? '?' + s : ''
}

async function get(path, params) {
  const token = localStorage.getItem('ss_token')
  const headers = {}
  if (token) headers['Authorization'] = 'Bearer ' + token

  const res = await fetch(BASE + path + buildQuery(params), { method: 'GET', headers })

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

/**
 * 人物检索（分级分类搜索）
 * @param {{keyword?:string, faction?:string, grade?:string, sortBy?:string}} query
 */
export function listApi(query = {}) {
  return get('/characters/list', query)
}

/** 势力筛选项（含数量） */
export function factionsApi() {
  return get('/characters/factions')
}

/** 定位分级筛选项（含数量） */
export function gradesApi() {
  return get('/characters/grades')
}

/** 热门人物姓名 */
export function hotApi() {
  return get('/characters/hot')
}

/** 人物详情 */
export function detailApi(id) {
  return get('/characters/' + id)
}
