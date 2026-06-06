import { onUnmounted, ref } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import type { MessageDto } from '@/types'

export function useWebSocket() {
  const connected = ref(false)
  let client: Client | null = null
  const messageHandlers = new Map<number, (msg: MessageDto) => void>()
  let refreshHandler: (() => void) | null = null

  function connect() {
    if (client?.connected) return

    client = new Client({
      webSocketFactory: () => new SockJS('/ws') as unknown as WebSocket,
      reconnectDelay: 3000,
      onConnect: () => {
        connected.value = true
        client?.subscribe('/topic/conversations', () => {
          refreshHandler?.()
        })
      },
      onDisconnect: () => {
        connected.value = false
      },
    })
    client.activate()
  }

  function subscribeConversation(conversationId: number, onMessage: (msg: MessageDto) => void) {
    messageHandlers.set(conversationId, onMessage)
    const sub = () => {
      client?.subscribe(`/topic/conversation/${conversationId}`, (frame) => {
        const msg = JSON.parse(frame.body) as MessageDto
        onMessage(msg)
      })
    }
    if (client?.connected) {
      sub()
    } else {
      const prev = client?.onConnect
      client!.onConnect = (frame) => {
        prev?.(frame)
        sub()
      }
    }
  }

  function onConversationsRefresh(handler: () => void) {
    refreshHandler = handler
  }

  function disconnect() {
    client?.deactivate()
    client = null
    connected.value = false
  }

  onUnmounted(disconnect)

  return { connected, connect, subscribeConversation, onConversationsRefresh, disconnect }
}
