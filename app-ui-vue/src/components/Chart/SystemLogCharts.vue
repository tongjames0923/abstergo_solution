<template>
  <div class="system-log-charts">
    <el-row :gutter="20" class="chart-statistics">
      <el-col :span="6" :xs="24" :sm="12">
        <el-card class="stat-card stat-card-success">
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-shield" />
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
              <i class="el-icon-timer" />
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
              <i class="el-icon-s-data" />
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
              <i class="el-icon-check" />
            </div>
            <div class="stat-title">平均成功率</div>
            <div class="stat-value">{{ averageSuccessRate }}<span style="font-size: 18px;">%</span></div>
            <div class="stat-desc">请求成功比例</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 走马灯图表展示区域 -->
    <el-card class="carousel-chart-card">
      <div slot="header" class="chart-header">
        <span>系统运行图表</span>
        <div class="carousel-controls">
          <el-button
            icon="el-icon-arrow-left"
            circle
            size="mini"
            :disabled="carouselActiveIndex === 0"
            @click="prevChart"
          />
          <el-button
            icon="el-icon-arrow-right"
            circle
            size="mini"
            :disabled="carouselActiveIndex === 3"
            @click="nextChart"
          />
        </div>
      </div>

      <el-carousel
        ref="chartCarousel"
        :autoplay="false"
        indicator-position="none"
        arrow="never"
        height="400px"
        @change="handleCarouselChange"
      >
        <el-carousel-item name="stability">
          <div class="chart-item">
            <h3>稳定性指数分布</h3>
            <div ref="stabilityChart" class="chart-container" />
          </div>
        </el-carousel-item>

        <el-carousel-item name="responseTime">
          <div class="chart-item">
            <h3>响应时间分析</h3>
            <div class="chart-controls-wrapper">
              <div class="chart-controls">
                <el-select
                  v-model="responseTimeConfig.sortType"
                  size="mini"
                  placeholder="排序方式"
                  @change="updateResponseTimeChart"
                >
                  <el-option label="标准差" value="stdDev" />
                  <el-option label="平均耗时" value="avg" />
                  <el-option label="最大响应时间" value="max" />
                  <el-option label="最小响应时间" value="min" />
                  <el-option label="中位数响应时间" value="median" />
                  <el-option label="访问量" value="count" />
                </el-select>
                <el-select
                  v-model="responseTimeConfig.sortOrder"
                  size="mini"
                  placeholder="排序顺序"
                  @change="updateResponseTimeChart"
                >
                  <el-option label="降序" value="desc" />
                  <el-option label="升序" value="asc" />
                </el-select>
                <el-select
                  v-model="responseTimeConfig.showTop"
                  size="mini"
                  placeholder="显示数量"
                  @change="updateResponseTimeChart"
                >
                  <el-option label="显示5个" :value="5" />
                  <el-option label="显示10个" :value="10" />
                  <el-option label="显示15个" :value="15" />
                  <el-option label="显示20个" :value="20" />
                </el-select>
              </div>
            </div>
            <div ref="responseTimeChart" class="chart-container" />
          </div>
        </el-carousel-item>

        <el-carousel-item name="visitCount">
          <div class="chart-item">
            <h3>访问量Top10</h3>
            <div ref="visitCountChart" class="chart-container" />
          </div>
        </el-carousel-item>

        <el-carousel-item name="successRate">
          <div class="chart-item">
            <h3>成功率分布</h3>
            <div ref="successRateChart" class="chart-container" />
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>
  </div>
</template>

<script>
import { initResponseTimeChart } from './js/ResponseTimeChart.js'
import { initStabilityChart } from './js/StabilityChart.js'
import { initSuccessRateChart } from './js/SuccessRateChart.js'
import { initVisitCountChart } from './js/VisitCountChart.js'

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
      visitCountChart: null,
      // 响应时间图表配置
      responseTimeConfig: {
        sortType: 'stdDev',
        sortOrder: 'desc',
        showTop: 15
      },
      carouselActiveIndex: 0
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
      this.responseTimeChart = initResponseTimeChart(this.$refs.responseTimeChart, this.chartData, this.responseTimeConfig)
    },

    // 更新响应时间分析图表
    updateResponseTimeChart() {
      if (this.responseTimeChart) {
        this.responseTimeChart.dispose()
      }
      this.initResponseTimeChart()
    },

    // 初始化成功率分布图表
    initSuccessRateChart() {
      this.successRateChart = initSuccessRateChart(this.$refs.successRateChart, this.chartData)
    },

    // 初始化访问量图表
    initVisitCountChart() {
      this.visitCountChart = initVisitCountChart(this.$refs.visitCountChart, this.chartData)
    },

    // 上一个图表
    prevChart() {
      if (this.carouselActiveIndex > 0) {
        this.carouselActiveIndex--
        this.$refs.chartCarousel.setActiveItem(this.carouselActiveIndex)
      }
    },

    // 下一个图表
    nextChart() {
      if (this.carouselActiveIndex < 3) {
        this.carouselActiveIndex++
        this.$refs.chartCarousel.setActiveItem(this.carouselActiveIndex)
      }
    },

    // 处理走马灯切换
    handleCarouselChange(index) {
      this.carouselActiveIndex = index
    }
  }
}
</script>

<style lang="scss" scoped>
@import 'style/SystemLogCharts';

.chart-controls-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.chart-controls {
  display: flex;
  gap: 10px;
}

.carousel-chart-card {
  margin-top: 20px;

  ::v-deep .el-card__body {
    padding: 10px;
  }
}

.chart-item {
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;

  h3 {
    text-align: center;
    margin: 10px 0;
    font-size: 18px;
    font-weight: bold;
  }

  .chart-container {
    flex: 1;
  }
}

.carousel-controls {
  float: right;
  display: flex;
  gap: 10px;
}

// 响应式设计，小屏幕时将控制面板移到下一行
@media screen and (max-width: 768px) {
  .chart-controls-wrapper {
    justify-content: center;
  }

  .carousel-controls {
    float: none;
    justify-content: center;
    margin-top: 10px;
  }
}
</style>
