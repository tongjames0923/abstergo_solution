<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled || dictLoading"
    :loading=" dictLoading"
    :multiple="multiple"
    @change="handleChange">
    <el-option
      v-for="item in finalOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value">
    </el-option>
  </el-select>
</template>

<script>
import {mapActions, mapGetters} from 'vuex'
import request from "@/utils/request";

export default {
  name: 'DynamicOptions',
  props: {
    // API地址，例如: '/api/options?name=用户状态'
    apiUrl: {
      type: String,
      default: ''
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
    },
    // 字典键名，如果提供则使用字典功能
    dictKey: {
      type: String,
      default: null
    },
    // 是否懒加载字典
    lazy: {
      type: Boolean,
      default: true
    },
    // 外部传入选项数据（不使用API）
    options: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      selectedValue: this.value,
      localOptions: []
    }
  },
  computed: {
    ...mapGetters('dictionaries', [
      'dictionaryItems',
      'dictionaryLoading',
      'dictionaryLoaded',
      'dictionaryError'
    ]),

    // 判断使用哪种选项数据源
    finalOptions() {
      // 如果传入了options属性，则使用它
      if (this.options && this.options.length > 0) {
        return this.options
      }

      // 如果提供了dictKey且字典已加载，则使用字典数据
      if (this.dictKey && this.dictionaryLoaded(this.dictKey)) {
        return this.dictionaryItems(this.dictKey)
      }

      // 否则使用本地加载的选项
      return this.localOptions
    },

    // 字典加载状态
    dictLoading() {
      return this.dictKey ? this.dictionaryLoading(this.dictKey) : false
    },

    // 组件整体加载状态
    loading() {
      return this.dictLoading || (this.localOptions.length === 0 && !this.dictKey && this.apiUrl)
    }
  },
  watch: {
    value(newVal) {
      this.selectedValue = newVal
    },

    // 监听dictKey变化，重新初始化字典
    dictKey: {
      handler(newVal) {
        if (newVal) {
          this.initDict()
        }
      },
      immediate: true
    }
  },
  created() {
    // 如果没有提供dictKey但提供了apiUrl，则直接加载选项
    if (!this.dictKey && this.apiUrl) {
      this.loadOptions()
    }
  },
  methods: {
    ...mapActions('dictionaries', [
      'createDictionary',
      'loadDictionary',
      'ensureDictionaryLoaded'
    ]),

    // 初始化字典
    async initDict() {
      if (!this.dictKey) return

      try {
        // 创建字典实例
        await this.createDictionary({
          key: this.dictKey,
          apiUrl: this.apiUrl,
          transform: this.transform,
          lazy: this.lazy
        })
      } catch (error) {
        console.error('初始化字典失败:', error)
        // 初始化失败时回退到直接加载选项
        this.loadOptions()
      }
    },

    // 从API加载选项数据
    async loadOptions() {
      if (!this.apiUrl) return

      try {
        // 发起请求获取数据
        const response = await request({
          url: this.apiUrl,
          method: 'get'
        })

        // 如果提供了转换函数，则使用它来处理数据
        if (this.transform && typeof this.transform === 'function') {
          this.localOptions = this.transform(response)
        } else {
          // 默认处理格式 {data: [{label: '', value: ''}]}
          this.localOptions = response.data || []
        }
      } catch (error) {
        console.error('Failed to load options:', error)
        this.localOptions = []
      }
    },

    // 处理值变化
    handleChange(value) {
      this.$emit('input', value)
      this.$emit('change', value)
    },

    // 手动重新加载选项
    async reload() {
      if (this.dictKey) {
        try {
          await this.loadDictionary(this.dictKey)
        } catch (error) {
          console.error('重新加载字典失败:', error)
        }
      } else {
        await this.loadOptions()
      }
    }
  }
}
</script>

<style scoped>

</style>
