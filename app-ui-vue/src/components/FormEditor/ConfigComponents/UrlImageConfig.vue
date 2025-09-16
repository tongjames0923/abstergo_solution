<template>
  <div class="url-image-config">
    <el-alert
      title="图片URL配置说明"
      description="配置图片URL显示组件的属性"
      type="info"
      show-icon
      style="margin-bottom: 15px;"
    />

    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="图片宽度">
          <el-input
            :value="value && value.width"
            placeholder="例如: 100px 或 100%"
            @input="updateConfig('width', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="图片高度">
          <el-input
            :value="value && value.height"
            placeholder="例如: 100px 或 auto"
            @input="updateConfig('height', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="适应方式">
          <el-select
            :value="value && value.fit"
            placeholder="请选择适应方式"
            style="width: 100%;"
            @change="updateConfig('fit', $event)"
          >
            <el-option label="填充 (fill)" value="fill" />
            <el-option label="包含 (contain)" value="contain" />
            <el-option label="覆盖 (cover)" value="cover" />
            <el-option label="无缩放 (none)" value="none" />
            <el-option label="缩放 (scale-down)" value="scale-down" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="备用文本">
          <el-input
            :value="value && value.alt"
            placeholder="图片加载失败时显示的文本"
            @input="updateConfig('alt', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="预览功能">
          <el-switch
            :value="value && value.preview"
            @change="updateConfig('preview', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="懒加载">
          <el-switch
            :value="value && value.lazy"
            @change="updateConfig('lazy', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="是否隐藏">
          <el-switch
            :value="value && value.hide"
            @change="updateConfig('hide', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="是否阻塞加载">
          <el-switch
            :value="value && value['load-before-show']"
            @change="updateConfig('load-before-show', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="加载中占位">
          <el-input
            :value="value && value['loading-src']"
            placeholder="加载中显示的图片URL"
            @input="updateConfig('loading-src', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="错误占位">
          <el-input
            :value="value && value['error-src']"
            placeholder="错误时显示的图片URL"
            @input="updateConfig('error-src', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="图片标题">
          <el-input
            :value="value && value.title"
            placeholder="图片标题"
            @input="updateConfig('title', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="是否可拖拽">
          <el-switch
            :value="value && value.draggable"
            @change="updateConfig('draggable', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="是否显示工具栏">
          <el-switch
            :value="value && value['show-toolbar']"
            @change="updateConfig('show-toolbar', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="是否显示标题">
          <el-switch
            :value="value && value['show-title']"
            @change="updateConfig('show-title', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="图片样式">
          <el-input
            :value="value && value.style"
            placeholder="CSS样式，例如: border-radius: 4px;"
            @input="updateConfig('style', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="自定义类名">
          <el-input
            :value="value && value['class-name']"
            placeholder="自定义CSS类名"
            @input="updateConfig('class-name', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="点击事件处理">
          <el-select
            :value="value && value['click-handler']"
            placeholder="请选择点击事件处理方式"
            style="width: 100%;"
            @change="updateConfig('click-handler', $event)"
          >
            <el-option label="无操作" value="" />
            <el-option label="预览图片" value="preview" />
            <el-option label="打开链接" value="open-url" />
            <el-option label="下载图片" value="download" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'UrlImageConfig',
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
.url-image-config {
  padding: 10px 0;
}
</style>
