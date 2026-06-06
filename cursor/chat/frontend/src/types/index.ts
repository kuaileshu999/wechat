export type UserRole = 'AGENT' | 'TEACHER' | 'STUDENT'
export type ConversationType = 'PRIVATE' | 'GROUP'
export type PeriodPhase = 'CONVERSION' | 'UNDERTAKING'

export interface UserDto {
  id: number
  name: string
  avatar: string
  role: UserRole
  online: boolean
}

export interface TagDto {
  id: number
  name: string
  color: string
}

export interface ConversationDto {
  id: number
  type: ConversationType
  student: UserDto
  lastMessagePreview: string
  lastMessageAt: string
  lastMessageTimeLabel: string
  phase: PeriodPhase
  unreadCount: number
  tags: TagDto[]
  assignedTeacherTag: TagDto | null
}

export interface MessageDto {
  id: number
  conversationId: number
  sender: UserDto
  content: string
  sentAt: string
  timeLabel: string
  containsMath: boolean
  outgoing: boolean
}

export interface AgentProfileDto {
  user: UserDto
  statusLabel: string
  accountCount: number
  totalUnread: number
  conversionCount: number
  undertakingCount: number
}
