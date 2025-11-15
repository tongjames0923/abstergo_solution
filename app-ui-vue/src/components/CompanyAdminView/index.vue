<template>
  <div class="company-admin-view">
    <div class="header-section">
      <div class="header-content">
        <h2 class="main-title">公司管理</h2>
        <div class="header-actions">
          <el-button
            type="primary"
            icon="el-icon-plus"
            @click="handleAdd"
          >
            新增公司
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" ref="searchForm" label-width="100px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="公司名称">
              <el-input
                v-model="searchForm.likeName"
                placeholder="请输入公司名称"
                clearable
                @input="onSearchInput"
                @keyup.enter.native="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="税号">
              <el-input
                v-model="searchForm.equalsTaxNumber"
                placeholder="请输入税号"
                clearable
                @input="onSearchInput"
                @keyup.enter.native="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.flagEquals"
                placeholder="请选择状态"
                clearable
                multiple
                @change="handleSearch"
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.code"
                  :label="item.label"
                  :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <div class="search-actions">
              <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
              <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <div v-loading="loading" style="min-height:200px; position:relative;">
        <el-table
          :data="tableData"
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          border
          stripe
          lazy
          :load="loadNode"
          @sort-change="handleSortChange"
          style="width: 100%;"
        >
          <el-table-column prop="companyName" label="公司名称" width="300">
            <template slot-scope="scope">
              <el-tooltip class="item" effect="dark" :content="scope.row.companyName" placement="top">
                <span v-html="highlight(scope.row.companyName)"></span>
              </el-tooltip>
              <span v-if="scope.row.childrenCount && scope.row.childrenCount > 0" class="children-count">
                ({{ scope.row.childrenCount }})
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="taxNumber" label="税号" width="200">
            <template slot-scope="scope">
              <el-tooltip class="item" effect="dark" :content="scope.row.taxNumber" placement="top">
                <span>{{ maskTaxNumber(scope.row.taxNumber) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="flag" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="getCompanyStatusTagType(scope.row.flag)">
                {{ getCompanyStatusLabel(scope.row.flag) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" sortable="custom">
            <template slot-scope="scope">
              {{ scope.row.createTime | formatDate }}
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="180" sortable="custom">
            <template slot-scope="scope">
              {{ scope.row.updateTime | formatDate }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-dropdown @command="handleMoreActions">
                <el-button size="mini">
                  更多<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item :command="{type: 'move', row: scope.row}">
                    移动到其他公司
                  </el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.id!==1" :command="{type: 'delete', row: scope.row}" divided>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && (!tableData || tableData.length === 0)" class="empty-wrap">
          <el-empty description="暂无数据"/>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 公司编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      :modal="true"
      :before-close="handleDialogClose"
      append-to-body
    >
      <el-form :model="editForm" :rules="formRules" ref="editForm" label-width="100px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="editForm.companyName" placeholder="请输入公司名称"/>
        </el-form-item>
        <el-form-item label="税号" prop="taxNumber">
          <el-input v-model="editForm.taxNumber" placeholder="请输入税号"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            placeholder="请输入描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="flag">
          <el-select v-model="editForm.flag" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.code"
              :label="item.label"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogClose">取 消</el-button>
        <el-button type="primary" @click="handleDialogConfirm" :loading="dialogSaving">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 移动公司对话框（改为远程搜索父公司） -->
    <el-dialog
      title="移动公司"
      :visible.sync="moveCompanyDialogVisible"
      width="520px"
      :modal="true"
      :before-close="handleMoveCompanyDialogClose"
      append-to-body
    >
      <el-form :model="moveCompanyForm" ref="moveCompanyForm" label-width="100px">
        <el-form-item label="目标父公司">
          <el-alert type="info" show-icon
                    title="支持模糊搜索目标父公司；请选择目标父公司，不能选择自身"/>
          <div style="margin-top:10px;">
            <el-select
              v-model="moveCompanyForm.targetParentId"
              filterable
              remote
              clearable
              placeholder="请输入公司名称或税号搜索"
              :remote-method="remoteSearchParents"
              :loading="parentRemoteLoading"
              style="width:100%;"
              :popper-class="'parent-select-popper'"
              @change="onParentSelect"
            >
              <el-option
                v-for="item in parentCandidates"
                :key="item.id"
                :label="item.companyName + (item.taxNumber ? ' — ' + item.taxNumber : '')"
                :value="item.id"
              />
              <el-option v-if="!parentCandidates.length && !parentRemoteLoading" :key="'no-result'"
                         :label="'无匹配结果'" :value="null" disabled/>
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleMoveCompanyDialogClose">取 消</el-button>
        <el-button type="primary" @click="handleMoveCompanyConfirm" :loading="moveSaving">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {adminQueryCompany, deleteCompany, listChildren, moveCompanyTo, saveCompany} from '@/api/company'

export default {
  name: 'CompanyAdminView',
  data() {
    return {
      loading: false,
      dialogSaving: false,
      moveSaving: false,
      searchDebounceTimer: null,
      searchForm: {
        likeName: '',
        equalsTaxNumber: '',
        flagEquals: [],
        treeRootFlag: 1
      },
      sortParams: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      dialogVisible: false,
      moveCompanyDialogVisible: false,
      dialogType: 'add',
      editForm: {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1,
        parentId: undefined
      },
      moveCompanyForm: {
        companyId: undefined,
        targetParentId: undefined
      },
      // 远程选择父公司相关状态
      parentCandidates: [],
      parentRemoteLoading: false,
      parentRemoteSize: 50,
      parentRemoteLastQuery: '',
      allCompanies: [],
      formRules: {
        companyName: [
          {required: true, message: '请输入公司名称', trigger: 'blur'}
        ],
        taxNumber: [
          {required: true, message: '请输入税号', trigger: 'blur'}
        ],
        flag: [
          {required: true, message: '请选择状态', trigger: 'change'}
        ]
      },
      statusOptions: [
        {code: 1, label: '已激活'},
        {code: 2, label: '禁用'},
        {code: 5, label: '系统内置'}
      ]
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogType === 'add' ? '新增公司' : '编辑公司'
    },
    companyTree() {
      const map = {}
      const roots = []
      for (const c of this.allCompanies) {
        map[c.id] = {...c, children: []}
      }
      for (const id in map) {
        const node = map[id]
        if (node.parentId && map[node.parentId]) {
          map[node.parentId].children.push(node)
        } else {
          roots.push(node)
        }
      }
      return roots
    }
  },
  created() {
    this.loadCompanyData()
  },
  filters: {
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
    }
  },
  methods: {
    escapeHtml(str = '') {
      return String(str).replace(/[&<>"'\/]/g, function (s) {
        const entityMap = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;', '/': '&#x2F;'}
        return entityMap[s]
      })
    },
    highlight(text = '') {
      const keyword = (this.searchForm.likeName || '').trim()
      if (!keyword) return this.escapeHtml(text)
      const safe = this.escapeHtml(text)
      const re = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'ig')
      return safe.replace(re, '<mark>$1</mark>')
    },
    maskTaxNumber(tax = '') {
      if (!tax) return ''
      if (tax.length <= 6) return tax
      return tax.slice(0, 3) + '****' + tax.slice(-3)
    },

    onSearchInput() {
      clearTimeout(this.searchDebounceTimer)
      this.searchDebounceTimer = setTimeout(() => {
        this.currentPage = 1
        this.handleSearch()
      }, 450)
    },

    async loadCompanyData() {
      this.loading = true
      try {
        const params = {
          ...this.searchForm,
          ...this.sortParams,
          page: this.currentPage,
          size: this.pageSize,
          treeRootFlag: 1
        }
        const response = await adminQueryCompany(params)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        this.total = resData.total != null ? resData.total : list.length
        const treeData = await this.buildTreeData(list)
        this.tableData = treeData
      } catch (error) {
        console.error('加载公司数据失败:', error)
        this.$message.error('加载公司数据失败')
      } finally {
        this.loading = false
      }
    },

    async buildTreeData(companies) {
      const treeData = []
      for (const company of companies) {
        const companyWithChildren = {...company}
        if (company.childrenCount && company.childrenCount > 0) {
          companyWithChildren.hasChildren = true
        }
        treeData.push(companyWithChildren)
      }
      return treeData
    },

    async loadNode(tree, treeNode, resolve) {
      try {
        const response = await listChildren(tree.id)
        const children = response.data || []
        const childrenWithHasChildren = []
        for (const child of children) {
          if (child.childrenCount && child.childrenCount > 0) {
            child.hasChildren = true
          }
          childrenWithHasChildren.push(child)
        }
        resolve(childrenWithHasChildren)
      } catch (error) {
        console.error(`加载公司 ${tree.id} 的子节点失败:`, error)
        resolve([])
      }
    },

    handleSearch() {
      this.currentPage = 1
      this.loadCompanyData()
    },

    handleReset() {
      this.searchForm = {
        likeName: '',
        equalsTaxNumber: '',
        flagEquals: [],
        treeRootFlag: 1
      }
      this.sortParams = {}
      this.currentPage = 1
      this.pageSize = 10
      this.loadCompanyData()
    },

    handleAdd() {
      this.dialogType = 'add'
      this.editForm = {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1,
        parentId: undefined
      }
      this.dialogVisible = true
    },

    handleAddChild(row) {
      this.dialogType = 'add'
      this.editForm = {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1,
        parentId: row && row.id ? row.id : undefined
      }
      this.dialogVisible = true
    },

    async handleMoveCompany(row) {
      this.moveCompanyForm.companyId = row.id
      this.moveCompanyForm.targetParentId = undefined
      this.parentCandidates = []
      this.parentRemoteLastQuery = ''
      this.moveCompanyDialogVisible = true
      // 预加载最近的若干（可快速选择父级）
      this.remoteSearchParents('')
    },

    handleEdit(row) {
      this.dialogType = 'edit'
      this.editForm = {...row}
      this.dialogVisible = true
    },

    handleDelete(row) {
      this.$confirm(`确定要删除公司 "${row.companyName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteCompany(row.id)
          this.$message.success('删除成功')
          this.loadCompanyData()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      })
    },

    getCompanyStatusTagType(flag) {
      switch (flag) {
        case 0:
          return 'info'
        case 1:
          return 'success'
        case 2:
          return 'warning'
        case 5:
          return 'primary'
        default:
          return 'info'
      }
    },

    getCompanyStatusLabel(flag) {
      const status = this.statusOptions.find(item => item.code === flag)
      return status ? status.label : '未知状态'
    },

    handleDialogClose() {
      this.dialogVisible = false
      if (this.$refs.editForm && this.$refs.editForm.resetFields) {
        this.$refs.editForm.resetFields()
      }
    },

    async handleDialogConfirm() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          this.dialogSaving = true
          try {
            if (this.dialogType === 'add' && this.editForm.parentId) {
              const saveResponse = await saveCompany({
                companyName: this.editForm.companyName,
                taxNumber: this.editForm.taxNumber,
                description: this.editForm.description,
                flag: this.editForm.flag
              })
              if (saveResponse.data && saveResponse.data.id) {
                await moveCompanyTo(this.editForm.parentId, saveResponse.data.id)
                this.$message.success('新增子公司成功')
              }
            } else {
              await saveCompany(this.editForm)
              this.$message.success(this.dialogType === 'add' ? '新增成功' : '编辑成功')
            }
            this.dialogVisible = false
            this.loadCompanyData()
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败')
          } finally {
            this.dialogSaving = false
          }
        }
      })
    },

    handleMoreActions(command) {
      switch (command.type) {
        case 'addChild':
          this.handleAddChild(command.row)
          break
        case 'move':
          this.handleMoveCompany(command.row)
          break
        case 'delete':
          this.handleDelete(command.row)
          break
      }
    },

    handleSortChange({column, prop, order}) {
      if (order) {
        const direction = order === 'ascending' ? 'ASC' : 'DESC'
        this.sortParams = {
          [prop + 'Order']: direction
        }
      } else {
        this.sortParams = {}
      }
      this.loadCompanyData()
    },

    handleMoveCompanyDialogClose() {
      this.moveCompanyDialogVisible = false
      this.moveCompanyForm.targetParentId = undefined
      this.parentCandidates = []
      this.parentRemoteLoading = false
      this.parentRemoteLastQuery = ''
      if (this.$refs.moveCompanyForm && this.$refs.moveCompanyForm.resetFields) {
        this.$refs.moveCompanyForm.resetFields()
      }
    },

    onParentSelect(selectedId) {
      if (!selectedId) return
      if (selectedId === this.moveCompanyForm.companyId) {
        this.$message.warning('不能将公司移动到自身')
        this.moveCompanyForm.targetParentId = undefined
      }
    },

    // 远程搜索父公司（防止一次性加载全部公司）
    async remoteSearchParents(query) {
      // 避免重复同样的快速请求
      if (query === this.parentRemoteLastQuery) return
      this.parentRemoteLastQuery = query
      this.parentRemoteLoading = true
      try {
        const params = {
          likeName: query || undefined,
          equalsTaxNumber: query && /^\d+$/.test(query) ? query : undefined,
          page: 1,
          size: this.parentRemoteSize,
          treeRootFlag: undefined
        }
        const response = await adminQueryCompany(params)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        // 过滤掉自身（前端快速防护），后端仍需校验子孙关系
        this.parentCandidates = list.filter(item => item.id !== this.moveCompanyForm.companyId)
      } catch (error) {
        console.error('搜索父公司失败:', error)
        this.$message.error('搜索父公司失败')
        this.parentCandidates = []
      } finally {
        this.parentRemoteLoading = false
      }
    },

    async handleMoveCompanyConfirm() {
      if (!this.moveCompanyForm.targetParentId) {
        this.$message.warning('请选择目标父公司')
        return
      }
      if (this.moveCompanyForm.targetParentId === this.moveCompanyForm.companyId) {
        this.$message.warning('不能将公司移动到自身')
        return
      }
      this.moveSaving = true
      try {
        await moveCompanyTo(this.moveCompanyForm.targetParentId, this.moveCompanyForm.companyId)
        this.$message.success('移动公司成功')
        this.handleMoveCompanyDialogClose()
        this.loadCompanyData()
      } catch (error) {
        console.error('移动公司失败:', error)
        // 如果后端返回子孙校验失败，应显示后端错误信息
        const msg = (error && error.response && error.response.data && error.response.data.message) ? error.response.data.message : '移动公司失败'
        this.$message.error(msg)
      } finally {
        this.moveSaving = false
      }
    },

    handlePageChange(page) {
      this.currentPage = page
      this.loadCompanyData()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadCompanyData()
    }
  },
  beforeDestroy() {
    clearTimeout(this.searchDebounceTimer)
  }
}
</script>

<style lang="scss" scoped>
.company-admin-view {
  padding: 20px;
  min-height: calc(100vh - 84px);

  .header-section {
    margin-bottom: 20px;
  }

  .main-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }

  .search-card {
    margin-bottom: 20px;
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 20px;
    }
  }

  .table-card {
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 10px;
    }
  }

  .children-count {
    color: #909399;
    margin-left: 6px;
    font-size: 12px;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    padding: 12px 0;
  }

  .empty-wrap {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px 0;
  }

  .move-tree-wrap {
    max-height: 320px;
    overflow: auto;
    border: 1px solid #edf0f5;
    border-radius: 4px;
    padding: 10px;
    margin-top: 10px;
    background: #fff;
  }

  mark {
    background: #ffe58f;
    padding: 0 2px;
    border-radius: 3px;
  }

  @media (max-width: 768px) {
    padding: 10px;
    .main-title {
      font-size: 20px;
    }
    .search-card, .table-card {
      ::v-deep .el-card__body {
        padding: 12px;
      }
    }
  }
}

/* 可选：调整下拉样式使显示更友好 */
.parent-select-popper .el-select-dropdown__item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
