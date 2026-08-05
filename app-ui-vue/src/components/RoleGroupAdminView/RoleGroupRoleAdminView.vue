<template>
  <div class="role-group-role-admin-view">
    <!-- 顶部标题栏 -->
    <div class="header-bar">
      <div class="header-left">
        <h2 class="page-title">
          <span class="title-icon">⚙</span>
          <span>权限组权限管理</span>
        </h2>
      </div>
      <div class="header-right">
        <el-button
          :type="showAddPanel ? 'default' : 'primary'"
          class="toggle-add-btn"
          @click="toggleAddPanel"
        >
          <i :class="showAddPanel ? 'el-icon-close' : 'el-icon-plus'" />
          <span>{{ showAddPanel ? '收起面板' : '添加权限' }}</span>
        </el-button>
        <el-button
          icon="el-icon-refresh"
          circle
          size="small"
          class="refresh-btn"
          @click="fetchAllData"
          :loading="loadingAssigned"
        />
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 左侧：已分配权限 -->
      <div class="panel assigned-panel">
        <div class="panel-header">
          <span class="panel-title">
            <span class="panel-dot assigned" />
            已分配权限
          </span>
          <span class="panel-count">{{ assignedRoles.length }}</span>
        </div>

        <div class="panel-body" v-loading="loadingAssigned">
          <div v-if="assignedRoles.length === 0" class="empty-state">
            <i class="el-icon-folder-opened" />
            <p>暂未分配任何权限</p>
          </div>

          <div v-else class="permission-groups">
            <div
              v-for="(items, type) in groupedAssignedRoles"
              :key="type"
              class="perm-group"
            >
              <div class="group-label">
                <span class="type-badge" :class="getTypeClass(Number(type))">
                  {{ getTypeLabel(Number(type)) }}
                </span>
                <span class="group-count">{{ items.length }}</span>
              </div>
              <div class="perm-chips">
                <div
                  v-for="perm in items"
                  :key="perm.id"
                  class="perm-chip assigned"
                  :class="getTypeClass(perm.type)"
                  @click="handleRemovePermission(perm)"
                >
                  <span class="chip-name" :title="perm.name">{{ perm.name }}</span>
                  <i class="el-icon-close chip-remove" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：添加权限面板 -->
      <transition name="slide-right">
        <div v-if="showAddPanel" class="panel add-panel">
          <div class="panel-header">
            <span class="panel-title">
              <span class="panel-dot available" />
              可添加权限
            </span>
            <span class="panel-count">
              {{ filteredAvailableRoles.length }}
            </span>
          </div>

          <!-- 搜索和筛选 -->
          <div class="filter-bar">
            <div class="search-box">
              <i class="el-icon-search" />
              <input
                v-model="searchKeyword"
                placeholder="搜索权限名称..."
                class="search-input"
              />
              <i
                v-if="searchKeyword"
                class="el-icon-close clear-btn"
                @click="searchKeyword = ''"
              />
            </div>
            <div class="type-filters">
              <span
                class="filter-tag"
                :class="{ active: filterType === null }"
                @click="filterType = null"
              >
                全部
              </span>
              <span
                v-for="t in permissionTypes"
                :key="t.value"
                class="filter-tag"
                :class="[getTypeClass(t.value), { active: filterType === t.value }]"
                @click="filterType = t.value"
              >
                {{ t.label }}
              </span>
            </div>
          </div>

          <!-- 操作栏 -->
          <div class="action-bar">
            <el-button
              size="mini"
              type="text"
              class="select-all-btn"
              @click="handleSelectAll"
              :disabled="filteredAvailableRoles.length === 0"
            >
              <i class="el-icon-check" /> 全选当前筛选结果
            </el-button>
            <el-button
              size="mini"
              type="primary"
              class="batch-add-btn"
              @click="handleBatchAdd"
              :disabled="selectedPermIds.length === 0"
              :loading="batchAdding"
            >
              批量添加 ({{ selectedPermIds.length }})
            </el-button>
          </div>

          <!-- 可分配权限列表 -->
          <div class="panel-body" v-loading="loadingAvailable">
            <div v-if="filteredAvailableRoles.length === 0" class="empty-state">
              <i class="el-icon-search" />
              <p>{{ searchKeyword ? '未找到匹配的权限' : '没有可添加的权限' }}</p>
            </div>

            <div v-else class="permission-groups">
              <div
                v-for="(items, type) in groupedAvailableRoles"
                :key="type"
                class="perm-group"
              >
                <div class="group-label">
                  <span class="type-badge" :class="getTypeClass(Number(type))">
                    {{ getTypeLabel(Number(type)) }}
                  </span>
                  <span class="group-count">{{ items.length }}</span>
                </div>
                <div class="perm-chips">
                  <div
                    v-for="perm in items"
                    :key="perm.id"
                    class="perm-chip available"
                    :class="[getTypeClass(perm.type), { selected: selectedPermIds.includes(perm.id) }]"
                    @click="togglePermSelection(perm.id)"
                  >
                    <i class="el-icon-check chip-check" />
                    <span class="chip-name" :title="perm.name">{{ perm.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import { getRoleListByGroupId, appendRoleToGroup, removeRoleFromGroup } from '@/api/role_group'
import { queryRoles } from '@/api/role'

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
      showAddPanel: false,
      searchKeyword: '',
      filterType: null,
      permissionTypes: [
        { value: 1, label: '菜单' },
        { value: 2, label: '数据' },
        { value: 3, label: 'API' }
      ]
    }
  },
  computed: {
    groupedAssignedRoles() {
      return this._groupByType(this.assignedRoles)
    },
    groupedAvailableRoles() {
      return this._groupByType(this.filteredAvailableRoles)
    },
    filteredAvailableRoles() {
      let list = this.availableRoles.filter(
        p => !this.assignedRoles.find(a => a.id === p.id)
      )
      if (this.filterType !== null) {
        list = list.filter(p => p.type === this.filterType)
      }
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(p =>
          p.name.toLowerCase().includes(kw) ||
          (p.description && p.description.toLowerCase().includes(kw))
        )
      }
      return list
    }
  },
  mounted() {
    this.fetchAssignedRoles()
  },
  methods: {
    _groupByType(list) {
      const groups = {}
      list.forEach(item => {
        const t = item.type || 0
        if (!groups[t]) groups[t] = []
        groups[t].push(item)
      })
      return groups
    },

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

    fetchAllData() {
      this.fetchAssignedRoles()
      if (this.showAddPanel) {
        this.fetchAvailableRoles()
      }
    },

    toggleAddPanel() {
      this.showAddPanel = !this.showAddPanel
      if (this.showAddPanel && this.availableRoles.length === 0) {
        this.fetchAvailableRoles()
      }
    },

    togglePermSelection(permId) {
      const idx = this.selectedPermIds.indexOf(permId)
      if (idx > -1) {
        this.selectedPermIds.splice(idx, 1)
      } else {
        this.selectedPermIds.push(permId)
      }
    },

    handleSelectAll() {
      const allIds = this.filteredAvailableRoles.map(p => p.id)
      const allSelected = allIds.every(id => this.selectedPermIds.includes(id))
      if (allSelected) {
        this.selectedPermIds = []
      } else {
        this.selectedPermIds = [...allIds]
      }
    },

    async handleBatchAdd() {
      if (this.selectedPermIds.length === 0) return

      this.batchAdding = true
      try {
        for (const permId of this.selectedPermIds) {
          await appendRoleToGroup(this.groupId, permId)
        }
        this.$message.success(`成功添加 ${this.selectedPermIds.length} 个权限`)
        this.selectedPermIds = []
        await this.fetchAssignedRoles()
        await this.fetchAvailableRoles()
      } catch (error) {
        console.error('批量添加权限失败:', error)
        this.$message.error('添加部分权限失败，请重试')
      } finally {
        this.batchAdding = false
      }
    },

    async handleRemovePermission(perm) {
      try {
        await this.$confirm(
          `确定要移除权限 "${perm.name}" 吗？`,
          '移除确认',
          {
            confirmButtonText: '确定移除',
            cancelButtonText: '取消',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
        await removeRoleFromGroup(this.groupId, perm.id)
        this.$message.success('已移除')
        await this.fetchAssignedRoles()
        if (this.showAddPanel) {
          await this.fetchAvailableRoles()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除权限失败:', error)
        }
      }
    },

    getTypeLabel(type) {
      const typeMap = {
        1: '菜单权限',
        2: '数据权限',
        3: 'API权限'
      }
      return typeMap[type] || '未知'
    },

    getTypeClass(type) {
      const classMap = {
        1: 'type-menu',
        2: 'type-data',
        3: 'type-api'
      }
      return classMap[type] || 'type-unknown'
    }
  }
}
</script>

<style lang="scss" scoped>
.role-group-role-admin-view {
  display: flex;
  flex-direction: column;
  gap: 16px;

  /* ===== 顶部标题栏 ===== */
  .header-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e8ecf1;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

    .page-title {
      display: flex;
      align-items: center;
      gap: 10px;
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #1a1a2e;

      .title-icon {
        font-size: 20px;
        color: #0071e3;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 10px;

      .toggle-add-btn {
        height: 36px;
        border-radius: 8px;
        font-weight: 500;
        transition: all 0.2s;

        i {
          margin-right: 4px;
        }
      }

      .refresh-btn {
        border-radius: 8px;
      }
    }
  }

  /* ===== 主体内容区 ===== */
  .main-content {
    display: flex;
    gap: 16px;
    min-height: 400px;
  }

  /* ===== 面板公共样式 ===== */
  .panel {
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e8ecf1;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    display: flex;
    flex-direction: column;
    overflow: hidden;

    &.assigned-panel {
      flex: 1;
    }

    &.add-panel {
      flex: 1;
      border: 2px dashed #0071e3;
      border-color: rgba(0, 113, 227, 0.3);
    }

    .panel-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 14px 18px;
      background: #fafbfc;
      border-bottom: 1px solid #e8ecf1;

      .panel-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 600;
        color: #374151;

        .panel-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;

          &.assigned {
            background: #34c759;
            box-shadow: 0 0 6px rgba(52, 199, 89, 0.4);
          }

          &.available {
            background: #0071e3;
            box-shadow: 0 0 6px rgba(0, 113, 227, 0.4);
          }
        }
      }

      .panel-count {
        background: #f0f2f5;
        color: #6b7280;
        font-size: 12px;
        font-weight: 600;
        padding: 2px 10px;
        border-radius: 12px;
      }
    }

    .panel-body {
      flex: 1;
      padding: 16px;
      overflow-y: auto;
      max-height: 500px;
    }
  }

  /* ===== 空状态 ===== */
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 20px;
    color: #9ca3af;

    i {
      font-size: 40px;
      margin-bottom: 12px;
      opacity: 0.5;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }

  /* ===== 筛选栏 ===== */
  .filter-bar {
    padding: 12px 18px;
    border-bottom: 1px solid #e8ecf1;
    background: #fafbfc;

    .search-box {
      display: flex;
      align-items: center;
      background: #fff;
      border: 1px solid #d1d5db;
      border-radius: 8px;
      padding: 6px 12px;
      margin-bottom: 10px;
      transition: border-color 0.2s;

      &:focus-within {
        border-color: #0071e3;
        box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.1);
      }

      i {
        color: #9ca3af;
        font-size: 14px;

        &.clear-btn {
          cursor: pointer;
          margin-left: auto;

          &:hover {
            color: #6b7280;
          }
        }
      }

      .search-input {
        flex: 1;
        border: none;
        outline: none;
        padding: 0 8px;
        font-size: 13px;
        color: #374151;
        background: transparent;

        &::placeholder {
          color: #9ca3af;
        }
      }
    }

    .type-filters {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;

      .filter-tag {
        display: inline-flex;
        align-items: center;
        padding: 3px 10px;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 500;
        cursor: pointer;
        background: #f3f4f6;
        color: #6b7280;
        transition: all 0.15s;
        border: 1px solid transparent;

        &:hover {
          background: #e5e7eb;
        }

        &.active {
          background: #0071e3;
          color: #fff;
        }

        &.type-menu.active {
          background: #0071e3;
        }

        &.type-data.active {
          background: #34c759;
        }

        &.type-api.active {
          background: #ff9500;
        }
      }
    }
  }

  /* ===== 操作栏 ===== */
  .action-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 18px;
    border-bottom: 1px solid #e8ecf1;
    background: #fafbfc;

    .select-all-btn {
      color: #0071e3;
      font-weight: 500;
    }

    .batch-add-btn {
      border-radius: 8px;
      font-weight: 500;
    }
  }

  /* ===== 权限分组 ===== */
  .permission-groups {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .perm-group {
      .group-label {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 10px;

        .type-badge {
          display: inline-flex;
          align-items: center;
          padding: 3px 10px;
          border-radius: 6px;
          font-size: 12px;
          font-weight: 600;
          letter-spacing: 0.3px;

          &.type-menu {
            background: rgba(0, 113, 227, 0.1);
            color: #0071e3;
            border: 1px solid rgba(0, 113, 227, 0.2);
          }

          &.type-data {
            background: rgba(52, 199, 89, 0.1);
            color: #248a3d;
            border: 1px solid rgba(52, 199, 89, 0.2);
          }

          &.type-api {
            background: rgba(255, 149, 0, 0.1);
            color: #c77c00;
            border: 1px solid rgba(255, 149, 0, 0.2);
          }

          &.type-unknown {
            background: #f3f4f6;
            color: #6b7280;
            border: 1px solid #d1d5db;
          }
        }

        .group-count {
          font-size: 12px;
          color: #9ca3af;
          font-weight: 500;
        }
      }
    }
  }

  /* ===== 权限卡片/芯片 ===== */
  .perm-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .perm-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.15s;
    border: 1px solid #e5e7eb;
    background: #fff;
    color: #374151;
    max-width: 180px;

    .chip-name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .chip-check {
      display: none;
      font-size: 12px;
      color: #fff;
    }

    /* 已分配状态 */
    &.assigned {
      cursor: pointer;

      &:hover {
        border-color: #ef4444;
        background: rgba(239, 68, 68, 0.04);

        .chip-remove {
          opacity: 1;
          color: #ef4444;
        }
      }

      .chip-remove {
        font-size: 12px;
        color: #d1d5db;
        opacity: 0.5;
        transition: all 0.15s;
        margin-left: 2px;
      }

      &.type-menu {
        border-color: rgba(0, 113, 227, 0.25);
        background: rgba(0, 113, 227, 0.04);

        &:hover {
          border-color: #ef4444;
          background: rgba(239, 68, 68, 0.04);
        }
      }

      &.type-data {
        border-color: rgba(52, 199, 89, 0.25);
        background: rgba(52, 199, 89, 0.04);

        &:hover {
          border-color: #ef4444;
          background: rgba(239, 68, 68, 0.04);
        }
      }

      &.type-api {
        border-color: rgba(255, 149, 0, 0.25);
        background: rgba(255, 149, 0, 0.04);

        &:hover {
          border-color: #ef4444;
          background: rgba(239, 68, 68, 0.04);
        }
      }
    }

    /* 可添加状态 */
    &.available {
      &:hover {
        background: #f0f7ff;
        border-color: #0071e3;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(0, 113, 227, 0.15);
      }

      &.selected {
        background: #0071e3;
        border-color: #0071e3;
        color: #fff;
        box-shadow: 0 2px 8px rgba(0, 113, 227, 0.3);

        .chip-check {
          display: inline-flex;
        }

        .chip-name {
          color: #fff;
        }

        &:hover {
          background: #005bb5;
        }
      }

      &.type-menu.selected {
        background: #0071e3;
        border-color: #0071e3;
      }

      &.type-data.selected {
        background: #34c759;
        border-color: #34c759;
      }

      &.type-api.selected {
        background: #ff9500;
        border-color: #ff9500;
      }
    }
  }

  /* ===== 动画 ===== */
  .slide-right-enter-active,
  .slide-right-leave-active {
    transition: all 0.3s ease;
  }

  .slide-right-enter,
  .slide-right-leave-to {
    opacity: 0;
    transform: translateX(20px);
  }
}
</style>
