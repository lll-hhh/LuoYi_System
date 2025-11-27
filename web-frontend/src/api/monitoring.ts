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
