<template>
  <div v-loading="loading" :element-loading-text="loadingText || $t('common.loading')" class="mobile-card-list">
    <!-- 空状态 -->
    <div v-if="!loading && (!data || data.length === 0)" class="mobile-card-list__empty">
      <i class="el-icon-document" />
      <p>{{ $t('common.noData') }}</p>
    </div>

    <div
      v-for="(row, index) in data"
      :key="row[idField] !== undefined ? row[idField] : index"
      class="mobile-card"
      :class="{ 'is-selected': selection && isSelected(row) }"
      @click="handleCardClick(row, index)"
    >
      <!-- 选择控件：多选场景下展示于卡片右上角 -->
      <div v-if="selection" class="mobile-card__check" @click.stop="toggleSelect(row)">
        <el-checkbox :value="isSelected(row)" @change="toggleSelect(row)" />
      </div>

      <!-- 标题字段：取第一个展示字段，视觉层级最高 -->
      <div class="mobile-card__title">
        <render
          v-if="titleColumn && titleColumn.render"
          :render="titleColumn.render"
          :params="{ row, $index: index, column: titleColumn }"
        />
        <slot
          v-else-if="titleColumn && titleColumn.slot"
          :name="titleColumn.slot"
          :row="row"
          :$index="index"
          :column="titleColumn"
        />
        <template v-else>{{ formatValue(row, titleColumn) || '-' }}</template>
      </div>

      <!-- 其余关键字段：label/value 两栏对齐 -->
      <div v-if="detailColumns.length" class="mobile-card__fields">
        <div v-for="field in detailColumns" :key="field.prop || field.label" class="mobile-card__field">
          <span class="mobile-card__label">{{ field.label }}</span>
          <span class="mobile-card__value">
            <render
              v-if="field.render"
              :render="field.render"
              :params="{ row, $index: index, column: field }"
            />
            <slot
              v-else-if="field.slot"
              :name="field.slot"
              :row="row"
              :$index="index"
              :column="field"
            />
            <template v-else>{{ formatValue(row, field) === '' || formatValue(row, field) === null || formatValue(row, field) === undefined ? '-' : formatValue(row, field) }}</template>
          </span>
        </div>
      </div>

      <!-- 操作区：插槽按钮 + actions 配置按钮，触摸目标 >= 44px -->
      <div v-if="hasActions" class="mobile-card__actions" @click.stop>
        <slot name="actions" :row="row" :$index="index" />
        <template v-if="actions && Array.isArray(actions)">
          <el-button
            v-for="(action, actionIndex) in actions"
            :key="actionIndex"
            :type="action.type || 'text'"
            :icon="action.icon"
            :disabled="checkDisabled(action, row, index)"
            :loading="checkLoading(action, row, index)"
            class="mobile-card__action-btn"
            @click.stop="handleAction(action, row, index)"
          >
            {{ action.label }}
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
// 函数式渲染组件：执行列配置中的 render 函数
const Render = {
  name: 'Render',
  functional: true,
  props: {
    render: Function,
    params: Object
  },
  render: (h, ctx) => {
    return ctx.props.render(h, ctx.props.params)
  }
}

export default {
  name: 'MobileCardList',
  components: {
    Render
  },
  props: {
    // 行数据
    data: {
      type: Array,
      default: () => []
    },
    // 列配置，与 DynamicTable columns 同构
    columns: {
      type: Array,
      default: () => []
    },
    // 卡片展示字段（prop 名称数组），不传则默认取前 maxFields 个有 label 的列
    cardFields: {
      type: Array,
      default: null
    },
    // 卡片标题字段（prop 名称），不传则取展示字段的第一列
    titleField: {
      type: String,
      default: ''
    },
    // 行唯一键字段名
    idField: {
      type: String,
      default: 'id'
    },
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 加载提示文本
    loadingText: {
      type: String,
      default: ''
    },
    // 是否启用多选
    selection: {
      type: Boolean,
      default: false
    },
    // 已选中行（由父组件受控）
    value: {
      type: Array,
      default: () => []
    },
    // 操作按钮配置，与 DynamicTable actions 同构
    actions: {
      type: Array,
      default: null
    },
    // 父组件是否提供了 actions 作用域插槽（v-slot 语法只进入 $scopedSlots，需由父组件显式告知）
    hasActionsSlot: {
      type: Boolean,
      default: false
    },
    // 默认展示的最大字段数（含标题字段）
    maxFields: {
      type: Number,
      default: 4
    }
  },
  computed: {
    /**
     * 参与卡片展示的列：仅保留有 label 且具备取值/渲染能力的列
     * @returns {Array} 列配置数组
     */
    displayColumns() {
      const columns = (this.columns || []).filter(column => {
        if (!column || !column.label) {
          return false
        }
        return !!column.prop || !!column.slot || !!column.render
      })
      if (this.cardFields && this.cardFields.length) {
        const ordered = []
        this.cardFields.forEach(prop => {
          const matched = columns.find(column => column.prop === prop)
          if (matched) {
            ordered.push(matched)
          }
        })
        return ordered.length ? ordered : columns
      }
      return columns
    },
    /**
     * 标题列：优先 titleField 指定，否则取第一个展示列
     * @returns {Object|null} 列配置
     */
    titleColumn() {
      if (this.titleField) {
        const matched = this.displayColumns.find(column => column.prop === this.titleField)
        if (matched) {
          return matched
        }
      }
      return this.displayColumns[0] || null
    },
    /**
     * 标题列之外的关键字段列
     * @returns {Array} 列配置数组
     */
    detailColumns() {
      return this.displayColumns
        .filter(column => column !== this.titleColumn)
        .slice(0, Math.max(this.maxFields - 1, 0))
    },
    /**
     * 是否存在可渲染的操作按钮（actions 配置数组或父组件提供的 actions 插槽）
     * 注意：v-slot 语法的插槽仅存在于 $scopedSlots，$slots 永远为空，
     * 故插槽存在性由父组件通过 hasActionsSlot prop 告知
     * @returns {Boolean} 结果
     */
    hasActions() {
      return (Array.isArray(this.actions) && this.actions.length > 0) || this.hasActionsSlot
    }
  },
  methods: {
    /**
     * 格式化字段显示值，与 DynamicTable 的 formatValue 语义一致
     * @param {Object} row - 行数据
     * @param {Object} column - 列配置
     * @returns {String|Number} 显示值
     */
    formatValue(row, column) {
      if (!column || !row) {
        return ''
      }
      const value = column.prop ? row[column.prop] : ''
      if (column.formatter && typeof column.formatter === 'function') {
        return column.formatter(row, column, value)
      }
      return value
    },
    /**
     * 判断行是否选中
     * @param {Object} row - 行数据
     * @returns {Boolean} 结果
     */
    isSelected(row) {
      return this.value.some(item => item === row)
    },
    /**
     * 切换行选中状态
     * @param {Object} row - 行数据
     */
    toggleSelect(row) {
      const next = this.isSelected(row)
        ? this.value.filter(item => item !== row)
        : this.value.concat(row)
      this.$emit('input', next)
      this.$emit('selection-change', next)
    },
    /**
     * 卡片点击，转发 row-click 事件（column/event 以 null 占位，与表格事件签名对齐）
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     */
    handleCardClick(row, index) {
      this.$emit('row-click', row, null, null, index)
    },
    /**
     * 检查操作按钮属性值（disabled/loading），与 DynamicTable 行为一致
     * @param {Object} action - 操作配置
     * @param {String} prop - 属性名
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     * @returns {Boolean} 属性值
     */
    checkActionProp(action, prop, row, index) {
      const value = action[prop]
      if (value === true) return true
      if (typeof value === 'function') return value(row, index)
      return false
    },
    /**
     * 检查按钮是否禁用
     * @param {Object} action - 操作配置
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     * @returns {Boolean} 结果
     */
    checkDisabled(action, row, index) {
      return this.checkActionProp(action, 'disabled', row, index)
    },
    /**
     * 检查按钮是否加载中
     * @param {Object} action - 操作配置
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     * @returns {Boolean} 结果
     */
    checkLoading(action, row, index) {
      return this.checkActionProp(action, 'loading', row, index)
    },
    /**
     * 执行操作配置中的 handler
     * @param {Object} action - 操作配置
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     */
    handleAction(action, row, index) {
      if (typeof action.handler === 'function') {
        action.handler(row, index)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";

.mobile-card-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 2px;
  padding-bottom: 12px;
  -webkit-tap-highlight-color: transparent;

  &__empty {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
    min-height: 160px;

    i {
      font-size: 40px;
      margin-bottom: 8px;
      color: #c0c4cc;
    }

    p {
      font-size: 13px;
      margin: 0;
    }
  }
}

.mobile-card {
  position: relative;
  background: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease, transform 0.15s ease;
  cursor: pointer;
  overflow: hidden;

  // 触摸按压态替代 hover 依赖
  &:active {
    background: #f7f9fc;
    transform: scale(0.995);
  }

  &.is-selected {
    border-color: rgba(0, 113, 227, 0.45);
    box-shadow: 0 0 0 1px rgba(0, 113, 227, 0.25), 0 1px 3px rgba(0, 113, 227, 0.08);
  }

  &__check {
    position: absolute;
    top: 8px;
    right: 10px;
    // 44px 触摸目标
    padding: 10px;
    margin: -10px;
    display: flex;
    align-items: center;
  }

  &__title {
    font-size: 1rem;
    font-weight: 600;
    color: #333;
    line-height: 1.4;
    // 给右上角勾选框让位
    padding-right: 32px;
    word-break: break-all;
  }

  &__fields {
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__field {
    display: flex;
    align-items: baseline;
    gap: 10px;
    line-height: 1.5;
  }

  &__label {
    flex-shrink: 0;
    min-width: 4.5em;
    font-size: 0.857rem;
    color: #999;
  }

  &__value {
    flex: 1;
    min-width: 0;
    font-size: 0.929rem;
    color: #333;
    word-break: break-all;
    text-align: right;
  }

  &__actions {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
    align-items: center;

    ::v-deep .el-button + .el-button {
      margin-left: 0;
    }
  }

  // 操作按钮触摸目标 >= 44px
  &__action-btn,
  ::v-deep .mobile-card__actions .el-button {
    min-height: 44px;
    padding: 10px 14px;
    font-size: 0.929rem;
    border-radius: 8px;
  }
}
</style>
