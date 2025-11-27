<template>
  <div class="video-player-container">
    <div class="player-header" v-if="showHeader">
      <div class="camera-info">
        <el-icon><VideoCamera /></el-icon>
        <span class="camera-name">{{ cameraName || '摄像头直播' }}</span>
        <el-tag :type="isLive ? 'success' : 'danger'" size="small">
          {{ isLive ? '在线' : '离线' }}
        </el-tag>
      </div>
      <div class="player-controls">
        <el-button-group>
          <el-button size="small" @click="toggleFullscreen" :icon="FullScreen">
            全屏
          </el-button>
          <el-button size="small" @click="takeSnapshot" :icon="Camera">
            截图
          </el-button>
          <el-button size="small" @click="toggleRecording" :type="isRecording ? 'danger' : 'default'" :icon="VideoPlay">
            {{ isRecording ? '停止录制' : '录制' }}
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <div ref="playerContainer" class="player-wrapper" :class="{ 'loading': loading }">
      <video
        ref="videoElement"
        class="video-js vjs-default-skin vjs-big-play-centered"
        playsinline
        controls
        preload="auto"
      >
        <p class="vjs-no-js">
          请启用JavaScript并升级到支持HTML5视频的浏览器
        </p>
      </video>
      
      <!-- 加载中遮罩 -->
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-overlay">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ error }}</span>
        <el-button type="primary" size="small" @click="retry">重试</el-button>
      </div>
      
      <!-- 离线提示 -->
      <div v-if="!isLive && !loading && !error" class="offline-overlay">
        <el-icon><VideoPause /></el-icon>
        <span>摄像头离线</span>
      </div>
    </div>
    
    <div class="player-footer" v-if="showFooter">
      <div class="stream-info">
        <span class="resolution">{{ resolution }}</span>
        <span class="bitrate" v-if="bitrate">{{ bitrate }} kbps</span>
        <span class="fps" v-if="fps">{{ fps }} fps</span>
      </div>
      <div class="timestamp">
        {{ currentTime }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import Hls from 'hls.js'
import { ElMessage } from 'element-plus'
import { 
  VideoCamera, 
  FullScreen, 
  Camera, 
  VideoPlay, 
  Loading,
  WarningFilled,
  VideoPause
} from '@element-plus/icons-vue'

// Props
interface Props {
  src: string                    // HLS流地址
  cameraId?: string              // 摄像头ID
  cameraName?: string            // 摄像头名称
  autoplay?: boolean             // 是否自动播放
  muted?: boolean                // 是否静音
  showHeader?: boolean           // 显示头部
  showFooter?: boolean           // 显示底部
  poster?: string                // 封面图
  retryInterval?: number         // 重试间隔(ms)
  maxRetries?: number            // 最大重试次数
}

const props = withDefaults(defineProps<Props>(), {
  autoplay: true,
  muted: true,
  showHeader: true,
  showFooter: true,
  retryInterval: 5000,
  maxRetries: 3
})

// Emits
const emit = defineEmits<{
  (e: 'ready'): void
  (e: 'play'): void
  (e: 'pause'): void
  (e: 'error', error: string): void
  (e: 'snapshot', dataUrl: string): void
  (e: 'recording-start'): void
  (e: 'recording-stop', blob: Blob): void
  (e: 'online-change', isOnline: boolean): void
}>()

// Refs
const videoElement = ref<HTMLVideoElement | null>(null)
const playerContainer = ref<HTMLDivElement | null>(null)

// State
const loading = ref(true)
const error = ref('')
const isLive = ref(false)
const isRecording = ref(false)
const resolution = ref('--')
const bitrate = ref(0)
const fps = ref(0)
const retryCount = ref(0)

// HLS instance
let hls: Hls | null = null
let mediaRecorder: MediaRecorder | null = null
let recordedChunks: Blob[] = []
let retryTimer: ReturnType<typeof setTimeout> | null = null
let statsTimer: ReturnType<typeof setInterval> | null = null

// Computed
const currentTime = computed(() => {
  return new Date().toLocaleTimeString('zh-CN', {
    hour12: false
  })
})

// 初始化HLS播放器
const initPlayer = () => {
  if (!videoElement.value) return
  
  loading.value = true
  error.value = ''
  
  if (Hls.isSupported()) {
    hls = new Hls({
      debug: false,
      enableWorker: true,
      lowLatencyMode: true,
      backBufferLength: 90,
      maxBufferLength: 30,
      maxMaxBufferLength: 600,
      maxBufferSize: 60 * 1000 * 1000,
      maxBufferHole: 0.5,
      levelLoadingRetryDelay: 1000,
      manifestLoadingRetryDelay: 1000,
      fragLoadingRetryDelay: 1000,
    })
    
    hls.loadSource(props.src)
    hls.attachMedia(videoElement.value)
    
    hls.on(Hls.Events.MANIFEST_PARSED, () => {
      loading.value = false
      isLive.value = true
      retryCount.value = 0
      emit('ready')
      emit('online-change', true)
      
      if (props.autoplay) {
        videoElement.value?.play().catch(() => {
          // 自动播放失败，可能需要用户交互
        })
      }
    })
    
    hls.on(Hls.Events.LEVEL_LOADED, (_, data) => {
      const level = data.details
      if (level.live) {
        isLive.value = true
      }
    })
    
    hls.on(Hls.Events.FRAG_LOADED, (_, data) => {
      // 更新统计信息
      const stats = data.frag.stats
      if (stats.total) {
        bitrate.value = Math.round((stats.total * 8) / (stats.loading.end - stats.loading.start))
      }
    })
    
    hls.on(Hls.Events.ERROR, (_, data) => {
      if (data.fatal) {
        switch (data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            handleNetworkError()
            break
          case Hls.ErrorTypes.MEDIA_ERROR:
            hls?.recoverMediaError()
            break
          default:
            error.value = '播放器错误'
            destroyPlayer()
            break
        }
      }
    })
    
    // 获取视频分辨率
    videoElement.value.onloadedmetadata = () => {
      if (videoElement.value) {
        resolution.value = `${videoElement.value.videoWidth}x${videoElement.value.videoHeight}`
      }
    }
    
    videoElement.value.onplay = () => emit('play')
    videoElement.value.onpause = () => emit('pause')
    
  } else if (videoElement.value.canPlayType('application/vnd.apple.mpegurl')) {
    // Safari原生支持HLS
    videoElement.value.src = props.src
    videoElement.value.onloadedmetadata = () => {
      loading.value = false
      isLive.value = true
      if (props.autoplay) {
        videoElement.value?.play()
      }
    }
    videoElement.value.onerror = () => {
      handleNetworkError()
    }
  } else {
    error.value = '浏览器不支持HLS播放'
    emit('error', error.value)
  }
}

// 处理网络错误
const handleNetworkError = () => {
  isLive.value = false
  emit('online-change', false)
  
  if (retryCount.value < props.maxRetries) {
    retryCount.value++
    error.value = `连接失败，${props.retryInterval / 1000}秒后重试 (${retryCount.value}/${props.maxRetries})`
    
    retryTimer = setTimeout(() => {
      if (hls) {
        hls.startLoad()
      } else {
        initPlayer()
      }
    }, props.retryInterval)
  } else {
    error.value = '连接失败，请检查网络或摄像头状态'
    emit('error', error.value)
    loading.value = false
  }
}

// 重试
const retry = () => {
  retryCount.value = 0
  destroyPlayer()
  initPlayer()
}

// 销毁播放器
const destroyPlayer = () => {
  if (retryTimer) {
    clearTimeout(retryTimer)
    retryTimer = null
  }
  
  if (statsTimer) {
    clearInterval(statsTimer)
    statsTimer = null
  }
  
  if (hls) {
    hls.destroy()
    hls = null
  }
}

// 切换全屏
const toggleFullscreen = async () => {
  if (!playerContainer.value) return
  
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
    } else {
      await playerContainer.value.requestFullscreen()
    }
  } catch (e) {
    ElMessage.error('全屏切换失败')
  }
}

// 截图
const takeSnapshot = () => {
  if (!videoElement.value) return
  
  try {
    const canvas = document.createElement('canvas')
    canvas.width = videoElement.value.videoWidth
    canvas.height = videoElement.value.videoHeight
    
    const ctx = canvas.getContext('2d')
    if (ctx) {
      ctx.drawImage(videoElement.value, 0, 0)
      const dataUrl = canvas.toDataURL('image/jpeg', 0.9)
      emit('snapshot', dataUrl)
      
      // 下载截图
      const link = document.createElement('a')
      link.href = dataUrl
      link.download = `snapshot_${props.cameraId || 'camera'}_${Date.now()}.jpg`
      link.click()
      
      ElMessage.success('截图已保存')
    }
  } catch (e) {
    ElMessage.error('截图失败')
  }
}

// 切换录制
const toggleRecording = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// 开始录制
const startRecording = () => {
  if (!videoElement.value) return
  
  try {
    const stream = (videoElement.value as any).captureStream?.() || 
                   (videoElement.value as any).mozCaptureStream?.()
    
    if (!stream) {
      ElMessage.error('浏览器不支持视频录制')
      return
    }
    
    recordedChunks = []
    mediaRecorder = new MediaRecorder(stream, {
      mimeType: 'video/webm;codecs=vp9'
    })
    
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        recordedChunks.push(event.data)
      }
    }
    
    mediaRecorder.onstop = () => {
      const blob = new Blob(recordedChunks, { type: 'video/webm' })
      emit('recording-stop', blob)
      
      // 下载录像
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `recording_${props.cameraId || 'camera'}_${Date.now()}.webm`
      link.click()
      URL.revokeObjectURL(url)
      
      ElMessage.success('录像已保存')
    }
    
    mediaRecorder.start(1000)
    isRecording.value = true
    emit('recording-start')
    ElMessage.info('开始录制')
  } catch (e) {
    ElMessage.error('录制启动失败')
  }
}

// 停止录制
const stopRecording = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }
  isRecording.value = false
}

// 监听src变化
watch(() => props.src, (newSrc) => {
  if (newSrc) {
    destroyPlayer()
    initPlayer()
  }
})

// 生命周期
onMounted(() => {
  if (props.src) {
    initPlayer()
  }
  
  // 定时更新FPS
  statsTimer = setInterval(() => {
    if (videoElement.value && !videoElement.value.paused) {
      const quality = (videoElement.value as any).getVideoPlaybackQuality?.()
      if (quality) {
        fps.value = Math.round(quality.totalVideoFrames / (performance.now() / 1000))
      }
    }
  }, 1000)
})

onBeforeUnmount(() => {
  stopRecording()
  destroyPlayer()
})

// 暴露方法
defineExpose({
  play: () => videoElement.value?.play(),
  pause: () => videoElement.value?.pause(),
  retry,
  takeSnapshot,
  toggleFullscreen
})
</script>

<style scoped lang="scss">
.video-player-container {
  display: flex;
  flex-direction: column;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  
  .player-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: rgba(0, 0, 0, 0.8);
    
    .camera-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #fff;
      
      .camera-name {
        font-weight: 500;
      }
    }
  }
  
  .player-wrapper {
    position: relative;
    width: 100%;
    padding-top: 56.25%; // 16:9 aspect ratio
    background: #1a1a1a;
    
    video {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
    
    .loading-overlay,
    .error-overlay,
    .offline-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      gap: 12px;
      background: rgba(0, 0, 0, 0.7);
      color: #fff;
      font-size: 14px;
      
      .el-icon {
        font-size: 48px;
      }
    }
    
    .loading-overlay .el-icon {
      animation: rotate 1s linear infinite;
    }
    
    .error-overlay {
      .el-icon {
        color: #f56c6c;
      }
    }
    
    .offline-overlay {
      .el-icon {
        color: #909399;
      }
    }
  }
  
  .player-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background: rgba(0, 0, 0, 0.8);
    color: #909399;
    font-size: 12px;
    
    .stream-info {
      display: flex;
      gap: 16px;
    }
    
    .timestamp {
      font-family: monospace;
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// Video.js styles override
:deep(.video-js) {
  width: 100%;
  height: 100%;
  
  .vjs-big-play-button {
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
  }
}
</style>
