<template>
  <div>
    <div class="adm-page-head">
      <h2>数据概览</h2>
      <p>大荒世界内容与修士动态总览</p>
      <span class="grow"></span>
      <button class="adm-btn ghost" type="button" :disabled="loading" @click="load">
        {{ loading ? '刷新中…' : '刷新数据' }}
      </button>
    </div>

    <!-- 内容体量 -->
    <div class="adm-stats">
      <div class="adm-stat">
        <b>{{ n('gongfaCount') }}</b>
        <span>功法典籍</span>
        <em class="hint">首页「大荒功法库」</em>
      </div>
      <div class="adm-stat">
        <b>{{ n('characterCount') }}</b>
        <span>大荒人物</span>
        <em class="hint">人物介绍页</em>
      </div>
      <div class="adm-stat">
        <b>{{ n('realmCount') }}</b>
        <span>秘境</span>
        <em class="hint">秘境首页</em>
      </div>
      <div class="adm-stat">
        <b>{{ n('questCount') }}</b>
        <span>悬赏任务</span>
        <em class="hint">任务接取页</em>
      </div>
    </div>

    <!-- 修士数据 -->
    <div class="adm-stats">
      <div class="adm-stat">
        <b>{{ n('userCount') }}</b>
        <span>修士账号</span>
        <em class="hint">已注册用户</em>
      </div>
      <div class="adm-stat">
        <b>{{ n('signupCount') }}</b>
        <span>秘境报名</span>
        <em class="hint">累计报名记录</em>
      </div>
      <div class="adm-stat">
        <b>{{ n('acceptCount') }}</b>
        <span>任务接取</span>
        <em class="hint">累计接取记录</em>
      </div>
      <div class="adm-stat">
        <b>{{ n('totalPoints') }}</b>
        <span>累计发放积分</span>
        <em class="hint">按接取留档积分求和</em>
      </div>
    </div>

    <div class="adm-two-col" style="margin-bottom:14px">
      <div class="adm-panel">
        <h4>功法分类分布</h4>
        <div v-if="bars(gongfaByCat).length" class="adm-bars">
          <div v-for="b in bars(gongfaByCat)" :key="b.name" class="adm-bar-row">
            <span>{{ b.name }}</span>
            <span class="adm-bar"><i :style="{ width: b.pct + '%' }"></i></span>
            <b>{{ b.cnt }}</b>
          </div>
        </div>
        <p v-else class="adm-feed-empty">暂无数据</p>
      </div>

      <div class="adm-panel">
        <h4>秘境等级分布</h4>
        <div v-if="bars(realmByGrade).length" class="adm-bars">
          <div v-for="b in bars(realmByGrade)" :key="b.name" class="adm-bar-row">
            <span>{{ b.name }}</span>
            <span class="adm-bar"><i :style="{ width: b.pct + '%' }"></i></span>
            <b>{{ b.cnt }}</b>
          </div>
        </div>
        <p v-else class="adm-feed-empty">暂无数据</p>
      </div>
    </div>

    <div class="adm-two-col">
      <div class="adm-panel">
        <h4>最近秘境报名</h4>
        <div v-if="recentSignups.length" class="adm-feed">
          <div v-for="r in recentSignups" :key="r.id" class="adm-feed-row">
            <span class="dot"></span>
            <span class="who">{{ r.username }}</span>
            <span class="what">报名「{{ r.target }}」</span>
            <span class="when">{{ r.time }}</span>
          </div>
        </div>
        <p v-else class="adm-feed-empty">暂无报名记录</p>
      </div>

      <div class="adm-panel">
        <h4>最近任务接取</h4>
        <div v-if="recentAccepts.length" class="adm-feed">
          <div v-for="r in recentAccepts" :key="r.id" class="adm-feed-row">
            <span class="dot"></span>
            <span class="who">{{ r.username }}</span>
            <span class="what">接取「{{ r.target }}」+{{ r.points }}</span>
            <span class="when">{{ r.time }}</span>
          </div>
        </div>
        <p v-else class="adm-feed-empty">暂无接取记录</p>
      </div>
    </div>

    <div class="adm-quick">
      <button v-for="q in quicks" :key="q.path" type="button" @click="router.push(q.path)">
        <span v-html="q.icon"></span>
        <span>{{ q.label }}<span class="k">{{ q.hint }}</span></span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminOverviewApi, isAuthError } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const router = useRouter()
const { show } = useToast()

const loading = ref(false)
const data = ref({})

const gongfaByCat = ref([])
const realmByGrade = ref([])
const recentSignups = ref([])
const recentAccepts = ref([])

const quicks = [
  { path: '/admin/gongfa', label: '新增功法', hint: '扩充大荒功法库', icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.8" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>' },
  { path: '/admin/characters', label: '维护人物志', hint: '人物介绍页数据', icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#8ce8b8" stroke-width="1.8" stroke-linecap="round"><circle cx="12" cy="8" r="3.4"/><path d="M5 20c0-3.4 3.1-5.6 7-5.6s7 2.2 7 5.6"/></svg>' },
  { path: '/admin/realms', label: '秘境与报名', hint: '查看报名记录', icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.8" stroke-linecap="round"><circle cx="12" cy="12" r="8.5"/><path d="M3.5 12h17"/></svg>' },
  { path: '/admin/users', label: '修士账号', hint: '重置密码 / 清理账号', icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ff9d7a" stroke-width="1.8" stroke-linecap="round"><rect x="3" y="10" width="18" height="11" rx="2"/><path d="M7.5 10V7a4.5 4.5 0 0 1 9 0v3"/></svg>' }
]

function n(key) {
  const v = data.value[key]
  return v === undefined || v === null ? '—' : v
}

function bars(list) {
  const arr = Array.isArray(list) ? list : []
  const max = Math.max(1, ...arr.map(x => Number(x.cnt) || 0))
  return arr
    .map(x => ({ name: x.name || '未分类', cnt: Number(x.cnt) || 0, pct: Math.round(((Number(x.cnt) || 0) / max) * 100) }))
    .sort((a, b) => b.cnt - a.cnt)
}

async function load() {
  loading.value = true
  try {
    const res = await adminOverviewApi()
    data.value = res.data || {}
    gongfaByCat.value = data.value.gongfaByCat || []
    realmByGrade.value = data.value.realmByGrade || []
    recentSignups.value = data.value.recentSignups || []
    recentAccepts.value = data.value.recentAccepts || []
  } catch (e) {
    if (isAuthError(e)) {
      show(e.message || '登录已失效，请重新登录', 'err')
      router.push('/admin/login')
      return
    }
    show(e.message || '概览加载失败', 'err')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
