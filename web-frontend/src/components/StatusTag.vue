<template>
  <el-tag :type="tagType" :effect="effect" :size="size">
    <span v-if="dot" class="status-dot" :style="{ backgroundColor: dotColor }"></span>
    {{ text }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type TagType = 'success' | 'warning' | 'danger' | 'info' | 'primary' | ''

const props = withDefaults(defineProps<{
  status: string | number
  statusMap?: Record<string | number, { text: string; type: TagType; color?: string }>
  dot?: boolean
  effect?: 'dark' | 'light' | 'plain'
  size?: 'large' | 'default' | 'small'
}>(), {
  effect: 'light',
  size: 'default',
  dot: false
})

const defaultStatusMap: Record<string | number, { text: string; type: TagType; color?: string }> = {
  active: { text: '启用', type: 'success', color: '#67c23a' },
  inactive: { text: '禁用', type: 'info', color: '#909399' },
  pending: { text: '待处理', type: 'warning', color: '#e6a23c' },
  processing: { text: '处理中', type: 'primary', color: '#409eff' },
  completed: { text: '已完成', type: 'success', color: '#67c23a' },
  cancelled: { text: '已取消', type: 'info', color: '#909399' },
  failed: { text: '失败', type: 'danger', color: '#f56c6c' },
  online: { text: '在线', type: 'success', color: '#67c23a' },
  offline: { text: '离线', type: 'danger', color: '#f56c6c' },
  maintenance: { text: '维护中', type: 'warning', color: '#e6a23c' },
  normal: { text: '正常', type: 'success', color: '#67c23a' },
  congested: { text: '拥堵', type: 'warning', color: '#e6a23c' },
  blocked: { text: '阻塞', type: 'danger', color: '#f56c6c' },
  open: { text: '开放', type: 'success', color: '#67c23a' },
  closed: { text: '关闭', type: 'info', color: '#909399' },
  full: { text: '已满', type: 'danger', color: '#f56c6c' },
  low: { text: '低', type: 'info', color: '#909399' },
  medium: { text: '中', type: 'warning', color: '#e6a23c' },
  high: { text: '高', type: 'danger', color: '#f56c6c' },
  critical: { text: '严重', type: 'danger', color: '#f56c6c' },
  urgent: { text: '紧急', type: 'danger', color: '#f56c6c' }
}

const statusConfig = computed(() => {
  const map = { ...defaultStatusMap, ...props.statusMap }
  return map[props.status] || { text: String(props.status), type: '' as TagType }
})

const text = computed(() => statusConfig.value.text)
const tagType = computed(() => statusConfig.value.type)
const dotColor = computed(() => statusConfig.value.color || '#909399')
</script>

<style scoped lang="scss">
.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
</style>
