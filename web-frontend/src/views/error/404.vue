<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <div class="error-description">请检查您输入的网址是否正确，或点击下方按钮返回首页</div>
      <el-button type="primary" size="large" @click="goHome">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .error-content {
    text-align: center;
    color: #fff;
    
    .error-code {
      font-size: 120px;
      font-weight: 700;
      line-height: 1;
      text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }
    
    .error-message {
      font-size: 24px;
      margin-top: 16px;
    }
    
    .error-description {
      font-size: 14px;
      margin-top: 8px;
      opacity: 0.8;
    }
    
    .el-button {
      margin-top: 32px;
    }
  }
}
</style>
