import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Characters from '../views/Characters.vue'
import Realms from '../views/Realms.vue'
import Quests from '../views/Quests.vue'
import Profile from '../views/Profile.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminIndex from '../views/admin/AdminIndex.vue'

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
  // 后台管理系统：登录页（仅登录）+ 管理页（功法数据管理）
  { path: '/admin/login', name: 'adminLogin', component: AdminLogin },
  { path: '/admin', name: 'admin', component: AdminIndex }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 未登录（无 token）访问主页/人物介绍/秘境/任务/个人主页则跳回登录；后台管理页需要独立的 admin token
const NEED_TOKEN = ['/home', '/characters', '/realms', '/quests', '/profile']
router.beforeEach((to) => {
  if (NEED_TOKEN.includes(to.path) && !localStorage.getItem('ss_token')) {
    return '/login'
  }
  if (to.path === '/admin' && !localStorage.getItem('ss_admin_token')) {
    return '/admin/login'
  }
})

export default router
