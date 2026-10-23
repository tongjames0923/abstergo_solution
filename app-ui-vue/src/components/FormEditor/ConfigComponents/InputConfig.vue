<template>
  <div class="input-config">
    <el-alert
      title="输入框配置说明"
      description="配置输入框的各种属性"
      type="info"
      show-icon
      style="margin-bottom: 15px;"
    />

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12">
        <el-form-item label="输入框类型">
          <el-select
            :value="value && value.type"
            placeholder="请选择输入框类型"
            style="width: 100%;"
            @change="updateConfig('type', $event)"
          >
            <el-option label="文本 (text)" value="text" />
            <el-option label="密码 (password)" value="password" />
            <el-option label="数字 (number)" value="number" />
            <el-option label="邮箱 (email)" value="email" />
            <el-option label="网址 (url)" value="url" />
            <el-option label="电话 (tel)" value="tel" />
            <el-option label="搜索 (search)" value="search" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="尺寸">
          <el-select
            :value="value && value.size"
            placeholder="请选择尺寸"
            style="width: 100%;"
            @change="updateConfig('size', $event)"
          >
            <el-option label="大 (large)" value="large" />
            <el-option label="默认 (default)" value="default" />
            <el-option label="小 (small)" value="small" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="占位符">
          <el-input
            :value="value && value.placeholder"
            placeholder="请输入占位符"
            @input="updateConfig('placeholder', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="最大长度">
          <el-input-number
            :value="value && value.maxlength"
            :min="0"
            controls-position="right"
            style="width: 100%;"
            @change="updateConfig('maxlength', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="最小长度">
          <el-input-number
            :value="value && value.minlength"
            :min="0"
            controls-position="right"
            style="width: 100%;"
            @change="updateConfig('minlength', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="是否禁用">
          <el-switch
            :value="value && value.disabled"
            @change="updateConfig('disabled', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="是否只读">
          <el-switch
            :value="value && value.readonly"
            @change="updateConfig('readonly', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="是否可清除">
          <el-switch
            :value="value && value.clearable"
            @change="updateConfig('clearable', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="是否显示密码切换">
          <el-switch
            :value="value && value['show-password']"
            @change="updateConfig('show-password', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="是否显示字数统计">
          <el-switch
            :value="value && value['show-word-limit']"
            @change="updateConfig('show-word-limit', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="自动补全">
          <el-select
            :value="value && value.autocomplete"
            placeholder="请选择自动补全"
            style="width: 100%;"
            @change="updateConfig('autocomplete', $event)"
          >
            <el-option label="开启 (on)" value="on" />
            <el-option label="关闭 (off)" value="off" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="前缀图标">
          <el-input
            :value="value && value.prefix"
            placeholder="例如: el-icon-search"
            @input="updateConfig('prefix', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="后缀图标">
          <el-input
            :value="value && value.suffix"
            placeholder="例如: el-icon-date"
            @input="updateConfig('suffix', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="前置文本">
          <el-input
            :value="value && value['prefix-label']"
            placeholder="例如: http://"
            @input="updateConfig('prefix-label', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="后置文本">
          <el-input
            :value="value && value['suffix-label']"
            placeholder="例如: .com"
            @input="updateConfig('suffix-label', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="自定义样式">
          <el-input
            :value="value && value.style"
            placeholder="CSS样式，例如: width: 200px;"
            @input="updateConfig('style', $event)"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'InputConfig',
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  methods: {
    updateConfig(key, value) {
      const newConfig = { ...this.value, [key]: value }
      this.$emit('input', newConfig)
    }
  }
}
</script>

<style scoped>
.input-config {
  padding: 10px 0;
}
</style>
