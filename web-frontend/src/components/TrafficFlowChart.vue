<template>
  <div class="traffic-flow-chart">
    <div class="chart-header">
      <span class="title">{{ title }}</span>
      <div class="controls">
        <el-radio-group v-model="timeRange" size="small" @change="updateChart">
          <el-radio-button label="1h">1小时</el-radio-button>
          <el-radio-button label="6h">6小时</el-radio-button>
          <el-radio-button label="24h">24小时</el-radio-button>
          <el-radio-button label="7d">7天</el-radio-button>
        </el-radio-group>
        <el-select v-model="selectedRoad" placeholder="选择道路" size="small" style="width: 150px" @change="updateChart">
          <el-option label="全部道路" value="all" />
          <el-option v-for="road in roads" :key="road.id" :label="road.name" :value="road.id" />
        </el-select>
      </div>
    </div>
    <div ref="chartRef" class="chart-container"></div>
    <div class="chart-footer">
      <div class="stat-item">
        <span class="label">峰值流量:</span>
        <span class="value">{{ peakFlow }}</span>
      </div>
      <div class="stat-item">
        <span class="label">平均流量:</span>
        <span class="value">{{ avgFlow }}</span>
      </div>
      <div class="stat-item">
        <span class="label">峰值时段:</span>
        <span class="value">{{ peakTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'

interface FlowDataPoint {
  time: string
  value: number
  congestionIndex?: number
}

interface Road {
  id: string
  name: string
}

interface Props {
  title?: string
  data?: FlowDataPoint[]
  roads?: Road[]
  showCongestion?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '交通流量趋势',
  data: () => [],
  roads: () => [
    { id: '1', name: '中山路' },
    { id: '2', name: '人民大道' },
    { id: '3', name: '建设路' },
    { id: '4', name: '解放大道' }
  ],
  showCongestion: true
})

const emit = defineEmits(['timeRangeChange', 'roadChange'])

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts
const timeRange = ref('24h')
const selectedRoad = ref('all')

// 生成模拟数据
const generateMockData = (): FlowDataPoint[] => {
  const data: FlowDataPoint[] = []
  const hours = timeRange.value === '1h' ? 60 : timeRange.value === '6h' ? 360 : timeRange.value === '24h' ? 24 : 168
  const interval = timeRange.value === '1h' || timeRange.value === '6h' ? 1 : timeRange.value === '24h' ? 1 : 4
  
  for (let i = 0; i < hours; i += interval) {
    const hour = i % 24
    // 模拟早晚高峰
    let baseValue = 500
    if (hour >= 7 && hour <= 9) baseValue = 1500
    else if (hour >= 17 && hour <= 19) baseValue = 1800
    else if (hour >= 0 && hour <= 6) baseValue = 200
    
    const value = baseValue + Math.random() * 300 - 150
    const congestionIndex = value > 1200 ? 4 + Math.random() * 2 : value > 800 ? 2 + Math.random() * 2 : 1 + Math.random()
    
    let timeLabel: string
    if (timeRange.value === '1h') {
      timeLabel = `${i}分钟`
    } else if (timeRange.value === '6h') {
      timeLabel = `${Math.floor(i / 60)}:${(i % 60).toString().padStart(2, '0')}`
    } else if (timeRange.value === '24h') {
      timeLabel = `${hour}:00`
    } else {
      timeLabel = `第${Math.floor(i / 24) + 1}天 ${hour}:00`
    }
    
    data.push({
      time: timeLabel,
      value: Math.round(value),
      congestionIndex: Math.round(congestionIndex * 10) / 10
    })
  }
  
  return data
}

// 统计数据
const chartData = computed(() => props.data.length > 0 ? props.data : generateMockData())
const peakFlow = computed(() => Math.max(...chartData.value.map(d => d.value)))
const avgFlow = computed(() => Math.round(chartData.value.reduce((sum, d) => sum + d.value, 0) / chartData.value.length))
const peakTime = computed(() => {
  const peak = chartData.value.find(d => d.value === peakFlow.value)
  return peak?.time || '-'
})

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  updateChart()
  
  window.addEventListener('resize', () => chart?.resize())
}

// 更新图表
const updateChart = () => {
  const data = chartData.value
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: (params: any) => {
        const data = params[0]
        let html = `<div><strong>${data.axisValue}</strong></div>`
        params.forEach((item: any) => {
          html += `<div>${item.marker} ${item.seriesName}: ${item.value}${item.seriesName.includes('指数') ? '' : ' 辆'}</div>`
        })
        return html
      }
    },
    legend: {
      data: props.showCongestion ? ['车流量', '拥堵指数'] : ['车流量'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.time),
      boundaryGap: false,
      axisLabel: {
        rotate: data.length > 24 ? 45 : 0,
        fontSize: 10
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '车流量',
        position: 'left',
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '拥堵指数',
        position: 'right',
        min: 0,
        max: 10,
        axisLabel: {
          formatter: '{value}'
        },
        show: props.showCongestion
      }
    ],
    series: [
      {
        name: '车流量',
        type: 'line' as const,
        data: data.map(d => d.value),
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.4)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        itemStyle: {
          color: '#409EFF'
        },
        markLine: {
          data: [
            { type: 'average', name: '平均值' }
          ],
          lineStyle: {
            color: '#E6A23C'
          }
        }
      },
      ...(props.showCongestion ? [{
        name: '拥堵指数',
        type: 'line' as const,
        yAxisIndex: 1,
        data: data.map(d => d.congestionIndex),
        smooth: true,
        lineStyle: {
          color: '#F56C6C',
          width: 2,
          type: 'dashed' as const
        },
        itemStyle: {
          color: '#F56C6C'
        }
      }] : [])
    ],
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        type: 'slider',
        show: data.length > 30,
        start: 0,
        end: 100,
        bottom: 30,
        height: 15
      }
    ]
  }
  
  chart.setOption(option)
  
  emit('timeRangeChange', timeRange.value)
  emit('roadChange', selectedRoad.value)
}

watch(() => props.data, () => updateChart(), { deep: true })

onMounted(() => initChart())

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})

defineExpose({
  refresh: updateChart
})
</script>

<style scoped lang="scss">
.traffic-flow-chart {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    border-bottom: 1px solid #eee;
    
    .title {
      font-weight: bold;
      font-size: 14px;
    }
    
    .controls {
      display: flex;
      gap: 10px;
    }
  }
  
  .chart-container {
    flex: 1;
    min-height: 300px;
  }
  
  .chart-footer {
    display: flex;
    justify-content: space-around;
    padding: 10px;
    background: #f8f9fa;
    border-top: 1px solid #eee;
    
    .stat-item {
      text-align: center;
      
      .label {
        color: #909399;
        font-size: 12px;
      }
      
      .value {
        color: #303133;
        font-weight: bold;
        font-size: 14px;
        margin-left: 5px;
      }
    }
  }
}
</style>
