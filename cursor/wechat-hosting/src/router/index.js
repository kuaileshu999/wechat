import { createRouter, createWebHistory } from 'vue-router'
import { getUser, isLoggedIn } from '../utils/auth'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/workbench',
    children: [
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('../views/WorkbenchView.vue'),
        meta: { title: '消息工作台' },
      },
      {
        path: 'hosting-config',
        name: 'HostingConfig',
        component: () => import('../views/HostingConfigView.vue'),
        meta: { title: '接管配置', roles: [1] },
      },
      {
        path: 'tutors',
        name: 'Tutors',
        component: () => import('../views/TutorsView.vue'),
        meta: { title: '辅导老师管理', roles: [1] },
      },
      {
        path: 'takeover-managers',
        name: 'TakeoverManagers',
        component: () => import('../views/TakeoverManagersView.vue'),
        meta: { title: '接管者管理', roles: [1] },
      },
      {
        path: 'transfer-logs',
        name: 'TransferLogs',
        component: () => import('../views/TransferLogsView.vue'),
        meta: { title: '转接记录' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.public) {
    if (to.path === '/login' && isLoggedIn()) {
      return '/workbench'
    }
    return true
  }
  if (!isLoggedIn()) {
    return '/login'
  }
  const roles = to.meta.roles
  if (roles?.length) {
    const user = getUser()
    if (!roles.includes(user?.role)) {
      return '/workbench'
    }
  }
  return true
})

export default router
