// 首页「大荒功法库」接口层（对接 Spring Boot 后端）
// 开发环境通过 Vite 代理把 /api 转发到 http://localhost:8081
const BASE = '/api'

// 过滤掉空值参数，避免发出 keyword=&cat= 这类无意义查询
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

/** 首页聚合数据：统计 + 分类/分级筛选项 + 热门典籍 + 默认列表 */
export function homeApi() {
  return get('/gongfa/home')
}

/**
 * 功法检索（分级分类搜索）
 * @param {{keyword?:string, cat?:string, grade?:string, sortBy?:string}} query
 */
export function listApi(query = {}) {
  return get('/gongfa/list', query)
}

/** 分类筛选项（含数量） */
export function categoriesApi() {
  return get('/gongfa/categories')
}

/** 品阶分级筛选项（含数量） */
export function gradesApi() {
  return get('/gongfa/grades')
}

/** 典籍详情 */
export function detailApi(id) {
  return get('/gongfa/' + id)
}
