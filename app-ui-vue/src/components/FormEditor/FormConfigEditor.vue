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
                <template v-if="field.type === 'select'">
                  <el-col :span="24">
                    <el-form-item label="选项配置">
                      <SelectOptionsEditor v-model="field.options" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                </template>

                <template v-if="field.type === 'date-range'">
                  <el-col :span="24">
                    <el-alert
                      title="提示"
                      description="日期范围选择器的所有属性都应该在'其他属性(props)'中配置，如placeholder、format等"
                      type="info"
                      show-icon
                      style="margin-bottom: 15px;"
                    />
                  </el-col>
                </template>

                <template v-if="field.type === 'dynamic-options'">
                  <el-col :span="24">
                    <el-form-item label="API地址">
                      <el-input v-model="field.apiUrl" placeholder="/api/options" @input="onConfigChange" />
                    </el-form-item>
                  </el-col>
                </template>

                <!-- 通用属性配置 -->
                <el-col :span="24">
                  <el-form-item label="通用属性">
                    <el-button size="mini" @click="togglePropsEditor(field)">
                      {{ field.showPropsEditor ? '隐藏属性编辑器' : '显示属性编辑器' }}
                    </el-button>
                  </el-form-item>
                </el-col>

                <template v-if="field.showPropsEditor">
                  <el-col :span="24" class="props-editor-container">
                    <div class="props-editor">
                      <div class="props-editor-header">
                        <h4>属性编辑器</h4>
                        <el-button size="mini" type="primary" @click="addFieldProp(field)">添加属性</el-button>
                      </div>

                      <el-table
                        :data="getPropsArray(field)"
                        style="width: 100%"
                        size="mini"
                        empty-text="暂无属性配置，点击上方按钮添加"
                      >
                        <el-table-column prop="key" label="属性名" width="180">
                          <template slot-scope="scope">
                            <el-input
                              v-model="scope.row.key"
                              size="mini"
                              placeholder="属性名"
                              @blur="updateFieldPropKey(field, scope.row)"
                            />
                          </template>
                        </el-table-column>
                        <el-table-column prop="value" label="属性值">
                          <template slot-scope="scope">
                            <el-input
                              v-if="isSimpleValue(scope.row.value)"
                              v-model="scope.row.value"
                              size="mini"
                              placeholder="属性值"
                              @blur="updateFieldPropValue(field, scope.row)"
                            />
                            <el-switch
                              v-else-if="typeof scope.row.value === 'boolean'"
                              v-model="scope.row.value"
                              active-text="是"
                              inactive-text="否"
                              @change="val => updateFieldPropValue(field, {...scope.row, value: val})"
                            />
                            <el-input
                              v-else
                              :value="JSON.stringify(scope.row.value)"
                              size="mini"
                              placeholder="复杂类型值"
                              disabled
                            >
                              <el-button
                                slot="append"
                                icon="el-icon-edit"
                                @click="editComplexProp(field, scope.row)"
                              />
                            </el-input>
                          </template>
                        </el-table-column>
                        <el-table-column label="操作" width="80">
                          <template slot-scope="scope">
                            <el-button
                              size="mini"
                              type="text"
                              icon="el-icon-delete"
                              @click="removeFieldProp(field, scope.row)"
                            />
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-col>
                </template>

                <el-col :span="24">
                  <el-form-item label="属性预览">
                    <el-input
                      :value="formatProps(field.props)"
                      type="textarea"
                      :rows="3"
                      readonly
                      placeholder="当前属性配置将显示在这里"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </li>
      </ul>
    </el-card>

    <!-- 编辑复杂属性值的对话框 -->
    <el-dialog
      :visible.sync="complexPropDialog.visible"
      title="编辑复杂属性值"
      width="500px"
    >
      <el-input
        v-model="complexPropDialog.value"
        type="textarea"
        :rows="6"
        placeholder="请输入有效的JSON格式"
      />
      <span v-if="complexPropDialog.error" class="error-text">{{ complexPropDialog.error }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="complexPropDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="saveComplexProp">确 定</el-button>
      </span>
    </el-dialog>
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
      localConfig: this.initializeConfig(this.value),
      complexPropDialog: {
        visible: false,
        field: null,
        prop: null,
        value: '',
        error: ''
      }
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
          options: field.options,
          apiUrl: field.apiUrl,
          props: field.props || {},
          showPropsEditor: field.showPropsEditor || false,
          propsInput: this.formatProps(field.props),
          propsError: '',
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

    onFieldTypeChange(field) {
      this.$set(field, 'props', field.props || {})
      this.onConfigChange()
    },

    togglePropsEditor(field) {
      this.$set(field, 'showPropsEditor', !field.showPropsEditor)
      if (!field.props) {
        this.$set(field, 'props', {})
      }
    },

    getPropsArray(field) {
      if (!field.props) return []
      return Object.keys(field.props).map(key => ({
        key,
        value: field.props[key]
      }))
    },

    addFieldProp(field) {
      if (!field.props) {
        this.$set(field, 'props', {})
      }

      // 查找一个未使用的属性名
      let newKey = 'prop'
      let counter = 1
      while (field.props.hasOwnProperty(newKey + counter)) {
        counter++
      }
      newKey = newKey + counter

      // 添加新属性
      this.$set(field.props, newKey, '')
      this.onConfigChange()
    },

    updateFieldPropKey(field, propRow) {
      // 只有当键发生变化时才更新
      if (propRow.originalKey !== undefined && propRow.originalKey !== propRow.key) {
        const value = field.props[propRow.originalKey];
        this.$delete(field.props, propRow.originalKey);
        this.$set(field.props, propRow.key, value);
        this.onConfigChange();
      } else if (propRow.originalKey === undefined) {
        // 新增属性的情况
        propRow.originalKey = propRow.key;
      }
    },

    updateFieldPropValue(field, propRow) {
      this.$set(field.props, propRow.key, propRow.value)
      this.onConfigChange()
    },

    removeFieldProp(field, propRow) {
      this.$delete(field.props, propRow.key)
      this.onConfigChange()
    },

    isSimpleValue(value) {
      return typeof value === 'string' || typeof value === 'number'
    },

    editComplexProp(field, propRow) {
      this.complexPropDialog.field = field
      this.complexPropDialog.prop = propRow
      try {
        this.complexPropDialog.value = JSON.stringify(propRow.value, null, 2)
        this.complexPropDialog.error = ''
      } catch (e) {
        this.complexPropDialog.value = String(propRow.value)
        this.complexPropDialog.error = '无法格式化值: ' + e.message
      }
      this.complexPropDialog.visible = true
    },

    saveComplexProp() {
      try {
        const value = JSON.parse(this.complexPropDialog.value)
        this.$set(
          this.complexPropDialog.prop,
          'value',
          value
        )
        this.$set(
          this.complexPropDialog.field.props,
          this.complexPropDialog.prop.key,
          value
        )
        this.complexPropDialog.visible = false
        this.complexPropDialog.error = ''
        this.onConfigChange()
      } catch (e) {
        this.complexPropDialog.error = 'JSON格式错误: ' + e.message
      }
    },

    addFormField() {
      this.localConfig.fields.push({
        prop: '',
        label: '',
        type: 'input',
        props: {},
        showPropsEditor: false,
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
        delete field.showPropsEditor
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

.props-editor-container {
  margin-bottom: 15px;
}

.props-editor {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
}

.props-editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.props-editor-header h4 {
  margin: 0;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
  display: block;
  margin-top: 5px;
}

.dialog-footer {
  text-align: right;
}
</style>
