<template>
  <div class="app-container">
    <div class="generic-api-admin">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-left">
          <h2 class="page-title">
            <i class="el-icon-connection title-icon" />
            {{ $t('genericApi.title') }}
          </h2>
          <span class="page-subtitle">{{ $t('genericApi.subtitle') }}</span>
        </div>
        <div class="header-right">
          <el-button
            type="success"
            icon="el-icon-position"
            size="small"
            class="test-btn"
            @click="openTestUrl('private')"
            style="margin-right: 8px;"
          >
            {{ $t('genericApi.testPrivate') }}
          </el-button>
          <el-button
            type="info"
            icon="el-icon-position"
            size="small"
            class="test-btn"
            @click="openTestUrl('public')"
            style="margin-right: 8px;"
          >
            {{ $t('genericApi.testPublic') }}
          </el-button>
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="small"
            class="add-btn"
            @click="handleAdd"
          >
            {{ $t('genericApi.add') }}
          </el-button>
        </div>
      </div>

      <!-- 搜索过滤区域 -->
      <div class="filter-panel">
        <div class="filter-row">
          <div class="filter-item">
            <label class="filter-label">{{ $t('genericApi.apiName') }}</label>
            <el-input
              v-model="searchForm.apiNameLike"
              :placeholder="$t('genericApi.apiNamePlaceholder')"
              size="small"
              clearable
              prefix-icon="el-icon-search"
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="filter-item">
            <label class="filter-label">{{ $t('genericApi.appName') }}</label>
            <el-input
              v-model="searchForm.appNameLike"
              :placeholder="$t('genericApi.appNamePlaceholder')"
              size="small"
              clearable
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="filter-item">
            <label class="filter-label">{{ $t('genericApi.interfaceName') }}</label>
            <el-input
              v-model="searchForm.interfaceNameLike"
              :placeholder="$t('genericApi.interfaceNamePlaceholder')"
              size="small"
              clearable
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="filter-item filter-item--status">
            <label class="filter-label">{{ $t('genericApi.status') }}</label>
            <el-select
              v-model="searchForm.flag"
              :placeholder="$t('genericApi.allStatus')"
              size="small"
              clearable
            >
              <el-option :label="$t('genericApi.enabled')" :value="1" />
              <el-option :label="$t('genericApi.disabled')" :value="0" />
            </el-select>
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

      <!-- 宫格卡片布局 -->
      <div class="grid-panel">
        <el-skeleton v-if="loading" :rows="6" animated />
        <div v-else-if="tableData.length > 0" class="api-grid">
          <div v-for="item in tableData" :key="item.id" class="api-card">
            <div class="card-header">
              <div class="card-title">
                <i class="el-icon-monitor card-icon"></i>
                <h3 class="api-name">{{ item.apiName }}</h3>
              </div>
              <div class="card-status">
                <el-tag :type="item.flag === 1 ? 'success' : 'info'" size="small">
                  {{ item.flag === 1 ? $t('genericApi.enabled') : $t('genericApi.disabled') }}
                </el-tag>
              </div>
            </div>
            <div class="card-body">
              <div class="card-section">
                <div class="section-title">API信息</div>
                <div class="info-item">
                  <label>ID:</label>
                  <span class="mono-text">{{ item.apiId }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('genericApi.appName') }}:</label>
                  <el-tag size="mini" type="info">{{ item.appName }}</el-tag>
                </div>
                <div class="info-item">
                  <label>{{ $t('genericApi.interfaceName') }}:</label>
                  <code class="mono-text mono-text--sm">{{ item.interfaceName }}</code>
                </div>
                <div class="info-item">
                  <label>{{ $t('genericApi.methodName') }}:</label>
                  <code class="mono-text mono-text--method">{{ item.methodName }}</code>
                </div>
                <div class="info-item">
                  <label>{{ $t('genericApi.version') }}:</label>
                  <span>{{ item.version || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('genericApi.needAuth') }}:</label>
                  <el-tag :type="item.needAuth ? 'warning' : 'info'" size="mini">
                    {{ item.needAuth ? $t('common.yes') : $t('common.no') }}
                  </el-tag>
                </div>
              </div>
              <div class="card-section">
                <div class="section-title">时间信息</div>
                <div class="info-item">
                  <label>{{ $t('genericApi.updateTime') }}:</label>
                  <span>{{ formatTime(item.updateTime) }}</span>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <el-button size="mini" type="text" class="test-api-btn" @click="openApiTestDialog(item)">
                <i class="el-icon-caret-right"></i> {{ $t('genericApi.test') }}
              </el-button>
              <el-button size="mini" type="text" class="edit-text" @click="handleEdit(item)">
                <i class="el-icon-edit-outline"></i> {{ $t('common.edit') }}
              </el-button>
              <el-button
                size="mini"
                type="text"
                :class="item.flag === 1 ? 'warning-text' : 'success-text'"
                @click="handleToggle(item)"
              >
                <i :class="item.flag === 1 ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
                {{ item.flag === 1 ? $t('genericApi.disable') : $t('genericApi.enable') }}
              </el-button>
              <el-button size="mini" type="text" class="danger-text" @click="handleDelete(item)">
                <i class="el-icon-delete"></i> {{ $t('common.delete') }}
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-graphic">
            <svg width="100" height="100" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="15" y="25" width="70" height="50" rx="6" stroke="#cbd5e1" stroke-width="2" fill="none" />
              <path d="M35 45 L45 55 L35 65" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              <line x1="52" y1="65" x2="65" y2="65" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" />
              <circle cx="75" cy="30" r="12" stroke="#e2e8f0" stroke-width="2" fill="#f8fafc" />
              <line x1="75" y1="25" x2="75" y2="35" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" />
              <line x1="70" y1="30" x2="80" y2="30" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" />
            </svg>
          </div>
          <p class="empty-title">{{ $t('genericApi.emptyTitle') }}</p>
          <p class="empty-desc">{{ $t('genericApi.emptyDesc') }}</p>
        </div>

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
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog
        :title="dialogType === 'add' ? $t('genericApi.addTitle') : $t('genericApi.editTitle')"
        :visible.sync="dialogVisible"
        width="620px"
        :close-on-click-modal="false"
        @close="resetForm"
      >
        <el-form
          ref="apiForm"
          :model="apiForm"
          :rules="formRules"
          label-width="110px"
          class="api-dialog-form"
        >
          <el-form-item :label="$t('genericApi.apiId')" prop="apiId">
            <el-input
              v-model="apiForm.apiId"
              :placeholder="$t('genericApi.apiIdPlaceholder')"
              :disabled="dialogType === 'edit'"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('genericApi.apiName')" prop="apiName">
            <el-input
              v-model="apiForm.apiName"
              :placeholder="$t('genericApi.apiNameInputPlaceholder')"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('genericApi.appName')" prop="appName">
            <el-input
              v-model="apiForm.appName"
              :placeholder="$t('genericApi.appNameInputPlaceholder')"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('genericApi.interfaceName')" prop="interfaceName">
            <el-input
              v-model="apiForm.interfaceName"
              :placeholder="$t('genericApi.interfaceNameInputPlaceholder')"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
          <el-form-item :label="$t('genericApi.methodName')" prop="methodName">
            <el-input
              v-model="apiForm.methodName"
              :placeholder="$t('genericApi.methodNamePlaceholder')"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          <div class="form-row">
            <el-form-item :label="$t('genericApi.version')" class="form-row-item">
              <el-input
                v-model="apiForm.version"
                :placeholder="$t('genericApi.versionPlaceholder')"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item :label="$t('genericApi.needAuth')" class="form-row-item">
              <el-switch
                v-model="apiForm.needAuth"
                :active-text="$t('common.yes')"
                :inactive-text="$t('common.no')"
              />
            </el-form-item>
          </div>
          <el-form-item :label="$t('genericApi.description')">
            <el-input
              v-model="apiForm.description"
              type="textarea"
              :placeholder="$t('genericApi.descriptionPlaceholder')"
              :rows="3"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ saving ? $t('genericApi.saving') : $t('common.confirm') }}
          </el-button>
        </div>
      </el-dialog>

      <!-- API测试对话框 -->
      <el-dialog
        :title="$t('genericApi.testDialogTitle', { apiName: currentApi.apiName })"
        :visible.sync="testDialogVisible"
        width="700px"
        :close-on-click-modal="false"
        @close="closeTestDialog"
      >
        <div class="test-dialog-content">
          <div class="test-info">
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item :label="$t('genericApi.apiId')">{{ currentApi.apiId }}</el-descriptions-item>
              <el-descriptions-item :label="$t('genericApi.needAuth')">
                <el-tag :type="currentApi.needAuth ? 'warning' : 'info'" size="mini">
                  {{ currentApi.needAuth ? $t('common.yes') : $t('common.no') }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('genericApi.appName')">{{ currentApi.appName }}</el-descriptions-item>
              <el-descriptions-item :label="$t('genericApi.interfaceName')">{{ currentApi.interfaceName }}</el-descriptions-item>
              <el-descriptions-item :label="$t('genericApi.methodName')">{{ currentApi.methodName }}</el-descriptions-item>
              <el-descriptions-item :label="$t('genericApi.version')">{{ currentApi.version || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="request-section">
            <div class="section-header">
              <h4>{{ $t('genericApi.requestBody') }}</h4>
              <el-button size="mini" type="primary" @click="executeApiTest" :loading="testLoading">
                <i class="el-icon-caret-right"></i> {{ $t('genericApi.executeTest') }}
              </el-button>
            </div>
            <el-input
              v-model="requestBody"
              type="textarea"
              :rows="8"
              placeholder="输入JSON格式的请求参数"
            />
          </div>

          <div class="response-section" v-if="testResult">
            <h4>{{ $t('genericApi.responseResult') }}</h4>
            <pre class="response-result">{{ testResult }}</pre>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  queryGenericApis,
  saveGenericApi,
  deleteGenericApi,
  enableGenericApi,
  disableGenericApi
} from '@/api/generic-api'
import request from "@/utils/request";


export default {
  name: 'GenericApiAdmin',
  data() {
    return {
      // 搜索
      searchForm: {
        apiNameLike: '',
        appNameLike: '',
        interfaceNameLike: '',
        flag: null
      },

      // 表格
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,

      // 弹窗
      dialogVisible: false,
      dialogType: 'add',
      saving: false,
      apiForm: {
        id: null,
        apiId: '',
        apiName: '',
        appName: '',
        interfaceName: '',
        methodName: '',
        version: '',
        needAuth: false,
        description: ''
      },
      formRules: {
        apiId: [
          { required: true, message: this.$t('genericApi.apiIdRequired'), trigger: 'blur' }
        ],
        apiName: [
          { required: true, message: this.$t('genericApi.apiNameRequired'), trigger: 'blur' }
        ],
        appName: [
          { required: true, message: this.$t('genericApi.appNameRequired'), trigger: 'blur' }
        ],
        interfaceName: [
          { required: true, message: this.$t('genericApi.interfaceNameRequired'), trigger: 'blur' }
        ],
        methodName: [
          { required: true, message: this.$t('genericApi.methodNameRequired'), trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  data() {
    return {
      // 搜索
      searchForm: {
        apiNameLike: '',
        appNameLike: '',
        interfaceNameLike: '',
        flag: null
      },

      // 表格
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,

      // 弹窗
      dialogVisible: false,
      dialogType: 'add',
      saving: false,
      apiForm: {
        id: null,
        apiId: '',
        apiName: '',
        appName: '',
        interfaceName: '',
        methodName: '',
        version: '',
        needAuth: false,
        description: ''
      },
      formRules: {
        apiId: [
          { required: true, message: this.$t('genericApi.apiIdRequired'), trigger: 'blur' }
        ],
        apiName: [
          { required: true, message: this.$t('genericApi.apiNameRequired'), trigger: 'blur' }
        ],
        appName: [
          { required: true, message: this.$t('genericApi.appNameRequired'), trigger: 'blur' }
        ],
        interfaceName: [
          { required: true, message: this.$t('genericApi.interfaceNameRequired'), trigger: 'blur' }
        ],
        methodName: [
          { required: true, message: this.$t('genericApi.methodNameRequired'), trigger: 'blur' }
        ]
      },

      // API测试对话框
      testDialogVisible: false,
      currentApi: {},
      requestBody: '{\n  \"param1\": \"value1\",\n  \"param2\": \"value2\"\n}',
      testResult: '',
      testLoading: false
    }
  },
  methods: {
    // ========== 数据加载 ==========
    async loadData() {
      this.loading = true
      try {
        const criteria = {}
        if (this.searchForm.apiNameLike) {
          criteria.apiNameLike = this.searchForm.apiNameLike
        }
        if (this.searchForm.appNameLike) {
          criteria.appNameLike = this.searchForm.appNameLike
        }
        if (this.searchForm.interfaceNameLike) {
          criteria.interfaceNameLike = this.searchForm.interfaceNameLike
        }
        if (this.searchForm.flag !== null && this.searchForm.flag !== '') {
          criteria.flagIn = [this.searchForm.flag]
        }
        // 默认按更新时间倒序
        criteria.updateTimeOrder = { order: 'DESC' }

        const res = await queryGenericApis(criteria, this.pageNum, this.pageSize)
        const pageData = res.data || {}
        this.tableData = pageData.data || []
        this.total = pageData.total || 0
      } catch (error) {
        console.error('加载泛化API列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    // ========== 搜索 ==========
    handleSearch() {
      this.pageNum = 1
      this.loadData()
    },

    handleReset() {
      this.searchForm = {
        apiNameLike: '',
        appNameLike: '',
        interfaceNameLike: '',
        flag: null
      }
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

    // ========== 新增/编辑 ==========
    handleAdd() {
      this.dialogType = 'add'
      this.apiForm = {
        id: null,
        apiId: '',
        apiName: '',
        appName: '',
        interfaceName: '',
        methodName: '',
        version: '',
        needAuth: false,
        description: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.apiForm && this.$refs.apiForm.clearValidate()
      })
    },

    handleEdit(row) {
      this.dialogType = 'edit'
      this.apiForm = {
        id: row.id,
        apiId: row.apiId,
        apiName: row.apiName,
        appName: row.appName,
        interfaceName: row.interfaceName,
        methodName: row.methodName,
        version: row.version || '',
        needAuth: row.needAuth || false,
        description: row.description || ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.apiForm && this.$refs.apiForm.clearValidate()
      })
    },

    async handleSave() {
      try {
        await this.$refs.apiForm.validate()
      } catch {
        return
      }

      this.saving = true
      try {
        await saveGenericApi({ ...this.apiForm })
        this.dialogVisible = false
        await this.loadData()
      } catch (error) {
        console.error('保存泛化API失败:', error)
      } finally {
        this.saving = false
      }
    },

    resetForm() {
      if (this.$refs.apiForm) {
        this.$refs.apiForm.resetFields()
      }
    },

    // ========== 启停用 ==========
    async handleToggle(row) {
      const isEnable = row.flag !== 1
      const action = isEnable ? this.$t('genericApi.enable') : this.$t('genericApi.disable')
      try {
        await this.$confirm(
          this.$t('genericApi.confirmToggle', { action, name: row.apiName }),
          this.$t('common.tip'),
          { confirmButtonText: this.$t('common.confirm'), cancelButtonText: this.$t('common.cancel'), type: 'warning' }
        )
        if (isEnable) {
          await enableGenericApi(row.apiId)
        } else {
          await disableGenericApi(row.apiId)
        }
        await this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}泛化API失败:`, error)
        }
      }
    },

    // ========== 删除 ==========
    async handleDelete(row) {
      try {
        await this.$confirm(
          this.$t('genericApi.confirmDelete', { name: row.apiName }),
          this.$t('common.tip'),
          { confirmButtonText: this.$t('common.delete'), cancelButtonText: this.$t('common.cancel'), type: 'error' }
        )
        await deleteGenericApi(row.id)
        // 如果删除后当前页无数据，回退一页
        if (this.tableData.length === 1 && this.pageNum > 1) {
          this.pageNum--
        }
        await this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除泛化API失败:', error)
        }
      }
    },

    // 打开API测试对话框
    openApiTestDialog(api) {
      this.currentApi = api;
      this.requestBody = this.getDefaultRequestBody(api);
      this.testDialogVisible = true;
    },

    // 获取默认请求体
    getDefaultRequestBody(api) {
      // 根据API信息生成符合后端规范的请求体
      const defaultBody = {
        id: api.apiId,
        version: api.version ? parseInt(api.version) : null,
        parameters: [
          // 这里可以根据接口定义动态生成参数模板
          {
            parameterName: "param1",
            exceptdClassName: "java.lang.String",
            parameterValue: "value1"
          },
          {
            parameterName: "param2",
            exceptdClassName: "java.lang.String",
            parameterValue: "value2"
          }
        ]
      };
      return JSON.stringify(defaultBody, null, 2);
    },

    // 执行API测试
    async executeApiTest() {
      this.testLoading = true;
      try {
        // 构建请求URL，根据是否需要认证选择不同的端点
        const api = this.currentApi;
        let url;
        if (api.needAuth) {
          // 需要认证的API使用私有端点
          url = '/private/genric/invoke';
        } else {
          // 不需要认证的API使用公共端点
          url = '/public/genric/invoke';
        }

        // 发送测试请求
        const response = await request.post(url, JSON.parse(this.requestBody));

        this.testResult = JSON.stringify(response.data, null, 2);
      } catch (error) {
        console.error('API测试失败:', error);
        this.testResult = `错误: ${error.message || '请求失败'}`;
      } finally {
        this.testLoading = false;
      }
    },

    // 关闭测试对话框
    closeTestDialog() {
      this.testDialogVisible = false;
      this.testResult = '';
    },

    // 打开测试页面
    openTestUrl(type) {
      let url;
      if (type === 'private') {
        url = 'http://127.0.0.1:8080/doc.html#/default/genric-controller/invoke_1';
      } else if (type === 'public') {
        url = 'http://127.0.0.1:8080/doc.html#/default/public-genric-controller/invoke';
      }
      window.open(url, '_blank');
    },

    // ========== 工具方法 ==========
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
.generic-api-admin {
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
}

.page-header:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
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

.test-btn {
  border-radius: 8px;
  font-weight: 500;
  padding: 9px 16px;
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
  min-width: 180px;
  flex: 1;
  max-width: 240px;

  &--status {
    min-width: 130px;
    max-width: 160px;
    flex: 0.6;
  }
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

::v-deep .filter-panel .el-input__inner,
::v-deep .filter-panel .el-select .el-input__inner {
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.25s;

  &:hover {
    border-color: #94a3b8;
  }

  &:focus {
    border-color: #0ea5e9;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.08);
  }
}

/* ========== 宫格面板 ========== */
.grid-panel {
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
  padding: 20px;
}

.api-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.api-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
}

.api-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border-bottom: 1px solid #e2e8f0;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-icon {
  font-size: 18px;
  color: #0ea5e9;
}

.api-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.card-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-body {
  padding: 20px;
}

.card-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #e2e8f0;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-item label {
  width: 100px;
  color: #64748b;
  font-weight: 500;
  margin-right: 10px;
  text-align: left;
}

.info-item span {
  flex: 1;
  color: #1e293b;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

@media (max-width: 1200px) {
  .api-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .api-grid {
    grid-template-columns: 1fr;
  }

  .info-item label {
    width: 80px;
  }
}

.api-table {
  width: 100%;
}

.mono-text {
  font-family: 'JetBrains Mono', 'SF Mono', 'Fira Code', 'Cascadia Code', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 3px 8px;
  border-radius: 5px;
  color: #334155;
  border: 1px solid #e2e8f0;
  white-space: nowrap;

  &--sm {
    font-size: 11px;
    background: #f8fafc;
  }

  &--method {
    background: #eff6ff;
    border-color: #bfdbfe;
    color: #1d4ed8;
  }
}

.api-name-text {
  font-weight: 500;
  color: #1e293b;
}

.app-tag {
  border-radius: 4px;
  font-size: 11px;
}

.version-text {
  font-size: 12px;
  color: #94a3b8;
}

.time-text {
  font-size: 12px;
  color: #64748b;
}

/* ========== 状态指示器 ========== */
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;

  &--active {
    background: #10b981;
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
    animation: pulse-active 2s infinite;
  }

  &--inactive {
    background: #94a3b8;
    box-shadow: 0 0 0 3px rgba(148, 163, 184, 0.12);
  }
}

@keyframes pulse-active {
  0%, 100% {
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
  }
  50% {
    box-shadow: 0 0 0 5px rgba(16, 185, 129, 0.08);
  }
}

.status-text {
  font-size: 12px;
  font-weight: 500;

  &--active {
    color: #059669;
  }

  &--inactive {
    color: #94a3b8;
  }
}

/* ========== 操作按钮 ========== */
.edit-text {
  color: #0ea5e9 !important;

  &:hover {
    color: #38bdf8 !important;
  }
}

.warning-text {
  color: #f59e0b !important;

  &:hover {
    color: #fbbf24 !important;
  }
}

.success-text {
  color: #10b981 !important;

  &:hover {
    color: #34d399 !important;
  }
}

.test-api-btn {
  color: #10b981 !important;

  &:hover {
    color: #34d399 !important;
  }
}

.test-api-btn {
  color: #10b981 !important;

  &:hover {
    color: #34d399 !important;
  }
}

.danger-text {
  color: #ef4444 !important;

  &:hover {
    color: #f87171 !important;
  }
}

/* ========== 测试对话框 ========== */
.test-dialog-content {
  .test-info {
    margin-bottom: 20px;
  }

  .request-section {
    margin-bottom: 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      h4 {
        margin: 0;
        font-size: 14px;
        font-weight: 600;
        color: #334155;
      }
    }
  }

  .response-section {
    h4 {
      margin: 0 0 8px 0;
      font-size: 14px;
      font-weight: 600;
      color: #334155;
    }

    .response-result {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 12px;
      font-size: 13px;
      color: #334155;
      white-space: pre-wrap;
      word-wrap: break-word;
      max-height: 200px;
      overflow-y: auto;
    }
  }
}

/* ========== 空状态 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
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

/* ========== 分页 ========== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
}

/* ========== 弹窗表单 ========== */
.api-dialog-form {
  padding: 10px 20px 0;

  ::v-deep .el-input__inner {
    border-radius: 8px;
    border-color: #e2e8f0;
    transition: all 0.25s;

    &:focus {
      border-color: #0ea5e9;
      box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.08);
    }
  }

  ::v-deep .el-textarea__inner {
    border-radius: 8px;
    border-color: #e2e8f0;

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

.form-row {
  display: flex;
  gap: 20px;

  .form-row-item {
    flex: 1;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 20px 10px;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .filter-row {
    gap: 12px;
  }

  .filter-item {
    min-width: 150px;
  }
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

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
