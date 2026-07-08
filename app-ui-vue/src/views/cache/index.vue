<template>
  <div class="cache-container">
    <el-row :gutter="20" class="cache-main">
      <!-- 左侧：缓存名称列表 -->
      <el-col :span="7">
        <el-card class="cache-names-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="card-title">缓存名称</span>
            <el-button
              icon="el-icon-refresh"
              size="mini"
              type="text"
              @click="refreshCacheNames"
            >刷新</el-button>
          </div>
          <div v-loading="namesLoading" class="cache-names-list">
            <div
              v-if="cacheNames.length === 0 && !namesLoading"
              class="empty-tip"
            >
              <i class="el-icon-folder-opened" />
              <span>暂无缓存数据</span>
            </div>
            <div
              v-for="(name, index) in cacheNames"
              :key="index"
              :class="['cache-name-item', { active: selectedCacheName === name }]"
              @click="handleSelectCacheName(name)"
            >
              <i class="el-icon-coin" />
              <span class="name-text" :title="name">{{ name }}</span>
              <el-button
                class="clear-btn"
                icon="el-icon-delete"
                size="mini"
                title="清除此缓存"
                type="text"
                @click.stop="handleClearCache(name)"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：缓存键列表 -->
      <el-col :span="17">
        <el-card class="cache-keys-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="card-title">
              缓存键列表
              <el-tag
                v-if="selectedCacheName"
                size="small"
                type="info"
              >{{ selectedCacheName }}</el-tag>
            </span>
            <div class="header-actions">
              <el-input
                v-model="keySearchKeyword"
                clearable
                placeholder="搜索缓存键..."
                size="small"
                style="width: 200px; margin-right: 10px;"
                @clear="handleKeySearch"
                @keyup.enter.native="handleKeySearch"
              >
                <i slot="prefix" class="el-input__icon el-icon-search" />
              </el-input>
              <el-button
                icon="el-icon-refresh"
                size="small"
                @click="refreshCacheKeys"
              >刷新</el-button>
            </div>
          </div>

          <div v-if="!selectedCacheName" class="empty-placeholder">
            <i class="el-icon-info" />
            <span>请在左侧选择一个缓存名称</span>
          </div>

          <div v-else v-loading="keysLoading">
            <el-table
              :data="filteredCacheKeys"
              border
              empty-text="该缓存下暂无数据"
              stripe
              style="width: 100%"
            >
              <el-table-column label="序号" width="70" type="index" :index="indexMethod" />
              <el-table-column
                label="缓存键"
                min-width="300"
                prop="key"
                show-overflow-tooltip
              />
              <el-table-column label="操作" width="160" fixed="right">
                <template slot-scope="scope">
                  <el-button
                    icon="el-icon-view"
                    size="mini"
                    type="text"
                    @click="handleViewValue(scope.row)"
                  >查看</el-button>
                  <el-button
                    icon="el-icon-delete"
                    size="mini"
                    type="text"
                    @click="handleEvictKey(scope.row)"
                  >删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="cacheKeys.length > 0" class="key-count-tip">
              共 {{ filteredCacheKeys.length }} 个键
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查看缓存值对话框 -->
    <el-dialog
      :visible.sync="valueDialogVisible"
      title="缓存值详情"
      width="650px"
      top="8vh"
    >
      <div v-loading="valueLoading" class="cache-value-detail">
        <el-descriptions :column="1" border size="medium">
          <el-descriptions-item label="缓存名称">
            <el-tag>{{ valueDetail.cacheName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="缓存键">
            {{ valueDetail.key }}
          </el-descriptions-item>
          <el-descriptions-item label="缓存值">
            <div class="value-content">
              <pre v-if="valueDetail.value !== null && valueDetail.value !== undefined">{{ formatValue(valueDetail.value) }}</pre>
              <span v-else class="null-value">null</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button
          icon="el-icon-copy-document"
          size="small"
          @click="handleCopyValue"
        >复制值</el-button>
        <el-button size="small" @click="valueDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listCacheNames, getCacheKeys, getCache, evictCache } from '@/api/cache'

export default {
  name: 'CacheManage',
  data() {
    return {
      // 缓存名称相关
      cacheNames: [],
      selectedCacheName: '',
      namesLoading: false,

      // 缓存键相关
      cacheKeys: [],
      keysLoading: false,
      keySearchKeyword: '',

      // 缓存值详情
      valueDialogVisible: false,
      valueLoading: false,
      valueDetail: {
        cacheName: '',
        key: '',
        value: null
      }
    }
  },
  computed: {
    filteredCacheKeys() {
      if (!this.keySearchKeyword) {
        return this.cacheKeys.map(k => ({ key: k }))
      }
      const keyword = this.keySearchKeyword.toLowerCase()
      return this.cacheKeys
        .filter(k => k.toLowerCase().includes(keyword))
        .map(k => ({ key: k }))
    }
  },
  created() {
    this.refreshCacheNames()
  },
  methods: {
    // ===== 缓存名称相关 =====
    refreshCacheNames() {
      this.namesLoading = true
      listCacheNames()
        .then(res => {
          this.cacheNames = res.data || []
          // 如果当前选中的缓存不在列表中，清除选中
          if (this.selectedCacheName && !this.cacheNames.includes(this.selectedCacheName)) {
            this.selectedCacheName = ''
            this.cacheKeys = []
          }
          this.namesLoading = false
        })
        .catch(() => {
          this.namesLoading = false
        })
    },

    handleSelectCacheName(name) {
      if (this.selectedCacheName === name) return
      this.selectedCacheName = name
      this.keySearchKeyword = ''
      this.loadCacheKeys()
    },

    handleClearCache(name) {
      this.$confirm(
        `确定要清除缓存「${name}」下的所有数据吗？此操作不可恢复！`,
        '清除确认',
        {
          confirmButtonText: '确定清除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        evictCache(name)
          .then(() => {
            // 消息提示由后端统一控制
            if (this.selectedCacheName === name) {
              this.cacheKeys = []
              this.selectedCacheName = ''
            }
          })
          .catch(() => {})
      }).catch(() => {})
    },

    // ===== 缓存键相关 =====
    loadCacheKeys() {
      if (!this.selectedCacheName) return
      this.keysLoading = true
      getCacheKeys(this.selectedCacheName)
        .then(res => {
          this.cacheKeys = res.data || []
          this.keysLoading = false
        })
        .catch(() => {
          this.cacheKeys = []
          this.keysLoading = false
        })
    },

    refreshCacheKeys() {
      this.loadCacheKeys()
    },

    handleKeySearch() {
      // 搜索通过 computed 自动过滤，这里仅作为搜索触发点
    },

    indexMethod(index) {
      return index + 1
    },

    // ===== 缓存值操作 =====
    handleViewValue(row) {
      this.valueDialogVisible = true
      this.valueLoading = true
      this.valueDetail = {
        cacheName: this.selectedCacheName,
        key: row.key,
        value: null
      }

      getCache(this.selectedCacheName, row.key)
        .then(res => {
          this.valueDetail.value = res.data
          this.valueLoading = false
        })
        .catch(() => {
          this.valueLoading = false
        })
    },

    handleEvictKey(row) {
      this.$confirm(
        `确定要删除缓存键「${row.key}」吗？`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        evictCache(this.selectedCacheName, row.key)
          .then(() => {
            // 消息提示由后端统一控制
            this.loadCacheKeys()
          })
          .catch(() => {})
      }).catch(() => {})
    },

    handleCopyValue() {
      const valueStr = this.formatValue(this.valueDetail.value)
      if (navigator.clipboard) {
        navigator.clipboard.writeText(valueStr).then(() => {
          // 消息提示由后端统一控制
        }).catch(() => {
          this.fallbackCopy(valueStr)
        })
      } else {
        this.fallbackCopy(valueStr)
      }
    },

    fallbackCopy(text) {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        // 消息提示由后端统一控制
      } catch (e) {
        // 消息提示由后端统一控制
      }
      document.body.removeChild(textarea)
    },

    formatValue(value) {
      if (value === null || value === undefined) return 'null'
      if (typeof value === 'object') {
        try {
          return JSON.stringify(value, null, 2)
        } catch (e) {
          return String(value)
        }
      }
      return String(value)
    }
  }
}
</script>

<style lang="scss" scoped>
.cache-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .cache-main {
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

  // 左侧缓存名称卡片
  .cache-names-card {
    height: 100%;

    ::v-deep .el-card__body {
      padding: 0;
    }
  }

  .cache-names-list {
    max-height: calc(100vh - 220px);
    overflow-y: auto;
  }

  .cache-name-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    transition: all 0.2s;
    user-select: none;

    i.el-icon-coin {
      margin-right: 8px;
      color: #909399;
      font-size: 16px;
      flex-shrink: 0;
    }

    .name-text {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 13px;
      color: #303133;
    }

    .clear-btn {
      opacity: 0;
      transition: opacity 0.2s;
      flex-shrink: 0;
    }

    &:hover {
      background-color: #f5f7fa;

      .clear-btn {
        opacity: 1;
        color: #f56c6c;
      }
    }

    &.active {
      background-color: #ecf5ff;
      border-right: 3px solid #409eff;

      i.el-icon-coin {
        color: #409eff;
      }

      .name-text {
        color: #409eff;
        font-weight: 600;
      }

      .clear-btn {
        opacity: 1;
        color: #f56c6c;
      }
    }
  }

  // 右侧缓存键卡片
  .cache-keys-card {
    height: 100%;

    .header-actions {
      display: flex;
      align-items: center;
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

  .key-count-tip {
    margin-top: 12px;
    text-align: right;
    color: #909399;
    font-size: 12px;
  }

  // 缓存值详情
  .cache-value-detail {
    .value-content {
      max-height: 400px;
      overflow-y: auto;

      pre {
        margin: 0;
        white-space: pre-wrap;
        word-break: break-all;
        font-size: 13px;
        line-height: 1.6;
        background-color: #fafafa;
        padding: 12px;
        border-radius: 4px;
        border: 1px solid #ebeef5;
      }
    }

    .null-value {
      color: #c0c4cc;
      font-style: italic;
    }
  }

  // 滚动条美化
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
