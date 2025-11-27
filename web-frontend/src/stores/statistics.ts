import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { statisticsApi } from '@/api/statistics'

export interface TrafficStatistics {
  date: string
  totalFlow: number
  avgSpeed: number
  avgCongestionIndex: number
  accidentCount: number
  violationCount: number
}

export interface RoadStatistics {
  roadId: number
  roadName: string
  totalFlow: number
  avgSpeed: number
  congestionIndex: number
  peakHour: string
  peakFlow: number
}

export interface TimeDistribution {
  hour: number
  flow: number
  speed: number
  congestionIndex: number
}

export interface Report {
  id: number
  name: string
  type: 'daily' | 'weekly' | 'monthly' | 'custom'
  category: 'traffic' | 'accident' | 'congestion' | 'comprehensive'
  startDate: string
  endDate: string
  status: 'pending' | 'generating' | 'completed' | 'failed'
  filePath?: string
  fileSize?: number
  createdBy: string
  createdAt: string
  completedAt?: string
}

export interface DashboardStats {
  totalRoads: number
  totalJunctions: number
  totalCameras: number
  onlineCameras: number
  totalWarehouses: number
  totalCargos: number
  todayFlow: number
  avgCongestionIndex: number
  pendingAnomalies: number
  criticalAnomalies: number
}

export const useStatisticsStore = defineStore('statistics', () => {
  // 仪表盘统计
  const dashboardStats = ref<DashboardStats | null>(null)
  const dashboardLoading = ref(false)
  
  // 交通统计
  const trafficStats = ref<TrafficStatistics[]>([])
  const trafficLoading = ref(false)
  
  // 道路统计
  const roadStats = ref<RoadStatistics[]>([])
  const roadStatsLoading = ref(false)
  
  // 时段分布
  const timeDistribution = ref<TimeDistribution[]>([])
  const timeDistributionLoading = ref(false)
  
  // 报表
  const reports = ref<Report[]>([])
  const reportLoading = ref(false)
  const reportTotal = ref(0)
  const currentReport = ref<Report | null>(null)

  // 计算属性
  const totalFlow = computed(() => 
    trafficStats.value.reduce((sum, s) => sum + s.totalFlow, 0)
  )
  const avgSpeed = computed(() => {
    if (trafficStats.value.length === 0) return 0
    return trafficStats.value.reduce((sum, s) => sum + s.avgSpeed, 0) / trafficStats.value.length
  })
  const completedReports = computed(() => 
    reports.value.filter(r => r.status === 'completed')
  )
  const pendingReports = computed(() => 
    reports.value.filter(r => r.status === 'pending' || r.status === 'generating')
  )

  // 获取仪表盘统计
  async function fetchDashboardStats() {
    dashboardLoading.value = true
    try {
      const res = await statisticsApi.getDashboardStats()
      dashboardStats.value = res.data
    } finally {
      dashboardLoading.value = false
    }
  }

  // 获取交通统计
  async function fetchTrafficStats(params: {
    startDate: string
    endDate: string
    granularity?: 'hour' | 'day' | 'week' | 'month'
  }) {
    trafficLoading.value = true
    try {
      const res = await statisticsApi.getTrafficStats(params)
      trafficStats.value = res.data.list
    } finally {
      trafficLoading.value = false
    }
  }

  // 获取道路统计排行
  async function fetchRoadStats(params: {
    startDate: string
    endDate: string
    orderBy?: 'flow' | 'speed' | 'congestion'
    limit?: number
  }) {
    roadStatsLoading.value = true
    try {
      const res = await statisticsApi.getRoadStats(params)
      roadStats.value = res.data.list
    } finally {
      roadStatsLoading.value = false
    }
  }

  // 获取时段分布
  async function fetchTimeDistribution(params: {
    date: string
    roadId?: number
  }) {
    timeDistributionLoading.value = true
    try {
      const res = await statisticsApi.getTimeDistribution(params)
      timeDistribution.value = res.data.list
    } finally {
      timeDistributionLoading.value = false
    }
  }

  // 获取报表列表
  async function fetchReports(params: {
    page?: number
    pageSize?: number
    type?: string
    category?: string
    status?: string
  } = {}) {
    reportLoading.value = true
    try {
      const res = await statisticsApi.getReportList(params)
      reports.value = res.data.list
      reportTotal.value = res.data.total
    } finally {
      reportLoading.value = false
    }
  }

  // 获取报表详情
  async function fetchReportDetail(id: number) {
    const res = await statisticsApi.getReportDetail(id)
    currentReport.value = res.data
    return res.data
  }

  // 创建报表
  async function createReport(data: Partial<Report>) {
    const res = await statisticsApi.createReport(data)
    await fetchReports()
    return res.data
  }

  // 删除报表
  async function deleteReport(id: number) {
    await statisticsApi.deleteReport(id)
    await fetchReports()
  }

  // 下载报表
  async function downloadReport(id: number) {
    const res = await statisticsApi.downloadReport(id)
    return res.data
  }

  // 导出数据
  async function exportData(params: {
    type: 'traffic' | 'road' | 'anomaly'
    format: 'excel' | 'csv' | 'pdf'
    startDate: string
    endDate: string
    filters?: Record<string, any>
  }) {
    const res = await statisticsApi.exportData(params)
    return res.data
  }

  // 获取趋势分析
  async function fetchTrendAnalysis(params: {
    metric: 'flow' | 'speed' | 'congestion' | 'accident'
    period: 'week' | 'month' | 'quarter' | 'year'
  }) {
    const res = await statisticsApi.getTrendAnalysis(params)
    return res.data
  }

  // 获取对比分析
  async function fetchCompareAnalysis(params: {
    type: 'period' | 'road'
    targets: number[] | string[]
    startDate: string
    endDate: string
  }) {
    const res = await statisticsApi.getCompareAnalysis(params)
    return res.data
  }

  // 重置状态
  function reset() {
    dashboardStats.value = null
    trafficStats.value = []
    roadStats.value = []
    timeDistribution.value = []
    reports.value = []
    currentReport.value = null
  }

  return {
    dashboardStats,
    dashboardLoading,
    trafficStats,
    trafficLoading,
    roadStats,
    roadStatsLoading,
    timeDistribution,
    timeDistributionLoading,
    reports,
    reportLoading,
    reportTotal,
    currentReport,
    totalFlow,
    avgSpeed,
    completedReports,
    pendingReports,
    fetchDashboardStats,
    fetchTrafficStats,
    fetchRoadStats,
    fetchTimeDistribution,
    fetchReports,
    fetchReportDetail,
    createReport,
    deleteReport,
    downloadReport,
    exportData,
    fetchTrendAnalysis,
    fetchCompareAnalysis,
    reset
  }
})
