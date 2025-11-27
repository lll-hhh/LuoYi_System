<template>
  <div class="role-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="角色编码" width="120" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="userCount" label="用户数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handlePermission(row)">权限配置</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.isSystem">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            active-value="ACTIVE"
            inactive-value="INACTIVE"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="permissionVisible"
      title="权限配置"
      width="600px"
    >
      <div class="permission-header">
        <span>角色: {{ currentRole.roleName }}</span>
      </div>
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :default-expanded-keys="expandedKeys"
        :default-checked-keys="checkedKeys"
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const permissionVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  roleName: ''
})

const tableData = ref([
  { roleId: 1, roleCode: 'ADMIN', roleName: '系统管理员', description: '拥有系统所有权限', userCount: 3, status: 'ACTIVE', createdAt: '2024-01-01 00:00:00', isSystem: true },
  { roleId: 2, roleCode: 'MONITOR', roleName: '监控员', description: '负责实时监控和异常处理', userCount: 15, status: 'ACTIVE', createdAt: '2024-01-01 00:00:00', isSystem: true },
  { roleId: 3, roleCode: 'DISPATCHER', roleName: '调度员', description: '负责车辆调度和任务分配', userCount: 8, status: 'ACTIVE', createdAt: '2024-01-01 00:00:00', isSystem: true },
  { roleId: 4, roleCode: 'STAFF', roleName: '普通员工', description: '基础查看权限', userCount: 50, status: 'ACTIVE', createdAt: '2024-01-01 00:00:00', isSystem: false }
])

const formData = reactive({
  roleId: null as number | null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 'ACTIVE'
})

const currentRole = reactive({
  roleId: null as number | null,
  roleName: ''
})

const permissionTree = ref([
  {
    id: 1,
    name: '首页',
    children: [
      { id: 11, name: '数据概览' }
    ]
  },
  {
    id: 2,
    name: '交通管理',
    children: [
      { id: 21, name: '道路管理' },
      { id: 22, name: '路口管理' },
      { id: 23, name: '摄像头管理' }
    ]
  },
  {
    id: 3,
    name: '实时监控',
    children: [
      { id: 31, name: '实时监控' },
      { id: 32, name: '异常管理' }
    ]
  },
  {
    id: 4,
    name: '仓储管理',
    children: [
      { id: 41, name: '仓库管理' },
      { id: 42, name: '货物管理' }
    ]
  },
  {
    id: 5,
    name: '系统管理',
    children: [
      { id: 51, name: '员工管理' },
      { id: 52, name: '部门管理' },
      { id: 53, name: '角色管理' }
    ]
  }
])

const expandedKeys = ref([1, 2, 3, 4, 5])
const checkedKeys = ref([11, 21, 22, 31])

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

const handleSearch = () => {
  // TODO: 调用API
}

const handleReset = () => {
  searchForm.roleName = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    roleId: null,
    roleCode: '',
    roleName: '',
    description: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handlePermission = (row: any) => {
  currentRole.roleId = row.roleId
  currentRole.roleName = row.roleName
  permissionVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该角色吗?', '提示', { type: 'warning' })
    .then(() => {
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

const handleSubmit = () => {
  ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
  dialogVisible.value = false
}

const handleSavePermission = () => {
  ElMessage.success('权限保存成功')
  permissionVisible.value = false
}
</script>

<style scoped lang="scss">
.role-management {
  .search-card {
    margin-bottom: 16px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .permission-header {
    margin-bottom: 16px;
    font-weight: 600;
  }
}
</style>
