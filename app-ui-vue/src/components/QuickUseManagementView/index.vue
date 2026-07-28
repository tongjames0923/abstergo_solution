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
            class="add-button"
            @click="showAddDialog"
          >
            {{ addButtonText }}
          </el-button>
          
          <!-- 添加导出按钮 -->
          <el-button
            type="success"
            icon="el-icon-download"
            @click="handleExport"
          >
            导出Excel
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="search-card" shadow="never">
      <DynamicFormWithApi
        ref="dynamicForm"
        :title="searchFormTitle"
        :config-name="configName"
        @submit="handleSubmit"
        @reset="handleReset"
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
            title="刷新数据"
            @click="refresh"
          />
        </div>
      </div>

      <DynamicTable
        ref="dynamicTable"
        :data-fetcher="fetchData"
        :columns.sync="internalColumns"
        :actions="actions"
        :auto-load="autoLoad"
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
              size="mini"
              type="primary"
              plain
              :disabled="isEditDisabled(row, $index)"
              @click="handleEdit(row, $index)"
            >
              {{ editActionButtonText }}
            </el-button>

            <el-button
              v-if="deleteHandler"
              size="mini"
              type="danger"
              plain
              :disabled="isDeleteDisabled(row, $index)"
              @click="handleDelete(row, $index)"
            >
              {{ deleteActionButtonText }}
            </el-button>
            <!-- 兼容旧版actions插槽 -->
            <slot name="actions" :row="row" :index="$index" />
            <!-- 更多功能按钮 -->
            <template v-if="$scopedSlots['actions-dropdown']">
              <el-dropdown @command="handleActionCommand($event, row, $index)">
                <el-button size="mini" type="primary" plain>
                  更多功能<i class="el-icon-arrow-down el-icon--right" />
                </el-button>
                <el-dropdown-menu>
                  <slot name="actions-dropdown" :row="row" :index="$index" />
                </el-dropdown-menu>
              </el-dropdown>
            </template>

          </div>
        </template>
      </DynamicTable>
    </el-card>

    <!-- 通用弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      :modal="true"
      :close-on-click-modal="true"
      :modal-append-to-body="false"
      class="form-dialog"
      :fullscreen="isMobile"
      :width="dialogWidth"
      @close="handleDialogClose"
    >
      <DynamicFormWithApi
        v-if="dialogVisible"
        ref="commonForm"
        :config-name="currentFormConfigName"
        :value="editFormData"
      >
        <template #actions="{formData ,submit,cancel}">
          <div class="dialog-actions">
            <el-button @click="handleDialogClose">取消</el-button>
            <el-button type="primary" @click="handleDialogSubmit(formData)">保存</el-button>
          </div>
        </template>
      </DynamicFormWithApi>
    </el-dialog>
  </div>
</template>

<script>
import DynamicFormWithApi from '@/components/DynamicForm/DynamicFormWithApi'
import DynamicTable from '@/components/DynamicTable'
import { exportTableToExcel } from '@/utils/excel' // 导入Excel导出工具函数

export default {
  name: 'QuickUseManagementView',
  components: {
    DynamicFormWithApi,
    DynamicTable
  },
  props: {
    // 主标题
    mainTitle: {
      type: String,
      default: '管理'
    },
    // 新增按钮文本
    addButtonText: {
      type: String,
      default: '新增'
    },
    // 搜索表单标题
    searchFormTitle: {
      type: String,
      default: '查询'
    },
    // 搜索表单配置名称（必填）
    configName: {
      type: String,
      required: true
    },
    // 表格列配置
    columns: {
      type: Array,
      default: () => [],
      validator(value) {
        return Array.isArray(value)
      }
    },
    // 删除按钮文本
    deleteActionButtonText: {
      type: String,
      default: '删除'
    },
    // 编辑按钮文本
    editActionButtonText: {
      type: String,
      default: '编辑'
    },
    // 表格操作按钮配置
    actions: {
      type: Array,
      default: () => []
    },
    // 是否自动加载数据
    autoLoad: {
      type: Boolean,
      default: true
    },
    // 查询处理器（必填）
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
    // 编辑表单配置名称
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
    // 编辑按钮禁用判断函数
    editable: {
      type: Function,
      default: null
    },
    // 删除处理器
    deleteHandler: {
      type: Function,
      default: null
    },
    // 删除按钮禁用判断函数
    deleteable: {
      type: Function,
      default: null
    },
    // 删除确认文本生成函数
    deleteConfirmText: {
      type: Function,
      default: (row, index) => `确定要删除第${index + 1}项吗？`
    },
    // 查询参数转换器
    queryTransformer: {
      type: Function,
      default: null
    },
    // 排序参数转换器
    sortTransformer: {
      type: Function,
      default: null
    },
    // 分页参数转换器
    pageTransformer: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      searchForm: {},
      dialogVisible: false,
      dialogType: 'add',
      editFormData: {},
      internalColumns: [],
      windowWidth: window.innerWidth,
      originalData: [] // 存储原始数据
    }
  },
  computed: {
    isMobile() {
      return this.windowWidth <= 768
    },

    dialogWidth() {
      if (this.windowWidth < 768) {
        return '90%'
      } else if (this.windowWidth < 1200) {
        return '75%'
      } else {
        return '60%'
      }
    },

    // 弹窗标题
    dialogTitle() {
      return this.dialogType === 'add' ? this.addFormTitle : this.editFormTitle
    },

    // 当前表单配置名称
    currentFormConfigName() {
      return this.dialogType === 'add' ? this.addFormConfigName : this.editFormConfigName
    }
  },
  watch: {
    // 监听 columns 变化并更新 internalColumns
    columns: {
      handler(newColumns) {
        this.internalColumns = [...newColumns]
      },
      immediate: true
    }
  },
  mounted() {
    // 监听窗口大小变化，以动态调整弹窗尺寸
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前移除事件监听器
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    // 检查编辑按钮是否禁用
    isEditDisabled(row, index) {
      return this.editable != null && !this.editable(row, index)
    },

    // 检查删除按钮是否禁用
    isDeleteDisabled(row, index) {
      return this.deleteable != null && !this.deleteable(row, index)
    },

    // 处理窗口大小变化
    handleResize() {
      // 通过触发计算属性更新来响应窗口大小变化
      this.windowWidth = window.innerWidth
    },

    // 更新列配置的方法
    updateColumns(newColumns) {
      this.internalColumns = [...newColumns]
      this.$emit('update:columns', newColumns)
    },

    handleSubmit(data) {
      this.searchForm = data
      // 重置分页到第一页
      this.resetPagination()
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
        const tableResult = {
          data: Array.isArray(result.data) ? result.data : [],
          total: result.total || 0
        }
        
        // 保存原始数据用于导出
        this.originalData = Array.isArray(result.data) ? [...result.data] : []
        
        return tableResult
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
      if (typeof this.queryTransformer === 'function') {
        return this.queryTransformer({
          ...queryParams,
          ...this.searchForm
        })
      }
      return {
        ...queryParams,
        ...this.searchForm
      }
    },

    // 转换排序参数
    transformSortParams(sortParams) {
      if (typeof this.sortTransformer === 'function') {
        return this.sortTransformer(sortParams)
      }

      if (sortParams.prop && sortParams.order) {
        const direction = sortParams.order === 'ascending' ? 'ASC' : 'DESC'
        return {
          [`${sortParams.prop}Order`]: direction
        }
      }
      return {}
    },

    // 转换分页参数
    transformPageParams(pageParams) {
      if (typeof this.pageTransformer === 'function') {
        return this.pageTransformer(pageParams)
      }
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
      this.dialogType = 'add'
      this.editFormData = {}
      this.dialogVisible = true
    },

    // 处理表单提交
    async handleDialogSubmit(formData) {
      const isAdd = this.dialogType === 'add'
      const handler = isAdd ? this.addHandler : this.editHandler
      const successEvent = isAdd ? 'add-success' : 'edit-success'
      const errorEvent = isAdd ? 'add-error' : 'edit-error'
      const warningMsg = isAdd ? '未配置新增处理器' : '未配置编辑处理器'

      if (!handler || typeof handler !== 'function') {
        console.warn(`未提供 ${isAdd ? 'addHandler' : 'editHandler'} 方法`)
        // 消息提示由后端统一控制
        return
      }

      try {
        await handler(formData)
        this.dialogVisible = false
        this.$emit(successEvent, formData)
        this.refresh()
      } catch (error) {
        console.error('操作失败:', error)
        this.$emit(errorEvent, error)
      }
    },

    // 显示编辑弹窗
    showEditDialog(rowData) {
      this.dialogType = 'edit'
      this.editFormData = { ...rowData }
      this.dialogVisible = true
    },

    // 处理编辑
    handleEdit(row, index) {
      this.showEditDialog(row)
    },

    handleDelete(row, index) {
      this.$confirm(this.deleteConfirmText(row, index), '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        // 如果提供了删除处理器，则调用它
        if (this.deleteHandler && typeof this.deleteHandler === 'function') {
          await this.deleteHandler(row)
          this.$emit('delete-success', row)
          // 刷新表格数据
          this.refresh()
        } else {
          console.warn('未提供 deleteHandler 方法')
          // 消息提示由后端统一控制
        }
      }).catch(() => {
        // 用户取消删除
        this.$emit('delete-cancel')
      })
    },

    // 处理操作命令
    handleActionCommand(command, row, index) {
      this.$emit('action-command', { command, row, index })
    },

    // 处理弹窗关闭
    handleDialogClose() {
      this.$refs.commonForm && this.$refs.commonForm.resetForm()
      const cancelEvent = this.dialogType === 'add' ? 'add-cancel' : 'edit-cancel'
      this.$emit(cancelEvent)
      this.dialogVisible = false
    },
    
    // 重置分页到第一页
    resetPagination() {
      if (this.$refs.dynamicTable) {
        this.$refs.dynamicTable.pageParams.page = 1
        // 同时触发页码变更事件，确保UI同步更新
        this.$refs.dynamicTable.$emit('update:currentPage', 1)
        this.$refs.dynamicTable.$emit('current-page-change', 1)
      }
    },
    
    // 导出功能 - 使用原始数据
    async handleExport() {
      try {
        if (!this.originalData || this.originalData.length === 0) {
          this.$message.warning('当前没有可导出的数据');
          return;
        }
        
        // 数据量限制提醒
        if (this.originalData.length > 5000) {
          const confirm = await this.$confirm(
            `当前数据量较大(${this.originalData.length}条)，导出可能需要较长时间，是否继续？`,
            '提示',
            { 
              confirmButtonText: '继续导出',
              cancelButtonText: '取消',
              type: 'warning'
            }
          );
          if (!confirm) return;
        }
        
        // 准备列配置用于导出
        const exportColumns = this.columns.filter(col => col.prop) // 只导出有prop的列
        
        // 调用导出函数，使用原始数据
        exportTableToExcel(this.originalData, exportColumns, this.mainTitle + '查询数据')
        
        this.$message.success(`成功导出 ${this.originalData.length} 条数据`);
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败，请稍后重试')
      }
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


  // 移动端适配
  @media (max-width: 768px) {
    padding: 10px;

    .header-section {
      margin-bottom: 15px;

      .header-content {
        .main-title {
          font-size: 20px;
        }
      }
    }

    .search-card, .table-card {
      ::v-deep .el-card__body {
        padding: 15px;
      }
    }

    .table-card {
      ::v-deep .el-card__body {
        padding: 15px 5px;
      }
    }

    .table-actions {
      gap: 6px;

      ::v-deep .el-button {
        padding: 8px 12px;
        font-size: 12px;
      }
    }

    .form-dialog {
      ::v-deep .el-dialog__body {
        padding: 15px 20px;
      }
    }
  }
}
</style>
