<template>
  <div class="admin-shell">
    <!-- 顶栏 -->
    <header class="admin-topbar">
      <div class="tb-left">
        <span class="tb-mark">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffe08a" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/><circle cx="12" cy="16" r="1.4"/>
          </svg>
        </span>
        <b>大荒功法库 · 后台管理</b>
      </div>
      <div class="tb-right">
        <span class="tb-user">{{ adminName }}</span>
        <button class="tb-btn" @click="goHome">返回首页</button>
        <button class="tb-btn danger" @click="doLogout">退出登录</button>
      </div>
    </header>

    <main class="admin-main">
      <!-- 统计条 -->
      <div class="stat-row">
        <div class="stat"><b>{{ total }}</b><span>典籍总数</span></div>
        <div class="stat"><b>{{ catCount }}</b><span>一级分类</span></div>
        <div class="stat"><b>{{ gradeCount }}</b><span>品阶分级</span></div>
        <div class="stat"><b>{{ hotCount }}</b><span>热门典籍</span></div>
      </div>

      <!-- 工具条 -->
      <div class="tool-row">
        <input v-model="query.keyword" class="ipt" type="text" placeholder="搜索 名称 / 传承 / 修习者 / 简介…" maxlength="40" @keyup.enter="load">
        <select v-model="query.cat" class="ipt sel">
          <option value="">全部分类</option>
          <option v-for="c in CATS" :key="c" :value="c">{{ c }}</option>
        </select>
        <select v-model="query.grade" class="ipt sel">
          <option value="">全部品阶</option>
          <option v-for="g in GRADES" :key="g" :value="g">{{ g }}</option>
        </select>
        <button class="btn gold" @click="load">查 询</button>
        <button class="btn" @click="resetQuery">重 置</button>
        <span class="tool-spacer"></span>
        <button class="btn gold" @click="openCreate">+ 新增典籍</button>
      </div>

      <!-- 数据表格 -->
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th><th>图标</th><th>名称</th><th>分类</th><th>品阶</th><th>评分</th>
              <th>传承来源</th><th>修习者</th><th>热门</th><th>创建时间</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="11" class="empty">正在加载功法库数据…</td></tr>
            <tr v-else-if="pageRows.length === 0"><td colspan="11" class="empty">暂无数据，点击右上角「新增典籍」添加</td></tr>
            <tr v-for="row in pageRows" :key="row.id">
              <td>{{ row.id }}</td>
              <td><span class="glyph-cell">{{ row.glyph }}</span></td>
              <td class="name-cell" :title="row.tier">{{ row.name }}</td>
              <td><span class="cat-tag" :style="catStyle(row.cat)">{{ row.cat }}</span></td>
              <td>{{ row.grade }}</td>
              <td>{{ row.rank }}</td>
              <td>{{ row.from }}</td>
              <td>{{ row.owner }}</td>
              <td><span class="hot-yes" v-if="row.hot">热门</span><span class="hot-no" v-else>—</span></td>
              <td class="time-cell">{{ row.createdAt || '—' }}</td>
              <td>
                <button class="op" @click="openEdit(row)">编辑</button>
                <button class="op del" @click="confirmDelete(row)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="page-row" v-if="filteredTotal > PAGE_SIZE">
        <button class="btn" :disabled="page <= 1" @click="page--">上一页</button>
        <span class="page-info">第 {{ page }} / {{ pageCount }} 页 · 共 {{ filteredTotal }} 条</span>
        <button class="btn" :disabled="page >= pageCount" @click="page++">下一页</button>
      </div>
    </main>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="closeModal">
      <div class="modal">
        <h3>{{ editing ? '编辑典籍 #' + form.id : '新增典籍' }}</h3>
        <div class="form-grid">
          <label class="f"><span>名称 *</span><input v-model="form.name" class="ipt" maxlength="50" placeholder="功法名称"></label>
          <label class="f"><span>分类 *</span>
            <select v-model="form.cat" class="ipt">
              <option value="" disabled>请选择分类</option>
              <option v-for="c in CATS" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label class="f"><span>品阶分级 *</span>
            <select v-model="form.grade" class="ipt">
              <option value="" disabled>请选择品阶</option>
              <option v-for="g in GRADES" :key="g" :value="g">{{ g }}</option>
            </select>
          </label>
          <label class="f"><span>品阶评分 (0-100)</span><input v-model.number="form.rank" class="ipt" type="number" min="0" max="100" step="0.1"></label>
          <label class="f"><span>卡面图标</span><input v-model="form.glyph" class="ipt" maxlength="4" placeholder="如 ✦ ⚔ 🐉"></label>
          <label class="f"><span>品阶 / 流派简述</span><input v-model="form.tier" class="ipt" maxlength="60" placeholder="如 天人合一·雷道"></label>
          <label class="f"><span>传承来源</span><input v-model="form.from" class="ipt" maxlength="40" placeholder="如 大荒藏书阁"></label>
          <label class="f"><span>修习者</span><input v-model="form.owner" class="ipt" maxlength="40" placeholder="如 石昊"></label>
          <label class="f wide"><span>功法简介</span><textarea v-model="form.desc" class="ipt" rows="3" maxlength="300" placeholder="典籍简介，展示在首页卡片上"></textarea></label>
          <label class="f check-line"><input v-model="form.hot" type="checkbox"> 标记为热门典籍（首页「热门典籍」展示）</label>
        </div>
        <div class="modal-foot">
          <button class="btn" @click="closeModal">取 消</button>
          <button class="btn gold" :disabled="saving" @click="save">{{ saving ? '保存中…' : '保 存' }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="deleting" class="modal-mask" @click.self="deleting = null">
      <div class="modal small">
        <h3>删除确认</h3>
        <p class="del-tip">确定要删除典籍「<b>{{ deleting.name }}</b>」（#{{ deleting.id }}）吗？<br>删除后首页将不再展示该典籍，此操作不可恢复。</p>
        <div class="modal-foot">
          <button class="btn" @click="deleting = null">取 消</button>
          <button class="btn danger" :disabled="saving" @click="doDelete">{{ saving ? '删除中…' : '确认删除' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  adminListApi, adminCreateApi, adminUpdateApi, adminDeleteApi,
  adminLogout, getAdminSession
} from '../../api/admin.js'
import { useToast } from '../../composables/useToast.js'

const router = useRouter()
const { show } = useToast()

const CATS = ['功法', '宝术', '至尊术', '剑诀', '秘法', '身法']
const GRADES = ['绝世', '绝顶', '一流', '上乘', '中乘']
const PAGE_SIZE = 10
const CAT_COLORS = {
  功法: '#e0a83c', 宝术: '#4fb6a8', 至尊术: '#d96b6b',
  剑诀: '#9a86e8', 秘法: '#5aa9e6', 身法: '#7fbf5a'
}

const adminName = computed(() => {
  const s = getAdminSession()
  return s ? (s.nickname || s.username) : '管理员'
})

// ---------- 列表 / 筛选 / 分页 ----------
const rows = ref([])
const loading = ref(false)
const query = reactive({ keyword: '', cat: '', grade: '' })
const page = ref(1)

const filteredTotal = computed(() => rows.value.length)
const pageCount = computed(() => Math.max(1, Math.ceil(filteredTotal.value / PAGE_SIZE)))
const pageRows = computed(() => rows.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))

const total = computed(() => rows.value.length)
const catCount = computed(() => new Set(rows.value.map(r => r.cat).filter(Boolean)).size)
const gradeCount = computed(() => new Set(rows.value.map(r => r.grade).filter(Boolean)).size)
const hotCount = computed(() => rows.value.filter(r => r.hot).length)

async function load() {
  loading.value = true
  try {
    const res = await adminListApi({ keyword: query.keyword, cat: query.cat, grade: query.grade })
    rows.value = res.data || []
    page.value = 1
  } catch (e) {
    if (e.code === 401) {
      show('登录已失效，请重新登录', 'err')
      router.push('/admin/login')
      return
    }
    show(e.message || '加载失败', 'err')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.cat = ''
  query.grade = ''
  load()
}

// ---------- 新增 / 编辑 ----------
const showModal = ref(false)
const editing = ref(false)
const saving = ref(false)
const emptyForm = () => ({
  id: null, name: '', cat: '', grade: '', rank: null, glyph: '',
  tier: '', from: '', owner: '', desc: '', hot: false
})
const form = reactive(emptyForm())

function openCreate() {
  Object.assign(form, emptyForm())
  editing.value = false
  showModal.value = true
}

function openEdit(row) {
  // 后端 JSON 字段名：from / rank / desc / hot
  Object.assign(form, emptyForm(), {
    id: row.id, name: row.name, cat: row.cat, grade: row.grade,
    rank: row.rank, glyph: row.glyph, tier: row.tier,
    from: row.from, owner: row.owner, desc: row.desc,
    hot: !!row.hot
  })
  editing.value = true
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function save() {
  if (!form.name.trim()) { show('请填写功法名称', 'err'); return }
  if (!form.cat) { show('请选择分类', 'err'); return }
  if (!form.grade) { show('请选择品阶分级', 'err'); return }
  saving.value = true
  const payload = {
    name: form.name.trim(), cat: form.cat, grade: form.grade,
    rank: form.rank === '' || form.rank === null ? null : Number(form.rank),
    glyph: form.glyph, tier: form.tier,
    from: form.from, owner: form.owner, desc: form.desc,
    hot: form.hot ? 1 : 0
  }
  try {
    if (editing.value) {
      await adminUpdateApi(form.id, payload)
      show('典籍已更新，首页刷新后生效', 'ok')
    } else {
      await adminCreateApi(payload)
      show('典籍已新增，首页刷新后生效', 'ok')
    }
    showModal.value = false
    load()
  } catch (e) {
    if (e.code === 401) {
      show('登录已失效，请重新登录', 'err')
      router.push('/admin/login')
      return
    }
    show(e.message || '保存失败', 'err')
  } finally {
    saving.value = false
  }
}

// ---------- 删除 ----------
const deleting = ref(null)

function confirmDelete(row) {
  deleting.value = row
}

async function doDelete() {
  saving.value = true
  try {
    await adminDeleteApi(deleting.value.id)
    show('典籍已删除，首页刷新后生效', 'ok')
    deleting.value = null
    load()
  } catch (e) {
    if (e.code === 401) {
      show('登录已失效，请重新登录', 'err')
      router.push('/admin/login')
      return
    }
    show(e.message || '删除失败', 'err')
  } finally {
    saving.value = false
  }
}

// ---------- 其他 ----------
function goHome() { router.push('/home') }
function doLogout() {
  adminLogout()
  router.push('/admin/login')
}

function catStyle(cat) {
  const c = CAT_COLORS[cat] || '#e0a83c'
  return { color: c, borderColor: c + '66', background: c + '1a' }
}

load()
</script>

<style scoped>
.admin-shell{min-height:100vh;display:flex;flex-direction:column}
.admin-topbar{
  height:58px;flex:none;display:flex;align-items:center;justify-content:space-between;
  padding:0 22px;background:var(--panel);border-bottom:1px solid var(--panel-line);
  backdrop-filter:blur(18px) saturate(140%);-webkit-backdrop-filter:blur(18px) saturate(140%);
}
.tb-left{display:flex;align-items:center;gap:10px;font-size:15px}
.tb-left b{background:linear-gradient(90deg,#fff,var(--gold));-webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent}
.tb-mark{
  width:32px;height:32px;border-radius:9px;display:grid;place-items:center;
  background:rgba(255,224,138,.1);border:1px solid var(--panel-line);
}
.tb-right{display:flex;align-items:center;gap:10px}
.tb-user{font-size:13px;color:var(--gold)}
.tb-btn{
  height:32px;padding:0 14px;border-radius:999px;cursor:pointer;font-size:12px;
  background:transparent;border:1px solid var(--panel-line);color:var(--muted);
  transition:.2s;font-family:inherit;
}
.tb-btn:hover{border-color:var(--gold);color:var(--gold)}
.tb-btn.danger:hover{border-color:var(--danger);color:var(--danger)}

.admin-main{flex:1;width:min(1180px,100%);margin:0 auto;padding:22px 20px 40px;display:flex;flex-direction:column;gap:16px}

.stat-row{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}
.stat{
  background:var(--panel);border:1px solid var(--panel-line);border-radius:14px;
  padding:16px 18px;backdrop-filter:blur(14px);-webkit-backdrop-filter:blur(14px);
  display:flex;flex-direction:column;gap:4px;
}
.stat b{
  font-size:26px;background:linear-gradient(90deg,#fff,var(--gold));
  -webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent;
}
.stat span{font-size:12px;color:var(--muted);letter-spacing:1px}

.tool-row{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
.ipt{
  height:38px;border-radius:10px;border:1px solid var(--panel-line);
  background:rgba(255,255,255,.04);color:var(--text);padding:0 12px;
  font-size:13px;font-family:inherit;outline:none;transition:.2s;
}
.ipt:focus{border-color:var(--gold)}
textarea.ipt{height:auto;padding:9px 12px;resize:vertical;line-height:1.6}
.ipt.sel{width:120px}
.tool-row .ipt:first-child{flex:1;min-width:220px}
.tool-spacer{flex:1}

.btn{
  height:38px;padding:0 18px;border-radius:10px;cursor:pointer;font-size:13px;
  background:transparent;border:1px solid var(--panel-line);color:var(--muted);
  transition:.2s;font-family:inherit;letter-spacing:1px;
}
.btn:hover:not(:disabled){border-color:var(--gold);color:var(--gold)}
.btn.gold{
  background:linear-gradient(135deg,var(--gold),var(--gold-2));
  border-color:transparent;color:#1a1305;font-weight:700;
}
.btn.gold:hover:not(:disabled){filter:brightness(1.08);color:#1a1305}
.btn.danger{background:var(--danger);border-color:transparent;color:#fff;font-weight:700}
.btn:disabled{opacity:.55;cursor:not-allowed}

.table-wrap{
  background:var(--panel);border:1px solid var(--panel-line);border-radius:14px;
  overflow:auto;backdrop-filter:blur(14px);-webkit-backdrop-filter:blur(14px);
}
table{width:100%;border-collapse:collapse;min-width:960px}
th{
  text-align:left;padding:12px 14px;font-size:12px;color:var(--gold);
  letter-spacing:1px;border-bottom:1px solid var(--panel-line);white-space:nowrap;
  position:sticky;top:0;background:rgba(26,21,12,.95);
}
td{padding:10px 14px;font-size:13px;color:var(--text);border-bottom:1px solid rgba(255,214,130,.08);white-space:nowrap}
tbody tr:last-child td{border-bottom:none}
tbody tr:hover{background:rgba(255,224,138,.04)}
.empty{text-align:center;color:var(--muted);padding:36px 0!important}
.glyph-cell{font-size:18px}
.name-cell{font-weight:700;color:var(--gold);max-width:180px;overflow:hidden;text-overflow:ellipsis}
.cat-tag{font-size:11px;border:1px solid;border-radius:999px;padding:2px 9px}
.hot-yes{color:var(--danger);font-size:12px}
.hot-no{color:var(--muted)}
.time-cell{color:var(--muted);font-size:12px}
.op{
  height:26px;padding:0 11px;border-radius:7px;cursor:pointer;font-size:12px;
  background:transparent;border:1px solid var(--panel-line);color:var(--text);
  transition:.2s;font-family:inherit;margin-right:6px;
}
.op:hover{border-color:var(--gold);color:var(--gold)}
.op.del:hover{border-color:var(--danger);color:var(--danger)}

.page-row{display:flex;align-items:center;justify-content:center;gap:14px}
.page-info{font-size:12px;color:var(--muted)}

.modal-mask{
  position:fixed;inset:0;background:rgba(8,6,2,.7);backdrop-filter:blur(4px);
  display:grid;place-items:center;z-index:50;padding:20px;
}
.modal{
  width:min(640px,100%);max-height:86vh;overflow:auto;
  background:#1c1509;border:1px solid var(--panel-line);border-radius:16px;
  padding:22px 24px;box-shadow:0 20px 60px rgba(0,0,0,.5);
}
.modal.small{width:min(420px,100%)}
.modal h3{margin:0 0 16px;color:var(--gold);font-size:16px;letter-spacing:2px}
.del-tip{font-size:13px;color:var(--text);line-height:1.8;margin:0 0 18px}
.del-tip b{color:var(--danger)}
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px 14px}
.f{display:flex;flex-direction:column;gap:6px;font-size:12px;color:var(--muted)}
.f.wide{grid-column:1/-1}
.check-line{
  grid-column:1/-1;flex-direction:row;align-items:center;gap:8px;
  color:var(--text);cursor:pointer;
}
.check-line input{accent-color:var(--gold-2);width:15px;height:15px;cursor:pointer}
.modal-foot{display:flex;justify-content:flex-end;gap:10px;margin-top:18px}

@media (max-width:760px){
  .stat-row{grid-template-columns:repeat(2,1fr)}
  .form-grid{grid-template-columns:1fr}
}
</style>
