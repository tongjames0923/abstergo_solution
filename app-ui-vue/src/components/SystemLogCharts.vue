<template>
  <div class="system-log-charts">
    <el-row :gutter="20" class="chart-statistics">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">平均稳定性指数</div>
            <div class="stat-value">{{ averageStabilityIndex }}</div>
            <div class="stat-desc">系统整体稳定性评分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">平均90%分位响应时间</div>
            <div class="stat-value">{{ average90PercentileResponseTime }}ms</div>
            <div class="stat-desc">大部分请求的响应时间</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">总访问量</div>
            <div class="stat-value">{{ totalVisitCount }}</div>
            <div class="stat-desc">统计周期内总请求次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">平均成功率</div>
            <div class="stat-value">{{ averageSuccessRate }}%</div>
            <div class="stat-desc">请求成功比例</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>稳定性指数分布</span>
          </div>
          <div ref="stabilityChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>响应时间分析</span>
          </div>
          <div ref="responseTimeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">

      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <span>访问量Top10</span>
          </div>
          <div ref="visitCountChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
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
import * as echarts from 'echarts'

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
      if (!this.$refs.stabilityChart) return

      if (this.stabilityChart) {
        this.stabilityChart.dispose()
      }

      this.stabilityChart = echarts.init(this.$refs.stabilityChart)

      // 分段统计稳定性指数
      const segments = [
        {name: '优秀(≥90)', value: 0},
        {name: '良好(80-89)', value: 0},
        {name: '一般(70-79)', value: 0},
        {name: '较差(<70)', value: 0}
      ]

      this.chartData.forEach(item => {
        const index = item.stabilityIndex
        if (index >= 90) {
          segments[0].value++
        } else if (index >= 80) {
          segments[1].value++
        } else if (index >= 70) {
          segments[2].value++
        } else {
          segments[3].value++
        }
      })

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          bottom: '0',
          left: 'center'
        },
        series: [
          {
            name: '稳定性指数分布',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: segments
          }
        ]
      }

      this.stabilityChart.setOption(option)
    },

    // 初始化响应时间分析图表
    initResponseTimeChart() {
      if (!this.$refs.responseTimeChart) return

      if (this.responseTimeChart) {
        this.responseTimeChart.dispose()
      }

      this.responseTimeChart = echarts.init(this.$refs.responseTimeChart)

      // 按响应时间标准差排序，取前10个
      const sortedData = [...this.chartData].sort((a, b) =>
        parseFloat(b.operateTimeStdDev) - parseFloat(a.operateTimeStdDev))
        .slice(0, 10)

      // 为水平条形图交换数据顺序（因为ECharts的y轴是从下到上显示的）
      const yAxisData = sortedData.map(item => item.operateType).reverse()

      // 多系列数据
      const seriesMax = sortedData.map(item => parseFloat(item.operateTimeMax) || 0).reverse()
      const series90Percentile = sortedData.map(item => parseFloat(item.operateTime90Percentile) || 0).reverse()
      const seriesMedian = sortedData.map(item => parseFloat(item.operateTimeMedian) || 0).reverse()
      const seriesMin = sortedData.map(item => parseFloat(item.operateTimeMin) || 0).reverse()
      const seriesAvg = sortedData.map(item => {
        const totalTime = parseFloat(item.operateTime) || 0;
        const count = parseFloat(item.operateCount) || 0;
        return count > 0 ? (totalTime / count) : 0;
      }).reverse()
      const seriesStdDev = sortedData.map(item => parseFloat(item.operateTimeStdDev) || 0).reverse()

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function (params) {
            let tooltip = params[0].name + '<br/>';
            params.forEach(param => {
              tooltip += `${param.seriesName}: ${param.value.toFixed(2)}ms<br/>`;
            });
            return tooltip;
          }
        },
        legend: {
          data: ['最大响应时间', '90%分位响应时间', '中位数响应时间', '平均耗时', '最小响应时间', '响应时间标准差'],
          top: '2%'
        },
        grid: {
          left: '3%',
          right: '4%',
          top: '15%',
          bottom: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '响应时间(ms)'
        },
        yAxis: {
          type: 'category',
          data: yAxisData,
          axisTick: {
            alignWithLabel: true
          },
          axisLabel: {
            fontSize: 12
          }
        },
        series: [
          {
            name: '最大响应时间',
            type: 'bar',
            barGap: '0%',
            barWidth: '12%',
            data: seriesMax,
            itemStyle: {
              color: '#f36f6f'
            },
            label: {
              show: false
            }
          },
          {
            name: '90%分位响应时间',
            type: 'bar',
            barGap: '0%',
            barWidth: '12%',
            data: series90Percentile,
            itemStyle: {
              color: '#83bff6'
            },
            label: {
              show: false
            }
          },
          {
            name: '中位数响应时间',
            type: 'bar',
            barGap: '0%',
            barWidth: '12%',
            data: seriesMedian,
            itemStyle: {
              color: '#188df0'
            },
            label: {
              show: false
            }
          },
          {
            name: '平均耗时',
            type: 'bar',
            barGap: '0%',
            barWidth: '12%',
            data: seriesAvg,
            itemStyle: {
              color: '#9966cc'
            },
            label: {
              show: false
            }
          },
          {
            name: '最小响应时间',
            type: 'bar',
            barGap: '0%',
            barWidth: '12%',
            data: seriesMin,
            itemStyle: {
              color: '#4682B4'
            },
            label: {
              show: false
            }
          },
          {
            name: '响应时间标准差',
            type: 'bar',
            barGap: '0%',
            barWidth: '12%',
            data: seriesStdDev,
            itemStyle: {
              color: '#ff7f00'
            },
            label: {
              show: false
            }
          }
        ]
      }

      this.responseTimeChart.setOption(option)
    },

    // 初始化成功率分布图表
    initSuccessRateChart() {
      if (!this.$refs.successRateChart) return

      if (this.successRateChart) {
        this.successRateChart.dispose()
      }

      this.successRateChart = echarts.init(this.$refs.successRateChart)

      // 分段统计成功率
      const segments = [
        {name: '优秀(≥95%)', value: 0},
        {name: '良好(90%-95%)', value: 0},
        {name: '一般(80%-90%)', value: 0},
        {name: '较差(<80%)', value: 0}
      ]

      this.chartData.forEach(item => {
        const rate = parseFloat(item.successRate)
        if (rate >= 95) {
          segments[0].value++
        } else if (rate >= 90) {
          segments[1].value++
        } else if (rate >= 80) {
          segments[2].value++
        } else {
          segments[3].value++
        }
      })

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          bottom: '0',
          left: 'center'
        },
        series: [
          {
            name: '成功率分布',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: segments
          }
        ]
      }

      this.successRateChart.setOption(option)
    },

    // 初始化访问量图表
    initVisitCountChart() {
      if (!this.$refs.visitCountChart) return

      if (this.visitCountChart) {
        this.visitCountChart.dispose()
      }

      this.visitCountChart = echarts.init(this.$refs.visitCountChart)

      // 按操作次数排序，取前10个
      const sortedData = [...this.chartData].sort((a, b) =>
        parseFloat(b.operateCount) - parseFloat(a.operateCount))
        .slice(0, 10)

      // 为水平条形图交换数据顺序（因为ECharts的y轴是从下到上显示的）
      const yAxisData = sortedData.map(item => item.operateType).reverse()
      const seriesData = sortedData.map(item => parseFloat(item.operateCount)).reverse()

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function (params) {
            const param = params[0]
            return `${param.name}<br/>访问量: ${parseInt(param.value).toLocaleString()}`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          top: '5%',
          bottom: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '访问量(次)',
          axisLabel: {
            formatter: function (value) {
              if (value >= 1000) {
                return (value / 1000) + 'k'
              }
              return value
            }
          }
        },
        yAxis: {
          type: 'category',
          data: yAxisData,
          axisTick: {
            alignWithLabel: true
          },
          axisLabel: {
            fontSize: 12
          }
        },
        series: [
          {
            name: '访问量',
            type: 'bar',
            barWidth: '50%',
            data: seriesData,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                {offset: 0, color: '#87CEEB'},
                {offset: 1, color: '#4682B4'}
              ]),
              borderRadius: [0, 4, 4, 0]  // 添加圆角
            },
            label: {
              show: true,
              position: 'right',
              formatter: function (params) {
                // 在柱状图右侧显示数值，大于1000的显示为k单位
                const value = params.value
                if (value >= 1000) {
                  return (value / 1000).toFixed(1) + 'k'
                }
                return value
              },
              fontSize: 10
            }
          }
        ]
      }

      this.visitCountChart.setOption(option)
    }
  }
}
</script>

<style lang="scss" scoped>
.system-log-charts {
  margin-bottom: 20px;

  .chart-statistics {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        text-align: center;
        padding: 15px 0;

        .stat-title {
          font-size: 14px;
          color: #606266;
          margin-bottom: 10px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 5px;
        }

        .stat-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;
  }

  .chart-card {
    .chart-header {
      font-weight: bold;
    }

    .chart-container {
      height: 400px;
      width: 100%;
    }
  }
}
</style>
