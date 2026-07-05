<template>
  <div class="trace-container">
    <el-row :gutter="20" class="trace-main">
      <!-- 左侧：traceId列表 -->
      <el-col :span="7">
        <el-card class="trace-list-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="card-title">调用链列表</span>
            <el-button
              icon="el-icon-refresh"
              size="mini"
              type="text"
              @click="refreshTraceIds"
            >刷新</el-button>
          </div>
          <div v-loading="idsLoading" class="trace-ids-list">
            <div
              v-if="traceIds.length === 0 && !idsLoading"
              class="empty-tip"
            >
              <i class="el-icon-folder-opened" />
              <span>暂无trace记录</span>
            </div>
            <div
              v-for="(id, index) in traceIds"
              :key="index"
              :class="['trace-id-item', { active: selectedTraceId === id }]"
              @click="handleSelectTraceId(id)"
            >
              <i class="el-icon-connection" />
              <span class="id-text" :title="id">{{ id }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：调用链详情 -->
      <el-col :span="17">
        <el-card class="trace-detail-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="card-title">
              调用链详情
              <el-tag
                v-if="selectedTraceId"
                size="small"
                type="info"
              >{{ selectedTraceId }}</el-tag>
            </span>
            <div class="header-actions">
              <el-button
                v-if="selectedTraceId"
                icon="el-icon-copy-document"
                size="small"
                @click="handleCopyText"
              >复制文本</el-button>
              <el-button
                v-if="selectedTraceId"
                icon="el-icon-refresh"
                size="small"
                @click="refreshTraceDetail"
              >刷新</el-button>
            </div>
          </div>

          <div v-if="!selectedTraceId" class="empty-placeholder">
            <i class="el-icon-info" />
            <span>请在左侧选择一个traceId查看调用链</span>
          </div>

          <div v-else v-loading="detailLoading">
            <!-- 摘要信息 -->
            <div v-if="summary" class="summary-section">
              <el-row :gutter="16">
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-value">{{ summary.totalDurationMs != null ? summary.totalDurationMs + 'ms' : '-' }}</div>
                    <div class="summary-label">总耗时</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-value">{{ summary.nodeCount || 0 }}</div>
                    <div class="summary-label">调用节点数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-value">{{ summary.dubboCallCount || 0 }}</div>
                    <div class="summary-label">Dubbo调用数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-value">{{ summary.maxDepth || 0 }}</div>
                    <div class="summary-label">最大深度</div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- Tab切换 -->
            <el-tabs v-model="activeTab" type="border-card">
              <!-- 调用链文本 -->
              <el-tab-pane label="调用链" name="text">
                <pre v-if="treeText" class="tree-text">{{ treeText }}</pre>
                <div v-else class="empty-tab">暂无数据</div>
              </el-tab-pane>

              <!-- 调用树JSON -->
              <el-tab-pane label="调用树JSON" name="json">
                <pre v-if="treeJson" class="tree-json">{{ treeJson }}</pre>
                <div v-else class="empty-tab">暂无数据</div>
              </el-tab-pane>

              <!-- 节点详情 -->
              <el-tab-pane label="节点详情" name="nodes">
                <div v-if="flatNodes && flatNodes.length > 0" class="nodes-table">
                  <el-table
                    :data="flatNodes"
                    border
                    size="small"
                    stripe
                    style="width: 100%"
                    :row-class-name="tableRowClassName"
                  >
                    <el-table-column label="方法" min-width="200">
                      <template slot-scope="scope">
                        <span v-if="scope.row.getFullSignature()" class="method-name">{{ scope.row.getFullSignature() }}</span>
                        <span v-else-if="scope.row.httpMethod" class="method-name root-method">[入口] {{ scope.row.httpMethod }} {{ scope.row.httpUrl }}</span>
                        <el-tag v-if="scope.row.className" size="mini" type="warning" style="margin-left: 4px;">{{ scope.row.className }}</el-tag>
                        <el-tag v-if="scope.row.httpStatus" size="mini" :type="scope.row.httpStatus >= 400 ? 'danger' : 'success'" style="margin-left: 4px;">{{ scope.row.httpStatus }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="耗时" width="90" align="center">
                      <template slot-scope="scope">
                        <span :class="getDurationClass(scope.row)">{{ scope.row.durationMs }}ms</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="traceId" width="100">
                      <template slot-scope="scope">
                        <span class="trace-id-cell" :title="scope.row.traceId">{{ scope.row.traceId ? scope.row.traceId.substring(0, 8) : '' }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="线程" width="150">
                      <template slot-scope="scope">
                        <span class="thread-info">{{ scope.row.threadName }}<br><small>#{{ scope.row.threadId }}</small></span>
                      </template>
                    </el-table-column>
                    <el-table-column label="类型" width="70" align="center">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.dubboCall" size="mini" type="danger">dubbo</el-tag>
                        <el-tag v-else size="mini" type="info">local</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="服务/来源" width="140">
                      <template slot-scope="scope">
                        <div v-if="scope.row.serviceName" class="service-info">
                          <el-tag size="mini" type="info">{{ scope.row.serviceName }}</el-tag>
                        </div>
                        <div v-if="scope.row.remoteAddr" class="client-info">
                          <small>{{ scope.row.remoteAddr }}</small>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="参数" min-width="250">
                      <template slot-scope="scope">
                        <div v-if="scope.row.paramTypes" class="param-info">
                          <div class="param-types">{{ scope.row.paramTypes }}</div>
                          <div v-if="scope.row.argsDescription" class="param-values" :title="scope.row.argsDescription">
                            {{ scope.row.argsDescription.length > 150 ? scope.row.argsDescription.substring(0, 150) + '...' : scope.row.argsDescription }}
                          </div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="时间" width="180">
                      <template slot-scope="scope">
                        <div class="time-info">
                          <div>{{ formatTime(scope.row.availableTime) }}</div>
                          <div v-if="scope.row.disposeTime">→ {{ formatTime(scope.row.disposeTime) }}</div>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                <div v-else class="empty-tab">暂无数据</div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listTraceIds, getTraceSummary, getTraceTree } from '@/api/trace'

export default {
  name: 'TraceQuery',
  data() {
    return {
      traceIds: [],
      selectedTraceId: '',
      idsLoading: false,

      detailLoading: false,
      summary: null,
      treeText: '',
      treeJson: '',
      rawTree: null,
      flatNodes: [],
      activeTab: 'text'
    }
  },
  created() {
    this.refreshTraceIds()
  },
  methods: {
    refreshTraceIds() {
      this.idsLoading = true
      listTraceIds()
        .then(res => {
          this.traceIds = res.data || []
          if (this.selectedTraceId && !this.traceIds.includes(this.selectedTraceId)) {
            this.clearDetail()
          }
          this.idsLoading = false
        })
        .catch(() => {
          this.idsLoading = false
        })
    },

    handleSelectTraceId(id) {
      if (this.selectedTraceId === id) return
      this.selectedTraceId = id
      this.activeTab = 'text'
      this.loadTraceDetail()
    },

    refreshTraceDetail() {
      this.loadTraceDetail()
    },

    clearDetail() {
      this.selectedTraceId = ''
      this.summary = null
      this.treeText = ''
      this.treeJson = ''
      this.rawTree = null
      this.flatNodes = []
    },

    loadTraceDetail() {
      if (!this.selectedTraceId) return
      this.detailLoading = true
      this.summary = null
      this.treeText = ''
      this.treeJson = ''
      this.rawTree = null
      this.flatNodes = []

      Promise.all([
        getTraceSummary(this.selectedTraceId),
        getTraceTree(this.selectedTraceId)
      ]).then(([summaryRes, treeRes]) => {
        this.summary = summaryRes.data || null
        this.treeText = (summaryRes.data && summaryRes.data.text) || ''
        this.rawTree = treeRes.data || null
        this.treeJson = treeRes.data ? JSON.stringify(treeRes.data, null, 2) : ''
        this.flatNodes = this.flattenTree(treeRes.data)
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    },

    flattenTree(node, depth) {
      if (!node) return []
      depth = depth || 0
      var result = []
      var durationMs = node.disposeTime && node.availableTime
        ? new Date(node.disposeTime).getTime() - new Date(node.availableTime).getTime()
        : -1

      result.push({
        traceId: node.traceId,
        parentTraceId: node.parentTraceId,
        serviceName: node.serviceName,
        interfaceName: node.interfaceName,
        className: node.className,
        methodSignature: node.methodSignature,
        paramTypes: node.paramTypes,
        argsDescription: node.argsDescription,
        dubboCall: node.dubboCall,
        threadName: node.threadName,
        threadId: node.threadId,
        contextId: node.contextId,
        httpMethod: node.httpMethod,
        httpUrl: node.httpUrl,
        httpStatus: node.httpStatus,
        remoteAddr: node.remoteAddr,
        contentType: node.contentType,
        exceptionMessage: node.exceptionMessage,
        exceptionType: node.exceptionType,
        availableTime: node.availableTime,
        disposeTime: node.disposeTime,
        depth: depth,
        durationMs: durationMs,
        getFullSignature: function() {
          var name = this.interfaceName || this.className
          return name && this.methodSignature ? name + '.' + this.methodSignature : this.methodSignature || ''
        }
      })

      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          result = result.concat(this.flattenTree(node.children[i], depth + 1))
        }
      }
      return result
    },

    tableRowClassName(scope) {
      if (scope.row.dubboCall) return 'dubbo-row'
      return ''
    },

    getDurationClass(row) {
      if (row.durationMs > 1000) return 'duration-slow'
      if (row.durationMs > 200) return 'duration-medium'
      return 'duration-fast'
    },

    formatTime(time) {
      if (!time) return '-'
      return time
    },

    handleCopyText() {
      var text = this.activeTab === 'json' ? this.treeJson : this.treeText
      if (!text) return
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopy(text)
        })
      } else {
        this.fallbackCopy(text)
      }
    },

    fallbackCopy(text) {
      var textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.$message.success('已复制到剪贴板')
      } catch (e) {
        this.$message.error('复制失败')
      }
      document.body.removeChild(textarea)
    }
  }
}
</script>

<style lang="scss" scoped>
.trace-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .trace-main {
    height: calc(100vh - 120px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-weight: 600;
    font-size: 15px;
  }

  .trace-list-card {
    height: 100%;

    ::v-deep .el-card__body {
      padding: 0;
    }
  }

  .trace-ids-list {
    max-height: calc(100vh - 220px);
    overflow-y: auto;
  }

  .trace-id-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    transition: all 0.2s;
    user-select: none;

    i.el-icon-connection {
      margin-right: 8px;
      color: #909399;
      font-size: 16px;
      flex-shrink: 0;
    }

    .id-text {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 12px;
      font-family: monospace;
      color: #303133;
    }

    &:hover {
      background-color: #f5f7fa;

      i.el-icon-connection {
        color: #409eff;
      }
    }

    &.active {
      background-color: #ecf5ff;
      border-right: 3px solid #409eff;

      i.el-icon-connection {
        color: #409eff;
      }

      .id-text {
        color: #409eff;
        font-weight: 600;
      }
    }
  }

  .trace-detail-card {
    height: 100%;

    ::v-deep .el-card__body {
      padding: 0 20px 20px;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .empty-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 300px;
    color: #c0c4cc;

    i {
      font-size: 48px;
      margin-bottom: 12px;
    }

    span {
      font-size: 14px;
    }
  }

  .empty-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    color: #c0c4cc;

    i {
      font-size: 36px;
      margin-bottom: 8px;
    }

    span {
      font-size: 13px;
    }
  }

  .empty-tab {
    text-align: center;
    padding: 40px 0;
    color: #c0c4cc;
    font-size: 13px;
  }

  .summary-section {
    padding: 16px 0;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 16px;

    .summary-item {
      text-align: center;
      padding: 8px 0;

      .summary-value {
        font-size: 24px;
        font-weight: 700;
        color: #409eff;
        line-height: 1.2;
      }

      .summary-label {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  .tree-text,
  .tree-json {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 13px;
    line-height: 1.6;
    background-color: #fafafa;
    padding: 16px;
    border-radius: 4px;
    border: 1px solid #ebeef5;
    font-family: monospace;
    max-height: calc(100vh - 420px);
    overflow-y: auto;
  }

  .nodes-table {
    max-height: calc(100vh - 420px);
    overflow-y: auto;

    ::v-deep .el-table .dubbo-row {
      background-color: #fef0f0;
    }

    .method-name {
      font-weight: 600;
      font-size: 13px;

      &.root-method {
        color: #409eff;
      }
    }

    .service-info {
      margin-bottom: 2px;
    }

    .client-info {
      color: #909399;
    }

    .trace-id-cell {
      font-family: monospace;
      font-size: 12px;
    }

    .thread-info {
      font-size: 12px;
      color: #606266;
    }

    .param-info {
      font-size: 12px;

      .param-types {
        color: #909399;
        margin-bottom: 2px;
      }

      .param-values {
        color: #303133;
        font-family: monospace;
        word-break: break-all;
      }
    }

    .time-info {
      font-size: 12px;
      color: #606266;
    }

    .duration-fast {
      color: #67c23a;
    }

    .duration-medium {
      color: #e6a23c;
      font-weight: 600;
    }

    .duration-slow {
      color: #f56c6c;
      font-weight: 700;
    }
  }

  ::-webkit-scrollbar {
    width: 6px;
  }

  ::-webkit-scrollbar-thumb {
    background-color: #dcdfe6;
    border-radius: 3px;
  }

  ::-webkit-scrollbar-track {
    background-color: transparent;
  }
}
</style>
