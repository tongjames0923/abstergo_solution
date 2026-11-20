<template>
  <div class="role-group-management">
    <quick-use-management-view
      ref="roleGroupManagement"
      :main-title="$t('roleGroup.title')"
      :config-name="roleGroupSearchFormKey()"
      :search-handler="searchHandler"
      :columns="columns"
      title-field="roleGroupName"
      :card-fields="['description', 'flag', 'createTime']"
      :add-form-config-name="roleGroupAddFormKey()"
      :add-handler="(formData) => saveRoleGroupData(formData)"
      :edit-form-config-name="roleGroupEditFormKey()"
      :edit-handler="(formData) => saveRoleGroupData(formData)"
      :query-transformer="rangeQueryTransform"
      :delete-handler="handleDeleteGroup"
      :delete-confirm-text="deleteGroupConfirmText"
      @add-success="(data)=>{}"
      @edit-success="(data)=>{}"
    >
      <template #actions-dropdown="{ row }">
        <el-dropdown-item command="manageRoles" @click.native="handleManageRoles(row)">
          {{ $t('roleGroup.managePerms') }}
        </el-dropdown-item>
        <el-dropdown-item command="manageUsers" @click.native="handleManageUsers(row)">
          {{ $t('roleGroup.manageUsers') }}
        </el-dropdown-item>
      </template>
    </quick-use-management-view>

    <el-dialog
      :title="$t('roleGroup.workspaceTitle')"
      :visible.sync="workspaceVisible"
      width="94%"
      top="4vh"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="role-group-workspace"
      @close="handleWorkspaceClose"
    >
      <div class="workspace-context">
        <span class="context-caption">{{ $t('roleGroup.currentGroup') }}</span>
        <span class="context-rule" />
        <span class="context-name">{{ currentGroupName }}</span>
        <span v-if="currentGroupDescription" class="context-desc">{{ currentGroupDescription }}</span>
      </div>

      <el-tabs v-model="activeTab" class="workspace-tabs">
        <el-tab-pane :label="$t('roleGroup.tabPermissions')" name="permissions">
          <RoleGroupRoleAdminView
            v-if="workspaceVisible && activeTab === 'permissions'"
            :group-id="currentGroupId"
          />
        </el-tab-pane>
        <el-tab-pane :label="$t('roleGroup.tabMembers')" name="users">
          <RoleGroupUserAdminView
            v-if="workspaceVisible && activeTab === 'users'"
            :group-id="currentGroupId"
          />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import RoleGroupRoleAdminView from '@/components/RoleGroupAdminView/RoleGroupRoleAdminView.vue'
import RoleGroupUserAdminView from '@/components/RoleGroupAdminView/RoleGroupUserAdminView.vue'
import { queryRoleGroups, saveRoleGroup, deleteRoleGroup } from '@/api/role_group'
import { rangeQueryTransform } from '@/utils/query-help'
import { roleGroupAddFormKey, roleGroupEditFormKey, roleGroupSearchFormKey } from '@/settings'

export default {
  name: 'RoleGroupAdminView',
  components: {
    QuickUseManagementView,
    RoleGroupRoleAdminView,
    RoleGroupUserAdminView
  },
  data() {
    return {
      workspaceVisible: false,
      activeTab: 'permissions',
      currentGroupId: null,
      currentGroupName: '',
      currentGroupDescription: ''
    }
  },
  computed: {
    columns() {
      return [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'roleGroupName', label: this.$t('roleGroup.groupName'), minWidth: 180 },
        { prop: 'description', label: this.$t('roleGroup.description'), minWidth: 200 },
        {
          prop: 'flag',
          label: this.$t('roleGroup.status'),
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1) {
              return <el-tag type='success'>{ this.$t('roleGroup.normal') }</el-tag>
            }
            if (scope.row.flag === 0) {
              return <el-tag type='danger'>{ this.$t('roleGroup.disabled') }</el-tag>
            }
            return <el-tag>{ this.$t('roleGroup.unknown') }</el-tag>
          }
        },
        { prop: 'createTime', label: this.$t('roleGroup.createTime'), width: 180, sortable: true },
        { prop: 'updateTime', label: this.$t('roleGroup.updateTime'), width: 180, sortable: true }
      ]
    }
  },
  methods: {
    roleGroupEditFormKey() {
      return roleGroupEditFormKey
    },
    roleGroupAddFormKey() {
      return roleGroupAddFormKey
    },
    roleGroupSearchFormKey() {
      return roleGroupSearchFormKey
    },

    saveRoleGroupData: saveRoleGroup,
    rangeQueryTransform,

    /**
     * 删除权限组（后端为逻辑删除，删除后组内绑定随之失效）
     * @param {Object} row - 权限组行数据
     * @returns {Promise} 删除结果
     */
    handleDeleteGroup(row) {
      return deleteRoleGroup(row.id)
    },

    /**
     * 删除确认文案，带出权限组名称避免误删
     * @param {Object} row - 权限组行数据
     * @returns {string} 确认文案
     */
    deleteGroupConfirmText(row) {
      return this.$t('roleGroup.deleteConfirm', { name: row.roleGroupName })
    },

    /**
     * 权限组列表查询，适配 AbstractCrudController 的数据格式
     * @param {Object} bodyParams - 查询条件
     * @param {Object} requestParams - 分页参数
     * @returns {Promise} 查询结果
     */
    async searchHandler(bodyParams, requestParams) {
      return await queryRoleGroups(bodyParams, requestParams.pageNum, requestParams.pageSize)
    },

    /**
     * 打开指定权限组的工作台并定位到权限分配页
     * @param {Object} row - 权限组行数据
     */
    handleManageRoles(row) {
      this.openWorkspace(row, 'permissions')
    },

    /**
     * 打开指定权限组的工作台并定位到成员管理页
     * @param {Object} row - 权限组行数据
     */
    handleManageUsers(row) {
      this.openWorkspace(row, 'users')
    },

    /**
     * 统一打开工作台，避免两个独立弹窗来回切换
     * @param {Object} row - 权限组行数据
     * @param {string} tab - 初始页签
     */
    openWorkspace(row, tab) {
      this.currentGroupId = row.id
      this.currentGroupName = row.roleGroupName
      this.currentGroupDescription = row.description || ''
      this.activeTab = tab
      this.workspaceVisible = true
    },

    /**
     * 关闭工作台时清空上下文，防止下一次打开残留上一个权限组
     */
    handleWorkspaceClose() {
      this.currentGroupId = null
      this.currentGroupName = ''
      this.currentGroupDescription = ''
      this.activeTab = 'permissions'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.role-group-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.role-group-workspace {
  ::v-deep .el-dialog__header {
    padding: 18px 24px 14px;
    border-bottom: 1px solid #e7ecf3;
    background: linear-gradient(120deg, #f7faff 0%, #ffffff 70%);
    border-radius: 12px 12px 0 0;
  }

  ::v-deep .el-dialog__title {
    font-size: 17px;
    font-weight: 700;
    letter-spacing: 0.2px;
    color: #14213d;
  }

  ::v-deep .el-dialog__body {
    padding: 0 20px 20px;
    background: #fbfcfe;
    border-radius: 0 0 12px 12px;
  }

  .workspace-context {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 4px 10px;
    font-size: 13px;

    .context-caption {
      color: #94a3b8;
      font-weight: 500;
      letter-spacing: 0.4px;
    }

    .context-rule {
      width: 1px;
      height: 12px;
      background: #dbe3ee;
    }

    .context-name {
      color: #14213d;
      font-weight: 700;
      font-size: 15px;
    }

    .context-desc {
      color: #64748b;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .workspace-tabs {
    ::v-deep .el-tabs__header {
      margin: 0 0 12px;
    }

    ::v-deep .el-tabs__item {
      font-weight: 600;
      color: #64748b;

      &.is-active {
        color: #0b63ce;
      }
    }

    ::v-deep .el-tabs__active-bar {
      height: 3px;
      border-radius: 3px;
    }
  }

  @include respond-to(mobile) {
    ::v-deep .el-dialog__body {
      padding: 0 12px 12px;
    }
  }
}
</style>
