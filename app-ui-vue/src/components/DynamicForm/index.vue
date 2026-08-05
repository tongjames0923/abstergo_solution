<template>
  <el-card class="dynamic-form-card" shadow="never">
    <!-- 头部区域 -->
    <div slot="header" class="clearfix card-header">
      <slot name="header">
        <span class="card-title">{{ finalTitle }}</span>
        <el-button
          v-if="config && config.expandable !== false"
          class="expand-button"
          :class="{ 'is-expanded': isExpanded }"
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
      :label-width="finalLabelWidth"
      :model="formData"
      class="dynamic-form"
      v-bind="formProps"
      v-on="formListeners"
    >
      <!-- 表单前内容 -->
      <slot name="before-form" />

      <!-- 默认布局：根据配置自动生成 -->
      <template v-if="!$slots.default && config && config.fields && config.fields.length > 0">
        <el-row
          :gutter="finalGutter"
          class="dynamic-form-row"
        >
          <el-col
            v-for="field in config.fields"
            :key="field.prop || field.label || field.type"
            :span="getFieldSpan(field)"
            :xs="getFieldResponsiveSpan(field, 'xs')"
            :sm="getFieldResponsiveSpan(field, 'sm')"
            :md="getFieldResponsiveSpan(field, 'md')"
            :lg="getFieldResponsiveSpan(field, 'lg')"
            :xl="getFieldResponsiveSpan(field, 'xl')"
            class="dynamic-form-col"
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
                class="dynamic-form-item"
                v-bind="getFieldItemProps(field)"
              >
                <!-- 输入框 -->
                <el-input
                  v-if="field && field.type === 'input'"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  class="dynamic-form-component"
                  @input="val => setFieldValue((field && field.prop) || '', val)"
                />

                <!-- 选择器 -->
                <el-select
                  v-else-if="field && field.type === 'select'"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  class="dynamic-form-component"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                >
                  <el-option
                    v-for="option in (field.props && field.props.options || [])"
                    :key="(option && option.value) || ''"
                    :label="(option && option.label) || ''"
                    :value="(option && option.value)"
                  />
                </el-select>

                <!-- 日期范围 -->
                <el-date-picker
                  v-else-if="field && field.type === 'date-range'"
                  :value="formData[(field && field.prop) || '']"
                  type="daterange"
                  v-bind="field.props"
                  class="dynamic-form-component date-range-picker"
                  :popper-class="`date-range-picker-popper ${field.prop || ''}`"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  @change="val => handleDateRangeChange((field && field.prop) || '', val)"
                  @input="val => handleDateRangeChange((field && field.prop) || '', val)"
                />

                <!-- 动态选项 -->
                <DynamicOptions
                  v-else-if="field && field.type === 'dynamic-options'"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  class="dynamic-form-component"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                />
                <url-image
                  v-else-if="field && field.type === 'url-image'"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  class="dynamic-form-component url-image-component"
                  @change="val => setFieldValue((field && field.prop) || '', val)"
                />
                <!-- 其他自定义组件 -->
                <component
                  :is="customComponents[(field && field.type) || '']"
                  v-else-if="field && field.type && customComponents[(field && field.type) || '']"
                  :value="formData[(field && field.prop) || '']"
                  v-bind="field.props"
                  class="dynamic-form-component"
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
      isExpanded: this.getInitialExpandedState(),
      formData: this.initializeFormData(this.value || {})
    }
  },
  computed: {
    form() {
      return this.$refs.form
    },

    // 计算最终标题，优先使用config.title，其次是props.title，最后是props中的title
    finalTitle() {
      if (this.title) {
        return this.title
      }

      if (this.config && this.config.title) {
        return this.config.title
      }

      if (this.config && this.config.props && this.config.props.title) {
        return this.config.props.title
      }

      return ''
    },

    // 计算最终标签宽度
    finalLabelWidth() {
      // 优先使用props中指定的labelWidth
      if (this.config && this.config.props && this.config.props.labelWidth) {
        return this.config.props.labelWidth
      }

      // 其次使用config.labelWidth
      if (this.config && this.config.labelWidth) {
        return this.config.labelWidth
      }

      // 默认值
      return '100px'
    },

    // 计算最终gutter
    finalGutter() {
      // 优先使用props中指定的gutter
      if (this.config && this.config.props && this.config.props.gutter !== undefined) {
        return this.config.props.gutter
      }

      // 其次使用config.gutter
      if (this.config && this.config.gutter !== undefined) {
        return this.config.gutter
      }

      // 默认值
      return 20
    },

    // 计算默认span
    defaultSpan() {
      // 优先使用props中指定的span
      if (this.config && this.config.props && this.config.props.span !== undefined) {
        return this.config.props.span
      }

      // 其次使用config.span
      if (this.config && this.config.span !== undefined) {
        return this.config.span
      }

      // 默认值
      return 8
    },

    // 表单事件监听器
    formListeners() {
      const listeners = {}
      // 将所有非特殊事件传递给formProps
      Object.keys(this.$listeners).forEach(event => {
        if (!['submit', 'reset'].includes(event)) {
          listeners[event] = this.$listeners[event]
        }
      })
      return listeners
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
        this.isExpanded = this.getInitialExpandedState()
        this.formData = this.initializeFormData(this.formData)
      },
      deep: true
    }
  },
  methods: {
    // 获取初始展开状态
    getInitialExpandedState() {
      // 优先使用config.expanded
      if (this.config && this.config.expanded !== undefined) {
        return this.config.expanded !== false
      }

      // 其次使用config.props.expanded
      if (this.config && this.config.props && this.config.props.expanded !== undefined) {
        return this.config.props.expanded !== false
      }

      // 默认展开
      return true
    },

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
    },

    // 获取字段栅格跨度
    getFieldSpan(field) {
      // url-image 类型字段独占一行
      if (field && field.type === 'url-image') {
        return 24
      }

      // 优先使用字段自身配置的span
      if (field && field.span !== undefined) {
        return field.span
      }

      // 其次使用全局配置的默认span
      return this.defaultSpan
    },

    // 获取字段响应式栅格跨度
    getFieldResponsiveSpan(field, size) {
      // url-image 类型字段在所有屏幕尺寸下都独占一行
      if (field && field.type === 'url-image') {
        return 24
      }

      // 如果字段配置了特定尺寸的span，则使用该配置
      if (field && field.responsive && field.responsive[size]) {
        return field.responsive[size]
      }

      // 否则使用全局配置
      if (this.config && this.config.responsive && this.config.responsive[size]) {
        return this.config.responsive[size]
      }

      // 根据屏幕尺寸设置默认的响应式行为
      switch (size) {
        case 'xs':
          return 24 // 在超小屏幕上占满整行
        case 'sm':
          return 12 // 在小屏幕上一行显示2个
        case 'md':
          return 8 // 在中等屏幕上一行显示3个
        case 'lg':
          return 8 // 在大屏幕上一行显示3个
        case 'xl':
          return 6 // 在超大屏幕上一行显示4个
        default:
          return undefined
      }
    },

    // 获取表单项属性
    getFieldItemProps(field) {
      // 如果字段有props属性，则返回它
      if (field && field.itemProps) {
        return field.itemProps
      }

      // 默认返回空对象
      return {}
    }
  }
}
</script>

<style scoped>
.dynamic-form-card {
  margin-bottom: var(--dynamic-form-margin);
  background: var(--dynamic-form-bg);
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.dynamic-form-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.03);
  transform: translateY(-1px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 252, 0.9) 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(10px);
}

.card-title {
  font-weight: 600;
  font-size: 14px;
  color: #1a1a2e;
  letter-spacing: -0.01em;
  position: relative;
  padding-left: 10px;
}

.card-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 2px;
}

.expand-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(59, 130, 246, 0.2);
  padding: 4px 10px;
  border-radius: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(59, 130, 246, 0.05);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 1px 2px rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  font-weight: 500;
  font-size: 12px;
  cursor: pointer;
}

.expand-button:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.15);
  transform: translateY(-1px);
}

.expand-button:active {
  transform: translateY(0);
}

.expand-button.is-expanded {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.25);
}

.expand-button i {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.expand-button.is-expanded i {
  transform: rotate(180deg);
}

.form-actions {
  text-align: center;
  padding: 12px 0 6px;
  margin-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  background: linear-gradient(180deg, transparent 0%, rgba(248, 250, 252, 0.5) 100%);
}

.form-actions .el-button {
  min-width: 80px;
  padding: 6px 12px;
  font-weight: 500;
  font-size: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-actions .el-button--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.form-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.form-actions .el-button--default {
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.8);
}

.form-actions .el-button--default:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px);
}

.dynamic-form-row {
  margin-left: -8px;
  margin-right: -8px;
  padding: 8px 0;
}

.dynamic-form-col {
  padding-left: 8px;
  padding-right: 8px;
  margin-bottom: 10px;
}

.dynamic-form-item {
  margin-bottom: 0;
  transition: all 0.3s ease;
}

.dynamic-form-item:hover {
  transform: translateX(2px);
}

.dynamic-form-item ::v-deep .el-form-item__label {
  font-weight: 500;
  font-size: 13px;
  color: #374151;
  padding-right: 8px;
}

.dynamic-form-item ::v-deep .el-form-item__content {
  display: flex;
  align-items: center;
}

.dynamic-form-component {
  width: 100%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px !important;
}

.dynamic-form-component:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.dynamic-form-component:focus-within {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 为url-image组件设置特殊样式 */
.dynamic-form-component.url-image-component {
  width: 100%;
}

.date-range-picker {
  width: 100%;
}

/* 日期范围选择器弹出框优化 */
::v-deep .date-range-picker-popper {
  max-width: none !important;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.06);
}

::v-deep .date-range-picker-popper .el-picker-panel__body-wrapper {
  display: flex;
  flex-wrap: wrap;
  padding: 8px;
}

::v-deep .date-range-picker-popper .el-picker-panel__body {
  min-width: auto;
  margin: 0;
  padding: 8px;
}

::v-deep .date-range-picker-popper .el-date-range-picker__content {
  flex: 1;
  min-width: 220px;
  border-radius: 8px;
  overflow: hidden;
}

::v-deep .date-range-picker-popper .el-date-range-picker__content.is-left {
  border-right: 1px solid #e5e7eb;
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  background: rgba(248, 250, 252, 0.5);
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header > span {
  flex: 1;
  text-align: center;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header > span:hover {
  background: rgba(59, 130, 246, 0.05);
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header > span:nth-child(2) {
  text-align: right;
}

/* 小屏幕适配 */
@media (max-width: 768px) {
  .card-header {
    padding: 8px 12px;
  }

  .card-title {
    font-size: 13px;
  }

  .form-actions {
    padding: 8px 0 4px;
  }

  .form-actions .el-button {
    min-width: 70px;
    padding: 6px 12px;
    font-size: 12px;
  }

  .dynamic-form-row {
    padding: 4px 0;
  }

  .dynamic-form-col {
    margin-bottom: 8px;
  }

  ::v-deep .date-range-picker-popper .el-picker-panel {
    width: auto;
    min-width: 280px;
  }

  ::v-deep .date-range-picker-popper .el-date-range-picker__content {
    min-width: 220px;
  }

  ::v-deep .date-range-picker-popper .el-date-range-picker__content.is-left {
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }

  ::v-deep .date-range-picker-popper .el-date-range-picker__content.is-right {
    margin-top: 10px;
  }

  ::v-deep .date-range-picker-popper .el-picker-panel__body-wrapper {
    flex-direction: column;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dynamic-form-card {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 表单项出现动画 */
.dynamic-form-col {
  animation: fadeIn 0.3s ease forwards;
  opacity: 0;
}

.dynamic-form-col:nth-child(1) { animation-delay: 0.05s; }
.dynamic-form-col:nth-child(2) { animation-delay: 0.1s; }
.dynamic-form-col:nth-child(3) { animation-delay: 0.15s; }
.dynamic-form-col:nth-child(4) { animation-delay: 0.2s; }
.dynamic-form-col:nth-child(5) { animation-delay: 0.25s; }
.dynamic-form-col:nth-child(6) { animation-delay: 0.3s; }
.dynamic-form-col:nth-child(7) { animation-delay: 0.35s; }
.dynamic-form-col:nth-child(8) { animation-delay: 0.4s; }
</style>
