// 「我的主页」接口层（对接 Spring Boot 后端 /api/profile，全部需登录）
// 开发环境通过 Vite 代理把 /api 转发到 http://localhost:8081
const BASE = '/api'

// 统一带上登录 token
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

function get(path) {
  return request(path, { method: 'GET', headers: authHeaders() })
}

/** 主页总览（账号 + 积分 + 境界进度 + 成就徽章 + 修炼动态） */
export function summaryApi() {
  return get('/profile/summary')
}

/** 我接取的任务明细 */
export function myQuestsApi() {
  return get('/profile/quests')
}

/** 我报名的秘境明细 */
export function myRealmsApi() {
  return get('/profile/realms')
}
