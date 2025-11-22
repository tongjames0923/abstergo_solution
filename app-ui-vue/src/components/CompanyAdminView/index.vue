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

    <!-- 树形区域 -->
    <el-card class="table-card" shadow="never">
      <div v-loading="loading" style="min-height:200px; position:relative;">
        <el-tree
          :data="treeData"
          :props="treeProps"
          node-key="id"
          lazy
          :load="loadNode"
          ref="companyTree"
        >
          <span class="custom-tree-node" slot-scope="{ node, data }">
            <span>
              <el-tooltip class="item" placement="right" effect="light" popper-class="company-info-tooltip">
                <span class="tree-node-title">{{ data.companyName }}</span>
                <template slot="content">
                  <div class="company-detail-card">
                    <div class="card-header">
                      <h3>公司信息</h3>
                    </div>
                    <div class="card-content">
                      <el-row :gutter="12">
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">公司名称:</span>
                            <span class="info-value">{{ data.companyName }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">税号:</span>
                            <span class="info-value tax-number">{{ data.taxNumber }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">状态:</span>
                            <el-tag size="small" :type="getCompanyStatusTagType(data.flag)">
                              {{ getCompanyStatusLabel(data.flag) }}
                            </el-tag>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">添加时间:</span>
                            <span class="info-value">{{ data.createTime }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="info-item">
                            <span class="info-label">更新时间:</span>
                            <span class="info-value">{{ data.updateTime }}</span>
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
              <el-dropdown @command="handleMoreActions" size="mini">
                <el-button type="text" size="mini">
                  更多<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item :command="{type: 'move', row: data}">
                    移动到其他公司
                  </el-dropdown-item>
                  <el-dropdown-item v-if="data.id!==1" :command="{type: 'delete', row: data}" divided>
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
        :current-page="pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageSizeChange"
        @current-change="handlePageNumChange"
      />
    </div>

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
      treeData: [], // 树形数据
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
      ],
      treeProps: {
        label: 'companyName',
        children: 'children',
        isLeaf: 'isLeaf'
      }
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
          treeRootFlag: 1
        }
        const response = await adminQueryCompany(params, this.currentPage, this.pageSize)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        this.total = resData.total != null ? resData.total : list.length

        // 转换为树形结构数据
        const treeData = []
        for (const company of list) {
          const companyWithChildren = {
            ...company,
            isLeaf: !(company.childrenCount && company.childrenCount > 0)
          }
          treeData.push(companyWithChildren)
        }
        this.treeData = treeData
      } catch (error) {
        console.error('加载公司数据失败:', error)
        this.$message.error('加载公司数据失败')
      } finally {
        this.loading = false
      }
    },

    async loadNode(node, resolve) {
      // 根节点
      if (node.level === 0) {
        resolve(this.treeData);
        return;
      }

      try {
        const response = await listChildren(node.data.id);
        const children = response.data || [];
        const childrenWithIsLeaf = [];
        for (const child of children) {
          childrenWithIsLeaf.push({
            ...child,
            isLeaf: !(child.childrenCount && child.childrenCount > 0)
          });
        }
        resolve(childrenWithIsLeaf);
      } catch (error) {
        console.error(`加载公司 ${node.data.id} 的子节点失败:`, error);
        resolve([]);
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
          await deleteCompany(row.id);
          this.$message.success('删除成功');
          // 重新加载整个树
          this.loadCompanyData();
        } catch (error) {
          console.error('删除失败:', error);
          this.$message.error('删除失败');
        }
      });
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
          this.dialogSaving = true;
          try {
            if (this.dialogType === 'add' && this.editForm.parentId) {
              const saveResponse = await saveCompany({
                companyName: this.editForm.companyName,
                taxNumber: this.editForm.taxNumber,
                description: this.editForm.description,
                flag: this.editForm.flag
              });
              if (saveResponse.data && saveResponse.data.id) {
                await moveCompanyTo(this.editForm.parentId, saveResponse.data.id);
                this.$message.success('新增子公司成功');
              }
            } else {
              await saveCompany(this.editForm);
              this.$message.success(this.dialogType === 'add' ? '新增成功' : '编辑成功');
            }
            this.dialogVisible = false;
            // 重新加载整个树
            this.loadCompanyData();
          } catch (error) {
            console.error('保存失败:', error);
            this.$message.error('保存失败');
          } finally {
            this.dialogSaving = false;
          }
        }
      });
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
        await moveCompanyTo(this.moveCompanyForm.targetParentId, this.moveCompanyForm.companyId);
        this.$message.success('移动公司成功');
        this.handleMoveCompanyDialogClose();
        // 重新加载整个树
        this.loadCompanyData();
      } catch (error) {
        console.error('移动公司失败:', error);
        // 如果后端返回子孙校验失败，应显示后端错误信息
        const msg = (error && error.response && error.response.data && error.response.data.message) ? error.response.data.message : '移动公司失败';
        this.$message.error(msg);
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
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border: 1px solid #ebeef5;

    ::v-deep .el-card__body {
      padding: 20px;
      min-height: 400px;
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
    padding: 80px 0;
    min-height: 300px;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-radius: 8px;
    margin: 20px 0;
  }

  /* 加载动画优化 */
  ::v-deep .el-loading-mask {
    background-color: rgba(255, 255, 255, 0.9);
    border-radius: 8px;
  }

  ::v-deep .el-loading-spinner .circular {
    width: 42px;
    height: 42px;
  }

  ::v-deep .el-loading-spinner .path {
    stroke: #409eff;
    stroke-width: 3;
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

  /* 树形组件样式优化 */
  ::v-deep .el-tree {
    font-size: 16px;
    line-height: 1.6;
  }

  ::v-deep .el-tree-node {
    margin: 8px 0;
  }

  ::v-deep .el-tree-node__content {
    height: 44px;
    line-height: 44px;
    font-size: 16px;
    font-weight: 500;
    transition: all 0.3s ease;
    border-radius: 6px;
    margin: 4px 0;
  }

  ::v-deep .el-tree-node__content:hover {
    background-color: #f5f7fa;
    transform: translateX(4px);
  }

  ::v-deep .el-tree-node:focus > .el-tree-node__content {
    background-color: #ecf5ff;
  }

  ::v-deep .el-tree-node__expand-icon {
    font-size: 18px;
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
    font-size: 16px;
    font-weight: 500;
    padding: 0 12px;
    min-height: 40px;
  }

  .tree-node-actions {
    display: none;
    margin-left: 12px;
  }

  .custom-tree-node:hover .tree-node-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .tree-node-actions .el-button {
    font-size: 14px;
    padding: 6px 12px;
    border-radius: 4px;
    transition: all 0.3s ease;
  }

  .tree-node-actions .el-button:hover {
    background-color: #409eff;
    color: #fff;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }

  /* 高亮搜索关键词样式 */
  mark {
    background: linear-gradient(135deg, #ffd740, #ffab00);
    color: #000;
    padding: 2px 6px;
    border-radius: 4px;
    font-weight: 600;
    box-shadow: 0 1px 3px rgba(255, 171, 0, 0.3);
  }

  /* 子节点数量样式 */
  .children-count {
    color: #909399;
    margin-left: 8px;
    font-size: 14px;
    background: #f0f2f5;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: 400;
  }

  /* 公司详情卡片样式 */
  .company-detail-card {
    min-width: 320px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 10px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    overflow: hidden;

    .card-header {
      padding: 16px;
      background: linear-gradient(120deg, #409eff 0%, #1a73e8 100%);
      color: white;

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .card-content {
      padding: 16px;

      .info-item {
        display: flex;
        margin-bottom: 12px;
        align-items: center;

        &:last-child {
          margin-bottom: 0;
        }

        .info-label {
          font-weight: 500;
          color: #606266;
          width: 80px;
          font-size: 14px;
        }

        .info-value {
          flex: 1;
          color: #303133;
          font-size: 14px;

          &.tax-number {
            font-family: 'Courier New', monospace;
            letter-spacing: 1px;
          }

          &.description {
            line-height: 1.5;
          }
        }
      }
    }
  }

  /* 自定义 tooltip 样式 */
  ::v-deep .company-info-tooltip {
    background: transparent !important;
    border: none !important;
    padding: 0 !important;
    box-shadow: none !important;
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
