import SockJS from 'sockjs-client'
import { Client, Message, StompSubscription, IFrame } from '@stomp/stompjs'

export interface WebSocketOptions {
  onConnect?: () => void
  onDisconnect?: () => void
  onError?: (error: string) => void
  reconnectDelay?: number
  debug?: boolean
}

export interface TrafficData {
  roadId: number
  roadName: string
  vehicleCount: number
  avgSpeed: number
  congestionLevel: number
  timestamp: string
}

export interface AnomalyAlert {
  id: number
  type: string
  level: string
  location: string
  description: string
  timestamp: string
}

export interface ParkingStatus {
  lotId: number
  lotName: string
  totalSpaces: number
  availableSpaces: number
  occupancyRate: number
  timestamp: string
}

export interface SystemStatus {
  onlineCameras: number
  totalCameras: number
  cpuUsage: number
  memoryUsage: number
  timestamp: string
}

class WebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, StompSubscription> = new Map()
  private connected: boolean = false

  connect(options: WebSocketOptions = {}): void {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const wsUrl = (import.meta as any).env?.VITE_WS_URL || 'http://localhost:8080/ws'
    
    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
      reconnectDelay: options.reconnectDelay || 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: options.debug ? (str: string) => console.log('[STOMP]', str) : () => {},
      onConnect: () => {
        this.connected = true
        console.log('WebSocket connected')
        options.onConnect?.()
      },
      onDisconnect: () => {
        this.connected = false
        console.log('WebSocket disconnected')
        options.onDisconnect?.()
      },
      onStompError: (frame: IFrame) => {
        console.error('STOMP error:', frame.headers['message'])
        options.onError?.(frame.headers['message'] || 'Unknown error')
      }
    })

    this.client.activate()
  }

  disconnect(): void {
    if (this.client) {
      this.subscriptions.forEach((sub) => sub.unsubscribe())
      this.subscriptions.clear()
      this.client.deactivate()
      this.client = null
      this.connected = false
    }
  }

  isConnected(): boolean {
    return this.connected
  }

  // 订阅实时车流量数据
  subscribeTrafficRealtime(callback: (data: TrafficData[]) => void): void {
    this.subscribe('/topic/traffic/realtime', (message) => {
      const data = JSON.parse(message.body) as TrafficData[]
      callback(data)
    })
  }

  // 订阅异常告警
  subscribeAnomalyAlert(callback: (data: AnomalyAlert) => void): void {
    this.subscribe('/topic/anomaly/alert', (message) => {
      const data = JSON.parse(message.body) as AnomalyAlert
      callback(data)
    })
  }

  // 订阅停车场状态
  subscribeParkingStatus(callback: (data: ParkingStatus[]) => void): void {
    this.subscribe('/topic/parking/status', (message) => {
      const data = JSON.parse(message.body) as ParkingStatus[]
      callback(data)
    })
  }

  // 订阅系统状态
  subscribeSystemStatus(callback: (data: SystemStatus) => void): void {
    this.subscribe('/topic/system/status', (message) => {
      const data = JSON.parse(message.body) as SystemStatus
      callback(data)
    })
  }

  // 订阅系统通知
  subscribeNotification(callback: (message: string) => void): void {
    this.subscribe('/topic/system/notification', (message) => {
      callback(message.body)
    })
  }

  // 通用订阅方法
  private subscribe(destination: string, callback: (message: Message) => void): void {
    if (!this.client || !this.connected) {
      console.warn('WebSocket not connected, waiting...')
      setTimeout(() => this.subscribe(destination, callback), 1000)
      return
    }

    if (this.subscriptions.has(destination)) {
      return
    }

    const subscription = this.client.subscribe(destination, callback)
    this.subscriptions.set(destination, subscription)
  }

  // 取消订阅
  unsubscribe(destination: string): void {
    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(destination)
    }
  }

  // 发送消息
  send(destination: string, body: object): void {
    if (!this.client || !this.connected) {
      console.warn('WebSocket not connected')
      return
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body)
    })
  }

  // 请求刷新数据
  requestRefresh(type: string): void {
    this.send('/app/refresh', { type })
  }
}

export const wsService = new WebSocketService()
export default wsService
