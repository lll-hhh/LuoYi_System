<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #409eff"><Van /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayVehicles }}</div>
              <div class="stat-label">今日车流量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #67c23a"><VideoCamera /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.onlineCameras }}/{{ stats.totalCameras }}</div>
              <div class="stat-label">在线摄像头</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #e6a23c"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayAnomalies }}</div>
              <div class="stat-label">今日异常</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #909399"><Odometer /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgCongestionIndex }}</div>
              <div class="stat-label">平均拥堵指数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>24小时车流量趋势</span>
          </template>
          <div ref="trafficChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>车辆类型分布</span>
          </template>
          <div ref="vehicleTypeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时信息 -->
    <el-row :gutter="20" class="info-row">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <span>最新异常告警</span>
          </template>
          <el-table :data="recentAnomalies" size="small" max-height="300">
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="location" label="位置" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === '已处理' ? 'success' : 'warning'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <span>拥堵路段TOP5</span>
          </template>
          <el-table :data="congestionRoads" size="small" max-height="300">
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="roadName" label="道路名称" />
            <el-table-column prop="congestionIndex" label="拥堵指数" width="100" />
            <el-table-column prop="speed" label="平均车速" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const trafficChartRef = ref<HTMLElement>()
const vehicleTypeChartRef = ref<HTMLElement>()

let trafficChart: echarts.ECharts
let vehicleTypeChart: echarts.ECharts

// 模拟数据
const stats = ref({
  todayVehicles: 12580,
  onlineCameras: 156,
  totalCameras: 168,
  todayAnomalies: 23,
  avgCongestionIndex: 2.8
})

const recentAnomalies = ref([
  { time: '2024-01-15 14:32', type: '超速', location: '中山路-人民路', status: '待处理' },
  { time: '2024-01-15 14:28', type: '违停', location: '建设路口', status: '已处理' },
  { time: '2024-01-15 14:15', type: '闯红灯', location: '解放大道', status: '待处理' },
  { time: '2024-01-15 13:58', type: '逆行', location: '和平路', status: '已处理' },
  { time: '2024-01-15 13:42', type: '超速', location: '胜利街', status: '已处理' }
])

const congestionRoads = ref([
  { rank: 1, roadName: '中山路', congestionIndex: 5.8, speed: '12km/h' },
  { rank: 2, roadName: '人民大道', congestionIndex: 4.6, speed: '18km/h' },
  { rank: 3, roadName: '建设路', congestionIndex: 4.2, speed: '22km/h' },
  { rank: 4, roadName: '解放大道', congestionIndex: 3.8, speed: '26km/h' },
  { rank: 5, roadName: '和平路', congestionIndex: 3.5, speed: '28km/h' }
])

const initCharts = () => {
  // 车流量趋势图
  if (trafficChartRef.value) {
    trafficChart = echarts.init(trafficChartRef.value)
    trafficChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: Array.from({ length: 24 }, (_, i) => `${i}:00`)
      },
      yAxis: { type: 'value', name: '车流量' },
      series: [{
        name: '车流量',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        data: [120, 80, 45, 30, 25, 40, 180, 520, 680, 580, 420, 380, 
               450, 520, 480, 560, 680, 720, 580, 420, 320, 280, 220, 160]
      }]
    })
  }

  // 车辆类型分布图
  if (vehicleTypeChartRef.value) {
    vehicleTypeChart = echarts.init(vehicleTypeChartRef.value)
    vehicleTypeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        name: '车辆类型',
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 6500, name: '小型客车' },
          { value: 2800, name: '大型客车' },
          { value: 1800, name: '小型货车' },
          { value: 1200, name: '大型货车' },
          { value: 280, name: '其他' }
        ]
      }]
    })
  }
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', () => {
    trafficChart?.resize()
    vehicleTypeChart?.resize()
  })
})

onUnmounted(() => {
  trafficChart?.dispose()
  vehicleTypeChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stats-row {
    margin-bottom: 20px;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .stat-icon {
        font-size: 48px;
      }
      
      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
  
  .chart-row, .info-row {
    margin-bottom: 20px;
  }
  
  .chart-card, .info-card {
    height: 100%;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>
