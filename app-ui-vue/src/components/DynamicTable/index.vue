<template>
  <div class="dynamic-table">
    <!-- 表格工具栏插槽 -->
    <div v-if="$slots.toolbar" class="dynamic-table__toolbar">
      <slot name="toolbar" />
    </div>

    <!-- 数据表格（桌面/平板） -->
    <el-table
      v-if="!isMobileView"
      ref="table"
      v-loading="loading"
      :data="tableData"
      :element-loading-text="computedLoadingText"
      :border="border"
      :stripe="stripe"
      :height="height"
      :max-height="maxHeight"
      :size="size"
      :fit="fit"
      :highlight-current-row="highlightCurrentRow"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @filter-change="handleFilterChange"
      @current-change="handleCurrentChange"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDblclick"
      @cell-click="handleCellClick"
      @cell-dblclick="handleCellDblclick"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="selection"
        type="selection"
        :width="selectionWidth"
        :reserve-selection="reserveSelection"
        :selectable="selectable"
        fixed="left"
      />

      <!-- 序号列 -->
      <el-table-column
        v-if="index"
        type="index"
        :label="indexLabel"
        :width="indexWidth"
        fixed="left"
      />

      <!-- 动态列 -->
      <template v-for="column in internalColumns">
        <el-table-column
          :key="column.prop || column.label"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :fixed="column.fixed"
          :align="column.align || 'left'"
          :header-align="column.headerAlign"
          :class-name="column.className"
          :label-class-name="column.labelClassName"
          :sortable="column.sortable || false"
          :sort-method="column.sortMethod"
          :sort-by="column.sortBy"
          :sort-orders="column.sortOrders"
          :resizable="column.resizable !== undefined ? column.resizable : true"
          :formatter="column.formatter"
          :show-overflow-tooltip="column.showOverflowTooltip !== undefined ? column.showOverflowTooltip : true"
          :filter-method="column.filterMethod"
          :filters="column.filters"
          :filter-placement="column.filterPlacement"
          :filter-multiple="column.filterMultiple !== undefined ? column.filterMultiple : true"
          :column-key="column.columnKey || column.prop"
          :render-header="column.renderHeader"
        >
          <!-- 自定义列模板 -->
          <template slot-scope="scope">
            <!-- 使用render函数渲染 -->
            <template v-if="column.render">
              <render :render="column.render" :params="scope" />
            </template>
            <!-- 使用slot渲染 -->
            <template v-else-if="column.slot">
              <slot :name="column.slot" :row="scope.row" :$index="scope.$index" :column="column" />
            </template>
            <!-- 默认文本渲染 -->
            <template v-else>
              {{ formatValue(scope.row, column) }}
            </template>
          </template>
        </el-table-column>
      </template>

      <!-- 操作列 -->
      <el-table-column
        :label="computedActionsLabel"
        :width="actionsWidth"
        :fixed="actionsFixed"
        :align="actionsAlign || 'center'"
      >
        <template slot-scope="scope">
          <div class="action-buttons-container">
            <slot name="actions" :row="scope.row" :$index="scope.$index" />
            <template v-if="actions && Array.isArray(actions)">
              <el-button
                v-for="(action, index) in actions"
                :key="index"
                :type="action.type || 'text'"
                :size="action.size || 'mini'"
                :icon="action.icon"
                :disabled="checkDisabled(action, scope.row, scope.$index)"
                :loading="checkLoading(action, scope.row, scope.$index)"
                @click.stop="handleAction(action, scope.row, scope.$index)"
              >
                {{ action.label }}
              </el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 手机端卡片视图：列配置/操作/选择与表格视图共用，业务方无感切换 -->
    <mobile-card-list
      v-else
      :data="tableData"
      :columns="internalColumns"
      :card-fields="cardFields"
      :title-field="titleField"
      :loading="loading"
      :loading-text="computedLoadingText"
      :selection="selection"
      :value="selectionData"
      :actions="actions"
      :has-actions-slot="hasActionsSlot"
      @selection-change="handleSelectionChange"
      @row-click="handleCardRowClick"
    >
      <!-- 列作用域插槽转发：卡片视图复用表格的自定义列渲染 -->
      <template
        v-for="column in mobileSlotColumns"
        v-slot:[column.slot]="{ row, $index, column: col }"
      >
        <slot :name="column.slot" :row="row" :$index="$index" :column="col" />
      </template>
      <!-- 操作插槽转发（v-if 不可与 v-slot 同置于 template，存在性由 hasActionsSlot 控制） -->
      <template #actions="{ row, $index }">
        <slot name="actions" :row="row" :$index="$index" />
      </template>
    </mobile-card-list>

    <!-- 分页组件 -->
    <el-pagination
      v-if="pagination"
      class="dynamic-table__pagination"
      :layout="paginationLayoutForDevice"
      :page-sizes="pageSizes"
      hide-on-single-page
      :page-size="pageParams.size"
      :total="computedTotal"
      :current-page="pageParams.page"
      :pager-count="pagerCountForDevice"
      :background="paginationBackground"
      @size-change="handleSizeChange"
      @current-change="handleCurrentPageChange"
    />
  </div>
</template>

<script>
import MobileCardList from './MobileCardList.vue'

// 定义render组件用于渲染函数
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
  name: 'DynamicTable',
  components: {
    Render,
    MobileCardList
  },
  props: {
    // 数据获取函数，必须返回Promise
    dataFetcher: {
      type: Function,
      default: null
    },
    // 表格数据，与父组件双向绑定
    data: {
      type: Array,
      default: () => []
    },
    // 列配置
    columns: {
      type: Array,
      default: () => [],
      validator(value) {
        return Array.isArray(value)
      }
    },
    // 是否自动加载数据
    autoLoad: {
      type: Boolean,
      default: true
    },
    // 加载提示文本
    loadingText: {
      type: String,
      default: ''
    },
    // 是否显示边框
    border: {
      type: Boolean,
      default: true
    },
    // 是否显示斑马纹
    stripe: {
      type: Boolean,
      default: false
    },
    // 表格高度
    height: [String, Number],
    // 表格最大高度
    maxHeight: [String, Number],
    // 表格大小
    size: {
      type: String,
      default: 'medium'
    },
    // 列宽是否自动撑开
    fit: {
      type: Boolean,
      default: true
    },
    // 是否高亮当前行
    highlightCurrentRow: {
      type: Boolean,
      default: false
    },
    // 是否显示选择列
    selection: {
      type: Boolean,
      default: false
    },
    // 选择列宽度
    selectionWidth: {
      type: [String, Number],
      default: 55
    },
    // 是否保留选择状态
    reserveSelection: {
      type: Boolean,
      default: false
    },
    // 选择列的 selectable 函数
    selectable: Function,
    // 是否显示序号列
    index: {
      type: Boolean,
      default: false
    },
    // 序号列标签
    indexLabel: {
      type: String,
      default: '#'
    },
    // 序号列宽度
    indexWidth: {
      type: [String, Number],
      default: 55
    },
    // 是否显示分页
    pagination: {
      type: Boolean,
      default: true
    },
    // 分页布局
    paginationLayout: {
      type: String,
      default: 'total, sizes, prev, pager, next, jumper'
    },
    // 分页页码数组
    pageSizes: {
      type: Array,
      default: () => [10, 20, 50, 100]
    },
    // 分页按钮数量
    pagerCount: {
      type: Number,
      default: 7
    },
    // 分页背景色
    paginationBackground: {
      type: Boolean,
      default: true
    },
    // 数据总条数（支持从外部传入）
    total: {
      type: Number,
      default: 0
    },
    // 操作列配置
    actions: {
      type: Array,
      default: null,
      validator(value) {
        return value === null || Array.isArray(value)
      }
    },
    // 操作列标签
    actionsLabel: {
      type: String,
      default: ''
    },
    // 操作列宽度
    actionsWidth: {
      type: [String, Number],
      default: 150
    },
    // 操作列固定位置
    actionsFixed: {
      type: [String, Boolean],
      default: 'right'
    },
    // 操作列对齐方式
    actionsAlign: {
      type: String,
      default: 'center'
    },
    currentPage: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 10
    },
    // 是否启用响应式：手机档自动切换为卡片视图
    responsive: {
      type: Boolean,
      default: true
    },
    // 卡片视图展示字段（prop 名称数组），不传则取前几个有 label 的列
    cardFields: {
      type: Array,
      default: null
    },
    // 卡片视图标题字段（prop 名称），不传则取第一个展示列
    titleField: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      // 表格数据
      tableData: [],
      // 加载状态
      loading: false,
      // 内部使用的数据总条数
      internalTotal: 0,
      // 分页参数
      pageParams: {
        page: this.currentPage || 1,
        size: this.pageSize || 10
      },
      // 排序参数
      sortParams: {},
      // 过滤参数
      filterParams: {},
      // 查询参数
      queryParams: {},
      // 选中行数据
      selectionData: [],
      // 内部使用的列配置
      internalColumns: []
    }
  },
  computed: {
    // 计算总条数，优先使用外部传入的total，否则使用内部的internalTotal
    computedTotal() {
      return this.total || this.internalTotal
    },
    // 加载提示文本（支持 i18n 回退）
    computedLoadingText() {
      return this.loadingText || this.$t('common.loading')
    },
    // 操作列标签（支持 i18n 回退）
    computedActionsLabel() {
      return this.actionsLabel || this.$t('common.operation')
    },
    // 当前设备档位（phone/tablet/desktop），由 store 全局驱动
    device() {
      return this.$store.state.app.device
    },
    // 手机端是否切换为卡片视图（responsive 关闭或非手机档仍用表格）
    isMobileView() {
      return this.responsive && this.device === 'phone'
    },
    // 卡片视图需要转发的列作用域插槽（父组件已提供对应插槽的列）
    mobileSlotColumns() {
      return (this.internalColumns || []).filter(
        column => column.slot && this.$scopedSlots[column.slot]
      )
    },
    // 父组件是否提供了 actions 作用域插槽（决定卡片视图是否渲染操作区）
    hasActionsSlot() {
      return !!this.$scopedSlots.actions
    },
    // 分页布局：手机端精简为前后翻页，避免溢出
    paginationLayoutForDevice() {
      return this.isMobileView ? 'prev, pager, next' : this.paginationLayout
    },
    // 分页按钮数量：手机端收紧为 5 个
    pagerCountForDevice() {
      return this.isMobileView ? 5 : this.pagerCount
    }
  },
  watch: {
    // 监听外部数据变化
    data: {
      handler(newData) {
        if (newData && Array.isArray(newData)) {
          this.tableData = [...newData]
        }
      },
      immediate: true
    },
    // 监听 columns 变化并更新 internalColumns
    columns: {
      handler(newColumns) {
        this.internalColumns = [...newColumns]
      },
      immediate: true
    },
    // 监听 currentPage 变化并更新 pageParams
    currentPage: {
      handler(newVal) {
        this.pageParams.page = newVal
      },
      immediate: true
    },
    // 监听 pageSize 变化并更新 pageParams
    pageSize: {
      handler(newVal) {
        this.pageParams.size = newVal
      },
      immediate: true
    }
  },
  created() {
    // 如果设置了自动加载，则初始化时加载数据
    if (this.autoLoad) {
      this.loadData()
    } else if (this.data && this.data.length > 0) {
      // 如果提供了静态数据，则使用静态数据
      this.tableData = [...this.data]
    }
  },
  methods: {
    /**
     * 更新列配置
     * @param {Array} newColumns - 新的列配置
     */
    updateColumns(newColumns) {
      this.internalColumns = [...newColumns]
      this.$emit('update:columns', newColumns)
    },

    /**
     * 加载表格数据
     * @param {Object} params - 额外的查询参数
     */
    async loadData(params = {}) {
      // 如果没有提供dataFetcher，直接使用传入的data
      if (!this.dataFetcher) {
        if (this.data && this.data.length > 0) {
          this.tableData = [...this.data]
        }
        return
      }

      // 设置加载状态
      this.loading = true

      try {
        // 合并所有参数
        const fetchParams = {
          pagination: { ...this.pageParams },
          sort: { ...this.sortParams },
          filter: { ...this.filterParams },
          query: { ...this.queryParams, ...params }
        }

        // 调用数据获取函数
        const result = await this.dataFetcher(fetchParams)

        // 处理返回结果
        if (result && typeof result === 'object') {
          this.tableData = Array.isArray(result.data) ? result.data : []
          this.internalTotal = result.total || 0

          // 更新父组件的数据
          this.$emit('update:data', this.tableData)
        } else {
          this.tableData = []
          this.internalTotal = 0
        }

        // 触发加载成功事件
        this.$emit('load-success', result)
      } catch (error) {
        // 清空数据
        this.tableData = []
        this.internalTotal = 0

        // 触发加载失败事件
        this.$emit('load-error', error)

        // 显示错误信息
        console.error('DynamicTable load data error:', error)
      } finally {
        // 关闭加载状态
        this.loading = false
      }
    },

    /**
     * 刷新数据
     * @param {Object} params - 额外的查询参数
     */
    refresh(params = {}) {
      this.loadData(params)
    },

    /**
     * 格式化单元格显示值
     * @param {Object} row - 行数据
     * @param {Object} column - 列配置
     */
    formatValue(row, column) {
      const { prop, formatter } = column
      const value = prop ? row[prop] : ''

      // 如果有自定义格式化函数，则使用
      if (formatter && typeof formatter === 'function') {
        return formatter(row, column, value)
      }

      // 默认返回原始值
      return value
    },

    /**
     * 更新分页状态并加载数据
     * @param {Object} options - 分页选项
     * @param {Number} options.page - 页码
     * @param {Number} options.size - 每页条数
     */
    updatePagination({ page, size }) {
      if (page !== undefined) {
        this.pageParams.page = page
        this.$emit('update:currentPage', page)
        this.$emit('current-page-change', page)
      }
      if (size !== undefined) {
        this.pageParams.size = size
        this.$emit('update:pageSize', size)
        this.$emit('page-size-change', size)
        // 切换每页条数时重置到第一页
        if (page === undefined) {
          this.pageParams.page = 1
          this.$emit('update:currentPage', 1)
          this.$emit('current-page-change', 1)
        }
      }
      this.loadData()
    },

    /**
     * 处理分页大小变化
     * @param {Number} size - 新的分页大小
     */
    handleSizeChange(size) {
      this.updatePagination({ size })
    },

    /**
     * 处理当前页变化
     * @param {Number} page - 新的当前页
     */
    handleCurrentPageChange(page) {
      this.updatePagination({ page })
    },

    /**
     * 处理排序变化
     * @param {Object} sortInfo - 排序信息
     */
    handleSortChange(sortInfo) {
      this.sortParams = sortInfo
      this.$emit('sort-change', sortInfo)
      this.loadData()
    },

    /**
     * 处理过滤变化
     * @param {Object} filters - 过滤信息
     */
    handleFilterChange(filters) {
      this.filterParams = filters
      this.$emit('filter-change', filters)
      this.loadData()
    },

    /**
     * 处理当前行变化
     * @param {Object} currentRow - 当前行
     * @param {Object} oldCurrentRow - 旧行
     */
    // 通用事件转发方法
    forwardEvent(eventName, ...args) {
      this.$emit(eventName, ...args)
    },

    handleCurrentChange(currentRow, oldCurrentRow) {
      this.forwardEvent('current-change', currentRow, oldCurrentRow)
    },

    handleRowClick(row, column, event) {
      this.forwardEvent('row-click', row, column, event)
    },

    /**
     * 卡片视图行点击转发（column/event 以 null 占位，与表格事件签名对齐）
     * @param {Object} row - 行数据
     */
    handleCardRowClick(row) {
      this.forwardEvent('row-click', row, null, null)
    },

    handleRowDblclick(row, column, event) {
      this.forwardEvent('row-dblclick', row, column, event)
    },

    handleCellClick(row, column, cell, event) {
      this.forwardEvent('cell-click', row, column, cell, event)
    },

    handleCellDblclick(row, column, cell, event) {
      this.forwardEvent('cell-dblclick', row, column, cell, event)
    },

    /**
     * 处理选择变化
     * @param {Array} selection - 选中的行数组
     */
    handleSelectionChange(selection) {
      this.selectionData = selection
      this.$emit('selection-change', selection)
    },

    /**
     * 检查操作按钮的属性值
     * @param {Object} action - 操作按钮配置
     * @param {String} prop - 属性名（disabled/loading）
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
     */
    checkDisabled(action, row, index) {
      return this.checkActionProp(action, 'disabled', row, index)
    },

    /**
     * 检查按钮是否加载中
     */
    checkLoading(action, row, index) {
      return this.checkActionProp(action, 'loading', row, index)
    },

    /**
     * 处理操作按钮点击
     */
    handleAction(action, row, index) {
      if (typeof action.handler === 'function') {
        action.handler(row, index)
      }
    },

    /**
     * 清空选择（卡片视图下直接重置内部选中状态）
     */
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelection()
      }
      this.selectionData = []
    },

    /**
     * 获取选中行
     * @returns {Array} 选中行数组
     */
    getSelection() {
      return this.selectionData
    },

    /**
     * 设置选中行（卡片视图下直接以受控方式写入选中状态）
     * @param {Array} rows - 要选中的行数组
     */
    setSelection(rows) {
      if (!Array.isArray(rows)) {
        return
      }
      if (this.$refs.table) {
        rows.forEach(row => {
          this.$refs.table.toggleRowSelection(row, true)
        })
      } else {
        this.selectionData = [...rows]
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dynamic-table {
  width: 100%;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;

  &__toolbar {
    flex: 0 0 auto;
    margin-bottom: 6px;
  }

  &__pagination {
    flex: 0 0 auto;
    margin-top: 6px;
    display: flex;
    justify-content: flex-end;
    align-items: center;

    ::v-deep .el-pagination {
      font-weight: 400;

      .btn-prev,
      .btn-next,
      .el-pager li {
        min-width: 26px;
        height: 26px;
        line-height: 26px;
        font-size: 12px;
      }

      .el-pagination__total,
      .el-pagination__jump {
        font-size: 12px;
        color: #909399;
      }

      .el-select .el-input__inner {
        height: 26px;
        line-height: 26px;
        font-size: 12px;
      }
    }
  }

  // 表格撑满 toolbar 与分页之间的剩余空间（内部完整 flex 链路，避免底部留白）
  ::v-deep .el-table {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    font-size: 13px;

    .el-table__header-wrapper {
      flex: 0 0 auto;
    }

    .el-table__body-wrapper {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
    }

    // 紧凑表头
    th.el-table__cell {
      padding: 6px 0;
      background-color: #fafbfc;
      color: #606266;
      font-weight: 600;
      font-size: 12px;

      .cell {
        padding-left: 8px;
        padding-right: 8px;
        line-height: 1.4;
      }
    }

    // 紧凑单元格
    td.el-table__cell {
      padding: 4px 0;

      .cell {
        padding-left: 8px;
        padding-right: 8px;
        line-height: 1.45;
      }
    }

    // 斑马纹与悬停：保持现有色调，仅做轻微蓝调提亮
    &--striped .el-table__body tr.el-table__row--striped td {
      background: #fafbfd;
    }

    .el-table__body tr {
      &:hover > td {
        background-color: #f2f7fe;
      }

      &.current-row > td {
        background-color: #eaf3fe;
      }
    }

    // 空数据状态紧凑化
    .el-table__empty-block {
      min-height: 96px;

      .el-table__empty-text {
        font-size: 13px;
        color: #909399;
        line-height: 96px;
      }
    }

    // 行内按钮整体收紧
    .el-button--mini {
      padding: 4px 8px;
      font-size: 12px;
    }
  }

  .action-buttons-container {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    justify-content: center;
    align-items: center;

    ::v-deep .el-button + .el-button {
      margin-left: 0;
    }
  }
}
</style>
