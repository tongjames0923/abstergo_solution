<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
    :multiple="multiple"
    @change="handleChange">
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value">
    </el-option>
  </el-select>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'DynamicOptions',
  props: {
    // API地址，例如: '/api/options?name=用户状态' 或者一个返回Promise的函数
    apiUrl: {
      type: [String, Function],
      required: true
    },
    // v-model绑定值
    value: {
      type: [String, Number, Array],
      default: ''
    },
    // 占位符
    placeholder: {
      type: String,
      default: '请选择'
    },
    // 是否可清空
    clearable: {
      type: Boolean,
      default: true
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 是否多选
    multiple: {
      type: Boolean,
      default: false
    },
    // 数据转换函数，用于处理API返回的数据格式
    transform: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      options: [],
      loading: false,
      selectedValue: this.value
    }
  },
  watch: {
    value(newVal) {
      this.selectedValue = newVal
    }
  },
  created() {
    this.loadOptions()
  },
  methods: {
    // 从API加载选项数据
    async loadOptions() {
      this.loading = true
      try {
        let response
        // 如果 apiUrl 是函数，则调用它获取数据
        if (typeof this.apiUrl === 'function') {
          response = await this.apiUrl()
        } else {
          // 否则使用原有的请求方式
          response = await request({
            url: this.apiUrl,
            method: 'get'
          })
        }

        // 如果提供了转换函数，则使用它来处理数据
        if (this.transform && typeof this.transform === 'function') {
          this.options = this.transform(response)
        } else {
          // 默认处理格式 {data: [{label: '', value: ''}]}
          this.options = response.data || []
        }
      } catch (error) {
        console.error('Failed to load options:', error)
        this.options = []
      } finally {
        this.loading = false
      }
    },

    // 处理值变化
    handleChange(value) {
      this.$emit('input', value)
      this.$emit('change', value)
    },

    // 手动重新加载选项
    reload() {
      this.loadOptions()
    }
  }
}
</script>

<style scoped>

</style>
