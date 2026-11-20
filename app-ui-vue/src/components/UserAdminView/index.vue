<template>
  <div class="user-management">
    <quick-use-management-view
      ref="userManagement"
      :main-title="$t('user.title')"
      :config-name="userSearchFormKey()"
      :search-handler="searchHandler"
      :columns="columns"
      title-field="userName"
      :card-fields="['avatar', 'nickName', 'flag', 'createTime']"
      :add-form-config-name="userAddFormKey()"
      :add-handler="(formData) => saveUser(formData)"
      :edit-form-config-name="userEditFormKey()"
      :edit-handler="(formData) => saveUser(formData)"
      :query-transformer="rangeQueryTransform"
      @add-success="(data)=>{}"
      @edit-success="(data)=>{}"
    >
      <template #actions-dropdown="{ row }">
        <el-dropdown-item command="toggleStatus" @click.native="handleToggleStatus(row)">
          {{ row.flag === 1 ? $t('user.disable') : $t('user.enable') }}
        </el-dropdown-item>
        <el-dropdown-item command="showLoginInfo" @click.native="handleShowLoginInfo(row)">
          {{ $t('user.loginInfo') }}
        </el-dropdown-item>
        <el-dropdown-item command="resetPassword" @click.native="handleResetPassword(row)">
          {{ $t('user.resetPassword') }}
        </el-dropdown-item>
        <el-dropdown-item command="showRoleGroups" @click.native="handleShowRoleGroups(row)">
          {{ $t('user.roleGroups') }}
        </el-dropdown-item>
        <el-dropdown-item command="deleteUser" divided @click.native="handleDeleteUser(row)">
          {{ $t('user.delete') }}
        </el-dropdown-item>
      </template>
    </quick-use-management-view>

    <!-- 重置密码弹窗 -->
    <el-dialog
      :title="$t('user.resetPasswordTitle')"
      :visible.sync="resetPasswordDialogVisible"
      width="450px"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="password-dialog"
      @close="handleResetPasswordDialogClose"
    >
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item :label="$t('user.usernameLabel')" prop="userName">
          <el-input
            v-model="passwordForm.userName"
            disabled
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item :label="$t('user.newPasswordLabel')" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            show-password
            prefix-icon="el-icon-lock"
            :placeholder="$t('user.inputNewPassword')"
          />
        </el-form-item>
        <el-form-item :label="$t('user.confirmPasswordLabel')" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            prefix-icon="el-icon-lock"
            :placeholder="$t('user.inputConfirmPassword')"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetPasswordDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">{{ $t('common.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!-- 登录信息弹窗 -->
    <el-dialog
      :title="$t('user.loginInfoTitle')"
      :visible.sync="loginInfoDialogVisible"
      width="90%"
      :modal="false"
      class="login-info-dialog"
      @close="handleLoginInfoDialogClose"
    >
      <UserLoginInfoView
        v-if="loginInfoDialogVisible"
        :user-id="currentUserId"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="loginInfoDialogVisible = false">{{ $t('common.cancel') }}</el-button>
      </div>
    </el-dialog>

    <!-- 所属权限组弹窗：展示该用户被分配到哪些权限组及组内权限 -->
    <el-dialog
      :title="$t('user.roleGroupsTitle', { name: roleGroupsUser ? (roleGroupsUser.nickName || roleGroupsUser.userName) : '' })"
      :visible.sync="roleGroupsDialogVisible"
      width="560px"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="role-groups-dialog"
      @close="handleRoleGroupsDialogClose"
    >
      <div v-loading="roleGroupsLoading" class="role-groups-body">
        <div v-if="!roleGroupsLoading && userRoleGroups.length === 0" class="role-groups-empty">
          <i class="el-icon-collection" />
          <p>{{ $t('userDetail.noRoleGroups') }}</p>
        </div>
        <div v-else class="role-groups-list">
          <div
            v-for="item in userRoleGroups"
            :key="item.group && item.group.id"
            class="role-group-item"
          >
            <div class="role-group-head">
              <span class="role-group-name">{{ item.group ? item.group.roleGroupName : '-' }}</span>
              <span
                class="role-group-status"
                :class="getGroupStatusClass(item.group ? item.group.flag : null)"
              >
                {{ getGroupStatusLabel(item.group ? item.group.flag : null) }}
              </span>
              <span class="role-group-count">
                {{ $t('roleGroup.totalPerm', { count: (item.permissions || []).length }) }}
              </span>
            </div>
            <p v-if="item.group && item.group.description" class="role-group-desc">
              {{ item.group.description }}
            </p>
            <div class="role-group-perms">
              <el-tag
                v-for="role in item.permissions || []"
                :key="role.id"
                size="mini"
                effect="plain"
              >
                {{ role.name }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="roleGroupsDialogVisible = false">{{ $t('common.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import UserLoginInfoView from '@/components/UserAdminView/UserLoginInfoView.vue'
import {activateUser, adminQueryUser, deleteUser, disableUser, saveUser} from '@/api/admin_user'
import {getUserRoleGroupVOList, isUserInAnyGroup} from '@/api/role_group'
import {rangeQueryTransform} from '@/utils/query-help'
import {userAddFormKey, userEditFormKey, userSearchFormKey} from '@/settings'

export default {
  name: 'UserAdminView',
  components: {
    QuickUseManagementView,
    UserLoginInfoView
  },
  data() {
    // 确认密码验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('user.confirmPasswordRequired')))
      } else if (value !== this.passwordForm.password) {
        callback(new Error(this.$t('user.confirmPasswordMismatch')))
      } else {
        callback()
      }
    }

    return {
      resetPasswordDialogVisible: false,
      loginInfoDialogVisible: false,
      roleGroupsDialogVisible: false,
      roleGroupsLoading: false,
      roleGroupsUser: null,
      userRoleGroups: [],
      currentUserId: null,
      passwordForm: {
        id: undefined,
        userName: '',
        password: '',
        confirmPassword: ''
      },
      passwordRules: {
        // password: [
        //   { required: true, message: '请输入新密码', trigger: 'blur' },
        //   { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        // ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      columns: [
        { prop: 'id', label: 'ID', width: 80 },
        {
          prop: 'avatar',
          label: this.$t('user.avatar'),
          width: 120,
          render: (h, scope) => {
            return <el-avatar fit='fill' src={scope.row.avatar}>
              <i class='el-icon-warning-outline'></i>
            </el-avatar>
          }
        },
        { prop: 'userName', label: this.$t('user.username'), width: 180 },
        { prop: 'nickName', label: this.$t('user.nickname'), width: 180 },
        { prop: 'remark', label: this.$t('user.remark'), minWidth: 200 },
        {
          prop: 'flag',
          label: this.$t('user.status'),
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1) {
              return <el-tag type='success'>{this.$t('common.normal')}</el-tag>
            } else if (scope.row.flag === 0) {
              return <el-tag type='danger'>{this.$t('user.disable')}</el-tag>
            } else if (scope.row.flag === 2) {
              return <el-tag type='warning'>{this.$t('user.lock')}</el-tag>
            } else {
              return <el-tag>{this.$t('common.error')}</el-tag>
            }
          }
        },
        { prop: 'createTime', label: this.$t('user.createTime'), width: 180, sortable: true },
        { prop: 'updateTime', label: this.$t('user.updateTime'), width: 180, sortable: true }
      ]
    }
  },
  methods: {
    userEditFormKey() {
      return userEditFormKey
    },
    userAddFormKey() {
      return userAddFormKey
    },
    userSearchFormKey() {
      return userSearchFormKey
    },

    async handleShowLoginInfo(row) {
      this.currentUserId = row.id
      this.loginInfoDialogVisible = true
    },

    handleLoginInfoDialogClose() {
      this.currentUserId = null
      this.loginInfoDialogVisible = false
    },

    /**
     * 查看用户所属权限组及组内权限
     * @param {Object} row - 用户行数据
     * @returns {Promise} 加载结果
     */
    async handleShowRoleGroups(row) {
      this.roleGroupsUser = row
      this.userRoleGroups = []
      this.roleGroupsDialogVisible = true
      this.roleGroupsLoading = true
      try {
        const response = await getUserRoleGroupVOList(row.id)
        this.userRoleGroups = response.data || []
      } catch (error) {
        console.error('获取用户权限组失败:', error)
      } finally {
        this.roleGroupsLoading = false
      }
    },

    /**
     * 关闭权限组弹窗时清空上下文
     */
    handleRoleGroupsDialogClose() {
      this.roleGroupsUser = null
      this.userRoleGroups = []
    },

    /**
     * 权限组状态文案（组实体 flag 走 CommonFlagEnum：1 启用、-1 禁用）
     * @param {number} flag - 状态标识
     * @returns {string} 文案
     */
    getGroupStatusLabel(flag) {
      if (flag === 1) {
        return this.$t('roleGroup.normal')
      }
      if (flag === -1) {
        return this.$t('roleGroup.disabled')
      }
      return this.$t('roleGroup.unknown')
    },

    /**
     * 权限组状态样式类
     * @param {number} flag - 状态标识
     * @returns {string} 样式类名
     */
    getGroupStatusClass(flag) {
      if (flag === 1) {
        return 'is-normal'
      }
      if (flag === -1) {
        return 'is-disabled'
      }
      return 'is-unknown'
    },

    saveUser,
    deleteUser,
    activateUser,
    disableUser,
    rangeQueryTransform,
    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {
      // 返回符合组件要求的数据格式
      return await adminQueryUser(
        bodyParams, // 查询条件和排序作为body
        requestParams.pageNum, // 分页参数作为查询参数
        requestParams.pageSize
      )
    },
    /**
     * 切换用户状态
     */
    async handleToggleStatus(row) {
      try {
        let response
        if (row.flag === 1) {
          // 当前是正常状态，点击后应该禁用
          await this.$confirm(this.$t('user.confirmDisable'), this.$t('common.tip'), {
            confirmButtonText: this.$t('common.confirm'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning'
          })
          response = await disableUser(row.id)
        } else {
          // 当前是禁用或锁定状态，点击后应该激活
          await this.$confirm(this.$t('user.confirmEnable'), this.$t('common.tip'), {
            confirmButtonText: this.$t('common.confirm'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning'
          })
          response = await activateUser(row.id)
        }

        if (response.code === 200) {
          // 消息提示由后端统一控制
          // 刷新数据
          this.$refs.userManagement.refresh()
        } else {
          // 消息提示由后端统一控制
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('操作失败:', error)
          // 消息提示由后端统一控制
        }
      }
    },

    /**
     * 重置密码按钮点击事件
     */
    handleResetPassword(row) {
      this.passwordForm = {
        id: row.id,
        userName: row.userName,
        password: '',
        confirmPassword: ''
      }
      this.resetPasswordDialogVisible = true
    },

    /**
     * 处理密码重置提交事件
     */
    async handlePasswordSubmit() {
      this.$refs.passwordForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await saveUser(this.passwordForm)
            if (response.code === 200) {
              // 消息提示由后端统一控制
              this.resetPasswordDialogVisible = false
            } else {
              // 消息提示由后端统一控制
            }
          } catch (error) {
            console.error('密码重置失败:', error)
            // 消息提示由后端统一控制
          }
        }
      })
    },

    /**
     * 处理重置密码弹窗关闭事件
     */
    handleResetPasswordDialogClose() {
      this.$refs.passwordForm.resetFields()
    },

    /**
     * 删除用户（删除前检查权限组占用，仍在组内时在确认文案中明确告知后果）
     */
    async handleDeleteUser(row) {
      let confirmText = this.$t('user.confirmDelete')
      try {
        // 占用检查失败不阻断删除，由后端兜底
        const checkResponse = await isUserInAnyGroup(row.id)
        if (checkResponse.data === true) {
          confirmText = this.$t('user.deleteInGroupWarning', { name: row.nickName || row.userName })
        }
      } catch (error) {
        console.error('检查用户权限组占用失败:', error)
      }
      this.$confirm(confirmText, this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(async() => {
        try {
          await deleteUser(row.id)
          // 消息提示由后端统一控制
          this.$refs.userManagement.refresh()
        } catch (error) {
          if (error !== 'cancel') {
            console.error('删除用户失败:', error)
            // 消息提示由后端统一控制
          }
        }
      }).catch(() => {
        // 用户取消删除
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-info-dialog {
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

  ::v-deep .el-table-column--selection .cell {
    padding-left: 14px;
    padding-right: 14px;
  }
}

.user-management {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  padding: 20px;
  background-color: #f5f7fa;
  overflow: hidden;

  .password-dialog {
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

.role-groups-dialog {
  ::v-deep .el-dialog__body {
    padding-top: 10px;
  }

  .role-groups-body {
    min-height: 120px;
    max-height: 420px;
    overflow-y: auto;
  }

  .role-groups-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 32px 16px;
    color: #9ca3af;

    i {
      font-size: 32px;
      margin-bottom: 10px;
      opacity: 0.5;
    }

    p {
      margin: 0;
      font-size: 13px;
    }
  }

  .role-groups-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .role-group-item {
    padding: 12px 14px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #fff;

    .role-group-head {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .role-group-name {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .role-group-status {
      flex-shrink: 0;
      display: inline-flex;
      align-items: center;
      padding: 1px 8px;
      border-radius: 10px;
      font-size: 12px;
      font-weight: 600;

      &.is-normal {
        background: #f0f9eb;
        color: #67c23a;
        border: 1px solid #e1f3d8;
      }

      &.is-disabled {
        background: #fdf6ec;
        color: #e6a23c;
        border: 1px solid #faecd8;
      }

      &.is-unknown {
        background: #f4f4f5;
        color: #909399;
        border: 1px solid #e9e9eb;
      }
    }

    .role-group-count {
      flex-shrink: 0;
      margin-left: auto;
      font-size: 12px;
      color: #909399;
      font-variant-numeric: tabular-nums;
    }

    .role-group-desc {
      margin: 6px 0 0;
      font-size: 12px;
      color: #909399;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .role-group-perms {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 8px;
    }
  }
}
</style>
