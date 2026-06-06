<template>
  <div class="pc-layout">
    <aside class="pc-sidebar">
      <div class="brand">
        <span class="brand-icon">托</span>
        <div>
          <div class="brand-title">消息托管系统</div>
          <div class="brand-sub">企微消息工作台</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" class="side-menu" router>
        <el-menu-item index="/workbench">
          <span>消息工作台</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/hosting-config">
          <span>接管配置</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/tutors">
          <span>辅导老师管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/takeover-managers">
          <span>接管者管理</span>
        </el-menu-item>
        <el-menu-item index="/transfer-logs">
          <span>转接记录</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-user">
        <div class="user-name">{{ user?.realName }}</div>
        <div class="user-role">{{ roleLabel(user?.role) }}</div>
        <el-button link type="primary" @click="logout">退出登录</el-button>
      </div>
    </aside>
    <section class="pc-main">
      <header class="pc-header">
        <h2>{{ pageTitle }}</h2>
      </header>
      <div class="pc-content">
        <router-view />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearUser, getUser, roleLabel, ROLE } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())
const isAdmin = computed(() => user.value?.role === ROLE.ADMIN)

const activeMenu = computed(() => {
  if (route.path.startsWith('/hosting-config')) return '/hosting-config'
  if (route.path.startsWith('/tutors')) return '/tutors'
  if (route.path.startsWith('/takeover-managers')) return '/takeover-managers'
  if (route.path.startsWith('/transfer-logs')) return '/transfer-logs'
  return '/workbench'
})

const pageTitle = computed(() => route.meta.title || '消息托管系统')

function logout() {
  clearUser()
  router.push('/login')
}
</script>

<style scoped>
.pc-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.pc-sidebar {
  width: var(--sidebar-width);
  background: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.brand-title {
  font-size: 15px;
  font-weight: 600;
}

.brand-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  margin-top: 2px;
}

.side-menu {
  border-right: none;
  background: transparent;
  flex: 1;
}

.side-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.75);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: var(--primary) !important;
  color: #fff !important;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-user {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  margin: 4px 0 8px;
}

.pc-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg);
}

.pc-header {
  height: 56px;
  padding: 0 24px;
  background: var(--card);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.pc-header h2 {
  font-size: 18px;
  font-weight: 600;
}

.pc-content {
  flex: 1;
  overflow: hidden;
  padding: 16px 24px 24px;
}
</style>
