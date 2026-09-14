<template>
  <div class="download-code-panel">
    <!-- 统计概览：语义色卡 + 悬停抬升 -->
    <div class="stat-row">
      <div
        v-for="card in statCards"
        :key="card.key"
        :class="['stat-card', card.className]"
      >
        <div class="stat-icon"><i :class="card.icon" /></div>
        <div class="stat-body">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ $t(card.labelKey) }}</div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="panel-toolbar">
      <el-input
        v-model="query.keyword"
        :placeholder="$t('oss.codeSearchPlaceholder')"
        clearable
        size="small"
        style="width: 300px;"
        @clear="handleSearch"
        @keyup.enter.native="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch" />
      </el-input>
      <el-select
        v-model="query.status"
        :placeholder="$t('oss.filterAll')"
        clearable
        size="small"
        style="width: 150px; margin-left: 10px;"
        @change="handleSearch"
      >
        <el-option :label="$t('oss.statusValid')" value="VALID" />
        <el-option :label="$t('oss.statusExpired')" value="EXPIRED" />
        <el-option :label="$t('oss.statusPermanent')" value="PERMANENT" />
      </el-select>
      <el-button icon="el-icon-refresh" size="small" style="margin-left: 10px;" @click="loadAll">{{ $t('oss.refresh') }}</el-button>
      <div class="toolbar-spacer" />
      <el-button icon="el-icon-key" size="small" type="primary" @click="openCreateDrawer()">{{ $t('oss.createCode') }}</el-button>
    </div>

    <!-- 下载码列表 -->
    <el-table v-loading="loading" :data="rows" border stripe style="width: 100%">
      <el-table-column :label="$t('oss.codeResult')" width="210">
        <template slot-scope="scope">
          <span class="code-text">{{ scope.row.code }}</span>
          <el-button
            class="code-copy"
            icon="el-icon-copy-document"
            size="mini"
            type="text"
            @click="copyText(scope.row.code)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.objectKey')" min-width="220" show-overflow-tooltip>
        <template slot-scope="scope">
          <span class="plain-text">{{ scope.row.objectKey }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.remark')" min-width="120" show-overflow-tooltip>
        <template slot-scope="scope">
          <span class="remark-text">{{ scope.row.remark || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.codeStatus')" width="104" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row)" size="mini" effect="dark">{{ statusLabel(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.createdAt')" width="164">
        <template slot-scope="scope">
          <span class="time-text">{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.codeExpireAt')" width="164">
        <template slot-scope="scope">
          <span class="time-text">{{ isPermanent(scope.row) ? '-' : formatTime(scope.row.expireAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.remaining')" width="180">
        <template slot-scope="scope">
          <div v-if="isPermanent(scope.row)" class="permanent-text">
            <i class="el-icon-time" /> {{ $t('oss.statusPermanent') }}
          </div>
          <template v-else-if="isExpired(scope.row)">
            <div class="expired-text"><i class="el-icon-warning-outline" /> {{ $t('oss.statusExpired') }}</div>
          </template>
          <template v-else>
            <el-progress
              :percentage="remainPercent(scope.row)"
              :color="remainColor(scope.row)"
              :stroke-width="8"
              :show-text="false"
            />
            <span class="remain-text">{{ remainText(scope.row) }}</span>
          </template>
        </template>
      </el-table-column>
      <el-table-column :label="$t('oss.operation')" width="236" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button icon="el-icon-link" size="mini" type="text" @click="copyPublicUrl(scope.row)">{{ $t('oss.copyLink') }}</el-button>
          <el-button icon="el-icon-refresh-right" size="mini" type="text" @click="openRenewDrawer(scope.row)">{{ $t('oss.renewCode') }}</el-button>
          <el-button icon="el-icon-delete" size="mini" type="text" class="danger-text" @click="removeCode(scope.row)">{{ $t('oss.delete') }}</el-button>
        </template>
      </el-table-column>
      <template slot="empty">
        <div class="empty-block">
          <i class="el-icon-key empty-icon" />
          <p class="empty-title">{{ $t('oss.emptyCode') }}</p>
          <p class="empty-tip">{{ $t('oss.emptyCodeTip') }}</p>
          <el-button icon="el-icon-key" plain size="small" type="primary" @click="openCreateDrawer()">{{ $t('oss.createCode') }}</el-button>
        </div>
      </template>
    </el-table>

    <!-- 底部：统计与分页 -->
    <div class="panel-footer">
      <span class="count-tip">{{ $t('oss.codeCountTip', { count: total }) }}</span>
      <el-pagination
        :current-page.sync="query.pageNum"
        :page-size.sync="query.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        background
        layout="sizes, prev, pager, next"
        @current-change="loadPage"
        @size-change="handleSearch"
      />
    </div>

    <!-- 新建下载码抽屉 -->
    <el-drawer
      :title="$t('oss.createCode')"
      :visible.sync="createVisible"
      :close-on-click-modal="false"
      size="480px"
    >
      <div class="drawer-body">
        <el-form label-width="96px" size="small">
          <el-form-item :label="$t('oss.objectKey')">
            <el-input v-model="createForm.objectKey" :placeholder="$t('oss.objectKeyPlaceholder')" clearable />
          </el-form-item>
          <el-form-item :label="$t('oss.codeMode')">
            <el-radio-group v-model="createForm.mode">
              <el-radio-button label="auto">{{ $t('oss.modeAuto') }}</el-radio-button>
              <el-radio-button label="custom">{{ $t('oss.modeCustom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="createForm.mode === 'custom'" :label="$t('oss.customCode')">
            <el-input
              v-model="createForm.code"
              :placeholder="$t('oss.customCodePlaceholder')"
              clearable
              maxlength="64"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('oss.codeExpire')">
            <el-radio-group v-model="createForm.expireMode">
              <el-radio-button label="never">{{ $t('oss.neverExpire') }}</el-radio-button>
              <el-radio-button label="day">{{ $t('oss.oneDay') }}</el-radio-button>
              <el-radio-button label="week">{{ $t('oss.sevenDays') }}</el-radio-button>
              <el-radio-button label="custom">{{ $t('oss.custom') }}</el-radio-button>
            </el-radio-group>
            <el-input-number
              v-if="createForm.expireMode === 'custom'"
              v-model="createForm.expireSeconds"
              :min="1"
              size="small"
              style="margin-top: 8px;"
            />
          </el-form-item>
          <el-form-item :label="$t('oss.remark')">
            <el-input v-model="createForm.remark" :placeholder="$t('oss.remarkPlaceholder')" clearable maxlength="100" />
          </el-form-item>
        </el-form>

        <div v-if="createResult" class="result-card">
          <div class="result-title"><i class="el-icon-success" /> {{ $t('oss.createSuccess') }}</div>
          <div class="result-item">
            <span class="result-label">{{ $t('oss.codeResult') }}</span>
            <span class="code-text">{{ createResult.code }}</span>
            <el-button icon="el-icon-copy-document" size="mini" type="text" @click="copyText(createResult.code)" />
          </div>
          <div class="result-item">
            <span class="result-label">{{ $t('oss.publicUrl') }}</span>
            <el-input :value="publicUrl" readonly type="textarea" :rows="2" />
          </div>
          <div class="result-item">
            <span class="result-label">{{ $t('oss.codeExpireAt') }}</span>
            <span class="time-text">{{ isPermanent(createResult) ? $t('oss.statusPermanent') : formatTime(createResult.expireAt) }}</span>
          </div>
          <el-button icon="el-icon-copy-document" plain size="small" type="primary" @click="copyText(publicUrl)">{{ $t('oss.copyLink') }}</el-button>
        </div>
      </div>
      <div class="drawer-footer">
        <el-button size="small" @click="createVisible = false">{{ $t('oss.close') }}</el-button>
        <el-button :loading="createLoading" icon="el-icon-key" size="small" type="primary" @click="submitCreate">{{ $t('oss.generateCode') }}</el-button>
      </div>
    </el-drawer>

    <!-- 续期抽屉 -->
    <el-drawer :title="$t('oss.renewTitle')" :visible.sync="renewVisible" size="440px">
      <div class="drawer-body">
        <div class="renew-summary">
          <div class="summary-row">
            <span class="result-label">{{ $t('oss.codeResult') }}</span>
            <span class="code-text">{{ renewForm.code }}</span>
          </div>
          <div class="summary-row">
            <span class="result-label">{{ $t('oss.objectKey') }}</span>
            <span class="plain-text summary-key">{{ renewForm.objectKey }}</span>
          </div>
          <div class="summary-row">
            <span class="result-label">{{ $t('oss.currentExpireAt') }}</span>
            <span class="time-text">{{ renewForm.permanent ? $t('oss.statusPermanent') : formatTime(renewForm.expireAt) }}</span>
          </div>
        </div>
        <el-form label-width="96px" size="small">
          <el-form-item :label="$t('oss.newExpireAt')">
            <el-radio-group v-model="renewForm.mode">
              <el-radio-button label="never">{{ $t('oss.neverExpire') }}</el-radio-button>
              <el-radio-button label="day">{{ $t('oss.oneDay') }}</el-radio-button>
              <el-radio-button label="week">{{ $t('oss.sevenDays') }}</el-radio-button>
              <el-radio-button label="custom">{{ $t('oss.custom') }}</el-radio-button>
            </el-radio-group>
            <el-input-number
              v-if="renewForm.mode === 'custom'"
              v-model="renewForm.seconds"
              :min="1"
              size="small"
              style="margin-top: 8px;"
            />
          </el-form-item>
          <el-form-item :label="$t('oss.remark')">
            <el-input v-model="renewForm.remark" :placeholder="$t('oss.remarkPlaceholder')" clearable maxlength="100" />
          </el-form-item>
        </el-form>
      </div>
      <div class="drawer-footer">
        <el-button size="small" @click="renewVisible = false">{{ $t('oss.close') }}</el-button>
        <el-button :loading="renewSaving" icon="el-icon-refresh-right" size="small" type="primary" @click="submitRenew">{{ $t('oss.renewCode') }}</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  createDownloadCode,
  updateDownloadCode,
  deleteDownloadCode,
  queryDownloadCodePage,
  getDownloadCodeStatistics,
  buildPublicDownloadUrl
} from '@/api/oss'

/**
 * 下载码管理面板
 * <p>
 * OSS 页内「下载码管理」Tab：统计概览、条件筛选与分页列表、续期抽屉、新建抽屉；
 * 支持自定义 4~64 位 Base64 码并展示免认证公开下载链接
 * </p>
 */
export default {
  name: 'DownloadCodePanel',
  data() {
    return {
      // 查询与分页
      query: { keyword: '', status: '', pageNum: 1, pageSize: 10 },
      rows: [],
      total: 0,
      loading: false,

      // 统计概览
      statistics: { total: 0, valid: 0, expired: 0, permanent: 0 },

      // 新建抽屉
      createVisible: false,
      createLoading: false,
      createForm: { objectKey: '', mode: 'auto', code: '', expireMode: 'never', expireSeconds: 86400, remark: '' },
      createResult: null,

      // 续期抽屉
      renewVisible: false,
      renewSaving: false,
      renewForm: { code: '', objectKey: '', expireAt: 0, permanent: false, remark: '', mode: 'day', seconds: 86400 }
    }
  },
  computed: {
    // 统计卡配置（语义色与图标）
    statCards() {
      return [
        { key: 'total', className: 'stat-total', icon: 'el-icon-key', labelKey: 'oss.statTotal', value: this.statistics.total },
        { key: 'valid', className: 'stat-valid', icon: 'el-icon-circle-check', labelKey: 'oss.statusValid', value: this.statistics.valid },
        { key: 'expired', className: 'stat-expired', icon: 'el-icon-warning-outline', labelKey: 'oss.statusExpired', value: this.statistics.expired },
        { key: 'permanent', className: 'stat-permanent', icon: 'el-icon-time', labelKey: 'oss.statusPermanent', value: this.statistics.permanent }
      ]
    },
    // 当前新建结果的公开下载地址
    publicUrl() {
      return this.createResult ? buildPublicDownloadUrl(this.createResult.code) : ''
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    // ===== 列表与统计 =====
    loadAll() {
      this.loadPage()
      this.loadStatistics()
    },

    loadPage() {
      this.loading = true
      const form = { pageNum: this.query.pageNum, pageSize: this.query.pageSize }
      if (this.query.keyword) {
        form.keyword = this.query.keyword
      }
      if (this.query.status) {
        form.status = this.query.status
      }
      queryDownloadCodePage(form)
        .then(res => {
          const page = res.data || {}
          this.rows = page.data || []
          this.total = page.total || 0
          this.loading = false
        })
        .catch(() => {
          this.rows = []
          this.total = 0
          this.loading = false
        })
    },

    loadStatistics() {
      getDownloadCodeStatistics()
        .then(res => {
          this.statistics = res.data || { total: 0, valid: 0, expired: 0, permanent: 0 }
        })
        .catch(() => {})
    },

    handleSearch() {
      this.query.pageNum = 1
      this.loadPage()
    },

    // ===== 新建 =====
    openCreateDrawer(objectKey) {
      this.createForm = { objectKey: objectKey || '', mode: 'auto', code: '', expireMode: 'never', expireSeconds: 86400, remark: '' }
      this.createResult = null
      this.createVisible = true
    },

    submitCreate() {
      const form = this.createForm
      if (!form.objectKey) {
        this.$message.warning(this.$t('oss.objectKeyPlaceholder'))
        return
      }
      if (form.mode === 'custom' && !/^[A-Za-z0-9_-]{4,64}$/.test(form.code || '')) {
        this.$message.warning(this.$t('oss.codeInvalid'))
        return
      }
      const payload = { objectKey: form.objectKey }
      if (form.mode === 'custom') {
        payload.code = form.code
      }
      const expireSeconds = this.resolveExpireSeconds(form.expireMode, form.expireSeconds)
      if (expireSeconds !== undefined) {
        payload.expireSeconds = expireSeconds
      }
      if (form.remark) {
        payload.remark = form.remark
      }
      this.createLoading = true
      createDownloadCode(payload)
        .then(res => {
          this.createResult = res.data || null
          this.createLoading = false
          this.$message.success(this.$t('oss.createSuccess'))
          this.loadAll()
        })
        .catch(() => {
          this.createLoading = false
        })
    },

    // ===== 续期与备注 =====
    openRenewDrawer(row) {
      this.renewForm = {
        code: row.code,
        objectKey: row.objectKey,
        expireAt: row.expireAt,
        permanent: this.isPermanent(row),
        remark: row.remark || '',
        mode: 'day',
        seconds: 86400
      }
      this.renewVisible = true
    },

    submitRenew() {
      const form = this.renewForm
      const expireSeconds = this.resolveExpireSeconds(form.mode, form.seconds, true)
      this.renewSaving = true
      updateDownloadCode(form.code, { expireSeconds, remark: form.remark })
        .then(() => {
          this.renewSaving = false
          this.renewVisible = false
          this.$message.success(this.$t('oss.renewSuccess'))
          this.loadAll()
        })
        .catch(() => {
          this.renewSaving = false
        })
    },

    removeCode(row) {
      this.$confirm(
        this.$t('oss.confirmDeleteCode', { code: row.code }),
        this.$t('common.tip'),
        {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        }
      ).then(() => {
        deleteDownloadCode(row.code)
          .then(() => {
            this.$message.success(this.$t('oss.deleteCodeSuccess'))
            this.loadAll()
          })
          .catch(() => {})
      }).catch(() => {})
    },

    // ===== 有效期解析 =====
    // 快捷选项转秒数：never 创建时返回 undefined（后端视为永不过期），续期时返回 0（显式改为永久）
    resolveExpireSeconds(mode, seconds, forRenew) {
      if (mode === 'never') {
        return forRenew ? 0 : undefined
      }
      if (mode === 'day') {
        return 86400
      }
      if (mode === 'week') {
        return 604800
      }
      return seconds
    },

    // ===== 状态与剩余时长 =====
    isPermanent(row) {
      return !row || !row.expireAt || row.expireAt <= 0
    },

    isExpired(row) {
      return !this.isPermanent(row) && Date.now() > row.expireAt
    },

    statusTagType(row) {
      if (this.isPermanent(row)) {
        return ''
      }
      return this.isExpired(row) ? 'danger' : 'success'
    },

    statusLabel(row) {
      if (this.isPermanent(row)) {
        return this.$t('oss.statusPermanent')
      }
      return this.isExpired(row) ? this.$t('oss.statusExpired') : this.$t('oss.statusValid')
    },

    // 剩余比例：以创建时刻到过期时刻为总周期，用于进度条可视化
    remainPercent(row) {
      const totalSpan = row.expireAt - row.createTime
      if (totalSpan <= 0) {
        return 0
      }
      const remain = row.expireAt - Date.now()
      return Math.max(0, Math.min(100, Math.round((remain / totalSpan) * 100)))
    },

    remainColor(row) {
      const percent = this.remainPercent(row)
      if (percent > 50) {
        return '#67C23A'
      }
      return percent > 20 ? '#E6A23C' : '#F56C6C'
    },

    remainText(row) {
      const remain = row.expireAt - Date.now()
      if (remain <= 0) {
        return this.$t('oss.statusExpired')
      }
      const days = Math.floor(remain / 86400000)
      if (days > 0) {
        return days + ' ' + this.$t('oss.dayUnit')
      }
      const hours = Math.floor(remain / 3600000)
      if (hours > 0) {
        return hours + ' ' + this.$t('oss.hourUnit')
      }
      return Math.max(1, Math.floor(remain / 60000)) + ' ' + this.$t('oss.minuteUnit')
    },

    // ===== 通用工具 =====
    formatTime(value) {
      if (!value) {
        return '-'
      }
      const date = new Date(Number(value))
      if (isNaN(date.getTime())) {
        return String(value)
      }
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },

    copyPublicUrl(row) {
      this.copyText(buildPublicDownloadUrl(row.code))
    },

    copyText(text) {
      if (!text) {
        return
      }
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success(this.$t('oss.copySuccess'))
        }).catch(() => {
          this.fallbackCopy(text)
        })
      } else {
        this.fallbackCopy(text)
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
        this.$message.success(this.$t('oss.copySuccess'))
      } catch (e) {
        this.$message.error(this.$t('oss.copyFail'))
      }
      document.body.removeChild(textarea)
    },

    // 供父组件查询单条码后打开续期（预留扩展）
    refresh() {
      this.loadAll()
    }
  }
}
</script>

<style lang="scss" scoped>
.download-code-panel {
  // ===== 统计概览 =====
  .stat-row {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 14px;
  }

  .stat-card {
    flex: 1;
    display: flex;
    align-items: center;
    min-width: 160px;
    margin-right: 14px;
    padding: 14px 16px;
    background-color: #fff;
    border-radius: 6px;
    border-left: 3px solid #409eff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    transition: transform 0.25s ease, box-shadow 0.25s ease;

    &:last-child {
      margin-right: 0;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      margin-right: 12px;
      border-radius: 50%;
      font-size: 20px;
      color: #409eff;
      background-color: rgba(64, 158, 255, 0.1);
      flex-shrink: 0;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      line-height: 1.1;
      color: #303133;
      font-variant-numeric: tabular-nums;
    }

    .stat-label {
      margin-top: 2px;
      font-size: 12px;
      color: #909399;
    }
  }

  .stat-valid {
    border-left-color: #67c23a;

    .stat-icon {
      color: #67c23a;
      background-color: rgba(103, 194, 58, 0.1);
    }
  }

  .stat-expired {
    border-left-color: #f56c6c;

    .stat-icon {
      color: #f56c6c;
      background-color: rgba(245, 108, 108, 0.1);
    }
  }

  .stat-permanent {
    border-left-color: #909399;

    .stat-icon {
      color: #909399;
      background-color: rgba(144, 147, 153, 0.12);
    }
  }

  // ===== 工具栏 =====
  .panel-toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .toolbar-spacer {
      flex: 1;
    }
  }

  // ===== 表格单元格 =====
  .code-text {
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace !important;
    font-weight: 600;
    letter-spacing: 0.5px;
    color: #303133;
  }

  .code-copy {
    margin-left: 4px;
    padding: 0;
  }

  .plain-text {
    color: #606266;
    word-break: break-all;
  }

  .remark-text {
    color: #909399;
  }

  .time-text {
    color: #606266;
    font-variant-numeric: tabular-nums;
  }

  .permanent-text {
    font-size: 12px;
    color: #409eff;
  }

  .expired-text {
    font-size: 12px;
    color: #f56c6c;
  }

  .remain-text {
    display: inline-block;
    margin-top: 2px;
    font-size: 12px;
    color: #909399;
  }

  .danger-text {
    color: #f56c6c;
  }

  // ===== 空态 =====
  .empty-block {
    padding: 26px 0;
    text-align: center;

    .empty-icon {
      font-size: 36px;
      color: #dcdfe6;
    }

    .empty-title {
      margin: 10px 0 4px;
      font-size: 14px;
      color: #606266;
    }

    .empty-tip {
      margin: 0 0 14px;
      font-size: 12px;
      color: #909399;
    }
  }

  // ===== 底部 =====
  .panel-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;

    .count-tip {
      font-size: 12px;
      color: #909399;
    }
  }

  // ===== 抽屉 =====
  .drawer-body {
    padding: 0 20px;
  }

  .drawer-footer {
    position: sticky;
    bottom: 0;
    padding: 12px 20px;
    margin-top: 16px;
    background-color: #fff;
    border-top: 1px solid #ebeef5;
    text-align: right;
  }

  .result-card {
    padding: 14px 16px;
    background-color: #f5f7fa;
    border: 1px solid #ebeef5;
    border-radius: 6px;

    .result-title {
      margin-bottom: 10px;
      font-size: 13px;
      font-weight: 600;
      color: #67c23a;
    }

    .result-item {
      margin-bottom: 10px;
    }

    .result-label {
      display: inline-block;
      margin-bottom: 4px;
      font-size: 12px;
      color: #909399;
    }
  }

  .renew-summary {
    margin-bottom: 16px;
    padding: 12px 14px;
    background-color: #f5f7fa;
    border-radius: 6px;

    .summary-row {
      display: flex;
      align-items: baseline;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .result-label {
        width: 84px;
        font-size: 12px;
        color: #909399;
        flex-shrink: 0;
      }

      .summary-key {
        word-break: break-all;
      }
    }
  }
}
</style>
