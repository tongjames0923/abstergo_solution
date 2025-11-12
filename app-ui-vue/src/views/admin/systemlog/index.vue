<template>
  <div class="dashboard-container">
    <!-- 优化后的头部区域 -->
    <div class="dashboard-header">
      <div class="header-main">
        <h1 class="dashboard-title">系统运行报表</h1>
        <p class="dashboard-subtitle">监控和分析系统运行状态与性能指标</p>
      </div>
      <div class="header-actions">
        <el-button
          type="primary"
          icon="el-icon-refresh"
          @click="fetchData"
          :loading="loading"
          class="refresh-button">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 重新设计的统计卡片 -->
    <el-row :gutter="20" class="stats-grid">
      <el-col :xs="24" :sm="12" :md="12" :lg="6" class="stat-col">
        <div class="stat-card primary">
          <div class="stat-icon">
            <i class="el-icon-s-operation"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(totalOperations) }}</div>
            <div class="stat-label">总操作数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" class="stat-col">
        <div class="stat-card success">
          <div class="stat-icon">
            <i class="el-icon-success"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ successRate }}%</div>
            <div class="stat-label">成功率</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" class="stat-col">
        <div class="stat-card warning">
          <div class="stat-icon">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ avgResponseTime }}<span class="unit">ms</span></div>
            <div class="stat-label">平均响应时间</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" class="stat-col">
        <div class="stat-card danger">
          <div class="stat-icon">
            <i class="el-icon-warning"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(totalErrors) }}</div>
            <div class="stat-label">错误总数</div>
          </div>
        </div>
      </el-col>

      <!-- 新增的统计卡片 -->
      <el-col :xs="24" :sm="12" :md="12" :lg="6" class="stat-col">
        <div class="stat-card info">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ avgUsersPerOperation }}</div>
            <div class="stat-label">平均每操作用户数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" class="stat-col">
        <div class="stat-card purple">
          <div class="stat-icon">
            <i class="el-icon-monitor"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stabilityIndex }}%</div>
            <div class="stat-label">稳定性指数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 优化后的图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 平均耗时最多的Top10 -->
        <el-col :xs="24" :sm="24" :md="12" class="chart-col">
          <el-card class="chart-card" shadow="never">
            <div slot="header" class="chart-header">
              <div class="chart-header-content">
                <i class="el-icon-data-line"></i>
                <span class="chart-title">平均耗时Top10</span>
              </div>
            </div>
            <div ref="combinedChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 错误次数最多的Top5 -->
        <el-col :xs="24" :sm="24" :md="12" class="chart-col">
          <el-card class="chart-card" shadow="never">
            <div slot="header" class="chart-header">
              <div class="chart-header-content">
                <i class="el-icon-warning-outline"></i>
                <span class="chart-title">错误次数Top5</span>
              </div>
            </div>
            <div ref="errorChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 新增的图表区域 -->
      <el-row :gutter="20">
        <!-- 稳定性分析图表 -->
        <el-col :xs="24" :sm="24" :md="12" class="chart-col">
          <el-card class="chart-card" shadow="never">
            <div slot="header" class="chart-header">
              <div class="chart-header-content">
                <i class="el-icon-s-data"></i>
                <span class="chart-title">操作稳定性分析</span>
              </div>
            </div>
            <div ref="stabilityChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 成功率分布图表 -->
        <el-col :xs="24" :sm="24" :md="12" class="chart-col">
          <el-card class="chart-card" shadow="never">
            <div slot="header" class="chart-header">
              <div class="chart-header-content">
                <i class="el-icon-pie-chart"></i>
                <span class="chart-title">成功率分布</span>
              </div>
            </div>
            <div ref="successRateChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 优化后的详细数据区域 -->
    <el-card class="data-card" shadow="never">
      <div slot="header" class="card-header">
        <div class="card-header-content">
          <i class="el-icon-document"></i>
          <span class="card-title">详细数据</span>
        </div>
        <div class="table-header-actions">
          <el-button
            type="text"
            @click="toggleTableVisibility"
            :icon="tableVisible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
            class="toggle-button">
            {{ tableVisible ? '收起表格' : '展开表格' }}
          </el-button>
        </div>
      </div>

      <el-collapse-transition>
        <div v-show="tableVisible" class="table-container">
          <el-table
            v-loading="loading"
            :data="paginatedData"
            border
            stripe
            style="width: 100%"
            :default-sort="{prop: 'operateCount', order: 'descending'}"
            class="modern-table"
            highlight-current-row
            @row-click="handleRowClick">
            <el-table-column prop="operateType" label="操作类型" min-width="180" show-overflow-tooltip>
              <template slot-scope="scope">
                <span class="operate-type">{{ scope.row.operateType }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operateCount" label="操作次数" width="100" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <el-tag size="small" type="primary">{{ formatNumber(scope.row.operateCount) }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="operateTime" label="总耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value">{{ formatNumber(scope.row.operateTime) }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operateTimeMax" label="最大耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value max-time">{{ formatNumber(scope.row.operateTimeMax) }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operateTimeMin" label="最小耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value min-time">{{ formatNumber(scope.row.operateTimeMin) }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="avgTimeExcludingMinMax" label="平均耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value avg-time">{{ calculateAvgExcludingMinMax(scope.row) }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="errorCount" label="错误次数" width="100" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <el-tag
                  size="small"
                  :type="scope.row.errorCount > 0 ? 'danger' : 'success'">
                  {{ formatNumber(scope.row.errorCount) }}
                </el-tag>
              </template>
            </el-table-column>

            <!-- 新增的表格列 -->
            <el-table-column prop="successRate" label="成功率" width="100" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <el-tag
                  size="small"
                  :type="scope.row.successRate >= 95 ? 'success' : (scope.row.successRate >= 80 ? 'warning' : 'danger')">
                  {{ scope.row.successRate }}%
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="operateTime90Percentile" label="90%分位响应时间(ms)" width="150" sortable
                             align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value">{{ formatNumber(scope.row.operateTime90Percentile) }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operateTimeStdDev" label="响应时间标准差" width="130" sortable align="center"
                             :sort-orders="['ascending', 'descending']">
              <template slot-scope="scope">
                <span class="time-value">{{ formatNumber(scope.row.operateTimeStdDev) }}</span>
                <el-tag
                  size="mini"
                  :type="scope.row.operateTimeStdDev < 100 ? 'success' : (scope.row.operateTimeStdDev < 500 ? 'warning' : 'danger')"
                  style="margin-left: 5px;">
                  {{
                    scope.row.operateTimeStdDev < 100 ? '稳定' : (scope.row.operateTimeStdDev < 500 ? '一般' : '波动')
                  }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="userCount" label="涉及用户数" width="100" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <el-tag size="small" type="info">{{ formatNumber(scope.row.userCount) }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column label="成功率" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <el-progress
                  v-if="scope.row.operateCount > 0"
                  :percentage="getSuccessRate(scope.row)"
                  :status="getSuccessRateStatus(scope.row)"
                  :show-text="false"
                  :stroke-width="6"
                  class="success-progress"/>
                <span v-else>0%</span>
                <div v-if="scope.row.operateCount > 0" class="success-rate-text">
                  {{ getSuccessRate(scope.row) }}%
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pageSize"
              :total="logData.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
              :pager-count="5">
            </el-pagination>
          </div>
        </div>
      </el-collapse-transition>
    </el-card>

  </div>
</template>

<script>
import {getSystemLogOperateTypeList} from '@/api/systemlog'
import * as echarts from 'echarts'

export default {
  name: 'SystemLog',
  data() {
    return {
      loading: true,
      logData: [],
      tableVisible: true,
      currentPage: 1,
      pageSize: 10
    }
  },
  computed: {
    totalOperations() {
      return this.logData.reduce((sum, item) => sum + parseFloat(item.operateCount || 0), 0)
    },
    totalErrors() {
      return this.logData.reduce((sum, item) => sum + parseFloat(item.errorCount || 0), 0)
    },
    successRate() {
      if (this.totalOperations === 0) return 0
      const success = this.totalOperations - this.totalErrors
      return ((success / this.totalOperations) * 100).toFixed(2)
    },
    avgResponseTime() {
      if (this.logData.length === 0) return 0
      const totalTime = this.logData.reduce((sum, item) => sum + parseFloat(item.operateTime || 0), 0)
      const totalCount = this.logData.reduce((sum, item) => sum + parseFloat(item.operateCount || 0), 0)
      return totalCount > 0 ? (totalTime / totalCount).toFixed(2) : 0
    },
    paginatedData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.logData.slice(start, end)
    },
    // 新增的计算属性
    avgUsersPerOperation() {
      if (this.logData.length === 0) return 0
      const totalUsers = this.logData.reduce((sum, item) => sum + parseFloat(item.userCount || 0), 0)
      return (totalUsers / this.logData.length).toFixed(2)
    },

    stabilityIndex() {
      if (this.logData.length === 0) return 0

      // 获取有效数据（有操作次数且大于0的记录）
      const validData = this.logData.filter(item =>
        item.operateCount > 0
      )

      if (validData.length === 0) return 0

      // 计算总操作次数
      const totalOperations = validData.reduce((sum, item) =>
        sum + parseFloat(item.operateCount || 0), 0)

      if (totalOperations === 0) return 0

      // 1. 成功率因子 (权重: 40%)
      const totalErrors = validData.reduce((sum, item) =>
        sum + parseFloat(item.errorCount || 0), 0)
      const successRate = (totalOperations - totalErrors) / totalOperations
      const successFactor = successRate * 40

      // 2. 90%分位响应时间因子 (权重: 30%)
      // 计算加权的90%分位响应时间
      let weighted90PercentileSum = 0
      let valid90PercentileOperations = 0

      validData.forEach(item => {
        if (item.operateTime90Percentile !== undefined &&
          item.operateTime90Percentile !== null) {
          const operations = parseFloat(item.operateCount || 0)
          const percentile90 = parseFloat(item.operateTime90Percentile || 0)
          weighted90PercentileSum += percentile90 * operations
          valid90PercentileOperations += operations
        }
      })

      let percentileFactor = 0
      if (valid90PercentileOperations > 0) {
        const weightedAvg90Percentile = weighted90PercentileSum / valid90PercentileOperations
        // 500ms内得满分，超过按比例扣分
        percentileFactor = Math.max(0, 30 * (1 - (weightedAvg90Percentile / 500)))
      } else {
        // 如果没有90%分位数据，默认给满分
        percentileFactor = 30
      }

      // 3. 标准差因子 (权重: 20%)
      let weightedStdDevSum = 0
      let validStdDevOperations = 0

      validData.forEach(item => {
        if (item.operateTimeStdDev !== undefined &&
          item.operateTimeStdDev !== null) {
          const operations = parseFloat(item.operateCount || 0)
          const stdDev = parseFloat(item.operateTimeStdDev || 0)
          weightedStdDevSum += stdDev * operations
          validStdDevOperations += operations
        }
      })

      let stdDevFactor = 0
      if (validStdDevOperations > 0) {
        const weightedAvgStdDev = weightedStdDevSum / validStdDevOperations
        // 标准差为0时得满分，随着标准差增大得分递减（参考值1000ms）
        stdDevFactor = Math.max(0, 20 * Math.exp(-weightedAvgStdDev / 500))
      } else {
        // 如果没有标准差数据，默认给满分
        stdDevFactor = 20
      }

      // 4. 操作次数因子 (权重: 10%)
      // 操作次数越多说明越常用，稳定性应该更重要
      const avgOperations = totalOperations / validData.length
      // 以1000次为基准，进行对数缩放
      const operationsFactor = Math.min(10, 10 * (Math.log10(avgOperations + 1) / Math.log10(1000)))

      // 计算最终稳定性指数
      const stabilityScore = successFactor + percentileFactor + stdDevFactor + operationsFactor

      return Math.max(0, Math.min(100, stabilityScore)).toFixed(2)

    }
  },
  mounted() {
    this.fetchData()
  },
  beforeDestroy() {
    if (this.combinedChart) {
      this.combinedChart.dispose()
    }
    if (this.errorChart) {
      this.errorChart.dispose()
    }
    // 新增图表的销毁
    if (this.stabilityChart) {
      this.stabilityChart.dispose()
    }
    if (this.successRateChart) {
      this.successRateChart.dispose()
    }
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const response = await getSystemLogOperateTypeList()
        this.logData = response.data || []
        this.$nextTick(() => {
          this.initCharts()
        })
      } catch (error) {
        console.error('获取系统日志数据失败:', error)
        this.$message.error('获取系统日志数据失败')
      } finally {
        this.loading = false
      }
    },

    initCharts() {
      this.initCombinedChart()
      this.initErrorChart()
      // 新增图表初始化
      this.initStabilityChart()
      this.initSuccessRateChart()
    },

    initCombinedChart() {
      if (this.combinedChart) {
        this.combinedChart.dispose()
      }

      this.combinedChart = echarts.init(this.$refs.combinedChart)

      // 计算平均耗时并排序，取前10
      const timeData = this.logData.map(item => ({
        operateType: item.operateType,
        avgTime: item.operateCount > 0 ?
          (item.operateTime / item.operateCount).toFixed(2) : 0,
        operateCount: item.operateCount
      }))

      const sortedData = timeData
        .sort((a, b) => b.avgTime - a.avgTime)
        .slice(0, 10)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: (params) => {
            let html = params[0].name + '<br/>'
            params.forEach(param => {
              if (param.seriesName === '平均耗时(ms)') {
                html += '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + param.color + ';"></span>'
                html += param.seriesName + ': <b>' + this.formatNumber(param.value) + ' ms</b><br/>'
              } else {
                html += '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + param.color + ';"></span>'
                html += param.seriesName + ': <b>' + this.formatNumber(param.value) + '</b><br/>'
              }
            })
            return html
          }
        },
        legend: {
          data: ['平均耗时(ms)', '操作次数'],
          top: '5%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            data: sortedData.map(item => item.operateType),
            axisTick: {
              alignWithLabel: true
            },
            axisLabel: {
              rotate: 30,
              interval: 0,
              color: '#666'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '平均耗时(ms)',
            nameTextStyle: {
              color: '#666'
            },
            position: 'left',
            axisLabel: {
              color: '#666',
              formatter: (value) => this.formatNumber(value)
            }
          },
          {
            type: 'value',
            name: '操作次数',
            nameTextStyle: {
              color: '#666'
            },
            position: 'right',
            axisLabel: {
              color: '#666',
              formatter: (value) => this.formatNumber(value)
            }
          }
        ],
        series: [
          {
            name: '平均耗时(ms)',
            type: 'bar',
            barWidth: '30%',
            data: sortedData.map(item => item.avgTime),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: '#e6a23c'},
                {offset: 1, color: '#cf7a0a'}
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {offset: 0, color: '#ebb563'},
                  {offset: 1, color: '#e6a23c'}
                ])
              }
            }
          },
          {
            name: '操作次数',
            type: 'line',
            yAxisIndex: 1,
            data: sortedData.map(item => item.operateCount),
            itemStyle: {
              color: '#409eff'
            },
            smooth: true,
            lineStyle: {
              width: 3
            },
            symbolSize: 8,
            symbol: 'circle'
          }
        ]
      }

      this.combinedChart.setOption(option)
      window.addEventListener('resize', () => this.combinedChart.resize())
    },

    initErrorChart() {
      if (this.errorChart) {
        this.errorChart.dispose()
      }

      this.errorChart = echarts.init(this.$refs.errorChart)

      // 按错误次数排序，取前5
      const sortedData = [...this.logData]
        .filter(item => item.errorCount > 0) // 只显示有错误的项
        .sort((a, b) => b.errorCount - a.errorCount)
        .slice(0, 5)

      // 如果没有错误数据，显示提示信息
      if (sortedData.length === 0) {
        const option = {
          title: {
            text: '暂无错误数据',
            left: 'center',
            top: '40%',
            textStyle: {
              color: '#999',
              fontSize: 16
            }
          }
        }
        this.errorChart.setOption(option)
        return
      }

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            return params.name + '<br/>' +
              '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + params.color + ';"></span>' +
              params.seriesName + ': <b>' + this.formatNumber(params.value) + '</b> (' + params.percent + '%)'
          }
        },
        legend: {
          orient: 'horizontal',
          bottom: '5%',
          data: sortedData.map(item => item.operateType)
        },
        series: [
          {
            name: '错误次数',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 4,
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
            data: sortedData.map(item => ({
              name: item.operateType,
              value: item.errorCount
            })),
            color: ['#f56c6c', '#ff9c9c', '#ffbab8', '#ff7875', '#c42f2f']
          }
        ]
      }

      this.errorChart.setOption(option)
      window.addEventListener('resize', () => this.errorChart.resize())
    },

    // 新增的图表初始化方法
    initStabilityChart() {
      if (this.stabilityChart) {
        this.stabilityChart.dispose()
      }

      this.stabilityChart = echarts.init(this.$refs.stabilityChart)

      // 准备数据：按稳定性排序，取前10
      const stabilityData = this.logData
        .filter(item => item.operateTimeStdDev !== undefined && item.operateTimeStdDev !== null)
        .map(item => ({
          operateType: item.operateType,
          stdDev: parseFloat(item.operateTimeStdDev || 0),
          avgTime: item.operateCount > 0 ?
            (parseFloat(item.operateTime) / parseFloat(item.operateCount)).toFixed(2) : 0
        }))

      const sortedData = stabilityData
        .sort((a, b) => a.stdDev - b.stdDev) // 标准差越小越稳定
        .slice(0, 10)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: (params) => {
            let html = params[0].name + '<br/>'
            params.forEach(param => {
              if (param.seriesName === '响应时间标准差') {
                html += '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + param.color + ';"></span>'
                html += param.seriesName + ': <b>' + this.formatNumber(param.value) + ' ms</b><br/>'
              } else {
                html += '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + param.color + ';"></span>'
                html += param.seriesName + ': <b>' + this.formatNumber(param.value) + ' ms</b><br/>'
              }
            })
            return html
          }
        },
        legend: {
          data: ['响应时间标准差', '平均响应时间'],
          top: '5%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            data: sortedData.map(item => item.operateType),
            axisTick: {
              alignWithLabel: true
            },
            axisLabel: {
              rotate: 30,
              interval: 0,
              color: '#666'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '标准差(ms)',
            nameTextStyle: {
              color: '#666'
            },
            position: 'left',
            axisLabel: {
              color: '#666',
              formatter: (value) => this.formatNumber(value)
            }
          },
          {
            type: 'value',
            name: '平均响应时间(ms)',
            nameTextStyle: {
              color: '#666'
            },
            position: 'right',
            axisLabel: {
              color: '#666',
              formatter: (value) => this.formatNumber(value)
            }
          }
        ],
        series: [
          {
            name: '响应时间标准差',
            type: 'bar',
            barWidth: '30%',
            data: sortedData.map(item => item.stdDev),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: '#67c23a'},
                {offset: 1, color: '#3b7a1c'}
              ]),
              borderRadius: [4, 4, 0, 0]
            }
          },
          {
            name: '平均响应时间',
            type: 'line',
            yAxisIndex: 1,
            data: sortedData.map(item => item.avgTime),
            itemStyle: {
              color: '#409eff'
            },
            smooth: true
          }
        ]
      }

      this.stabilityChart.setOption(option)
      window.addEventListener('resize', () => this.stabilityChart.resize())
    }
    ,

    initSuccessRateChart() {
      if (this.successRateChart) {
        this.successRateChart.dispose()
      }

      this.successRateChart = echarts.init(this.$refs.successRateChart)

      // 按成功率分组
      const highSuccess = this.logData.filter(item => item.successRate >= 95).length
      const mediumSuccess = this.logData.filter(item => item.successRate >= 80 && item.successRate < 95).length
      const lowSuccess = this.logData.filter(item => item.successRate < 80).length

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            return params.name + '<br/>' +
              '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + params.color + ';"></span>' +
              params.seriesName + ': <b>' + this.formatNumber(params.value) + '</b> (' + params.percent + '%)'
          }
        },
        legend: {
          orient: 'horizontal',
          bottom: '5%',
          data: ['高成功率(≥95%)', '中等成功率(80-95%)', '低成功率(<80%)']
        },
        series: [
          {
            name: '成功率分布',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 4,
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
            data: [
              {value: highSuccess, name: '高成功率(≥95%)'},
              {value: mediumSuccess, name: '中等成功率(80-95%)'},
              {value: lowSuccess, name: '低成功率(<80%)'}
            ],
            color: ['#67c23a', '#e6a23c', '#f56c6c']
          }
        ]
      }

      this.successRateChart.setOption(option)
      window.addEventListener('resize', () => this.successRateChart.resize())
    }
    ,

    getSuccessRate(row) {
      if (row.operateCount === 0) return 0
      const successRate = ((row.operateCount - row.errorCount) / row.operateCount * 100)
      return Math.round(successRate)
    },

    getSuccessRateStatus(row) {
      const rate = this.getSuccessRate(row)
      if (rate >= 95) {
        return 'success'
      } else if (rate >= 80) {
        return 'warning'
      } else {
        return 'exception'
      }
    },

    toggleTableVisibility() {
      this.tableVisible = !this.tableVisible
    },

    handleSizeChange(newSize) {
      this.pageSize = newSize
    },

    handleCurrentChange(newPage) {
      this.currentPage = newPage
    },

    handleRowClick(row) {
      console.log('Row clicked:', row)
    },

    calculateAvgExcludingMinMax(row) {
      if (row.operateCount <= 2) return 0
      const total = parseFloat(row.operateTime) - parseFloat(row.operateTimeMax) - parseFloat(row.operateTimeMin)
      const count = row.operateCount - 2
      return (total / count).toFixed(2)
    }
    ,

    formatNumber(value) {
      if (value === undefined || value === null) return '0'
      // 添加千分位分隔符
      return parseFloat(value).toLocaleString()
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 20px;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;
  }
}

.header-main {
  flex: 1;
}

.dashboard-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.dashboard-subtitle {
  font-size: 15px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

.refresh-button {
  height: 40px;
  padding: 10px 20px;

  @media (max-width: 768px) {
    align-self: flex-end;
  }
}

.stats-grid {
  margin-bottom: 24px;
}

.stat-col {
  display: flex;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  width: 100%;
  gap: 16px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  }

  &.primary {
    border-left: 4px solid #409eff;
  }

  &.success {
    border-left: 4px solid #67c23a;
  }

  &.warning {
    border-left: 4px solid #e6a23c;
  }

  &.danger {
    border-left: 4px solid #f56c6c;
  }

  // 新增的样式
  &.info {
    border-left: 4px solid #909399;
  }

  &.purple {
    border-left: 4px solid #9c27b0;
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;

  .primary & {
    background: linear-gradient(135deg, #409eff, #1a56db);
  }

  .success & {
    background: linear-gradient(135deg, #67c23a, #3b7a1c);
  }

  .warning & {
    background: linear-gradient(135deg, #e6a23c, #cf7a0a);
  }

  .danger & {
    background: linear-gradient(135deg, #f56c6c, #c42f2f);
  }

  // 新增的样式
  .info & {
    background: linear-gradient(135deg, #909399, #606266);
  }

  .purple & {
    background: linear-gradient(135deg, #9c27b0, #7b1fa2);
  }
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
  line-height: 1.2;

  .unit {
    font-size: 16px;
    color: #999;
  }
}

.stat-label {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.data-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

.card-header-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.toggle-button {
  font-weight: 500;
}

.table-container {
  padding: 0 20px 20px;
}

.modern-table {
  border-radius: 8px;
  overflow: hidden;

  ::v-deep .el-table__header th {
    background-color: #f8f9fa;
    color: #333;
    font-weight: 600;
  }
}

.operate-type {
  font-weight: 500;
  color: #409eff;
}

.time-value {
  font-weight: 500;
}

.max-time {
  color: #f56c6c;
}

.min-time {
  color: #67c23a;
}

.avg-time {
  color: #e6a23c;
}

.success-progress {
  ::v-deep .el-progress-bar__outer {
    background-color: #f0f9eb;
  }
}

.success-rate-text {
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;

  ::v-deep .el-pagination {
    padding: 0;
  }

  @media (max-width: 768px) {
    justify-content: center;
  }
}

.charts-section {
  margin-top: 24px;
}

.chart-col {
  display: flex;
  margin-bottom: 20px;
}

.chart-card {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.chart-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.chart-header-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.chart-container {
  height: 400px;
  padding: 16px;

  @media (max-width: 768px) {
    height: 300px;
  }
}
</style>
