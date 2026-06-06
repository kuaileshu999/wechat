<template>
  <div class="pc-layout">
    <aside class="pc-sidebar">
      <div class="brand">
        <span class="brand-icon">企</span>
        <div>
          <div class="brand-title">消息接管系统</div>
          <div class="brand-sub">PC 工作台</div>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="side-menu"
        router
      >
        <el-menu-item index="/messages">
          <span>消息中心</span>
        </el-menu-item>
        <el-menu-item index="/config">
          <span>接管配置</span>
        </el-menu-item>
      </el-menu>
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
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => {
  if (route.path.startsWith('/config')) return '/config'
  return '/messages'
})

const pageTitle = computed(() => route.meta.title || '企微消息接管系统')
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
