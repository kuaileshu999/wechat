<template>
  <div class="chat-panel">
    <template v-if="conversationId">
      <header class="chat-panel-header">
        <div class="chat-title">
          <div class="name">{{ displayName }}</div>
          <div class="sub">{{ metaLine }}</div>
        </div>
        <div class="header-tags">
          <span class="tag" :class="conversation?.chatType === 'PRIVATE' ? 'tag-private' : 'tag-group'">
            {{ conversation?.chatTypeLabel }}
          </span>
          <span
            class="tag"
            :class="conversation?.category === 'UNDERTAKING' ? 'tag-undertaking' : 'tag-conversion'"
          >
            {{ conversation?.categoryLabel }}
          </span>
        </div>
      </header>

      <div ref="msgBox" v-loading="loading" class="chat-messages">
        <div v-for="msg in messages" :key="msg.id" class="msg-row">
          <div
            class="chat-bubble"
            :class="msg.direction === 'INCOMING' ? 'incoming' : 'outgoing'"
          >
            <div v-if="msg.direction === 'INCOMING'" class="chat-sender">{{ msg.senderName }}</div>
            <div>{{ msg.content }}</div>
            <div class="chat-time">{{ formatTime(msg.sentAt) }}</div>
          </div>
        </div>
      </div>

      <footer class="chat-footer">
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="3"
          placeholder="输入回复内容，Ctrl+Enter 发送"
          resize="none"
          @keyup.ctrl.enter="sendReply"
        />
        <el-button type="primary" :loading="sending" @click="sendReply">发送回复</el-button>
      </footer>
    </template>
    <el-empty v-else class="chat-empty" description="请从左侧选择会话" />
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { conversationApi, messageApi } from '../api'

const props = defineProps({
  conversationId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['replied', 'read'])

const loading = ref(false)
const sending = ref(false)
const conversation = ref(null)
const messages = ref([])
const replyText = ref('')
const msgBox = ref(null)

const displayName = computed(() => {
  const c = conversation.value
  if (!c) return ''
  if (c.chatType === 'GROUP') return c.groupName || `${c.studentName}的群`
  return c.studentName
})

const metaLine = computed(() => {
  const c = conversation.value
  if (!c) return ''
  return `${c.tutorName} · ${c.wecomAccountName}`
})

async function loadData(id) {
  if (!id) return
  loading.value = true
  try {
    const [conv, msgs] = await Promise.all([
      conversationApi.detail(id),
      messageApi.list(id),
    ])
    conversation.value = conv
    messages.value = msgs
    emit('read', id)
    await nextTick()
    scrollBottom()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function sendReply() {
  const text = replyText.value.trim()
  if (!text) {
    ElMessage.warning('请输入回复内容')
    return
  }
  sending.value = true
  try {
    const msg = await messageApi.reply({
      conversationId: props.conversationId,
      content: text,
      senderName: '接管辅导',
    })
    messages.value.push(msg)
    replyText.value = ''
    emit('replied')
    await nextTick()
    scrollBottom()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    sending.value = false
  }
}

function scrollBottom() {
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

watch(
  () => props.conversationId,
  (id) => {
    conversation.value = null
    messages.value = []
    replyText.value = ''
    if (id) loadData(id)
  },
  { immediate: true }
)
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--card);
  border-radius: 8px;
  border: 1px solid var(--border);
  overflow: hidden;
}

.chat-panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.chat-title .name {
  font-size: 16px;
  font-weight: 600;
}

.chat-title .sub {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.header-tags {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  background: #fafbfc;
}

.msg-row {
  display: flex;
  flex-direction: column;
}

.chat-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--border);
  align-items: flex-end;
}

.chat-footer .el-textarea {
  flex: 1;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
