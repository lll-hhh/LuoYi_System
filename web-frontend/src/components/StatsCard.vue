<template>
  <el-card class="stats-card" :body-style="{ padding: '20px' }">
    <div class="stats-content">
      <div class="stats-icon" :style="{ backgroundColor: iconBg }">
        <el-icon :size="24" :color="iconColor">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="stats-info">
        <div class="stats-value">
          <span class="value">{{ formattedValue }}</span>
          <span v-if="unit" class="unit">{{ unit }}</span>
        </div>
        <div class="stats-label">{{ label }}</div>
        <div v-if="trend !== undefined" class="stats-trend" :class="trendClass">
          <el-icon>
            <CaretTop v-if="trend > 0" />
            <CaretBottom v-else-if="trend < 0" />
            <Minus v-else />
          </el-icon>
          <span>{{ Math.abs(trend) }}%</span>
          <span class="trend-text">较昨日</span>
        </div>
      </div>
    </div>
    <div v-if="$slots.footer" class="stats-footer">
      <slot name="footer"></slot>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  label: string
  value: number | string
  unit?: string
  icon?: string
  iconColor?: string
  iconBg?: string
  trend?: number
}>(), {
  iconColor: '#409eff',
  iconBg: '#e8f4ff'
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    if (props.value >= 10000) {
      return (props.value / 10000).toFixed(1) + '万'
    }
    return props.value.toLocaleString()
  }
  return props.value
})

const trendClass = computed(() => {
  if (props.trend === undefined) return ''
  return props.trend > 0 ? 'trend-up' : props.trend < 0 ? 'trend-down' : 'trend-flat'
})
</script>

<style scoped lang="scss">
.stats-card {
  .stats-content {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  
  .stats-info {
    flex: 1;
    min-width: 0;
  }
  
  .stats-value {
    .value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }
    
    .unit {
      font-size: 14px;
      color: #909399;
      margin-left: 4px;
    }
  }
  
  .stats-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
  
  .stats-trend {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    margin-top: 8px;
    
    &.trend-up {
      color: #f56c6c;
    }
    
    &.trend-down {
      color: #67c23a;
    }
    
    &.trend-flat {
      color: #909399;
    }
    
    .trend-text {
      color: #c0c4cc;
      margin-left: 4px;
    }
  }
  
  .stats-footer {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}
</style>
