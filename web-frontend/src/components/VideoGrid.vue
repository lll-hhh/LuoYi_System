<template>
  <div class="video-grid-container">
    <!-- 工具栏 -->
    <div class="grid-toolbar">
      <div class="grid-title">
        <el-icon><Monitor /></el-icon>
        <span>实时监控</span>
        <el-tag type="success" size="small">{{ onlineCameras }} / {{ cameras.length }} 在线</el-tag>
      </div>
      
      <div class="toolbar-actions">
        <el-select v-model="layout" size="small" style="width: 120px" @change="onLayoutChange">
          <el-option label="1x1" value="1x1" />
          <el-option label="2x2" value="2x2" />
          <el-option label="3x3" value="3x3" />
          <el-option label="4x4" value="4x4" />
          <el-option label="2x3" value="2x3" />
          <el-option label="3x2" value="3x2" />
        </el-select>
        
        <el-button-group>
          <el-button size="small" @click="refreshAll" :icon="Refresh">刷新</el-button>
          <el-button size="small" @click="toggleAutoRotate" :type="autoRotate ? 'primary' : 'default'" :icon="Aim">
            {{ autoRotate ? '停止轮播' : '自动轮播' }}
          </el-button>
        </el-button-group>
        
        <el-select 
          v-model="selectedCameras" 
          multiple 
          collapse-tags 
          placeholder="选择摄像头"
          size="small"
          style="width: 200px"
        >
          <el-option
            v-for="camera in allCameras"
            :key="camera.id"
            :label="camera.name"
            :value="camera.id"
          >
            <span>{{ camera.name }}</span>
            <el-tag 
              :type="camera.status === 'online' ? 'success' : 'danger'" 
              size="small" 
              style="margin-left: 8px"
            >
              {{ camera.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </el-option>
        </el-select>
      </div>
    </div>
    
    <!-- 视频网格 -->
    <div class="video-grid" :style="gridStyle">
      <div 
        v-for="(camera, index) in displayCameras" 
        :key="camera?.id || `empty-${index}`"
        class="grid-cell"
        :class="{ 'selected': selectedCell === index }"
        @click="selectCell(index)"
        @dblclick="openFullscreen(camera)"
      >
        <template v-if="camera">
          <VideoPlayer
            :ref="el => setPlayerRef(index, el)"
            :src="camera.streamUrl"
            :camera-id="camera.id"
            :camera-name="camera.name"
            :show-header="showHeaders"
            :show-footer="showFooters"
            :autoplay="true"
            :muted="true"
            @online-change="(online) => onCameraStatusChange(camera.id, online)"
            @snapshot="(url) => onSnapshot(camera, url)"
          />
        </template>
        <template v-else>
          <div class="empty-cell">
            <el-icon><Plus /></el-icon>
            <span>点击添加摄像头</span>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 全屏播放对话框 -->
    <el-dialog
      v-model="fullscreenDialogVisible"
      :title="fullscreenCamera?.name || '全屏播放'"
      width="90%"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <VideoPlayer
        v-if="fullscreenCamera"
        :src="fullscreenCamera.streamUrl"
        :camera-id="fullscreenCamera.id"
        :camera-name="fullscreenCamera.name"
        :show-header="false"
        :autoplay="true"
        :muted="false"
        style="height: 70vh"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import VideoPlayer from './VideoPlayer.vue'
import { Monitor, Refresh, Aim, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Types
interface Camera {
  id: string
  name: string
  streamUrl: string
  status: 'online' | 'offline'
  location?: string
  type?: string
}

// Props
interface Props {
  allCameras: Camera[]
  initialLayout?: string
  showHeaders?: boolean
  showFooters?: boolean
  rotateInterval?: number  // 轮播间隔(秒)
}

const props = withDefaults(defineProps<Props>(), {
  initialLayout: '2x2',
  showHeaders: true,
  showFooters: false,
  rotateInterval: 30
})

// Emits
const emit = defineEmits<{
  (e: 'camera-select', camera: Camera): void
  (e: 'layout-change', layout: string): void
  (e: 'snapshot', camera: Camera, dataUrl: string): void
}>()

// State
const layout = ref(props.initialLayout)
const selectedCameras = ref<string[]>([])
const selectedCell = ref<number | null>(null)
const autoRotate = ref(false)
const fullscreenDialogVisible = ref(false)
const fullscreenCamera = ref<Camera | null>(null)
const cameraStatus = ref<Map<string, boolean>>(new Map())
const playerRefs = ref<Map<number, any>>(new Map())

let rotateTimer: ReturnType<typeof setInterval> | null = null
let rotateIndex = 0

// Computed
const gridDimensions = computed(() => {
  const [cols, rows] = layout.value.split('x').map(Number)
  return { cols, rows }
})

const gridStyle = computed(() => ({
  display: 'grid',
  gridTemplateColumns: `repeat(${gridDimensions.value.cols}, 1fr)`,
  gridTemplateRows: `repeat(${gridDimensions.value.rows}, 1fr)`,
  gap: '4px'
}))

const cellCount = computed(() => {
  return gridDimensions.value.cols * gridDimensions.value.rows
})

const cameras = computed(() => {
  if (selectedCameras.value.length > 0) {
    return props.allCameras.filter(c => selectedCameras.value.includes(c.id))
  }
  return props.allCameras
})

const displayCameras = computed(() => {
  const result: (Camera | null)[] = []
  const cams = cameras.value
  
  for (let i = 0; i < cellCount.value; i++) {
    if (autoRotate.value) {
      // 轮播模式
      const idx = (rotateIndex + i) % cams.length
      result.push(cams[idx] || null)
    } else {
      result.push(cams[i] || null)
    }
  }
  
  return result
})

const onlineCameras = computed(() => {
  return Array.from(cameraStatus.value.values()).filter(v => v).length
})

// Methods
const setPlayerRef = (index: number, el: any) => {
  if (el) {
    playerRefs.value.set(index, el)
  } else {
    playerRefs.value.delete(index)
  }
}

const onLayoutChange = () => {
  emit('layout-change', layout.value)
}

const selectCell = (index: number) => {
  selectedCell.value = index
  const camera = displayCameras.value[index]
  if (camera) {
    emit('camera-select', camera)
  }
}

const openFullscreen = (camera: Camera | null) => {
  if (camera) {
    fullscreenCamera.value = camera
    fullscreenDialogVisible.value = true
  }
}

const refreshAll = () => {
  playerRefs.value.forEach((player) => {
    player?.retry?.()
  })
  ElMessage.success('正在刷新所有视频流')
}

const toggleAutoRotate = () => {
  autoRotate.value = !autoRotate.value
  
  if (autoRotate.value) {
    startRotate()
    ElMessage.info(`开始自动轮播，间隔 ${props.rotateInterval} 秒`)
  } else {
    stopRotate()
    ElMessage.info('已停止自动轮播')
  }
}

const startRotate = () => {
  stopRotate()
  rotateIndex = 0
  
  rotateTimer = setInterval(() => {
    rotateIndex = (rotateIndex + cellCount.value) % cameras.value.length
  }, props.rotateInterval * 1000)
}

const stopRotate = () => {
  if (rotateTimer) {
    clearInterval(rotateTimer)
    rotateTimer = null
  }
}

const onCameraStatusChange = (cameraId: string, online: boolean) => {
  cameraStatus.value.set(cameraId, online)
}

const onSnapshot = (camera: Camera, dataUrl: string) => {
  emit('snapshot', camera, dataUrl)
}

// Watchers
watch(() => props.initialLayout, (newLayout) => {
  layout.value = newLayout
})

watch(autoRotate, (rotating) => {
  if (!rotating) {
    stopRotate()
  }
})

// Lifecycle
onMounted(() => {
  // 初始化摄像头状态
  props.allCameras.forEach(camera => {
    cameraStatus.value.set(camera.id, camera.status === 'online')
  })
  
  // 默认选择前N个摄像头
  selectedCameras.value = props.allCameras
    .slice(0, cellCount.value)
    .map(c => c.id)
})

onBeforeUnmount(() => {
  stopRotate()
})
</script>

<style scoped lang="scss">
.video-grid-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1a1a1a;
  border-radius: 8px;
  overflow: hidden;
  
  .grid-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #2a2a2a;
    border-bottom: 1px solid #333;
    
    .grid-title {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #fff;
      font-size: 16px;
      font-weight: 500;
      
      .el-icon {
        font-size: 20px;
      }
    }
    
    .toolbar-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
  
  .video-grid {
    flex: 1;
    padding: 4px;
    overflow: hidden;
    
    .grid-cell {
      position: relative;
      border: 2px solid transparent;
      border-radius: 4px;
      overflow: hidden;
      transition: border-color 0.2s;
      cursor: pointer;
      
      &:hover {
        border-color: #409eff;
      }
      
      &.selected {
        border-color: #67c23a;
      }
      
      .empty-cell {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        height: 100%;
        background: #2a2a2a;
        color: #666;
        gap: 8px;
        
        .el-icon {
          font-size: 32px;
        }
        
        &:hover {
          background: #333;
          color: #999;
        }
      }
    }
  }
}

:deep(.el-select) {
  .el-input__wrapper {
    background: #333;
  }
  
  .el-input__inner {
    color: #fff;
  }
}

:deep(.el-dialog) {
  .el-dialog__header {
    background: #2a2a2a;
    color: #fff;
    margin: 0;
    padding: 16px 20px;
  }
  
  .el-dialog__body {
    padding: 0;
    background: #000;
  }
}
</style>
