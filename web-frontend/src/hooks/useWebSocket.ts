import { ref, onMounted, onUnmounted } from 'vue'

interface WebSocketOptions {
  url: string
  reconnect?: boolean
  reconnectInterval?: number
  maxReconnects?: number
  onMessage?: (data: any) => void
  onOpen?: () => void
  onClose?: () => void
  onError?: (error: Event) => void
}

export function useWebSocket(options: WebSocketOptions) {
  const { 
    url, 
    reconnect = true, 
    reconnectInterval = 3000, 
    maxReconnects = 5,
    onMessage,
    onOpen,
    onClose,
    onError
  } = options

  const ws = ref<WebSocket | null>(null)
  const isConnected = ref(false)
  const reconnectCount = ref(0)
  const messageQueue = ref<any[]>([])

  let reconnectTimer: ReturnType<typeof setTimeout> | null = null

  const connect = () => {
    if (ws.value?.readyState === WebSocket.OPEN) return

    ws.value = new WebSocket(url)

    ws.value.onopen = () => {
      isConnected.value = true
      reconnectCount.value = 0
      onOpen?.()
      
      // 发送队列中的消息
      while (messageQueue.value.length > 0) {
        const msg = messageQueue.value.shift()
        send(msg)
      }
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        onMessage?.(data)
      } catch {
        onMessage?.(event.data)
      }
    }

    ws.value.onclose = () => {
      isConnected.value = false
      onClose?.()
      
      if (reconnect && reconnectCount.value < maxReconnects) {
        reconnectTimer = setTimeout(() => {
          reconnectCount.value++
          connect()
        }, reconnectInterval)
      }
    }

    ws.value.onerror = (error) => {
      onError?.(error)
    }
  }

  const send = (data: any) => {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(typeof data === 'string' ? data : JSON.stringify(data))
    } else {
      // 连接未建立时，将消息加入队列
      messageQueue.value.push(data)
    }
  }

  const close = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    ws.value?.close()
    ws.value = null
    isConnected.value = false
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    close()
  })

  return {
    ws,
    isConnected,
    reconnectCount,
    send,
    connect,
    close
  }
}
