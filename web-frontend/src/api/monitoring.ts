import request from '@/utils/request'

// 获取实时监控数据
export function getRealtimeData() {
  return request({
    url: '/api/monitoring/realtime',
    method: 'get'
  })
}

// 获取交通流量数据
export function getTrafficFlow(params?: any) {
  return request({
    url: '/api/monitoring/traffic-flow',
    method: 'get',
    params
  })
}

// 获取异常事件列表
export function getAnomalyList(params?: any) {
  return request({
    url: '/api/monitoring/anomalies',
    method: 'get',
    params
  })
}

// 获取异常事件详情
export function getAnomalyDetail(id: number) {
  return request({
    url: `/api/monitoring/anomalies/${id}`,
    method: 'get'
  })
}

// 处理异常事件
export function handleAnomaly(id: number, data: any) {
  return request({
    url: `/api/monitoring/anomalies/${id}/handle`,
    method: 'post',
    data
  })
}

// 获取摄像头视频流
export function getCameraStream(cameraId: number) {
  return request({
    url: `/api/monitoring/cameras/${cameraId}/stream`,
    method: 'get'
  })
}

// 获取历史视频
export function getHistoryVideo(params: any) {
  return request({
    url: '/api/monitoring/history-video',
    method: 'get',
    params
  })
}

// 触发车辆检测
export function detectVehicles(data: any) {
  return request({
    url: '/api/algorithm/detect/vehicle',
    method: 'post',
    data
  })
}

// 触发车牌识别
export function recognizePlate(data: any) {
  return request({
    url: '/api/algorithm/detect/plate',
    method: 'post',
    data
  })
}

// 触发异常检测
export function detectAnomaly(data: any) {
  return request({
    url: '/api/algorithm/detect/anomaly',
    method: 'post',
    data
  })
}

// 创建异常事件
export function createAnomaly(data: any) {
  return request({
    url: '/api/monitoring/anomalies',
    method: 'post',
    data
  })
}

// 更新异常事件
export function updateAnomaly(id: number, data: any) {
  return request({
    url: `/api/monitoring/anomalies/${id}`,
    method: 'put',
    data
  })
}

// 处理异常事件（分配）
export function processAnomaly(id: number, data: any) {
  return request({
    url: `/api/monitoring/anomalies/${id}/process`,
    method: 'post',
    data
  })
}

// 解决异常事件
export function resolveAnomaly(id: number, data: any) {
  return request({
    url: `/api/monitoring/anomalies/${id}/resolve`,
    method: 'post',
    data
  })
}

// 关闭异常事件
export function closeAnomaly(id: number) {
  return request({
    url: `/api/monitoring/anomalies/${id}/close`,
    method: 'post'
  })
}

// 获取告警规则列表
export function getAlertRules() {
  return request({
    url: '/api/monitoring/alert-rules',
    method: 'get'
  })
}

// 创建告警规则
export function createAlertRule(data: any) {
  return request({
    url: '/api/monitoring/alert-rules',
    method: 'post',
    data
  })
}

// 更新告警规则
export function updateAlertRule(id: number, data: any) {
  return request({
    url: `/api/monitoring/alert-rules/${id}`,
    method: 'put',
    data
  })
}

// 删除告警规则
export function deleteAlertRule(id: number) {
  return request({
    url: `/api/monitoring/alert-rules/${id}`,
    method: 'delete'
  })
}

// 切换告警规则状态
export function toggleAlertRule(id: number, enabled: boolean) {
  return request({
    url: `/api/monitoring/alert-rules/${id}/toggle`,
    method: 'post',
    data: { enabled }
  })
}

// 导出为统一对象
export const monitoringApi = {
  getRealtimeData,
  getTrafficFlow,
  getAnomalyList,
  getAnomalyDetail,
  handleAnomaly,
  getCameraStream,
  getHistoryVideo,
  detectVehicles,
  recognizePlate,
  detectAnomaly,
  createAnomaly,
  updateAnomaly,
  processAnomaly,
  resolveAnomaly,
  closeAnomaly,
  getAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  toggleAlertRule
}
