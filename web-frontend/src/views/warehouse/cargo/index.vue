<template>
  <div class="cargo-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="货物名称">
          <el-input v-model="searchForm.name" placeholder="请输入货物名称" clearable />
        </el-form-item>
        <el-form-item label="货物类别">
          <el-select v-model="searchForm.category" placeholder="请选择类别" clearable>
            <el-option label="普通货物" value="normal" />
            <el-option label="危险品" value="dangerous" />
            <el-option label="冷链货物" value="cold" />
            <el-option label="贵重物品" value="valuable" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable>
            <el-option
              v-for="item in warehouseList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>货物列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加货物
            </el-button>
            <el-button type="success" @click="handleInbound">
              <el-icon><Download /></el-icon>
              入库
            </el-button>
            <el-button type="warning" @click="handleOutbound">
              <el-icon><Upload /></el-icon>
              出库
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="code" label="货物编码" width="120" />
        <el-table-column prop="name" label="货物名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">
              {{ getCategoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="所属仓库" width="120" />
        <el-table-column prop="areaName" label="库区" width="100" />
        <el-table-column prop="locationName" label="库位" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="weight" label="重量(kg)" width="100" align="right" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' ? 'success' : 'warning'">
              {{ row.status === 'normal' ? '正常' : '预警' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="入库时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加货物' : '编辑货物'"
      width="600px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="货物名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入货物名称" />
        </el-form-item>
        <el-form-item label="货物编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入货物编码" />
        </el-form-item>
        <el-form-item label="货物类别" prop="category">
          <el-select v-model="formData.category" placeholder="请选择类别">
            <el-option label="普通货物" value="normal" />
            <el-option label="危险品" value="dangerous" />
            <el-option label="冷链货物" value="cold" />
            <el-option label="贵重物品" value="valuable" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select v-model="formData.warehouseId" placeholder="请选择仓库">
            <el-option
              v-for="item in warehouseList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="formData.quantity" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="formData.unit" placeholder="如: 件、箱、吨" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="重量(kg)" prop="weight">
          <el-input-number v-model="formData.weight" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download, Upload } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  name: '',
  category: '',
  warehouseId: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([
  {
    id: 1,
    code: 'CARGO001',
    name: '电子元器件',
    category: 'normal',
    warehouseId: 1,
    warehouseName: '主仓库',
    areaName: 'A区',
    locationName: 'A-01-01',
    quantity: 1000,
    unit: '件',
    weight: 50,
    status: 'normal',
    createdAt: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    code: 'CARGO002',
    name: '化工原料',
    category: 'dangerous',
    warehouseId: 2,
    warehouseName: '危险品仓库',
    areaName: 'B区',
    locationName: 'B-02-03',
    quantity: 200,
    unit: '桶',
    weight: 2000,
    status: 'warning',
    createdAt: '2024-01-14 14:20:00'
  }
])

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')

// 仓库列表
const warehouseList = ref([
  { id: 1, name: '主仓库' },
  { id: 2, name: '危险品仓库' },
  { id: 3, name: '冷链仓库' }
])

// 表单数据
const formData = reactive({
  name: '',
  code: '',
  category: '',
  warehouseId: '',
  quantity: 0,
  unit: '',
  weight: 0,
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入货物名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入货物编码', trigger: 'blur' }],
  category: [{ required: true, message: '请选择货物类别', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择所属仓库', trigger: 'change' }]
}

const formRef = ref()

// 获取类别标签
const getCategoryLabel = (category: string) => {
  const map: Record<string, string> = {
    normal: '普通货物',
    dangerous: '危险品',
    cold: '冷链货物',
    valuable: '贵重物品'
  }
  return map[category] || category
}

// 获取类别标签类型
const getCategoryType = (category: string) => {
  const map: Record<string, string> = {
    normal: 'info',
    dangerous: 'danger',
    cold: 'primary',
    valuable: 'warning'
  }
  return map[category] || 'info'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  // fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.warehouseId = ''
  handleSearch()
}

// 添加
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(formData, {
    name: '',
    code: '',
    category: '',
    warehouseId: '',
    quantity: 0,
    unit: '',
    weight: 0,
    remark: ''
  })
  dialogVisible.value = true
}

// 入库
const handleInbound = () => {
  ElMessage.info('入库功能开发中...')
}

// 出库
const handleOutbound = () => {
  ElMessage.info('出库功能开发中...')
}

// 查看详情
const handleView = (row: any) => {
  ElMessage.info(`查看货物: ${row.name}`)
}

// 编辑
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该货物吗?', '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

// 提交表单
const handleSubmit = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '编辑成功')
      dialogVisible.value = false
    }
  })
}

// 分页
const handleSizeChange = (val: number) => {
  pagination.size = val
  // fetchData()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  // fetchData()
}

onMounted(() => {
  // fetchData()
})
</script>

<style scoped lang="scss">
.cargo-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
