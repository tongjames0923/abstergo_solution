<template>
  <div class="dynamic-form-with-api">
    <DynamicForm
      v-if="localConfig"
      ref="dynamicForm"
      v-model="formData"
      :config="localConfig"
      :title="title"
      :form-props="formProps"
      :custom-components="customComponents"
      @reset="handleReset"
      @submit="handleSubmit"
    >
      <template v-for="(_, slotName) in $scopedSlots" #[slotName]="slotData">
        <slot :name="slotName" v-bind="slotData" />
      </template>
    </DynamicForm>

    <!-- 加载状态 -->
    <el-card v-else-if="loading" shadow="never" class="loading-card">
      <div class="loading-placeholder">
        <div class="loading-spinner">
          <div class="spinner" />
        </div>
        <div class="loading-content">
          <span class="loading-title">正在加载表单配置</span>
          <span class="loading-subtitle">请稍候...</span>
        </div>
      </div>
    </el-card>

    <!-- 错误状态 -->
    <el-card v-else-if="error" shadow="never" class="error-card">
      <div class="error-placeholder">
        <div class="error-icon">
          <i class="el-icon-warning" />
        </div>
        <div class="error-content">
          <span class="error-title">加载失败</span>
          <span class="error-message">{{ error }}</span>
          <el-button
            type="primary"
            size="small"
            class="retry-button"
            @click="loadConfig"
          >
            <i class="el-icon-refresh" />
            重新加载
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 空配置状态 -->
    <el-card v-else shadow="never" class="empty-card">
      <div class="empty-placeholder">
        <div class="empty-icon">
          <i class="el-icon-document" />
        </div>
        <div class="empty-content">
          <span class="empty-title">暂无表单配置</span>
          <span class="empty-subtitle">未找到对应的表单配置信息</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import DynamicForm from './index.vue'
import request from '@/utils/request'

export default {
  name: 'DynamicFormWithApi',
  components: {
    DynamicForm
  },
  props: {
    // 表单标题
    title: {
      type: String,
      default: ''
    },
    // 配置名称（用于获取配置）
    configName: {
      type: String,
      default: ''
    },
    // 直接传入的配置（优先级最高）
    config: {
      type: Object,
      default: null
    },
    // 获取配置的方法（优先级高于API和Store）
    loadConfigMethod: {
      type: Function,
      default: null
    },
    // API地址（用于获取配置）
    apiUrl: {
      type: String,
      default: '/private/view/getSearchForm'
    },
    // 是否使用store缓存（默认使用）
    useStore: {
      type: Boolean,
      default: true
    },
    // 表单初始值
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
      localConfig: null,
      formData: this.value,
      loading: false,
      error: null
    }
  },
  watch: {
    value: {
      handler(newVal) {
        this.formData = newVal
      },
      deep: true
    },
    config: {
      handler(newVal) {
        if (newVal) {
          this.localConfig = newVal
        }
      },
      immediate: true
    }
  },
  async created() {
    // 如果直接传入了config，则使用传入的配置
    if (this.config) {
      this.localConfig = this.config
      return
    }

    // 如果提供了configName，则获取配置
    if (this.configName) {
      await this.loadConfig()
    }
  },
  methods: {
    // 获取配置
    async loadConfig() {
      if (!this.configName) {
        this.error = '未提供配置名称'
        return
      }

      this.loading = true
      this.error = null

      try {
        // 优先使用传入的加载方法
        if (this.loadConfigMethod && typeof this.loadConfigMethod === 'function') {
          this.localConfig = await this.loadConfigMethod(this.configName)
          return
        }

        // 如果启用store且有store，则使用store的fetchFormConfig方法
        if (this.useStore && this.$store) {
          this.localConfig = await this.$store.dispatch('formConfigs/fetchFormConfig', this.configName)
          return
        }

        // 否则通过API获取配置
        const response = await request({
          url: this.apiUrl,
          method: 'get',
          params: { key: this.configName }
        })

        this.localConfig = response.data
      } catch (err) {
        this.error = err.message || '加载配置失败'
        console.error('加载表单配置失败:', err)
      } finally {
        this.loading = false
      }
    },

    // 重置表单
    handleReset() {
      this.formData = {}
      this.$emit('reset')
    },

    // 提交表单
    handleSubmit(data) {
      this.$emit('submit', data)
    },

    // 验证表单
    validate(callback) {
      return this.$refs.dynamicForm?.validate(callback)
    },

    // 清除验证
    clearValidate(props) {
      return this.$refs.dynamicForm?.clearValidate(props)
    },

    // 验证字段
    validateField(props, callback) {
      return this.$refs.dynamicForm?.validateField(props, callback)
    },

    // 重置表单
    resetForm() {
      this.$refs.dynamicForm?.resetForm()
      this.formData = this.initializeFormData()
    },

    // 初始化表单数据
    initializeFormData() {
      const data = {}
      if (this.localConfig && this.localConfig.fields && Array.isArray(this.localConfig.fields)) {
        this.localConfig.fields.forEach(field => {
          if (field && field.prop) {
            if (field.type === 'date-range') {
              data[field.prop] = []
            } else {
              data[field.prop] = undefined
            }
          }
        })
      }
      return data
    }
  }
}
</script>

<style scoped>
.dynamic-form-with-api {
  width: 100%;
}

/* 加载状态样式 */
.loading-card {
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.loading-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 36px 20px;
  min-height: 120px;
}

.loading-spinner {
  margin-bottom: 14px;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(59, 130, 246, 0.1);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.loading-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.loading-subtitle {
  font-size: 12px;
  color: #6b7280;
}

/* 错误状态样式 */
.error-card {
  border-radius: 12px;
  border: 1px solid rgba(239, 68, 68, 0.15);
  box-shadow: 0 4px 24px rgba(239, 68, 68, 0.08);
  background: linear-gradient(135deg, rgba(254, 242, 242, 0.5) 0%, rgba(255, 255, 255, 1) 100%);
}

.error-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 36px 20px;
  min-height: 120px;
}

.error-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 50%;
  margin-bottom: 14px;
}

.error-icon i {
  font-size: 26px;
  color: #ef4444;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.error-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.error-message {
  font-size: 13px;
  color: #6b7280;
  text-align: center;
  max-width: 300px;
  line-height: 1.5;
}

.retry-button {
  margin-top: 12px;
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.retry-button i {
  margin-right: 6px;
}

/* 空配置状态样式 */
.empty-card {
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.5) 0%, rgba(255, 255, 255, 1) 100%);
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 36px 20px;
  min-height: 120px;
}

.empty-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(147, 197, 253, 0.1) 100%);
  border-radius: 50%;
  margin-bottom: 14px;
}

.empty-icon i {
  font-size: 28px;
  color: #3b82f6;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.empty-subtitle {
  font-size: 12px;
  color: #6b7280;
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

.loading-card,
.error-card,
.empty-card {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .loading-placeholder,
  .error-placeholder,
  .empty-placeholder {
    padding: 40px 16px;
    min-height: 160px;
  }

  .loading-title,
  .error-title,
  .empty-title {
    font-size: 15px;
  }

  .error-message {
    font-size: 13px;
  }
}
</style>
