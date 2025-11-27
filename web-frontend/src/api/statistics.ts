import request from '@/utils/request'

// 获取交通统计数据
export function getTrafficStatistics(params?: any) {
  return request({
    url: '/api/statistics/traffic',
    method: 'get',
    params
  })
}

// 获取每日交通统计
export function getDailyTrafficStats(params?: any) {
  return request({
    url: '/api/statistics/traffic/daily',
    method: 'get',
    params
  })
}

// 获取仓储统计数据
export function getWarehouseStatistics(params?: any) {
  return request({
    url: '/api/statistics/warehouse',
    method: 'get',
    params
  })
}

// 获取每日仓储统计
export function getDailyWarehouseStats(params?: any) {
  return request({
    url: '/api/statistics/warehouse/daily',
    method: 'get',
    params
  })
}

// 获取停车统计数据
export function getParkingStatistics(params?: any) {
  return request({
    url: '/api/statistics/parking',
    method: 'get',
    params
  })
}

// 获取每日停车统计
export function getDailyParkingStats(params?: any) {
  return request({
    url: '/api/statistics/parking/daily',
    method: 'get',
    params
  })
}

// 获取仪表盘数据
export function getDashboardData() {
  return request({
    url: '/api/statistics/dashboard',
    method: 'get'
  })
}

// 导出报表
export function exportReport(params: any) {
  return request({
    url: '/api/statistics/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取告警统计
export function getAlertStatistics(params?: any) {
  return request({
    url: '/api/statistics/alerts',
    method: 'get',
    params
  })
}

// 获取趋势分析数据
export function getTrendAnalysis(params?: any) {
  return request({
    url: '/api/statistics/trend',
    method: 'get',
    params
  })
}
