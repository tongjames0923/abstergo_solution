<template>
  <div class="system-log-charts">
    <el-row :gutter="20" class="chart-statistics">
      <el-col :span="6" :xs="24" :sm="12">
        <el-card class="stat-card stat-card-success">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-shield"></i>
            </div>
            <div class="stat-title">平均稳定性指数</div>
            <div class="stat-value">{{ averageStabilityIndex }}</div>
            <div class="stat-desc">系统整体稳定性评分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12">
        <el-card class="stat-card stat-card-primary">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-timer"></i>
            </div>
            <div class="stat-title">平均90%分位响应时间</div>
            <div class="stat-value">{{ average90PercentileResponseTime }}<span style="font-size: 18px;">ms</span></div>
            <div class="stat-desc">大部分请求的响应时间</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12">
        <el-card class="stat-card stat-card-info">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-s-data"></i>
            </div>
            <div class="stat-title">总访问量</div>
            <div class="stat-value">{{ totalVisitCount }}</div>
            <div class="stat-desc">统计周期内总请求次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12">
        <el-card class="stat-card stat-card-warning">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-check"></i>
            </div>
            <div class="stat-title">平均成功率</div>
            <div class="stat-value">{{ averageSuccessRate }}<span style="font-size: 18px;">%</span></div>
            <div class="stat-desc">请求成功比例</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-row">
      <el-col :span="12" :xs="24">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>稳定性指数分布</span>
          </div>
          <div ref="stabilityChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>响应时间分析</span>
          </div>
          <div ref="responseTimeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-row">
      <el-col :span="12" :xs="24">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>访问量Top10</span>
          </div>
          <div ref="visitCountChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>成功率分布</span>
          </div>
          <div ref="successRateChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {initResponseTimeChart} from './js/ResponseTimeChart.js'
import {initStabilityChart} from './js/StabilityChart.js'
import {initSuccessRateChart} from './js/SuccessRateChart.js'
import {initVisitCountChart} from './js/VisitCountChart.js'

export default {
  name: 'SystemLogCharts',
  props: {
    chartData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      stabilityChart: null,
      responseTimeChart: null,
      successRateChart: null,
      visitCountChart: null
    }
  },
  computed: {
    // 平均稳定性指数
    averageStabilityIndex() {
      if (!this.chartData || this.chartData.length === 0) return '0.00'
      // 根据规范，需要过滤无效数据后求算术平均值
      const validData = this.chartData.filter(item => item.stabilityIndex !== null && item.stabilityIndex !== undefined)
      if (validData.length === 0) return '0.00'
      const sum = validData.reduce((acc, item) => acc + item.stabilityIndex, 0)
      return (sum / validData.length).toFixed(2)
    },

    // 平均90%分位响应时间
    average90PercentileResponseTime() {
      if (!this.chartData || this.chartData.length === 0) return '0.00'
      const validData = this.chartData.filter(item =>
        item.operateTime90Percentile !== null &&
        item.operateTime90Percentile !== undefined)
      if (validData.length === 0) return '0.00'
      const sum = validData.reduce((acc, item) => acc + parseFloat(item.operateTime90Percentile), 0)
      return (sum / validData.length).toFixed(2)
    },

    // 总访问量
    totalVisitCount() {
      if (!this.chartData || this.chartData.length === 0) return '0'
      const sum = this.chartData.reduce((acc, item) => acc + parseFloat(item.operateCount || 0), 0)
      return parseInt(sum).toLocaleString()
    },

    // 平均成功率
    averageSuccessRate() {
      if (!this.chartData || this.chartData.length === 0) return '0.00'
      const validData = this.chartData.filter(item =>
        item.successRate !== null &&
        item.successRate !== undefined)
      if (validData.length === 0) return '0.00'
      const sum = validData.reduce((acc, item) => acc + parseFloat(item.successRate), 0)
      return (sum / validData.length).toFixed(2)
    }
  },
  watch: {
    chartData: {
      handler() {
        this.$nextTick(() => {
          this.initCharts()
        })
      },
      deep: true
    }
  },
  mounted() {
    this.initCharts()
  },
  beforeDestroy() {
    if (this.stabilityChart) {
      this.stabilityChart.dispose()
    }
    if (this.responseTimeChart) {
      this.responseTimeChart.dispose()
    }
    if (this.successRateChart) {
      this.successRateChart.dispose()
    }
    if (this.visitCountChart) {
      this.visitCountChart.dispose()
    }
  },
  methods: {
    initCharts() {
      this.$nextTick(() => {
        this.initStabilityChart()
        this.initResponseTimeChart()
        this.initSuccessRateChart()
        this.initVisitCountChart()
      })
    },

    // 初始化稳定性指数分布图表
    initStabilityChart() {
      this.stabilityChart = initStabilityChart(this.$refs.stabilityChart, this.chartData)
    },

    // 初始化响应时间分析图表
    initResponseTimeChart() {
      this.responseTimeChart = initResponseTimeChart(this.$refs.responseTimeChart, this.chartData)
    },

    // 初始化成功率分布图表
    initSuccessRateChart() {
      this.successRateChart = initSuccessRateChart(this.$refs.successRateChart, this.chartData)
    },

    // 初始化访问量图表
    initVisitCountChart() {
      this.visitCountChart = initVisitCountChart(this.$refs.visitCountChart, this.chartData)
    }
  }
}
</script>

<style lang="scss" scoped>
@import 'style/SystemLogCharts';
</style>
