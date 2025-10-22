<template>
  <el-card class="dynamic-form-card" shadow="never">
    <!-- 头部区域 -->
    <div slot="header" class="clearfix card-header">
      <slot name="header">
        <span class="card-title">{{ title }}</span>
        <el-button
          v-if="config && config.expandable !== false"
          class="expand-button"
          :class="{ 'is-expanded': isExpanded }"
          @click="toggleExpand"
        >
          {{ isExpanded ? '收起' : '展开' }}
          <i :class="isExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"/>
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
      <slot name="before-form"/>

      <!-- 默认布局：根据配置自动生成 -->
      <template v-if="!$slots.default && config && config.fields && config.fields.length > 0">
        <el-row
          :gutter="config.gutter || 20"
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
                  class="dynamic-form-component"
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
      <slot name="after-form"/>

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
    },

    // 获取字段栅格跨度
    getFieldSpan(field) {
      // 优先使用字段自身配置的span
      if (field && field.span !== undefined) {
        return field.span;
      }

      // 其次使用全局配置的span
      if (this.config && this.config.span !== undefined) {
        return this.config.span;
      }

      // 默认返回8
      return 8;
    },

    // 获取字段响应式栅格跨度
    getFieldResponsiveSpan(field, size) {
      // 如果字段配置了特定尺寸的span，则使用该配置
      if (field && field.responsive && field.responsive[size]) {
        return field.responsive[size];
      }

      // 否则使用全局配置
      if (this.config && this.config.responsive && this.config.responsive[size]) {
        return this.config.responsive[size];
      }

      // 根据屏幕尺寸设置默认的响应式行为
      switch (size) {
        case 'xs':
          return 24; // 在超小屏幕上占满整行
        case 'sm':
          return 12; // 在小屏幕上一行显示2个
        case 'md':
          return 8;  // 在中等屏幕上一行显示3个
        case 'lg':
          return 8;  // 在大屏幕上一行显示3个
        case 'xl':
          return 6;  // 在超大屏幕上一行显示4个
        default:
          return undefined;
      }
    }
  }
}
</script>

<style scoped>

.dynamic-form-card {
  margin-bottom: var(--dynamic-form-margin);
  background: var(--dynamic-form-bg);
}

.card-header {
  display: var(--card-header-display);
  justify-content: var(--card-header-justify);
  align-items: var(--card-header-align);
  padding: var(--card-header-padding);
}

.card-title {
  font-weight: var(--card-title-weight);
  font-size: var(--card-title-size);
}

.expand-button {
  border: var(--expand-button-border);
  padding: var(--expand-button-padding);
  border-radius: var(--expand-button-radius);
  transition: var(--expand-button-transition);
  background: var(--expand-button-bg);
  backdrop-filter: var(--expand-button-backdrop);
  -webkit-backdrop-filter: var(--expand-button-backdrop);
  box-shadow: var(--expand-button-box-shadow);
  color: var(--expand-button-color);
  font-weight: var(--expand-button-font-weight);
}

.expand-button:hover {
  background: var(--expand-button-hover-bg);
  border-color: var(--expand-button-hover-border);
  box-shadow: var(--expand-button-hover-box-shadow);
}

.expand-button.is-expanded {
  background: var(--expand-button-expanded-bg);
  border-color: var(--expand-button-expanded-border);
}

.form-actions {
  text-align: var(--form-actions-align);
  padding-top: var(--form-actions-padding);
  box-sizing: var(--form-actions-box);
}

.dynamic-form-row {
  margin-left: var(--dynamic-form-row-margin-x);
  margin-right: var(--dynamic-form-row-margin-x);
}

.dynamic-form-col {
  padding-left: var(--dynamic-form-col-padding-x);
  padding-right: var(--dynamic-form-col-padding-x);
}

.dynamic-form-item ::v-deep .el-form-item__content {
  display: var(--dynamic-form-item-content-display);
}

.dynamic-form-component {
  width: var(--dynamic-form-component-width);
}

.date-range-picker {
  width: var(--date-range-picker-width);
}

/* 日期范围选择器弹出框优化 */
::v-deep .date-range-picker-popper {
  max-width: var(--date-range-picker-popper-max-width);
}

::v-deep .date-range-picker-popper .el-picker-panel__body-wrapper {
  display: flex;
  flex-wrap: wrap;
}

::v-deep .date-range-picker-popper .el-picker-panel__body {
  min-width: auto;
  margin: 0;
}

::v-deep .date-range-picker-popper .el-date-range-picker__content {
  flex: 1;
  min-width: var(--date-range-picker-content-min-width);
}

::v-deep .date-range-picker-popper .el-date-range-picker__content.is-left {
  border-right: var(--date-range-picker-popper-border);
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header {
  display: flex;
  justify-content: space-between;
  padding: var(--date-range-picker-popper-header-padding);
  border-bottom: var(--date-range-picker-popper-header-border);
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header > span {
  flex: 1;
  text-align: center;
}

::v-deep .date-range-picker-popper .el-date-range-picker__time-header > span:nth-child(2) {
  text-align: right;
}

/* 小屏幕适配 */
@media (max-width: 768px) {
  ::v-deep .date-range-picker-popper .el-picker-panel {
    width: var(--date-range-picker-popper-mobile-width);
    min-width: var(--date-range-picker-popper-mobile-min-width);
  }

  ::v-deep .date-range-picker-popper .el-date-range-picker__content {
    min-width: var(--date-range-picker-content-mobile-min-width);
  }

  ::v-deep .date-range-picker-popper .el-date-range-picker__content.is-left {
    border-right: none;
    border-bottom: var(--date-range-picker-popper-border);
  }

  ::v-deep .date-range-picker-popper .el-date-range-picker__content.is-right {
    margin-top: 10px;
  }

  ::v-deep .date-range-picker-popper .el-picker-panel__body-wrapper {
    flex-direction: column;
  }
}
</style>
