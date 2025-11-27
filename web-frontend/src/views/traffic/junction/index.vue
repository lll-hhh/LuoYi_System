<template>
  <div class="junction-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="路口名称">
          <el-input v-model="searchForm.junctionName" placeholder="请输入路口名称" clearable />
        </el-form-item>
        <el-form-item label="路口类型">
          <el-select v-model="searchForm.junctionType" placeholder="请选择" clearable>
            <el-option label="十字路口" value="CROSS" />
            <el-option label="T字路口" value="T_JUNCTION" />
            <el-option label="环岛" value="ROUNDABOUT" />
            <el-option label="立交" value="INTERCHANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有信号灯">
          <el-select v-model="searchForm.hasSignal" placeholder="请选择" clearable>
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
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
          <span>路口列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增路口
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="junctionCode" label="路口编号" width="120" />
        <el-table-column prop="junctionName" label="路口名称" min-width="150" />
        <el-table-column prop="junctionType" label="路口类型" width="100">
          <template #default="{ row }">
            {{ junctionTypeMap[row.junctionType] || row.junctionType }}
          </template>
        </el-table-column>
        <el-table-column prop="connectedRoads" label="连接道路数" width="100" />
        <el-table-column prop="hasSignal" label="信号灯" width="80">
          <template #default="{ row }">
            <el-tag :type="row.hasSignal ? 'success' : 'info'">
              {{ row.hasSignal ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signalCycleTime" label="信号周期(秒)" width="110" />
        <el-table-column prop="congestionLevel" label="拥堵等级" width="100">
          <template #default="{ row }">
            <el-tag :type="congestionTagType(row.congestionLevel)">
              {{ congestionMap[row.congestionLevel] || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleSignal(row)">信号配时</el-button>
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
        <el-form-item label="路口编号" prop="junctionCode">
          <el-input v-model="formData.junctionCode" placeholder="请输入路口编号" />
        </el-form-item>
        <el-form-item label="路口名称" prop="junctionName">
          <el-input v-model="formData.junctionName" placeholder="请输入路口名称" />
        </el-form-item>
        <el-form-item label="路口类型" prop="junctionType">
          <el-select v-model="formData.junctionType" placeholder="请选择">
            <el-option label="十字路口" value="CROSS" />
            <el-option label="T字路口" value="T_JUNCTION" />
            <el-option label="环岛" value="ROUNDABOUT" />
            <el-option label="立交" value="INTERCHANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接道路">
          <el-select v-model="formData.connectedRoadIds" multiple placeholder="请选择连接的道路">
            <el-option label="中山路" :value="1" />
            <el-option label="人民大道" :value="2" />
            <el-option label="建设路" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有信号灯">
          <el-switch v-model="formData.hasSignal" />
        </el-form-item>
        <el-form-item label="信号周期" v-if="formData.hasSignal">
          <el-input-number v-model="formData.signalCycleTime" :min="30" :max="300" />
          <span style="margin-left: 8px">秒</span>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 信号配时对话框 -->
    <el-dialog
      v-model="signalDialogVisible"
      title="信号配时设置"
      width="700px"
    >
      <el-form :model="signalForm" label-width="100px">
        <el-form-item label="路口名称">
          <el-input v-model="signalForm.junctionName" disabled />
        </el-form-item>
        <el-form-item label="信号周期">
          <el-input-number v-model="signalForm.cycleTime" :min="30" :max="300" />
          <span style="margin-left: 8px">秒</span>
        </el-form-item>
        <el-divider>相位配置</el-divider>
        <el-table :data="signalForm.phases">
          <el-table-column prop="phaseName" label="相位名称" width="120" />
          <el-table-column prop="direction" label="方向" width="100" />
          <el-table-column label="绿灯时长(秒)" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.greenTime" :min="10" :max="120" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="黄灯时长(秒)" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.yellowTime" :min="3" :max="5" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="全红时长(秒)" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.allRedTime" :min="2" :max="5" size="small" />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="signalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSignalSubmit">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const signalDialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  junctionName: '',
  junctionType: '',
  hasSignal: null as boolean | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { junctionId: 1, junctionCode: 'JC001', junctionName: '中山路-人民大道', junctionType: 'CROSS', connectedRoads: 4, hasSignal: true, signalCycleTime: 120, congestionLevel: 2 },
  { junctionId: 2, junctionCode: 'JC002', junctionName: '建设路-解放路', junctionType: 'T_JUNCTION', connectedRoads: 3, hasSignal: true, signalCycleTime: 90, congestionLevel: 1 },
  { junctionId: 3, junctionCode: 'JC003', junctionName: '市政广场环岛', junctionType: 'ROUNDABOUT', connectedRoads: 5, hasSignal: false, signalCycleTime: null, congestionLevel: 3 }
])

const formData = reactive({
  junctionId: null as number | null,
  junctionCode: '',
  junctionName: '',
  junctionType: '',
  connectedRoadIds: [] as number[],
  hasSignal: false,
  signalCycleTime: 120,
  longitude: 0,
  latitude: 0
})

const signalForm = reactive({
  junctionId: null as number | null,
  junctionName: '',
  cycleTime: 120,
  phases: [
    { phaseId: 1, phaseName: '相位1', direction: '东西直行', greenTime: 40, yellowTime: 3, allRedTime: 2 },
    { phaseId: 2, phaseName: '相位2', direction: '东西左转', greenTime: 20, yellowTime: 3, allRedTime: 2 },
    { phaseId: 3, phaseName: '相位3', direction: '南北直行', greenTime: 35, yellowTime: 3, allRedTime: 2 },
    { phaseId: 4, phaseName: '相位4', direction: '南北左转', greenTime: 15, yellowTime: 3, allRedTime: 2 }
  ]
})

const formRules = {
  junctionCode: [{ required: true, message: '请输入路口编号', trigger: 'blur' }],
  junctionName: [{ required: true, message: '请输入路口名称', trigger: 'blur' }],
  junctionType: [{ required: true, message: '请选择路口类型', trigger: 'change' }]
}

const junctionTypeMap: Record<string, string> = {
  CROSS: '十字路口',
  T_JUNCTION: 'T字路口',
  ROUNDABOUT: '环岛',
  INTERCHANGE: '立交'
}

const congestionMap: Record<number, string> = {
  1: '畅通',
  2: '轻度拥堵',
  3: '中度拥堵',
  4: '严重拥堵'
}

const dialogTitle = computed(() => isEdit.value ? '编辑路口' : '新增路口')

const congestionTagType = (level: number) => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'info',
    3: 'warning',
    4: 'danger'
  }
  return map[level] || 'info'
}

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  searchForm.junctionName = ''
  searchForm.junctionType = ''
  searchForm.hasSignal = null
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    junctionId: null,
    junctionCode: '',
    junctionName: '',
    junctionType: '',
    connectedRoadIds: [],
    hasSignal: false,
    signalCycleTime: 120,
    longitude: 0,
    latitude: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSignal = (row: any) => {
  signalForm.junctionId = row.junctionId
  signalForm.junctionName = row.junctionName
  signalForm.cycleTime = row.signalCycleTime || 120
  signalDialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该路口吗?', '提示', { type: 'warning' })
    .then(() => {
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

const handleSubmit = () => {
  ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
  dialogVisible.value = false
}

const handleSignalSubmit = () => {
  ElMessage.success('信号配时保存成功')
  signalDialogVisible.value = false
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
.junction-management {
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
