<template>
  <div class="anomaly-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="车牌号">
          <el-input v-model="searchForm.plateNumber" placeholder="请输入车牌号" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="searchForm.anomalyType" placeholder="请选择" clearable>
            <el-option label="超速" value="SPEEDING" />
            <el-option label="违停" value="ILLEGAL_PARKING" />
            <el-option label="闯红灯" value="RED_LIGHT" />
            <el-option label="逆行" value="WRONG_WAY" />
            <el-option label="黑名单车辆" value="BLACKLIST" />
            <el-option label="套牌车辆" value="FAKE_PLATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="PROCESSED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
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
          <span>异常记录列表</span>
          <div>
            <el-button type="warning" @click="handleBatchProcess">批量处理</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="anomalyId" label="ID" width="80" />
        <el-table-column prop="plateNumber" label="车牌号" width="110">
          <template #default="{ row }">
            <span class="plate-number" :class="plateColorClass(row.plateColor)">
              {{ row.plateNumber }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="anomalyType" label="异常类型" width="100">
          <template #default="{ row }">
            <el-tag :type="anomalyTagType(row.anomalyType)">
              {{ anomalyTypeMap[row.anomalyType] || row.anomalyType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="cameraName" label="检测摄像头" width="150" />
        <el-table-column prop="detectedAt" label="检测时间" width="160" />
        <el-table-column prop="confidence" label="置信度" width="90">
          <template #default="{ row }">
            {{ (row.confidence * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button link type="success" @click="handleProcess(row)" v-if="row.status === 'PENDING'">处理</el-button>
            <el-button link type="warning" @click="handleIgnore(row)" v-if="row.status === 'PENDING'">忽略</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="异常详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车牌号">
          <span class="plate-number" :class="plateColorClass(currentAnomaly.plateColor)">
            {{ currentAnomaly.plateNumber }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="异常类型">
          <el-tag :type="anomalyTagType(currentAnomaly.anomalyType)">
            {{ anomalyTypeMap[currentAnomaly.anomalyType] || currentAnomaly.anomalyType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测时间">{{ currentAnomaly.detectedAt }}</el-descriptions-item>
        <el-descriptions-item label="检测摄像头">{{ currentAnomaly.cameraName }}</el-descriptions-item>
        <el-descriptions-item label="置信度">{{ (currentAnomaly.confidence * 100).toFixed(1) }}%</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentAnomaly.status)">
            {{ statusMap[currentAnomaly.status] || currentAnomaly.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentAnomaly.description }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>抓拍图片</el-divider>
      <div class="capture-images">
        <div class="image-item" v-for="i in 3" :key="i">
          <div class="image-placeholder">
            <el-icon :size="48"><Picture /></el-icon>
            <span>抓拍图片 {{ i }}</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleProcess(currentAnomaly)" v-if="currentAnomaly.status === 'PENDING'">处理</el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog
      v-model="processVisible"
      title="处理异常"
      width="500px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理方式">
          <el-select v-model="processForm.processType" placeholder="请选择">
            <el-option label="生成罚单" value="FINE" />
            <el-option label="警告提醒" value="WARNING" />
            <el-option label="转交交警" value="TRANSFER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const loading = ref(false)
const detailVisible = ref(false)
const processVisible = ref(false)
const selectedRows = ref<any[]>([])

const searchForm = reactive({
  plateNumber: '',
  anomalyType: '',
  status: '',
  dateRange: null as [string, string] | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 50
})

const currentAnomaly = reactive({
  anomalyId: 0,
  plateNumber: '',
  plateColor: 'blue',
  anomalyType: '',
  description: '',
  cameraName: '',
  detectedAt: '',
  confidence: 0,
  status: ''
})

const processForm = reactive({
  processType: '',
  remark: ''
})

const tableData = ref([
  { anomalyId: 1, plateNumber: '京A12345', plateColor: 'blue', anomalyType: 'SPEEDING', description: '超速行驶，实测速度85km/h，限速60km/h', cameraName: '中山路测速点', detectedAt: '2024-01-15 10:30:25', confidence: 0.95, status: 'PENDING' },
  { anomalyId: 2, plateNumber: '京B88888', plateColor: 'blue', anomalyType: 'RED_LIGHT', description: '闯红灯', cameraName: '人民路口电警', detectedAt: '2024-01-15 10:25:18', confidence: 0.92, status: 'PROCESSED' },
  { anomalyId: 3, plateNumber: '京AD8888', plateColor: 'yellow', anomalyType: 'ILLEGAL_PARKING', description: '违章停车', cameraName: '建设路监控', detectedAt: '2024-01-15 10:20:12', confidence: 0.88, status: 'PENDING' },
  { anomalyId: 4, plateNumber: '京F66666', plateColor: 'green', anomalyType: 'BLACKLIST', description: '黑名单车辆', cameraName: '火车站卡口', detectedAt: '2024-01-15 10:15:05', confidence: 0.99, status: 'PENDING' },
  { anomalyId: 5, plateNumber: '京C55555', plateColor: 'blue', anomalyType: 'FAKE_PLATE', description: '疑似套牌车辆', cameraName: '机场路卡口', detectedAt: '2024-01-15 10:10:58', confidence: 0.75, status: 'IGNORED' }
])

const anomalyTypeMap: Record<string, string> = {
  SPEEDING: '超速',
  ILLEGAL_PARKING: '违停',
  RED_LIGHT: '闯红灯',
  WRONG_WAY: '逆行',
  BLACKLIST: '黑名单',
  FAKE_PLATE: '套牌'
}

const statusMap: Record<string, string> = {
  PENDING: '待处理',
  PROCESSED: '已处理',
  IGNORED: '已忽略'
}

const anomalyTagType = (type: string) => {
  const map: Record<string, string> = {
    SPEEDING: 'warning',
    ILLEGAL_PARKING: 'info',
    RED_LIGHT: 'danger',
    WRONG_WAY: 'danger',
    BLACKLIST: 'danger',
    FAKE_PLATE: 'warning'
  }
  return map[type] || 'info'
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    PROCESSED: 'success',
    IGNORED: 'info'
  }
  return map[status] || 'info'
}

const plateColorClass = (color: string) => {
  return `plate-${color}`
}

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  searchForm.plateNumber = ''
  searchForm.anomalyType = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const handleBatchProcess = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要处理的记录')
    return
  }
  ElMessageBox.confirm(`确定要批量处理选中的 ${selectedRows.value.length} 条记录吗?`, '提示', { type: 'warning' })
    .then(() => {
      ElMessage.success('批量处理成功')
    })
    .catch(() => {})
}

const handleExport = () => {
  ElMessage.success('正在导出...')
}

const handleView = (row: any) => {
  Object.assign(currentAnomaly, row)
  detailVisible.value = true
}

const handleProcess = (row: any) => {
  Object.assign(currentAnomaly, row)
  processForm.processType = ''
  processForm.remark = ''
  processVisible.value = true
}

const handleIgnore = (row: any) => {
  ElMessageBox.confirm('确定要忽略该异常吗?', '提示', { type: 'warning' })
    .then(() => {
      row.status = 'IGNORED'
      ElMessage.success('已忽略')
    })
    .catch(() => {})
}

const submitProcess = () => {
  if (!processForm.processType) {
    ElMessage.warning('请选择处理方式')
    return
  }
  ElMessage.success('处理成功')
  processVisible.value = false
  detailVisible.value = false
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
.anomaly-management {
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
  
  .plate-number {
    padding: 2px 6px;
    border-radius: 4px;
    font-weight: 600;
    font-size: 13px;
    
    &.plate-blue {
      background: #409EFF;
      color: #fff;
    }
    
    &.plate-yellow {
      background: #E6A23C;
      color: #fff;
    }
    
    &.plate-green {
      background: linear-gradient(to right, #67C23A, #409EFF);
      color: #fff;
    }
  }
  
  .capture-images {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    
    .image-item {
      .image-placeholder {
        height: 150px;
        background: #f5f7fa;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #909399;
        
        span {
          margin-top: 8px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
