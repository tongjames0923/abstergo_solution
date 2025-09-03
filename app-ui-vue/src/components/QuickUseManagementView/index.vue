<template>
  <div>
    <DynamicFormWithApi
      :title="searchFormTitle"
      :config-name="configName"
      @submit="handleSubmit"
      @reset="handleReset"
      ref="dynamicForm"
    />
    <DynamicTable
      :data-fetcher="fetchData"
      :columns="columns"
      :actions="actions"
      :auto-load="autoLoad"
      ref="dynamicTable"
    >
      <!-- 自定义操作按钮插槽 -->
      <template #actions="{ row, $index }">
        <slot name="actions" :row="row" :index="$index"/>
      </template>

    </DynamicTable>
  </div>
</template>

<script>
import DynamicFormWithApi from '@/components/DynamicForm/DynamicFormWithApi'
import DynamicTable from '@/components/DynamicTable'

export default {
  name: 'QuickUseManagementView',
  components: {
    DynamicFormWithApi,
    DynamicTable
  },
  props: {
    searchFormTitle: {
      type: String,
      default: '查询'
    },
    configName: {
      type: String,
      required: true
    },
    // 表格列配置
    columns: {
      type: Array,
      default: () => []
    },
    // 操作按钮配置
    actions: {
      type: Array,
      default: () => []
    },
    // 是否自动加载数据
    autoLoad: {
      type: Boolean,
      default: true
    },
    // 查询处理器，由使用者提供具体实现
    searchHandler: {
      type: Function,
      required: true
    },
    // 查询参数转换器，可选
    queryTransformer: {
      type: Function,
      default: null
    },
    // 排序参数转换器，可选
    sortTransformer: {
      type: Function,
      default: null
    },
    // 分页参数转换器，可选
    pageTransformer: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      searchForm: {}
    }
  },
  methods: {
    handleSubmit(data) {
      this.searchForm = data
      this.$refs.dynamicTable.refresh()
    },
    handleReset() {
      this.searchForm = {}
      this.$refs.dynamicTable.refresh()
      this.$emit('reset')
    },
    // 数据获取方法
    async fetchData(params) {
      try {
        // 处理查询参数
        const queryData = this.transformQueryParams(params.query || {})

        // 处理排序参数
        const sortData = this.transformSortParams(params.sort || {})

        // 处理分页参数
        const pageData = this.transformPageParams(params.pagination || {})

        // 构造最终的查询参数对象
        // query+sort作为body参数
        const bodyParams = {
          ...queryData,
          ...sortData
        }

        // page作为查询参数
        const requestParams = {
          ...pageData
        }

        // 调用传入的searchHandler方法获取数据
        let result = await this.searchHandler(bodyParams, requestParams)
        result = result.data
        // 确保返回的数据格式符合DynamicTable的要求
        return {
          data: Array.isArray(result.data) ? result.data : [],
          total: result.total || 0
        }
      } catch (error) {
        console.error('数据获取失败:', error)
        this.$emit('load-error', error)
        return {
          data: [],
          total: 0
        }
      }
    },

    // 转换查询参数
    transformQueryParams(queryParams) {
      // 如果提供了自定义转换器，则使用它
      if (this.queryTransformer && typeof this.queryTransformer === 'function') {
        return this.queryTransformer({
          ...queryParams,
          ...this.searchForm
        })
      }

      // 默认处理：合并表单数据和查询参数
      return {
        ...queryParams,
        ...this.searchForm
      }
    },

    // 转换排序参数
    transformSortParams(sortParams) {
      // 如果提供了自定义转换器，则使用它
      if (this.sortTransformer && typeof this.sortTransformer === 'function') {
        return this.sortTransformer(sortParams)
      }

      // 默认处理：将DynamicTable的排序参数转换为后端需要的格式
      if (sortParams.prop && sortParams.order) {
        const direction = sortParams.order === 'ascending' ? 'ASC' : 'DESC'
        return {
          [sortParams.prop + 'Order']: direction
        }
      }

      return {}
    },

    // 转换分页参数
    transformPageParams(pageParams) {
      // 如果提供了自定义转换器，则使用它
      if (this.pageTransformer && typeof this.pageTransformer === 'function') {
        return this.pageTransformer(pageParams)
      }

      // 默认处理：转换为后端期望的参数名
      return {
        pageNum: pageParams.page,
        pageSize: pageParams.size
      }
    },

    // 刷新表格数据
    refresh() {
      this.$refs.dynamicTable.refresh()
    },

    // 获取选中行数据
    getSelection() {
      return this.$refs.dynamicTable.getSelection()
    },

    // 清空选中状态
    clearSelection() {
      this.$refs.dynamicTable.clearSelection()
    }
  }
}
</script>
