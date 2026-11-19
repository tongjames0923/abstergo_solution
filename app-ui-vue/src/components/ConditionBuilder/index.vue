<template>
  <div class="condition-builder">
    <el-row :gutter="12" class="cb-row">
      <el-col :xs="24" :sm="24" :md="10">
        <div class="cb-operand">
          <div class="cb-operand-head">
            <el-tag size="mini" type="primary" effect="plain">A</el-tag>
            <span>{{ $t('conditionLab.left') }}</span>
          </div>
          <value-getter-operand
            :value="value.left"
            :getters="getters"
            :depth="depth + 1"
            :value-type="leftValueType"
            @input="update('left', $event)"
          />
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :md="4">
        <div class="cb-operator">
          <div class="cb-operator-label">{{ $t('conditionLab.op') }}</div>
          <el-select
            :value="value.op"
            :placeholder="$t('conditionLab.selectOp')"
            size="small"
            @change="update('op', $event)"
          >
            <el-option v-for="op in operators" :key="op.value" :label="op.label" :value="op.value" />
          </el-select>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :md="10">
        <div class="cb-operand">
          <div class="cb-operand-head">
            <el-tag size="mini" type="warning" effect="plain">B</el-tag>
            <span>{{ $t('conditionLab.right') }}</span>
          </div>
          <value-getter-operand
            :value="value.right"
            :getters="getters"
            :depth="depth + 1"
            :value-type="rightValueType"
            @input="update('right', $event)"
          />
        </div>
      </el-col>
    </el-row>

    <div class="cb-foot">
      <span class="cb-hint">{{ $t('conditionLab.hint') }}</span>
      <el-tag size="mini" type="info" effect="plain" class="cb-preview">A {{ previewOp }} B</el-tag>
    </div>
  </div>
</template>

<script>
import ValueGetterOperand from '@/components/ValueGetterOperand'

const CONDITION_GETTER_ID = 'ConditionGetter'
const DEFAULT_OPERATORS = [
  { value: 'eq', label: '==' },
  { value: 'ne', label: '!=' },
  { value: 'gt', label: '>' },
  { value: 'ge', label: '>=' },
  { value: 'lt', label: '<' },
  { value: 'le', label: '<=' },
  { value: 'contains', label: 'contains' },
  { value: 'notContains', label: 'not contains' },
  { value: 'startsWith', label: 'starts with' },
  { value: 'endsWith', label: 'ends with' },
  { value: 'isTrue', label: 'is true' },
  { value: 'isFalse', label: 'is false' },
  { value: 'isEmpty', label: 'is empty' },
  { value: 'isNotEmpty', label: 'is not empty' }
]

/**
 * 布尔条件构建器控件。
 * <p>
 * 以三元组可视化编辑 ConditionGetter 的左操作数、运算符与右操作数；
 * 左右操作数委托给 ValueGetterOperand，从而支持 getter 调用与递归嵌套。
 * 对外通过 v-model 输出 {left, op, right} 三元字符串对象。
 * </p>
 */
export default {
  name: 'ConditionBuilder',
  components: { ValueGetterOperand },
  props: {
    value: {
      type: Object,
      default: () => ({ left: '', op: 'eq', right: '' })
    },
    getters: {
      type: Array,
      default: () => []
    },
    depth: {
      type: Number,
      default: 0
    }
  },
  computed: {
    /**
     * 大小比较运算符集合
     *
     * @returns {Array} 运算符值列表
     */
    orderOps() {
      return ['gt', 'ge', 'lt', 'le']
    },
    /**
     * 左操作数字面值类型
     *
     * @returns {string} 类型标识
     */
    leftValueType() {
      return 'string'
    },
    /**
     * 右操作数字面值类型：大小比较时为数字
     *
     * @returns {string} 类型标识
     */
    rightValueType() {
      return this.orderOps.indexOf(this.value.op) >= 0 ? 'number' : 'string'
    },
    /**
     * 条件 getter 的元信息
     *
     * @returns {Object|null} 元信息
     */
    conditionMeta() {
      return this.getters.find(g => g.getterId === CONDITION_GETTER_ID) || null
    },
    /**
     * 可选运算符列表，优先取后端枚举定义
     *
     * @returns {Array} 运算符列表
     */
    operators() {
      if (!this.conditionMeta || !Array.isArray(this.conditionMeta.paramTypes)) {
        return DEFAULT_OPERATORS
      }
      const opField = this.conditionMeta.paramTypes.find(p => p && p.isEnum && Array.isArray(p.enumValues))
      return opField ? opField.enumValues : DEFAULT_OPERATORS
    },
    /**
     * 预览用的运算符展示
     *
     * @returns {string} 运算符标签
     */
    previewOp() {
      const op = this.operators.find(o => o.value === this.value.op)
      return op ? op.label : '?'
    }
  },
  methods: {
    /**
     * 更新三元组字段并对外发出
     *
     * @param {string} key 字段名：left / op / right
     * @param {string} val 新值
     */
    update(key, val) {
      this.$emit('input', Object.assign({}, this.value, { [key]: val }))
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.condition-builder {
  @include responsive-container;

  .cb-row {
    align-items: stretch;
  }

  .cb-operand,
  .cb-operator {
    height: 100%;
    padding: 12px;
    background: #fafafa;
    border: 1px solid #ebeef5;
    border-radius: 4px;
  }

  .cb-operand-head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
    font-size: 13px;
    font-weight: 600;
    color: #606266;
  }

  .cb-operator {
    display: flex;
    flex-direction: column;
    gap: 8px;
    justify-content: center;

    .el-select {
      width: 100%;
    }
  }

  .cb-operator-label {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    text-align: center;
  }

  .cb-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 10px;

    @include respond-to(phone) {
      flex-direction: column;
      align-items: flex-start;
    }
  }

  .cb-hint {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
  }

  .cb-preview {
    font-family: Menlo, Consolas, monospace;
    white-space: nowrap;
  }
}
</style>
