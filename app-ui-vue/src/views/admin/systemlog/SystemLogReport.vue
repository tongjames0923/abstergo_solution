<template>
  <div class="system-log-report">
    <!-- 图表组件 -->
    <system-log-charts :chart-data="tableData"/>

    <!-- 报表视图 - 详细数据表格 -->
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>{{ $t('systemlog.detailData') }}</span>
        <div class="table-header-actions">
          <el-input
            v-model="searchKeyword"
            clearable
            :placeholder="$t('systemlog.searchOperateType')"
            style="width: 200px; margin-right: 10px;"
            @clear="handleSearch"
            @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"/>
          </el-input>
          <el-button icon="el-icon-search" type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="paginatedTableData"
        :default-sort="{prop: 'stabilityIndex', order: 'descending'}"
        :element-loading-text="$t('systemlog.loadingText')"
        stripe
        style="width: 100%"
      >
        <el-table-column :label="$t('systemlog.reportOperateType')" min-width="150" prop="operateType" sortable/>
        <el-table-column :label="$t('systemlog.reportOperateCount')" min-width="100" prop="operateCount" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.operateCount) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.reportSuccessRate')" min-width="100" prop="successRate" sortable>
          <template slot-scope="scope">
            <el-progress
              :percentage="parseFloat(formatNumber(scope.row.successRate))"
              :status="getProgressStatus(scope.row.successRate)"
            />
            <span>{{ formatNumber(scope.row.successRate) }}%</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.reportTotalTime')" min-width="100" prop="operateTime" sortable>
          <template slot-scope="scope">
            {{ formatNumber(scope.row.operateTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.reportResponseTime')" min-width="150" prop="operateTimeMedian" sortable>
          <template slot-scope="scope">
            <div>{{ $t('systemlog.median') }}: {{ formatNumber(scope.row.operateTimeMedian) }}</div>
            <div>{{ $t('systemlog.percentile90') }}: {{ formatNumber(scope.row.operateTime90Percentile) }}</div>
            <div>{{ $t('systemlog.stdDev') }}: {{ formatNumber(scope.row.operateTimeStdDev) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.reportStabilityIndex')" min-width="120" prop="stabilityIndex" sortable>
          <template slot-scope="scope">
            <el-tag :type="getStabilityTagType(scope.row.stabilityIndex)">
              {{ scope.row.stabilityIndex }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredTableData.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import {getSystemLogOperateTypeList} from '@/api/systemlog'
import SystemLogCharts from '@/components/Chart/SystemLogCharts.vue'

export default {
  name: 'SystemLogReport',
  components: {
    SystemLogCharts
  },
  data() {
    return {
      loading: true,
      tableData: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10
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
          // 消息提示由后端统一控制
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
    formatNumber(value) {
      if (value === null || value === undefined) return '0.00'
      return parseFloat(value).toFixed(2)
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
    }
  }
}
</script>

<style lang="scss" scoped>
.system-log-report {
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

  ::v-deep .el-table .cell > div:not(:last-child) {
    margin-bottom: 2px;
  }
}
</style>
