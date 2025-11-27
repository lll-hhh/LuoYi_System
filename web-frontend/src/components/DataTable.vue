<template>
  <el-card class="data-table" :body-style="{ padding: 0 }">
    <template #header v-if="title || $slots.header">
      <div class="table-header">
        <span v-if="title" class="table-title">{{ title }}</span>
        <slot name="header"></slot>
      </div>
    </template>
    
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="data"
      :border="border"
      :stripe="stripe"
      :row-key="rowKey"
      :height="height"
      :max-height="maxHeight"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column v-if="selection" type="selection" width="50" align="center" />
      <el-table-column v-if="index" type="index" label="序号" width="60" align="center" />
      <slot></slot>
    </el-table>
    
    <div v-if="pagination" class="table-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        :layout="paginationLayout"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = withDefaults(defineProps<{
  data: any[]
  loading?: boolean
  title?: string
  border?: boolean
  stripe?: boolean
  rowKey?: string
  height?: string | number
  maxHeight?: string | number
  selection?: boolean
  index?: boolean
  pagination?: boolean
  total?: number
  page?: number
  limit?: number
  pageSizes?: number[]
  paginationLayout?: string
}>(), {
  loading: false,
  border: true,
  stripe: true,
  rowKey: 'id',
  selection: false,
  index: false,
  pagination: true,
  total: 0,
  page: 1,
  limit: 10,
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper'
})

const emit = defineEmits<{
  (e: 'update:page', val: number): void
  (e: 'update:limit', val: number): void
  (e: 'selection-change', val: any[]): void
  (e: 'sort-change', val: { prop: string; order: string }): void
  (e: 'pagination', val: { page: number; limit: number }): void
}>()

const tableRef = ref()

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val)
})

const pageSize = computed({
  get: () => props.limit,
  set: (val) => emit('update:limit', val)
})

const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  emit('sort-change', { prop, order })
}

const handleSizeChange = (val: number) => {
  emit('pagination', { page: 1, limit: val })
}

const handleCurrentChange = (val: number) => {
  emit('pagination', { page: val, limit: props.limit })
}

// 暴露表格方法
defineExpose({
  clearSelection: () => tableRef.value?.clearSelection(),
  toggleRowSelection: (row: any, selected?: boolean) => tableRef.value?.toggleRowSelection(row, selected),
  toggleAllSelection: () => tableRef.value?.toggleAllSelection(),
  getSelectionRows: () => tableRef.value?.getSelectionRows()
})
</script>

<style scoped lang="scss">
.data-table {
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .table-title {
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .table-pagination {
    display: flex;
    justify-content: flex-end;
    padding: 16px;
    background: #fff;
  }
}
</style>
