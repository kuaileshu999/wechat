<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'

const store = useChatStore()
const text = ref('')
const sending = ref(false)

async function send() {
  if (!text.value.trim() || sending.value) return
  sending.value = true
  try {
    await store.sendMessage(text.value)
    text.value = ''
  } finally {
    sending.value = false
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

const identityHint = () => {
  const tag = store.activeConversation?.assignedTeacherTag
  if (store.replyAsTeacher && tag) {
    return `当前以 ${tag.name} 身份发送`
  }
  return '当前以客服身份发送'
}
</script>

<template>
  <footer class="input-area">
    <textarea
      v-model="text"
      placeholder="输入回复内容..."
      rows="3"
      @keydown="onKeydown"
    />
    <div class="input-footer">
      <span class="hint">{{ identityHint() }} · 按 Enter 发送</span>
      <button class="send-btn" :disabled="sending || !text.trim()" @click="send" title="发送">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
        </svg>
      </button>
    </div>
  </footer>
</template>

<style scoped>
.input-area {
  background: var(--surface);
  border-top: 1px solid var(--border);
  padding: 12px 20px 16px;
}

textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 12px 14px;
  resize: none;
  outline: none;
  background: var(--bg);
}

textarea:focus {
  border-color: var(--primary);
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}

.send-btn:hover:not(:disabled) {
  background: var(--primary-hover);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
