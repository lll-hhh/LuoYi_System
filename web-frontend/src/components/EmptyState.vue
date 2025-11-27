<template>
  <div class="empty-state">
    <el-empty :image-size="imageSize" :description="description">
      <template v-if="$slots.default">
        <slot></slot>
      </template>
      <template v-else-if="actionText">
        <el-button type="primary" @click="$emit('action')">
          {{ actionText }}
        </el-button>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  description?: string
  imageSize?: number
  actionText?: string
}>(), {
  description: '暂无数据',
  imageSize: 100
})

defineEmits<{
  (e: 'action'): void
}>()
</script>

<style scoped lang="scss">
.empty-state {
  padding: 40px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
