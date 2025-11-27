<template>
  <div class="page-header">
    <div class="header-content">
      <div class="header-left">
        <h2 class="title">{{ title }}</h2>
        <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-right">
        <slot name="actions"></slot>
      </div>
    </div>
    <div v-if="$slots.tabs" class="header-tabs">
      <slot name="tabs"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<style scoped lang="scss">
.page-header {
  background: #fff;
  padding: 20px 24px;
  margin-bottom: 16px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .header-left {
    .title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
    
    .subtitle {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }
  }
  
  .header-tabs {
    margin-top: 16px;
    border-top: 1px solid #ebeef5;
    padding-top: 16px;
  }
}
</style>
