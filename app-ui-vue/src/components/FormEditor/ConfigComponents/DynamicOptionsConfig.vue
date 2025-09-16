<template>
  <div class="dynamic-options-config">
    <el-alert
      title="动态选项配置说明"
      description="配置从API获取选项的动态选择器"
      type="info"
      show-icon
      style="margin-bottom: 15px;"
    />

    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item label="API地址">
          <el-input
            :value="value && value.apiUrl"
            placeholder="/api/options"
            @input="updateConfig('apiUrl', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="标签字段">
          <el-input
            :value="value && value.labelField"
            placeholder="label"
            @input="updateConfig('labelField', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="值字段">
          <el-input
            :value="value && value.valueField"
            placeholder="value"
            @input="updateConfig('valueField', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="请求方法">
          <el-select
            :value="value && value.method"
            placeholder="请选择请求方法"
            style="width: 100%;"
            @change="updateConfig('method', $event)"
          >
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="自动加载">
          <el-switch
            :value="value && value.autoLoad"
            @change="updateConfig('autoLoad', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="缓存结果">
          <el-switch
            :value="value && value.cache"
            @change="updateConfig('cache', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="多选支持">
          <el-switch
            :value="value && value.multiple"
            @change="updateConfig('multiple', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="可清除">
          <el-switch
            :value="value && value.clearable"
            @change="updateConfig('clearable', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="可过滤">
          <el-switch
            :value="value && value.filterable"
            @change="updateConfig('filterable', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="远程搜索">
          <el-switch
            :value="value && value.remote"
            @change="updateConfig('remote', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="占位符">
          <el-input
            :value="value && value.placeholder"
            placeholder="请选择"
            @input="updateConfig('placeholder', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="尺寸">
          <el-select
            :value="value && value.size"
            placeholder="请选择尺寸"
            style="width: 100%;"
            @change="updateConfig('size', $event)"
          >
            <el-option label="大 (large)" value="large" />
            <el-option label="默认 (default)" value="default" />
            <el-option label="小 (small)" value="small" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="请求头">
          <el-input
            :value="headersInput"
            type="textarea"
            :rows="3"
            placeholder="JSON格式，例如: {&quot;Authorization&quot;: &quot;Bearer token&quot;}"
            @input="updateHeaders"
          />
          <div v-if="headersError" class="params-error">
            {{ headersError }}
          </div>
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="请求参数">
          <el-input
            :value="paramsInput"
            type="textarea"
            :rows="3"
            placeholder="JSON格式，例如: {&quot;status&quot;: &quot;active&quot;}"
            @input="updateParams"
          />
          <div v-if="paramsError" class="params-error">
            {{ paramsError }}
          </div>
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'DynamicOptionsConfig',
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      paramsInput: this.formatParams(this.value && this.value.params),
      paramsError: '',
      headersInput: this.formatParams(this.value && this.value.headers),
      headersError: ''
    }
  },
  watch: {
    value: {
      handler(newVal) {
        this.paramsInput = this.formatParams(newVal && newVal.params)
        this.headersInput = this.formatParams(newVal && newVal.headers)
      },
      deep: true
    }
  },
  methods: {
    formatParams(params) {
      if (!params) return ''
      try {
        return JSON.stringify(params, null, 2)
      } catch (e) {
        return String(params)
      }
    },

    updateConfig(key, value) {
      const newConfig = { ...this.value, [key]: value }
      this.$emit('input', newConfig)
    },

    updateParams(value) {
      this.paramsInput = value

      if (!value || value.trim() === '') {
        const newConfig = { ...this.value }
        delete newConfig.params
        this.paramsError = ''
        this.$emit('input', newConfig)
      } else {
        try {
          const parsedParams = JSON.parse(value)
          const newConfig = { ...this.value, params: parsedParams }
          this.paramsError = ''
          this.$emit('input', newConfig)
        } catch (e) {
          this.paramsError = 'JSON格式错误: ' + e.message
        }
      }
    },

    updateHeaders(value) {
      this.headersInput = value

      if (!value || value.trim() === '') {
        const newConfig = { ...this.value }
        delete newConfig.headers
        this.headersError = ''
        this.$emit('input', newConfig)
      } else {
        try {
          const parsedHeaders = JSON.parse(value)
          const newConfig = { ...this.value, headers: parsedHeaders }
          this.headersError = ''
          this.$emit('input', newConfig)
        } catch (e) {
          this.headersError = 'JSON格式错误: ' + e.message
        }
      }
    }
  }
}
</script>

<style scoped>
.dynamic-options-config {
  padding: 10px 0;
}

.params-error {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}
</style>
