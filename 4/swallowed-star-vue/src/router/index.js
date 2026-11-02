import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Characters from '../views/Characters.vue'
import Realms from '../views/Realms.vue'
import Quests from '../views/Quests.vue'
import Profile from '../views/Profile.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminGongfa from '../views/admin/GongfaManage.vue'
import AdminCharacters from '../views/admin/CharacterManage.vue'
import AdminRealms from '../views/admin/RealmManage.vue'
import AdminQuests from '../views/admin/QuestManage.vue'
import AdminUsers from '../views/admin/UserManage.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  { path: '/home', name: 'home', component: Home },
  // 大荒人物志 · 人物介绍页
  { path: '/characters', name: 'characters', component: Characters },
  // 大荒秘境录 · 秘境首页
  { path: '/realms', name: 'realms', component: Realms },
  // 大荒悬赏榜 · 任务接取页
  { path: '/quests', name: 'quests', component: Quests },
  // 我的主页 · 修士名帖（积分/任务/秘境/成就/动态）
  { path: '/profile', name: 'profile', component: Profile },
  // 后台管理：登录页 + 六模块控制台
  { path: '/admin/login', name: 'adminLogin', component: AdminLogin },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'adminDashboard', component: AdminDashboard, meta: { title: '数据概览' } },
      { path: 'gongfa', name: 'adminGongfa', component: AdminGongfa, meta: { title: '功法管理' } },
      { path: 'characters', name: 'adminCharacters', component: AdminCharacters, meta: { title: '人物管理' } },
      { path: 'realms', name: 'adminRealms', component: AdminRealms, meta: { title: '秘境管理' } },
      { path: 'quests', name: 'adminQuests', component: AdminQuests, meta: { title: '悬赏任务' } },
      { path: 'users', name: 'adminUsers', component: AdminUsers, meta: { title: '修士账号' } }
    ]
  },
  // 兜底：未知路径回登录页
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 未登录（无 token）访问前台页面跳回登录；后台页面需要独立的 admin token
const NEED_TOKEN = ['/home', '/characters', '/realms', '/quests', '/profile']
router.beforeEach((to) => {
  if (NEED_TOKEN.includes(to.path) && !localStorage.getItem('ss_token')) {
    return '/login'
  }
  if (to.path.startsWith('/admin') && to.name !== 'adminLogin' && !localStorage.getItem('ss_admin_token')) {
    return '/admin/login'
  }
})

export default router
