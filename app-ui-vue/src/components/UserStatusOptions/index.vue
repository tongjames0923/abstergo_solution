<template>
  <div class="user-status-options">
    <template v-if="readonly">
      <!-- 只读模式：显示文本 -->
      <span class="readonly-status" :class="statusClass">
        {{ statusLabel }}
      </span>
    </template>
    <template v-else>
      <!-- 可选模式：使用DynamicOptions -->
      <DynamicOptions
        ref="dynamicOptions"
        :api-url="apiUrl"
        :dict-key="dictKey"
        :value="value"
        :placeholder="placeholder"
        :clearable="clearable"
        :disabled="disabled"
        :multiple="multiple"
        v-bind="dynamicOptionsProps"
        v-on="dynamicOptionsListeners"
      />
    </template>
  </div>
</template>

<script>
import DynamicOptions from '@/components/DynamicOptions'

export default {
  name: 'UserStatusOptions',
  components: {
    DynamicOptions
  },
  props: {
    // v-model绑定值
    value: {
      type: [String, Number],
      default: null
    },
    // 是否只读模式
    readonly: {
      type: Boolean,
      default: false
    },
    // 占位符
    placeholder: {
      type: String,
      default: '请选择用户状态'
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
    // API地址
    apiUrl: {
      type: String,
      default: '/private/user/statusOptions'
    },
    // 字典键名
    dictKey: {
      type: String,
      default: 'userStatus'
    }
  },
  data() {
    return {
      // // 默认的数据转换函数
      // transform: (response, meta) => {
      //   // 如果响应数据已经是正确的格式，则直接返回
      //   if (response && Array.isArray(response.data)) {
      //     return response.data
      //   }
      //   // 如果是对象形式，则返回对象
      //   return response.data
      // }
    }
  },
  computed: {
    // 获取状态标签
    statusLabel() {
      if (this.value === null || this.value === undefined) {
        return '未设置'
      }

      // 通过字典获取标签
      const label = this.getDictLabel(this.dictKey, this.value)
      return label || `未知状态(${this.value})`
    },

    // 根据状态值设置不同的样式类
    statusClass() {
      const classes = []
      if (this.value === 1) {
        classes.push('status-active')
      } else if (this.value === 0) {
        classes.push('status-inactive')
      } else if (this.value === 2) {
        classes.push('status-locked')
      }
      return classes
    },

    // 传递给DynamicOptions的属性
    dynamicOptionsProps() {
      const {readonly, ...props} = this.$attrs
      return props
    },

    // 传递给DynamicOptions的事件监听器
    dynamicOptionsListeners() {
      return this.$listeners || {}
    }
  },
  methods: {
    // 重新加载选项
    reload() {
      if (this.$refs.dynamicOptions) {
        this.$refs.dynamicOptions.reload()
      }
    },

    // 确保字典已加载
    async ensureLoaded() {
      if (!this.readonly && this.dictKey) {
        try {
          await this.ensureDictLoaded(this.dictKey)
        } catch (error) {
          console.error('确保字典加载失败:', error)
        }
      }
    }
  },
  async mounted() {
    // 如果不是只读模式，初始化字典
    try {
      await this.initDictionary(this.dictKey, {
        apiUrl: this.apiUrl,
        labelField: 'label',
        valueField: 'value',
        lazy: false
      })
    } catch (error) {
      console.error('初始化用户状态字典失败:', error)
    }
  }
}
</script>

<style scoped>
.user-status-options {
  display: inline-block;
  width: 100%;
}

.readonly-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.status-inactive {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.status-locked {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}
</style>
