<template>
  <div class="workbench">
    <div class="workbench-header page-card">
      <div class="user-info">
        <div class="avatar">{{ userInitial }}</div>
        <div>
          <div class="name">{{ user?.realName }}</div>
          <div class="status-line">
            接管中 · {{ stats.hostingTutorCount || 0 }} 位老师 · {{ stats.totalUnread || 0 }} 条未读
          </div>
        </div>
      </div>
    </div>

    <div class="workbench-body">
      <aside class="list-panel">
        <div class="list-toolbar">
          <el-tabs v-model="stage" class="stage-tabs" @tab-change="onFilterChange">
            <el-tab-pane :name="1">
              <template #label>
                <span>转化期 <el-badge v-if="stats.conversionCount" :value="stats.conversionCount" /></span>
              </template>
            </el-tab-pane>
            <el-tab-pane :name="2">
              <template #label>
                <span>承接期 <el-badge v-if="stats.undertakingCount" :value="stats.undertakingCount" /></span>
              </template>
            </el-tab-pane>
            <el-tab-pane :name="3">
              <template #label>
                <span>已结课 <el-badge v-if="stats.completedCount" :value="stats.completedCount" /></span>
              </template>
            </el-tab-pane>
          </el-tabs>

          <el-tabs v-model="convType" class="chat-tabs" @tab-change="onFilterChange">
            <el-tab-pane label="私聊" :name="1" />
            <el-tab-pane label="群聊" :name="2" />
          </el-tabs>

          <div class="toolbar-row search-tutor-row">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="搜索学生昵称或群名称"
              clearable
              @clear="onFilterChange"
              @keyup.enter="onFilterChange"
            />
            <el-select
              v-model="tutorId"
              class="tutor-select"
              placeholder="全部老师"
              clearable
              @change="onFilterChange"
            >
              <el-option label="全部" :value="null" />
              <el-option
                v-for="t in tutors"
                :key="t.id"
                :label="t.name"
                :value="t.id"
              />
            </el-select>
          </div>
        </div>

        <div v-loading="loading" class="conv-scroll">
          <template v-for="group in groupedConversations" :key="group.label">
            <div class="group-header">
              <span class="group-label">{{ group.label }}</span>
              <span v-if="group.unreadTotal > 0" class="group-unread">{{ group.unreadTotal }} 未读</span>
            </div>
            <div
              v-for="item in group.items"
              :key="item.id"
              class="conv-item"
              :class="{ active: selectedId === item.id }"
              @click="selectConversation(item)"
            >
              <div class="conv-avatar">{{ avatarText(item) }}</div>
              <div class="conv-main">
                <div class="conv-top">
                  <span class="conv-name">{{ displayName(item) }}</span>
                  <span class="conv-time">{{ formatTime(item.lastMessageAt) }}</span>
                </div>
                <div class="conv-meta">{{ item.tutorName }}-{{ item.subject }} · {{ item.stageLabel }}</div>
                <div class="conv-preview">{{ item.lastMessagePreview || '暂无消息' }}</div>
              </div>
              <span v-if="item.unreadCount > 0" class="conv-badge">
                {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
              </span>
            </div>
          </template>
          <el-empty v-if="!loading && conversations.length === 0" description="暂无会话" />
        </div>
      </aside>

      <main class="detail-panel">
        <ChatPanel
          :conversation-id="selectedId"
          @replied="refreshAll"
          @read="onConversationRead"
          @transferred="refreshAll"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { conversationApi, tutorApi } from '../api'
import { getUser, ROLE } from '../utils/auth'
import ChatPanel from '../components/ChatPanel.vue'

const user = computed(() => getUser())
const userInitial = computed(() => user.value?.realName?.charAt(0) || '?')

const loading = ref(false)
const conversations = ref([])
const tutors = ref([])
const selectedId = ref(null)
const stage = ref(1)
const convType = ref(1)
const keyword = ref('')
const tutorId = ref(null)
const stats = ref({
  hostingTutorCount: 0,
  totalUnread: 0,
  conversionCount: 0,
  undertakingCount: 0,
  completedCount: 0,
})

const groupedConversations = computed(() => {
  const map = new Map()
  for (const item of conversations.value) {
    const label = `${item.subject || '未知'} · ${item.grade || '未知'}`
    if (!map.has(label)) {
      map.set(label, [])
    }
    map.get(label).push(item)
  }
  return [...map.entries()]
    .sort((a, b) => a[0].localeCompare(b[0], 'zh-CN'))
    .map(([label, items]) => ({
      label,
      items,
      unreadTotal: items.reduce((sum, i) => sum + (i.unreadCount || 0), 0),
    }))
})

async function loadStats() {
  if (!user.value?.userId || user.value.role !== ROLE.TAKEOVER) {
    stats.value = {
      hostingTutorCount: 0,
      totalUnread: 0,
      conversionCount: 0,
      undertakingCount: 0,
      completedCount: 0,
    }
    return
  }
  try {
    stats.value = await conversationApi.stats(user.value.userId)
  } catch {
    stats.value = {
      hostingTutorCount: 0,
      totalUnread: 0,
      conversionCount: 0,
      undertakingCount: 0,
      completedCount: 0,
    }
  }
}

async function loadTutors() {
  try {
    tutors.value = await tutorApi.list()
  } catch {
    tutors.value = []
  }
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      stage: stage.value,
      convType: convType.value,
      keyword: keyword.value || undefined,
      tutorId: tutorId.value || undefined,
      page: 1,
      pageSize: 50,
    }
    if (user.value?.role === ROLE.TAKEOVER) {
      params.handlerUserId = user.value.userId
    }
    const result = await conversationApi.list(params)
    conversations.value = result.list
    if (conversations.value.length > 0) {
      const exists = conversations.value.some((c) => c.id === selectedId.value)
      if (!exists) {
        selectedId.value = conversations.value[0].id
      }
    } else {
      selectedId.value = null
    }
  } catch (e) {
    conversations.value = []
    selectedId.value = null
    ElMessage.error(e.message || '加载会话失败')
  } finally {
    loading.value = false
  }
}

async function refreshAll() {
  await loadStats()
  await loadList()
}

function onFilterChange() {
  loadList()
}

function selectConversation(item) {
  selectedId.value = item.id
  item.unreadCount = 0
}

function onConversationRead() {
  loadStats()
  const item = conversations.value.find((c) => c.id === selectedId.value)
  if (item) item.unreadCount = 0
}

function displayName(item) {
  if (item.convType === 2) return item.groupName || item.studentName
  return item.studentName
}

function avatarText(item) {
  const name = displayName(item)
  return name ? name.charAt(0) : '?'
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

onMounted(async () => {
  await loadTutors()
  await refreshAll()
})
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100vh - 56px - 40px);
  min-height: 560px;
}

.workbench-header {
  padding: 12px 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.name {
  font-size: 16px;
  font-weight: 600;
}

.status-line {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.workbench-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.list-panel {
  width: 420px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--card);
  border-radius: 8px;
  border: 1px solid var(--border);
  overflow: hidden;
}

.list-toolbar {
  padding: 8px 12px 0;
  border-bottom: 1px solid var(--border);
}

.stage-tabs :deep(.el-tabs__item),
.chat-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  height: 36px;
}

.search-tutor-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px 12px;
}

.search-input {
  flex: 1;
  min-width: 120px;
}

.tutor-select {
  flex: 0 0 120px;
  width: 120px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 4px;
  background: #fafafa;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 1;
}

.group-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.group-unread {
  font-size: 11px;
  color: #ff4d4f;
}

.conv-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.detail-panel {
  flex: 1;
  min-width: 0;
}
</style>
