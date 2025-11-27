<template>
  <div class="realtime-monitoring">
    <!-- 顶部统计 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card online">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><VideoCamera /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.onlineCameras }}</div>
              <div class="stat-label">在线摄像头</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card traffic">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayVehicles }}</div>
              <div class="stat-label">今日过车</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayAnomalies }}</div>
              <div class="stat-label">今日异常</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card congestion">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgSpeed }} km/h</div>
              <div class="stat-label">平均车速</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="16">
      <!-- 左侧视频墙 -->
      <el-col :span="16">
        <el-card class="video-wall-card">
          <template #header>
            <div class="card-header">
              <span>实时监控</span>
              <div>
                <el-radio-group v-model="gridLayout" size="small">
                  <el-radio-button :value="4">2x2</el-radio-button>
                  <el-radio-button :value="9">3x3</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div class="video-wall" :class="`grid-${gridLayout}`">
            <div 
              v-for="(camera, index) in displayCameras" 
              :key="index" 
              class="video-cell"
              :class="{ active: selectedCamera?.cameraId === camera?.cameraId }"
              @click="selectCamera(camera)"
            >
              <template v-if="camera">
                <div class="video-placeholder">
                  <el-icon :size="32"><VideoCamera /></el-icon>
                </div>
                <div class="video-info">
                  <span class="camera-name">{{ camera.cameraName }}</span>
                  <el-tag :type="camera.status === 'ONLINE' ? 'success' : 'danger'" size="small">
                    {{ camera.status === 'ONLINE' ? '在线' : '离线' }}
                  </el-tag>
                </div>
              </template>
              <template v-else>
                <div class="empty-cell">
                  <el-icon :size="24"><Plus /></el-icon>
                  <span>添加摄像头</span>
                </div>
              </template>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息面板 -->
      <el-col :span="8">
        <!-- 实时过车记录 -->
        <el-card class="passing-card">
          <template #header>
            <div class="card-header">
              <span>实时过车记录</span>
              <el-tag type="danger" effect="dark" size="small">实时</el-tag>
            </div>
          </template>
          <div class="passing-list">
            <div v-for="record in passingRecords" :key="record.id" class="passing-item">
              <div class="plate-number" :class="plateColorClass(record.plateColor)">
                {{ record.plateNumber }}
              </div>
              <div class="passing-info">
                <div class="camera-name">{{ record.cameraName }}</div>
                <div class="passing-time">{{ record.passTime }}</div>
              </div>
              <div class="vehicle-type">{{ record.vehicleType }}</div>
            </div>
          </div>
        </el-card>

        <!-- 拥堵路段 -->
        <el-card class="congestion-card">
          <template #header>
            <span>拥堵路段</span>
          </template>
          <div class="congestion-list">
            <div v-for="road in congestionRoads" :key="road.roadId" class="congestion-item">
              <div class="road-info">
                <span class="road-name">{{ road.roadName }}</span>
                <el-progress 
                  :percentage="road.congestionIndex" 
                  :color="congestionColor(road.congestionIndex)"
                  :stroke-width="8"
                />
              </div>
              <div class="congestion-stats">
                <span>车速: {{ road.avgSpeed }} km/h</span>
                <span>车流: {{ road.vehicleCount }}/h</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { VideoCamera, Van, WarningFilled, Timer, Plus } from '@element-plus/icons-vue'

const gridLayout = ref(4)
const selectedCamera = ref<any>(null)

const stats = reactive({
  onlineCameras: 156,
  todayVehicles: 45823,
  todayAnomalies: 23,
  avgSpeed: 42.5
})

const cameras = ref([
  { cameraId: 1, cameraName: '中山路1号卡口', status: 'ONLINE' },
  { cameraId: 2, cameraName: '人民大道监控', status: 'ONLINE' },
  { cameraId: 3, cameraName: '建设路测速点', status: 'OFFLINE' },
  { cameraId: 4, cameraName: '解放路东入口', status: 'ONLINE' },
  { cameraId: 5, cameraName: '市政广场环岛', status: 'ONLINE' },
  { cameraId: 6, cameraName: '火车站出站口', status: 'ONLINE' },
  { cameraId: 7, cameraName: '机场高速入口', status: 'ONLINE' },
  { cameraId: 8, cameraName: '开发区主干道', status: 'OFFLINE' },
  { cameraId: 9, cameraName: '商业中心路口', status: 'ONLINE' }
])

const passingRecords = ref([
  { id: 1, plateNumber: '京A12345', plateColor: 'blue', cameraName: '中山路1号卡口', passTime: '10:30:25', vehicleType: '小型车' },
  { id: 2, plateNumber: '京B88888', plateColor: 'blue', cameraName: '人民大道监控', passTime: '10:30:18', vehicleType: '小型车' },
  { id: 3, plateNumber: '京AD8888', plateColor: 'yellow', cameraName: '建设路测速点', passTime: '10:30:12', vehicleType: '大型车' },
  { id: 4, plateNumber: '京F66666', plateColor: 'green', cameraName: '解放路东入口', passTime: '10:30:05', vehicleType: '新能源' },
  { id: 5, plateNumber: '京C55555', plateColor: 'blue', cameraName: '中山路1号卡口', passTime: '10:29:58', vehicleType: '小型车' }
])

const congestionRoads = ref([
  { roadId: 1, roadName: '中山路(人民路-解放路)', congestionIndex: 85, avgSpeed: 18, vehicleCount: 1200 },
  { roadId: 2, roadName: '人民大道(火车站-市政府)', congestionIndex: 72, avgSpeed: 25, vehicleCount: 980 },
  { roadId: 3, roadName: '建设路高架', congestionIndex: 45, avgSpeed: 45, vehicleCount: 650 }
])

const displayCameras = computed(() => {
  const result = [...cameras.value.slice(0, gridLayout.value)]
  while (result.length < gridLayout.value) {
    result.push(null as any)
  }
  return result
})

const plateColorClass = (color: string) => {
  return `plate-${color}`
}

const congestionColor = (index: number) => {
  if (index >= 80) return '#F56C6C'
  if (index >= 60) return '#E6A23C'
  if (index >= 40) return '#409EFF'
  return '#67C23A'
}

const selectCamera = (camera: any) => {
  if (camera) {
    selectedCamera.value = camera
  }
}

let timer: number | null = null

onMounted(() => {
  // 模拟实时数据更新
  timer = window.setInterval(() => {
    stats.todayVehicles += Math.floor(Math.random() * 5)
    stats.avgSpeed = 40 + Math.random() * 10
  }, 3000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.realtime-monitoring {
  .stat-row {
    margin-bottom: 16px;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      padding: 8px 0;
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
    
    &.online .stat-icon {
      background: rgba(103, 194, 58, 0.1);
      color: #67C23A;
    }
    
    &.traffic .stat-icon {
      background: rgba(64, 158, 255, 0.1);
      color: #409EFF;
    }
    
    &.warning .stat-icon {
      background: rgba(245, 108, 108, 0.1);
      color: #F56C6C;
    }
    
    &.congestion .stat-icon {
      background: rgba(230, 162, 60, 0.1);
      color: #E6A23C;
    }
  }
  
  .video-wall-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .video-wall {
    display: grid;
    gap: 8px;
    
    &.grid-4 {
      grid-template-columns: repeat(2, 1fr);
    }
    
    &.grid-9 {
      grid-template-columns: repeat(3, 1fr);
    }
    
    .video-cell {
      aspect-ratio: 16 / 9;
      background: #1a1a1a;
      border-radius: 4px;
      overflow: hidden;
      position: relative;
      cursor: pointer;
      border: 2px solid transparent;
      
      &.active {
        border-color: #409EFF;
      }
      
      .video-placeholder {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #666;
      }
      
      .video-info {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 8px;
        background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .camera-name {
          color: #fff;
          font-size: 12px;
        }
      }
      
      .empty-cell {
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #666;
        
        span {
          margin-top: 8px;
          font-size: 12px;
        }
      }
    }
  }
  
  .passing-card {
    margin-bottom: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .passing-list {
      max-height: 280px;
      overflow-y: auto;
    }
    
    .passing-item {
      display: flex;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .plate-number {
        padding: 4px 8px;
        border-radius: 4px;
        font-weight: 600;
        font-size: 13px;
        min-width: 90px;
        text-align: center;
        
        &.plate-blue {
          background: #409EFF;
          color: #fff;
        }
        
        &.plate-yellow {
          background: #E6A23C;
          color: #fff;
        }
        
        &.plate-green {
          background: linear-gradient(to right, #67C23A, #409EFF);
          color: #fff;
        }
      }
      
      .passing-info {
        flex: 1;
        margin-left: 12px;
        
        .camera-name {
          font-size: 13px;
          color: #303133;
        }
        
        .passing-time {
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
        }
      }
      
      .vehicle-type {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .congestion-card {
    .congestion-list {
      .congestion-item {
        padding: 12px 0;
        border-bottom: 1px solid #ebeef5;
        
        &:last-child {
          border-bottom: none;
        }
        
        .road-info {
          .road-name {
            font-size: 14px;
            color: #303133;
            margin-bottom: 8px;
            display: block;
          }
        }
        
        .congestion-stats {
          display: flex;
          justify-content: space-between;
          margin-top: 8px;
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>
