<template>
  <div class="department-container">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加部门
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" row-key="id" default-expand-all v-loading="loading">
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="code" label="部门编码" width="120" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleAddChild(row)">添加子部门</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级部门"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="formData.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')

const dialogTitle = computed(() => {
  if (dialogType.value === 'add') return '添加部门'
  if (dialogType.value === 'addChild') return '添加子部门'
  return '编辑部门'
})

const tableData = ref([
  {
    id: 1,
    name: '总公司',
    code: 'HQ',
    leader: '张总',
    phone: '13800000001',
    sort: 1,
    status: 1,
    createdAt: '2024-01-01 00:00:00',
    children: [
      {
        id: 2,
        name: '运营部',
        code: 'OP',
        leader: '李经理',
        phone: '13800000002',
        sort: 1,
        status: 1,
        createdAt: '2024-01-01 00:00:00'
      },
      {
        id: 3,
        name: '技术部',
        code: 'TECH',
        leader: '王经理',
        phone: '13800000003',
        sort: 2,
        status: 1,
        createdAt: '2024-01-01 00:00:00'
      }
    ]
  }
])

const treeData = computed(() => {
  return [{ id: 0, name: '顶级部门', children: tableData.value }]
})

const formData = reactive({
  id: 0,
  parentId: 0,
  name: '',
  code: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

const formRef = ref()

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(formData, {
    id: 0,
    parentId: 0,
    name: '',
    code: '',
    leader: '',
    phone: '',
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleAddChild = (row: any) => {
  dialogType.value = 'addChild'
  Object.assign(formData, {
    id: 0,
    parentId: row.id,
    name: '',
    code: '',
    leader: '',
    phone: '',
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除部门 "${row.name}" 吗?`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

const handleSubmit = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success(dialogType.value === 'edit' ? '编辑成功' : '添加成功')
      dialogVisible.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.department-container {
  padding: 20px;
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
