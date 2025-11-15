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
      <el-table
        :data="tableData"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        stripe
        v-loading="loading"
        lazy
        :load="loadNode"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="companyName" label="公司名称" width="250"/>
        <el-table-column prop="taxNumber" label="税号" width="200"/>
        <el-table-column prop="flag" label="状态" width="100">
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
        <el-table-column label="操作" fixed="right" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-dropdown @command="handleMoreActions">
              <el-button size="mini">
                更多<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="{type: 'addChild', row: scope.row}">
                  新增子公司
                </el-dropdown-item>
                <el-dropdown-item :command="{type: 'move', row: scope.row}">
                  移动到其他公司
                </el-dropdown-item>
                <el-dropdown-item :command="{type: 'delete', row: scope.row}" divided>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 公司编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      :modal="false"
      :before-close="handleDialogClose"
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
        <el-button type="primary" @click="handleDialogConfirm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 移动公司对话框 -->
    <el-dialog
      title="移动公司"
      :visible.sync="moveCompanyDialogVisible"
      width="500px"
      :modal="false"
      :before-close="handleMoveCompanyDialogClose"
    >
      <el-form :model="moveCompanyForm" ref="moveCompanyForm" label-width="100px">
        <el-form-item label="目标父公司">
          <el-select v-model="moveCompanyForm.targetParentId" placeholder="请选择目标父公司" style="width: 100%">
            <el-option
              v-for="item in allCompanies"
              :key="item.id"
              :label="item.companyName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleMoveCompanyDialogClose">取 消</el-button>
        <el-button type="primary" @click="handleMoveCompanyConfirm">确 定</el-button>
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
      searchForm: {
        likeName: '',
        equalsTaxNumber: '',
        flagEquals: [],
        treeRootFlag: 1 // 默认限制只查询顶级节点
      },
      sortParams: {}, // 排序参数
      tableData: [],
      dialogVisible: false,
      moveCompanyDialogVisible: false, // 移动公司对话框可见性
      dialogType: 'add', // 'add' 或 'edit'
      editForm: {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1 // 默认激活状态
      },
      moveCompanyForm: {
        companyId: undefined, // 要移动的公司ID
        targetParentId: undefined // 目标父公司ID
      },
      allCompanies: [], // 所有公司列表（用于移动公司时选择父公司）
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
        {code: 0, label: '已删除'},
        {code: 1, label: '已激活'},
        {code: 2, label: '禁用'},
        {code: 5, label: '系统内置'}
      ]
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogType === 'add' ? '新增公司' : '编辑公司'
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
    // 加载公司数据（构建树形结构）
    async loadCompanyData() {
      this.loading = true
      try {
        // 查询根节点公司（parentId为null的公司）
        const response = await adminQueryCompany({
          ...this.searchForm,
          ...this.sortParams,
          treeRootFlag: 1 // 限制只查询顶级节点
        })
        
        // 构建树形结构
        const rootCompanies = response.data.data || []
        const treeData = await this.buildTreeData(rootCompanies)
        this.tableData = treeData
      } catch (error) {
        console.error('加载公司数据失败:', error)
        this.$message.error('加载公司数据失败')
      } finally {
        this.loading = false
      }
    },

    // 构建树形数据
    async buildTreeData(companies) {
      const treeData = []
      for (const company of companies) {
        const companyWithChildren = {...company}
        // 使用childrenCount字段判断是否有子节点
        if (company.childrenCount && company.childrenCount > 0) {
          companyWithChildren.hasChildren = true
        }
        treeData.push(companyWithChildren)
      }
      return treeData
    },

    // 加载树节点
    async loadNode(tree, treeNode, resolve) {
      try {
        const response = await listChildren(tree.id)
        const children = response.data || []
        // 处理子节点，标记它们是否还有子节点
        const childrenWithHasChildren = []
        for (const child of children) {
          // 使用childrenCount字段判断是否有子节点
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

    // 搜索
    handleSearch() {
      this.loadCompanyData()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        likeName: '',
        equalsTaxNumber: '',
        flagEquals: [],
        treeRootFlag: 1 // 保持顶级节点限制
      }
      this.sortParams = {}
      this.loadCompanyData()
    },

    // 新增公司
    handleAdd() {
      this.dialogType = 'add'
      this.editForm = {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1
      }
      this.dialogVisible = true
    },

    // 新增子公司
    handleAddChild(row) {
      this.dialogType = 'add'
      this.editForm = {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1
      }

      // 如果有父级公司，则设置为该父级公司的子级
      if (row && row.id) {
        // 这里可以设置一些标记，表示是添加子级
        this.editForm.parentId = row.id
      }

      this.dialogVisible = true
    },

    // 处理移动公司
    async handleMoveCompany(row) {
      this.moveCompanyForm.companyId = row.id
      this.moveCompanyDialogVisible = true
      
      // 加载所有公司数据用于选择目标父公司
      try {
        const response = await adminQueryCompany({
          treeRootFlag: undefined // 加载所有公司，不限制顶级节点
        })
        this.allCompanies = response.data.data || []
      } catch (error) {
        console.error('加载公司数据失败:', error)
        this.$message.error('加载公司数据失败')
      }
    },

    // 编辑公司
    handleEdit(row) {
      this.dialogType = 'edit'
      this.editForm = {...row}
      this.dialogVisible = true
    },

    // 删除公司
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

    // 获取公司状态标签类型
    getCompanyStatusTagType(flag) {
      switch (flag) {
        case 0: // 已删除
          return 'info'
        case 1: // 已激活
          return 'success'
        case 2: // 禁用
          return 'warning'
        case 5: // 系统内置
          return 'primary'
        default:
          return 'info'
      }
    },

    // 获取公司状态标签文本
    getCompanyStatusLabel(flag) {
      const status = this.statusOptions.find(item => item.code === flag)
      return status ? status.label : '未知状态'
    },

    // 对话框关闭
    handleDialogClose() {
      this.dialogVisible = false
      this.$refs.editForm.resetFields()
    },

    // 对话框确认
    handleDialogConfirm() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            // 如果是新增子公司，需要在保存后设置父子关系
            if (this.dialogType === 'add' && this.editForm.parentId) {
              // 先保存公司
              const saveResponse = await saveCompany({
                companyName: this.editForm.companyName,
                taxNumber: this.editForm.taxNumber,
                description: this.editForm.description,
                flag: this.editForm.flag
              })

              if (saveResponse.data && saveResponse.data.id) {
                // 保存成功后，设置父子关系
                await moveCompanyTo(this.editForm.parentId, saveResponse.data.id)
                this.$message.success('新增子公司成功')
              }
            } else {
              // 普通保存操作
              await saveCompany(this.editForm)
              this.$message.success(this.dialogType === 'add' ? '新增成功' : '编辑成功')
            }

            this.dialogVisible = false
            this.loadCompanyData()
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败')
          }
        }
      })
    },

    // 处理更多操作
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

    // 处理排序变化
    handleSortChange({column, prop, order}) {
      if (order) {
        // 根据 Element UI 的排序规则转换为后端需要的参数
        const direction = order === 'ascending' ? 'ASC' : 'DESC'
        this.sortParams = {
          [prop + 'Order']: direction
        }
      } else {
        this.sortParams = {}
      }
      this.loadCompanyData()
    },

    // 移动公司对话框关闭
    handleMoveCompanyDialogClose() {
      this.moveCompanyDialogVisible = false
      this.$refs.moveCompanyForm.resetFields()
    },

    // 处理移动公司确认
    async handleMoveCompanyConfirm() {
      if (!this.moveCompanyForm.targetParentId) {
        this.$message.warning('请选择目标父公司')
        return
      }

      if (this.moveCompanyForm.targetParentId === this.moveCompanyForm.companyId) {
        this.$message.warning('不能将公司移动到自身')
        return
      }

      try {
        await moveCompanyTo(this.moveCompanyForm.targetParentId, this.moveCompanyForm.companyId)
        this.$message.success('移动公司成功')
        this.handleMoveCompanyDialogClose()
        this.loadCompanyData()
      } catch (error) {
        console.error('移动公司失败:', error)
        this.$message.error('移动公司失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.company-admin-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

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

    .search-form {
      .search-actions {
        display: flex;
        justify-content: flex-start;
        gap: 10px;
        align-items: center;
        height: 100%;
      }
    }
  }

  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);

    ::v-deep .el-card__body {
      padding: 20px 10px;
    }
  }

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

    .search-card,
    .table-card {
      ::v-deep .el-card__body {
        padding: 15px;
      }
    }

    .table-card {
      ::v-deep .el-card__body {
        padding: 15px 5px;
      }
    }
  }
}
</style>
