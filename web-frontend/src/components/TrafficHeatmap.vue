<template>
  <div class="traffic-heatmap">
    <div ref="mapRef" class="map-container"></div>
    <div class="legend">
      <div class="legend-title">拥堵程度</div>
      <div class="legend-items">
        <div class="legend-item">
          <span class="color-box" style="background: #00FF00"></span>
          <span>畅通</span>
        </div>
        <div class="legend-item">
          <span class="color-box" style="background: #FFFF00"></span>
          <span>缓行</span>
        </div>
        <div class="legend-item">
          <span class="color-box" style="background: #FF8800"></span>
          <span>拥堵</span>
        </div>
        <div class="legend-item">
          <span class="color-box" style="background: #FF0000"></span>
          <span>严重拥堵</span>
        </div>
      </div>
    </div>
    <div class="controls">
      <el-select v-model="selectedTime" placeholder="选择时间" size="small" @change="updateHeatmap">
        <el-option label="实时" value="realtime" />
        <el-option label="1小时前" value="1h" />
        <el-option label="3小时前" value="3h" />
        <el-option label="6小时前" value="6h" />
      </el-select>
      <el-switch
        v-model="showLabels"
        active-text="显示路名"
        inactive-text=""
        size="small"
        @change="toggleLabels"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

interface HeatmapPoint {
  lat: number
  lng: number
  value: number
  roadName?: string
}

interface Props {
  data?: HeatmapPoint[]
  center?: [number, number]
  zoom?: number
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  center: () => [116.4074, 39.9042],
  zoom: 12
})

const emit = defineEmits(['pointClick', 'dataLoaded'])

const mapRef = ref<HTMLElement>()
let chart: echarts.ECharts
const selectedTime = ref('realtime')
const showLabels = ref(true)

// 模拟热力图数据
const generateHeatmapData = (): HeatmapPoint[] => {
  const roads = [
    { name: '中山路', lat: 39.9100, lng: 116.4200, baseValue: 0.7 },
    { name: '人民大道', lat: 39.9150, lng: 116.4100, baseValue: 0.5 },
    { name: '建设路', lat: 39.9080, lng: 116.4300, baseValue: 0.8 },
    { name: '解放大道', lat: 39.9200, lng: 116.4150, baseValue: 0.4 },
    { name: '和平路', lat: 39.9050, lng: 116.4050, baseValue: 0.6 },
    { name: '胜利街', lat: 39.9180, lng: 116.4250, baseValue: 0.3 },
    { name: '文化路', lat: 39.9120, lng: 116.4000, baseValue: 0.5 },
    { name: '科技大道', lat: 39.9250, lng: 116.4350, baseValue: 0.2 }
  ]
  
  return roads.map(road => ({
    lat: road.lat + (Math.random() - 0.5) * 0.01,
    lng: road.lng + (Math.random() - 0.5) * 0.01,
    value: Math.min(1, road.baseValue + Math.random() * 0.3),
    roadName: road.name
  }))
}

// 获取颜色
const getColor = (value: number): string => {
  if (value < 0.25) return '#00FF00'
  if (value < 0.5) return '#FFFF00'
  if (value < 0.75) return '#FF8800'
  return '#FF0000'
}

// 初始化图表
const initChart = () => {
  if (!mapRef.value) return
  
  chart = echarts.init(mapRef.value)
  updateHeatmap()
  
  // 点击事件
  chart.on('click', (params: any) => {
    if (params.data) {
      emit('pointClick', params.data)
    }
  })
  
  // 自适应
  window.addEventListener('resize', () => {
    chart?.resize()
  })
}

// 更新热力图
const updateHeatmap = () => {
  const heatmapData = props.data.length > 0 ? props.data : generateHeatmapData()
  
  const option: echarts.EChartsOption = {
    title: {
      text: '交通拥堵热力图',
      subtext: selectedTime.value === 'realtime' ? '实时数据' : `${selectedTime.value}前数据`,
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const data = params.data
        if (!data) return ''
        const level = data[2] < 0.25 ? '畅通' : data[2] < 0.5 ? '缓行' : data[2] < 0.75 ? '拥堵' : '严重拥堵'
        return `${data[3] || '未知路段'}<br/>拥堵程度: ${level}<br/>拥堵指数: ${(data[2] * 10).toFixed(1)}`
      }
    },
    geo: {
      map: 'china',
      roam: true,
      center: props.center,
      zoom: props.zoom,
      emphasis: {
        itemStyle: {
          areaColor: '#f5f5f5'
        }
      },
      itemStyle: {
        areaColor: '#f0f0f0',
        borderColor: '#ccc'
      }
    },
    visualMap: {
      min: 0,
      max: 1,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 20,
      inRange: {
        color: ['#00FF00', '#FFFF00', '#FF8800', '#FF0000']
      },
      text: ['严重拥堵', '畅通']
    },
    series: [
      {
        name: '拥堵热力',
        type: 'scatter',
        coordinateSystem: 'geo',
        data: heatmapData.map(point => [point.lng, point.lat, point.value, point.roadName]),
        symbolSize: (val: number[]) => Math.max(10, val[2] * 30),
        encode: {
          value: 2
        },
        itemStyle: {
          color: (params: any) => getColor(params.data[2])
        },
        label: {
          show: showLabels.value,
          formatter: (params: any) => params.data[3] || '',
          position: 'right',
          fontSize: 10
        }
      },
      {
        name: '热力图',
        type: 'heatmap',
        coordinateSystem: 'geo',
        data: heatmapData.map(point => [point.lng, point.lat, point.value]),
        pointSize: 20,
        blurSize: 30
      }
    ]
  }
  
  chart.setOption(option)
  emit('dataLoaded', heatmapData)
}

// 切换标签显示
const toggleLabels = () => {
  updateHeatmap()
}

// 监听数据变化
watch(() => props.data, () => {
  updateHeatmap()
}, { deep: true })

onMounted(() => {
  initChart()
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})

// 暴露方法
defineExpose({
  refresh: updateHeatmap
})
</script>

<style scoped lang="scss">
.traffic-heatmap {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
  
  .map-container {
    width: 100%;
    height: 100%;
  }
  
  .legend {
    position: absolute;
    top: 60px;
    right: 20px;
    background: rgba(255, 255, 255, 0.9);
    padding: 10px 15px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .legend-title {
      font-weight: bold;
      margin-bottom: 8px;
      font-size: 12px;
    }
    
    .legend-items {
      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;
        font-size: 11px;
        
        .color-box {
          width: 16px;
          height: 16px;
          border-radius: 2px;
        }
      }
    }
  }
  
  .controls {
    position: absolute;
    top: 10px;
    left: 10px;
    display: flex;
    gap: 10px;
    align-items: center;
  }
}
</style>
