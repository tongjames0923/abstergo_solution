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
            <div class="stat-value">{{ totalOperations }}</div>
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
            <div class="stat-value">{{ totalErrors }}</div>
            <div class="stat-label">错误总数</div>
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
                <el-tag size="small" type="primary">{{ scope.row.operateCount }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="operateTime" label="总耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value">{{ scope.row.operateTime }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operateTimeMax" label="最大耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value max-time">{{ scope.row.operateTimeMax }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operateTimeMin" label="最小耗时(ms)" width="120" sortable align="center"
                             :sort-orders="['descending', 'ascending']">
              <template slot-scope="scope">
                <span class="time-value min-time">{{ scope.row.operateTimeMin }}</span>
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
                  {{ scope.row.errorCount }}
                </el-tag>
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
      return (totalTime / this.logData.length).toFixed(2)
    },
    paginatedData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.logData.slice(start, end)
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
              color: '#666'
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
              color: '#666'
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
          formatter: '{a} <br/>{b}: {c} ({d}%)'
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
