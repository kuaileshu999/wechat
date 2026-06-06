import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/api/client'
import type {
  AgentProfileDto,
  ConversationDto,
  ConversationType,
  MessageDto,
  PeriodPhase,
  TagDto,
} from '@/types'

const TEACHER_SENDER_ID = 2

export const useChatStore = defineStore('chat', () => {
  const profile = ref<AgentProfileDto | null>(null)
  const tags = ref<TagDto[]>([])
  const conversations = ref<ConversationDto[]>([])
  const messages = ref<MessageDto[]>([])
  const activeConversationId = ref<number | null>(null)
  const phase = ref<PeriodPhase>('CONVERSION')
  const chatType = ref<ConversationType>('PRIVATE')
  const selectedTagId = ref<number | null>(null)
  const searchKeyword = ref('')
  const loading = ref(false)
  const replyAsTeacher = ref(true)

  const activeConversation = computed(() =>
    conversations.value.find((c) => c.id === activeConversationId.value) ?? null
  )

  const privateCount = computed(() =>
    conversations.value.filter((c) => c.type === 'PRIVATE').length
  )

  async function loadProfile() {
    profile.value = await api.profile()
  }

  async function loadTags() {
    tags.value = await api.tags()
  }

  async function loadConversations() {
    loading.value = true
    try {
      conversations.value = await api.conversations({
        phase: phase.value,
        type: chatType.value,
        tagId: selectedTagId.value,
        keyword: searchKeyword.value || undefined,
      })
      if (
        activeConversationId.value &&
        !conversations.value.some((c) => c.id === activeConversationId.value)
      ) {
        activeConversationId.value = conversations.value[0]?.id ?? null
      }
      if (!activeConversationId.value && conversations.value.length > 0) {
        activeConversationId.value = conversations.value[0].id
      }
    } finally {
      loading.value = false
    }
  }

  async function selectConversation(id: number) {
    activeConversationId.value = id
    messages.value = await api.messages(id)
    await api.markRead(id)
    const idx = conversations.value.findIndex((c) => c.id === id)
    if (idx >= 0) {
      conversations.value[idx] = { ...conversations.value[idx], unreadCount: 0 }
    }
    await loadProfile()
  }

  function appendMessage(msg: MessageDto) {
    if (msg.conversationId !== activeConversationId.value) return
    if (!messages.value.some((m) => m.id === msg.id)) {
      messages.value.push(msg)
    }
  }

  async function sendMessage(content: string) {
    if (!activeConversationId.value || !content.trim()) return
    const senderId = replyAsTeacher.value ? TEACHER_SENDER_ID : (profile.value?.user.id ?? 1)
    const msg = await api.sendMessage({
      conversationId: activeConversationId.value,
      senderId,
      content: content.trim(),
    })
    appendMessage(msg)
    await loadConversations()
  }

  async function transferToTag(tagId: number) {
    if (!activeConversationId.value) return
    const updated = await api.transfer(activeConversationId.value, tagId)
    const idx = conversations.value.findIndex((c) => c.id === updated.id)
    if (idx >= 0) conversations.value[idx] = updated
  }

  async function refreshAll() {
    await Promise.all([loadProfile(), loadConversations()])
    if (activeConversationId.value) {
      messages.value = await api.messages(activeConversationId.value)
    }
  }

  return {
    profile,
    tags,
    conversations,
    messages,
    activeConversationId,
    activeConversation,
    phase,
    chatType,
    selectedTagId,
    searchKeyword,
    loading,
    replyAsTeacher,
    privateCount,
    loadProfile,
    loadTags,
    loadConversations,
    selectConversation,
    appendMessage,
    sendMessage,
    transferToTag,
    refreshAll,
  }
})
