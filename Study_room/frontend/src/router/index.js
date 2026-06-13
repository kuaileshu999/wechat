import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

function systemRedirect() {
  const userStore = useUserStore()
  if (userStore.hasPermission('system:campus')) return '/system/campus'
  if (userStore.hasPermission('system:role')) return '/system/role'
  if (userStore.hasPermission('system:permission')) return '/system/user'
  return '/employee'
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/employee',
    children: [
      { path: 'system', component: () => import('@/layouts/SystemLayout.vue'), redirect: systemRedirect, children: [
        { path: 'campus', name: 'SystemCampus', component: () => import('@/views/system/Campus.vue'), meta: { permission: 'system:campus' } },
        { path: 'role', name: 'SystemRole', component: () => import('@/views/system/Role.vue'), meta: { permission: 'system:role' } },
        { path: 'user', name: 'SystemUser', component: () => import('@/views/system/User.vue'), meta: { permission: 'system:permission' } }
      ]},
      { path: 'employee', name: 'Employee', component: () => import('@/views/employee/Index.vue'), meta: { permission: 'employee' } },
      { path: 'student', name: 'Student', component: () => import('@/views/student/Index.vue'), meta: { permission: 'student' } },
      { path: 'course-type', name: 'CourseType', component: () => import('@/views/course-type/Index.vue'), meta: { permission: 'course-type' } },
      { path: 'course', name: 'Course', component: () => import('@/views/course/Index.vue'), meta: { permission: 'course' } },
      { path: 'order', name: 'Order', component: () => import('@/views/order/Index.vue'), meta: { permission: 'order' } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/order/Detail.vue'), meta: { permission: 'order' } },
      { path: 'consumption', name: 'Consumption', component: () => import('@/views/consumption/Index.vue'), meta: { permission: 'consumption' } },
      { path: 'finance', name: 'Finance', component: () => import('@/views/finance/Index.vue'), meta: { permission: 'finance' } },
      { path: 'schedule', name: 'Schedule', component: () => import('@/views/schedule/Index.vue'), meta: { permission: 'schedule' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    return true
  }
  if (!userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
    return '/employee'
  }
  return true
})

export default router
