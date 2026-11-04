<template>
  <div>
    <div class="tab-toolbar">
      <div class="filter-row">
        <el-select v-model="search.sourceAppKeyEquals" :placeholder="$t('version.sourceAppKey')" size="small" filterable clearable style="width:140px" @change="handleSearch">
          <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
        </el-select>
        <el-select v-model="search.targetAppKeyEquals" :placeholder="$t('version.targetAppKey')" size="small" filterable clearable style="width:140px" @change="handleSearch">
          <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
        </el-select>
        <el-select v-model="search.targetPlatformEquals" :placeholder="$t('version.targetPlatform')" size="small" clearable style="width:120px" @change="handleSearch">
          <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
        </el-select>
        <el-select v-model="search.actionEquals" :placeholder="$t('version.action')" size="small" clearable style="width:120px" @change="handleSearch">
          <el-option :label="$t('version.actionForce')" :value="1" />
          <el-option :label="$t('version.actionRecommend')" :value="2" />
          <el-option :label="$t('version.actionNotify')" :value="3" />
        </el-select>
        <el-select v-model="search.statusFilter" :placeholder="$t('version.status')" size="small" clearable style="width:110px" @change="handleSearch">
          <el-option :label="$t('version.enabled')" value="enabled" />
          <el-option :label="$t('version.disabled')" value="disabled" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">{{ $t('common.search') }}</el-button>
        <el-button icon="el-icon-refresh-left" size="small" @click="handleReset">{{ $t('common.reset') }}</el-button>
      </div>
      <div class="filter-row">
        <el-button icon="el-icon-aim" size="small" @click="openUpgradeCheck">{{ $t('version.upgradeCheck') }}</el-button>
        <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">{{ $t('version.addPolicy') }}</el-button>
      </div>
    </div>
    <el-table v-loading="loading" :data="list" stripe border style="width:100%;margin-top:12px" size="small">
      <el-table-column :label="$t('version.ruleSummary')" min-width="340">
        <template slot-scope="{ row }">
          <div class="rule-summary">
            <span class="rule-app">{{ row.sourceAppKey }}</span>
            <el-tag size="mini" :type="platformTagType(row.sourcePlatform)">{{ row.sourcePlatform }}</el-tag>
            <span class="rule-cond">{{ row.sourceVersionCode ? $t('version.summaryFrom', { code: row.sourceVersionCode }) : $t('version.summaryAny') }}</span>
            <i class="el-icon-right rule-arrow" />
            <span class="rule-app">{{ row.targetAppKey }}</span>
            <el-tag size="mini" :type="platformTagType(row.targetPlatform)">{{ row.targetPlatform }}</el-tag>
            <span class="rule-cond">{{ $t('version.summaryRequire', { code: row.minCompatVersion }) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="action" :label="$t('version.action')" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tooltip :content="actionDesc(row.action)" placement="top">
            <el-tag :type="actionTagType(row.action)" size="mini">{{ actionLabel(row.action) }}</el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="flag" :label="$t('version.status')" width="80" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.flag === 1 ? 'success' : 'info'" size="mini">{{ row.flag === 1 ? $t('version.enabled') : $t('version.disabled') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="190" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" @click="handleEdit(row)"><i class="el-icon-edit-outline" /> {{ $t('common.edit') }}</el-button>
          <el-button size="mini" type="text" :class="row.flag === 1 ? 'warning-text' : 'success-text'" @click="handleToggle(row)">
            <i :class="row.flag === 1 ? 'el-icon-video-pause' : 'el-icon-video-play'" /> {{ row.flag === 1 ? $t('version.disable') : $t('version.enable') }}
          </el-button>
          <el-button size="mini" type="text" class="danger-text" @click="handleDelete(row)"><i class="el-icon-delete" /> {{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total" @size-change="v => { pageSize = v; page = 1; loadPolicies() }" @current-change="v => { page = v; loadPolicies() }" />
    </div>

    <!-- 兼容策略编辑弹窗（按触发条件/升级要求分组，底部实时预览规则） -->
    <el-dialog :title="form.id ? $t('version.editPolicy') : $t('version.addPolicy')" :visible.sync="dialogVisible" width="620px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="policyForm" :model="form" :rules="rules" label-width="110px">
        <el-divider content-position="left">{{ $t('version.triggerSection') }}</el-divider>
        <el-form-item :label="$t('version.sourceAppKey')" prop="sourceAppKey">
          <el-select v-model="form.sourceAppKey" filterable style="width:100%" :placeholder="$t('version.sourceAppKeyPh')">
            <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
          </el-select>
        </el-form-item>
        <div class="form-row">
          <el-form-item :label="$t('version.sourcePlatform')" prop="sourcePlatform" class="form-row-item">
            <el-select v-model="form.sourcePlatform" style="width:100%">
              <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="$t('version.limitSourceVersion')">
          <el-switch v-model="form.sourceVersionLimit" />
          <el-input-number v-if="form.sourceVersionLimit" v-model="form.sourceVersionCode" :min="1" style="width:200px;margin-left:12px" />
          <div class="form-tip">{{ $t('version.sourceVersionCodeTip') }}</div>
        </el-form-item>
        <el-divider content-position="left">{{ $t('version.targetSection') }}</el-divider>
        <el-form-item :label="$t('version.targetAppKey')" prop="targetAppKey">
          <el-select v-model="form.targetAppKey" filterable style="width:100%" :placeholder="$t('version.targetAppKeyPh')">
            <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
          </el-select>
        </el-form-item>
        <div class="form-row">
          <el-form-item :label="$t('version.targetPlatform')" prop="targetPlatform" class="form-row-item">
            <el-select v-model="form.targetPlatform" style="width:100%">
              <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="$t('version.minCompatVersion')" prop="minCompatVersion">
          <el-input-number v-model="form.minCompatVersion" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item :label="$t('version.action')" prop="action">
          <el-radio-group v-model="form.action" class="action-radio-group">
            <el-radio :label="1">
              <span class="action-radio-title">{{ $t('version.actionForce') }}</span>
              <span class="action-radio-desc">{{ $t('version.actionForceDesc') }}</span>
            </el-radio>
            <el-radio :label="2">
              <span class="action-radio-title">{{ $t('version.actionRecommend') }}</span>
              <span class="action-radio-desc">{{ $t('version.actionRecommendDesc') }}</span>
            </el-radio>
            <el-radio :label="3">
              <span class="action-radio-title">{{ $t('version.actionNotify') }}</span>
              <span class="action-radio-desc">{{ $t('version.actionNotifyDesc') }}</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div class="policy-preview">
        <i class="el-icon-view" /> {{ $t('version.rulePreview') }}：{{ policyPreview }}
      </div>
      <div slot="footer">
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ $t('common.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!-- 升级检查弹窗（模拟客户端升级探测） -->
    <el-dialog :title="$t('version.upgradeCheck')" :visible.sync="checkDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form ref="checkForm" :model="checkForm" label-width="100px">
        <el-form-item :label="$t('version.appKey')">
          <el-select v-model="checkForm.appKey" filterable style="width:100%">
            <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('version.platform')">
          <el-select v-model="checkForm.platform" style="width:100%">
            <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('version.currentVersionCode')">
          <el-input-number v-model="checkForm.currentVersionCode" :min="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <div v-if="checkResult" class="check-result">
        <el-alert :title="checkResult.needUpdate ? $t('version.needUpgrade') : $t('version.noNeedUpgrade')" :type="checkResult.needUpdate ? 'warning' : 'success'" show-icon :closable="false" />
        <el-descriptions v-if="checkResult.needUpdate" :column="1" border size="small" style="margin-top:12px">
          <el-descriptions-item :label="$t('version.checkLatestName')">{{ checkResult.latestVersionName || '-' }} ({{ checkResult.latestVersionCode }})</el-descriptions-item>
          <el-descriptions-item :label="$t('version.checkAction')">
            <el-tag :type="actionTagType(checkResult.action)" size="mini">{{ actionLabel(checkResult.action) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="checkResult.releaseNote" :label="$t('version.releaseNote')">{{ checkResult.releaseNote }}</el-descriptions-item>
          <el-descriptions-item v-if="checkResult.downloadUrls && checkResult.downloadUrls.length" :label="$t('version.downloadUrl')">
            <div v-for="(url, idx) in checkResult.downloadUrls" :key="idx" style="margin-bottom:4px">
              <el-button size="mini" type="text" @click="copyText(url)"><i class="el-icon-copy-document" /> {{ url }}</el-button>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer">
        <el-button @click="checkDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="checkLoading" @click="handleCheckUpgrade">{{ $t('version.executeCheck') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {checkUpgrade, deletePolicy, disablePolicy, enablePolicy, queryPolicies, savePolicy} from '@/api/app-version'

export default {
  name: 'PolicyList',
  inject: ['actionLabel', 'actionDesc', 'actionTagType', 'platformTagType', 'copyText'],
  props: {
    appKeys: { type: Array, default: () => [] }
  },
  data() {
    return {
      loading: false, list: [], total: 0, page: 1, pageSize: 10,
      search: { sourceAppKeyEquals: '', targetAppKeyEquals: '', targetPlatformEquals: '', actionEquals: null, statusFilter: '' },
      dialogVisible: false, saving: false,
      form: { id: null, sourceAppKey: '', sourceVersionLimit: false, sourceVersionCode: null, sourcePlatform: 'android', targetAppKey: '', targetPlatform: 'android', minCompatVersion: null, action: 1 },
      rules: {
        sourceAppKey: [{ required: true, message: this.$t('version.sourceAppKeyRequired'), trigger: 'change' }],
        targetAppKey: [{ required: true, message: this.$t('version.targetAppKeyRequired'), trigger: 'change' }],
        targetPlatform: [{ required: true, message: this.$t('version.targetPlatformRequired'), trigger: 'change' }],
        minCompatVersion: [{ required: true, message: this.$t('version.minCompatVersionRequired'), trigger: 'blur' }],
        action: [{ required: true, message: this.$t('version.actionRequired'), trigger: 'change' }]
      },
      checkDialogVisible: false, checkLoading: false, checkResult: null,
      checkForm: { appKey: '', platform: 'android', currentVersionCode: 1 }
    }
  },
  computed: {
    /** 弹窗底部实时预览规则语义，所见即所得 */
    policyPreview() {
      const f = this.form
      if (!f.sourceAppKey && !f.targetAppKey) return '-'
      const source = `${f.sourceAppKey || '?'} (${f.sourcePlatform}) ${f.sourceVersionLimit && f.sourceVersionCode ? this.$t('version.summaryFrom', { code: f.sourceVersionCode }) : this.$t('version.summaryAny')}`
      const target = `${f.targetAppKey || '?'} (${f.targetPlatform}) ${this.$t('version.summaryRequire', { code: f.minCompatVersion || '?' })}`
      return `${source} → ${target}，${this.actionLabel(f.action)}`
    }
  },
  methods: {
    async loadPolicies() {
      this.loading = true
      try {
        const criteria = { createTimeOrder: { order: 'DESC' }}
        if (this.search.sourceAppKeyEquals) criteria.sourceAppKeyEquals = this.search.sourceAppKeyEquals
        if (this.search.targetAppKeyEquals) criteria.targetAppKeyEquals = this.search.targetAppKeyEquals
        if (this.search.targetPlatformEquals) criteria.targetPlatformEquals = this.search.targetPlatformEquals
        if (this.search.actionEquals) criteria.actionEquals = this.search.actionEquals
        if (this.search.statusFilter) criteria.flagIn = [this.search.statusFilter === 'enabled' ? 1 : -1]
        const res = await queryPolicies(criteria, this.page, this.pageSize)
        const pd = res.data || {}
        this.list = pd.data || []
        this.total = pd.total || 0
      } catch (e) { console.error(e) } finally { this.loading = false }
    },
    handleSearch() { this.page = 1; this.loadPolicies() },
    handleReset() { this.search = { sourceAppKeyEquals: '', targetAppKeyEquals: '', targetPlatformEquals: '', actionEquals: null, statusFilter: '' }; this.page = 1; this.loadPolicies() },
    handleAdd() {
      this.form = { id: null, sourceAppKey: '', sourceVersionLimit: false, sourceVersionCode: null, sourcePlatform: 'android', targetAppKey: '', targetPlatform: 'android', minCompatVersion: null, action: 1 }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.policyForm && this.$refs.policyForm.clearValidate())
    },
    handleEdit(row) {
      this.form = { id: row.id, sourceAppKey: row.sourceAppKey, sourceVersionLimit: !!row.sourceVersionCode, sourceVersionCode: row.sourceVersionCode, sourcePlatform: row.sourcePlatform, targetAppKey: row.targetAppKey, targetPlatform: row.targetPlatform, minCompatVersion: row.minCompatVersion, action: row.action }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.policyForm && this.$refs.policyForm.clearValidate())
    },
    async handleSave() {
      try { await this.$refs.policyForm.validate() } catch { return }
      this.saving = true
      try {
        // 未开启版本限制时不传版本号，后端按不限版本处理；sourceVersionLimit 为纯前端字段不提交
        const payload = { ...this.form, sourceVersionCode: this.form.sourceVersionLimit ? this.form.sourceVersionCode : null }
        delete payload.sourceVersionLimit
        await savePolicy(payload); this.dialogVisible = false; await this.loadPolicies()
      } catch (e) { console.error(e) } finally { this.saving = false }
    },
    async handleToggle(row) {
      const isEnable = row.flag !== 1
      try {
        await this.$confirm(this.$t('version.confirmTogglePolicy', { action: isEnable ? this.$t('version.enable') : this.$t('version.disable') }), this.$t('common.tip'), { type: 'warning' })
        if (isEnable) { await enablePolicy(row.id) } else { await disablePolicy(row.id) }
        await this.loadPolicies()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(this.$t('version.confirmDeletePolicy'), this.$t('common.tip'), { type: 'error' })
        await deletePolicy(row.id)
        if (this.list.length === 1 && this.page > 1) this.page--
        await this.loadPolicies()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    openUpgradeCheck() {
      this.checkResult = null
      this.checkDialogVisible = true
    },
    async handleCheckUpgrade() {
      this.checkLoading = true; this.checkResult = null
      try {
        const res = await checkUpgrade(this.checkForm)
        this.checkResult = res.data
      } catch (e) { console.error(e) } finally { this.checkLoading = false }
    },
    handleDialogClose() {
      this.$refs.policyForm && this.$refs.policyForm.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.form-row {
  display: flex;
  gap: 16px;
  .form-row-item { flex: 1; }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.success-text { color: #67c23a !important; }
.warning-text { color: #e6a23c !important; }
.danger-text { color: #f56c6c !important; }

.rule-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;

  .rule-app {
    font-weight: 600;
    color: #1e293b;
  }

  .rule-cond {
    color: #64748b;
    font-size: 12px;
  }

  .rule-arrow {
    color: #94a3b8;
    margin: 0 2px;
  }
}

.action-radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;

  ::v-deep .el-radio {
    height: auto;
    margin-right: 0;
    padding: 8px 12px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    transition: all 0.2s;

    &.is-checked {
      border-color: #409eff;
      background: #ecf5ff;
    }
  }

  .action-radio-title {
    font-weight: 600;
  }

  .action-radio-desc {
    display: block;
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
    font-weight: normal;
  }
}

.policy-preview {
  margin-top: 4px;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  color: #475569;

  i {
    color: #94a3b8;
    margin-right: 4px;
  }
}

.check-result {
  margin-top: 16px;
}
</style>
