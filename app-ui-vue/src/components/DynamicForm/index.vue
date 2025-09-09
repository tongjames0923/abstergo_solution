<template>
  <el-card class="dynamic-form-card" shadow="never">
    <!-- 头部区域 -->
    <div slot="header" class="clearfix card-header">
      <slot name="header">
        <span class="card-title">{{ title }}</span>
        <el-button
          v-if="config && config.expandable !== false"
          style="float: right; padding: 3px 0"
          type="text"
          @click="toggleExpand"
        >
          {{ isExpanded ? '收起' : '展开' }}
          <i :class="isExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
        </el-button>
      </slot>
    </div>

    <!-- 表单主体 -->
    <el-form
      v-show="isExpanded"
      ref="form"
      :label-width="(config && config.labelWidth) || '100px'"
      :model="formData"
      class="dynamic-form"
      v-bind="formProps"
    >
      <!-- 表单前内容 -->
      <slot name="before-form" />

      <!-- 默认布局：根据配置自动生成 -->
      <template v-if="!$slots.default && config && config.fields && config.fields.length > 0">
        <el-row :gutter="config.gutter || 20">
          <el-col
            v-for="field in config.fields"
            :key="field.prop || field.label || field.type"
            :span="(field && field.span) || (config && config.span) || 8"
          >
            <slot
              :field="field"
              :name="`form-item-${field && field.prop}`"
              :set-value="val => setFieldValue((field && field.prop) || '', val)"
              :value="formData[(field && field.prop) || '']"
            >
              <el-form-item
                :label="(field && field.label) || ''"
                :prop="(field && field.prop) || ''"
              >
                <!-- 输入框 -->
                <el-input
                  v-if="field && field.type === 'input'"
                  :clearable="field.clearable !== false"
                  :placeholder="(field && field.placeholder) || ''"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  @input="val => setFieldValue((field && field.prop) || '', val)"
                >
                  <template v-if="field && field.prefixIcon" #prefix>
                    <i :class="field.prefixIcon" />
                  </template>
                </el-input>

                <!-- 选择器 -->
                <el-select
                  v-else-if="field && field.type === 'select'"
                  :clearable="field.clearable !== false"
                  :placeholder="(field && field.placeholder) || ''"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                >
                  <el-option
                    v-for="option in ((field && field.options) || [])"
                    :key="(option && option.value) || ''"
                    :label="(option && option.label) || ''"
                    :value="(option && option.value)"
                  />
                </el-select>

                <!-- 日期范围 -->
                <el-date-picker
                  v-else-if="field && field.type === 'date-range'"
                  :end-placeholder="(field && field.endPlaceholder) || '结束日期'"
                  :format="(field && field.format) || 'yyyy-MM-dd'"
                  :range-separator="(field && field.rangeSeparator) || '至'"
                  :start-placeholder="(field && field.startPlaceholder) || '开始日期'"
                  :value="formData[(field && field.prop) || '']"
                  :value-format="(field && field.valueFormat) || 'yyyy-MM-dd'"
                  class="date-range-picker"
                  type="daterange"
                  v-bind="field.props"
                  @change="val => handleDateRangeChange((field && field.prop) || '', val)"
                  @input="val => handleDateRangeChange((field && field.prop) || '', val)"
                />

                <!-- 动态选项 -->
                <DynamicOptions
                  v-else-if="field && field.type === 'dynamic-options'"
                  :api-url="(field && field.apiUrl) || ''"
                  :placeholder="(field && field.placeholder) || ''"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                />
                <url-image
                  v-else-if="field && field.type === 'url-image'"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                />
                <!-- 其他自定义组件 -->
                <component
                  :is="customComponents[(field && field.type) || '']"
                  v-else-if="field && field.type && customComponents[(field && field.type) || '']"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                  @input="val => setFieldValue((field && field.prop) || '', val)"
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
      <slot name="after-form" />

      <!-- 操作按钮 -->
      <div class="form-actions">
        <slot
          :form="form"
          :formData="formData"
          :reset="resetForm"
          :submit="submitForm"
          name="actions"
        >
          <el-button icon="el-icon-search" type="primary" @click="submitForm">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetForm">重置</el-button>
        </slot>
      </div>
    </el-form>
  </el-card>
</template>

<script>
import DynamicOptions from '@/components/DynamicOptions'
import UrlImage from '@/components/UrlImage/index.vue'

export default {
  name: 'DynamicForm',
  components: {
    DynamicOptions,
    UrlImage
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
      isExpanded: (this.config && this.config.expanded) !== false,
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
    },
    config: {
      handler(newVal) {
        // 当配置发生变化时，确保表单数据仍然有效
        this.isExpanded = (newVal && newVal.expanded) !== false
        this.formData = this.initializeFormData(this.formData)
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
      if (data && typeof data === 'object') {
        Object.keys(data).forEach(key => {
          initialized[key] = data[key]
        })
      }

      // 确保所有字段都有默认值
      if (this.config && this.config.fields && Array.isArray(this.config.fields)) {
        this.config.fields.forEach(field => {
          if (field && field.prop && !(field.prop in initialized)) {
            // 为不同类型的字段设置合适的默认值
            if (field.type === 'date-range') {
              initialized[field.prop] = []
            } else {
              initialized[field.prop] = undefined
            }
          }
        })
      }

      return initialized
    },

    // 处理日期范围变更
    handleDateRangeChange(prop, val) {
      // 当用户清空选择器时，将null值转换为空数组
      this.setFieldValue(prop, val || [])

      // 如果有自定义的变更处理函数，则调用它
      if (this.config && this.config.fields && Array.isArray(this.config.fields)) {
        const field = this.config.fields.find(f => f && f.prop === prop)
        if (field && field.onChange) {
          field.onChange(val, this.formData)
        }
      }
    },

    toggleExpand() {
      this.isExpanded = !this.isExpanded
    },

    setFieldValue(prop, value) {
      this.$set(this.formData, prop, value)
      // 立即触发input事件，通知父组件数据变化
      this.$emit('input', this.formData)
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
    },

    // 获取完整的表单数据
    getFormData() {
      return this.formData
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
