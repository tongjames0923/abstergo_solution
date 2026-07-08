<template>
  <div class="form-creator-container">
    <div class="header-section">
      <h2>{{ $route.query.id ? '表单编辑器' : '表单创建器' }}</h2>
      <p>{{ $route.query.id ? '编辑现有动态表单' : '创建和配置动态表单' }}</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
      <el-tab-pane label="表单配置" name="config">
        <div class="config-section">
          <el-alert
            title="使用说明"
            type="info"
            description="在此面板中配置表单的各项参数和字段，配置完成后可以预览和保存表单"
            show-icon
            class="config-info"
          />

          <div class="config-actions">
            <el-button type="primary" icon="el-icon-view" @click="previewForm">预览表单</el-button>
            <el-button type="success" icon="el-icon-document-checked" @click="saveForm">保存表单</el-button>
            <el-button type="info" icon="el-icon-refresh" @click="resetForm">重置配置</el-button>
          </div>

          <FormConfigEditor
            ref="formConfigEditor"
            :value.sync="formConfig"
            class="form-config-editor"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="表单预览" name="preview">
        <div class="preview-section">
          <div class="preview-actions">
            <el-button icon="el-icon-back" @click="activeTab = 'config'">返回配置</el-button>
            <el-button type="primary" icon="el-icon-check" @click="submitForm">提交表单</el-button>
            <el-button type="warning" icon="el-icon-refresh" @click="resetPreviewForm">重置表单</el-button>
            <el-button type="info" icon="el-icon-data-analysis" @click="collectFormData">获取数据</el-button>
          </div>

          <DynamicForm
            v-if="previewConfig"
            ref="dynamicForm"
            :config="previewConfig"
            :value="formData"
            class="dynamic-form-preview"
            @submit="handleFormSubmit"
            @reset="handleFormReset"
          />

          <el-card v-if="collectedData" class="data-card">
            <div slot="header" class="clearfix">
              <span>表单数据</span>
            </div>
            <pre>{{ collectedData }}</pre>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="使用说明" name="instructions">
        <div class="instructions-section">
          <el-card>
            <div slot="header">
              <span>表单创建器使用说明</span>
            </div>
            <div class="instructions-content">
              <h3>基本操作</h3>
              <ol>
                <li><strong>表单配置</strong>：在"表单配置"标签页中设置表单的基本属性和字段</li>
                <li><strong>添加字段</strong>：点击"添加字段"按钮添加新的表单字段</li>
                <li><strong>配置字段</strong>：为每个字段设置属性名、标签、类型等参数</li>
                <li><strong>预览表单</strong>：点击"预览表单"按钮查看表单效果</li>
                <li><strong>保存表单</strong>：在配置完成后点击"保存表单"按钮保存配置</li>
              </ol>

              <h3>字段类型说明</h3>
              <ul>
                <li><strong>输入框 (input)</strong>：普通的文本输入框</li>
                <li><strong>选择器 (select)</strong>：下拉选择框，需要配置选项</li>
                <li><strong>日期范围 (date-range)</strong>：日期范围选择器</li>
                <li><strong>动态选项 (dynamic-options)</strong>：通过API获取选项的选择器</li>
                <li><strong>图片URL (url-image)</strong>：图片链接输入框</li>
              </ul>

              <h3>技巧提示</h3>
              <ul>
                <li>可以拖拽字段来调整顺序</li>
                <li>可以复制现有字段快速创建相似字段</li>
                <li>可以使用"其他属性"为字段添加额外的Element UI属性</li>
                <li>表单配置会自动保存，刷新页面也不会丢失</li>
              </ul>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Index from '@/components/FormEditor/index.vue'
import DynamicForm from '@/components/DynamicForm/index.vue'
import { saveFormConfig, updateFormConfig, getFormById } from '@/api/view'

export default {
  name: 'FormCreator',
  components: {
    FormConfigEditor: Index,
    DynamicForm
  },
  data() {
    return {
      activeTab: 'config',
      formConfig: {
        expandable: true,
        expanded: true,
        labelWidth: '100px',
        gutter: 20,
        span: 8,
        fields: []
      },
      previewConfig: null,
      formData: {},
      collectedData: null
    }
  },
  created() {
    // 检查是否有表单ID参数，如果有则加载现有表单
    const formId = this.$route.query.id
    if (formId) {
      this.loadExistingForm(formId)
    } else {
      // 从本地存储加载保存的配置（如果存在）
      const savedConfig = localStorage.getItem('formCreatorConfig')
      if (savedConfig) {
        try {
          this.formConfig = JSON.parse(savedConfig)
        } catch (e) {
          console.warn('Failed to parse saved form config:', e)
        }
      }
    }
  },
  methods: {
    // 加载现有表单
    async loadExistingForm(formId) {
      try {
        this.loading = true
        const response = await getFormById(formId)
        this.formConfig = response.data
        // 清除本地存储的配置，避免冲突
        localStorage.removeItem('formCreatorConfig')
        // 消息提示由后端统一控制
      } catch (error) {
        // 消息提示由后端统一控制
      } finally {
        this.loading = false
      }
    },

    previewForm() {
      // 通过 ref 调用子组件的 getData 方法获取数据
      const data = this.$refs.formConfigEditor.getData()
      this.previewConfig = data
      this.activeTab = 'preview'
    },

    saveForm() {
      // 保存表单配置到服务器
      try {
        const data = this.$refs.formConfigEditor.getData()
        console.log('表单配置数据:', data)

        // 检查是否是更新现有表单
        if (this.$route.query.id) {
          // 更新现有表单
          updateFormConfig(this.$route.query.id, data).then(response => {
            // 消息提示由后端统一控制
          }).catch(error => {
            // 消息提示由后端统一控制
          })
        } else {
          // 创建新表单
          saveFormConfig(data).then(response => {
            // 消息提示由后端统一控制
            // 同时也保存到本地存储作为备份
            localStorage.setItem('formCreatorConfig', JSON.stringify(data))
          }).catch(error => {
            // 消息提示由后端统一控制
          })
        }
      } catch (e) {
        // 消息提示由后端统一控制
      }
    },

    resetForm() {
      this.$confirm('确定要重置所有配置吗？此操作不可恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.formConfig = {
          expandable: true,
          expanded: true,
          labelWidth: '100px',
          gutter: 20,
          span: 8,
          fields: []
        }
        localStorage.removeItem('formCreatorConfig')
        // 消息提示由后端统一控制
      }).catch(() => {
        // 用户取消操作
      })
    },

    handleFormSubmit(data) {
      // 消息提示由后端统一控制
      console.log('表单提交数据:', data)
    },

    handleFormReset() {
      // 消息提示由后端统一控制
      this.formData = {}
      this.collectedData = null
    },

    resetPreviewForm() {
      this.formData = {}
      this.collectedData = null
      // 消息提示由后端统一控制
    },

    collectFormData() {
      if (this.$refs.dynamicForm) {
        const data = this.$refs.dynamicForm.getFormData()
        this.collectedData = JSON.stringify(data, null, 2)
        console.log('表单当前数据:', data)
        // 消息提示由后端统一控制
      }
    },

    submitForm() {
      if (this.$refs.dynamicForm) {
        const data = this.$refs.dynamicForm.getFormData()
        // 消息提示由后端统一控制
        console.log('提交的表单数据:', data)
      }
    }
  }
}
</script>

<style scoped>
.form-creator-container {
  padding: 20px;
  min-height: calc(100vh - 84px);
}

.header-section {
  margin-bottom: 20px;
}

.header-section h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.header-section p {
  color: #606266;
  font-size: 14px;
}

.main-tabs {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.config-section,
.preview-section,
.instructions-section {
  padding: 20px 0;
}

.config-info {
  margin-bottom: 20px;
}

.config-actions,
.preview-actions {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.config-actions .el-button,
.preview-actions .el-button {
  margin-left: 0;
}

.form-config-editor {
  margin-top: 20px;
}

.dynamic-form-preview {
  margin-bottom: 20px;
}

.data-card pre {
  padding: 15px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
}

.instructions-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
}

.instructions-content ol,
.instructions-content ul {
  padding-left: 20px;
}

.instructions-content li {
  margin-bottom: 8px;
  line-height: 1.6;
}
</style>
