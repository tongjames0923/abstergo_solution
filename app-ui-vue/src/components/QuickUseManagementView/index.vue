<template>
  <div class="quick-use-management-view">
    <div class="header-section">
      <div class="header-content">
        <h2 class="main-title">{{ mainTitle }}</h2>
        <div class="header-actions">
          <el-button
            v-if="addFormConfigName"
            type="primary"
            icon="el-icon-plus"
            @click="showAddDialog"
            class="add-button"
          >
            {{ addButtonText }}
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="search-card" shadow="never">
      <DynamicFormWithApi
        :title="searchFormTitle"
        :config-name="configName"
        @submit="handleSubmit"
        @reset="handleReset"
        ref="dynamicForm"
      />
    </el-card>

    <el-card class="table-card" shadow="never">
      <div slot="header" class="table-header">
        <span class="table-header-title">数据列表</span>
        <div class="table-header-actions">
          <el-button
            icon="el-icon-refresh"
            circle
            size="mini"
            @click="refresh"
            title="刷新数据"
          />
        </div>
      </div>

      <DynamicTable
        :data-fetcher="fetchData"
        :columns="columns"
        :actions="actions"
        :auto-load="autoLoad"
        ref="dynamicTable"
        actions-fixed="right"
        actions-width="160px"
        border
        stripe
      >
        <!-- 自定义操作按钮插槽 -->
        <template #actions="{ row, $index }">
          <div class="table-actions">
            <el-button
              v-if="editFormConfigName && editHandler"
              @click="handleEdit(row, $index)"
              size="mini"
              type="primary"
              plain
            >
              {{ editActionButtonText }}
            </el-button>

            <el-button
              v-if="deleteHandler"
              @click="handleDelete(row, $index)"
              size="mini"
              type="danger"
              plain
            >
              {{ deleteActionButtonText }}
            </el-button>
            <!-- 兼容旧版actions插槽 -->
            <slot name="actions" :row="row" :index="$index"></slot>
            <!-- 更多功能按钮 -->
            <template v-if="$scopedSlots['actions-dropdown']">
              <el-dropdown @command="handleActionCommand($event, row, $index)">
                <el-button size="mini" type="primary" plain>
                  更多功能<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu>
                  <slot name="actions-dropdown" :row="row" :index="$index"></slot>
                </el-dropdown-menu>
              </el-dropdown>
            </template>


          </div>
        </template>
      </DynamicTable>
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog
      :title="addFormTitle"
      :visible.sync="addDialogVisible"
      width="600px"
      @close="handleAddDialogClose"
      class="form-dialog"
    >
      <DynamicFormWithApi
        v-if="addDialogVisible"
        ref="addForm"
        :config-name="addFormConfigName"
      >
        <template #actions="{formData ,submit,cancel}">
          <div class="dialog-actions">
            <el-button @click="handleAddDialogClose">取消</el-button>
            <el-button type="primary" @click="handleAddSubmit(formData)">保存</el-button>
          </div>
        </template>
      </DynamicFormWithApi>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog
      :title="editFormTitle"
      :visible.sync="editDialogVisible"
      width="600px"
      @close="handleEditDialogClose"
      class="form-dialog"
    >
      <DynamicFormWithApi
        v-if="editDialogVisible"
        ref="editForm"
        :config-name="editFormConfigName"
        :value="editFormData"
      >
        <template #actions="{formData ,submit,cancel}">
          <div class="dialog-actions">
            <el-button @click="handleEditDialogClose">取消</el-button>
            <el-button type="primary" @click="handleEditSubmit(formData)">保存</el-button>
          </div>
        </template>
      </DynamicFormWithApi>
    </el-dialog>
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
    mainTitle: {
      type: String,
      default: '管理'
    },
    addButtonText: {
      type: String,
      default: '新增'
    },
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
    deleteActionButtonText: {
      type: String,
      default: '删除'
    },
    editActionButtonText: {
      type: String,
      default: '编辑'
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
    // 新增表单配置名称
    addFormConfigName: {
      type: String,
      default: ''
    },
    // 新增表单标题
    addFormTitle: {
      type: String,
      default: '新增'
    },
    // 新增处理器
    addHandler: {
      type: Function,
      default: null
    },
    editFormConfigName: {
      type: String,
      default: ''
    },
    // 编辑表单标题
    editFormTitle: {
      type: String,
      default: '编辑'
    },
    // 编辑处理器
    editHandler: {
      type: Function,
      default: null
    },
    // 删除处理器
    deleteHandler: {
      type: Function,
      default: null
    },
    deleteConfirmText: {
      type: Function,
      default: (row, index) => {
        // 输出确定要删除第index项吗？
        return `确定要删除第${index + 1}项吗？`
      }
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
      searchForm: {},
      // 新增弹窗可见性
      addDialogVisible: false,
      // 编辑弹窗可见性
      editDialogVisible: false,
      // 编辑表单数据
      editFormData: {}
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
    },

    // 显示新增弹窗
    showAddDialog() {
      this.addDialogVisible = true
    },

    // 处理新增表单提交
    async handleAddSubmit(formData) {
      try {

        // 如果提供了新增处理器，则调用它
        if (this.addHandler && typeof this.addHandler === 'function') {
          await this.addHandler(formData)
          this.addDialogVisible = false
          this.$emit('add-success', formData)
          // 刷新表格数据
          this.refresh()
        } else {
          console.warn('未提供 addHandler 方法')
          this.$message.warning('未配置新增处理器')
        }
      } catch (error) {
        console.error('新增失败:', error)
        this.$emit('add-error', error)
      } finally {
        this.addDialogVisible = false
      }
    },

    // 显示编辑弹窗
    showEditDialog(rowData) {
      this.editFormData = {...rowData}
      this.editDialogVisible = true
    },

    // 处理编辑
    handleEdit(row, index) {
      this.showEditDialog(row)
    },

    // 处理编辑表单提交
    async handleEditSubmit(formData) {
      try {
        // 如果提供了编辑处理器，则调用它
        if (this.editHandler && typeof this.editHandler === 'function') {
          await this.editHandler(formData)
          this.editDialogVisible = false
          this.$emit('edit-success', formData)
          // 刷新表格数据
          this.refresh()
        } else {
          console.warn('未提供 editHandler 方法')
          this.$message.warning('未配置编辑处理器')
        }
      } catch (error) {
        console.error('编辑失败:', error)
        this.$emit('edit-error', error)
      } finally {
        this.editDialogVisible = false
      }
    },

    handleDelete(row, index) {
      this.$confirm(this.deleteConfirmText(row, index), '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        // 如果提供了删除处理器，则调用它
        if (this.deleteHandler && typeof this.deleteHandler === 'function') {
          await this.deleteHandler(row)
          this.$emit('delete-success', row)
          // 刷新表格数据
          this.refresh()
        } else {
          console.warn('未提供 deleteHandler 方法')
          this.$message.warning('未配置删除处理器')
        }

      }).catch(() => {
        // 用户取消删除
        this.$emit('delete-cancel')
      })
    },

    // 处理操作命令
    handleActionCommand(command, row, index) {
      // 这里可以处理下拉菜单中的命令
      this.$emit('action-command', {command, row, index})
    },

    // 处理新增弹窗关闭
    handleAddDialogClose() {
      this.$refs.addForm && this.$refs.addForm.resetForm()
      this.$emit('add-cancel')
      this.addDialogVisible = false
    },

    // 处理编辑弹窗关闭
    handleEditDialogClose() {
      this.$refs.editForm && this.$refs.editForm.resetForm()
      this.$emit('edit-cancel')
      this.editDialogVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.quick-use-management-view {
  padding: 20px;

  .header-section {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;

      .main-title {
        font-size: 24px;
        font-weight: 600;
        margin: 0;
        color: #303133;
      }

      .header-actions {
        .add-button {
          height: 36px;
        }
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);

    ::v-deep .el-card__body {
      padding: 20px;
    }
  }

  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);

    ::v-deep .el-card__body {
      padding: 20px 10px;
    }

    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .table-header-title {
        font-weight: 600;
        color: #303133;
      }

      .table-header-actions {
        display: flex;
        gap: 8px;
      }
    }
  }

  .table-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: center;
  }

  .dialog-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 0 20px 20px;
  }

  .form-dialog {
    ::v-deep .el-dialog__body {
      padding: 20px 30px;
    }
  }
}
</style>
