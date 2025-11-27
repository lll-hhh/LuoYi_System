import request from '@/utils/request'

// 获取用户列表
export function getUserList(params?: any) {
  return request({
    url: '/api/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id: number) {
  return request({
    url: `/api/users/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data: any) {
  return request({
    url: '/api/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id: number, data: any) {
  return request({
    url: `/api/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id: number) {
  return request({
    url: `/api/users/${id}`,
    method: 'delete'
  })
}

// 重置密码
export function resetPassword(id: number) {
  return request({
    url: `/api/users/${id}/reset-password`,
    method: 'post'
  })
}

// 获取角色列表
export function getRoleList(params?: any) {
  return request({
    url: '/api/roles',
    method: 'get',
    params
  })
}

// 获取角色详情
export function getRoleDetail(id: number) {
  return request({
    url: `/api/roles/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data: any) {
  return request({
    url: '/api/roles',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id: number, data: any) {
  return request({
    url: `/api/roles/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id: number) {
  return request({
    url: `/api/roles/${id}`,
    method: 'delete'
  })
}

// 获取权限列表
export function getPermissionList() {
  return request({
    url: '/api/permissions',
    method: 'get'
  })
}

// 获取部门列表
export function getDepartmentList(params?: any) {
  return request({
    url: '/api/departments',
    method: 'get',
    params
  })
}

// 获取员工列表
export function getEmployeeList(params?: any) {
  return request({
    url: '/api/employees',
    method: 'get',
    params
  })
}

// 获取员工详情
export function getEmployeeDetail(id: number) {
  return request({
    url: `/api/employees/${id}`,
    method: 'get'
  })
}

// 创建员工
export function createEmployee(data: any) {
  return request({
    url: '/api/employees',
    method: 'post',
    data
  })
}

// 更新员工
export function updateEmployee(id: number, data: any) {
  return request({
    url: `/api/employees/${id}`,
    method: 'put',
    data
  })
}

// 删除员工
export function deleteEmployee(id: number) {
  return request({
    url: `/api/employees/${id}`,
    method: 'delete'
  })
}

// 获取操作日志
export function getOperationLogs(params?: any) {
  return request({
    url: '/api/operation-logs',
    method: 'get',
    params
  })
}
