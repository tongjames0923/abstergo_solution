<template>
  <el-card class="dynamic-form-card" shadow="never">
    <!-- 头部控制条：整体可点击折叠/展开，右侧为图标化开关 -->
    <div
      v-if="showHeader"
      slot="header"
      class="clearfix card-header"
      :class="{ 'is-clickable': expandable }"
      @click="expandable && toggleExpand()"
    >
      <slot name="header">
        <div class="header-left">
          <span class="card-title">{{ finalTitle }}</span>
          <transition name="df-fade">
            <span v-if="!isExpanded && filledCount > 0" class="filled-summary">
              {{ $t('common.filledConditions', { count: filledCount }) }}
            </span>
          </transition>
        </div>
        <button
          v-if="expandable"
          class="expand-button"
          :class="{ 'is-expanded': isExpanded }"
          type="button"
          :aria-expanded="String(isExpanded)"
          :title="isExpanded ? $t('common.collapse') : $t('common.expand')"
          @click.stop="toggleExpand"
        >
          <i class="el-icon-arrow-down" />
        </button>
      </slot>
    </div>

    <!-- 表单主体（带高度过渡的折叠容器） -->
    <transition
      name="df-collapse"
      @before-enter="beforeCollapseEnter"
      @enter="onCollapseEnter"
      @after-enter="afterCollapseEnter"
      @before-leave="beforeCollapseLeave"
      @leave="onCollapseLeave"
      @after-leave="afterCollapseLeave"
    >
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
            <el-button icon="el-icon-refresh" @click="resetForm">{{ $t('common.reset') }}</el-button>
            <el-button icon="el-icon-search" type="primary" @click="submitForm">{{ $t('common.query') }}</el-button>
          </slot>
        </div>
      </el-form>
    </transition>
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
      return 16
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

    // 是否可折叠（默认可以，config.expandable === false 时关闭）
    expandable() {
      return !(this.config && this.config.expandable === false)
    },

    // 头部仅在存在标题、自定义头部插槽或可折叠时显示
    showHeader() {
      return Boolean(this.finalTitle) || Boolean(this.$slots.header) || this.expandable
    },

    // 折叠状态下已填写的条件数量摘要
    filledCount() {
      if (!this.formData || typeof this.formData !== 'object') return 0
      return Object.keys(this.formData).filter(key => {
        const value = this.formData[key]
        if (value === undefined || value === null || value === '') return false
        if (Array.isArray(value)) return value.length > 0
        return true
      }).length
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
      this.$emit('expand-change', this.isExpanded)
    },

    /* 折叠高度过渡钩子 */
    beforeCollapseEnter(el) {
      el.style.height = '0'
      el.style.overflow = 'hidden'
    },
    onCollapseEnter(el) {
      el.style.height = el.scrollHeight + 'px'
    },
    afterCollapseEnter(el) {
      el.style.height = ''
      el.style.overflow = ''
    },
    beforeCollapseLeave(el) {
      el.style.height = el.scrollHeight + 'px'
      el.style.overflow = 'hidden'
    },
    onCollapseLeave(el) {
      el.style.height = '0'
    },
    afterCollapseLeave(el) {
      el.style.height = ''
      el.style.overflow = ''
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

/* 容器内边距由控制条与主体自行控制，避免双重留白 */
.dynamic-form-card ::v-deep .el-card__header {
  padding: 0;
}

.dynamic-form-card ::v-deep .el-card__body {
  padding: 12px 16px;
}

.dynamic-form-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.03);
  transform: translateY(-1px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 7px 14px;
  background: transparent;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  user-select: none;
  transition: background 0.2s ease;
}

.card-header.is-clickable {
  cursor: pointer;
}

.card-header.is-clickable:hover {
  background: rgba(59, 130, 246, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.card-title {
  font-weight: 600;
  font-size: 13px;
  color: #1a1a2e;
  letter-spacing: -0.01em;
  position: relative;
  padding-left: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 13px;
  background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 2px;
}

/* 折叠状态下的已填条件摘要 */
.filled-summary {
  flex: 0 0 auto;
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
  color: #1d4ed8;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.16);
  white-space: nowrap;
}

.df-fade-enter-active,
.df-fade-leave-active {
  transition: opacity 0.2s ease;
}

.df-fade-enter,
.df-fade-leave-to {
  opacity: 0;
}

/* 图标化折叠开关 */
.expand-button {
  flex: 0 0 auto;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  outline: none;
  transition: background 0.2s ease, color 0.2s ease;
}

.expand-button:hover {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.expand-button:active {
  background: rgba(59, 130, 246, 0.16);
}

.expand-button i {
  font-size: 12px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 展开状态箭头朝上 */
.expand-button.is-expanded i {
  transform: rotate(180deg);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  padding: 8px 0 2px;
  margin-top: 2px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.form-actions .el-button {
  min-width: 68px;
  margin-left: 0;
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
  padding: 6px 0;
}

.dynamic-form-col {
  padding-left: 8px;
  padding-right: 8px;
  margin-bottom: 8px;
}

.dynamic-form-item {
  margin-bottom: 0;
}

.dynamic-form-item ::v-deep .el-form-item__label {
  font-weight: 500;
  font-size: 12px;
  color: #374151;
  padding-right: 8px;
  line-height: 32px;
}

.dynamic-form-item ::v-deep .el-form-item__content {
  display: flex;
  align-items: center;
}

.dynamic-form-component {
  width: 100%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 6px !important;
}

/* 统一收紧表单控件高度，提升信息密度 */
.dynamic-form-item ::v-deep .el-input__inner {
  height: 32px;
  line-height: 32px;
}

.dynamic-form-item ::v-deep .el-input__icon {
  line-height: 32px;
}

.dynamic-form-item ::v-deep .el-input--small .el-input__suffix,
.dynamic-form-item ::v-deep .el-input__prefix {
  display: flex;
  align-items: center;
}

.dynamic-form-item ::v-deep .el-range-editor--medium.el-input__inner {
  height: 32px;
  padding: 0 8px;
}

.dynamic-form-item ::v-deep .el-range-editor .el-range-input {
  font-size: 13px;
}

.dynamic-form-item ::v-deep .el-textarea__inner {
  font-size: 13px;
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
    padding: 6px 12px;
  }

  .card-title {
    font-size: 13px;
  }

  .form-actions {
    padding: 8px 0 2px;
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

  /* 小屏下 label 置顶堆叠（覆盖内联 label-width，避免横排标签挤压输入区） */
  ::v-deep .el-form-item__label {
    width: auto !important;
    float: none;
    display: block;
    text-align: left;
    padding: 0 0 4px;
    line-height: 1.5;
  }

  ::v-deep .el-form-item__content {
    margin-left: 0 !important;
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
  animation: fadeIn 0.25s ease forwards;
  opacity: 0;
}

.dynamic-form-col:nth-child(1) { animation-delay: 0.02s; }
.dynamic-form-col:nth-child(2) { animation-delay: 0.04s; }
.dynamic-form-col:nth-child(3) { animation-delay: 0.06s; }
.dynamic-form-col:nth-child(4) { animation-delay: 0.08s; }
.dynamic-form-col:nth-child(5) { animation-delay: 0.1s; }
.dynamic-form-col:nth-child(6) { animation-delay: 0.12s; }
.dynamic-form-col:nth-child(7) { animation-delay: 0.14s; }
.dynamic-form-col:nth-child(8) { animation-delay: 0.16s; }
</style>
