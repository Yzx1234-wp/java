import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminIndex from '../views/admin/AdminIndex.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  { path: '/home', name: 'home', component: Home },
  // 后台管理系统：登录页（仅登录）+ 管理页（功法数据管理）
  { path: '/admin/login', name: 'adminLogin', component: AdminLogin },
  { path: '/admin', name: 'admin', component: AdminIndex }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 未登录（无 token）访问主页则跳回登录；后台管理页需要独立的 admin token
router.beforeEach((to) => {
  if (to.path === '/home' && !localStorage.getItem('ss_token')) {
    return '/login'
  }
  if (to.path === '/admin' && !localStorage.getItem('ss_admin_token')) {
    return '/admin/login'
  }
})

export default router
