<template>
  <div class="dynamic-table">
    <!-- 表格工具栏插槽 -->
    <div v-if="$slots.toolbar" class="dynamic-table__toolbar">
      <slot name="toolbar"></slot>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="table"
      :data="tableData"
      v-loading="loading"
      :element-loading-text="loadingText"
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
      ></el-table-column>

      <!-- 序号列 -->
      <el-table-column
        v-if="index"
        type="index"
        :label="indexLabel"
        :width="indexWidth"
        fixed="left"
      ></el-table-column>

      <!-- 动态列 -->
      <template v-for="column in columns">
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
              <render :render="column.render" :params="scope"></render>
            </template>
            <!-- 使用slot渲染 -->
            <template v-else-if="column.slot">
              <slot :name="column.slot" :row="scope.row" :$index="scope.$index" :column="column"></slot>
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
        :label="actionsLabel"
        :width="actionsWidth"
        :fixed="actionsFixed"
        :align="actionsAlign || 'center'"
      >
        <template slot-scope="scope">
          <div class="action-buttons-container">
            <slot name="actions" :row="scope.row" :$index="scope.$index"></slot>
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

    <!-- 分页组件 -->
    <el-pagination
      v-if="pagination"
      class="dynamic-table__pagination"
      :layout="paginationLayout"
      :page-sizes="pageSizes"
      :page-size="pageParams.size"
      :total="total"
      :current-page="pageParams.page"
      :pager-count="pagerCount"
      :background="paginationBackground"
      @size-change="handleSizeChange"
      @current-change="handleCurrentPageChange"
    ></el-pagination>
  </div>
</template>

<script>
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
    Render
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
      default: () => []
    },
    // 是否自动加载数据
    autoLoad: {
      type: Boolean,
      default: true
    },
    // 加载提示文本
    loadingText: {
      type: String,
      default: '加载中...'
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
    // 操作列配置
    actions: {
      type: Array,
      default: null
    },
    // 操作列标签
    actionsLabel: {
      type: String,
      default: '操作'
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
    }
  },
  data() {
    return {
      // 表格数据
      tableData: [],
      // 加载状态
      loading: false,
      // 数据总条数
      total: 0,
      // 分页参数
      pageParams: {
        page: 1,
        size: 10
      },
      // 排序参数
      sortParams: {},
      // 过滤参数
      filterParams: {},
      // 查询参数
      queryParams: {},
      // 选中行数据
      selectionData: []
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
  watch: {
    // 监听外部数据变化
    data: {
      handler(newData) {
        if (newData && Array.isArray(newData)) {
          this.tableData = [...newData]
        }
      },
      immediate: true
    }
  },
  methods: {
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
          pagination: {...this.pageParams},
          sort: {...this.sortParams},
          filter: {...this.filterParams},
          query: {...this.queryParams, ...params}
        }

        // 调用数据获取函数
        const result = await this.dataFetcher(fetchParams)

        // 处理返回结果
        if (result && typeof result === 'object') {
          this.tableData = Array.isArray(result.data) ? result.data : []
          this.total = result.total || 0

          // 更新父组件的数据
          this.$emit('update:data', this.tableData)
        } else {
          this.tableData = []
          this.total = 0
        }

        // 触发加载成功事件
        this.$emit('load-success', result)
      } catch (error) {
        // 清空数据
        this.tableData = []
        this.total = 0

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
      const {prop, formatter} = column
      const value = prop ? row[prop] : ''

      // 如果有自定义格式化函数，则使用
      if (formatter && typeof formatter === 'function') {
        return formatter(row, column, value)
      }

      // 默认返回原始值
      return value
    },

    /**
     * 处理分页大小变化
     * @param {Number} size - 新的分页大小
     */
    handleSizeChange(size) {
      this.pageParams.size = size
      this.pageParams.page = 1 // 重置到第一页
      this.loadData()
    },

    /**
     * 处理当前页变化
     * @param {Number} page - 新的当前页
     */
    handleCurrentPageChange(page) {
      this.pageParams.page = page
      this.loadData()
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
    handleCurrentChange(currentRow, oldCurrentRow) {
      this.$emit('current-change', currentRow, oldCurrentRow)
    },

    /**
     * 处理行点击
     * @param {Object} row - 点击的行
     * @param {Object} column - 点击的列
     * @param {Event} event - 事件对象
     */
    handleRowClick(row, column, event) {
      this.$emit('row-click', row, column, event)
    },

    /**
     * 处理行双击
     * @param {Object} row - 双击的行
     * @param {Object} column - 双击的列
     * @param {Event} event - 事件对象
     */
    handleRowDblclick(row, column, event) {
      this.$emit('row-dblclick', row, column, event)
    },

    /**
     * 处理单元格点击
     * @param {Object} row - 点击的行
     * @param {Object} column - 点击的列
     * @param {Object} cell - 点击的单元格
     * @param {Event} event - 事件对象
     */
    handleCellClick(row, column, cell, event) {
      this.$emit('cell-click', row, column, cell, event)
    },

    /**
     * 处理单元格双击
     * @param {Object} row - 双击的行
     * @param {Object} column - 双击的列
     * @param {Object} cell - 双击的单元格
     * @param {Event} event - 事件对象
     */
    handleCellDblclick(row, column, cell, event) {
      this.$emit('cell-dblclick', row, column, cell, event)
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
     * 检查按钮是否禁用
     * @param {Object} action - 操作按钮配置
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     * @returns {Boolean} 是否禁用
     */
    checkDisabled(action, row, index) {
      if (action.disabled === true) {
        return true
      }
      if (typeof action.disabled === 'function') {
        return action.disabled(row, index)
      }
      return false
    },

    /**
     * 检查按钮是否加载中
     * @param {Object} action - 操作按钮配置
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     * @returns {Boolean} 是否加载中
     */
    checkLoading(action, row, index) {
      if (action.loading === true) {
        return true
      }
      if (typeof action.loading === 'function') {
        return action.loading(row, index)
      }
      return false
    },

    /**
     * 处理操作按钮点击
     * @param {Object} action - 操作按钮配置
     * @param {Object} row - 行数据
     * @param {Number} index - 行索引
     */
    handleAction(action, row, index) {
      if (action.handler && typeof action.handler === 'function') {
        action.handler(row, index)
      }
    },

    /**
     * 清空选择
     */
    clearSelection() {
      this.$refs.table.clearSelection()
    },

    /**
     * 获取选中行
     * @returns {Array} 选中行数组
     */
    getSelection() {
      return this.selectionData
    },

    /**
     * 设置选中行
     * @param {Array} rows - 要选中的行数组
     */
    setSelection(rows) {
      if (Array.isArray(rows)) {
        rows.forEach(row => {
          this.$refs.table.toggleRowSelection(row, true)
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dynamic-table {
  width: 100%;

  &__toolbar {
    margin-bottom: 10px;
  }

  &__pagination {
    margin-top: 20px;
    text-align: right;
  }

  .action-buttons-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
  }
}
</style>
