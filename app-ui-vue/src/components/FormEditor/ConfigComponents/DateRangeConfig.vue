<template>
  <div class="date-range-config">
    <el-alert
      title="日期范围配置说明"
      description="配置日期范围选择器的属性"
      type="info"
      show-icon
      style="margin-bottom: 15px;"
    />

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12">
        <el-form-item label="开始日期占位符">
          <el-input
            :value="value && value['start-placeholder']"
            placeholder="开始日期"
            @input="updateConfig('start-placeholder', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="结束日期占位符">
          <el-input
            :value="value && value['end-placeholder']"
            placeholder="结束日期"
            @input="updateConfig('end-placeholder', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="日期格式">
          <el-input
            :value="value && value.format"
            placeholder="yyyy-MM-dd"
            @input="updateConfig('format', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="值格式">
          <el-input
            :value="value && value['value-format']"
            placeholder="yyyy-MM-dd"
            @input="updateConfig('value-format', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="选择类型">
          <el-select
            :value="value && value.type"
            placeholder="请选择日期类型"
            style="width: 100%;"
            @change="updateConfig('type', $event)"
          >
            <el-option label="日期 (date)" value="date" />
            <el-option label="周 (week)" value="week" />
            <el-option label="月 (month)" value="month" />
            <el-option label="年 (year)" value="year" />
            <el-option label="日期时间 (datetime)" value="datetime" />
            <el-option label="日期时间范围 (datetimerange)" value="datetimerange" />
            <el-option label="日期范围 (daterange)" value="daterange" />
            <el-option label="月范围 (monthrange)" value="monthrange" />
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
            <el-option label="默认 (default)" value="small" />
            <el-option label="小 (small)" value="mini" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="范围分隔符">
          <el-input
            :value="value && value['range-separator']"
            placeholder="-"
            @input="updateConfig('range-separator', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="清除按钮">
          <el-switch
            :value="value && value.clearable"
            @change="updateConfig('clearable', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="只读">
          <el-switch
            :value="value && value.readonly"
            @change="updateConfig('readonly', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="禁用">
          <el-switch
            :value="value && value.disabled"
            @change="updateConfig('disabled', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="可编辑">
          <el-switch
            :value="value && value.editable"
            @change="updateConfig('editable', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="显示箭头">
          <el-switch
            :value="value && value['arrow-control']"
            @change="updateConfig('arrow-control', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="默认时间">
          <el-input
            :value="value && value['default-time']"
            placeholder="00:00:00"
            @input="updateConfig('default-time', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="确认按钮">
          <el-switch
            :value="value && value['confirm-button']"
            @change="updateConfig('confirm-button', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="显示格式化">
          <el-input
            :value="value && value['format-display']"
            placeholder="yyyy年MM月dd日"
            @input="updateConfig('format-display', $event)"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="弹出定位">
          <el-select
            :value="value && value['placement']"
            placeholder="请选择弹出位置"
            style="width: 100%;"
            @change="updateConfig('placement', $event)"
          >
            <el-option label="顶部 (top)" value="top" />
            <el-option label="顶部开始 (top-start)" value="top-start" />
            <el-option label="顶部结束 (top-end)" value="top-end" />
            <el-option label="底部 (bottom)" value="bottom" />
            <el-option label="底部开始 (bottom-start)" value="bottom-start" />
            <el-option label="底部结束 (bottom-end)" value="bottom-end" />
            <el-option label="左侧 (left)" value="left" />
            <el-option label="左侧开始 (left-start)" value="left-start" />
            <el-option label="左侧结束 (left-end)" value="left-end" />
            <el-option label="右侧 (right)" value="right" />
            <el-option label="右侧开始 (right-start)" value="right-start" />
            <el-option label="右侧结束 (right-end)" value="right-end" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-form-item label="弹出类名">
          <el-input
            :value="value && value['popper-class']"
            placeholder="自定义弹出层类名"
            @input="updateConfig('popper-class', $event)"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'DateRangeConfig',
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
.date-range-config {
  padding: 10px 0;
}
</style>
