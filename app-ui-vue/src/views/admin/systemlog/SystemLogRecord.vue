<template>
  <div class="system-log-record">
    <!-- 日志记录视图 - 搜索表单 -->
    <el-card class="search-card">
      <div slot="header" class="card-header">
        <span>{{ $t('systemlog.searchConditions') }}</span>
      </div>
      <el-form ref="searchForm" :model="searchForm" :inline="true" class="search-form">
        <el-form-item :label="$t('systemlog.operateType')" prop="operateTypes">
          <el-select
            v-model="searchForm.operateTypes"
            multiple
            collapse-tags
            filterable
            remote
            reserve-keyword
            :placeholder="$t('systemlog.selectOperateType')"
            :remote-method="handleOperateTypeSearch"
            :loading="operateTypeLoading"
            value-format="string"
            style="width: 280px"
          >
            <el-option
              v-for="item in operateTypeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('systemlog.operateTime')" prop="operateTime">
          <el-date-picker
            v-model="searchForm.operateTime"
            :end-placeholder="$t('systemlog.endDate')"
            :start-placeholder="$t('systemlog.startDate')"
            type="daterange"
            range-separator="→"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 360px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleRecordSearch">{{
            $t('common.search')
          }}
          </el-button>
          <el-button icon="el-icon-refresh-left" @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志记录视图 - 数据表格 -->
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>{{ $t('systemlog.recordList') }}</span>
        <div class="table-header-actions">
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click="fetchRecordData">{{
            $t('common.refresh')
          }}
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="recordLoading"
        :data="recordData"
        :element-loading-text="$t('systemlog.loadingText')"
        stripe
        style="width: 100%"
      >
        <el-table-column
          :label="$t('systemlog.operateType')"
          min-width="200"
          show-overflow-tooltip
          sortable
          prop="operateType"
        >
          <template slot-scope="scope">
            <div class="operate-type-cell">
              <span class="type-short">{{ formatOperateType(scope.row.operateType) }}</span>
              <span class="type-desc">{{ scope.row.operateDesc }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.requestInfo')" min-width="220" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag :type="getMethodTagType(scope.row.requestMethod)" size="mini" style="margin-right: 6px;">
              {{ scope.row.requestMethod }}
            </el-tag>
            <code class="url-text">{{ scope.row.requestUrl }}</code>
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.logRecordUser')" width="130" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ formatOperateUser(scope.row.operateUser) }}
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('systemlog.costTime')"
          width="100"
          align="center"
          sortable
          :sort-method="sortByCostTime"
        >
          <template slot-scope="scope">
            <el-tag :type="getCostTimeTagType(scope.row)" size="small" effect="plain">
              {{ calcCostTime(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.operateTime')" width="160" prop="operateTime" sortable>
          <template slot-scope="scope">
            {{ formatDate(scope.row.operateTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('systemlog.operateResult')" width="90" align="center">
          <template slot-scope="scope">
            <el-tooltip
              :disabled="!getResultTipContent(scope.row)"
              :content="getResultTipContent(scope.row)"
              placement="top"
            >
              <el-tag :type="getResultTagType(scope.row)" size="small">
                {{ getResultTagText(scope.row) }}
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="80" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="showLogDetail(scope.row)">{{
              $t('systemlog.detail')
            }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-show="recordTotal > 0"
          :current-page="recordPageNum"
          :page-size="recordPageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="recordTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleRecordSizeChange"
          @current-change="handleRecordPageChange"
        />
      </div>
    </el-card>

    <!-- 日志详情侧边栏 -->
    <el-drawer
      :visible.sync="detailDrawerVisible"
      :title="$t('systemlog.logDetail')"
      direction="rtl"
      size="60%"
      :before-close="handleDetailDrawerClose"
    >
      <div v-if="currentLogDetail" class="log-detail-content">
        <!-- 基本信息 -->
        <el-card class="detail-section" shadow="never">
          <div slot="header" class="section-header">
            <i class="el-icon-info" />
            <span>{{ $t('systemlog.basicInfo') }}</span>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item :label="$t('systemlog.operateType')" :span="2">
              <code>{{ currentLogDetail.operateType }}</code>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.operateDesc')">
              {{ currentLogDetail.operateDesc }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.operateResult')">
              <el-tag :type="getResultTagType(currentLogDetail)" size="small">
                {{ getResultTagText(currentLogDetail) }}
              </el-tag>
              <div v-if="getResultTipContent(currentLogDetail)" class="result-fail-message">
                {{ getResultTipContent(currentLogDetail) }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.operateUser')">
              {{ formatOperateUser(currentLogDetail.operateUser) }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.operateIp')">
              {{ currentLogDetail.operateIp }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.operateTime')">
              {{ formatDate(currentLogDetail.operateTime) }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.costTime')">
              <el-tag :type="getCostTimeTagType(currentLogDetail)" size="small" effect="plain">
                {{ calcCostTime(currentLogDetail) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 请求信息 -->
        <el-card class="detail-section" shadow="never">
          <div slot="header" class="section-header">
            <i class="el-icon-download" />
            <span>{{ $t('systemlog.requestInfo') }}</span>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="$t('systemlog.requestUrl')">
              <code>{{ currentLogDetail.requestUrl }}</code>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('systemlog.requestMethod')">
              <el-tag :type="getMethodTagType(currentLogDetail.requestMethod)" size="small">
                {{ currentLogDetail.requestMethod }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 参数与结果 -->
        <el-card class="detail-section" shadow="never">
          <div slot="header" class="section-header">
            <i class="el-icon-document" />
            <span>{{ $t('systemlog.paramResult') }}</span>
          </div>
          <el-tabs type="border-card">
            <el-tab-pane :label="$t('systemlog.operateParam')">
              <div class="json-content">
                <pre>{{ formatJson(currentLogDetail.operateParam) }}</pre>
              </div>
            </el-tab-pane>
            <el-tab-pane :label="$t('systemlog.responseInfo')">
              <div class="json-content">
                <pre>{{ formatJson(currentLogDetail.operateResult) }}</pre>
              </div>
            </el-tab-pane>
            <el-tab-pane v-if="currentLogDetail.errorMessage" :label="$t('systemlog.errorMessage')">
              <div class="json-content error-content">
                <pre>{{ currentLogDetail.errorMessage }}</pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getOperateTypeList, getSystemLogRecordList } from '@/api/systemlog'

export default {
  name: 'SystemLogRecord',
  data() {
    return {
      // 日志记录视图数据
      recordLoading: false,
      recordData: [],
      recordTotal: 0,
      recordPageNum: 1,
      recordPageSize: 10,
      // 操作类型搜索相关
      operateTypeLoading: false,
      operateTypeOptions: [],
      // 日志详情侧边栏
      detailDrawerVisible: false,
      currentLogDetail: null,
      searchForm: {
        operateTypes: [],
        operateTime: null,
        operateTimeBegin: null,
        operateTimeEnd: null
      }
    }
  },
  created() {
    this.fetchRecordData()
  },
  methods: {
    handleRecordSearch() {
      this.recordPageNum = 1
      this.fetchRecordData()
    },
    async handleOperateTypeSearch(query) {
      // 远程搜索:根据关键词调用后端接口获取操作类型
      if (!query || query.trim() === '') {
        // 空关键词不搜索,返回空数组(避免初始化时显示大量数据)
        this.operateTypeOptions = []
        return
      }

      this.operateTypeLoading = true
      try {
        const response = await getOperateTypeList(query)
        this.operateTypeOptions = response.data || []
      } catch (error) {
        console.error('获取操作类型列表失败:', error)
        this.operateTypeOptions = []
      } finally {
        this.operateTypeLoading = false
      }
    },
    fetchRecordData() {
      this.recordLoading = true
      const form = {
        operateTypes: this.searchForm.operateTypes,
        operateTimeBegin: this.searchForm.operateTimeBegin,
        operateTimeEnd: this.searchForm.operateTimeEnd,
        pageNum: this.recordPageNum,
        pageSize: this.recordPageSize
      }
      getSystemLogRecordList(form)
        .then(response => {
          if (response.data) {
            this.recordData = response.data.data || []
            this.recordTotal = response.data.total || 0
          }
          this.recordLoading = false
        })
        .catch(error => {
          console.error('获取日志记录列表失败:', error)
          this.recordLoading = false
        })
    },
    handleDateChange(value) {
      if (value && value.length === 2) {
        this.searchForm.operateTimeBegin = value[0]
        this.searchForm.operateTimeEnd = value[1]
      } else {
        this.searchForm.operateTimeBegin = null
        this.searchForm.operateTimeEnd = null
      }
    },
    handleRecordSizeChange(val) {
      this.recordPageSize = val
      this.recordPageNum = 1
      this.fetchRecordData()
    },
    handleRecordPageChange(val) {
      this.recordPageNum = val
      this.fetchRecordData()
    },
    resetSearchForm() {
      this.searchForm = {
        operateTypes: [],
        operateTime: null,
        operateTimeBegin: null,
        operateTimeEnd: null
      }
    },
    handleReset() {
      this.resetSearchForm()
      this.recordPageNum = 1
      this.fetchRecordData()
    },
    // ========== 日志详情侧边栏方法 ==========
    showLogDetail(row) {
      this.currentLogDetail = { ...row } // 创建副本避免响应式问题
      this.detailDrawerVisible = true
    },
    handleDetailDrawerClose() {
      this.detailDrawerVisible = false
      this.currentLogDetail = null
    },
    // ========== 格式化方法 ==========
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    // ========== 运行结果状态判定(与后端契约对齐) ==========
    // 后端契约:
    // 1. 方法正常返回时,operateResult为控制器原始返回值的JSON;
    //    返回AjaxResult时结构为{map:{code,message,data}},仅code=200代表成功
    // 2. 方法抛出异常时,operateResult为空,errorMessage记录异常类名与堆栈
    parseResultJson(resultStr) {
      if (!resultStr) return null
      try {
        const parsed = JSON.parse(resultStr)
        return parsed && typeof parsed === 'object' ? parsed : null
      } catch (e) {
        return null
      }
    },
    getOperateStatus(row) {
      // 存在异常信息即为失败
      if (row.errorMessage) return 'fail'
      // 标准AjaxResult格式:按code判定成败
      const result = this.parseResultJson(row.operateResult)
      if (result && result.map && result.map.code !== undefined && result.map.code !== null) {
        return Number(result.map.code) === 200 ? 'success' : 'fail'
      }
      // void方法无返回值、或非标准返回(如token对象),方法正常执行完毕,视为成功
      return 'success'
    },
    getResultTagType(row) {
      return this.getOperateStatus(row) === 'success' ? 'success' : 'danger'
    },
    getResultTagText(row) {
      return this.getOperateStatus(row) === 'success'
        ? this.$t('systemlog.resultSuccess')
        : this.$t('systemlog.resultFail')
    },
    // 失败原因:业务失败取后端message,异常失败取异常摘要;成功时返回空串(不展示提示)
    getResultTipContent(row) {
      if (this.getOperateStatus(row) === 'success') return ''
      if (row.errorMessage) {
        // errorMessage格式为"异常类名:完整堆栈",截取首行作为摘要
        const summary = row.errorMessage.split('\n')[0]
        return summary.length > 100 ? summary.substring(0, 100) + '...' : summary
      }
      const result = this.parseResultJson(row.operateResult)
      if (result && result.map) {
        return result.map.message || `code: ${result.map.code}`
      }
      return ''
    },
    formatJson(jsonStr) {
      if (!jsonStr) return '-'
      try {
        // 尝试解析为JSON对象
        const parsed = JSON.parse(jsonStr)
        return JSON.stringify(parsed, null, 2)
      } catch (e) {
        // 如果不是有效的JSON,直接返回原字符串
        return jsonStr
      }
    },
    getMethodTagType(method) {
      if (!method) return 'info'
      const upper = method.toUpperCase()
      if (upper === 'GET') return 'success'
      if (upper === 'POST') return 'primary'
      if (upper === 'PUT') return 'warning'
      if (upper === 'DELETE') return 'danger'
      return 'info'
    },
    formatOperateType(type) {
      // 从完整类名提取短名: com.tbs.app.controller.publics.PublicUserController.login -> PublicUserController.login
      if (!type) return '-'
      const parts = type.split('.')
      if (parts.length >= 2) {
        return parts.slice(-2).join('.')
      }
      return type
    },
    formatOperateUser(user) {
      if (!user) return '-'
      if (user.startsWith('NULL USERNAME:')) {
        return this.$t('systemlog.anonymousUser')
      }
      return user
    },
    calcCostTime(row) {
      if (row.costTime == null) return '-'
      return row.costTime + 'ms'
    },
    getCostTimeTagType(row) {
      if (row.costTime == null) return 'info'
      if (row.costTime <= 500) return 'success'
      if (row.costTime <= 2000) return 'warning'
      return 'danger'
    },
    sortByCostTime(a, b) {
      return (a.costTime || 0) - (b.costTime || 0)
    }
  }
}
</script>

<style lang="scss" scoped>
.system-log-record {
  .search-card {
    margin-bottom: 20px;

    .card-header {
      font-weight: 500;
    }

    .search-form {
      padding: 5px 0;

      ::v-deep .el-form-item {
        margin-bottom: 16px;
        margin-right: 20px;
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

  .operate-type-cell {
    .type-short {
      font-weight: 500;
      color: #303133;
      display: block;
    }

    .type-desc {
      font-size: 12px;
      color: #909399;
      display: block;
    }
  }

  .url-text {
    font-size: 12px;
    color: #606266;
    background: #f5f7fa;
    padding: 2px 4px;
    border-radius: 3px;
  }

  .result-fail-message {
    margin-top: 6px;
    font-size: 12px;
    color: #f56c6c;
    word-break: break-all;
    white-space: pre-wrap;
  }
}

.log-detail-content {
  padding: 20px;

  .detail-section {
    margin-bottom: 20px;

    .section-header {
      display: flex;
      align-items: center;
      font-weight: 500;

      i {
        margin-right: 8px;
        font-size: 16px;
      }
    }
  }

  .json-content {
    max-height: 400px;
    overflow: auto;

    pre {
      background-color: #f5f7fa;
      padding: 12px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-wrap: break-word;
      margin: 0;
    }
  }

  .error-content pre {
    color: #f56c6c;
    background-color: #fef0f0;
  }

  ::v-deep .el-descriptions-item__label {
    font-weight: 500;
  }
}
</style>
