import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { systemApi } from '@/api/system'

export interface Employee {
  id: number
  username: string
  name: string
  avatar?: string
  email: string
  phone: string
  gender: 'male' | 'female'
  departmentId: number
  departmentName: string
  roleId: number
  roleName: string
  position: string
  status: 'active' | 'inactive' | 'locked'
  lastLoginAt?: string
  createdAt: string
  updatedAt: string
}

export interface Department {
  id: number
  name: string
  code: string
  parentId?: number
  parentName?: string
  manager?: string
  managerId?: number
  description?: string
  order: number
  status: 'active' | 'inactive'
  employeeCount: number
  children?: Department[]
  createdAt: string
  updatedAt: string
}

export interface Role {
  id: number
  name: string
  code: string
  description?: string
  permissions: string[]
  status: 'active' | 'inactive'
  employeeCount: number
  createdAt: string
  updatedAt: string
}

export interface Task {
  id: number
  title: string
  type: 'transport' | 'maintenance' | 'inspection' | 'emergency'
  priority: 'low' | 'medium' | 'high' | 'urgent'
  status: 'pending' | 'assigned' | 'in_progress' | 'completed' | 'cancelled'
  description: string
  assigneeId?: number
  assigneeName?: string
  departmentId?: number
  departmentName?: string
  startTime?: string
  endTime?: string
  deadline: string
  progress: number
  attachments: string[]
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface Permission {
  id: string
  name: string
  module: string
  children?: Permission[]
}

export const useSystemStore = defineStore('system', () => {
  // 员工数据
  const employees = ref<Employee[]>([])
  const employeeLoading = ref(false)
  const employeeTotal = ref(0)
  const currentEmployee = ref<Employee | null>(null)
  
  // 部门数据
  const departments = ref<Department[]>([])
  const departmentTree = ref<Department[]>([])
  const departmentLoading = ref(false)
  const currentDepartment = ref<Department | null>(null)
  
  // 角色数据
  const roles = ref<Role[]>([])
  const roleLoading = ref(false)
  const currentRole = ref<Role | null>(null)
  
  // 任务数据
  const tasks = ref<Task[]>([])
  const taskLoading = ref(false)
  const taskTotal = ref(0)
  const currentTask = ref<Task | null>(null)
  
  // 权限数据
  const permissions = ref<Permission[]>([])

  // 计算属性
  const activeEmployees = computed(() => 
    employees.value.filter(e => e.status === 'active')
  )
  const activeDepartments = computed(() => 
    departments.value.filter(d => d.status === 'active')
  )
  const activeRoles = computed(() => 
    roles.value.filter(r => r.status === 'active')
  )
  const pendingTasks = computed(() => 
    tasks.value.filter(t => t.status === 'pending' || t.status === 'assigned')
  )
  const urgentTasks = computed(() => 
    tasks.value.filter(t => t.priority === 'urgent' && t.status !== 'completed')
  )

  // ==================== 员工管理 ====================
  
  async function fetchEmployees(params: {
    page?: number
    pageSize?: number
    status?: string
    departmentId?: number
    roleId?: number
    keyword?: string
  } = {}) {
    employeeLoading.value = true
    try {
      const res = await systemApi.getEmployeeList(params)
      employees.value = res.data.list
      employeeTotal.value = res.data.total
    } finally {
      employeeLoading.value = false
    }
  }

  async function fetchEmployeeDetail(id: number) {
    const res = await systemApi.getEmployeeDetail(id)
    currentEmployee.value = res.data
    return res.data
  }

  async function createEmployee(data: Partial<Employee>) {
    const res = await systemApi.createEmployee(data)
    await fetchEmployees()
    return res.data
  }

  async function updateEmployee(id: number, data: Partial<Employee>) {
    const res = await systemApi.updateEmployee(id, data)
    await fetchEmployees()
    return res.data
  }

  async function deleteEmployee(id: number) {
    await systemApi.deleteEmployee(id)
    await fetchEmployees()
  }

  async function resetPassword(id: number) {
    const res = await systemApi.resetPassword(id)
    return res.data
  }

  async function toggleEmployeeStatus(id: number, status: 'active' | 'inactive' | 'locked') {
    await systemApi.updateEmployeeStatus(id, status)
    await fetchEmployees()
  }

  // ==================== 部门管理 ====================
  
  async function fetchDepartments(params: { status?: string; keyword?: string } = {}) {
    departmentLoading.value = true
    try {
      const res = await systemApi.getDepartmentList(params)
      departments.value = res.data.list
    } finally {
      departmentLoading.value = false
    }
  }

  async function fetchDepartmentTree() {
    const res = await systemApi.getDepartmentTree()
    departmentTree.value = res.data
    return res.data
  }

  async function fetchDepartmentDetail(id: number) {
    const res = await systemApi.getDepartmentDetail(id)
    currentDepartment.value = res.data
    return res.data
  }

  async function createDepartment(data: Partial<Department>) {
    const res = await systemApi.createDepartment(data)
    await fetchDepartments()
    await fetchDepartmentTree()
    return res.data
  }

  async function updateDepartment(id: number, data: Partial<Department>) {
    const res = await systemApi.updateDepartment(id, data)
    await fetchDepartments()
    await fetchDepartmentTree()
    return res.data
  }

  async function deleteDepartment(id: number) {
    await systemApi.deleteDepartment(id)
    await fetchDepartments()
    await fetchDepartmentTree()
  }

  // ==================== 角色管理 ====================
  
  async function fetchRoles(params: { status?: string; keyword?: string } = {}) {
    roleLoading.value = true
    try {
      const res = await systemApi.getRoleList(params)
      roles.value = res.data.list
    } finally {
      roleLoading.value = false
    }
  }

  async function fetchRoleDetail(id: number) {
    const res = await systemApi.getRoleDetail(id)
    currentRole.value = res.data
    return res.data
  }

  async function createRole(data: Partial<Role>) {
    const res = await systemApi.createRole(data)
    await fetchRoles()
    return res.data
  }

  async function updateRole(id: number, data: Partial<Role>) {
    const res = await systemApi.updateRole(id, data)
    await fetchRoles()
    return res.data
  }

  async function deleteRole(id: number) {
    await systemApi.deleteRole(id)
    await fetchRoles()
  }

  async function updateRolePermissions(id: number, permissionIds: string[]) {
    const res = await systemApi.updateRolePermissions(id, permissionIds)
    await fetchRoleDetail(id)
    return res.data
  }

  // ==================== 任务管理 ====================
  
  async function fetchTasks(params: {
    page?: number
    pageSize?: number
    status?: string
    type?: string
    priority?: string
    assigneeId?: number
    departmentId?: number
    keyword?: string
  } = {}) {
    taskLoading.value = true
    try {
      const res = await systemApi.getTaskList(params)
      tasks.value = res.data.list
      taskTotal.value = res.data.total
    } finally {
      taskLoading.value = false
    }
  }

  async function fetchTaskDetail(id: number) {
    const res = await systemApi.getTaskDetail(id)
    currentTask.value = res.data
    return res.data
  }

  async function createTask(data: Partial<Task>) {
    const res = await systemApi.createTask(data)
    await fetchTasks()
    return res.data
  }

  async function updateTask(id: number, data: Partial<Task>) {
    const res = await systemApi.updateTask(id, data)
    await fetchTasks()
    return res.data
  }

  async function deleteTask(id: number) {
    await systemApi.deleteTask(id)
    await fetchTasks()
  }

  async function assignTask(id: number, assigneeId: number) {
    const res = await systemApi.assignTask(id, assigneeId)
    await fetchTasks()
    return res.data
  }

  async function updateTaskStatus(id: number, status: Task['status']) {
    const res = await systemApi.updateTaskStatus(id, status)
    await fetchTasks()
    return res.data
  }

  async function updateTaskProgress(id: number, progress: number) {
    const res = await systemApi.updateTaskProgress(id, progress)
    await fetchTaskDetail(id)
    return res.data
  }

  // ==================== 权限管理 ====================
  
  async function fetchPermissions() {
    const res = await systemApi.getPermissions()
    permissions.value = res.data
    return res.data
  }

  // 重置状态
  function reset() {
    employees.value = []
    departments.value = []
    departmentTree.value = []
    roles.value = []
    tasks.value = []
    permissions.value = []
    currentEmployee.value = null
    currentDepartment.value = null
    currentRole.value = null
    currentTask.value = null
  }

  return {
    employees,
    employeeLoading,
    employeeTotal,
    currentEmployee,
    departments,
    departmentTree,
    departmentLoading,
    currentDepartment,
    roles,
    roleLoading,
    currentRole,
    tasks,
    taskLoading,
    taskTotal,
    currentTask,
    permissions,
    activeEmployees,
    activeDepartments,
    activeRoles,
    pendingTasks,
    urgentTasks,
    fetchEmployees,
    fetchEmployeeDetail,
    createEmployee,
    updateEmployee,
    deleteEmployee,
    resetPassword,
    toggleEmployeeStatus,
    fetchDepartments,
    fetchDepartmentTree,
    fetchDepartmentDetail,
    createDepartment,
    updateDepartment,
    deleteDepartment,
    fetchRoles,
    fetchRoleDetail,
    createRole,
    updateRole,
    deleteRole,
    updateRolePermissions,
    fetchTasks,
    fetchTaskDetail,
    createTask,
    updateTask,
    deleteTask,
    assignTask,
    updateTaskStatus,
    updateTaskProgress,
    fetchPermissions,
    reset
  }
})
