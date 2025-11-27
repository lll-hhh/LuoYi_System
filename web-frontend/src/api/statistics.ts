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

// 获取仪表盘统计
export function getDashboardStats() {
  return request({
    url: '/api/statistics/dashboard/stats',
    method: 'get'
  })
}

// 获取交通统计
export function getTrafficStats(params?: any) {
  return request({
    url: '/api/statistics/traffic/stats',
    method: 'get',
    params
  })
}

// 获取道路统计
export function getRoadStats(params?: any) {
  return request({
    url: '/api/statistics/road/stats',
    method: 'get',
    params
  })
}

// 获取时段分布
export function getTimeDistribution(params?: any) {
  return request({
    url: '/api/statistics/time-distribution',
    method: 'get',
    params
  })
}

// 获取报表列表
export function getReportList(params?: any) {
  return request({
    url: '/api/statistics/reports',
    method: 'get',
    params
  })
}

// 获取报表详情
export function getReportDetail(id: number) {
  return request({
    url: `/api/statistics/reports/${id}`,
    method: 'get'
  })
}

// 创建报表
export function createReport(data: any) {
  return request({
    url: '/api/statistics/reports',
    method: 'post',
    data
  })
}

// 删除报表
export function deleteReport(id: number) {
  return request({
    url: `/api/statistics/reports/${id}`,
    method: 'delete'
  })
}

// 下载报表
export function downloadReport(id: number) {
  return request({
    url: `/api/statistics/reports/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 导出数据
export function exportData(params: any) {
  return request({
    url: '/api/statistics/export-data',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

// 获取对比分析
export function getCompareAnalysis(params?: any) {
  return request({
    url: '/api/statistics/compare',
    method: 'get',
    params
  })
}

// 导出为统一对象
export const statisticsApi = {
  getTrafficStatistics,
  getDailyTrafficStats,
  getWarehouseStatistics,
  getDailyWarehouseStats,
  getParkingStatistics,
  getDailyParkingStats,
  getDashboardData,
  exportReport,
  getAlertStatistics,
  getTrendAnalysis,
  getDashboardStats,
  getTrafficStats,
  getRoadStats,
  getTimeDistribution,
  getReportList,
  getReportDetail,
  createReport,
  deleteReport,
  downloadReport,
  exportData,
  getCompareAnalysis
}
