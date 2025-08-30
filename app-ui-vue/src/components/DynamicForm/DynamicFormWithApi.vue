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
        <slot :name="slotName" v-bind="slotData"></slot>
      </template>
    </DynamicForm>

    <el-card v-else-if="loading" shadow="never">
      <div class="loading-placeholder">
        <i class="el-icon-loading"></i>
        <span>加载配置中...</span>
      </div>
    </el-card>

    <el-card v-else-if="error" shadow="never">
      <div class="error-placeholder">
        <i class="el-icon-warning"></i>
        <span>加载配置失败: {{ error }}</span>
        <el-button size="mini" @click="loadConfig">重新加载</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import DynamicForm from './index.vue'
import request from "@/utils/request";

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
          params: {key: this.configName}
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
    }
  }
}
</script>

<style scoped>
.loading-placeholder, .error-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
}

.error-placeholder {
  flex-direction: column;
  gap: 10px;
}

.loading-placeholder i, .error-placeholder i {
  margin-right: 5px;
  font-size: 18px;
}
</style>
