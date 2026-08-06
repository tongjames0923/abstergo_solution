<template>
  <div class="role-group-user-admin-view">
    <!-- 顶部标题栏 -->
    <div class="header-bar">
      <div class="header-left">
        <h2 class="page-title">
          <span class="title-icon">👥</span>
          <span>角色组用户管理</span>
        </h2>
      </div>
      <div class="header-right">
        <el-button
          :type="showAddPanel ? 'default' : 'primary'"
          class="toggle-add-btn"
          @click="toggleAddPanel"
        >
          <i :class="showAddPanel ? 'el-icon-close' : 'el-icon-plus'" />
          <span>{{ showAddPanel ? '收起面板' : '添加用户' }}</span>
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
      <!-- 左侧：已分配用户 -->
      <div class="panel assigned-panel">
        <div class="panel-header">
          <span class="panel-title">
            <span class="panel-dot assigned" />
            已分配用户
          </span>
          <span class="panel-count">{{ assignedUsers.length }}</span>
        </div>

        <div class="panel-body" v-loading="loadingAssigned">
          <div v-if="assignedUsers.length === 0" class="empty-state">
            <i class="el-icon-user" />
            <p>暂未分配任何用户</p>
          </div>

          <div v-else class="user-list">
            <div
              v-for="user in assignedUsers"
              :key="user.id"
              class="user-card assigned"
              @click="handleRemoveUser(user)"
            >
              <div class="user-avatar">
                <el-avatar :size="36" :src="user.avatar">
                  <i class="el-icon-user" />
                </el-avatar>
              </div>
              <div class="user-info">
                <span class="user-name" :title="user.nickName || user.userName">
                  {{ user.nickName || user.userName }}
                </span>
                <span class="user-username" :title="user.userName">
                  @{{ user.userName }}
                </span>
              </div>
              <i class="el-icon-close remove-icon" />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：添加用户面板 -->
      <transition name="slide-right">
        <div v-if="showAddPanel" class="panel add-panel">
          <div class="panel-header">
            <span class="panel-title">
              <span class="panel-dot available" />
              可添加用户
            </span>
            <span class="panel-count">
              {{ filteredAvailableUsers.length }}
            </span>
          </div>

          <!-- 搜索 -->
          <div class="filter-bar">
            <div class="search-box">
              <i class="el-icon-search" />
              <input
                v-model="searchKeyword"
                placeholder="搜索用户名或昵称..."
                class="search-input"
              />
              <i
                v-if="searchKeyword"
                class="el-icon-close clear-btn"
                @click="searchKeyword = ''"
              />
            </div>
          </div>

          <!-- 操作栏 -->
          <div class="action-bar">
            <el-button
              size="mini"
              type="text"
              class="select-all-btn"
              @click="handleSelectAll"
              :disabled="filteredAvailableUsers.length === 0"
            >
              <i class="el-icon-check" /> 全选当前筛选结果
            </el-button>
            <el-button
              size="mini"
              type="primary"
              class="batch-add-btn"
              @click="handleBatchAdd"
              :disabled="selectedUserIds.length === 0"
              :loading="batchAdding"
            >
              批量添加 ({{ selectedUserIds.length }})
            </el-button>
          </div>

          <!-- 可分配用户列表 -->
          <div class="panel-body" v-loading="loadingAvailable">
            <div v-if="filteredAvailableUsers.length === 0" class="empty-state">
              <i class="el-icon-search" />
              <p>{{ searchKeyword ? '未找到匹配的用户' : '没有可添加的用户' }}</p>
            </div>

            <div v-else class="user-list">
              <div
                v-for="user in filteredAvailableUsers"
                :key="user.id"
                class="user-card available"
                :class="{ selected: selectedUserIds.includes(user.id) }"
                @click="toggleUserSelection(user.id)"
              >
                <div class="user-avatar">
                  <el-avatar :size="36" :src="user.avatar">
                    <i class="el-icon-user" />
                  </el-avatar>
                </div>
                <div class="user-info">
                  <span class="user-name" :title="user.nickName || user.userName">
                    {{ user.nickName || user.userName }}
                  </span>
                  <span class="user-username" :title="user.userName">
                    @{{ user.userName }}
                  </span>
                </div>
                <i class="el-icon-check check-icon" />
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import { getUserListByGroupId, appendUserToGroup, removeUserFromGroup } from '@/api/role_group'
import { adminQueryUser } from '@/api/admin_user'

export default {
  name: 'RoleGroupUserAdminView',
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
      assignedUsers: [],
      availableUsers: [],
      selectedUserIds: [],
      showAddPanel: false,
      searchKeyword: ''
    }
  },
  computed: {
    filteredAvailableUsers() {
      let list = this.availableUsers.filter(
        u => !this.assignedUsers.find(a => a.id === u.id)
      )
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(u =>
          (u.userName && u.userName.toLowerCase().includes(kw)) ||
          (u.nickName && u.nickName.toLowerCase().includes(kw))
        )
      }
      return list
    }
  },
  mounted() {
    this.fetchAssignedUsers()
  },
  methods: {
    async fetchAssignedUsers() {
      this.loadingAssigned = true
      try {
        const response = await getUserListByGroupId(this.groupId)
        this.assignedUsers = response.data || []
      } catch (error) {
        console.error('获取已分配用户失败:', error)
      } finally {
        this.loadingAssigned = false
      }
    },

    async fetchAvailableUsers() {
      this.loadingAvailable = true
      try {
        const response = await adminQueryUser({}, 1, 1000)
        this.availableUsers = response.data?.data || []
      } catch (error) {
        console.error('获取可分配用户失败:', error)
      } finally {
        this.loadingAvailable = false
      }
    },

    fetchAllData() {
      this.fetchAssignedUsers()
      if (this.showAddPanel) {
        this.fetchAvailableUsers()
      }
    },

    toggleAddPanel() {
      this.showAddPanel = !this.showAddPanel
      if (this.showAddPanel && this.availableUsers.length === 0) {
        this.fetchAvailableUsers()
      }
    },

    toggleUserSelection(userId) {
      const idx = this.selectedUserIds.indexOf(userId)
      if (idx > -1) {
        this.selectedUserIds.splice(idx, 1)
      } else {
        this.selectedUserIds.push(userId)
      }
    },

    handleSelectAll() {
      const allIds = this.filteredAvailableUsers.map(u => u.id)
      const allSelected = allIds.every(id => this.selectedUserIds.includes(id))
      if (allSelected) {
        this.selectedUserIds = []
      } else {
        this.selectedUserIds = [...allIds]
      }
    },

    async handleBatchAdd() {
      if (this.selectedUserIds.length === 0) return

      this.batchAdding = true
      try {
        for (const userId of this.selectedUserIds) {
          await appendUserToGroup(this.groupId, userId)
        }
        this.$message.success(`成功添加 ${this.selectedUserIds.length} 个用户`)
        this.selectedUserIds = []
        await this.fetchAssignedUsers()
        await this.fetchAvailableUsers()
      } catch (error) {
        console.error('批量添加用户失败:', error)
        this.$message.error('添加部分用户失败，请重试')
      } finally {
        this.batchAdding = false
      }
    },

    async handleRemoveUser(user) {
      try {
        await this.$confirm(
          `确定要移除用户 "${user.nickName || user.userName}" 吗？`,
          '移除确认',
          {
            confirmButtonText: '确定移除',
            cancelButtonText: '取消',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
        await removeUserFromGroup(this.groupId, user.id)
        this.$message.success('已移除')
        await this.fetchAssignedUsers()
        if (this.showAddPanel) {
          await this.fetchAvailableUsers()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除用户失败:', error)
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.role-group-user-admin-view {
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

  /* ===== 用户列表 ===== */
  .user-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .user-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    background: #fff;
    cursor: pointer;
    transition: all 0.15s;

    .user-avatar {
      flex-shrink: 0;
    }

    .user-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 2px;

      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .user-username {
        font-size: 12px;
        color: #9ca3af;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .remove-icon {
      font-size: 14px;
      color: #d1d5db;
      opacity: 0.5;
      transition: all 0.15s;
    }

    .check-icon {
      font-size: 14px;
      color: #fff;
      display: none;
    }

    /* 已分配状态 */
    &.assigned {
      &:hover {
        border-color: #ef4444;
        background: rgba(239, 68, 68, 0.04);

        .remove-icon {
          opacity: 1;
          color: #ef4444;
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

        .check-icon {
          display: inline-flex;
        }

        .user-name,
        .user-username {
          color: #fff;
        }

        &:hover {
          background: #005bb5;
        }
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
