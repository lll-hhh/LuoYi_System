import request from '@/utils/request'

// 获取停车场列表
export function getParkingLotList(params?: any) {
  return request({
    url: '/api/parking-lots',
    method: 'get',
    params
  })
}

// 获取停车场详情
export function getParkingLotDetail(id: number) {
  return request({
    url: `/api/parking-lots/${id}`,
    method: 'get'
  })
}

// 创建停车场
export function createParkingLot(data: any) {
  return request({
    url: '/api/parking-lots',
    method: 'post',
    data
  })
}

// 更新停车场
export function updateParkingLot(id: number, data: any) {
  return request({
    url: `/api/parking-lots/${id}`,
    method: 'put',
    data
  })
}

// 删除停车场
export function deleteParkingLot(id: number) {
  return request({
    url: `/api/parking-lots/${id}`,
    method: 'delete'
  })
}

// 获取车位列表
export function getParkingSpaceList(params?: any) {
  return request({
    url: '/api/parking-spaces',
    method: 'get',
    params
  })
}

// 获取停车记录
export function getParkingRecordList(params?: any) {
  return request({
    url: '/api/parking-records',
    method: 'get',
    params
  })
}

// 车辆入场
export function vehicleEntry(data: any) {
  return request({
    url: '/api/parking-records/entry',
    method: 'post',
    data
  })
}

// 车辆出场
export function vehicleExit(data: any) {
  return request({
    url: '/api/parking-records/exit',
    method: 'post',
    data
  })
}

// 获取收费规则
export function getFeeRules(params?: any) {
  return request({
    url: '/api/parking-fees',
    method: 'get',
    params
  })
}

// 计算停车费用
export function calculateFee(recordId: number) {
  return request({
    url: `/api/parking-records/${recordId}/fee`,
    method: 'get'
  })
}

// 获取车位列表
export function getSpaceList(lotId: number, params?: any) {
  return request({
    url: `/api/parking-lots/${lotId}/spaces`,
    method: 'get',
    params
  })
}

// 获取车位详情
export function getSpaceDetail(lotId: number, spaceId: number) {
  return request({
    url: `/api/parking-lots/${lotId}/spaces/${spaceId}`,
    method: 'get'
  })
}

// 更新车位状态
export function updateSpaceStatus(lotId: number, spaceId: number, status: string) {
  return request({
    url: `/api/parking-lots/${lotId}/spaces/${spaceId}/status`,
    method: 'put',
    data: { status }
  })
}

// 预约车位
export function reserveSpace(lotId: number, spaceId: number, data: any) {
  return request({
    url: `/api/parking-lots/${lotId}/spaces/${spaceId}/reserve`,
    method: 'post',
    data
  })
}

// 取消预约
export function cancelReservation(lotId: number, spaceId: number) {
  return request({
    url: `/api/parking-lots/${lotId}/spaces/${spaceId}/cancel-reservation`,
    method: 'post'
  })
}

// 获取停车记录列表
export function getRecordList(params?: any) {
  return request({
    url: '/api/parking-records',
    method: 'get',
    params
  })
}

// 获取实时统计
export function getRealtimeStats() {
  return request({
    url: '/api/parking/realtime-stats',
    method: 'get'
  })
}

// 获取收入统计
export function getRevenueStats(params?: any) {
  return request({
    url: '/api/parking/revenue-stats',
    method: 'get',
    params
  })
}

// 导出为统一对象
export const parkingApi = {
  getParkingLotList,
  getParkingLotDetail,
  createParkingLot,
  updateParkingLot,
  deleteParkingLot,
  getParkingSpaceList,
  getParkingRecordList,
  vehicleEntry,
  vehicleExit,
  getFeeRules,
  calculateFee,
  getSpaceList,
  getSpaceDetail,
  updateSpaceStatus,
  reserveSpace,
  cancelReservation,
  getRecordList,
  getRealtimeStats,
  getRevenueStats
}
