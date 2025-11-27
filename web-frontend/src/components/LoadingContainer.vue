<template>
  <div v-loading="loading" class="loading-container">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  loading: boolean
}>()
</script>

<style scoped lang="scss">
.loading-container {
  min-height: 200px;
}
</style>
