<template>
  <div class="traffic-statistics">
    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="道路">
          <el-select v-model="filterForm.roadId" placeholder="全部道路" clearable>
            <el-option label="中山路" :value="1" />
            <el-option label="人民大道" :value="2" />
            <el-option label="建设路" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalVehicles.toLocaleString() }}</div>
            <div class="stat-label">总过车数</div>
            <div class="stat-trend up">
              <el-icon><ArrowUp /></el-icon>
              <span>+12.5%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.avgSpeed }} km/h</div>
            <div class="stat-label">平均车速</div>
            <div class="stat-trend down">
              <el-icon><ArrowDown /></el-icon>
              <span>-3.2%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.peakHourFlow }}</div>
            <div class="stat-label">高峰小时流量</div>
            <div class="stat-trend up">
              <el-icon><ArrowUp /></el-icon>
              <span>+5.8%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.congestionIndex }}</div>
            <div class="stat-label">拥堵指数</div>
            <div class="stat-trend up">
              <el-icon><ArrowUp /></el-icon>
              <span>+2.1%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>交通流量趋势</span>
          </template>
          <div ref="flowChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>车型分布</span>
          </template>
          <div ref="vehicleTypeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>道路流量排行</span>
          </template>
          <div ref="roadRankChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>时段拥堵分析</span>
          </template>
          <div ref="congestionChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const filterForm = reactive({
  dateRange: null as [string, string] | null,
  roadId: null as number | null
})

const stats = reactive({
  totalVehicles: 1256789,
  avgSpeed: 42.5,
  peakHourFlow: 8523,
  congestionIndex: 1.85
})

const flowChartRef = ref<HTMLElement | null>(null)
const vehicleTypeChartRef = ref<HTMLElement | null>(null)
const roadRankChartRef = ref<HTMLElement | null>(null)
const congestionChartRef = ref<HTMLElement | null>(null)

const handleQuery = () => {
  ElMessage.success('查询成功')
}

const handleExport = () => {
  ElMessage.success('正在导出报表...')
}

const initFlowChart = () => {
  if (!flowChartRef.value) return
  const chart = echarts.init(flowChartRef.value)
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['今日', '昨日', '上周同期'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: hours },
    yAxis: { type: 'value', name: '车流量' },
    series: [
      { name: '今日', type: 'line', smooth: true, data: [1200, 800, 600, 500, 600, 1200, 2500, 4500, 5200, 4800, 4200, 3800, 3500, 3800, 4200, 4800, 5500, 6200, 5800, 4500, 3500, 2800, 2000, 1500] },
      { name: '昨日', type: 'line', smooth: true, data: [1100, 750, 580, 480, 580, 1100, 2400, 4300, 5000, 4600, 4000, 3600, 3300, 3600, 4000, 4600, 5300, 6000, 5600, 4300, 3300, 2600, 1900, 1400] },
      { name: '上周同期', type: 'line', smooth: true, data: [1050, 700, 550, 450, 550, 1050, 2300, 4100, 4800, 4400, 3800, 3400, 3100, 3400, 3800, 4400, 5100, 5800, 5400, 4100, 3100, 2400, 1800, 1300] }
    ]
  })
}

const initVehicleTypeChart = () => {
  if (!vehicleTypeChartRef.value) return
  const chart = echarts.init(vehicleTypeChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      labelLine: { show: false },
      data: [
        { value: 45, name: '小型车' },
        { value: 25, name: '中型货车' },
        { value: 18, name: '大型货车' },
        { value: 8, name: '新能源车' },
        { value: 4, name: '其他' }
      ]
    }]
  })
}

const initRoadRankChart = () => {
  if (!roadRankChartRef.value) return
  const chart = echarts.init(roadRankChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '车流量' },
    yAxis: { type: 'category', data: ['开发区主干道', '建设路', '解放路', '人民大道', '中山路'] },
    series: [{
      type: 'bar',
      data: [15234, 18562, 22341, 28956, 35678],
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#83bff6' },
        { offset: 0.5, color: '#188df0' },
        { offset: 1, color: '#188df0' }
      ]) }
    }]
  })
}

const initCongestionChart = () => {
  if (!congestionChartRef.value) return
  const chart = echarts.init(congestionChartRef.value)
  const hours = ['6:00', '7:00', '8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '拥堵指数', max: 5 },
    visualMap: { show: false, min: 1, max: 5, inRange: { color: ['#67C23A', '#E6A23C', '#F56C6C'] } },
    series: [{
      type: 'bar',
      data: [1.2, 2.8, 4.2, 3.5, 2.1, 1.8, 2.2, 2.0, 1.9, 2.3, 2.8, 3.8, 4.5, 3.2, 1.8]
    }]
  })
}

onMounted(() => {
  initFlowChart()
  initVehicleTypeChart()
  initRoadRankChart()
  initCongestionChart()
})
</script>

<style scoped lang="scss">
.traffic-statistics {
  .filter-card {
    margin-bottom: 16px;
  }
  
  .stat-row {
    margin-bottom: 16px;
  }
  
  .stat-card {
    .stat-content {
      text-align: center;
      padding: 8px 0;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }
    
    .stat-trend {
      margin-top: 8px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      
      &.up {
        color: #67C23A;
      }
      
      &.down {
        color: #F56C6C;
      }
    }
  }
  
  .chart-card {
    .chart-container {
      height: 350px;
    }
  }
}
</style>
