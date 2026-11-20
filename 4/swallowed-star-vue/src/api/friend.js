// 「大荒交友录 · 好友」接口层（对接 Spring Boot 后端 /api/friends）
// 开发环境通过 Vite 代理把 /api 转发到后端 8081
const BASE = '/api/friends'

function authHeaders(extra = {}) {
  const headers = { ...extra }
  const token = localStorage.getItem('ss_token')
  if (token) headers['Authorization'] = 'Bearer ' + token
  return headers

}

async function request(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    headers: authHeaders(body ? { 'Content-Type': 'application/json' } : {}),
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

/* ===== 好友 ===== */

export function searchUsersApi(keyword) {
  return request('GET', '/search?keyword=' + encodeURIComponent(keyword || ''))
}

export function sendFriendRequestApi(username) {
  return request('POST', '/request', { username })
}

export function friendRequestsApi() {
  return request('GET', '/requests')
}

export function acceptFriendRequestApi(id) {
  return request('POST', `/request/${id}/accept`)
}

export function declineFriendRequestApi(id) {
  return request('POST', `/request/${id}/decline`)
}

export function friendListApi() {
  return request('GET', '/list')
}

export function removeFriendApi(username) {
  return request('DELETE', '/' + encodeURIComponent(username))
}

/* ===== 聊天 ===== */

export function chatHistoryApi(username, afterId = 0) {
  return request('GET', `/chat/${encodeURIComponent(username)}?afterId=${afterId}`)
}

export function sendChatApi(username, content) {
  return request('POST', `/chat/${encodeURIComponent(username)}`, { content })
}

export function chatUnreadApi() {
  return request('GET', '/chat/unread')
}

/* ===== 好友对战 ===== */

export function battleListApi() {
  return request('GET', '/battle/list')
}

export function createBattleApi(friend, game, stake) {
  return request('POST', '/battle', { friend, game, stake })
}

export function acceptBattleApi(id) {
  return request('POST', `/battle/${id}/accept`)
}

export function declineBattleApi(id) {
  return request('POST', `/battle/${id}/decline`)
}

export function battleDetailApi(id) {
  return request('GET', `/battle/${id}`)
}

export function battleMoveApi(id, move) {
  return request('POST', `/battle/${id}/move`, { move })
}

export function battleFinishApi(id, result) {
  return request('POST', `/battle/${id}/finish`, { result })
}
