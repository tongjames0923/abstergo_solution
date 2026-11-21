<template>
  <div class="event-param-form">
    <el-form
      v-if="fields.length"
      :label-width="labelWidth"
      :label-position="labelPosition"
      size="small"
      @submit.native.prevent
    >
      <el-form-item v-for="field in fields" :key="field.name" :required="field.required">
        <template slot="label">
          <span class="field-label">{{ field.label }}</span>
          <el-tooltip v-if="field.description" :content="field.description" placement="top">
            <i class="el-icon-info field-info" />
          </el-tooltip>
        </template>
        <getter-param-field
          :field="field"
          :value="formData[field.name]"
          :error="errors[field.name]"
          @input="setValue(field.name, $event)"
        />
        <div v-if="field.description" class="field-desc">{{ field.description }}</div>
      </el-form-item>
    </el-form>
    <div v-else class="form-empty">{{ emptyText }}</div>
  </div>
</template>

<script>
import GetterParamField from '@/components/GetterParamField'
import { buildFormData, parseDefaultValue, toParamFields, toParamValues, validateFields } from '@/utils/valueGetter'

/**
 * 基于 ParamType 声明的动态参数表单。
 * <p>
 * 与取值器调试台复用同一套控件映射与校验规则，供事件模拟上报与动作参数编排使用；
 * 对外暴露校验与序列化方法，父组件无需重复实现参数转换逻辑。
 * </p>
 */
export default {
  name: 'EventParamForm',
  components: { GetterParamField },
  props: {
    paramTypes: {
      type: Array,
      default: () => []
    },
    labelWidth: {
      type: String,
      default: '112px'
    },
    labelPosition: {
      type: String,
      default: 'right'
    },
    emptyText: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      fields: [],
      formData: {},
      errors: {}
    }
  },
  watch: {
    paramTypes: {
      immediate: true,
      handler() {
        this.reset()
      }
    }
  },
  methods: {
    /**
     * 重置表单为参数默认值
     */
    reset() {
      this.fields = toParamFields(this.paramTypes)
      this.formData = buildFormData(this.fields)
      this.errors = {}
    },
    /**
     * 更新字段值并清理该字段错误
     *
     * @param {string} name 参数名
     * @param {*} value 参数值
     */
    setValue(name, value) {
      this.$set(this.formData, name, value)
      if (this.errors[name]) {
        this.$set(this.errors, name, '')
      }
    },
    /**
     * 用已保存的参数值回填表单
     *
     * @param {Array} values ParamValue 列表
     */
    setValues(values) {
      if (!Array.isArray(values)) {
        return
      }
      const fieldMap = {}
      this.fields.forEach(field => {
        fieldMap[field.name] = field
      })
      values.forEach(param => {
        if (!param || !param.name || !fieldMap[param.name]) {
          return
        }
        this.$set(this.formData, param.name,
          parseDefaultValue({ ...fieldMap[param.name], defaultValue: param.value }))
      })
    },
    /**
     * 校验参数表单
     *
     * @returns {boolean} 是否全部通过
     */
    validate() {
      this.errors = validateFields(this.fields, this.formData)
      return Object.keys(this.errors).length === 0
    },
    /**
     * 序列化为 ParamValue 列表
     *
     * @returns {Array} 参数值列表
     */
    buildParamValues() {
      return toParamValues(this.fields, this.formData)
    },
    /**
     * 获取当前表单数据副本
     *
     * @returns {Object} 表单数据
     */
    getFormData() {
      return { ...this.formData }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.event-param-form {
  @include responsive-container;
}

.field-label {
  font-size: 13px;
  color: #303133;
}

.field-info {
  margin-left: 4px;
  color: #909399;
  cursor: help;
}

.field-desc {
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.5;
  color: #909399;
}

.form-empty {
  padding: 18px 0;
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>
