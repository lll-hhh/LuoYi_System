<template>
  <div class="parking-management">
    <!-- 顶部统计 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon :size="28"><Location /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalSpaces }}</div>
              <div class="stat-label">总车位</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon available">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.availableSpaces }}</div>
              <div class="stat-label">空闲车位</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon occupied">
              <el-icon :size="28"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.occupiedSpaces }}</div>
              <div class="stat-label">已占用</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate">
              <el-icon :size="28"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.usageRate }}%</div>
              <div class="stat-label">使用率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="停车场名称">
          <el-input v-model="searchForm.parkingName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.parkingType" placeholder="请选择" clearable>
            <el-option label="货车专用" value="TRUCK" />
            <el-option label="综合停车" value="MIXED" />
            <el-option label="危险品专用" value="HAZARDOUS" />
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
          <span>停车场列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增停车场
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="parkingCode" label="编号" width="100" />
        <el-table-column prop="parkingName" label="停车场名称" min-width="150" />
        <el-table-column prop="parkingType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ parkingTypeMap[row.parkingType] || row.parkingType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalSpaces" label="总车位" width="80" />
        <el-table-column prop="availableSpaces" label="空闲" width="80">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.availableSpaces < 10 }">{{ row.availableSpaces }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用率" width="120">
          <template #default="{ row }">
            <el-progress 
              :percentage="Math.round((row.totalSpaces - row.availableSpaces) / row.totalSpaces * 100)" 
              :color="usageRateColor(Math.round((row.totalSpaces - row.availableSpaces) / row.totalSpaces * 100))"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
        <el-table-column prop="hourlyRate" label="收费标准" width="100">
          <template #default="{ row }">
            ¥{{ row.hourlyRate }}/小时
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'OPEN' ? 'success' : 'danger'">
              {{ row.status === 'OPEN' ? '营业中' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleMonitor(row)">监控</el-button>
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
        <el-form-item label="停车场编号" prop="parkingCode">
          <el-input v-model="formData.parkingCode" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="停车场名称" prop="parkingName">
          <el-input v-model="formData.parkingName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="类型" prop="parkingType">
          <el-select v-model="formData.parkingType" placeholder="请选择">
            <el-option label="货车专用" value="TRUCK" />
            <el-option label="综合停车" value="MIXED" />
            <el-option label="危险品专用" value="HAZARDOUS" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总车位" prop="totalSpaces">
              <el-input-number v-model="formData.totalSpaces" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收费标准" prop="hourlyRate">
              <el-input-number v-model="formData.hourlyRate" :min="0" :precision="1" style="width: 100%" />
              <span style="margin-left: 8px">元/小时</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="运营时间">
          <el-time-picker
            v-model="formData.operatingHours"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
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
import { Location, CircleCheck, Van, DataLine } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const stats = reactive({
  totalSpaces: 2500,
  availableSpaces: 856,
  occupiedSpaces: 1644,
  usageRate: 65.8
})

const searchForm = reactive({
  parkingName: '',
  parkingType: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { parkingId: 1, parkingCode: 'PK001', parkingName: '城东货运停车场', parkingType: 'TRUCK', address: '物流大道200号', totalSpaces: 500, availableSpaces: 120, hourlyRate: 5, status: 'OPEN' },
  { parkingId: 2, parkingCode: 'PK002', parkingName: '高速服务区停车场', parkingType: 'MIXED', address: 'G15高速城东服务区', totalSpaces: 800, availableSpaces: 350, hourlyRate: 3, status: 'OPEN' },
  { parkingId: 3, parkingCode: 'PK003', parkingName: '危险品专用停车场', parkingType: 'HAZARDOUS', address: '化工园区入口', totalSpaces: 200, availableSpaces: 86, hourlyRate: 10, status: 'OPEN' },
  { parkingId: 4, parkingCode: 'PK004', parkingName: '保税区货运停车场', parkingType: 'TRUCK', address: '保税区物流中心', totalSpaces: 1000, availableSpaces: 300, hourlyRate: 4, status: 'OPEN' }
])

const formData = reactive({
  parkingId: null as number | null,
  parkingCode: '',
  parkingName: '',
  parkingType: '',
  address: '',
  totalSpaces: 0,
  hourlyRate: 0,
  operatingHours: null as [Date, Date] | null
})

const formRules = {
  parkingCode: [{ required: true, message: '请输入停车场编号', trigger: 'blur' }],
  parkingName: [{ required: true, message: '请输入停车场名称', trigger: 'blur' }],
  parkingType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const parkingTypeMap: Record<string, string> = {
  TRUCK: '货车专用',
  MIXED: '综合停车',
  HAZARDOUS: '危险品专用'
}

const dialogTitle = computed(() => isEdit.value ? '编辑停车场' : '新增停车场')

const usageRateColor = (rate: number) => {
  if (rate >= 80) return '#F56C6C'
  if (rate >= 60) return '#E6A23C'
  return '#67C23A'
}

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  searchForm.parkingName = ''
  searchForm.parkingType = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    parkingId: null,
    parkingCode: '',
    parkingName: '',
    parkingType: '',
    address: '',
    totalSpaces: 0,
    hourlyRate: 0,
    operatingHours: null
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleMonitor = (row: any) => {
  ElMessage.info('查看停车场监控: ' + row.parkingName)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该停车场吗?', '提示', { type: 'warning' })
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
.parking-management {
  .stat-row {
    margin-bottom: 16px;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
    }
    
    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      &.total {
        background: rgba(64, 158, 255, 0.1);
        color: #409EFF;
      }
      
      &.available {
        background: rgba(103, 194, 58, 0.1);
        color: #67C23A;
      }
      
      &.occupied {
        background: rgba(230, 162, 60, 0.1);
        color: #E6A23C;
      }
      
      &.rate {
        background: rgba(144, 147, 153, 0.1);
        color: #909399;
      }
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
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
  
  .text-danger {
    color: #F56C6C;
    font-weight: 600;
  }
}
</style>
