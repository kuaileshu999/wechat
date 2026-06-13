<template>
  <div class="system-layout">
    <el-tabs :model-value="activeTab" @tab-change="onTabChange">
      <el-tab-pane v-if="userStore.hasPermission('system:campus')" label="校区管理" name="campus" />
      <el-tab-pane v-if="userStore.hasPermission('system:role')" label="角色管理" name="role" />
      <el-tab-pane v-if="userStore.hasPermission('system:permission')" label="用户管理" name="user" />
    </el-tabs>
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = computed(() => {
  if (route.path.includes('/system/campus')) return 'campus'
  if (route.path.includes('/system/user')) return 'user'
  return 'role'
})

function onTabChange(name) {
  router.push(`/system/${name}`)
}
</script>

<style scoped>
.system-layout {
  padding: 16px;
}
</style>
