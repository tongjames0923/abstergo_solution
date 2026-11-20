<template>
  <div class="member-workspace">
    <div class="workspace-toolbar">
      <div class="toolbar-heading">
        <span class="heading-rail" />
        <h3 class="heading-title">{{ $t('roleGroup.tabMembers') }}</h3>
        <span class="heading-meta">{{ $t('roleGroup.totalMember', { count: assignedUsers.length }) }}</span>
      </div>
      <div class="toolbar-actions">
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
            <span class="identity-name">{{ $t('roleGroup.assignedUsers') }}</span>
            <span class="identity-count">{{ assignedUsers.length }}</span>
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
          <div v-if="filteredAssignedUsers.length === 0" class="pane-empty">
            <div class="empty-glyph">
              <i :class="assignedKeyword ? 'el-icon-search' : 'el-icon-user'" />
            </div>
            <p>{{ assignedKeyword ? $t('roleGroup.emptySearch') : $t('roleGroup.emptyAssignedUser') }}</p>
          </div>

          <div v-else class="member-list">
            <button
              v-for="user in filteredAssignedUsers"
              :key="user.userId || user.id"
              type="button"
              class="member-card is-assigned"
              :title="$t('roleGroup.removeUserConfirm', { name: displayName(user) })"
              @click="handleRemoveUser(user)"
            >
              <el-avatar :size="34" :src="user.avatar" class="member-avatar">
                <i class="el-icon-user" />
              </el-avatar>
              <span class="member-info">
                <span class="member-name">{{ displayName(user) }}</span>
                <span class="member-account">@{{ user.userName }}</span>
              </span>
              <i class="el-icon-close member-action" />
            </button>
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
            <span class="identity-name">{{ $t('roleGroup.availableUsers') }}</span>
            <span class="identity-count">{{ filteredAvailableUsers.length }}</span>
          </div>
          <div class="pane-search">
            <i class="el-icon-search" />
            <input
              v-model="searchKeyword"
              :placeholder="$t('roleGroup.searchUser')"
              class="pane-search-input"
            >
            <i
              v-if="searchKeyword"
              class="el-icon-close search-clear"
              @click="searchKeyword = ''"
            />
          </div>
        </header>

        <div v-loading="loadingAvailable" class="pane-scroll">
          <div v-if="filteredAvailableUsers.length === 0" class="pane-empty">
            <div class="empty-glyph">
              <i :class="searchKeyword ? 'el-icon-search' : 'el-icon-user'" />
            </div>
            <p>{{ searchKeyword ? $t('roleGroup.emptySearch') : $t('roleGroup.emptyAvailableUser') }}</p>
          </div>

          <div v-else class="member-list">
            <button
              v-for="user in filteredAvailableUsers"
              :key="user.id"
              type="button"
              class="member-card is-available"
              :class="{ 'is-selected': selectedUserIds.includes(user.id) }"
              @click="toggleUserSelection(user.id)"
            >
              <el-avatar :size="34" :src="user.avatar" class="member-avatar">
                <i class="el-icon-user" />
              </el-avatar>
              <span class="member-info">
                <span class="member-name">{{ displayName(user) }}</span>
                <span class="member-account">@{{ user.userName }}</span>
              </span>
              <i class="el-icon-check member-action" />
            </button>
          </div>
        </div>
      </section>
    </div>

    <footer class="workspace-footer">
      <div class="footer-selection">
        <span class="selection-badge">{{ selectedUserIds.length }}</span>
        <span class="selection-label">{{ $t('roleGroup.pendingLabel') }}</span>
        <el-button
          type="text"
          size="mini"
          class="clear-link"
          :disabled="selectedUserIds.length === 0"
          @click="selectedUserIds = []"
        >
          {{ $t('roleGroup.clearSelection') }}
        </el-button>
      </div>
      <div class="footer-actions">
        <el-button
          size="small"
          type="text"
          class="select-all-link"
          :disabled="filteredAvailableUsers.length === 0"
          @click="handleSelectAll"
        >
          {{ $t('roleGroup.selectAll') }}
        </el-button>
        <el-button
          size="small"
          type="primary"
          class="commit-button"
          :disabled="selectedUserIds.length === 0"
          :loading="batchAdding"
          @click="handleBatchAdd"
        >
          {{ $t('roleGroup.confirmAdd') }}
        </el-button>
      </div>
    </footer>
  </div>
</template>

<script>
import {appendUserToGroup, getUserListByGroupId, removeUserFromGroup} from '@/api/role_group'
import {adminQueryUser} from '@/api/admin_user'
import {runBatchWithLimit} from '@/utils/batch-request'

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
      searchKeyword: '',
      assignedKeyword: ''
    }
  },
  computed: {
    filteredAssignedUsers() {
      if (!this.assignedKeyword) return this.assignedUsers
      return this.assignedUsers.filter(user => this.matchKeyword(user, this.assignedKeyword))
    },
    filteredAvailableUsers() {
      return this.availableUsers.filter(user => {
        if (this.isAssigned(user)) return false
        return !this.searchKeyword || this.matchKeyword(user, this.searchKeyword)
      })
    }
  },
  mounted() {
    this.fetchAllData()
  },
  methods: {
    /**
     * 列表接口返回的 id 与组内接口返回的 userId 口径不同，两侧都需要识别
     * @param {Object} user - 待判断用户
     * @returns {boolean} 是否已在当前组
     */
    isAssigned(user) {
      return this.assignedUsers.some(assigned =>
        (user.id && assigned.id === user.id) ||
        (user.userId && assigned.userId === user.userId) ||
        (assigned.userId && assigned.userId === user.id) ||
        (assigned.id && assigned.id === user.userId)
      )
    },

    /**
     * 按用户名、昵称匹配关键词
     * @param {Object} user - 用户项
     * @param {string} keyword - 关键词
     * @returns {boolean} 是否匹配
     */
    matchKeyword(user, keyword) {
      const kw = keyword.toLowerCase()
      return [user.userName, user.nickName]
        .filter(Boolean)
        .some(field => String(field).toLowerCase().includes(kw))
    },

    /**
     * 用户展示名优先用昵称
     * @param {Object} user - 用户项
     * @returns {string} 展示名
     */
    displayName(user) {
      return user.nickName || user.userName
    },

    /**
     * 加载组内成员
     * @returns {Promise} 加载结果
     */
    async fetchAssignedUsers() {
      this.loadingAssigned = true
      try {
        const response = await getUserListByGroupId(this.groupId)
        this.assignedUsers = response.data || []
      } catch (error) {
        console.error('获取组内成员失败:', error)
      } finally {
        this.loadingAssigned = false
      }
    },

    /**
     * 加载全量用户
     * @returns {Promise} 加载结果
     */
    async fetchAvailableUsers() {
      this.loadingAvailable = true
      try {
        const response = await adminQueryUser({}, 1, 1000)
        this.availableUsers = response.data?.data || []
      } catch (error) {
        console.error('获取可添加用户失败:', error)
      } finally {
        this.loadingAvailable = false
      }
    },

    /**
     * 两侧一起刷新，保证分配后状态一致
     * @returns {Promise} 加载结果
     */
    async fetchAllData() {
      await Promise.all([this.fetchAssignedUsers(), this.fetchAvailableUsers()])
    },

    /**
     * 切换单个待添加成员
     * @param {number} userId - 用户ID
     */
    toggleUserSelection(userId) {
      const index = this.selectedUserIds.indexOf(userId)
      if (index > -1) {
        this.selectedUserIds.splice(index, 1)
      } else {
        this.selectedUserIds.push(userId)
      }
    },

    /**
     * 全选或取消当前筛选结果
     */
    handleSelectAll() {
      const allIds = this.filteredAvailableUsers.map(user => user.id)
      const allSelected = allIds.every(id => this.selectedUserIds.includes(id))
      this.selectedUserIds = allSelected ? [] : [...allIds]
    },

    /**
     * 并发批量添加成员，按成功/失败数量分别反馈
     * @returns {Promise} 操作结果
     */
    async handleBatchAdd() {
      if (this.selectedUserIds.length === 0) return
      const ids = [...this.selectedUserIds]
      this.batchAdding = true
      try {
        const results = await runBatchWithLimit(ids, id => appendUserToGroup(this.groupId, id))
        const success = results.filter(result => result.ok).length
        const fail = results.length - success
        if (fail === 0) {
          this.$message.success(this.$t('roleGroup.addSuccess', { count: success }))
        } else {
          this.$message.warning(this.$t('roleGroup.addPartialFail', { success, fail }))
        }
        this.selectedUserIds = []
        await this.fetchAllData()
      } catch (error) {
        console.error('批量添加成员失败:', error)
      } finally {
        this.batchAdding = false
      }
    },

    /**
     * 移除组内成员
     * @param {Object} user - 用户项
     * @returns {Promise} 操作结果
     */
    async handleRemoveUser(user) {
      try {
        await this.$confirm(
          this.$t('roleGroup.removeUserConfirm', { name: this.displayName(user) }),
          this.$t('roleGroup.removeConfirmTitle'),
          {
            confirmButtonText: this.$t('roleGroup.removeConfirmBtn'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
        await removeUserFromGroup(this.groupId, user.userId || user.id)
        this.$message.success(this.$t('roleGroup.removeSuccess'))
        await this.fetchAllData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除成员失败:', error)
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.member-workspace {
  --ink: #16233c;
  --muted: #7b8aa3;
  --line: #e4eaf2;
  --accent: #0b63ce;

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
      flex-shrink: 0;
    }

    .refresh-button {
      border-radius: 8px;
    }
  }

  .transfer-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 36px minmax(0, 1fr);
    align-items: stretch;
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

  .pane-scroll {
    flex: 1;
    min-height: 300px;
    max-height: 460px;
    overflow-y: auto;
    padding: 12px 16px;
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

  .member-list {
    display: flex;
    flex-direction: column;
    gap: 7px;
  }

  .member-card {
    appearance: none;
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    padding: 9px 11px;
    border: 1px solid #e6ebf2;
    border-radius: 9px;
    background: #fff;
    text-align: left;
    cursor: pointer;
    transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease, border-color 0.16s ease;

    .member-avatar {
      flex-shrink: 0;
    }

    .member-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .member-name {
      font-size: 13px;
      font-weight: 600;
      color: #334155;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .member-account {
      font-size: 11px;
      color: #94a3b8;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .member-action {
      flex-shrink: 0;
      font-size: 12px;
      color: #b6c2d2;
    }

    &.is-assigned {
      background: #f8fafc;

      &:hover {
        border-color: #f0a8a8;
        background: #fff5f5;
        transform: translateY(-1px);

        .member-action {
          color: #d64545;
        }
      }
    }

    &.is-available {
      .member-action {
        display: none;
      }

      &:hover {
        border-color: #9dc2ef;
        background: #f4f9ff;
        transform: translateY(-1px);
        box-shadow: 0 3px 10px rgba(11, 99, 206, 0.1);
      }

      &.is-selected {
        border-color: var(--accent);
        background: var(--accent);
        box-shadow: 0 3px 10px rgba(11, 99, 206, 0.24);

        .member-name,
        .member-account {
          color: #fff;
        }

        .member-action {
          display: inline-flex;
          color: #fff;
        }

        &:hover {
          background: #0954af;
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

  .clear-link,
  .select-all-link {
    font-size: 12px;
  }

  .footer-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
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
</style>
