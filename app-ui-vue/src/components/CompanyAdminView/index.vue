<template>
  <div class="company-admin">
    <!-- Page Header -->
    <div class="ca-header">
      <div class="ca-header-left">
        <div class="ca-header-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z" /><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2" /><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2" /><path d="M10 6h4" /><path d="M10 10h4" /><path d="M10 14h4" /><path d="M10 18h4" /></svg>
        </div>
        <div>
          <h1 class="ca-title">公司管理</h1>
          <p class="ca-subtitle">管理公司组织架构与部门信息</p>
        </div>
      </div>
      <button class="ca-btn ca-btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
        新增公司
      </button>
    </div>

    <!-- Search -->
    <div class="ca-search">
      <div class="ca-search-inner">
        <div class="ca-search-field">
          <svg class="ca-search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
          <input
            v-model="searchForm.likeName"
            type="text"
            class="ca-input"
            placeholder="搜索公司名称..."
            @input="onSearchInput"
            @keyup.enter="handleSearch"
          >
        </div>
        <div class="ca-search-field ca-search-field-sm">
          <input
            v-model="searchForm.equalsTaxNumber"
            type="text"
            class="ca-input"
            placeholder="税号"
            @input="onSearchInput"
            @keyup.enter="handleSearch"
          >
        </div>
        <div class="ca-search-field ca-search-field-select">
          <el-select
            v-model="searchForm.flagEquals"
            placeholder="状态"
            clearable
            multiple
            collapse-tags
            @change="handleSearch"
          >
            <el-option v-for="item in statusOptions" :key="item.code" :label="item.label" :value="item.code" />
          </el-select>
        </div>
        <div class="ca-search-actions">
          <button class="ca-btn ca-btn-primary ca-btn-sm" @click="handleSearch">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
            搜索
          </button>
          <button class="ca-btn ca-btn-ghost" @click="handleReset">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="1 4 1 10 7 10" /><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" /></svg>
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- Master-Detail Layout -->
    <div class="ca-layout">
      <!-- Left: Company Tree -->
      <transition name="slide-fade">
        <div v-show="!isCollapsed" class="ca-panel ca-panel-left">
          <div class="ca-panel-header">
            <span class="ca-panel-label">公司列表</span>
            <span class="ca-panel-count">{{ total }} 家</span>
            <button class="ca-collapse-btn" title="收起" @click="toggleCollapse">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="15 18 9 12 15 6" />
              </svg>
            </button>
          </div>
          <div v-loading="loading" class="ca-tree-wrap">
            <el-tree
              ref="companyTree"
              :data="treeData"
              :props="treeProps"
              node-key="id"
              lazy
              :load="loadNode"
              :highlight-current="true"
              :expand-on-click-node="false"
              @node-click="handleNodeClick"
            >
              <span slot-scope="{ node, data }" class="ca-tree-row">
                <span class="ca-tree-main">
                  <span class="ca-tree-name" v-html="highlight(data.companyName)" />
                  <span v-if="data.flag" class="ca-status-badge" :class="'ca-status-' + data.flag">
                    {{ getCompanyStatusLabel(data.flag) }}
                  </span>
                  <span v-if="data.childrenCount > 0" class="ca-count-badge">
                    {{ data.childrenCount }} 子
                  </span>
                </span>
                <span class="ca-tree-actions" @click.stop>
                  <button v-if="data.id!==1 && data.flag!==5" class="ca-action-btn" title="编辑" @click="handleEdit(data)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
                  </button>
                  <el-dropdown
                    v-if="data.id!==1 && data.flag!==5"
                    size="small"
                    trigger="click"
                    @command="handleMoreActions"
                  >
                    <button class="ca-action-btn ca-action-more" title="更多">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="1" /><circle cx="19" cy="12" r="1" /><circle cx="5" cy="12" r="1" /></svg>
                    </button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item v-if="data.id!==1 && data.flag!==5" :command="{type: 'manageDept', row: data}" icon="el-icon-office-building">管理部门</el-dropdown-item>
                      <el-dropdown-item v-if="data.id!==1 && data.flag!==5" :command="{type: 'move', row: data}" icon="el-icon-sort">移动到其他公司</el-dropdown-item>
                      <el-dropdown-item v-if="data.id!==1 && data.flag!==5" :command="{type: 'delete', row: data}" divided icon="el-icon-delete">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </el-tree>
            <div v-if="!loading && (!treeData || treeData.length === 0)" class="ca-empty">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" opacity="0.25"><path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z" /><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2" /></svg>
              <p>暂无公司数据</p>
            </div>
          </div>
          <div class="ca-pagination">
            <el-pagination
              :current-page="currentPage"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pageSize"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </transition>

      <!-- 收起状态的浮动触发按钮 -->
      <transition name="fade">
        <button v-if="isCollapsed" class="ca-float-trigger" title="展开公司列表" @click="toggleCollapse">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z" />
            <path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2" />
            <path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2" />
          </svg>
          <span class="ca-float-trigger-text">公司</span>
        </button>
      </transition>

      <!-- Right: Department Panel -->
      <div class="ca-panel ca-panel-right">
        <div v-if="selectedCompany" :key="selectedCompany.id" class="ca-dept-host">
          <department-tree-view
            :company-id="selectedCompany.id"
            :company-name="selectedCompany.companyName"
          />
        </div>
        <div v-else class="ca-placeholder">
          <div class="ca-placeholder-icon">
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity="0.2"><path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z" /><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2" /><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2" /><path d="M10 6h4" /><path d="M10 10h4" /><path d="M10 14h4" /><path d="M10 18h4" /></svg>
          </div>
          <p class="ca-placeholder-text">点击左侧公司查看部门</p>
          <p class="ca-placeholder-hint">选择一家公司，即可在此管理其下的部门架构</p>
        </div>
      </div>
    </div>

    <!-- Company Edit Dialog -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      :before-close="handleDialogClose"
      append-to-body
      class="ca-dialog"
    >
      <el-form ref="editForm" :model="editForm" :rules="formRules" label-width="90px" class="ca-form">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="editForm.companyName" placeholder="请输入公司名称" size="medium" />
        </el-form-item>
        <el-form-item label="税号" prop="taxNumber">
          <el-input v-model="editForm.taxNumber" placeholder="请输入税号" size="medium" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" placeholder="请输入描述" :rows="3" resize="none" />
        </el-form-item>
        <el-form-item label="状态" prop="flag">
          <el-select v-model="editForm.flag" placeholder="请选择状态" size="medium" style="width:100%">
            <el-option v-for="item in statusOptions" :key="item.code" :label="item.label" :value="item.code" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="ca-dialog-footer">
        <button class="ca-btn ca-btn-ghost" @click="handleDialogClose">取消</button>
        <button class="ca-btn ca-btn-primary" :disabled="dialogSaving" @click="handleDialogConfirm">
          <span v-if="dialogSaving" class="ca-spinner" />
          {{ dialogSaving ? '保存中...' : '确认' }}
        </button>
      </div>
    </el-dialog>

    <!-- Move Company Dialog -->
    <el-dialog
      title="移动公司"
      :visible.sync="moveCompanyDialogVisible"
      width="520px"
      :before-close="handleMoveCompanyDialogClose"
      append-to-body
      class="ca-dialog"
    >
      <el-form ref="moveCompanyForm" :model="moveCompanyForm" label-width="90px" class="ca-form">
        <el-form-item label="目标父公司">
          <div class="ca-move-hint">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10" /><line x1="12" y1="16" x2="12" y2="12" /><line x1="12" y1="8" x2="12.01" y2="8" /></svg>
            <span>支持模糊搜索；不能选择自身作为目标</span>
          </div>
          <el-select
            v-model="moveCompanyForm.targetParentId"
            filterable
            remote
            clearable
            placeholder="请输入公司名称或税号搜索"
            :remote-method="remoteSearchParents"
            :loading="parentRemoteLoading"
            style="width:100%; margin-top:8px;"
            :popper-class="'ca-parent-select-popper'"
            @change="onParentSelect"
          >
            <el-option v-for="item in parentCandidates" :key="item.id" :label="item.companyName + (item.taxNumber ? ' — ' + item.taxNumber : '')" :value="item.id" />
            <el-option v-if="!parentCandidates.length && !parentRemoteLoading" :key="'no-result'" label="无匹配结果" :value="null" disabled />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="ca-dialog-footer">
        <button class="ca-btn ca-btn-ghost" @click="handleMoveCompanyDialogClose">取消</button>
        <button class="ca-btn ca-btn-primary" :disabled="moveSaving" @click="handleMoveCompanyConfirm">
          <span v-if="moveSaving" class="ca-spinner" />
          {{ moveSaving ? '移动中...' : '确认移动' }}
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { adminQueryCompany, deleteCompany, listChildren, moveCompanyTo, saveCompany } from '@/api/company'
import DepartmentTreeView from '@/components/DepartmentTreeView/index.vue'

export default {
  name: 'CompanyAdminView',
  components: { DepartmentTreeView },
  data() {
    return {
      isCollapsed: false,
      loading: false,
      dialogSaving: false,
      moveSaving: false,
      searchDebounceTimer: null,
      searchForm: { likeName: '', equalsTaxNumber: '', flagEquals: [], treeRootFlag: 1 },
      sortParams: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      treeData: [],
      selectedCompany: null,
      dialogVisible: false,
      moveCompanyDialogVisible: false,
      dialogType: 'add',
      editForm: { id: undefined, companyName: '', taxNumber: '', description: '', flag: 1, parentId: undefined },
      moveCompanyForm: { companyId: undefined, targetParentId: undefined },
      parentCandidates: [],
      parentRemoteLoading: false,
      parentRemoteSize: 50,
      parentRemoteLastQuery: '',
      formRules: {
        companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
        taxNumber: [{ required: true, message: '请输入税号', trigger: 'blur' }],
        flag: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      statusOptions: [
        { code: 1, label: '已激活' },
        { code: 2, label: '禁用' },
        { code: 5, label: '系统内置' }
      ],
      treeProps: { label: 'companyName', children: 'children', isLeaf: 'isLeaf' }
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
  beforeDestroy() {
    clearTimeout(this.searchDebounceTimer)
  },
  methods: {
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed
    },
    escapeHtml(str = '') {
      return String(str).replace(/[&<>"'\/]/g, function(s) {
        const entityMap = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;', '/': '&#x2F;' }
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
      this.searchDebounceTimer = setTimeout(() => { this.currentPage = 1; this.handleSearch() }, 450)
    },
    async loadCompanyData() {
      this.loading = true
      try {
        const params = { ...this.searchForm, ...this.sortParams, treeRootFlag: 1 }
        const response = await adminQueryCompany(params, this.currentPage, this.pageSize)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        this.total = resData.total != null ? resData.total : list.length
        this.treeData = list.map(c => ({ ...c, isLeaf: !(c.childrenCount && c.childrenCount > 0) }))
      } catch (error) {
        console.error('加载公司数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadNode(node, resolve) {
      if (node.level === 0) { resolve(this.treeData); return }
      try {
        const response = await listChildren(node.data.id)
        const children = response.data || []
        resolve(children.map(c => ({ ...c, isLeaf: !(c.childrenCount && c.childrenCount > 0) })))
      } catch (error) {
        console.error(`加载子节点失败:`, error)
        resolve([])
      }
    },
    handleNodeClick(data) {
      if (this.selectedCompany && this.selectedCompany.id === data.id) {
        this.selectedCompany = null
      } else {
        this.selectedCompany = { id: data.id, companyName: data.companyName }
      }
    },
    handleSearch() { this.currentPage = 1; this.loadCompanyData() },
    handleReset() {
      this.searchForm = { likeName: '', equalsTaxNumber: '', flagEquals: [], treeRootFlag: 1 }
      this.sortParams = {}
      this.currentPage = 1
      this.pageSize = 10
      this.loadCompanyData()
    },
    handleAdd() {
      this.dialogType = 'add'
      this.editForm = { id: undefined, companyName: '', taxNumber: '', description: '', flag: 1, parentId: undefined }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogType = 'edit'
      this.editForm = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm(`确定要删除公司 "${row.companyName}" 吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async() => {
        try {
          await deleteCompany(row.id)
          // 消息提示由后端统一控制
          if (this.selectedCompany && this.selectedCompany.id === row.id) this.selectedCompany = null
          this.loadCompanyData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      })
    },
    getCompanyStatusLabel(flag) {
      const s = this.statusOptions.find(i => i.code === flag)
      return s ? s.label : '未知'
    },
    handleDialogClose() {
      this.dialogVisible = false
      if (this.$refs.editForm && this.$refs.editForm.resetFields) this.$refs.editForm.resetFields()
    },
    async handleDialogConfirm() {
      this.$refs.editForm.validate(async(valid) => {
        if (!valid) return
        this.dialogSaving = true
        try {
          if (this.dialogType === 'add' && this.editForm.parentId) {
            const res = await saveCompany({ companyName: this.editForm.companyName, taxNumber: this.editForm.taxNumber, description: this.editForm.description, flag: this.editForm.flag })
            if (res.data && res.data.id) await moveCompanyTo(this.editForm.parentId, res.data.id)
            // 消息提示由后端统一控制
          } else {
            await saveCompany(this.editForm)
            // 消息提示由后端统一控制
          }
          this.dialogVisible = false
          this.loadCompanyData()
        } catch (error) {
          console.error('保存失败:', error)
          // 消息提示由后端统一控制
        } finally {
          this.dialogSaving = false
        }
      })
    },
    handleMoreActions(command) {
      switch (command.type) {
        case 'manageDept':
          this.selectedCompany = { id: command.row.id, companyName: command.row.companyName }
          break
        case 'move':
          this.handleMoveCompany(command.row)
          break
        case 'delete':
          this.handleDelete(command.row)
          break
      }
    },
    async handleMoveCompany(row) {
      this.moveCompanyForm.companyId = row.id
      this.moveCompanyForm.targetParentId = undefined
      this.parentCandidates = []
      this.parentRemoteLastQuery = ''
      this.moveCompanyDialogVisible = true
      this.remoteSearchParents('')
    },
    handleMoveCompanyDialogClose() {
      this.moveCompanyDialogVisible = false
      this.moveCompanyForm.targetParentId = undefined
      this.parentCandidates = []
      this.parentRemoteLoading = false
      this.parentRemoteLastQuery = ''
      if (this.$refs.moveCompanyForm && this.$refs.moveCompanyForm.resetFields) this.$refs.moveCompanyForm.resetFields()
    },
    onParentSelect(selectedId) {
      if (selectedId && selectedId === this.moveCompanyForm.companyId) {
        // 消息提示由后端统一控制
        this.moveCompanyForm.targetParentId = undefined
      }
    },
    async remoteSearchParents(query) {
      if (query === this.parentRemoteLastQuery) return
      this.parentRemoteLastQuery = query
      this.parentRemoteLoading = true
      try {
        const params = { likeName: query || undefined, equalsTaxNumber: query && /^\d+$/.test(query) ? query : undefined, page: 1, size: this.parentRemoteSize, treeRootFlag: undefined }
        const response = await adminQueryCompany(params)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        this.parentCandidates = list.filter(item => item.id !== this.moveCompanyForm.companyId)
      } catch (error) {
        this.parentCandidates = []
      } finally {
        this.parentRemoteLoading = false
      }
    },
    async handleMoveCompanyConfirm() {
      if (!this.moveCompanyForm.targetParentId) {
        // 消息提示由后端统一控制
        return
      }
      if (this.moveCompanyForm.targetParentId === this.moveCompanyForm.companyId) {
        // 消息提示由后端统一控制
        return
      }
      this.moveSaving = true
      try {
        await moveCompanyTo(this.moveCompanyForm.targetParentId, this.moveCompanyForm.companyId)
        // 消息提示由后端统一控制
        this.handleMoveCompanyDialogClose()
        this.loadCompanyData()
      } catch (error) {
        console.error('移动公司失败:', error)
      } finally {
        this.moveSaving = false
      }
    },
    handlePageChange(page) { this.currentPage = page; this.loadCompanyData() },
    handleSizeChange(size) { this.pageSize = size; this.currentPage = 1; this.loadCompanyData() }
  }
}
</script>

<style lang="scss" scoped>
/* ==================== Design Tokens ==================== */
.company-admin {
  --ca-primary: #D97706;
  --ca-primary-light: #F59E0B;
  --ca-primary-bg: #FFFBEB;
  --ca-primary-border: #FDE68A;
  --ca-text: #1E293B;
  --ca-text-secondary: #64748B;
  --ca-text-muted: #94A3B8;
  --ca-surface: #FFFFFF;
  --ca-bg: #F8FAFC;
  --ca-border: #E2E8F0;
  --ca-border-light: #F1F5F9;
  --ca-success: #059669;
  --ca-success-bg: #ECFDF5;
  --ca-warning: #D97706;
  --ca-danger: #DC2626;
  --ca-danger-bg: #FEF2F2;
  --ca-radius: 10px;
  --ca-radius-sm: 6px;
  --ca-transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Noto Sans SC', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: var(--ca-text);
  background: var(--ca-bg);
  padding: 20px 24px;
  min-height: calc(100vh - 84px);
}

/* ==================== Header ==================== */
.ca-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.ca-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ca-header-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--ca-radius-sm);
  background: var(--ca-primary-bg);
  border: 1px solid var(--ca-primary-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ca-primary);
  flex-shrink: 0;
}

.ca-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--ca-text);
  margin: 0;
  letter-spacing: -0.3px;
  line-height: 1.2;
}

.ca-subtitle {
  font-size: 12px;
  color: var(--ca-text-muted);
  margin: 2px 0 0;
  font-weight: 400;
}

/* ==================== Buttons ==================== */
.ca-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--ca-transition);
  white-space: nowrap;
  line-height: 1;
}

.ca-btn-primary {
  background: var(--ca-primary);
  color: #fff;
  &:hover:not(:disabled) {
    background: var(--ca-primary-light);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(217, 119, 6, 0.35);
  }
  &:active:not(:disabled) { transform: translateY(0); }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
}

.ca-btn-ghost {
  background: transparent;
  color: var(--ca-text-secondary);
  border: 1px solid var(--ca-border);
  &:hover { color: var(--ca-text); border-color: var(--ca-text-muted); background: var(--ca-border-light); }
}

.ca-btn-sm {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 5px;
}

/* ==================== Search ==================== */
.ca-search {
  background: var(--ca-surface);
  border-radius: var(--ca-radius);
  border: 1px solid var(--ca-border);
  padding: 12px 16px;
  margin-bottom: 16px;
  transition: border-color var(--ca-transition);
  &:focus-within { border-color: var(--ca-primary-border); }
}

.ca-search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ca-search-field {
  position: relative;
  flex: 1;
  min-width: 180px;
}
.ca-search-field-sm { min-width: 140px; flex: 0.6; }
.ca-search-field-select { min-width: 160px; flex: 0.8; }

.ca-search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--ca-text-muted);
  pointer-events: none;
}

.ca-input {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--ca-border);
  border-radius: var(--ca-radius-sm);
  font-size: 13px;
  font-family: inherit;
  color: var(--ca-text);
  background: var(--ca-bg);
  transition: all var(--ca-transition);
  outline: none;
  &:focus {
    border-color: var(--ca-primary);
    background: var(--ca-surface);
    box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.1);
  }
  &::placeholder { color: var(--ca-text-muted); }
}

.ca-search-field:first-child .ca-input { padding-left: 38px; }

.ca-search-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

/* ==================== Master-Detail Layout ==================== */
.ca-layout {
  display: flex;
  gap: 16px;
  min-height: 480px;
  position: relative;
}

.ca-panel {
  background: var(--ca-surface);
  border-radius: var(--ca-radius);
  border: 1px solid var(--ca-border);
  overflow: hidden;
}

.ca-panel-left {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.ca-panel-right {
  flex: 1;
  min-width: 0;
}

/* ==================== Float Trigger Button ==================== */
.ca-float-trigger {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 10px;
  background: var(--ca-surface);
  border: 1px solid var(--ca-border);
  border-left: none;
  border-radius: 0 var(--ca-radius-sm) var(--ca-radius-sm) 0;
  color: var(--ca-text-secondary);
  cursor: pointer;
  transition: all var(--ca-transition);
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.05);

  &:hover {
    background: var(--ca-primary-bg);
    color: var(--ca-primary);
    border-color: var(--ca-primary-border);
    padding-right: 14px;
    box-shadow: 4px 0 16px rgba(217, 119, 6, 0.15);
  }
}

.ca-float-trigger-text {
  font-size: 11px;
  font-weight: 600;
  writing-mode: vertical-rl;
  letter-spacing: 2px;
}

/* ==================== Transitions ==================== */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}
.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}

.ca-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--ca-border-light);
}

.ca-panel-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ca-text);
  letter-spacing: -0.1px;
}

.ca-panel-count {
  font-size: 12px;
  font-weight: 500;
  color: var(--ca-text-muted);
  background: var(--ca-bg);
  padding: 3px 10px;
  border-radius: 10px;
  border: 1px solid var(--ca-border);
}

/* ==================== Collapse Button ==================== */
.ca-collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--ca-border);
  border-radius: 8px;
  background: var(--ca-surface);
  color: var(--ca-text-secondary);
  cursor: pointer;
  transition: all var(--ca-transition);
  padding: 0;
  margin-left: auto;
  flex-shrink: 0;

  &:hover {
    border-color: var(--ca-primary);
    color: var(--ca-primary);
    background: var(--ca-primary-bg);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }
}

/* ==================== Tree ==================== */
.ca-tree-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 8px 6px;
  min-height: 200px;
  position: relative;
}

.ca-tree-row {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  border-radius: var(--ca-radius-sm);
  transition: background var(--ca-transition);
  min-height: 42px;
}

.ca-tree-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.ca-tree-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--ca-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  ::v-deep mark {
    background: var(--ca-primary-bg);
    color: var(--ca-primary);
    padding: 1px 4px;
    border-radius: 3px;
    font-weight: 600;
    border: 1px solid var(--ca-primary-border);
  }
}

.ca-status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
  line-height: 1.3;
  flex-shrink: 0;

  &.ca-status-1 {
    color: var(--ca-success);
    background: var(--ca-success-bg);
    border: 1px solid #A7F3D0;
  }
  &.ca-status-2 {
    color: #B45309;
    background: #FEF3C7;
    border: 1px solid #FDE68A;
  }
  &.ca-status-5 {
    color: #6366F1;
    background: #EEF2FF;
    border: 1px solid #C7D2FE;
  }
}

.ca-count-badge {
  font-size: 11px;
  font-weight: 600;
  color: var(--ca-text-muted);
  background: var(--ca-border-light);
  padding: 2px 7px;
  border-radius: 10px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* ==================== Tree Actions ==================== */
.ca-tree-actions {
  display: flex;
  align-items: center;
  gap: 3px;
  opacity: 0;
  transform: translateX(8px);
  transition: all var(--ca-transition);
  flex-shrink: 0;
  margin-left: 8px;
}

.ca-tree-row:hover .ca-tree-actions {
  opacity: 1;
  transform: translateX(0);
}

/*
 * 触摸设备没有 hover 态：行内操作入口必须常显，
 * 否则编辑/删除按钮永远无法被触发（功能不可用，不只是体验问题）
 */
@media (hover: none) {
  .ca-tree-actions {
    opacity: 1;
    transform: translateX(0);
  }
}

.ca-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid var(--ca-border);
  border-radius: 6px;
  background: var(--ca-surface);
  color: var(--ca-text-secondary);
  cursor: pointer;
  transition: all var(--ca-transition);
  padding: 0;

  &:hover {
    border-color: var(--ca-primary);
    color: var(--ca-primary);
    background: var(--ca-primary-bg);
  }
}

.ca-action-more {
  width: auto;
  padding: 0 6px;
}

/* ==================== Empty State ==================== */
.ca-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  color: var(--ca-text-muted);
  p { margin: 10px 0 0; font-size: 14px; }
}

/* ==================== Pagination ==================== */
.ca-pagination {
  padding: 12px 20px;
  border-top: 1px solid var(--ca-border-light);
  display: flex;
  justify-content: flex-end;
  overflow-x: auto;
  white-space: nowrap;
}

/* Pagination Element Overrides */
::v-deep .ca-pagination .el-pagination {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  font-size: 12px;
  padding: 0;
}

::v-deep .ca-pagination .el-pagination__total {
  font-size: 12px;
  color: var(--ca-text-muted);
  margin-right: 12px;
}

::v-deep .ca-pagination .el-pagination__sizes {
  margin-right: 12px;
}

::v-deep .ca-pagination .el-pagination .el-select .el-input {
  width: 100px;
}

::v-deep .ca-pagination .el-pagination .el-select .el-input__inner {
  height: 28px;
  line-height: 28px;
  font-size: 12px;
  border-radius: var(--ca-radius-sm);
}

::v-deep .ca-pagination .el-pager {
  display: flex;
  align-items: center;
  gap: 2px;
}

::v-deep .ca-pagination .el-pager li {
  min-width: 28px;
  height: 28px;
  line-height: 28px;
  font-size: 12px;
  border-radius: var(--ca-radius-sm);
  background: transparent;
  color: var(--ca-text-secondary);

  &:hover {
    color: var(--ca-primary);
    background: var(--ca-primary-bg);
  }

  &.active {
    background: var(--ca-primary);
    color: #fff;
  }
}

::v-deep .ca-pagination .btn-prev,
::v-deep .ca-pagination .btn-next {
  width: 28px;
  height: 28px;
  line-height: 28px;
  border-radius: var(--ca-radius-sm);
  background: transparent;
  color: var(--ca-text-secondary);

  &:hover {
    color: var(--ca-primary);
    background: var(--ca-primary-bg);
  }

  &:disabled {
    color: var(--ca-text-muted);
    opacity: 0.5;
  }
}

/* ==================== Right Panel: Department Host ==================== */
.ca-dept-host {
  height: 100%;
}

.ca-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  padding: 40px;
}

.ca-placeholder-icon {
  margin-bottom: 16px;
  color: var(--ca-text-muted);
}

.ca-placeholder-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--ca-text-secondary);
  margin: 0 0 6px 0;
}

.ca-placeholder-hint {
  font-size: 13px;
  color: var(--ca-text-muted);
  margin: 0;
}

/* ==================== Element Overrides ==================== */
::v-deep .el-tree {
  font-size: 14px;
  line-height: 1;
  color: var(--ca-text);
}
::v-deep .el-tree-node { margin: 2px 0; }
::v-deep .el-tree-node__content {
  height: auto;
  min-height: 42px;
  line-height: normal;
  border-radius: var(--ca-radius-sm);
  margin: 0;
  padding: 0;
  transition: background var(--ca-transition);
  &:hover { background: transparent; }
}
::v-deep .el-tree-node:focus > .el-tree-node__content { background: transparent; }
::v-deep .el-tree-node.is-current > .el-tree-node__content {
  background: var(--ca-primary-bg) !important;
  border-right: 3px solid var(--ca-primary);
}
::v-deep .el-tree-node__expand-icon {
  font-size: 14px;
  color: var(--ca-text-muted);
  margin-right: 4px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--ca-transition);
  border-radius: 4px;
  &:not(.is-leaf):hover { color: var(--ca-primary); background: var(--ca-primary-bg); }
}
::v-deep .el-tree-node__expand-icon.expanded { transform: rotate(90deg); }
::v-deep .el-tree-node__expand-icon.is-leaf { color: transparent; cursor: default; }

::v-deep .el-loading-mask {
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(2px);
  border-radius: var(--ca-radius);
}

/* Search Select */
::v-deep .ca-search-field-select .el-input__inner {
  height: 36px;
  line-height: 36px;
  border-radius: var(--ca-radius-sm);
  border-color: var(--ca-border);
  background: var(--ca-bg);
  font-size: 13px;
}
::v-deep .ca-search-field-select .el-tag {
  background: var(--ca-primary-bg);
  border-color: var(--ca-primary-border);
  color: var(--ca-primary);
}

/* ==================== Dialog ==================== */
::v-deep .ca-dialog {
  .el-dialog {
    border-radius: var(--ca-radius);
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  }
  .el-dialog__header {
    padding: 20px 24px 16px;
    margin: 0;
    border-bottom: 1px solid var(--ca-border-light);
    background: var(--ca-surface);
  }
  .el-dialog__title { font-size: 16px; font-weight: 700; color: var(--ca-text); letter-spacing: -0.2px; }
  .el-dialog__headerbtn { top: 20px; right: 20px; }
  .el-dialog__body { padding: 0; }
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid var(--ca-border-light);
    background: var(--ca-bg);
  }
}

.ca-form {
  padding: 24px;
  ::v-deep .el-form-item { margin-bottom: 20px; &:last-child { margin-bottom: 0; } }
  ::v-deep .el-form-item__label { font-size: 13px; font-weight: 600; color: var(--ca-text-secondary); }
  ::v-deep .el-input__inner,
  ::v-deep .el-textarea__inner {
    border-radius: var(--ca-radius-sm);
    border-color: var(--ca-border);
    font-size: 13px;
    transition: all var(--ca-transition);
    &:focus { border-color: var(--ca-primary); box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.08); }
  }
}

.ca-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.ca-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: ca-spin 0.6s linear infinite;
  margin-right: 6px;
}

@keyframes ca-spin { to { transform: rotate(360deg); } }

/* Move Hint */
.ca-move-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #EFF6FF;
  border-radius: var(--ca-radius-sm);
  border: 1px solid #BFDBFE;
  font-size: 12px;
  color: #1D4ED8;
}

/* Parent Select Popper */
.ca-parent-select-popper .el-select-dropdown__item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ==================== Responsive ==================== */
@media (max-width: 1024px) {
  .ca-layout {
    flex-direction: column;
  }
  .ca-panel-left {
    width: 100%;
  }
  .ca-float-trigger {
    left: 50%;
    top: auto;
    bottom: 0;
    transform: translateX(-50%);
    flex-direction: row;
    border-radius: var(--ca-radius-sm) var(--ca-radius-sm) 0 0;
    border: 1px solid var(--ca-border);
    border-bottom: none;
    padding: 10px 14px;
  }
  .ca-float-trigger-text {
    writing-mode: horizontal-tb;
    letter-spacing: 1px;
  }
}
@media (max-width: 768px) {
  .company-admin { padding: 16px; }
  .ca-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .ca-title { font-size: 22px; }
  .ca-search-inner { flex-direction: column; }
  .ca-search-field, .ca-search-field-sm, .ca-search-field-select { min-width: 100%; }
  .ca-search-actions { width: 100%; margin-left: 0; }
  /* 上下堆叠后取消固定最小高度，避免小屏大面积留白 */
  .ca-layout { min-height: auto; }
}

/* 横屏手机：压缩占位区高度，减少纵向滚动 */
@media (max-height: 480px) {
  .ca-placeholder { min-height: 300px; }
}
</style>

<style lang="scss">
.ca-parent-select-popper .el-select-dropdown__item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
