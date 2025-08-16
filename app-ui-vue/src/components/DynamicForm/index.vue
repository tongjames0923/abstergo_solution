<template>
  <el-card class="dynamic-form-card" shadow="never">
    <!-- 头部区域 -->
    <div slot="header" class="clearfix card-header">
      <slot name="header">
        <span class="card-title">{{ title }}</span>
        <el-button
          v-if="config.expandable !== false"
          style="float: right; padding: 3px 0"
          type="text"
          @click="toggleExpand"
        >
          {{ isExpanded ? '收起' : '展开' }}
          <i :class="isExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
        </el-button>
      </slot>
    </div>

    <!-- 表单主体 -->
    <el-form
      v-show="isExpanded"
      ref="form"
      :label-width="config.labelWidth || '100px'"
      :model="formData"
      class="dynamic-form"
      v-bind="formProps"
    >
      <!-- 表单前内容 -->
      <slot name="before-form"></slot>

      <!-- 默认布局：根据配置自动生成 -->
      <template v-if="!$slots.default && config.fields">
        <el-row :gutter="config.gutter || 20">
          <el-col
            v-for="field in config.fields"
            :key="field.prop"
            :span="field.span || config.span || 8"
          >
            <slot
              :field="field"
              :name="`form-item-${field.prop}`"
              :set-value="val => setFieldValue(field.prop, val)"
              :value="formData[field.prop]"
            >
              <el-form-item :label="field.label" :prop="field.prop">
                <!-- 输入框 -->
                <el-input
                  v-if="field.type === 'input'"
                  :clearable="field.clearable !== false"
                  :placeholder="field.placeholder"
                  :value="formData[field.prop]"
                  v-bind="field.props"
                  @input="val => setFieldValue(field.prop, val)"
                >
                  <template v-if="field.prefixIcon" #prefix>
                    <i :class="field.prefixIcon"></i>
                  </template>
                </el-input>

                <!-- 选择器 -->
                <el-select
                  v-else-if="field.type === 'select'"
                  :clearable="field.clearable !== false"
                  :placeholder="field.placeholder"
                  :value="formData[field.prop]"
                  v-bind="field.props"
                  @change="val => setFieldValue(field.prop, val)"
                >
                  <el-option
                    v-for="option in (field.options || [])"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>

                <!-- 日期范围 -->
                <el-date-picker
                  v-else-if="field.type === 'date-range'"
                  :end-placeholder="field.endPlaceholder || '结束日期'"
                  :format="field.format || 'yyyy-MM-dd'"
                  :range-separator="field.rangeSeparator || '至'"
                  :start-placeholder="field.startPlaceholder || '开始日期'"
                  :value="formData[field.prop]"
                  :value-format="field.valueFormat || 'yyyy-MM-dd'"
                  class="date-range-picker"
                  type="daterange"
                  v-bind="field.props"
                  @change="val => handleDateRangeChange(field.prop, val)"
                  @input="val => handleDateRangeChange(field.prop, val)"
                />


                <!-- 动态选项 -->
                <DynamicOptions
                  v-else-if="field.type === 'dynamic-options'"
                  :api-url="field.apiUrl"
                  :placeholder="field.placeholder"
                  :value="formData[field.prop]"
                  v-bind="field.props"
                  @change="val => setFieldValue(field.prop, val)"
                />

                <!-- 其他自定义组件 -->
                <component
                  :is="customComponents[field.type]"
                  v-else-if="field.type && customComponents[field.type]"
                  :value="formData[field.prop]"
                  v-bind="field.props"
                  @change="val => setFieldValue(field.prop, val)"
                  @input="val => setFieldValue(field.prop, val)"
                />
              </el-form-item>
            </slot>
          </el-col>
        </el-row>
      </template>

      <!-- 自定义布局 -->
      <slot
        v-else
        :form="form"
        :form-data="formData"
        :set-value="setFieldValue"
      />

      <!-- 表单后内容 -->
      <slot name="after-form"></slot>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <slot :form="form" :form-data="formData" :reset="resetForm" :submit="submitForm" name="actions">
          <el-button icon="el-icon-search" type="primary" @click="submitForm">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetForm">重置</el-button>
        </slot>
      </div>
    </el-form>
  </el-card>
</template>

<script>
import DynamicOptions from '@/components/DynamicOptions'

export default {
  name: 'DynamicForm',
  components: {
    DynamicOptions
  },
  props: {
    // 表单标题
    title: {
      type: String,
      default: ''
    },
    // 表单配置
    config: {
      type: Object,
      default: () => ({})
    },
    // 表单数据
    value: {
      type: Object,
      default: () => ({})
    },
    // 表单属性
    formProps: {
      type: Object,
      default: () => ({})
    },
    // 自定义组件
    customComponents: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      isExpanded: this.config.expanded !== false,
      formData: this.initializeFormData(this.value || {})
    }
  },
  computed: {
    form() {
      return this.$refs.form
    }
  },
  watch: {
    value: {
      handler(newVal) {
        this.formData = this.initializeFormData(newVal || {})
      },
      deep: true
    }
  },
  methods: {
    // 初始化表单数据
    initializeFormData(data) {
      // 创建一个新的对象，避免直接修改传入的数据
      const initialized = {}

      // 复制传入数据的所有属性
      Object.keys(data || {}).forEach(key => {
        initialized[key] = data[key]
      })

      // 确保所有日期范围字段都是有效的数组
      if (this.config.fields) {
        this.config.fields.forEach(field => {
          // if (field.type === 'date-range') {
          //   if (!initialized[field.prop] || !Array.isArray(initialized[field.prop])) {
          //     // 使用Vue.set确保响应性
          //     this.$set(initialized, field.prop, [])
          //   }
          // }
        })
      }

      return initialized
    },

    // 处理日期范围变更
    handleDateRangeChange(prop, val) {
      console.log('handleDateRangeChange', prop, val)
      // 当用户清空选择器时，将null值转换为空数组
      this.setFieldValue(prop, val || [])

      // 如果有自定义的变更处理函数，则调用它
      const field = this.config.fields?.find(f => f.prop === prop)
      if (field?.onChange) {
        field.onChange(val, this.formData)
      }
    },

    toggleExpand() {
      this.isExpanded = !this.isExpanded
    },

    setFieldValue(prop, value) {
      console.log('setFieldValue', prop, value)
      this.$set(this.formData, prop, value)
      // 立即触发input事件，通知父组件数据变化
      this.$emit('input', this.formData)
    },

    getComponentByType(type) {
      const componentMap = {
        input: 'el-input',
        select: 'el-select',
        'date-range': 'el-date-picker'
      }
      return componentMap[type] || 'el-input'
    },

    getFieldProps(field) {
      const defaultProps = {
        clearable: field.clearable !== false,
        placeholder: field.placeholder
      }

      if (field.type === 'date-range') {
        return {
          ...defaultProps,
          ...field.props,
          rangeSeparator: field.rangeSeparator || '至',
          startPlaceholder: field.startPlaceholder || '开始日期',
          endPlaceholder: field.endPlaceholder || '结束日期',
          valueFormat: field.valueFormat || 'yyyy-MM-dd',
          format: field.format || 'yyyy-MM-dd'
        }
      }

      return {...defaultProps, ...field.props}
    },

    resetForm() {
      // 重置表单时也调用初始化函数
      this.formData = this.initializeFormData({})
      this.$emit('reset')
      if (this.form) {
        this.form.clearValidate()
      }
    },

    submitForm() {
      this.$emit('submit', this.formData)
    },

    validate(callback) {
      if (this.form) {
        return this.form.validate(callback)
      }
    },

    validateField(props, callback) {
      if (this.form) {
        return this.form.validateField(props, callback)
      }
    },

    clearValidate(props) {
      if (this.form) {
        return this.form.clearValidate(props)
      }
    }
  }
}
</script>

<style scoped>
.dynamic-form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.form-actions {
  text-align: center;
  padding-top: 20px;
  box-sizing: border-box;
}

.date-range-picker {
  width: 100%;
}
</style>
