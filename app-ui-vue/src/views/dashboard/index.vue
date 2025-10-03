<template>
  <div class="dashboard-container">
    <div class="dashboard-card">
      <el-tabs v-model="activeTab" class="dashboard-tabs">
        <el-tab-pane label="表单配置" name="config">
          <div class="tab-content">
            <el-input v-model="val" type="number" class="dashboard-input" />
            <el-button class="dashboard-button" @click="click()">test</el-button>
            <el-button class="dashboard-button" @click="simpleTest()">自我测试</el-button>
            <span class="divider">分割线</span>
            <FormConfigEditor ref="formConfigEditor" :value.sync="config" class="form-config-editor" />
            <el-button class="dashboard-button primary" @click="previewForm()">预览表单</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="表单预览" name="preview">
          <div class="tab-content">
            <el-button class="dashboard-button" @click="activeTab = 'config'">返回配置</el-button>
            <DynamicForm
              v-if="previewConfig"
              ref="dynamicForm"
              :config="previewConfig"
              :value="formData"
              class="dynamic-form"
              @submit="handleFormSubmit"
              @reset="handleFormReset"
            />
            <el-button v-if="previewConfig" class="dashboard-button primary" @click="collectFormData">获取表单数据</el-button>
            <pre v-if="collectedData" class="data-preview">{{ collectedData }}</pre>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import request from '@/utils/request'
import { getInfo } from '@/api/user'
import Index from '@/components/FormEditor/index.vue'
import DynamicForm from '@/components/DynamicForm/index.vue'

export default {
  name: 'Dashboard',
  components: {
    FormConfigEditor: Index,
    DynamicForm
  },
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  data() {
    return {
      val: 0,
      config: {},
      activeTab: 'config',
      previewConfig: null,
      formData: {},
      collectedData: null
    }
  },
  methods: {
    previewForm() {
      // 通过 ref 调用子组件的 getData 方法获取数据
      const data = this.$refs.formConfigEditor.getData()
      this.previewConfig = data
      this.activeTab = 'preview'
    },

    handleFormSubmit(data) {
      this.$message.success('表单提交成功')
      console.log('表单提交数据:', data)
    },

    handleFormReset() {
      this.$message.info('表单已重置')
      this.formData = {}
      this.collectedData = null
    },

    collectFormData() {
      if (this.$refs.dynamicForm) {
        const data = this.$refs.dynamicForm.getFormData()
        this.collectedData = JSON.stringify(data, null, 2)
        console.log('表单当前数据:', data)
      }
    },

    simpleTest() {
      getInfo().then(res => {
        this.$message.success(res.data)
      })
    },

    click() {
      request('private/test', {
        method: 'get',
        params: {
          value: this.val
        }
      }).then(res => {
        this.$message.success(res)
      }).catch(err => {
        debugger
        console.error(err)
        this.$message.error(err.message)
      })
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}

.dashboard-card {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.3);
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.dashboard-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tab-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.dashboard-input {
  margin-bottom: 15px;
}

.dashboard-button {
  margin: 0 10px 15px 0;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(5px);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

  &:hover {
    background: rgba(64, 158, 255, 0.1);
    border-color: #409eff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &.primary {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.8), rgba(64, 158, 255, 0.6));
    border: none;
    color: white;

    &:hover {
      background: linear-gradient(135deg, rgba(64, 158, 255, 1), rgba(64, 158, 255, 0.8));
      box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
    }
  }
}

.divider {
  display: block;
  margin: 20px 0;
  font-weight: bold;
  color: #606266;
}

.form-config-editor, .dynamic-form {
  margin: 15px 0;
}

.data-preview {
  background: rgba(245, 247, 250, 0.7);
  padding: 15px;
  border-radius: 10px;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-top: 15px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

::v-deep .el-tabs__content {
  flex: 1;
  overflow: hidden;
}

::v-deep .el-tab-pane {
  height: 100%;
  overflow: hidden;
}
</style>
