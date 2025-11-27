<template>
  <div class="camera-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="摄像头名称">
          <el-input v-model="searchForm.cameraName" placeholder="请输入摄像头名称" clearable />
        </el-form-item>
        <el-form-item label="摄像头类型">
          <el-select v-model="searchForm.cameraType" placeholder="请选择" clearable>
            <el-option label="卡口摄像头" value="CHECKPOINT" />
            <el-option label="监控摄像头" value="SURVEILLANCE" />
            <el-option label="电警摄像头" value="ELECTRONIC_POLICE" />
            <el-option label="测速摄像头" value="SPEED" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="故障" value="FAULT" />
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
          <span>摄像头列表</span>
          <div>
            <el-button type="success" @click="handleBatchCheck">批量检测</el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增摄像头
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="cameraCode" label="编号" width="100" />
        <el-table-column prop="cameraName" label="名称" min-width="150" />
        <el-table-column prop="cameraType" label="类型" width="100">
          <template #default="{ row }">
            {{ cameraTypeMap[row.cameraType] || row.cameraType }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="安装位置" min-width="150" />
        <el-table-column prop="roadName" label="所属道路" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
      width="650px"
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
            <el-form-item label="摄像头编号" prop="cameraCode">
              <el-input v-model="formData.cameraCode" placeholder="请输入编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="摄像头名称" prop="cameraName">
              <el-input v-model="formData.cameraName" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="摄像头类型" prop="cameraType">
              <el-select v-model="formData.cameraType" placeholder="请选择">
                <el-option label="卡口摄像头" value="CHECKPOINT" />
                <el-option label="监控摄像头" value="SURVEILLANCE" />
                <el-option label="电警摄像头" value="ELECTRONIC_POLICE" />
                <el-option label="测速摄像头" value="SPEED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属道路" prop="roadId">
              <el-select v-model="formData.roadId" placeholder="请选择">
                <el-option label="中山路" :value="1" />
                <el-option label="人民大道" :value="2" />
                <el-option label="建设路" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="安装位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="formData.longitude" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="formData.latitude" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="RTSP地址" prop="rtspUrl">
          <el-input v-model="formData.rtspUrl" placeholder="rtsp://..." />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="formData.brand" placeholder="海康/大华等" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="formData.model" placeholder="设备型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分辨率">
          <el-select v-model="formData.resolution">
            <el-option label="1080P" value="1920x1080" />
            <el-option label="2K" value="2560x1440" />
            <el-option label="4K" value="3840x2160" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 视频预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="视频预览"
      width="800px"
      destroy-on-close
    >
      <div class="video-preview">
        <div class="video-placeholder">
          <el-icon :size="64"><VideoCamera /></el-icon>
          <p>{{ previewCamera?.cameraName }}</p>
          <p class="rtsp-url">{{ previewCamera?.rtspUrl || '暂无视频流地址' }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const isEdit = ref(false)
const selectedRows = ref<any[]>([])
const previewCamera = ref<any>(null)

const searchForm = reactive({
  cameraName: '',
  cameraType: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { cameraId: 1, cameraCode: 'CAM001', cameraName: '中山路1号卡口', cameraType: 'CHECKPOINT', location: '中山路与人民路交叉口', roadName: '中山路', status: 'ONLINE', lastHeartbeat: '2024-01-15 10:30:00', rtspUrl: 'rtsp://192.168.1.100:554/stream1' },
  { cameraId: 2, cameraCode: 'CAM002', cameraName: '人民大道监控', cameraType: 'SURVEILLANCE', location: '人民大道中段', roadName: '人民大道', status: 'ONLINE', lastHeartbeat: '2024-01-15 10:29:00', rtspUrl: 'rtsp://192.168.1.101:554/stream1' },
  { cameraId: 3, cameraCode: 'CAM003', cameraName: '建设路测速点', cameraType: 'SPEED', location: '建设路高架入口', roadName: '建设路', status: 'OFFLINE', lastHeartbeat: '2024-01-15 08:00:00', rtspUrl: '' }
])

const formData = reactive({
  cameraId: null as number | null,
  cameraCode: '',
  cameraName: '',
  cameraType: '',
  location: '',
  roadId: null as number | null,
  longitude: 0,
  latitude: 0,
  rtspUrl: '',
  brand: '',
  model: '',
  resolution: '1920x1080'
})

const formRules = {
  cameraCode: [{ required: true, message: '请输入摄像头编号', trigger: 'blur' }],
  cameraName: [{ required: true, message: '请输入摄像头名称', trigger: 'blur' }],
  cameraType: [{ required: true, message: '请选择摄像头类型', trigger: 'change' }],
  roadId: [{ required: true, message: '请选择所属道路', trigger: 'change' }]
}

const cameraTypeMap: Record<string, string> = {
  CHECKPOINT: '卡口摄像头',
  SURVEILLANCE: '监控摄像头',
  ELECTRONIC_POLICE: '电警摄像头',
  SPEED: '测速摄像头'
}

const statusMap: Record<string, string> = {
  ONLINE: '在线',
  OFFLINE: '离线',
  FAULT: '故障'
}

const dialogTitle = computed(() => isEdit.value ? '编辑摄像头' : '新增摄像头')

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    ONLINE: 'success',
    OFFLINE: 'info',
    FAULT: 'danger'
  }
  return map[status] || 'info'
}

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  searchForm.cameraName = ''
  searchForm.cameraType = ''
  searchForm.status = ''
  handleSearch()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const handleBatchCheck = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要检测的摄像头')
    return
  }
  ElMessage.success(`正在检测 ${selectedRows.value.length} 个摄像头...`)
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    cameraId: null,
    cameraCode: '',
    cameraName: '',
    cameraType: '',
    location: '',
    roadId: null,
    longitude: 0,
    latitude: 0,
    rtspUrl: '',
    brand: '',
    model: '',
    resolution: '1920x1080'
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handlePreview = (row: any) => {
  previewCamera.value = row
  previewVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该摄像头吗?', '提示', { type: 'warning' })
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
.camera-management {
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
  
  .video-preview {
    .video-placeholder {
      height: 400px;
      background: #000;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      color: #fff;
      
      p {
        margin-top: 16px;
        font-size: 16px;
      }
      
      .rtsp-url {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
