<template>
  <div :class="['getter-param-field', 'ui-' + field.uiType, {'is-invalid': !!error}]">
    <el-select
      v-if="field.uiType === 'select'"
      :value="value"
      :placeholder="field.placeholder || $t('valueGetter.pleaseSelect')"
      :filterable="field.filterable"
      :allow-create="field.allowCreate"
      :default-first-option="field.allowCreate"
      :size="size"
      clearable
      class="field-control"
      @change="emitInput"
    >
      <el-option
        v-for="item in field.options"
        :key="item.value"
        :label="item.label || item.value"
        :value="item.value"
      />
    </el-select>

    <el-input-number
      v-else-if="field.uiType === 'number'"
      :value="value"
      :min="field.min"
      :max="field.max"
      :step="field.step || 1"
      :placeholder="field.placeholder || $t('valueGetter.pleaseInputNumber')"
      :size="size"
      controls-position="right"
      class="field-control"
      @change="emitInput"
    />

    <div v-else-if="field.uiType === 'boolean'" class="field-switch">
      <el-switch :value="value === true" :size="size" @change="emitInput" />
      <span class="switch-text">{{ value === true ? $t('valueGetter.booleanTrue') : $t('valueGetter.booleanFalse') }}</span>
    </div>

    <el-date-picker
      v-else-if="isDateField"
      :value="value"
      :type="pickerType"
      :value-format="field.dateFormat"
      :placeholder="field.placeholder || $t('valueGetter.pleaseSelect')"
      :size="size"
      class="field-control"
      @change="emitInput"
    />

    <div v-else-if="field.uiType === 'json'" class="field-json">
      <el-input
        :value="value"
        type="textarea"
        :rows="4"
        :placeholder="field.placeholder || $t('valueGetter.jsonPlaceholder')"
        :size="size"
        resize="vertical"
        class="field-control"
        @input="emitInput"
      />
      <el-button type="text" size="mini" class="json-format" @click="formatJson">
        {{ $t('valueGetter.formatJson') }}
      </el-button>
    </div>

    <el-input
      v-else-if="field.uiType === 'textarea'"
      :value="value"
      type="textarea"
      :rows="3"
      :maxlength="field.maxLength"
      :show-word-limit="!!field.maxLength"
      :placeholder="field.placeholder || $t('valueGetter.pleaseInput')"
      :size="size"
      resize="vertical"
      class="field-control"
      @input="emitInput"
    />

    <el-input
      v-else
      :value="value"
      :maxlength="field.maxLength"
      :placeholder="field.placeholder || $t('valueGetter.pleaseInput')"
      :size="size"
      clearable
      class="field-control"
      @input="emitInput"
    />

    <div v-if="error" class="field-error">{{ errorText }}</div>
  </div>
</template>

<script>
/**
 * 单个 getter 参数的类型特化输入控件。
 * <p>
 * 依据后端 ParamType 声明的 inputType 渲染对应控件：下拉、数字、开关、日期时间、
 * 多行文本、JSON 编辑器与普通文本，并把字段级校验结果转换为可读提示。
 * </p>
 */
export default {
  name: 'GetterParamField',
  props: {
    field: {
      type: Object,
      required: true
    },
    value: {
      type: [String, Number, Boolean, Array, Object],
      default: null
    },
    error: {
      type: String,
      default: ''
    },
    size: {
      type: String,
      default: 'small'
    }
  },
  computed: {
    /**
     * 是否为日期类字段
     *
     * @returns {boolean} 是否为日期类
     */
    isDateField() {
      return ['date', 'datetime', 'time'].indexOf(this.field.uiType) >= 0
    },
    /**
     * 日期选择器粒度
     *
     * @returns {string} Element 日期类型
     */
    pickerType() {
      if (this.field.uiType === 'time') {
        return 'time'
      }
      return this.field.uiType === 'date' ? 'date' : 'datetime'
    },
    /**
     * 校验失败提示文案
     *
     * @returns {string} 提示文案
     */
    errorText() {
      switch (this.error) {
        case 'required':
          return this.$t('valueGetter.errRequired')
        case 'number':
          return this.$t('valueGetter.errNumber')
        case 'json':
          return this.$t('valueGetter.errJson')
        case 'maxLength':
          return this.$t('valueGetter.errMaxLength', { max: this.field.maxLength })
        case 'min':
          return this.$t('valueGetter.errMin', { min: this.field.min })
        case 'max':
          return this.$t('valueGetter.errMax', { max: this.field.max })
        case 'pattern':
          return this.$t('valueGetter.errPattern')
        default:
          return ''
      }
    }
  },
  methods: {
    /**
     * 对外发出控件值
     *
     * @param {*} val 控件值
     */
    emitInput(val) {
      this.$emit('input', val)
    },
    /**
     * 格式化 JSON 文本，非法 JSON 保持原样
     */
    formatJson() {
      try {
        this.$emit('input', JSON.stringify(JSON.parse(this.value), null, 2))
      } catch (e) {
        this.$emit('invalid-json')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.getter-param-field {
  @include responsive-container;

  .field-control {
    width: 100%;
  }

  .field-switch {
    display: flex;
    align-items: center;
    gap: 10px;
    min-height: 32px;
  }

  .switch-text {
    font-size: 12px;
    color: #606266;
    font-family: ui-monospace, 'SFMono-Regular', 'Cascadia Mono', Menlo, Consolas, monospace;
  }

  .field-json {
    position: relative;

    ::v-deep .el-textarea__inner {
      font-family: ui-monospace, 'SFMono-Regular', 'Cascadia Mono', Menlo, Consolas, monospace;
      font-size: 12px;
      line-height: 1.6;
    }
  }

  .json-format {
    position: absolute;
    right: 8px;
    top: 4px;
    padding: 0;
    font-size: 12px;
  }

  &.is-invalid {
    ::v-deep .el-input__inner,
    ::v-deep .el-textarea__inner {
      border-color: #f56c6c;
    }
  }

  .field-error {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.4;
    color: #f56c6c;
  }
}
</style>
