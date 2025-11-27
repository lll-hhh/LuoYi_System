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
