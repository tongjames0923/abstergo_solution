<template>
  <div class="value-getter-operand">
    <div class="operand-mode">
      <el-radio-group v-model="mode" size="mini" @change="handleModeChange">
        <el-radio-button label="literal">{{ $t('valueGetter.literal') }}</el-radio-button>
        <el-radio-button label="getter">{{ $t('valueGetter.getterCall') }}</el-radio-button>
      </el-radio-group>
      <span v-if="depth > 0" class="depth-tag">{{ $t('valueGetter.nestedDepth', { depth }) }}</span>
    </div>

    <div v-if="mode === 'literal'" class="literal-input">
      <el-input-number
        v-if="literalUiType === 'number'"
        :value="literalNumber"
        controls-position="right"
        size="small"
        class="literal-control"
        @change="handleLiteralNumber"
      />
      <el-date-picker
        v-else-if="literalUiType === 'date'"
        :value="literalValue"
        :type="literalDateType"
        :value-format="literalDateFormat"
        :placeholder="$t('valueGetter.literalDatePlaceholder')"
        size="small"
        class="literal-control"
        @change="handleLiteralInput"
      />
      <el-select
        v-else-if="literalUiType === 'boolean'"
        :value="literalValue"
        :placeholder="$t('valueGetter.pleaseSelect')"
        size="small"
        clearable
        class="literal-control"
        @change="handleLiteralInput"
      >
        <el-option :label="$t('valueGetter.booleanTrue')" value="true" />
        <el-option :label="$t('valueGetter.booleanFalse')" value="false" />
      </el-select>
      <el-input
        v-else
        :value="literalValue"
        :placeholder="$t('valueGetter.literalPlaceholder')"
        size="small"
        clearable
        class="literal-control"
        @input="handleLiteralInput"
      />
    </div>

    <div v-else class="getter-summary">
      <template v-if="selectedGetter">
        <div class="summary-body">
          <div class="summary-title">
            <i class="el-icon-cpu" />
            <span class="summary-name">{{ selectedGetter.getterName }}</span>
            <el-tag size="mini" type="info" effect="plain">{{ selectedGetter.getterId }}</el-tag>
          </div>
          <div class="summary-params">{{ paramsSummary }}</div>
        </div>
        <div class="summary-actions">
          <el-button type="text" size="mini" @click="openDialog">{{ $t('common.edit') }}</el-button>
          <el-button type="text" size="mini" @click="clearGetter">{{ $t('valueGetter.clearCall') }}</el-button>
        </div>
      </template>
      <el-button
        v-else
        type="primary"
        plain
        size="small"
        icon="el-icon-plus"
        @click="openDialog"
      >
        {{ $t('valueGetter.selectGetter') }}
      </el-button>
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      :title="dialogTitle"
      :width="dialogWidth"
      append-to-body
      :close-on-click-modal="false"
      custom-class="value-getter-operand-dialog"
      @closed="handleDialogClosed"
    >
      <el-form label-width="120px" size="small" class="operand-dialog-form">
        <el-form-item :label="$t('valueGetter.getter')" required>
          <el-select
            v-model="draftGetterId"
            :placeholder="$t('valueGetter.selectGetter')"
            filterable
            class="dialog-getter-select"
            @change="handleDraftGetterChange"
          >
            <el-option
              v-for="g in availableGetters"
              :key="g.getterId"
              :label="g.getterName"
              :value="g.getterId"
            />
          </el-select>
        </el-form-item>

        <logic-condition-builder
          v-if="draftIsLogicCondition && logicTree"
          :value="logicTree"
          :getters="getters"
          :operand-component="selfComponent"
          :depth="depth + 1"
          @input="setLogicTree"
        />

        <template v-else>
          <el-form-item
            v-for="field in draftFields"
            :key="field.name"
            :label="field.label"
            :required="field.required"
          >
            <value-getter-operand
              v-if="field.uiType === 'operand'"
              :value="draftFormData[field.name]"
              :getters="getters"
              :depth="depth + 1"
              :value-type="field.type"
              @input="setDraftField(field.name, $event)"
            />
            <getter-param-field
              v-else
              :field="field"
              :value="draftFormData[field.name]"
              :error="draftErrorOf(field)"
              @input="setDraftField(field.name, $event)"
            />
            <div v-if="field.description" class="field-desc">{{ field.description }}</div>
          </el-form-item>
        </template>

        <div v-if="!draftGetterId" class="dialog-empty">
          <i class="el-icon-cpu" />
          <span>{{ $t('valueGetter.selectGetterFirst') }}</span>
        </div>
      </el-form>

      <div slot="footer">
        <el-button size="small" @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button size="small" type="primary" @click="confirmDialog">{{ $t('common.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  buildFormData,
  isBlank,
  isGetterOperand,
  parseGetterOperand,
  resolveUiType,
  stringifyGetterOperand,
  toParamFields,
  toParamValues,
  validateFields
} from '@/utils/valueGetter'
import GetterParamField from '@/components/GetterParamField'
import LogicConditionBuilder from '@/components/LogicConditionBuilder'
import ResponsiveMixin from '@/mixins/responsive'
import {compileGroup, countConditions, createGroup, hasCondition, restoreLogicTree} from '@/utils/logicCondition'

const CONDITION_GETTER_ID = 'ConditionGetter'
const LOGIC_CONDITION_GETTER_ID = 'LogicConditionGetter'

/**
 * 嵌套 getter 调用操作数编辑器。
 * <p>
 * 字面值直接内联输入；选择 getter 调用后在弹窗内选择 getter 并按参数类型配置，
 * 确认后以「getter 名称 + 参数摘要」呈现，可再次进入弹窗修改。弹窗内操作数参数
 * 通过组件自引用递归展开，每层各开一个弹窗，避免无限嵌套的内联表单。
 * </p>
 */
export default {
  name: 'ValueGetterOperand',
  components: { GetterParamField, LogicConditionBuilder },
  mixins: [ResponsiveMixin],
  props: {
    value: {
      type: String,
      default: ''
    },
    getters: {
      type: Array,
      default: () => []
    },
    depth: {
      type: Number,
      default: 0
    },
    valueType: {
      type: String,
      default: 'string'
    }
  },
  data() {
    return {
      mode: 'literal',
      literalValue: '',
      selectedGetterId: '',
      formData: {},
      dialogVisible: false,
      draftGetterId: '',
      draftFormData: {},
      logicTree: null,
      showDraftErrors: false,
      pendingParams: null,
      emitting: false
    }
  },
  computed: {
    /**
     * 字面值控件类型
     *
     * @returns {string} number / date / string
     */
    literalUiType() {
      return resolveUiType(this.valueType).uiType
    },
    /**
     * 字面值日期控件类型
     *
     * @returns {string} time 或 datetime
     */
    literalDateType() {
      return this.valueType.toLowerCase() === 'time' ? 'time' : 'datetime'
    },
    /**
     * 字面值日期格式
     *
     * @returns {string} value-format 格式
     */
    literalDateFormat() {
      return resolveUiType(this.valueType).dateFormat || 'yyyy-MM-dd HH:mm:ss'
    },
    /**
     * 数字控件值
     *
     * @returns {number|null} 数值
     */
    literalNumber() {
      if (this.literalValue === '' || this.literalValue === null) {
        return null
      }
      const n = Number(this.literalValue)
      return Number.isNaN(n) ? null : n
    },
    /**
     * 最大嵌套深度
     *
     * @returns {number} 深度上限
     */
    maxDepth() {
      return 6
    },
    /**
     * 可选的 getter 列表，达到深度上限时排除条件 getter
     *
     * @returns {Array} getter 列表
     */
    availableGetters() {
      if (this.depth >= this.maxDepth) {
        return this.getters.filter(g => g.getterId !== CONDITION_GETTER_ID)
      }
      return this.getters
    },
    /**
     * 当前选中的 getter 元信息
     *
     * @returns {Object|null} 元信息
     */
    selectedGetter() {
      if (!this.selectedGetterId) {
        return null
      }
      return this.getters.find(g => g.getterId === this.selectedGetterId) || null
    },
    /**
     * 当前 getter 的参数字段
     *
     * @returns {Array} 字段列表
     */
    paramFields() {
      return this.selectedGetter ? toParamFields(this.selectedGetter.paramTypes, this.selectedGetter.getterId) : []
    },
    /**
     * 弹窗中选中的 getter 元信息
     *
     * @returns {Object|null} 元信息
     */
    draftGetter() {
      if (!this.draftGetterId) {
        return null
      }
      return this.getters.find(g => g.getterId === this.draftGetterId) || null
    },
    /**
     * 弹窗中的参数字段
     *
     * @returns {Array} 字段列表
     */
    draftFields() {
      return this.draftGetter ? toParamFields(this.draftGetter.paramTypes, this.draftGetter.getterId) : []
    },
    /**
     * 弹窗中是否选择了逻辑条件 getter
     *
     * @returns {boolean} 是否为逻辑条件 getter
     */
    draftIsLogicCondition() {
      return this.draftGetterId === LOGIC_CONDITION_GETTER_ID
    },
    /**
     * 自身组件构造器，注入给逻辑条件构建器实现递归编辑
     *
     * @returns {Object} 组件构造器
     */
    selfComponent() {
      return this.constructor
    },
    /**
     * 弹窗中的参数校验结果
     *
     * @returns {Object} 参数名到失败规则的映射
     */
    draftErrors() {
      return validateFields(this.draftFields, this.draftFormData)
    },
    /**
     * 弹窗标题
     *
     * @returns {string} 标题文案
     */
    dialogTitle() {
      return this.$t('valueGetter.configCall')
    },
    /**
     * 弹窗宽度，手机端占满可视宽度
     *
     * @returns {string} CSS 宽度
     */
    dialogWidth() {
      return this.isPhone ? '92%' : '560px'
    },
    /**
     * 已配置参数的摘要文本
     *
     * @returns {string} 摘要
     */
    paramsSummary() {
      const fields = this.paramFields
      if (!fields.length) {
        return this.$t('valueGetter.noArgs')
      }
      if (this.selectedGetterId === LOGIC_CONDITION_GETTER_ID) {
        const opField = fields.find(f => f.isEnum)
        const op = opField ? this.formData[opField.name] : ''
        const tree = restoreLogicTree(op, fields.filter(f => f.uiType === 'operand').map(f => this.formData[f.name]))
        const mode = this.$t(op === 'or' ? 'logicCondition.any' : 'logicCondition.all')
        return mode + ' · ' + this.$t('logicCondition.filled', { count: countConditions(tree) })
      }
      return fields.map(field => {
        const raw = this.formData[field.name]
        let text
        if (isBlank(raw)) {
          text = '—'
        } else if (isGetterOperand(raw)) {
          text = this.$t('valueGetter.nestedCall')
        } else {
          text = String(raw)
        }
        return field.name + '=' + text
      }).join(' · ')
    },
    /**
     * 序列化后的操作数值
     *
     * @returns {string} 字面量或 getter 调用 JSON
     */
    serialized() {
      if (this.mode !== 'getter') {
        return this.literalValue
      }
      if (!this.selectedGetterId) {
        return ''
      }
      return stringifyGetterOperand(this.selectedGetterId, toParamValues(this.paramFields, this.formData))
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        this.applyValue(val)
      }
    },
    getters() {
      if (this.mode === 'getter' && this.selectedGetterId && this.pendingParams) {
        this.initFormData(this.pendingParams)
        this.pendingParams = null
      }
    },
    serialized(val) {
      if (this.emitting) {
        return
      }
      this.emitting = true
      this.$emit('input', val)
      this.$nextTick(() => {
        this.emitting = false
      })
    }
  },
  methods: {
    /**
     * 应用外部传入的操作数
     *
     * @param {string} val 字面量或 getter 调用 JSON
     */
    applyValue(val) {
      const parsed = parseGetterOperand(val)
      if (parsed) {
        this.mode = 'getter'
        this.selectedGetterId = parsed.getterId
        if (this.selectedGetter) {
          this.initFormData(parsed.paramValues)
        } else {
          this.pendingParams = parsed.paramValues
        }
        return
      }
      this.mode = 'literal'
      this.literalValue = val == null ? '' : String(val)
    },

    /**
     * 按 getter 元信息初始化参数表单
     *
     * @param {Array} paramValues 已有参数值
     */
    initFormData(paramValues) {
      const data = buildFormData(this.paramFields)
      const values = Array.isArray(paramValues) ? paramValues : []
      this.paramFields.forEach((field, index) => {
        const found = values.find(p => p && p.name === field.name) || values[index]
        if (found && found.value !== undefined && found.value !== null) {
          data[field.name] = found.value
        }
      })
      this.formData = data
    },

    /**
     * 切换字面值 / getter 调用模式
     *
     * @param {string} mode 目标模式
     */
    handleModeChange(mode) {
      if (mode === 'getter' && !this.selectedGetterId) {
        this.openDialog()
        return
      }
      this.emitValue()
    },

    /**
     * 打开 getter 配置弹窗
     */
    openDialog() {
      this.draftGetterId = this.selectedGetterId || ''
      const fields = this.draftGetter
        ? toParamFields(this.draftGetter.paramTypes, this.draftGetter.getterId)
        : []
      const data = buildFormData(fields)
      fields.forEach(field => {
        const current = this.formData[field.name]
        if (current !== undefined && current !== null) {
          data[field.name] = current
        }
      })
      this.draftFormData = data
      this.logicTree = this.draftIsLogicCondition ? this.buildDraftLogicTree() : null
      this.showDraftErrors = false
      this.dialogVisible = true
    },

    /**
     * 从草稿参数位还原逻辑条件表达式树
     *
     * @returns {Object} 分组节点
     */
    buildDraftLogicTree() {
      const fields = this.draftFields
      const opField = fields.find(f => f.isEnum)
      const operandFields = fields.filter(f => f.type === 'operand')
      return restoreLogicTree(
        opField ? this.draftFormData[opField.name] : 'and',
        operandFields.map(f => this.draftFormData[f.name])
      )
    },

    /**
     * 弹窗内切换 getter
     *
     * @param {string} getterId getter ID
     */
    handleDraftGetterChange(getterId) {
      const getter = this.getters.find(g => g.getterId === getterId)
      this.draftFormData = buildFormData(getter ? toParamFields(getter.paramTypes, getter.getterId) : [])
      this.logicTree = getterId === LOGIC_CONDITION_GETTER_ID ? createGroup() : null
      this.showDraftErrors = false
    },

    /**
     * 更新弹窗中的参数值
     *
     * @param {string} name 参数名
     * @param {*} val 参数值
     */
    setDraftField(name, val) {
      this.$set(this.draftFormData, name, val)
    },

    /**
     * 更新逻辑条件表达式树并同步回各参数位
     *
     * @param {Object} val 分组节点
     */
    setLogicTree(val) {
      this.logicTree = val
      const params = compileGroup(val)
      const fields = this.draftFields
      const opField = fields.find(f => f.isEnum)
      const operandFields = fields.filter(f => f.type === 'operand')
      if (opField) {
        this.$set(this.draftFormData, opField.name, val.op)
      }
      operandFields.forEach((field, index) => {
        const param = params[index + 1]
        this.$set(this.draftFormData, field.name, param && param.value ? param.value : '')
      })
    },

    /**
     * 弹窗字段错误提示
     *
     * @param {Object} field 字段描述
     * @returns {string} 错误规则标识
     */
    draftErrorOf(field) {
      return this.showDraftErrors ? this.draftErrors[field.name] || '' : ''
    },

    /**
     * 确认 getter 配置
     */
    confirmDialog() {
      if (!this.draftGetterId) {
        this.$message.warning(this.$t('valueGetter.selectGetterFirst'))
        return
      }
      this.showDraftErrors = true
      if (this.draftIsLogicCondition && !hasCondition(this.logicTree)) {
        this.$message.warning(this.$t('logicCondition.requireOne'))
        return
      }
      if (Object.keys(this.draftErrors).length) {
        this.$message.warning(this.$t('valueGetter.fixParams'))
        return
      }
      this.selectedGetterId = this.draftGetterId
      this.formData = { ...this.draftFormData }
      this.mode = 'getter'
      this.dialogVisible = false
      this.emitValue()
    },

    /**
     * 弹窗关闭后复位草稿状态
     */
    handleDialogClosed() {
      this.draftGetterId = ''
      this.draftFormData = {}
      this.logicTree = null
      this.showDraftErrors = false
      if (this.mode === 'getter' && !this.selectedGetterId) {
        this.mode = 'literal'
      }
    },

    /**
     * 清除已配置的 getter 调用
     */
    clearGetter() {
      this.selectedGetterId = ''
      this.formData = {}
      this.emitValue()
    },

    /**
     * 输入字面量
     *
     * @param {string} val 字面量内容
     */
    handleLiteralInput(val) {
      this.literalValue = val == null ? '' : String(val)
      this.emitValue()
    },

    /**
     * 数字字面量输入
     *
     * @param {number|null} val 数值
     */
    handleLiteralNumber(val) {
      this.literalValue = val === null || val === undefined ? '' : String(val)
      this.emitValue()
    },

    /**
     * 对外发出序列化结果
     */
    emitValue() {
      this.emitting = true
      this.$emit('input', this.serialized)
      this.$nextTick(() => {
        this.emitting = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.value-getter-operand {
  @include responsive-container;

  .operand-mode {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .depth-tag {
    font-size: 12px;
    color: #909399;
  }

  .literal-input {
    margin-top: 8px;
  }

  .literal-control {
    width: 100%;
  }

  .getter-summary {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 8px;
    padding: 8px 10px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #fafafa;
  }

  .summary-body {
    flex: 1;
    min-width: 0;
  }

  .summary-title {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;

    i {
      color: #909399;
    }
  }

  .summary-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
  }

  .summary-params {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.5;
    color: #909399;
    word-break: break-word;
  }

  .summary-actions {
    flex: none;
  }

  .dialog-getter-select {
    width: 100%;
  }

  .operand-dialog-form {
    ::v-deep .el-form-item:last-child {
      margin-bottom: 0;
    }
  }

  .field-desc {
    font-size: 12px;
    line-height: 1.5;
    color: #909399;
  }

  .dialog-empty {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 18px 0;
    font-size: 13px;
    color: #909399;

    i {
      font-size: 16px;
    }
  }
}
</style>
