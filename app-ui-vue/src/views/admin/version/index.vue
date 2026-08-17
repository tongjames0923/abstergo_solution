<template>
  <div class="app-container">
    <div class="version-admin">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-left">
          <h2 class="page-title"><i class="el-icon-box" /> {{ $t('version.title') }}</h2>
          <span class="page-subtitle">{{ $t('version.subtitle') }}</span>
        </div>
      </div>

      <!-- 主标签页 -->
      <el-tabs v-model="activeTab" class="version-tabs" @tab-click="handleTabClick">
        <!-- ========== 版本列表 ========== -->
        <el-tab-pane :label="$t('version.tabVersions')" name="versions">
          <div class="tab-toolbar">
            <div class="filter-row">
              <el-input v-model="versionSearch.appKeyEquals" :placeholder="$t('version.appKey')" size="small" clearable prefix-icon="el-icon-search" style="width:160px" @keyup.enter.native="loadVersions" />
              <el-input v-model="versionSearch.versionNameLike" :placeholder="$t('version.versionName')" size="small" clearable style="width:160px" @keyup.enter.native="loadVersions" />
              <el-select v-model="versionSearch.platformEquals" :placeholder="$t('version.platform')" size="small" clearable style="width:130px">
                <el-option label="Android" value="android" />
                <el-option label="iOS" value="ios" />
                <el-option label="Windows" value="windows" />
                <el-option label="macOS" value="macos" />
                <el-option label="Linux" value="linux" />
              </el-select>
              <el-select v-model="versionSearch.flagIn" :placeholder="$t('version.allStatus')" size="small" clearable multiple collapse-tags style="width:200px">
                <el-option :label="$t('version.statusDraft')" :value="0" />
                <el-option :label="$t('version.statusPending')" :value="2" />
                <el-option :label="$t('version.statusRelease')" :value="1" />
                <el-option :label="$t('version.statusOffline')" :value="-1" />
                <el-option :label="$t('version.statusRolledBack')" :value="-2" />
              </el-select>
              <el-button type="primary" icon="el-icon-search" size="small" @click="handleVersionSearch">{{ $t('common.search') }}</el-button>
              <el-button icon="el-icon-refresh-left" size="small" @click="handleVersionReset">{{ $t('common.reset') }}</el-button>
            </div>
            <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAddVersion">{{ $t('version.add') }}</el-button>
          </div>

          <el-table v-loading="versionLoading" :data="versionList" stripe border style="width:100%;margin-top:12px" size="small">
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
                <el-button v-if="row.flag === 0" size="mini" type="text" @click="handleEditVersion(row)"><i class="el-icon-edit-outline" /> {{ $t('common.edit') }}</el-button>
                <el-button v-if="row.flag === 0" size="mini" type="text" class="danger-text" @click="handleDeleteVersion(row)"><i class="el-icon-delete" /> {{ $t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="versionTotal > 0" class="pagination-wrapper">
            <el-pagination :current-page="versionPage" :page-sizes="[10,20,50]" :page-size="versionPageSize" layout="total, sizes, prev, pager, next" :total="versionTotal" @size-change="v => { versionPageSize = v; versionPage = 1; loadVersions() }" @current-change="v => { versionPage = v; loadVersions() }" />
          </div>
        </el-tab-pane>

        <!-- ========== 安装包管理 ========== -->
        <el-tab-pane :label="$t('version.tabPackages')" name="packages">
          <div class="tab-toolbar">
            <div class="filter-row">
              <el-select v-model="pkgFilterVersionId" :placeholder="$t('version.selectVersion')" size="small" filterable clearable style="width:280px" @change="loadPackages">
                <el-option v-for="v in allVersions" :key="v.id" :label="`${v.appKey} - ${v.versionName} (${v.platform})`" :value="v.id" />
              </el-select>
            </div>
            <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAddPackage">{{ $t('version.addPackage') }}</el-button>
          </div>
          <el-table v-loading="pkgLoading" :data="pkgList" stripe border style="width:100%;margin-top:12px" size="small">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bucketName" :label="$t('version.bucketName')" show-overflow-tooltip />
            <el-table-column prop="objectName" :label="$t('version.objectName')" show-overflow-tooltip />
            <el-table-column prop="md5" :label="$t('version.md5')" width="280">
              <template slot-scope="{ row }"><code class="mono-text">{{ row.md5 || '-' }}</code></template>
            </el-table-column>
            <el-table-column prop="downloadUrl" :label="$t('version.downloadUrl')" width="120">
              <template slot-scope="{ row }">
                <el-button v-if="row.downloadUrl" size="mini" type="text" @click="copyText(row.downloadUrl)"><i class="el-icon-link" /> URL</el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="190" fixed="right">
              <template slot-scope="{ row }">
                <el-button size="mini" type="text" @click="handleDownloadPackage(row)"><i class="el-icon-download" /> {{ $t('version.download') }}</el-button>
                <el-button size="mini" type="text" @click="handleEditPackage(row)"><i class="el-icon-edit-outline" /> {{ $t('common.edit') }}</el-button>
                <el-button size="mini" type="text" class="danger-text" @click="handleDeletePackage(row)"><i class="el-icon-delete" /> {{ $t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ========== 兼容策略 ========== -->
        <el-tab-pane :label="$t('version.tabPolicies')" name="policies">
          <div class="tab-toolbar">
            <div class="filter-row">
              <el-select v-model="policySearch.sourceAppKeyEquals" :placeholder="$t('version.sourceAppKey')" size="small" filterable clearable style="width:140px" @change="handlePolicySearch">
                <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
              </el-select>
              <el-select v-model="policySearch.targetAppKeyEquals" :placeholder="$t('version.targetAppKey')" size="small" filterable clearable style="width:140px" @change="handlePolicySearch">
                <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
              </el-select>
              <el-select v-model="policySearch.targetPlatformEquals" :placeholder="$t('version.targetPlatform')" size="small" clearable style="width:120px" @change="handlePolicySearch">
                <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
              </el-select>
              <el-select v-model="policySearch.actionEquals" :placeholder="$t('version.action')" size="small" clearable style="width:120px" @change="handlePolicySearch">
                <el-option :label="$t('version.actionForce')" :value="1" />
                <el-option :label="$t('version.actionRecommend')" :value="2" />
                <el-option :label="$t('version.actionNotify')" :value="3" />
              </el-select>
              <el-select v-model="policySearch.statusFilter" :placeholder="$t('version.status')" size="small" clearable style="width:110px" @change="handlePolicySearch">
                <el-option :label="$t('version.enabled')" value="enabled" />
                <el-option :label="$t('version.disabled')" value="disabled" />
              </el-select>
              <el-button type="primary" icon="el-icon-search" size="small" @click="handlePolicySearch">{{ $t('common.search') }}</el-button>
              <el-button icon="el-icon-refresh-left" size="small" @click="handlePolicyReset">{{ $t('common.reset') }}</el-button>
            </div>
            <div class="filter-row">
              <el-button icon="el-icon-aim" size="small" @click="openUpgradeCheck">{{ $t('version.upgradeCheck') }}</el-button>
              <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAddPolicy">{{ $t('version.addPolicy') }}</el-button>
            </div>
          </div>
          <el-table v-loading="policyLoading" :data="policyList" stripe border style="width:100%;margin-top:12px" size="small">
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
                <el-button size="mini" type="text" @click="handleEditPolicy(row)"><i class="el-icon-edit-outline" /> {{ $t('common.edit') }}</el-button>
                <el-button size="mini" type="text" :class="row.flag === 1 ? 'warning-text' : 'success-text'" @click="handleTogglePolicy(row)">
                  <i :class="row.flag === 1 ? 'el-icon-video-pause' : 'el-icon-video-play'" /> {{ row.flag === 1 ? $t('version.disable') : $t('version.enable') }}
                </el-button>
                <el-button size="mini" type="text" class="danger-text" @click="handleDeletePolicy(row)"><i class="el-icon-delete" /> {{ $t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="policyTotal > 0" class="pagination-wrapper">
            <el-pagination :current-page="policyPage" :page-sizes="[10,20,50]" :page-size="policyPageSize" layout="total, sizes, prev, pager, next" :total="policyTotal" @size-change="v => { policyPageSize = v; policyPage = 1; loadPolicies() }" @current-change="v => { policyPage = v; loadPolicies() }" />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- ========== 版本编辑弹窗 ========== -->
      <el-dialog :title="versionForm.id ? $t('version.editVersion') : $t('version.addVersion')" :visible.sync="versionDialogVisible" width="600px" :close-on-click-modal="false" @close="resetVersionForm">
        <el-form ref="versionForm" :model="versionForm" :rules="versionRules" label-width="100px">
          <el-form-item :label="$t('version.appKey')" prop="appKey">
            <el-input v-model="versionForm.appKey" :placeholder="$t('version.appKeyPh')" :disabled="!!versionForm.id" />
          </el-form-item>
          <div class="form-row">
            <el-form-item :label="$t('version.versionCode')" prop="versionCode" class="form-row-item">
              <el-input-number v-model="versionForm.versionCode" :min="1" :disabled="!!versionForm.id" style="width:100%" />
            </el-form-item>
            <el-form-item :label="$t('version.versionName')" prop="versionName" class="form-row-item">
              <el-input v-model="versionForm.versionName" :placeholder="$t('version.versionNamePh')" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item :label="$t('version.versionType')" prop="versionType" class="form-row-item">
              <el-select v-model="versionForm.versionType" style="width:100%">
                <el-option :label="$t('version.typeRelease')" :value="1" />
                <el-option :label="$t('version.typeBeta')" :value="2" />
                <el-option :label="$t('version.typeAlpha')" :value="3" />
                <el-option :label="$t('version.typeRC')" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('version.platform')" prop="platform" class="form-row-item">
              <el-select v-model="versionForm.platform" style="width:100%">
                <el-option label="Android" value="android" />
                <el-option label="iOS" value="ios" />
                <el-option label="Windows" value="windows" />
                <el-option label="macOS" value="macos" />
                <el-option label="Linux" value="linux" />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item :label="$t('version.releaseNote')">
            <el-input v-model="versionForm.releaseNote" type="textarea" :rows="3" :placeholder="$t('version.releaseNotePh')" />
          </el-form-item>
          <el-form-item :label="$t('version.buildInfo')">
            <el-input v-model="versionForm.buildInfo" type="textarea" :rows="2" :placeholder="$t('version.buildInfoPh')" />
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="versionDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveVersion">{{ $t('common.confirm') }}</el-button>
        </div>
      </el-dialog>

      <!-- ========== 版本详情抽屉 ========== -->
      <el-drawer :title="$t('version.versionDetail')" :visible.sync="detailDrawerVisible" size="600px" direction="rtl">
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
                  <el-button size="mini" type="text" @click="handleDownloadPackage(row)"><i class="el-icon-download" /></el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-drawer>

      <!-- ========== 安装包编辑弹窗（文件经统一 OSS 接口直传，无需手填存储坐标） ========== -->
      <el-dialog :title="pkgForm.id ? $t('version.editPackage') : $t('version.addPackage')" :visible.sync="pkgDialogVisible" width="540px" :close-on-click-modal="false" @close="resetPkgForm">
        <el-form ref="pkgForm" :model="pkgForm" :rules="pkgRules" label-width="100px">
          <el-form-item :label="$t('version.versionLabel')" prop="versionId">
            <el-select v-model="pkgForm.versionId" :disabled="!!pkgForm.id" filterable style="width:100%">
              <el-option v-for="v in allVersions" :key="v.id" :label="`${v.appKey} - ${v.versionName} (${v.platform})`" :value="v.id" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('version.packageFile')">
            <div v-if="pkgForm.objectName" class="pkg-uploaded">
              <i class="el-icon-document-checked" />
              <div class="pkg-uploaded-info">
                <span class="mono-text">{{ pkgForm.objectName }}</span>
                <span v-if="pkgForm.md5" class="pkg-uploaded-md5">MD5: {{ pkgForm.md5 }}</span>
              </div>
              <el-button type="text" :disabled="pkgUploading" @click="resetPkgFile">{{ $t('version.replaceFile') }}</el-button>
            </div>
            <el-upload v-else drag action="" :http-request="handlePkgUpload" :show-file-list="false" :disabled="pkgUploading" class="pkg-uploader">
              <i class="el-icon-upload" />
              <div class="el-upload__text">{{ $t('version.uploadTip') }}</div>
              <div slot="tip" class="el-upload__tip">{{ $t('version.uploadNote') }}</div>
            </el-upload>
            <el-progress v-if="pkgUploading" :percentage="pkgUploadPercent" :stroke-width="6" style="margin-top:8px" />
          </el-form-item>
          <el-form-item :label="$t('version.downloadUrl')">
            <el-input v-model="pkgForm.downloadUrl" :placeholder="$t('version.externalUrlPh')" />
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="pkgDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" :disabled="pkgUploading" @click="handleSavePackage">{{ $t('common.confirm') }}</el-button>
        </div>
      </el-dialog>

      <!-- ========== 兼容策略编辑弹窗（按触发条件/升级要求分组，底部实时预览规则） ========== -->
      <el-dialog :title="policyForm.id ? $t('version.editPolicy') : $t('version.addPolicy')" :visible.sync="policyDialogVisible" width="620px" :close-on-click-modal="false" @close="resetPolicyForm">
        <el-form ref="policyForm" :model="policyForm" :rules="policyRules" label-width="110px">
          <el-divider content-position="left">{{ $t('version.triggerSection') }}</el-divider>
          <el-form-item :label="$t('version.sourceAppKey')" prop="sourceAppKey">
            <el-select v-model="policyForm.sourceAppKey" filterable style="width:100%" :placeholder="$t('version.sourceAppKeyPh')">
              <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
            </el-select>
          </el-form-item>
          <div class="form-row">
            <el-form-item :label="$t('version.sourcePlatform')" prop="sourcePlatform" class="form-row-item">
              <el-select v-model="policyForm.sourcePlatform" style="width:100%">
                <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item :label="$t('version.limitSourceVersion')">
            <el-switch v-model="policyForm.sourceVersionLimit" />
            <el-input-number v-if="policyForm.sourceVersionLimit" v-model="policyForm.sourceVersionCode" :min="1" style="width:200px;margin-left:12px" />
            <div class="form-tip">{{ $t('version.sourceVersionCodeTip') }}</div>
          </el-form-item>
          <el-divider content-position="left">{{ $t('version.targetSection') }}</el-divider>
          <el-form-item :label="$t('version.targetAppKey')" prop="targetAppKey">
            <el-select v-model="policyForm.targetAppKey" filterable style="width:100%" :placeholder="$t('version.targetAppKeyPh')">
              <el-option v-for="key in appKeys" :key="key" :label="key" :value="key" />
            </el-select>
          </el-form-item>
          <div class="form-row">
            <el-form-item :label="$t('version.targetPlatform')" prop="targetPlatform" class="form-row-item">
              <el-select v-model="policyForm.targetPlatform" style="width:100%">
                <el-option label="Android" value="android" /><el-option label="iOS" value="ios" /><el-option label="Windows" value="windows" /><el-option label="macOS" value="macos" /><el-option label="Linux" value="linux" />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item :label="$t('version.minCompatVersion')" prop="minCompatVersion">
            <el-input-number v-model="policyForm.minCompatVersion" :min="1" style="width:100%" />
          </el-form-item>
          <el-form-item :label="$t('version.action')" prop="action">
            <el-radio-group v-model="policyForm.action" class="action-radio-group">
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
          <el-button @click="policyDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" @click="handleSavePolicy">{{ $t('common.confirm') }}</el-button>
        </div>
      </el-dialog>

      <!-- ========== 升级检查弹窗（模拟客户端升级探测） ========== -->
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
            <el-descriptions-item v-if="checkResult.downloadUrl" :label="$t('version.downloadUrl')">
              <el-button size="mini" type="text" @click="copyText(checkResult.downloadUrl)"><i class="el-icon-copy-document" /> {{ checkResult.downloadUrl }}</el-button>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div slot="footer">
          <el-button @click="checkDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="checkLoading" @click="handleCheckUpgrade">{{ $t('version.executeCheck') }}</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  queryVersions, saveVersion, deleteVersion, submitVersion, releaseVersion,
  offlineVersion, rollbackVersion, archiveVersion, getVersionDetail, listAppKeys,
  queryPackages, savePackage, deletePackage, listPackagesByVersion,
  queryPolicies, savePolicy, deletePolicy, enablePolicy, disablePolicy, checkUpgrade
} from '@/api/app-version'
import { uploadObject, getUploadProgress, deleteObject, downloadObject } from '@/api/oss'

export default {
  name: 'AppVersionAdmin',
  data() {
    return {
      activeTab: 'versions',
      // --- 版本列表 ---
      versionLoading: false, versionList: [], versionTotal: 0, versionPage: 1, versionPageSize: 10,
      allVersions: [],
      versionSearch: { appKeyEquals: '', versionNameLike: '', platformEquals: '', flagIn: [] },
      // --- 版本表单 ---
      versionDialogVisible: false, saving: false,
      versionForm: { id: null, appKey: '', versionCode: null, versionName: '', versionType: 1, platform: 'android', releaseNote: '', buildInfo: '' },
      versionRules: {
        appKey: [{ required: true, message: this.$t('version.appKeyRequired'), trigger: 'blur' }],
        versionCode: [{ required: true, message: this.$t('version.versionCodeRequired'), trigger: 'blur' }],
        versionName: [{ required: true, message: this.$t('version.versionNameRequired'), trigger: 'blur' }],
        versionType: [{ required: true, message: this.$t('version.versionTypeRequired'), trigger: 'change' }],
        platform: [{ required: true, message: this.$t('version.platformRequired'), trigger: 'change' }]
      },
      // --- 详情抽屉 ---
      detailDrawerVisible: false, detailData: null,
      // --- 安装包 ---
      pkgLoading: false, pkgList: [], pkgFilterVersionId: null,
      pkgDialogVisible: false, pkgUploading: false, pkgUploadPercent: 0,
      pkgForm: { id: null, versionId: null, bucketName: '', objectName: '', md5: '', downloadUrl: '' },
      pkgRules: {
        versionId: [{ required: true, message: this.$t('version.versionRequired'), trigger: 'change' }]
      },
      // --- 兼容策略 ---
      policyLoading: false, policyList: [], policyTotal: 0, policyPage: 1, policyPageSize: 10,
      appKeys: [],
      policySearch: { sourceAppKeyEquals: '', targetAppKeyEquals: '', targetPlatformEquals: '', actionEquals: null, statusFilter: '' },
      policyDialogVisible: false,
      policyForm: { id: null, sourceAppKey: '', sourceVersionLimit: false, sourceVersionCode: null, sourcePlatform: 'android', targetAppKey: '', targetPlatform: 'android', minCompatVersion: null, action: 1 },
      policyRules: {
        sourceAppKey: [{ required: true, message: this.$t('version.sourceAppKeyRequired'), trigger: 'change' }],
        targetAppKey: [{ required: true, message: this.$t('version.targetAppKeyRequired'), trigger: 'change' }],
        targetPlatform: [{ required: true, message: this.$t('version.targetPlatformRequired'), trigger: 'change' }],
        minCompatVersion: [{ required: true, message: this.$t('version.minCompatVersionRequired'), trigger: 'blur' }],
        action: [{ required: true, message: this.$t('version.actionRequired'), trigger: 'change' }]
      },
      // --- 升级检查 ---
      checkDialogVisible: false, checkLoading: false, checkResult: null,
      checkForm: { appKey: '', platform: 'android', currentVersionCode: 1 }
    }
  },
  mounted() {
    this.loadVersions()
  },
  computed: {
    /** 弹窗底部实时预览规则语义，所见即所得 */
    policyPreview() {
      const f = this.policyForm
      if (!f.sourceAppKey && !f.targetAppKey) return '-'
      const source = `${f.sourceAppKey || '?'} (${f.sourcePlatform}) ${f.sourceVersionLimit && f.sourceVersionCode ? this.$t('version.summaryFrom', { code: f.sourceVersionCode }) : this.$t('version.summaryAny')}`
      const target = `${f.targetAppKey || '?'} (${f.targetPlatform}) ${this.$t('version.summaryRequire', { code: f.minCompatVersion || '?' })}`
      return `${source} → ${target}，${this.actionLabel(f.action)}`
    }
  },
  methods: {
    // ==================== 版本 ====================
    async loadVersions() {
      this.versionLoading = true
      try {
        const criteria = { createTimeOrder: { order: 'DESC' } }
        if (this.versionSearch.appKeyEquals) criteria.appKeyEquals = this.versionSearch.appKeyEquals
        if (this.versionSearch.versionNameLike) criteria.versionNameLike = this.versionSearch.versionNameLike
        if (this.versionSearch.platformEquals) criteria.platformEquals = this.versionSearch.platformEquals
        if (this.versionSearch.flagIn && this.versionSearch.flagIn.length) criteria.flagIn = this.versionSearch.flagIn
        const res = await queryVersions(criteria, this.versionPage, this.versionPageSize)
        const pd = res.data || {}
        this.versionList = pd.data || []
        this.versionTotal = pd.total || 0
      } catch (e) { console.error(e) } finally { this.versionLoading = false }
    },
    async loadAllVersions() {
      try {
        const res = await queryVersions({ createTimeOrder: { order: 'DESC' } }, 1, 500)
        this.allVersions = (res.data || {}).data || []
      } catch (e) { console.error(e) }
    },
    handleVersionSearch() { this.versionPage = 1; this.loadVersions() },
    handleVersionReset() { this.versionSearch = { appKeyEquals: '', versionNameLike: '', platformEquals: '', flagIn: [] }; this.versionPage = 1; this.loadVersions() },
    handleAddVersion() {
      this.versionForm = { id: null, appKey: '', versionCode: null, versionName: '', versionType: 1, platform: 'android', releaseNote: '', buildInfo: '' }
      this.versionDialogVisible = true
      this.$nextTick(() => this.$refs.versionForm && this.$refs.versionForm.clearValidate())
    },
    handleEditVersion(row) {
      this.versionForm = { id: row.id, appKey: row.appKey, versionCode: row.versionCode, versionName: row.versionName, versionType: row.versionType, platform: row.platform, releaseNote: row.releaseNote || '', buildInfo: row.buildInfo || '' }
      this.versionDialogVisible = true
      this.$nextTick(() => this.$refs.versionForm && this.$refs.versionForm.clearValidate())
    },
    async handleSaveVersion() {
      try { await this.$refs.versionForm.validate() } catch { return }
      this.saving = true
      try { await saveVersion(this.versionForm); this.versionDialogVisible = false; await this.loadVersions() } catch (e) { console.error(e) } finally { this.saving = false }
    },
    async handleDeleteVersion(row) {
      try {
        await this.$confirm(this.$t('version.confirmDeleteVersion', { name: row.versionName }), this.$t('common.tip'), { type: 'error' })
        await deleteVersion(row.id)
        if (this.versionList.length === 1 && this.versionPage > 1) this.versionPage--
        await this.loadVersions()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    async handleLifecycle(row, action) {
      const actionMap = { submit: this.$t('version.submit'), release: this.$t('version.release'), offline: this.$t('version.offline'), rollback: this.$t('version.rollback'), archive: this.$t('version.archive') }
      try {
        await this.$confirm(this.$t('version.confirmLifecycle', { action: actionMap[action], name: row.versionName }), this.$t('common.tip'), { type: 'warning' })
        const fnMap = { submit: submitVersion, release: releaseVersion, offline: offlineVersion, rollback: rollbackVersion, archive: archiveVersion }
        await fnMap[action](row.id)
        await this.loadVersions()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    async openDetail(row) {
      this.detailDrawerVisible = true
      this.detailData = null
      try {
        const res = await getVersionDetail(row.id)
        this.detailData = res.data
      } catch (e) { console.error(e) }
    },
    // ==================== 安装包 ====================
    async loadPackages() {
      if (!this.pkgFilterVersionId) { this.pkgList = []; return }
      this.pkgLoading = true
      try {
        const res = await listPackagesByVersion(this.pkgFilterVersionId)
        this.pkgList = res.data || []
      } catch (e) { console.error(e) } finally { this.pkgLoading = false }
    },
    handleAddPackage() {
      if (!this.allVersions.length) this.loadAllVersions()
      this.pkgForm = { id: null, versionId: this.pkgFilterVersionId, bucketName: '', objectName: '', md5: '', downloadUrl: '' }
      this.pkgUploading = false; this.pkgUploadPercent = 0
      this.pkgDialogVisible = true
      this.$nextTick(() => this.$refs.pkgForm && this.$refs.pkgForm.clearValidate())
    },
    handleEditPackage(row) {
      if (!this.allVersions.length) this.loadAllVersions()
      this.pkgForm = { id: row.id, versionId: row.versionId, bucketName: row.bucketName || '', objectName: row.objectName || '', md5: row.md5 || '', downloadUrl: row.downloadUrl || '' }
      this.pkgUploading = false; this.pkgUploadPercent = 0
      this.pkgDialogVisible = true
    },
    resetPkgFile() {
      this.$set(this.pkgForm, 'bucketName', '')
      this.$set(this.pkgForm, 'objectName', '')
      this.$set(this.pkgForm, 'md5', '')
    },
    /** 选中文件后经统一 OSS 接口直传，成功后回填存储坐标与 MD5 */
    async handlePkgUpload({ file }) {
      if (!this.pkgForm.versionId) {
        this.$message.warning(this.$t('version.versionRequired'))
        return
      }
      this.pkgUploading = true
      this.pkgUploadPercent = 0
      const previousKey = this.pkgForm.objectName || null
      const objectKey = `app-package/${this.pkgForm.versionId}/${Date.now()}_${file.name}`
      // 两阶段真实进度（与 OSS 模块一致）：浏览器传输阶段（XHR进度）占0~50%，
      // 服务端写入对象存储阶段每3秒轮询后端真实进度占50%~100%
      const taskId = Date.now().toString(36) + Math.random().toString(36).slice(2)
      let requestDone = false
      const timer = setInterval(() => {
        getUploadProgress(taskId).then(res => {
          const progress = res.data || {}
          if (progress.status === 'STORING' || progress.status === 'SUCCESS') {
            this.pkgUploadPercent = 50 + Math.round((progress.percentage || 0) / 2)
          }
          // RECEIVING：服务端仍在接收请求体，保持当前浏览器传输进度
        }).catch(() => {})
      }, 3000)
      try {
        const res = await uploadObject(file, objectKey, undefined, percent => {
          if (!requestDone) {
            this.pkgUploadPercent = Math.min(50, Math.round(percent / 2))
          }
        }, taskId)
        requestDone = true
        clearInterval(timer)
        const result = res.data || {}
        this.$set(this.pkgForm, 'bucketName', result.bucketName || '')
        this.$set(this.pkgForm, 'objectName', result.objectKey || objectKey)
        this.$set(this.pkgForm, 'md5', result.etag || '')
        this.pkgUploadPercent = 100
        this.$message.success(this.$t('version.uploadSuccess'))
        // 替换旧文件时同步清理 OSS 中的旧对象，避免残留孤儿文件
        if (previousKey && previousKey !== this.pkgForm.objectName) {
          deleteObject(previousKey, this.pkgForm.bucketName || undefined).catch(() => {})
        }
      } catch (e) {
        requestDone = true
        clearInterval(timer)
        console.error(e)
        this.$message.error(this.$t('version.uploadFailed'))
      } finally {
        this.pkgUploading = false
      }
    },
    async handleSavePackage() {
      try { await this.$refs.pkgForm.validate() } catch { return }
      if (!this.pkgForm.objectName && !this.pkgForm.downloadUrl) {
        this.$message.warning(this.$t('version.fileRequired'))
        return
      }
      this.saving = true
      try { await savePackage(this.pkgForm); this.pkgDialogVisible = false; await this.loadPackages() } catch (e) { console.error(e) } finally { this.saving = false }
    },
    async handleDeletePackage(row) {
      try {
        const tip = row.objectName ? this.$t('version.confirmDeletePackageOss') : this.$t('version.confirmDeletePackage')
        await this.$confirm(tip, this.$t('common.tip'), { type: 'error' })
        await deletePackage(row.id)
        // 记录删除后尽力清理 OSS 对象，失败仅提示不阻断
        if (row.objectName) {
          deleteObject(row.objectName, row.bucketName || undefined).catch(() => {
            this.$message.warning(this.$t('version.ossCleanFailed', { key: row.objectName }))
          })
        }
        await this.loadPackages()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    /** 优先走 OSS 预签名下载，外部托管的退回直链 */
    async handleDownloadPackage(row) {
      if (row.objectName) {
        try { await downloadObject(row.objectName, row.bucketName || undefined) } catch (e) { console.error(e) }
      } else if (row.downloadUrl) {
        window.open(row.downloadUrl, '_blank')
      }
    },
    resetVersionForm() { this.$refs.versionForm && this.$refs.versionForm.resetFields() },
    resetPkgForm() { this.$refs.pkgForm && this.$refs.pkgForm.resetFields() },
    resetPolicyForm() { this.$refs.policyForm && this.$refs.policyForm.resetFields() },
    // ==================== 兼容策略 ====================
    async loadAppKeys() {
      try {
        const res = await listAppKeys()
        this.appKeys = res.data || []
      } catch (e) { console.error(e) }
    },
    async loadPolicies() {
      this.policyLoading = true
      try {
        const criteria = { createTimeOrder: { order: 'DESC' } }
        if (this.policySearch.sourceAppKeyEquals) criteria.sourceAppKeyEquals = this.policySearch.sourceAppKeyEquals
        if (this.policySearch.targetAppKeyEquals) criteria.targetAppKeyEquals = this.policySearch.targetAppKeyEquals
        if (this.policySearch.targetPlatformEquals) criteria.targetPlatformEquals = this.policySearch.targetPlatformEquals
        if (this.policySearch.actionEquals) criteria.actionEquals = this.policySearch.actionEquals
        if (this.policySearch.statusFilter) criteria.flagIn = [this.policySearch.statusFilter === 'enabled' ? 1 : -1]
        const res = await queryPolicies(criteria, this.policyPage, this.policyPageSize)
        const pd = res.data || {}
        this.policyList = pd.data || []
        this.policyTotal = pd.total || 0
      } catch (e) { console.error(e) } finally { this.policyLoading = false }
    },
    handlePolicySearch() { this.policyPage = 1; this.loadPolicies() },
    handlePolicyReset() { this.policySearch = { sourceAppKeyEquals: '', targetAppKeyEquals: '', targetPlatformEquals: '', actionEquals: null, statusFilter: '' }; this.policyPage = 1; this.loadPolicies() },
    handleAddPolicy() {
      if (!this.appKeys.length) this.loadAppKeys()
      this.policyForm = { id: null, sourceAppKey: '', sourceVersionLimit: false, sourceVersionCode: null, sourcePlatform: 'android', targetAppKey: '', targetPlatform: 'android', minCompatVersion: null, action: 1 }
      this.policyDialogVisible = true
      this.$nextTick(() => this.$refs.policyForm && this.$refs.policyForm.clearValidate())
    },
    handleEditPolicy(row) {
      if (!this.appKeys.length) this.loadAppKeys()
      this.policyForm = { id: row.id, sourceAppKey: row.sourceAppKey, sourceVersionLimit: !!row.sourceVersionCode, sourceVersionCode: row.sourceVersionCode, sourcePlatform: row.sourcePlatform, targetAppKey: row.targetAppKey, targetPlatform: row.targetPlatform, minCompatVersion: row.minCompatVersion, action: row.action }
      this.policyDialogVisible = true
      this.$nextTick(() => this.$refs.policyForm && this.$refs.policyForm.clearValidate())
    },
    async handleSavePolicy() {
      try { await this.$refs.policyForm.validate() } catch { return }
      this.saving = true
      try {
        // 未开启版本限制时不传版本号，后端按不限版本处理；sourceVersionLimit 为纯前端字段不提交
        const payload = { ...this.policyForm, sourceVersionCode: this.policyForm.sourceVersionLimit ? this.policyForm.sourceVersionCode : null }
        delete payload.sourceVersionLimit
        await savePolicy(payload); this.policyDialogVisible = false; await this.loadPolicies()
      } catch (e) { console.error(e) } finally { this.saving = false }
    },
    async handleTogglePolicy(row) {
      const isEnable = row.flag !== 1
      try {
        await this.$confirm(this.$t('version.confirmTogglePolicy', { action: isEnable ? this.$t('version.enable') : this.$t('version.disable') }), this.$t('common.tip'), { type: 'warning' })
        if (isEnable) { await enablePolicy(row.id) } else { await disablePolicy(row.id) }
        await this.loadPolicies()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    async handleDeletePolicy(row) {
      try {
        await this.$confirm(this.$t('version.confirmDeletePolicy'), this.$t('common.tip'), { type: 'error' })
        await deletePolicy(row.id)
        if (this.policyList.length === 1 && this.policyPage > 1) this.policyPage--
        await this.loadPolicies()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    // ==================== 升级检查 ====================
    openUpgradeCheck() {
      if (!this.appKeys.length) this.loadAppKeys()
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
    // ==================== Tab ====================
    handleTabClick(tab) {
      if (tab.name === 'packages') { this.loadAllVersions() }
      if (tab.name === 'policies') {
        if (!this.appKeys.length) this.loadAppKeys()
        if (this.policyList.length === 0) { this.loadPolicies() }
      }
    },
    // ==================== 工具 ====================
    statusLabel(flag) {
      const map = { 0: this.$t('version.statusDraft'), 1: this.$t('version.statusRelease'), 2: this.$t('version.statusPending'), '-1': this.$t('version.statusOffline'), '-2': this.$t('version.statusRolledBack') }
      return map[flag] || flag
    },
    statusTagType(flag) {
      const map = { 0: 'info', 1: 'success', 2: 'warning', '-1': '', '-2': 'danger' }
      return map[flag] || 'info'
    },
    versionTypeLabel(type) {
      const map = { 1: this.$t('version.typeRelease'), 2: this.$t('version.typeBeta'), 3: this.$t('version.typeAlpha'), 4: this.$t('version.typeRC') }
      return map[type] || type
    },
    platformTagType(p) {
      const map = { android: 'success', ios: '', windows: 'warning', macos: 'info', linux: 'danger' }
      return map[p] || 'info'
    },
    actionLabel(a) {
      const map = { 1: this.$t('version.actionForce'), 2: this.$t('version.actionRecommend'), 3: this.$t('version.actionNotify') }
      return map[a] || a
    },
    actionDesc(a) {
      const map = { 1: this.$t('version.actionForceDesc'), 2: this.$t('version.actionRecommendDesc'), 3: this.$t('version.actionNotifyDesc') }
      return map[a] || ''
    },
    actionTagType(a) { return { 1: 'danger', 2: 'warning', 3: 'info' }[a] || 'info' },
    formatTime(t) {
      if (!t) return '-'
      const d = new Date(t); const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },
    async copyText(text) {
      try { await navigator.clipboard.writeText(text); this.$message.success(this.$t('version.copySuccess')) } catch {
        const ta = document.createElement('textarea'); ta.value = text; document.body.appendChild(ta); ta.select(); document.execCommand('copy'); document.body.removeChild(ta)
        this.$message.success(this.$t('version.copySuccess'))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.version-admin {
  padding: 0;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 24px 30px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f1f5f9 100%);
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.page-title {
  margin: 0;
  font-size: 21px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.page-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 13px;
  color: #94a3b8;
}

.version-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

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

  .form-row-item {
    flex: 1;
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.mono-text {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
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

.check-result {
  margin-top: 16px;
}

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

.pkg-uploader {
  width: 100%;

  ::v-deep .el-upload {
    width: 100%;
  }

  ::v-deep .el-upload-dragger {
    width: 100%;
  }
}

.pkg-uploaded {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 6px;

  > i {
    font-size: 20px;
    color: #67c23a;
  }

  .pkg-uploaded-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .mono-text {
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 100%;
    }
  }

  .pkg-uploaded-md5 {
    font-size: 12px;
    color: #909399;
  }
}
</style>
