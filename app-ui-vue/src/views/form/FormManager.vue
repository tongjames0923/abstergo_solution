<template>
  <div class="form-manager-container">
    <div class="header-section">
      <h2>表单管理器</h2>
      <p>管理和编辑现有的动态表单配置</p>
    </div>

    <el-card class="search-card">
      <div slot="header" class="clearfix">
        <span>搜索条件</span>
      </div>
      <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
        <el-form-item label="表单标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入表单标题"
            clearable
            @keyup.enter.native="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div slot="header" class="clearfix">
        <span>表单列表</span>
        <div style="float: right;">
          <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新建表单</el-button>
        </div>
      </div>

      <el-table
        :data="formList"
        :loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="表单标题" min-width="200" />
        <el-table-column prop="labelWidth" label="标签宽度" width="120" />
        <el-table-column prop="span" label="栅格列数" width="100" />
        <el-table-column prop="gutter" label="栅格间距" width="100" />
        <el-table-column label="可展开" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.expandable ? 'success' : 'info'">
              {{ scope.row.expandable ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认展开" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.expanded ? 'success' : 'info'">
              {{ scope.row.expanded ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字段数量" width="100">
          <template slot-scope="scope">
            {{ scope.row.fields ? scope.row.fields.length : 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="info" @click="handlePreview(scope.row)">预览</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog
      :visible.sync="previewDialogVisible"
      :title="previewFormTitle"
      width="60%"
      :before-close="handlePreviewClose"
      :modal="false"
    >
      <DynamicForm
        v-if="previewConfig"
        ref="previewForm"
        :config="previewConfig"
        :value="previewFormData"
        @submit="handlePreviewSubmit"
        @reset="handlePreviewReset"
      />
      <span slot="footer" class="dialog-footer">
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import DynamicForm from '@/components/DynamicForm/index.vue'
import {deleteFormConfig, getFormById, listForms} from '@/api/view'

export default {
  name: 'FormManager',
  components: {
    DynamicForm
  },
  data() {
    return {
      searchForm: {
        title: ''
      },
      allFormList: [], // 存储全部数据
      formList: [], // 当前页显示的数据
      loading: false,
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      previewDialogVisible: false,
      previewConfig: null,
      previewFormData: {},
      previewFormTitle: ''
    }
  },
  created() {
    this.fetchAllFormList()
  },
  methods: {
    // 获取全部表单列表（用于搜索和分页）
    async fetchAllFormList() {
      this.loading = true
      try {
        // 获取全部数据，使用较大的pageSize
        const response = await listForms(1, 10000)
        this.allFormList = response.data || []
        this.pagination.total = this.allFormList.length
        // 应用搜索过滤并更新当前页数据
        this.applyFilter()
      } catch (error) {
        console.error('获取表单列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 应用搜索过滤并更新分页数据
    applyFilter() {
      let filtered = [...this.allFormList]
      
      // 按标题过滤
      if (this.searchForm.title && this.searchForm.title.trim()) {
        const keyword = this.searchForm.title.trim().toLowerCase()
        filtered = filtered.filter(item => 
          item.title && item.title.toLowerCase().includes(keyword)
        )
      }
      
      // 更新总数
      this.pagination.total = filtered.length
      
      // 计算当前页数据
      const startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const endIndex = startIndex + this.pagination.pageSize
      this.formList = filtered.slice(startIndex, endIndex)
    },

    // 搜索
    handleSearch() {
      this.pagination.currentPage = 1
      this.applyFilter()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        title: ''
      }
      this.pagination.currentPage = 1
      this.applyFilter()
    },

    // 新建表单
    handleCreate() {
      this.$router.push('form')
    },

    // 编辑表单
    handleEdit(row) {
      // 跳转到表单创建页面并传递表单ID
      this.$router.push({
        path: 'form',
        query: { id: row.id }
      })
    },

    // 预览表单
    async handlePreview(row) {
      try {
        // 获取表单详细配置
        const response = await getFormById(row.id)

        this.previewConfig = response.data
        this.previewFormData = {}
        this.previewFormTitle = row.title ? `预览表单: ${row.title}` : '预览表单'
        this.previewDialogVisible = true
      } catch (error) {
        // 消息提示由后端统一控制
      }
    },

    // 关闭预览
    handlePreviewClose() {
      this.previewDialogVisible = false
      this.previewConfig = null
      this.previewFormData = {}
    },

    // 预览表单提交
    handlePreviewSubmit(data) {
      // 消息提示由后端统一控制
      console.log('预览表单提交数据:', data)
    },

    // 预览表单重置
    handlePreviewReset() {
      // 消息提示由后端统一控制
    },

    // 删除表单
    handleDelete(row) {
      this.$confirm(`确定要删除表单"${row.title || row.id}"吗？此操作不可恢复`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        try {
          await deleteFormConfig(row.id)
          // 重新获取全部数据
          this.fetchAllFormList()
        } catch (error) {
        }
      }).catch(() => {
        // 用户取消删除
      })
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.applyFilter()
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.applyFilter()
    }
  }
}
</script>

<style scoped>
.form-manager-container {
  padding: 20px;
  min-height: calc(100vh - 84px);
}

.header-section {
  margin-bottom: 20px;
}

.header-section h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.header-section p {
  color: #606266;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.search-form .el-form-item {
  margin-bottom: 0;
  margin-right: 15px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>
