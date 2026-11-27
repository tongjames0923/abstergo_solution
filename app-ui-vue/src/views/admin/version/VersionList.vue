<template>
  <div class="version-list">
    <div class="tab-toolbar">
      <div class="toolbar-main">
        <div class="filter-row">
          <el-select v-model="search.appKeyEquals" :placeholder="$t('version.appKey')" size="small" filterable clearable style="width:160px" @change="handleSearch">
            <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
          </el-select>
          <el-input v-model="search.versionNameLike" :placeholder="$t('version.versionName')" size="small" clearable style="width:160px" @keyup.enter.native="handleSearch" />
          <el-select v-model="search.platformEquals" :placeholder="$t('version.platform')" size="small" clearable style="width:130px" @change="handleSearch">
            <el-option label="Android" value="android" />
            <el-option label="iOS" value="ios" />
            <el-option label="Windows" value="windows" />
            <el-option label="macOS" value="macos" />
            <el-option label="Linux" value="linux" />
          </el-select>
          <el-select v-model="search.flagIn" :placeholder="$t('version.allStatus')" size="small" clearable multiple collapse-tags style="width:200px" @change="syncQuickStatus">
            <el-option :label="$t('version.statusDraft')" :value="0" />
            <el-option :label="$t('version.statusPending')" :value="2" />
            <el-option :label="$t('version.statusRelease')" :value="1" />
            <el-option :label="$t('version.statusOffline')" :value="-1" />
            <el-option :label="$t('version.statusRolledBack')" :value="-2" />
            <el-option :label="$t('version.statusArchived')" :value="-3" />
          </el-select>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button icon="el-icon-refresh-left" size="small" @click="handleReset">{{ $t('common.reset') }}</el-button>
        </div>
        <div class="quick-filter-row">
          <span class="quick-filter-label">{{ $t('version.quickFilter') }}</span>
          <el-radio-group v-model="quickStatus" size="mini" @change="handleQuickFilter">
            <el-radio-button :label="''">{{ $t('version.allStatus') }}</el-radio-button>
            <el-radio-button v-for="status in quickStatuses" :key="status" :label="status">{{ statusLabel(status) }}</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <el-button class="toolbar-add" type="primary" icon="el-icon-plus" size="small" @click="handleAdd">{{ $t('version.add') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="list" :empty-text="$t('version.emptyList')" stripe border style="width:100%;margin-top:12px" size="small">
      <el-table-column prop="appKey" :label="$t('version.appKey')" width="120" show-overflow-tooltip />
      <el-table-column prop="versionName" :label="$t('version.versionName')" width="120" />
      <el-table-column prop="versionCode" :label="$t('version.versionCode')" width="100" align="center" />
      <el-table-column prop="platform" :label="$t('version.platform')" width="90" align="center">
        <template slot-scope="{ row }">
          <el-tag size="mini" :type="platformTagType(row.platform)">{{ row.platform }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="versionType" :label="$t('version.versionType')" width="80" align="center">
        <template slot-scope="{ row }">{{ versionTypeLabel(row.versionType) }}</template>
      </el-table-column>
      <el-table-column prop="flag" :label="$t('version.status')" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="statusTagType(row.flag)" size="mini" effect="dark">{{ statusLabel(row.flag) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="releaseTime" :label="$t('version.releaseTime')" width="155">
        <template slot-scope="{ row }">{{ formatTime(row.releaseTime) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" :label="$t('version.createTime')" width="155">
        <template slot-scope="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" min-width="220" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" @click="openDetail(row)"><i class="el-icon-view" /> {{ $t('version.detail') }}</el-button>
          <el-button v-if="primaryAction(row)" size="mini" type="text" class="success-text" @click="handleLifecycle(row, primaryAction(row))">
            <i :class="primaryActionIcon(row)" /> {{ lifecycleLabel(primaryAction(row)) }}
          </el-button>
          <el-dropdown v-if="rowCommands(row).length" size="mini" trigger="click" @command="cmd => handleRowCommand(row, cmd)">
            <el-button size="mini" type="text">{{ $t('version.moreActions') }}<i class="el-icon-arrow-down el-icon--right" /></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="cmd in rowCommands(row)" :key="cmd" :command="cmd" :divided="cmd === 'delete'">{{ commandLabel(cmd) }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total" @size-change="v => { pageSize = v; page = 1; loadVersions() }" @current-change="v => { page = v; loadVersions() }" />
    </div>

    <!-- 版本编辑弹窗 -->
    <el-dialog :title="cloneSourceId ? $t('version.cloneVersion') : (form.id ? $t('version.editVersion') : $t('version.addVersion'))" :visible.sync="dialogVisible" width="600px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="versionForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('version.appKey')" prop="appKey">
          <!-- allow-create 支持直接输入新的应用标识创建首个版本 -->
          <el-select v-model="form.appKey" filterable allow-create default-first-option :placeholder="$t('version.appKeyPh')" :disabled="!!form.id" style="width:100%">
            <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
          </el-select>
        </el-form-item>
        <div class="form-row">
          <el-form-item :label="$t('version.versionCode')" prop="versionCode" class="form-row-item">
            <el-input-number v-model="form.versionCode" :min="1" :disabled="!!form.id" style="width:100%" />
          </el-form-item>
          <el-form-item :label="$t('version.versionName')" prop="versionName" class="form-row-item">
            <el-input v-model="form.versionName" :placeholder="$t('version.versionNamePh')" />
          </el-form-item>
        </div>
        <div class="form-hint"><i class="el-icon-info" /> {{ latestHint }}</div>
        <div class="form-row">
          <el-form-item :label="$t('version.versionType')" prop="versionType" class="form-row-item">
            <el-select v-model="form.versionType" style="width:100%">
              <el-option :label="$t('version.typeRelease')" :value="1" />
              <el-option :label="$t('version.typeBeta')" :value="2" />
              <el-option :label="$t('version.typeAlpha')" :value="3" />
              <el-option :label="$t('version.typeRC')" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('version.platform')" prop="platform" class="form-row-item">
            <el-select v-model="form.platform" style="width:100%">
              <el-option label="Android" value="android" />
              <el-option label="iOS" value="ios" />
              <el-option label="Windows" value="windows" />
              <el-option label="macOS" value="macos" />
              <el-option label="Linux" value="linux" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="$t('version.releaseNote')">
          <el-input v-model="form.releaseNote" type="textarea" :rows="3" :placeholder="$t('version.releaseNotePh')" />
        </el-form-item>
        <el-form-item :label="$t('version.buildInfo')">
          <el-input v-model="form.buildInfo" type="textarea" :rows="2" :placeholder="$t('version.buildInfoPh')" />
        </el-form-item>
        <el-form-item v-if="cloneSourceId" :label="$t('version.copyPackages')">
          <el-checkbox v-model="copyPackages">{{ $t('version.copyPackagesTip') }}</el-checkbox>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ $t('common.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!-- 版本详情抽屉 -->
    <el-drawer :title="$t('version.versionDetail')" :visible.sync="detailVisible" size="600px" direction="rtl">
      <div v-if="detailData" class="detail-drawer-body">
        <div class="detail-section">
          <h4>{{ $t('version.basicInfo') }}</h4>
          <el-descriptions :column="isPhone ? 1 : 2" border size="small">
            <el-descriptions-item :label="$t('version.appKey')">{{ detailData.version.appKey }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.versionName')">{{ detailData.version.versionName }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.versionCode')">{{ detailData.version.versionCode }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.platform')">{{ detailData.version.platform }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.versionType')">{{ versionTypeLabel(detailData.version.versionType) }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.status')">
              <el-tag :type="statusTagType(detailData.version.flag)" size="mini" effect="dark">{{ statusLabel(detailData.version.flag) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('version.releaseTime')" :span="isPhone ? 1 : 2">{{ formatTime(detailData.version.releaseTime) }}</el-descriptions-item>
            <el-descriptions-item v-if="detailData.version.releaseNote" :label="$t('version.releaseNote')" :span="isPhone ? 1 : 2">{{ detailData.version.releaseNote }}</el-descriptions-item>
            <el-descriptions-item v-if="detailData.version.buildInfo" :label="$t('version.buildInfo')" :span="isPhone ? 1 : 2">
              <pre class="build-info-block">{{ detailData.version.buildInfo }}</pre>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section">
          <h4>{{ $t('version.packages') }} ({{ (detailData.packages || []).length }})</h4>
          <el-table :data="detailData.packages || []" size="mini" stripe border>
            <el-table-column prop="objectName" label="Object" show-overflow-tooltip />
            <el-table-column prop="md5" label="MD5" width="180" show-overflow-tooltip />
            <el-table-column :label="$t('version.download')" width="80" align="center">
              <template slot-scope="{ row }">
                <el-button size="mini" type="text" @click="handleDownloadPkg(row)"><i class="el-icon-download" /></el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  archiveVersion,
  cloneVersion,
  deleteVersion,
  getLatestReleased,
  getVersionDetail,
  offlineVersion,
  queryVersions,
  releaseVersion,
  rollbackRestoreVersion,
  saveVersion,
  submitVersion
} from '@/api/app-version'
import {downloadObject} from '@/api/oss'
import ResponsiveMixin from '@/mixins/responsive'

export default {
  name: 'VersionList',
  mixins: [ResponsiveMixin],
  inject: ['statusLabel', 'statusTagType', 'versionTypeLabel', 'platformTagType', 'formatTime'],
  props: {
    appKeys: { type: Array, default: () => [] }
  },
  data() {
    return {
      loading: false, list: [], total: 0, page: 1, pageSize: 10,
      search: { appKeyEquals: '', versionNameLike: '', platformEquals: '', flagIn: [] },
      quickStatus: '',
      quickStatuses: [0, 2, 1, -1, -2, -3],
      dialogVisible: false, saving: false,
      cloneSourceId: null, copyPackages: true,
      form: this.defaultForm(),
      latestReleased: null,
      rules: {
        appKey: [{ required: true, message: this.$t('version.appKeyRequired'), trigger: 'change' }],
        versionCode: [{ required: true, message: this.$t('version.versionCodeRequired'), trigger: 'blur' }],
        versionName: [{ required: true, message: this.$t('version.versionNameRequired'), trigger: 'blur' }],
        versionType: [{ required: true, message: this.$t('version.versionTypeRequired'), trigger: 'change' }],
        platform: [{ required: true, message: this.$t('version.platformRequired'), trigger: 'change' }]
      },
      detailVisible: false, detailData: null
    }
  },
  computed: {
    latestHint() {
      if (!this.latestReleased) return this.$t('version.latestNoVersion')
      return this.$t('version.latestHint', { name: this.latestReleased.versionName || '-', code: this.latestReleased.versionCode })
    }
  },
  watch: {
    'form.appKey': 'onVersionContextChange',
    'form.platform': 'onVersionContextChange'
  },
  mounted() {
    this.loadVersions()
  },
  methods: {
    defaultForm() {
      return { id: null, appKey: '', versionCode: null, versionName: '', versionType: 1, platform: 'android', releaseNote: '', buildInfo: '' }
    },
    async loadVersions() {
      this.loading = true
      try {
        const criteria = { createTimeOrder: { order: 'DESC' }}
        if (this.search.appKeyEquals) criteria.appKeyEquals = this.search.appKeyEquals
        if (this.search.versionNameLike) criteria.versionNameLike = this.search.versionNameLike
        if (this.search.platformEquals) criteria.platformEquals = this.search.platformEquals
        if (this.search.flagIn && this.search.flagIn.length) criteria.flagIn = this.search.flagIn
        const res = await queryVersions(criteria, this.page, this.pageSize)
        const pd = res.data || {}
        this.list = pd.data || []
        this.total = pd.total || 0
      } catch (e) { console.error(e) } finally { this.loading = false }
    },
    handleSearch() { this.page = 1; this.loadVersions() },
    handleReset() {
      this.search = { appKeyEquals: '', versionNameLike: '', platformEquals: '', flagIn: [] }
      this.quickStatus = ''
      this.page = 1
      this.loadVersions()
    },
    syncQuickStatus() {
      this.quickStatus = this.search.flagIn.length === 1 ? this.search.flagIn[0] : ''
    },
    handleQuickFilter(val) {
      this.search.flagIn = val === '' ? [] : [val]
      this.handleSearch()
    },
    onVersionContextChange() {
      if (!this.dialogVisible || this.form.id) {
        this.latestReleased = null
        return
      }
      this.loadLatestReleased()
    },
    async loadLatestReleased() {
      const { appKey, platform } = this.form
      if (!appKey || !platform) {
        this.latestReleased = null
        return
      }
      try {
        const res = await getLatestReleased(appKey, platform)
        this.latestReleased = res.data || null
      } catch (e) {
        this.latestReleased = null
        console.error(e)
      }
    },
    handleAdd() {
      this.form = this.defaultForm()
      this.cloneSourceId = null
      this.copyPackages = true
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.versionForm && this.$refs.versionForm.clearValidate())
    },
    /** 基于现有版本复制新建：预填全部版本字段，候选版默认转为正式版，安装包由服务端复制到新 key */
    handleClone(row) {
      this.form = {
        id: null, appKey: row.appKey, versionCode: null, versionName: '',
        versionType: row.versionType === 4 ? 1 : row.versionType, platform: row.platform,
        releaseNote: row.releaseNote || '', buildInfo: row.buildInfo || ''
      }
      this.cloneSourceId = row.id
      this.copyPackages = true
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.versionForm && this.$refs.versionForm.clearValidate()
        this.$message.info(this.$t('version.cloneFrom', { name: row.versionName }))
      })
    },
    handleEdit(row) {
      this.form = { id: row.id, appKey: row.appKey, versionCode: row.versionCode, versionName: row.versionName, versionType: row.versionType, platform: row.platform, releaseNote: row.releaseNote || '', buildInfo: row.buildInfo || '' }
      this.cloneSourceId = null
      this.copyPackages = true
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.versionForm && this.$refs.versionForm.clearValidate())
    },
    async handleSave() {
      try { await this.$refs.versionForm.validate() } catch { return }
      if (!this.form.id && this.latestReleased && this.form.versionCode <= this.latestReleased.versionCode) {
        try {
          await this.$confirm(this.$t('version.codeNotGreaterConfirm', { code: this.latestReleased.versionCode }), this.$t('common.tip'), { type: 'warning' })
        } catch { return }
      }
      this.saving = true
      try {
        if (this.cloneSourceId) {
          await cloneVersion(this.cloneSourceId, this.form, this.copyPackages)
        } else {
          await saveVersion(this.form)
        }
        this.dialogVisible = false
        await this.loadVersions()
        this.$emit('version-changed')
      } catch (e) { console.error(e) } finally { this.saving = false }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(this.$t('version.confirmDeleteVersion', { name: row.versionName }), this.$t('common.tip'), { type: 'error' })
        await deleteVersion(row.id)
        if (this.list.length === 1 && this.page > 1) this.page--
        await this.loadVersions()
        this.$emit('version-changed')
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    async handleLifecycle(row, action) {
      try {
        await this.$confirm(this.$t('version.confirmLifecycle', { action: this.lifecycleLabel(action), name: row.versionName }), this.$t('common.tip'), { type: 'warning' })
        const fnMap = { submit: submitVersion, release: releaseVersion, offline: offlineVersion, rollbackRestore: rollbackRestoreVersion, archive: archiveVersion }
        await fnMap[action](row.id)
        await this.loadVersions()
        this.$emit('version-changed')
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    lifecycleLabel(action) {
      const map = { submit: this.$t('version.submit'), release: this.$t('version.release'), offline: this.$t('version.offline'), rollbackRestore: this.$t('version.rollbackRestore'), archive: this.$t('version.archive') }
      return map[action] || action
    },
    primaryAction(row) {
      const map = { 0: 'submit', 2: 'release', 1: 'offline', '-1': 'archive' }
      return map[row.flag] || null
    },
    primaryActionIcon(row) {
      const map = { submit: 'el-icon-s-promotion', release: 'el-icon-upload2', offline: 'el-icon-download', archive: 'el-icon-folder-checked' }
      return map[this.primaryAction(row)] || ''
    },
    rowCommands(row) {
      const map = { 0: ['edit', 'clone', 'delete'], 1: ['clone', 'rollbackRestore'] }
      return map[row.flag] || []
    },
    commandLabel(cmd) {
      const map = { edit: this.$t('common.edit'), clone: this.$t('version.cloneVersion'), rollbackRestore: this.$t('version.rollbackRestore'), archive: this.$t('version.archive'), delete: this.$t('common.delete') }
      return map[cmd] || cmd
    },
    handleRowCommand(row, cmd) {
      if (cmd === 'edit') { this.handleEdit(row); return }
      if (cmd === 'clone') { this.handleClone(row); return }
      if (cmd === 'delete') { this.handleDelete(row); return }
      this.handleLifecycle(row, cmd)
    },
    async openDetail(row) {
      this.detailVisible = true
      this.detailData = null
      try {
        const res = await getVersionDetail(row.id)
        this.detailData = res.data
      } catch (e) { console.error(e) }
    },
    async handleDownloadPkg(row) {
      if (row.objectName) {
        try { await downloadObject(row.objectName, row.bucketName || undefined) } catch (e) { console.error(e) }
      }
    },
    handleDialogClose() {
      this.$refs.versionForm && this.$refs.versionForm.resetFields()
      this.latestReleased = null
      this.cloneSourceId = null
      this.copyPackages = true
    }
  }
}
</script>

<style lang="scss" scoped>
.version-list {
  --version-ink: #1e293b;
  --version-muted: #64748b;
  --version-line: #e2e8f0;
  --version-surface: #f8fafc;
}

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.toolbar-add {
  flex-shrink: 0;
}

.quick-filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  max-width: 100%;
  overflow-x: auto;
}

.quick-filter-label {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--version-muted, #64748b);
}

.form-hint {
  margin: 0 0 12px;
  padding: 8px 10px;
  background: var(--version-surface, #f8fafc);
  border-left: 3px solid #409eff;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--version-muted, #64748b);

  i {
    margin-right: 4px;
    color: #409eff;
  }
}

.build-info-block {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  font-size: 12px;
  color: var(--version-ink, #1e293b);
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

.success-text { color: #67c23a !important; }
.warning-text { color: #e6a23c !important; }
.danger-text { color: #f56c6c !important; }

.detail-drawer-body {
  padding: 16px 20px;
}

.detail-section {
  margin-bottom: 24px;

  h4 {
    margin: 0 0 12px;
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;
    padding-bottom: 8px;
    border-bottom: 2px solid #e2e8f0;
  }
}
</style>
