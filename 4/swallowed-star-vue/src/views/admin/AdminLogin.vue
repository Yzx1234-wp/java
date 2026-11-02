<template>
  <main class="panel">
    <header class="brand">
      <div class="sigil">
        <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/><circle cx="12" cy="16" r="1.4"/>
        </svg>
      </div>
      <h1>后台管理</h1>
      <p>PERFECT <span class="dot">·</span> WORLD <span class="dot">·</span> 大荒数据管理控制台</p>
    </header>

    <form @submit.prevent="submit">
      <div class="field" :class="{ invalid: errors.username }">
        <span class="field-label">管理员账号</span>
        <div class="input-wrap">
          <span class="ico">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-6 8-6s8 2 8 6"/></svg>
          </span>
          <input v-model="form.username" type="text" placeholder="请输入管理员账号" maxlength="32" @input="errors.username=''">
        </div>
        <div class="field-error">{{ errors.username }}</div>
      </div>

      <div class="field" :class="{ invalid: errors.password }">
        <span class="field-label">管理密钥</span>
        <div class="input-wrap">
          <span class="ico">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
          </span>
          <input v-model="form.password" :type="showPw ? 'text' : 'password'" placeholder="请输入密码" maxlength="32" @input="errors.password=''">
          <button type="button" class="eye" :style="{ color: showPw ? 'var(--gold)' : '' }" aria-label="显示密码" @click="showPw=!showPw">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
        </div>
        <div class="field-error">{{ errors.password }}</div>
      </div>

      <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? '身份核验中…' : '进 入 后 台' }}</button>

      <p class="admin-hint">仅管理员账号可进入（默认 admin / 123456）；普通修士账号会被拒绝。</p>

      <div class="switch"><router-link to="/home">← 返回首页</router-link></div>
    </form>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin, fetchAdminMe, getAdminToken } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const router = useRouter()
const { show } = useToast()

const form = reactive({ username: '', password: '' })
const showPw = ref(false)
const loading = ref(false)
const errors = reactive({ username: '', password: '' })

onMounted(async () => {
  if (!getAdminToken()) return
  try {
    await fetchAdminMe()
    router.replace('/admin/dashboard')
  } catch { /* 令牌失效：留在登录页 */ }
})

async function submit() {
  errors.username = ''
  errors.password = ''
  let ok = true
  if (!form.username.trim()) { errors.username = '请输入管理员账号'; ok = false }
  if (!form.password) { errors.password = '请输入管理密钥'; ok = false }
  if (!ok || loading.value) return

  loading.value = true
  try {
    await adminLogin(form.username.trim(), form.password)
    show('身份核验通过，正在进入后台…', 'ok')
    router.push('/admin/dashboard')
  } catch (e) {
    if (e.code === 403) {
      errors.password = '该账号没有后台管理权限'
      show(e.message || '该账号没有后台管理权限', 'err')
    } else if (e.code === 401) {
      errors.password = '登录已失效，请重试'
      show(e.message || '登录已失效', 'err')
    } else {
      errors.password = '账号或密钥错误'
      show(e.message || '账号或管理密钥不正确', 'err')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-hint{
  margin:12px 0 0;font-size:11.5px;line-height:1.7;color:var(--muted);text-align:center;
}
</style>
