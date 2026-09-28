<template>
  <div class="app-container">
    <div class="third-party-admin">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-left">
          <h2 class="page-title">
            <i class="el-icon-link title-icon" />
            {{ $t('thirdParty.title') }}
          </h2>
          <span class="page-subtitle">{{ $t('thirdParty.subtitle') }}</span>
        </div>
        <div class="header-right">
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="small"
            class="add-btn"
            @click="handleAdd"
          >
            {{ $t('thirdParty.add') }}
          </el-button>
        </div>
      </div>

      <!-- 搜索过滤区域 -->
      <div class="filter-panel">
        <div class="filter-row">
          <div class="filter-item">
            <label class="filter-label">{{ $t('thirdParty.appIdFilter') }}</label>
            <el-input
              v-model="searchForm.appId"
              :placeholder="$t('thirdParty.appIdFilterPlaceholder')"
              size="small"
              clearable
              prefix-icon="el-icon-search"
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="filter-item">
            <label class="filter-label">{{ $t('thirdParty.proxyFilter') }}</label>
            <el-input
              v-model="searchForm.proxyId"
              :placeholder="$t('thirdParty.proxyFilterPlaceholder')"
              size="small"
              clearable
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="filter-actions">
            <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">
              {{ $t('common.search') }}
            </el-button>
            <el-button icon="el-icon-refresh-left" size="small" @click="handleReset">
              {{ $t('common.reset') }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 列表 -->
      <div class="table-panel">
        <el-skeleton v-if="loading" :rows="6" animated />
        <template v-else>
          <el-table
            :data="tableData"
            border
            stripe
            style="width: 100%"
            :header-cell-style="{ background: '#f8fafc', color: '#334155', fontWeight: 600 }"
          >
            <el-table-column prop="id" :label="$t('thirdParty.id')" width="80" align="center" />
            <el-table-column prop="appId" :label="$t('thirdParty.appId')" min-width="180" show-overflow-tooltip />
            <el-table-column :label="$t('thirdParty.grantType')" width="140" align="center">
              <template slot-scope="{ row }">
                <el-tag size="mini" type="info">{{ grantTypeLabel(row.grantType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="proxyId" :label="$t('thirdParty.proxyId')" width="120" align="center" />
            <el-table-column :label="$t('thirdParty.flag')" width="110" align="center">
              <template slot-scope="{ row }">
                <el-tag :type="statusType(row.flag)" size="mini">{{ statusText(row.flag) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('thirdParty.createTime')" width="160" align="center">
              <template slot-scope="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="updateTime" :label="$t('thirdParty.updateTime')" width="160" align="center">
              <template slot-scope="{ row }">{{ formatTime(row.updateTime) }}</template>
            </el-table-column>
            <el-table-column :label="$t('thirdParty.operation')" width="210" align="center" fixed="right">
              <template slot-scope="{ row }">
                <el-button size="mini" type="text" class="detail-text" @click="handleDetail(row)">
                  <i class="el-icon-view" /> {{ $t('thirdParty.detail') }}
                </el-button>
                <el-button size="mini" type="text" class="test-text" @click="handleFillTest(row)">
                  <i class="el-icon-cpu" /> {{ $t('thirdParty.test.rowBtn') }}
                </el-button>
                <el-button
                  size="mini"
                  type="text"
                  class="danger-text"
                  @click="handleDelete(row)"
                >
                  <i class="el-icon-delete" /> {{ $t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
            <template slot="empty">
              <div class="empty-state">
                <div class="empty-graphic">
                  <svg width="96" height="96" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="18" y="28" width="64" height="44" rx="6" stroke="#cbd5e1" stroke-width="2" fill="none" />
                    <circle cx="50" cy="50" r="12" stroke="#94a3b8" stroke-width="2" fill="#f8fafc" />
                    <path d="M44 50 h12 M50 44 v12" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" />
                  </svg>
                </div>
                <p class="empty-title">{{ $t('thirdParty.emptyTitle') }}</p>
                <p class="empty-desc">{{ $t('thirdParty.emptyDesc') }}</p>
              </div>
            </template>
          </el-table>

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination-wrapper">
            <el-pagination
              :current-page="pageNum"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </div>

      <!-- Token 连通性测试 -->
      <div ref="testPanel" class="test-panel">
        <div class="test-panel-header">
          <h3 class="test-title">
            <i class="el-icon-cpu test-title-icon" />
            {{ $t('thirdParty.test.title') }}
          </h3>
          <span class="test-subtitle">{{ $t('thirdParty.test.subtitle') }}</span>
        </div>

        <el-alert
          :title="$t('thirdParty.test.guideTitle')"
          type="info"
          :closable="false"
          class="test-guide"
        >
          <ol class="guide-list">
            <li>{{ $t('thirdParty.test.guideStep1') }}</li>
            <li>{{ $t('thirdParty.test.guideStep2') }}</li>
            <li>{{ $t('thirdParty.test.guideStep3') }}</li>
            <li>{{ $t('thirdParty.test.guideStep4') }}</li>
            <li>{{ $t('thirdParty.test.guideStep5') }}</li>
          </ol>
          <p class="guide-tip">
            <i class="el-icon-warning-outline" /> {{ $t('thirdParty.test.clockTip') }}
          </p>
        </el-alert>

        <div class="test-form-row">
          <div class="filter-item">
            <label class="filter-label">{{ $t('thirdParty.test.appId') }}</label>
            <el-input
              v-model="testForm.appId"
              :placeholder="$t('thirdParty.test.appIdPlaceholder')"
              size="small"
              clearable
            />
          </div>
          <div class="filter-item">
            <label class="filter-label">{{ $t('thirdParty.test.appSecret') }}</label>
            <el-input
              v-model="testForm.appSecret"
              :placeholder="$t('thirdParty.test.appSecretPlaceholder')"
              size="small"
              show-password
            />
          </div>
          <div class="filter-actions">
            <el-button
              type="primary"
              icon="el-icon-video-play"
              size="small"
              :loading="testing"
              @click="handleTest"
            >
              {{ testing ? $t('thirdParty.test.testing') : $t('thirdParty.test.startTest') }}
            </el-button>
            <el-button icon="el-icon-refresh-left" size="small" @click="resetTest">
              {{ $t('common.reset') }}
            </el-button>
          </div>
        </div>

        <div v-if="testResult" class="test-result">
          <el-alert
            :type="testResult.passed ? 'success' : 'error'"
            :title="testResult.passed ? $t('thirdParty.test.passed') : $t('thirdParty.test.failed')"
            :closable="false"
            show-icon
            class="test-summary"
          />
          <div class="test-steps">
            <div v-for="step in testResult.steps" :key="step.key" class="test-step">
              <el-tag :type="testStepTagType(step.status)" size="mini">{{ testStepStatusText(step.status) }}</el-tag>
              <span class="step-label">{{ testStepLabel(step.key) }}</span>
              <span v-if="step.detail" class="step-detail">{{ step.detail }}</span>
            </div>
          </div>
          <el-descriptions
            v-if="testResult.user"
            :column="2"
            border
            size="small"
            class="test-user-descriptions"
          >
            <el-descriptions-item :label="$t('thirdParty.test.userName')">{{ testResult.user.userName || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('thirdParty.test.nickName')">{{ testResult.user.nickName || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('thirdParty.test.userId')">{{ testResult.user.id || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('thirdParty.test.accountStatus')">{{ testResult.user.accountStatusStr || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 新增弹窗 -->
      <el-dialog
        :title="$t('thirdParty.addTitle')"
        :visible.sync="dialogVisible"
        width="560px"
        :close-on-click-modal="false"
        @close="resetForm"
      >
        <el-form
          ref="appForm"
          :model="appForm"
          :rules="formRules"
          label-width="100px"
          class="app-dialog-form"
        >
          <el-form-item :label="$t('thirdParty.appId')" prop="appId">
            <el-input
              v-model="appForm.appId"
              :placeholder="$t('thirdParty.appIdPlaceholder')"
              maxlength="64"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('thirdParty.appSecret')">
            <el-input
              :value="$t('thirdParty.appSecretAuto')"
              disabled
              :placeholder="$t('thirdParty.appSecretPlaceholder')"
            />
          </el-form-item>
          <el-form-item :label="$t('thirdParty.grantType')" prop="grantType">
            <el-select
              v-model="appForm.grantType"
              :placeholder="$t('thirdParty.grantTypePlaceholder')"
              style="width: 100%;"
            >
              <el-option
                v-for="opt in grantTypeOptions"
                :key="opt.value"
                :label="grantTypeLabel(opt.value)"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('thirdParty.proxyId')" prop="proxyId">
            <el-select
              v-model="appForm.proxyId"
              :placeholder="$t('thirdParty.proxyIdPlaceholder')"
              filterable
              style="width: 100%;"
              :loading="proxyLoading"
            >
              <el-option
                v-for="u in proxyUserOptions"
                :key="u.value"
                :label="u.label"
                :value="u.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ saving ? $t('thirdParty.saving') : $t('common.confirm') }}
          </el-button>
        </div>
      </el-dialog>

      <!-- 详情弹窗（只读） -->
      <el-dialog
        :title="$t('thirdParty.detailTitle')"
        :visible.sync="detailVisible"
        width="560px"
        :close-on-click-modal="false"
      >
        <el-descriptions v-if="detailData" :column="1" border size="small" class="detail-descriptions">
          <el-descriptions-item :label="$t('thirdParty.id')">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.appId')">{{ detailData.appId || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.appSecret')">
            <span class="secret-text">{{ $t('thirdParty.appSecretNotShow') }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.grantType')">
            {{ grantTypeLabel(detailData.grantType) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.proxyId')">
            {{ detailData.proxyId }}
            <span v-if="proxyName(detailData.proxyId)" class="proxy-name">（{{ proxyName(detailData.proxyId) }}）</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.flag')">
            <el-tag :type="statusType(detailData.flag)" size="mini">{{ statusText(detailData.flag) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.createBy')">{{ detailData.createBy || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.updateBy')">{{ detailData.updateBy || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.createTime')">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.updateTime')">{{ formatTime(detailData.updateTime) }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>

      <!-- 创建后一次性展示应用密钥 -->
      <el-dialog
        :title="$t('thirdParty.secretGeneratedTitle')"
        :visible.sync="secretVisible"
        width="520px"
        :close-on-click-modal="false"
      >
        <div class="secret-generated">
          <p class="secret-tip">{{ $t('thirdParty.secretGeneratedTip') }}</p>
          <el-input
            :value="generatedSecret"
            type="textarea"
            :rows="3"
            readonly
            class="secret-textarea"
          />
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="secretVisible = false">{{ $t('common.close') }}</el-button>
          <el-button type="primary" @click="copySecret">{{ $t('thirdParty.copySecret') }}</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  buildThirdPartyCode,
  deleteThirdPartyApp,
  getLoginGrants,
  getThirdPartyApp,
  getUserInfoByToken,
  loginByGrantType,
  queryThirdPartyApps,
  saveThirdPartyApp,
  THIRD_PARTY_GRANT_TYPE
} from '@/api/third-party'
import { adminQueryUser } from '@/api/admin_user'

export default {
  name: 'ThirdPartyAdmin',
  data() {
    return {
      // 搜索
      searchForm: {
        appId: '',
        proxyId: ''
      },

      // 列表
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,

      // 新增弹窗
      dialogVisible: false,
      saving: false,
      proxyLoading: false,
      proxyUserOptions: [],
      // 授权类型选项由 /public/user/login-grants 动态下发，组件挂载时加载
      grantTypeOptions: [],
      appForm: {
        appId: '',
        grantType: '',
        proxyId: null
      },
      formRules: {
        grantType: [
          { required: true, message: this.$t('thirdParty.grantTypeRequired'), trigger: 'change' }
        ],
        proxyId: [
          { required: true, message: this.$t('thirdParty.proxyIdRequired'), trigger: 'change' }
        ]
      },

      // 详情弹窗
      detailVisible: false,
      detailData: null,

      // 创建后一次性展示的应用密钥
      secretVisible: false,
      generatedSecret: '',

      // Token 连通性测试
      testForm: {
        appId: '',
        appSecret: ''
      },
      testing: false,
      testResult: null
    }
  },
  mounted() {
    this.loadGrantTypes()
    this.loadData()
  },
  methods: {
    // ========== 数据加载 ==========
    async loadData() {
      this.loading = true
      try {
        const criteria = {}
        if (this.searchForm.appId) {
          criteria.EqAppId = this.searchForm.appId
        }
        if (this.searchForm.proxyId !== '' && this.searchForm.proxyId != null) {
          criteria.proxyUserIdList = [Number(this.searchForm.proxyId)]
        }

        const res = await queryThirdPartyApps(criteria, this.pageNum, this.pageSize)
        const pageData = res.data || {}
        this.tableData = pageData.data || []
        this.total = pageData.total || 0
      } catch (error) {
        console.error('加载第三方应用列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    // ========== 搜索 / 重置 ==========
    handleSearch() {
      this.pageNum = 1
      this.loadData()
    },

    handleReset() {
      this.searchForm = { appId: '', proxyId: '' }
      this.pageNum = 1
      this.loadData()
    },

    // ========== 分页 ==========
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.loadData()
    },

    handlePageChange(val) {
      this.pageNum = val
      this.loadData()
    },

    // ========== 代理用户下拉 ==========
    async loadProxyUsers() {
      this.proxyLoading = true
      try {
        const res = await adminQueryUser({ flagEquals: [1] }, 1, 1000)
        const list = (res.data && res.data.data) || []
        this.proxyUserOptions = list.map(u => ({ value: u.id, label: u.userName || u.id }))
      } catch (error) {
        console.error('加载代理用户失败:', error)
        this.proxyUserOptions = []
      } finally {
        this.proxyLoading = false
      }
    },

    proxyName(id) {
      const found = this.proxyUserOptions.find(o => o.value === id)
      return found ? found.label : ''
    },

    // ========== 授权类型下拉（来自登录方式下发接口） ==========
    async loadGrantTypes() {
      try {
        const res = await getLoginGrants()
        const list = res.data || []
        this.grantTypeOptions = list.map(g => ({
          value: g.grantType,
          labelKey: g.labelKey
        }))
      } catch (error) {
        console.error('加载授权类型失败:', error)
        this.grantTypeOptions = []
      }
    },

    // ========== 新增 ==========
    handleAdd() {
      this.appForm = { appId: '', grantType: '', proxyId: null }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.appForm && this.$refs.appForm.clearValidate()
      })
      this.loadProxyUsers()
      this.loadGrantTypes()
    },

    async handleSave() {
      try {
        await this.$refs.appForm.validate()
      } catch {
        return
      }

      this.saving = true
      try {
        // 应用标识留空时由后端自动生成，不传该字段
        const payload = { ...this.appForm }
        if (!payload.appId) {
          delete payload.appId
        }
        const res = await saveThirdPartyApp(payload)
        const rows = res.data
        const saved = Array.isArray(rows) ? rows[0] : rows
        const secret = saved && saved.appSecret
        this.dialogVisible = false
        if (secret) {
          this.generatedSecret = secret
          this.secretVisible = true
        }
        await this.loadData()
      } catch (error) {
        console.error('保存第三方应用失败:', error)
      } finally {
        this.saving = false
      }
    },

    // 复制一次性生成的密钥到剪贴板
    async copySecret() {
      try {
        await navigator.clipboard.writeText(this.generatedSecret)
        this.$message.success(this.$t('thirdParty.copySuccess'))
      } catch {
        this.$message.warning(this.$t('thirdParty.copyFailed'))
      }
    },

    resetForm() {
      if (this.$refs.appForm) {
        this.$refs.appForm.resetFields()
      }
    },

    // ========== 详情 ==========
    async handleDetail(row) {
      try {
        const res = await getThirdPartyApp(row.id)
        this.detailData = res.data || null
        if (!this.proxyUserOptions.length) {
          await this.loadProxyUsers()
        }
        this.detailVisible = true
      } catch (error) {
        console.error('加载第三方应用详情失败:', error)
      }
    },

    // ========== 删除 ==========
    async handleDelete(row) {
      try {
        await this.$confirm(
          this.$t('thirdParty.confirmDelete', { name: row.appId || row.id }),
          this.$t('common.tip'),
          { confirmButtonText: this.$t('common.delete'), cancelButtonText: this.$t('common.cancel'), type: 'warning' }
        )
        await deleteThirdPartyApp(row.id)
        if (this.tableData.length === 1 && this.pageNum > 1) {
          this.pageNum--
        }
        await this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除第三方应用失败:', error)
        }
      }
    },

    // ========== Token 连通性测试 ==========
    // 模拟第三方应用完整链路：生成授权码 → 换取访问令牌 → 用令牌获取用户信息
    async handleTest() {
      if (!this.testForm.appId || !this.testForm.appSecret) {
        this.$message.warning(this.$t('thirdParty.test.inputRequired'))
        return
      }
      this.testing = true
      const steps = [
        { key: 'code', status: 'running', detail: '' },
        { key: 'token', status: 'pending', detail: '' },
        { key: 'info', status: 'pending', detail: '' }
      ]
      this.testResult = { passed: false, steps, user: null }
      let current = 0
      const timestamp = Date.now()
      try {
        const code = buildThirdPartyCode(this.testForm.appId, this.testForm.appSecret, timestamp)
        steps[0].status = 'success'
        steps[0].detail = this.$t('thirdParty.test.codeDetail', { code: this.ellipsisMiddle(code), time: timestamp })

        current = 1
        steps[1].status = 'running'
        const loginRes = await loginByGrantType(THIRD_PARTY_GRANT_TYPE, {
          appId: this.testForm.appId,
          code,
          timestamp
        })
        const token = loginRes && loginRes.data && loginRes.data.token
        if (!loginRes || loginRes.code !== 200 || !token) {
          throw new Error((loginRes && loginRes.message) || this.$t('thirdParty.test.unknownError'))
        }
        steps[1].status = 'success'
        steps[1].detail = this.$t('thirdParty.test.tokenDetail', { token: this.ellipsisMiddle(token) })

        current = 2
        steps[2].status = 'running'
        const infoRes = await getUserInfoByToken(token)
        if (!infoRes || infoRes.code !== 200 || !infoRes.data) {
          throw new Error((infoRes && infoRes.message) || this.$t('thirdParty.test.unknownError'))
        }
        steps[2].status = 'success'
        steps[2].detail = this.$t('thirdParty.test.infoDetail', { user: infoRes.data.userName || '-' })
        this.testResult.user = infoRes.data
        this.testResult.passed = true
      } catch (error) {
        steps[current].status = 'failed'
        steps[current].detail = (error && error.message) || this.$t('thirdParty.test.unknownError')
        for (let i = current + 1; i < steps.length; i++) {
          steps[i].status = 'skipped'
        }
      } finally {
        this.testing = false
      }
    },

    // 列表行一键将应用标识填入测试表单
    handleFillTest(row) {
      this.testForm.appId = row.appId
      this.testForm.appSecret = ''
      this.testResult = null
      if (this.$refs.testPanel && this.$refs.testPanel.scrollIntoView) {
        this.$refs.testPanel.scrollIntoView({ behavior: 'smooth' })
      }
    },

    resetTest() {
      this.testForm = { appId: '', appSecret: '' }
      this.testResult = null
    },

    testStepLabel(key) {
      const map = {
        code: this.$t('thirdParty.test.stepCode'),
        token: this.$t('thirdParty.test.stepToken'),
        info: this.$t('thirdParty.test.stepInfo')
      }
      return map[key] || key
    },

    testStepStatusText(status) {
      const map = {
        pending: this.$t('thirdParty.test.statusPending'),
        running: this.$t('thirdParty.test.statusRunning'),
        success: this.$t('thirdParty.test.statusSuccess'),
        failed: this.$t('thirdParty.test.statusFailed'),
        skipped: this.$t('thirdParty.test.statusSkipped')
      }
      return map[status] || status
    },

    testStepTagType(status) {
      if (status === 'success') return 'success'
      if (status === 'failed') return 'danger'
      if (status === 'running') return ''
      return 'info'
    },

    // 长串（授权码/令牌）只展示首尾片段，避免撑破界面排版
    ellipsisMiddle(str) {
      if (!str || str.length <= 24) return str || ''
      return `${str.slice(0, 12)}...${str.slice(-8)}`
    },

    // ========== 展示辅助 ==========
    statusType(flag) {
      if (flag === 1) return 'success' // ACTIVE
      if (flag === -1) return 'info' // INACTIVE
      return 'danger' // DELETED (0)
    },

    statusText(flag) {
      if (flag === 1) return this.$t('thirdParty.active')
      if (flag === -1) return this.$t('thirdParty.inactive')
      return this.$t('thirdParty.deleted')
    },

    grantTypeLabel(value) {
      if (!value) return '-'
      const found = this.grantTypeOptions.find(o => o.value === value)
      if (found && found.labelKey) {
        const label = this.$t(found.labelKey)
        if (label && label !== found.labelKey) {
          return label
        }
      }
      return value
    },

    formatTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }
  }
}
</script>

<style lang="scss" scoped>
.third-party-admin {
  padding: 0;
  min-height: calc(100vh - 84px);
}

/* ========== 页面标题 ========== */
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

.title-icon {
  color: #0ea5e9;
  font-size: 23px;
}

.page-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 13px;
  color: #94a3b8;
}

.add-btn {
  border-radius: 8px;
  font-weight: 500;
  padding: 9px 18px;
}

/* ========== 搜索过滤 ========== */
.filter-panel {
  margin-bottom: 16px;
  padding: 18px 22px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 200px;
  flex: 1;
  max-width: 260px;
}

.filter-label {
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  letter-spacing: 0.02em;
}

.filter-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 1px;
}

/* ========== 表格面板 ========== */
.table-panel {
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
  padding: 20px;
}

::v-deep .filter-panel .el-input__inner,
::v-deep .filter-panel .el-select .el-input__inner {
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.25s;

  &:hover { border-color: #94a3b8; }
  &:focus {
    border-color: #0ea5e9;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.08);
  }
}

/* ========== 操作按钮 ========== */
.detail-text {
  color: #0ea5e9 !important;
  &:hover { color: #38bdf8 !important; }
}

.test-text {
  color: #8b5cf6 !important;
  &:hover { color: #a78bfa !important; }
}

.danger-text {
  color: #ef4444 !important;
  &:hover { color: #f87171 !important; }
}

/* ========== Token 连通性测试 ========== */
.test-panel {
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  padding: 20px;
}

.test-panel-header {
  margin-bottom: 14px;
}

.test-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-title-icon {
  color: #8b5cf6;
}

.test-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.test-guide {
  margin-bottom: 16px;

  ::v-deep .el-alert__description {
    width: 100%;
  }
}

.guide-list {
  margin: 6px 0 0;
  padding-left: 20px;
  font-size: 12px;
  line-height: 1.9;
  color: #475569;
}

.guide-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #b45309;
}

.test-form-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.test-result {
  margin-top: 14px;
}

.test-summary {
  margin-bottom: 12px;
}

.test-steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-step {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 13px;
}

.step-label {
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
}

.step-detail {
  color: #64748b;
  word-break: break-all;
}

.test-user-descriptions {
  margin-top: 14px;

  ::v-deep .el-descriptions-item__label {
    width: 110px;
    color: #64748b;
    font-weight: 500;
  }
}

/* ========== 分页 ========== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

/* ========== 弹窗表单 ========== */
.app-dialog-form {
  padding: 10px 20px 0;

  ::v-deep .el-input__inner,
  ::v-deep .el-textarea__inner {
    border-radius: 8px;
    border-color: #e2e8f0;
    transition: all 0.25s;

    &:focus {
      border-color: #0ea5e9;
      box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.08);
    }
  }

  ::v-deep .el-form-item {
    margin-bottom: 20px;
  }

  ::v-deep .el-form-item__label {
    font-weight: 500;
    color: #475569;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 20px 10px;
}

/* ========== 详情 ========== */
.detail-descriptions {
  ::v-deep .el-descriptions-item__label {
    width: 110px;
    color: #64748b;
    font-weight: 500;
  }
}

.secret-text {
  font-family: 'JetBrains Mono', 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  color: #334155;
  word-break: break-all;
}

.secret-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.proxy-name {
  color: #64748b;
  font-size: 12px;
}

/* ========== 空状态 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 20px;
}

.empty-graphic {
  margin-bottom: 16px;
  opacity: 0.7;
}

.empty-title {
  font-size: 15px;
  font-weight: 500;
  color: #64748b;
  margin: 0 0 6px;
}

.empty-desc {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .filter-row { gap: 12px; }
  .filter-item { min-width: 160px; }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .filter-row,
  .test-form-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    max-width: none;
  }

  .filter-actions {
    justify-content: flex-end;
  }
}
</style>
