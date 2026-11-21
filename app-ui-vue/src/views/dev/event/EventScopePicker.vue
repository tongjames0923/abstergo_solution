<template>
  <div class="scope-picker">
    <el-form :inline="true" size="small" class="scope-form" @submit.native.prevent>
      <el-form-item :label="$t('eventCommon.reporter')" required>
        <el-select
          :value="reporterId"
          filterable
          clearable
          class="scope-select"
          :placeholder="$t('eventCommon.reporterPlaceholder')"
          @change="handleReporterChange"
        >
          <el-option
            v-for="item in reporterOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('eventCommon.event')" required>
        <el-select
          :value="eventCode"
          filterable
          clearable
          class="scope-select"
          :placeholder="$t('eventCommon.eventPlaceholder')"
          @change="handleEventChange"
        >
          <el-option
            v-for="item in eventOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <div class="scope-params">
      <span class="params-label">{{ $t('eventCommon.eventParams') }}</span>
      <template v-if="paramTypes.length">
        <el-tag
          v-for="param in paramTypes"
          :key="param.name"
          size="mini"
          effect="plain"
          :type="param.required ? 'warning' : 'info'"
        >
          {{ param.label || param.name }}
          <span class="param-name">{{ param.name }}</span>
        </el-tag>
      </template>
      <span v-else class="params-empty">{{ $t('eventCommon.noParams') }}</span>
    </div>
  </div>
</template>

<script>
/**
 * 事件范围选择器。
 * <p>
 * 负责「上报器 → 事件」两级选择，并以只读标签展示事件参数声明，
 * 让编排者在配置触发器与动作前明确该事件会携带哪些载荷。
 * </p>
 */
export default {
  name: 'EventScopePicker',
  props: {
    reporters: {
      type: Array,
      default: () => []
    },
    events: {
      type: Array,
      default: () => []
    },
    reporterId: {
      type: String,
      default: ''
    },
    eventCode: {
      type: String,
      default: ''
    }
  },
  computed: {
    /**
     * 上报器下拉选项（兼容 id/name 与 reporterId/reporterName 两种字段）
     *
     * @returns {Array} 下拉选项
     */
    reporterOptions() {
      return (this.reporters || []).map(item => ({
        value: item.reporterId || item.id || '',
        label: item.reporterName || item.name || item.reporterId || item.id || ''
      })).filter(item => item.value)
    },
    /**
     * 事件下拉选项（兼容 code/label 与 eventCode/eventName 两种字段）
     *
     * @returns {Array} 下拉选项
     */
    eventOptions() {
      return (this.events || []).map(item => {
        const code = item.code || item.eventCode || ''
        return {
          value: code,
          code,
          label: item.label || item.name || item.eventName || code,
          paramTypes: Array.isArray(item.paramTypes) ? item.paramTypes : []
        }
      }).filter(item => item.value)
    },
    /**
     * 当前事件的参数声明
     *
     * @returns {Array} 参数定义列表
     */
    paramTypes() {
      const matched = this.eventOptions.find(item => item.value === this.eventCode)
      return matched && Array.isArray(matched.paramTypes) ? matched.paramTypes : []
    }
  },
  methods: {
    /**
     * 切换上报器
     *
     * @param {string} value 上报器ID
     */
    handleReporterChange(value) {
      this.$emit('update:reporterId', value || '')
      this.$emit('update:eventCode', '')
      this.$emit('change', { reporterId: value || '', eventCode: '' })
    },
    /**
     * 切换事件
     *
     * @param {string} value 事件编码
     */
    handleEventChange(value) {
      this.$emit('update:eventCode', value || '')
      this.$emit('change', { reporterId: this.reporterId, eventCode: value || '' })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.scope-picker {
  @include responsive-container;
}

.scope-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 8px;
}

.scope-select {
  width: 220px;
}

.scope-params {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin: 2px 0 12px;
  padding: 8px 10px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.params-label {
  font-size: 12px;
  color: #606266;
}

.param-name {
  margin-left: 4px;
  font-family: Menlo, Consolas, monospace;
  font-size: 11px;
  opacity: 0.7;
}

.params-empty {
  font-size: 12px;
  color: #909399;
}

@include respond-to(phone) {
  .scope-select {
    width: 100%;
  }
}
</style>
