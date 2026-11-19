<template>
  <div class="getter-result">
    <div v-if="empty" class="result-empty">
      <i class="el-icon-document" />
      <span>{{ $t('getterResult.empty') }}</span>
    </div>

    <template v-else>
      <div v-if="kind === 'boolean'" :class="['result-boolean', truthy ? 'is-true' : 'is-false']">
        <i :class="truthy ? 'el-icon-success' : 'el-icon-error'" class="bool-icon" />
        <div class="bool-body">
          <div class="bool-value">{{ displayValue }}</div>
          <div class="bool-desc">{{ truthy ? $t('getterResult.true') : $t('getterResult.false') }}</div>
        </div>
      </div>

      <div v-else-if="kind === 'number'" class="result-number">
        <span class="number-value">{{ displayValue }}</span>
      </div>

      <div v-else class="result-text">
        <i class="el-icon-document-checked text-icon" />
        <span class="text-value">{{ displayValue }}</span>
      </div>

      <div class="result-meta">
        <el-tag size="mini" type="info" effect="plain">{{ typeName }}</el-tag>
        <el-tag v-if="arrayValue" size="mini" type="warning" effect="plain">
          {{ $t('getterResult.array') }} · {{ arrayValue.length }}
        </el-tag>
        <span v-if="elapsedMs !== null" class="elapsed">{{ elapsedMs }} ms</span>
      </div>
    </template>
  </div>
</template>

<script>
/**
 * 取值结果展示控件。
 * <p>
 * 依据后端 ResultType 声明的类型对 ResultValue 做语义化展示：布尔显示状态图标与判定文案，
 * 数值突出显示，字符串按文本展示；原始返回体由调用方切换查看。
 * </p>
 */
export default {
  name: 'GetterResult',
  props: {
    result: {
      type: Object,
      default: null
    },
    elapsedMs: {
      type: Number,
      default: null
    }
  },
  computed: {
    /**
     * 结果类型标识（string / boolean / number ...）
     *
     * @returns {string} 类型标识
     */
    kind() {
      if (!this.result || !this.result.type) {
        return 'string'
      }
      const type = this.result.type.type
      if (!type) {
        return 'string'
      }
      const lower = String(type).toLowerCase()
      if (lower.startsWith('bool')) {
        return 'boolean'
      }
      if (['int', 'integer', 'long', 'float', 'double', 'number', 'decimal'].some(p => lower.startsWith(p))) {
        return 'number'
      }
      return 'string'
    },
    /**
     * 类型展示名称
     *
     * @returns {string} 类型名
     */
    typeName() {
      if (!this.result || !this.result.type) {
        return this.$t('getterResult.typeUnknown')
      }
      return this.result.type.name || this.result.type.type || this.$t('getterResult.typeUnknown')
    },
    /**
     * 是否为空结果
     *
     * @returns {boolean} 是否为空
     */
    empty() {
      if (!this.result) {
        return true
      }
      return this.displayValue === ''
    },
    /**
     * 展示值
     *
     * @returns {string} 字符串形式的值
     */
    displayValue() {
      if (!this.result) {
        return ''
      }
      if (this.result.value !== null && this.result.value !== undefined) {
        return String(this.result.value)
      }
      if (this.arrayValue && this.arrayValue.length) {
        return this.arrayValue.map(v => String(v)).join(', ')
      }
      return ''
    },
    /**
     * 集合值
     *
     * @returns {Array|null} 值集合
     */
    arrayValue() {
      return this.result && Array.isArray(this.result.values) ? this.result.values : null
    },
    /**
     * 布尔结果是否为真
     *
     * @returns {boolean} 是否为真
     */
    truthy() {
      const v = this.displayValue.trim().toLowerCase()
      return v === 'true' || v === '1'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.getter-result {
  @include responsive-container;

  .result-empty {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 0;
    font-size: 13px;
    color: #909399;

    i {
      font-size: 15px;
    }
  }

  .result-boolean {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 14px;
    border-radius: 4px;
    border: 1px solid;

    &.is-true {
      background: #f0f9eb;
      border-color: #e1f3d8;
      color: #67c23a;
    }

    &.is-false {
      background: #fef0f0;
      border-color: #fde2e2;
      color: #f56c6c;
    }

    .bool-icon {
      font-size: 24px;
    }

    .bool-value {
      font-size: 18px;
      font-weight: 600;
      line-height: 1.2;
    }

    .bool-desc {
      margin-top: 2px;
      font-size: 12px;
      opacity: 0.85;
    }
  }

  .result-number {
    padding: 12px 14px;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 4px;

    .number-value {
      font-size: 22px;
      font-weight: 600;
      color: $menuActiveText;
      font-family: Menlo, Consolas, monospace;
      word-break: break-all;
    }
  }

  .result-text {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 12px 14px;
    background: #f5f7fa;
    border: 1px solid #ebeef5;
    border-radius: 4px;

    .text-icon {
      margin-top: 2px;
      color: $menuActiveText;
      font-size: 15px;
    }

    .text-value {
      flex: 1;
      min-width: 0;
      font-size: 14px;
      line-height: 1.6;
      color: #303133;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }

  .result-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 10px;

    .elapsed {
      margin-left: auto;
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
