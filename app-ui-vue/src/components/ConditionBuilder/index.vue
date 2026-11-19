<template>
  <div class="condition-builder">
    <el-row :gutter="12" class="cb-row">
      <el-col :xs="24" :sm="24" :md="10">
        <div class="cb-operand">
          <div class="cb-operand-head">
            <el-tag size="mini" type="primary" effect="plain">A</el-tag>
            <span>{{ $t('conditionLab.left') }}</span>
            <el-select
              v-if="!leftIsGetter"
              v-model="leftKind"
              size="mini"
              class="cb-kind-select"
            >
              <el-option
                v-for="kind in literalKinds"
                :key="kind.value"
                :label="kind.label"
                :value="kind.value"
              />
            </el-select>
            <el-tag v-else size="mini" type="info" effect="plain">
              {{ typeLabel(leftType) }}
            </el-tag>
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
            class="cb-op-select"
            @change="update('op', $event)"
          >
            <el-option-group
              v-for="group in operatorGroups"
              :key="group.key"
              :label="group.label"
            >
              <el-option
                v-for="op in group.options"
                :key="op.value"
                :label="op.label"
                :value="op.value"
              />
            </el-option-group>
          </el-select>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :md="10">
        <div class="cb-operand">
          <div class="cb-operand-head">
            <el-tag size="mini" type="warning" effect="plain">B</el-tag>
            <span>{{ $t('conditionLab.right') }}</span>
            <el-tag v-if="!isUnary" size="mini" type="info" effect="plain">
              {{ typeLabel(rightKind) }}
            </el-tag>
          </div>
          <div v-if="isUnary" class="cb-unary-tip">
            <i class="el-icon-info" />
            <span>{{ $t('conditionLab.unaryHint') }}</span>
          </div>
          <value-getter-operand
            v-else
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
      <span class="cb-hint">{{ operatorHint || $t('conditionLab.hint') }}</span>
      <el-tag size="mini" type="info" effect="plain" class="cb-preview">{{ previewText }}</el-tag>
    </div>
  </div>
</template>

<script>
import ValueGetterOperand from '@/components/ValueGetterOperand'
import {parseGetterOperand} from '@/utils/valueGetter'

const CONDITION_GETTER_ID = 'ConditionGetter'

/**
 * 一元运算符：只使用左操作数
 */
const UNARY_OPERATORS = ['isTrue', 'isFalse', 'isEmpty', 'isNotEmpty']

/**
 * 文本类运算符：右侧按文本匹配
 */
const TEXT_OPERATORS = ['contains', 'notContains', 'startsWith', 'endsWith']

/**
 * 运算符元信息：所属分组与适用的左操作数类型
 */
const OPERATOR_META = {
  eq: { group: 'compare', kinds: ['string', 'number', 'boolean', 'date'] },
  ne: { group: 'compare', kinds: ['string', 'number', 'boolean', 'date'] },
  gt: { group: 'compare', kinds: ['string', 'number', 'date'] },
  ge: { group: 'compare', kinds: ['string', 'number', 'date'] },
  lt: { group: 'compare', kinds: ['string', 'number', 'date'] },
  le: { group: 'compare', kinds: ['string', 'number', 'date'] },
  contains: { group: 'text', kinds: ['string'] },
  notContains: { group: 'text', kinds: ['string'] },
  startsWith: { group: 'text', kinds: ['string'] },
  endsWith: { group: 'text', kinds: ['string'] },
  isTrue: { group: 'judge', kinds: ['boolean', 'string'] },
  isFalse: { group: 'judge', kinds: ['boolean', 'string'] },
  isEmpty: { group: 'judge', kinds: ['string', 'number', 'boolean', 'date'] },
  isNotEmpty: { group: 'judge', kinds: ['string', 'number', 'boolean', 'date'] }
}

/**
 * 运算符分组顺序
 */
const OPERATOR_GROUPS = [
  { key: 'compare', labelKey: 'groupCompare', hintKey: 'opHintCompare' },
  { key: 'text', labelKey: 'groupText', hintKey: 'opHintText' },
  { key: 'judge', labelKey: 'groupJudge', hintKey: 'opHintJudge' }
]

/**
 * 后端枚举缺失时的兜底运算符列表
 */
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
 * 以三元组可视化编辑 ConditionGetter 的左操作数、运算符与右操作数。
 * 左操作数可声明字面值类型（文本/数字/布尔/日期），或由所选 getter 的返回类型自动推断；
 * 运算符按当前类型过滤并按「比较 / 文本 / 判定」分组；右操作数控件跟随左操作数类型，
 * 一元运算符下隐藏右侧并给出提示。对外通过 v-model 输出 {left, op, right} 三元字符串对象。
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
  data() {
    return {
      leftKind: 'string'
    }
  },
  computed: {
    /**
     * 左操作数字面值可选类型
     *
     * @returns {Array} 类型选项
     */
    literalKinds() {
      return [
        { value: 'string', label: this.$t('valueGetter.typeText') },
        { value: 'number', label: this.$t('valueGetter.typeNumber') },
        { value: 'boolean', label: this.$t('valueGetter.typeBoolean') },
        { value: 'date', label: this.$t('valueGetter.typeDate') }
      ]
    },
    /**
     * 左操作数是否为 getter 调用
     *
     * @returns {boolean} 是否为 getter 调用
     */
    leftIsGetter() {
      return !!parseGetterOperand(this.value.left)
    },
    /**
     * 左操作数类型：getter 调用按返回类型推断，字面值取用户选择
     *
     * @returns {string} string / number / boolean / date
     */
    leftType() {
      if (this.leftIsGetter) {
        return this.resolveGetterKind(this.value.left)
      }
      return this.leftKind
    },
    /**
     * 左操作数控件类型
     *
     * @returns {string} 控件类型
     */
    leftValueType() {
      return this.leftType === 'date' ? 'datetime' : this.leftType
    },
    /**
     * 右操作数类型：文本类运算符固定文本，其余跟随左操作数
     *
     * @returns {string} string / number / boolean / date
     */
    rightKind() {
      if (this.isUnary) {
        return 'string'
      }
      if (TEXT_OPERATORS.indexOf(this.value.op) >= 0) {
        return 'string'
      }
      return this.leftType
    },
    /**
     * 右操作数控件类型
     *
     * @returns {string} 控件类型
     */
    rightValueType() {
      return this.rightKind === 'date' ? 'datetime' : this.rightKind
    },
    /**
     * 当前运算符是否为一元运算符
     *
     * @returns {boolean} 是否一元
     */
    isUnary() {
      return UNARY_OPERATORS.indexOf(this.value.op) >= 0
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
     * 全部运算符列表，优先取后端枚举定义
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
     * 按左操作数类型过滤后的运算符
     *
     * @returns {Array} 可用运算符
     */
    availableOperators() {
      return this.operators.filter(op => {
        const meta = OPERATOR_META[op.value]
        return !meta || meta.kinds.indexOf(this.leftType) >= 0
      })
    },
    /**
     * 分组后的运算符，空分组不展示
     *
     * @returns {Array} 分组列表
     */
    operatorGroups() {
      return OPERATOR_GROUPS.map(group => ({
        key: group.key,
        label: this.$t('conditionLab.' + group.labelKey),
        options: this.availableOperators.filter(op => {
          const meta = OPERATOR_META[op.value]
          return meta && meta.group === group.key
        })
      })).filter(group => group.options.length)
    },
    /**
     * 当前运算符的说明文案
     *
     * @returns {string} 说明
     */
    operatorHint() {
      const meta = OPERATOR_META[this.value.op]
      if (!meta) {
        return ''
      }
      const group = OPERATOR_GROUPS.find(item => item.key === meta.group)
      return group ? this.$t('conditionLab.' + group.hintKey) : ''
    },
    /**
     * 当前运算符展示标签
     *
     * @returns {string} 运算符标签
     */
    operatorLabel() {
      const op = this.operators.find(item => item.value === this.value.op)
      return op ? op.label : '?'
    },
    /**
     * 条件表达式预览
     *
     * @returns {string} 预览文本
     */
    previewText() {
      return this.isUnary
        ? 'A ' + this.operatorLabel
        : 'A ' + this.operatorLabel + ' B'
    }
  },
  watch: {
    leftType() {
      this.ensureOperatorValid()
    },
    value: {
      immediate: true,
      handler() {
        this.ensureOperatorValid()
      }
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
    },

    /**
     * 当前运算符不适用于左操作数类型时，自动回退到第一个可用运算符
     */
    ensureOperatorValid() {
      if (!this.availableOperators.length) {
        return
      }
      if (this.availableOperators.some(op => op.value === this.value.op)) {
        return
      }
      this.update('op', this.availableOperators[0].value)
    },

    /**
     * 由 getter 调用推断返回值类型
     *
     * @param {string} raw 操作数原始字符串
     * @returns {string} string / number / boolean / date
     */
    resolveGetterKind(raw) {
      const parsed = parseGetterOperand(raw)
      if (!parsed) {
        return 'string'
      }
      const getter = this.getters.find(g => g.getterId === parsed.getterId)
      const type = (getter && getter.resultType && getter.resultType.type || '').toLowerCase()
      if (type.startsWith('bool')) {
        return 'boolean'
      }
      if (['int', 'integer', 'long', 'float', 'double', 'number', 'decimal'].some(p => type.startsWith(p))) {
        return 'number'
      }
      if (type.startsWith('date') || type.startsWith('time')) {
        return 'date'
      }
      return 'string'
    },

    /**
     * 操作数类型文案
     *
     * @param {string} kind 类型标识
     * @returns {string} 类型文案
     */
    typeLabel(kind) {
      const map = {
        string: 'typeText',
        number: 'typeNumber',
        boolean: 'typeBoolean',
        date: 'typeDate'
      }
      return this.$t('valueGetter.' + (map[kind] || 'typeText'))
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
    padding: 10px 12px;
    background: #fafafa;
    border: 1px solid #ebeef5;
    border-radius: 4px;
  }

  .cb-operand-head {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 600;
    color: #606266;
  }

  .cb-kind-select {
    width: 96px;
    margin-left: auto;
  }

  .cb-operator {
    display: flex;
    flex-direction: column;
    gap: 8px;
    justify-content: center;

    .cb-op-select {
      width: 100%;
    }
  }

  .cb-operator-label {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    text-align: center;
  }

  .cb-unary-tip {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    padding: 8px 10px;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    background: #f5f7fa;
    font-size: 12px;
    line-height: 1.5;
    color: #909399;

    i {
      margin-top: 2px;
    }
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
