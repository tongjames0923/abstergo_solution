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
            <el-table-column prop="id" label="ID" width="80" align="center" />
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
            <el-table-column :label="$t('thirdParty.operation')" width="150" align="center" fixed="right">
              <template slot-scope="{ row }">
                <el-button size="mini" type="text" class="detail-text" @click="handleDetail(row)">
                  <i class="el-icon-view" /> {{ $t('thirdParty.detail') }}
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
          <el-form-item :label="$t('thirdParty.appSecret')" prop="appSecret">
            <el-input
              v-model="appForm.appSecret"
              :placeholder="$t('thirdParty.appSecretPlaceholder')"
              maxlength="64"
              show-word-limit
              type="password"
              autocomplete="new-password"
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
          <el-descriptions-item :label="'ID'">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.appId')">{{ detailData.appId || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('thirdParty.appSecret')">
            <code class="secret-text">{{ detailData.appSecret || '-' }}</code>
            <div class="secret-tip">{{ $t('thirdParty.appSecretPlaceholder') }}</div>
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
    </div>
  </div>
</template>

<script>
import {
  deleteThirdPartyApp,
  getLoginGrants,
  getThirdPartyApp,
  queryThirdPartyApps,
  saveThirdPartyApp
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
        appSecret: '',
        grantType: '',
        proxyId: null
      },
      formRules: {
        appSecret: [
          { required: true, message: this.$t('thirdParty.appSecretRequired'), trigger: 'blur' }
        ],
        grantType: [
          { required: true, message: this.$t('thirdParty.grantTypeRequired'), trigger: 'change' }
        ],
        proxyId: [
          { required: true, message: this.$t('thirdParty.proxyIdRequired'), trigger: 'change' }
        ]
      },

      // 详情弹窗
      detailVisible: false,
      detailData: null
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
      this.appForm = { appId: '', appSecret: '', grantType: '', proxyId: null }
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
        await saveThirdPartyApp(payload)
        this.dialogVisible = false
        await this.loadData()
      } catch (error) {
        console.error('保存第三方应用失败:', error)
      } finally {
        this.saving = false
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

.danger-text {
  color: #ef4444 !important;
  &:hover { color: #f87171 !important; }
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

  .filter-row {
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
