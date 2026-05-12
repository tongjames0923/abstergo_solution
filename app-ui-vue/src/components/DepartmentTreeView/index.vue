<template>
  <div class="department-tree-view">
    <div class="header-section">
      <div class="header-content">
        <h3 class="main-title">部门管理 - {{ companyName }}</h3>
        <div class="header-actions">
          <el-button
            type="primary"
            icon="el-icon-plus"
            @click="handleAdd"
          >
            新增部门
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form ref="searchForm" :model="searchForm" label-width="100px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="部门名称">
              <el-input
                v-model="searchForm.likeName"
                placeholder="请输入部门名称"
                clearable
                @input="onSearchInput"
                @keyup.enter.native="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="部门编码">
              <el-input
                v-model="searchForm.equalsDepartmentCode"
                placeholder="请输入部门编码"
                clearable
                @input="onSearchInput"
                @keyup.enter.native="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
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
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="search-actions">
              <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
              <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 树形区域 -->
    <el-card class="table-card" shadow="never">
      <div v-loading="loading" style="min-height:200px; position:relative;">
        <el-tree
          ref="departmentTree"
          :data="treeData"
          :props="treeProps"
          node-key="id"
          lazy
          :load="loadNode"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <span>
              <el-tooltip class="item" placement="right" effect="light" popper-class="department-info-tooltip">
                <span class="tree-node-title">{{ data.departmentName }}</span>
                <template slot="content">
                  <div class="department-detail-card">
                    <div class="card-header">
                      <h3>部门信息</h3>
                    </div>
                    <div class="card-content">
                      <el-row :gutter="12">
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">部门名称:</span>
                            <span class="info-value">{{ data.departmentName }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">部门编码:</span>
                            <span class="info-value">{{ data.departmentCode || '无' }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">负责人:</span>
                            <span class="info-value">{{ data.manager || '无' }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">状态:</span>
                            <el-tag size="small" :type="getDepartmentStatusTagType(data.flag)">
                              {{ getDepartmentStatusLabel(data.flag) }}
                            </el-tag>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">添加时间:</span>
                            <span class="info-value">{{ data.createTime | formatDate }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">更新时间:</span>
                            <span class="info-value">{{ data.updateTime | formatDate }}</span>
                          </div>
                        </el-col>
                        <el-col :span="24">
                          <div class="info-item">
                            <span class="info-label">描述:</span>
                            <span class="info-value description">{{ data.description || '无' }}</span>
                          </div>
                        </el-col>
                      </el-row>
                    </div>
                  </div>
                </template>
              </el-tooltip>
              <span v-if="data.childrenCount && data.childrenCount > 0" class="children-count">
                ({{ data.childrenCount }})
              </span>
            </span>
            <span class="tree-node-actions">
              <el-button type="text" size="mini" @click="handleEdit(data)">编辑</el-button>
              <el-dropdown size="mini" @command="handleMoreActions">
                <el-button type="text" size="mini">
                  更多<i class="el-icon-arrow-down el-icon--right"/>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item :command="{type: 'move', row: data}">
                    移动到其他部门
                  </el-dropdown-item>
                  <el-dropdown-item :command="{type: 'delete', row: data}" divided>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </span>
        </el-tree>

        <div v-if="!loading && (!treeData || treeData.length === 0)" class="empty-wrap">
          <el-empty description="暂无数据"/>
        </div>
      </div>
    </el-card>

    <div class="pagination-section">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 部门编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      :modal="true"
      :before-close="handleDialogClose"
      append-to-body
    >
      <el-form ref="editForm" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="部门名称" prop="departmentName">
          <el-input v-model="editForm.departmentName" placeholder="请输入部门名称"/>
        </el-form-item>
        <el-form-item label="部门编码" prop="departmentCode">
          <el-input v-model="editForm.departmentCode" placeholder="请输入部门编码"/>
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="editForm.manager" placeholder="请输入负责人"/>
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
        <el-button type="primary" :loading="dialogSaving" @click="handleDialogConfirm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 移动部门对话框 -->
    <el-dialog
      title="移动部门"
      :visible.sync="moveDepartmentDialogVisible"
      width="520px"
      :modal="true"
      :before-close="handleMoveDepartmentDialogClose"
      append-to-body
    >
      <el-form ref="moveDepartmentForm" :model="moveDepartmentForm" label-width="100px">
        <el-form-item label="目标父部门">
          <el-alert
            type="info"
            show-icon
            title="支持模糊搜索目标父部门；请选择目标父部门，不能选择自身"
          />
          <div style="margin-top:10px;">
            <el-select
              v-model="moveDepartmentForm.targetParentId"
              filterable
              remote
              clearable
              placeholder="请输入部门名称搜索"
              :remote-method="remoteSearchParents"
              :loading="parentRemoteLoading"
              style="width:100%;"
              :popper-class="'parent-select-popper'"
              @change="onParentSelect"
            >
              <el-option
                v-for="item in parentCandidates"
                :key="item.id"
                :label="item.departmentName + (item.departmentCode ? ' — ' + item.departmentCode : '')"
                :value="item.id"
              />
              <el-option
                v-if="!parentCandidates.length && !parentRemoteLoading"
                :key="'no-result'"
                :label="'无匹配结果'"
                :value="null"
                disabled
              />
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleMoveDepartmentDialogClose">取 消</el-button>
        <el-button type="primary" :loading="moveSaving" @click="handleMoveDepartmentConfirm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {adminQueryDepartment, deleteDepartment, listChildren, moveDepartmentTo, saveDepartment} from '@/api/department'

export default {
  name: 'DepartmentTreeView',
  props: {
    companyId: {
      type: Number,
      required: true
    },
    companyName: {
      type: String,
      default: ''
    }
  },
  filters: {
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
    }
  },
  data() {
    return {
      loading: false,
      dialogSaving: false,
      moveSaving: false,
      searchDebounceTimer: null,
      searchForm: {
        likeName: '',
        equalsDepartmentCode: '',
        flagEquals: [],
        equalsCompanyId: null
      },
      sortParams: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      treeData: [],
      dialogVisible: false,
      moveDepartmentDialogVisible: false,
      dialogType: 'add',
      editForm: {
        id: undefined,
        departmentName: '',
        departmentCode: '',
        companyId: null,
        manager: '',
        description: '',
        flag: 1,
        parentId: undefined
      },
      moveDepartmentForm: {
        departmentId: undefined,
        targetParentId: undefined
      },
      parentCandidates: [],
      parentRemoteLoading: false,
      parentRemoteSize: 50,
      parentRemoteLastQuery: '',
      formRules: {
        departmentName: [
          {required: true, message: '请输入部门名称', trigger: 'blur'}
        ],
        flag: [
          {required: true, message: '请选择状态', trigger: 'change'}
        ]
      },
      statusOptions: [
        {code: 1, label: '已激活'},
        {code: 2, label: '禁用'}
      ],
      treeProps: {
        label: 'departmentName',
        children: 'children',
        isLeaf: 'isLeaf'
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogType === 'add' ? '新增部门' : '编辑部门'
    }
  },
  watch: {
    companyId: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.searchForm.equalsCompanyId = newVal
          this.editForm.companyId = newVal
          this.loadDepartmentData()
        }
      }
    }
  },
  beforeDestroy() {
    clearTimeout(this.searchDebounceTimer)
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

    onSearchInput() {
      clearTimeout(this.searchDebounceTimer)
      this.searchDebounceTimer = setTimeout(() => {
        this.currentPage = 1
        this.handleSearch()
      }, 450)
    },

    async loadDepartmentData() {
      this.loading = true
      try {
        const params = {
          ...this.searchForm,
          ...this.sortParams,
          treeRootFlag: 1
        }
        const response = await adminQueryDepartment(params, this.currentPage, this.pageSize)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        this.total = resData.total != null ? resData.total : list.length

        const treeData = []
        for (const department of list) {
          const departmentWithChildren = {
            ...department,
            isLeaf: !(department.childrenCount && department.childrenCount > 0)
          }
          treeData.push(departmentWithChildren)
        }
        this.treeData = treeData
      } catch (error) {
        console.error('加载部门数据失败:', error)
        this.$message.error('加载部门数据失败')
      } finally {
        this.loading = false
      }
    },

    async loadNode(node, resolve) {
      if (node.level === 0) {
        resolve(this.treeData)
        return
      }

      try {
        const response = await listChildren(node.data.id)
        const children = response.data || []
        const childrenWithIsLeaf = []
        for (const child of children) {
          childrenWithIsLeaf.push({
            ...child,
            isLeaf: !(child.childrenCount && child.childrenCount > 0)
          })
        }
        resolve(childrenWithIsLeaf)
      } catch (error) {
        console.error(`加载部门 ${node.data.id} 的子节点失败:`, error)
        resolve([])
      }
    },

    handleSearch() {
      this.currentPage = 1
      this.loadDepartmentData()
    },

    handleReset() {
      this.searchForm = {
        likeName: '',
        equalsDepartmentCode: '',
        flagEquals: [],
        equalsCompanyId: this.companyId,
        treeRootFlag: 1
      }
      this.sortParams = {}
      this.currentPage = 1
      this.pageSize = 10
      this.loadDepartmentData()
    },

    handleAdd() {
      this.dialogType = 'add'
      this.editForm = {
        id: undefined,
        departmentName: '',
        departmentCode: '',
        companyId: this.companyId,
        manager: '',
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
        departmentName: '',
        departmentCode: '',
        companyId: this.companyId,
        manager: '',
        description: '',
        flag: 1,
        parentId: row && row.id ? row.id : undefined
      }
      this.dialogVisible = true
    },

    async handleMoveDepartment(row) {
      this.moveDepartmentForm.departmentId = row.id
      this.moveDepartmentForm.targetParentId = undefined
      this.parentCandidates = []
      this.parentRemoteLastQuery = ''
      this.moveDepartmentDialogVisible = true
      this.remoteSearchParents('')
    },

    handleEdit(row) {
      this.dialogType = 'edit'
      this.editForm = {...row}
      this.dialogVisible = true
    },

    handleDelete(row) {
      this.$confirm(`确定要删除部门 "${row.departmentName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteDepartment(row.id)
          this.$message.success('删除成功')
          this.loadDepartmentData()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      })
    },

    getDepartmentStatusTagType(flag) {
      switch (flag) {
        case 1:
          return 'success'
        case 2:
          return 'warning'
        default:
          return 'info'
      }
    },

    getDepartmentStatusLabel(flag) {
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
              const saveResponse = await saveDepartment({
                departmentName: this.editForm.departmentName,
                departmentCode: this.editForm.departmentCode,
                companyId: this.companyId,
                manager: this.editForm.manager,
                description: this.editForm.description,
                flag: this.editForm.flag
              })
              if (saveResponse.data && saveResponse.data.id) {
                await moveDepartmentTo(this.editForm.parentId, saveResponse.data.id)
                this.$message.success('新增子部门成功')
              }
            } else {
              await saveDepartment(this.editForm)
              this.$message.success(this.dialogType === 'add' ? '新增成功' : '编辑成功')
            }
            this.dialogVisible = false
            this.loadDepartmentData()
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
          this.handleMoveDepartment(command.row)
          break
        case 'delete':
          this.handleDelete(command.row)
          break
      }
    },

    handleMoveDepartmentDialogClose() {
      this.moveDepartmentDialogVisible = false
      this.moveDepartmentForm.targetParentId = undefined
      this.parentCandidates = []
      this.parentRemoteLoading = false
      this.parentRemoteLastQuery = ''
      if (this.$refs.moveDepartmentForm && this.$refs.moveDepartmentForm.resetFields) {
        this.$refs.moveDepartmentForm.resetFields()
      }
    },

    onParentSelect(selectedId) {
      if (!selectedId) return
      if (selectedId === this.moveDepartmentForm.departmentId) {
        this.$message.warning('不能将部门移动到自身')
        this.moveDepartmentForm.targetParentId = undefined
      }
    },

    async remoteSearchParents(query) {
      if (query === this.parentRemoteLastQuery) return
      this.parentRemoteLastQuery = query
      this.parentRemoteLoading = true
      try {
        const params = {
          likeName: query || undefined,
          equalsDepartmentCode: undefined,
          equalsCompanyId: this.companyId,
          pageNum: 1,
          pageSize: this.parentRemoteSize
        }
        const response = await adminQueryDepartment(params)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        this.parentCandidates = list.filter(item => item.id !== this.moveDepartmentForm.departmentId)
      } catch (error) {
        console.error('搜索父部门失败:', error)
        this.$message.error('搜索父部门失败')
        this.parentCandidates = []
      } finally {
        this.parentRemoteLoading = false
      }
    },

    async handleMoveDepartmentConfirm() {
      if (!this.moveDepartmentForm.targetParentId) {
        this.$message.warning('请选择目标父部门')
        return
      }
      if (this.moveDepartmentForm.targetParentId === this.moveDepartmentForm.departmentId) {
        this.$message.warning('不能将部门移动到自身')
        return
      }
      this.moveSaving = true
      try {
        await moveDepartmentTo(this.moveDepartmentForm.targetParentId, this.moveDepartmentForm.departmentId)
        this.$message.success('移动部门成功')
        this.handleMoveDepartmentDialogClose()
        this.loadDepartmentData()
      } catch (error) {
        console.error('移动部门失败:', error)
        const msg = (error && error.response && error.response.data && error.response.data.message) ? error.response.data.message : '移动部门失败'
        this.$message.error(msg)
      } finally {
        this.moveSaving = false
      }
    },

    handlePageChange(page) {
      this.currentPage = page
      this.loadDepartmentData()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadDepartmentData()
    }
  }
}
</script>

<style lang="scss" scoped>
.department-tree-view {
  padding: 0;
  min-height: 400px;
  max-height: 70vh;
  overflow-y: auto;

  .header-section {
    margin-bottom: 16px;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .main-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 16px;
    }
  }

  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #ebeef5;

    ::v-deep .el-card__body {
      padding: 16px;
      min-height: 300px;
    }
  }

  .children-count {
    color: #909399;
    margin-left: 6px;
    font-size: 12px;
  }

  .pagination-section {
    margin-top: 16px;
    text-align: right;
  }

  .empty-wrap {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 60px 0;
    min-height: 250px;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-radius: 8px;
    margin: 16px 0;
  }

  ::v-deep .el-loading-mask {
    background-color: rgba(255, 255, 255, 0.9);
    border-radius: 8px;
  }

  mark {
    background: #ffe58f;
    padding: 0 2px;
    border-radius: 3px;
  }

  ::v-deep .el-tree {
    font-size: 14px;
    line-height: 1.6;
  }

  ::v-deep .el-tree-node {
    margin: 6px 0;
  }

  ::v-deep .el-tree-node__content {
    height: 40px;
    line-height: 40px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;
    border-radius: 6px;
    margin: 3px 0;
  }

  ::v-deep .el-tree-node__content:hover {
    background-color: #f5f7fa;
    transform: translateX(4px);
  }

  ::v-deep .el-tree-node:focus > .el-tree-node__content {
    background-color: #ecf5ff;
  }

  ::v-deep .el-tree-node__expand-icon {
    font-size: 16px;
    color: #606266;
    margin-right: 8px;
  }

  ::v-deep .el-tree-node__expand-icon.expanded {
    transform: rotate(0deg);
  }

  ::v-deep .el-tree-node__expand-icon.is-leaf {
    color: transparent;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    font-weight: 500;
    padding: 0 10px;
    min-height: 36px;
  }

  .tree-node-actions {
    display: none;
    margin-left: 10px;
  }

  .custom-tree-node:hover .tree-node-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .tree-node-actions .el-button {
    font-size: 13px;
    padding: 5px 10px;
    border-radius: 4px;
    transition: all 0.3s ease;
  }

  .tree-node-actions .el-button:hover {
    background-color: #409eff;
    color: #fff;
    transform: translateY(-1px);
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
  }

  mark {
    background: linear-gradient(135deg, #ffd740, #ffab00);
    color: #000;
    padding: 2px 5px;
    border-radius: 3px;
    font-weight: 600;
    box-shadow: 0 1px 2px rgba(255, 171, 0, 0.3);
  }

  .children-count {
    color: #909399;
    margin-left: 6px;
    font-size: 12px;
    background: #f0f2f5;
    padding: 2px 6px;
    border-radius: 10px;
    font-weight: 400;
  }

  .department-detail-card {
    min-width: 300px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 8px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    overflow: hidden;

    .card-header {
      padding: 14px;
      background: linear-gradient(120deg, #67c23a 0%, #529b2e 100%);
      color: white;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
    }

    .card-content {
      padding: 14px;

      .info-item {
        display: flex;
        margin-bottom: 10px;
        align-items: center;

        &:last-child {
          margin-bottom: 0;
        }

        .info-label {
          font-weight: 500;
          color: #606266;
          width: 75px;
          font-size: 13px;
        }

        .info-value {
          flex: 1;
          color: #303133;
          font-size: 13px;

          &.description {
            line-height: 1.5;
          }
        }
      }
    }
  }

  ::v-deep .department-info-tooltip {
    background: transparent !important;
    border: none !important;
    padding: 0 !important;
    box-shadow: none !important;
  }

  @media (max-width: 768px) {
    padding: 10px;
    .main-title {
      font-size: 16px;
    }
    .search-card, .table-card {
      ::v-deep .el-card__body {
        padding: 10px;
      }
    }
  }
}

.parent-select-popper .el-select-dropdown__item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
