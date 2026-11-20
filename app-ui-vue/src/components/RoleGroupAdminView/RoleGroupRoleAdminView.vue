<template>
  <div class="perm-workspace">
    <div class="workspace-toolbar">
      <div class="toolbar-heading">
        <span class="heading-rail" />
        <h3 class="heading-title">{{ $t('roleGroup.tabPermissions') }}</h3>
        <span class="heading-meta">{{ $t('roleGroup.totalPerm', { count: assignedRoles.length }) }}</span>
      </div>
      <div class="toolbar-actions">
        <el-button
          size="small"
          type="primary"
          icon="el-icon-plus"
          class="create-button"
          @click="openAddRoleDialog"
        >
          {{ $t('roleGroup.addPerm') }}
        </el-button>
        <el-tooltip :content="$t('common.refresh')" placement="bottom">
          <el-button
            size="small"
            icon="el-icon-refresh"
            circle
            class="refresh-button"
            :loading="loadingAssigned || loadingAvailable"
            @click="fetchAllData"
          />
        </el-tooltip>
      </div>
    </div>

    <div class="transfer-grid">
      <section class="transfer-pane is-assigned">
        <header class="pane-top">
          <div class="pane-identity">
            <span class="identity-dot dot-assigned" />
            <span class="identity-name">{{ $t('roleGroup.assignedPerms') }}</span>
            <span class="identity-count">{{ assignedRoles.length }}</span>
          </div>
          <div class="pane-search">
            <i class="el-icon-search" />
            <input
              v-model="assignedKeyword"
              :placeholder="$t('roleGroup.searchAssigned')"
              class="pane-search-input"
            >
            <i
              v-if="assignedKeyword"
              class="el-icon-close search-clear"
              @click="assignedKeyword = ''"
            />
          </div>
        </header>

        <div v-loading="loadingAssigned" class="pane-scroll">
          <div v-if="Object.keys(groupedAssignedRoles).length === 0" class="pane-empty">
            <div class="empty-glyph">
              <i :class="assignedKeyword ? 'el-icon-search' : 'el-icon-collection'" />
            </div>
            <p>{{ assignedKeyword ? $t('roleGroup.emptySearch') : $t('roleGroup.emptyAssignedPerm') }}</p>
          </div>

          <div v-else class="perm-groups">
            <div v-for="(items, type) in groupedAssignedRoles" :key="type" class="perm-group">
              <div class="group-heading">
                <span class="type-flag" :class="getTypeClass(Number(type))">
                  {{ getTypeLabel(Number(type)) }}
                </span>
                <span class="group-total">{{ items.length }}</span>
              </div>
              <div class="chip-field">
                <button
                  v-for="perm in items"
                  :key="perm.id"
                  type="button"
                  class="perm-chip is-assigned"
                  :class="getTypeClass(perm.type)"
                  :title="$t('roleGroup.removePermConfirm', { name: perm.name })"
                  @click="handleRemovePermission(perm)"
                >
                  <i class="chip-minus el-icon-close" />
                  <span class="chip-label">{{ perm.name }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div class="transfer-bridge" aria-hidden="true">
        <span class="bridge-line" />
        <span class="bridge-symbol">⇄</span>
        <span class="bridge-line" />
      </div>

      <section class="transfer-pane is-available">
        <header class="pane-top">
          <div class="pane-identity">
            <span class="identity-dot dot-available" />
            <span class="identity-name">{{ $t('roleGroup.availablePerms') }}</span>
            <span class="identity-count">{{ filteredAvailableRoles.length }}</span>
          </div>
          <div class="pane-search">
            <i class="el-icon-search" />
            <input
              v-model="searchKeyword"
              :placeholder="$t('roleGroup.searchPlaceholder')"
              class="pane-search-input"
            >
            <i
              v-if="searchKeyword"
              class="el-icon-close search-clear"
              @click="searchKeyword = ''"
            />
          </div>
        </header>

        <div class="type-filters">
          <button
            type="button"
            class="filter-pill"
            :class="{ 'is-active': filterType === null }"
            @click="filterType = null"
          >
            {{ $t('roleGroup.typeAll') }}
          </button>
          <button
            v-for="typeOption in permissionTypes"
            :key="typeOption.value"
            type="button"
            class="filter-pill"
            :class="[typeOption.typeClass, { 'is-active': filterType === typeOption.value }]"
            @click="filterType = typeOption.value"
          >
            {{ typeOption.label }}
          </button>
        </div>

        <div v-loading="loadingAvailable" class="pane-scroll">
          <div v-if="filteredAvailableRoles.length === 0" class="pane-empty">
            <div class="empty-glyph">
              <i :class="searchKeyword ? 'el-icon-search' : 'el-icon-collection'" />
            </div>
            <p>{{ searchKeyword || filterType !== null ? $t('roleGroup.emptySearch') : $t('roleGroup.emptyAvailablePerm') }}</p>
          </div>

          <div v-else class="perm-groups">
            <div v-for="(items, type) in groupedAvailableRoles" :key="type" class="perm-group">
              <div class="group-heading">
                <span class="type-flag" :class="getTypeClass(Number(type))">
                  {{ getTypeLabel(Number(type)) }}
                </span>
                <span class="group-total">{{ items.length }}</span>
              </div>
              <div class="chip-field">
                <button
                  v-for="perm in items"
                  :key="perm.id"
                  type="button"
                  class="perm-chip is-available"
                  :class="[getTypeClass(perm.type), { 'is-selected': selectedPermIds.includes(perm.id) }]"
                  @click="togglePermSelection(perm.id)"
                >
                  <i class="chip-check el-icon-check" />
                  <span class="chip-label">{{ perm.name }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <footer class="workspace-footer">
      <div class="footer-selection">
        <span class="selection-badge">{{ selectedPermIds.length }}</span>
        <span class="selection-label">{{ $t('roleGroup.pendingLabel') }}</span>
        <el-button
          type="text"
          size="mini"
          class="clear-link"
          :disabled="selectedPermIds.length === 0"
          @click="selectedPermIds = []"
        >
          {{ $t('roleGroup.clearSelection') }}
        </el-button>
      </div>
      <div class="footer-actions">
        <el-button
          size="small"
          type="text"
          class="select-all-link"
          :disabled="filteredAvailableRoles.length === 0"
          @click="handleSelectAll"
        >
          {{ $t('roleGroup.selectAll') }}
        </el-button>
        <el-button
          size="small"
          type="primary"
          class="commit-button"
          :disabled="selectedPermIds.length === 0"
          :loading="batchAdding"
          @click="handleBatchAdd"
        >
          {{ $t('roleGroup.confirmAdd') }}
        </el-button>
      </div>
    </footer>

    <el-dialog
      :title="$t('roleGroup.addPermTitle')"
      :visible.sync="roleDialogVisible"
      width="480px"
      :close-on-click-modal="false"
      append-to-body
      class="quick-create-dialog"
    >
      <el-alert
        :title="$t('roleGroup.menuPermTip')"
        type="info"
        :closable="false"
        show-icon
        class="create-alert"
      />
      <el-form ref="roleForm" :model="roleForm" :rules="roleRules" label-width="96px">
        <el-form-item :label="$t('role.permissionCode')" prop="role">
          <el-input v-model="roleForm.role" maxlength="128" :placeholder="$t('roleGroup.codePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('role.name')" prop="name">
          <el-input v-model="roleForm.name" maxlength="128" :placeholder="$t('roleGroup.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('role.description')">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            :placeholder="$t('roleGroup.descPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="roleSaving" @click="handleAddRoleConfirm">
          {{ $t('common.confirm') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {appendRoleToGroup, getRoleListByGroupId, removeRoleFromGroup} from '@/api/role_group'
import {activateRole, queryRoles, saveRole} from '@/api/role'
import {runBatchWithLimit} from '@/utils/batch-request'

export default {
  name: 'RoleGroupRoleAdminView',
  props: {
    groupId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      loadingAssigned: false,
      loadingAvailable: false,
      batchAdding: false,
      assignedRoles: [],
      availableRoles: [],
      selectedPermIds: [],
      searchKeyword: '',
      assignedKeyword: '',
      filterType: null,
      roleDialogVisible: false,
      roleSaving: false,
      roleForm: this.createEmptyRoleForm(),
      roleRules: {
        role: [
          { required: true, message: this.$t('roleGroup.codeRequired'), trigger: 'blur' },
          { pattern: /^[A-Z][A-Z0-9_.-]{1,127}$/, message: this.$t('roleGroup.codePattern'), trigger: 'blur' }
        ],
        name: [{ required: true, message: this.$t('roleGroup.nameRequired'), trigger: 'blur' }]
      }
    }
  },
  computed: {
    permissionTypes() {
      return [
        { value: 1, label: this.$t('roleGroup.typeMenu'), typeClass: 'type-menu' },
        { value: 2, label: this.$t('roleGroup.typeData'), typeClass: 'type-data' },
        { value: 3, label: this.$t('roleGroup.typeApi'), typeClass: 'type-api' }
      ]
    },
    groupedAssignedRoles() {
      const source = this.assignedKeyword
        ? this.assignedRoles.filter(perm => this.matchKeyword(perm, this.assignedKeyword))
        : this.assignedRoles
      return this.groupByType(source)
    },
    groupedAvailableRoles() {
      return this.groupByType(this.filteredAvailableRoles)
    },
    filteredAvailableRoles() {
      let list = this.availableRoles.filter(
        perm => !this.assignedRoles.some(assigned => assigned.id === perm.id)
      )
      if (this.filterType !== null) {
        list = list.filter(perm => perm.type === this.filterType)
      }
      if (this.searchKeyword) {
        list = list.filter(perm => this.matchKeyword(perm, this.searchKeyword))
      }
      return list
    }
  },
  mounted() {
    this.fetchAllData()
  },
  methods: {
    /**
     * 按权限类型分组，保持后端返回顺序
     * @param {Array} list - 权限列表
     * @returns {Object} type -> 权限数组
     */
    groupByType(list) {
      const groups = {}
      list.forEach(item => {
        const type = item.type || 0
        if (!groups[type]) groups[type] = []
        groups[type].push(item)
      })
      return groups
    },

    /**
     * 按名称、编码、描述匹配关键词
     * @param {Object} perm - 权限项
     * @param {string} keyword - 关键词
     * @returns {boolean} 是否匹配
     */
    matchKeyword(perm, keyword) {
      const kw = keyword.toLowerCase()
      return [perm.name, perm.role, perm.description]
        .filter(Boolean)
        .some(field => String(field).toLowerCase().includes(kw))
    },

    /**
     * 加载已分配权限
     * @returns {Promise} 加载结果
     */
    async fetchAssignedRoles() {
      this.loadingAssigned = true
      try {
        const response = await getRoleListByGroupId(this.groupId)
        this.assignedRoles = response.data || []
      } catch (error) {
        console.error('获取已分配权限失败:', error)
      } finally {
        this.loadingAssigned = false
      }
    },

    /**
     * 加载全部可分配权限（一次取全量，本地过滤已分配项）
     * @returns {Promise} 加载结果
     */
    async fetchAvailableRoles() {
      this.loadingAvailable = true
      try {
        const response = await queryRoles({}, 1, 1000)
        this.availableRoles = response.data?.data || []
      } catch (error) {
        console.error('获取可分配权限失败:', error)
      } finally {
        this.loadingAvailable = false
      }
    },

    /**
     * 两侧数据一起刷新，避免分配后只更新一边造成状态不一致
     * @returns {Promise} 加载结果
     */
    async fetchAllData() {
      await Promise.all([this.fetchAssignedRoles(), this.fetchAvailableRoles()])
    },

    /**
     * 创建空的新建权限表单
     * @returns {Object} 表单对象
     */
    createEmptyRoleForm() {
      return {
        role: '',
        name: '',
        description: ''
      }
    },

    /**
     * 打开新建权限弹窗并重置校验状态
     */
    openAddRoleDialog() {
      this.roleForm = this.createEmptyRoleForm()
      this.roleDialogVisible = true
      this.$nextTick(() => {
        this.$refs.roleForm?.clearValidate()
      })
    },

    /**
     * 新建菜单权限并直接分配到当前权限组（一步完成，不再需要二次勾选）
     * @returns {Promise} 操作结果
     */
    async handleAddRoleConfirm() {
      try {
        await this.$refs.roleForm.validate()
      } catch (e) {
        return
      }
      const code = this.roleForm.role.trim()
      const name = this.roleForm.name.trim()
      this.roleSaving = true
      try {
        // roleEquals 实为左模糊，判重必须用 roleIn 精确匹配
        const response = await queryRoles({ roleIn: [code] }, 1, 10)
        const matched = response.data?.data || []
        // 同编码可能同时存在启用与禁用两条记录，优先取启用态
        const existed = matched.find(item => item.flag === 1) || matched[0]
        if (existed) {
          await this.handleExistedRole(existed)
        } else {
          // 类型固定为菜单权限；flag/dataScope 由服务层按默认值处理
          await saveRole({ role: code, name, type: 1, description: this.roleForm.description })
          await this.assignPermByCode(code, this.$t('roleGroup.permCreatedAndAssigned', { name }))
        }
      } catch (error) {
        console.error('新建并分配权限失败:', error)
      } finally {
        this.roleSaving = false
      }
    },

    /**
     * 编码已存在时按类型与状态分支处理，最终统一走分配逻辑
     * @param {Object} existed - 已存在的权限记录
     * @returns {Promise} 操作结果
     */
    async handleExistedRole(existed) {
      const code = existed.role
      if (existed.flag === 2) {
        this.$message.error(this.$t('roleGroup.permCodeSystem', { code }))
        return
      }
      if (existed.type !== 1) {
        this.$message.error(this.$t('roleGroup.permCodeOccupied', { code }))
        return
      }
      if (existed.flag === 0) {
        try {
          await this.$confirm(
            this.$t('roleGroup.enableAssignConfirm', { code }),
            this.$t('common.tip'),
            {
              confirmButtonText: this.$t('common.confirm'),
              cancelButtonText: this.$t('common.cancel'),
              type: 'warning'
            }
          )
          await activateRole(existed.id)
          await this.assignPermByCode(code, this.$t('roleGroup.permEnabledAndAssigned', { name: existed.name }))
        } catch (error) {
          if (error !== 'cancel') {
            console.error('启用权限失败:', error)
          }
        }
        return
      }
      await this.assignPermByCode(code, this.$t('roleGroup.permExistsAndAssigned', { code }))
    },

    /**
     * 按编码找到权限并直接分配到当前组，已在组内则只提示不重复请求
     * @param {string} code - 权限编码
     * @param {string} successMessage - 成功提示
     * @returns {Promise} 操作结果
     */
    async assignPermByCode(code, successMessage) {
      await this.fetchAvailableRoles()
      const target = this.availableRoles.find(perm => perm.role === code && perm.flag === 1) ||
        this.availableRoles.find(perm => perm.role === code)
      if (!target) {
        this.$message.warning(this.$t('roleGroup.permNotListed', { name: code }))
        this.roleDialogVisible = false
        return
      }
      if (this.assignedRoles.some(perm => perm.id === target.id)) {
        this.$message.info(this.$t('roleGroup.permAlreadyAssigned', { name: target.name }))
        this.roleDialogVisible = false
        return
      }
      await appendRoleToGroup(this.groupId, target.id)
      this.$message.success(successMessage)
      this.roleDialogVisible = false
      await this.fetchAllData()
    },

    /**
     * 切换单个待添加权限
     * @param {number} permId - 权限ID
     */
    togglePermSelection(permId) {
      const index = this.selectedPermIds.indexOf(permId)
      if (index > -1) {
        this.selectedPermIds.splice(index, 1)
      } else {
        this.selectedPermIds.push(permId)
      }
    },

    /**
     * 全选或取消当前筛选结果
     */
    handleSelectAll() {
      const allIds = this.filteredAvailableRoles.map(perm => perm.id)
      const allSelected = allIds.every(id => this.selectedPermIds.includes(id))
      this.selectedPermIds = allSelected ? [] : [...allIds]
    },

    /**
     * 并发批量添加，按成功/失败数量分别反馈，不再串行逐条请求
     * @returns {Promise} 操作结果
     */
    async handleBatchAdd() {
      if (this.selectedPermIds.length === 0) return
      const ids = [...this.selectedPermIds]
      this.batchAdding = true
      try {
        const results = await runBatchWithLimit(ids, id => appendRoleToGroup(this.groupId, id))
        const success = results.filter(result => result.ok).length
        const fail = results.length - success
        if (fail === 0) {
          this.$message.success(this.$t('roleGroup.addSuccess', { count: success }))
        } else {
          this.$message.warning(this.$t('roleGroup.addPartialFail', { success, fail }))
        }
        this.selectedPermIds = []
        await this.fetchAllData()
      } catch (error) {
        console.error('批量添加权限失败:', error)
      } finally {
        this.batchAdding = false
      }
    },

    /**
     * 移除已分配权限
     * @param {Object} perm - 权限项
     * @returns {Promise} 操作结果
     */
    async handleRemovePermission(perm) {
      try {
        await this.$confirm(
          this.$t('roleGroup.removePermConfirm', { name: perm.name }),
          this.$t('roleGroup.removeConfirmTitle'),
          {
            confirmButtonText: this.$t('roleGroup.removeConfirmBtn'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
        await removeRoleFromGroup(this.groupId, perm.id)
        this.$message.success(this.$t('roleGroup.removeSuccess'))
        await this.fetchAllData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除权限失败:', error)
        }
      }
    },

    /**
     * 权限类型展示文案
     * @param {number} type - 权限类型
     * @returns {string} 文案
     */
    getTypeLabel(type) {
      const keys = {
        1: 'roleGroup.typeMenu',
        2: 'roleGroup.typeData',
        3: 'roleGroup.typeApi'
      }
      return keys[type] ? this.$t(keys[type]) : this.$t('roleGroup.unknown')
    },

    /**
     * 权限类型对应的样式类
     * @param {number} type - 权限类型
     * @returns {string} 样式类名
     */
    getTypeClass(type) {
      const classes = {
        1: 'type-menu',
        2: 'type-data',
        3: 'type-api'
      }
      return classes[type] || 'type-unknown'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.perm-workspace {
  --ink: #16233c;
  --muted: #7b8aa3;
  --line: #e4eaf2;
  --accent: #0b63ce;
  --menu: #0b63ce;
  --data: #12996b;
  --api: #d9820b;

  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 500px;
  color: var(--ink);

  .workspace-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 18px;
    border: 1px solid var(--line);
    border-radius: 12px;
    background: linear-gradient(115deg, #f4f8ff 0%, #ffffff 62%);

    .toolbar-heading {
      display: flex;
      align-items: center;
      gap: 10px;
      min-width: 0;
    }

    .heading-rail {
      width: 3px;
      height: 22px;
      border-radius: 3px;
      background: linear-gradient(180deg, #0b63ce, #6aa7f5);
    }

    .heading-title {
      margin: 0;
      font-size: 16px;
      font-weight: 700;
      letter-spacing: 0.2px;
    }

    .heading-meta {
      font-size: 12px;
      font-weight: 600;
      color: var(--muted);
      font-variant-numeric: tabular-nums;
    }

    .toolbar-actions {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-shrink: 0;
    }

    .create-button,
    .commit-button {
      border-radius: 8px;
      font-weight: 600;
    }

    .refresh-button {
      border-radius: 8px;
    }
  }

  .transfer-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 36px minmax(0, 1fr);
    align-items: stretch;
    gap: 0;
    min-height: 380px;
  }

  .transfer-pane {
    display: flex;
    flex-direction: column;
    min-width: 0;
    border: 1px solid var(--line);
    background: #fff;
    overflow: hidden;

    &.is-assigned {
      border-radius: 12px 0 0 12px;
      border-right: none;
    }

    &.is-available {
      border-radius: 0 12px 12px 0;
      border-left: none;
    }
  }

  .transfer-bridge {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-top: 1px solid var(--line);
    border-bottom: 1px solid var(--line);
    background: #f8fafc;

    .bridge-line {
      width: 1px;
      height: 34%;
      background: #dbe3ee;
    }

    .bridge-symbol {
      width: 26px;
      height: 26px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: #fff;
      border: 1px solid #dbe3ee;
      color: var(--accent);
      font-size: 13px;
      box-shadow: 0 2px 6px rgba(15, 23, 42, 0.06);
    }
  }

  .pane-top {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 13px 16px;
    border-bottom: 1px solid var(--line);
    background: #fbfcfe;
  }

  .pane-identity {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .identity-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;

    &.dot-assigned {
      background: #12996b;
      box-shadow: 0 0 0 3px rgba(18, 153, 107, 0.12);
    }

    &.dot-available {
      background: var(--accent);
      box-shadow: 0 0 0 3px rgba(11, 99, 206, 0.12);
    }
  }

  .identity-name {
    font-size: 13px;
    font-weight: 700;
    letter-spacing: 0.2px;
  }

  .identity-count {
    font-size: 11px;
    font-weight: 700;
    color: var(--muted);
    background: #eef2f7;
    border-radius: 10px;
    padding: 1px 8px;
    font-variant-numeric: tabular-nums;
  }

  .pane-search {
    display: flex;
    align-items: center;
    gap: 6px;
    height: 34px;
    padding: 0 10px;
    border: 1px solid #dbe3ee;
    border-radius: 8px;
    background: #fff;
    transition: border-color 0.2s, box-shadow 0.2s;

    &:focus-within {
      border-color: var(--accent);
      box-shadow: 0 0 0 3px rgba(11, 99, 206, 0.1);
    }

    > i {
      color: #9aa8bd;
      font-size: 13px;
    }

    .search-clear {
      cursor: pointer;

      &:hover {
        color: var(--ink);
      }
    }
  }

  .pane-search-input {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    font-size: 13px;
    color: var(--ink);
    background: transparent;

    &::placeholder {
      color: #a8b4c6;
    }
  }

  .type-filters {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    padding: 10px 16px;
    border-bottom: 1px solid var(--line);
    background: #fff;
  }

  .filter-pill {
    appearance: none;
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    padding: 4px 12px;
    font-size: 12px;
    font-weight: 600;
    color: #64748b;
    background: #f8fafc;
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover {
      border-color: #cbd5e1;
      color: var(--ink);
    }

    &.is-active {
      color: #fff;
      border-color: var(--accent);
      background: var(--accent);
      box-shadow: 0 2px 8px rgba(11, 99, 206, 0.22);
    }

    &.type-data.is-active {
      border-color: var(--data);
      background: var(--data);
      box-shadow: 0 2px 8px rgba(18, 153, 107, 0.22);
    }

    &.type-api.is-active {
      border-color: var(--api);
      background: var(--api);
      box-shadow: 0 2px 8px rgba(217, 130, 11, 0.22);
    }
  }

  .pane-scroll {
    flex: 1;
    min-height: 300px;
    max-height: 460px;
    overflow-y: auto;
    padding: 16px;
    overscroll-behavior: contain;
  }

  .pane-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 220px;
    color: #a8b4c6;

    .empty-glyph {
      width: 52px;
      height: 52px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16px;
      background: #f4f7fb;
      margin-bottom: 12px;

      i {
        font-size: 24px;
      }
    }

    p {
      margin: 0;
      font-size: 13px;
    }
  }

  .perm-groups {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .group-heading {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  .type-flag {
    display: inline-flex;
    align-items: center;
    padding: 2px 9px;
    border-radius: 5px;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.4px;

    &.type-menu {
      color: var(--menu);
      background: rgba(11, 99, 206, 0.09);
    }

    &.type-data {
      color: var(--data);
      background: rgba(18, 153, 107, 0.1);
    }

    &.type-api {
      color: var(--api);
      background: rgba(217, 130, 11, 0.1);
    }

    &.type-unknown {
      color: var(--muted);
      background: #eef2f7;
    }
  }

  .group-total {
    font-size: 11px;
    font-weight: 600;
    color: #a8b4c6;
    font-variant-numeric: tabular-nums;
  }

  .chip-field {
    display: flex;
    flex-wrap: wrap;
    gap: 7px;
  }

  .perm-chip {
    appearance: none;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    max-width: 100%;
    padding: 6px 10px;
    border-radius: 7px;
    border: 1px solid #e2e8f0;
    background: #fff;
    color: #334155;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease, border-color 0.16s ease;

    .chip-label {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &.is-assigned {
      background: #f8fafc;

      .chip-minus {
        font-size: 11px;
        color: #b6c2d2;
        transition: color 0.16s ease;
      }

      &:hover {
        border-color: #f0a8a8;
        background: #fff5f5;
        transform: translateY(-1px);

        .chip-minus {
          color: #d64545;
        }
      }
    }

    &.is-available {
      .chip-check {
        display: none;
        font-size: 11px;
      }

      &:hover {
        border-color: #9dc2ef;
        background: #f4f9ff;
        transform: translateY(-1px);
        box-shadow: 0 3px 10px rgba(11, 99, 206, 0.1);
      }

      &.is-selected {
        color: #fff;
        border-color: var(--accent);
        background: var(--accent);
        box-shadow: 0 3px 10px rgba(11, 99, 206, 0.24);

        .chip-check {
          display: inline-flex;
        }

        &:hover {
          background: #0954af;
        }
      }

      &.is-selected.type-data {
        border-color: var(--data);
        background: var(--data);

        &:hover {
          background: #0e7f58;
        }
      }

      &.is-selected.type-api {
        border-color: var(--api);
        background: var(--api);

        &:hover {
          background: #b96c05;
        }
      }
    }
  }

  .workspace-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 12px 18px;
    border: 1px solid var(--line);
    border-radius: 10px;
    background: linear-gradient(115deg, #f8fafc 0%, #ffffff 70%);
  }

  .footer-selection {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .selection-badge {
    min-width: 26px;
    height: 26px;
    padding: 0 7px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: #eaf2fd;
    color: var(--accent);
    font-size: 13px;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
  }

  .selection-label {
    font-size: 12px;
    color: var(--muted);
  }

  .clear-link {
    font-size: 12px;
  }

  .footer-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
  }

  .select-all-link {
    font-size: 12px;
  }

  @include respond-to(mobile) {
    .transfer-grid {
      grid-template-columns: minmax(0, 1fr);
      gap: 12px;
    }

    .transfer-pane {
      &.is-assigned,
      &.is-available {
        border: 1px solid var(--line);
        border-radius: 12px;
      }
    }

    .transfer-bridge {
      display: none;
    }

    .pane-scroll {
      max-height: 320px;
      min-height: 200px;
    }

    .workspace-toolbar,
    .workspace-footer {
      flex-wrap: wrap;
    }

    @include respond-to(phone) {
      .pane-scroll {
        max-height: 280px;
      }
    }
  }
}

.create-alert {
  margin-bottom: 16px;
}
</style>
