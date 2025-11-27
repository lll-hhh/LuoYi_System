import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { monitoringApi } from '@/api/monitoring'

export interface RealtimeData {
  roadId: number
  roadName: string
  flow: number
  speed: number
  congestionIndex: number
  status: 'normal' | 'congested' | 'blocked'
  timestamp: string
}

export interface AnomalyEvent {
  id: number
  type: 'accident' | 'congestion' | 'violation' | 'equipment' | 'weather'
  level: 'low' | 'medium' | 'high' | 'critical'
  title: string
  description: string
  location: string
  roadId?: number
  junctionId?: number
  cameraId?: number
  latitude: number
  longitude: number
  status: 'pending' | 'processing' | 'resolved' | 'closed'
  reportedBy?: string
  assignedTo?: string
  images: string[]
  videoUrl?: string
  createdAt: string
  resolvedAt?: string
  resolvedBy?: string
  resolution?: string
}

export interface AlertRule {
  id: number
  name: string
  type: 'congestion' | 'speed' | 'flow' | 'equipment'
  condition: string
  threshold: number
  level: 'low' | 'medium' | 'high' | 'critical'
  enabled: boolean
  notifyMethods: ('email' | 'sms' | 'push')[]
  createdAt: string
}

export const useMonitoringStore = defineStore('monitoring', () => {
  // 实时数据
  const realtimeData = ref<RealtimeData[]>([])
  const realtimeLoading = ref(false)
  const lastUpdateTime = ref<string>('')
  
  // 异常事件
  const anomalyEvents = ref<AnomalyEvent[]>([])
  const anomalyLoading = ref(false)
  const anomalyTotal = ref(0)
  const currentAnomaly = ref<AnomalyEvent | null>(null)
  
  // 告警规则
  const alertRules = ref<AlertRule[]>([])
  
  // WebSocket 连接状态
  const wsConnected = ref(false)
  
  // 计算属性
  const pendingAnomalies = computed(() => 
    anomalyEvents.value.filter(e => e.status === 'pending')
  )
  const criticalAnomalies = computed(() => 
    anomalyEvents.value.filter(e => e.level === 'critical' && e.status !== 'closed')
  )
  const congestedRoads = computed(() => 
    realtimeData.value.filter(r => r.status === 'congested' || r.status === 'blocked')
  )
  const avgCongestionIndex = computed(() => {
    if (realtimeData.value.length === 0) return 0
    return realtimeData.value.reduce((sum, r) => sum + r.congestionIndex, 0) / realtimeData.value.length
  })

  // 获取实时数据
  async function fetchRealtimeData() {
    realtimeLoading.value = true
    try {
      const res = await monitoringApi.getRealtimeData()
      realtimeData.value = res.data.list
      lastUpdateTime.value = new Date().toISOString()
    } finally {
      realtimeLoading.value = false
    }
  }

  // 更新单条实时数据
  function updateRealtimeItem(data: RealtimeData) {
    const index = realtimeData.value.findIndex(r => r.roadId === data.roadId)
    if (index > -1) {
      realtimeData.value[index] = data
    } else {
      realtimeData.value.push(data)
    }
    lastUpdateTime.value = new Date().toISOString()
  }

  // 获取异常事件列表
  async function fetchAnomalyEvents(params: { 
    page?: number
    pageSize?: number
    status?: string
    level?: string
    type?: string
    startDate?: string
    endDate?: string
  } = {}) {
    anomalyLoading.value = true
    try {
      const res = await monitoringApi.getAnomalyList(params)
      anomalyEvents.value = res.data.list
      anomalyTotal.value = res.data.total
    } finally {
      anomalyLoading.value = false
    }
  }

  // 获取异常事件详情
  async function fetchAnomalyDetail(id: number) {
    const res = await monitoringApi.getAnomalyDetail(id)
    currentAnomaly.value = res.data
    return res.data
  }

  // 创建异常事件
  async function createAnomaly(data: Partial<AnomalyEvent>) {
    const res = await monitoringApi.createAnomaly(data)
    await fetchAnomalyEvents()
    return res.data
  }

  // 更新异常事件
  async function updateAnomaly(id: number, data: Partial<AnomalyEvent>) {
    const res = await monitoringApi.updateAnomaly(id, data)
    await fetchAnomalyEvents()
    return res.data
  }

  // 处理异常事件
  async function processAnomaly(id: number, assignedTo: string) {
    const res = await monitoringApi.processAnomaly(id, { assignedTo })
    await fetchAnomalyEvents()
    return res.data
  }

  // 解决异常事件
  async function resolveAnomaly(id: number, resolution: string) {
    const res = await monitoringApi.resolveAnomaly(id, { resolution })
    await fetchAnomalyEvents()
    return res.data
  }

  // 关闭异常事件
  async function closeAnomaly(id: number) {
    const res = await monitoringApi.closeAnomaly(id)
    await fetchAnomalyEvents()
    return res.data
  }

  // 添加新的异常事件（来自WebSocket）
  function addAnomalyEvent(event: AnomalyEvent) {
    anomalyEvents.value.unshift(event)
    anomalyTotal.value++
  }

  // 获取告警规则
  async function fetchAlertRules() {
    const res = await monitoringApi.getAlertRules()
    alertRules.value = res.data.list
  }

  // 创建告警规则
  async function createAlertRule(data: Partial<AlertRule>) {
    const res = await monitoringApi.createAlertRule(data)
    await fetchAlertRules()
    return res.data
  }

  // 更新告警规则
  async function updateAlertRule(id: number, data: Partial<AlertRule>) {
    const res = await monitoringApi.updateAlertRule(id, data)
    await fetchAlertRules()
    return res.data
  }

  // 删除告警规则
  async function deleteAlertRule(id: number) {
    await monitoringApi.deleteAlertRule(id)
    await fetchAlertRules()
  }

  // 切换告警规则状态
  async function toggleAlertRule(id: number, enabled: boolean) {
    await monitoringApi.toggleAlertRule(id, enabled)
    await fetchAlertRules()
  }

  // 设置WebSocket连接状态
  function setWsConnected(status: boolean) {
    wsConnected.value = status
  }

  // 重置状态
  function reset() {
    realtimeData.value = []
    anomalyEvents.value = []
    alertRules.value = []
    currentAnomaly.value = null
    wsConnected.value = false
  }

  return {
    realtimeData,
    realtimeLoading,
    lastUpdateTime,
    anomalyEvents,
    anomalyLoading,
    anomalyTotal,
    currentAnomaly,
    alertRules,
    wsConnected,
    pendingAnomalies,
    criticalAnomalies,
    congestedRoads,
    avgCongestionIndex,
    fetchRealtimeData,
    updateRealtimeItem,
    fetchAnomalyEvents,
    fetchAnomalyDetail,
    createAnomaly,
    updateAnomaly,
    processAnomaly,
    resolveAnomaly,
    closeAnomaly,
    addAnomalyEvent,
    fetchAlertRules,
    createAlertRule,
    updateAlertRule,
    deleteAlertRule,
    toggleAlertRule,
    setWsConnected,
    reset
  }
})
