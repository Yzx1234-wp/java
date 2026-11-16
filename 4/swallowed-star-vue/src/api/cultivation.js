// 「修炼境界 · 问道台」接口层（对接 Spring Boot 后端 /api/cultivation）
// 开发环境通过 Vite 代理把 /api 转发到 http://192.168.68.71:8081
const BASE = '/api'

// 统一带上登录 token（修炼相关接口全部需登录）
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

/** 修炼总览：境界 / 修为进度 / 渡劫成功率 / 十八境阶梯 / 可参悟典籍 */
export function overviewApi() {
  return request('/cultivation/overview', { method: 'GET', headers: authHeaders() })
}

/** 参悟功法（每部典籍仅可参悟一次） */
export function comprehendApi(gongfaId) {
  return request('/cultivation/comprehend', {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify({ gongfaId })
  })
}

/** 打坐吐纳（30 秒冷却） */
export function meditateApi() {
  return request('/cultivation/meditate', { method: 'POST', headers: authHeaders() })
}

/** 冲击境界（渡劫）：成功晋升，失败修为折损 */
export function breakthroughApi() {
  return request('/cultivation/breakthrough', { method: 'POST', headers: authHeaders() })
}

/** 修炼记录 */
export function logsApi(limit = 20) {
  return request('/cultivation/logs?limit=' + limit, { method: 'GET', headers: authHeaders() })
}
