<template>
  <div class="app-container value-getter-tester">
    <div class="page-head">
      <div class="page-head-main">
        <h1 class="page-title">{{ $t('valueGetter.benchTitle') }}</h1>
        <p class="page-desc">{{ $t('valueGetter.benchDesc') }}</p>
      </div>
      <div class="page-stats">
        <el-tag size="small" type="info" effect="plain">
          {{ $t('valueGetter.statGetters') }} · {{ getterList.length }}
        </el-tag>
        <el-tag size="small" type="info" effect="plain">
          {{ $t('valueGetter.statRuns') }} · {{ history.length }}
        </el-tag>
      </div>
    </div>

    <div class="bench-grid">
      <el-card class="catalog-panel" shadow="hover">
        <div slot="header" class="panel-head">
          <span class="panel-title">{{ $t('conditionLab.library') }}</span>
          <el-button
            type="text"
            size="mini"
            icon="el-icon-refresh"
            :loading="loading"
            @click="fetchGetters"
          >
            {{ $t('common.refresh') }}
          </el-button>
        </div>

        <div class="catalog-body">
          <el-input
            v-model="keyword"
            :placeholder="$t('conditionLab.searchPlaceholder')"
            prefix-icon="el-icon-search"
            size="small"
            clearable
            @input="debouncedFetch"
          />
        </div>

        <ul class="getter-list">
          <li
            v-for="g in getterList"
            :key="g.getterId"
            :class="['getter-item', { active: g.getterId === selectedGetterId }]"
            @click="selectGetter(g)"
          >
            <i :class="['getter-icon', isCondition(g) ? 'el-icon-s-operation' : 'el-icon-cpu']" />
            <div class="getter-body">
              <div class="getter-name">{{ g.getterName }}</div>
              <div class="getter-id">{{ g.getterId }}</div>
            </div>
            <el-tag size="mini" :type="isCondition(g) ? 'warning' : 'info'" effect="plain">
              {{ shortType(g) }}
            </el-tag>
          </li>
        </ul>

        <div v-if="loading" class="list-hint">{{ $t('common.loading') }}</div>
        <div v-else-if="!getterList.length" class="list-hint">{{ $t('conditionLab.noGetters') }}</div>
      </el-card>

      <el-card class="console-panel" shadow="hover">
        <div v-if="!currentGetter" class="console-empty">
          <i class="el-icon-menu empty-icon" />
          <p class="empty-title">{{ $t('conditionLab.emptyTitle') }}</p>
          <p class="empty-desc">{{ $t('conditionLab.emptyDesc') }}</p>
        </div>

        <template v-else>
          <div class="console-head">
            <div class="console-title">
              <span class="console-name">{{ currentGetter.getterName }}</span>
              <el-tag size="mini" type="info" effect="plain">{{ currentGetter.getterId }}</el-tag>
              <el-tooltip v-if="resultTypeDesc" :content="resultTypeDesc" placement="bottom">
                <el-tag size="mini" type="success" effect="plain">
                  {{ $t('valueGetter.returnType') }} · {{ resultTypeName }}
                </el-tag>
              </el-tooltip>
              <el-tag v-else size="mini" type="success" effect="plain">
                {{ $t('valueGetter.returnType') }} · {{ resultTypeName }}
              </el-tag>
            </div>
            <p class="console-desc">{{ currentGetter.description }}</p>
          </div>

          <div class="params">
            <div class="section-head">
              <span class="section-label">{{ $t('conditionLab.params') }}</span>
              <span class="section-hint">{{ $t('valueGetter.paramsHint') }}</span>
            </div>

            <condition-builder
              v-if="isCondition(currentGetter)"
              v-model="condition"
              :getters="getterList"
            />

            <div v-else-if="!paramFields.length" class="no-params">
              {{ $t('valueGetter.noParams') }}
            </div>

            <el-form
              v-else
              :label-width="formLabelWidth"
              :label-position="formLabelPosition"
              size="small"
              class="param-form"
              @submit.native.prevent
            >
              <el-form-item
                v-for="field in paramFields"
                :key="field.name"
                :required="field.required"
              >
                <template slot="label">
                  <span class="param-label">{{ field.label }}</span>
                  <el-tooltip v-if="field.description" :content="field.description" placement="top">
                    <i class="el-icon-info param-info" />
                  </el-tooltip>
                </template>
                <div class="param-control-row">
                  <value-getter-operand
                    v-if="field.uiType === 'operand'"
                    :value="formData[field.name]"
                    :getters="getterList"
                    @input="setParam(field.name, $event)"
                  />
                  <getter-param-field
                    v-else
                    :field="field"
                    :value="formData[field.name]"
                    :error="errorOf(field)"
                    @input="setParam(field.name, $event)"
                  />
                  <el-tag size="mini" type="info" effect="plain" class="param-type">
                    {{ typeLabel(field) }}
                  </el-tag>
                </div>
                <div v-if="field.uiType === 'operand' && errorOf(field)" class="param-error">
                  {{ $t('valueGetter.errRequired') }}
                </div>
                <div v-if="field.description" class="field-desc">{{ field.description }}</div>
              </el-form-item>
            </el-form>
          </div>

          <div class="run-bar">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-caret-right"
              :loading="submitting"
              @click="handleInvoke"
            >
              {{ $t('conditionLab.execute') }}
            </el-button>
            <el-button size="small" icon="el-icon-refresh-left" @click="resetForm">
              {{ $t('common.reset') }}
            </el-button>
            <span class="run-hint">Ctrl / ⌘ + Enter</span>
          </div>

          <details class="payload">
            <summary class="payload-summary">
              <span class="payload-label">{{ $t('valueGetter.payload') }}</span>
              <span class="payload-hint">{{ $t('valueGetter.payloadHint') }}</span>
            </summary>
            <div class="payload-body">
              <pre>{{ payloadPreview }}</pre>
              <el-button type="text" size="mini" @click="copyText(payloadPreview)">
                {{ $t('valueGetter.copy') }}
              </el-button>
            </div>
          </details>
        </template>
      </el-card>

      <el-card class="readout-panel" shadow="hover">
        <div slot="header" class="panel-head">
          <span class="panel-title">{{ $t('valueGetter.resultTitle') }}</span>
          <div v-if="result" class="readout-tools">
            <el-button type="text" size="mini" @click="rawView = !rawView">
              {{ rawView ? $t('valueGetter.viewSemantic') : $t('valueGetter.viewRaw') }}
            </el-button>
            <el-button type="text" size="mini" @click="copyText(rawResult)">
              {{ $t('valueGetter.copy') }}
            </el-button>
          </div>
        </div>

        <div class="readout-body">
          <div v-if="resultError" class="result-error">
            <i class="el-icon-warning-outline" />
            <span>{{ resultError }}</span>
          </div>
          <div v-else-if="!result" class="readout-idle">
            <i class="el-icon-document" />
            <span>{{ $t('valueGetter.readoutWaiting') }}</span>
          </div>
          <template v-else>
            <pre v-if="rawView" class="readout-raw">{{ rawResult }}</pre>
            <getter-result v-else :key="resultKey" :result="result" :elapsed-ms="elapsedMs" />
          </template>
        </div>

        <div class="history">
          <div class="history-head">
            <span class="history-title">{{ $t('valueGetter.history') }}</span>
            <el-button v-if="history.length" type="text" size="mini" @click="clearHistory">
              {{ $t('valueGetter.clearHistory') }}
            </el-button>
          </div>

          <ul v-if="history.length" class="history-list">
            <li
              v-for="item in history"
              :key="item.id"
              :class="['history-item', { 'is-failed': !!item.error }]"
              @click="replay(item)"
            >
              <span class="history-clock">{{ item.clock }}</span>
              <span class="history-main">
                <span class="history-name">{{ item.getterName }}</span>
                <span class="history-value">{{ summaryOf(item) }}</span>
              </span>
              <span class="history-cost">{{ item.elapsedMs }}ms</span>
            </li>
          </ul>
          <p v-else class="history-empty">{{ $t('valueGetter.historyEmpty') }}</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import {getValueGetters, invokeValueGetter} from '@/api/script/valueGetter'
import ConditionBuilder from '@/components/ConditionBuilder'
import GetterParamField from '@/components/GetterParamField'
import GetterResult from '@/components/GetterResult'
import ValueGetterOperand from '@/components/ValueGetterOperand'
import ResponsiveMixin from '@/mixins/responsive'
import {buildFormData, isBlank, toParamFields, toParamValues, validateFields} from '@/utils/valueGetter'

const CONDITION_GETTER_ID = 'ConditionGetter'
const HISTORY_LIMIT = 8

/**
 * 值获取器调试页面。
 * <p>
 * 左侧展示可用的值获取器索引，中部按参数类型生成专用输入控件并执行调用，
 * 右侧展示执行读数与本次会话的执行历史。参数控件形态、默认值与校验规则由后端
 * ParamType 的取值语义与 ParamUiMeta 的控件元信息共同驱动。
 * </p>
 */
export default {
  name: 'ValueGetterTester',
  components: { ConditionBuilder, GetterParamField, GetterResult, ValueGetterOperand },
  mixins: [ResponsiveMixin],
  data() {
    return {
      keyword: '',
      loading: false,
      submitting: false,
      initialized: false,
      getterList: [],
      selectedGetterId: null,
      currentGetter: null,
      formData: {},
      condition: { left: '', op: 'eq', right: '' },
      touched: {},
      showErrors: false,
      result: null,
      resultError: '',
      elapsedMs: 0,
      resultKey: 0,
      rawView: false,
      history: [],
      historySeq: 0
    }
  },
  computed: {
    /**
     * 当前 getter 的参数字段描述
     *
     * @returns {Array} 字段描述列表
     */
    paramFields() {
      return this.currentGetter ? toParamFields(this.currentGetter.paramTypes, this.currentGetter.getterId) : []
    },
    /**
     * 当前字段校验结果
     *
     * @returns {Object} 参数名到失败规则的映射
     */
    errors() {
      return validateFields(this.paramFields, this.formData)
    },
    /**
     * 表单标签位置，手机端置顶
     *
     * @returns {string} Element 标签位置
     */
    formLabelPosition() {
      return this.isPhone ? 'top' : 'right'
    },
    /**
     * 表单标签宽度，手机端自适应
     *
     * @returns {string} 标签宽度
     */
    formLabelWidth() {
      return this.isPhone ? 'auto' : '150px'
    },
    /**
     * 返回类型展示名称
     *
     * @returns {string} 类型名称
     */
    resultTypeName() {
      const resultType = this.currentGetter && this.currentGetter.resultType
      if (!resultType) {
        return this.$t('getterResult.typeUnknown')
      }
      return resultType.name || resultType.type || this.$t('getterResult.typeUnknown')
    },
    /**
     * 返回类型描述
     *
     * @returns {string} 类型描述
     */
    resultTypeDesc() {
      const resultType = this.currentGetter && this.currentGetter.resultType
      return resultType ? resultType.description || '' : ''
    },
    /**
     * 请求载荷预览
     *
     * @returns {string} JSON 文本
     */
    payloadPreview() {
      if (!this.currentGetter) {
        return ''
      }
      try {
        return JSON.stringify({ getterId: this.selectedGetterId, paramValues: this.buildParams() }, null, 2)
      } catch (e) {
        return ''
      }
    },
    /**
     * 原始结果文本
     *
     * @returns {string} JSON 文本
     */
    rawResult() {
      return this.result ? JSON.stringify(this.result, null, 2) : ''
    }
  },
  created() {
    this.debouncedFetch = this.debounce(() => this.fetchGetters(), 300)
    this.fetchGetters()
  },
  mounted() {
    document.addEventListener('keydown', this.handleKeydown)
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.handleKeydown)
  },
  methods: {
    /**
     * 防抖包装
     *
     * @param {Function} fn 目标函数
     * @param {number} delay 延迟毫秒
     * @returns {Function} 防抖函数
     */
    debounce(fn, delay) {
      let timer = null
      return function(...args) {
        clearTimeout(timer)
        timer = setTimeout(() => fn.apply(this, args), delay)
      }
    },

    /**
     * 判断是否为布尔条件 getter
     *
     * @param {Object} getter getter 元信息
     * @returns {boolean} 是否为条件 getter
     */
    isCondition(getter) {
      return !!getter && getter.getterId === CONDITION_GETTER_ID
    },

    /**
     * 全局快捷键：执行取值
     *
     * @param {KeyboardEvent} event 键盘事件
     */
    handleKeydown(event) {
      if ((event.ctrlKey || event.metaKey) && event.key === 'Enter' && this.currentGetter) {
        event.preventDefault()
        this.handleInvoke()
      }
    },

    /**
     * 加载 getter 列表
     *
     * @returns {Promise} 请求 Promise
     */
    async fetchGetters() {
      this.loading = true
      try {
        const res = await getValueGetters({
          nameLike: this.keyword || undefined,
          idLike: this.keyword || undefined
        })
        this.getterList = (res && res.data) || []
        if (this.selectedGetterId && !this.getterList.some(g => g.getterId === this.selectedGetterId)) {
          this.selectedGetterId = null
          this.currentGetter = null
        }
        if (!this.initialized && this.getterList.length) {
          this.initialized = true
          const preferred = this.getterList.find(g => g.getterId !== CONDITION_GETTER_ID) || this.getterList[0]
          this.selectGetter(preferred)
        }
      } catch (e) {
        this.getterList = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 选中 getter 并初始化参数表单
     *
     * @param {Object} getter getter 元信息
     */
    selectGetter(getter) {
      this.selectedGetterId = getter.getterId
      this.currentGetter = getter
      this.condition = { left: '', op: 'eq', right: '' }
      this.formData = buildFormData(toParamFields(getter.paramTypes, getter.getterId))
      this.touched = {}
      this.showErrors = false
      this.resetOutput()
    },

    /**
     * 清空读数区
     */
    resetOutput() {
      this.result = null
      this.resultError = ''
      this.elapsedMs = 0
      this.rawView = false
    },

    /**
     * 重置参数与读数
     */
    resetForm() {
      this.formData = buildFormData(this.paramFields)
      this.condition = { left: '', op: 'eq', right: '' }
      this.touched = {}
      this.showErrors = false
      this.resetOutput()
    },

    /**
     * 更新单个参数值
     *
     * @param {string} name 参数名
     * @param {*} value 参数值
     */
    setParam(name, value) {
      this.$set(this.formData, name, value)
      this.$set(this.touched, name, true)
    },

    /**
     * 读取字段错误提示
     *
     * @param {Object} field 字段描述
     * @returns {string} 错误规则标识，无需展示时返回空串
     */
    errorOf(field) {
      if (!this.showErrors && !this.touched[field.name]) {
        return ''
      }
      return this.errors[field.name] || ''
    },

    /**
     * 参数控件类型文案
     *
     * @param {Object} field 字段描述
     * @returns {string} 类型文案
     */
    typeLabel(field) {
      const map = {
        text: 'typeText',
        textarea: 'typeTextarea',
        number: 'typeNumber',
        boolean: 'typeBoolean',
        date: 'typeDate',
        datetime: 'typeDatetime',
        time: 'typeTime',
        select: 'typeSelect',
        json: 'typeJson',
        operand: 'typeOperand'
      }
      return this.$t('valueGetter.' + (map[field.uiType] || 'typeText'))
    },

    /**
     * getter 返回类型简写
     *
     * @param {Object} getter getter 元信息
     * @returns {string} 大写类型标识
     */
    shortType(getter) {
      const type = getter && getter.resultType && getter.resultType.type
      return type ? String(type).toUpperCase() : '—'
    },

    /**
     * 构造调用参数
     *
     * @returns {Array} ParamValue 列表
     */
    buildParams() {
      if (this.isCondition(this.currentGetter)) {
        const operands = this.paramFields.filter(f => f.uiType === 'operand')
        const op = this.paramFields.find(f => f.isEnum)
        return [
          {
            type: 'operand',
            name: operands[0] ? operands[0].name : 'left',
            sort: 1,
            value: this.condition.left || null
          },
          { type: 'string', name: op ? op.name : 'op', sort: 2, value: this.condition.op || null },
          {
            type: 'operand',
            name: operands[1] ? operands[1].name : 'right',
            sort: 3,
            value: this.condition.right || null
          }
        ]
      }
      return toParamValues(this.paramFields, this.formData)
    },

    /**
     * 执行 getter 取值
     *
     * @returns {Promise} 请求 Promise
     */
    async handleInvoke() {
      if (!this.currentGetter || this.submitting) {
        return
      }
      this.showErrors = true
      if (this.isCondition(this.currentGetter)) {
        if (isBlank(this.condition.left)) {
          this.$message.warning(this.$t('valueGetter.conditionLeftRequired'))
          return
        }
      } else if (Object.keys(this.errors).length) {
        this.$message.warning(this.$t('valueGetter.fixParams'))
        return
      }

      const paramValues = this.buildParams()
      this.submitting = true
      this.resultError = ''
      this.result = null
      const start = Date.now()
      try {
        const res = await invokeValueGetter({
          getterId: this.selectedGetterId,
          paramValues
        })
        this.result = (res && res.data) || null
        this.elapsedMs = Date.now() - start
        this.resultKey += 1
      } catch (e) {
        this.resultError = (e && e.message) || this.$t('common.fail')
        this.elapsedMs = Date.now() - start
      } finally {
        this.submitting = false
        this.pushHistory(paramValues)
      }
    },

    /**
     * 记录本次执行
     *
     * @param {Array} paramValues 本次调用的参数
     */
    pushHistory(paramValues) {
      this.historySeq += 1
      this.history.unshift({
        id: this.historySeq,
        getterId: this.selectedGetterId,
        getterName: this.currentGetter ? this.currentGetter.getterName : this.selectedGetterId,
        paramValues,
        condition: { ...this.condition },
        result: this.result,
        error: this.resultError,
        elapsedMs: this.elapsedMs,
        clock: this.formatClock(new Date())
      })
      this.history = this.history.slice(0, HISTORY_LIMIT)
    },

    /**
     * 清空执行历史
     */
    clearHistory() {
      this.history = []
    },

    /**
     * 回放历史记录：回填参数并展示当时读数
     *
     * @param {Object} item 历史记录
     */
    replay(item) {
      const getter = this.getterList.find(g => g.getterId === item.getterId)
      if (!getter) {
        return
      }
      this.selectedGetterId = getter.getterId
      this.currentGetter = getter
      const fields = toParamFields(getter.paramTypes, getter.getterId)
      const formData = buildFormData(fields)
      fields.forEach(field => {
        const found = (item.paramValues || []).find(p => p && p.name === field.name)
        if (found) {
          formData[field.name] = this.restoreParam(field, found.value)
        }
      })
      this.formData = formData
      this.condition = { ...item.condition }
      this.touched = {}
      this.showErrors = false
      this.result = item.result
      this.resultError = item.error
      this.elapsedMs = item.elapsedMs
      this.rawView = false
      this.resultKey += 1
    },

    /**
     * 把历史参数还原为控件值
     *
     * @param {Object} field 字段描述
     * @param {*} raw 历史值
     * @returns {*} 控件值
     */
    restoreParam(field, raw) {
      if (raw === null || raw === undefined) {
        return field.uiType === 'boolean' ? false : ''
      }
      if (field.uiType === 'number') {
        const num = Number(raw)
        return Number.isNaN(num) ? undefined : num
      }
      return field.uiType === 'boolean' ? raw === 'true' : raw
    },

    /**
     * 历史结果摘要
     *
     * @param {Object} item 历史记录
     * @returns {string} 摘要文本
     */
    summaryOf(item) {
      if (item.error) {
        return item.error
      }
      if (!item.result) {
        return ''
      }
      let text = ''
      if (item.result.value !== null && item.result.value !== undefined) {
        text = String(item.result.value)
      } else if (Array.isArray(item.result.values)) {
        text = item.result.values.join(', ')
      }
      return text.length > 42 ? text.slice(0, 42) + '…' : text
    },

    /**
     * 时间点格式化
     *
     * @param {Date} date 时间对象
     * @returns {string} HH:mm:ss
     */
    formatClock(date) {
      const pad = n => String(n).padStart(2, '0')
      return pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds())
    },

    /**
     * 复制文本到剪贴板
     *
     * @param {string} text 待复制文本
     * @returns {Promise} 复制结果
     */
    async copyText(text) {
      if (!text) {
        return
      }
      try {
        await navigator.clipboard.writeText(text)
        this.$message.success(this.$t('valueGetter.copied'))
      } catch (e) {
        const textarea = document.createElement('textarea')
        textarea.value = text
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        this.$message.success(this.$t('valueGetter.copied'))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.value-getter-tester {
  @include responsive-container;

  padding: 12px;
  overflow-y: auto;

  .page-head {
    display: flex;
    flex: none;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 16px;

    @include respond-to(phone) {
      flex-direction: column;
    }
  }

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .page-desc {
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.6;
    color: #909399;
  }

  .page-stats {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .bench-grid {
    display: grid;
    flex: none;
    grid-template-columns: 248px minmax(0, 1fr) 340px;
    gap: 16px;
    align-items: start;

    @include container-down(1240px) {
      grid-template-columns: 240px minmax(0, 1fr);
    }

    @include container-down(880px) {
      grid-template-columns: minmax(0, 1fr);
    }
  }

  .catalog-panel,
  .console-panel,
  .readout-panel {
    ::v-deep .el-card__header {
      padding: 12px 16px;
    }

    ::v-deep .el-card__body {
      padding: 0;
    }
  }

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .panel-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .catalog-body {
    padding: 12px 16px 0;
  }

  .getter-list {
    list-style: none;
    margin: 12px 0 0;
    padding: 0;
    max-height: calc(100vh - 330px);
    overflow-y: auto;

    @include respond-to(mobile) {
      max-height: 300px;
    }
  }

  .getter-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    border-left: 2px solid transparent;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.2s, border-color 0.2s;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #ecf5ff;
      border-left-color: $menuActiveText;

      .getter-icon {
        color: $menuActiveText;
      }

      .getter-name {
        color: $menuActiveText;
      }
    }

    @media (hover: none) {
      &:active {
        background: #f5f7fa;
      }
    }
  }

  .getter-icon {
    flex: none;
    font-size: 16px;
    color: #909399;
  }

  .getter-body {
    flex: 1;
    min-width: 0;
  }

  .getter-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .getter-id {
    margin-top: 2px;
    font-family: Menlo, Consolas, monospace;
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .list-hint {
    padding: 10px 16px 14px;
    font-size: 13px;
    color: #909399;
  }

  .console-empty {
    padding: 60px 20px;
    text-align: center;

    .empty-icon {
      font-size: 42px;
      color: #dcdfe6;
    }
  }

  .empty-title {
    margin: 12px 0 0;
    font-size: 15px;
    color: #606266;
  }

  .empty-desc {
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.7;
    color: #909399;
  }

  .console-head {
    padding: 16px 16px 12px;
    border-bottom: 1px solid #ebeef5;
  }

  .console-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .console-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .console-desc {
    margin: 10px 0 0;
    font-size: 13px;
    line-height: 1.7;
    color: #606266;
  }

  .params {
    padding: 14px 16px 0;
  }

  .section-head {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  .section-label {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .section-hint {
    font-size: 12px;
    color: #909399;
  }

  .no-params {
    padding: 12px;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    font-size: 13px;
    color: #909399;
  }

  .param-form {
    margin-bottom: 4px;

    ::v-deep .el-form-item {
      margin-bottom: 16px;
    }

    .param-label {
      font-size: 13px;
      color: #606266;
    }

    .param-info {
      margin-left: 4px;
      color: #c0c4cc;
      cursor: help;
    }
  }

  .param-control-row {
    display: flex;
    align-items: flex-start;
    gap: 8px;

    ::v-deep .getter-param-field {
      flex: 1;
      min-width: 0;
    }

    .param-type {
      flex: none;
      margin-top: 2px;
    }
  }

  .field-desc {
    margin-top: 2px;
    font-size: 12px;
    line-height: 1.5;
    color: #909399;
    word-break: break-word;
  }

  .param-error {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.4;
    color: #f56c6c;
  }

  .run-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    padding: 4px 16px 0;

    @include respond-to(phone) {
      .el-button--primary {
        width: 100%;
      }
    }
  }

  .run-hint {
    margin-left: auto;
    font-size: 12px;
    color: #c0c4cc;
  }

  .payload {
    margin: 14px 16px 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;

    &[open] .payload-summary::after {
      transform: rotate(90deg);
    }
  }

  .payload-summary {
    display: flex;
    align-items: baseline;
    gap: 8px;
    padding: 8px 12px;
    background: #fafafa;
    cursor: pointer;
    list-style: none;

    &::-webkit-details-marker {
      display: none;
    }

    &::after {
      content: '▸';
      margin-left: auto;
      font-size: 12px;
      color: #909399;
      transition: transform 0.2s;
    }
  }

  .payload-label {
    font-size: 13px;
    color: #606266;
  }

  .payload-hint {
    font-size: 12px;
    color: #909399;
  }

  .payload-body {
    position: relative;
    border-top: 1px solid #ebeef5;

    pre {
      margin: 0;
      padding: 10px 12px 14px;
      max-height: 220px;
      overflow: auto;
      font-family: Menlo, Consolas, monospace;
      font-size: 12px;
      line-height: 1.6;
      color: #303133;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .el-button {
      position: absolute;
      top: 2px;
      right: 8px;
    }
  }

  .readout-panel {
    position: sticky;
    top: 84px;

    @include container-down(1240px) {
      grid-column: 1 / -1;
      position: static;
    }

    @include container-down(880px) {
      grid-column: auto;
    }
  }

  .readout-tools {
    display: flex;
    gap: 4px;
  }

  .readout-body {
    padding: 16px;
    min-height: 120px;
  }

  .readout-idle {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 28px 0;
    font-size: 13px;
    color: #909399;

    i {
      font-size: 28px;
      color: #dcdfe6;
    }
  }

  .result-error {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    padding: 10px 12px;
    font-size: 13px;
    line-height: 1.6;
    color: #f56c6c;
    background: #fef0f0;
    border-radius: 4px;
    word-break: break-word;
  }

  .readout-raw {
    margin: 0;
    padding: 10px 12px;
    max-height: 320px;
    overflow: auto;
    font-family: Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 1.6;
    color: #303133;
    background: #f5f7fa;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .history {
    padding: 12px 16px 14px;
    border-top: 1px solid #ebeef5;
  }

  .history-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 4px;
  }

  .history-title {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
  }

  .history-list {
    list-style: none;
    margin: 0;
    padding: 0;
    max-height: 200px;
    overflow-y: auto;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 7px 0;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #f5f7fa;
    }

    &.is-failed .history-value {
      color: #f56c6c;
    }

    @media (hover: none) {
      &:active {
        background: #f5f7fa;
      }
    }
  }

  .history-clock {
    flex: none;
    font-family: Menlo, Consolas, monospace;
    font-size: 12px;
    color: #909399;
  }

  .history-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .history-name {
    font-size: 12px;
    color: #606266;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .history-value {
    font-family: Menlo, Consolas, monospace;
    font-size: 12px;
    color: #67c23a;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .history-cost {
    flex: none;
    font-size: 12px;
    color: #c0c4cc;
  }

  .history-empty {
    margin: 4px 0 0;
    font-size: 12px;
    color: #909399;
  }
}
</style>
