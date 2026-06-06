import type {
  AgentProfileDto,
  ConversationDto,
  MessageDto,
  PeriodPhase,
  ConversationType,
  TagDto,
} from '@/types'

const BASE = '/api'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    throw new Error(`API ${res.status}: ${path}`)
  }
  return res.json() as Promise<T>
}

export const api = {
  profile: () => request<AgentProfileDto>('/profile'),
  tags: () => request<TagDto[]>('/tags'),
  conversations: (params: {
    phase: PeriodPhase
    type: ConversationType
    tagId?: number | null
    keyword?: string
  }) => {
    const q = new URLSearchParams({
      phase: params.phase,
      type: params.type,
    })
    if (params.tagId) q.set('tagId', String(params.tagId))
    if (params.keyword) q.set('keyword', params.keyword)
    return request<ConversationDto[]>(`/conversations?${q}`)
  },
  messages: (id: number) => request<MessageDto[]>(`/conversations/${id}/messages`),
  sendMessage: (body: { conversationId: number; senderId: number; content: string }) =>
    request<MessageDto>('/messages', { method: 'POST', body: JSON.stringify(body) }),
  markRead: (id: number) =>
    request<ConversationDto>(`/conversations/${id}/read`, { method: 'PUT' }),
  transfer: (id: number, targetTeacherTagId: number) =>
    request<ConversationDto>(`/conversations/${id}/transfer`, {
      method: 'PUT',
      body: JSON.stringify({ targetTeacherTagId }),
    }),
  updatePhase: (id: number, phase: PeriodPhase) =>
    request<ConversationDto>(`/conversations/${id}/phase?phase=${phase}`, { method: 'PUT' }),
}
