<template>
  <div>
    <div class="adm-page-head">
      <h2>修士账号</h2>
      <p>共 {{ total }} 个账号 · 可改昵称、重置密码、清理账号</p>
      <span class="grow"></span>
      <button class="adm-btn" type="button" :disabled="loading" @click="load()">刷新列表</button>
    </div>

    <div class="adm-toolbar">
      <input
        v-model="query.keyword"
        class="adm-ipt search"
        type="text"
        placeholder="搜索 账号 / 昵称…"
        maxlength="40"
        @keyup.enter="load()"
      >
      <button class="adm-btn" type="button" @click="load()">查 询</button>
      <button class="adm-btn" type="button" @click="reset">重 置</button>
      <span class="grow"></span>
      <span class="adm-muted">管理员账号受保护，不可删除</span>
    </div>

    <AdminTable
      v-model:page="page"
      :columns="columns"
      :rows="pageRows"
      :loading="loading"
      empty-text="没有找到账号"
      :page-size="PAGE_SIZE"
      :total="total"
      :page-count="pageCount"
    >
      <template #col-username="{ row }">
        <span class="adm-name" :title="row.username">{{ row.username }}</span>
      </template>
      <template #col-points="{ row }"><b style="color:var(--gold)">{{ row.points }}</b></template>
      <template #col-role="{ row }">
        <span class="adm-tag" :style="row.admin ? adminTagStyle() : userTagStyle()">{{ row.admin ? '管理员' : '修士' }}</span>
      </template>
      <template #col-createdAt="{ row }"><span class="adm-muted">{{ row.createdAt || '—' }}</span></template>
      <template #actions="{ row }">
        <button class="adm-op" type="button" @click="openRename(row)">改昵称</button>
        <button class="adm-op" type="button" @click="openPassword(row)">重置密码</button>
        <button
          class="adm-op del"
          type="button"
          :disabled="row.admin"
          :title="row.admin ? '管理员账号不可删除' : ''"
          @click="askDelete(row)"
        >删除</button>
      </template>
    </AdminTable>

    <AdminFormModal
      :open="rename.open"
      :title="`修改昵称 · ${rename.username}`"
      :fields="renameFields"
      :initial="rename.form"
      :saving="saving"
      @submit="doRename"
      @cancel="rename.open = false"
    />

    <AdminFormModal
      :open="pwd.open"
      :title="`重置密码 · ${pwd.username}`"
      :fields="passwordFields"
      :initial="pwd.form"
      :saving="saving"
      @submit="doResetPassword"
      @cancel="pwd.open = false"
    />

    <AdminConfirm
      :open="!!deleting"
      title="删除账号"
      :message="deleting ? `确定删除账号「<b>${deleting.username}</b>」吗？<br><span class='warn'>该账号的 ${deleting.questCount} 条接取记录与 ${deleting.realmCount} 条报名记录会一并清除，积分随之失效，且不可恢复。</span>` : ''"
      confirm-text="确认删除"
      danger
      :loading="saving"
      @confirm="doDelete"
      @cancel="deleting = null"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import AdminTable from './components/AdminTable.vue'
import AdminFormModal from './components/AdminFormModal.vue'
import AdminConfirm from './components/AdminConfirm.vue'
import { useCrudPage } from './composables/useCrudPage.js'
import { userApi } from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const PAGE_SIZE = 10

const { show } = useToast()
const {
  loading, page, pageCount, pageRows, total, query,
  load, reset, fail
} = useCrudPage({ list: userApi.list, pageSize: PAGE_SIZE, filters: ['keyword'] })

const columns = [
  { key: 'username', label: '账号' },
  { key: 'nickname', label: '昵称' },
  { key: 'points', label: '积分', width: '70px' },
  { key: 'questCount', label: '接取任务', width: '86px' },
  { key: 'realmCount', label: '报名秘境', width: '86px' },
  { key: 'role', label: '角色', width: '84px' },
  { key: 'createdAt', label: '注册时间', width: '136px' }
]

const saving = ref(false)
const deleting = ref(null)

const renameFields = [{ key: 'nickname', label: '新昵称', type: 'text', required: true, maxlength: 64, placeholder: '2-64 个字符' }]
const passwordFields = [
  { key: 'password', label: '新密码', type: 'password', required: true, maxlength: 20, placeholder: '6-20 位' },
  { key: 'confirm', label: '确认新密码', type: 'password', required: true, maxlength: 20, placeholder: '再次输入' }
]

const rename = reactive({ open: false, id: null, username: '', form: {} })
const pwd = reactive({ open: false, id: null, username: '', form: {} })

onMounted(load)

function openRename(row) {
  rename.open = true
  rename.id = row.id
  rename.username = row.username
  rename.form = { nickname: row.nickname || '' }
}

function openPassword(row) {
  pwd.open = true
  pwd.id = row.id
  pwd.username = row.username
  pwd.form = { password: '', confirm: '' }
}

async function doRename(data) {
  saving.value = true
  try {
    await userApi.rename(rename.id, (data.nickname || '').trim())
    show('昵称已更新', 'ok')
    rename.open = false
    load(true)
  } catch (e) {
    fail(e, '修改失败')
  } finally {
    saving.value = false
  }
}

async function doResetPassword(data) {
  const pw = (data.password || '').trim()
  if (pw.length < 6 || pw.length > 20) {
    show('新密码需 6-20 位', 'err')
    return
  }
  if (pw !== (data.confirm || '').trim()) {
    show('两次输入的新密码不一致', 'err')
    return
  }
  saving.value = true
  try {
    await userApi.resetPassword(pwd.id, pw)
    show('密码已重置，请通知该修士使用新密码登录', 'ok')
    pwd.open = false
  } catch (e) {
    fail(e, '重置失败')
  } finally {
    saving.value = false
  }
}

function askDelete(row) {
  if (row.admin) {
    show('管理员账号不可删除', 'err')
    return
  }
  deleting.value = row
}

async function doDelete() {
  if (!deleting.value) return
  saving.value = true
  try {
    await userApi.remove(deleting.value.id)
    show('账号已删除', 'ok')
    deleting.value = null
    load(true)
  } catch (e) {
    fail(e, '删除失败')
  } finally {
    saving.value = false
  }
}

function adminTagStyle() {
  return { color: '#ffe08a', borderColor: 'rgba(255,224,138,.4)', background: 'rgba(255,224,138,.12)' }
}

function userTagStyle() {
  return { color: '#8ce8b8', borderColor: 'rgba(140,232,184,.35)', background: 'rgba(140,232,184,.1)' }
}
</script>
