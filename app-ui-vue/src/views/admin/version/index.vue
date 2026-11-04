<template>
  <div class="app-container">
    <div class="version-admin">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-left">
          <h2 class="page-title"><i class="el-icon-box" /> {{ $t('version.title') }}</h2>
          <span class="page-subtitle">{{ $t('version.subtitle') }}</span>
        </div>
        <el-button size="small" icon="el-icon-question" circle @click="helpDialogVisible = true" />
      </div>

      <!-- 主标签页 -->
      <el-tabs v-model="activeTab" class="version-tabs" @tab-click="handleTabClick">
        <el-tab-pane :label="$t('version.tabVersions')" name="versions">
          <VersionList ref="versionList" :app-keys="appKeys" @version-changed="onVersionChanged" />
        </el-tab-pane>
        <el-tab-pane :label="$t('version.tabPackages')" name="packages">
          <PackageList ref="packageList" :all-versions="allVersions" />
        </el-tab-pane>
        <el-tab-pane :label="$t('version.tabPolicies')" name="policies">
          <PolicyList ref="policyList" :app-keys="appKeys" />
        </el-tab-pane>
      </el-tabs>

      <!-- 使用帮助弹窗 -->
      <el-dialog :title="$t('version.helpTitle')" :visible.sync="helpDialogVisible" width="640px" :close-on-click-modal="true" class="help-dialog">
        <div class="help-steps">
          <div v-for="step in helpSteps" :key="step.num" class="help-step">
            <div class="help-step-num">{{ step.num }}</div>
            <div class="help-step-body">
              <div class="help-step-title">{{ step.title }}</div>
              <div class="help-step-desc">{{ step.desc }}</div>
            </div>
          </div>
        </div>
        <div class="help-tip">
          <i class="el-icon-info" /> {{ $t('version.helpTip') }}
        </div>
        <div slot="footer">
          <el-button type="primary" @click="helpDialogVisible = false">{{ $t('common.confirm') }}</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {listAppKeys, queryVersions} from '@/api/app-version'
import VersionList from './VersionList'
import PackageList from './PackageList'
import PolicyList from './PolicyList'

export default {
  name: 'AppVersionAdmin',
  components: { VersionList, PackageList, PolicyList },
  data() {
    return {
      activeTab: 'versions',
      appKeys: [],
      allVersions: [],
      helpDialogVisible: false
    }
  },
  provide() {
    return {
      statusLabel: this.statusLabel,
      statusTagType: this.statusTagType,
      versionTypeLabel: this.versionTypeLabel,
      platformTagType: this.platformTagType,
      formatTime: this.formatTime,
      actionLabel: this.actionLabel,
      actionDesc: this.actionDesc,
      actionTagType: this.actionTagType,
      copyText: this.copyText
    }
  },
  computed: {
    /** 使用帮助步骤数据 */
    helpSteps() {
      return [
        { num: 1, title: this.$t('version.helpStep1Title'), desc: this.$t('version.helpStep1Desc') },
        { num: 2, title: this.$t('version.helpStep2Title'), desc: this.$t('version.helpStep2Desc') },
        { num: 3, title: this.$t('version.helpStep3Title'), desc: this.$t('version.helpStep3Desc') },
        { num: 4, title: this.$t('version.helpStep4Title'), desc: this.$t('version.helpStep4Desc') },
        { num: 5, title: this.$t('version.helpStep5Title'), desc: this.$t('version.helpStep5Desc') }
      ]
    }
  },
  mounted() {
    this.loadAppKeys()
  },
  methods: {
    async loadAppKeys() {
      try {
        const res = await listAppKeys()
        this.appKeys = res.data || []
      } catch (e) { console.error(e) }
    },
    async loadAllVersions() {
      try {
        const res = await queryVersions({ createTimeOrder: { order: 'DESC' }}, 1, 500)
        this.allVersions = (res.data || {}).data || []
      } catch (e) { console.error(e) }
    },
    /** 版本变更后刷新全量版本列表，供安装包选择器使用 */
    onVersionChanged() {
      this.loadAllVersions()
    },
    handleTabClick(tab) {
      if (tab.name === 'packages') { this.loadAllVersions() }
      if (tab.name === 'policies') {
        const policyList = this.$refs.policyList
        if (policyList && policyList.list.length === 0) { policyList.loadPolicies() }
      }
    },
    // ==================== 共享工具方法（通过 provide 注入子组件） ====================
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

.help-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.help-step {
  display: flex;
  gap: 14px;
  align-items: flex-start;

  .help-step-num {
    flex-shrink: 0;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409eff, #66b1ff);
    color: #fff;
    font-weight: 600;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }

  .help-step-body {
    flex: 1;
    min-width: 0;
  }

  .help-step-title {
    font-weight: 600;
    font-size: 14px;
    color: #1e293b;
    margin-bottom: 4px;
  }

  .help-step-desc {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
  }
}

.help-tip {
  margin-top: 18px;
  padding: 10px 14px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  font-size: 13px;
  color: #409eff;

  i {
    margin-right: 4px;
  }
}
</style>
