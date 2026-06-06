<script setup lang="ts">
import { onMounted } from 'vue'
import Sidebar from './components/Sidebar.vue'
import ChatWindow from './components/ChatWindow.vue'
import { useChatStore } from './stores/chat'
import { useWebSocket } from './composables/useWebSocket'

const store = useChatStore()
const ws = useWebSocket()

function bindConversationWs(id: number) {
  ws.subscribeConversation(id, (msg) => {
    store.appendMessage(msg)
    store.loadConversations()
  })
}

async function onSelect(id: number) {
  await store.selectConversation(id)
  bindConversationWs(id)
}

onMounted(async () => {
  ws.connect()
  ws.onConversationsRefresh(() => store.refreshAll())
  await store.loadProfile()
  await store.loadTags()
  await store.loadConversations()
  if (store.activeConversationId) {
    await store.selectConversation(store.activeConversationId)
    bindConversationWs(store.activeConversationId)
  }
})
</script>

<template>
  <div class="app-layout">
    <Sidebar @select="onSelect" />
    <ChatWindow v-if="store.activeConversation" />
    <div v-else class="empty-chat">
      <p>选择左侧会话开始聊天</p>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  background: var(--surface);
  box-shadow: var(--shadow);
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  background: var(--bg);
}
</style>
