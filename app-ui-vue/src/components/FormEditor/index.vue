<template>
  <div class="form-config-editor">
    <el-card class="config-card">
      <div slot="header" class="card-header">
        <span>表单配置编辑器</span>
        <div>
          <el-button type="primary" size="small" @click="addFormField">添加字段</el-button>
          <el-button type="success" size="small" @click="saveConfig">保存配置</el-button>
        </div>
      </div>

      <el-form label-width="120px" label-position="left">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否可展开">
              <el-switch v-model="localConfig.expandable" @change="onConfigChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认展开">
              <el-switch v-model="localConfig.expanded" @change="onConfigChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签宽度">
              <el-input v-model="localConfig.labelWidth" placeholder="例如: 100px" @input="onConfigChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="栅格间距">
              <el-input-number
                v-model="localConfig.gutter"
                :min="0"
                :max="40"
                controls-position="right"
                @change="onConfigChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="栅格列数">
              <el-slider v-model="localConfig.span" :min="1" :max="24" show-input @change="onConfigChange" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="fields-card">
      <div slot="header" class="card-header">
        <span>表单字段配置</span>
      </div>

      <ul class="fields-list">
        <li v-for="(field, index) in localConfig.fields" :key="index" class="field-item">
          <el-card class="field-card">
            <div slot="header" class="field-header">
              <span>{{ field.label || '未命名字段' }} ({{ field.type || '未设置类型' }})</span>
              <div>
                <el-button type="text" size="small" :disabled="index === 0" @click="moveFieldUp(index)">上移</el-button>
                <el-button
                  type="text"
                  size="small"
                  :disabled="index === localConfig.fields.length - 1"
                  @click="moveFieldDown(index)"
                >下移
                </el-button>
                <el-button type="text" size="small" @click="duplicateField(index)">复制</el-button>
                <el-button type="text" size="small" style="color: #f56c6c;" @click="deleteField(index)">删除</el-button>
              </div>
            </div>

            <el-form label-width="120px" label-position="left" size="small">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="字段属性名(prop)">
                    <el-input v-model="field.prop" placeholder="字段在数据中的属性名" @input="onConfigChange" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="字段标签(label)">
                    <el-input v-model="field.label" placeholder="字段显示名称" @input="onConfigChange" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="字段类型(type)">
                    <el-select
                      v-model="field.type"
                      placeholder="请选择字段类型"
                      style="width: 100%;"
                      @change="onFieldTypeChange"
                    >
                      <el-option label="输入框 (input)" value="input" />
                      <el-option label="选择器 (select)" value="select" />
                      <el-option label="日期范围 (date-range)" value="date-range" />
                      <el-option label="动态选项 (dynamic-options)" value="dynamic-options" />
                      <el-option label="图片URL (url-image)" value="url-image" />
                    </el-select>
                  </el-form-item>
                </el-col>

                <!-- 特定类型字段的额外配置 -->
                <template v-if="field.type === 'input'">
                  <el-col :span="24">
                    <el-form-item label="输入框配置">
                      <InputConfig
                        :value="field.props"
                        @input="val => updateFieldProps(field, val)"
                      />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'select'">
                  <el-col :span="24">
                    <el-form-item label="选项配置">
                      <SelectOptionsEditor
                        :value="field.props && field.props.options"
                        @input="val => updateFieldOptions(field, val)"
                      />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'date-range'">
                  <el-col :span="24">
                    <el-form-item label="日期范围配置">
                      <DateRangeConfig
                        :value="field.props"
                        @input="val => updateFieldProps(field, val)"
                      />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'dynamic-options'">
                  <el-col :span="24">
                    <el-form-item label="动态选项配置">
                      <DynamicOptionsConfig
                        :value="field.props"
                        @input="val => updateFieldProps(field, val)"
                      />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'url-image'">
                  <el-col :span="24">
                    <el-form-item label="图片配置">
                      <UrlImageConfig
                        :value="field.props"
                        @input="val => updateFieldProps(field, val)"
                      />
                    </el-form-item>
                  </el-col>
                </template>

                <!-- 通用属性配置 -->
                <el-col :span="24">
                  <el-form-item label="其他属性(props)">
                    <el-input
                      :value="field.propsInput"
                      type="textarea"
                      :rows="3"
                      placeholder="JSON格式，例如: {&quot;size&quot;: &quot;medium&quot;, &quot;clearable&quot;: true}"
                      @input="val => updatePropsInput(field, val)"
                    />
                    <div v-if="field.propsError" class="props-error">
                      {{ field.propsError }}
                    </div>
                    <div class="props-hint">
                      <p>提示：这里可以配置该字段的其他属性，如 placeholder、clearable 等</p>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </li>
      </ul>
    </el-card>
  </div>
</template>

<script>
import SelectOptionsEditor from './ConfigComponents/SelectOptionsEditor.vue'
import DateRangeConfig from './ConfigComponents/DateRangeConfig.vue'
import DynamicOptionsConfig from './ConfigComponents/DynamicOptionsConfig.vue'
import UrlImageConfig from './ConfigComponents/UrlImageConfig.vue'
import InputConfig from './ConfigComponents/InputConfig.vue'

export default {
  name: 'FormConfigEditor',
  components: {
    SelectOptionsEditor,
    DateRangeConfig,
    DynamicOptionsConfig,
    UrlImageConfig,
    InputConfig
  },
  props: {
    value: {
      type: Object,
      default: () => ({
        expandable: true,
        expanded: true,
        labelWidth: '100px',
        gutter: 20,
        span: 8,
        fields: []
      })
    }
  },
  data() {
    return {
      localConfig: this.initializeConfig(this.value)
    }
  },
  watch: {
    value: {
      handler(newVal) {
        this.localConfig = this.initializeConfig(newVal)
      },
      deep: true
    }
  },
  methods: {
    initializeConfig(config) {
      // 确保配置对象具有所有必要的属性
      const initialized = {
        expandable: true,
        expanded: true,
        labelWidth: '100px',
        gutter: 20,
        span: 8,
        fields: [],
        ...config
      }

      // 确保每个字段都有必要的属性
      if (initialized.fields && Array.isArray(initialized.fields)) {
        initialized.fields = initialized.fields.map(field => ({
          prop: field.prop || '',
          label: field.label || '',
          type: field.type || 'input',
          props: field.props || {},
          propsInput: this.formatProps(field.props),
          propsError: ''
        }))
      } else {
        initialized.fields = []
      }

      return initialized
    },

    formatProps(props) {
      if (!props) return ''
      try {
        return JSON.stringify(props, null, 2)
      } catch (e) {
        return String(props)
      }
    },

    updatePropsInput(field, value) {
      // 更新输入框的值
      this.$set(field, 'propsInput', value)

      if (!value || value.trim() === '') {
        field.props = {}
        field.propsError = ''
      } else {
        try {
          const parsedProps = JSON.parse(value)
          field.props = parsedProps
          field.propsError = ''
        } catch (e) {
          // 保留输入但显示错误
          field.propsError = 'JSON格式错误: ' + e.message
        }
      }
      this.onConfigChange()
    },

    updateFieldOptions(field, options) {
      if (!field.props) {
        this.$set(field, 'props', {})
      }
      this.$set(field.props, 'options', options)
      // 更新显示的JSON字符串
      this.$set(field, 'propsInput', this.formatProps(field.props))
      this.onConfigChange()
    },

    updateFieldProps(field, props) {
      this.$set(field, 'props', props)
      // 更新显示的JSON字符串
      this.$set(field, 'propsInput', this.formatProps(field.props))
      this.onConfigChange()
    },

    updateFieldApiUrl(field, apiUrl) {
      if (!field.props) {
        this.$set(field, 'props', {})
      }
      this.$set(field.props, 'apiUrl', apiUrl)
      // 更新显示的JSON字符串
      this.$set(field, 'propsInput', this.formatProps(field.props))
      this.onConfigChange()
    },

    onFieldTypeChange() {
      this.onConfigChange()
    },

    addFormField() {
      this.localConfig.fields.push({
        prop: '',
        label: '',
        type: 'input',
        props: {},
        propsInput: '',
        propsError: ''
      })
      this.onConfigChange()
    },

    deleteField(index) {
      this.localConfig.fields.splice(index, 1)
      this.onConfigChange()
    },

    duplicateField(index) {
      const field = JSON.parse(JSON.stringify(this.localConfig.fields[index]))
      // 修改属性名避免重复
      if (field.prop) {
        field.prop = field.prop + '_copy'
      }
      if (field.label) {
        field.label = field.label + ' (副本)'
      }
      // 重新初始化props相关字段
      field.propsInput = this.formatProps(field.props)
      field.propsError = ''
      this.localConfig.fields.splice(index + 1, 0, field)
      this.onConfigChange()
    },

    moveFieldUp(index) {
      if (index > 0) {
        const temp = this.localConfig.fields[index]
        this.localConfig.fields.splice(index, 1)
        this.localConfig.fields.splice(index - 1, 0, temp)
        this.onConfigChange()
      }
    },

    moveFieldDown(index) {
      if (index < this.localConfig.fields.length - 1) {
        const temp = this.localConfig.fields[index]
        this.localConfig.fields.splice(index, 1)
        this.localConfig.fields.splice(index + 1, 0, temp)
        this.onConfigChange()
      }
    },

    saveConfig() {
      this.$emit('input', this.localConfig)
      this.$emit('save', this.localConfig)
    },

    onConfigChange() {
      this.$emit('input', this.localConfig)
    },

    // 提供一个直接获取当前配置数据的方法
    getData() {
      // 清理临时字段
      const cleanConfig = JSON.parse(JSON.stringify(this.localConfig))
      cleanConfig.fields.forEach(field => {
        delete field.propsInput
        delete field.propsError
      })
      return cleanConfig
    }
  }
}
</script>

<style scoped>
.form-config-editor {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fields-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.field-item {
  margin-bottom: 20px;
}

.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-card >>> .el-card__header {
  padding: 10px 20px;
}

.field-card >>> .el-form-item {
  margin-bottom: 12px;
}

.field-card >>> .el-form-item__label {
  font-weight: normal;
}

.props-error {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.props-hint {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.props-hint p {
  margin: 0;
}
</style>
