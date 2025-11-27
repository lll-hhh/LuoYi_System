<template>
  <div class="warehouse-management">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="仓库名称">
          <el-input v-model="searchForm.warehouseName" placeholder="请输入仓库名称" clearable />
        </el-form-item>
        <el-form-item label="仓库类型">
          <el-select v-model="searchForm.warehouseType" placeholder="请选择" clearable>
            <el-option label="普通仓库" value="NORMAL" />
            <el-option label="冷链仓库" value="COLD_CHAIN" />
            <el-option label="危险品仓库" value="HAZARDOUS" />
            <el-option label="保税仓库" value="BONDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="正常运营" value="ACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已关闭" value="CLOSED" />
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
          <span>仓库列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增仓库
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="warehouseCode" label="仓库编号" width="120" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="warehouseType" label="类型" width="100">
          <template #default="{ row }">
            {{ warehouseTypeMap[row.warehouseType] || row.warehouseType }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column prop="capacity" label="容量(吨)" width="100" />
        <el-table-column prop="usageRate" label="使用率" width="120">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.usageRate" 
              :color="usageRateColor(row.usageRate)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
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
            <el-form-item label="仓库编号" prop="warehouseCode">
              <el-input v-model="formData.warehouseCode" placeholder="请输入编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库名称" prop="warehouseName">
              <el-input v-model="formData.warehouseName" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="仓库类型" prop="warehouseType">
              <el-select v-model="formData.warehouseType" placeholder="请选择">
                <el-option label="普通仓库" value="NORMAL" />
                <el-option label="冷链仓库" value="COLD_CHAIN" />
                <el-option label="危险品仓库" value="HAZARDOUS" />
                <el-option label="保税仓库" value="BONDED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="formData.status">
                <el-option label="正常运营" value="ACTIVE" />
                <el-option label="维护中" value="MAINTENANCE" />
                <el-option label="已关闭" value="CLOSED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面积" prop="area">
              <el-input-number v-model="formData.area" :min="0" style="width: 100%" />
              <span style="margin-left: 8px">㎡</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number v-model="formData.capacity" :min="0" style="width: 100%" />
              <span style="margin-left: 8px">吨</span>
            </el-form-item>
          </el-col>
        </el-row>
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
        <el-form-item label="联系人">
          <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
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
  warehouseName: '',
  warehouseType: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { warehouseId: 1, warehouseCode: 'WH001', warehouseName: '城东物流中心', warehouseType: 'NORMAL', address: '开发区物流大道100号', area: 50000, capacity: 10000, usageRate: 75, status: 'ACTIVE' },
  { warehouseId: 2, warehouseCode: 'WH002', warehouseName: '冷链仓储中心', warehouseType: 'COLD_CHAIN', address: '高新区冷链路88号', area: 20000, capacity: 5000, usageRate: 60, status: 'ACTIVE' },
  { warehouseId: 3, warehouseCode: 'WH003', warehouseName: '保税区仓库', warehouseType: 'BONDED', address: '保税区海关大道1号', area: 30000, capacity: 8000, usageRate: 45, status: 'ACTIVE' },
  { warehouseId: 4, warehouseCode: 'WH004', warehouseName: '危险品存储库', warehouseType: 'HAZARDOUS', address: '工业区化工路200号', area: 10000, capacity: 2000, usageRate: 30, status: 'MAINTENANCE' }
])

const formData = reactive({
  warehouseId: null as number | null,
  warehouseCode: '',
  warehouseName: '',
  warehouseType: '',
  address: '',
  area: 0,
  capacity: 0,
  longitude: 0,
  latitude: 0,
  contactPerson: '',
  contactPhone: '',
  status: 'ACTIVE'
})

const formRules = {
  warehouseCode: [{ required: true, message: '请输入仓库编号', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  warehouseType: [{ required: true, message: '请选择仓库类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const warehouseTypeMap: Record<string, string> = {
  NORMAL: '普通仓库',
  COLD_CHAIN: '冷链仓库',
  HAZARDOUS: '危险品仓库',
  BONDED: '保税仓库'
}

const statusMap: Record<string, string> = {
  ACTIVE: '正常运营',
  MAINTENANCE: '维护中',
  CLOSED: '已关闭'
}

const dialogTitle = computed(() => isEdit.value ? '编辑仓库' : '新增仓库')

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    MAINTENANCE: 'warning',
    CLOSED: 'danger'
  }
  return map[status] || 'info'
}

const usageRateColor = (rate: number) => {
  if (rate >= 80) return '#F56C6C'
  if (rate >= 60) return '#E6A23C'
  return '#67C23A'
}

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  searchForm.warehouseName = ''
  searchForm.warehouseType = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    warehouseId: null,
    warehouseCode: '',
    warehouseName: '',
    warehouseType: '',
    address: '',
    area: 0,
    capacity: 0,
    longitude: 0,
    latitude: 0,
    contactPerson: '',
    contactPhone: '',
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
  ElMessage.info('查看详情: ' + row.warehouseName)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该仓库吗?', '提示', { type: 'warning' })
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
.warehouse-management {
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
