<template>
  <div class="task-management">
    <!-- 搜索和操作区 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="巡检" value="inspection" />
            <el-option label="维修" value="repair" />
            <el-option label="异常处理" value="anomaly" />
            <el-option label="调度" value="dispatch" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="searchForm.assigneeId" placeholder="全部" clearable style="width: 150px">
            <el-option 
              v-for="emp in employees" 
              :key="emp.id" 
              :label="emp.name" 
              :value="emp.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新建任务
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon pending"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon progress"><Loading /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.inProgress }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon completed"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon rate"><DataAnalysis /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.completionRate }}%</div>
              <div class="stat-label">完成率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table 
        :data="taskList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="title" label="任务标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityName(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="160" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              type="success" 
              link 
              size="small" 
              @click="handleStart(row)"
            >
              开始
            </el-button>
            <el-button 
              v-if="row.status === 'in_progress'"
              type="success" 
              link 
              size="small" 
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新建/编辑对话框 -->
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
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="巡检" value="inspection" />
            <el-option label="维修" value="repair" />
            <el-option label="异常处理" value="anomaly" />
            <el-option label="调度" value="dispatch" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="formData.priority">
            <el-radio value="low">低</el-radio>
            <el-radio value="medium">中</el-radio>
            <el-radio value="high">高</el-radio>
            <el-radio value="urgent">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-select v-model="formData.assigneeId" placeholder="请选择负责人" style="width: 100%">
            <el-option 
              v-for="emp in employees" 
              :key="emp.id" 
              :label="emp.name" 
              :value="emp.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="formData.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入任务描述" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">
            {{ getStatusName(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务标题" :span="2">{{ currentTask.title }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">
          {{ getTypeName(currentTask.type) }}
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentTask.priority)">
            {{ getPriorityName(currentTask.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentTask.assigneeName }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentTask.deadline }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentTask.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">
          {{ currentTask.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'

interface Task {
  id: number
  title: string
  type: string
  priority: string
  status: string
  assigneeId: number
  assigneeName: string
  deadline: string
  description: string
  createdAt: string
  updatedAt: string
}

interface Employee {
  id: number
  name: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新建任务')
const formRef = ref<FormInstance>()

const searchForm = reactive({
  type: '',
  status: '',
  assigneeId: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statistics = reactive({
  pending: 12,
  inProgress: 8,
  completed: 156,
  completionRate: 88.6
})

const formData = reactive({
  id: null as number | null,
  title: '',
  type: '',
  priority: 'medium',
  assigneeId: null as number | null,
  deadline: '',
  description: ''
})

const formRules: FormRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const currentTask = ref<Task>({} as Task)

// 模拟数据
const employees = ref<Employee[]>([
  { id: 1, name: '张三' },
  { id: 2, name: '李四' },
  { id: 3, name: '王五' },
  { id: 4, name: '赵六' }
])

const taskList = ref<Task[]>([
  { id: 1, title: '中山路摄像头巡检', type: 'inspection', priority: 'medium', status: 'completed', assigneeId: 1, assigneeName: '张三', deadline: '2024-01-20 18:00', description: '对中山路沿线10个摄像头进行例行巡检', createdAt: '2024-01-15 09:00', updatedAt: '2024-01-16 14:30' },
  { id: 2, title: '人民大道信号灯维修', type: 'repair', priority: 'high', status: 'in_progress', assigneeId: 2, assigneeName: '李四', deadline: '2024-01-18 12:00', description: '人民大道与解放路交叉口信号灯故障维修', createdAt: '2024-01-15 10:30', updatedAt: '2024-01-15 10:30' },
  { id: 3, title: '处理违停事件#2845', type: 'anomaly', priority: 'urgent', status: 'pending', assigneeId: 3, assigneeName: '王五', deadline: '2024-01-16 16:00', description: '建设路口发现车辆违停，需要现场处理', createdAt: '2024-01-15 14:20', updatedAt: '2024-01-15 14:20' },
  { id: 4, title: '调度巡逻车至胜利街', type: 'dispatch', priority: 'medium', status: 'pending', assigneeId: 4, assigneeName: '赵六', deadline: '2024-01-16 15:00', description: '根据交通拥堵预警，调度巡逻车前往胜利街疏导', createdAt: '2024-01-15 13:45', updatedAt: '2024-01-15 13:45' },
  { id: 5, title: '和平路传感器检查', type: 'inspection', priority: 'low', status: 'completed', assigneeId: 1, assigneeName: '张三', deadline: '2024-01-19 18:00', description: '检查和平路地埋式传感器工作状态', createdAt: '2024-01-14 09:00', updatedAt: '2024-01-15 11:20' }
])

const getTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    inspection: '巡检',
    repair: '维修',
    anomaly: '异常处理',
    dispatch: '调度'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    inspection: 'info',
    repair: 'warning',
    anomaly: 'danger',
    dispatch: 'primary'
  }
  return typeMap[type] || 'info'
}

const getPriorityName = (priority: string) => {
  const map: Record<string, string> = {
    low: '低',
    medium: '中',
    high: '高',
    urgent: '紧急'
  }
  return map[priority] || priority
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    low: 'info',
    medium: '',
    high: 'warning',
    urgent: 'danger'
  }
  return map[priority] || 'info'
}

const getStatusName = (status: string) => {
  const map: Record<string, string> = {
    pending: '待处理',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    in_progress: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.type = ''
  searchForm.status = ''
  searchForm.assigneeId = null
  handleSearch()
}

const loadData = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    pagination.total = taskList.value.length
    loading.value = false
  }, 300)
}

const handleAdd = () => {
  dialogTitle.value = '新建任务'
  Object.assign(formData, {
    id: null,
    title: '',
    type: '',
    priority: 'medium',
    assigneeId: null,
    deadline: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: Task) => {
  dialogTitle.value = '编辑任务'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row: Task) => {
  currentTask.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success(formData.id ? '任务更新成功' : '任务创建成功')
      dialogVisible.value = false
      loadData()
    }
  })
}

const handleStart = (row: Task) => {
  ElMessageBox.confirm('确定开始执行此任务？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    row.status = 'in_progress'
    statistics.pending--
    statistics.inProgress++
    ElMessage.success('任务已开始')
  })
}

const handleComplete = (row: Task) => {
  ElMessageBox.confirm('确定完成此任务？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    row.status = 'completed'
    statistics.inProgress--
    statistics.completed++
    ElMessage.success('任务已完成')
  })
}

const handleDelete = (row: Task) => {
  ElMessageBox.confirm('确定删除此任务？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = taskList.value.findIndex(t => t.id === row.id)
    if (index > -1) {
      taskList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.task-management {
  .search-card {
    margin-bottom: 16px;
  }

  .stat-row {
    margin-bottom: 16px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        font-size: 40px;
        
        &.pending { color: #e6a23c; }
        &.progress { color: #409eff; }
        &.completed { color: #67c23a; }
        &.rate { color: #909399; }
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .table-card {
    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
