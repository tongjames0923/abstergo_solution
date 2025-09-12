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
                      @change="onConfigChange"
                    >
                      <el-option label="输入框 (input)" value="input" />
                      <el-option label="选择器 (select)" value="select" />
                      <el-option label="日期范围 (date-range)" value="date-range" />
                      <el-option label="动态选项 (dynamic-options)" value="dynamic-options" />
                      <el-option label="图片URL (url-image)" value="url-image" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="占位符(placeholder)">
                    <el-input v-model="field.placeholder" placeholder="输入框提示文字" @input="onConfigChange" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="前缀图标">
                    <el-input v-model="field.prefixIcon" placeholder="例如: el-icon-user" @input="onConfigChange" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="可清除">
                    <el-switch v-model="field.clearable" active-text="是" inactive-text="否" @change="onConfigChange" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="栅格列数(span)">
                    <el-input-number
                      v-model="field.span"
                      :min="1"
                      :max="24"
                      controls-position="right"
                      @change="onConfigChange"
                    />
                  </el-form-item>
                </el-col>

                <!-- 特定类型字段的额外配置 -->
                <template v-if="field.type === 'select'">
                  <el-col :span="24">
                    <el-form-item label="选项配置">
                      <SelectOptionsEditor v-model="field.options" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'date-range'">
                  <el-col :span="12">
                    <el-form-item label="值格式">
                      <el-input
                        v-model="field.valueFormat"
                        placeholder="例如: yyyy-MM-dd HH:mm:ss"
                        @input="onConfigChange"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="显示格式">
                      <el-input v-model="field.format" placeholder="例如: yyyy-MM-dd" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="开始占位符">
                      <el-input v-model="field.startPlaceholder" placeholder="开始日期" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="结束占位符">
                      <el-input v-model="field.endPlaceholder" placeholder="结束日期" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="分隔符">
                      <el-input v-model="field.rangeSeparator" placeholder="至" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'dynamic-options'">
                  <el-col :span="24">
                    <el-form-item label="API地址">
                      <el-input v-model="field.apiUrl" placeholder="/api/options" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                </template>

                <el-col :span="24">
                  <el-form-item label="其他属性(props)">
                    <el-input
                      v-model="field.propsInput"
                      type="textarea"
                      :rows="3"
                      placeholder="JSON格式，例如: {&quot;size&quot;: &quot;medium&quot;, &quot;clearable&quot;: true}"
                      @input="val => updatePropsInput(field, val)"
                    />
                    <div v-if="field.propsError" class="props-error">
                      {{ field.propsError }}
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
import SelectOptionsEditor from './SelectOptionsEditor.vue'

export default {
  name: 'FormConfigEditor',
  components: {
    SelectOptionsEditor
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
          prop: '',
          label: '',
          type: 'input',
          placeholder: '',
          clearable: true,
          prefixIcon: '',
          span: initialized.span,
          props: undefined,
          propsInput: this.formatProps(field.props),
          propsError: '',
          ...field
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
      field.propsInput = value
      if (!value || value.trim() === '') {
        field.props = undefined
        field.propsError = ''
      } else {
        try {
          field.props = JSON.parse(value)
          field.propsError = ''
        } catch (e) {
          // 不设置props，保留错误信息用于显示
          field.propsError = 'JSON格式错误: ' + e.message
        }
      }
      this.onConfigChange()
    },

    addFormField() {
      this.localConfig.fields.push({
        prop: '',
        label: '',
        type: 'input',
        placeholder: '',
        clearable: true,
        prefixIcon: '',
        span: this.localConfig.span,
        props: undefined,
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
</style>
