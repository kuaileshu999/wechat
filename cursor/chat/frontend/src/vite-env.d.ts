/// <reference types="vite/client" />

declare module 'sockjs-client' {
  export default class SockJS {
    constructor(url: string, _reserved?: unknown, options?: Record<string, unknown>)
    close(): void
    onopen: (() => void) | null
    onclose: (() => void) | null
    onmessage: ((e: { data: string }) => void) | null
    send(data: string): void
  }
}
