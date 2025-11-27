<template>
  <div class="report-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="报表类型">
          <el-select v-model="filterForm.type" placeholder="请选择报表类型">
            <el-option label="交通流量报表" value="traffic" />
            <el-option label="仓储统计报表" value="warehouse" />
            <el-option label="停车收入报表" value="parking" />
            <el-option label="员工绩效报表" value="employee" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate">
            <el-icon><Document /></el-icon>
            生成报表
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>数据趋势图</span>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="summary-card">
          <template #header>
            <span>数据汇总</span>
          </template>
          <div class="summary-list">
            <div class="summary-item">
              <div class="label">总流量</div>
              <div class="value">{{ summaryData.totalFlow }}</div>
            </div>
            <div class="summary-item">
              <div class="label">平均流量</div>
              <div class="value">{{ summaryData.avgFlow }}</div>
            </div>
            <div class="summary-item">
              <div class="label">峰值流量</div>
              <div class="value">{{ summaryData.peakFlow }}</div>
            </div>
            <div class="summary-item">
              <div class="label">低谷流量</div>
              <div class="value">{{ summaryData.valleyFlow }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <span>报表历史</span>
      </template>
      <el-table :data="historyData" stripe>
        <el-table-column prop="name" label="报表名称" min-width="200" />
        <el-table-column prop="type" label="报表类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dateRange" label="时间范围" width="200" />
        <el-table-column prop="createdBy" label="生成人" width="100" />
        <el-table-column prop="createdAt" label="生成时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleDownload(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const filterForm = reactive({
  type: 'traffic',
  dateRange: []
})

const summaryData = reactive({
  totalFlow: '1,234,567',
  avgFlow: '45,678',
  peakFlow: '89,012',
  valleyFlow: '12,345'
})

const historyData = ref([
  {
    id: 1,
    name: '2024年1月交通流量报表',
    type: 'traffic',
    dateRange: '2024-01-01 至 2024-01-31',
    createdBy: 'admin',
    createdAt: '2024-02-01 09:00:00'
  },
  {
    id: 2,
    name: '2024年1月仓储统计报表',
    type: 'warehouse',
    dateRange: '2024-01-01 至 2024-01-31',
    createdBy: 'admin',
    createdAt: '2024-02-01 10:00:00'
  }
])

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    traffic: '交通流量',
    warehouse: '仓储统计',
    parking: '停车收入',
    employee: '员工绩效'
  }
  return map[type] || type
}

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['今日', '昨日', '上周同期']
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: {
      type: 'value',
      name: '流量'
    },
    series: [
      {
        name: '今日',
        type: 'line',
        smooth: true,
        data: [120, 80, 350, 280, 420, 380, 150]
      },
      {
        name: '昨日',
        type: 'line',
        smooth: true,
        data: [100, 90, 320, 250, 400, 350, 130]
      },
      {
        name: '上周同期',
        type: 'line',
        smooth: true,
        data: [110, 85, 330, 260, 410, 360, 140]
      }
    ]
  }
  chartInstance.setOption(option)
}

const handleGenerate = () => {
  ElMessage.success('报表生成中...')
}

const handleExport = () => {
  ElMessage.success('正在导出...')
}

const handleView = (row: any) => {
  ElMessage.info(`查看报表: ${row.name}`)
}

const handleDownload = (row: any) => {
  ElMessage.success(`下载报表: ${row.name}`)
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  chartInstance?.dispose()
})
</script>

<style scoped lang="scss">
.report-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.chart-card, .summary-card, .table-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
}

.summary-list {
  .summary-item {
    display: flex;
    justify-content: space-between;
    padding: 15px 0;
    border-bottom: 1px solid #eee;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      color: #666;
    }
    
    .value {
      font-size: 18px;
      font-weight: bold;
      color: #409EFF;
    }
  }
}
</style>
