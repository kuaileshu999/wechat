<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import ContactItem from './ContactItem.vue'

const emit = defineEmits<{ select: [id: number] }>()
const store = useChatStore()
</script>

<template>
  <ul class="contact-list">
    <ContactItem
      v-for="c in store.conversations"
      :key="c.id"
      :conversation="c"
      :active="c.id === store.activeConversationId"
      @click="emit('select', c.id)"
    />
    <li v-if="!store.conversations.length && !store.loading" class="empty">
      暂无会话
    </li>
  </ul>
</template>

<style scoped>
.contact-list {
  list-style: none;
  overflow-y: auto;
  flex: 1;
}

.empty {
  padding: 24px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}
</style>
