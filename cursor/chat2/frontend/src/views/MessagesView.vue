<template>
  <div class="messages-workbench">
    <aside class="list-panel">
      <div class="list-toolbar">
        <!-- 第一层：承接期 / 转化率 -->
        <el-tabs v-model="category" class="category-tabs">
          <el-tab-pane name="UNDERTAKING">
            <template #label>
              <span class="tab-label">
                承接期
                <el-badge
                  v-if="undertakingTotal > 0"
                  :value="undertakingTotal"
                  :max="99999"
                  class="tab-badge"
                />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="CONVERSION">
            <template #label>
              <span class="tab-label">
                转化率
                <el-badge
                  v-if="conversionTotal > 0"
                  :value="conversionTotal"
                  :max="99999"
                  class="tab-badge"
                />
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 第二层：私聊 / 群聊 -->
        <el-tabs v-model="chatType" class="chat-tabs">
          <el-tab-pane name="PRIVATE">
            <template #label>
              <span class="tab-label">
                私聊消息
                <el-badge
                  v-if="unrepliedPrivate > 0"
                  :value="unrepliedPrivate"
                  :max="99999"
                  class="tab-badge"
                />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="GROUP">
            <template #label>
              <span class="tab-label">
                群聊消息
                <el-badge
                  v-if="unrepliedGroup > 0"
                  :value="unrepliedGroup"
                  :max="99999"
                  class="tab-badge"
                />
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <div class="toolbar-search">
          <el-input
            v-model="keyword"
            placeholder="搜索消息内容"
            clearable
            @clear="onFilterChange"
            @keyup.enter="onFilterChange"
          >
            <template #append>
              <el-button @click="onFilterChange">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <div v-loading="loading" class="conv-scroll">
        <div
          v-for="item in conversations"
          :key="item.id"
          class="conv-item"
          :class="{ active: selectedId === item.id }"
          @click="selectConversation(item.id)"
        >
          <div class="conv-avatar">{{ avatarText(item) }}</div>
          <div class="conv-main">
            <div class="conv-top">
              <span class="conv-name">{{ displayName(item) }}</span>
              <span class="conv-time">{{ formatTime(item.lastMessageAt) }}</span>
            </div>
            <div class="conv-meta">
              {{ item.tutorName }} · {{ item.wecomAccountName }}
            </div>
            <div class="conv-preview">{{ item.lastMessagePreview || '暂无消息' }}</div>
          </div>
          <span
            v-if="badgeCount(item) > 0"
            class="conv-badge"
            :title="badgeTitle(item)"
          >
            {{ badgeCount(item) > 99 ? '99+' : badgeCount(item) }}
          </span>
        </div>
        <el-empty v-if="!loading && conversations.length === 0" :description="emptyTip" />
      </div>

      <div class="list-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          background
          small
          @current-change="loadList"
          @size-change="onSizeChange"
        />
      </div>
    </aside>

    <main class="detail-panel">
      <ChatPanel :conversation-id="selectedId" @replied="onReplied" @read="onConversationRead" />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { conversationApi } from '../api'
import ChatPanel from '../components/ChatPanel.vue'

const loading = ref(false)
const conversations = ref([])
const selectedId = ref(null)
const category = ref('UNDERTAKING')
const chatType = ref('PRIVATE')
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const unrepliedStats = ref({
  undertaking: { privateCount: 0, groupCount: 0 },
  conversion: { privateCount: 0, groupCount: 0 },
})

const currentStats = computed(() =>
  category.value === 'CONVERSION'
    ? unrepliedStats.value.conversion
    : unrepliedStats.value.undertaking
)

const unrepliedPrivate = computed(() => currentStats.value?.privateCount ?? 0)
const unrepliedGroup = computed(() => currentStats.value?.groupCount ?? 0)
const undertakingTotal = computed(
  () =>
    (unrepliedStats.value.undertaking?.privateCount ?? 0) +
    (unrepliedStats.value.undertaking?.groupCount ?? 0)
)
const conversionTotal = computed(
  () =>
    (unrepliedStats.value.conversion?.privateCount ?? 0) +
    (unrepliedStats.value.conversion?.groupCount ?? 0)
)

const emptyTip = computed(() =>
  chatType.value === 'GROUP' ? '暂无群聊会话' : '暂无私聊会话'
)

async function loadUnrepliedCounts() {
  try {
    const stats = await conversationApi.unrepliedCounts()
    unrepliedStats.value = {
      undertaking: stats.undertaking ?? { privateCount: 0, groupCount: 0 },
      conversion: stats.conversion ?? { privateCount: 0, groupCount: 0 },
    }
  } catch {
    unrepliedStats.value = {
      undertaking: { privateCount: 0, groupCount: 0 },
      conversion: { privateCount: 0, groupCount: 0 },
    }
  }
}

async function loadList() {
  loading.value = true
  try {
    const result = await conversationApi.list({
      category: category.value,
      chatType: chatType.value,
      keyword: keyword.value || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    conversations.value = result.list || []
    total.value = result.total || 0
    if (conversations.value.length > 0) {
      const stillExists = conversations.value.some((c) => c.id === selectedId.value)
      if (!stillExists) {
        const firstId = conversations.value[0].id
        selectedId.value = firstId
        clearConversationBadge(firstId)
      }
    } else {
      selectedId.value = null
    }
  } catch (e) {
    conversations.value = []
    total.value = 0
    selectedId.value = null
    ElMessage.error(e.message || '加载会话失败，请确认后端已启动')
  } finally {
    loading.value = false
  }
}

async function refreshAll() {
  await loadUnrepliedCounts()
  await loadList()
}

function onFilterChange() {
  page.value = 1
  loadList()
}

function onSizeChange() {
  page.value = 1
  loadList()
}

function clearConversationBadge(id) {
  const item = conversations.value.find((c) => c.id === id)
  if (item) {
    item.unreadCount = 0
    item.pendingReplyCount = 0
    item.pendingReply = false
  }
}

function selectConversation(id) {
  selectedId.value = id
  clearConversationBadge(id)
}

function onConversationRead(id) {
  clearConversationBadge(id)
  loadUnrepliedCounts()
}

function onReplied() {
  refreshAll()
}

function displayName(item) {
  if (item.chatType === 'GROUP') {
    return item.groupName || `${item.studentName}的群`
  }
  return item.studentName
}

/** 私聊：未回消息数；群聊：未读消息数（含待回复条数） */
function badgeCount(item) {
  const pending = item.pendingReplyCount ?? 0
  const unread = item.unreadCount ?? 0
  if (item.chatType === 'GROUP') {
    return Math.max(unread, pending)
  }
  return pending
}

function badgeTitle(item) {
  const n = badgeCount(item)
  return item.chatType === 'GROUP' ? `${n} 条未读消息` : `${n} 条未回消息`
}

function avatarText(item) {
  const name = displayName(item)
  return name ? name.charAt(0) : '?'
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

watch(category, () => {
  page.value = 1
  refreshAll()
})

watch(chatType, () => {
  page.value = 1
  loadList()
})

onMounted(refreshAll)
</script>

<style scoped>
.messages-workbench {
  display: flex;
  gap: 16px;
  height: calc(100vh - 56px - 40px);
  min-height: 560px;
}

.list-panel {
  width: 400px;
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
  flex-shrink: 0;
}

.category-tabs :deep(.el-tabs__header) {
  margin-bottom: 4px;
}

.category-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  padding: 0 20px;
  height: 40px;
}

.chat-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.chat-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  height: 36px;
}

.toolbar-search {
  padding: 10px 4px 12px;
}

.conv-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.list-pagination {
  padding: 10px 12px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.detail-panel {
  flex: 1;
  min-width: 0;
  height: 100%;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tab-badge :deep(.el-badge__content) {
  font-size: 11px;
}

.conv-badge {
  min-width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  background: #ff4d4f;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 11px;
  padding: 0 6px;
  align-self: center;
  flex-shrink: 0;
}
</style>
