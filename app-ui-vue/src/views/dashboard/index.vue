<template>
  <div class="dashboard-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="表单配置" name="config">
        <el-input v-model="val" type="number" />
        <el-button @click="click()">test</el-button>
        <el-button @click="simpleTest()">自我测试</el-button>
        <span>分割线</span>
        <FormConfigEditor ref="formConfigEditor" :value.sync="config" />
        <el-button @click="previewForm()">预览表单</el-button>
      </el-tab-pane>

      <el-tab-pane label="表单预览" name="preview">
        <el-button @click="activeTab = 'config'">返回配置</el-button>
        <DynamicForm
          v-if="previewConfig"
          ref="dynamicForm"
          :config="previewConfig"
          :value="formData"
          @submit="handleFormSubmit"
          @reset="handleFormReset"
        />
        <el-button v-if="previewConfig" @click="collectFormData">获取表单数据</el-button>
        <pre v-if="collectedData">{{ collectedData }}</pre>
      </el-tab-pane>
    </el-tabs>
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
}

pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-top: 10px;
}
</style>
