<template>
  <div class="role-group-management">
    <quick-use-management-view
      ref="roleGroupManagement"
      main-title="角色组管理"
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
      @add-success="(data)=>{}"
      @edit-success="(data)=>{}"
    >
      <template #actions-dropdown="{ row, index }">
        <el-dropdown-item command="manageRoles" @click.native="handleManageRoles(row)">
          管理权限
        </el-dropdown-item>
        <el-dropdown-item command="manageUsers" @click.native="handleManageUsers(row)">
          管理用户
        </el-dropdown-item>
      </template>
    </quick-use-management-view>

    <!-- 权限管理弹窗 -->
    <el-dialog
      title="权限管理"
      :visible.sync="roleManagementDialogVisible"
      width="90%"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="role-dialog"
      @close="handleRoleManagementDialogClose"
    >
      <RoleGroupRoleAdminView
        v-if="roleManagementDialogVisible"
        :group-id="currentGroupId"
        @close="handleRoleManagementDialogClose"
      />
    </el-dialog>

    <!-- 用户管理弹窗 -->
    <el-dialog
      title="用户管理"
      :visible.sync="userManagementDialogVisible"
      width="90%"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="user-dialog"
      @close="handleUserManagementDialogClose"
    >
      <RoleGroupUserAdminView
        v-if="userManagementDialogVisible"
        :group-id="currentGroupId"
        @close="handleUserManagementDialogClose"
      />
    </el-dialog>
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import RoleGroupRoleAdminView from '@/components/RoleGroupAdminView/RoleGroupRoleAdminView.vue'
import RoleGroupUserAdminView from '@/components/RoleGroupAdminView/RoleGroupUserAdminView.vue'
import { queryRoleGroups, saveRoleGroup } from '@/api/role_group'
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
      roleManagementDialogVisible: false,
      userManagementDialogVisible: false,
      currentGroupId: null,
      columns: [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'roleGroupName', label: '角色组名称', width: 200 },
        { prop: 'description', label: '描述', minWidth: 200 },
        {
          prop: 'flag',
          label: '状态',
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1) {
              return <el-tag type='success'>正常</el-tag>
            } else if (scope.row.flag === 0) {
              return <el-tag type='danger'>禁用</el-tag>
            } else {
              return <el-tag>未知</el-tag>
            }
          }
        },
        { prop: 'createTime', label: '创建时间', width: 180, sortable: true },
        { prop: 'updateTime', label: '更新时间', width: 180, sortable: true }
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

    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {
      // 返回符合组件要求的数据格式
      return await queryRoleGroups(
        bodyParams, // 查询条件和排序作为body
        requestParams.pageNum, // 分页参数作为查询参数
        requestParams.pageSize
      )
    },

    /**
     * 处理权限管理按钮点击事件
     */
    handleManageRoles(row) {
      this.currentGroupId = row.id
      this.roleManagementDialogVisible = true
    },

    /**
     * 处理权限管理弹窗关闭事件
     */
    handleRoleManagementDialogClose() {
      this.currentGroupId = null
      this.roleManagementDialogVisible = false
    },

    /**
     * 处理用户管理按钮点击事件
     */
    handleManageUsers(row) {
      this.currentGroupId = row.id
      this.userManagementDialogVisible = true
    },

    /**
     * 处理用户管理弹窗关闭事件
     */
    handleUserManagementDialogClose() {
      this.currentGroupId = null
      this.userManagementDialogVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.role-group-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .role-dialog, .user-dialog {
    ::v-deep .el-dialog__header {
      background-color: #f8f9fa;
      border-bottom: 1px solid #ebeef5;
      padding: 15px 20px;
      border-radius: 8px 8px 0 0 !important;
    }

    ::v-deep .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  ::v-deep .el-dialog__footer {
    border-top: 1px solid #ebeef5;
    padding: 15px 20px;
  }
}
</style>
