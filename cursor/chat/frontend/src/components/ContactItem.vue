<script setup lang="ts">
import type { ConversationDto } from '@/types'

defineProps<{
  conversation: ConversationDto
  active: boolean
}>()

function phaseLabel(phase: string) {
  return phase === 'CONVERSION' ? '转化期' : '承接期'
}
</script>

<template>
  <li :class="['contact-item', { active }]">
    <img :src="conversation.student.avatar" alt="" class="avatar" />
    <div class="body">
      <div class="top">
        <span class="name">{{ conversation.student.name }}</span>
        <span class="time">{{ conversation.lastMessageTimeLabel }}</span>
      </div>
      <p class="preview">{{ conversation.lastMessagePreview }}</p>
      <div class="tags">
        <span
          v-if="conversation.assignedTeacherTag"
          class="tag teacher"
        >{{ conversation.assignedTeacherTag.name }}</span>
        <span class="tag phase">{{ phaseLabel(conversation.phase) }}</span>
      </div>
    </div>
    <span v-if="conversation.unreadCount > 0" class="unread">{{ conversation.unreadCount }}</span>
  </li>
</template>

<style scoped>
.contact-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  border-left: 3px solid transparent;
  position: relative;
  align-items: flex-start;
}

.contact-item:hover {
  background: #fafbfc;
}

.contact-item.active {
  background: #f0f4ff;
  border-left-color: var(--primary);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.body {
  flex: 1;
  min-width: 0;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-weight: 500;
  font-size: 14px;
}

.time {
  font-size: 11px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.preview {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tags {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.tag.teacher {
  background: var(--primary-light);
  color: var(--primary);
}

.tag.phase {
  background: #fff3e0;
  color: #e65100;
}

.unread {
  position: absolute;
  right: 12px;
  top: 36px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
