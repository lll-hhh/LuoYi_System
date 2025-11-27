<template>
  <div class="road-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="道路名称">
          <el-input v-model="searchForm.roadName" placeholder="请输入道路名称" clearable />
        </el-form-item>
        <el-form-item label="道路等级">
          <el-select v-model="searchForm.roadLevel" placeholder="请选择" clearable>
            <el-option label="主干道" value="MAIN" />
            <el-option label="次干道" value="SECONDARY" />
            <el-option label="支路" value="BRANCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="正常" value="ACTIVE" />
            <el-option label="施工中" value="CONSTRUCTION" />
            <el-option label="关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>道路列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增道路
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="roadCode" label="道路编号" width="120" />
        <el-table-column prop="roadName" label="道路名称" min-width="150" />
        <el-table-column prop="roadLevel" label="道路等级" width="100">
          <template #default="{ row }">
            {{ roadLevelMap[row.roadLevel] || row.roadLevel }}
          </template>
        </el-table-column>
        <el-table-column prop="laneCount" label="车道数" width="80" />
        <el-table-column prop="speedLimit" label="限速(km/h)" width="100" />
        <el-table-column prop="length" label="长度(km)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
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
        <el-form-item label="道路编号" prop="roadCode">
          <el-input v-model="formData.roadCode" placeholder="请输入道路编号" />
        </el-form-item>
        <el-form-item label="道路名称" prop="roadName">
          <el-input v-model="formData.roadName" placeholder="请输入道路名称" />
        </el-form-item>
        <el-form-item label="道路等级" prop="roadLevel">
          <el-select v-model="formData.roadLevel" placeholder="请选择">
            <el-option label="主干道" value="MAIN" />
            <el-option label="次干道" value="SECONDARY" />
            <el-option label="支路" value="BRANCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="车道数" prop="laneCount">
          <el-input-number v-model="formData.laneCount" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="限速" prop="speedLimit">
          <el-input-number v-model="formData.speedLimit" :min="20" :max="120" />
          <span style="margin-left: 8px">km/h</span>
        </el-form-item>
        <el-form-item label="长度" prop="length">
          <el-input-number v-model="formData.length" :min="0" :precision="2" />
          <span style="margin-left: 8px">km</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="施工中" value="CONSTRUCTION" />
            <el-option label="关闭" value="CLOSED" />
          </el-select>
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
  roadName: '',
  roadLevel: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { roadId: 1, roadCode: 'RD001', roadName: '中山路', roadLevel: 'MAIN', laneCount: 6, speedLimit: 60, length: 5.2, status: 'ACTIVE' },
  { roadId: 2, roadCode: 'RD002', roadName: '人民大道', roadLevel: 'MAIN', laneCount: 8, speedLimit: 80, length: 8.5, status: 'ACTIVE' },
  { roadId: 3, roadCode: 'RD003', roadName: '建设路', roadLevel: 'SECONDARY', laneCount: 4, speedLimit: 50, length: 3.2, status: 'CONSTRUCTION' }
])

const formData = reactive({
  roadId: null as number | null,
  roadCode: '',
  roadName: '',
  roadLevel: '',
  laneCount: 2,
  speedLimit: 60,
  length: 0,
  status: 'ACTIVE'
})

const formRules = {
  roadCode: [{ required: true, message: '请输入道路编号', trigger: 'blur' }],
  roadName: [{ required: true, message: '请输入道路名称', trigger: 'blur' }],
  roadLevel: [{ required: true, message: '请选择道路等级', trigger: 'change' }]
}

const roadLevelMap: Record<string, string> = {
  MAIN: '主干道',
  SECONDARY: '次干道',
  BRANCH: '支路'
}

const statusMap: Record<string, string> = {
  ACTIVE: '正常',
  CONSTRUCTION: '施工中',
  CLOSED: '关闭'
}

const dialogTitle = computed(() => isEdit.value ? '编辑道路' : '新增道路')

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    CONSTRUCTION: 'warning',
    CLOSED: 'danger'
  }
  return map[status] || 'info'
}

const handleSearch = () => {
  pagination.page = 1
  // TODO: 调用API
}

const handleReset = () => {
  searchForm.roadName = ''
  searchForm.roadLevel = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    roadId: null,
    roadCode: '',
    roadName: '',
    roadLevel: '',
    laneCount: 2,
    speedLimit: 60,
    length: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row: any) => {
  ElMessage.info('查看详情: ' + row.roadName)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该道路吗?', '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
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
.road-management {
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
