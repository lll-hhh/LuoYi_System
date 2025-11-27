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
