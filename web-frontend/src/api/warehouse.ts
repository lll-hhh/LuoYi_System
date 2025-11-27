import request from '@/utils/request'

// 获取仓库列表
export function getWarehouseList(params?: any) {
  return request({
    url: '/api/warehouses',
    method: 'get',
    params
  })
}

// 获取仓库详情
export function getWarehouseDetail(id: number) {
  return request({
    url: `/api/warehouses/${id}`,
    method: 'get'
  })
}

// 创建仓库
export function createWarehouse(data: any) {
  return request({
    url: '/api/warehouses',
    method: 'post',
    data
  })
}

// 更新仓库
export function updateWarehouse(id: number, data: any) {
  return request({
    url: `/api/warehouses/${id}`,
    method: 'put',
    data
  })
}

// 删除仓库
export function deleteWarehouse(id: number) {
  return request({
    url: `/api/warehouses/${id}`,
    method: 'delete'
  })
}

// 获取货物列表
export function getGoodsList(params?: any) {
  return request({
    url: '/api/goods',
    method: 'get',
    params
  })
}

// 获取库存列表
export function getInventoryList(params?: any) {
  return request({
    url: '/api/inventory',
    method: 'get',
    params
  })
}

// 入库操作
export function createInbound(data: any) {
  return request({
    url: '/api/inventory/inbound',
    method: 'post',
    data
  })
}

// 出库操作
export function createOutbound(data: any) {
  return request({
    url: '/api/inventory/outbound',
    method: 'post',
    data
  })
}

// 获取货物列表
export function getCargoList(params?: any) {
  return request({
    url: '/api/cargos',
    method: 'get',
    params
  })
}

// 获取货物详情
export function getCargoDetail(id: number) {
  return request({
    url: `/api/cargos/${id}`,
    method: 'get'
  })
}

// 创建货物
export function createCargo(data: any) {
  return request({
    url: '/api/cargos',
    method: 'post',
    data
  })
}

// 更新货物
export function updateCargo(id: number, data: any) {
  return request({
    url: `/api/cargos/${id}`,
    method: 'put',
    data
  })
}

// 删除货物
export function deleteCargo(id: number) {
  return request({
    url: `/api/cargos/${id}`,
    method: 'delete'
  })
}

// 货物入库
export function inboundCargo(id: number, data: any) {
  return request({
    url: `/api/cargos/${id}/inbound`,
    method: 'post',
    data
  })
}

// 货物出库
export function outboundCargo(id: number, data: any) {
  return request({
    url: `/api/cargos/${id}/outbound`,
    method: 'post',
    data
  })
}

// 货物调拨
export function transferCargo(id: number, data: any) {
  return request({
    url: `/api/cargos/${id}/transfer`,
    method: 'post',
    data
  })
}

// 获取货物流转记录
export function getCargoMovements(cargoId: number) {
  return request({
    url: `/api/cargos/${cargoId}/movements`,
    method: 'get'
  })
}

// 追踪货物
export function trackCargo(trackingNumber: string) {
  return request({
    url: `/api/cargos/track/${trackingNumber}`,
    method: 'get'
  })
}

// 导出为统一对象
export const warehouseApi = {
  getWarehouseList,
  getWarehouseDetail,
  createWarehouse,
  updateWarehouse,
  deleteWarehouse,
  getGoodsList,
  getInventoryList,
  createInbound,
  createOutbound,
  getCargoList,
  getCargoDetail,
  createCargo,
  updateCargo,
  deleteCargo,
  inboundCargo,
  outboundCargo,
  transferCargo,
  getCargoMovements,
  trackCargo
}
