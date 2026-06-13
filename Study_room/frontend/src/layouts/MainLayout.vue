<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">自习室管理</div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9"
               active-text-color="#409EFF">
        <template v-for="menu in userStore.menus" :key="menu.code">
          <el-sub-menu v-if="menu.children?.length" :index="menu.path">
            <template #title>{{ menu.name }}</template>
            <el-menu-item v-for="child in menu.children" :key="child.code" :index="child.path">
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">{{ menu.name }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
        <el-button link type="danger" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/system/')) {
    return route.path
  }
  return route.path
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.aside {
  background: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: #263445;
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.el-main {
  background: #f5f7fa;
  padding: 0;
}
</style>
