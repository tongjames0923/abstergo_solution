<template>
  <div class="value-getter-operand">
    <el-radio-group v-model="mode" size="mini" @change="handleModeChange">
      <el-radio-button label="literal">{{ $t('valueGetter.literal') }}</el-radio-button>
      <el-radio-button label="getter">{{ $t('valueGetter.getterCall') }}</el-radio-button>
    </el-radio-group>
    <span v-if="depth > 0" class="depth-tag">{{ $t('valueGetter.nestedDepth', { depth }) }}</span>

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
      <el-input
        v-else
        :value="literalValue"
        :placeholder="$t('valueGetter.literalPlaceholder')"
        size="small"
        clearable
        @input="handleLiteralInput"
      />
    </div>

    <div v-else class="getter-config">
      <el-select
        v-model="selectedGetterId"
        :placeholder="$t('valueGetter.selectGetter')"
        size="small"
        filterable
        class="getter-select"
        @change="handleGetterChange"
      >
        <el-option
          v-for="g in availableGetters"
          :key="g.getterId"
          :label="g.getterName"
          :value="g.getterId"
        />
      </el-select>

      <div v-if="selectedGetter && paramFields.length" class="param-list">
        <div v-for="field in paramFields" :key="field.name" class="param-item">
          <div class="param-label">
            <span>{{ field.name }}</span>
            <span v-if="field.required" class="required-flag">*</span>
          </div>
          <div v-if="field.description" class="param-desc">{{ field.description }}</div>

          <value-getter-operand
            v-if="field.uiType === 'operand'"
            :value="formData[field.name]"
            :getters="getters"
            :depth="depth + 1"
            :value-type="field.type"
            @input="setFieldValue(field.name, $event)"
          />

          <el-select
            v-else-if="field.isEnum"
            :value="formData[field.name]"
            :placeholder="field.description || $t('valueGetter.pleaseSelect')"
            size="small"
            clearable
            class="param-control"
            @change="setFieldValue(field.name, $event)"
          >
            <el-option v-for="ev in field.enumValues" :key="ev.value" :label="ev.label || ev.value" :value="ev.value" />
          </el-select>

          <el-input-number
            v-else-if="field.uiType === 'number'"
            :value="formData[field.name]"
            size="small"
            controls-position="right"
            class="param-control"
            @change="setFieldValue(field.name, $event)"
          />

          <el-date-picker
            v-else-if="field.uiType === 'date'"
            :value="formData[field.name]"
            :type="field.type === 'time' ? 'time' : 'datetime'"
            :value-format="field.dateFormat"
            :placeholder="field.description || $t('valueGetter.pleaseSelect')"
            size="small"
            class="param-control"
            @change="setFieldValue(field.name, $event)"
          />

          <el-input
            v-else
            :value="formData[field.name]"
            :placeholder="field.description || $t('valueGetter.pleaseInput')"
            size="small"
            clearable
            @input="setFieldValue(field.name, $event)"
          />
        </div>
      </div>

      <div v-else-if="selectedGetterId && !selectedGetter" class="getter-missing">
        {{ $t('valueGetter.getterNotFound', { id: selectedGetterId }) }}
      </div>
    </div>
  </div>
</template>

<script>
import {
  buildFormData,
  parseGetterOperand,
  resolveUiType,
  stringifyGetterOperand,
  toParamFields,
  toParamValues
} from '@/utils/valueGetter'

const CONDITION_GETTER_ID = 'ConditionGetter'

/**
 * 嵌套 getter 操作数编辑器。
 * <p>
 * 支持字面量与 getter 调用两种模式；选择 getter 后按其元信息渲染参数控件。
 * 组件通过 name 自引用实现递归，因此选中 ConditionGetter 时其左右操作数
 * （同为 operand 类型）会继续展开编辑器，形成任意深度的条件嵌套。
 * </p>
 */
export default {
  name: 'ValueGetterOperand',
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
     * 序列化后的操作数值
     *
     * @returns {string} 字面量或 getter 调用 JSON
     */
    serialized() {
      return this.mode === 'getter'
        ? stringifyGetterOperand(this.selectedGetterId, toParamValues(this.paramFields, this.formData))
        : this.literalValue
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
     * 切换模式
     *
     * @param {string} mode 目标模式
     */
    handleModeChange(mode) {
      if (mode === 'literal') {
        this.selectedGetterId = ''
        this.formData = {}
      } else {
        this.literalValue = ''
      }
      this.emitValue()
    },

    /**
     * 选择 getter
     *
     * @param {string} getterId getter ID
     */
    handleGetterChange(getterId) {
      const getter = this.getters.find(g => g.getterId === getterId)
      this.formData = buildFormData(getter ? toParamFields(getter.paramTypes, getter.getterId) : [])
      this.emitValue()
    },

    /**
     * 设置参数值
     *
     * @param {string} name 参数名
     * @param {*} val 参数值
     */
    setFieldValue(name, val) {
      this.$set(this.formData, name, val)
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

  .depth-tag {
    margin-left: 8px;
    font-size: 12px;
    color: #909399;
  }

  .getter-config {
    margin-top: 8px;
    padding: 10px;
    background: #fafafa;
    border: 1px solid #ebeef5;
    border-radius: 4px;
  }

  .getter-select,
  .param-control,
  .literal-control {
    width: 100%;
  }

  .param-list {
    margin-top: 10px;
  }

  .param-item {
    padding-top: 10px;
    border-top: 1px dashed #ebeef5;

    &:first-child {
      padding-top: 0;
      border-top: none;
    }
  }

  .param-label {
    font-size: 13px;
    color: #606266;
    margin-bottom: 4px;
  }

  .required-flag {
    color: #f56c6c;
    margin-left: 2px;
  }

  .param-desc {
    font-size: 12px;
    color: #909399;
    margin-bottom: 6px;
    line-height: 1.4;
  }

  .getter-missing {
    margin-top: 8px;
    font-size: 12px;
    color: #f56c6c;
  }
}
</style>
