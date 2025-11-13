<template>
  <div class="system-log-container">
    <el-card class="header-card">
      <div slot="header" class="card-header">
        <span>系统运行报表</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">刷新数据</el-button>
      </div>
      <div class="header-content">
        <el-alert
          title="系统运行状态概览"
          type="info"
          description="以下数据展示了最近28天内系统各项操作的统计情况，包括成功率、响应时间、稳定性等关键指标。"
          show-icon
          :closable="false">
        </el-alert>
      </div>
    </el-card>

    <!-- 图表组件 -->
    <system-log-charts :chart-data="tableData"/>

    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>详细数据</span>
        <div class="table-header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索操作类型..."
            clearable
            style="width: 200px; margin-right: 10px;"
            @clear="handleSearch"
            @keyup.enter.native="handleSearch">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <el-table
        :data="paginatedTableData"
        stripe
        style="width: 100%"
        v-loading="loading"
        element-loading-text="数据加载中..."
        :default-sort="{prop: 'stabilityIndex', order: 'descending'}"
      >
        <el-table-column prop="operateType" label="操作类型" min-width="150" sortable></el-table-column>
        <el-table-column prop="operateCount" label="操作次数" min-width="100" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.operateCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="successRate" label="成功率(%)" min-width="100" sortable>
          <template slot-scope="scope">
            <el-progress
              :percentage="parseFloat(formatNumber(scope.row.successRate))"
              :status="getProgressStatus(scope.row.successRate)">
            </el-progress>
            <span>{{ formatNumber(scope.row.successRate) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="operateTime" label="总耗时(ms)" min-width="100" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.operateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="operateTimeMedian" label="响应时间(ms)" min-width="150" sortable>
          <template slot-scope="scope">
            <div>中位数: {{ formatNumber(scope.row.operateTimeMedian) }}</div>
            <div>90%: {{ formatNumber(scope.row.operateTime90Percentile) }}</div>
            <div>标准差: {{ formatNumber(scope.row.operateTimeStdDev) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="stabilityIndex" label="稳定性指数" min-width="120" sortable>
          <template slot-scope="scope">
            <el-tag :type="getStabilityTagType(scope.row.stabilityIndex)">
              {{ scope.row.stabilityIndex }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数" min-width="80" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.userCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="ipCount" label="IP数" min-width="80" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.ipCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误数" min-width="80" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.errorCount) }}
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
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredTableData.length">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="操作详情" :visible.sync="dialogVisible" width="60%">
      <el-table :data="dialogTableData" border>
        <el-table-column prop="metric" label="指标" width="200"></el-table-column>
        <el-table-column prop="value" label="数值"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {getSystemLogOperateTypeList} from '@/api/systemlog'
import SystemLogCharts from '@/components/Chart/SystemLogCharts.vue'

export default {
  name: 'SystemLog',
  components: {
    SystemLogCharts
  },
  data() {
    return {
      loading: true,
      tableData: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogTableData: []
    }
  },
  computed: {
    totalCount() {
      return this.tableData.length
    },
    averageSuccessRate() {
      if (this.tableData.length === 0) return '0.00'
      const sum = this.tableData.reduce((acc, item) => acc + parseFloat(item.successRate), 0)
      return (sum / this.tableData.length).toFixed(2)
    },
    maxStabilityIndex() {
      if (this.tableData.length === 0) return 0
      return Math.max(...this.tableData.map(item => item.stabilityIndex))
    },
    averageResponseTime() {
      if (this.tableData.length === 0) return '0.00'
      const sum = this.tableData.reduce((acc, item) => acc + parseFloat(item.operateTimeMedian), 0)
      return (sum / this.tableData.length).toFixed(2)
    },
    filteredTableData() {
      if (!this.searchKeyword) {
        return this.tableData
      }
      const keyword = this.searchKeyword.toLowerCase()
      return this.tableData.filter(item =>
        item.operateType.toLowerCase().includes(keyword)
      )
    },
    paginatedTableData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredTableData.slice(start, end)
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true
      getSystemLogOperateTypeList()
        .then(response => {
          this.tableData = response.data || []
          this.loading = false
        })
        .catch(error => {
          console.error('获取系统日志数据失败:', error)
          this.$message.error('获取数据失败，请稍后重试')
          this.loading = false
        })
    },
    refreshData() {
      this.fetchData()
    },
    handleSearch() {
      this.currentPage = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
    },
    getProgressStatus(rate) {
      const value = parseFloat(rate)
      if (value >= 95) return 'success'
      if (value >= 80) return 'warning'
      return 'exception'
    },
    getStabilityTagType(index) {
      if (index >= 85) return 'success'
      else if (index >= 60) return 'warning'
      else if (index >= 45) return ''
      return 'danger'
    },
    formatNumber(value) {
      if (value === null || value === undefined) return '0.00'
      return parseFloat(value).toFixed(2)
    },
    showDetail(row) {
      this.dialogTableData = [
        {metric: '操作类型', value: row.operateType},
        {metric: '操作次数', value: this.formatNumber(row.operateCount)},
        {metric: '操作总时间', value: this.formatNumber(row.operateTime)},
        {metric: '最长执行时间', value: this.formatNumber(row.operateTimeMax)},
        {metric: '最短执行时间', value: this.formatNumber(row.operateTimeMin)},
        {metric: '错误次数', value: this.formatNumber(row.errorCount)},
        {metric: '成功率', value: this.formatNumber(row.successRate) + '%'},
        {metric: '90%分位数响应时间', value: this.formatNumber(row.operateTime90Percentile)},
        {metric: '中位数响应时间', value: this.formatNumber(row.operateTimeMedian)},
        {metric: '用户数', value: this.formatNumber(row.userCount)},
        {metric: 'IP数', value: this.formatNumber(row.ipCount)},
        {metric: '响应时间标准差', value: this.formatNumber(row.operateTimeStdDev)},
        {metric: '稳定性指数', value: row.stabilityIndex}
      ]
      this.dialogVisible = true
    }
  }
}
</script>

<style lang="scss" scoped>
.system-log-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .header-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-content {
      margin-top: 15px;
    }
  }

  .statistic-row {
    margin-bottom: 20px;

    .statistic-card {
      .statistic-item {
        text-align: center;

        .statistic-title {
          font-size: 14px;
          color: #606266;
          margin-bottom: 10px;
        }

        .statistic-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 5px;
        }

        .statistic-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .table-header-actions {
      display: flex;
      align-items: center;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  ::v-deep .el-table .cell {
    display: flex;
    flex-direction: column;
  }

  ::v-deep .el-table .cell > div:not(:last-child) {
    margin-bottom: 2px;
  }
}
</style>
