<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import MessageList from './MessageList.vue'
import MessageInput from './MessageInput.vue'

const store = useChatStore()

const c = () => store.activeConversation!

function phaseLabel(phase: string) {
  return phase === 'CONVERSION' ? '转化期' : '承接期'
}

async function onTransfer() {
  const tags = store.tags.filter((t) => t.name !== '全部')
  const next = tags.find((t) => t.id !== c().assignedTeacherTag?.id) ?? tags[0]
  if (next) await store.transferToTag(next.id)
}
</script>

<template>
  <section class="chat-window" v-if="store.activeConversation">
    <header class="chat-header">
      <img :src="c().student.avatar" alt="" class="avatar" />
      <div class="header-info">
        <div class="title-row">
          <h2>{{ c().student.name }}</h2>
          <span v-if="c().student.online" class="online">在线</span>
        </div>
        <p class="subtitle">{{ c().type === 'PRIVATE' ? '私聊' : '群聊' }} · 学生</p>
      </div>
      <div class="header-actions">
        <button
          class="identity-btn"
          :class="{ on: store.replyAsTeacher }"
          @click="store.replyAsTeacher = !store.replyAsTeacher"
        >
          {{ store.replyAsTeacher && c().assignedTeacherTag != null
            ? `以${c().assignedTeacherTag!.name}辅导身份回复`
            : '以客服身份回复' }}
        </button>
        <span class="phase-pill">{{ phaseLabel(c().phase) }}</span>
        <button class="transfer-btn" @click="onTransfer">转接</button>
      </div>
    </header>

    <MessageList />
    <MessageInput />
  </section>
</template>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
}

.chat-header .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-row h2 {
  font-size: 16px;
  font-weight: 600;
}

.online {
  font-size: 12px;
  color: #34c759;
  display: flex;
  align-items: center;
  gap: 4px;
}

.online::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #34c759;
  border-radius: 50%;
}

.subtitle {
  font-size: 12px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.identity-btn {
  font-size: 12px;
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text-secondary);
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.identity-btn.on {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-light);
}

.phase-pill {
  font-size: 12px;
  padding: 4px 10px;
  background: #fff3e0;
  color: #e65100;
  border-radius: 6px;
}

.transfer-btn {
  font-size: 13px;
  padding: 6px 14px;
  border: 1px solid var(--primary);
  color: var(--primary);
  border-radius: 8px;
}

.transfer-btn:hover {
  background: var(--primary-light);
}
</style>
