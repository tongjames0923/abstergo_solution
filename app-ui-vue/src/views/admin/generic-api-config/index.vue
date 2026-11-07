<template>
  <div class="app-container">
    <div class="gac-page">
      <!-- 页面标题 -->
      <div class="gac-header">
        <div class="gac-header__left">
          <h2 class="gac-title">
            <i class="el-icon-connection gac-title__icon" />
            {{ $t('genericApiConfig.title') }}
          </h2>
          <span class="gac-subtitle">{{ $t('genericApiConfig.subtitle') }}</span>
        </div>
        <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">
          {{ $t('genericApiConfig.add') }}
        </el-button>
      </div>

      <!-- 查询条件 -->
      <div class="gac-filter">
        <div class="gac-filter__row">
          <div class="gac-filter__item">
            <label class="gac-filter__label">{{ $t('genericApiConfig.apiName') }}</label>
            <el-input
              v-model="searchForm.apiNameLike"
              size="small"
              clearable
              :placeholder="$t('genericApiConfig.apiNamePlaceholder')"
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="gac-filter__item">
            <label class="gac-filter__label">{{ $t('genericApiConfig.interfaceName') }}</label>
            <el-input
              v-model="searchForm.interfaceNameLike"
              size="small"
              clearable
              :placeholder="$t('genericApiConfig.interfaceNamePlaceholder')"
              @keyup.enter.native="handleSearch"
            />
          </div>
          <div class="gac-filter__item gac-filter__item--sm">
            <label class="gac-filter__label">{{ $t('genericApiConfig.status') }}</label>
            <el-select v-model="searchForm.flag" size="small" clearable :placeholder="$t('genericApiConfig.allStatus')">
              <el-option :label="$t('genericApiConfig.enabled')" :value="1" />
              <el-option :label="$t('genericApiConfig.disabled')" :value="-1" />
            </el-select>
          </div>
          <div class="gac-filter__actions">
            <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">
              {{ $t('common.search') }}
            </el-button>
            <el-button icon="el-icon-refresh-left" size="small" @click="handleReset">{{ $t('common.reset') }}</el-button>
          </div>
        </div>
      </div>

      <!-- 配置列表 -->
      <div v-loading="loading" class="gac-panel">
        <el-table v-if="!isPhone" :data="tableData" size="small" class="gac-table" border>
          <el-table-column prop="apiId" :label="$t('genericApiConfig.apiId')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="apiName" :label="$t('genericApiConfig.apiName')" min-width="150" show-overflow-tooltip />
          <el-table-column prop="interfaceName" :label="$t('genericApiConfig.interfaceName')" min-width="220" show-overflow-tooltip />
          <el-table-column prop="methodName" :label="$t('genericApiConfig.methodName')" min-width="140" show-overflow-tooltip />
          <el-table-column prop="business" :label="$t('genericApiConfig.business')" width="110">
            <template slot-scope="{ row }">{{ row.business || '-' }}</template>
          </el-table-column>
          <el-table-column prop="version" :label="$t('genericApiConfig.version')" width="90">
            <template slot-scope="{ row }">{{ row.version === null ? '-' : row.version }}</template>
          </el-table-column>
          <el-table-column :label="$t('genericApiConfig.needAuth')" width="90">
            <template slot-scope="{ row }">
              <el-tag :type="row.needAuth ? 'warning' : 'info'" size="mini">
                {{ row.needAuth ? $t('common.yes') : $t('common.no') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="$t('genericApiConfig.status')" width="90">
            <template slot-scope="{ row }">
              <el-tag :type="row.flag === 1 ? 'success' : 'info'" size="mini">
                {{ row.flag === 1 ? $t('genericApiConfig.enabled') : $t('genericApiConfig.disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="$t('genericApiConfig.actions')" width="250" fixed="right">
            <template slot-scope="{ row }">
              <el-button type="text" size="mini" @click="openTestDialog(row)">
                <i class="el-icon-caret-right" /> {{ $t('genericApiConfig.test') }}
              </el-button>
              <el-button type="text" size="mini" @click="handleEdit(row)">
                <i class="el-icon-edit-outline" /> {{ $t('common.edit') }}
              </el-button>
              <el-button type="text" size="mini" @click="handleToggle(row)">
                <i :class="row.flag === 1 ? 'el-icon-video-pause' : 'el-icon-video-play'" />
                {{ row.flag === 1 ? $t('genericApiConfig.disable') : $t('genericApiConfig.enable') }}
              </el-button>
              <el-button type="text" size="mini" class="gac-danger-text" @click="handleDelete(row)">
                <i class="el-icon-delete" /> {{ $t('common.delete') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 手机档：行改为卡片，避免横向溢出 -->
        <div v-else class="gac-cards">
          <div v-for="row in tableData" :key="row.id" class="gac-card">
            <div class="gac-card__head">
              <span class="gac-card__name">{{ row.apiName }}</span>
              <el-tag :type="row.flag === 1 ? 'success' : 'info'" size="mini">
                {{ row.flag === 1 ? $t('genericApiConfig.enabled') : $t('genericApiConfig.disabled') }}
              </el-tag>
            </div>
            <div class="gac-card__line"><label>{{ $t('genericApiConfig.apiId') }}</label><span>{{ row.apiId }}</span></div>
            <div class="gac-card__line"><label>{{ $t('genericApiConfig.interfaceName') }}</label><span>{{ row.interfaceName }}</span></div>
            <div class="gac-card__line"><label>{{ $t('genericApiConfig.methodName') }}</label><span>{{ row.methodName }}</span></div>
            <div class="gac-card__line">
              <label>{{ $t('genericApiConfig.business') }}</label>
              <span>{{ row.business || '-' }}</span>
            </div>
            <div class="gac-card__actions">
              <el-button size="mini" @click="openTestDialog(row)">{{ $t('genericApiConfig.test') }}</el-button>
              <el-button size="mini" @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
              <el-button size="mini" @click="handleToggle(row)">
                {{ row.flag === 1 ? $t('genericApiConfig.disable') : $t('genericApiConfig.enable') }}
              </el-button>
              <el-button size="mini" type="danger" plain @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
            </div>
          </div>
          <el-empty v-if="!tableData.length" :description="$t('genericApiConfig.emptyDesc')" />
        </div>

        <div v-if="total > 0" class="gac-pagination">
          <el-pagination
            :current-page="pageNum"
            :page-sizes="[10, 20, 50]"
            :page-size="pageSize"
            :layout="isPhone ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 配置弹窗：API 声明 + 入参定义 -->
      <el-dialog
        :title="dialogType === 'add' ? $t('genericApiConfig.addTitle') : $t('genericApiConfig.editTitle')"
        :visible.sync="dialogVisible"
        :width="isPhone ? '96%' : '960px'"
        top="5vh"
        :close-on-click-modal="false"
        @closed="handleDialogClosed"
      >
        <el-form ref="apiForm" :model="apiForm" :rules="formRules" label-width="120px" class="gac-form">
          <el-form-item :label="$t('genericApiConfig.apiId')" prop="apiId">
            <el-input
              v-model="apiForm.apiId"
              :disabled="dialogType === 'edit'"
              maxlength="100"
              show-word-limit
              :placeholder="$t('genericApiConfig.apiIdPlaceholder')"
            />
          </el-form-item>
          <el-form-item :label="$t('genericApiConfig.apiName')" prop="apiName">
            <el-input v-model="apiForm.apiName" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item :label="$t('genericApiConfig.appName')" prop="appName">
            <el-input v-model="apiForm.appName" maxlength="100" :placeholder="$t('genericApiConfig.appNameHint')" />
          </el-form-item>
          <el-form-item :label="$t('genericApiConfig.interfaceName')" prop="interfaceName">
            <el-input
              v-model="apiForm.interfaceName"
              maxlength="300"
              :placeholder="$t('genericApiConfig.interfaceNameHint')"
            />
          </el-form-item>
          <el-form-item :label="$t('genericApiConfig.methodName')" prop="methodName">
            <el-input v-model="apiForm.methodName" maxlength="100" />
          </el-form-item>
          <div class="gac-form__grid">
            <el-form-item :label="$t('genericApiConfig.business')">
              <el-input v-model="apiForm.business" maxlength="100" :placeholder="$t('genericApiConfig.businessHint')" />
            </el-form-item>
            <el-form-item :label="$t('genericApiConfig.version')">
              <el-input v-model="apiForm.version" maxlength="10" :placeholder="$t('genericApiConfig.versionHint')" />
            </el-form-item>
            <el-form-item :label="$t('genericApiConfig.needAuth')">
              <el-switch v-model="apiForm.needAuth" :active-text="$t('common.yes')" :inactive-text="$t('common.no')" />
            </el-form-item>
            <el-form-item :label="$t('genericApiConfig.status')">
              <el-switch
                v-model="apiForm.enabled"
                :active-text="$t('genericApiConfig.enabled')"
                :inactive-text="$t('genericApiConfig.disabled')"
              />
            </el-form-item>
          </div>
          <el-form-item :label="$t('genericApiConfig.preAuthBeans')">
            <el-select
              v-model="apiForm.preAuthBeans"
              multiple
              clearable
              style="width: 100%;"
              :placeholder="$t('genericApiConfig.preAuthHint')"
            >
              <el-option v-for="item in preAuthOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('genericApiConfig.description')">
            <el-input v-model="apiForm.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>

        <!-- 入参定义 -->
        <div class="gac-params">
          <div class="gac-params__head">
            <h4>{{ $t('genericApiConfig.paramsTitle') }}</h4>
            <span class="gac-params__tip">{{ $t('genericApiConfig.paramsTip') }}</span>
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="addParam">
              {{ $t('genericApiConfig.addParam') }}
            </el-button>
          </div>

          <el-table v-if="!isPhone" :data="paramRows" size="mini" border class="gac-params__table">
            <el-table-column :label="$t('genericApiConfig.paramOrder')" width="70">
              <template slot-scope="{ $index }">{{ $index }}</template>
            </el-table-column>
            <el-table-column :label="$t('genericApiConfig.paramName')" min-width="140">
              <template slot-scope="{ row }">
                <el-input v-model="row.paramName" size="mini" :placeholder="$t('genericApiConfig.paramNameHint')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('genericApiConfig.paramType')" min-width="220">
              <template slot-scope="{ row }">
                <el-select
                  v-model="row.paramType"
                  size="mini"
                  filterable
                  allow-create
                  default-first-option
                  :placeholder="$t('genericApiConfig.paramTypeHint')"
                  style="width: 100%;"
                >
                  <el-option v-for="type in typeOptions" :key="type.value" :label="type.label" :value="type.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="$t('genericApiConfig.paramRequired')" width="70">
              <template slot-scope="{ row }">
                <el-checkbox v-model="row.requiredFlag" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('genericApiConfig.paramDescription')" min-width="140">
              <template slot-scope="{ row }">
                <el-input v-model="row.description" size="mini" maxlength="500" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('genericApiConfig.paramExample')" min-width="160">
              <template slot-scope="{ row }">
                <el-input v-model="row.example" size="mini" maxlength="500" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('genericApiConfig.actions')" width="150">
              <template slot-scope="{ $index }">
                <el-button
                  type="text"
                  size="mini"
                  icon="el-icon-top"
                  :disabled="$index === 0"
                  @click="moveParam($index, -1)"
                />
                <el-button
                  type="text"
                  size="mini"
                  icon="el-icon-bottom"
                  :disabled="$index === paramRows.length - 1"
                  @click="moveParam($index, 1)"
                />
                <el-button
                  type="text"
                  size="mini"
                  class="gac-danger-text"
                  icon="el-icon-delete"
                  @click="removeParam($index)"
                />
              </template>
            </el-table-column>
          </el-table>

          <!-- 手机档参数卡片 -->
          <div v-else class="gac-params__cards">
            <div v-for="(row, index) in paramRows" :key="index" class="gac-param-card">
              <div class="gac-param-card__head">
                <span class="gac-param-card__index">#{{ index }}</span>
                <div class="gac-param-card__ops">
                  <el-button size="mini" icon="el-icon-top" :disabled="index === 0" @click="moveParam(index, -1)" />
                  <el-button
                    size="mini"
                    icon="el-icon-bottom"
                    :disabled="index === paramRows.length - 1"
                    @click="moveParam(index, 1)"
                  />
                  <el-button size="mini" type="danger" plain icon="el-icon-delete" @click="removeParam(index)" />
                </div>
              </div>
              <el-input v-model="row.paramName" size="small" :placeholder="$t('genericApiConfig.paramNameHint')" />
              <el-select
                v-model="row.paramType"
                size="small"
                filterable
                allow-create
                default-first-option
                :placeholder="$t('genericApiConfig.paramTypeHint')"
                style="width: 100%;"
              >
                <el-option v-for="type in typeOptions" :key="type.value" :label="type.label" :value="type.value" />
              </el-select>
              <el-input v-model="row.description" size="small" :placeholder="$t('genericApiConfig.paramDescription')" />
              <el-input v-model="row.example" size="small" :placeholder="$t('genericApiConfig.paramExample')" />
              <el-checkbox v-model="row.requiredFlag">{{ $t('genericApiConfig.paramRequired') }}</el-checkbox>
            </div>
            <p v-if="!paramRows.length" class="gac-params__empty">{{ $t('genericApiConfig.paramsEmpty') }}</p>
          </div>
        </div>

        <div slot="footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ saving ? $t('genericApiConfig.saving') : $t('common.confirm') }}
          </el-button>
        </div>
      </el-dialog>

      <!-- 试调用弹窗：走新版 /call 接口 -->
      <el-dialog
        :title="$t('genericApiConfig.testTitle', { apiId: testTarget.apiId })"
        :visible.sync="testDialogVisible"
        :width="isPhone ? '96%' : '860px'"
        top="5vh"
        :close-on-click-modal="false"
      >
        <div class="gac-test">
          <div class="gac-test__meta">
            <span class="gac-test__meta-item">{{ testTarget.interfaceName }}.{{ testTarget.methodName }}</span>
            <span class="gac-test__meta-item">{{ $t('genericApiConfig.business') }}: {{ testTarget.business || '-' }}</span>
            <span class="gac-test__meta-item">{{ $t('genericApiConfig.version') }}: {{ testTarget.version === null ? '-' : testTarget.version }}</span>
          </div>

          <div class="gac-test__mode">
            <el-radio-group v-model="callMode" size="mini">
              <el-radio-button label="named">{{ $t('genericApiConfig.modeNamed') }}</el-radio-button>
              <el-radio-button label="positional">{{ $t('genericApiConfig.modePositional') }}</el-radio-button>
            </el-radio-group>
            <span class="gac-test__mode-tip">
              {{ callMode === 'named' ? $t('genericApiConfig.modeNamedTip') : $t('genericApiConfig.modePositionalTip') }}
            </span>
          </div>

          <div v-if="!testParams.length" class="gac-test__no-param">
            {{ $t('genericApiConfig.testNoParam') }}
          </div>
          <div v-else class="gac-test__params">
            <div v-for="(param, index) in testParams" :key="index" class="gac-test__param">
              <div class="gac-test__param-label">
                <span class="gac-test__param-order">#{{ index }}</span>
                <span class="gac-test__param-name">{{ param.paramName || $t('genericApiConfig.unnamedParam') }}</span>
                <code class="gac-test__param-type">{{ param.paramType }}</code>
                <el-tag v-if="param.requiredFlag" type="danger" size="mini">{{ $t('genericApiConfig.paramRequired') }}</el-tag>
              </div>
              <el-input v-model="param.inputValue" size="small" :placeholder="valuePlaceholder(param)" />
              <p v-if="param.description" class="gac-test__param-desc">{{ param.description }}</p>
            </div>
          </div>

          <div class="gac-test__preview">
            <div class="gac-test__preview-head">
              <h4>{{ $t('genericApiConfig.requestBodyPreview') }}</h4>
              <el-button size="mini" type="text" icon="el-icon-document-copy" @click="copyRequestBody">
                {{ $t('genericApiConfig.copy') }}
              </el-button>
            </div>
            <pre class="gac-test__json">{{ requestBodyPreview }}</pre>
          </div>

          <div v-if="testResult" class="gac-test__result">
            <div class="gac-test__preview-head">
              <h4>{{ $t('genericApiConfig.responseResult') }}</h4>
              <el-tag :type="testResult.success ? 'success' : 'danger'" size="small">
                {{ testResult.success ? $t('genericApiConfig.callSuccess') : $t('genericApiConfig.callFailed') }}
              </el-tag>
              <span v-if="testResult.cost" class="gac-test__cost">{{ testResult.cost }} ms</span>
            </div>
            <pre class="gac-test__json">{{ testResult.text }}</pre>
          </div>
        </div>

        <div slot="footer">
          <el-button @click="testDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" icon="el-icon-caret-right" :loading="testLoading" @click="executeTest">
            {{ $t('genericApiConfig.executeTest') }}
          </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import responsive from '@/mixins/responsive'
import {
  deleteGenericApi,
  disableGenericApi,
  enableGenericApi,
  getAvailablePreAuths,
  getGenericApiParams,
  invokeGenericApi,
  queryGenericApis,
  saveGenericApiWithParams
} from '@/api/generic-api-config'

const INT_TYPES = ['int', 'long', 'short', 'byte', 'java.lang.Integer', 'java.lang.Long', 'java.lang.Short', 'java.lang.Byte']
const FLOAT_TYPES = ['double', 'float', 'java.lang.Double', 'java.lang.Float']
const BOOL_TYPES = ['boolean', 'java.lang.Boolean']
const PLAIN_TEXT_TYPES = ['java.lang.String', 'char', 'java.lang.Character']

export default {
  name: 'GenericApiConfigAdmin',
  mixins: [responsive],
  data() {
    return {
      searchForm: { apiNameLike: '', interfaceNameLike: '', flag: null },
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogType: 'add',
      saving: false,
      apiForm: this.emptyForm(),
      formRules: {
        apiId: [{ required: true, message: this.$t('genericApiConfig.apiIdRequired'), trigger: 'blur' }],
        apiName: [{ required: true, message: this.$t('genericApiConfig.apiNameRequired'), trigger: 'blur' }],
        appName: [{ required: true, message: this.$t('genericApiConfig.appNameRequired'), trigger: 'blur' }],
        interfaceName: [{ required: true, message: this.$t('genericApiConfig.interfaceNameRequired'), trigger: 'blur' }],
        methodName: [{ required: true, message: this.$t('genericApiConfig.methodNameRequired'), trigger: 'blur' }]
      },
      preAuthOptions: [],
      paramRows: [],
      testDialogVisible: false,
      testTarget: {},
      testParams: [],
      testLoading: false,
      testResult: null,
      callMode: 'named',
      typeOptions: [
        { value: 'java.lang.String', label: 'String' },
        { value: 'int', label: 'int（基本类型写关键字）' },
        { value: 'java.lang.Integer', label: 'Integer' },
        { value: 'long', label: 'long（基本类型写关键字）' },
        { value: 'java.lang.Long', label: 'Long' },
        { value: 'double', label: 'double' },
        { value: 'float', label: 'float' },
        { value: 'boolean', label: 'boolean' },
        { value: 'java.lang.Boolean', label: 'Boolean' },
        { value: 'java.math.BigDecimal', label: 'BigDecimal' },
        { value: 'java.util.Date', label: 'Date' },
        { value: 'java.util.List', label: 'List（擦除，不写泛型）' },
        { value: 'java.util.Set', label: 'Set' },
        { value: 'java.util.Map', label: 'Map' },
        { value: 'int[]', label: 'int[]' },
        { value: 'java.lang.String[]', label: 'String[]' }
      ]
    }
  },
  computed: {
    /**
     * 请求体预览：命名式产出对象、位置式产出数组，与服务端装配规则一致
     */
    requestBodyPreview() {
      try {
        return JSON.stringify(this.buildCallBody(), null, 2)
      } catch (e) {
        return this.$t('genericApiConfig.invalidJsonHint')
      }
    }
  },
  created() {
    this.loadPreAuthOptions()
    this.loadData()
  },
  methods: {
    /**
     * 空表单初始值
     */
    emptyForm() {
      return {
        id: null,
        apiId: '',
        apiName: '',
        appName: '',
        interfaceName: '',
        methodName: '',
        business: '',
        version: '',
        needAuth: false,
        enabled: true,
        preAuthBeans: [],
        description: ''
      }
    },
    async loadPreAuthOptions() {
      try {
        const res = await getAvailablePreAuths()
        this.preAuthOptions = res.data || []
      } catch (e) {
        this.preAuthOptions = []
      }
    },
    async loadData() {
      this.loading = true
      try {
        const criteria = {}
        if (this.searchForm.apiNameLike) criteria.apiNameLike = this.searchForm.apiNameLike
        if (this.searchForm.interfaceNameLike) criteria.interfaceNameLike = this.searchForm.interfaceNameLike
        if (this.searchForm.flag !== null && this.searchForm.flag !== '') criteria.flagIn = [this.searchForm.flag]
        criteria.updateTimeOrder = { order: 'DESC' }
        const res = await queryGenericApis(criteria, this.pageNum, this.pageSize)
        const page = res.data || {}
        this.tableData = page.data || []
        this.total = page.total || 0
      } catch (e) {
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { apiNameLike: '', interfaceNameLike: '', flag: null }
      this.pageNum = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.loadData()
    },
    handlePageChange(val) {
      this.pageNum = val
      this.loadData()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.apiForm = this.emptyForm()
      this.paramRows = []
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.apiForm && this.$refs.apiForm.clearValidate())
    },
    async handleEdit(row) {
      this.dialogType = 'edit'
      this.apiForm = {
        id: row.id,
        apiId: row.apiId,
        apiName: row.apiName,
        appName: row.appName,
        interfaceName: row.interfaceName,
        methodName: row.methodName,
        business: row.business || '',
        version: row.version === null || row.version === undefined ? '' : String(row.version),
        needAuth: !!row.needAuth,
        enabled: row.flag === 1,
        preAuthBeans: row.preAuthBeans ? row.preAuthBeans.split(',').filter(i => i) : [],
        description: row.description || ''
      }
      this.paramRows = []
      this.dialogVisible = true
      try {
        const res = await getGenericApiParams(row.apiId)
        this.paramRows = (res.data || []).map((item, index) => ({
          paramName: item.paramName || '',
          paramType: item.paramType || '',
          paramOrder: index,
          requiredFlag: !!item.requiredFlag,
          description: item.description || '',
          example: item.example || ''
        }))
      } catch (e) {
        this.paramRows = []
      }
    },
    handleDialogClosed() {
      if (this.$refs.apiForm) this.$refs.apiForm.clearValidate()
    },
    addParam() {
      this.paramRows.push({
        paramName: '',
        paramType: 'java.lang.String',
        paramOrder: this.paramRows.length,
        requiredFlag: false,
        description: '',
        example: ''
      })
    },
    removeParam(index) {
      this.paramRows.splice(index, 1)
    },
    moveParam(index, offset) {
      const target = index + offset
      if (target < 0 || target >= this.paramRows.length) return
      const rows = this.paramRows.slice()
      const moved = rows.splice(index, 1)[0]
      rows.splice(target, 0, moved)
      this.paramRows = rows
    },
    async handleSave() {
      try {
        await this.$refs.apiForm.validate()
      } catch (e) {
        return
      }
      const invalid = this.paramRows.some(row => !row.paramType)
      if (invalid) {
        this.$message.warning(this.$t('genericApiConfig.paramTypeRequired'))
        return
      }
      this.saving = true
      try {
        const apiConfig = {
          id: this.apiForm.id,
          apiId: this.apiForm.apiId,
          apiName: this.apiForm.apiName,
          appName: this.apiForm.appName,
          interfaceName: this.apiForm.interfaceName,
          methodName: this.apiForm.methodName,
          business: this.apiForm.business || '',
          version: this.apiForm.version === '' ? null : parseInt(this.apiForm.version, 10),
          needAuth: this.apiForm.needAuth,
          flag: this.apiForm.enabled ? 1 : -1,
          preAuthBeans: (this.apiForm.preAuthBeans || []).join(','),
          description: this.apiForm.description
        }
        const params = this.paramRows.map((row, index) => ({
          apiId: this.apiForm.apiId,
          paramOrder: index,
          paramName: row.paramName || '',
          paramType: row.paramType,
          requiredFlag: !!row.requiredFlag,
          description: row.description || '',
          example: row.example || ''
        }))
        await saveGenericApiWithParams({ apiConfig, params })
        this.$message.success(this.$t('genericApiConfig.saveSuccess'))
        this.dialogVisible = false
        await this.loadData()
      } catch (e) {
        // 失败提示由 request 拦截器统一弹出，此处仅结束 loading
      } finally {
        this.saving = false
      }
    },
    async handleToggle(row) {
      const isEnable = row.flag !== 1
      try {
        await this.$confirm(
          this.$t('genericApiConfig.confirmToggle', { name: row.apiName }),
          this.$t('common.tip'),
          { confirmButtonText: this.$t('common.confirm'), cancelButtonText: this.$t('common.cancel'), type: 'warning' }
        )
        if (isEnable) {
          await enableGenericApi(row.apiId)
        } else {
          await disableGenericApi(row.apiId)
        }
        await this.loadData()
      } catch (e) {
        // 取消或失败均无需额外处理
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(
          this.$t('genericApiConfig.confirmDelete', { name: row.apiName }),
          this.$t('common.tip'),
          { confirmButtonText: this.$t('common.delete'), cancelButtonText: this.$t('common.cancel'), type: 'error' }
        )
        await deleteGenericApi(row.id)
        if (this.tableData.length === 1 && this.pageNum > 1) this.pageNum -= 1
        await this.loadData()
      } catch (e) {
        // 取消或失败均无需额外处理
      }
    },
    async openTestDialog(row) {
      this.testTarget = row
      this.testResult = null
      this.testParams = []
      this.callMode = 'named'
      this.testDialogVisible = true
      try {
        const res = await getGenericApiParams(row.apiId)
        this.testParams = (res.data || []).map(item => ({
          ...item,
          inputValue: item.example || ''
        }))
        if (!this.testParams.length) this.callMode = 'named'
      } catch (e) {
        this.testParams = []
      }
    },
    /**
     * 按登记的参数类型把输入文本折算成 JSON 值
     * 与后端约定一致：类型串是擦除后的 JVM 名，复杂结构由调用方以 JSON 文本提供
     */
    toValue(param) {
      const raw = param.inputValue
      const type = param.paramType || ''
      if (raw === '' || raw === null || raw === undefined) return null
      if (INT_TYPES.indexOf(type) >= 0 || FLOAT_TYPES.indexOf(type) >= 0) {
        const num = Number(raw)
        if (Number.isNaN(num)) throw new Error(this.$t('genericApiConfig.numberRequired', { name: param.paramName || type }))
        return num
      }
      if (BOOL_TYPES.indexOf(type) >= 0) return raw === 'true'
      if (type === 'java.math.BigDecimal') return raw
      if (PLAIN_TEXT_TYPES.indexOf(type) >= 0) return raw
      // 其余（集合、数组、POJO、枚举）：优先按 JSON 解析，失败则作为字符串（枚举名等场景）
      try {
        return JSON.parse(raw)
      } catch (e) {
        return raw
      }
    },
    buildCallBody() {
      if (!this.testParams.length) return {}
      if (this.callMode === 'positional') return this.testParams.map(p => this.toValue(p))
      const body = {}
      this.testParams.forEach(p => {
        const key = p.paramName || `arg${p.paramOrder}`
        body[key] = this.toValue(p)
      })
      return body
    },
    valuePlaceholder(param) {
      if (param.example) return param.example
      const type = param.paramType || ''
      if (type.indexOf('java.util.') === 0 || type.endsWith('[]')) {
        return this.$t('genericApiConfig.jsonValuePlaceholder')
      }
      return this.$t('genericApiConfig.valuePlaceholder')
    },
    async executeTest() {
      let body
      try {
        body = this.buildCallBody()
      } catch (e) {
        this.$message.warning(e.message)
        return
      }
      if (this.callMode === 'named' && this.testParams.some(p => !p.paramName)) {
        this.$message.warning(this.$t('genericApiConfig.namedModeNeedsName'))
        return
      }
      this.testLoading = true
      const begin = Date.now()
      try {
        const version = this.testTarget.version === null ? null : this.testTarget.version
        const res = await invokeGenericApi(this.testTarget.apiId, body, version)
        // 新入口只有一层：res.data 就是远端返回值本体，失败已由 request 拦截器按错误码抛出
        this.testResult = {
          success: true,
          cost: Date.now() - begin,
          text: JSON.stringify(res.data === undefined ? null : res.data, null, 2)
        }
      } catch (e) {
        this.testResult = {
          success: false,
          cost: Date.now() - begin,
          text: (e && e.message) ? e.message : String(e)
        }
      } finally {
        this.testLoading = false
      }
    },
    async copyRequestBody() {
      try {
        await navigator.clipboard.writeText(this.requestBodyPreview)
        this.$message.success(this.$t('genericApiConfig.copySuccess'))
      } catch (e) {
        const textarea = document.createElement('textarea')
        textarea.value = this.requestBodyPreview
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        this.$message.success(this.$t('genericApiConfig.copySuccess'))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
@import "~@/styles/mixin.scss";

.gac-page {
  padding: 0;
}

.gac-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.gac-title {
  margin: 0;
  font-size: 21px;
  &__icon { color: #0ea5e9; }
}

.gac-subtitle {
  display: block;
  font-size: 13px;
  color: #94a3b8;
}

.gac-filter {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;

  &__row {
    display: flex;
    gap: 16px;
    align-items: flex-end;
    flex-wrap: wrap;
  }

  &__item {
    flex: 1 1 220px;
    min-width: 0;

    &--sm { flex: 0 1 160px; }
  }

  &__label {
    display: block;
    font-size: 12px;
    color: #64748b;
    margin-bottom: 6px;
  }

  &__actions { display: flex; gap: 8px; }
}

.gac-panel {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 16px;
}

.gac-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.gac-danger-text { color: #ef4444 !important; }

.gac-form {
  &__grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 16px;
  }
}

.gac-params {
  margin-top: 8px;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 12px;

    h4 { margin: 0; font-size: 14px; color: #1e293b; }
  }

  &__tip { flex: 1; font-size: 12px; color: #94a3b8; }
  &__empty { margin: 8px 0; font-size: 12px; color: #94a3b8; }
}

.gac-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.gac-card {
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  &__name { font-weight: 600; color: #1e293b; }

  &__line {
    display: flex;
    gap: 8px;
    font-size: 12px;
    color: #334155;
    margin-bottom: 4px;
    word-break: break-all;

    label { color: #94a3b8; flex: 0 0 76px; }
  }

  &__actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-top: 10px;
  }
}

.gac-param-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  margin-bottom: 10px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &__index { font-size: 12px; color: #64748b; font-weight: 600; }
  &__ops { display: flex; gap: 4px; }
}

.gac-test {
  &__meta {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    font-size: 12px;
    color: #64748b;
    margin-bottom: 12px;
  }

  &__mode {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 12px;

    &-tip { font-size: 12px; color: #94a3b8; }
  }

  &__no-param {
    padding: 14px;
    font-size: 13px;
    color: #94a3b8;
    background: #f8fafc;
    border-radius: 8px;
    margin-bottom: 12px;
  }

  &__param {
    margin-bottom: 12px;

    &-label {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 4px;
    }

    &-order { font-size: 12px; color: #94a3b8; }
    &-name { font-size: 13px; font-weight: 600; color: #1e293b; }

    &-type {
      font-family: 'JetBrains Mono', monospace;
      font-size: 11px;
      color: #1d4ed8;
      background: #eff6ff;
      padding: 2px 6px;
      border-radius: 4px;
    }

    &-desc { margin: 4px 0 0; font-size: 12px; color: #94a3b8; }
  }

  &__preview, &__result {
    margin-top: 14px;
    padding: 12px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
  }

  &__preview-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;

    h4 { margin: 0; font-size: 13px; color: #334155; }
  }

  &__cost { font-size: 12px; color: #64748b; }

  &__json {
    margin: 0;
    max-height: 260px;
    overflow: auto;
    font-size: 12px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

@include respond-to(mobile) {
  .gac-form__grid { grid-template-columns: 1fr; }
}

@include respond-to(phone) {
  .gac-filter__item,
  .gac-filter__item--sm { flex: 1 1 100%; }
}
</style>
