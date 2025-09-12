<template>
  <div class="select-options-editor">
    <el-button type="primary" size="mini" icon="el-icon-plus" @click="addOption">添加选项</el-button>

    <ul class="options-list">
      <li v-for="(option, index) in localOptions" :key="index" class="option-item">
        <el-row :gutter="10" align="middle">
          <el-col :span="10">
            <el-input
              v-model="option.label"
              size="small"
              placeholder="选项标签"
              @input="onOptionsChange"
            />
          </el-col>
          <el-col :span="10">
            <el-input
              v-model="option.value"
              size="small"
              placeholder="选项值"
              @input="onOptionsChange"
            />
          </el-col>
          <el-col :span="4">
            <el-button
              type="danger"
              size="mini"
              icon="el-icon-delete"
              @click="removeOption(index)"
            />
          </el-col>
        </el-row>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'SelectOptionsEditor',
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localOptions: this.value ? JSON.parse(JSON.stringify(this.value)) : []
    }
  },
  watch: {
    value: {
      handler(newVal) {
        this.localOptions = newVal ? JSON.parse(JSON.stringify(newVal)) : []
      },
      deep: true
    }
  },
  methods: {
    addOption() {
      this.localOptions.push({
        label: '',
        value: ''
      })
      this.onOptionsChange()
    },

    removeOption(index) {
      this.localOptions.splice(index, 1)
      this.onOptionsChange()
    },

    onOptionsChange() {
      this.$emit('input', this.localOptions)
    }
  }
}
</script>

<style scoped>
.select-options-editor {
  padding: 10px 0;
}

.options-list {
  list-style: none;
  padding: 10px 0 0 0;
  margin: 0;
}

.option-item {
  margin-bottom: 10px;
  padding: 5px;
  border: 1px dashed #ebeef5;
  border-radius: 4px;
}
</style>
