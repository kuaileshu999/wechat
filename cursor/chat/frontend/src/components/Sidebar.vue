<script setup lang="ts">
import { watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import ContactList from './ContactList.vue'
import type { PeriodPhase } from '@/types'

const emit = defineEmits<{ select: [id: number] }>()
const store = useChatStore()

watch([() => store.phase, () => store.chatType, () => store.selectedTagId, () => store.searchKeyword], () => {
  store.loadConversations()
})

function setPhase(p: PeriodPhase) {
  store.phase = p
  store.loadConversations()
}

</script>

<template>
  <aside class="sidebar">
    <header class="profile" v-if="store.profile">
      <img :src="store.profile.user.avatar" alt="" class="avatar" />
      <div class="profile-info">
        <div class="profile-row">
          <span class="name">{{ store.profile.user.name }}</span>
          <span class="status-badge">{{ store.profile.statusLabel }}</span>
          <button class="icon-btn" title="设置">⚙</button>
        </div>
        <p class="meta">
          {{ store.profile.accountCount }}个账号 · {{ store.profile.totalUnread }}条未读
        </p>
      </div>
    </header>

    <div class="phase-tabs">
      <button
        :class="{ active: store.phase === 'CONVERSION' }"
        @click="setPhase('CONVERSION')"
      >
        转化期
        <span class="count">{{ store.profile?.conversionCount ?? 0 }}</span>
      </button>
      <button
        :class="{ active: store.phase === 'UNDERTAKING' }"
        @click="setPhase('UNDERTAKING')"
      >
        承接期
        <span class="count">{{ store.profile?.undertakingCount ?? 0 }}</span>
      </button>
    </div>

    <div class="type-tabs">
      <button
        :class="{ active: store.chatType === 'PRIVATE' }"
        @click="store.chatType = 'PRIVATE'; store.loadConversations()"
      >
        私聊
        <span class="count">3</span>
      </button>
      <button
        :class="{ active: store.chatType === 'GROUP' }"
        @click="store.chatType = 'GROUP'; store.loadConversations()"
      >
        群聊
        <span class="count">1</span>
      </button>
    </div>

    <div class="search-wrap">
      <span class="search-icon">🔍</span>
      <input
        v-model="store.searchKeyword"
        type="search"
        placeholder="搜索学生姓名、群名称"
        @input="store.loadConversations()"
      />
    </div>

    <div class="tag-filters">
      <button
        v-for="tag in store.tags"
        :key="tag.id"
        :class="{ active: store.selectedTagId === (tag.name === '全部' ? null : tag.id) }"
        :style="store.selectedTagId === tag.id || (tag.name === '全部' && !store.selectedTagId)
          ? { borderColor: tag.color, color: tag.name === '全部' ? 'var(--primary)' : tag.color }
          : {}"
        @click="store.selectedTagId = tag.name === '全部' ? null : tag.id; store.loadConversations()"
      >
        {{ tag.name }}
      </button>
    </div>

    <ContactList @select="emit('select', $event)" />
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  background: var(--surface);
  height: 100%;
}

.profile {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--border);
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--primary-light);
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-weight: 600;
  font-size: 15px;
}

.status-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: var(--primary-light);
  color: var(--primary);
}

.icon-btn {
  margin-left: auto;
  color: var(--text-secondary);
  font-size: 16px;
}

.meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.phase-tabs,
.type-tabs {
  display: flex;
  padding: 0 12px;
  gap: 4px;
}

.phase-tabs {
  margin-top: 8px;
}

.type-tabs {
  margin-bottom: 8px;
}

.phase-tabs button,
.type-tabs button {
  flex: 1;
  padding: 8px 4px;
  font-size: 13px;
  color: var(--text-secondary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.phase-tabs button.active,
.type-tabs button.active {
  color: var(--primary);
  background: var(--primary-light);
  font-weight: 500;
}

.count {
  font-size: 11px;
  opacity: 0.8;
}

.search-wrap {
  margin: 0 12px 8px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  opacity: 0.5;
}

.search-wrap input {
  width: 100%;
  padding: 8px 12px 8px 32px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg);
  outline: none;
}

.search-wrap input:focus {
  border-color: var(--primary);
}

.tag-filters {
  display: flex;
  gap: 8px;
  padding: 0 12px 12px;
  overflow-x: auto;
  flex-shrink: 0;
}

.tag-filters button {
  flex-shrink: 0;
  padding: 4px 12px;
  font-size: 12px;
  border: 1px solid var(--border);
  border-radius: 16px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.tag-filters button.active {
  background: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
  font-weight: 500;
}
</style>
