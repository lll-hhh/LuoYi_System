<template>
  <div class="search-form">
    <el-form :model="form" inline>
      <slot></slot>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <slot name="extra"></slot>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  form: Record<string, any>
}>()

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()

const handleSearch = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}
</script>

<style scoped lang="scss">
.search-form {
  background: #fff;
  padding: 20px 24px 4px;
  margin-bottom: 16px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
