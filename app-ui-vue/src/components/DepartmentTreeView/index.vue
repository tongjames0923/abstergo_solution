<template>
  <div class="dept-view">
    <!-- 页面头部 -->
    <div class="dept-hero">
      <div class="hero-left">
        <div class="hero-badge">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
        </div>
        <div>
          <h2 class="hero-title">部门管理</h2>
          <p class="hero-subtitle">{{ companyName }}</p>
        </div>
      </div>
      <button class="dept-btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        <span>新增部门</span>
      </button>
    </div>

    <!-- 分栏布局 -->
    <div class="dept-split">
      <!-- 左侧面板：搜索 + 树 -->
      <div class="dept-left">
        <!-- 搜索区域 -->
        <div class="dept-search">
          <div class="dept-search-inner">
            <div class="search-field">
              <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              <input
                v-model="searchForm.likeName"
                type="text"
                class="search-input"
                placeholder="搜索部门名称..."
                @input="onSearchInput"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="search-field">
              <input
                v-model="searchForm.equalsDepartmentCode"
                type="text"
                class="search-input"
                placeholder="部门编码"
                @input="onSearchInput"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="search-field search-select-field">
              <el-select
                v-model="searchForm.flagEquals"
                placeholder="状态筛选"
                clearable
                multiple
                collapse-tags
                @change="handleSearch"
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.code"
                  :label="item.label"
                  :value="item.code"
                />
              </el-select>
            </div>
            <div class="search-actions">
              <button class="dept-btn-primary dept-btn-sm" @click="handleSearch">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                搜索
              </button>
              <button class="dept-btn-ghost" @click="handleReset">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>
                重置
              </button>
            </div>
          </div>
        </div>

        <!-- 树形区域 -->
        <div class="dept-tree-wrap">
          <div v-loading="loading" class="dept-tree-inner">
            <el-tree
              ref="departmentTree"
              :data="treeData"
              :props="treeProps"
              node-key="id"
              lazy
              :load="loadNode"
              @node-click="handleNodeClick"
            >
              <span slot-scope="{ node, data }" class="tree-row">
                <span class="tree-row-main">
                  <el-tooltip class="item" placement="right" effect="light" popper-class="dept-tooltip">
                    <span class="tree-name">{{ data.departmentName }}</span>
                    <template slot="content">
                      <div class="detail-card">
                        <div class="detail-head">
                          <span class="detail-head-icon">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
                          </span>
                          部门详情
                        </div>
                        <div class="detail-body">
                          <div class="detail-row">
                            <span class="detail-label">名称</span>
                            <span class="detail-val">{{ data.departmentName }}</span>
                          </div>
                          <div class="detail-row">
                            <span class="detail-label">编码</span>
                            <span class="detail-val">{{ data.departmentCode || '—' }}</span>
                          </div>
                          <div class="detail-row">
                            <span class="detail-label">负责人</span>
                            <span class="detail-val">{{ data.manager || '—' }}</span>
                          </div>
                          <div class="detail-row">
                            <span class="detail-label">状态</span>
                            <span class="detail-val">
                              <span class="status-dot" :class="'dot-' + data.flag"></span>
                              {{ getDepartmentStatusLabel(data.flag) }}
                            </span>
                          </div>
                          <div class="detail-row">
                            <span class="detail-label">添加时间</span>
                            <span class="detail-val">{{ data.createTime | formatDate }}</span>
                          </div>
                          <div class="detail-row">
                            <span class="detail-label">更新时间</span>
                            <span class="detail-val">{{ data.updateTime | formatDate }}</span>
                          </div>
                          <div class="detail-row detail-row-full">
                            <span class="detail-label">描述</span>
                            <span class="detail-val">{{ data.description || '—' }}</span>
                          </div>
                        </div>
                      </div>
                    </template>
                  </el-tooltip>
                  <span v-if="data.childrenCount && data.childrenCount > 0" class="dept-count">{{ data.childrenCount }}</span>
                </span>
                <span class="tree-row-actions">
                  <button class="action-btn action-edit" @click="handleEdit(data)" title="编辑">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    <span>编辑</span>
                  </button>
                  <el-dropdown size="small" @command="handleMoreActions" trigger="click">
                    <button class="action-btn action-more" title="更多">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
                    </button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{type: 'move', row: data}" icon="el-icon-sort">
                        移动到其他部门
                      </el-dropdown-item>
                      <el-dropdown-item :command="{type: 'delete', row: data}" divided icon="el-icon-delete">
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </el-tree>

            <div v-if="!loading && (!treeData || treeData.length === 0)" class="dept-empty">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" opacity="0.3"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
              <p>暂无部门数据</p>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="dept-pagination">
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
      </div>

      <!-- 右侧面板：员工管理 -->
      <div class="dept-right">
        <div v-if="!selectedDepartment" class="emp-placeholder">
          <div class="emp-placeholder-inner">
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity="0.2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            <p class="emp-placeholder-title">选择一个部门</p>
            <p class="emp-placeholder-desc">点击左侧部门树中的任意部门，即可查看和管理该部门的员工</p>
          </div>
        </div>

        <template v-else>
          <!-- 员工面板头部 -->
          <div class="emp-panel-header">
            <div class="emp-panel-title-area">
              <div class="emp-dept-badge">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              </div>
              <div>
                <h3 class="emp-panel-title">{{ selectedDepartment.departmentName }}</h3>
                <p class="emp-panel-subtitle">
                  <span class="emp-count-badge">{{ employees.length }}</span>
                  名成员
                </p>
              </div>
            </div>
            <button class="dept-btn-primary dept-btn-sm" @click="handleOpenAddEmployeeDialog">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              添加成员
            </button>
          </div>

          <!-- 员工列表 -->
          <div v-loading="employeesLoading" class="emp-list">
            <div v-if="employees.length > 0" class="emp-list-inner">
              <div v-for="emp in employees" :key="emp.id" class="emp-card">
                <div class="emp-card-main">
                  <div class="emp-avatar">
                    {{ (emp.nickName || emp.userName || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div class="emp-info">
                    <span class="emp-name">{{ emp.nickName || emp.userName }}</span>
                    <span class="emp-username">@{{ emp.userName }}</span>
                  </div>
                  <span class="emp-status" :class="emp.flag === 1 ? 'emp-status-active' : 'emp-status-disabled'">
                    {{ emp.flag === 1 ? '活跃' : '禁用' }}
                  </span>
                </div>
                <button class="emp-remove-btn" @click="handleRemoveEmployee(emp)" title="从部门移除">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                </button>
              </div>
            </div>
            <div v-else-if="!employeesLoading" class="emp-empty">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity="0.25"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
              <p>该部门暂无成员</p>
              <button class="dept-btn-ghost dept-btn-sm" @click="handleOpenAddEmployeeDialog">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                添加第一个成员
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 部门编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      :modal="true"
      :before-close="handleDialogClose"
      append-to-body
      class="dept-dialog"
    >
      <el-form ref="editForm" :model="editForm" :rules="formRules" label-width="90px" class="dept-form">
        <el-form-item label="部门名称" prop="departmentName">
          <el-input v-model="editForm.departmentName" placeholder="请输入部门名称" size="medium"/>
        </el-form-item>
        <el-form-item label="部门编码" prop="departmentCode">
          <el-input v-model="editForm.departmentCode" placeholder="请输入部门编码" size="medium"/>
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="editForm.manager" placeholder="请输入负责人" size="medium"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            placeholder="请输入描述"
            :rows="3"
            resize="none"
          />
        </el-form-item>
        <el-form-item label="状态" prop="flag">
          <el-select v-model="editForm.flag" placeholder="请选择状态" size="medium" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.code"
              :label="item.label"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dept-dialog-footer">
        <button class="dept-btn-ghost" @click="handleDialogClose">取消</button>
        <button class="dept-btn-primary" :disabled="dialogSaving" @click="handleDialogConfirm">
          <span v-if="dialogSaving" class="btn-spinner"></span>
          {{ dialogSaving ? '保存中...' : '确认' }}
        </button>
      </div>
    </el-dialog>

    <!-- 移动部门对话框 -->
    <el-dialog
      title="移动部门"
      :visible.sync="moveDepartmentDialogVisible"
      width="480px"
      :modal="true"
      :before-close="handleMoveDepartmentDialogClose"
      append-to-body
      class="dept-dialog"
    >
      <el-form ref="moveDepartmentForm" :model="moveDepartmentForm" label-width="90px" class="dept-form">
        <el-form-item label="目标父部门">
          <div class="move-hint">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
            <span>支持模糊搜索；不能选择自身作为目标</span>
          </div>
          <el-select
            v-model="moveDepartmentForm.targetParentId"
            filterable
            remote
            clearable
            placeholder="请输入部门名称搜索"
            :remote-method="remoteSearchParents"
            :loading="parentRemoteLoading"
            style="width:100%; margin-top: 8px;"
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
        </el-form-item>
      </el-form>
      <div slot="footer" class="dept-dialog-footer">
        <button class="dept-btn-ghost" @click="handleMoveDepartmentDialogClose">取消</button>
        <button class="dept-btn-primary" :disabled="moveSaving" @click="handleMoveDepartmentConfirm">
          <span v-if="moveSaving" class="btn-spinner"></span>
          {{ moveSaving ? '移动中...' : '确认移动' }}
        </button>
      </div>
    </el-dialog>

    <!-- 添加员工对话框 -->
    <el-dialog
      title="添加成员"
      :visible.sync="addEmployeeDialogVisible"
      width="520px"
      :modal="true"
      append-to-body
      class="dept-dialog emp-add-dialog"
      @close="addEmployeeDialogVisible = false"
    >
      <div class="emp-search-bar">
        <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input
          v-model="userSearchQuery"
          type="text"
          class="emp-search-input"
          placeholder="输入用户昵称搜索..."
          @input="onUserSearchInput"
          @keyup.enter="searchUsersList"
        />
      </div>

      <div class="emp-search-results" v-loading="searchUsersLoading">
        <div v-if="!userSearchQuery.trim()" class="emp-search-hint">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.4"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <span>请输入用户昵称进行搜索</span>
        </div>
        <div v-else-if="searchUsers.length > 0" class="emp-search-list">
          <div v-for="user in searchUsers" :key="user.id" class="emp-search-item">
            <div class="emp-search-item-main">
              <div class="emp-avatar emp-avatar-sm">
                {{ (user.nickName || user.userName || '?').charAt(0).toUpperCase() }}
              </div>
              <div class="emp-info">
                <span class="emp-name">{{ user.nickName || user.userName }}</span>
                <span class="emp-username">@{{ user.userName }}</span>
              </div>
            </div>
            <button
              class="emp-add-btn"
              :disabled="addingUserId === user.id"
              @click="handleAddEmployee(user)"
            >
              <span v-if="addingUserId === user.id" class="btn-spinner"></span>
              {{ addingUserId === user.id ? '添加中' : '添加' }}
            </button>
          </div>
        </div>
        <div v-else class="emp-search-empty">
          <p>未找到匹配的用户</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { adminQueryDepartment, deleteDepartment, listChildren, moveDepartmentTo, saveDepartment, getDepartmentEmployees, applyUserToDepartment, removeUserFromDepartment } from '@/api/department'
import { adminQueryUser } from '@/api/admin_user'

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
    },
    compact: {
      type: Boolean,
      default: false
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
      // 员工管理相关
      selectedDepartment: null,
      employees: [],
      employeesLoading: false,
      addEmployeeDialogVisible: false,
      addEmployeeLoading: false,
      userSearchQuery: '',
      userSearchDebounceTimer: null,
      searchUsers: [],
      searchUsersLoading: false,
      addingUserId: null,
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
    clearTimeout(this.userSearchDebounceTimer)
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

    // ==================== 员工管理 ====================

    handleNodeClick(data) {
      this.selectedDepartment = data
      this.loadEmployees(data.id)
    },

    async loadEmployees(departmentId) {
      this.employeesLoading = true
      this.employees = []
      try {
        const response = await getDepartmentEmployees(departmentId)
        this.employees = response.data || []
      } catch (error) {
        console.error('加载员工列表失败:', error)
        this.$message.error('加载员工列表失败')
      } finally {
        this.employeesLoading = false
      }
    },

    handleOpenAddEmployeeDialog() {
      this.userSearchQuery = ''
      this.searchUsers = []
      this.addEmployeeDialogVisible = true
    },

    onUserSearchInput() {
      clearTimeout(this.userSearchDebounceTimer)
      this.userSearchDebounceTimer = setTimeout(() => {
        this.searchUsersList()
      }, 400)
    },

    async searchUsersList() {
      if (!this.userSearchQuery.trim()) {
        this.searchUsers = []
        return
      }
      this.searchUsersLoading = true
      try {
        const params = {
          nickNameLike: this.userSearchQuery.trim()
        }
        const response = await adminQueryUser(params, 1, 50)
        const resData = response.data || {}
        const list = (resData.data && Array.isArray(resData.data)) ? resData.data : (resData.list || [])
        const memberIds = this.employees.map(e => e.id)
        this.searchUsers = list.filter(u => !memberIds.includes(u.id))
      } catch (error) {
        console.error('搜索用户失败:', error)
        this.searchUsers = []
      } finally {
        this.searchUsersLoading = false
      }
    },

    async handleAddEmployee(user) {
      if (!this.selectedDepartment) return
      this.addingUserId = user.id
      try {
        await applyUserToDepartment(user.id, this.selectedDepartment.id)
        this.$message.success(`已将 ${user.nickName || user.userName} 添加到 ${this.selectedDepartment.departmentName}`)
        this.loadEmployees(this.selectedDepartment.id)
        this.searchUsers = this.searchUsers.filter(u => u.id !== user.id)
      } catch (error) {
        console.error('添加员工失败:', error)
        const msg = (error && error.response && error.response.data && error.response.data.message) ? error.response.data.message : '添加员工失败'
        this.$message.error(msg)
      } finally {
        this.addingUserId = null
      }
    },

    handleRemoveEmployee(user) {
      if (!this.selectedDepartment) return
      this.$confirm(`确定要将 ${user.nickName || user.userName} 从 ${this.selectedDepartment.departmentName} 中移除吗？`, '移除确认', {
        confirmButtonText: '确认移除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await removeUserFromDepartment(user.id, this.selectedDepartment.id)
          this.$message.success(`已将 ${user.nickName || user.userName} 从部门中移除`)
          this.loadEmployees(this.selectedDepartment.id)
        } catch (error) {
          console.error('移除员工失败:', error)
          const msg = (error && error.response && error.response.data && error.response.data.message) ? error.response.data.message : '移除员工失败'
          this.$message.error(msg)
        }
      }).catch(() => {})
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
/* ==================== Design Tokens ==================== */
.dept-view {
  --dv-primary: #D97706;
  --dv-primary-light: #F59E0B;
  --dv-primary-bg: #FFFBEB;
  --dv-primary-border: #FDE68A;
  --dv-text: #1E293B;
  --dv-text-secondary: #64748B;
  --dv-text-muted: #94A3B8;
  --dv-surface: #FFFFFF;
  --dv-bg: #F8FAFC;
  --dv-border: #E2E8F0;
  --dv-border-light: #F1F5F9;
  --dv-success: #059669;
  --dv-success-bg: #ECFDF5;
  --dv-warning: #D97706;
  --dv-warning-bg: #FFFBEB;
  --dv-radius: 12px;
  --dv-radius-sm: 8px;
  --dv-transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Noto Sans SC', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: var(--dv-text);
  background: var(--dv-bg);
  padding: 28px 32px;
  min-height: 400px;
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
}

/* ==================== Hero Header ==================== */
.dept-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--dv-border);
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-badge {
  width: 48px;
  height: 48px;
  border-radius: var(--dv-radius);
  background: var(--dv-primary-bg);
  border: 1px solid var(--dv-primary-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dv-primary);
  flex-shrink: 0;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--dv-text);
  margin: 0 0 2px 0;
  letter-spacing: -0.3px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 14px;
  color: var(--dv-text-muted);
  margin: 0;
  font-weight: 400;
}

/* ==================== Primary Button ==================== */
.dept-btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  background: var(--dv-primary);
  color: #fff;
  border: none;
  border-radius: var(--dv-radius-sm);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--dv-transition);
  white-space: nowrap;
  line-height: 1;

  &:hover:not(:disabled) {
    background: var(--dv-primary-light);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(217, 119, 6, 0.35);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.dept-btn-sm {
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 6px;
}

/* ==================== Ghost Button ==================== */
.dept-btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  background: transparent;
  color: var(--dv-text-secondary);
  border: 1px solid var(--dv-border);
  border-radius: var(--dv-radius-sm);
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--dv-transition);
  line-height: 1;

  &:hover {
    color: var(--dv-text);
    border-color: var(--dv-text-muted);
    background: var(--dv-border-light);
  }
}

/* ==================== Split Layout ==================== */
.dept-split {
  display: flex;
  gap: 24px;
  min-height: 0;
}

.dept-left {
  flex: 0 0 480px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.dept-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--dv-surface);
  border-radius: var(--dv-radius);
  border: 1px solid var(--dv-border);
  overflow: hidden;
}

/* ==================== Search Section ==================== */
.dept-search {
  background: var(--dv-surface);
  border-radius: var(--dv-radius);
  border: 1px solid var(--dv-border);
  padding: 16px 20px;
  margin-bottom: 20px;
  transition: border-color var(--dv-transition);

  &:focus-within {
    border-color: var(--dv-primary-border);
  }
}

.dept-search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-field {
  position: relative;
  flex: 1;
  min-width: 180px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--dv-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 40px;
  padding: 0 14px;
  border: 1px solid var(--dv-border);
  border-radius: var(--dv-radius-sm);
  font-size: 13px;
  font-family: inherit;
  color: var(--dv-text);
  background: var(--dv-bg);
  transition: all var(--dv-transition);
  outline: none;

  &:focus {
    border-color: var(--dv-primary);
    background: var(--dv-surface);
    box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.1);
  }

  &::placeholder {
    color: var(--dv-text-muted);
  }
}

.search-field:first-child .search-input {
  padding-left: 38px;
}

.search-select-field {
  min-width: 160px;
}

.search-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

/* ==================== Tree Section ==================== */
.dept-tree-wrap {
  background: var(--dv-surface);
  border-radius: var(--dv-radius);
  border: 1px solid var(--dv-border);
  overflow: hidden;
}

.dept-tree-inner {
  min-height: 200px;
  padding: 8px 4px;
  position: relative;
}

.tree-row {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  border-radius: var(--dv-radius-sm);
  transition: background var(--dv-transition);
  min-height: 42px;
}

.tree-row:hover {
  background: var(--dv-primary-bg);
}

.tree-row-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.tree-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dv-text);
  cursor: default;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 700;
  color: var(--dv-primary);
  background: var(--dv-primary-bg);
  border: 1px solid var(--dv-primary-border);
  border-radius: 11px;
  flex-shrink: 0;
  line-height: 1;
}

/* ==================== Tree Actions ==================== */
.tree-row-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transform: translateX(8px);
  transition: all var(--dv-transition);
  flex-shrink: 0;
  margin-left: 12px;
}

.tree-row:hover .tree-row-actions {
  opacity: 1;
  transform: translateX(0);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border: 1px solid var(--dv-border);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--dv-transition);
  background: var(--dv-surface);
  color: var(--dv-text-secondary);
  line-height: 1;
  white-space: nowrap;

  &:hover {
    border-color: var(--dv-primary);
    color: var(--dv-primary);
    background: var(--dv-primary-bg);
  }
}

.action-edit {
  svg {
    flex-shrink: 0;
  }
}

.action-more {
  padding: 5px 8px;
}

/* ==================== Empty State ==================== */
.dept-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  color: var(--dv-text-muted);

  p {
    margin: 12px 0 0 0;
    font-size: 14px;
    color: var(--dv-text-muted);
  }
}

/* ==================== Pagination ==================== */
.dept-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 4px 0;
}

/* ==================== Loading Mask ==================== */
::v-deep .el-loading-mask {
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(2px);
  border-radius: var(--dv-radius);
}

/* ==================== Tree Element Overrides ==================== */
::v-deep .el-tree {
  font-size: 14px;
  line-height: 1;
  color: var(--dv-text);
}

::v-deep .el-tree-node {
  margin: 2px 0;
}

::v-deep .el-tree-node__content {
  height: auto;
  min-height: 42px;
  line-height: normal;
  border-radius: var(--dv-radius-sm);
  margin: 0;
  padding: 0;
  transition: background var(--dv-transition);

  &:hover {
    background: transparent;
  }
}

::v-deep .el-tree-node:focus > .el-tree-node__content {
  background: transparent;
}

::v-deep .el-tree-node__expand-icon {
  font-size: 14px;
  color: var(--dv-text-muted);
  margin-right: 4px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dv-transition);
  border-radius: 4px;

  &:not(.is-leaf):hover {
    color: var(--dv-primary);
    background: var(--dv-primary-bg);
  }
}

::v-deep .el-tree-node__expand-icon.expanded {
  transform: rotate(90deg);
}

::v-deep .el-tree-node__expand-icon.is-leaf {
  color: transparent;
  cursor: default;
}

/* ==================== Search Input Overrides ==================== */
::v-deep .search-select-field .el-input__inner {
  height: 40px;
  line-height: 40px;
  border-radius: var(--dv-radius-sm);
  border-color: var(--dv-border);
  background: var(--dv-bg);
  font-size: 13px;
}

::v-deep .search-select-field .el-input__suffix {
  right: 8px;
}

::v-deep .search-select-field .el-tag {
  background: var(--dv-primary-bg);
  border-color: var(--dv-primary-border);
  color: var(--dv-primary);
}

/* ==================== Tooltip ==================== */
::v-deep .dept-tooltip {
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
  box-shadow: none !important;
  &.popper__arrow {
    display: none !important;
  }
}

.detail-card {
  min-width: 280px;
  max-width: 360px;
  background: var(--dv-surface);
  border-radius: var(--dv-radius);
  border: 1px solid var(--dv-border);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.detail-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--dv-text);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.detail-head-icon {
  display: flex;
  align-items: center;
  opacity: 0.7;
}

.detail-body {
  padding: 12px 16px 14px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid var(--dv-border-light);

  &:last-child {
    border-bottom: none;
  }
}

.detail-row-full {
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  color: var(--dv-text-muted);
  width: 64px;
  flex-shrink: 0;
  font-weight: 500;
}

.detail-val {
  color: var(--dv-text);
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;

  &.dot-1 {
    background: var(--dv-success);
    box-shadow: 0 0 0 3px var(--dv-success-bg);
  }

  &.dot-2 {
    background: var(--dv-warning);
    box-shadow: 0 0 0 3px var(--dv-warning-bg);
  }
}

/* ==================== Dialog ==================== */
::v-deep .dept-dialog {
  .el-dialog {
    border-radius: var(--dv-radius);
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  }

  .el-dialog__header {
    padding: 20px 24px 16px;
    margin: 0;
    border-bottom: 1px solid var(--dv-border-light);
    background: var(--dv-surface);
  }

  .el-dialog__title {
    font-size: 16px;
    font-weight: 700;
    color: var(--dv-text);
    letter-spacing: -0.2px;
  }

  .el-dialog__headerbtn {
    top: 20px;
    right: 20px;
  }

  .el-dialog__body {
    padding: 0;
  }

  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid var(--dv-border-light);
    background: var(--dv-bg);
  }
}

.dept-form {
  padding: 24px;

  ::v-deep .el-form-item {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  ::v-deep .el-form-item__label {
    font-size: 13px;
    font-weight: 600;
    color: var(--dv-text-secondary);
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-textarea__inner {
    border-radius: var(--dv-radius-sm);
    border-color: var(--dv-border);
    font-size: 13px;
    transition: all var(--dv-transition);

    &:focus {
      border-color: var(--dv-primary);
      box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.08);
    }
  }
}

.dept-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* ==================== Loading Spinner ==================== */
.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: dv-spin 0.6s linear infinite;
}

@keyframes dv-spin {
  to { transform: rotate(360deg); }
}

/* ==================== Search Highlight ==================== */
::v-deep mark {
  background: var(--dv-primary-bg);
  color: var(--dv-primary);
  padding: 1px 4px;
  border-radius: 3px;
  font-weight: 600;
  border: 1px solid var(--dv-primary-border);
}

/* ==================== Move Dialog Hint ==================== */
.move-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #EFF6FF;
  border-radius: var(--dv-radius-sm);
  border: 1px solid #BFDBFE;
  font-size: 12px;
  color: #1D4ED8;
  margin-bottom: 8px;
}

/* ==================== Employee Panel ==================== */
.emp-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.emp-placeholder-inner {
  text-align: center;
}

.emp-placeholder-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dv-text-secondary);
  margin: 16px 0 6px;
}

.emp-placeholder-desc {
  font-size: 13px;
  color: var(--dv-text-muted);
  margin: 0;
  max-width: 240px;
  line-height: 1.6;
}

.emp-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--dv-border-light);
  background: var(--dv-surface);
  flex-shrink: 0;
}

.emp-panel-title-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

.emp-dept-badge {
  width: 42px;
  height: 42px;
  border-radius: var(--dv-radius-sm);
  background: var(--dv-primary-bg);
  border: 1px solid var(--dv-primary-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dv-primary);
  flex-shrink: 0;
}

.emp-panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--dv-text);
  margin: 0 0 2px 0;
  line-height: 1.2;
}

.emp-panel-subtitle {
  font-size: 13px;
  color: var(--dv-text-muted);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.emp-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 700;
  color: var(--dv-primary);
  background: var(--dv-primary-bg);
  border: 1px solid var(--dv-primary-border);
  border-radius: 10px;
  line-height: 1;
}

.emp-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  position: relative;
  min-height: 120px;
}

.emp-list-inner {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.emp-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: var(--dv-radius-sm);
  transition: background var(--dv-transition);
  border: 1px solid transparent;
}

.emp-card:hover {
  background: var(--dv-primary-bg);
  border-color: var(--dv-primary-border);
}

.emp-card-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.emp-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--dv-primary-bg), var(--dv-primary-border));
  color: var(--dv-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  border: 2px solid var(--dv-surface);
  box-shadow: 0 0 0 1px var(--dv-border);
}

.emp-avatar-sm {
  width: 32px;
  height: 32px;
  font-size: 12px;
}

.emp-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.emp-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--dv-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.emp-username {
  font-size: 12px;
  color: var(--dv-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.emp-status {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
  flex-shrink: 0;
  white-space: nowrap;
}

.emp-status-active {
  color: var(--dv-success);
  background: var(--dv-success-bg);
}

.emp-status-disabled {
  color: var(--dv-warning);
  background: var(--dv-warning-bg);
}

.emp-remove-btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--dv-text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dv-transition);
  flex-shrink: 0;
  opacity: 0;
}

.emp-card:hover .emp-remove-btn {
  opacity: 1;
}

.emp-remove-btn:hover {
  background: #FEE2E2;
  color: #DC2626;
}

.emp-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: var(--dv-text-muted);
}

.emp-empty p {
  margin: 10px 0 14px;
  font-size: 14px;
  color: var(--dv-text-muted);
}

/* ==================== Add Employee Dialog ==================== */
.emp-add-dialog {
  ::v-deep .el-dialog__body {
    padding: 0;
  }
}

.emp-search-bar {
  position: relative;
  padding: 16px 20px 0;
}

.emp-search-bar .search-icon {
  position: absolute;
  left: 32px;
  top: 28px;
  color: var(--dv-text-muted);
  pointer-events: none;
}

.emp-search-input {
  width: 100%;
  height: 42px;
  padding: 0 14px 0 40px;
  border: 1px solid var(--dv-border);
  border-radius: var(--dv-radius-sm);
  font-size: 13px;
  font-family: inherit;
  color: var(--dv-text);
  background: var(--dv-bg);
  outline: none;
  transition: all var(--dv-transition);
  box-sizing: border-box;
}

.emp-search-input:focus {
  border-color: var(--dv-primary);
  background: var(--dv-surface);
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.1);
}

.emp-search-input::placeholder {
  color: var(--dv-text-muted);
}

.emp-search-results {
  min-height: 120px;
  max-height: 360px;
  overflow-y: auto;
  padding: 12px 20px 16px;
  position: relative;
}

.emp-search-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  gap: 8px;
  color: var(--dv-text-muted);
  font-size: 13px;
}

.emp-search-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.emp-search-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: var(--dv-radius-sm);
  transition: background var(--dv-transition);
}

.emp-search-item:hover {
  background: var(--dv-border-light);
}

.emp-search-item-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.emp-add-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  border: 1px solid var(--dv-primary-border);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--dv-transition);
  background: var(--dv-primary-bg);
  color: var(--dv-primary);
  white-space: nowrap;
  flex-shrink: 0;
}

.emp-add-btn:hover:not(:disabled) {
  background: var(--dv-primary);
  color: #fff;
  border-color: var(--dv-primary);
}

.emp-add-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.emp-search-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
}

.emp-search-empty p {
  margin: 0;
  font-size: 13px;
  color: var(--dv-text-muted);
}

/* ==================== Responsive ==================== */
@media (max-width: 768px) {
  .dept-view {
    padding: 16px;
  }

  .dept-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .hero-title {
    font-size: 20px;
  }

  .dept-split {
    flex-direction: column;
  }

  .dept-left {
    flex: none;
  }

  .dept-right {
    min-height: 300px;
  }

  .dept-search-inner {
    flex-direction: column;
  }

  .search-field {
    min-width: 100%;
  }

  .search-actions {
    width: 100%;
    margin-left: 0;
  }

  .emp-panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

<style lang="scss">
.parent-select-popper .el-select-dropdown__item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
