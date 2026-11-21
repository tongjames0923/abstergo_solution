<template>
  <div class="json-edit-panel">
    <div class="json-toolbar">
      <span class="json-label">{{ $t('jsonMode.label') }}</span>
      <span v-if="value" :class="['json-status', parsed.ok ? 'is-valid' : 'is-invalid']">
        {{ parsed.ok ? $t('jsonMode.valid') : $t('jsonMode.invalid') }}
      </span>
      <div class="json-actions">
        <el-button size="mini" icon="el-icon-download" @click="$emit('fill')">
          {{ $t('jsonMode.fillCurrent') }}
        </el-button>
        <el-button size="mini" icon="el-icon-refresh" @click="formatJson">
          {{ $t('jsonMode.format') }}
        </el-button>
        <el-button type="primary" size="mini" icon="el-icon-check" @click="applyJson">
          {{ $t('jsonMode.apply') }}
        </el-button>
      </div>
    </div>

    <el-input
      :value="value"
      type="textarea"
      :rows="12"
      class="json-input"
      :placeholder="$t('jsonMode.placeholder')"
      @input="handleInput"
    />

    <p v-if="showError" class="json-error">{{ $t('jsonMode.invalidTip', { error: parsed.error }) }}</p>

    <details class="json-help" open>
      <summary class="json-help-summary">{{ $t('jsonMode.hintsTitle') }}</summary>
      <ul class="json-hints">
        <li v-for="(hint, index) in hints" :key="index">{{ hint }}</li>
      </ul>
      <div class="json-example">
        <span class="json-example-title">{{ $t('jsonMode.exampleTitle') }}</span>
        <pre>{{ example }}</pre>
      </div>
    </details>
  </div>
</template>

<script>
/**
 * JSON 输入面板。
 * <p>
 * 提供带格式校验的粘贴入口，通过 apply 事件把解析结果交给页面转换为图形编辑态；
 * fill 事件由页面用当前图形配置回填文本，作为可直接修改的编写样例。
 * </p>
 */
export default {
  name: 'JsonEditPanel',
  props: {
    value: {
      type: String,
      default: ''
    },
    hints: {
      type: Array,
      default: () => []
    },
    example: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      attempted: false
    }
  },
  computed: {
    /**
     * 当前文本的解析结果
     *
     * @returns {Object} {ok, data, error}
     */
    parsed() {
      try {
        return { ok: true, data: JSON.parse(this.value), error: '' }
      } catch (e) {
        return { ok: false, data: null, error: e.message }
      }
    },
    /**
     * 是否在尝试应用后展示解析错误
     *
     * @returns {boolean} 是否展示
     */
    showError() {
      return this.attempted && !this.parsed.ok
    }
  },
  methods: {
    /**
     * 文本内容变化
     *
     * @param {string} value 新文本
     */
    handleInput(value) {
      this.attempted = false
      this.$emit('input', value)
    },
    /**
     * 美化当前 JSON 文本
     */
    formatJson() {
      if (!this.parsed.ok) {
        this.attempted = true
        return
      }
      this.$emit('input', JSON.stringify(this.parsed.data, null, 2))
    },
    /**
     * 把解析结果交给页面回填图形编辑器
     */
    applyJson() {
      if (!this.parsed.ok) {
        this.attempted = true
        return
      }
      this.attempted = false
      this.$emit('apply', this.parsed.data)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.json-edit-panel {
  max-width: 100%;
}

.json-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.json-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.json-status {
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;

  &.is-valid {
    background: #f0f9eb;
    color: #67c23a;
  }

  &.is-invalid {
    background: #fef0f0;
    color: #f56c6c;
  }
}

.json-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-left: auto;
}

.json-input ::v-deep .el-textarea__inner {
  font-family: Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
}

.json-error {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #f56c6c;
  word-break: break-word;
}

.json-help {
  margin-top: 10px;
  padding: 8px 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.json-help-summary {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  cursor: pointer;
}

.json-hints {
  margin: 8px 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.7;
  color: #606266;

  li {
    margin-bottom: 4px;
  }
}

.json-example-title {
  font-size: 12px;
  color: #909399;
}

.json-example pre {
  margin: 6px 0 0;
  padding: 8px 10px;
  max-width: 100%;
  max-height: 200px;
  overflow: auto;
  font-family: Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #303133;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
