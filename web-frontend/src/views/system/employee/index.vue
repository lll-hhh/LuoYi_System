<template>
  <div class="employee-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employeeName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.departmentId" placeholder="请选择" clearable>
            <el-option label="技术部" :value="1" />
            <el-option label="运营部" :value="2" />
            <el-option label="监控中心" :value="3" />
            <el-option label="行政部" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="在职" value="ACTIVE" />
            <el-option label="离职" value="INACTIVE" />
          </el-select>
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
          <span>员工列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增员工
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="employeeCode" label="工号" width="100" />
        <el-table-column prop="employeeName" label="姓名" width="100" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="employeeCode">
              <el-input v-model="formData.employeeCode" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="employeeName">
              <el-input v-model="formData.employeeName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!isEdit">
            <el-form-item label="密码" prop="password">
              <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentId">
              <el-select v-model="formData.departmentId" placeholder="请选择">
                <el-option label="技术部" :value="1" />
                <el-option label="运营部" :value="2" />
                <el-option label="监控中心" :value="3" />
                <el-option label="行政部" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="formData.roleId" placeholder="请选择">
                <el-option label="系统管理员" :value="1" />
                <el-option label="监控员" :value="2" />
                <el-option label="调度员" :value="3" />
                <el-option label="普通员工" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            active-value="ACTIVE"
            inactive-value="INACTIVE"
            active-text="在职"
            inactive-text="离职"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  employeeName: '',
  departmentId: null as number | null,
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 50
})

const tableData = ref([
  { employeeId: 1, employeeCode: 'EMP001', employeeName: '张三', username: 'zhangsan', departmentName: '技术部', roleName: '系统管理员', phone: '13800138001', email: 'zhangsan@luoyi.com', status: 'ACTIVE', lastLoginTime: '2024-01-15 10:30:00' },
  { employeeId: 2, employeeCode: 'EMP002', employeeName: '李四', username: 'lisi', departmentName: '监控中心', roleName: '监控员', phone: '13800138002', email: 'lisi@luoyi.com', status: 'ACTIVE', lastLoginTime: '2024-01-15 09:20:00' },
  { employeeId: 3, employeeCode: 'EMP003', employeeName: '王五', username: 'wangwu', departmentName: '运营部', roleName: '调度员', phone: '13800138003', email: 'wangwu@luoyi.com', status: 'ACTIVE', lastLoginTime: '2024-01-14 18:00:00' },
  { employeeId: 4, employeeCode: 'EMP004', employeeName: '赵六', username: 'zhaoliu', departmentName: '行政部', roleName: '普通员工', phone: '13800138004', email: 'zhaoliu@luoyi.com', status: 'INACTIVE', lastLoginTime: '2024-01-10 12:00:00' }
])

const formData = reactive({
  employeeId: null as number | null,
  employeeCode: '',
  employeeName: '',
  username: '',
  password: '',
  departmentId: null as number | null,
  roleId: null as number | null,
  phone: '',
  email: '',
  status: 'ACTIVE'
})

const formRules = {
  employeeCode: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  employeeName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑员工' : '新增员工')

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  searchForm.employeeName = ''
  searchForm.departmentId = null
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    employeeId: null,
    employeeCode: '',
    employeeName: '',
    username: '',
    password: '',
    departmentId: null,
    roleId: null,
    phone: '',
    email: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleResetPassword = (row: any) => {
  ElMessageBox.confirm(`确定要重置用户 ${row.employeeName} 的密码吗?`, '提示', { type: 'warning' })
    .then(() => {
      ElMessage.success('密码已重置为默认密码: 123456')
    })
    .catch(() => {})
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该员工吗?', '提示', { type: 'warning' })
    .then(() => {
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

const handleSubmit = () => {
  ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
  dialogVisible.value = false
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  handleSearch()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  handleSearch()
}
</script>

<style scoped lang="scss">
.employee-management {
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
  
  .pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
