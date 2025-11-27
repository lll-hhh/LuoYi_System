<template>
  <div class="realtime-monitor">
    <!-- 连接状态 -->
    <div class="status-bar">
      <el-tag :type="connected ? 'success' : 'danger'" effect="dark" size="small">
        <el-icon><Connection /></el-icon>
        {{ connected ? '实时连接' : '连接断开' }}
      </el-tag>
      <span class="update-time">最后更新: {{ lastUpdateTime }}</span>
      <el-button size="small" text @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 实时统计 -->
    <el-row :gutter="15" class="stats-row">
      <el-col :span="6" v-for="stat in realtimeStats" :key="stat.key">
        <div class="stat-card" :style="{ borderColor: stat.color }">
          <div class="stat-value" :style="{ color: stat.color }">
            {{ stat.value }}
            <span class="unit">{{ stat.unit }}</span>
          </div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'" v-if="stat.trend !== 0">
            <el-icon><ArrowUp v-if="stat.trend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stat.trend) }}%
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 实时数据表格 -->
    <el-card class="data-table-card">
      <template #header>
        <div class="card-header">
          <span>实时路况监控</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索道路名称"
            size="small"
            style="width: 200px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
      <el-table
        :data="filteredRoads"
        :row-class-name="getRowClassName"
        height="300"
        size="small"
        @row-click="handleRowClick"
      >
        <el-table-column prop="name" label="道路名称" width="150" fixed />
        <el-table-column prop="vehicleCount" label="当前车流" width="100">
          <template #default="{ row }">
            <span :class="getFlowClass(row.vehicleCount, row.capacity)">
              {{ row.vehicleCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="道路容量" width="100" />
        <el-table-column prop="saturation" label="饱和度" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.saturation"
              :color="getSaturationColor(row.saturation)"
              :stroke-width="12"
              text-inside
            />
          </template>
        </el-table-column>
        <el-table-column prop="avgSpeed" label="平均车速" width="100">
          <template #default="{ row }">
            {{ row.avgSpeed }} km/h
          </template>
        </el-table-column>
        <el-table-column prop="congestionIndex" label="拥堵指数" width="120">
          <template #default="{ row }">
            <el-tag :type="getCongestionType(row.congestionIndex)" size="small">
              {{ row.congestionIndex.toFixed(1) }} - {{ getCongestionLevel(row.congestionIndex) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdate" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.lastUpdate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="viewDetail(row)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click.stop="viewHistory(row)">
              历史
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 告警信息 -->
    <el-card class="alert-card">
      <template #header>
        <div class="card-header">
          <span>
            实时告警
            <el-badge :value="unreadAlerts" :max="99" class="alert-badge" v-if="unreadAlerts > 0" />
          </span>
          <el-button size="small" text @click="markAllRead">全部已读</el-button>
        </div>
      </template>
      
      <el-scrollbar height="200">
        <div class="alert-list">
          <div
            v-for="alert in alerts"
            :key="alert.id"
            class="alert-item"
            :class="{ unread: !alert.read }"
            @click="handleAlertClick(alert)"
          >
            <el-icon class="alert-icon" :style="{ color: getAlertColor(alert.level) }">
              <WarningFilled />
            </el-icon>
            <div class="alert-content">
              <div class="alert-title">{{ alert.title }}</div>
              <div class="alert-desc">{{ alert.description }}</div>
              <div class="alert-time">{{ formatTime(alert.time) }}</div>
            </div>
            <el-tag :type="getAlertType(alert.level)" size="small">{{ alert.level }}</el-tag>
          </div>
          <el-empty v-if="alerts.length === 0" description="暂无告警" :image-size="60" />
        </div>
      </el-scrollbar>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Connection, Refresh, Search, ArrowUp, ArrowDown, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

interface RoadData {
  id: number
  name: string
  vehicleCount: number
  capacity: number
  saturation: number
  avgSpeed: number
  congestionIndex: number
  lastUpdate: Date
}

interface Alert {
  id: number
  title: string
  description: string
  level: string
  time: Date
  read: boolean
}

const emit = defineEmits(['roadSelect', 'alertClick', 'refresh'])

const connected = ref(true)
const lastUpdateTime = ref('')
const searchKeyword = ref('')

// 实时统计
const realtimeStats = ref([
  { key: 'flow', label: '当前车流量', value: 15680, unit: '辆/时', color: '#409EFF', trend: 5 },
  { key: 'speed', label: '平均车速', value: 32, unit: 'km/h', color: '#67C23A', trend: -3 },
  { key: 'congestion', label: '平均拥堵指数', value: 3.2, unit: '', color: '#E6A23C', trend: 8 },
  { key: 'alerts', label: '待处理告警', value: 12, unit: '个', color: '#F56C6C', trend: 0 }
])

// 模拟道路数据
const roads = ref<RoadData[]>([
  { id: 1, name: '中山路', vehicleCount: 850, capacity: 1000, saturation: 85, avgSpeed: 25, congestionIndex: 5.2, lastUpdate: new Date() },
  { id: 2, name: '人民大道', vehicleCount: 620, capacity: 800, saturation: 78, avgSpeed: 32, congestionIndex: 4.1, lastUpdate: new Date() },
  { id: 3, name: '建设路', vehicleCount: 450, capacity: 600, saturation: 75, avgSpeed: 35, congestionIndex: 3.5, lastUpdate: new Date() },
  { id: 4, name: '解放大道', vehicleCount: 380, capacity: 700, saturation: 54, avgSpeed: 45, congestionIndex: 2.3, lastUpdate: new Date() },
  { id: 5, name: '和平路', vehicleCount: 520, capacity: 650, saturation: 80, avgSpeed: 28, congestionIndex: 4.5, lastUpdate: new Date() },
  { id: 6, name: '胜利街', vehicleCount: 280, capacity: 500, saturation: 56, avgSpeed: 42, congestionIndex: 2.8, lastUpdate: new Date() },
  { id: 7, name: '文化路', vehicleCount: 190, capacity: 400, saturation: 48, avgSpeed: 50, congestionIndex: 1.5, lastUpdate: new Date() },
  { id: 8, name: '科技大道', vehicleCount: 320, capacity: 800, saturation: 40, avgSpeed: 55, congestionIndex: 1.2, lastUpdate: new Date() }
])

// 告警数据
const alerts = ref<Alert[]>([
  { id: 1, title: '中山路拥堵告警', description: '拥堵指数超过5.0，建议启动应急预案', level: '高', time: new Date(), read: false },
  { id: 2, title: '建设路车流量预警', description: '车流量接近饱和，预计10分钟后拥堵', level: '中', time: new Date(Date.now() - 300000), read: false },
  { id: 3, title: '人民大道事故', description: '发现轻微交通事故，已通知交警', level: '高', time: new Date(Date.now() - 600000), read: true },
  { id: 4, title: '和平路设备异常', description: '2号摄像头离线', level: '低', time: new Date(Date.now() - 900000), read: true }
])

// 计算属性
const filteredRoads = computed(() => {
  if (!searchKeyword.value) return roads.value
  return roads.value.filter(road => 
    road.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const unreadAlerts = computed(() => alerts.value.filter(a => !a.read).length)

// 更新时间
let updateTimer: ReturnType<typeof setInterval>

const updateTime = () => {
  lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN')
}

// 模拟实时数据更新
const simulateDataUpdate = () => {
  roads.value.forEach(road => {
    road.vehicleCount = Math.max(100, road.vehicleCount + Math.round((Math.random() - 0.5) * 50))
    road.saturation = Math.round((road.vehicleCount / road.capacity) * 100)
    road.avgSpeed = Math.max(10, road.avgSpeed + Math.round((Math.random() - 0.5) * 5))
    road.congestionIndex = Math.max(1, Math.min(10, road.congestionIndex + (Math.random() - 0.5) * 0.3))
    road.lastUpdate = new Date()
  })
  
  // 更新统计
  realtimeStats.value[0].value = roads.value.reduce((sum, r) => sum + r.vehicleCount, 0)
  realtimeStats.value[1].value = Math.round(roads.value.reduce((sum, r) => sum + r.avgSpeed, 0) / roads.value.length)
  realtimeStats.value[2].value = Math.round(roads.value.reduce((sum, r) => sum + r.congestionIndex, 0) / roads.value.length * 10) / 10
}

// 工具函数
const formatTime = (date: Date) => {
  return new Date(date).toLocaleString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  })
}

const getRowClassName = ({ row }: { row: RoadData }) => {
  if (row.congestionIndex >= 6) return 'row-danger'
  if (row.congestionIndex >= 4) return 'row-warning'
  return ''
}

const getFlowClass = (count: number, capacity: number) => {
  const ratio = count / capacity
  if (ratio >= 0.9) return 'flow-danger'
  if (ratio >= 0.7) return 'flow-warning'
  return 'flow-normal'
}

const getSaturationColor = (value: number) => {
  if (value >= 90) return '#F56C6C'
  if (value >= 70) return '#E6A23C'
  if (value >= 50) return '#409EFF'
  return '#67C23A'
}

const getCongestionType = (index: number) => {
  if (index >= 6) return 'danger'
  if (index >= 4) return 'warning'
  if (index >= 2) return 'info'
  return 'success'
}

const getCongestionLevel = (index: number) => {
  if (index >= 6) return '严重拥堵'
  if (index >= 4) return '拥堵'
  if (index >= 2) return '缓行'
  return '畅通'
}

const getAlertColor = (level: string) => {
  const colors: Record<string, string> = { '高': '#F56C6C', '中': '#E6A23C', '低': '#909399' }
  return colors[level] || '#909399'
}

const getAlertType = (level: string) => {
  const types: Record<string, string> = { '高': 'danger', '中': 'warning', '低': 'info' }
  return types[level] as any || 'info'
}

// 事件处理
const refreshData = () => {
  simulateDataUpdate()
  updateTime()
  ElMessage.success('数据已刷新')
  emit('refresh')
}

const handleRowClick = (row: RoadData) => {
  emit('roadSelect', row)
}

const viewDetail = (row: RoadData) => {
  ElMessage.info(`查看 ${row.name} 详情`)
}

const viewHistory = (row: RoadData) => {
  ElMessage.info(`查看 ${row.name} 历史数据`)
}

const handleAlertClick = (alert: Alert) => {
  alert.read = true
  emit('alertClick', alert)
}

const markAllRead = () => {
  alerts.value.forEach(a => a.read = true)
}

onMounted(() => {
  updateTime()
  updateTimer = setInterval(() => {
    updateTime()
    simulateDataUpdate()
  }, 5000)
})

onUnmounted(() => {
  if (updateTimer) clearInterval(updateTimer)
})

defineExpose({
  refresh: refreshData
})
</script>

<style scoped lang="scss">
.realtime-monitor {
  padding: 15px;
  
  .status-bar {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
    padding: 10px 15px;
    background: #f5f7fa;
    border-radius: 4px;
    
    .update-time {
      color: #909399;
      font-size: 12px;
    }
  }
  
  .stats-row {
    margin-bottom: 15px;
    
    .stat-card {
      background: #fff;
      padding: 15px;
      border-radius: 4px;
      border-left: 3px solid;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      position: relative;
      
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        
        .unit {
          font-size: 12px;
          font-weight: normal;
          color: #909399;
        }
      }
      
      .stat-label {
        color: #606266;
        font-size: 12px;
        margin-top: 5px;
      }
      
      .stat-trend {
        position: absolute;
        top: 10px;
        right: 10px;
        font-size: 12px;
        display: flex;
        align-items: center;
        
        &.up { color: #F56C6C; }
        &.down { color: #67C23A; }
      }
    }
  }
  
  .data-table-card, .alert-card {
    margin-bottom: 15px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .alert-badge {
    margin-left: 5px;
  }
  
  .alert-list {
    .alert-item {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 10px;
      border-bottom: 1px solid #eee;
      cursor: pointer;
      transition: background 0.2s;
      
      &:hover {
        background: #f5f7fa;
      }
      
      &.unread {
        background: #ecf5ff;
      }
      
      .alert-icon {
        font-size: 20px;
        margin-top: 2px;
      }
      
      .alert-content {
        flex: 1;
        
        .alert-title {
          font-weight: 500;
          margin-bottom: 4px;
        }
        
        .alert-desc {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }
        
        .alert-time {
          font-size: 11px;
          color: #c0c4cc;
        }
      }
    }
  }
  
  :deep(.row-danger) {
    background: #fef0f0;
  }
  
  :deep(.row-warning) {
    background: #fdf6ec;
  }
  
  .flow-danger { color: #F56C6C; font-weight: bold; }
  .flow-warning { color: #E6A23C; }
  .flow-normal { color: #67C23A; }
}
</style>
