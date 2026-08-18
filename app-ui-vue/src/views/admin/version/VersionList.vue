<template>
  <div>
    <div class="tab-toolbar">
      <div class="filter-row">
        <el-select v-model="search.appKeyEquals" :placeholder="$t('version.appKey')" size="small" filterable clearable style="width:160px" @change="handleSearch">
          <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
        </el-select>
        <el-input v-model="search.versionNameLike" :placeholder="$t('version.versionName')" size="small" clearable style="width:160px" @keyup.enter.native="loadVersions" />
        <el-select v-model="search.platformEquals" :placeholder="$t('version.platform')" size="small" clearable style="width:130px">
          <el-option label="Android" value="android" />
          <el-option label="iOS" value="ios" />
          <el-option label="Windows" value="windows" />
          <el-option label="macOS" value="macos" />
          <el-option label="Linux" value="linux" />
        </el-select>
        <el-select v-model="search.flagIn" :placeholder="$t('version.allStatus')" size="small" clearable multiple collapse-tags style="width:200px">
          <el-option :label="$t('version.statusDraft')" :value="0" />
          <el-option :label="$t('version.statusPending')" :value="2" />
          <el-option :label="$t('version.statusRelease')" :value="1" />
          <el-option :label="$t('version.statusOffline')" :value="-1" />
          <el-option :label="$t('version.statusRolledBack')" :value="-2" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">{{ $t('common.search') }}</el-button>
        <el-button icon="el-icon-refresh-left" size="small" @click="handleReset">{{ $t('common.reset') }}</el-button>
      </div>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">{{ $t('version.add') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe border style="width:100%;margin-top:12px" size="small">
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
      <el-table-column :label="$t('common.operation')" min-width="260" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" @click="openDetail(row)"><i class="el-icon-view" /> {{ $t('version.detail') }}</el-button>
          <el-button v-if="row.flag === 0" size="mini" type="text" class="success-text" @click="handleLifecycle(row, 'submit')"><i class="el-icon-s-promotion" /> {{ $t('version.submit') }}</el-button>
          <el-button v-if="row.flag === 2" size="mini" type="text" class="success-text" @click="handleLifecycle(row, 'release')"><i class="el-icon-upload2" /> {{ $t('version.release') }}</el-button>
          <el-button v-if="row.flag === 1" size="mini" type="text" class="warning-text" @click="handleLifecycle(row, 'offline')"><i class="el-icon-download" /> {{ $t('version.offline') }}</el-button>
          <el-button v-if="row.flag === 1" size="mini" type="text" class="warning-text" @click="handleLifecycle(row, 'rollback')"><i class="el-icon-refresh-left" /> {{ $t('version.rollback') }}</el-button>
          <el-button v-if="row.flag === -1" size="mini" type="text" @click="handleLifecycle(row, 'archive')"><i class="el-icon-folder-checked" /> {{ $t('version.archive') }}</el-button>
          <el-button v-if="row.flag === 0" size="mini" type="text" @click="handleEdit(row)"><i class="el-icon-edit-outline" /> {{ $t('common.edit') }}</el-button>
          <el-button v-if="row.flag === 0 || row.flag === 1" size="mini" type="text" class="success-text" @click="handleClone(row)"><i class="el-icon-document-copy" /> {{ $t('version.cloneVersion') }}</el-button>
          <el-button v-if="row.flag === 0" size="mini" type="text" class="danger-text" @click="handleDelete(row)"><i class="el-icon-delete" /> {{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total" @size-change="v => { pageSize = v; page = 1; loadVersions() }" @current-change="v => { page = v; loadVersions() }" />
    </div>

    <!-- 版本编辑弹窗 -->
    <el-dialog :title="form.id ? $t('version.editVersion') : $t('version.addVersion')" :visible.sync="dialogVisible" width="600px" :close-on-click-modal="false" @close="handleDialogClose">
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
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item :label="$t('version.appKey')">{{ detailData.version.appKey }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.versionName')">{{ detailData.version.versionName }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.versionCode')">{{ detailData.version.versionCode }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.platform')">{{ detailData.version.platform }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.versionType')">{{ versionTypeLabel(detailData.version.versionType) }}</el-descriptions-item>
            <el-descriptions-item :label="$t('version.status')">
              <el-tag :type="statusTagType(detailData.version.flag)" size="mini" effect="dark">{{ statusLabel(detailData.version.flag) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('version.releaseTime')" :span="2">{{ formatTime(detailData.version.releaseTime) }}</el-descriptions-item>
            <el-descriptions-item v-if="detailData.version.releaseNote" :label="$t('version.releaseNote')" :span="2">{{ detailData.version.releaseNote }}</el-descriptions-item>
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
  queryVersions, saveVersion, deleteVersion, submitVersion, releaseVersion,
  offlineVersion, rollbackVersion, archiveVersion, getVersionDetail
} from '@/api/app-version'
import { downloadObject } from '@/api/oss'

export default {
  name: 'VersionList',
  inject: ['statusLabel', 'statusTagType', 'versionTypeLabel', 'platformTagType', 'formatTime'],
  props: {
    appKeys: { type: Array, default: () => [] }
  },
  data() {
    return {
      loading: false, list: [], total: 0, page: 1, pageSize: 10,
      search: { appKeyEquals: '', versionNameLike: '', platformEquals: '', flagIn: [] },
      dialogVisible: false, saving: false,
      form: this.defaultForm(),
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
        const criteria = { createTimeOrder: { order: 'DESC' } }
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
    handleReset() { this.search = { appKeyEquals: '', versionNameLike: '', platformEquals: '', flagIn: [] }; this.page = 1; this.loadVersions() },
    handleAdd() {
      this.form = this.defaultForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.versionForm && this.$refs.versionForm.clearValidate())
    },
    /** 基于现有版本快捷复制新建：预填 appKey/platform/versionType/releaseNote/buildInfo，清空 id/versionCode/versionName */
    handleClone(row) {
      this.form = {
        id: null, appKey: row.appKey, versionCode: null, versionName: '',
        versionType: row.versionType, platform: row.platform,
        releaseNote: row.releaseNote || '', buildInfo: row.buildInfo || ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.versionForm && this.$refs.versionForm.clearValidate()
        this.$message.info(this.$t('version.cloneFrom', { name: row.versionName }))
      })
    },
    handleEdit(row) {
      this.form = { id: row.id, appKey: row.appKey, versionCode: row.versionCode, versionName: row.versionName, versionType: row.versionType, platform: row.platform, releaseNote: row.releaseNote || '', buildInfo: row.buildInfo || '' }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.versionForm && this.$refs.versionForm.clearValidate())
    },
    async handleSave() {
      try { await this.$refs.versionForm.validate() } catch { return }
      this.saving = true
      try {
        await saveVersion(this.form)
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
      const actionMap = { submit: this.$t('version.submit'), release: this.$t('version.release'), offline: this.$t('version.offline'), rollback: this.$t('version.rollback'), archive: this.$t('version.archive') }
      try {
        await this.$confirm(this.$t('version.confirmLifecycle', { action: actionMap[action], name: row.versionName }), this.$t('common.tip'), { type: 'warning' })
        const fnMap = { submit: submitVersion, release: releaseVersion, offline: offlineVersion, rollback: rollbackVersion, archive: archiveVersion }
        await fnMap[action](row.id)
        await this.loadVersions()
        this.$emit('version-changed')
      } catch (e) { if (e !== 'cancel') console.error(e) }
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
