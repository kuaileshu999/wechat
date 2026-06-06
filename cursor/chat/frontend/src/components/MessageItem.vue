<script setup lang="ts">
import { computed } from 'vue'
import type { MessageDto } from '@/types'
import { renderContentWithMath } from '@/utils/renderMath'

const props = defineProps<{ message: MessageDto }>()

const html = computed(() => renderContentWithMath(props.message.content))
</script>

<template>
  <div :class="['message-row', message.outgoing ? 'outgoing' : 'incoming']">
    <img
      v-if="!message.outgoing"
      :src="message.sender.avatar"
      alt=""
      class="avatar"
    />
    <div class="bubble-wrap">
      <span v-if="!message.outgoing" class="sender-name">{{ message.sender.name }}</span>
      <div class="bubble" v-html="html" />
      <span class="time">{{ message.timeLabel }}</span>
    </div>
  </div>
</template>

<style scoped>
.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-row.outgoing {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
}

.bubble-wrap {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.outgoing .bubble-wrap {
  align-items: flex-end;
}

.sender-name {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.bubble {
  padding: 10px 14px;
  border-radius: var(--radius);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.incoming .bubble {
  background: var(--surface);
  color: var(--text);
  border: 1px solid var(--border);
  border-top-left-radius: 4px;
}

.outgoing .bubble {
  background: var(--primary);
  color: #fff;
  border-top-right-radius: 4px;
}

.outgoing .bubble :deep(.katex) {
  color: #fff;
}

.time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
