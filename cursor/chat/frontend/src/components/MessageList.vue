<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import MessageItem from './MessageItem.vue'

const store = useChatStore()
const listRef = ref<HTMLElement | null>(null)

watch(
  () => store.messages.length,
  async () => {
    await nextTick()
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  }
)

</script>

<template>
  <div ref="listRef" class="message-list">
    <template v-for="(msg, index) in store.messages" :key="msg.id">
      <div
        v-if="index === 1 && store.activeConversation?.unreadCount"
        class="unread-divider"
      >
        <span>{{ store.activeConversation.unreadCount }}</span>
      </div>
      <p v-if="index === 0" class="date-sep">{{ msg.timeLabel }}</p>
      <MessageItem :message="msg" />
    </template>
  </div>
</template>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.date-sep {
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.unread-divider {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.unread-divider span {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
