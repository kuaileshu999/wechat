import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/messages',
    children: [
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('../views/MessagesView.vue'),
        meta: { title: '消息中心' },
      },
      {
        path: 'config',
        name: 'TakeoverConfig',
        component: () => import('../views/TakeoverConfigView.vue'),
        meta: { title: '接管配置' },
      },
    ],
  },
  {
    path: '/chat/:id',
    redirect: (to) => ({ path: '/messages', query: { id: to.params.id } }),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
