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
            <el-button type="warning" icon="el-icon-edit-outline" @click="openGenerateDialog">
              {{ $t('form.generateFromController') }}
            </el-button>
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

    <el-dialog
      :title="$t('form.generateFromController')"
      :visible.sync="generateDialogVisible"
      width="720px"
      append-to-body
      :close-on-click-modal="false"
      class="generate-dialog"
    >
      <el-alert
        :title="$t('form.generateFromControllerHint')"
        type="info"
        show-icon
        class="generate-hint"
      />

      <div class="generate-controls">
        <el-select
          v-model="generateController"
          filterable
          :placeholder="$t('form.controllerPlaceholder')"
          class="generate-controller"
        >
          <el-option
            v-for="item in crudControllers"
            :key="item.name"
            :label="controllerOptionLabel(item)"
            :value="item.name"
          />
        </el-select>
        <el-select v-model="generateScene" class="generate-scene">
          <el-option :label="$t('form.sceneSearch')" value="search" />
          <el-option :label="$t('form.sceneSave')" value="save" />
          <el-option :label="$t('form.sceneEdit')" value="edit" />
        </el-select>
        <el-button
          type="primary"
          :loading="generateLoading"
          :disabled="!generateController"
          @click="generateFormConfig"
        >
          {{ $t('form.generate') }}
        </el-button>
      </div>

      <div v-if="generatedConfig" class="generate-preview">
        <div class="generate-preview-title">{{ $t('form.generatePreview') }}</div>
        <pre>{{ generatedConfigText }}</pre>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="generateDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!generatedConfig" @click="applyGeneratedConfig">
          {{ $t('form.generateApply') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Index from '@/components/FormEditor/index.vue'
import DynamicForm from '@/components/DynamicForm/index.vue'
import {
  generateFormConfigFromController,
  getFormById,
  listCrudControllers,
  saveFormConfig,
  updateFormConfig
} from '@/api/view'

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
      collectedData: null,
      generateDialogVisible: false,
      generateController: '',
      generateScene: 'search',
      crudControllers: [],
      generatedConfig: null,
      generateLoading: false
    }
  },
  computed: {
    /**
     * 生成配置的 JSON 预览文本
     * @returns {string} 格式化后的 JSON
     */
    generatedConfigText() {
      return this.generatedConfig ? JSON.stringify(this.generatedConfig, null, 2) : ''
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

    /**
     * 控制器选项显示文本
     * @param {Object} item - 控制器选项
     * @returns {string} 显示文本
     */
    controllerOptionLabel(item) {
      return item.path ? `${item.name}（${item.path}）` : item.name
    },

    /**
     * 打开自动生成弹窗并加载控制器列表
     * @returns {Promise} 加载结果
     */
    async openGenerateDialog() {
      this.generateDialogVisible = true
      if (this.crudControllers.length === 0) {
        await this.loadCrudControllers()
      }
    },

    /**
     * 加载可生成配置的 CRUD 控制器
     * @returns {Promise} 加载结果
     */
    async loadCrudControllers() {
      try {
        const response = await listCrudControllers()
        this.crudControllers = Array.isArray(response.data) ? response.data : []
        if (this.crudControllers.length > 0 && !this.generateController) {
          this.generateController = this.crudControllers[0].name
        }
      } catch (error) {
        // 消息提示由 request.js 统一处理
      }
    },

    /**
     * 根据所选控制器生成默认表单配置
     * @returns {Promise} 生成结果
     */
    async generateFormConfig() {
      this.generateLoading = true
      try {
        const response = await generateFormConfigFromController(this.generateController, this.generateScene)
        this.generatedConfig = response.data || null
      } catch (error) {
        this.generatedConfig = null
        // 消息提示由 request.js 统一处理
      } finally {
        this.generateLoading = false
      }
    },

    /**
     * 确认后将生成配置写入编辑器，用户可继续微调
     * @returns {Promise} 应用结果
     */
    async applyGeneratedConfig() {
      if (!this.generatedConfig) return
      try {
        await this.$confirm(
          this.$t('form.generateConfirmTip'),
          this.$t('form.generateConfirm'),
          {
            confirmButtonText: this.$t('common.confirm'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning'
          }
        )
        this.formConfig = JSON.parse(JSON.stringify(this.generatedConfig))
        this.generateDialogVisible = false
        this.activeTab = 'config'
      } catch (error) {
        // 用户取消应用
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
          }).catch(() => {
            // 消息提示由后端统一控制
          })
        } else {
          // 创建新表单
          saveFormConfig(data).then(response => {
            // 消息提示由后端统一控制
            // 同时也保存到本地存储作为备份
            localStorage.setItem('formCreatorConfig', JSON.stringify(data))
          }).catch(() => {
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

.generate-hint {
  margin-bottom: 16px;
}

.generate-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  max-width: 100%;
}

.generate-controller {
  min-width: 240px;
  max-width: 100%;
}

.generate-scene {
  width: 160px;
  max-width: 100%;
}

.generate-preview {
  margin-top: 16px;
}

.generate-preview-title {
  margin-bottom: 8px;
  font-weight: 600;
}

.generate-preview pre {
  padding: 12px;
  border-radius: 4px;
  background: #f5f7fa;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 320px;
  max-width: 100%;
  overflow: auto;
}
</style>
