<template>
  <div class="adm-shell">
    <!-- 左侧导航 -->
    <aside class="adm-side">
      <div class="adm-brand">
        <span class="sigil">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/><circle cx="12" cy="16" r="1.4"/>
          </svg>
        </span>
        <div>
          <b>大荒后台</b>
          <span>PERFECT WORLD</span>
        </div>
      </div>

      <nav class="adm-nav">
        <button
          v-for="m in menus"
          :key="m.path"
          class="adm-nav-item"
          :class="{ active: route.path === m.path }"
          type="button"
          @click="router.push(m.path)"
        >
          <span v-html="m.icon"></span>
          <span class="label">{{ m.label }}</span>
          <span v-if="counts[m.countKey]" class="cnt">{{ counts[m.countKey] }}</span>
        </button>
      </nav>

      <div class="adm-side-foot">
        完美世界 · 数据管理控制台<br>
        <span style="color:rgba(179,166,132,.6)">v2.0 · 六模块</span>
      </div>
    </aside>

    <!-- 右侧主区 -->
    <div class="adm-main">
      <header class="adm-topbar">
        <h1>{{ pageTitle }}</h1>
        <span class="crumb">/ 大荒后台</span>
        <span class="spacer"></span>

        <button class="adm-icon-btn" type="button" @click="router.push('/home')">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/>
          </svg>
          返回前台
        </button>

        <span class="adm-chip">
          <span class="av">{{ avatarChar }}</span>
          {{ adminName }}
        </span>

        <button class="adm-icon-btn danger" type="button" @click="doLogout">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>
          </svg>
          退出
        </button>
      </header>

      <div class="adm-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminLogout, adminOverviewApi, fetchAdminMe, getAdminSession } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'
import './admin.css'

const route = useRoute()
const router = useRouter()
const { show } = useToast()

const ICONS = {
  dashboard: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1.6"/><rect x="14" y="3" width="7" height="7" rx="1.6"/><rect x="3" y="14" width="7" height="7" rx="1.6"/><rect x="14" y="14" width="7" height="7" rx="1.6"/></svg>',
  gongfa: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H20v18H6.5A2.5 2.5 0 0 0 4 22z"/><path d="M8 7h8M8 11h6"/></svg>',
  characters: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8" r="3.2"/><path d="M2.5 20c0-3.3 2.9-5.4 6.5-5.4s6.5 2.1 6.5 5.4"/><path d="M17 5.2a3 3 0 0 1 0 5.6"/><path d="M18.5 14.6c2 .6 3.2 2.2 3.2 4.4"/></svg>',
  realms: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3a14 14 0 0 1 0 18 14 14 0 0 1 0-18z"/></svg>',
  quests: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 21V4"/><path d="M5 4h11l-2 4 2 4H5"/></svg>',
  users: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M15 7a4 4 0 1 0-6.9 2.7"/><path d="M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"/><path d="M19 21v-1a5 5 0 0 0-5-5h-4a5 5 0 0 0-5 5v1"/><circle cx="18.5" cy="9.5" r="2.2"/></svg>'
}

const menus = [
  { path: '/admin/dashboard', label: '数据概览', icon: ICONS.dashboard, countKey: '' },
  { path: '/admin/gongfa', label: '功法管理', icon: ICONS.gongfa, countKey: 'gongfaCount' },
  { path: '/admin/characters', label: '人物管理', icon: ICONS.characters, countKey: 'characterCount' },
  { path: '/admin/realms', label: '秘境管理', icon: ICONS.realms, countKey: 'realmCount' },
  { path: '/admin/quests', label: '悬赏任务', icon: ICONS.quests, countKey: 'questCount' },
  { path: '/admin/users', label: '修士账号', icon: ICONS.users, countKey: 'userCount' }
]

const counts = reactive({})

const adminName = ref('管理员')
const avatarChar = computed(() => (adminName.value || '管').trim().slice(0, 1))
const pageTitle = computed(() => route.meta.title || '后台管理')

onMounted(async () => {
  const s = getAdminSession()
  if (s) adminName.value = s.nickname || s.username
  try {
    const me = await fetchAdminMe()
    if (me) adminName.value = me.nickname || me.username
  } catch (e) {
    adminLogout()
    show(e.message || '登录已失效，请重新登录', 'err')
    router.replace('/admin/login')
    return
  }
  loadCounts()
})

watch(() => route.path, loadCounts)

async function loadCounts() {
  try {
    const res = await adminOverviewApi()
    const d = res.data || {}
    Object.assign(counts, {
      gongfaCount: d.gongfaCount || 0,
      characterCount: d.characterCount || 0,
      realmCount: d.realmCount || 0,
      questCount: d.questCount || 0,
      userCount: d.userCount || 0
    })
  } catch { /* 概览失败不影响使用 */ }
}

function doLogout() {
  adminLogout()
  router.replace('/admin/login')
}
</script>
