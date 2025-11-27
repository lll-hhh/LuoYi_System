import request from '@/utils/request'

// 获取道路列表
export function getRoadList(params?: any) {
  return request({
    url: '/api/roads',
    method: 'get',
    params
  })
}

// 获取道路详情
export function getRoadDetail(id: number) {
  return request({
    url: `/api/roads/${id}`,
    method: 'get'
  })
}

// 创建道路
export function createRoad(data: any) {
  return request({
    url: '/api/roads',
    method: 'post',
    data
  })
}

// 更新道路
export function updateRoad(id: number, data: any) {
  return request({
    url: `/api/roads/${id}`,
    method: 'put',
    data
  })
}

// 删除道路
export function deleteRoad(id: number) {
  return request({
    url: `/api/roads/${id}`,
    method: 'delete'
  })
}

// 获取路口列表
export function getJunctionList(params?: any) {
  return request({
    url: '/api/junctions',
    method: 'get',
    params
  })
}

// 获取摄像头列表
export function getCameraList(params?: any) {
  return request({
    url: '/api/cameras',
    method: 'get',
    params
  })
}

// 获取摄像头详情
export function getCameraDetail(id: number) {
  return request({
    url: `/api/cameras/${id}`,
    method: 'get'
  })
}

// 创建摄像头
export function createCamera(data: any) {
  return request({
    url: '/api/cameras',
    method: 'post',
    data
  })
}

// 更新摄像头
export function updateCamera(id: number, data: any) {
  return request({
    url: `/api/cameras/${id}`,
    method: 'put',
    data
  })
}

// 删除摄像头
export function deleteCamera(id: number) {
  return request({
    url: `/api/cameras/${id}`,
    method: 'delete'
  })
}

// 获取路口详情
export function getJunctionDetail(id: number) {
  return request({
    url: `/api/junctions/${id}`,
    method: 'get'
  })
}

// 创建路口
export function createJunction(data: any) {
  return request({
    url: '/api/junctions',
    method: 'post',
    data
  })
}

// 更新路口
export function updateJunction(id: number, data: any) {
  return request({
    url: `/api/junctions/${id}`,
    method: 'put',
    data
  })
}

// 删除路口
export function deleteJunction(id: number) {
  return request({
    url: `/api/junctions/${id}`,
    method: 'delete'
  })
}

// 更新信号灯配时
export function updateSignalTiming(id: number, data: any) {
  return request({
    url: `/api/junctions/${id}/signal-timing`,
    method: 'put',
    data
  })
}

// 导出为统一对象
export const trafficApi = {
  getRoadList,
  getRoadDetail,
  createRoad,
  updateRoad,
  deleteRoad,
  getJunctionList,
  getJunctionDetail,
  createJunction,
  updateJunction,
  deleteJunction,
  updateSignalTiming,
  getCameraList,
  getCameraDetail,
  createCamera,
  updateCamera,
  deleteCamera
}
